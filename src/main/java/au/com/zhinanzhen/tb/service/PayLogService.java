package au.com.zhinanzhen.tb.service;

import java.util.Date;

public interface PayLogService {
    boolean addPayLog (int userId, int orderId, String payType, String payCode, double payAmount, Date payDate) throws ServiceException;
}
