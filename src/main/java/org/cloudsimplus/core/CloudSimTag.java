package org.cloudsimplus.core;

import java.util.Set;

public class CloudSimTag {
    public static final int NONE = -99;

    /**
     * Starting constant value for cloud-related tags.
     */
    private static final int BASE = 0;
    public static final int CHANGE_COLLABORATION_SYN = BASE - 4;
    public static final int END_INSTANCE_RUN = -3;
    public static final int SYN_STATE = BASE - 2;
    public static final int USER_REQUEST_FAIL = BASE - 1;
    public static final int DC_REGISTRATION_REQUEST = BASE + 1;
    public static final int DC_LIST_REQUEST = BASE + 2;
    public static final int USER_REQUEST_SEND = BASE + 3;//需要dc处理接收到的用户请求
    public static final int NEED_SEND_USER_REQUEST = BASE + 4;//USER发送用户请求给dc
    public static final int GROUP_FILTER_DC_BEGIN = BASE + 5;//USER发送用户请求,需要比USER_REQUEST_SEND小
    public static final int GROUP_FILTER_DC_END = GROUP_FILTER_DC_BEGIN + 1;//USER发送用户请求,需要比USER_REQUEST_SEND小
    public static final int ASK_DC_REVIVE_GROUP = GROUP_FILTER_DC_END + 1;
    public static final int RESPOND_DC_REVIVE_GROUP = ASK_DC_REVIVE_GROUP + 1;
    public static final int RESPOND_DC_REVIVE_GROUP_GIVE_UP = RESPOND_DC_REVIVE_GROUP + 1;
    public static final int RESPOND_DC_REVIVE_GROUP_EMPLOY = RESPOND_DC_REVIVE_GROUP_GIVE_UP + 1;
    public static final int ASK_SIMPLE_STATE = RESPOND_DC_REVIVE_GROUP_EMPLOY + 1;//需要在RESPOND_DC_REVIVE_GROUP_EMPLOY之后，
    // 因为数据中心间调度器在调度了一批亲和组后如果接着调度就又可能会询问该数据中心的粗粒度状态，
    // 而这返回的粗粒度状态应该是在这一批调度的亲和组到达数据中心后，
    // 实现的方法是设置动态网络在同一时刻的两地是相同的延迟，然后通过设置tag的优先级再来实现RESPOND_DC_REVIVE_GROUP_EMPLOY抢先处理
    public static final int RESPOND_SIMPLE_STATE = ASK_SIMPLE_STATE + 1;
    public static final int LOAD_BALANCE_SEND = RESPOND_SIMPLE_STATE + 1;
    public static final int INNER_SCHEDULE_END = LOAD_BALANCE_SEND + 1;
    public static final int ALLOCATE_RESOURCE = INNER_SCHEDULE_END + 1;
    public static final int PRE_ALLOCATE_RESOURCE = ALLOCATE_RESOURCE + 1;//需要在ALLOCATE_RESOURCE之后
    public static final int INNER_SCHEDULE_BEGIN = PRE_ALLOCATE_RESOURCE + 1;//需要在ALLOCATE_RESOURCE之后,因为单调器在决策开始和结束期间不应该有资源在变化

    public static final Set<Integer> UNIQUE_TAG = Set.of(LOAD_BALANCE_SEND, PRE_ALLOCATE_RESOURCE);

    public static String tagToString(int tag) {
        return switch (tag) {
            case CHANGE_COLLABORATION_SYN -> "CHANGE_COLLABORATION_SYN";
            case SYN_STATE -> "SYN_STATE";
            case USER_REQUEST_FAIL -> "USER_REQUEST_FAIL";
            case DC_REGISTRATION_REQUEST -> "DC_REGISTRATION_REQUEST";
            case DC_LIST_REQUEST -> "DC_LIST_REQUEST";
            case USER_REQUEST_SEND -> "USER_REQUEST_SEND";
            case NEED_SEND_USER_REQUEST -> "NEED_SEND_USER_REQUEST";
            case GROUP_FILTER_DC_BEGIN -> "GROUP_FILTER_DC_BEGIN";
            case ASK_SIMPLE_STATE -> "ASK_SIMPLE_STATE";
            case RESPOND_SIMPLE_STATE -> "RESPOND_SIMPLE_STATE";
            case GROUP_FILTER_DC_END -> "GROUP_FILTER_DC_END";
            case ASK_DC_REVIVE_GROUP -> "ASK_DC_REVIVE_GROUP";
            case RESPOND_DC_REVIVE_GROUP -> "RESPOND_DC_REVIVE_GROUP_ACCEPT";
            case RESPOND_DC_REVIVE_GROUP_GIVE_UP -> "RESPOND_DC_REVIVE_GROUP_GIVE_UP";
            case RESPOND_DC_REVIVE_GROUP_EMPLOY -> "RESPOND_DC_REVIVE_GROUP_EMPLOY";
            case LOAD_BALANCE_SEND -> "LOAD_BALANCE_SEND";
            case INNER_SCHEDULE_BEGIN -> "INNER_SCHEDULE_BEGIN";
            case INNER_SCHEDULE_END -> "INNER_SCHEDULE_END";
            case PRE_ALLOCATE_RESOURCE -> "PRE_ALLOCATE_RESOURCE";
            case ALLOCATE_RESOURCE -> "ALLOCATE_RESOURCE";
            case END_INSTANCE_RUN -> "END_INSTANCE_RUN";
            case NONE -> "NONE";
            case BASE -> "Base";
            default -> "UNKNOWN";
        };
    }
}
