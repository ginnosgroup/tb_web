package au.com.zhinanzhen.tb.web;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import au.com.zhinanzhen.tb.service.RegionService;
import au.com.zhinanzhen.tb.service.ServiceException;
import au.com.zhinanzhen.tb.service.pojo.RegionDTO;
import au.com.zhinanzhen.tb.utils.JsonResult;

@Controller
@RequestMapping("/region")
public class RegionController extends AbstractController {
    @Resource
    private RegionService regionService;
    @ResponseBody
    @RequestMapping("/all")
    public JsonResult getParentList() throws ServiceException{
	List<RegionDTO> list = regionService.getALLRegion();
	return new JsonResult(list);
    }
   
}
