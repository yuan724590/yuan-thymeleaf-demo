package yuan.thymeleaf.demo.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
        String result1 = "";
        String result2 = "";
        String result3 = "";
        try {
            long nowTime = System.currentTimeMillis();
            long startTime = (long)start.getStartTime() * 1000;
            if(startTime >= nowTime){
                //准备抢跑
                while (true){
                    nowTime = System.currentTimeMillis();
                    if(startTime <= nowTime){
                        result1 = checkAll(start.getCookie());
                        result2 = getOrderInfo(start.getCookie());
                        result3 = submitOrder(start.getCookie(), start.getEid(), start.getFp());
                        JSONObject jsonObject = JSONObject.parseObject(result3);
                        model.addAttribute("orderId", jsonObject.getString("orderId"));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("result1结果是:{}", result1);
        log.info("result2结果是:{}", result2);
        log.info("result3结果是:{}", result3);
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
        log.info("请求submitOrder,耗时:{}", t1);
        return result;
    }

    private String getOrderInfo(String cookie){
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
        log.info("请求getOrderInfo,耗时:{}", t1);
        return result;
    }

    private String checkAll(String cookie){
        long t1 = System.currentTimeMillis();
        int i = 0;
        String result;
        while(true){
            i++;
            String checkAll = "http://api.m.jd.com/api?functionId=pcCart_jc_cartCheckAll&appid=JDC_mall_cart" +
                    "&loginType=3&body=%7B%22serInfo%22:%7B%22area%22:%2219_1607_3155_62120%22,%22user-key%22:%22ae3ec562-a1a4-4b11-8ae7-2501f2522618%22%7D%7D";
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("cookie", cookie);
            headerMap.put("referer", "https://cart.jd.com/");
            result = HttpUtils.doGet(checkAll, headerMap);
            int num = JSONObject.parseObject(result).getJSONObject("resultData").getJSONObject("cartInfo").getIntValue("checkedWareNum");
            if(num > 0){
                break;
            }
        }

        t1 = System.currentTimeMillis() - t1;
        log.info("请求checkAll,耗时:{}, num:{}", t1, i);
        return result;
    }
}
