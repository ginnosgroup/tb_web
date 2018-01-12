package au.com.zhinanzhen.tb.service;

public enum PayTypeEnum {

    WECHAT("微信"), PAYPAL("PayPal"), BALANCE("余额抵扣"), OTHER("其它支付方式"), IOSPAY("IOS第三方支付");
    ;

    private String value;

    private PayTypeEnum(String value) {
	this.value = value;
    }

    public static PayTypeEnum get(String name) {
	for (PayTypeEnum e : PayTypeEnum.values()) {
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
