package person.shw.gateway.dto;

import person.shw.gateway.constant.ResultCode;

import java.beans.Transient;
import java.io.Serializable;

/**
 * @author shihaowei
 * @date 2020/7/9 4:19 下午
 */
public class ResultDTO<T> implements Serializable {

    private static final long serialVersionUID = -6155809858596159858L;

    private int rtn;
    private String msg;
    private T data;

    public ResultDTO(int rtn, String msg, T data) {
        this.rtn = rtn;
        this.msg = msg;
        this.data = data;
    }

    public ResultDTO() {
    }

    public static <T> ResultDTO<T> ok() {
        return ResultCode.SUCCESS.toResultDTO();
    }

    public static <T> ResultDTO<T> ok(T data) {
        return ResultCode.SUCCESS.toResultDTO(data);
    }

    public static <T> ResultDTO<T> ok(T data, String msg) {
        return ResultCode.SUCCESS.toResultDTO(data, msg);
    }

    public static <T> ResultDTO<T> failed() {
        return ResultCode.FAILED.toResultDTO();
    }

    public static <T> ResultDTO<T> failed(String msg) {
        return ResultCode.FAILED.toResultDTO(null, msg);
    }

    public static <T> ResultDTO<T> failed(int rtn, String msg) {
        return new ResultDTO<>(rtn, msg, null);
    }

    public static <T> ResultDTO<T> failed(int rtn, T data, String msg) {
        return new ResultDTO<>(rtn, msg, data);
    }

    /** 创建新ResultDTO */
    public static <T> ResultDTO<T> newResult(int rtn, T data, String msg) {
        return new ResultDTO<>(rtn, msg, data);
    }

    /** 创建新ResultDTO */
    public static <T> ResultDTO<T> newResult(ResultCode resultCode) {
        return resultCode.toResultDTO();
    }

    /** 无效的请求参数 */
    public static <T> ResultDTO<T> invalidParam() {
        return ResultCode.INVALID_PARAM.toResultDTO();
    }

    /** 无效的请求参数 */
    public static <T> ResultDTO<T> invalidParam(String msg) {
        return ResultCode.INVALID_PARAM.toResultDTO(null, msg);
    }

    /** 身份未认证 */
    public static <T> ResultDTO<T> unauthorized() {
        return ResultCode.UNAUTHORIZED.toResultDTO();
    }

    /** 身份未认证 */
    public static <T> ResultDTO<T> unauthorized(String msg) {
        return ResultCode.UNAUTHORIZED.toResultDTO(null, msg);
    }

    /** 没有访问权限 */
    public static <T> ResultDTO<T> noPermission() {
        return ResultCode.NO_PERMISSION.toResultDTO();
    }

    /** 没有访问权限 */
    public static <T> ResultDTO<T> noPermission(String msg) {
        return ResultCode.NO_PERMISSION.toResultDTO(null, msg);
    }

    /** 无效的访问地址 */
    public static <T> ResultDTO<T> urlNotFound() {
        return ResultCode.URL_NOT_FOUND.toResultDTO();
    }

    /** 无效的访问地址 */
    public static <T> ResultDTO<T> urlNotFound(String msg) {
        return ResultCode.URL_NOT_FOUND.toResultDTO(null, msg);
    }

    @Transient
    public boolean isSuccess() {
        return rtn == 0;
    }

    @Transient
    public boolean isFailure() {
        return rtn != 0;
    }

    public int getRtn() {
        return rtn;
    }

    public void setRtn(int rtn) {
        this.rtn = rtn;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 保持和原来的rnt一致，请用getRtn()
     */
    @Deprecated
    @Transient
    public int getCode() {
        return rtn;
    }

    @Override
    public String toString() {
        return "ResultDTO{" +
                "rtn=" + rtn +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
