package person.shw.gateway.dubbo.listen;

import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.InvokerListener;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shihaowei
 * @date 2020/7/9 11:53 上午
 */
@Activate
public class DubboInvokerListener implements InvokerListener {

    private static final Logger LOG = LoggerFactory.getLogger(DubboInvokerListener.class);

    @Override
    public void referred(Invoker<?> invoker) throws RpcException {
        LOG.info("[Gateway] dubbo referred invoker. {}", invoker.getUrl());
    }

    @Override
    public void destroyed(Invoker<?> invoker) {
        LOG.info("[Gateway] dubbo destroyed invoker. {}", invoker.getUrl());
    }
}
