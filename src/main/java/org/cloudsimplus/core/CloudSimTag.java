package org.cloudsimplus.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CloudSimTag {
    public static final int NONE = -1;

    /**
     * Starting constant value for cloud-related tags.
     */
    private static final int BASE = 0;
    public static final int DC_REGISTRATION_REQUEST = BASE + 1;
    public static final int DC_LIST_REQUEST = BASE + 2;
    public static final int USER_REQUEST_SEND = BASE + 3;//需要dc处理接收到的用户请求
    public static final int NEED_SEND_USER_REQUEST = BASE + 4;//USER发送用户请求给dc
    public static final int INTER_SCHEDULE = BASE + 5;//USER发送用户请求,需要比USER_REQUEST_SEND小
    public static final int ASK_DC_REVIVE_GROUP = BASE + 6;
    public static final int RESPOND_DC_REVIVE_GROUP_ACCEPT = BASE + 7;
    public static final int RESPOND_DC_REVIVE_GROUP_REJECT = BASE + 8;
    public static final int RESPOND_DC_REVIVE_GROUP_GIVE_UP = BASE + 9;
    public static final int RESPOND_DC_REVIVE_GROUP_EMPLOY = BASE + 10;
    public static final int INNER_SCHEDULE = BASE + 11;
    private final int priority;

    //初始化一个值为1,2,3的set。
    public static final Set<Integer> UNIQUE_TAG = Set.of(INNER_SCHEDULE);

    public int priority() {
        return priority;
    }

    CloudSimTag() {
        this.priority = 0;
    }
}
