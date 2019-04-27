package au.com.zhinanzhen.tb.web;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.esotericsoftware.minlog.Log;
import com.ikasoa.core.utils.StringUtil;

import au.com.zhinanzhen.tb.service.ServiceException;
import au.com.zhinanzhen.tb.service.ThirdTypeEnum;
import au.com.zhinanzhen.tb.service.UserService;
import au.com.zhinanzhen.tb.utils.CommonUtil;
import au.com.zhinanzhen.tb.utils.JsonResult;

@Controller
@RequestMapping("/app")
public class AppController extends AbstractController {
	@Resource
	private UserService userService;

	@RequestMapping("/regist")
	@ResponseBody
	public JsonResult appRegist(String openId, String thirdType, String nickName, String logUrl, String recommendOpenid)
			throws ServiceException {
		String date = CommonUtil.getSystemTime();
		if (StringUtil.isEmpty(openId)) {
			Exception e = new Exception("openId is null");
			return new JsonResult(2, e);
		}
		if (StringUtil.isEmpty(thirdType)) {
			Exception e = new Exception("thirdType is null");
			return new JsonResult(2, e);
		}
		if (userService.thirdVerify(ThirdTypeEnum.WECHAT.toString(), openId)) {
			Log.info("ios login ,openId = " + openId + ",date:" + date);
			return new JsonResult(true);
		}
		if (ThirdTypeEnum.get(thirdType) == null) {
			Exception e = new Exception("thirdType error ! thirdType:" + thirdType);
			return new JsonResult(2, e);
		}
		if (StringUtil.isEmpty(nickName)) {
			Exception e = new Exception("nickName is null");
			return new JsonResult(2, e);
		}
		if (StringUtil.isEmpty(logUrl)) {
			Exception e = new Exception("logUrl is null");
			return new JsonResult(2, e);
		}
		if (!userService.addUser("", "", "", ThirdTypeEnum.WECHAT.toString(), "", openId, nickName, logUrl,
				recommendOpenid)) {
			Exception e = new Exception("增加用户失败");
			return new JsonResult(2, e);
		}
		Log.info("ios regist ,openId = " + openId + ",date:" + date);
		return new JsonResult(true);
	}
}
