package cn.how2j.trend.service.impl;

import cn.how2j.trend.dubbo.IndexCodesDubboService;
import cn.how2j.trend.pojo.Index;
import cn.how2j.trend.service.IndexService;
import cn.hutool.core.collection.CollUtil;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author qqlin
 * @date 2020-6-13 23:20
 */
@Service
@DubboService(version = "1.0.0", group = "index-codes-group", timeout = 30000)
@CacheConfig(cacheNames = "indexes")
public class IndexServiceImpl implements IndexService {

    private List<Index> indexes;

    @Override
    @Cacheable(key = "'all_codes'")
    public List<Index> getIndexes() {
        Index index = new Index();
        index.setName("无效指数代码");
        index.setCode("000000");
        return CollUtil.toList(index);
    }

    @Override
    @Cacheable(key = "'all_codes'")
    public List<Index> getCodes() {
        return getIndexes();
    }

    @Override
    @Cacheable(key = "'index:' + #code")
    public Index getIndex(String code) {
        Index index = new Index();
        index.setCode(code);
        index.setName("指数-" + code);
        return index;
    }

    @Override
    @CacheEvict(allEntries = true)
    public List<Index> fresh() {
        return getIndexes();
    }

    @Override
    @CacheEvict(allEntries = true)
    public void remove(String code) {
        // Redis cache eviction
    }

    @Override
    @CacheEvict(allEntries = true)
    public Index store(String code) {
        Index index = new Index();
        index.setCode(code);
        index.setName("指数-" + code);
        return index;
    }
}