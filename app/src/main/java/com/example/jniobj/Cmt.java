package com.example.jniobj;

public class Cmt {

    public final long nativeObj;
    public long matObj;

    public Cmt(long addr) {
        if (addr == 0)
            throw new java.lang.UnsupportedOperationException("Native object address is NULL");
        nativeObj = addr;
    }


}
