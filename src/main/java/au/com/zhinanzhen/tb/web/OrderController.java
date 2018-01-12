package au.com.zhinanzhen.tb.web;

import java.util.List;

import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ikasoa.core.utils.StringUtil;

import au.com.zhinanzhen.tb.service.pojo.OrderResultDTO;
import au.com.zhinanzhen.tb.service.AdviserService;
import au.com.zhinanzhen.tb.service.OrderService;
import au.com.zhinanzhen.tb.service.OrderStateEnum;
import au.com.zhinanzhen.tb.service.RegionService;
import au.com.zhinanzhen.tb.service.UserService;
import au.com.zhinanzhen.tb.service.pojo.UserDTO;
import au.com.zhinanzhen.tb.utils.JsonResult;

@Controller
@RequestMapping("/order")
public class OrderController extends AbstractController {
    @Resource
    private RegionService regionService;
    @Resource
    private OrderService orderService;
    @Resource
    private UserService userService;
    @Resource
    private AdviserService adviserService;

    @ResponseBody
    @RequestMapping("/add")
    public JsonResult addOrder(int subjectId, int num, double balancePayAmount, int regionId, String introducerOpenId,
	    String openId, String thirdType) throws Exception {
	if (StringUtil.isEmpty(thirdType)) {
	    Exception e = new Exception("thirdTyp不能空");
	    return new JsonResult(2, e);
	}
	if (StringUtil.isEmpty(openId)) {
	    Exception e = new Exception("openId不能空");
	    return new JsonResult(2, e);
	}
	int userId = getUserIdByOpenId(thirdType, openId);
	UserDTO userDto = userService.getUserDTOById(userId);
	if (balancePayAmount > userDto.getBalance()) {
	    Exception e = new Exception("余额不够");
	    return new JsonResult(3, e);
	}
	if (balancePayAmount > 100) {
	    balancePayAmount = 100;
	}
	Integer introducerId = null;
	if (introducerOpenId == null || introducerOpenId.equals("0")) {
	    introducerId = null;
	} else if (introducerOpenId.equals(openId)) {
	    introducerId = null;
	} else {
	    introducerId = getUserIdByOpenId(thirdType, introducerOpenId);
	}
	if (introducerId == null && userDto.getRecommendOpenid() != null) {
	    List<OrderResultDTO> list = orderService.getOrderResultList(userId, OrderStateEnum.ALL.toString());
	    if (list == null || list.size() == 0) {
		try {
		    introducerId = getUserIdByOpenId(thirdType, userDto.getRecommendOpenid());
		} catch (Exception e) {
		    introducerId = null;
		}
	    }
	}
	int orderId = orderService.addOrder(subjectId, num, userId, balancePayAmount, introducerId, regionId);
	return new JsonResult(orderId);
    }

    @ResponseBody
    @RequestMapping("/list")
    public JsonResult getOrderList(String state, String openId, String thirdType) throws Exception {
	if (StringUtil.isEmpty(thirdType)) {
	    Exception e = new Exception("thirdTyp不能空");
	    return new JsonResult(2, e);
	}
	if (StringUtil.isEmpty(openId)) {
	    Exception e = new Exception("openId不能空");
	    return new JsonResult(2, e);
	}
	int userId = getUserIdByOpenId(thirdType, openId);
	if (StringUtil.isEmpty(state) || OrderStateEnum.get(state) == null) {
	    Throwable e = new Throwable("分类参数错误");
	    return new JsonResult(2, e);
	}
	List<OrderResultDTO> orderResultList = orderService.getOrderResultList(userId, state);
	return new JsonResult(orderResultList);
    }

    @ResponseBody
    @RequestMapping("/listByIntroducer")
    public JsonResult getListByIntroducer(String openId, String thirdType) throws Exception {
	if (StringUtil.isEmpty(thirdType)) {
	    Exception e = new Exception("thirdType不能空");
	    return new JsonResult(2, e);
	}
	if (StringUtil.isEmpty(openId)) {
	    Exception e = new Exception("openId不能空");
	    return new JsonResult(2, e);
	}
	int userId = getUserIdByOpenId(thirdType, openId);
	List<OrderResultDTO> orderResultList = orderService.getListByIntroducer(userId);
	return new JsonResult(orderResultList);
    }

    @ResponseBody
    @RequestMapping("/get")
    public JsonResult getOrderResult(int orderId) throws Exception {
	if (orderId < 0) {
	    Throwable e = new Throwable("orderId error !");
	    return new JsonResult(2, e);
	}
	OrderResultDTO orderResult = orderService.getOrderResult(orderId);
	return new JsonResult(orderResult);
    }

    @ResponseBody
    @RequestMapping("/update")
    public JsonResult update(Integer userId, String openId, String thirdType) throws Exception {
	if (userId == null && openId == null) {
	    Throwable e = new Throwable("userId and openId is null !");
	    return new JsonResult(2, e);
	}
	if (userId == null && StringUtil.isNotEmpty(openId) && StringUtil.isNotEmpty(thirdType)) {
	    userId = getUserIdByOpenId(thirdType, openId);
	}
	return new JsonResult(orderService.updateOrderStateBySubjectState(userId));
    }

    @ResponseBody
    @RequestMapping("/listBySubjectId")
    public JsonResult getOrderList(int subjectId) throws Exception {
	if (subjectId <= 0) {
	    Throwable e = new Throwable("课程Id错误 !");
	    return new JsonResult(2, e);
	}
	List<OrderResultDTO> orderResultList = orderService.getListBySubjectId(subjectId);
	return new JsonResult(orderResultList);
    }
}