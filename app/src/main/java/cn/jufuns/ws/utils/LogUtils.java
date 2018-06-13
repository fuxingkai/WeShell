package cn.jufuns.ws.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

/**
 * 全局自定义Log日志工具类
 * 
 * @author zch 2016-7-1
 */
@TargetApi(Build.VERSION_CODES.DONUT)
public class LogUtils {

	private static String tag = "LogUtils";//默认的tag
	private static int logLevel = Log.VERBOSE;// 日志输出级别
	public final static boolean DEBUG = true;// 是否输出日志

	private static boolean isDebug;
	static {// 应用正式打包签名之后，BuildConfig.DEBUG的值会自动变为false
		if (Build.VERSION.SDK_INT < 10) {
			isDebug = false;
		} else {
			isDebug = DEBUG;
		}
	}

	private LogUtils() {
	}

	public static void i(Object str) {
		if (isDebug) {
			info(str);
		}
	}

	public static void v(Object str) {
		if (isDebug) {
			verbose(str);
		}
	}

	public static void w(Object str) {
		if (isDebug) {
			warn(str);
		}
	}

	public static void e(Object str) {
		if (isDebug) {
			error(str);
		}
	}

	public static void e(Exception ex) {
		if (isDebug) {
			error(ex);
		}
	}

	public static void d(Object str) {
		if (isDebug) {
			debug(str);
		}
	}

	private static String getFunctionName() {
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();

		if (sts == null) {
			return null;
		}

		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}

			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}

			if (st.getClassName().equals(LogUtils.class.getName())) {
				continue;
			}

			return "[" + Thread.currentThread().getName() + "("
					+ Thread.currentThread().getId() + "): " + st.getFileName()
					+ ":" + st.getLineNumber() + "]";
		}

		return null;
	}

	private static void info(Object str) {
		if (logLevel <= Log.INFO) {
			String name = getFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.i(tag, ls);
		}
	}

	private static void verbose(Object str) {
		if (logLevel <= Log.VERBOSE) {
			String name = getFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.v(tag, ls);
		}
	}

	private static void warn(Object str) {
		if (logLevel <= Log.WARN) {
			String name = getFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.w(tag, ls);
		}
	}

	private static void error(Object str) {
		if (logLevel <= Log.ERROR) {
			String name = getFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.e(tag, ls);
		}
	}

	private static void error(Exception ex) {
		if (logLevel <= Log.ERROR) {
			StringBuffer sb = new StringBuffer();
			String name = getFunctionName();
			StackTraceElement[] sts = ex.getStackTrace();

			if (name != null) {
				sb.append(name + " - " + ex + "\r\n");
			} else {
				sb.append(ex + "\r\n");
			}

			if (sts != null && sts.length > 0) {
				for (StackTraceElement st : sts) {
					if (st != null) {
						sb.append("[ " + st.getFileName() + ":"
								+ st.getLineNumber() + " ]\r\n");
					}
				}
			}

			Log.e(tag, sb.toString());
		}
	}

	private static void debug(Object str) {
		if (logLevel <= Log.DEBUG) {
			String name = getFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.d(tag, ls);
		}
	}

}
