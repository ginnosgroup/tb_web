package au.com.zhinanzhen.tb.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;


public class QrCodeUtil {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;
    public static void main(String[] args) {
	String sendUrl = "/user/test?openId=xyh";
	String jsonstr = HttpUtil2.httpGet(sendUrl, new HashMap<String, String>());
	System.out.println("jsonstr:"+jsonstr);
    }

    public static BufferedImage printCode(String qrData) throws Exception {
////	Qrcode qrcode = new Qrcode();
////	qrcode.setQrcodeErrorCorrect('M');// 纠错等级（分为L、M、H三个等级）
////	qrcode.setQrcodeEncodeMode('B');// N代表数字，A代表a-Z，B代表其它字符
////	qrcode.setQrcodeVersion(7);// 版本
////	// 生成二维码中要存储的信息
////	BufferedImage bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
////	// 绘图
//	Graphics2D gs = bufferedImage.createGraphics();
//	gs.setBackground(Color.WHITE);
//	gs.setColor(Color.BLACK);
//	gs.clearRect(0, 0, WIDTH, HEIGHT);// 清除下画板内容
//	// 设置下偏移量,如果不加偏移量，有时会导致出错。
//	int pixoff = 2;
//	byte[] d = qrData.getBytes("gb2312");
//	if (d.length > 0 && d.length < 120) {
//////	    boolean[][] s = qrcode.calQrcode(d);
////	    for (int i = 0; i < s.length; i++) {
////		for (int j = 0; j < s.length; j++) {
////		    if (s[j][i]) {
////			gs.fillRect(j * 3 + pixoff, i * 3 + pixoff, 3, 3);
////		    }
////		}
////	    }
////	}
//	gs.dispose();
//	bufferedImage.flush();
//	return bufferedImage;
	return null;
    }
}