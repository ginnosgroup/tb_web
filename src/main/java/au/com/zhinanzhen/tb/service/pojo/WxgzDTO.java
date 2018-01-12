package au.com.zhinanzhen.tb.service.pojo;

import lombok.Data;

/**
 * 微信公众平台参数
 * 
 * @author <a href="mailto:leisu@zhinanzhen.org">sulei</a>
 * @version 0.1
 */
@Data
public class WxgzDTO {
    
    String appId;
    
    Long timestamp;
    
    String nonceStr;
    
    String signature;
}
