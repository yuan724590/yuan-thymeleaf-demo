package yuan.thymeleaf.demo.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import yuan.thymeleaf.demo.common.Constants;
import yuan.thymeleaf.demo.entity.CompareJson;
import yuan.thymeleaf.demo.entity.EntityToVo;
import yuan.thymeleaf.demo.entity.Judgment;
import yuan.thymeleaf.demo.entity.WordToConstant;
import yuan.thymeleaf.demo.enums.EntityTypeEnum;
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
        String[] content1Arr = content.split(Constants.LINE_FEED);
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
        String[] content1Arr = content.split(Constants.LINE_FEED);
        List<Integer> indexList = new ArrayList<>();
        boolean flag = true;
        for(String keys : keyList) {
            //获取key数组
            String[] keyArr = getKeyArray(keys, format);
            int index = 0;
            List<Integer> list;
            for(int i = 0; i < keyArr.length; i++){
                list = calcStylePosition(content1Arr, keyArr[i], i + 1 < keyArr.length ? keyArr[i + 1] : keyArr[i], index);
                if(list.size() > 1){
                    indexList.addAll(list);
                    flag = false;
                }else if(list.size() > 0){
                    index = list.get(0);
                }else{
                    index = 0;
                    break;
                }
            }
            if(flag && index > 0){
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
    private List<Integer> calcStylePosition(String[] contentArr, String key, String nextKey, int index){
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
                    if(nextKey.equals(key)){
                        //如果下标为最后一个, 则把整个对象都标识出来
                        for(int j = list.get(0); j <= list.get(1); j++){
                            indexList.add(j);
                        }
                    }else{
                        for (int l = list.get(0); l <= list.get(1); l++) {
                            if(contentArr[l].trim().contains("\"" + nextKey + "\":")){
                                indexList.add(l);
                                break;
                            }
                        }
                    }
                }else{
                    indexList = new ArrayList<>();
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
                switch (String.valueOf(content[l])){
                    case "[":
                        leftCounts++;
                        break;
                    case "]":
                        rightCounts++;
                        exitFlag = true;
                        break;
                    case "{":
                        leftCount++;
                        flag = true;
                        break;
                    case "}":
                        rightCount++;
                        break;
                    default:
                        break;
                }
                if(exitFlag && leftCounts == rightCounts){
                    //中括号数量相同, 说明数组结束
                    return list;
                }
                if(flag && leftCount > 0 && leftCount == rightCount){
                    count++;
                    flag = false;
                    //这里直接操作数字的原因是偷懒了, 实际上没必要写的那么通用
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
            stringBuilder.append(Constants.SPAN_LABEL).append(row).append(Constants.JSON_LABEL_SUFFIX);
        }else{
            stringBuilder.append(Constants.SPAN_LABEL_CLASS_RED).append(row).append(Constants.JSON_LABEL_SUFFIX);
        }
    }

    /**
     * 将文本按照回车切割为string数组
     */
    private String[] getRows(String content){
        return content.split(Constants.LINE_FEED);
    }

    @Override
    public void compareOld(Model model, CompareJson compare) {
        isIgnoreValueType = compare.getIsIgnoreValueType();
        JSONObject json1 = parseJson(compare.getContent1());
        JSONObject json2 = parseJson(compare.getContent2());
        if(json1.isEmpty() || json2.isEmpty()){
            //认为不是json, 进行对比文本
            compareContent(model, compare);
        }else{
            //对比json
            compareJsonOld(model, compare, json1, json2);
        }
    }

    /**
     * 对比json
     */
    private void compareJsonOld(Model model, CompareJson compare, JSONObject json1, JSONObject json2){
        //去除格式化
        json1 = removeFormat(json1, compare.getFormat1());
        json2 = removeFormat(json2, compare.getFormat2());
        //对比去除格式化的json
        StringBuilder stringBuilder1 = new StringBuilder();
        StringBuilder stringBuilder2 = new StringBuilder();
        compareObj(stringBuilder1, json1, stringBuilder2, json2, "");
        //json的格式化后展示出来
        model.addAttribute("result1", stringBuilder1.toString());
        model.addAttribute("result2", stringBuilder2.toString());
    }

    /**
     * 对比去除格式化的json
     */
    private void compareObj(StringBuilder stringBuilder1, Object obj1, StringBuilder stringBuilder2, Object obj2, String key){
        if(obj1 instanceof JSONObject && obj2 instanceof JSONObject){
            //处理json的对比
            processJsonObj(stringBuilder1, (JSONObject)obj1, stringBuilder2, (JSONObject)obj2, key);
        }else if(obj1 instanceof JSONArray && obj2 instanceof JSONArray){
            processJsonArray(stringBuilder1, (JSONArray)obj1, stringBuilder2, (JSONArray)obj2, key);
        }else{
            //判断值是否相同
            Judgment judgment = judgmentValueOld(obj1, obj2);
            if(!"".equals(key)){
                String content = key + ":" + String.valueOf(obj1) + judgment.getClassName1();
                addClassLabel(stringBuilder1, content, judgment.getFlag());
                content = key + ":" + String.valueOf(obj2) + judgment.getClassName2();
                addClassLabel(stringBuilder2, content, judgment.getFlag());
            }else{
                addClassLabel(stringBuilder1, String.valueOf(obj1) + judgment.getClassName1(), judgment.getFlag());
                addClassLabel(stringBuilder2, String.valueOf(obj2) + judgment.getClassName2(), judgment.getFlag());
            }
        }
    }

    /**
     * 判断值是否相同
     */
    private Judgment judgmentValueOld(Object obj1, Object obj2){
        Judgment judgment = new Judgment();
        if(Constants.IS_IGNORE_VALUE_TYPE.equals(isIgnoreValueType)){
            //忽略大小写
            judgment.setFlag(String.valueOf(obj1).equals(String.valueOf(obj2)));
            judgment.setClassName1("");
            judgment.setClassName2("");
        }else{
            boolean flag = obj1.getClass().getName().equals(obj2.getClass().getName());
            judgment.setFlag(flag);
            if(!flag){
                String[] str1 = obj1.getClass().getName().split("\\.");
                judgment.setClassName1("(" + str1[str1.length - 1] + ")");

                String[] str2 = obj2.getClass().getName().split("\\.");
                judgment.setClassName2("(" + str2[str2.length - 1] + ")");
            }
        }
        return judgment;
    }

    /**
     * 处理jsonArray的对比
     */
    private void processJsonArray(StringBuilder stringBuilder1, JSONArray json1, StringBuilder stringBuilder2, JSONArray json2, String key){
        for(int i = 0; i < json1.size(); i++){
            if(json2.size() < i + 1){
                //如果json2的长度不够, 不进行比较, 直接打印
                addClassLabel(stringBuilder1, json1.getString(i), false);
                addClassLabel(stringBuilder2, "null", false);
                continue;
            }
            compareObj(stringBuilder1, json1.get(i), stringBuilder2, json2.get(i), key + "[" + i + "]");
        }
    }

    /**
     * 处理json的对比
     */
    private void processJsonObj(StringBuilder stringBuilder1, JSONObject json1, StringBuilder stringBuilder2, JSONObject json2, String keys){
        for(String key : json1.keySet()){
            if(json2.containsKey(key)){
                //如果json2包含json1的这个key 就继续循环
                compareObj(stringBuilder1, json1.get(key), stringBuilder2, json2.get(key), !"".equals(keys) ? keys + "." + key : key);
            }else{
                addClassLabel(stringBuilder1, !"".equals(keys) ? keys + "." + key : key + ":" + json1.getString(key), false);
                String row = !"".equals(keys) ? keys + "." + key : key + ":null";
                addClassLabel(stringBuilder2, row, false);
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

    @Override
    public void entityToVo(Model model, EntityToVo entityToVo){
        if(StringUtils.isEmpty(entityToVo.getContent())){
            return;
        }
        String[] entityArray = entityToVo.getContent().split(Constants.LINE_FEED);
        String line;

        entityToVo.setStringBuilder(new StringBuilder());
        entityToVo.setEnums(EntityTypeEnum.DEFAULT_TYPE);
        for(String entity : entityArray){
            line = entity.replaceAll("\\s", "");
            //解析entity的每一行
            entity = analysisEntity(entityToVo, line, entity);
            //处理vo的string
            processVoString(entityToVo, entity);
        }
        model.addAttribute("voContent", entityToVo.getStringBuilder().toString());
    }

    /**
     * 解析实体类的内容
     */
    private String analysisEntity(EntityToVo entityToVo, String line, String entity){
        String[] strArray;
        if(line.startsWith(Constants.COLUMN_VALUE_ANNOTATION)
                || line.startsWith(Constants.SLASH_STAR_STAR) || line.startsWith(Constants.STAR_SLASH)
                || line.startsWith(Constants.DATA_ANNOTATION) || line.startsWith(Constants.TABLE_ANNOTATION)
                || line.startsWith(Constants.GENERATED_VALUE_ANNOTATION) || line.startsWith(Constants.ID_ANNOTATION)){
            //如果是空行 || 属性 || @Column || /** || */ || @Data || @Table || @GeneratedValue || @Id就跳过
            entityToVo.setEnums(EntityTypeEnum.SKIP_TYPE);
        }else if(line.startsWith(Constants.STAR)){
            //如果是注释行, 则将内容提取出来
            strArray = entity.split(Constants.STAR_REGEX);
            entity = strArray[0] + Constants.API_MODEL_PROPERTY_ANNOTATION + strArray[1].trim() + Constants.ANNOTATION_SUFFIX;
        }else if(line.startsWith(Constants.IMPORT)){
            if(line.contains(Constants.JAVAX_PERSISTENCE) || line.contains(Constants.LOMBOK_DATA)){
                //去掉@Data和@Table所import的包名
                entityToVo.setEnums(EntityTypeEnum.SKIP_TYPE);
            }
        }else if(entity.startsWith(Constants.PUBLIC_CLASS)){
            //将文件名增加vo后缀
            entity = entity.replace(Constants.SPACE_BRACKET, Constants.VO_SPACE_BRACKET);
            entityToVo.setExtra("");
            entityToVo.setEnums(EntityTypeEnum.AFTER_EXTRA_TYPE);
        }else if(line.startsWith(Constants.PACKAGE)){
            if(entity.contains(Constants.ENTITY)){
                //将包地址移动至vo下
                strArray = entity.split(Constants.ENTITY);
                entity = strArray[0] + Constants.VO + strArray[1];
            }
            entityToVo.setExtra("import io.swagger.annotations.ApiModelProperty;");
            entityToVo.setEnums(EntityTypeEnum.AFTER_EXTRA_TYPE);
        }
        return entity;
    }

    /**
     * 处理vo的string
     */
    private void processVoString(EntityToVo entityToVo, String entity){
        switch (entityToVo.getEnums()){
            case DEFAULT_TYPE:
                //如果是默认类型
                addClassLabel(entityToVo.getStringBuilder(), entity, false);
                break;
            case SKIP_TYPE:
                //如果是跳过类型, 就只重置
                entityToVo.setEnums(EntityTypeEnum.DEFAULT_TYPE);
                break;
            case AFTER_EXTRA_TYPE:
                //如果是之后拓展类型
                addClassLabel(entityToVo.getStringBuilder(), entity, false);
                addClassLabel(entityToVo.getStringBuilder(), entityToVo.getExtra(), false);
                entityToVo.setEnums(EntityTypeEnum.DEFAULT_TYPE);
            default:
                break;
        }
    }

    @Override
    public String wordToConstant(WordToConstant wordToConstant){
        if(StringUtils.isEmpty(wordToConstant.getContent())){
            return wordToConstant.getContent();
        }
        return wordToConstant.getContent().replaceAll("[A-Z]", "_$0").toUpperCase();
    }
}
