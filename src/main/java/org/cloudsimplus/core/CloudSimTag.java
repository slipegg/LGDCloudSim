package org.cloudsimplus.core;

public class CloudSimTag {
    public static final int NONE = -1;

    /**
     * Starting constant value for cloud-related tags.
     */
    private static final int BASE = 0;
    public static final int DC_REGISTRATION_REQUEST = BASE + 1;
    public static final int DC_LIST_REQUEST = BASE + 2;
    public static final int USER_REQUEST_SEND = BASE + 3;//需要dc处理接收到的用户请求
    public static final int SEND_USER_REQUEST = BASE + 4;//USER发送用户请求给dc
    public static final int INTER_SCHEDULE = BASE + 5;//USER发送用户请求,需要比USER_REQUEST_SEND小
    public static final int ASK_DC_REVIVE_GROUP = BASE + 6;
    private final int priority;

    public int priority() {
        return priority;
    }

    CloudSimTag() {
        this.priority = 0;
    }
}
