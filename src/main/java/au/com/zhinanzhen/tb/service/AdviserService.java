package au.com.zhinanzhen.tb.service;

import java.util.List;

import au.com.zhinanzhen.tb.service.pojo.AdviserDTO;

public interface AdviserService {
    /**
     * 根据区域获取adviser
     * @param regionId  传0则不限区域
     * @return
     * @throws ServiceException
     */
    List<AdviserDTO> selectByRegionId(int regionId) throws ServiceException;
    /**
     * 根据id获取adviser
     * @param id
     * @return
     */
    AdviserDTO getAdviserById(int id) throws ServiceException;
    /**
     * 选取上个订单的顾问 如果不存在  则null
     * @param userId
     * @return
     * @throws ServiceException
     */
    AdviserDTO getAdviserByLastOrderAdviser(int userId) throws ServiceException;
}
