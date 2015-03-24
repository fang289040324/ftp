package com.navinfo.test.FtpTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

import sun.net.TelnetInputStream;
import sun.net.TelnetOutputStream;
import sun.net.ftp.FtpClient;
import sun.net.ftp.FtpProtocolException;

/**
 * Title. <br>
 * Description.
 * <p>
 * Copyright: Copyright (c) 2015年1月21日 下午5:04:34
 * <p>
 * Company: 北京四维图新科技股份有限公司
 * <p>
 * Author: fangshaowei@navinfo.com
 * <p>
 * Version: 1.0
 * <p>
 */
public class SunFtpTest {
	/**
	 * 本地文件名
	 */
	private String localfilename;
	/**
	 * 远程文件名
	 */
	private String remotefilename;
	/**
	 * FTP客户端
	 */
	private FtpClient ftpClient;

	/**
	 * 服务器连接
	 * 
	 * @param ip
	 *            服务器IP
	 * @param port
	 *            服务器端口
	 * @param user
	 *            用户名
	 * @param password
	 *            密码
	 * @param path
	 *            服务器路径
	 * @throws FtpProtocolException
	 * 
	 */
	public void connectServer(String ip, int port, String user,
			String password, String path) throws FtpProtocolException {
		try {
			/* ******连接服务器的两种方法****** */
			// 第一种方法

			ftpClient = FtpClient.create();
			ftpClient.connect(new InetSocketAddress(ip, port));

			// 第二种方法
			// ftpClient = FtpClient.create(ip);
			ftpClient.login(user, null, password);
			// 设置成2进制传输
			ftpClient.setBinaryType();
			System.out.println("login success!");

			if (path != null && path.length() != 0) {
				// 把远程系统上的目录切换到参数path所指定的目录
				ftpClient.changeDirectory(path);
			}
			ftpClient.setBinaryType();
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}

	}

	/**
	 * 关闭连接
	 */

	public void closeConnect() {
		try {
			ftpClient.close();
			System.out.println("disconnect success");
		} catch (IOException ex) {
			System.out.println("not disconnect");
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 
	 * 上传文件
	 * 
	 * @param localFile
	 *            本地文件
	 * @param remoteFile
	 *            远程文件
	 * @throws FtpProtocolException
	 */
	public void upload(String localFile, String remoteFile)
			throws FtpProtocolException {
		this.localfilename = localFile;
		this.remotefilename = remoteFile;
		TelnetOutputStream os = null;
		FileInputStream is = null;
		try {
			// 将远程文件加入输出流中
			os = (TelnetOutputStream) ftpClient.putFileStream(
					this.remotefilename, true);

			// 获取本地文件的输入流
			File file_in = new File(this.localfilename);
			is = new FileInputStream(file_in);

			// 创建一个缓冲区
			byte[] bytes = new byte[1024];
			int c;
			while ((c = is.read(bytes)) != -1) {
				os.write(bytes, 0, c);
			}
			System.out.println("upload success");
		} catch (IOException ex) {
			System.out.println("not upload");
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (os != null) {
						os.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * 下载文件
	 * 
	 * @param remoteFile
	 *            远程文件路径(服务器端)
	 * @param localFile
	 *            本地文件路径(客户端)
	 * @throws FtpProtocolException
	 * 
	 */
	public void download(String remoteFile, String localFile)
			throws FtpProtocolException {
		TelnetInputStream is = null;
		FileOutputStream os = null;
		try {

			// 获取远程机器上的文件filename，借助TelnetInputStream把该文件传送到本地。
			is = (TelnetInputStream) ftpClient.getFileStream(remoteFile);
			File file_in = new File(localFile);
			if (!file_in.exists()) {
				file_in.createNewFile();
			}
			os = new FileOutputStream(file_in);

			byte[] bytes = new byte[1024];
			int c;
			while ((c = is.read(bytes)) != -1) {
				os.write(bytes, 0, c);
			}
			System.out.println("download success");
		} catch (IOException ex) {
			System.out.println("not download");
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (os != null) {
						os.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 函数入口
	 * 
	 * @param agrs
	 */
	public static void main(String agrs[]) {
		
		/*
		 * jdk7+的BUG
		 * 在安装有 IPv6 和 IPv4 的计算机上，会使用一种 IPv6 模拟的 IPv4，
		 * 而 windows 防火墙会把这种模拟的 IPv4 数据挡住。
		 * 所以要配置系统参数优先用IP4
		 */
		System.setProperty("java.net.preferIPv4Stack" , "true");

		String filepath[] = { "/test.txt" };
		String localfilepath[] = { "d:\\tmp\\1.txt" };
		SunFtpTest ftp = new SunFtpTest();
		/*
		 * 使用默认的端口号、用户名、密码以及根目录连接FTP服务器
		 */
		try {
			ftp.connectServer("192.168.4.226", 21, "test", "123456", "/");
		} catch (FtpProtocolException e) {
			e.printStackTrace();
		}
		// 下载
		for (int i = 0; i < filepath.length; i++) {
			try {
				ftp.download(filepath[i], localfilepath[i]);
			} catch (FtpProtocolException e) {
				e.printStackTrace();
			}
		}
		// String localfile = "E:\\contact.txt";
		// String remotefile = "/temp/records.txt";
		// // 上传
		// try {
		// ftp.upload(localfile, remotefile);
		// } catch (FtpProtocolException e) {
		// e.printStackTrace();
		// }
		ftp.closeConnect();
	}

}
