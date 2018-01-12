package au.com.zhinanzhen.tb.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.esotericsoftware.minlog.Log;
import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;
import au.com.zhinanzhen.tb.service.AdviserService;
import au.com.zhinanzhen.tb.service.RegionService;
import au.com.zhinanzhen.tb.service.ServiceException;
import au.com.zhinanzhen.tb.service.ThirdTypeEnum;
import au.com.zhinanzhen.tb.service.UserService;
import au.com.zhinanzhen.tb.service.pojo.AdviserDTO;
import au.com.zhinanzhen.tb.service.pojo.UserDTO;
import au.com.zhinanzhen.tb.service.pojo.WxUserInfoDTO;
import au.com.zhinanzhen.tb.utils.Base64Util;
import au.com.zhinanzhen.tb.utils.CommonUtil;
import au.com.zhinanzhen.tb.utils.ConfigService;
import au.com.zhinanzhen.tb.utils.EmojiFilter;
import au.com.zhinanzhen.tb.utils.HttpUtil2;
import au.com.zhinanzhen.tb.utils.JsonResult;
import au.com.zhinanzhen.tb.utils.MailUtil;

@Controller
@RequestMapping("/user")
public class UserController extends AbstractController {
    @Resource
    private ConfigService configService;
    @Resource
    private UserService userService;
    @Resource
    private RegionService regionService;
    @Resource
    private AdviserService adviserService;

    private static Map<Integer, Integer> adviserCache = new HashMap<Integer, Integer>();

    // 授权返回信息
    @RequestMapping("/wxresult")
    @Transactional
    public void WeiXinResult(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String code = request.getParameter("code");
	String state = request.getParameter("state");
	Log.info("state:" + state);
	String base = configService.getHost();
	Map<String, String> map = new HashMap<String, String>();
	String takenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token";
	String APPID = configService.getAppId();
	String SECRET = configService.getSecret();
	map.put("appid", APPID);
	map.put("secret", SECRET);
	map.put("code", code);
	map.put("grant_type", "authorization_code");
	String r = HttpUtil2.httpGet(takenUrl, map);
	if (StringUtil.isEmpty(r)) {
	    throw new Exception("r is null !");
	}
	JSONObject object = JSON.parseObject(r);
	if (object == null) {
	    throw new Exception("object is null !");
	}
	String openId = (String) object.get("openid");
	String accessToken = (String) object.get("access_token");
	String refreshToken = (String) object.get("refresh_token");
	String returnUrl;
	String recommendOpenId;
	String s = "?";
	if (state.contains("?")) {
	    s = "&";
	}
	if (state.contains(",recommendOpenId:")) {
	    returnUrl = base + state.split(",recommendOpenId:")[0] + s + "openId=" + openId;
	    recommendOpenId = state.split(",recommendOpenId:")[1];
	} else {
	    returnUrl = base + state + s + "openId=" + openId;
	    recommendOpenId = null;
	}
	if (StringUtil.isEmpty(openId) || StringUtil.isEmpty(accessToken) || StringUtil.isEmpty(refreshToken)) {
	    throw new Exception("openId or token is null !");
	}
	String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo";
	map = new HashMap<String, String>();
	map.put("openId", openId);
	map.put("access_token", accessToken);
	String r2 = HttpUtil2.httpGet(userInfoUrl, map);
	if (StringUtil.isEmpty(r2)) {
	    throw new Exception("r2 is null !");
	}
	WxUserInfoDTO wxInfoDto = JSON.parseObject(r2, WxUserInfoDTO.class);
	if (wxInfoDto == null) {
	    throw new Exception("wxInfoDto is null !");
	}
	String nickName = wxInfoDto.getNickName();
	nickName = new String(nickName.getBytes("iso-8859-1"), "utf-8");
	nickName=EmojiFilter.filterEmoji(nickName);
	String logo = wxInfoDto.getHeadimgurl();
	// 登陆并且返回
	if (userService.thirdVerify(ThirdTypeEnum.WECHAT.toString(), openId)) {
	    UserDTO userDto = userService.thirdLogin(ThirdTypeEnum.WECHAT.toString(), openId);
	    byte[] input = nickName.getBytes();
	    try {
		nickName = Base64Util.encodeBase64(input);
	    } catch (Exception e) {
		Log.info("nickname toBase64 error! nickname = " + nickName);
	    }
	    userDto.setNickname(nickName);
	    userDto.setLogo(logo);
	    if (!userService.updateUser(userDto)) {
		Log.info("login update user's logoUrl and nickName  fail ! userId = " + userDto.getId());
	    }
	    response.sendRedirect(returnUrl + "&isNew=false");
	    return;
	}
	// 注册并且返回
	if (!userService.addUser(ThirdTypeEnum.WECHAT.toString(), openId, nickName, logo, recommendOpenId)) {
	    throw new Exception("添加用户失败 !");
	}
	response.sendRedirect(returnUrl + "&isNew=true");
	return;
    }
    @RequestMapping("/update")
    @ResponseBody
    @Transactional
    public JsonResult update(String name, String email, String openId, String thirdType, String username)
	    throws Exception {
	if (StringUtil.isEmpty(thirdType)) {
	    Exception e = new Exception("thirdType不能空");
	    return new JsonResult(2, e);
	}
	if (StringUtil.isEmpty(openId)) {
	    Exception e = new Exception("openId不能空");
	    return new JsonResult(2, e);
	}
	int userId = getUserIdByOpenId(thirdType, openId);
	UserDTO userDto = userService.getUserDTOById(userId);
	if (StringUtil.isNotEmpty(name)) {
	    userDto.setName(name);
	}
	if (StringUtil.isNotEmpty(email)) {
	    userDto.setEmail(email);
	}
	if (StringUtil.isNotEmpty(username)) {
	    userDto.setUsername(username);
	}
	if (!userService.updateUser(userDto)) {
	    Exception e = new Exception("更新用户信息失败");
	    return new JsonResult(3, e);
	}
	return new JsonResult(true);
    }

    @RequestMapping("/regist")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public JsonResult region(String phone, String username, String openId, String thirdType, String identifyingCode,
	    HttpServletRequest req) throws Exception {
	if (StringUtil.isEmpty(thirdType)) {
	    Exception e = new Exception("thirdType不能空");
	    return new JsonResult(2, e);
	}
	if (StringUtil.isEmpty(openId)) {
	    Exception e = new Exception("openId不能空");
	    return new JsonResult(2, e);
	}
	int userId = getUserIdByOpenId(thirdType, openId);
	if (StringUtil.isEmpty(phone)) {
	    Exception e = new Exception("phone不能空");
	    return new JsonResult(2, e);
	}
	if (StringUtil.isEmpty(identifyingCode)) {
	    Exception e = new Exception("identifyingCode不能空");
	    return new JsonResult(2, e);
	}
	UserDTO userDto = userService.getUserDTOById(userId);
	if (StringUtil.isNotEmpty(userDto.getPhone())) {
	    Exception e = new Exception("该账号已绑定手机,不能重复绑定! phone = " + phone);
	    return new JsonResult(2, e);
	}
	if (userService.isPhoneExitst(phone)) {
	    Exception e = new Exception("该手机号已注册 ! phone = " + phone);
	    return new JsonResult(2, e);
	}
	HttpSession session = req.getSession();
	if (session == null) {
	    Exception e = new Exception("验证超时，请重新验证 ");
	    return new JsonResult(3, e);
	}
	String verStrCode = (String) session.getAttribute("identifyingCode");
	String verPhone = (String) session.getAttribute("verPhone");
	String verTime = (String) session.getAttribute("verTime");
	String systemDate = CommonUtil.getSystemTime();
	if (verTime == null || CommonUtil.getTimeDifference(systemDate, verTime) >= 600) {
	    Exception e = new Exception("验证超时,请在10分钟内完成验证 ");
	    return new JsonResult(3, e);
	}
	if (!identifyingCode.equals(verStrCode)) {
	    Exception e = new Exception("验证码错误  ");
	    return new JsonResult(4, e);
	}
	if (!phone.equals(verPhone)) {
	    Exception e = new Exception("验证的手机号不匹配 ");
	    return new JsonResult(4, e);
	}
	userDto.setPhone(phone);
	if (StringUtil.isNotEmpty(username)) {
	    userDto.setUsername(username);
	}
	return new JsonResult(userService.updateUser(userDto));
    }

    @RequestMapping("/allocation")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public JsonResult allocationAdviser(String openId, String thirdType, int adviserId, int regionId) throws Exception {
	if (StringUtil.isEmpty(thirdType)) {
	    Exception e = new Exception("thirdType不能空");
	    return new JsonResult(2, e);
	}
	if (StringUtil.isEmpty(openId)) {
	    Exception e = new Exception("openId不能空");
	    return new JsonResult(2, e);
	}
	if (adviserId < 0) {
	    Exception e = new Exception("adviserId error ! adviserId = " + adviserId);
	    return new JsonResult(2, e);
	}
	if (regionId <= 0) {
	    Exception e = new Exception("regionId error ! regionId = " + regionId);
	    return new JsonResult(2, e);
	}
	int userId = getUserIdByOpenId(thirdType, openId);
	UserDTO userDto = userService.getUserDTOById(userId);
	// 分配顾问
	if (adviserId == 0) {
		boolean isChoose = true;
		if(userDto.getRecommendUserDO() != null && userDto.getRecommendUserDO().getAdviserId() != null
			    && userDto.getRecommendUserDO().getAdviserId() > 0) {
			adviserId = userDto.getRecommendUserDO().getAdviserId();
			AdviserDTO aDto = adviserService.getAdviserById(adviserId);
			isChoose = aDto == null || "DISABLED".equals(aDto.getState());
		}
	    if (isChoose) {
		List<AdviserDTO> adviserDtoList = adviserService.selectByRegionId(regionId);
		Integer acNum = adviserCache.get(regionId);
		// Random random = new Random();
		if (adviserDtoList != null && adviserDtoList.size() > 0) {
		    // int r = random.nextInt(adviserDtoList.size());
		    // adviserId = adviserDtoList.get(r).getId();
		    if (acNum == null) {
			acNum = new Random().nextInt(adviserDtoList.size());
		    }
		    if (acNum >= adviserDtoList.size())
			acNum = 0;
		    adviserId = adviserDtoList.get(acNum++).getId();
		    adviserCache.put(regionId, acNum);
		} else {
		    Exception e = new Exception("该区域没有可分配顾问 ");
		    return new JsonResult(3, e);
		}
	    }
	}
	userDto.setRegionId(regionId);
	userDto.setAdviserId(adviserId);
	if (!userService.updateUser(userDto)) {
	    Exception e = new Exception("更新用户信息失败");
	    return new JsonResult(6, e);
	}
	try{
	    sendMailToAdviserByNewUser(userId);    
	}catch(Exception e){
	    Log.info("sendMailToAdviserByNewUser error ! userId="+userId);
	}
	
	return new JsonResult(true);
    }
    @RequestMapping("/getIntroducer")
    @ResponseBody
    public JsonResult getIntroducerList(String openId, String thirdType) throws Exception {
	if (StringUtil.isEmpty(thirdType)) {
	    Exception e = new Exception("thirdTyp不能空");
	    return new JsonResult(2, e);
	}
	if (StringUtil.isEmpty(openId)) {
	    Exception e = new Exception("openId不能空");
	    return new JsonResult(2, e);
	}
	int userId = getUserIdByOpenId(thirdType, openId);
	Set<UserDTO> introducerSet = userService.getIntroducer(userId);
	return new JsonResult(introducerSet);
    }

    @RequestMapping("/getUser")
    @ResponseBody
    public JsonResult islogin(String openId, String thirdType) throws Exception {
	if (StringUtil.isEmpty(thirdType)) {
	    Exception e = new Exception("thirdTyp不能空");
	    return new JsonResult(2, e);
	}
	if (StringUtil.isEmpty(openId)) {
	    Exception e = new Exception("openId不能空");
	    return new JsonResult(2, e);
	}
	int userId = getUserIdByOpenId(thirdType, openId);
	UserDTO userDto = userService.getUserDTOById(userId);
	return new JsonResult(userDto);
    }

    @RequestMapping("/showBalance")
    @ResponseBody
    public JsonResult showBalance(String openId, String thirdType) throws Exception {
	if (StringUtil.isEmpty(thirdType)) {
	    Exception e = new Exception("thirdTyp不能空");
	    return new JsonResult(2, e);
	}
	if (StringUtil.isEmpty(openId)) {
	    Exception e = new Exception("openId不能空");
	    return new JsonResult(2, e);
	}
	int userId = getUserIdByOpenId(thirdType, openId);
	UserDTO userDto2 = userService.getUserDTOById(userId);
	return new JsonResult(userDto2.getBalance());
    }

    @RequestMapping("/showBalancePayMoney")
    @ResponseBody
    public JsonResult showPayByBalanceMoney(String openId, String thirdType) throws Exception {
	if (StringUtil.isEmpty(thirdType)) {
	    Exception e = new Exception("thirdTyp不能空");
	    return new JsonResult(2, e);
	}
	if (StringUtil.isEmpty(openId)) {
	    Exception e = new Exception("openId不能空");
	    return new JsonResult(2, e);
	}
	int userId = getUserIdByOpenId(thirdType, openId);
	return new JsonResult(userService.getPayByBalanceMoney(userId));
    }

    public boolean sendMailToAdviserByNewUser(int userId) throws ServiceException {
	if (userId <= 0) {
	    ServiceException se = new ServiceException("userId error ! userId = " + userId);
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	UserDTO userDto = userService.getUserDTOById(userId);
	String nickname = userDto.getNickname();
	String username = userDto.getUsername();
	String phone = userDto.getPhone();
	AdviserDTO adviserDto = userDto.getAdviserDto();
	if (adviserDto == null) {
	    ServiceException se = new ServiceException("adviserDto not found !userId = " + userId);
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	String title = "新学生_" + nickname;
	String recommendName;
	if (userDto.getRecommendUserDO() == null) {
	    recommendName = "无";
	} else {
	    recommendName = userDto.getRecommendUserDO().getNickname();
	}
	String content = "您的新客户:" + nickname + "<br/>微信号:" + username + "<br/>手机号:" + phone + "<br/>分享者:"
		+ recommendName;
	return MailUtil.sendMail(adviserDto.getEmail(), title, content);
    }
}