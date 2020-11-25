package person.shw.gateway.dubbo.filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import person.shw.gateway.constant.RequestKeyConst;

import java.util.HashMap;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;

/**
 * @author shihaowei
 * @date 2020/7/9 11:52 上午
 */
@Activate(group = CONSUMER,order = -11111)
public class ConsumerTraceLogFilter implements Filter{

    protected static final Logger LOG = LoggerFactory.getLogger(ConsumerTraceLogFilter.class);

    public static final String RPC_TRACE_ID = "traceId";
    public static final String RPC_USER_ID = "userId";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        final RpcContext rpcContext = RpcContext.getContext();
        String requestTraceId = null;
        String requestUserId = null;
        String requestMethod = null;
        Object[] arguments = invocation.getArguments();
        if (arguments.length > 0) {
            requestMethod = arguments[0].toString();
            HashMap paramMap = (HashMap) ((Object[]) arguments[2])[0];

            requestTraceId = paramMap.get(RequestKeyConst.REQUEST_TRACE_ID) + StringUtils.EMPTY;
            requestUserId = paramMap.get(RequestKeyConst.REQUEST_USER_ID) + StringUtils.EMPTY;

            rpcContext.setAttachment(RPC_TRACE_ID, requestTraceId);
            rpcContext.setAttachment(RPC_USER_ID, requestUserId);
        }

        // 日志格式如：时间 | 打印类.方法| 日志级别 | [空格]  服务名.方法名 | traceId  | 服务提供者的IP:端口 | 提供者可用 | 完成调用 | 未取消 | 无异常 | 耗时ms
        // eg： 12:12:12.445|ConsumerTraceLogFilter.invoke|INFO| xxxRpcService.func1|12272191341342724|127.0.0.1:2800|1|1|0|0|383ms
        final URL invokerUrl = invoker.getUrl();
        final long startTime = System.currentTimeMillis();
        final String longServiceName = invokerUrl.getServiceInterface();
        final String serviceName = longServiceName.substring(longServiceName.lastIndexOf(".") + 1);
        final String providerHost = invokerUrl.getHost();
        final int port = invokerUrl.getPort();
        final String isAvailable = invoker.isAvailable() ? "1" : "0";
        String isDone = "1";
        String isNotCancelled = "1";
        String noExcpetion = "1";

        try {
//          TODO 异步请求的处理
            Result result = invoker.invoke(invocation);
            isDone = result.isDone() ? "1" : "0";
            isNotCancelled = result.isCancelled() ? "0" : "1";
            noExcpetion = result.hasException() ? "0" : "1";
            return result;
        } finally {
            final long endTime = System.currentTimeMillis();
            LOG.info("{}.{}|{}|{}|{}:{}|{}|{}|{}|{}|{}ms", serviceName, requestMethod, requestTraceId, requestUserId, providerHost, port, isAvailable, isDone, isNotCancelled, noExcpetion,
                    endTime - startTime);
        }
    }

}
