package yuan.thymeleaf.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@Data
@NoArgsConstructor
@XmlRootElement // mediatype 转为xml
public class User {

    private Long id;

    private String name;

    private Integer age;

    private Date birthday;
}
