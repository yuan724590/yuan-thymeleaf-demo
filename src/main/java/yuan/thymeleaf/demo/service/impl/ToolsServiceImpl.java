package yuan.thymeleaf.demo.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import yuan.thymeleaf.demo.common.Constants;
import yuan.thymeleaf.demo.entity.CompareJson;
import yuan.thymeleaf.demo.service.ToolsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ToolsServiceImpl implements ToolsService {

    /**
     * 是否忽略值类型
     */
    private static Byte isIgnoreValueType;

    /**
     * key列表,用于区分不合法部分
     */
    private static List<String> keyList;

    @Override
    public void compare(Model model, CompareJson compare) {
        isIgnoreValueType = compare.getIsIgnoreValueType();
        JSONObject json1 = parseJson(compare.getContent1());
        JSONObject json2 = parseJson(compare.getContent2());
        if(json1.isEmpty() || json2.isEmpty()){
            //认为不是json, 进行对比文本
            compareContent(model, compare);
        }else{
            //对比json
            compareJson(model, compare, json1, json2);
        }
    }

    /**
     * 对比json
     */
    private void compareJson(Model model, CompareJson compare, JSONObject jsonObject1, JSONObject jsonObject2){
        //用于统计不相符的key
        keyList = new ArrayList<>();
        //去除格式化
        JSONObject json1 = removeFormat(jsonObject1, compare.getFormat1());
        JSONObject json2 = removeFormat(jsonObject2, compare.getFormat2());
        //对比去除格式化的json
        compareObj(json1, json2, "");
        //处理json展示, json的格式化后展示出来
        model.addAttribute("result1", labelStyle(compare.getContent1(), compare.getFormat1()));
        model.addAttribute("result2", labelStyle(compare.getContent2(), compare.getFormat2()));
    }

    /**
     * 组装标签样式
     */
    private String labelStyle(String content, String format){
        String[] content1Arr = content.split("\r\n");
        Map<Integer, Boolean> map = new HashMap<>();
        for(Integer index : getIndexList(content, format)){
            map.put(index, true);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < content1Arr.length; i++){
            content1Arr[i] = content1Arr[i].replaceAll("\t", Constants.TAB_ESCAPE).replaceAll(" ", Constants.ESCAPE);
            if(map.containsKey(i)){
                stringBuilder.append(Constants.SPAN_LABEL_CLASS_RED).append(content1Arr[i]).append(Constants.JSON_LABEL_SUFFIX);
            }else{
                stringBuilder.append(Constants.SPAN_LABEL).append(content1Arr[i]).append(Constants.JSON_LABEL_SUFFIX);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 获取所有需要标红的索引
     */
    private List<Integer> getIndexList(String content, String format){
        String[] content1Arr = content.split("\r\n");
        List<Integer> indexList = new ArrayList<>();
        for(String keys : keyList) {
            //获取key数组
            String[] keyArr = getKeyArray(keys, format);
            int index = 0;
            List<Integer> list;
            for(int i = 0; i < keyArr.length; i++){
                list = calcStylePosition(content1Arr, keyArr[i], i == 0 ? "" : keyArr[i - 1], index);
                if(list.size() > 1){
                    indexList.addAll(list);
                }else if(list.size() > 0){
                    index = list.get(0);
                }else{
                    index = -1;
                }
            }
            if(index > 0){
                //该key在json中的行数
                indexList.add(index);
            }
        }
        return indexList;
    }

    /**
     * 获取key数组
     */
    private String[] getKeyArray(String keys, String format){
        String[] keyArr = keys.split(Constants.PLACEHOLDER_REGEX);
        if(StringUtils.isEmpty(format)){
            return keyArr;
        }
        String[] formats = format.split("\\.");
        String[] keyArray = new String[formats.length + keyArr.length];
        System.arraycopy(formats, 0, keyArray, 0, formats.length);
        System.arraycopy(keyArr, 0, keyArray, formats.length, keyArr.length);
        return keyArray;

    }

    /**
     * 计算key出现的位置
     */
    private List<Integer> calcStylePosition(String[] contentArr, String key, String lastKey, int index){
        List<Integer> indexList = new ArrayList<>();
        for (int i = index; i < contentArr.length; i++) {
            if(StringUtils.isNotEmpty(key) && contentArr[i].trim().contains("\"" + key + "\":")){
                indexList.add(i);
                return indexList;
            }else if(key.startsWith("[") && key.endsWith("]")){
                //如果是数组类型
                int arrayIndex = Integer.valueOf(key.replace("[", "").replace("]", ""));
                //数量
                List<Integer> list = getIndexRange(arrayIndex, i, contentArr);
                if(list.size() > 1){
                    for(int j = list.get(0); j <= list.get(1); j++){
                        indexList.add(j);
                    }
                }
                return indexList;
            }
        }
        return indexList;
    }

    /**
     * 获取行数范围
     */
    private List<Integer> getIndexRange(int arrayIndex, int i, String[] contentArr){
        int startIndex = 0;
        int endIndex = 0;
        int count = 0;
        //大括号数量
        int leftCount = 0;
        int rightCount = 0;
        //中括号数量
        int leftCounts = 0;
        int rightCounts = 0;
        char[] content;
        boolean flag = false;
        boolean exitFlag = false;
        List<Integer> list = new ArrayList<>();
        for (int j = i; j < contentArr.length; j++) {
            content = contentArr[j].toCharArray();
            for(int l = 0; l < content.length; l++){
                if("[".equals(String.valueOf(content[l]))){
                    leftCounts++;
                }else if("]".equals(String.valueOf(content[l]))){
                    rightCounts++;
                    exitFlag = true;
                }else if("{".equals(String.valueOf(content[l]))){
                    leftCount++;
                    flag = true;
                }else if("}".equals(String.valueOf(content[l]))){
                    rightCount++;
                }
                if(exitFlag && leftCounts == rightCounts){
                    //中括号数量相同, 说明数组结束
                    return list;
                }
                if(flag && leftCount > 0 && leftCount == rightCount){
                    count++;
                    flag = false;
                    if(arrayIndex == 0){
                        list.add(i + 1);
                        list.add(j - 1);
                        return list;
                    }else if(count == arrayIndex){
                        list.add(j + 2);
                    }else if(count - 1 == arrayIndex){
                        list.add(j - 1);
                        return list;
                    }
                }
            }
        }
        return list;
    }

    /**
     * 计算某字符数量
     */
    private Integer charCount(String content, String regex){
        return content.length() - content.replaceAll(regex, "").length();
    }

    /**
     * 对比去除格式化的json
     */
    private void compareObj(Object obj1, Object obj2, String key){
        if(obj1 instanceof JSONObject && obj2 instanceof JSONObject){
            //处理json的对比
            processJsonObj((JSONObject)obj1, (JSONObject)obj2, key);
        }else if(obj1 instanceof JSONArray && obj2 instanceof JSONArray){
            processJsonArray((JSONArray)obj1, (JSONArray)obj2, key);
        }else{
            //判断值是否相同
            if(judgmentValue(obj1, obj2)){
                //如果相同就跳过
                return;
            }
            keyList.add(key);
        }
    }

    /**
     * 判断值是否相同
     */
    private Boolean judgmentValue(Object obj1, Object obj2){

        boolean flag = String.valueOf(obj1).equals(String.valueOf(obj2));

        if(Constants.IS_IGNORE_VALUE_TYPE.equals(isIgnoreValueType)){
            //忽略值类型
            return flag;
        }else{
            //不忽略值类型,还需要判断className
            return flag && obj1.getClass().getName().equals(obj2.getClass().getName());
        }
    }

    /**
     * 处理jsonArray的对比
     */
    private void processJsonArray(JSONArray json1, JSONArray json2, String key){
        for(int i = 0; i < json1.size(); i++){
            if(json2.size() < i + 1){
                //如果json2的长度不够, 不进行比较, 直接打印
                keyList.add(key);
                continue;
            }
            compareObj(json1.get(i), json2.get(i), key + Constants.PLACEHOLDER + "[" + i + "]");
        }
        if(json2.size() > json1.size()){
            for(int i = json1.size(); i < json2.size(); i++){
                keyList.add(key + Constants.PLACEHOLDER + "[" + i + "]");
            }
        }
    }

    /**
     * 处理json的对比
     */
    private void processJsonObj(JSONObject json1, JSONObject json2, String keys){
        String objKey;
        for(String key : json1.keySet()){
            if(json2.containsKey(key)){
                //如果json2包含json1的这个key 就继续循环
                objKey = !"".equals(keys) ? keys + Constants.PLACEHOLDER + key : key;
                compareObj(json1.get(key), json2.get(key), objKey);
            }else{
                objKey = !"".equals(keys) ? keys + Constants.PLACEHOLDER + key : key + ":" + json1.getString(key);
                keyList.add(objKey);
            }
        }
    }

    /**
     * 去除格式化
     */
    private JSONObject removeFormat(JSONObject json, String format){
        if(StringUtils.isNotEmpty(format)){
            String[] formats = format.split("\\.");
            for(String key : formats){
                json = json.getJSONObject(key);
            }
        }
        return json;
    }

    /**
     * 解析为json
     */
    private JSONObject parseJson(String content){
        try{
            return JSONObject.parseObject(content.replaceAll(" ", ""));
        }catch (Exception e){
            log.error(e.getLocalizedMessage());
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
        if(rows2.length > rows1.length){
            for(int i = rows1.length; i < rows2.length; i++){
                addClassLabel(stringBuilder2, rows2[i], false);
            }
        }
        model.addAttribute("result2", stringBuilder2.toString());
    }

    /**
     * 增加标签的封装
     */
    private void addClassLabel(StringBuilder stringBuilder, String row, boolean flag){
        if(flag){
            stringBuilder.append("<span>").append(row).append("</span><br/>");
        }else{
            stringBuilder.append("<span class=red>").append(row).append("</span><br/>");
        }
    }

    /**
     * 将文本按照回车切割为string数组
     */
    private String[] getRows(String content){
        return content.split("\r\n");
    }
}
