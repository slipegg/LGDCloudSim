package org.cpnsim.interscheduler;

import org.cloudsimplus.core.DatacenterEntity;
import org.cloudsimplus.core.Nameable;
import org.cloudsimplus.core.Simulation;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.statemanager.SimpleState;

import java.util.List;
import java.util.Map;

//进行域间调度
/*
 * 0.得到同一个协作区的所有数据中心
 * 1.根据接入时延要求得到可调度的数据中心
 * 2.根据个数据中心的资源抽样信息得到可调度的数据中心
 * 3.在可调度的数据中心中根据网络时延和宽带情况，每个个亲和组都可能会得到多个的调度方案
 * 4.将亲和组发送给调度方案中的各个数据中心进行询问
 * 4.如果没有可以的调度方案就当做失败将其返回给亲和组队列等待下次调度
 * 5.各个数据中心接收亲和组调度请求，并进行决策，决定是否接收该亲和组，如果决定接收就为其预留资源，并返回结果信息
 * 6.原数据中心接收发送出去的各个亲和组的调度结果，进行最终决策，为其决定指定的数据中心，并发送信息释放其他数据中心的资源
 * 7.各个数据中心如果接收到释放资源的消息，就释放资源，如果接收到确认亲和组放置的信息就将其实例放入到域内实例调度请求队列中
 */
public interface InterScheduler extends Nameable, DatacenterEntity {
    Map<InstanceGroup, List<Datacenter>> filterSuitableDatacenter(List<InstanceGroup> instanceGroups);

    double getFilterSuitableDatacenterCostTime();

    Map<InstanceGroup, Double> decideReciveGroupResult(List<InstanceGroup> instanceGroups);

    double getDecideReciveGroupResultCostTime();

    Map<InstanceGroup, Datacenter> decideTargetDatacenter(Map<InstanceGroup, Map<Datacenter, Double>> instanceGroupSendResultMap, List<InstanceGroup> instanceGroups);

    double getDecideTargetDatacenterCostTime();

    void receiveNotEmployGroup(List<InstanceGroup> instanceGroups);

    void receiveEmployGroup(List<InstanceGroup> instanceGroups);

    boolean isDirectedSend();

    InterScheduler setDirectedSend(boolean directedSend);

    InterScheduler setSimulation(Simulation simulation);

    int getCollaborationId();

    InterScheduler setCollaborationId(int collaborationId);

    Map<Datacenter, SimpleState> getInterScheduleSimpleStateMap();
}
