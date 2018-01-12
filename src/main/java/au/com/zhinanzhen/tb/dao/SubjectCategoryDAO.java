package au.com.zhinanzhen.tb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import au.com.zhinanzhen.tb.dao.pojo.SubjectCategoryDO;


public interface SubjectCategoryDAO {
    List<SubjectCategoryDO> selectSubjectCategory(@Param("classify") String classify);
}
