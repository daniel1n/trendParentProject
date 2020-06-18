package cn.how2j.trend.service.impl;

import cn.how2j.trend.pojo.IndexData;
import cn.how2j.trend.service.IndexDataService;
import cn.hutool.core.collection.CollUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author qqlin
 * @date 2020-6-14 0:01
 */
@Service
@CacheConfig(cacheNames = "index_datas")
public class IndexDataServiceImpl implements IndexDataService {

    @Override
    @Cacheable(key = "'indexData-code-'+ #p0")
    public List<IndexData> get(String code) {
        return CollUtil.toList();
    }
}
