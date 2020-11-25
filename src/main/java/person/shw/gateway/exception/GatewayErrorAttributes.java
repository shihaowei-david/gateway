package person.shw.gateway.exception;

import com.alibaba.fastjson.JSONException;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author shihaowei
 * @date 2020/7/9 2:25 下午
 */
public class GatewayErrorAttributes implements ErrorAttributes {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayErrorAttributes.class);

    private static final String ERROR_ATTRIBUTE = GatewayErrorAttributes.class.getName() + ".ERROR";

    private final boolean includeException;

    public GatewayErrorAttributes() {
        this(false);
    }

    public GatewayErrorAttributes(boolean includeException) {
        this.includeException = includeException;
    }

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        Throwable error = getError(request);
        if (error instanceof RpcException) {
            errorAttributes.put("rtn", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorAttributes.put("msg", error.getMessage());
            handleException(errorAttributes, error, includeStackTrace);
        } else if (error instanceof GatewayAPITimeoutException) {
            errorAttributes.put("rtn", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorAttributes.put("msg", error.getMessage());
        } else if (error instanceof JSONException) {
            errorAttributes.put("rtn", HttpStatus.BAD_REQUEST.value());
            errorAttributes.put("msg", error.getMessage());
        } else if (error instanceof ServerWebInputException) {
            errorAttributes.put("rtn", HttpStatus.BAD_REQUEST.value());
            errorAttributes.put("msg", ((ServerWebInputException) error).getReason());
        } else if (error instanceof ResponseStatusException) {
            HttpStatus status = ((ResponseStatusException) error).getStatus();
            errorAttributes.put("rtn", status.value());
            errorAttributes.put("msg", status.getReasonPhrase());
        } else {
            errorAttributes.put("rtn", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorAttributes.put("msg", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            handleException(errorAttributes, error, includeStackTrace);
        }
        return errorAttributes;
    }

    private void handleException(Map<String, Object> errorAttributes, Throwable error, boolean includeStackTrace) {
        if (error instanceof RpcException) {
            LOG.error("gateway error \n {}", getStackTrace(error, 3));
        } else {
            LOG.error("gateway error", error);
        }

        if (this.includeException) {
            errorAttributes.put("exception", error.getClass().getName());
        }
        if (includeStackTrace) {
            addStackTrace(errorAttributes, error);
        }
    }

    private void addStackTrace(Map<String, Object> errorAttributes, Throwable error) {
        StringWriter stackTrace = new StringWriter();
        error.printStackTrace(new PrintWriter(stackTrace));
        stackTrace.flush();
        errorAttributes.put("trace", stackTrace.toString());
    }

    private static String getStackTrace(Throwable throwable, int maxLine) {
        StringBuilder sb = new StringBuilder();
        iterateStackTrace(sb, throwable, maxLine);
        return sb.toString();
    }

    private static void iterateStackTrace(StringBuilder sb, Throwable throwable, int maxLine) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        int lineSize = Math.min(stackTrace.length, maxLine);
        if (sb.length() > 0) {
            sb.append("\n cause ");
        }else{
            sb.append("\n");
        }
        sb.append(throwable.getClass().getName());
        sb.append(": ");
        sb.append(throwable.getMessage());
        for (int i = 0; i < lineSize; i++) {
            sb.append("\n\tat ");
            sb.append(stackTrace[i]);
        }
        sb.append("\t...");

        Throwable cause = throwable.getCause();
        if (cause != null) {
            iterateStackTrace(sb, cause, maxLine);
        }
    }

    @Override
    public Throwable getError(ServerRequest request) {
        return (Throwable) request.attribute(ERROR_ATTRIBUTE)
                .orElseThrow(() -> new IllegalStateException("Missing exception attribute in ServerWebExchange"));
    }

    @Override
    public void storeErrorInformation(Throwable error, ServerWebExchange exchange) {
        exchange.getAttributes().putIfAbsent(ERROR_ATTRIBUTE,error);
    }
}
