package person.shw.gateway.exception;

/**
 * @author shihaowei
 * @date 2020/7/9 2:11 下午
 */
public class GatewayAPITimeoutException extends RuntimeException{


    public GatewayAPITimeoutException() {
    }

    public GatewayAPITimeoutException(String message) {
        super(message);
    }

    public GatewayAPITimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
