package cn.how2j.trend.web;

import cn.how2j.trend.pojo.Index;
import cn.how2j.trend.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author qqlin
 * @date 2020-6-13 11:06
 */
@RestController
public class IndexController {

    @Autowired
    private IndexService indexService;

    /*@GetMapping("/getCodes")
    public List<Index> get() throws Exception {
        return indexService.fetchIndexesFromThirdPart();
    }*/

    //  http://127.0.0.1:8001/freshCodes
    //  http://127.0.0.1:8001/getCodes
    //  http://127.0.0.1:8001/removeCodes

	@GetMapping("/freshCodes")
	public String fresh() throws Exception {
		indexService.fresh();
		return "fresh codes successfully";
	}
	@GetMapping("/getCodes")
	public List<Index> get() throws Exception {
		return indexService.get();
	}
	@GetMapping("/removeCodes")
	public String remove() throws Exception {
		indexService.remove();
		return "remove codes successfully";
	}
}
