package com.navinfo.test.FtpTest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.commons.net.util.TrustManagerUtils;

/**
 * Title. <br>
 * Description.
 * <p>
 * Copyright: Copyright (c) 2015年1月21日 下午5:54:39
 * <p>
 * Company: 北京四维图新科技股份有限公司
 * <p>
 * Author: fangshaowei@navinfo.com
 * <p>
 * Version: 1.0
 * <p>
 */

public final class FTPClientExample {
	public static final String USAGE = "Usage: ftp [options] <hostname> <username> <password> [<remote file> [<local file>]]\n\nDefault behavior is to download a file and use ASCII transfer mode.\n\t-a - use local active mode (default is local passive)\n\t-A - anonymous login (omit username and password parameters)\n\t-b - use binary transfer mode\n\t-c cmd - issue arbitrary command (remote is used as a parameter if provided) \n\t-d - list directory details using MLSD (remote is used as the pathname if provided)\n\t-e - use EPSV with IPv4 (default false)\n\t-f - issue FEAT command (remote and local files are ignored)\n\t-h - list hidden files (applies to -l and -n only)\n\t-k secs - use keep-alive timer (setControlKeepAliveTimeout)\n\t-l - list files using LIST (remote is used as the pathname if provided)\n\t     Files are listed twice: first in raw mode, then as the formatted parsed data.\n\t-L - use lenient future dates (server dates may be up to 1 day into future)\n\t-n - list file names using NLST (remote is used as the pathname if provided)\n\t-p true|false|protocol[,true|false] - use FTPSClient with the specified protocol and/or isImplicit setting\n\t-s - store file on server (upload)\n\t-t - list file details using MLST (remote is used as the pathname if provided)\n\t-w msec - wait time for keep-alive reply (setControlKeepAliveReplyTimeout)\n\t-T  all|valid|none - use one of the built-in TrustManager implementations (none = JVM default)\n\t-PrH server[:port] - HTTP Proxy host and optional port[80] \n\t-PrU user - HTTP Proxy server username\n\t-PrP password - HTTP Proxy server password\n\t-# - add hash display during transfers\n";

	public static void main(String[] args) throws UnknownHostException {
		/*
		 * jdk7+的BUG
		 * 在安装有 IPv6 和 IPv4 的计算机上，会使用一种 IPv6 模拟的 IPv4，
		 * 而 windows 防火墙会把这种模拟的 IPv4 数据挡住。
		 * 所以要配置系统参数优先用IP4
		 */
		System.setProperty("java.net.preferIPv4Stack" , "true");
		
		boolean storeFile = false;
		boolean binaryTransfer = false;
		boolean error = false;
		boolean listFiles = false;
		boolean listNames = false;
		boolean hidden = false;
		boolean localActive = false;
		boolean useEpsvWithIPv4 = false;
		boolean feat = false;
		boolean printHash = false;
		boolean mlst = false;
		boolean mlsd = false;
		boolean lenient = false;
		long keepAliveTimeout = -1L;
		int controlKeepAliveReplyTimeout = -1;
		int minParams = 5;
		String protocol = null;
		String doCommand = null;
		String trustmgr = null;
		String proxyHost = null;
		int proxyPort = 80;
		String proxyUser = null;
		String proxyPassword = null;
		String username = null;
		String password = null;

		int base = 0;
		for (base = 0; base < args.length; base++) {
			if (args[base].equals("-s")) {
				storeFile = true;
			} else if (args[base].equals("-a")) {
				localActive = true;
			} else if (args[base].equals("-A")) {
				username = "anonymous";
				password = System.getProperty("user.name") + "@" + InetAddress.getLocalHost().getHostName();
			} else if (args[base].equals("-b")) {
				binaryTransfer = true;
			} else if (args[base].equals("-c")) {
				base++;
				doCommand = args[base];
				minParams = 3;
			} else if (args[base].equals("-d")) {
				mlsd = true;
				minParams = 3;
			} else if (args[base].equals("-e")) {
				useEpsvWithIPv4 = true;
			} else if (args[base].equals("-f")) {
				feat = true;
				minParams = 3;
			} else if (args[base].equals("-h")) {
				hidden = true;
			} else if (args[base].equals("-k")) {
				base++;
				keepAliveTimeout = Long.parseLong(args[base]);
			} else if (args[base].equals("-l")) {
				listFiles = true;
				minParams = 3;
			} else if (args[base].equals("-L")) {
				lenient = true;
			} else if (args[base].equals("-n")) {
				listNames = true;
				minParams = 3;
			} else if (args[base].equals("-p")) {
				base++;
				protocol = args[base];
			} else if (args[base].equals("-t")) {
				mlst = true;
				minParams = 3;
			} else if (args[base].equals("-w")) {
				base++;
				controlKeepAliveReplyTimeout = Integer.parseInt(args[base]);
			} else if (args[base].equals("-T")) {
				base++;
				trustmgr = args[base];
			} else if (args[base].equals("-PrH")) {
				base++;
				proxyHost = args[base];
				String[] parts = proxyHost.split(":");
				if (parts.length == 2) {
					proxyHost = parts[0];
					proxyPort = Integer.parseInt(parts[1]);
				}
			} else if (args[base].equals("-PrU")) {
				base++;
				proxyUser = args[base];
			} else if (args[base].equals("-PrP")) {
				base++;
				proxyPassword = args[base];
			} else {
				if (!args[base].equals("-#"))
					break;
				printHash = true;
			}

		}

		int remain = args.length - base;
		if (username != null) {
			minParams -= 2;
		}
		if (remain < minParams) {
			System.err.println("Usage: ftp [options] <hostname> <username> <password> [<remote file> [<local file>]]\n\nDefault behavior is to download a file and use ASCII transfer mode.\n\t-a - use local active mode (default is local passive)\n\t-A - anonymous login (omit username and password parameters)\n\t-b - use binary transfer mode\n\t-c cmd - issue arbitrary command (remote is used as a parameter if provided) \n\t-d - list directory details using MLSD (remote is used as the pathname if provided)\n\t-e - use EPSV with IPv4 (default false)\n\t-f - issue FEAT command (remote and local files are ignored)\n\t-h - list hidden files (applies to -l and -n only)\n\t-k secs - use keep-alive timer (setControlKeepAliveTimeout)\n\t-l - list files using LIST (remote is used as the pathname if provided)\n\t     Files are listed twice: first in raw mode, then as the formatted parsed data.\n\t-L - use lenient future dates (server dates may be up to 1 day into future)\n\t-n - list file names using NLST (remote is used as the pathname if provided)\n\t-p true|false|protocol[,true|false] - use FTPSClient with the specified protocol and/or isImplicit setting\n\t-s - store file on server (upload)\n\t-t - list file details using MLST (remote is used as the pathname if provided)\n\t-w msec - wait time for keep-alive reply (setControlKeepAliveReplyTimeout)\n\t-T  all|valid|none - use one of the built-in TrustManager implementations (none = JVM default)\n\t-PrH server[:port] - HTTP Proxy host and optional port[80] \n\t-PrU user - HTTP Proxy server username\n\t-PrP password - HTTP Proxy server password\n\t-# - add hash display during transfers\n");
			System.exit(1);
		}

		String server = args[(base++)];
		int port = 0;
		String[] parts = server.split(":");
		if (parts.length == 2) {
			server = parts[0];
			port = Integer.parseInt(parts[1]);
		}
		if (username == null) {
			username = args[(base++)];
			password = args[(base++)];
		}

		String remote = null;
		if (args.length - base > 0) {
			remote = args[(base++)];
		}

		String local = null;
		if (args.length - base > 0)
			local = args[(base++)];
		FTPClient ftp;
		// FTPClient ftp;
		if (protocol == null) {
			// FTPClient ftp;
			if (proxyHost != null) {
				System.out.println("Using HTTP proxy server: " + proxyHost);
				ftp = new FTPHTTPClient(proxyHost, proxyPort, proxyUser,
						proxyPassword);
			} else {
				ftp = new FTPClient();
			}
		} else {
			FTPSClient ftps;
			// FTPSClient ftps;
			if (protocol.equals("true")) {
				ftps = new FTPSClient(true);
			} else {
				// FTPSClient ftps;
				if (protocol.equals("false")) {
					ftps = new FTPSClient(false);
				} else {
					String[] prot = protocol.split(",");
					// FTPSClient ftps;
					if (prot.length == 1)
						ftps = new FTPSClient(protocol);
					else
						ftps = new FTPSClient(prot[0],
								Boolean.parseBoolean(prot[1]));
				}
			}
			ftp = ftps;
			if ("all".equals(trustmgr))
				ftps.setTrustManager(TrustManagerUtils
						.getAcceptAllTrustManager());
			else if ("valid".equals(trustmgr))
				ftps.setTrustManager(TrustManagerUtils
						.getValidateServerCertificateTrustManager());
			else if ("none".equals(trustmgr)) {
				ftps.setTrustManager(null);
			}
		}

		if (printHash) {
			ftp.setCopyStreamListener(createListener());
		}
		if (keepAliveTimeout >= 0L) {
			ftp.setControlKeepAliveTimeout(keepAliveTimeout);
		}
		if (controlKeepAliveReplyTimeout >= 0) {
			ftp.setControlKeepAliveReplyTimeout(controlKeepAliveReplyTimeout);
		}
		ftp.setListHiddenFiles(hidden);

		ftp.addProtocolCommandListener(new PrintCommandListener(
				new PrintWriter(System.out), true));
		try {
			if (port > 0)
				ftp.connect(server, port);
			else {
				ftp.connect(server);
			}
			ftp.setSoTimeout(3000);
			System.out.println("Connected to " + server + " on "
					+ (port > 0 ? port : ftp.getDefaultPort()));

			int reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				System.err.println("FTP server refused connection.");
				System.exit(1);
			}
		} catch (IOException e) {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException f) {
				}
			}

			System.err.println("Could not connect to server.");
			e.printStackTrace();
			System.exit(1);
		}

		try {
			if (!ftp.login(username, password)) {
				ftp.logout();
				error = true;

				if (ftp.isConnected()) {
					try {
						ftp.disconnect();
					} catch (IOException f) {
					}
				}
			} else {
				System.out.println("Remote system is " + ftp.getSystemType());

				if (binaryTransfer) {
					ftp.setFileType(2);
				} else {
					ftp.setFileType(0);
				}

				if (localActive)
					ftp.enterLocalActiveMode();
				else {
					ftp.enterLocalPassiveMode();
				}

				ftp.setUseEPSVwithIPv4(useEpsvWithIPv4);

				if (storeFile) {
					InputStream input = new FileInputStream(local);

					ftp.storeFile(remote, input);

					input.close();
				} else if (listFiles) {
					if (lenient) {
						FTPClientConfig config = new FTPClientConfig();
						config.setLenientFutureDates(true);
						ftp.configure(config);
					}

					for (FTPFile f : ftp.listFiles(remote)) {
						System.out.println(f.getRawListing());
						System.out.println(f.toFormattedString());
					}
				} else if (mlsd) {
					for (FTPFile f : ftp.mlistDir(remote)) {
						System.out.println(f.getRawListing());
						System.out.println(f.toFormattedString());
					}
				} else if (mlst) {
					FTPFile f = ftp.mlistFile(remote);
					if (f != null) {
						System.out.println(f.toFormattedString());
					}
				} else if (listNames) {
					for (String s : ftp.listNames(remote)) {
						System.out.println(s);
					}
				} else if (feat) {
					if (remote != null) {
						if (ftp.hasFeature(remote)) {
							System.out.println("Has feature: " + remote);
						} else if (FTPReply.isPositiveCompletion(ftp
								.getReplyCode()))
							System.out.println("FEAT " + remote
									+ " was not detected");
						else {
							System.out.println("Command failed: "
									+ ftp.getReplyString());
						}

						String[] features = ftp.featureValues(remote);
						if (features != null) {
							for (String f : features) {
								System.out.println("FEAT " + remote + "=" + f
										+ ".");
							}
						} else if (FTPReply.isPositiveCompletion(ftp
								.getReplyCode()))
							System.out.println("FEAT " + remote
									+ " is not present");
						else {
							System.out.println("Command failed: "
									+ ftp.getReplyString());
						}

					} else if (!ftp.features()) {
						System.out.println("Failed: " + ftp.getReplyString());
					}

				} else if (doCommand != null) {
					if (!ftp.doCommand(doCommand, remote)) {
						System.out.println("Failed: " + ftp.getReplyString());
					}

				} else {
					OutputStream output = new FileOutputStream(local);
					ftp.retrieveFile(remote, output);
					output.close();
				}

				ftp.noop();

				ftp.logout();
			}
		} catch (FTPConnectionClosedException f) {
			error = true;
			System.err.println("Server closed connection.");
			f.printStackTrace();
		} catch (IOException f) {
			error = true;
			f.printStackTrace();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException f) {
				}
			}

		}

		System.exit(error ? 1 : 0);
	}

	private static CopyStreamListener createListener() {
		return new CopyStreamListener() {

			private long megsTotal = 0L;

			public void bytesTransferred(CopyStreamEvent event) {
				bytesTransferred(event.getTotalBytesTransferred(),
						event.getBytesTransferred(), event.getStreamSize());
			}

			public void bytesTransferred(long totalBytesTransferred,
					int bytesTransferred, long streamSize) {
				long megs = totalBytesTransferred / 1000000L;
				for (long l = this.megsTotal; l < megs; l += 1L) {
					System.err.print("#");
				}
				this.megsTotal = megs;
			}

		};
	}
}
