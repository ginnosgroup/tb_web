package au.com.zhinanzhen.tb.service;

public enum SubjectClassifyEnum {

    ALL("全部"), AVAILABLE("可选择"), WAIT("未开始"), START("拼团中"), END("已结束"), STOP("已终止");

    private String value;

    private SubjectClassifyEnum(String value) {
	this.value = value;
    }

    public static SubjectClassifyEnum get(String name) {
	for (SubjectClassifyEnum e : SubjectClassifyEnum.values()) {
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
