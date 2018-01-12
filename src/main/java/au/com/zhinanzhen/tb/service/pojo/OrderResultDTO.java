package au.com.zhinanzhen.tb.service.pojo;

import java.math.BigDecimal;
import java.util.Date;

import au.com.zhinanzhen.tb.dao.pojo.AdviserDO;
import au.com.zhinanzhen.tb.dao.pojo.RegionDO;
import au.com.zhinanzhen.tb.service.pojo.SubjectResultDTO;
import lombok.Data;

@Data
public class OrderResultDTO {

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

    private double waitPayAmount;

    private Integer userId;

    private Integer introducerUserId;

    private int adviserId;

    private Date createDate;

    private Date adviserDate;

    private int regionId;

    private double remainPayAmount;

    private Date remainPayDate;

    private double remainPayBalance;

    private UserDTO userDo;

    private UserDTO introducerDo;

    private SubjectResultDTO subjectResultDto;

    private RegionDO regionDo;

    private AdviserDO adviserDo;

}