package cn.how2j.trend.service.impl;

import cn.how2j.trend.pojo.IndexData;
import cn.how2j.trend.service.IndexDataService;
import cn.hutool.core.collection.CollUtil;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author qqlin
 * @date 2020-6-14 0:01
 */
@Service
@DubboService(version = "1.0.0", timeout = 30000)
@CacheConfig(cacheNames = "index_datas")
public class IndexDataServiceImpl implements IndexDataService {

    @Override
    @Cacheable(key = "'indexData-code-'+ #p0")
    public List<IndexData> get(String code) {
        return CollUtil.toList();
    }

    @Override
    @CacheEvict(allEntries = true)
    public List<IndexData> fresh(String code) {
        return get(code);
    }

    @Override
    public List<IndexData> fetchFromThirdPart(String code) {
        return CollUtil.toList();
    }

    @Override
    @CacheEvict(allEntries = true)
    public void remove(String code) {
        // Redis cache eviction
    }

    @Override
    @CacheEvict(allEntries = true)
    public List<IndexData> store(String code) {
        return get(code);
    }
}