package cn.how2j.trend;

import cn.hutool.core.util.NetUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.turbine.EnableTurbine;

/**
 * @author qqlin
 * @date 2020-6-16 12:29
 */
@SpringBootApplication
@EnableTurbine
public class IndexTurbineApplication {

    public static void main(String[] args) {
        int port = 8080;
        int eurekaServerPort = 8761;

        if (NetUtil.isUsableLocalPort(eurekaServerPort)) {
            System.err.printf("检查到端口%d未启用，判断eureka服务器没有启动， 本服务无法使用，故提出%n", eurekaServerPort);
            System.exit(1);
        }

        if (!NetUtil.isUsableLocalPort(port)) {
            System.err.printf("端口%d被占用了，无法启动%n", port);
            System.exit(1);
        }

        new SpringApplicationBuilder(IndexTurbineApplication.class)
                .properties("server.port=" + port)
                .run(args);
    }
}
