package cn.how2j.trend.service.impl;

import cn.how2j.trend.dubbo.ThirdPartIndexDataDubboService;
import cn.how2j.trend.pojo.Index;
import cn.how2j.trend.pojo.IndexData;
import cn.hutool.core.collection.CollUtil;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 第三方指数数据服务实现 (Dubbo RPC)
 *
 * @author qqlin
 */
@Service
@DubboService(version = "1.0.0", timeout = 30000)
public class ThirdPartIndexDataServiceImpl implements ThirdPartIndexDataDubboService {

    @Override
    public List<Index> getCodes() {
        List<Index> indices = new ArrayList<>();
        Index index1 = new Index();
        index1.setCode("000001");
        index1.setName("上证指数");
        indices.add(index1);

        Index index2 = new Index();
        index2.setCode("1399001");
        index2.setName("沪深300");
        indices.add(index2);

        Index index3 = new Index();
        index3.setCode("399006");
        index3.setName("创业板");
        indices.add(index3);

        return indices;
    }

    @Override
    public List<IndexData> getIndexData(String code) {
        // Mock data - in production, this would fetch from a real third-party API
        return CollUtil.toList();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}