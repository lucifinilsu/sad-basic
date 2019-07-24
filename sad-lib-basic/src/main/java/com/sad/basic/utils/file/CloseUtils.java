package com.sad.basic.utils.file;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by LucifinilSu on 2018/3/20 0020.
 */


public final class CloseUtils {

    private CloseUtils() {
        throw new UnsupportedOperationException(getClass().getSimpleName()+"为工具类，请不要实例化此类型");
    }

    /**
     * Close the io stream.
     *
     * @param closeables closeables
     */
    public static void closeIO(final Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Close the io stream quietly.
     *
     * @param closeables closeables
     */
    public static void closeIOQuietly(final Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}

