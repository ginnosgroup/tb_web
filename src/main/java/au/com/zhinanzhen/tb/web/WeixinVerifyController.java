package au.com.zhinanzhen.tb.web;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import au.com.zhinanzhen.tb.utils.ConfigService;
@CrossOrigin(origins = "*", maxAge = 3600)
@Controller
@RequestMapping("/wx")
public class WeixinVerifyController extends AbstractController {
    @Resource
    private ConfigService configService;
    @RequestMapping("/vertify")
    public void verifyWexin(HttpServletRequest request, HttpServletResponse response) throws Exception {
	// 微信加密签名
	String signature = request.getParameter("signature");
	// 时间戳
	String timestamp = request.getParameter("timestamp");
	// 随机数
	String nonce = request.getParameter("nonce");
	// 随机字符串
	String echostr = request.getParameter("echostr");

	PrintWriter out = response.getWriter();
	// 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
	if (checkSignature(signature, timestamp, nonce)) {
	    out.print(echostr);
	}
	out.close();
    }

    private boolean checkSignature(String signature, String timestamp, String nonce) {
        String token=configService.getKey();
	String[] arr = new String[] { token, timestamp, nonce };
	// 将token、timestamp、nonce三个参数进行字典序排序
	Arrays.sort(arr);
	StringBuilder content = new StringBuilder();
	for (int i = 0; i < arr.length; i++) {
	    content.append(arr[i]);
	}
	MessageDigest md = null;
	String tmpStr = null;

	try {
	    md = MessageDigest.getInstance("SHA-1");
	    // 将三个参数字符串拼接成一个字符串进行sha1加密
	    byte[] digest = md.digest(content.toString().getBytes());
	    tmpStr = byteToStr(digest);
	} catch (NoSuchAlgorithmException e) {
	    e.printStackTrace();
	}
	content = null;
	// 将sha1加密后的字符串可与signature对比，标识该请求来源于微信
	return tmpStr != null ? tmpStr.equals(signature.toUpperCase()) : false;
    }

    /**
     * 将字节数组转换为十六进制字符串
     * 
     * @param byteArray
     * @return
     */
    private static String byteToStr(byte[] byteArray) {
	String strDigest = "";
	for (int i = 0; i < byteArray.length; i++) {
	    strDigest += byteToHexStr(byteArray[i]);
	}
	return strDigest;
    }

    /**
     * 将字节转换为十六进制字符串
     * 
     * @param mByte
     * @return
     */
    private static String byteToHexStr(byte mByte) {
	char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	char[] tempArr = new char[2];
	tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
	tempArr[1] = Digit[mByte & 0X0F];

	String s = new String(tempArr);
	return s;
    }
}
