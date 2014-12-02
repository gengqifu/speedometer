package org.aurora.speedometer;

public final class Log {

    public static final int VERBOSE = android.util.Log.VERBOSE;
    public static final int DEBUG = android.util.Log.DEBUG;
    public static final int INFO = android.util.Log.INFO;
    public static final int WARN = android.util.Log.WARN;
    public static final int ERROR = android.util.Log.ERROR;
    public static final int ASSERT = android.util.Log.ASSERT;
    
    private static Log mInstance = null;
    private final static String TAG = "speedometer";

    public static synchronized Log getInstance(){
        if (mInstance == null) {
            mInstance = new Log();
        }

        return mInstance;
    }

    public static void v() {
        android.util.Log.v(TAG, getCodeAddress());
    }
    
    public static void v(String msg) {
        v(TAG, msg);
    }
    
    private static String getCodeAddress() {
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

            if (st.getClassName().equals(getInstance().getClass().getName())) {
                continue;
            }

            return "[ " + st.getFileName() + ":" + st.getLineNumber() + " ]";
        }

        return null;
    }

    public static int i(String tag, String msg) {
        return android.util.Log.i(TAG, getCodeAddress() + " - " + msg);

    }

    public static int e(String tag, String msg) {
        return android.util.Log.e(TAG, getCodeAddress() + " - " + msg);
    }

    public static int w(String tag, Throwable exception) {
        String msg = exception.getMessage();

        if (null == msg) {
            exception.printStackTrace();
            msg = "no exception msg!";
        }
        return w(TAG, getCodeAddress() + " - " + msg);
    }

    public static int d(String tag, String msg) {
        return android.util.Log.d(TAG, getCodeAddress() + " - " + msg);
    }

    public static int v(String tag, String msg) {
        return android.util.Log.v(TAG, getCodeAddress() + " - " + msg);
    }

    public static int w(String tag, String msg) {
        return android.util.Log.w(TAG, getCodeAddress() + " - " + msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return android.util.Log.w(TAG, getCodeAddress() + " - " + msg, tr);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return android.util.Log.d(TAG, getCodeAddress() + " - " + msg, tr);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return android.util.Log.w(TAG, getCodeAddress() + " - " + msg, tr);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return android.util.Log.e(TAG, getCodeAddress() + " - " + msg, tr);
    }

    public static int wtf(String tag, String msg) {
        return android.util.Log.wtf(TAG, getCodeAddress() + " - " + msg);
    }

    public static int wtfStack(String tag, String msg) {
        return android.util.Log.wtf(TAG, getCodeAddress() + " - " + msg);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     * Similar to {@link #wtf(String, String)}, with an exception to log.
     * @param tag Used to identify the source of a log message.
     * @param tr An exception to log.
     */
    public static int wtf(String tag, Throwable tr) {
        return android.util.Log.wtf(TAG, getCodeAddress(), tr);
    }

    public static int wtf(String tag, String msg, Throwable tr) {
        return android.util.Log.wtf(TAG, getCodeAddress() + " - " + msg, tr);
    }

    public static boolean isLoggable(String tag, int level) {
        return true;
//      return android.util.Log.isLoggable(TAG, level);
    }

    public static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    public static void printThreadStackTrace() {
        String str = "\n-------------------------------------------------------------";

        int count = -3; // Current function.
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            count ++;
            if (count <= 0) continue; 
            str += "\n" + count + "\t" + ste;
        }

        str += "\n-------------------------------------------------------------";

        android.util.Log.v(TAG, str);
    }

}

