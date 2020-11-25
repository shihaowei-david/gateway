package person.shw.gateway.constant;

import person.shw.gateway.dto.ResultDTO;

/**
 * @author shihaowei
 * @date 2020/7/9 4:18 下午
 */
public enum  ResultCode {

    FAILED(-1, "操作失败"),
    SUCCESS(0, "操作成功"),
    INVALID_VERIFY_CODE(10, "无效的验证码"),
    INVALID_PARAM(13, "无效的参数"),
    INVALID_REQUEST(400, "无效的请求"),
    UNAUTHORIZED(401, "身份未认证"),
    NO_PERMISSION(403, "没有访问权限"),
    URL_NOT_FOUND(404, "无效的访问地址"),
    ERROR(500, "服务器发生异常");

    private final int rtn;
    private final String message;

    ResultCode(int rtn, String message) {
        this.rtn = rtn;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getRtn() {
        return rtn;
    }

    public <T> ResultDTO<T> toResultDTO() {
        return new ResultDTO<>(this.rtn, this.message, null);
    }

    public <T> ResultDTO<T> toResultDTO(T data) {
        return new ResultDTO<>(this.rtn, this.message, data);
    }

    public <T> ResultDTO<T> toResultDTO(T data, String msg) {
        return new ResultDTO<>(this.rtn, (msg == null) ? this.message : msg, data);
    }
}
