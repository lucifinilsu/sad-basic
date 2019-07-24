package com.sad.basic.utils.clazz;

import android.content.Context;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Administrator on 2018/9/30 0030.
 */

public class ClassScannerClientBuilder {
    protected boolean instantRunSupport = true;
    protected boolean log = false;
    protected Context context;
    protected ThreadPoolExecutor threadPoolExecutor = ClassScanWorkerThreadPoolExecutor.getInstance();

    public boolean isInstantRunSupport() {
        return instantRunSupport;
    }

    public boolean isLog() {
        return log;
    }

    public Context getContext() {
        return context;
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    protected ClassScannerClientBuilder(Context context){
        this.context=context;
    }

    public ClassScannerClientBuilder instantRunSupport(boolean instantRunSupport) {
        this.instantRunSupport = instantRunSupport;
        return this;
    }

    public ClassScannerClientBuilder openLog(boolean log){
        this.log=log;
        return this;
    }

    public ClassScannerClientBuilder threadPoolExecutor(ThreadPoolExecutor threadPoolExecutor){
        this.threadPoolExecutor=threadPoolExecutor;
        return this;
    }

    public ClassScannerClient build(){
        return new ClassScannerClient(this);
    }
}
