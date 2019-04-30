package au.com.zhinanzhen.tb.service.impl;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esotericsoftware.minlog.Log;
import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;
import au.com.zhinanzhen.tb.dao.OrderDAO;
import au.com.zhinanzhen.tb.dao.PayLogDAO;
import au.com.zhinanzhen.tb.dao.RegionDAO;
import au.com.zhinanzhen.tb.dao.UserDAO;
import au.com.zhinanzhen.tb.dao.pojo.OrderDO;
import au.com.zhinanzhen.tb.dao.pojo.PayLogDO;
import au.com.zhinanzhen.tb.dao.pojo.UserDO;
import au.com.zhinanzhen.tb.service.AdviserService;
import au.com.zhinanzhen.tb.service.OrderStateEnum;
import au.com.zhinanzhen.tb.service.PayTypeEnum;
import au.com.zhinanzhen.tb.service.ServiceException;
import au.com.zhinanzhen.tb.service.ThirdTypeEnum;
import au.com.zhinanzhen.tb.service.UserService;
import au.com.zhinanzhen.tb.service.pojo.AdviserDTO;
import au.com.zhinanzhen.tb.service.pojo.UserDTO;
import au.com.zhinanzhen.tb.utils.Base64Util;
import au.com.zhinanzhen.tb.utils.ConfigService;

@Service("userService")
public class UserServiceImpl extends BaseService implements UserService {
	@Resource
	private UserDAO userDAO;
	@Resource
	private RegionDAO regionDAO;
	@Resource
	private OrderDAO orderDAO;
	@Resource
	private PayLogDAO payLogDAO;
	@Resource
	private AdviserService adviserService;
	@Resource
	private ConfigService configService;

	@Override
	public boolean thirdVerify(String thirdType, String thirdId) throws ServiceException {
		if (StringUtil.isEmpty(thirdType)) {
			ServiceException se = new ServiceException("thirdType is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (StringUtil.isEmpty(thirdId)) {
			ServiceException se = new ServiceException("thirdId is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (ThirdTypeEnum.get(thirdType) == null) {
			ServiceException se = new ServiceException("thirdType error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		UserDO userDo;
		try {
			userDo = userDAO.getUserByThird(thirdType, thirdId);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		if (userDo != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public UserDTO thirdLogin(String thirdType, String thirdId) throws ServiceException {
		if (StringUtil.isEmpty(thirdType)) {
			ServiceException se = new ServiceException("thirdType is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (StringUtil.isEmpty(thirdId)) {
			ServiceException se = new ServiceException("thirdId is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (ThirdTypeEnum.get(thirdType) == null) {
			ServiceException se = new ServiceException("thirdType error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		UserDO userDo;
		try {
			userDo = userDAO.getUserByThird(thirdType, thirdId);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		if (userDo == null) {
			ServiceException se = new ServiceException("userDo not found !");
			se.setCode(ErrorCodeEnum.DATA_ERROR.code());
			throw se;
		}
		UserDTO userDto;
		try {
			userDto = mapper.map(userDo, UserDTO.class);
			if (userDto.getRecommendOpenid() != null) {
				UserDO recommendUser = userDAO.getUserByThird(thirdType, userDto.getRecommendOpenid());
				userDto.setRecommendUserDO(recommendUser);
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		if (userDto.getAdviserId() != null) {
			AdviserDTO adviserDto = adviserService.getAdviserById(userDto.getAdviserId());
			userDto.setAdviserDto(adviserDto);
		}
		try {
			userDto.setNickname(new String(Base64Util.decodeBase64(userDto.getNickname())));
		} catch (Exception e) {
			Log.info("昵称转码失败 userId = " + userDto.getId());
		}
		return userDto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean addUser(String name, String familyName, String givenName, String thirdType, String phone,
			String thirdId, String nickname, String logo, String recommendOpenid) throws ServiceException {
		if (StringUtil.isEmpty(thirdType)) {
			ServiceException se = new ServiceException("thirdType is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (StringUtil.isEmpty(thirdId)) {
			ServiceException se = new ServiceException("thirdId is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (ThirdTypeEnum.get(thirdType) == null) {
			ServiceException se = new ServiceException("thirdType error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (userDAO.getUserByThird(thirdType, thirdId) != null) {
			ServiceException se = new ServiceException("third is exists !");
			se.setCode(ErrorCodeEnum.DATA_ERROR.code());
			throw se;
		}
		double balance = configService.getIntroduceAward();
		byte[] input = nickname.getBytes();
		try {
			nickname = Base64Util.encodeBase64(input);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("nickname toBase64 error! nickname = " + nickname);
		}
		if (userDAO.addUser(name, familyName, givenName, thirdType, phone, thirdId, nickname, logo, recommendOpenid,
				balance) == 0) {
			ServiceException se = new ServiceException("add user fail !");
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateUser(UserDTO userDto) throws ServiceException {
		if (userDto == null) {
			ServiceException se = new ServiceException("userDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		int userId = userDto.getId();
		if (userId <= 0) {
			ServiceException se = new ServiceException("userId <= 0 ! userId = " + userId);
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		UserDO oldUserDo = userDAO.getUserById(userId);
		if (oldUserDo == null) {
			ServiceException se = new ServiceException("old userDo not found !");
			se.setCode(ErrorCodeEnum.DATA_ERROR.code());
			throw se;
		}
		UserDO userDo;
		try {
			userDo = mapper.map(userDto, UserDO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		if (StringUtil.isNotEmpty(userDo.getNickname()))
			try {
				userDo.setNickname(new String(Base64Util.encodeBase64(userDo.getNickname().getBytes())));
			} catch (Exception e) {
				System.out.println(("昵称转码失败"));
			}
		if (!userDAO.update(userDo)) {
			ServiceException se = new ServiceException("update user fail !");
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		return true;
	}

	@Override
	public UserDTO getUserDTOById(int userId) throws ServiceException {
		if (userId < 0) {
			ServiceException se = new ServiceException("userId error !");
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		UserDO userDo = userDAO.getUserById(userId);
		if (userDo == null) {
			return null;
		}
		UserDTO userDto;
		try {
			userDto = mapper.map(userDo, UserDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		if (userDto.getAdviserId() != null) {
			AdviserDTO adviserDto = adviserService.getAdviserById(userDto.getAdviserId());
			userDto.setAdviserDto(adviserDto);
		}
		String recommendOpenId = userDto.getRecommendOpenid();
		if (StringUtil.isNotEmpty(recommendOpenId)) {
			Integer recommendUserId;
			try {
				recommendUserId = getUserIdByOpenId(ThirdTypeEnum.WECHAT.toString(), recommendOpenId);
				UserDO recommendUserDo = userDAO.getUserById(recommendUserId);
				userDto.setRecommendUserDO(recommendUserDo);
			} catch (Exception e) {
				Log.info(e.getMessage());
			}
		}

		try {
			userDto.setNickname(new String(Base64Util.decodeBase64(userDto.getNickname())));
		} catch (Exception e) {
			Log.info("昵称转码失败 userId = " + userDto.getId());
		}
		return userDto;
	}

	@Override
	public Set<UserDTO> getIntroducer(int userId) throws ServiceException {
		if (userId < 0) {
			ServiceException se = new ServiceException("userId < 0,userId = " + userId);
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		List<OrderDO> orderList = orderDAO.selectByUserId(userId, OrderStateEnum.ALL.toString());
		Set<UserDTO> userDtoSet = new HashSet<UserDTO>();
		for (OrderDO order : orderList) {
			if (!OrderStateEnum.NEW.toString().equals(order.getState())) {
				if (order.getIntroducerUserId() != null) {
					UserDO introducerDo = userDAO.getUserById(order.getIntroducerUserId());
					UserDTO introducerDto = mapper.map(introducerDo, UserDTO.class);
					if (introducerDto.getAdviserId() != null) {
						AdviserDTO adviserDto = adviserService.getAdviserById(introducerDto.getAdviserId());
						introducerDto.setAdviserDto(adviserDto);
					}
					String recommendOpenId = introducerDto.getRecommendOpenid();
					if (StringUtil.isNotEmpty(recommendOpenId)) {
						try {
							int recommendUserId = getUserIdByOpenId(ThirdTypeEnum.WECHAT.toString(), recommendOpenId);
							UserDO recommendUserDo = userDAO.getUserById(recommendUserId);
							introducerDto.setRecommendUserDO(recommendUserDo);
						} catch (Exception e) {
							Log.info(e.getMessage());
						}
					}
					try {
						introducerDto.setNickname(new String(Base64Util.decodeBase64(introducerDto.getNickname())));
					} catch (Exception e) {
						Log.info("昵称转码失败 userId = " + introducerDto.getId());
					}
					userDtoSet.add(introducerDto);
				}
			}
		}
		return userDtoSet;
	}

	@Override
	public double getPayByBalanceMoney(int userId) throws ServiceException {
		if (userId < 0) {
			ServiceException se = new ServiceException("userId < 0,userId = " + userId);
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		List<PayLogDO> payLogList = payLogDAO.selectByUserId(userId, PayTypeEnum.BALANCE.toString());
		double balancePayMoney = 0;
		if (payLogList != null && payLogList.size() > 0) {
			for (PayLogDO payLogDo : payLogList) {
				balancePayMoney += payLogDo.getPayAmount().doubleValue();
			}
			balancePayMoney = new BigDecimal(balancePayMoney).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		return balancePayMoney;
	}

	@Override
	public boolean isPhoneExitst(String phone) throws ServiceException {
		if (StringUtil.isEmpty(phone)) {
			ServiceException se = new ServiceException("phone is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		UserDO userDo = userDAO.getUserByPhone(phone);
		if (userDo == null) {
			return false;
		} else {
			return true;
		}
	}
}
