package yuan.thymeleaf.demo.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.util.DateUtils;
import yuan.thymeleaf.demo.entity.Start;
import yuan.thymeleaf.demo.utils.HttpUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * DogD
 */
@Controller
@Slf4j
public class DogDController {

    @GetMapping("/start")
    public String start(Model model){
        Start start = new Start();
        model.addAttribute("start", start);
        return "start";
    }

    @PostMapping("/start")
    public String start(Model model, @ModelAttribute("compare") Start start){
        try {
            long nowTime = System.currentTimeMillis();
            long startTime = (long)start.getStartTime() * 1000;
            if(startTime >= nowTime){
                while (true) {
                    nowTime = System.currentTimeMillis();
                    long diff = Math.abs(startTime - nowTime);
                    if(diff < 3000){
                        checkAll(start.getCookie());
                        getOrderInfo(start.getCookie());
                    }
                    if(startTime <= nowTime){
                        String result = submitOrder(start.getCookie(), start.getEid(), start.getFp());
                        JSONObject jsonObject = JSONObject.parseObject(result);
                        model.addAttribute("orderId", jsonObject.getString("orderId"));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("start", start);
        return "start";
    }

    private String submitOrder(String cookie, String eid, String fp){
        long t1 = System.currentTimeMillis();
        String result = "";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("submitOrderParam.eid",eid)
                    .addFormDataPart("submitOrderParam.fp",fp).build();
            Request request = new Request.Builder()
                    .url("https://trade.jd.com/shopping/order/submitOrder.action?&presaleStockSign=1")
                    .method("POST", body)
                    .addHeader("Cookie", cookie)
                    .addHeader("Referer", "https://trade.jd.com/shopping/order/getOrderInfo.action")
                    .build();
            Response response = client.newCall(request).execute();
            result = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        t1 = System.currentTimeMillis() - t1;
        log.info("请求submitOrder,耗时:{}, 结果为:{}", t1, result);
        return result;
    }

    private void getOrderInfo(String cookie){
        long t1 = System.currentTimeMillis();
        String result = "";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url("https://trade.jd.com/shopping/order/getOrderInfo.action")
                    .method("GET", null)
                    .addHeader("Referer", "https://cart.jd.com/")
                    .addHeader("Host", "trade.jd.com")
                    .addHeader("Cookie", cookie)
                    .build();
            Response response = client.newCall(request).execute();
            result = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        t1 = System.currentTimeMillis() - t1;
        log.info("请求getOrderInfo,耗时:{}, 结果为:{}", t1, result);
    }

    private void checkAll(String cookie){
        long t1 = System.currentTimeMillis();
        String checkAll = "http://api.m.jd.com/api?functionId=pcCart_jc_cartCheckAll&appid=JDC_mall_cart" +
                "&loginType=3&body=%7B%22serInfo%22:%7B%22area%22:%2219_1607_3155_62120%22,%22user-key%22:%22ae3ec562-a1a4-4b11-8ae7-2501f2522618%22%7D%7D";
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("cookie", cookie);
        headerMap.put("referer", "https://cart.jd.com/");
        String result = HttpUtils.doGet(checkAll, headerMap);
        t1 = System.currentTimeMillis() - t1;
        log.info("请求checkAll,耗时:{}, 结果为:{}", t1, result);
    }
}
