package yuan.thymeleaf.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@NoArgsConstructor
@XmlRootElement
public class WordToConstant {

    /**
     * 内容
     */
    private String content;
}