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

import com.netvisiontel.speedtest.Interface.ISpeedTestListener;
import com.netvisiontel.speedtest.util.Const;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * Speed Test example
 * 
 * <ul>
 * <li>Download test with progress bar and output</li>
 * <li>Upload test with progress bar and output</li>
 * </ul>
 * 
 * @author Bertrand Martel
 *
 */
public class SpeedTest {

	/** check download bar initialization */
	private static boolean initDownloadBar = false;

	/** check upload bar initialization */
	private static boolean initUploadBar = false;

	/**
	 * Instanciate Speed Test and start download and upload process with speed
	 * test server of your choice
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		/* instanciate speed test */
		SpeedTestSocket speedTestSocket = new SpeedTestSocket();

		/* add a listener to wait for speed test completion and progress */
		speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

			@Override
			public void onDownloadPacketsReceived(int packetSize, float transferRateBitPerSeconds, float transferRateOctetPerSeconds) {
				System.out.println("Download [ OK ]");
				System.out.println("download packetSize     : " + packetSize + " octet(s)");
				System.out.println("download transfer rate  : " + transferRateBitPerSeconds + " bit/second   | " + transferRateBitPerSeconds / 1000
						+ " Kbit/second  | " + transferRateBitPerSeconds / 1000000 + " Mbit/second");
				System.out.println("download transfer rate  : " + transferRateOctetPerSeconds + " octet/second | " + transferRateOctetPerSeconds / 1000
						+ " Koctet/second | " + +transferRateOctetPerSeconds / 1000000 + " Moctet/second");
				System.out.println("##################################################################");
			}

			@Override
			public void onDownloadError(int errorCode, String message) {
				System.out.println("Download error " + errorCode + " occured with message : " + message);
			}

			@Override
			public void onUploadPacketsReceived(int packetSize, float transferRateBitPerSeconds, float transferRateOctetPerSeconds) {
				System.out.println("");
				System.out.println("========= Upload [ OK ]   =============");
				System.out.println("upload packetSize     : " + packetSize + " octet(s)");
				System.out.println("upload transfer rate  : " + transferRateBitPerSeconds + " bit/second   | " + transferRateBitPerSeconds / 1000
						+ " Kbit/second  | " + transferRateBitPerSeconds / 1000000 + " Mbit/second");
				System.out.println("upload transfer rate  : " + transferRateOctetPerSeconds + " octet/second | " + transferRateOctetPerSeconds / 1000
						+ " Koctet/second | " + +transferRateOctetPerSeconds / 1000000 + " Moctet/second");
				System.out.println("##################################################################");
			}

			@Override
			public void onUploadError(int errorCode, String message) {
				System.out.println("Upload error " + errorCode + " occured with message : " + message);
			}

			@Override
			public void onDownloadProgress(int percent) {
				System.out.println("giang dbg onDownloadProgress percent " + percent);
				if (!initDownloadBar)
					System.out.print("download progress | < ");
				initDownloadBar = true;
				if (percent % 4 == 0)
					System.out.print("=");
				if (percent == 100)
					System.out.println(" 100%");
			}

			@Override
			public void onUploadProgress(int percent) {
				System.out.println("giang dbg onUploadProgress percent " + percent);
				if (!initUploadBar)
					System.out.print("upload progress | < ");
				initUploadBar = true;
				if (percent % 5 == 0)
					System.out.print("=");
				if (percent == 100)
					System.out.println(" 100%");
			}
		});
		
		/* start speed test download on favorite server */
//		speedTestSocket.startDownload("ipv4.intuxication.testdebit.info", 80, "/fichiers/10Mo.dat");
//		speedTestSocket.startDownload("mirror-fpt-telecom.fpt.net", 80, "/opera/Meego/1100/Opera_Mobile-MeeGo-11.00-86.i386.rpm");
//		speedTestSocket.startDownload(Const.HOST, Const.PORT, Const.URL);

		// socket will be closed and reading thread will die if it exists
//		speedTestSocket.closeSocketJoinRead();

		/* start speed test upload on favorite server */
		CheckHost("8.8.8.8");
		System.out.println(Integer.MAX_VALUE);
		speedTestSocket.startUpload(Const.HOST, Const.PORT, "/", 134217729);

		// socket will be closed and reading thread will die if it exists
//		speedTestSocket.closeSocketJoinRead();
		
	}
	
	public static boolean pingUrl(final String address) {
		 try {
		  final URL url = new URL("http://" + address);
		  final HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		  urlConn.setConnectTimeout(1000 * 10); // mTimeout is in seconds
		  final long startTime = System.currentTimeMillis();
		  urlConn.connect();
		  final long endTime = System.currentTimeMillis();
		  if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
		   System.out.println("Time (ms) : " + (endTime - startTime));
		   System.out.println("Ping to "+address +" was success");
		   return true;
		  }
		 } catch (final MalformedURLException e1) {
		  e1.printStackTrace();
		 } catch (final IOException e) {
		  e.printStackTrace();
		 }
		 return false;
		}
	public static boolean CheckHost(String host) {
		Runtime runtime = Runtime.getRuntime();
		try {

			Process ipProcess = runtime.exec("ping " + host);
			InputStream stdout = ipProcess.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
			String line;
			while ((line = reader.readLine ()) != null) {
				if(line.contains(String.format("Reply from %s:", host))) {
					System.out.println("giang dbg ping output: " + line.split(String.format("Reply from %s:", host))[1]);
				}

			}
			int     exitValue = ipProcess.waitFor();


			return (exitValue == 0);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;

	}
}
