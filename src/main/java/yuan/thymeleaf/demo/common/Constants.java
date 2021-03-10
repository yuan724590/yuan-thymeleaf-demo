package yuan.thymeleaf.demo.common;

public class Constants {

    /**
     * 忽略值类型
     */
    public static final Byte IS_IGNORE_VALUE_TYPE = 1;

    /**
     * 区分值类型
     */
    public static final Byte IS_NOT_IGNORE_VALUE_TYPE = 0;

    /**
     * 占位符
     */
    public static final String PLACEHOLDER = "$$$";
    public static final String PLACEHOLDER_REGEX = "\\$\\$\\$";
    public static final String TAB_SPACE = "\t";

    /**
     * \t tab 转义为html
     */
    public static final String TAB_ESCAPE = "&nbsp;&nbsp;&nbsp;&nbsp;";
    public static final String ESCAPE = "&nbsp;";

    /**
     * 标签的后缀
     */
    public static final String JSON_LABEL_SUFFIX = "</span><br/>";

    /**
     * 标签加颜色
     */
    public static final String SPAN_LABEL_CLASS_RED = "<span class=red>";
    public static final String SPAN_LABEL = "<span>";
}
