package yuan.thymeleaf.demo.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import yuan.thymeleaf.demo.common.Constants;
import yuan.thymeleaf.demo.entity.Goods;
import yuan.thymeleaf.demo.entity.Start;
import yuan.thymeleaf.demo.utils.CommonUtils;
import yuan.thymeleaf.demo.utils.HttpUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


/**
 * DogD
 */
@Controller
@Slf4j
public class DogDController {

    private static ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

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
                    if(startTime - 2000 <= nowTime) {
                        nowTime = System.currentTimeMillis();
                        result1 = checkAll(start.getCookie());
                        result2 = getOrderInfo(start.getCookie());
                    }
                    if(startTime <= nowTime){
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

    /**
     * 提交订单
     */
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        t1 = System.currentTimeMillis() - t1;
        log.info("请求submitOrder,耗时:{}", t1);
        return result;
    }

    /**
     * 获取订单信息
     */
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

    /**
     * 购物车列表全选
     */
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

    @GetMapping("/start1")
    public String start1(Model model){
        Start start = new Start();
        model.addAttribute("start", start);
        return "start1";
    }

    @PostMapping("/start1")
    public String start1(Model model, @ModelAttribute("compare") Start start){
        start.setFp(StringUtils.isEmpty(start.getFp()) ? "4084a70f7affa8e9f0b4f13f88404fea" : start.getFp());
        start.setEid(StringUtils.isEmpty(start.getEid())
                ? "ARZW6MLEMMWNWAKN2NXCMHWSOQ6XSRVKBESH6ANAVDAH4FLWXJHXPNFO67CX7JPLRM7IW6WR6GBLKPH7JK525TLD4Y" : start.getEid());
        start.setArea(StringUtils.isEmpty(start.getArea()) ? "19_1607_3155_0" : start.getArea());
        start.setUserKey(StringUtils.isEmpty(start.getUserKey()) ? "0ecd9ce8-b490-4660-9ed1-74371bdd6026" : start.getUserKey());
        String result2 = "";
        String result3 = "";
        boolean flag = false;
        Map<Integer, List<Goods>> map;
        List<Future> futureList;
        List<String> resultList;
        List<Goods> goodList;
        JSONObject jsonObject;
        while(true){
            goodList = retryGoodList(start.getCookie());
            if(goodList == null){
                CommonUtils.sendMail("登录态异常， 请重新登录");
                return "";
            }
            map = goodList.stream().collect(Collectors.groupingBy(Goods::getYuyueTime));
            boolean isGetOrderInfo = false;
            while(true){
                for(int yuyueTime : map.keySet()){
                    if(yuyueTime <= CommonUtils.getNowTime()){
                        result3 = submitOrder(start.getCookie(), start.getEid(), start.getFp());
                        jsonObject = JSONObject.parseObject(result3);
                        CommonUtils.sendMail(jsonObject.getString("orderId"));
                        flag = true;
                        break;
                    }
                    if(!isGetOrderInfo && yuyueTime - CommonUtils.getNowTime() < 30 && yuyueTime - CommonUtils.getNowTime() > 1){
                        futureList = new ArrayList<>();
                        //进行选择
                        for(Goods good : map.get(yuyueTime)){
                            futureList.add(syncSingleCheck(start.getCookie(), good.getId(), start, good.getSkuUuid()));
                        }
                        resultList = new ArrayList<>();
                        for(Future future : futureList){
                            try {
                                resultList.add(future.get().toString());
                            } catch (Exception e) {
                                e.getMessage();
                            }
                        }
                        log.info("singleCheck结果为:" + JSONObject.toJSONString(resultList));
                        result2 = getOrderInfo(start.getCookie());
                    }
                }
                isGetOrderInfo = true;
                if(flag || map.isEmpty()){
                    break;
                }
            }
            if(StringUtils.isNotEmpty(result2)){
                log.info("getOrderInfo结果为:" + result2);
            }
            if(StringUtils.isNotEmpty(result3)) {
                log.info("submitOrder结果为:" + result3);
            }
            threadSleep(5000);
        }
    }

    /**
     * 开线程进行额外处理
     */
    private void syncExtraProcess(String cookie, Long id, Start start, String skuUuid){
        try {
            Future future = cachedThreadPool.submit(() -> {
                //进行单选
                return singleCheck(cookie, id, start, skuUuid);
            });
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始处理茅台逻辑
     */
    private void bugMaoTai(String cookie){
        while(true){
            //获取预约开始时间
            int countdown = getYuyueStartTime(cookie);
            //等待预约开始
            threadSleep(countdown + 5);
        }
    }

    /**
     * 获取预约结束时间
     */
    private int getYuyueStartTime(String cookie){
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("https://item-soa.jd.com/getWareBusiness?callback=jQuery8163912&skuId=100012043978" +
                            "&cat=12259%2C12260%2C9435&area=19_1607_3155_62120&shopId=1000085463&venderId=1000085463" +
                            "&paramJson=%7B%22platform2%22%3A%221%22%2C%22specialAttrStr%22%3A%22p0pp1pppppppppppppppppp%22%2C%22skuMarkStr%22%3A%2200%22%7D")
                    .method("GET", null)
                    .addHeader("cookie", cookie)
                    .build();
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            result = result.substring(13, result.length() - 2);
            JSONObject jsonObject = JSONObject.parseObject(result);
            return jsonObject.getJSONObject("yuyueInfo").getIntValue("countdown");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 开线程获取进行单选
     */
    private Future syncSingleCheck(String cookie, Long id, Start start, String skuUuid){
        Future future = null;
        try {
            future = cachedThreadPool.submit(() -> {
                //进行单选
                return singleCheck(cookie, id, start, skuUuid);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return future;
    }

    /**
     * 进行单选
     */
    private String singleCheck(String cookie, Long id, Start start, String skuUuid){
        String result = "";
        long t1 = System.currentTimeMillis();
        try {
            while(true){
                OkHttpClient client = new OkHttpClient().newBuilder().build();
                MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                RequestBody body = RequestBody.create(mediaType, "functionId=pcCart_jc_cartCheckSingle&appid=JDC_mall_cart&body={\"operations\":[{\"TheSkus\":[{\"Id\":\""
                        + id +"\",\"num\":1,\"skuUuid\":\"" + skuUuid + "\",\"useUuid\":false}]}],\"serInfo\":{\"area\":\""
                        + start.getArea() + "\",\"user-key\":\"" + start.getUserKey() + "\"}}");
                Request request = new Request.Builder()
                        .url("http://api.m.jd.com/api")
                        .method("POST", body)
                        .addHeader("Cookie", cookie)
                        .addHeader("Host", "api.m.jd.com")
                        .addHeader("Referer", "https://cart.jd.com/")
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build();
                Response response = client.newCall(request).execute();
                result = response.body().string();
                if(JSONObject.parseObject(result).getJSONObject("resultData").getJSONObject("cartInfo").getIntValue("Price") > 0){
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        t1 = System.currentTimeMillis() - t1;
        log.info("请求singleChect,耗时:{}", t1);
        return result;
    }

    /**
     * 重试,防止时间误差
     */
    private List<Goods> retryGoodList(String cookie){
        List<Goods> goodList = new ArrayList<>();
        try {
            //获取5次
            Future future1 = getGoodFuture(cookie);
            Future future2 = getGoodFuture(cookie);
            Future future3 = getGoodFuture(cookie);
            Future future4 = getGoodFuture(cookie);
            Future future5 = getGoodFuture(cookie);
            Map<Long, Goods> map1 = (Map<Long, Goods>) future1.get();
            Map<Long, Goods> map2 = (Map<Long, Goods>) future2.get();
            Map<Long, Goods> map3 = (Map<Long, Goods>) future3.get();
            Map<Long, Goods> map4 = (Map<Long, Goods>) future4.get();
            Map<Long, Goods> map5 = (Map<Long, Goods>) future5.get();
            Map<Integer, Integer> countMap;
            for(long id : map1.keySet()){
                countMap = new HashMap<>();
                countMap.put(map1.get(id).getYuyueTime(), 1);
                countMap.put(map2.get(id).getYuyueTime(), countMap.containsKey(map2.get(id).getYuyueTime()) ? countMap.get(map2.get(id).getYuyueTime()) + 1 : 1);
                countMap.put(map3.get(id).getYuyueTime(), countMap.containsKey(map3.get(id).getYuyueTime()) ? countMap.get(map3.get(id).getYuyueTime()) + 1 : 1);
                countMap.put(map4.get(id).getYuyueTime(), countMap.containsKey(map4.get(id).getYuyueTime()) ? countMap.get(map4.get(id).getYuyueTime()) + 1 : 1);
                countMap.put(map5.get(id).getYuyueTime(), countMap.containsKey(map5.get(id).getYuyueTime()) ? countMap.get(map5.get(id).getYuyueTime()) + 1 : 1);
                int max = 0;
                int yuyueTime = 0;
                for(int time : countMap.keySet()){
                    if(countMap.get(time) > max){
                        max = countMap.get(time);
                        yuyueTime = time;
                    }
                }
                goodList.add(new Goods(id, yuyueTime, map1.get(id).getSkuUuid()));
            }
            return goodList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 开线程获取商品信息, 包含已估算服务器时间
     */
    private Future getGoodFuture(String cookie){
        Future future = null;
        try {
            future = cachedThreadPool.submit(() -> {
                //获取购物车列表
                JSONObject jsonObject = getMallCartList(cookie);
                if(jsonObject == null){
                    return Collections.emptyMap();
                }else if(jsonObject.isEmpty()){
                    return null;
                }
                //处理商品列表
                List<Goods> goods = processGoods(jsonObject.getJSONArray("vendors"), jsonObject.getIntValue("time"));
                return goods.stream().collect(Collectors.toMap(Goods::getId, a -> a));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return future;
    }

    private void threadSleep(long millis){
        try {
//            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理下商品
     */
    private List<Goods> processGoods(JSONArray jsonArray, int time){
        JSONObject jsonObject;
        JSONArray sortedArray;
        JSONObject itemJson;
        List<Goods> goodList = new ArrayList<>();
        for(int i = 0; i < jsonArray.size(); i++){
            jsonObject = jsonArray.getJSONObject(i);
            sortedArray = jsonObject.getJSONArray("sorted");
            for(int j = 0; j < sortedArray.size(); j++){
                itemJson = sortedArray.getJSONObject(j).getJSONObject("item");
                if(itemJson.get("yuyueLimitTime") == null || itemJson.getIntValue("yuyueLimitTime") > 3600 || itemJson.getIntValue("yuyueState") != 2){
                    continue;
                }
                goodList.add(new Goods(itemJson.getLongValue("Id"),
                        time + itemJson.getIntValue("yuyueLimitTime"), itemJson.getString("skuUuid")));
            }
        }
        return goodList;
    }

    /**
     * 获取购物车列表
     */
    private JSONObject getMallCartList(String cookie){
        int time;
        while(true){
            try{
                threadSleep(300000);
                OkHttpClient client = new OkHttpClient().newBuilder().build();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(mediaType, "{}");
                Request request = new Request.Builder()
                        .url("http://api.m.jd.com/api?functionId=pcCart_jc_getCurrentCart&appid=JDC_mall_cart&loginType=3&body=%7B%22serInfo%22:%7B%22area%22:%2219_1607_3155_0%22,%22user-key%22:%220ecd9ce8-b490-4660-9ed1-74371bdd6026%22%7D,%22cartExt%22:%7B%22specialId%22:1%7D%7D")
                        .method("POST", body)
                        .addHeader("Referer", " https://cart.jd.com/")
                        .addHeader("Cookie", cookie)
                        .build();
                int startTime = CommonUtils.getNowTime();
                Response response = client.newCall(request).execute();
                int endTime = CommonUtils.getNowTime();
                time = startTime + (endTime - startTime) / 2;
                String result = response.body().string();
                JSONObject jsonObject = JSONObject.parseObject(result);
                if(!jsonObject.isEmpty()){
                    if(StringUtils.isNotEmpty(jsonObject.getString("message")) && Constants.PIN_IS_NULL.equals(jsonObject.getString("message"))){
                        return new JSONObject();
                    }
                    JSONObject cartJson = jsonObject.getJSONObject("resultData").getJSONObject("cartInfo");
                    cartJson.put("time", time);
                    return cartJson;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
