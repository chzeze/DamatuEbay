package test;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

import cn.smy.dama2.Dama2Web;
import cn.smy.dama2.Dama2Web.DecodeResult;
import cn.smy.dama2.Dama2Web.ReadBalanceResult;

/**   
* @Title: main.java 
* @Package  
* @Description: TODO(用一句话描述该文件做什么) 
* @author A18ccms A18ccms_gmail_com   
* @date 2017年2月15日 下午3:42:00 
* @version V1.0   
*/

/**
 * @ClassName: main
 * @Description: TODO
 * @author zeze
 * @date 2017年2月15日 下午3:42:00
 * 
 */
public class EbaySingleThreadCheck {
	private static Logger logger = Logger.getLogger(EbaySingleThreadCheck.class);
	private static final long serialVersionUID = 1325980466616825482L;
	private static Dama2Web dama2 = new Dama2Web(46111, "41c5a58de68ebe2bd23b67f61645e3a7", "test", "test");
	private static int id;

	private static long nd = 1000 * 24 * 60 * 60;
	private static long nh = 1000 * 60 * 60;
	private static long nm = 1000 * 60;
	private static long ns = 1000;
	private static Date nowDate;
	private static Date endDate;
	private static long diff;
	private static long min;
	private static long sec;
	private static long ms;

	public static void main(String[] agrs) {
		String emailAccount = "asd@qq.com";


		for (int i = 0; i < 10; i++) {
			nowDate = new Date();

			emailAccount = "asd" + i + "@qq.com";
			if(i==0)
				emailAccount="asd@qq.com";
			
			int statusCode=checkEbayAccount(emailAccount);
			if(statusCode==0){
				System.out.println(emailAccount + " 该邮箱号不是ebay账号");
			}else if(statusCode==1){
				System.out.println(emailAccount + " 该账号是eBay账号!");
			}else if(statusCode==101){
				System.out.println("打码错误!");
				statusCode=checkEbayAccount(emailAccount);
				while(statusCode==101){
					statusCode=checkEbayAccount(emailAccount);
				}
			}else{
				System.out.println(statusCode);
			}

			endDate = new Date();
			diff = endDate.getTime() - nowDate.getTime();
			min = diff % nd % nh / nm;
			sec = diff % nd % nh % nm / ns;
			ms = diff % nd % nh % nm % ns;
			System.out.println(min + "分钟" + sec + "秒" + ms + "毫秒");
		}

	}

	// 验证邮箱是否为eBay账号
	public static int checkEbayAccount(String emailAccount) {
		System.out.println("开始验证账号：" + emailAccount);
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
		HtmlPage page = null;
		try {
			page = webClient.getPage("http://fyp.ebay.com/");
		} catch (FailingHttpStatusCodeException e) {
			logger.error(e);
		} catch (MalformedURLException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		HtmlForm form = page.getForms().get(1);
		form.getInputByName("input").setValueAttribute(emailAccount);
		HtmlButton button = (HtmlButton) form.getElementsByTagName("button").get(0);

		try {
			page = button.click();
		} catch (IOException e1) {
			logger.error(e1);
		}
		if (page.asText().indexOf("Select how you want to reset your password") != -1) {
//			System.out.println(emailAccount + " 该账号是eBay账号!");
			return 1;
		}

		while (page.asText().indexOf("Security Measure") != -1) {

			Document doc = Jsoup.parse(page.asXml());
			Elements imgSrc = doc.getElementsByTag("iframe");
			String imgUrl = imgSrc.attr("src");
			System.out.println("验证码图片链接：" + imgUrl);
			String code = getCode(imgUrl);

			// 提交验证码
			form = page.getForms().get(0);
			form.getInputByName("tokenText").setValueAttribute(code);
			HtmlSubmitInput input = (HtmlSubmitInput) form.getElementsByTagName("input").get(5);
			try {
				page = input.click();
			} catch (IOException e1) {
				logger.error(e1);
			}

			if (page.asText().indexOf("the verification code you entered doesn't match against the image") != -1) {
//				System.out.println("打码错误！");
				dama2.reportError(id);
				return 101;
			}

			// 再次提交邮箱
			form = page.getForms().get(1);
			form.getInputByName("input").setValueAttribute(emailAccount);
			button = (HtmlButton) form.getElementsByTagName("button").get(0);
			try {
				page = button.click();
			} catch (IOException e1) {
				logger.error(e1);
			}

			if (page.asText().indexOf("Security Measure") != -1){// 如果还是验证码页面
				System.out.println("提交还是验证码页面!");
				continue;
			}

			if (page.asText().indexOf("Oops, that's not a match. Try again?") != -1) {
//				System.out.println(emailAccount + " 该邮箱号不是ebay账号");
				return 0;
			} else if (page.asText().indexOf("Select how you want to reset your password") != 1) {
//				System.out.println(emailAccount + " 该账号是eBay账号!");
				return 1;
			} else {
				System.out.println(page.asText());
				return 2;
			}
		}
		System.out.println(page.asText());
		return 3;
	}

	// 打码兔获取验证码
	public static String getCode(String imgUrl) {
		// 打码兔
		int type = 6;
		int timeout = 30;
		ReadBalanceResult balanceResult = dama2.getBalance();
		// System.out.println(balanceResult);
		DecodeResult res = dama2.decodeUrlAndGetResult(imgUrl, type, timeout);
		String s;
		if (res.ret >= 0) {
			id = res.ret;
			s = "success: result=" + res.result + "; id=" + res.ret;
			System.out.println(s);
		} else {
			s = "failed: ret = " + res.ret + "; desc=" + res.desc;
			System.err.println(s);
		}
		return res.result;
	}

}
