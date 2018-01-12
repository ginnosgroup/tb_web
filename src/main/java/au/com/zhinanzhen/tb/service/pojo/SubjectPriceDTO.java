package au.com.zhinanzhen.tb.service.pojo;

import lombok.Data;
@Data
public class SubjectPriceDTO {
   
    private Integer id;

    private Integer subjectId;

    private Integer startNum;

    private Integer endNum;

    private String regionIds;
    
    private double price;
}