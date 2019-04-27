package au.com.zhinanzhen.tb.service;

import java.util.Set;

import au.com.zhinanzhen.tb.service.pojo.UserDTO;

//第三方授权
public interface UserService {
	/**
	 * 验证账号
	 * 
	 * @param thirdType
	 * @param thirdId
	 * @return
	 */
	boolean thirdVerify(String thirdType, String thirdId) throws ServiceException;

	/**
	 * 第三方登陆
	 * 
	 * @param thirdType
	 * @param thirdId
	 * @return
	 */
	UserDTO thirdLogin(String thirdType, String thirdId) throws ServiceException;

	/**
	 * 用户授权
	 * 
	 * @param thirdType
	 * @param thirdId
	 * @return
	 */
	boolean addUser(String name, String familyName, String givenName, String thirdType, String phone, String thirdId,
			String userName, String logo, String recommendOpenid) throws ServiceException;

	/**
	 * 更新用户信息
	 * 
	 * @param userDto
	 * @return
	 */
	boolean updateUser(UserDTO userDto) throws ServiceException;

	/**
	 * 根据编号获取用户信息
	 * 
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	UserDTO getUserDTOById(int userId) throws ServiceException;

	/**
	 * 获取该用户推荐的用户
	 * 
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	Set<UserDTO> getIntroducer(int userId) throws ServiceException;

	/**
	 * 获取用户通过余额支付的金额
	 * 
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	double getPayByBalanceMoney(int userId) throws ServiceException;

	/**
	 * 判断手机号是否存在 true 存在 false 不存在
	 * 
	 * @param phone
	 * @return
	 * @throws ServiceException
	 */
	boolean isPhoneExitst(String phone) throws ServiceException;
}
