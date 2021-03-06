package yuan.thymeleaf.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@NoArgsConstructor
@XmlRootElement
public class CompareJson {

    /**
     * 忽略值类型
     */
    private Byte isIgnoreValueType;

    /**
     * 原始内容
     */
    private String content1;

    /**
     * 原始内容格式
     */
    private String format1;

    /**
     * 对比内容结果
     */
    private String result1;

    /**
     * 对比内容
     */
    private String content2;

    /**
     * 对比内容格式
     */
    private String format2;

    /**
     * 对比内容结果
     */
    private String result2;
}
