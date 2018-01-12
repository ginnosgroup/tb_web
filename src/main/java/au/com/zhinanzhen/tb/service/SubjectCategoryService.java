package au.com.zhinanzhen.tb.service;

import java.util.List;

import au.com.zhinanzhen.tb.service.pojo.SubjectCategoryDTO;



public interface SubjectCategoryService {
   List<SubjectCategoryDTO> getSubjectCategoryList(String classify) throws ServiceException ;
}
