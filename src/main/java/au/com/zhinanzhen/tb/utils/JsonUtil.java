package au.com.zhinanzhen.tb.utils;


import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ikasoa.core.utils.StringUtil;

public class JsonUtil {

	public static void main(String[] args) throws Exception {
		 String jstr =
		 "{\"id\":\"PAY-757028912G260825HLBQ5X4A\",\"intent\":\"sale\",\"payer\":{\"payment_method\":\"paypal\",\"payer_info\":{\"email\":\"leisu-buyer@zhinanzhen.org\",\"first_name\":\"test\",\"last_name\":\"buyer\",\"payer_id\":\"RNZHKT6APM37E\",\"country_code\":\"C2\",\"shipping_address\":{\"recipient_name\":\"testbuyer\",\"line1\":\"NO1NanJinRoad\",\"city\":\"Shanghai\",\"country_code\":\"C2\",\"postal_code\":\"200000\",\"state\":\"Shanghai\"}}},\"cart\":\"30T3685268774942Y\",\"transactions\":[{\"related_resources\":[{\"sale\":{\"id\":\"13R144230U9615359\",\"amount\":{\"currency\":\"AUD\",\"total\":\"8.00\",\"details\":{\"subtotal\":\"0.00\",\"shipping\":\"8.00\",\"tax\":\"0.00\"}},\"payment_mode\":\"INSTANT_TRANSFER\",\"state\":\"completed\",\"protection_eligibility\":\"ELIGIBLE\",\"protection_eligibility_type\":\"ITEM_NOT_RECEIVED_ELIGIBLE,UNAUTHORIZED_PAYMENT_ELIGIBLE\",\"transaction_fee\":{\"currency\":\"AUD\",\"value\":\"0.57\"},\"parent_payment\":\"PAY-757028912G260825HLBQ5X4A\",\"create_time\":\"2016-12-27T03:12:20Z\",\"update_time\":\"2016-12-27T03:12:20Z\",\"links\":[{\"href\":\"https://api.sandbox.paypal.com/v1/payments/sale/13R144230U9615359\",\"rel\":\"self\",\"method\":\"GET\"},{\"href\":\"https://api.sandbox.paypal.com/v1/payments/sale/13R144230U9615359/refund\",\"rel\":\"refund\",\"method\":\"POST\"},{\"href\":\"https://api.sandbox.paypal.com/v1/payments/payment/PAY-757028912G260825HLBQ5X4A\",\"rel\":\"parent_payment\",\"method\":\"GET\"}]}}],\"amount\":{\"currency\":\"AUD\",\"total\":\"8.00\",\"details\":{\"subtotal\":\"0.00\",\"shipping\":\"8.00\",\"tax\":\"0.00\"}},\"description\":\"testpaypal\",\"item_list\":{\"items\":[{\"name\":\"PandaDeliveryService\",\"quantity\":\"1\",\"price\":\"0.00\",\"currency\":\"AUD\"}],\"shipping_address\":{\"line1\":\"NO1NanJinRoad\",\"city\":\"Shanghai\",\"country_code\":\"C2\",\"postal_code\":\"200000\",\"state\":\"Shanghai\"}}}],\"state\":\"approved\",\"create_time\":\"2016-12-27T03:12:21Z\",\"links\":[{\"href\":\"https://api.sandbox.paypal.com/v1/payments/payment/PAY-757028912G260825HLBQ5X4A\",\"rel\":\"self\",\"method\":\"GET\"}]}";
		// JSONObject object = JSON.parseObject(jstr);
		// System.out.println(object.get("id"));

//		String jstr = "{ \"id\": \"PAY-7WE51099967330643LBQ7WIA\", \"intent\": \"sale\", \"payer\": { \"payment_method\": \"paypal\" }, \"transactions\": [ { \"related_resources\": [], \"amount\": { \"currency\": \"AUD\", \"total\": \"8.00\", \"details\": { \"subtotal\": \"0.00\", \"shipping\": \"8.00\", \"tax\": \"0.00\" } }, \"description\": \"test paypal\", \"item_list\": { \"items\": [ { \"name\": \"Panda Delivery Service\", \"quantity\": \"1\", \"price\": \"0.00\", \"currency\": \"AUD\" } ] } } ], \"state\": \"created\", \"create_time\": \"2016-12-27T05:24:48Z\", \"links\": [ { \"href\": \"https://api.sandbox.paypal.com/v1/payments/payment/PAY-7WE51099967330643LBQ7WIA\", \"rel\": \"self\", \"method\": \"GET\" }, { \"href\": \"https://www.sandbox.paypal.com/cgi-bin/webscr?cmd\u003d_express-checkout\u0026token\u003dEC-08474060X21048350\", \"rel\": \"approval_url\", \"method\": \"REDIRECT\" }, { \"href\": \"https://api.sandbox.paypal.com/v1/payments/payment/PAY-7WE51099967330643LBQ7WIA/execute\", \"rel\": \"execute\", \"method\": \"POST\" } ] }";
		System.out.println(getState(jstr));

	}

	public static String getPayId(String jsonStr) throws Exception {
		if (StringUtil.isNotEmpty(jsonStr)) {
			JSONObject object = JSON.parseObject(jsonStr);
			return object.get("id").toString();
		}
		return null;
	}

	public static String getApprovalUrl(String jsonStr) throws Exception {
		if (StringUtil.isNotEmpty(jsonStr)) {
			JSONObject object = JSON.parseObject(jsonStr);
			@SuppressWarnings("unchecked")
			List<JSONObject> linkList = (List<JSONObject>) object.get("links");
			for (JSONObject linkObj : linkList) {
				if ("approval_url".equals(linkObj.get("rel"))) {
					return linkObj.get("href").toString();
				}
			}
		}
		return null;

	}

	public static String getState(String jsonStr) {
		if (StringUtil.isNotEmpty(jsonStr)) {
			JSONObject object = JSON.parseObject(jsonStr);
			return object.get("state").toString();
		}
		return null;
	}

}
