package au.com.zhinanzhen.tb.service.impl;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.ikasoa.core.thrift.ErrorCodeEnum;
import au.com.zhinanzhen.tb.dao.RegionDAO;
import au.com.zhinanzhen.tb.dao.pojo.RegionDO;
import au.com.zhinanzhen.tb.service.RegionService;
import au.com.zhinanzhen.tb.service.ServiceException;
import au.com.zhinanzhen.tb.service.pojo.RegionDTO;

@Service("regionService")
public class RegionServiceImpl extends BaseService implements RegionService {
    @Resource
    private RegionDAO regionDAO;

    @Override
    public RegionDTO getRegionById(int regionId) throws ServiceException {
	if (regionId < 0) {
	    ServiceException se = new ServiceException("regionId error !");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	RegionDO regionDo = regionDAO.selectByPrimaryKey(regionId);
	RegionDTO regionDto;
	if (regionDo == null) {
	    return null;
	}
	try {
	    regionDto = mapper.map(regionDo, RegionDTO.class);
	} catch (Exception e) {
	    ServiceException se = new ServiceException("regionId error !");
	    se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
	    throw se;
	}
	return regionDto;
    }

    @Override
    public List<RegionDTO> getALLRegion() throws ServiceException {
	List<RegionDO> regionDoList =regionDAO.selectByParent();
	List<RegionDTO> regionDtoList = new ArrayList<RegionDTO>();
	for(RegionDO regionDo:regionDoList){
	    if(regionDo == null){
		continue;
	    }
	    RegionDTO regionDto = mapper.map(regionDo, RegionDTO.class);
	    int parentId = regionDto.getId();
	    List<RegionDO> childRegionList = regionDAO.selectByParentId(parentId);
	    regionDto.getRegionList().addAll(childRegionList);
	    regionDtoList.add(regionDto);
	}
	return regionDtoList;
    }
}
