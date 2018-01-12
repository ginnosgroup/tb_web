package au.com.zhinanzhen.tb.web;

import java.math.BigDecimal;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.esotericsoftware.minlog.Log;
import com.ikasoa.core.utils.StringUtil;

import au.com.zhinanzhen.tb.service.OrderService;
import au.com.zhinanzhen.tb.service.OrderStateEnum;
import au.com.zhinanzhen.tb.service.PayTypeEnum;
import au.com.zhinanzhen.tb.service.SubjectStateEnum;
import au.com.zhinanzhen.tb.service.UserService;
import au.com.zhinanzhen.tb.service.pojo.OrderResultDTO;
import au.com.zhinanzhen.tb.service.pojo.UserDTO;
import au.com.zhinanzhen.tb.utils.CommonUtil;
import au.com.zhinanzhen.tb.utils.ConfigService;
import au.com.zhinanzhen.tb.utils.JsonResult;

@Controller
@RequestMapping("/iosPay")
public class IosPayController extends AbstractController {
    private BraintreeGateway gateway;
    @Resource
    private OrderService orderService;
    @Resource
    private UserService userService;
    @Resource
    private ConfigService configService;

    public BraintreeGateway init() {
	gateway = new BraintreeGateway(Environment.PRODUCTION, configService.getIosMerchantId(), configService.getIosPublicKey(),
		configService.getIosPrivateKey());
	return gateway;
    }

    @RequestMapping("/token")
    @ResponseBody
    public JsonResult getToken() {
	gateway=init();
	return new JsonResult(gateway.clientToken().generate());
    }

    @RequestMapping("/pay")
    @ResponseBody
    public JsonResult pay(int orderId, String methodNonce) throws Exception {
	String date = CommonUtil.getSystemTime();
	System.out.println("orderId:" + orderId);
	System.out.println("methodNonce:" + methodNonce);
	gateway=init();
	if (orderId <= 0) {
	    Exception e = new Exception("orderId error ! orderId = " + orderId);
	    return new JsonResult(2, e);
	}
	if (StringUtil.isEmpty(methodNonce)) {
	    Exception e = new Exception("methodNonce is null !");
	    return new JsonResult(2, e);
	}
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
	if (!SubjectStateEnum.START.toString().equals(order.getSubjectResultDto().getState())) {
	    Throwable e = new Throwable("订单课程的状态错误 orderId = " + orderId);
	    return new JsonResult(4, e);
	}
	double balancePay = new BigDecimal(order.getWaitPayAmount() - order.getAmount().doubleValue())
		.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	int userId = order.getUserId();
	UserDTO userDto = userService.getUserDTOById(userId);
	if (userDto == null) {
	    Throwable e = new Throwable("订单的下单人错误,orderId = " + orderId + ",userId = " + userId);
	    return new JsonResult(4, e);
	}
	double balance = userDto.getBalance();
	if (balance < balancePay) {
	    Throwable e = new Throwable("余额已不足,请重新下单,orderId = " + orderId);
	    return new JsonResult(4, e);
	}
	double totalPrice = order.getAmount().doubleValue();
	TransactionRequest request = new TransactionRequest().amount(new BigDecimal(totalPrice + ""))
		.paymentMethodNonce(methodNonce).options().submitForSettlement(true).done();
	Result<Transaction> result = gateway.transaction().sale(request);
	System.out.println("totalPrice:" + totalPrice);
	if (result.isSuccess()) {
	    double payAmount = result.getTarget().getAmount().doubleValue();
	    String payCode = result.getTarget().getId();
	    Log.info("pay Success !orderId = " + orderId + ",payType:" + PayTypeEnum.IOSPAY.toString() + ",date:"
		    + date);
	    orderService.payOrder(orderId, PayTypeEnum.IOSPAY.toString(), payCode, payAmount);
	} else {
	    Log.warn("ios pay fail! message = " + result.getMessage());
	    System.out.println("result.error:" + result.getErrors());
	}
	return new JsonResult(result.isSuccess());
    }
}
