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

    private Integer startTime;
}
