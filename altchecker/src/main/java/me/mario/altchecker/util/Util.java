package me.mario.altchecker.util;

import java.util.concurrent.Callable;
import java.util.regex.Pattern;

public class Util {

	private static final String IPV4_REGEX = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
	private static final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);
	
	public static void tryRun(Runnable r, boolean log) {
		try {
			r.run();
		} catch (Exception e) {
			if (log)
				e.printStackTrace();
		}
	}
	
	public static <T> T tryElse(Callable<T> c, T other, boolean log) {
		try {
			return c.call();
		} catch (Exception e) {
			if(log)
				e.printStackTrace();
			
			return other;
		}
	}
	
	public static boolean isValidIPV4(String ipv4) {
		return IPV4_PATTERN.matcher(ipv4).matches();
	}

}
