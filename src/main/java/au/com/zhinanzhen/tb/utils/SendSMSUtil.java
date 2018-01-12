package au.com.zhinanzhen.tb.utils;

import java.util.HashMap;
import java.util.Map;

import com.ikasoa.core.utils.StringUtil;

/**
 * 发送澳洲短信的实现类 (burstsms)
 * 
 * @author Larry
 */
public class SendSMSUtil {

	private static final String USER_NAME = "2d6c5ba574e0262d20aead9dd9b59b3c";

	private static final String PASSWORD = "acm1234567";

	private static final String CONTENT = "Your confirmation code is $0, please verify in 5 minutes.";

	private static final String ADDSUCCESS = "您的订单$1已确认。给大家推荐一个棒棒哒的外卖软件！复制整段文字发微信朋友圈并把截图给送餐员，即可得到1.5刀代金券，截图需显示已发出10分钟以上并对所有人可见哦！指南针集团祝您用餐愉快。App下载链接：https://www.ozhaha.com";

	public static Map<String, String> sendMsg(String phone) throws Exception {

		Map<String, String> map = new HashMap<String, String>();

		// 获取系统日期
		String systemDate = CommonUtil.getSystemDate();
		// 获取系统时间
		String systemTime = CommonUtil.getSystemTime();
		// 获取4为随机验证码
		String identifyingCode = CommonUtil.getRandom4();

		String sign = "Zhinanzhen";

		// 创建StringBuffer对象用来操作字符串
		StringBuffer sb = new StringBuffer(
				"https://" + USER_NAME + ":" + PASSWORD + "@api.transmitsms.com/send-sms.json?");

		Map<String, String> pmap = new HashMap<String, String>();
		String index="Zhinanzhen";
		pmap.put("message", "[" + index + "]" + CONTENT.replace("$0", identifyingCode));
		pmap.put("to", updatePhoneNum(phone));
                pmap.put("from", sign);
		String inputline = HttpUtil2.httpPost(sb.toString(), pmap, "UTF-8");
                System.out.println(inputline);
		if (inputline.indexOf("SUCCESS") > 0) {
			map = new HashMap<String, String>();
			map.put("date", systemDate);
			map.put("time", systemTime);
			map.put("identifyingCode", identifyingCode);
		} else {
			throw new Exception("sendSMS error !");
		}
		return map;
	}

	public static Map<String, String> sendAddOrderSuccess(String phone, int orderId) throws Exception {

		Map<String, String> map = new HashMap<String, String>();

		// 获取系统日期
		String systemDate = CommonUtil.getSystemDate();
		// 获取系统时间
		String systemTime = CommonUtil.getSystemTime();

		String sign = "哈哈外卖";

		// 创建StringBuffer对象用来操作字符串
		StringBuffer sb = new StringBuffer(
				"https://" + USER_NAME + ":" + PASSWORD + "@api.transmitsms.com/send-sms.json?");

		Map<String, String> pmap = new HashMap<String, String>();
		pmap.put("message", "【" + sign + "】" + ADDSUCCESS.replace("$1", orderId + ""));
		pmap.put("to", updatePhoneNum(phone));
		String inputline = HttpUtil2.httpPost(sb.toString(), pmap, "UTF-8");

		if (inputline.indexOf("SUCCESS") > 0) {
			map = new HashMap<String, String>();
			map.put("date", systemDate);
			map.put("time", systemTime);
		} else {
			throw new Exception("sendSMS error !");
		}
		return map;
	}

	private static String updatePhoneNum(String phone) throws Exception {
		if (StringUtil.isEmpty(phone)) {
			throw new Exception("phone error !");
		}
		Long l = Long.parseLong(phone);
		if (l < 500000000L && l > 400000000L) {
			return l + 61000000000L + "";
		} else {
			return phone;
		}
	}

	public static void main(String[] args) throws Exception {
		// System.out.println(updatePhoneNum("0452365915"));
		System.out.println(sendMsg("0452365915"));
	}
}
