package au.com.zhinanzhen.tb.web.interceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import au.com.zhinanzhen.tb.utils.CommonUtil;

@Component
public class AccessInterceptor implements HandlerInterceptor {
    private static Map<String, Integer> map = new HashMap<String, Integer>();

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
	    throws Exception {

	String name = request.getRemoteAddr();
	String url = request.getServletPath();
	if (map.get(name) == null) {
	    map.put(name, 1);
	} else {
	    map.put(name, map.get(name) + 1);
	}
	String date = CommonUtil.getSystemTime();
	System.out.println(name + "在调" + url + "接口,已经调用了" + map.get(name) + "次,调用时间:"+date);
	List<String> paths = new ArrayList<String>();
	paths.add("/user/showBalance");
	paths.add("/user/showBalancePayMoney");
	paths.add("/user/getIntroducer");
	paths.add("/user/update");
	paths.add("/order/add");
	paths.add("/order/list");
	paths.add("/order/listByIntroducer");
	paths.add("/order/listByIntroducer");
	paths.add("/paypal/pay");
	paths.add("/royalPay/pay");
	if("/wxgz/thirdLogin".equals(url)){
	    System.out.println("url:"+request.getAttribute("url"));
	    System.out.println("recommendOpenid:"+request.getAttribute("recommendOpenid"));
	}
	// 跨域
	response.setHeader("Access-Control-Allow-Origin", "*");
	response.setHeader("Access-Control-Allow-Headers", "Content-Type");
	response.setHeader("Access-Control-Allow-Credentials", "true");
	response.setHeader("Access-Control-Allow-Methods", "GET,POST");
	response.setHeader("Allow", "GET,POST");
//	response.setHeader("Access-Control-Allow-Headers", "X-Requested-With,content-type");
//	response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, PATCH, DELETE, HEAD");
//	response.setHeader("Access-Control-Allow-Headers",
//		"x-requested-with, Content-Type, MUserAgent, MToken, UID, JSESSIONID");
	return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
	    ModelAndView modelAndView) throws Exception {
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
	    throws Exception {
    }
}