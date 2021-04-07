package yuan.thymeleaf.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@NoArgsConstructor
@XmlRootElement
public class Start {

    private String cookie;

    private String eid;

    private String fp;

    /**
     * 开始执行时间
     */
    private Integer startTime;

    private String userKey;

    private String area;
}
