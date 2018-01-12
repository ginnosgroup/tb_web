package au.com.zhinanzhen.tb.service.pojo;

import lombok.Data;

@Data
public class AdviserDTO {
    private int id ;
    
    private String name;
    
    private String phone;

    private String email;
    
    private String state;
    
    private String imageUrl;

    private Integer regionId;

    
}