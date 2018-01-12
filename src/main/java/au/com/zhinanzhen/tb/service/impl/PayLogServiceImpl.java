package au.com.zhinanzhen.tb.service.impl;

import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

import au.com.zhinanzhen.tb.dao.OrderDAO;
import au.com.zhinanzhen.tb.dao.PayLogDAO;
import au.com.zhinanzhen.tb.dao.UserDAO;
import au.com.zhinanzhen.tb.dao.pojo.OrderDO;
import au.com.zhinanzhen.tb.dao.pojo.PayLogDO;
import au.com.zhinanzhen.tb.dao.pojo.UserDO;
import au.com.zhinanzhen.tb.service.PayLogService;
import au.com.zhinanzhen.tb.service.PayTypeEnum;
import au.com.zhinanzhen.tb.service.ServiceException;
@Service("payLogService")
public class PayLogServiceImpl implements PayLogService {
    @Resource
    OrderDAO orderDAO;
    @Resource
    UserDAO userDAO;
    @Resource
    PayLogDAO payLogDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPayLog(int userId, int orderId, String payType, String payCode, double payAmount, Date payDate)
	    throws ServiceException {
	if (userId < 0) {
	    ServiceException se = new ServiceException("userId < 0 !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (orderId < 0) {
	    ServiceException se = new ServiceException("orderId < 0 !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (StringUtil.isEmpty(payType) || PayTypeEnum.get(payType) == null) {
	    ServiceException se = new ServiceException("payType error !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (StringUtil.isEmpty(payCode)) {
	    ServiceException se = new ServiceException("payCode is null !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (StringUtil.isEmpty(payCode)) {
	    ServiceException se = new ServiceException("payCode is null !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (payAmount < 0) {
	    ServiceException se = new ServiceException("payAmount < 0 !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (payDate == null) {
	    payDate = new Date();
	}
	OrderDO order = orderDAO.selectByPrimaryKey(orderId);
	UserDO userDo = userDAO.getUserById(userId);
	if (order == null) {
	    ServiceException se = new ServiceException("order not found!orderId = " + orderId);
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	if (userDo == null) {
	    ServiceException se = new ServiceException("userDo not found!userId = " + userId);
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	if (order.getUserId() != userId) {
	    ServiceException se = new ServiceException(
		    "userId is not order's userId  " + "userId= " + userId + ",orderId=" + orderId);
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	PayLogDO payLogDo = new PayLogDO();
	payLogDo.setUserId(userId);
	payLogDo.setOrderId(orderId);
	payLogDo.setPayType(payType);
	payLogDo.setPayCode(payCode);
	payLogDo.setPayAmount(new BigDecimal(payAmount));
	payLogDo.setPayDate(payDate);
	int i = payLogDAO.insert(payLogDo);
	if(i==0){
	    ServiceException se = new ServiceException("insert payLog fail !");
	    se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
	    throw se;
	}
	return true;
    }

}
