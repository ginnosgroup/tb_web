package au.com.zhinanzhen.tb.web;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import au.com.zhinanzhen.tb.service.AdviserService;
import au.com.zhinanzhen.tb.service.ServiceException;
import au.com.zhinanzhen.tb.utils.JsonResult;

@Controller
@RequestMapping("/adviser")
public class AdviserController extends AbstractController {
    @Resource 
    private AdviserService adviserService;
    @ResponseBody
    @RequestMapping("/list")
    public JsonResult getlistByRegion(int regionId) throws ServiceException {
	if (regionId < 0) {
	    Exception e = new Exception("regionId < 0");
	    return new JsonResult(e);
	}
	return new JsonResult(adviserService.selectByRegionId(regionId));
    }
    @ResponseBody
    @RequestMapping("/get")
    public JsonResult getById(int id) throws ServiceException {
	if (id < 0) {
	    Exception e = new Exception("id < 0");
	    return new JsonResult(e);
	}
	return new JsonResult(adviserService.getAdviserById(id));
    }
    
}
