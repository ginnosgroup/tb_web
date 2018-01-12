package au.com.zhinanzhen.tb.service;

import java.util.List;

import au.com.zhinanzhen.tb.service.pojo.RegionDTO;


public interface RegionService {
    /**
     * 获取所有区域
     * @return
     * 区域集合
     * @throws ServiceException
     */
   List<RegionDTO> getALLRegion() throws ServiceException;
   
   /**
    * 根据regionId获取区域
    * @param regionId
    * @return
    */
   RegionDTO getRegionById(int regionId) throws ServiceException;
}
