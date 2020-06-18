package cn.how2j.trend.web;

import cn.how2j.trend.config.IpConfiguration;
import cn.how2j.trend.pojo.Index;
import cn.how2j.trend.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 表示允许跨域，@CrossOrigin
 * 因为后续的回测视图是另一个端口号的，访问这个服务是属于跨域了。
 *
 * @author qqlin
 * @date 2020-6-13 23:25
 */
@RestController
public class IndexController {

    @Autowired
    private IndexService indexService;
    @Autowired
    private IpConfiguration ipConfiguration;

//  http://127.0.0.1:8011/codes

    @GetMapping("/codes")
    @CrossOrigin
    public List<Index> codes() throws Exception {
        System.out.println("current instance's port is" + ipConfiguration.getServerPort());
        return indexService.getIndexes();
    }
}
