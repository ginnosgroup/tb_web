package au.com.zhinanzhen.tb.service.impl;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.ikasoa.core.thrift.ErrorCodeEnum;
import au.com.zhinanzhen.tb.dao.AdviserDAO;
import au.com.zhinanzhen.tb.dao.OrderDAO;
import au.com.zhinanzhen.tb.dao.pojo.AdviserDO;
import au.com.zhinanzhen.tb.dao.pojo.OrderDO;
import au.com.zhinanzhen.tb.service.AdviserService;
import au.com.zhinanzhen.tb.service.AdviserStateEnum;
import au.com.zhinanzhen.tb.service.OrderStateEnum;
import au.com.zhinanzhen.tb.service.ServiceException;
import au.com.zhinanzhen.tb.service.pojo.AdviserDTO;
import au.com.zhinanzhen.tb.utils.ConfigService;
@Service("adviserService")
public class AdviserServiceImpl extends BaseService implements AdviserService {
    @Resource
    private AdviserDAO adviserDAO;
    @Resource
    private OrderDAO orderDAO;
    @Resource
    private ConfigService configService;
    @Override
    public List<AdviserDTO> selectByRegionId(int regionId) throws ServiceException {
	if (regionId < 0) {
	    ServiceException se = new ServiceException("regionId < 0");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	List<AdviserDO> adviserDoList = adviserDAO.selectByRegion(regionId);
	List<AdviserDTO> adviserDtoList = new ArrayList<AdviserDTO>();
	try {
	    if (adviserDoList != null && adviserDoList.size() > 0) {
		for (AdviserDO adviserDo : adviserDoList) {
		    AdviserDTO adviserDto = mapper.map(adviserDo, AdviserDTO.class);
		    adviserDto.setImageUrl(configService.getStaticUrl()+adviserDto.getImageUrl());
		    adviserDtoList.add(adviserDto);
		}
	    }
	} catch (Exception e) {
	    ServiceException se = new ServiceException(e);
	    se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
	    throw se;
	}
	return adviserDtoList;
    }
    @Override
    public AdviserDTO getAdviserById(int id) throws ServiceException {
	if(id < 0){
	    ServiceException se = new ServiceException("id < 0");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	AdviserDO adviserDo = adviserDAO.selectByPrimaryKey(id);
	if(adviserDo ==null){
	    return null;
	}
	AdviserDTO adviserDto = mapper.map(adviserDo, AdviserDTO.class);
	adviserDto.setImageUrl(configService.getStaticUrl()+adviserDto.getImageUrl());
	return adviserDto;
    }
    @Override
    public AdviserDTO getAdviserByLastOrderAdviser(int userId) throws ServiceException {
	if(userId < 0){
	    ServiceException se = new ServiceException("userId < 0");
	    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
	    throw se;
	}
	List<OrderDO> orderList = orderDAO.selectByUserId(userId, OrderStateEnum.ALL.toString());
	for(OrderDO order:orderList){
	    if(order.getAdviserId()!=0){
		int adviserId = order.getAdviserId();
		AdviserDTO adviserDto = getAdviserById(adviserId);
		if(AdviserStateEnum.ENABLED.toString().equals(adviserDto.getState())){
		    return adviserDto;
		}
	    }
	}
	return null;
    }
}
