package test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

/***
 * 
 * @ClassName: EbayMultiplyThreadCheck
 * @Description: TODO
 * @author zeze
 * @date 2017年2月16日 上午8:49:46
 *
 */
public class EbayMultiplyThreadCheck {

	private static int threadNum = 30;

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

	public static void main(String[] args) {
		nowDate = new Date();

		ExecutorService exec = Executors.newFixedThreadPool(threadNum);
		ArrayList<Future<Integer>> results = new ArrayList<Future<Integer>>();

		for (int i = 0; i < threadNum; i++) {
			String email = "asd" + i + "@qq.com";
			if (i == 0)
				email = "asd@qq.com";
			results.add(exec.submit(new CheckEbayAccount(email)));
		}

		boolean isDone = false;
		while (!isDone) {
			isDone = true;
			for (Future<Integer> future : results) {
				if (!future.isDone()) {
					isDone = false;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					break;
				}
			}
		}
		exec.shutdown();

		endDate = new Date();
		diff = endDate.getTime() - nowDate.getTime();
		min = diff % nd % nh / nm;
		sec = diff % nd % nh % nm / ns;
		ms = diff % nd % nh % nm % ns;
		System.out.println("总耗时:"+min + "分钟" + sec + "秒" + ms + "毫秒");

	}
}

class CheckEbayAccount implements Callable<Integer> {

	private String email;
	private static Logger logger = Logger.getLogger(CheckEbayAccount.class);
	private static Dama2Web dama2 = new Dama2Web(46111, "41c5a58de68ebe2bd23b67f61645e3a7", "test", "test");
	private static int id;

	public CheckEbayAccount(String email) {
		this.email = email;
	}

	@Override
	public Integer call() {

		System.out.println(Thread.currentThread().getName() + " 开始验证账号：" + email);
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
		form.getInputByName("input").setValueAttribute(email);
		HtmlButton button = (HtmlButton) form.getElementsByTagName("button").get(0);

		try {
			page = button.click();
		} catch (IOException e1) {
			logger.error(e1);
		}

		if (page.asText().indexOf("Select how you want to reset your password") != -1) {
			System.out.println(Thread.currentThread().getName() + " " + email + " 该账号是eBay账号!");
			return 1;
		} else if (page.asText().indexOf("Oops, that's not a match. Try again?") != -1) {
			System.out.println(Thread.currentThread().getName() + " " + email + " 该邮箱号不是ebay账号");
			return 0;
		}

		while (page.asText().indexOf("Security Measure") != -1) {

			Document doc = Jsoup.parse(page.asXml());
			Elements imgSrc = doc.getElementsByTag("iframe");
			String imgUrl = imgSrc.attr("src");
			System.out.println(Thread.currentThread().getName() + " " + "验证码图片链接：" + imgUrl);
			String code = getCode(imgUrl);

			// 提交验证码
			form = page.getForms().get(0);
			form.getInputByName("tokenText").setValueAttribute(code);
			HtmlSubmitInput input = (HtmlSubmitInput) form.getElementsByTagName("input").get(5);
			try {
				page = input.click();
			} catch (IOException e1) {
				System.out.println(Thread.currentThread().getName() + " " + e1);
			}

			while (page.asText().indexOf("Sorry") != -1) {
				System.out.println(Thread.currentThread().getName() + " 打码错误！重试");
				dama2.reportError(id);

				doc = Jsoup.parse(page.asXml());
				imgSrc = doc.getElementsByTag("iframe");
				imgUrl = imgSrc.attr("src");
				System.out.println(Thread.currentThread().getName() + " " + "验证码图片链接：" + imgUrl);
				code = getCode(imgUrl);

				// 提交验证码
				form = page.getForms().get(0);
				form.getInputByName("tokenText").setValueAttribute(code);
				input = (HtmlSubmitInput) form.getElementsByTagName("input").get(5);
				try {
					page = input.click();
				} catch (IOException e1) {
					logger.error(e1);
				}
			}

			// 再次提交邮箱
			form = page.getForms().get(1);
			form.getInputByName("input").setValueAttribute(email);
			button = (HtmlButton) form.getElementsByTagName("button").get(0);
			try {
				page = button.click();
			} catch (IOException e1) {
				logger.error(e1);
			}

			if (page.asText().indexOf("Security Measure") != -1) {// 如果还是验证码页面
				System.out.println(Thread.currentThread().getName() + " 提交还是验证码页面!");
				continue;
			}

			if (page.asText().indexOf("Oops, that's not a match. Try again?") != -1) {
				System.out.println(Thread.currentThread().getName() + " " + email + " 该邮箱号不是ebay账号");
				return 0;
			} else if (page.asText().indexOf("Select how you want to reset your password") != 1) {
				System.out.println(Thread.currentThread().getName() + " " + email + " 该账号是eBay账号!");
				return 1;
			} else {
				System.out.println(Thread.currentThread().getName() + " " + page.asText());
				return 2;
			}
		}
		System.out.println(Thread.currentThread().getName() + " " + page.asText());
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
			s = "[打码结果=" + res.result + "] [id=" + res.ret + "] " + balanceResult;
			System.out.println(Thread.currentThread().getName() + " " + s);
		} else {
			while (res.result == null) {
				s = "打码失败,重试: ret = " + res.ret + "; desc=" + res.desc;
				System.out.println(Thread.currentThread().getName() + " " + s);
				dama2.reportError(id);
				res = dama2.decodeUrlAndGetResult(imgUrl, type, timeout);
				if (res.ret >= 0) {
					id = res.ret;
					s = "[打码结果=" + res.result + "] [id=" + res.ret + "] " + balanceResult;
					System.out.println(Thread.currentThread().getName() + " " + s);
				}
			}
		}
		return res.result;
	}

}
