package au.com.zhinanzhen.tb.service.impl;

import java.math.BigDecimal;

import javax.annotation.Resource;
import javax.servlet.ServletException;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;
import au.com.zhinanzhen.tb.dao.pojo.AdviserDO;
import au.com.zhinanzhen.tb.service.AdviserStateEnum;
import au.com.zhinanzhen.tb.service.OrderService;
import au.com.zhinanzhen.tb.service.PayTypeEnum;
import au.com.zhinanzhen.tb.service.ServiceException;
import au.com.zhinanzhen.tb.service.ThirdTypeEnum;
import au.com.zhinanzhen.tb.service.UserService;
import au.com.zhinanzhen.tb.service.pojo.OrderResultDTO;
import au.com.zhinanzhen.tb.service.pojo.UserDTO;
import au.com.zhinanzhen.tb.utils.MailUtil;

/**
 * 基础服务
 * 
 * @author <a href="mailto:leisu@zhinanzhen.org">sulei</a>
 * @version 0.1
 */
public abstract class BaseService {

    protected static final Logger LOG = LoggerFactory.getLogger(BaseService.class);

    protected Mapper mapper = new DozerBeanMapper();

    /**
     * 默认起始页编码
     */
    protected static final int DEFAULT_PAGE_NUM = 0;

    /**
     * 默认每页最大条数
     */
    protected static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 事务回滚
     */
    protected void rollback() {
	TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    @Resource
    private OrderService orderService;
    @Resource
    private UserService userService;

    public boolean sendSubjectEndMail(int orderId) throws ServiceException {
	if (orderId < 0) {
	    ServiceException se = new ServiceException("orderId < 0");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	OrderResultDTO orderDto = orderService.getOrderResult(orderId);
	UserDTO userDto = orderDto.getUserDo();
	AdviserDO adviserDo = orderDto.getAdviserDo();
	if (userDto == null) {
	    ServiceException se = new ServiceException("顾客未找到！");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	if (adviserDo == null) {
	    ServiceException se = new ServiceException("顾问未找到！");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	if (AdviserStateEnum.DISABLED.toString().equals(adviserDo.getState())) {
	    ServiceException se = new ServiceException("顾问被禁用！");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	return MailUtil.sendMail(userDto.getEmail(), getSubjectEndMailSubjectToUser(orderDto, userDto),
		getSubjectEndMailContentToUser(orderDto, userDto, adviserDo))
		&& MailUtil.sendMail(adviserDo.getEmail(), getSubjectEndMailSubject(orderDto, userDto),
			getSubjectEndMailContent(orderDto, userDto));
    }

    // 课程结束邮件title(顾客)
    private String getSubjectEndMailSubjectToUser(OrderResultDTO orderDto, UserDTO userDto) {
	return orderDto.getId() + "_" + userDto.getName() + "__" + orderDto.getName() + "已结束，请及时联系顾问交尾款";
    }

    // 课程结束邮件content(顾客)
    private String getSubjectEndMailContentToUser(OrderResultDTO orderDto, UserDTO userDto, AdviserDO adviserDo) {
	double finalPayment = new BigDecimal(orderDto.getFinishPrice().doubleValue() - orderDto.getWaitPayAmount())
		.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	String payType = orderDto.getPayType();
	if (PayTypeEnum.BALANCE.toString().equals(payType)) {
	    payType = "余额支付";
	}
	return "【" + userDto.getName() + "】客户购买了【" + orderDto.getName() + "】课程。<br/>已支付金额：【"
		+ orderDto.getWaitPayAmount() + "】，支付方式：【" + payType + "】；下团单价：【" + orderDto.getCreatePrice()
		+ "】，成团单价：【" + orderDto.getFinishPrice() + "】；<br/>尾款金额：【" + finalPayment + "】，待支付剩余尾款：【" + finalPayment
		+ "】，订单状态：【" + orderDto.getState() + "】。<br/><br/>顾问信息<br/>姓名：【" + adviserDo.getName() + "】<br/>邮箱：【"
		+ adviserDo.getEmail() + "】。<br/>电话：" + adviserDo.getPhone();
    }

    // 课程结束邮件title(顾问)
    private String getSubjectEndMailSubject(OrderResultDTO orderDto, UserDTO userDto) {
	return orderDto.getId() + "_" + userDto.getName() + "__" + orderDto.getName() + "已结束，请及时联系客户交尾款";
    }

    // 课程结束邮件content(顾问)
    private String getSubjectEndMailContent(OrderResultDTO orderDto, UserDTO userDto) {
	double finalPayment = new BigDecimal(orderDto.getFinishPrice().doubleValue() - orderDto.getWaitPayAmount())
		.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//	double finalWaitPay = new BigDecimal(
//		orderDto.getFinishPrice().doubleValue() - orderDto.getWaitPayAmount() - userDo.getBalance())
//			.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	String payType = orderDto.getPayType();
	if (PayTypeEnum.BALANCE.toString().equals(payType)) {
	    payType = "余额支付";
	}
	return "【" + userDto.getName() + "】客户购买了【" + orderDto.getName() + "】课程。<br/>已支付金额：【"
		+ orderDto.getWaitPayAmount() + "】，支付方式：【" + payType + "】；下团单价：【" + orderDto.getCreatePrice()
		+ "】，成团单价：【" + orderDto.getFinishPrice() + "】；<br/>尾款金额：【" + finalPayment + "】，待支付剩余尾款：【" + finalPayment
		+ "】，订单状态：【" + orderDto.getState() + "】。<br/><br/>客户信息<br/>电话号码：【" + userDto.getPhone() + "】<br/>"
		+ userDto.getAuthType() + "号：【" + userDto.getUsername() + "】<br/>邮箱：【" + userDto.getEmail() + "】";
    }

    public boolean endPayFinishMail(int orderId) throws ServiceException {
	if (orderId < 0) {
	    ServiceException se = new ServiceException("orderId < 0");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	OrderResultDTO orderDto = orderService.getOrderResult(orderId);
	UserDTO userDto = orderDto.getUserDo();
	AdviserDO adviserDo = orderDto.getAdviserDo();
	if (userDto == null) {
	    ServiceException se = new ServiceException("顾客未找到！");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	if (adviserDo == null) {
	    ServiceException se = new ServiceException("顾问未找到！");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	if (AdviserStateEnum.DISABLED.toString().equals(adviserDo.getState())) {
	    ServiceException se = new ServiceException("顾问被禁用！");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	return MailUtil.sendMail(adviserDo.getEmail(), getPayFinishMailSubject(orderDto, userDto),
		getPayFinishMailContent(orderDto, userDto));
    }

    // 订单支付完成邮件title
    private String getPayFinishMailSubject(OrderResultDTO orderDto, UserDTO userDto) {
	return orderDto.getId() + "_" + userDto.getName() + "__" + orderDto.getName() + "，客户已交预付款，请随时关注拼团动态";
    }

    // 订单支付完成邮件content
    private String getPayFinishMailContent(OrderResultDTO orderDto, UserDTO userDto) {
	String payType = orderDto.getPayType();
	if (PayTypeEnum.BALANCE.toString().equals(payType)) {
	    payType = "余额支付";
	}
	return "【" + userDto.getName() + "】客户购买了【" + orderDto.getName() + "】课程。<br/>已支付金额：【"
		+ orderDto.getWaitPayAmount() + "】，支付方式：【" + payType + "】；下团单价：【"
		+ orderDto.getCreatePrice() + "】，订单状态：【" + orderDto.getState() + "】。<br/><br/>客户信息<br/>电话号码："
		+ userDto.getPhone() + "<br/>" + userDto.getAuthType() + "号：【" + userDto.getUsername() + "】<br/>邮箱：【"
		+ userDto.getEmail() + "】";
    }
    
    protected int getUserIdByOpenId(String thirdType, String openId) throws Exception {
	if(ThirdTypeEnum.get(thirdType)==null){
	    throw new ServletException("第三方登陆类型错误!thirdType = "+thirdType);
	}
	if(StringUtil.isEmpty(openId)){
	    throw new ServletException("openId不能空");
	}
	if (!userService.thirdVerify(thirdType, openId)) {
	    throw new ServletException("系统未找到该 openId! openId = "+openId+",登陆类型:"+thirdType);
	}
	UserDTO userDto =userService.thirdLogin(thirdType, openId);
	return userDto.getId();
    }
}
