package person.shw.gateway.exception;

import person.shw.gateway.constant.ResultCode;

/**
 * @author shihaowei
 * @date 2020/7/9 4:33 下午
 */
public class GatewayServiceException extends RuntimeException{

    private final int rtn;

    public GatewayServiceException() {
        super(ResultCode.ERROR.getMessage());
        this.rtn = ResultCode.ERROR.getRtn();
    }

    public GatewayServiceException(String message) {
        super(message);
        this.rtn = ResultCode.ERROR.getRtn();
    }

    public GatewayServiceException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.rtn = resultCode.getRtn();
    }


    public GatewayServiceException(ResultCode resultCode, String message) {
        super(message);
        this.rtn = resultCode.getRtn();
    }
}
