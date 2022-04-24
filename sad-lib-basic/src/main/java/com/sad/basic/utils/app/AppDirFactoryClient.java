package com.sad.basic.utils.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.IntDef;

import com.sad.basic.utils.file.DirScanningResult;
import com.sad.basic.utils.file.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by LucifinilSu on 2018/3/16 0016.
 */

public class AppDirFactoryClient {
    //自定义一个注解来限制调用者的参数设定
    @IntDef({IStorageLocationStrategy.StorageLocation._R, IStorageLocationStrategy.StorageLocation._W})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RWMode {
    }

    //存储位置的最小预留空间限制，小于此值则更换位置或者抛异常
    public final static long MIN_RESERVE_SIZE = 3 * 1024 * 1024;

    //使用策略模式向外提供扩展
    public interface IStorageLocationStrategy {
        //存储位置
        public class StorageLocation {


            public final static int DATA = 0x00000110;//优先存储在DATA
            public final static int SDCARD = DATA << 2;//优先存储在SD卡
            public final static int _R = 0x00000010;//读模式
            public final static int _W = _R << 2;

            private int location = SDCARD;
            private boolean isAutoSwitch = true;
            private int rwMode = _W;

            public @RWMode
            int getRwMode() {
                return rwMode;
            }

            public void setRwMode(@RWMode int rwMode) {
                this.rwMode = rwMode;
            }

            public int getLocation() {
                return location;
            }

            public void setLocation(int location) {

                this.location = location;
            }

            public boolean isAutoSwitch() {
                return isAutoSwitch;
            }

            public void setAutoSwitch(boolean autoSwitch) {
                isAutoSwitch = autoSwitch;
            }
        }

        public StorageLocation location(long storageReserverSize);

    }

    //存储位置策略默认实现。默认优先存储至SD卡、自动选择根位置、写模式
    public class DefaultStorageLocationStrategy implements IStorageLocationStrategy {

        @Override
        public StorageLocation location(long storageReserverSize) {
            StorageLocation loc = new StorageLocation();
            loc.setAutoSwitch(true);
            loc.setLocation(StorageLocation.SDCARD);
            loc.setRwMode(StorageLocation._W);
            return loc;
        }
    }

    //创建结果回调
    public interface OnDirGoListener {
        public DirScanningResult OnGoCompleted(AppDirFactoryClient appDirFactoryClient, DirScanningResult result);
    }

    //扫描结果回调
    public interface OnDirScanListener {
        public DirScanningResult OnScanCompleted(AppDirFactoryClient appDirFactoryClient, boolean isRemove, DirScanningResult result);
    }

    private OnDirGoListener onDirGoListener;
    private OnDirScanListener onDirScanListener;


    private AppDirFactoryClient(Context context) {
        this.context = context;
    }

    private Context context;

    public static AppDirFactoryClient with(Context context) {

        return new AppDirFactoryClient(context);
    }

    public OnDirGoListener getOnDirGoListener() {
        return onDirGoListener;
    }

    public AppDirFactoryClient dirGoListener(OnDirGoListener onDirGoListener) {
        this.onDirGoListener = onDirGoListener;
        return this;
    }

    public OnDirScanListener getOnDirScanListener() {
        return onDirScanListener;
    }

    public AppDirFactoryClient dirScanListener(OnDirScanListener onDirScanListener) {
        this.onDirScanListener = onDirScanListener;
        return this;
    }

    private long storageReserverSize = MIN_RESERVE_SIZE;
    private IStorageLocationStrategy storageLocationStrategy = new DefaultStorageLocationStrategy();
    private boolean isBackground = false;
    /*public AppDirFactoryClient background(){
        isBackground=true;
        return this;
    }
    public AppDirFactoryClient foreground(){
        isBackground=false;
        return this;
    }*/

    public IStorageLocationStrategy getStorageLocationStrategy() {
        return storageLocationStrategy;
    }

    public AppDirFactoryClient setStorageLocationStrategy(IStorageLocationStrategy storageLocationStrategy) {
        this.storageLocationStrategy = storageLocationStrategy;
        return this;
    }

    /*private IStorageLocationStrategy.StorageLocation storagetLocation= IStorageLocationStrategy.StorageLocation.SDCARD;

    public IStorageLocationStrategy.StorageLocation getStoragetLocation(){
        return storagetLocation;
    }*/
    /*public AppDirFactoryClient forceStorageLocation(StorageLocation storageLocation){
        this.storagetLocation=storageLocation;
        return this;
    }*/
    public long getStorageReserverSize() {
        return storageReserverSize;
    }

    public AppDirFactoryClient setStorageReserverSize(long storageReserverSize) {
        if (storageReserverSize < MIN_RESERVE_SIZE) {
            storageReserverSize = MIN_RESERVE_SIZE;
        }
        this.storageReserverSize = storageReserverSize;
        return this;
    }

    private String path = "";

    public AppDirFactoryClient path(String path) {
        this.path = path;
        return this;
    }

    ;

    public AppDirFactoryClient toPathNode(String node) {
        this.path += File.separator + node;
        return this;
    }

    ;


    //根据策略，确定最终的目标目录
    private String getTargetDir(IStorageLocationStrategy.StorageLocation storageLocation) {
        String absPath = "";
        //策略分析:在确定cd目标位置之前，先判断读写模式
        int rwMode = storageLocation.getRwMode();
        boolean isA = storageLocation.isAutoSwitch();
        int root = storageLocation.getLocation();
        String dir = this.path;

        if (isA) {
            if (root == IStorageLocationStrategy.StorageLocation.SDCARD) {
                if (isEnableSDcard(storageReserverSize)) {
                    absPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + dir;
                } else {
                    if (isEnablePhoneData(storageReserverSize)) {
                        absPath = Environment.getDataDirectory().getAbsolutePath() + File.separator + "data" + File.separator + context.getPackageName()+ File.separator + dir;
                    }
                }
            } else if (root == IStorageLocationStrategy.StorageLocation.DATA) {
                if (isEnablePhoneData(storageReserverSize)) {
                    absPath = Environment.getDataDirectory().getAbsolutePath() + File.separator + "data" + File.separator + context.getPackageName()+ File.separator + dir;
                } else {
                    if (isEnableSDcard(storageReserverSize)) {
                        absPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + dir;
                    }
                }
            }
        } else {
            if (rwMode == IStorageLocationStrategy.StorageLocation._R) {
                //读模式下非自动切换模式不判断剩余空间是否符合要求
                if (root == IStorageLocationStrategy.StorageLocation.SDCARD) {
                    absPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + dir;
                } else if (root == IStorageLocationStrategy.StorageLocation.DATA) {
                    absPath = Environment.getDataDirectory().getAbsolutePath() + File.separator + "data" + File.separator + context.getPackageName()+ File.separator + dir;
                }
            } else if (rwMode == IStorageLocationStrategy.StorageLocation._W) {
                //写模式下非自动切换模式要判断剩余空间是否符合要求
                if (root == IStorageLocationStrategy.StorageLocation.SDCARD) {
                    if (isEnableSDcard(storageReserverSize)) {
                        absPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + dir;
                    }

                } else if (root == IStorageLocationStrategy.StorageLocation.DATA) {
                    if (isEnablePhoneData(storageReserverSize)) {
                        absPath = Environment.getDataDirectory().getAbsolutePath() + File.separator + "data" + File.separator + context.getPackageName()+ File.separator + dir;
                    }

                }
            }
        }
        return absPath;
    }

    public DirScanningResult go(OnDirGoListener onDirCreatedListener){
        return go(true, onDirCreatedListener);
    }
    /**
     * cd到当前目录
     *
     * @param onDirCreatedListener
     * @return
     */
    public DirScanningResult go(boolean createPathIfNotExsist,OnDirGoListener onDirCreatedListener) {
        DirScanningResult result = new DirScanningResult();
        if (!TextUtils.isEmpty(path)) {
            //根据存储策略建立目录。
            IStorageLocationStrategy.StorageLocation storageLocation = storageLocationStrategy.location(storageReserverSize);
            String absPath = getTargetDir(storageLocation);
            if (TextUtils.isEmpty(absPath)) {
                result.setSuccess(false);
                if (onDirCreatedListener != null) {
                    result.setException(new Exception("无法创建目录:" + path));
                    result = onDirCreatedListener.OnGoCompleted(this, result);
                }
                return result;
            }
            try {
                File file = new File(absPath);//createDir(absPath);
                if (createPathIfNotExsist){
                    file=createDir(absPath);
                }
                result.setSuccess(true);
                result.setFile(file);
                if (onDirCreatedListener != null) {
                    result = onDirCreatedListener.OnGoCompleted(this, result);
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                result.setSuccess(false);
                result.setException(e);
                if (onDirCreatedListener != null) {
                    result = onDirCreatedListener.OnGoCompleted(this, result);
                }
                result.setSuccess(false);
                return result;
            }
        }
        result.setSuccess(false);
        result.setException(new Exception("目录地址为空"));
        if (onDirCreatedListener != null) {
            result = onDirCreatedListener.OnGoCompleted(this, result);
        }
        return result;
    }

    private File createDir(String path) throws Exception {
        File file = new File(path);
        if ((file.exists() && file.isDirectory()) || file.mkdirs()) {
            return file;
        } else {
            throw new Exception("无法创建目录：" + path);
        }
    }


    public DirScanningResult scan(boolean isRemove, FileFilter filter, OnDirScanListener onDirScanListener, FileUtils.OnFileScaned scaned) {
        DirScanningResult result = new DirScanningResult();
        try {
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                long size = FileUtils.getDirLengthWithFilter(file, filter, scaned);
                boolean res = true;
                if (isRemove) {
                    res = FileUtils.deleteFilesInDirWithFilter(file, filter, scaned);
                }
                result.setSuccess(res);
                result.setSize(size);
                if (!res) {
                    result.setException(new Exception("清除失败"));
                }
                if (onDirScanListener != null) {
                    result = onDirScanListener.OnScanCompleted(this, isRemove, result);
                }
                return result;
            } else {
                result.setSuccess(false);
                result.setSize(0);
                result.setException(new Exception("目标是文件"));
                if (onDirScanListener != null) {
                    result = onDirScanListener.OnScanCompleted(this, isRemove, result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
            result.setSize(0);
            result.setException(e);
            if (onDirScanListener != null) {
                result = onDirScanListener.OnScanCompleted(this, isRemove, result);
            }

        }
        return result;

    }


    /**
     * 获取路径下的剩余容量
     *
     * @param path
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static Long getMemerySizeAvalibale(String path) {
        StatFs statfs = new StatFs(path);
        long nTotalBlocks = 0;
        long nBlocSize = 0;
        long nAvailaBlock = 0;
        long free = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // 获取SDCard上BLOCK总数
            nTotalBlocks = statfs.getBlockCountLong();
            // 获取SDCard上每个block的SIZE
            nBlocSize = statfs.getBlockSizeLong();
            // 获取可供程序使用的Block的数量
            nAvailaBlock = statfs.getAvailableBlocksLong();
            // 计算 SDCard 剩余大小MB
            free = nAvailaBlock * nBlocSize;
        } else {
            // 获取SDCard上BLOCK总数
            nTotalBlocks = statfs.getBlockCount();
            // 获取SDCard上每个block的SIZE
            nBlocSize = statfs.getBlockSize();
            // 获取可供程序使用的Block的数量
            nAvailaBlock = statfs.getAvailableBlocks();
            // 计算 SDCard 剩余大小MB
            free = nAvailaBlock * nBlocSize;
        }
        return free;
    }

    /**
     * 获取路径下的总容量
     *
     * @param path
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static Long getMemerySizeTotal(String path) {
        StatFs statfs = new StatFs(path);
        long nTotalBlocks = 0;
        long nBlocSize = 0;
        long nAvailaBlock = 0;
        long free = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // 获取SDCard上BLOCK总数
            nTotalBlocks = statfs.getBlockCountLong();
            // 获取SDCard上每个block的SIZE
            nBlocSize = statfs.getBlockSizeLong();
            // 获取可供程序使用的Block的数量
            nAvailaBlock = statfs.getAvailableBlocksLong();
            // 计算 SDCard 剩余大小MB
            free = nAvailaBlock * nTotalBlocks;
        } else {
            // 获取SDCard上BLOCK总数
            nTotalBlocks = statfs.getBlockCount();
            // 获取SDCard上每个block的SIZE
            nBlocSize = statfs.getBlockSize();
            // 获取可供程序使用的Block的数量
            nAvailaBlock = statfs.getAvailableBlocks();
            // 计算 SDCard 剩余大小MB
            free = nAvailaBlock * nTotalBlocks;
        }
        return free;
    }

    /**
     * 判断手机内部存储是否够用
     *
     * @param len
     * @return
     */
    public static boolean isEnablePhoneData(long len) {
        boolean b = false;
        long free = 0;
        // 获得手机内部存储控件的状态
        File pathFile = Environment.getDataDirectory();
        free = getMemerySizeAvalibale(pathFile.getPath());
        b = free >= len ? true : false;
        //LogUtils.w("手机内部存储剩余容量："+free+"");
        return b;
    }

    /**
     * 判断SD卡是否够用
     *
     * @param len
     * @return
     */
    public static boolean isEnableSDcard(long len) {
        boolean b = false;
        long free = 0;
        //先判断内存卡是否存在，如果存在则判断剩余容量
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File pathFile = Environment.getExternalStorageDirectory();

            free = getMemerySizeAvalibale(pathFile.getPath());
            b = free >= len ? true : false;
            //LogUtils.w("内存卡剩余容量："+free+"");
        }
        return b;
    }

    /**
     * 判断SD卡是否能用
     */
    public static boolean isEnableSDcard() {
        boolean b = false;
        //先判断内存卡是否存在，如果存在则判断剩余容量
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            b = true;
        }

        return b;
    }

    public void getUpNumber(int n) {
        Integer.bitCount(n);
    }
}
