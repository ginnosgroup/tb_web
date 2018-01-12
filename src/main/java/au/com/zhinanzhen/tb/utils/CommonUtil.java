package au.com.zhinanzhen.tb.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class CommonUtil {
	//获取UUID
		public static String getUuid(){
			SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS"); 
			return sdFormat.format(new Date());  
		}
			
		//ISO-8859-1转换为utf-8编码
		public static String iso8859ToUtf8(String strContent) throws Exception{
			
			return new String(strContent.getBytes("ISO-8859-1"),"utf-8"); 
		}
		
		public static String getSystemDate(){
			Date date = new Date(); 
			String systemDate = (new SimpleDateFormat("yyyy-MM-dd")).format(date);
			
			return systemDate;
		}
		
		public static String getSystemTime(){
			Date date = new Date(); 
			String systemDate = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(date);
			
			return systemDate;
		}
		public static String getSystemDate2(){
			Date date = new Date(); 
			String systemDate = (new SimpleDateFormat("yyyy-MM-dd")).format(date);
			
			return systemDate;
		}
		//时间差（秒）
		public static long getTimeDifference(String strDate1,String strDate2) throws Exception{
			
			 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
			 Date date1 = df.parse(strDate1);   
			 Date date2= df.parse(strDate2);   
			 
			 long l=date1.getTime()-date2.getTime();   
			 //long day=l/(24*60*60*1000);   
			 //long hour=(l/(60*60*1000)-day*24);   
			 //long min=((l/(60*1000))-day*24*60-hour*60);   
			 long s=(l/1000);
			
			return s;
		}
			
		public static String getRandom4(){
			
			Random random = new Random(); 
			String result="";

			for(int i=0;i<4;i++){
				result+=random.nextInt(10);   
			}	
			
			return result;
		}	
		
		public static boolean isNotNull(String str){
			
		    return ((str != null) && (!(str.trim().equals(""))) && (!(str.equalsIgnoreCase("null"))));
		    
		}
		/**
		 * 
		* @Title: CalculateTimeByAdd
		* @Description: 计算时间(时间+秒数)
		* @param @param String
		* @param @param int
		* @param @return
		* @return String
		* @throws
		*/
		public static String calculateTimeByAdd(String inputTime,long second) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time="";
			try {
				long times = sdf.parse(inputTime).getTime();
				//获取毫秒
				times = times + second * 1000;
				//long转date
				Date date = new Date(times);
				time=sdf.format(date);
				
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return time;
		}
}
