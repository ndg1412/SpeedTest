package speedtest;

//import android.annotation.SuppressLint;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class LatencyTest implements Runnable {

	public static final String STRING_ID = "JUDPLATENCY";
	public static final int STATUSFIELD = 2;
	public static final int TARGETFIELD = 3;
	public static final int IPTARGETFIELD = 4;
	public static final int AVERAGEFIELD = 5;
	private static final String LATENCYRUN = "Running latency and loss tests";
	private static final String LATENCYDONE = "Latency and loss tests completed";

	public static final String JSON_RTT_AVG = "rtt_avg";
	public static final String JSON_RTT_MIN = "rtt_min";
	public static final String JSON_RTT_MAX = "rtt_max";
	public static final String JSON_RTT_STDDEV = "rtt_stddev";
	public static final String JSON_RECEIVED_PACKETS = "received_packets";
	public static final String JSON_LOST_PACKETS = "lost_packets";

	public class Result {
		public String target;
		public long rtt;

		public Result(String _target, long _rtt) {
			target = _target;
			rtt = _rtt / 1000;
		}
	}
	
	public static int getPacketSize(){
		return UdpDatagram.PACKETSIZE;
	}

	static private class PacketTimeOutException extends Exception{
		
	}
	
	static private class UdpDatagram {
		static final int PACKETSIZE = 16;
		static final int SERVERTOCLIENTMAGIC = 0x00006000;
		static final int CLIENTTOSERVERMAGIC = 0x00009000;

		int datagramid;
		@SuppressWarnings("unused")
		int starttimesec;
		@SuppressWarnings("unused")
		int starttimeusec;
		int magic;

		// When we make the "ping we don't want to lose any time in memory
		// allocations, as much as possible should be ready (I miss structs...)
		byte[] arrayRepresentation;

		UdpDatagram(byte[] byteArray) {
			arrayRepresentation = byteArray;
			ByteBuffer bb = ByteBuffer.wrap(byteArray);
			datagramid = bb.getInt();
			starttimesec = bb.getInt();
			starttimeusec = bb.getInt();
			magic = bb.getInt();
		}

		UdpDatagram(int datagramid, int magic) {
			this.datagramid = datagramid;
			this.magic = magic;
			arrayRepresentation = new byte[PACKETSIZE];
			arrayRepresentation[0] = (byte) (datagramid >>> 24);
			arrayRepresentation[1] = (byte) (datagramid >>> 16);
			arrayRepresentation[2] = (byte) (datagramid >>> 8);
			arrayRepresentation[3] = (byte) (datagramid);
			arrayRepresentation[12] = (byte) (magic >>> 24);
			arrayRepresentation[13] = (byte) (magic >>> 16);
			arrayRepresentation[14] = (byte) (magic >>> 8);
			arrayRepresentation[15] = (byte) (magic);
		}

		byte[] byteArray() {
			return arrayRepresentation;
		}

		void setTime(long time) {
			int starttimesec = (int) (time / (int) 1e9);
			int starttimeusec = (int) ((time / (int) 1e3) % (int) 1e6);

			arrayRepresentation[4] = (byte) (starttimesec >>> 24);
			arrayRepresentation[5] = (byte) (starttimesec >>> 16);
			arrayRepresentation[6] = (byte) (starttimesec >>> 8);
			arrayRepresentation[7] = (byte) (starttimesec);
			arrayRepresentation[8] = (byte) (starttimeusec >>> 24);
			arrayRepresentation[9] = (byte) (starttimeusec >>> 16);
			arrayRepresentation[10] = (byte) (starttimeusec >>> 8);
			arrayRepresentation[11] = (byte) (starttimeusec);
		}
	}

	public LatencyTest() {
	}

	public String getStringID() {
		return STRING_ID;
	}

	public LatencyTest(String server, int port, int numdatagrams) {
		this.numdatagrams = numdatagrams;
		results = new long[numdatagrams];
	}

	public LatencyTest(String server, int port, int numdatagrams,
			int interPacketTime) {
		target = server;
		this.port = port;
		this.numdatagrams = numdatagrams;
		results = new long[numdatagrams];
		this.interPacketTime = interPacketTime * 1000; // nanoSeconds
	}

	public LatencyTest(String server, int port, int numdatagrams,
			int interPacketTime, int delayTimeout) {
		target = server;
		this.port = port;
		this.numdatagrams = numdatagrams;
		results = new long[numdatagrams];
		this.interPacketTime = interPacketTime * 1000; // nanoSeconds
		this.delayTimeout = delayTimeout / 1000; // mSeconds
	}

	public void setBlockingQueueResult(BlockingQueue<Result> queue) {
		bq_results = queue;
	}

	
	public int getNetUsage() {
		return UdpDatagram.PACKETSIZE * (sentPackets + recvPackets);
	}

	// @SuppressLint("NewApi")
	
	public boolean isReady() {
		if (target.length() == 0) {
			return false;
		}
		if (port == 0) {
			return false;
		}
		if (numdatagrams == 0 || results == null) {
			return false;
		}
		if (delayTimeout == 0) {
			return false;
		}
		if (interPacketTime == 0) {
			return false;
		}
		if (percentile < 0 || percentile > 100) {
			return false;
		}
		return true;
	}

	
	public void execute() {
		run();
	}

	
	public boolean isSuccessful() {
		return testStatus.equals("OK");
	}

	public String getInfo() {
		return infoString;
	}

	public String getHumanReadableResult() {
		String ret = "";
		if (testStatus.equals("FAIL")) {
			ret = String.format("The latency test has failed.");
		} else {
			// added cast otherwise it will always be 0 or 1;
			int packetLoss = (int) (100 * (((float) sentPackets - recvPackets) / sentPackets)); 
			
			int jitter = (int) ((average - minimum) / 1000000);
			ret = String.format(
					"Latency is %d ms. Packet loss is %d %%. Jitter is %d ms",
					(int) (average / 1000000), packetLoss, jitter);
		}
		return ret;
	}

	
	@Override
	public void run() {
		//set to zero internal variables in case the same test object is executed severals times
		System.out.println("giang dbg ping==========================>");
		sentPackets=0;
		recvPackets=0;
		startTime = System.nanoTime();
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(delayTimeout);
		} catch (SocketException e) {
			failure();
			return;
		}

		InetAddress address = null;
		try {
			address = InetAddress.getByName(target);
			ipAddress = address.getHostAddress();
		} catch (UnknownHostException e) {
			failure();
			e.printStackTrace();
			return;
		}
		System.out.println("giang dbg ping 0000000000000000000000");
		for (int i = 0; i < numdatagrams; ++i) {
			
			if ((maxExecutionTime > 0)
					&& (System.nanoTime() - startTime > maxExecutionTime)) {
				break;
			}

			UdpDatagram data = new UdpDatagram(i,
					UdpDatagram.CLIENTTOSERVERMAGIC);
			byte[] buf = data.byteArray();
			DatagramPacket packet = new DatagramPacket(buf, buf.length,
					address, port);
			long answerTime = 0;
			System.out.println("giang dbg ping 1111111111111111111111111111111");
			// It isn't the current time as in the original but a random value.
			// Let's hope nobody changes the server to make this important...
			long time = System.nanoTime();
			data.setTime(time);

			try {
				socket.send(packet);
				sentPackets++;
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}

			try {
				UdpDatagram answer;
				do {
					//Checks for the current time and set the SoTimeout accordingly 
					//because of duplicate packets or packets received after delayTimeout
					long now = System.nanoTime();
					long timeout = delayTimeout - (now - time)/1000000;
					if(timeout<0){
						throw new PacketTimeOutException();
					}
					socket.setSoTimeout((int) timeout);
					socket.receive(packet);
					answer = new UdpDatagram(buf);
				
				} while (answer.magic != UdpDatagram.SERVERTOCLIENTMAGIC || answer.datagramid != i);
				answerTime = System.nanoTime();
				System.out.println("giang dbg ping 222222222222222222222222222222");
				recvPackets++;
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
				continue;
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} catch (PacketTimeOutException e){
				e.printStackTrace();
				continue;
			}
			System.out.println("giang dbg ping 3333333333333333333333333333");
			System.out.println("giang dbg ping recvPackets: " + recvPackets);

			long rtt = answerTime - time;
			System.out.println("giang dbg ping rtt: " + rtt);
			results[recvPackets - 1] = rtt;

			sleep(rtt);
		}

		socket.close();

		getStats();
		if (bq_results != null) {
			Result r = new Result(target, (long) average);
			try {
				bq_results.put(r);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("giang dbg ping: " + getHumanReadableResult());
	}

	
	
	private void sleep(long rtt) {
		long sleepPeriod = interPacketTime - rtt;

		if (sleepPeriod > 0) {
			long millis = (long) Math.floor(sleepPeriod / 1000000);
			int nanos = (int) sleepPeriod % 1000000;
			try {
				Thread.sleep(millis, nanos);
			} catch (InterruptedException e) {

			}
		}
	}

	private void failure() {
		testStatus = "FAIL";		
	}

	private void getStats() {
		if (recvPackets <= 0) {
			failure();
			return;
		}
		testStatus = "OK";

		// Calculate statistics
		// Results sorted in order to take into account the percentile
		int nResults = 0;
		if (recvPackets < 100) {
			nResults = recvPackets;
		} else {
			nResults = (int) Math.ceil(percentile / 100.0 * recvPackets);
		}
		Arrays.sort(results, 0, recvPackets);
		minimum = results[0];
		maximum = results[nResults - 1];
		average = 0;
		for (int i = 0; i < nResults; i++) {
			average += results[i];
		}
		average /= nResults;

		stddeviation = 0;

		for (int i = 0; i < nResults; ++i) {
			stddeviation += Math.pow(results[i] - average, 2);
		}

		if (nResults - 1 > 0) {
			stddeviation = Math.sqrt(stddeviation / (nResults - 1));
		} else {
			stddeviation = 0;
		}

		// Return results

		infoString = LATENCYDONE;

	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setNumberOfDatagrams(int n) {
		numdatagrams = n;
		results = new long[numdatagrams];
	}

	public void setDelayTimeout(int delay) {
		delayTimeout = delay / 1000;
	}

	public void setInterPacketTime(int time) {
		interPacketTime = time * 1000; // nanoSeconds
	}

	public void setPercentile(int n) {
		percentile = n;
	}

	public void setMaxExecutionTime(long time) {
		maxExecutionTime = time * 1000; // nanoSeconds
	}

	public boolean isProgressAvailable() {
		return true;
	}

	public int getProgress() {
		double retTime = 0;
		double retPackets = 0;
		if (maxExecutionTime > 0) {
			long currTime = (System.nanoTime() - startTime);
			retTime = (double) currTime / maxExecutionTime;
		}
		retPackets = (double) sentPackets / numdatagrams;

		double ret = retTime > retPackets ? retTime : retPackets;
		ret = ret > 1 ? 1 : ret;
		return (int) (ret * 100);
	}

	String target = "";
	int port = 6000;
	String infoString = LATENCYRUN;
	String ipAddress;
	String testStatus = "FAIL";
	double average = 0.0;
	double stddeviation = 0.0;
	long minimum = 0;
	long maximum = 0;
	private long startTime = 0;
	private long maxExecutionTime = 30000000000l;
	private double percentile = 100;
	private int numdatagrams = 60;
	private int delayTimeout = 2000;
	private int sentPackets = 0;
	private int recvPackets = 0;
	private int interPacketTime = 500000000;
	private long[] results = null;
	private BlockingQueue<Result> bq_results = null;

}
