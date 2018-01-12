package au.com.zhinanzhen.tb.web;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ikasoa.core.utils.StringUtil;

import au.com.zhinanzhen.tb.service.ThirdTypeEnum;
import au.com.zhinanzhen.tb.service.UserService;
import au.com.zhinanzhen.tb.service.pojo.UserDTO;
import au.com.zhinanzhen.tb.utils.JsonResult;

public abstract class AbstractController {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractController.class);
    @Resource
    private UserService userService;

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public JsonResult exp(Exception e) {
	LOG.error("error ! message:" + e.getMessage());
	e.printStackTrace();
	return new JsonResult(e);
    }

    protected int getUserIdByOpenId(String thirdType, String openId) throws Exception {
	if(ThirdTypeEnum.get(thirdType)==null){
	    throw new Exception("第三方登陆类型错误!thirdType = "+thirdType);
	}
	if(StringUtil.isEmpty(openId)){
	    throw new Exception("openId不能空");
	}
	if (!userService.thirdVerify(thirdType, openId)) {
	    throw new Exception("系统未找到该 openId! openId = "+openId+",登陆类型:"+thirdType);
	}
	UserDTO userDto =userService.thirdLogin(thirdType, openId);
	return userDto.getId();
    }
}
