package au.com.zhinanzhen.tb.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class AdminUserDO {

    private Integer id;

    private String username;

    private String password;

    private String apList;

    private String sessionId;

    private Date gmtLogin;

    private String loginIp;

    private Byte status;

}