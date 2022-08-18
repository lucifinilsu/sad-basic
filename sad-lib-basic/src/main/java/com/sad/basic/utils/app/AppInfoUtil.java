package com.sad.basic.utils.app;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;

import com.sad.basic.utils.assistant.LogUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppInfoUtil {
	/**
	 * 获取内存信息
	 * 
	 * @param context
	 * @return
	 */
	public static MemoryInfo getMemoryInfo(Context context) {
		try {
			ActivityManager am;
			am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			MemoryInfo outInfo = new MemoryInfo();
			am.getMemoryInfo(outInfo);
			return outInfo;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取剩余可用内存
	 *
	 * @param context
	 * @return
	 */
	public static long getMemoryAvailable(Context context) {
		MemoryInfo outInfo = getMemoryInfo(context);
		if (outInfo != null) {
			return outInfo.availMem;
		}
		return -1L;
	}

	/**
	 * 获取内存杀后台阈值
	 *
	 * @param context
	 * @return
	 */
	public static long getMemoryThreshold(Context context) {
		MemoryInfo outInfo = getMemoryInfo(context);
		if (outInfo != null) {
			return outInfo.threshold;
		}
		return -1L;
	}

	/**
	 * 获取总内存值
	 *
	 * @param context
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static long getMemoryTotal(Context context) {
		MemoryInfo outInfo = getMemoryInfo(context);
		if (outInfo != null) {
			return outInfo.totalMem;
		}
		return -1L;
	}

	/**
	 * 获取内存数据数组
	 *
	 * @param context
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static long[] getMemoryInfoArray(Context context) {
		MemoryInfo outInfo = getMemoryInfo(context);
		if (outInfo != null) {
			long[] s = new long[3];
			s[0] = outInfo.availMem;
			s[1] = outInfo.threshold;
			if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				s[2] = outInfo.totalMem;
			} else {
				s[2] = -1;
			}
			return s;
		} else {
			return new long[] { -1L, -1L, -1L };
		}

	}

	/**
	 * 获取应用版本名称
	 *
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context) {
		// 获取packagemanager的实例
		PackageManager packageManager = context.getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			String version = packInfo.versionName;
			return version;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 获取版本号码
	 *
	 * @param context
	 * @return
	 */
	public static int getVersionCode(Context context) {
		int verCode = -1;
		try {
			verCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			// Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
		return verCode;
	}

	/**
	 * 安装App
	 *
	 * @param context
	 * @param filePath
	 * @return
	 */
	public static boolean installNormal(Context context, String filePath) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		java.io.File file = new java.io.File(filePath);
		if (file == null || !file.exists() || !file.isFile() || file.length() <= 0) {
			return false;
		}

		i.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
		return true;
	}

	/**
	 * 卸载App
	 *
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean uninstallNormal(Context context, String packageName) {
		if (packageName == null || packageName.length() == 0) {
			return false;
		}

		Intent i = new Intent(Intent.ACTION_DELETE,
				Uri.parse(new StringBuilder().append("package:").append(packageName).toString()));
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
		return true;
	}

	/**
	 * 判断是否是系统App
	 *
	 * @param context
	 * @param packageName
	 *            包名
	 * @return
	 */
	public static boolean isSystemApplication(Context context, String packageName) {
		if (context == null) {
			return false;
		}
		PackageManager packageManager = context.getPackageManager();
		if (packageManager == null || packageName == null || packageName.length() == 0) {
			return false;
		}

		try {
			ApplicationInfo app = packageManager.getApplicationInfo(packageName, 0);
			return (app != null && (app.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 判断某个包名是否运行在顶层
	 *
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static Boolean isTopActivity(Context context, String packageName) {
		if (context == null || TextUtils.isEmpty(packageName)) {
			return null;
		}

		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		if (tasksInfo == null || tasksInfo.isEmpty()) {
			return null;
		}
		try {
			return packageName.equals(tasksInfo.get(0).topActivity.getPackageName());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 获取Meta-Data
	 *
	 * @param context
	 * @param key
	 * @return
	 */
	public static String getAppMetaData(Context context, String key) {
		if (context == null || TextUtils.isEmpty(key)) {
			return null;
		}
		String resultData = null;
		try {
			PackageManager packageManager = context.getPackageManager();
			if (packageManager != null) {
				ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(),
						PackageManager.GET_META_DATA);
				if (applicationInfo != null) {
					if (applicationInfo.metaData != null) {
						resultData = applicationInfo.metaData.getString(key);
					}
				}

			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return resultData;
	}

	/**
	 * 判断当前应用是否运行在后台
	 *
	 * @param context
	 * @return
	 */
	public static boolean isApplicationInBackground(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskList = am.getRunningTasks(1);
		if (taskList != null && !taskList.isEmpty()) {
			ComponentName topActivity = taskList.get(0).topActivity;
			if (topActivity != null && !topActivity.getPackageName().equals(context.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	private static String getCurrAppProccessName2(){
		FileInputStream var1 = null;
		String var7;
		try {
			String var2 = "/proc/self/cmdline";
			var1 = new FileInputStream(var2);
			byte[] var3 = new byte[256];
			int var4;
			int var5;
			for(var4 = 0; (var5 = var1.read()) > 0 && var4 < var3.length; var3[var4++] = (byte)var5) {
			}
			if (var4 <= 0) {
				return null;
			}
			String var6 = new String(var3, 0, var4, "UTF-8");
			var7 = var6;
		} catch (Throwable var18) {
			var18.printStackTrace();
			return null;
		} finally {
			if (var1 != null) {
				try {
					var1.close();
				} catch (IOException var17) {
					var17.printStackTrace();
				}
			}

		}
		return var7;
	}

	private static String cacheCurrAppProccessName="";
	/**
	 * 获取当前运行的进程名
	 * @param context
	 * @return
	 */
	public static String getCurrAppProccessName(Context context){
		LogUtils.e("sad-basic",Log.getStackTraceString(new Throwable()));
		return getCurrAppProccessName(context,true);
	}
	public static String getCurrAppProccessName(Context context,boolean readCache) {
		if (readCache){
			if (!TextUtils.isEmpty(cacheCurrAppProccessName)){
				LogUtils.e("sad-basic","-------->获取进程名缓存:"+cacheCurrAppProccessName);
				return cacheCurrAppProccessName;
			}
		}
		try {
			cacheCurrAppProccessName=getCurrAppProccessName2();
			LogUtils.e("sad-basic","-------->获取进程名v2:"+cacheCurrAppProccessName);
			if (!TextUtils.isEmpty(cacheCurrAppProccessName)){
				return cacheCurrAppProccessName;
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}

		if (TextUtils.isEmpty(cacheCurrAppProccessName)){
			int pid = android.os.Process.myPid();
			ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			int i=0;
			List<ActivityManager.RunningAppProcessInfo> list=mActivityManager.getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo appProcess : list) {
				i++;
				if (appProcess.pid == pid) {
					LogUtils.e("sad-basic","获取进程名v1循环:"+i);
					cacheCurrAppProccessName= appProcess.processName;
					return cacheCurrAppProccessName;
				}
			}
		}
		return "";
	}

	/**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName
     *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
    
    /**
     * 获取当前运行的所有进程名
     */
    public static List<String> getProcessName(Context context, String packageName) {
        List<String> list = new ArrayList<String>();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.processName.startsWith(packageName)) {
                list.add(appProcess.processName);
            }
        }
        return list;
    }


	/**
	 * 获取app名称
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static String getAppName(Context context, final String packageName) {
		if (TextUtils.isEmpty(packageName)) return null;
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(packageName, 0);
			return pi == null ? null : pi.applicationInfo.loadLabel(pm).toString();
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 判断系统是否为YunOS系统
	 */
	public static boolean isYunOS() {
		try {
			String version = System.getProperty("ro.yunos.version");
			String vmName = System.getProperty("java.vm.name");
			return (vmName != null && vmName.toLowerCase().contains("lemur"))
					|| (version != null && version.trim().length() > 0);
		} catch (Exception ignore) {
			return false;
		}
	}
}
