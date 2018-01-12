package au.com.zhinanzhen.tb.utils;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http处理类
 * 
 * @author Larry
 */
public class HttpUtil2 {

	private static final Logger LOG = LoggerFactory.getLogger(HttpUtil.class);

	private static HttpClient httpClient = new DefaultHttpClient();

	public static String httpGet(String url, Map<String, String> requestParams) {

		HttpGet httpGet = null;

		try {
			// 参数设置
			StringBuilder builder = new StringBuilder(url);
			builder.append("?");
			for (Map.Entry<String, String> entry : requestParams.entrySet()) {
				builder.append(entry.getKey());
				builder.append("=");
				builder.append(entry.getValue());
				builder.append("&");
			}

			String tmpUrl = builder.toString();
			tmpUrl = tmpUrl.substring(0, tmpUrl.length() - 1);

			httpGet = new HttpGet(tmpUrl);

			HttpResponse response = httpClient.execute(httpGet);

			// 网页内容
			HttpEntity httpEntity = response.getEntity();
			return EntityUtils.toString(httpEntity);

		} catch (ClientProtocolException e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (httpGet != null) {
				httpGet.abort();
			}
		}
		return "";
	}

	public static String httpPost(String url, Map<String, String> requestParams, String urlEncode) {

		HttpPost httpPost = null;

		try {
			// 参数设置
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entry : requestParams.entrySet()) {
				params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}

			httpPost = new HttpPost(url);
			httpPost.setEntity(new UrlEncodedFormEntity(params, urlEncode));
			
			
			// reponse header
			HttpResponse response = httpClient.execute(httpPost);

			// 网页内容
			HttpEntity httpEntity = response.getEntity();

			return EntityUtils.toString(httpEntity);

		} catch (UnsupportedEncodingException e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (httpPost != null) {
				httpPost.abort();
			}
		}
		return "";
	}
	public static String httpPostByHeader(String url, Map<String, String> requestParams, String urlEncode,String headerName,String headerValue) {

		HttpPost httpPost = null;
		
		try {
			// 参数设置
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entry : requestParams.entrySet()) {
				params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}

			httpPost = new HttpPost(url);
			httpPost.setHeader(headerName, headerValue);
			httpPost.setEntity(new UrlEncodedFormEntity(params, urlEncode));
			
			
			// reponse header
			HttpResponse response = httpClient.execute(httpPost);

			// 网页内容
			HttpEntity httpEntity = response.getEntity();

			return EntityUtils.toString(httpEntity);

		} catch (UnsupportedEncodingException e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (httpPost != null) {
				httpPost.abort();
			}
		}
		return "";
	}
}

