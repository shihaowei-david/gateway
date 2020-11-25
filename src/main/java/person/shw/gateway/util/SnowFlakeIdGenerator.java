package person.shw.gateway.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * SnowFlakeIdGenerator
 * 雪花算法id生成器，可运行再集群环境中，dataCenterId 以IP为运算元，machineId以 pid为运算元
 *
 * @author shihaowei
 * @date 2020/7/9 11:35 上午
 */
public class SnowFlakeIdGenerator {
    private static final Logger log = LoggerFactory.getLogger(SnowFlakeIdGenerator.class);

    private static final SnowFlake SNOW_FLAKE = new SnowFlake(getDataCenterId(), getMachineId());

    /**
     * 通过雪花算法获得ID
     */
    public static long nextSnowFlakeId() {
        return SNOW_FLAKE.nextId();
    }

    /**
     * 根据ip计算一个小于 0~2^5的id
     */
    private static int getDataCenterId() {
        try {
            final InetAddress ip = InetAddress.getLocalHost();
            final int address = ip.hashCode();

            // 由于  机器标识占用的位数=5， 所以最多有2^5=32个centerId值
            // 使用"叠加异或运算" 让ip里的每一位都可以影响最终的值
            // 如：IP为198.18.36.243 则为 11000^11000^01001^00010^01001^11100^11
            return (address >>> 27 & 31)
                    ^ (address >>> 22 & 31)
                    ^ (address >>> 17 & 31)
                    ^ (address >>> 12 & 31)
                    ^ (address >>> 7 & 31)
                    ^ (address >>> 2 & 31)
                    ^ (address & 3);
        } catch (UnknownHostException e) {
            log.error("获取不到ip信息，使用随机数.");
        }
        // 随机数兜底
        return new Random().nextInt(1 << 5);
    }

    /**
     * 根据pid取余生成一个小于32的id
     */
    private static int getMachineId() {
        final int pid = getPid();
        return pid % 32;
    }

    private static int getPid() {
        try {
            String jvmName = ManagementFactory.getRuntimeMXBean().getName();
            return Integer.parseInt(jvmName.split("@")[0]);
        } catch (Throwable e) {
            return 0;
        }
    }
}
