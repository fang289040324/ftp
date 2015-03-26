package com.navinfo.test.FtpTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

public class FtpClient {
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
		
		FtpClient ftpClient = new FtpClient();
		
		ftpClient.setUrl("192.168.4.226");
//		ftpClient.setUrl("localhost");
		ftpClient.setPort(21);
		ftpClient.setUsername("test");
		ftpClient.setPassword("123456");
		ftpClient.setRemotePath("/333");
		
		try {
			ftpClient.open();
			
//			ftpClient.downloadBPFile("D:\\", "123.ISO"); // 下载文件
			
//			for(String name : ftpClient.getFTPFileList()){  // 显示ftp当前目录下的文件列表
//				System.out.println(name);
//			}
			
			ftpClient.downloadDir("D:\\"); // 下载文件夹
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ftpClient.disconnect();
		}
	}
	
	/**
	 * 连接ftp并登陆
	 * 
	 * @throws IOException
	 */
	public void open() throws IOException{
		connect();
		ftpLogin();
	}
	
	/**
	 * ftp连接
	 * 
	 * @throws IOException
	 */
	public void connect() throws IOException{
		
		/*
		 * jdk7+的BUG 在安装有 IPv6 和 IPv4 的计算机上，会使用一种 IPv6 模拟的 IPv4， 而 windows
		 * 防火墙会把这种模拟的 IPv4 数据挡住。 所以要配置系统参数优先用IP4
		 */
		System.setProperty("java.net.preferIPv4Stack", "true");

		ftp = new FTPClient();

		FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
		conf.setServerLanguageCode("zh");
		
		// ftp链接超时
		ftp.setDefaultTimeout(5000);
		
		// 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
		ftp.connect(url, port);

		System.out.println("ftp连接成功！！");
		
	}
	
	/**
	 * ftp登陆
	 * 
	 * @throws IOException
	 */
	public boolean ftpLogin() throws IOException {
		// 登录
		if(ftp.login(username, password)){
			System.out.println("用户：" + username + "登陆成功！！");
		}

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
	public void disconnect() {
		if (ftp.isConnected()) {
			try{
				if((null != username && !username.isEmpty()) && ftp.logout()){
					System.out.println("用户：" + username + "已退出！！");
				}
			}catch(IOException e){
				e.printStackTrace();
				System.out.println("用户：" + username + "退出失败！！");
			} finally {
				try {
					ftp.disconnect();
					System.out.println("ftp连接关闭！！");
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("ftp连接关闭失败！！");
				}
			}
		}
	}

	/**
	 * Description: 从FTP服务器下载文件
	 * 
	 * @return
	 * @throws IOException 
	 */
	private boolean downloadFile(String remotePath, FTPFile ftpFile) throws IOException {
		boolean result = false;
		OutputStream out = null;
		
		File localDir = new File(localPath + "/" + remotePath);
		if(!localDir.exists())
			localDir.mkdirs();
		final File localFile = new File(localDir, "/" + ftpFile.getName());
		
		final long lRemoteSize = ftpFile.getSize(); 
		if (localFile.exists()) {
            if (localFile.length() >= lRemoteSize) { 
                System.out.println(localFile.getName() + "文件已经下载完毕");  
			} else {
				out = new FileOutputStream(localFile, true);  
//				System.out.println(localFile.getName() + "文件已下载: " + new DecimalFormat("#.##").format(localFile.length() * 100.0 / lRemoteSize) + "%");  
				
				showDownloadRate(localFile, lRemoteSize);
				
				ftp.setRestartOffset(localFile.length());
				result = ftp.retrieveFile(ftpFile.getName(), out);  
			}
        } else {
        	showDownloadRate(localFile, lRemoteSize);
            out = new FileOutputStream(localFile); 
            result = ftp.retrieveFile(ftpFile.getName(), out);  
        } 
		
		if(out != null){
			out.close();
		}

		return result;
	}

	/**
	 * Description: 获取当前要下载的ftp文件
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
	private boolean downloadBPFile() throws IOException {
		boolean result = false;  
		OutputStream out = null;
		ftp.enterLocalPassiveMode();  
		ftp.setFileType(FTP.BINARY_FILE_TYPE);  
		
		FTPFile file = getFile();
		
//      File f = new File(localPath + "/" + file.getName()); 
		File localDir = new File(localPath + "/" + remotePath);
		if(!localDir.exists())
			localDir.mkdirs();
		final File f = new File(localDir, "/" + file.getName());
        
        final long lRemoteSize = file.getSize();  
        if (f.exists()) {
            if (f.length() >= lRemoteSize) { 
                System.out.println(fileName + "文件已经下载完毕");  
			} else {
				out = new FileOutputStream(f, true);  
//				System.out.println("文件已下载: " + new DecimalFormat("#.##").format(f.length() * 100.0 / lRemoteSize) + "%");  
				
				showDownloadRate(f, lRemoteSize);
				
				ftp.setRestartOffset(f.length());
				result = ftp.retrieveFile(file.getName(), out);  
			}
        } else {
        	showDownloadRate(f, lRemoteSize);
            out = new FileOutputStream(f); 
            result = ftp.retrieveFile(file.getName(), out);  
        } 
        
        if(null != out){
        	out.close();  
        }
        return result;  
	}
	
	/**
	 * Description: 从FTP服务器下载文件(断点)
	 * 
	 * @param remotePath 远程ftp目录
	 * @param localPath 本地保存目录
	 * @param fileName 要下载的文件名称
	 * @return
	 * @throws IOException
	 */
	public boolean downloadBPFile(String localPath, String fileName) throws IOException {
		this.setFileName(fileName);
		this.setLocalPath(localPath);
		return downloadBPFile();
	}
	
	/**
	 * Description：下载ftp文件夹
	 * 
	 * @return
	 * @throws IOException 
	 */
	private boolean downloadDir() throws IOException{
		
		File file = new File(remotePath);
		if(!file.exists()){
			file.mkdirs();
		}
		
		FTPFile[] ftpFiles = ftp.listFiles(".");
		
		downloadDirORFile(remotePath, ftpFiles);
		
		return true;
	}

	/**
	 * @param ftpFiles
	 * @throws IOException 
	 */
	private void downloadDirORFile(String remotePath, FTPFile[] ftpFiles) throws IOException {
		for(FTPFile ftpFile : ftpFiles){
			ftp.changeWorkingDirectory(new String(remotePath.getBytes("utf-8"), "iso-8859-1"));
			if(ftpFile.isDirectory()){
				downloadDirORFile((remotePath + "/" + ftpFile.getName()), ftp.listFiles(ftpFile.getName()));
			}else if(ftpFile.isFile()){
				downloadFile(remotePath, ftpFile);
			}
			
		}
	}
	
	/**
	 * Description：下载ftp文件夹
	 * 
	 * @param remotePath 远程ftp目录
	 * @param localPath 本地保存目录
	 * @return
	 * @throws IOException
	 */
	public boolean downloadDir(String localPath) throws IOException{
		
		this.setLocalPath(localPath);
		return downloadDir();
		
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
			nameList.add(file.getRawListing());
		}
		
		return nameList;
	}
	
	/**
	 * Description：获取ftp当前任务进度
	 * 
	 * @return
	 */
	public void showDownloadRate(final File f, final long lRemoteSize){
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				
				while(true){
					double p = f.length() * 100.0 / lRemoteSize;
					if(p < 100){
						System.out.println("文件已下载: " + new DecimalFormat("#.##").format(p) + "%");  
					}else{
						break;
					}
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println("文件已下载: 100%"); 
				
			}
			
		}).start();
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
