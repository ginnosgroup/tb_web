package au.com.zhinanzhen.tb.dao.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class UserDO {

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
	
	private String source;

	private String logo;

	private double balance;

	private int regionId;

	private Integer adviserId;

	private String recommendOpenid;

	private Date createDate;

}
