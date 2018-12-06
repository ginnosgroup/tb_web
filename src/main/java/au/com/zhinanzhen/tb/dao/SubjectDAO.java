package au.com.zhinanzhen.tb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import au.com.zhinanzhen.tb.dao.pojo.SubjectDO;


public interface SubjectDAO {
	
	int addSubject(SubjectDO subjectDo);
	
    List<SubjectDO> selectSubjectByCategoryIdAndRegionId(
	    @Param("categoryId") int categoryId,
	    @Param("regionId") int regionId,
            @Param("classify") String classify);
    SubjectDO selectById(int id);
    
    boolean updateStateByDate(
	    @Param("id") int id,
	    @Param("state") String state);
}
