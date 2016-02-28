/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Bertrand Martel
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package speedtest;

import android.util.Log;

import com.netvisiontel.speedtest.Interface.ISpeedTestListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

import fr.bmartel.protocol.http.HttpFrame;
import fr.bmartel.protocol.http.states.HttpStates;

/**
 * Client socket main implementation
 * 
 * Two modes upload and download
 * 
 * upload will write a file to a specific host with given uri. The file is
 * randomly generated with a given size
 * 
 * download will retrieve a content from a specific host with given uri.
 * 
 * For both mode, transfer rate is calculated independently from socket initial
 * connection
 * 
 * @author Bertrand Martel
 *
 */
public class SpeedTestSocket {

	private static final String TAG = "SpeedTestSocket";
	private int READ_BUFFER_SIZE = 65535;

	/**
	 * socket server hostname
	 */
	private String hostname = "";

	/**
	 * socket server port
	 */
	private int port = 0;

	/**
	 * socket object
	 */
	private Socket socket = null;

	/**
	 * thread used to read http inputstream data
	 */
	private Thread readingThread = null;

	/** define if reading thread is currently running */
	private volatile boolean isReading = false;

	/** speed test listener list */
	private List<ISpeedTestListener> speedTestListenerList = new ArrayList<ISpeedTestListener>();

	/** size of file to upload */
	private int uploadFileSize = 0;

	/** start time triggered in millis */
	private long timeStart = 0;

	/** end time triggered in millis */
	private long timeEnd = 0;

	/**
	 * Build Client socket
	 * 
	 * @param hostname
	 * @param port
	 */
	public SpeedTestSocket() {
	}

	/**
	 * Add a speed test listener to list
	 * 
	 * @param listener
	 */
	public void addSpeedTestListener(ISpeedTestListener listener) {
		speedTestListenerList.add(listener);
	}

	/**
	 * Relive a speed listener from list
	 * 
	 * @param listener
	 */
	public void removeSpeedTestListener(ISpeedTestListener listener) {
		speedTestListenerList.remove(listener);
	}

	/**
	 * Create and connect socket
	 * 
	 * @return
	 * @throws IOException
	 */
	public void connectAndExecuteTask(TimerTask task, final boolean isDownload) {

		// close socket before recreating it
		if (socket != null) {
			closeSocket();
		}
		try {
			/* create a basic socket connection */
			socket = new Socket();
			socket.setTcpNoDelay(false);
			socket.setSoTimeout(10*1000);
			/* establish socket parameters */
			socket.setReuseAddress(true);			

			socket.setKeepAlive(true);

			socket.connect(new InetSocketAddress(hostname, port));

			if (readingThread != null) {
				isReading = false;
				readingThread.join();
			}

			isReading = true;
			readingThread = new Thread(new Runnable() {

				@Override
				public void run() {

					boolean isSocketError = false;

					if (isDownload) {
						byte[] buffer = new byte[READ_BUFFER_SIZE];
						int read = 0;
						long totalPackets = 0;

						try {

							HttpFrame httpFrame = new HttpFrame();

							timeStart = System.currentTimeMillis();							

							HttpStates errorCode = httpFrame.decodeFrame(socket.getInputStream());
							if (errorCode != HttpStates.HTTP_FRAME_OK) {
								System.err.println("Error while parsing http frame");
								for (int i = 0; i < speedTestListenerList.size(); i++) {
									speedTestListenerList.get(i).onDownloadError(SpeedTestError.SPEED_TEST_ERROR_INVALID_HTTP_RESPONSE,
											"Http frame is not valid");
								}
							}

							HttpStates headerError = httpFrame.parseHeader(socket.getInputStream());
							if (headerError != HttpStates.HTTP_FRAME_OK) {
								System.err.println("Error while parsing http headers");
								for (int i = 0; i < speedTestListenerList.size(); i++) {
									speedTestListenerList.get(i).onDownloadError(SpeedTestError.SPEED_TEST_ERROR_INVALID_HTTP_RESPONSE,
											"Http headers are not valid");
								}
							}
							if (httpFrame.getContentLength() < 0) {
								System.err.println("Error content length is inconsistent");
								for (int i = 0; i < speedTestListenerList.size(); i++) {
									speedTestListenerList.get(i).onDownloadError(SpeedTestError.SPEED_TEST_ERROR_INVALID_HTTP_RESPONSE,
											"Http content length is inconsistent");
								}
							}

							int frameLength = httpFrame.getContentLength();
							Log.d(TAG, "giang dbg frameLength: " + frameLength);

							int step = 0;
							for (int i = 0; i < speedTestListenerList.size(); i++) {
								speedTestListenerList.get(i).onDownloadProgress(0);
							}
							while ((read = socket.getInputStream().read(buffer)) != -1) {
								/*long ti_now = System.currentTimeMillis();
								float fSpeed = (read * 8) / ((ti_now - ti_start) / 1000f);
								ti_start = ti_now;
								for (int i = 0; i < speedTestListenerList.size(); i++) {
									speedTestListenerList.get(i).onDownloadProgress(fSpeed, step);
								}*/								
								totalPackets += read;
								int new_step = (int)(totalPackets * 100 / frameLength);
								if(new_step > step) {
									step = new_step;
									for (int i = 0; i < speedTestListenerList.size(); i++) {
										speedTestListenerList.get(i).onDownloadProgress(step);
									}
									
								}
								
								if (totalPackets == frameLength) {
									break;
								}
							}
							timeEnd = System.currentTimeMillis();

							float transferRate_bps = (frameLength * 8) / ((timeEnd - timeStart) / 1000f);
							float transferRate_Bps = frameLength / ((timeEnd - timeStart) / 1000f);

							for (int i = 0; i < speedTestListenerList.size(); i++) {
								speedTestListenerList.get(i).onDownloadPacketsReceived(frameLength, transferRate_bps, transferRate_Bps);
							}

						} catch (IOException e) {
							e.printStackTrace();
							isSocketError = true;
						} catch (InterruptedException e) {
							e.printStackTrace();
							isSocketError = true;
						}

						if (isSocketError) {
							for (int i = 0; i < speedTestListenerList.size(); i++) {
								speedTestListenerList.get(i).onDownloadError(SpeedTestError.SPEED_TEST_ERROR_SOCKET_ERROR, "Socket error occured");
							}
						}

						closeSocket();

					} else {
						while (isReading) {
							try {
								HttpFrame frame = new HttpFrame();

								HttpStates httpStates = frame.parseHttp(socket.getInputStream());
								Log.d(TAG, "giang dbg httpStates: " + httpStates.name());
								Log.d(TAG, "giang dbg getStatusCode: " + frame.getStatusCode());
								Log.d(TAG, "giang dbg frame.getReasonPhrase(): " + frame.getReasonPhrase());
								if (httpStates == HttpStates.HTTP_FRAME_OK) {
									if (frame.getStatusCode() == 200 && frame.getReasonPhrase().toLowerCase().equals("ok")) {

										timeEnd = System.currentTimeMillis();
										float transferRate_bps = (uploadFileSize * 8) / ((timeEnd - timeStart) / 1000f);
										float transferRate_Bps = uploadFileSize / ((timeEnd - timeStart) / 1000f);

										for (int i = 0; i < speedTestListenerList.size(); i++) {
											speedTestListenerList.get(i).onUploadPacketsReceived(uploadFileSize, transferRate_bps, transferRate_Bps);
										}
										return;
									}

								} else if (httpStates == HttpStates.HTTP_READING_ERROR) {
//									isReading = false;
									Log.d(TAG, "giang dbg HTTP_READING_ERROR: ");
//									closeSocket();
									for (int i = 0; i < speedTestListenerList.size(); i++) {
										speedTestListenerList.get(i).onUploadError(SpeedTestError.SPEED_TEST_ERROR_SOCKET_ERROR, "HTTP reading error");
									}
								}


							} catch (SocketException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			});
			readingThread.start();

			if (task != null) {
				task.run();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start download process
	 * 
	 * @param hostname
	 *            server hostname
	 * @param port
	 *            server port
	 * @param uri
	 *            uri to fetch to download file
	 */
	public void startDownload(String hostname, int port, String uri) {
		this.hostname = hostname;
		this.port = port;
//		String downloadRequest = "GET " + uri + " HTTP/1.1\r\n" + "Host: " + hostname + "\r\n\r\n";
		String request = "GET /%s HTTP/1.1 \r\nHost: %s \r\nACCEPT: */*\r\n\r\n";
		String downloadRequest = String.format(request, uri, hostname);
		Log.d(TAG, "giang dbg downloadRequest: " + downloadRequest);
		writeDownload(downloadRequest.getBytes());
	}

	/**
	 * Write download request to server host
	 * 
	 * @param data
	 *            HTTP request to send to initiate download process
	 */
	public void writeDownload(final byte[] data) {

		connectAndExecuteTask(new TimerTask() {
			@Override
			public void run() {
				if (socket != null && !socket.isClosed()) {
					try {
						if (socket.getOutputStream() != null) {
							timeStart = System.currentTimeMillis();
							socket.getOutputStream().write(data);
							socket.getOutputStream().flush();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}, true);
	}

	/**
	 * Start upload process
	 * 
	 * @param hostname
	 *            server hostname
	 * @param port
	 *            server port
	 * @param uri
	 *            uri to fetch
	 * @param fileSizeOctet
	 *            size of file to upload
	 */
	public void startUpload(String hostname, int port, String uri, int fileSizeOctet) {
		this.hostname = hostname;
		this.port = port;
		this.uploadFileSize = fileSizeOctet;
		/* generate a file with size of fileSizeOctet octet */
		RandomGen random = new RandomGen(fileSizeOctet);
//		byte[] fileContent = random.nextArray();
		byte[] fileContent = random.getBuf();

//		String uploadRequest = "POST " + uri + " HTTP/1.1\r\n" + "Host: " + hostname + "\r\nAccept: */*\r\nContent-Length: " + fileSizeOctet + "\r\n\r\n";

		StringBuilder sb = new StringBuilder();
		sb.append("POST %s HTTP/1.0 \r\n");
		sb.append("Host :%s \r\n");
		sb.append("Accept: **/*//*\r\n");
		sb.append("Content-Length: %s \r\n");
		sb.append("Connection: keep-alive \r\n");
		sb.append("Content-Type: application/x-www-form-urlencoded\r\n");
//		sb.append("Content-Type: multipart/form-data; charset=utf-8; boundary=\"another cool boundary\"\r\n");
		sb.append("Expect: 100-continue\r\n");
		sb.append("\r\n");

		String uploadRequest = String.format(sb.toString(), uri, hostname, fileSizeOctet);
		Log.d(TAG, "uploadRequest: " + uploadRequest);
		writeUpload(uploadRequest.getBytes(), fileContent);
	}

	/**
	 * Write upload POST request with file generated randomly
	 * 
	 * @param head
	 * @param body
	 */
	public void writeUpload(final byte[] head, final byte[] body) {
		connectAndExecuteTask(new TimerTask() {
			@Override
			public void run() {
				if (socket != null && !socket.isClosed()) {
					try {

						int temp = 0;
						int step = body.length / 100;
						int remain = body.length % 100;
						Log.d(TAG, "giang dbg body.length: " + body.length);
						if (socket.getOutputStream() != null) {

							socket.getOutputStream().write(head);
							socket.getOutputStream().flush();
							timeStart = System.currentTimeMillis();
							for (int j = 0; j < speedTestListenerList.size(); j++) {
								speedTestListenerList.get(j).onUploadProgress(0);
							}
							for (int i = 1; i < 100; i++) {
								socket.getOutputStream().write(Arrays.copyOfRange(body, temp, temp + step));
								socket.getOutputStream().flush();
								for (int j = 0; j < speedTestListenerList.size(); j++) {
									speedTestListenerList.get(j).onUploadProgress(i);
								}
								temp += step;
							}

							Log.d(TAG, "giang dbg temp + remain + step: " + (temp + remain + step));
							socket.getOutputStream().write(Arrays.copyOfRange(body, temp, temp + remain + step));
							socket.getOutputStream().flush();
							for (int j = 0; j < speedTestListenerList.size(); j++) {
								speedTestListenerList.get(j).onUploadProgress(100);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}, false);
	}

	/**
	 * Close socket streams and socket object
	 */
	public void closeSocket() {

		if (socket != null) {
			try {
				socket.getOutputStream().close();
				socket.getInputStream().close();
				socket.close();
			} catch (IOException e) {
			}
		}
		socket = null;
	}

	/**
	 * Join reading thread before closing socket
	 */
	public void closeSocketJoinRead() {
		if (readingThread != null) {
			isReading = false;
			try {
				readingThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		closeSocket();
	}


}
