package yuan.thymeleaf.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Goods {

    private Long id;

    private Integer yuyueTime;

    private String skuUuid;
}
