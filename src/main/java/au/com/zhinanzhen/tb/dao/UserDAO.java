package au.com.zhinanzhen.tb.dao;

import org.apache.ibatis.annotations.Param;

import au.com.zhinanzhen.tb.dao.pojo.UserDO;

public interface UserDAO {
	int addUser(@Param("name") String name, @Param("familyName") String familyName,
			@Param("givenName") String givenName, @Param("thirdType") String thirdType, @Param("phone") String phone,
			@Param("thirdId") String thirdId, @Param("nickname") String nickname, @Param("logo") String logo,
			@Param("recommendOpenid") String recommendOpenid, @Param("balance") double balance);

	UserDO getUserByThird(@Param("thirdType") String thirdType, @Param("thirdId") String thirdId);

	boolean update(UserDO userDo);

	UserDO getUserById(int id);

	UserDO getUserByPhone(@Param("phone") String phone);

}
