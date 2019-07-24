package com.sad.basic.utils.file;

import java.io.File;
import java.io.Serializable;

/**
 * Created by LucifinilSu on 2018/5/24 0024.
 */

public class DirScanningResult implements Serializable {
    private long size=0;
    private boolean isSuccess=false;
    private File file;
    private Exception exception;

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
