package au.com.zhinanzhen.tb.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.esotericsoftware.minlog.Log;
import com.ikasoa.core.utils.StringUtil;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.Item;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import au.com.zhinanzhen.tb.service.pojo.OrderResultDTO;
import au.com.zhinanzhen.tb.service.pojo.UserDTO;
import au.com.zhinanzhen.tb.service.OrderService;
import au.com.zhinanzhen.tb.service.OrderStateEnum;
import au.com.zhinanzhen.tb.service.PayLogService;
import au.com.zhinanzhen.tb.service.PayTypeEnum;
import au.com.zhinanzhen.tb.service.SubjectStateEnum;
import au.com.zhinanzhen.tb.service.UserService;
import au.com.zhinanzhen.tb.utils.CommonUtil;
import au.com.zhinanzhen.tb.utils.ConfigService;
import au.com.zhinanzhen.tb.utils.JsonResult;

@CrossOrigin(origins = "*", maxAge = 3600)
@Controller
@RequestMapping("/paypal")
public class PaypalController extends AbstractController {
	private APIContext apiContext;
	@Resource
	private ConfigService configService;
	@Resource
	private OrderService orderService;
	@Resource
	private PayLogService payLogService;
	@Resource
	private UserService userService;

	@RequestMapping("/pay")
	@ResponseBody
	@Transactional(rollbackFor = Exception.class)
	public JsonResult tryPay(int orderId, String openId, String thirdType) throws Exception {
		String date = CommonUtil.getSystemTime();
		Log.info("order try pay,payType:paypal,orderId:" + orderId + ",date:" + date);
		String clientId = configService.getClientId();
		String clientSecret = configService.getClientSecret();
		String baseUrl = configService.getHost();
		String mode = configService.getMode();
		if (orderId <= 0) {
			Throwable e = new Throwable("订单号错误 !orderId = " + orderId);
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
		String name = order.getId() + "";
		int num = order.getNum();
		double totalPrice = order.getAmount().doubleValue();
		totalPrice = new BigDecimal((totalPrice + 0.3) / 0.974).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		String quantity = num + "";
		String subtotal = totalPrice + "";
		double shipping = 0;
		double tax = 0.00;
		double total = totalPrice;
		String currency = "AUD";
		String description = URLEncoder.encode(order.getId() + "", "UTF-8");
		// APIContext
		apiContext = new APIContext(clientId, clientSecret, mode);
		Payer payer = new Payer();
		payer.setPaymentMethod("paypal");
		RedirectUrls redirectUrls = new RedirectUrls();
		redirectUrls.setCancelUrl(baseUrl + "/paypal/" + orderId + "/result");
		redirectUrls.setReturnUrl(baseUrl + "/paypal/" + orderId + "/result");
		Details details = new Details().setShipping(shipping + "").setSubtotal(subtotal).setTax(tax + "");
		Amount amount = new Amount().setCurrency(currency).setTotal(total + "").setDetails(details);
		Transaction transaction = new Transaction();
		transaction.setAmount(amount).setDescription(description);
		Item item = new Item().setName(name).setQuantity(quantity).setCurrency(currency).setPrice(total + "");
		ItemList itemList = new ItemList();
		List<Item> items = new ArrayList<Item>();
		items.add(item);
		itemList.setItems(items);
		transaction.setItemList(itemList);
		List<Transaction> transactions = new ArrayList<Transaction>();
		transactions.add(transaction);
		Payment payment = new Payment().setIntent("sale").setPayer(payer).setRedirectUrls(redirectUrls)
				.setTransactions(transactions);
		try {
			Payment createdPayment = payment.create(apiContext);
			Iterator<Links> links = createdPayment.getLinks().iterator();
			while (links.hasNext()) {
				Links link = links.next();
				if (link.getRel().equalsIgnoreCase("approval_url")) {
					String payUrl = link.getHref();
					return new JsonResult(payUrl);
				}
			}
			Throwable e = new Throwable("use paypal interface fail !");
			return new JsonResult(2, e);
		} catch (PayPalRESTException e) {
			Log.error("PayPalAPI error,e=" + e.getMessage());
			e.printStackTrace();
			return new JsonResult(e);
		}
	}

	@RequestMapping(value = "/{orderIdStr}/result", method = RequestMethod.GET)
	@Transactional(rollbackFor = Exception.class)
	public void payResult(@PathVariable String orderIdStr, HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		String date = CommonUtil.getSystemTime();
		Log.info("try into this paypal result url,orderId = " + orderIdStr);
		String baseUrl = configService.getHost();
		String successUrl = baseUrl + "success.htm";
		String failUrl = baseUrl + "fail.htm";
		if (StringUtil.isEmpty(orderIdStr) || Integer.valueOf(orderIdStr) < 0) {
			Log.info("try into this url,orderId = " + orderIdStr);
			resp.sendRedirect(failUrl);
			return;
		}
		OrderResultDTO orderResultDto = orderService.getOrderResult(Integer.valueOf(orderIdStr));
		if (!OrderStateEnum.NEW.toString().equals(orderResultDto.getState())
				|| !PayTypeEnum.OTHER.toString().equals(orderResultDto.getPayType())
				|| StringUtil.isNotEmpty(orderResultDto.getPayCode())) {
			Log.info("order's state error !orderId = " + orderIdStr);
			resp.sendRedirect(failUrl);
			return;
		}
		String paymentId = req.getParameter("paymentId");
		String payerId = req.getParameter("PayerID");
		Payment payment = new Payment();
		payment.setId(paymentId);
		PaymentExecution paymentExecution = new PaymentExecution();
		paymentExecution.setPayerId(payerId);
		// 取消支付
		if (StringUtil.isEmpty(paymentId) || StringUtil.isEmpty(payerId)) {
			Log.info("paypal cancel !orderId:" + orderIdStr + ",date:" + date);
			resp.sendRedirect(failUrl);
			return;
		}
		String transactionId = null;
		try {
			// 付款成功回调
			Payment createdPayment = payment.execute(apiContext, paymentExecution);
			Log.info(createdPayment.toString());
			Log.info("paypal pay success wait change database , orderId:" + orderIdStr + ",date:" + date);
			transactionId = createdPayment.getTransactions().get(0).getRelatedResources().get(0).getSale().getId();
			double payMoney = Double.valueOf(createdPayment.getTransactions().get(0).getAmount().getTotal());
			if (StringUtil.isEmpty(transactionId)) {
				// Exception e = new Exception("transactionId is null !");
				// throw e;
				transactionId = "no Code !Date=" + date;
				Log.error("paypal's transactionId is null ,date = " + date + "orderId = " + orderIdStr);
			}
			int userId = orderResultDto.getUserId();
			int orderId = Integer.valueOf(orderIdStr);
			payLogService.addPayLog(userId, orderId, PayTypeEnum.PAYPAL.toString(), transactionId, payMoney,
					new Date());
			double balancePay = new BigDecimal(orderResultDto.getWaitPayAmount() - payMoney)
					.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			if (balancePay > 0) {
				if (!orderService.balancePay(orderId, balancePay)) {
					Exception e = new Exception("balanceDeduction fail !");
					throw e;
				}
			}
			if (!orderService.payOrder(orderId, PayTypeEnum.PAYPAL.toString(), transactionId, payMoney)) {
				Exception e = new Exception("pay success but update db fail ! orderId = " + orderId);
				throw e;
			}
			// SendEmailUtil.sendEmailByPayFinish(orderId);
			resp.sendRedirect(successUrl);
		} catch (Exception e) {
			resp.sendRedirect(failUrl);
			e.printStackTrace();
			Log.error(e.getMessage());
			return;
		}
	}

	@RequestMapping("/ipn")
	 @Transactional(rollbackFor = Exception.class)
	    public void setIpn(HttpServletRequest request, HttpServletResponse resp) throws Exception {
		String date = CommonUtil.getSystemTime();
		Log.info("paypal try access this url !data:" + date);
		Enumeration en = request.getParameterNames();
		String str = "cmd=_notify-validate";
		while (en.hasMoreElements()) {
		    String paramName = (String) en.nextElement();
		    String paramValue = request.getParameter(paramName);
		    str = str + "&" + paramName + "=" + URLEncoder.encode(paramValue);
		}
		Log.info(str);
		String ipnUrl = "https://ipnpb.paypal.com/cgi-bin/webscr";
		URL u = new URL(ipnUrl);
		URLConnection uc = u.openConnection();
		uc.setDoOutput(true);
		uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		PrintWriter pw = new PrintWriter(uc.getOutputStream());
		pw.println(str);
		pw.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
		String res = in.readLine();
		in.close();
		String name = request.getParameter("item_name1");
		String paymentStatus = request.getParameter("payment_status");
		String paymentAmount = request.getParameter("mc_gross");
		String paymentCurrency = request.getParameter("mc_currency");
		String txnId = request.getParameter("txn_id");
		String receiverEmail = request.getParameter("receiver_email");
		String payerEmail = request.getParameter("payer_email");
		String parentTxnId = request.getParameter("parent_txn_id");
		int orderId = Integer.valueOf(name);
		OrderResultDTO orderResultDto=orderService.getOrderResult(orderId);
		int userId=orderResultDto.getUserId();
		double payMoney = orderResultDto.getAmount().doubleValue();
		if (res.equals("VERIFIED")) {
		    Log.info("VERIFIED");
		    // 支付完成未更新数据库
		    if ("Completed".equals(paymentStatus) && PayTypeEnum.OTHER.toString().equals(orderResultDto.getPayType())
			    && orderResultDto.getPayCode() == null) {
		    	payLogService.addPayLog(userId, orderId, PayTypeEnum.PAYPAL.toString(), txnId, payMoney,
						new Date());
				double balancePay = new BigDecimal(orderResultDto.getWaitPayAmount() - payMoney)
						.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
				if (balancePay > 0) {
					if (!orderService.balancePay(orderId, balancePay)) {
						Exception e = new Exception("balanceDeduction fail !");
						throw e;
					}
				}
				if (!orderService.payOrder(orderId, PayTypeEnum.PAYPAL.toString(), txnId, payMoney)) {
					Exception e = new Exception("pay success but update db fail ! orderId = " + orderId);
					throw e;
				}
		    }
		} else if (res.equals("INVALID")) {
		    Log.info("INVALID");
		} else {
		    Log.info("error !");
		}
	}
}