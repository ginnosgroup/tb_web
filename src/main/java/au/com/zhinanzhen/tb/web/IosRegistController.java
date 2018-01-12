package au.com.zhinanzhen.tb.web;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import au.com.zhinanzhen.tb.utils.ConfigService;
import au.com.zhinanzhen.tb.utils.JsonResult;

@Controller
@RequestMapping("/ios")
public class IosRegistController extends AbstractController {
    @Resource
    private ConfigService configService;

    @RequestMapping("/regist")
    @ResponseBody
    public JsonResult regist() {
	return new JsonResult(configService.getIosRegist());
    }
}
