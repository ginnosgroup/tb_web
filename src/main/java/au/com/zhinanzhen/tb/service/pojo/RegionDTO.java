package au.com.zhinanzhen.tb.service.pojo;

import java.util.ArrayList;
import java.util.List;

import au.com.zhinanzhen.tb.dao.pojo.RegionDO;
import lombok.Data;

@Data
public class RegionDTO {
    
    private Integer id;

    private String name;

    private Integer parentId;
    
    private List<RegionDO> regionList = new ArrayList<RegionDO>();
}