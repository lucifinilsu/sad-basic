package com.sad.basic.utils.clazz;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import dalvik.system.DexFile;

/**
 * Created by Administrator on 2018/9/30 0030.
 */

public class ClassScannerClient {

    private static final String EXTRACTED_NAME_EXT = ".classes";
    private static final String EXTRACTED_SUFFIX = ".zip";
    private static final String SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes";
    private static final String PREFS_FILE = "multidex.version";
    private static final String KEY_DEX_NUMBER = "dex.number";



    private boolean instantRunSupport = true;
    private boolean log = false;
    private Context context;
    private ThreadPoolExecutor threadPoolExecutor = ClassScanWorkerThreadPoolExecutor.getInstance();

    public static ClassScannerClientBuilder with(Context context){
        return new ClassScannerClientBuilder(context);
    }

    protected ClassScannerClient(ClassScannerClientBuilder builder){
        this.instantRunSupport=builder.instantRunSupport;
        this.log=builder.log;
        this.context=builder.context;
        this.threadPoolExecutor=builder.threadPoolExecutor;
    }
    /**
     * 扫描指定包下面经过过滤后所有的ClassName
     *
     * @param packageName 包名
     * @return 所有class的集合
     */
    public Set<String> scan(String packageName, ClassScannerFilter filter) throws Exception {
        Set<String> classNames = new HashSet<>();
        List<String> paths = getSourcePaths(context);
        if (log){
            for (String path:paths) {
                Log.e("sad", "----------------->即将要扫描的路径："+path);
            }
        }

        final CountDownLatch parserCtl = new CountDownLatch(paths.size());

        for (final String path : paths) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    DexFile dexfile = null;
                    try {
                        if (path.endsWith(EXTRACTED_SUFFIX)) {
                            //NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"
                            if (log){
                                Log.e("sad", "----------------->"+path+"文件使用DexFile.loadDex方法");
                            }
                            dexfile = DexFile.loadDex(path, path + ".tmp", 0);
                        } else {
                            dexfile = new DexFile(path);
                        }

                        Enumeration<String> dexEntries = dexfile.entries();
                        while (dexEntries.hasMoreElements()) {
                            String className = dexEntries.nextElement();
                            if (className.startsWith(packageName)) {

                                //根据过滤器进行判断一下
                                boolean right=(filter==null || filter.accept(Class.forName(className)));
                                if (right){
                                    classNames.add(className);
                                }
                                if (log){
                                    Log.e("sad", "----------------->扫描到类"+(right?"(符合过滤条件)":"")+":"+className);
                                };

                            }
                        }
                    } catch (Throwable ignore) {
                        if (log){
                            Log.e("sad", "----------------->扫描发生异常："+ignore.getMessage());
                        };
                        ignore.printStackTrace();
                    } finally {
                        if (null != dexfile) {
                            try {
                                dexfile.close();
                            } catch (Throwable ignore) {
                            }
                        }
                        parserCtl.countDown();
                    }
                }
            });
        }

        parserCtl.await();
        if (log){
            Log.e("sad", "----------------->针对"+packageName+"的扫描结束");
        };
        //Log.d(Consts.TAG, "Filter " + classNames.size() + " classes by packageName <" + packageName + ">");
        return classNames;
    }

    private static SharedPreferences getMultiDexPreferences(Context context) {
        return context.getSharedPreferences(PREFS_FILE, Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? Context.MODE_PRIVATE : Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }


    /**
     * get all the dex path
     *
     * @param context the application context
     * @return all the dex path
     * @throws PackageManager.NameNotFoundException
     * @throws IOException
     */
    public List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        File sourceApk = new File(applicationInfo.sourceDir);
        List<String> sourcePaths = new ArrayList<>();
        sourcePaths.add(applicationInfo.sourceDir); //add the default apk path

        //the prefix of extracted file, ie: test.classes
        String extractedFilePrefix = sourceApk.getName() + EXTRACTED_NAME_EXT;
        if (log){
            Log.e("sad", "----------------->extractedFilePrefix="+extractedFilePrefix);
        };

//        如果VM已经支持了MultiDex，就不要去Secondary Folder加载 Classesx.zip了，那里已经么有了
//        通过是否存在sp中的multidex.version是不准确的，因为从低版本升级上来的用户，是包含这个sp配置的
        if (!ClassScannerUtils.isVMMultidexCapable()) {
            if (log){
                Log.e("sad", "----------------->VM不支持MultiDex");
            };
            //the total dex numbers
            int totalDexNumber = getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER, 1);
            File dexDir = new File(applicationInfo.dataDir, SECONDARY_FOLDER_NAME);

            for (int secondaryNumber = 2; secondaryNumber <= totalDexNumber; secondaryNumber++) {
                //for each dex file, ie: test.classes2.zip, test.classes3.zip...
                String fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX;
                File extractedFile = new File(dexDir, fileName);
                if (extractedFile.isFile()) {
                    sourcePaths.add(extractedFile.getAbsolutePath());
                    //we ignore the verify zip part
                } else {
                    throw new IOException("Missing extracted secondary dex file '" + extractedFile.getPath() + "'");
                }
            }
        }

        if (instantRunSupport) { // Search instant run support only debuggable
            sourcePaths.addAll(tryLoadInstantRunDexFile(applicationInfo));
        }
        return sourcePaths;
    }

    /**
     * Get instant run dex path, used to catch the branch usingApkSplits=false.
     */
    protected List<String> tryLoadInstantRunDexFile(ApplicationInfo applicationInfo) {
        if (log){
            Log.e("sad", "----------------->加载Instant Run所需要扫描的路径");
        };
        List<String> instantRunSourcePaths = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && null != applicationInfo.splitSourceDirs) {
            // add the split apk, normally for InstantRun, and newest version.
            instantRunSourcePaths.addAll(Arrays.asList(applicationInfo.splitSourceDirs));
            //Log.d(Consts.TAG, "Found InstantRun support");
        } else {
            try {
                // This man is reflection from Google instant run sdk, he will tell me where the dex files go.
                Class pathsByInstantRun = Class.forName("com.android.tools.fd.runtime.Paths");
                Method getDexFileDirectory = pathsByInstantRun.getMethod("getDexFileDirectory", String.class);
                String instantRunDexPath = (String) getDexFileDirectory.invoke(null, applicationInfo.packageName);

                File instantRunFilePath = new File(instantRunDexPath);
                if (instantRunFilePath.exists() && instantRunFilePath.isDirectory()) {
                    File[] dexFile = instantRunFilePath.listFiles();
                    for (File file : dexFile) {
                        if (null != file && file.exists() && file.isFile() && file.getName().endsWith(".dex")) {
                            instantRunSourcePaths.add(file.getAbsolutePath());
                        }
                    }
                    //Log.d(Consts.TAG, "Found InstantRun support");
                }

            } catch (Exception e) {
                //Log.e(Consts.TAG, "InstantRun support error, " + e.getMessage());
            }
        }
        if(log){
            Log.e("sad", "----------------->Instant Run所需要扫描的路径："+instantRunSourcePaths);
        }

        return instantRunSourcePaths;
    }


}
