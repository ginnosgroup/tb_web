package au.com.zhinanzhen.tb.web;

import java.util.Date;
import java.util.HashMap;
import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.esotericsoftware.minlog.Log;
import com.ikasoa.core.utils.StringUtil;

import au.com.zhinanzhen.tb.service.pojo.WxgzDTO;
import au.com.zhinanzhen.tb.utils.ConfigService;
import au.com.zhinanzhen.tb.utils.HttpUtil2;
import au.com.zhinanzhen.tb.utils.JsonResult;
import au.com.zhinanzhen.tb.utils.Sha1;

@CrossOrigin(origins = "*", maxAge = 3600)
@Controller
@RequestMapping("/wxgz")
public class WeixinGzptController extends AbstractController {
    @Resource
    MemcachedClient memcachedClient;
    @Resource
    private ConfigService configService;

    public String getWXToken(int i) throws Exception {
	if (i > 5) {
	    Exception e = new Exception("获取 token 失败,尝试 " + i + "次 未成功");
	    throw e;
	}
	String appId = configService.getAppId();
	String secret = configService.getSecret();
	String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
	url = url.replace("APPID", appId).replace("APPSECRET", secret);
	String str = HttpUtil2.httpGet(url, new HashMap<String, String>());
	JSONObject json = JSON.parseObject(str);
	String accessToken = json.getString("access_token");
	if (StringUtil.isNotEmpty(accessToken)) {
	    memcachedClient.set("accessTokenTB", accessToken);
	    return accessToken;
	} else {
	    i++;
	    Thread.sleep(200);
	    return getWXToken(i);
	}
    }

    public String getHttpTicket(int i) throws Exception {
	if (i > 5) {
	    Exception e = new Exception("获取 ticket 失败,尝试 " + i + "次 未成功");
	    throw e;
	}
	if (memcachedClient.get("jsapTicketTB") != null) {
	    String jsapTicket = (String) memcachedClient.get("jsapTicketTB");
	    return jsapTicket;
	}
	String accessToken;
	if (memcachedClient.get("accessTokenTB") != null) {
	    accessToken = (String) memcachedClient.get("accessTokenTB");
	} else {
	    accessToken = getWXToken(0);
	}
	String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi";
	url = url.replace("ACCESS_TOKEN", accessToken);
	String jsonStr = HttpUtil2.httpGet(url, new HashMap<String, String>());
	System.out.println("jsonStr:" + jsonStr);
	JSONObject json = JSON.parseObject(jsonStr);
	String jsapTicket = json.getString("ticket");
	System.out.println("jsapTicketTB:" + jsapTicket);
	System.out.println("accessTokenTB:" + accessToken);
	if (StringUtil.isNotEmpty(jsapTicket)) {
	    memcachedClient.set("jsapTicketTB", jsapTicket);
	    return jsapTicket;
	} else {
	    memcachedClient.del("accessTokenTB");
	    i++;
	    Thread.sleep(200);
	    return getHttpTicket(i);
	}
    }
    public String getSign(String noncestr, Long timestamp, String url) throws Exception {
	if (StringUtil.isEmpty(noncestr) || timestamp == null || timestamp < 0) {
	    throw new Exception("noncestr or timestamp is null");
	}
	String ticket = getHttpTicket(0);
	String str = "jsapi_ticket=" + ticket + "&noncestr=" + noncestr + "&timestamp=" + timestamp + "&url=" + url;
	Log.info("str:" + str);
	String result = Sha1.getSign(str);
	Log.info("result:" + result);
	return result;
    }
    @RequestMapping("/config")
    @ResponseBody
    public JsonResult WxConfig(String relativeUrl){
	if (StringUtil.isEmpty(relativeUrl)) {
	    return new JsonResult(new Exception("relativeUrl is null"));
	}
	String appId = configService.getAppId();
	Long timestamp = new Date().getTime() / 1000;
	String nonceStr = configService.getNonceStr();
	String fullUrl = configService.getHost() + relativeUrl;
	fullUrl = relativeUrl;
	Log.info("url:" + fullUrl);
	String sign = null;
	try {
	    sign = getSign(nonceStr, timestamp, fullUrl);
	} catch (Exception e) {
	    Log.info(e.getMessage());
	}
	WxgzDTO wxgzDto = new WxgzDTO();
	wxgzDto.setAppId(appId);
	wxgzDto.setNonceStr(nonceStr);
	wxgzDto.setTimestamp(timestamp);
	wxgzDto.setSignature(sign);
	return new JsonResult(wxgzDto);
    }
    @RequestMapping("/clean")
    @ResponseBody
    public JsonResult clean() throws Exception {
	memcachedClient.del("accessToken");
	memcachedClient.del("sapTicket");
	return new JsonResult(true);
    }
    @RequestMapping("/thirdLogin")
    @ResponseBody
    public synchronized JsonResult login(String url,String recommendOpenid) throws Exception {
	if (StringUtil.isEmpty(url)) {
	    Exception e = new Exception("url is null !");
	    return new JsonResult(e);
	}
	String takenUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect";
	String appId = configService.getAppId();
	String returnUrl = configService.getHost()+"/user/wxresult";
	String scope = "snsapi_userinfo";
	String state;
	if(recommendOpenid ==null){
	    state=url;
	}else{
	    state = url+",recommendOpenId:"+recommendOpenid;
	}
	takenUrl = takenUrl.replace("APPID", appId).replace("REDIRECT_URI", returnUrl).replace("SCOPE", scope)
		.replace("STATE", state);
	return new JsonResult(takenUrl);
    }
    
    @RequestMapping("/testMemchache")
    @ResponseBody
    public JsonResult TestGetTiket() throws Exception {
	String sapTicketTB =(String) memcachedClient.get("jsapTicketTB");
	return new JsonResult("sapTicketTB:"+sapTicketTB
		);
    }
}
