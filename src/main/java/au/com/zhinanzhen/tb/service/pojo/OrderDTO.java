package au.com.zhinanzhen.tb.service.pojo;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class OrderDTO {

    private Integer id;

    private String name;

    private String state;

    private Integer subjectId;

    private Integer num;

    private BigDecimal amount;

    private String payType;

    private String payCode;

    private BigDecimal payAmount;

    private Date payDate;

    private BigDecimal createPrice;

    private BigDecimal finishPrice;

    private Integer userId;

    private int adviserId;

    private Date adviserDate;

    private int regionId;
    
    private Date createDate;
    
    private double remainPayAmount;
    
    private Date remainPayDate;
    
    private double remainPayBalance;

}