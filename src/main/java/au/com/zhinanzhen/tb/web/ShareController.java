package au.com.zhinanzhen.tb.web;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import au.com.zhinanzhen.tb.service.RegionService;
import au.com.zhinanzhen.tb.service.SubjectService;
import au.com.zhinanzhen.tb.service.UserService;
import au.com.zhinanzhen.tb.service.pojo.SubjectResultDTO;
import au.com.zhinanzhen.tb.service.pojo.UserDTO;
import au.com.zhinanzhen.tb.utils.ConfigService;
import au.com.zhinanzhen.tb.utils.JsonResult;
import com.ikasoa.core.utils.SimpleDESUtil;
import com.ikasoa.core.utils.StringUtil;

@Controller
@RequestMapping("/share")
public class ShareController extends AbstractController {
    @Resource
    private RegionService regionService;
    @Resource
    private SubjectService subjectService;
    @Resource
    private UserService userService;
    @Resource
    private ConfigService configService;

    @ResponseBody
    @RequestMapping("/getUrl")
    public JsonResult shareSubject(int subjectId, int regionId, String openId, String thirdType) throws Exception {
	if (subjectId < 0) {
	    Exception e = new Exception("subjectId错误");
	    return new JsonResult(2, e);
	}
	if (StringUtil.isEmpty(thirdType)) {
	    Exception e = new Exception("thirdTyp不能空");
	    return new JsonResult(2, e);
	}
	if (StringUtil.isEmpty(openId)) {
	    Exception e = new Exception("openId不能空");
	    return new JsonResult(2, e);
	}
	int userId = getUserIdByOpenId(thirdType, openId);
	SubjectResultDTO subjectResultDto = subjectService.getSubjectById(subjectId, regionId);
	if (subjectResultDto == null) {
	    Throwable e = new Throwable("课程未找到!");
	    return new JsonResult(4, e);
	}
	String key = "znz630pd";
	String introducerIdEncrypt = SimpleDESUtil.encrypt(userId + "", key);
	String baseUrl = configService.getHost();
	String shareUrl = baseUrl + "share/subject?subjectId=" + subjectId + "&introducerIdStr=" + introducerIdEncrypt
		+ "&regionId=" + regionId;
	return new JsonResult(shareUrl);
    }

    @RequestMapping("/subject")
    public void shareSubject(int subjectId, int regionId, String introducerIdStr, HttpServletResponse resp)
	    throws Exception {
	if (subjectId < 0) {
	    Exception e = new Exception("subjectId错误 !");
	    throw e;
	}
	if (regionId < 0) {
	    Exception e = new Exception("regionId错误 !");
	    throw e;
	}
	SubjectResultDTO subjectResultDto = subjectService.getSubjectById(subjectId, regionId);
	if (subjectResultDto == null) {
	    Exception e = new Exception("课程未找到 !");
	    throw e;
	}
	String key = configService.getKey();
	int introducerId = Integer.valueOf(SimpleDESUtil.decrypt(introducerIdStr, key));
	System.out.println("introducerId:" + introducerId);
	Cookie cookie = new Cookie("introducerId", introducerId + "");
	cookie.setPath("/");
	resp.addCookie(cookie);
	String url = "../detail.htm?cid=" + subjectId + "&regionId=" + regionId;
	resp.sendRedirect(url);
	return;
    }
}