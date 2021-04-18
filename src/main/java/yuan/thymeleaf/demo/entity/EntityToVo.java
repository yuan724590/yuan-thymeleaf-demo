package yuan.thymeleaf.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import yuan.thymeleaf.demo.enums.EntityTypeEnum;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@NoArgsConstructor
@XmlRootElement
public class EntityToVo {

    /**
     * 内容
     */
    private String content;

    /**
     * vo的文本
     */
    private StringBuilder stringBuilder;

    /**
     * 处理类型的美剧
     */
    private EntityTypeEnum enums;

    /**
     * 额外的拓展字段
     */
    private String extra;
}