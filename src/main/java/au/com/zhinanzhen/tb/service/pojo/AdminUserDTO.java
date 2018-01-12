package au.com.zhinanzhen.tb.service.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class AdminUserDTO {

    private Integer id;

    private String username;

    private String password;

    private String apList;

    private String sessionId;

    private Date gmtLogin;

    private String loginIp;

    private Byte status;

}