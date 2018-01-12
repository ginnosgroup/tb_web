package au.com.zhinanzhen.tb.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class HttpUtil {
	/**
	 * 发起https请求并获取结果
	 * 
	 * @param requestUrl
	 *            请求地址
	 * @throws Exception
	 */
	public static String httpRequest(String url, Map<String, String> params)
			throws Exception {
		URL u = null;

		HttpURLConnection con = null;

		// 构建请求参数

		//StringBuffer sb = new StringBuffer();
		
		String str = "";

//		if (params != null) {
//
//			for (Entry<String, String> e : params.entrySet()) {
//
//				sb.append(e.getKey());
//
//				sb.append("=");
//
//				sb.append(e.getValue());
//
//				sb.append("&");
//
//			}
//
//			str = sb.substring(0, sb.length() - 1);
//
//		}

		System.out.println("send_url:" + url);

		System.out.println("send_data:" + str);

		// 尝试发送请求

		try {

			u = new URL(url);

			con = (HttpURLConnection) u.openConnection();

			con.setRequestMethod("GET");

			con.setDoOutput(true);

			con.setDoInput(true);

			con.setUseCaches(false);

			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			
			con.setRequestProperty("Accept-Charset", "UTF-8");

			//OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");

			//osw.write("query=1&region=58&output=json&ak=MEXwvluA50fG9MHFIELyodie");

			//osw.flush();

			//osw.close();

		} catch (Exception e) {

			e.printStackTrace();

		} finally {

			if (con != null) {

				con.disconnect();

			}

		}

		// 读取返回内容

		StringBuffer buffer = new StringBuffer();

		try {

			BufferedReader br = new BufferedReader(new InputStreamReader(con

			.getInputStream(), "UTF-8"));

			String temp;

			while ((temp = br.readLine()) != null) {

				buffer.append(temp);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return buffer.toString();
	}
	
	/**
	 * 
	 * @param put_url
	 * @param param
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String put(String put_url,  NameValuePair[] params) throws HttpException, IOException {
		HttpClient httpClient = new HttpClient();
		PutMethod httpPut = new PutMethod(put_url);
		httpPut.setQueryString(params);  //以键值对形式传递参数
		httpPut.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
		httpClient.executeMethod(httpPut);
		String res = httpPut.getResponseBodyAsString();
		return res;
	}
}
