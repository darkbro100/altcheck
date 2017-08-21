package me.mario.altchecker.util;

import java.util.concurrent.Callable;

public class Util {

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

}
