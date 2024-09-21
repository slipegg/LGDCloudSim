package org.lgdcloudsim.record;

import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.UserRequest;

import java.util.List;
import java.util.Map;

/**
 * SqlRecord is an interface for the record of the simulation through the SQLite database.
 * It records the information of the user requests, the instance groups, the instance group graph, the instances and the conflicts.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface SqlRecord {
    /**
     * Get the path of the SQLite database.
     *
     * @return the path of the SQLite database.
     */
    String getDbPath();

    /**
     * Set the path of the SQLite database.
     * @param dbPath the path of the SQLite database.
     */
    void setDbPath(String dbPath);

    /**
     * Record the user requests submit information.
     * It records the id, the data center id where the user request belongs, the submit time and the number of the instance groups.
     *
     * @param userRequests the user requests.
     */
    void recordUserRequestsSubmitInfo(List<UserRequest> userRequests);

    /**
     * Record the user request finish information.
     * It records the finish time, the state and the fail reason of the user request.
     * @param userRequest the user request.
     */
    void recordUserRequestFinishInfo(UserRequest userRequest);

    /**
     * Record the user request all information.
     * It records the id, the user request id, the retry times,
     * the data center id where the user request is received,
     * the receiving time and the number of the instances.
     * @param requests the user requests.
     */
    void recordInstanceGroupsReceivedInfo(List requests);

    /**
     * Record the instance group submit information.
     * It records the finish time.
     * @param instanceGroup the instance group.
     */
    void recordInstanceGroupFinishInfo(InstanceGroup instanceGroup);

    /**
     * Record the instance group all information.
     * It records the instance group id, the id of the user request where the instance group belongs,
     * the retry times, the data center id where the instance group is received,
     * the receiving time, the finish time and the number of the instances.
     * @param instanceGroup the instance group.
     */
    void recordInstanceGroupAllInfo(InstanceGroup instanceGroup);

    /**
     * It records the instance group graph allocate information.
     * It records the source data center id, the source instance group id, the destination data center id,
     * the destination instance group id, the bandwidth and the start time.
     * @param srcDcId the source data center id.
     * @param srcInstanceGroupId the source instance group id.
     * @param dstDcId the destination data center id.
     * @param dstInstanceGroupId the destination instance group id.
     * @param bw the bandwidth.
     * @param startTime the start time.
     */
    void recordInstanceGroupGraphAllocateInfo(int srcDcId, int srcInstanceGroupId, int dstDcId, int dstInstanceGroupId, double bw, double startTime);

    /**
     * Record the instance group graph release information.
     * It records the source data center id, the source instance group id, the destination data center id,
     * the destination instance group id, the bandwidth and the start time.
     * @param instanceGroups the instance groups.
     */
    void recordInstanceGroupsGraph(List<InstanceGroup> instanceGroups);

    /**
     * Record the instance group graph release information.
     * It records the source data center id, the source instance group id, the destination data center id,
     * the destination instance group id, the bandwidth and the start time.
     * @param srcInstanceGroupId the source instance group id.
     * @param dstInstanceGroupId the destination instance group id.
     */
    void recordInstanceGroupGraphReleaseInfoForFailedUserRequest(int srcInstanceGroupId, int dstInstanceGroupId);

    /**
     * Record the instance group graph release information.
     * It records the finish time.
     * @param srcInstanceGroupId the source instance group id.
     * @param dstInstanceGroupId the destination instance group id.
     * @param finishTime the finish time.
     */
    void recordInstanceGroupGraphReleaseInfo(int srcInstanceGroupId, int dstInstanceGroupId, double finishTime);

    /**
     * Record the instances create information.
     * It records the instance id, the id of the instance group where the instance belongs,
     * the id of the user request where the instance belongs, the cpu, the ram, the storage, the bandwidth,
     * the lifecycle, the retry times, the data center id where the instance is created,
     * the host id where the instance is created and the start time.
     * @param instances the instances.
     */
    void recordInstancesCreateInfo(Map<Integer, List<Instance>> instances);

    /**
     * Record the instances finish information.
     * It records the instance id, the id of the instance group where the instance belongs,
     * the id of the user request where the instance belongs, the cpu, the ram, the storage, the bandwidth,
     * the lifecycle, the retry times, the data center id where the instance is created,
     * the host id where the instance is created and the start time.
     * @param instanceGroups the instance groups.
     */
    void recordInstancesCreateInfo(List<InstanceGroup> instanceGroups);

    /**
     * Record the instances finish information.
     * It records the finish time.
     * @param instances the instances.
     */
    void recordInstancesFinishInfo(List<Instance> instances);

    /**
     * Record the instances all information.
     * It records the instance id, the id of the instance group where the instance belongs,
     * the id of the user request where the instance belongs, the cpu, the ram, the storage, the bandwidth,
     * the lifecycle, the retry times, the data center id where the instance is created,
     * the host id where the instance is created, the start time and the finish time.
     * @param instances the instances.
     */
    void recordInstancesAllInfo(List<Instance> instances);

    /**
     * Record the conflict information.
     * @param time the time.
     * @param sum the sum of the conflicts.
     */
    void recordConflict(double time, int sum);

    /**
     * Record the data center information.
     * It records the data center id, the name, the location, the cpu, the ram, the storage, the bandwidth and the cost.
     * @param datacenters the data centers.
     */
    void recordDatacentersInfo(List<Datacenter> datacenters);

    /**
     * Record the data center information.
     * It records the data center id, the name, the location, the cpu, the ram, the storage, the bandwidth and the cost.
     * @param datacenter the data center.
     */
    void recordDatacentersInfo(Datacenter datacenter);

    /**
     * Record the network between DCs.
     * It records the source data center id, the destination data center id, the bandwidth and the cost.
     * @param networkTopology the network topology.
     */
    void recordDcNetworkInfo(NetworkTopology networkTopology);

    /**
     * Record the network between DCs.
     * It records the source data center id, the destination data center id, the bandwidth and the cost.
     * @param srcDcId the source data center id.
     * @param dstDcId the destination data center id.
     * @param bw the bandwidth.
     * @param unitPrice the unit price.
     */
    void recordDcNetworkInfo(Integer srcDcId, Integer dstDcId, double bw, double unitPrice);

    /**
     * Close the SQLite database.
     */
    void close();
}
