package au.com.zhinanzhen.tb.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class SubjectDTO {

    private int id;

    private String name;
    
    private String logo;

    private double price;

    private Date startDate;

    private Date endDate;

    private String state;

    private int categoryId;
    
    private String categoryName;

    private double preAmount;

    private String codex;

    private String details;

    private String regionIds;

}
