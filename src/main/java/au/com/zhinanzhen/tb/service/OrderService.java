package au.com.zhinanzhen.tb.service;

import java.util.List;

import au.com.zhinanzhen.tb.service.pojo.OrderResultDTO;

public interface OrderService {
    /**
     * 增加订单
     * 
     * @param subjectId
     *            课程编号
     * @param num
     *            购买课程数量
     * @param userId
     *            顾客编号
     * @param introducerUserId
     *            推荐人编号
     * @return
     */
    int addOrder(int subjectId, int num, int userId, double balancePayAmount, Integer introducerUserId, int regionId) throws ServiceException;

    /**
     * 支付后修改订单状态(不修改付款日志)
     * 
     * @param orderId
     * @param payType
     * @param payCode
     * @param payMoney
     * @return
     * @throws ServiceException
     */
    boolean payOrder(int orderId, String payType, String payCode, Double payMoney) throws ServiceException;

    /**
     * 获取订单详情
     * 
     * @param orderId
     * @return
     * @throws ServiceException
     */
    OrderResultDTO getOrderResult(int orderId) throws ServiceException;
    /**
     * 获取顾客所有订单
     * 
     * @param buyerId
     * @param state
     * @return
     * @throws ServiceException
     */
    List<OrderResultDTO> getOrderResultList(int userId, String state) throws ServiceException;
    /**
     * 获取推荐人是该用户的订单列表
     * 
     * @param buyerId
     * @param state
     * @return
     * @throws ServiceException
     */
    List<OrderResultDTO> getListByIntroducer(int introducerId) throws ServiceException;
    /**
     * 更具课程状态更新订单状态
     * 
     * @return
     * @throws ServiceException
     */
    boolean updateOrderStateBySubjectState(int userId) throws ServiceException;

    /**
     * @param orderId  订单编号
     * @param balanceMoney  余额支付金额
     * @return
     * @throws ServiceException
     */
    boolean balancePay(int orderId, double balanceMoney) throws ServiceException;
    /**
     * 根据课程Id获取订单列表
     * @param subjectId
     * @return
     * @throws ServiceException
     */
    List<OrderResultDTO> getListBySubjectId(int subjectId) throws ServiceException;
}
