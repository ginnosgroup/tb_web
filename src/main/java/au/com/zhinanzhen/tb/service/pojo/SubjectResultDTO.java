package au.com.zhinanzhen.tb.service.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import au.com.zhinanzhen.tb.service.SubjectTypeEnum;
import lombok.Data;

@Data
public class SubjectResultDTO {

    private int id;

    private String name;
    
    private SubjectTypeEnum type;
	
	private int parentId;
    
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
    
    private int buyNumber;
 
    private List<SubjectPriceDTO> subjectPriceDtolist = new ArrayList<SubjectPriceDTO>();
}
