package au.com.zhinanzhen.tb.dao;

import java.util.List;

import au.com.zhinanzhen.tb.dao.pojo.RegionDO;

public interface RegionDAO {
    
    int deleteByPrimaryKey(Integer id);

    int insert(RegionDO record);

    RegionDO selectByPrimaryKey(Integer id);

    List<RegionDO> selectAll();

    int updateByPrimaryKey(RegionDO record);
    
    //获取父区域列表
    List<RegionDO> selectByParent();
    //根据父区域获取子区域
    List<RegionDO> selectByParentId(Integer parentId);
}