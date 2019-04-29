package au.com.zhinanzhen.tb.service.pojo;

import java.util.Date;

import au.com.zhinanzhen.tb.dao.pojo.UserDO;
import lombok.Data;

@Data
public class UserDTO {

	private int id;

	private String name;

	private String familyName;

	private String givenName;

	private Date birthday;

	private String phone;

	private String email;

	private String authType;

	private String authOpenId;

	private String username;

	private String nickname;
	
	private String wechatUsername;

	private String logo;

	private double balance;

	private int regionId;

	private Integer adviserId;

	private String recommendOpenid;

	private AdviserDTO adviserDto;

	private UserDO recommendUserDO;

	private Date createDate;
}
