package cn.how2j.trend.service.impl;

import cn.how2j.trend.pojo.Index;
import cn.how2j.trend.service.IndexService;
import cn.how2j.trend.util.SpringContextUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    @Autowired
    private RestTemplate restTemplate;

    @Override
    @HystrixCommand(fallbackMethod = "thirdPartNotConnected")
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
        List<Map> temp = restTemplate.getForObject(
                "http://localhost:8090/indexes/codes.json", List.class);
        assert temp != null;
        return map2Index(temp);
    }

    @Override
    public List<Index> thirdPartNotConnected() {
        System.out.println("thirdPartNotConnected()");
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
}
