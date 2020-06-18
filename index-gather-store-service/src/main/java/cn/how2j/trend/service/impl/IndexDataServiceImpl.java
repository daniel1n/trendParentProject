package cn.how2j.trend.service.impl;

import cn.how2j.trend.pojo.IndexData;
import cn.how2j.trend.service.IndexDataService;
import cn.how2j.trend.util.SpringContextUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author qqlin
 * @date 2020-6-13 16:38
 */
@Service
@CacheConfig(cacheNames = "index_datas")
public class IndexDataServiceImpl implements IndexDataService {

    private Map<String, List<IndexData>> indexDataMap = new HashMap<>();
    @Autowired
    private RestTemplate restTemplate;

    @Override
    @HystrixCommand(fallbackMethod = "thirdPartNotConnected")
    public List<IndexData> fresh(String code) {
        List<IndexData> indexDataList = fetchIndexesFromThirdPart(code);

        indexDataMap.put(code, indexDataList);

        System.out.println("code:" + code);
        System.out.println("indexesDateMap:" + indexDataMap.get(code).size());

        IndexDataService indexDataService = SpringContextUtil.getBean(IndexDataService.class);
        indexDataService.remove(code);
        return indexDataService.store(code);
    }

    @Override
    @CacheEvict(key = "'indexData-code-' + #p0")
    public void remove(String code) {

    }

    @Override
    @CachePut(key = "'indexData-code-' + #p0")
    public List<IndexData> store(String code) {
        return indexDataMap.get(code);
    }

    @Override
    @Cacheable(key = "'indexData-code-' + #p0")
    public List<IndexData> get(String code) {
        return CollUtil.toList();
    }

    @Override
    public List<IndexData> fetchIndexesFromThirdPart(String code) {
        List<Map> temp = restTemplate.getForObject(
                "http://localhost:8090/indexes/" + code + ".json",
                List.class);

        return map2IndexData(temp);
    }

    @Override
    public List<IndexData> thirdPartNotConnected(String code) {
        System.out.println("thirdPartNotConnected()");
        IndexData indexData = new IndexData();
        indexData.setClosePoint(0);
        indexData.setDate("n/a");
        return CollectionUtil.toList(indexData);
    }

    @Override
    public List<IndexData> map2IndexData(List<Map> temp) {
        List<IndexData> indexDataList = new ArrayList<>();

        for (Map map : temp) {
            String date = map.get("date").toString();
            float closePoint = Convert.toFloat(map.get("closePoint"));
            IndexData indexData = new IndexData();

            indexData.setDate(date);
            indexData.setClosePoint(closePoint);
            indexDataList.add(indexData);
        }

        return indexDataList;
    }


}
