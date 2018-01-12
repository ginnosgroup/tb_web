package au.com.zhinanzhen.tb.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

import au.com.zhinanzhen.tb.dao.SubjectCategoryDAO;
import au.com.zhinanzhen.tb.dao.pojo.SubjectCategoryDO;
import au.com.zhinanzhen.tb.service.ServiceException;
import au.com.zhinanzhen.tb.service.SubjectCategoryClassifyEnum;
import au.com.zhinanzhen.tb.service.SubjectCategoryService;
import au.com.zhinanzhen.tb.service.pojo.SubjectCategoryDTO;

@Service("subjectCategoryService")
public class SubjectCategoryServiceImpl extends BaseService implements SubjectCategoryService {
    @Resource
    private SubjectCategoryDAO subjectCategoryDAO;
    @Override
    public List<SubjectCategoryDTO> getSubjectCategoryList(String classify) throws ServiceException {
	if(StringUtil.isEmpty(classify)||SubjectCategoryClassifyEnum.get(classify)==null){
	    ServiceException se = new ServiceException("classify error !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	List<SubjectCategoryDTO> subjectCategoryDtoList = new ArrayList<SubjectCategoryDTO>();
	List<SubjectCategoryDO> subjectCategoryDoList;
	try{
	    subjectCategoryDoList = subjectCategoryDAO.selectSubjectCategory(classify);
	    if(subjectCategoryDoList!=null&&subjectCategoryDoList.size()>0){
		for(SubjectCategoryDO subjectCategoryDo:subjectCategoryDoList){
		    SubjectCategoryDTO subjectCategoryDto =mapper.map(subjectCategoryDo, SubjectCategoryDTO.class);
		    subjectCategoryDtoList.add(subjectCategoryDto);
		}
	    }
	}catch(Exception e){
	    ServiceException se = new ServiceException(e);
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	return subjectCategoryDtoList;
    }
}
