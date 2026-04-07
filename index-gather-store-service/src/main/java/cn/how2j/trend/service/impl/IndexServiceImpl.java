package cn.how2j.trend.service.impl;

import cn.how2j.trend.dubbo.ThirdPartIndexDataDubboService;
import cn.how2j.trend.pojo.Index;
import cn.how2j.trend.service.IndexService;
import cn.how2j.trend.util.SpringContextUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author qqlin
 * @date 2020-6-13 15:57
 */
@Service
@CacheConfig(cacheNames = "indexes")
public class IndexServiceImpl implements IndexService {

    private List<Index> indexes;

    @DubboReference(version = "1.0.0", timeout = 30000)
    private ThirdPartIndexDataDubboService thirdPartIndexDataDubboService;

    @Override
    @CircuitBreaker(name = "default", fallbackMethod = "thirdPartNotConnected")
    public List<Index> fresh() {
        indexes = fetchIndexesFromThirdPart();
        IndexService indexService = SpringContextUtil.getBean(IndexService.class);
        indexService.remove();
        return indexService.store();
    }

    @Override
    @CacheEvict(allEntries = true)
    public void remove() {
    }

    @Override
    @Cacheable(key = "'all_codes'")
    public List<Index> store() {
        System.out.println(this);
        return indexes;
    }

    @Override
    @Cacheable(key = "'all_codes'")
    public List<Index> get() {
        return CollUtil.toList();
    }

    @Override
    public List<Index> fetchIndexesFromThirdPart() {
        // 使用 Dubbo RPC 调用第三方服务
        return thirdPartIndexDataDubboService.getCodes();
    }

    @Override
    public List<Index> thirdPartNotConnected(Throwable t) {
        System.out.println("thirdPartNotConnected(), cause: " + t.getMessage());
        Index index = new Index();
        index.setCode("000000");
        index.setName("无效指数代码");
        return CollectionUtil.toList(index);
    }

    @Override
    public List<Index> map2Index(List<Map> temp) {
        List<Index> indexes = new ArrayList<>();
        for (Map map : temp) {
            String code = map.get("code").toString();
            String name = map.get("name").toString();
            Index index = new Index();
            index.setCode(code);
            index.setName(name);
            indexes.add(index);
        }

        return indexes;
    }

    // Dubbo service interface methods (required for consumer, may delegate to local methods)

    @Override
    public List<Index> getCodes() {
        return get();
    }

    @Override
    public Index getIndex(String code) {
        Index index = new Index();
        index.setCode(code);
        index.setName("指数-" + code);
        return index;
    }

    @Override
    public void remove(String code) {
        // Redis cache eviction for specific code
    }

    @Override
    public Index store(String code) {
        Index index = new Index();
        index.setCode(code);
        index.setName("指数-" + code);
        return index;
    }
}
