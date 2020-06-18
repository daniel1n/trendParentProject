package cn.how2j.trend.client;

import cn.how2j.trend.pojo.IndexData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author qqlin
 * @date 2020-6-14 10:54
 */
@Primary
@FeignClient(value = "INDEX-DATA-SERVICE", fallback = IndexDataClientFeignHystrix.class)
public interface IndexDataClient {

    /**
     * 根据指数代码获取指数数据
     *
     * @param code 指数代码
     * @return 返回所有查询数据
     */
    @GetMapping("/data/{code}")
    List<IndexData> getIndexData(@PathVariable("code") String code);
}
