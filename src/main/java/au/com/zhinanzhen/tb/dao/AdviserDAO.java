package au.com.zhinanzhen.tb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import au.com.zhinanzhen.tb.dao.pojo.AdviserDO;


public interface AdviserDAO {
    
    int deleteByPrimaryKey(Integer id);

    int insert(AdviserDO record);

    AdviserDO selectByPrimaryKey(Integer id);

    List<AdviserDO> selectAll();

    int updateByPrimaryKey(AdviserDO record);
    
    List<AdviserDO> selectByRegion(@Param("regionId") int regionId);
}