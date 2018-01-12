package au.com.zhinanzhen.tb.dao.pojo;

import lombok.Data;

@Data
public class RegionDO {
    
    private Integer id;

    private String name;

    private Integer parentId;
    
    private String parentName;
}