package au.com.zhinanzhen.tb.web;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.esotericsoftware.minlog.Log;
import com.ikasoa.core.utils.StringUtil;

import au.com.zhinanzhen.tb.service.OrderService;
import au.com.zhinanzhen.tb.service.OrderStateEnum;
import au.com.zhinanzhen.tb.service.PayLogService;
import au.com.zhinanzhen.tb.service.PayTypeEnum;
import au.com.zhinanzhen.tb.service.SubjectStateEnum;
import au.com.zhinanzhen.tb.service.UserService;
import au.com.zhinanzhen.tb.service.pojo.OrderResultDTO;
import au.com.zhinanzhen.tb.service.pojo.UserDTO;
import au.com.zhinanzhen.tb.utils.CommonUtil;
import au.com.zhinanzhen.tb.utils.ConfigService;
import au.com.zhinanzhen.tb.utils.HttpRequestGenerator;
import au.com.zhinanzhen.tb.utils.HttpRequestResult;
import au.com.zhinanzhen.tb.utils.HttpUtil;
import au.com.zhinanzhen.tb.utils.JsonResult;
import au.com.zhinanzhen.tb.utils.StringEncrypt;

@CrossOrigin(origins = "*", maxAge = 3600)
@Controller
@RequestMapping("/royalPay")
public class RoyalPayController extends AbstractController {
    @Resource
    private ConfigService configService;
    @Resource
    private OrderService orderService;
    @Resource
    private PayLogService payLogService;
    @Resource
    private UserService userService;

    public String getSign() {
	String nonceStr = configService.getNonceStr();
	String partneCode = configService.getPartneCode();
	String credentialCode = configService.getCredentialCode();
	String str = partneCode + "&" + new Date().getTime() + "&" + nonceStr + "&" + credentialCode;
	return StringEncrypt.Encrypt(str);
    }

    private String queryParams() {
	long time = System.currentTimeMillis();
	String sign = getSign();
	String nonceStr = configService.getNonceStr();
	return "time=" + time + "&nonce_str=" + nonceStr + "&sign=" + sign;
    }

    @RequestMapping("/getRate")
    @ResponseBody
    public JsonResult getRate() throws Exception {
	String partneCode = configService.getPartneCode();
	String url = "https://mpay.royalpay.com.au/api/v1.0/gateway/partners/" + partneCode + "/exchange_rate" + "?"
		+ queryParams();
	Map<String, String> map = new HashMap<String, String>();
	String result = HttpUtil.httpRequest(url, map);
	JSONObject object = JSON.parseObject(result);
	if (object.get("return_code") != null && "SUCCESS".equals(object.get("return_code"))) {
	    return new JsonResult("rate:" + object.get("rate"));
	} else {
	    Throwable e = new Throwable((String) object.get("message"));
	    return new JsonResult(2, e);
	}
    }
    @RequestMapping("/pay")
    @ResponseBody
    public JsonResult tryPay(int orderId, @RequestHeader(name = "User-Agent") String userAgent, String openId,
	    String thirdType) throws Exception {
	String orderIdStr = "X" + orderId;
	String date = CommonUtil.getSystemTime();
	Log.info("order try pay! orderId:" + orderId + ",payType:WECHAT,date:" + date);
	String partneCode = configService.getPartneCode();
	String baseUrl = configService.getHost();
	String createUrl;
	if (userAgent.toLowerCase().contains("micromessenger")) {
	    createUrl = "https://mpay.royalpay.com.au/api/v1.0/wechat_jsapi_gateway/partners/" + partneCode + "/orders/"
		    + orderIdStr + "?" + queryParams();
	} else {
	    createUrl = "https://mpay.royalpay.com.au/api/v1.0/gateway/partners/" + partneCode + "/orders/" + orderIdStr
		    + "?" + queryParams();
	}
	if (orderId < 0) {
	    Throwable e = new Throwable("订单号错误 !");
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
	UserDTO userDto = userService.getUserDTOById(userId);
	OrderResultDTO order = orderService.getOrderResult(orderId);
	if (order == null) {
	    Throwable e = new Throwable("订单未找到 ! orderId = " + orderId);
	    return new JsonResult(4, e);
	}
	if (!OrderStateEnum.NEW.toString().equals(order.getState())
		|| !PayTypeEnum.OTHER.toString().equals(order.getPayType()) || order.getPayCode() != null) {
	    Throwable e = new Throwable("订单状态错误 ! orderId = " + orderId);
	    return new JsonResult(4, e);
	}
	if (userId != order.getUserId()) {
	    Throwable e = new Throwable("登陆用户和下单用户不一致 orderId = " + orderId + ",login's userId :" + userId);
	    return new JsonResult(4, e);
	}
	if (!SubjectStateEnum.START.toString().equals(order.getSubjectResultDto().getState())) {
	    Throwable e = new Throwable("订单课程的状态错误 orderId = " + orderId);
	    return new JsonResult(4, e);
	}
	double balancePay = new BigDecimal(order.getWaitPayAmount() - order.getAmount().doubleValue())
		.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	double balance = userDto.getBalance();
	if (balance < balancePay) {
	    Throwable e = new Throwable("余额已不足,请重新下单,orderId = " + orderId);
	    return new JsonResult(4, e);
	}
	int totalPrice = new BigDecimal((order.getAmount().doubleValue() * 100)).intValue();
	JSONObject param = new JSONObject();
	param.put("price", totalPrice);
	param.put("description", "课程团购:" + orderId);
	param.put("notify_url", baseUrl + "/royalPay/callback");
	param.put("operator", userId);
	HttpRequestResult result = new HttpRequestGenerator(createUrl, RequestMethod.PUT).setJSONEntity(param)
		.execute();
	if (result.isSuccess()) {
	    JSONObject res = result.getResponseContentJSONObj();
	    String resultUrl = baseUrl + "/royalPay/" + orderIdStr + "/result";
	    String payUrl = res.getString("pay_url") + "?" + queryParams() + "&redirect=" + resultUrl;
	    return new JsonResult(payUrl);
	}
	return new JsonResult(new Exception("请重试 !"));
    }
    @RequestMapping(value = "/{orderIdStr}/result", method = RequestMethod.GET)
    public void resultPay(@PathVariable String orderIdStr, HttpServletResponse resp) throws Exception {
	String partneCode = configService.getPartneCode();
	String date = CommonUtil.getSystemTime();
	String baseUrl = configService.getHost();
	String successUrl = baseUrl + "/success.htm";
	String failUrl = baseUrl + "/fail.htm";
	String url = "https://mpay.royalpay.com.au/api/v1.0/gateway/partners/" + partneCode + "/orders/" + orderIdStr
		+ "?" + queryParams();
	int i = 0;
	while (true) {
	    HttpRequestResult result = new HttpRequestGenerator(url, RequestMethod.GET).execute();
	    if (result.isSuccess()) {
		JSONObject res = result.getResponseContentJSONObj();
		if ("PAY_SUCCESS".equals(res.getString("result_code"))) {
		    int orderId = Integer.valueOf(orderIdStr.substring(1));
		    OrderResultDTO orderResultDto = orderService.getOrderResult(orderId);
		    if (!PayTypeEnum.OTHER.toString().equals(orderResultDto.getPayType())
			    && orderResultDto.getPayCode() != null
			    && !OrderStateEnum.NEW.toString().equals(orderResultDto.getState())) {
			resp.sendRedirect(successUrl);
			return;
		    }
		    Log.info(res.getString("result_code"));
		    Log.info("return|total_fee:" + res.getIntValue("total_fee"));
		    Log.info("return|real_fee:" + res.getIntValue("real_fee"));
		    Log.info("return|rate:" + res.getDoubleValue("rate"));
		    Log.info("return|pay_time:" + res.getString("pay_time"));
		    Log.info("return|create_time:" + res.getString("currency"));
		    Log.info("return|partner_order_id:" + res.getString("partner_order_id"));
		    Log.info("return|payCode:" + res.getString("order_id"));
		    String payCode = res.getString("order_id");
		    double fee = res.getIntValue("total_fee");
		    double payMoney = new BigDecimal(fee / 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		    int userId = orderResultDto.getUserId();
		    payLogService.addPayLog(userId, orderId, PayTypeEnum.WECHAT.toString(), payCode, payMoney,
			    new Date());
		    double balancePay = new BigDecimal(orderResultDto.getWaitPayAmount() - payMoney)
			    .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		    if (balancePay > 0) {
			if (!orderService.balancePay(orderId, balancePay)) {
			    Exception e = new Exception("balancePay fail !");
			    throw e;
			}
		    }
		    try {
			if (!orderService.payOrder(orderId, PayTypeEnum.WECHAT.toString(), payCode, payMoney)) {
			    Log.info("WECHAT pay success !orderId:" + orderId + ",date:" + date + ",chDBType:return");
			    resp.sendRedirect(successUrl);
			    return;
			} else {
			    Log.error("ERROR ! WECHAT PAY SUCCESS BUT UPDATE DATABASES FAIL! orderId=" + orderId);
			    resp.sendRedirect(failUrl);
			    return;
			}
		    } catch (Exception e) {
			resp.sendRedirect(failUrl);
			return;
		    }
		} else {
		    Log.info("pay fail,orderId=" + orderIdStr);
		    resp.sendRedirect(failUrl);
		    return;
		}
	    } else {
		if (i > 10) {
		    Log.error("connection fail,orderId=" + orderIdStr);
		    resp.sendRedirect(failUrl);
		    return;
		}
		Thread.sleep(200);
		i++;
	    }
	}
    }

    @RequestMapping(value = "/callback", method = RequestMethod.POST)
    public JSONObject callback(@RequestBody JSONObject entity) throws Exception {
	String date = CommonUtil.getSystemTime();
	Log.info("Royalpay try access this url !data:" + date);
	String partneCode = configService.getPartneCode();
	String orderIdStr = (String) entity.get("partner_order_id");
	String url = "https://mpay.royalpay.com.au/api/v1.0/gateway/partners/" + partneCode + "/orders/" + orderIdStr
		+ "?" + queryParams();
	HttpRequestResult result = new HttpRequestGenerator(url, RequestMethod.GET).execute();
	if (result.isSuccess()) {
	    JSONObject res = result.getResponseContentJSONObj();
	    if ("PAY_SUCCESS".equals(res.getString("result_code"))) {
		int orderId = Integer.valueOf(orderIdStr.substring(1));
		OrderResultDTO orderResultDto = orderService.getOrderResult(orderId);
		if (!PayTypeEnum.OTHER.toString().equals(orderResultDto.getPayType())
			&& orderResultDto.getPayCode() != null
			&& !OrderStateEnum.NEW.toString().equals(orderResultDto.getState())) {
		    JSONObject resp = new JSONObject();
		    resp.put("return_code", "SUCCESS");
		    return resp;
		}
		Log.info(res.getString("result_code"));
		Log.info("callback|total_fee:" + res.getIntValue("total_fee"));
		Log.info("callback|real_fee:" + res.getIntValue("real_fee"));
		Log.info("callback|rate:" + res.getDoubleValue("rate"));
		Log.info("callback|pay_time:" + res.getString("pay_time"));
		Log.info("callback|create_time:" + res.getString("currency"));
		Log.info("callback|partner_order_id:" + res.getString("partner_order_id"));
		Log.info("callback|payCode:" + res.getString("order_id"));
		String payCode = res.getString("order_id");
		int userId = orderResultDto.getUserId();
		double fee = res.getIntValue("total_fee");
		double payMoney = new BigDecimal(fee / 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		payLogService.addPayLog(userId, orderId, PayTypeEnum.WECHAT.toString(), payCode, payMoney, new Date());
		double balancePay = new BigDecimal(orderResultDto.getWaitPayAmount() - payMoney)
			.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		if (balancePay > 0) {
		    if (!orderService.balancePay(orderId, balancePay)) {
			Exception e = new Exception("balancePay fail !");
			throw e;
		    }
		}
		if (orderService.payOrder(orderId, PayTypeEnum.WECHAT.toString(), payCode, payMoney)) {
		    Log.info("WECHAT pay SUCCESS !orderId:" + orderId + ",date:" + date + ",chDBType:callback");
		} else {
		    Log.error("WECHAT PAY SUCCESS BUT UPDATE DATABASES FAIL! orderId=" + orderId);
		}
	    } else {
		Log.info("payfail orderId=" + orderIdStr);
	    }
	}
	JSONObject resp = new JSONObject();
	resp.put("return_code", "SUCCESS");
	return resp;
    }
}