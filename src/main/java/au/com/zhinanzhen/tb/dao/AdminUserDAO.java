package au.com.zhinanzhen.tb.dao;

import java.util.List;

import au.com.zhinanzhen.tb.dao.pojo.AdminUserDO;

public interface AdminUserDAO {
    
    int deleteByPrimaryKey(Integer id);

    int insert(AdminUserDO record);

    AdminUserDO selectByPrimaryKey(Integer id);

    List<AdminUserDO> selectAll();

    int updateByPrimaryKey(AdminUserDO record);
}