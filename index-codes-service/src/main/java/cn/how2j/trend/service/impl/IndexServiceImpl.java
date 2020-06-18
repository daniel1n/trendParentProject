package cn.how2j.trend.service.impl;

import cn.how2j.trend.pojo.Index;
import cn.how2j.trend.service.IndexService;
import cn.hutool.core.collection.CollUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author qqlin
 * @date 2020-6-13 23:20
 */
@Service
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
}
