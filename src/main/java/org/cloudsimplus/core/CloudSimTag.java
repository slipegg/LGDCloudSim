package org.cloudsimplus.core;

public class CloudSimTag{
    public static final int NONE = -1;

    /** Starting constant value for cloud-related tags. */
    private static final int BASE = 0;
    public static final int DC_REGISTRATION_REQUEST=BASE+1;
    public static final int DC_LIST_REQUEST =BASE+2;
    public static final int USER_REQUEST_SEND =BASE+3;//给dc发送的用户请求
    public static final int SEND_USER_REQUEST =BASE+4;//USER发送用户请求
    private final int priority;
    public int priority(){
        return priority;
    }
    CloudSimTag() {
        this.priority = 0;
    }
}
