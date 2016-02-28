package speedtest;

import java.util.Random;

/**
 * Generate Random byte array for randomly generated uploaded file
 * 
 * @author Bertrand Martel
 */
public class RandomGen {

	private static final byte[] symbols;

	static {
		symbols = new byte[255];
		for (int i = 0; i < 255; i++)
			symbols[i] = (byte) i;
	}

	private final Random random = new Random();

	private final byte[] buf;

	public RandomGen(int length) {
		if (length < 1)
			throw new IllegalArgumentException("length < 1: " + length);
		buf = new byte[length];
	}

	public byte[] nextArray() {
		for (int idx = 0; idx < buf.length; ++idx)
			buf[idx] = symbols[random.nextInt(symbols.length)];
		return buf;
	}

	public byte[] getBuf() {
		String tmp = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int a = buf.length / 36;
		int b = buf.length % 36;
		for(int i = 0; i < a; i++) {
			System.arraycopy(tmp.getBytes(), 0, buf, 36*i, 36);
		}
		System.arraycopy(tmp.getBytes(), 0, buf, 36*a, b);
		return buf;
	}
}
