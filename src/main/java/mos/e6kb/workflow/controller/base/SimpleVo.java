package mos.e6kb.workflow.controller.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author ly
 * @since 2025/1/20
 */
@Data
@Accessors(chain = true)
public class SimpleVo<T> {

    /**
     * 返回数据
     */
    private T data;

    public SimpleVo(T data) {
        this.data = data;
    }

    public static <T> SimpleVo<T> ok(T data) {
        return new SimpleVo<>(data);
    }

}
