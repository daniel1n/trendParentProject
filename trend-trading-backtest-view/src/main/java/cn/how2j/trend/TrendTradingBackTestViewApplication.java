package cn.how2j.trend;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author qqlin
 * @date 2020-6-15 0:15
 */
@SpringBootApplication
@EnableDiscoveryClient
public class TrendTradingBackTestViewApplication {

    public static void main(String[] args) {
        int port = 0;
        int defaultPort = 8041;
        int nacosPort = 8848;

        if (NetUtil.isUsableLocalPort(nacosPort)) {
            System.err.printf("检查到端口%d 未启用，判断 Nacos 服务器没有启动，本服务无法使用，故退出%n", nacosPort);
            System.exit(1);
        }

        if (null != args && 0 != args.length) {
            for (String arg : args) {
                if (arg.startsWith("port=")) {
                    String strPort = StrUtil.subAfter(arg, "port=", true);
                    if (NumberUtil.isNumber(strPort)) {
                        port = Convert.toInt(strPort);
                    }
                }
            }
        }

        if (0 == port) {
            Future<Integer> future = ThreadUtil.execAsync(() -> {
                int p = 0;
                System.out.println("请于5秒钟内输入端口号, 推荐  8041 、 8042  或者  8043，超过5秒将默认使用 " + defaultPort);
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String strPort = scanner.nextLine();
                    if (!NumberUtil.isInteger(strPort)) {
                        System.err.println("只能是数字");
                        continue;
                    } else {
                        p = Convert.toInt(strPort);
                        scanner.close();
                        break;
                    }
                }
                return p;
            });
            try {
                port = future.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                port = defaultPort;
            }
            if (!NetUtil.isUsableLocalPort(port)) {
                System.err.printf("端口%d被占用了，无法启动%n", port);
                System.exit(1);
            }
        }


        new SpringApplicationBuilder(TrendTradingBackTestViewApplication.class)
                .properties("server.port=" + port)
                .run(args);
    }
}