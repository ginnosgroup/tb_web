package au.com.zhinanzhen.tb.web;

import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.esotericsoftware.minlog.Log;
import com.ikasoa.core.utils.StringUtil;

import au.com.zhinanzhen.tb.service.ServiceException;
import au.com.zhinanzhen.tb.service.UserService;
import au.com.zhinanzhen.tb.utils.CommonUtil;
import au.com.zhinanzhen.tb.utils.JsonResult;
import au.com.zhinanzhen.tb.utils.SendSMSUtil;

@CrossOrigin(origins = "*", maxAge = 3600)
@Controller
@RequestMapping("/SMS")
public class SMSCodeController extends AbstractController {
    private static final Logger LOG = LoggerFactory.getLogger(SMSCodeController.class);
    @Resource
    private UserService userService;

    @ResponseBody
    @RequestMapping("/isPhoneExitst")
    public JsonResult isPhoneExitst(String phone) throws ServiceException {
	if (StringUtil.isEmpty(phone)) {
	    Throwable e = new Throwable("phone is null");
	    return new JsonResult(2, e);
	}
	return new JsonResult(userService.isPhoneExitst(phone));
    }

    // 获取手机验证码
    @ResponseBody
    @RequestMapping("/getCode")
    public JsonResult getPhoneVerficationCode(String phone, HttpServletRequest req) {
	if (StringUtil.isEmpty(phone)) {
	    Throwable e = new Throwable("phone is null");
	    return new JsonResult(2, e);
	}
	HttpSession session = req.getSession();
	String verTime = (String) session.getAttribute("verTime");
	String systemDate = CommonUtil.getSystemTime();
	int overTime = 50;
	try {
	    if (verTime == null || CommonUtil.getTimeDifference(systemDate, verTime) >= overTime) {
		Map<String, String> map = SendSMSUtil.sendMsg(phone);
		if (map == null || map.size() == 0) {
		    Throwable e = new Throwable("send massage fail !");
		    return new JsonResult(4, e);
		}
		verTime = map.get("time");
		String identifyingCode = map.get("identifyingCode");
		session.setAttribute("verTime", verTime);
		session.setAttribute("identifyingCode", identifyingCode);
		session.setAttribute("verPhone", phone);
		Log.info("identify phone" + phone + ",identifyingCode:" + identifyingCode);
		return new JsonResult(identifyingCode);
	    } else {
		long time=CommonUtil.getTimeDifference(systemDate, verTime);
		Throwable e = new Throwable("请等待"+(overTime-time)+"秒");
		return new JsonResult(3, e);
	    }
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    e.printStackTrace();
	    Throwable eo = new Throwable(e.getMessage());
	    return new JsonResult(1, eo);
	}
    }
}