package au.com.zhinanzhen.tb.service;

public enum ThirdTypeEnum {

    WECHAT("微信"), FACEBOOK("facebook"), V("虚拟用户");

    private String value;

    private ThirdTypeEnum(String value) {
	this.value = value;
    }

    public static ThirdTypeEnum get(String name) {
	for (ThirdTypeEnum e : ThirdTypeEnum.values()) {
	    if (e.toString().equals(name)) {
		return e;
	    }
	}
	return null;
    }

    public String getValue() {
	return value;
    }

    public void setValue(String value) {
	this.value = value;
    }
}
