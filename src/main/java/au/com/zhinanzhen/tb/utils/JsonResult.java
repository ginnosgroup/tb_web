package au.com.zhinanzhen.tb.utils;


import java.io.Serializable;

/**
 * 用于封装AJAX调用以后的JSON返回?
 * 其中正确返回:
 *  {state:0, data:返回数据, message:错误消息}
 * 错误返回:
 *  {state:1, data:null, message:错误消息}
 */
public class JsonResult implements Serializable{
	private static final long serialVersionUID = -3644950655568598241L;

	private static final int SUCCESS=0;
	private static final int ERROR=1;
	
	/** 
	 * 返回是否成功, 0表示成功, 
	 * 1或其他 表示失败
	 */
	private int state;
	/**
	 * 成功时,返回的JSON数据
	 */
	private Object data;
	/**
	 * 是错误时候的错误消息
	 */
	private String message;
	
	
	public JsonResult() {
	}


	public JsonResult(int state, Object data, String message) {
 		this.state = state;
		this.data = data;
		this.message = message;
	}
	
	public JsonResult(Throwable e){
		state = ERROR;
		data=null;
		message=e.getMessage();
	}
	
	public JsonResult(Object data){
		state = SUCCESS;
		this.data=data;
		message="";
	}

	public JsonResult(
			int state, Throwable e) {
		this.state = state;
		data=null;
		message = e.getMessage();
	}


	public int getState() {
		return state;
	}


	public void setState(int state) {
		this.state = state;
	}


	public Object getData() {
		return data;
	}


	public void setData(Object data) {
		this.data = data;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	@Override
	public String toString() {
		return "JsonResult [state=" + state + ", data=" + data + ", message=" + message + "]";
	}
	
	
	
}







