package yuan.thymeleaf.demo.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import yuan.thymeleaf.demo.entity.CompareJson;
import yuan.thymeleaf.demo.service.ToolsService;

@Service
public class ToolsServiceImpl implements ToolsService {

    @Override
    public void compare(Model model, CompareJson compare) {
        JSONObject json1 = parseJson(compare.getContent1());
        JSONObject json2 = parseJson(compare.getContent2());
        if(json1.isEmpty() || json2.isEmpty()){
            //认为不是json, 进行对比文本
            compareContent(model, compare);
        }else{
            compareJson(model, compare, json1, json2);
        }
    }

    /**
     * 对比json
     */
    private void compareJson(Model model, CompareJson compare, JSONObject json1, JSONObject json2){
        //去除格式化
        removeFormat(json1, compare.getFormat1());
        removeFormat(json2, compare.getFormat2());
        //对比去除格式化的json
        StringBuilder stringBuilder1 = new StringBuilder();
        StringBuilder stringBuilder2 = new StringBuilder();
        compareObj(stringBuilder1, json1, stringBuilder2, json2);
    }

    /**
     * 对比去除格式化的json
     */
    private void compareObj(StringBuilder stringBuilder1, Object obj1, StringBuilder stringBuilder2, Object obj2){
        if(obj1 instanceof JSONObject && obj2 instanceof JSONObject){
            //处理json的对比
            processJsonObj(stringBuilder1, (JSONObject)obj1, stringBuilder2, (JSONObject)obj2);
        }else if(obj1 instanceof JSONArray && obj2 instanceof JSONArray){
            processJsonArray(stringBuilder1, (JSONArray)obj1, stringBuilder2, (JSONArray)obj2);
        }else{
            addClassLabel(stringBuilder1, String.valueOf(obj1), !String.valueOf(obj1).equals(String.valueOf(obj2)));
            addClassLabel(stringBuilder2, String.valueOf(obj2), !String.valueOf(obj1).equals(String.valueOf(obj2)));
        }
    }

    /**
     * 处理jsonArray的对比
     */
    private void processJsonArray(StringBuilder stringBuilder1, JSONArray json1, StringBuilder stringBuilder2, JSONArray json2){
        for(int i = 0; i < json1.size(); i++){
            if(json2.size() < i + 1){
                //如果json2的长度比json1短, 不进行比较, 直接打印
                addClassLabel(stringBuilder1, JSONObject.toJSONString(json1.get(i)), true);
                addClassLabel(stringBuilder2, "", true);
                continue;
            }
            compareObj(stringBuilder1, json1, stringBuilder2, json2);
        }
        //补充json1中没有的
        if(json1.size() < json2.size()){
            for(int i = json1.size() - 1; i < json2.size(); i++){
                addClassLabel(stringBuilder1, "", true);
                addClassLabel(stringBuilder2, JSONObject.toJSONString(json2.get(i)), true);
            }
        }
    }

    /**
     * 处理json的对比
     */
    private void processJsonObj(StringBuilder stringBuilder1, JSONObject json1, StringBuilder stringBuilder2, JSONObject json2){
        boolean flag;
        for(String key : json1.keySet()){
            flag = !json2.containsKey(key);
            addClassLabel(stringBuilder1, key + ":" + json1.getString(key), flag);
            String row = flag ? key + ":" + json2.getString(key) : "";
            addClassLabel(stringBuilder2, row, !json2.containsKey(key));
            if(!flag){
                //如果json2包含json1的这个key 就继续循环
                compareObj(stringBuilder1, json1.get(key), stringBuilder2, json2.get(key));
            }
        }
    }

    /**
     * 去除格式化
     */
    private void removeFormat(JSONObject json, String format){
        if(StringUtils.isNotEmpty(format)){
            String[] formats = format.split("\\.");
            for(String key : formats){
                json = json.getJSONObject(key);
            }
        }
    }

    /**
     * 解析为json
     */
    private JSONObject parseJson(String content){
        try{
            return JSONObject.parseObject(content);
        }catch (Exception e){
            e.getStackTrace();
        }
        return new JSONObject();
    }

    /**
     * 对比文本
     */
    private void compareContent(Model model, CompareJson compare){
        String[] rows1 = getRows(compare.getContent1());
        String[] rows2 = getRows(compare.getContent2());

        StringBuilder stringBuilder1 = new StringBuilder();
        StringBuilder stringBuilder2 = new StringBuilder();
        for(int i = 0; i < rows1.length; i++){
            addClassLabel(stringBuilder1, rows1[i], rows1[i].equals(rows2[i]));
            addClassLabel(stringBuilder2, rows2[i], rows1[i].equals(rows2[i]));
        }
        model.addAttribute("result1", stringBuilder1.toString());
        model.addAttribute("result2", stringBuilder2.toString());
    }

    /**
     * 增加标签的封装
     */
    private void addClassLabel(StringBuilder stringBuilder, String row, boolean flag){
        if(flag){
            stringBuilder.append("<p><span>").append(row).append("</span></p>");
        }else{
            stringBuilder.append("<p><span class=red>").append(row).append("</span></p>");
        }
    }

    /**
     * 将文本按照回车切割为string数组
     */
    private String[] getRows(String content){
        return content.split("\r\n");
    }
}
