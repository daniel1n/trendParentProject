package cn.how2j.trend;

import cn.how2j.trend.dubbo.IndexCodesDubboService;
import cn.how2j.trend.dubbo.IndexDataDubboService;
import cn.how2j.trend.dubbo.ThirdPartIndexDataDubboService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Trend API Module Test
 */
public class ApiTest {

    @Test
    public void testDubboInterfacesExist() {
        // Verify Dubbo interfaces can be loaded
        assertNotNull(IndexCodesDubboService.class);
        assertNotNull(IndexDataDubboService.class);
        assertNotNull(ThirdPartIndexDataDubboService.class);
    }
}
