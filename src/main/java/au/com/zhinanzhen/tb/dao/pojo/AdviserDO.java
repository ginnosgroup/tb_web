package au.com.zhinanzhen.tb.dao.pojo;

import lombok.Data;

@Data
public class AdviserDO {
    private int id ;
    
    private String name;
    
    private String phone;

    private String email;
    
    private String state;
    
    private String imageUrl;

    private Integer regionId;

    
}