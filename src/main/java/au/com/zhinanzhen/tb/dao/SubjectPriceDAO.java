package au.com.zhinanzhen.tb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import au.com.zhinanzhen.tb.dao.pojo.SubjectPriceDO;

public interface SubjectPriceDAO {
    
    int deleteByPrimaryKey(Integer id);

    int insert(SubjectPriceDO record);

    SubjectPriceDO selectByPrimaryKey(Integer id);

    List<SubjectPriceDO> selectAll();

    int updateByPrimaryKey(SubjectPriceDO record);
    
    List<SubjectPriceDO> selectBySubjectId(
	    @Param("subjectId") int subjectId,
	    @Param("regionId") int regionId);
}