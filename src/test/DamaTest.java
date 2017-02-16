/**   
* @Title: DamaTest.java 
* @Package test 
* @Description: TODO(用一句话描述该文件做什么) 
* @author A18ccms A18ccms_gmail_com   
* @date 2017年2月15日 下午2:23:53 
* @version V1.0   
*/
package test;

import java.util.Date;

import cn.smy.dama2.Dama2Web;
import cn.smy.dama2.Dama2Web.DecodeResult;
import cn.smy.dama2.Dama2Web.ReadBalanceResult;
import cn.smy.dama2.Dama2Web.ReadInfoResult;

/**
 * @ClassName: DamaTest
 * @Description: TODO
 * @author zeze
 * @date 2017年2月15日 下午2:23:53
 * 
 */
public class DamaTest {

	private static final long serialVersionUID = 1325980466616825482L;
	private static Dama2Web dama2 = new Dama2Web(46111, "41c5a58de68ebe2bd23b67f61645e3a7", "test", "test");
	private static int id;

	public static void main(String[] agrs) {

		int type = 6;
		int timeout = 30;
		String url = "https://signin.ebay.com//ws/eBayISAPI.dll?LoadBotImage&siteid=0&co_brandId=0&tokenstring=WXEysA8AAAA%3D&t=1487120719419";
		ReadInfoResult readInfoResult = dama2.readInfo();
		System.out.println(readInfoResult);

		long nd = 1000 * 24 * 60 * 60;
		long nh = 1000 * 60 * 60;
		long nm = 1000 * 60;
		long ns = 1000;
		// 获得两个时间的毫秒时间差异
		Date nowDate = new Date();
		Date endDate = new Date();
		long diff = endDate.getTime() - nowDate.getTime();// getTime返回的是一个long型的毫秒数
		// 计算差多少天
		long day = diff / nd;
		// 计算差多少小时
		long hour = diff % nd / nh;
		// 计算差多少分钟
		long min = diff % nd % nh / nm;
		// 计算差多少秒//输出结果
		long sec = diff % nd % nh % nm / ns;
		// 计算多少毫秒
		long ms = diff % nd % nh % nm % ns;

		for (int i = 0; i < 2; i++) {

			System.out.println("开始运行:" + i);
			nowDate = new Date();

			ReadBalanceResult balanceResult = dama2.getBalance();
			System.out.println(balanceResult);

			DecodeResult res = dama2.decodeUrlAndGetResult(url, type, timeout);
			String s;
			if (res.ret >= 0) {
				id = res.ret;
				s = "success: result=" + res.result + "; id=" + res.ret;
				System.out.println(s);
			} else {
				s = "failed: ret = " + res.ret + "; desc=" + res.desc;
				System.err.println(s);
			}
			
			endDate = new Date();
			diff = endDate.getTime() - nowDate.getTime();// getTime返回的是一个long型的毫秒数
			// 计算差多少天
			day = diff / nd;
			// 计算差多少小时
			hour = diff % nd / nh;
			// 计算差多少分钟
			min = diff % nd % nh / nm;
			// 计算差多少秒//输出结果
			sec = diff % nd % nh % nm / ns;
			// 计算多少毫秒
			ms = diff % nd % nh % nm % ns;
			System.out.println(day + "天" + hour + "小时" + min + "分钟" + sec + "秒" + ms + "毫秒");
		}

		// dama2.reportError(id);
	}

}
