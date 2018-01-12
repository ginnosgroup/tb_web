package au.com.zhinanzhen.tb.dao.pojo;

import lombok.Data;
@Data
public class SubjectPriceDO {
   
    private Integer id;

    private Integer subjectId;

    private Integer startNum;

    private Integer endNum;

    private String regionIds;
}