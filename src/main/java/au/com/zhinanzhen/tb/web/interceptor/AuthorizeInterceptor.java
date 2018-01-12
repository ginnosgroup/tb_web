package au.com.zhinanzhen.tb.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class AuthorizeInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request,
	    HttpServletResponse response, Object handler) throws Exception {
	boolean bool = super.preHandle(request, response, handler);
//	if (bool) {
//	    String url = request.getRequestURL().toString();
//	    if (url.endsWith("login")) {
//		return bool;
//	    }
//	    HttpSession session = request.getSession();
//	    User user = (User) session.getAttribute("user");
//	    if (user == null) {
//		response.sendRedirect("login.html");
//	    }
//	}
	return bool;
    }
}