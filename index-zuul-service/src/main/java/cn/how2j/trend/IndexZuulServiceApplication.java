package cn.how2j.trend;

import cn.hutool.core.net.NetUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * @author qqlin
 * @date 2020-6-14 10:33
 */
@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
public class IndexZuulServiceApplication {

    public static void main(String[] args) {
        int port = 8031;
        if (!NetUtil.isUsableLocalPort(port)) {
            System.err.printf("端口%d被占用了，无法启动%n", port);
            System.exit(1);
        }

        new SpringApplicationBuilder(IndexZuulServiceApplication.class)
                .properties("server.port=" + port)
                .run(args);
    }
}
