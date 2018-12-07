package au.com.zhinanzhen.tb.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esotericsoftware.minlog.Log;
import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

import au.com.zhinanzhen.tb.dao.AdviserDAO;
import au.com.zhinanzhen.tb.dao.OrderDAO;
import au.com.zhinanzhen.tb.dao.PayLogDAO;
import au.com.zhinanzhen.tb.dao.RegionDAO;
import au.com.zhinanzhen.tb.dao.SubjectDAO;
import au.com.zhinanzhen.tb.dao.SubjectPriceDAO;
import au.com.zhinanzhen.tb.dao.UserDAO;
import au.com.zhinanzhen.tb.dao.pojo.AdviserDO;
import au.com.zhinanzhen.tb.dao.pojo.OrderDO;
import au.com.zhinanzhen.tb.dao.pojo.PayLogDO;
import au.com.zhinanzhen.tb.service.pojo.OrderResultDTO;
import au.com.zhinanzhen.tb.dao.pojo.RegionDO;
import au.com.zhinanzhen.tb.dao.pojo.SubjectDO;
import au.com.zhinanzhen.tb.dao.pojo.UserDO;
import au.com.zhinanzhen.tb.service.AdviserService;
import au.com.zhinanzhen.tb.service.OrderService;
import au.com.zhinanzhen.tb.service.OrderStateEnum;
import au.com.zhinanzhen.tb.service.PayTypeEnum;
import au.com.zhinanzhen.tb.service.ServiceException;
import au.com.zhinanzhen.tb.service.SubjectService;
import au.com.zhinanzhen.tb.service.SubjectStateEnum;
import au.com.zhinanzhen.tb.service.SubjectTypeEnum;
import au.com.zhinanzhen.tb.service.UserService;
import au.com.zhinanzhen.tb.service.pojo.SubjectResultDTO;
import au.com.zhinanzhen.tb.service.pojo.UserDTO;
import au.com.zhinanzhen.tb.utils.CommonUtil;
import au.com.zhinanzhen.tb.utils.ConfigService;

@Service("orderService")
public class OrderServiceImpl extends BaseService implements OrderService {
    @Resource
    private OrderDAO orderDAO;
    @Resource
    private SubjectDAO subjectDAO;
    @Resource
    private SubjectService subjectService;
    @Resource
    private UserDAO userDAO;
    @Resource
    private RegionDAO regionDAO;
    @Resource
    private AdviserDAO adviserDAO;
    @Resource
    private SubjectPriceDAO subjectPriceDAO;
    @Resource
    private ConfigService configService;
    @Resource
    private PayLogDAO payLogDAO;
    @Resource
    private AdviserService adviserService;
    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addOrder(int subjectId, int num, int userId, double balancePayAmount, Integer introducerUserId,
	    int regionId) throws ServiceException {
	if (subjectId < 0) {
	    ServiceException se = new ServiceException("subjectId < 0");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (num <= 0) {
	    ServiceException se = new ServiceException("num <= 0");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (userId <= 0) {
	    ServiceException se = new ServiceException("userId <= 0");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (regionId <= 0) {
	    ServiceException se = new ServiceException("regionId <= 0");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	SubjectDO subjectDo = subjectDAO.selectById(subjectId);
	if (subjectDo == null) {
	    ServiceException se = new ServiceException("subjectDo not found");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	Date endDate = subjectDo.getEndDate();
	Date startDate = subjectDo.getStartDate();
	Date nowDate = new Date();
	if (nowDate.getTime() > endDate.getTime()) {
	    ServiceException se = new ServiceException("this subject is end");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	if (nowDate.getTime() < startDate.getTime()) {
	    ServiceException se = new ServiceException("this subject not begin");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	UserDO userDo = userDAO.getUserById(userId);
	if (userDo == null) {
	    ServiceException se = new ServiceException("userDo not found !");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	if (userDo.getRegionId() == 0 || StringUtil.isEmpty(userDo.getName())
		|| StringUtil.isEmpty(userDo.getPhone())) {
	    ServiceException se = new ServiceException("the order's user's info not complete !");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	int adviserId = userDo.getAdviserId();
	if (balancePayAmount > userDo.getBalance()) {
	    ServiceException se = new ServiceException("balancePayAmount > user's.balance");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	if (introducerUserId != null) {
	    if (introducerUserId == userId) {
		introducerUserId = null;
	    }
	    UserDO introducerUserDo = userDAO.getUserById(introducerUserId);
	    if (introducerUserDo == null) {
		ServiceException se = new ServiceException("introducer not found !");
		se.setCode(ErrorCodeEnum.DATA_ERROR.code());
		throw se;
	    }
	}

	String name = subjectDo.getName();
	BigDecimal createPrice = new BigDecimal(subjectService.getSubjectNextMoney(subjectId, regionId));
	double waitPayAmount = new BigDecimal(subjectDo.getPreAmount() * num).setScale(2, BigDecimal.ROUND_HALF_UP)
		.doubleValue();
	double thirdPayAmount = new BigDecimal(waitPayAmount - balancePayAmount).setScale(2, BigDecimal.ROUND_HALF_UP)
		.doubleValue();
	// 余额不够预付款的情况:
	if (thirdPayAmount > 0) {
	    OrderDO orderDo = new OrderDO();
	    orderDo.setName(name);
	    orderDo.setState(OrderStateEnum.NEW.toString());
	    orderDo.setSubjectId(subjectId);
	    orderDo.setNum(num);
	    orderDo.setPayType(PayTypeEnum.OTHER.toString());
	    orderDo.setUserId(userId);
	    orderDo.setRegionId(regionId);
	    orderDo.setPayAmount(new BigDecimal(0));
	    orderDo.setIntroducerUserId(introducerUserId);
	    orderDo.setAdviserId(adviserId);
	    orderDo.setCreatePrice(createPrice);
	    orderDo.setAmount(new BigDecimal(thirdPayAmount));
	    if (orderDAO.insert(orderDo) != 1) {
		ServiceException se = new ServiceException("inset order fail !");
		se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
		throw se;
	    }
	    return orderDo.getId();
	} else {
	    // 余额够预付款，则抵扣余额
	    OrderDO orderDo = new OrderDO();
	    orderDo.setName(name);
	    orderDo.setState(OrderStateEnum.WAIT.toString());
	    orderDo.setSubjectId(subjectId);
	    orderDo.setNum(num);
	    orderDo.setAmount(new BigDecimal(0));
	    orderDo.setPayType(PayTypeEnum.BALANCE.toString());
	    orderDo.setPayCode(waitPayAmount + "");
	    orderDo.setUserId(userId);
	    orderDo.setRegionId(regionId);
	    orderDo.setPayAmount(new BigDecimal(0));
	    orderDo.setIntroducerUserId(introducerUserId);
	    orderDo.setAdviserId(adviserId);
	    orderDo.setCreatePrice(createPrice);
	    orderDo.setPayDate(new Date());
	    if (orderDAO.insert(orderDo) != 1) {
		ServiceException se = new ServiceException("inset order fail !");
		se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
		throw se;
	    }
	    // 推荐人余额 +5
	    double reward = configService.getIntroduceAward();
	    if (orderDo.getIntroducerUserId() != null) {
		UserDO introducer = userDAO.getUserById(orderDo.getIntroducerUserId());
		introducer.setBalance(new BigDecimal(introducer.getBalance() + reward)
			.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		if (!userDAO.update(introducer)) {
		    ServiceException se = new ServiceException("update introducer fail !orderId = " + orderDo.getId());
		    se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
		    throw se;
		}
	    }
	    // 被推荐人余额扣除并生成支付日志
	    double balancePayMoney = waitPayAmount;
	    double remainBalance = new BigDecimal(userDo.getBalance() - balancePayMoney)
		    .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	    if (remainBalance < 0) {
		ServiceException se = new ServiceException("remainBalance < 0! orderId = " + orderDo.getId());
		se.setCode(ErrorCodeEnum.DATA_ERROR.code());
		throw se;
	    }
	    userDo.setBalance(remainBalance);
	    if (!userDAO.update(userDo)) {
		ServiceException se = new ServiceException("update userDo fail !userId = " + userId);
		se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
		throw se;
	    }
	    PayLogDO payLogDo = new PayLogDO();
	    payLogDo.setOrderId(orderDo.getId());
	    payLogDo.setPayCode(waitPayAmount + "");
	    payLogDo.setPayAmount(new BigDecimal(balancePayMoney));
	    payLogDo.setPayDate(new Date());
	    payLogDo.setPayType(PayTypeEnum.BALANCE.toString());
	    payLogDo.setUserId(userId);
	    if (payLogDAO.insert(payLogDo) != 1) {
		ServiceException se = new ServiceException("add payLog fail !orderId = " + orderDo.getId());
		se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
		throw se;
	    }
	    if (!endPayFinishMail(orderDo.getId())) {
		Log.warn("send payFinishMail fail! orderId = " + orderDo.getId());
	    }
	    String date = CommonUtil.getSystemTime();
	    Log.info("order balancePay success !orderId = " + orderDo.getId() + "date:" + date);
	    return orderDo.getId();
	}
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(int orderId, String payType, String payCode, Double payMoney) throws ServiceException {
	if (orderId < 0) {
	    ServiceException se = new ServiceException("orderId error !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (StringUtil.isEmpty(payType) || PayTypeEnum.get(payType) == null
		|| PayTypeEnum.OTHER.toString().equals(payType)) {
	    ServiceException se = new ServiceException("payType error !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (payCode == null) {
	    ServiceException se = new ServiceException("payCode is null !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (payMoney == null || payMoney < 0) {
	    ServiceException se = new ServiceException("payMoney is error !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	OrderDO orderDo = orderDAO.selectByPrimaryKey(orderId);
	if (orderDo == null) {
	    ServiceException se = new ServiceException("orderDo not found !");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	if (!OrderStateEnum.NEW.toString().equals(orderDo.getState())) {
	    ServiceException se = new ServiceException("order state error !");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	if (!PayTypeEnum.OTHER.toString().equals(orderDo.getPayType())) {
	    ServiceException se = new ServiceException("order payType error !");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	if (StringUtil.isNotEmpty(orderDo.getPayCode())) {
	    ServiceException se = new ServiceException("payCode not null !");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	int subjectId = orderDo.getSubjectId();
	SubjectDO subjectDo = subjectDAO.selectById(subjectId);
	if (subjectDo == null) {
	    ServiceException se = new ServiceException("order's subject not found !");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	Date endDate = subjectDo.getEndDate();
	Date nowDate = new Date();
	if (nowDate.getTime() > endDate.getTime()) {
	    ServiceException se = new ServiceException("the order's subject is end !");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	int userId = orderDo.getUserId();
	UserDO userDo = userDAO.getUserById(userId);
	if (userDo == null) {
	    ServiceException se = new ServiceException("the order's user not found !");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	if (userDo.getRegionId() == 0 || StringUtil.isEmpty(userDo.getName())
		|| StringUtil.isEmpty(userDo.getPhone())) {
	    ServiceException se = new ServiceException("the order's user's info not complete");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	// 推荐人奖励 +5
	double reward = configService.getIntroduceAward();
	if (orderDo.getIntroducerUserId() != null) {
	    UserDO introducer = userDAO.getUserById(orderDo.getIntroducerUserId());
	    introducer.setBalance(new BigDecimal(introducer.getBalance() + reward).setScale(2, BigDecimal.ROUND_HALF_UP)
		    .doubleValue());
	    if (!userDAO.update(introducer)) {
		ServiceException se = new ServiceException("update introducer fail !");
		se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
		throw se;
	    }
	}
	
		// 如果是小团就新创建子团
		if (SubjectTypeEnum.INDIE.name().equalsIgnoreCase(subjectDo.getType())) {
			subjectDo.setType(SubjectTypeEnum.CHILD.name());
			subjectDo.setParentId(subjectDo.getId());
			subjectDo.setId(-1);
			subjectDAO.addSubject(subjectDo);
			orderDo.setSubjectId(subjectDo.getId());
		}
	
	orderDo.setPayAmount(new BigDecimal(payMoney));
	orderDo.setPayType(payType);
	orderDo.setPayCode(payCode);
	orderDo.setState(OrderStateEnum.WAIT.toString());
	orderDo.setPayDate(new Date());
	if (orderDAO.updateByPrimaryKey(orderDo) != 1) {
	    ServiceException se = new ServiceException("update order fail !");
	    se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
	    throw se;
	}
	if (!endPayFinishMail(orderId)) {
	    Log.warn("send payFinishMail fail! orderId = " + orderId);
	}
	return true;
    }

    @Override
    public OrderResultDTO getOrderResult(int orderId) throws ServiceException {
	if (orderId < 0) {
	    ServiceException se = new ServiceException("orderId < 0!");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	OrderDO order = orderDAO.selectByPrimaryKey(orderId);
	if (order == null) {
	    ServiceException se = new ServiceException("order not found !orderId = " + orderId);
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	int userId = order.getUserId();
	UserDTO userDto = userService.getUserDTOById(userId);
	if (userDto == null) {
	    return null;
	}
	int regionId = order.getRegionId();
	if (regionId <= 0) {
	    ServiceException se = new ServiceException("regionId error !");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	RegionDO regionDo = regionDAO.selectByPrimaryKey(regionId);
	if (regionDo == null) {
	    ServiceException se = new ServiceException("regionDo not found !");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	int subjectId = order.getSubjectId();
	SubjectResultDTO subjectResultDto = subjectService.getSubjectById(subjectId, regionId);
	if (subjectResultDto == null) {
	    ServiceException se = new ServiceException("subjectResultDto not found !");
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	OrderResultDTO orderResultDto;
	try {
	    orderResultDto = mapper.map(order, OrderResultDTO.class);
	} catch (Exception e) {
	    ServiceException se = new ServiceException(e);
	    se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
	    throw se;
	}
	orderResultDto.setSubjectResultDto(subjectResultDto);
	orderResultDto.setUserDo(userDto);
	orderResultDto.setRegionDo(regionDo);
	Integer adviserId = order.getAdviserId();
	double waitPayAmount = new BigDecimal(subjectResultDto.getPreAmount() * order.getNum())
		.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	orderResultDto.setWaitPayAmount(waitPayAmount);
	if (adviserId != null) {
	    AdviserDO adviserDo = adviserDAO.selectByPrimaryKey(adviserId);
	    orderResultDto.setAdviserDo(adviserDo);
	}
	Integer introducerId = order.getIntroducerUserId();
	if (introducerId != null) {
	    UserDTO introducerDto = userService.getUserDTOById(introducerId);
	    if (introducerDto == null) {
		ServiceException se = new ServiceException("introducerDo not found !");
		se.setCode(ErrorCodeEnum.DATA_ERROR.code());
		throw se;
	    }
	    orderResultDto.setIntroducerDo(introducerDto);
	}
	return orderResultDto;
    }

    @Override
    public List<OrderResultDTO> getOrderResultList(int userId, String state) throws ServiceException {
	if (userId < 0) {
	    ServiceException se = new ServiceException("buyerId < 0 !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (StringUtil.isEmpty(state) || OrderStateEnum.get(state) == null) {
	    ServiceException se = new ServiceException("state error !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	UserDTO userDto = userService.getUserDTOById(userId);
	if (userDto == null) {
	    ServiceException se = new ServiceException("userDo not found ! userId = " + userId);
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	List<OrderDO> orderList = orderDAO.selectByUserId(userId, state);
	List<OrderResultDTO> orderResultList = new ArrayList<OrderResultDTO>();
	for (OrderDO order : orderList) {
	    OrderResultDTO orderResultDto = mapper.map(order, OrderResultDTO.class);
	    int regionId = order.getRegionId();
	    int subjectId = order.getSubjectId();
	    SubjectResultDTO subjectResultDto = subjectService.getSubjectById(subjectId, regionId);
	    if (subjectResultDto == null) {
		ServiceException se = new ServiceException(
			"subjectResultDto not found ! subjectResultId = " + subjectId);
		se.setCode(ErrorCodeEnum.DATA_ERROR.code());
		throw se;
	    }
	    RegionDO regionDo = regionDAO.selectByPrimaryKey(regionId);
	    double waitPayAmount = new BigDecimal(subjectResultDto.getPreAmount() * order.getNum())
		    .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	    orderResultDto.setWaitPayAmount(waitPayAmount);
	    orderResultDto.setSubjectResultDto(subjectResultDto);
	    orderResultDto.setUserDo(userDto);
	    orderResultDto.setRegionDo(regionDo);
	    Integer adviserId = order.getAdviserId();
	    if (adviserId != null) {
		AdviserDO adviserDo = adviserDAO.selectByPrimaryKey(adviserId);
		orderResultDto.setAdviserDo(adviserDo);
	    }
	    Integer introducerId = order.getIntroducerUserId();
	    if (introducerId != null) {
		UserDTO introducerDto = userService.getUserDTOById(introducerId);
		orderResultDto.setIntroducerDo(introducerDto);
	    }
	    orderResultList.add(orderResultDto);
	}
	return orderResultList;
    }

    @Override
    public List<OrderResultDTO> getListByIntroducer(int introducerId) throws ServiceException {
	if (introducerId <= 0) {
	    ServiceException se = new ServiceException("buyerId < 0 !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	UserDTO introducerDto = userService.getUserDTOById(introducerId);
	if (introducerDto == null) {
	    ServiceException se = new ServiceException("introducerDo not found ! introducerId = " + introducerId);
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	List<OrderDO> orderList = orderDAO.selectByIntroducerId(introducerId);
	List<OrderResultDTO> orderResultList = new ArrayList<OrderResultDTO>();
	for (OrderDO order : orderList) {
	    OrderResultDTO orderResultDto = mapper.map(order, OrderResultDTO.class);
	    int subjectId = order.getSubjectId();
	    int userId = order.getUserId();
	    UserDTO userDto = userService.getUserDTOById(userId);
	    if (userDto == null) {
		ServiceException se = new ServiceException("order's userDo not found orderId = " + order.getId());
		se.setCode(ErrorCodeEnum.DATA_ERROR.code());
		throw se;
	    }
	    int regionId = order.getRegionId();
	    RegionDO regionDo = regionDAO.selectByPrimaryKey(regionId);
	    SubjectResultDTO subjectResultDto = subjectService.getSubjectById(subjectId, regionId);
	    if (subjectResultDto == null) {
		ServiceException se = new ServiceException(
			"subjectResultDto not found ! subjectResultId = " + subjectId);
		se.setCode(ErrorCodeEnum.DATA_ERROR.code());
		throw se;
	    }
	    double waitPayAmount = new BigDecimal(subjectResultDto.getPreAmount() * order.getNum())
		    .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	    orderResultDto.setWaitPayAmount(waitPayAmount);
	    orderResultDto.setSubjectResultDto(subjectResultDto);
	    orderResultDto.setUserDo(userDto);
	    orderResultDto.setRegionDo(regionDo);
	    Integer adviserId = order.getAdviserId();
	    if (adviserId != null && adviserId != 0) {
		AdviserDO adviserDo = adviserDAO.selectByPrimaryKey(adviserId);
		orderResultDto.setAdviserDo(adviserDo);
	    }
	    orderResultDto.setIntroducerDo(introducerDto);
	    orderResultList.add(orderResultDto);
	}
	return orderResultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStateBySubjectState(int userId) throws ServiceException {
	try {
	    subjectService.updateSunjectStateByDate();
	} catch (Exception e) {
	    Log.info("根据时间更新课程课程状态失败,失败原因:" + e.getMessage());
	}
	List<OrderDO> orderDoList;
	if (userId == 0) {
	    orderDoList = orderDAO.selectAll();
	} else {
	    orderDoList = orderDAO.selectByUserId(userId, OrderStateEnum.WAIT.toString());
	}
	for (OrderDO order : orderDoList) {
	    try {
		if (OrderStateEnum.WAIT.toString().equals(order.getState())) {
		    int subjectId = order.getSubjectId();
		    SubjectDO subjectDo = subjectDAO.selectById(subjectId);
		    if (subjectDo == null) {
			ServiceException se = new ServiceException("subjectDo not found ! subjectId = " + subjectId);
			se.setCode(ErrorCodeEnum.DATA_ERROR.code());
			throw se;
		    }
		    if (SubjectStateEnum.END.toString().equals(subjectDo.getState())
			    || SubjectStateEnum.STOP.toString().equals(subjectDo.getState())) {
			// 订单数量
			int orderCount = subjectService.getSubjectCount(subjectId);
			int minCount = subjectService.getSubjectMinCount(subjectId);

			if (orderCount >= minCount) {
			    order.setState(OrderStateEnum.SUCCESS.toString());
			    userId = order.getUserId();
			    UserDO userDo = userDAO.getUserById(userId);
			    int regionId = userDo.getRegionId();
			    BigDecimal finishPrice = new BigDecimal(
				    subjectService.getSubjectNowMoney(subjectId, regionId));
			    order.setFinishPrice(finishPrice);
			    orderDAO.updateByPrimaryKey(order);
			    if (!sendSubjectEndMail(order.getId())) {
				Log.warn("sendSubjectEndMail fail! orderId = " + order.getId());
			    }
			} else {
			    order.setState(OrderStateEnum.END.toString());
			    orderDAO.updateByPrimaryKey(order);
			    if (!sendSubjectEndMail(order.getId())) {
				Log.warn("sendSubjectEndMail fail! orderId = " + order.getId());
			    }
			}
		    }
		}
	    } catch (Exception e) {
		Log.warn(e.getMessage());
		e.printStackTrace();
	    }
	}
	return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean balancePay(int orderId, double balanceMoney) throws ServiceException {
	if (orderId < 0) {
	    ServiceException se = new ServiceException("orderId < 0 !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (balanceMoney < 0) {
	    ServiceException se = new ServiceException("balanceMoney < 0 !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	OrderResultDTO orderResult = getOrderResult(orderId);
	if (orderResult == null) {
	    ServiceException se = new ServiceException("order not found !orderId = " + orderId);
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	UserDO userDo = userDAO.getUserById(orderResult.getUserId());
	if (userDo == null) {
	    ServiceException se = new ServiceException("order's userDto not found !orderId = " + orderId);
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (userDo.getBalance() < balanceMoney) {
	    ServiceException se = new ServiceException("balance not enough !orderId = " + orderId);
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	if (balanceMoney > orderResult.getWaitPayAmount()) {
	    ServiceException se = new ServiceException("balanceMoney > WaitPayAmount !orderId = " + orderId);
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	double oldBalance = userDo.getBalance();
	double newBalance = new BigDecimal(oldBalance - balanceMoney).setScale(2, BigDecimal.ROUND_HALF_UP)
		.doubleValue();
	userDo.setBalance(newBalance);
	if (!userDAO.update(userDo)) {
	    ServiceException se = new ServiceException("update userBalance fail !");
	    se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
	    throw se;
	}
	PayLogDO payLogDo = new PayLogDO();
	payLogDo.setOrderId(orderId);
	payLogDo.setPayCode(balanceMoney + "");
	payLogDo.setPayAmount(new BigDecimal(balanceMoney));
	payLogDo.setPayDate(new Date());
	payLogDo.setPayType(PayTypeEnum.BALANCE.toString());
	payLogDo.setUserId(orderResult.getUserId());
	if (payLogDAO.insert(payLogDo) != 1) {
	    ServiceException se = new ServiceException("add payLogDo fail !");
	    se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
	    throw se;
	}
	return true;
    }

    @Override
    public List<OrderResultDTO> getListBySubjectId(int subjectId) throws ServiceException {
	if (subjectId <= 0) {
	    ServiceException se = new ServiceException("subjectId error !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	List<OrderDO> orderList = orderDAO.selectBySubjectId(subjectId);
	List<OrderResultDTO> orderResultList = new ArrayList<OrderResultDTO>();
	for (OrderDO order : orderList) {
	    OrderResultDTO orderResultDto = mapper.map(order, OrderResultDTO.class);
	    int regionId = order.getRegionId();
	    int userId = order.getUserId();
	    UserDTO userDto = userService.getUserDTOById(userId);
	    String nickName = userDto.getNickname();
	    String s;
	    String w;
	    if (nickName.length() > 2) {
		s = nickName.substring(0, 1);
		w = nickName.substring(nickName.length() - 1);
	    } else if (nickName.length() > 1) {
		s = nickName.substring(0, 1);
		w = "*";
	    } else {
		s = "*";
		w = "*";
	    }
	    nickName = s + "**" + w;
	    userDto.setNickname(nickName);
	    SubjectResultDTO subjectResultDto = subjectService.getSubjectById(subjectId, regionId);
	    if (subjectResultDto == null) {
		ServiceException se = new ServiceException(
			"subjectResultDto not found ! subjectResultId = " + subjectId);
		se.setCode(ErrorCodeEnum.DATA_ERROR.code());
		throw se;
	    }
	    RegionDO regionDo = regionDAO.selectByPrimaryKey(regionId);
	    double waitPayAmount = new BigDecimal(subjectResultDto.getPreAmount() * order.getNum())
		    .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	    orderResultDto.setWaitPayAmount(waitPayAmount);
	    orderResultDto.setSubjectResultDto(subjectResultDto);
	    orderResultDto.setUserDo(userDto);
	    orderResultDto.setRegionDo(regionDo);
	    Integer adviserId = order.getAdviserId();
	    if (adviserId != null) {
		AdviserDO adviserDo = adviserDAO.selectByPrimaryKey(adviserId);
		orderResultDto.setAdviserDo(adviserDo);
	    }
	    Integer introducerId = order.getIntroducerUserId();
	    if (introducerId != null) {
		UserDTO introducerDto = userService.getUserDTOById(introducerId);
		orderResultDto.setIntroducerDo(introducerDto);
	    }
	    orderResultList.add(orderResultDto);
	}
	return orderResultList;
    }
}