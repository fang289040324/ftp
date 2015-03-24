package com.navinfo.test.FtpTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

public class FtpTest {
	/**
	 * FTP服务器客户端
	 */
	private FTPClient ftp = null;
	/**
	 * FTP服务器hostname
	 */
	private String url = null;
	/**
	 * FTP服务器端口
	 */
	private int port = 0;
	/**
	 * FTP登录账号
	 */
	private String username = null;
	/**
	 * FTP登录密码
	 */
	private String password = null;
	/**
	 * FTP服务器上的相对路径
	 */
	private String remotePath = null;
	/**
	 * 要下载的文件名
	 */
	private String fileName = null;
	/**
	 * 下载后保存到本地的路径
	 */
	private String localPath = null;

	public static void main(String[] args) {
		// downloadFile("192.168.4.226", 21, "test", "123456", "/",
		// "test01.txt", "D:\\");
		FtpTest ftp = new FtpTest();
		
		ftp.setUrl("localhost");
		ftp.setPort(21);
		ftp.setUsername("test");
		ftp.setPassword("123456");
		ftp.setRemotePath("/");
		ftp.setFileName("test.txt");
		ftp.setLocalPath("D:\\");
		
		try {
			ftp.ftpLogin();
//			ftp.downloadFile();
//			ftp.downloadBPFile();
			
			for(String name : ftp.getFTPFileList()){
				System.out.println(name);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ftp.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean ftpLogin() throws SocketException, IOException {
		/*
		 * jdk7+的BUG 在安装有 IPv6 和 IPv4 的计算机上，会使用一种 IPv6 模拟的 IPv4， 而 windows
		 * 防火墙会把这种模拟的 IPv4 数据挡住。 所以要配置系统参数优先用IP4
		 */
		System.setProperty("java.net.preferIPv4Stack", "true");

		ftp = new FTPClient();

		FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
		conf.setServerLanguageCode("zh");
		
		// 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
		ftp.connect(url, port);

		System.out.println("ftp连接成功！！");

		// 登录
		ftp.login(username, password);

		System.out.println("用户名：" + username + "登陆成功！！");

		// 设置被动模式
		// ftp.enterLocalPassiveMode();
		// ftp.sendCommand("PASV");

		ftp.setBufferSize(1024);

		// 设置文件类型（二进制）
		ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

		ftp.setControlEncoding("utf-8");

		ftp.setSoTimeout(3000);

		if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
			disconnect();
			return false;
		}

		// 转移到FTP服务器目录
		return ftp.changeWorkingDirectory(new String(remotePath.getBytes("utf-8"), "iso-8859-1"));

//		ftp.deleteFile(fileName);
	}

	/**
	 * 关闭ftp连接
	 * 
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		if (ftp.isConnected()) {
			ftp.logout();
			ftp.disconnect();
		}
	}

	/**
	 * Description: 从FTP服务器下载文件，支持下载文件夹
	 * 
	 * @return
	 * @throws IOException 
	 */
	public boolean downloadFile() throws IOException {
		boolean result = false;
		FTPFile fs = getFile();

		File localFile = new File(localPath + "/" + fs.getName());
		OutputStream out = new FileOutputStream(localFile);
		result = ftp.retrieveFile(fs.getName(), out);
		out.close();

		return result;
	}

	/**
	 * @return
	 * @throws IOException 
	 */
	private FTPFile getFile() throws IOException {
		FTPFile[] fs = ftp.listFiles(".", new FTPFileFilter() {
			@Override
			public boolean accept(FTPFile file) {
				return file.getName().equals(fileName);
			}
		});
		return fs[0];
	}

	/**
	 * Description: 从FTP服务器下载文件(断点)
	 * 
	 * @return
	 * @throws IOException 
	 */
	public boolean downloadBPFile() throws IOException {
		boolean result = false;  
		OutputStream out = null;
//		ftp.enterLocalPassiveMode();  
//		ftp.setFileType(FTP.BINARY_FILE_TYPE);  
		
        File f = new File(localPath + "/" + fileName); 
        
        FTPFile files = getFile();
        
        long lRemoteSize = files.getSize();  
        if (f.exists()) {
            out = new FileOutputStream(f, true);  
            if (f.length() >= lRemoteSize) { 
                System.out.println(fileName + "文件已经下载完毕");  
			} else {
				System.out.println("文件已下载: " + new DecimalFormat("#.##").format(f.length() * 1.0 / lRemoteSize) + "%");  
				ftp.setRestartOffset(f.length());
				result = ftp.retrieveFile(fileName, out);  
			}
        } else {
            out = new FileOutputStream(f); 
            result = ftp.retrieveFile(fileName, out);  
        } 
        
        out.close();  
        return result;  
	}

	/**
	 * Description：获取ftp当前路径的文件列表
	 * 
	 * @return
	 * @throws IOException 
	 */
	public List<String> getFTPFileList() throws IOException {
		
		FTPFile[] files = ftp.listFiles();
		
		List<String> nameList = new ArrayList<String>();
		
		for(FTPFile file : files){
			nameList.add(file.getName());
		}
		
		return nameList;
	}
	
	/**
	 * Description：获取ftp当前任务进度
	 * 
	 * @return
	 */
	public int getProgressRate(){
		return 0;
	}
	
	

	public FTPClient getFtp() {
		return ftp;
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

	public String getRemotePath() {
		return remotePath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

}
