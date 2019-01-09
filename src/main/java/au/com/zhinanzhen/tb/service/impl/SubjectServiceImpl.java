package au.com.zhinanzhen.tb.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esotericsoftware.minlog.Log;
import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

import au.com.zhinanzhen.tb.dao.OrderDAO;
import au.com.zhinanzhen.tb.dao.RegionDAO;
import au.com.zhinanzhen.tb.dao.SubjectDAO;
import au.com.zhinanzhen.tb.dao.SubjectPriceDAO;
import au.com.zhinanzhen.tb.dao.pojo.OrderDO;
import au.com.zhinanzhen.tb.dao.pojo.RegionDO;
import au.com.zhinanzhen.tb.dao.pojo.SubjectDO;
import au.com.zhinanzhen.tb.dao.pojo.SubjectPriceDO;
import au.com.zhinanzhen.tb.service.ServiceException;
import au.com.zhinanzhen.tb.service.SubjectClassifyEnum;
import au.com.zhinanzhen.tb.service.SubjectService;
import au.com.zhinanzhen.tb.service.SubjectStateEnum;
import au.com.zhinanzhen.tb.service.SubjectTypeEnum;
import au.com.zhinanzhen.tb.service.pojo.SubjectPriceDTO;
import au.com.zhinanzhen.tb.service.pojo.SubjectResultDTO;
import au.com.zhinanzhen.tb.utils.ConfigService;

@Service("subjectService")
public class SubjectServiceImpl extends BaseService implements SubjectService {
    @Resource
    private SubjectDAO subjectDAO;
    @Resource
    private SubjectPriceDAO subjectPriceDAO;
    @Resource
    private OrderDAO orderDAO;
    @Resource
    private RegionDAO regionDAO;
    @Resource
    ConfigService configService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    
    public List<SubjectResultDTO> getSubjectList(int categoryId, int regionId, String classify)
	    throws ServiceException {
	if (categoryId < 0) {
	    ServiceException se = new ServiceException("categoryId error !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (regionId < 0) {
	    ServiceException se = new ServiceException("regionId error !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	if (StringUtil.isEmpty(classify) || SubjectClassifyEnum.get(classify) == null) {
	    ServiceException se = new ServiceException("classify error !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	updateSunjectStateByDate();
	List<SubjectResultDTO> subjectDtoList = new ArrayList<SubjectResultDTO>();
	List<SubjectDO> subjectDoList;
	try {
	    subjectDoList = subjectDAO.selectSubjectByCategoryIdAndRegionId(categoryId, regionId, classify);
	    if (subjectDoList != null && subjectDoList.size() > 0) {
		for (SubjectDO subjectDo : subjectDoList) {
		    SubjectResultDTO subjectResultDto = mapper.map(subjectDo, SubjectResultDTO.class);
		    subjectResultDto.setLogo(configService.getStaticUrl()+subjectResultDto.getLogo());
		    int subjectId = subjectResultDto.getId();
		    List<SubjectPriceDO> subjectPriceDoList = subjectPriceDAO.selectBySubjectId(subjectId, regionId);
		    RegionDO regionDo = regionDAO.selectByPrimaryKey(regionId);
		    if (subjectPriceDoList == null || subjectPriceDoList.size() == 0) {
			if (regionDo != null && regionDo.getParentId() != null) {
			    regionId = regionDo.getParentId();
			    subjectPriceDoList = subjectPriceDAO.selectBySubjectId(subjectId, regionId);
			}
		    }
		    for (SubjectPriceDO subjectPriceDo : subjectPriceDoList) {
			SubjectPriceDTO subjectPriceDto = mapper.map(subjectPriceDo, SubjectPriceDTO.class);
			String regionIds = subjectPriceDto.getRegionIds();
			String[] regionStrs = regionIds.split(",");
			for (String str : regionStrs) {
			    if (Integer.valueOf(str.split(":")[0]) == regionId) {
			    	if(str.split(":").length==2) {
			    		double price = Double.valueOf(str.split(":")[1]);
						subjectPriceDto.setPrice(price);
			    	} else
			    		Log.error("Error region price : {}", str);
				
			    }
			}
			subjectResultDto.getSubjectPriceDtolist().add(subjectPriceDto);
		    }
		    List<OrderDO> orderDoList = orderDAO.selectBySubjectId(subjectId);
		    int totalNumber = 0;
		    for (OrderDO order : orderDoList) {
			totalNumber += order.getNum();
		    }
		    subjectResultDto.setBuyNumber(totalNumber);
		    subjectDtoList.add(subjectResultDto);
		}
	    }
	} catch (Exception e) {
	    ServiceException se = new ServiceException(e);
	    se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
	    throw se;
	}
	return subjectDtoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubjectResultDTO getSubjectById(int id, int regionId) throws ServiceException {
	if (id < 0) {
	    ServiceException se = new ServiceException("id < 0");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	updateSunjectStateByDate();
	SubjectResultDTO subjectResultDto = null;
	try {
	    SubjectDO subjectDo = subjectDAO.selectById(id);
	    if (subjectDo == null) {
		return null;
	    }
	    subjectResultDto = mapper.map(subjectDo, SubjectResultDTO.class);
	    subjectResultDto.setLogo(configService.getStaticUrl()+subjectResultDto.getLogo());
	    int subjectId = subjectResultDto.getId();
	    List<SubjectPriceDO> subjectPriceDoList = subjectPriceDAO.selectBySubjectId(subjectId, regionId);
	    RegionDO regionDo = regionDAO.selectByPrimaryKey(regionId);

	    if (subjectPriceDoList == null || subjectPriceDoList.size() == 0) {
		if (regionDo != null && regionDo.getParentId() != null) {
		    regionId = regionDo.getParentId();
		    subjectPriceDoList = subjectPriceDAO.selectBySubjectId(subjectId, regionId);
		}
	    }
	    if (subjectPriceDoList != null && subjectPriceDoList.size() > 0) {
		for (SubjectPriceDO subjectPriceDo : subjectPriceDoList) {
		    SubjectPriceDTO subjectPriceDto = mapper.map(subjectPriceDo, SubjectPriceDTO.class);
		    String regionIds = subjectPriceDto.getRegionIds();
		    String[] regionStrs = regionIds.split(",");
		    for (String str : regionStrs) {
			if (Integer.valueOf(str.split(":")[0]) == regionId) {
			    double price = Double.valueOf(str.split(":")[1]);
			    subjectPriceDto.setPrice(price);
			}
		    }
		    // 子区域没数据课程价格则自动选择父区域课程价格
		    if (subjectPriceDto.getPrice() == 0 && regionDAO.selectByPrimaryKey(regionId) != null
			    && regionDAO.selectByPrimaryKey(regionId).getParentId() != null) {
			int parentRegionId = regionDAO.selectByPrimaryKey(regionId).getParentId();
			for (String str : regionStrs) {
			    if (Integer.valueOf(str.split(":")[0]) == parentRegionId) {
				double price = Double.valueOf(str.split(":")[1]);
				subjectPriceDto.setPrice(price);
			    }
			}
		    }
		    subjectResultDto.getSubjectPriceDtolist().add(subjectPriceDto);
		}
	    }
	    int totalNumber = getSubjectCount(subjectId);
	    subjectResultDto.setBuyNumber(totalNumber);
	} catch (Exception e) {
	    ServiceException se = new ServiceException(e);
	    se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
	    throw se;
	}
	return subjectResultDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSunjectStateByDate() throws ServiceException {
	Date nowDate = new Date();
	try {
	    List<SubjectDO> waitSubjectDo = subjectDAO.selectSubjectByCategoryIdAndRegionId(0, 0,
		    SubjectClassifyEnum.WAIT.toString());
	    for (SubjectDO s : waitSubjectDo) {
		Date startDate = s.getStartDate();
		Date endDate = s.getEndDate();
		if (nowDate.getTime() > startDate.getTime()) {
		    subjectDAO.updateStateByDate(s.getId(), SubjectStateEnum.START.toString());
		}
		if (nowDate.getTime() > endDate.getTime()) {
		    subjectDAO.updateStateByDate(s.getId(), SubjectStateEnum.END.toString());
		}
	    }
	} catch (Exception e) {
	    ServiceException se = new ServiceException(e);
	    se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
	    throw se;
	}
	try {
	    List<SubjectDO> startSubjectDo = subjectDAO.selectSubjectByCategoryIdAndRegionId(0, 0,
		    SubjectClassifyEnum.START.toString());
	    for (SubjectDO s : startSubjectDo) {
		Date endDate = s.getEndDate();
		if (nowDate.getTime() > endDate.getTime()) {
		    subjectDAO.updateStateByDate(s.getId(), SubjectStateEnum.END.toString());
		}
	    }
	} catch (Exception e) {
	    ServiceException se = new ServiceException(e);
	    se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
	    throw se;
	}
	return true;
    }

    @Override
    public int getSubjectCount(int subjectId) throws ServiceException {
	if (subjectId < 0) {
	    ServiceException se = new ServiceException("subjectId error !subjectId : " + subjectId);
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	int totalNumber = 0;
	try {
	    List<OrderDO> orderDoList = orderDAO.selectBySubjectId(subjectId);
	    for (OrderDO order : orderDoList) {
		totalNumber += order.getNum();
	    }
	} catch (Exception e) {
	    ServiceException se = new ServiceException(e);
	    se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
	    throw se;
	}
	return totalNumber;
    }

    @Override
    public int getSubjectMinCount(int subjectId) throws ServiceException {
	if (subjectId < 0) {
	    ServiceException se = new ServiceException("subjectId error !subjectId : " + subjectId);
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	try {
	    List<SubjectPriceDO> subjectPriceList = subjectPriceDAO.selectBySubjectId(subjectId, 0);
	    if (subjectPriceList != null && subjectPriceList.size() > 0) {
		int mineCount = subjectPriceList.get(0).getStartNum();
		for (SubjectPriceDO subjectPriceDo : subjectPriceList) {
		    if (subjectPriceDo.getStartNum() < mineCount) {
			mineCount = subjectPriceDo.getStartNum();
		    }
		}
		return mineCount;
	    } else {
		ServiceException se = new ServiceException("未找到课程关联价格数据");
		se.setCode(ErrorCodeEnum.DATA_ERROR.code());
		throw se;
	    }
	} catch (Exception e) {
	    ServiceException se = new ServiceException(
		    "获取课程最小开课数失败 ! subjectId = " + subjectId + ",message:" + e.getMessage());
	    se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
	    throw se;
	}
    }
    @Override
    public double getSubjectMinMoney(int subjectId, int regionId) throws ServiceException {
	if (subjectId < 0) {
	    ServiceException se = new ServiceException("subjectId error !subjectId : " + subjectId);
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	SubjectResultDTO subjectResultDto = getSubjectById(subjectId, regionId);
	List<SubjectPriceDTO> subjectPriceDtoList = subjectResultDto.getSubjectPriceDtolist();
	if (subjectPriceDtoList == null || subjectPriceDtoList.size() == 0) {
	    ServiceException se = new ServiceException(
		    "this subject's subjectPriceList is null ! subjectId = " + subjectId+",regionId = "+regionId);
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	double minMoney = subjectPriceDtoList.get(0).getPrice();
	for (SubjectPriceDTO subjectPrice : subjectPriceDtoList) {
	    if (subjectPrice.getPrice() < minMoney) {
		minMoney = subjectPrice.getPrice();
	    }
	}
	return minMoney;
    }
    @Override
    public double getSubjectNowMoney(int subjectId, int regionId) throws ServiceException {
	if (subjectId < 0) {
	    ServiceException se = new ServiceException("subjectId error !subjectId : " + subjectId);
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	SubjectResultDTO subjectResultDto = getSubjectById(subjectId, regionId);
	int buyNum = subjectResultDto.getBuyNumber();
	int minNum = getSubjectMinCount(subjectId);
	if (buyNum < minNum) {
	    return subjectResultDto.getPrice();
	}
	List<SubjectPriceDTO> subjectPriceDtoList = subjectResultDto.getSubjectPriceDtolist();
	if (subjectPriceDtoList == null || subjectPriceDtoList.size() == 0) {
	    ServiceException se = new ServiceException(
		    "this subject's subjectPriceList is null ! subjectId = " + subjectId+",regionId = "+regionId);
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	for (SubjectPriceDTO subjectPrice : subjectPriceDtoList) {
	    if (subjectPrice.getEndNum() != null) {
		if (buyNum >= subjectPrice.getStartNum() && buyNum <=subjectPrice.getEndNum()) {
		    return subjectPrice.getPrice();
		}
	    }else{
		if(buyNum >= subjectPrice.getStartNum()){
		    return getSubjectMinMoney(subjectId,regionId);
		}
	    }
	}
	return 0;
    }
    @Override
    public double getSubjectNextMoney(int subjectId, int regionId) throws ServiceException {
	if (subjectId < 0) {
	    ServiceException se = new ServiceException("subjectId error !subjectId : " + subjectId);
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	SubjectResultDTO subjectResultDto = getSubjectById(subjectId, regionId);
	int buyNum = subjectResultDto.getBuyNumber();
	int nextNum = buyNum+1;
	int minNum = getSubjectMinCount(subjectId);
	if (nextNum < minNum) {
	    return subjectResultDto.getPrice();
	}
	List<SubjectPriceDTO> subjectPriceDtoList = subjectResultDto.getSubjectPriceDtolist();
	if (subjectPriceDtoList == null || subjectPriceDtoList.size() == 0) {
	    ServiceException se = new ServiceException(
		    "this subject's subjectPriceList is null ! subjectId = " + subjectId+",regionId = "+regionId);
	    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
	    throw se;
	}
	for (SubjectPriceDTO subjectPrice : subjectPriceDtoList) {
	    if (subjectPrice.getEndNum() != null) {
		if (nextNum >= subjectPrice.getStartNum() && nextNum <=subjectPrice.getEndNum()) {
		    return subjectPrice.getPrice();
		}
	    }else{
		if(nextNum >= subjectPrice.getStartNum()){
		    return getSubjectMinMoney(subjectId,regionId);
		}
	    }
	}
	return 0;
    }

}
