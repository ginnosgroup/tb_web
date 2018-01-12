package au.com.zhinanzhen.tb.service;

public enum SubjectCategoryClassifyEnum {

    ALL("全部"), ENABLED("显示"), DISABLED("不显示");

    private String value;

    private SubjectCategoryClassifyEnum(String value) {
	this.value = value;
    }

    public static SubjectCategoryClassifyEnum get(String name) {
	for (SubjectCategoryClassifyEnum e : SubjectCategoryClassifyEnum.values()) {
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
