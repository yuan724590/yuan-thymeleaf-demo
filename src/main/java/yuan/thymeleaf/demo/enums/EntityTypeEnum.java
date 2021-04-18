package yuan.thymeleaf.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yuan
 * @Date 2021/4/18 20:27
 */
@Getter
@AllArgsConstructor
public enum EntityTypeEnum {

    /**
     * 默认类型
     */
    DEFAULT_TYPE((byte) 0),

    /**
     * 需要跳过, 不执行
     */
    SKIP_TYPE((byte) 1),

    /**
     * 需要在之后额外新增
     */
    AFTER_EXTRA_TYPE((byte) 2),
    ;

    private Byte type;
}
