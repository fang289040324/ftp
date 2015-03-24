package com.navinfo.test.FtpTest;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

import net.sf.jftp.config.Settings;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionHandler;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.FtpConnection;

/**
 * Title. <br>
 * Description.
 * <p>
 * Copyright: Copyright (c) 2015年1月21日 上午11:10:03
 * <p>
 * Company: 北京四维图新科技股份有限公司
 * <p>
 * Author: fangshaowei@navinfo.com
 * <p>
 * Version: 1.0
 * <p>
 */
public class JftpTest implements ConnectionListener {

	private boolean isThere = false;

	public static long time = 0;

	private FTPModel ftpModel = null;

	private ConnectionHandler handler = new ConnectionHandler();

	private FtpConnection ftpcon = null;

	public JftpTest(FTPModel ftpModel) {
		this.ftpModel = ftpModel;
		// 登录FTP
		this.ftpLogin();
	}
	
	public static void main(String[] args) throws Exception{
		
		/*
		 * jdk7+的BUG
		 * 在安装有 IPv6 和 IPv4 的计算机上，会使用一种 IPv6 模拟的 IPv4，
		 * 而 windows 防火墙会把这种模拟的 IPv4 数据挡住。
		 * 所以要配置系统参数优先用IP4
		 */
		System.setProperty("java.net.preferIPv4Stack" , "true");
		
		FTPModel ftpm = new FTPModel();
		ftpm.setUsername("test");
		ftpm.setPassword("123456");
		ftpm.setPort(21);
		ftpm.setUrl("127.0.0.1");
		ftpm.setRemoteDir("/");
		JftpTest jftp = new JftpTest(ftpm);
		jftp.downloadToBinary("test.txt", new File("d:\\ftpDownload.txt"));
	}

	/**
	 * 连接并登录FTP服务器
	 * 
	 */
	public boolean ftpLogin() {
		ftpcon = new FtpConnection(this.ftpModel.getUrl());
		ftpcon.addConnectionListener(this);
		ftpcon.setConnectionHandler(handler);
		// 登录
		ftpcon.login(ftpModel.getUsername(), ftpModel.getPassword());
		while (!isThere) {
			try {
				Thread.sleep(10);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return isThere;
	}

	/**
	 * 关闭与FTP服务器的连接
	 * 
	 */
	public void close() {
		ftpcon.disconnect();
	}

	/**
	 * 获得FTP 服务器下所有的文件名列表
	 * 
	 * @param regex
	 * @return
	 */
	public String[] getListFiels() {
		ftpcon.exists("");
		Vector fileNameVector = ftpcon.currentFiles;
		String[] fileNames = new String[fileNameVector.size()];
		int i = 0;
		for (Iterator iter = fileNameVector.iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			fileNames[i] = name;
			i++;
		}
		return fileNames;
	}

	/**
	 * 将FTP服务器上的file下载为bype型数据
	 * 
	 * @param remoteFileName
	 *            文件名
	 * @return
	 */
	public byte[] downloadToBinary(String remoteFileName) {
		Settings.bufferSize = 16384;
		long current = System.currentTimeMillis();
		byte[] bytes = null;
		try {
			InputStream is = ftpcon.getDownloadInputStream(remoteFileName);
			ByteArrayOutputStream bais = new ByteArrayOutputStream();
			int bit = 0;
			while ((bit = is.read()) != -1) {
				bais.write(bit);
			}
			bytes = bais.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		time = (System.currentTimeMillis() - current);
		System.out.println("下载花费时间：" + time + "ms.");
		return bytes;
	}

	/**
	 * 下载FTP服务器文件
	 * 
	 * @param remoteFileName
	 *            FTP服务器文件名
	 * @param localFile
	 *            本地文件名
	 * @return
	 * @throws Exception
	 */
	public void downloadToBinary(String remoteFileName, File localFile)
			throws Exception {
		Settings.bufferSize = 16384;
		byte[] bytes = null;
		InputStream is = ftpcon.getDownloadInputStream(remoteFileName);
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		int bit = 0;
		while ((bit = is.read()) != -1) {
			bais.write(bit);
		}
		bytes = bais.toByteArray();
		CopyByteDataToLoacal(localFile, bytes);

	}

	/**
	 * 将二进制文件下载到本地
	 * 
	 * @param localFile
	 *            目标文件名
	 * @param fileDatas
	 *            文件数据
	 * @throws IOException
	 */
	public void CopyByteDataToLoacal(File localFile, byte[] fileDatas)
			throws IOException {
		FileOutputStream fileOutStream = null;
		BufferedOutputStream bufferOutStream = null;
		try {
			if (localFile.exists()) {
				localFile.delete();
			}
			fileOutStream = new FileOutputStream(localFile);
			bufferOutStream = new BufferedOutputStream(fileOutStream);
			bufferOutStream.write(fileDatas);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				if (null != bufferOutStream) {
					bufferOutStream.flush();
					bufferOutStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (null != fileOutStream) {
					try {
						fileOutStream.flush();
						fileOutStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void connectionFailed(BasicConnection arg0, String arg1) {
	}

	public void connectionInitialized(BasicConnection arg0) {
		isThere = true;
	}

	public void updateRemoteDirectory(BasicConnection arg0) {
	}

	public void updateProgress(String arg0, String arg1, long arg2) {
	}

	public void actionFinished(BasicConnection arg0) {
	}

}

/**
 * FTP实体对象
 * 
 * @author 张明学
 * 
 */
class FTPModel {

	private String ftpId;
	private String username;
	private String password;
	private String url;
	private int port;
	private String remoteDir;


	public String getFtpId() {
		return ftpId;
	}

	public void setFtpId(String ftpId) {
		this.ftpId = ftpId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getRemoteDir() {
		return remoteDir;
	}

	public void setRemoteDir(String remoteDir) {
		this.remoteDir = remoteDir;
	}

	public FTPModel() {

	}

	public FTPModel(String username, String password, String url, int port,
			String remoteDir) {
		this.username = username;
		this.password = password;
		this.url = url;
		this.port = port;
		this.remoteDir = remoteDir;
	}
}
