package au.com.zhinanzhen.tb.utils;

import java.security.MessageDigest;
import java.util.Formatter;

import com.ikasoa.core.utils.StringUtil;

public class Sha1 {
    public static void main(String[] args) throws Exception {
	String param = "";
	System.out.println(getSign(param));
    }

    public static String getSign(String param) throws Exception {
	if (StringUtil.isEmpty(param)) {
	    throw new Exception("param is null 1");
	}
	MessageDigest crypt = MessageDigest.getInstance("SHA-1");
	crypt.reset();
	crypt.update(param.getBytes("UTF-8"));
	String result = byteToHex(crypt.digest());
	return result;
    }

    private static String byteToHex(final byte[] hash) {
	Formatter formatter = new Formatter();
	for (byte b : hash) {
	    formatter.format("%02x", b);
	}
	String result = formatter.toString();
	formatter.close();
	return result;
    }
}
