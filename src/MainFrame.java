import cn.smy.dama2.*;
import cn.smy.dama2.Dama2Web.DecodeResult;
import cn.smy.dama2.Dama2Web.ReadBalanceResult;
import cn.smy.dama2.Dama2Web.ReadInfoResult;
import cn.smy.dama2.Dama2Web.RequestResult;

import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1325980466616825482L;
	private static Dama2Web dama2 = new Dama2Web(46111, "41c5a58de68ebe2bd23b67f61645e3a7", "test", "test");
	private int id;

	public MainFrame() throws HeadlessException {
		this.setLayout(new FlowLayout());
		
		//query balance
		JButton getBalanceButton = new JButton("queryBalance");
		getBalanceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ReadBalanceResult res = dama2.getBalance();
				String s;
				if (res.ret >= 0) {
					s = "balance=" + res.balance;
				} else {
					s = "failed: ret = " + res.ret + "; desc=" + res.desc;
				}
				JOptionPane.showMessageDialog(null, s);
			}
			
		});
		getContentPane().add(getBalanceButton);
		
		//read info
		JButton readInfoButton = new JButton("readInfo");
		readInfoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ReadInfoResult res = dama2.readInfo();
				String s;
				if (res.ret == 0) {
					s = "name=" + res.name + "; qq=" + res.qq + "; email="  + res.email + "; tel=" + res.tel;
				} else {
					s = "failed: ret = " + res.ret + "; desc=" + res.desc ;
				}
				JOptionPane.showMessageDialog(null, s);
			}
			
		});
		getContentPane().add(readInfoButton);
		
		//通过URL请求打码
		JButton decodeUrlButton = new JButton("decodeUrl");
		decodeUrlButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int type = 42;
				int timeout = 30;
				String url = "http://icode.renren.com/getcode.do?t=web_reg&rnd=1383107243557";
				DecodeResult res = dama2.decodeUrlAndGetResult(url, type, timeout);
				String s;
				if (res.ret >= 0) {
					id = res.ret;
					s = "success: result=" + res.result + "; id=" + res.ret + "; cookie=" + res.cookie;
				} else {
					s = "failed: ret = " + res.ret + "; desc=" + res.desc ;
				}
				JOptionPane.showMessageDialog(null, s);
			}
			
		});
		getContentPane().add(decodeUrlButton);

		//通过URL请求打码
		JButton decodeButton = new JButton("decode");
		decodeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int type = 200;
				int timeout = 30;
				JFileChooser fc = new JFileChooser();
				fc.showOpenDialog(null);
				File file = fc.getSelectedFile();
				if (file == null)
					return;
				
				FileInputStream fis;
				byte [] data = new byte[(int)file.length()];
				try {
					fis = new FileInputStream(file);
					fis.read(data);
					fis.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "open file failed");
					return;
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "read file failed");
					return;
				}
				
				DecodeResult res = dama2.decodeAndGetResult(type, timeout, data);
				String s;
				if (res.ret >= 0) {
					id = res.ret;
					s = "success: result=" + res.result + "; id=" + res.ret;
				} else {
					s = "failed: ret = " + res.ret + "; desc=" + res.desc ;
				}
				JOptionPane.showMessageDialog(null, s);
			}
			
		});
		getContentPane().add(decodeButton);

		//文本题
		JButton decodeTextButton = new JButton("decodeText");
		decodeTextButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int type = 106;
				int timeout = 30;
								
				DecodeResult res = dama2.decodeAndGetResult(type, timeout, "一加上五");
				String s;
				if (res.ret >= 0) {
					id = res.ret;
					s = "success: result=" + res.result + "; id=" + res.ret;
				} else {
					s = "failed: ret = " + res.ret + "; desc=" + res.desc ;
				}
				JOptionPane.showMessageDialog(null, s);
			}
			
		});
		getContentPane().add(decodeTextButton);
		
		//read info
		JButton reportErrorButton = new JButton("reportError");
		reportErrorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				RequestResult res = dama2.reportError(id);
				String s;
				if (res.ret == 0) {
					s = "report success(id=" + id + ")";
				} else {
					s = "failed: ret = " + res.ret + "; desc=" + res.desc ;
				}
				JOptionPane.showMessageDialog(null, s);
			}
			
		});
		getContentPane().add(reportErrorButton);

		this.pack();
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void main(String[] args) {
		new MainFrame();

	}

}
