package au.com.zhinanzhen.tb.web;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ikasoa.core.utils.StringUtil;

import au.com.zhinanzhen.tb.service.ServiceException;
import au.com.zhinanzhen.tb.service.SubjectCategoryClassifyEnum;
import au.com.zhinanzhen.tb.service.SubjectCategoryService;
import au.com.zhinanzhen.tb.service.SubjectClassifyEnum;
import au.com.zhinanzhen.tb.service.SubjectService;
import au.com.zhinanzhen.tb.service.pojo.SubjectCategoryDTO;
import au.com.zhinanzhen.tb.service.pojo.SubjectResultDTO;
import au.com.zhinanzhen.tb.utils.JsonResult;

@Controller
@RequestMapping("/subject")
public class SubjectController extends AbstractController {
    @Resource
    private SubjectService subjectService;
    @Resource
    private SubjectCategoryService subjectCategoryService;

    @ResponseBody
    @RequestMapping("/category")
    public JsonResult getCategoryList(String classify) throws ServiceException {
	if (StringUtil.isEmpty(classify) || SubjectCategoryClassifyEnum.get(classify) == null) {
	    Exception e = new Exception("分类参数错误");
	    return new JsonResult(2, e);
	}
	List<SubjectCategoryDTO> list = subjectCategoryService.getSubjectCategoryList(classify);
	return new JsonResult(list);
    }

    @ResponseBody
    @RequestMapping("/getBycategoryId")
    public JsonResult getCategoryList(int categoryId, int regionId, String classify) throws ServiceException {
	if (categoryId < 0) {
	    Exception e = new Exception("类目错误");
	    return new JsonResult(2, e);
	}
	if (regionId < 0) {
	    Exception e = new Exception("区域错误");
	    return new JsonResult(2, e);
	}
	if (StringUtil.isEmpty(classify) || SubjectClassifyEnum.get(classify) == null) {
	    Exception e = new Exception("分类参数错误");
	    return new JsonResult(2, e);
	}
	List<SubjectResultDTO> list = subjectService.getSubjectList(categoryId, regionId, classify);
	return new JsonResult(list);
    }

    @ResponseBody
    @RequestMapping("/getById")
    public JsonResult getById(int id, Integer regionId) throws ServiceException {
	if (id < 0) {
	    Exception e = new Exception("编号错误");
	    return new JsonResult(2, e);
	}
	if (regionId == null) {
	    regionId = 0;
	}
	SubjectResultDTO subjectResultDto = subjectService.getSubjectById(id, regionId);
	return new JsonResult(subjectResultDto);
    }

    @ResponseBody
    @RequestMapping("/getBuyCount")
    public JsonResult getBuyCount(int id) throws ServiceException {
	if (id < 0) {
	    Exception e = new Exception("编号错误");
	    return new JsonResult(2, e);
	}
	int count = subjectService.getSubjectCount(id);
	return new JsonResult(count);
    }

    @ResponseBody
    @RequestMapping("/update")
    public JsonResult updateByDate() throws ServiceException {
	return new JsonResult(subjectService.updateSunjectStateByDate());
    }

    @ResponseBody
    @RequestMapping("/minPrice")
    public JsonResult showMinPrice(int id,int regionId) throws ServiceException {
	if (id < 0) {
	    Exception e = new Exception("编号错误");
	    return new JsonResult(2, e);
	}
	return new JsonResult(subjectService.getSubjectMinMoney(id, regionId));
    }
    @ResponseBody
    @RequestMapping("/nowPrice")
    public JsonResult showNowPrice(int id,int regionId) throws ServiceException {
	if (id < 0) {
	    Exception e = new Exception("编号错误");
	    return new JsonResult(2, e);
	}
	return new JsonResult(subjectService.getSubjectNowMoney(id, regionId));
    }

}
