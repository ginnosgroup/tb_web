package au.com.zhinanzhen.tb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import au.com.zhinanzhen.tb.dao.pojo.OrderDO;

public interface OrderDAO {

    int deleteByPrimaryKey(Integer id);

    int insert(OrderDO record);

    OrderDO selectByPrimaryKey(Integer id);

    List<OrderDO> selectAll();

    int updateByPrimaryKey(OrderDO record);

    List<OrderDO> selectBySubjectId(int subjectId);

    List<OrderDO> selectByUserId(@Param("userId") int userId, @Param("state") String state);

    List<OrderDO> selectByIntroducerId(@Param("introducerId") int introducerId);
}