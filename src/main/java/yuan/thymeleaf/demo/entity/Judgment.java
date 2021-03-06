package yuan.thymeleaf.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@NoArgsConstructor
@XmlRootElement
public class Judgment {

    /**
     * 是否相同
     */
    private Boolean flag;

    /**
     * 内容1的class类型
     */
    private String className1;

    /**
     * 内容2的class类型
     */
    private String className2;
}
