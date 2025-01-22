package mos.e6kb.workflow.exception;

/**
 * 业务异常类
 * @author ly
 * @since 2025/1/21
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
