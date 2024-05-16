package org.lgdcloudsim.record;

import lombok.Getter;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.InstanceGroupEdge;
import org.lgdcloudsim.request.UserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * It is an implementation of the SqlRecord interface.
 * It records the simulation information through the SQLite database.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class SqlRecordSimple implements SqlRecord {
    /**
     * The Logger of the class.
     **/
    Logger LOGGER = LoggerFactory.getLogger(SqlRecordSimple.class.getSimpleName());

    /**
     * The connection of the SQLite database.
     */
    private Connection conn = null;

    /**
     * The statement of the SQLite database.
     */
    private Statement stmt = null;

    /**
     * The name of the user request table.
     */
    private String userRequestTableName = null;

    /**
     * The name of the instance group table.
     */
    private String instanceGroupTableName = null;

    /**
     * The name of the instance group graph table.
     */
    private String instanceGroupGraphTableName = null;

    /**
     * The name of the instance table.
     */
    private String instanceTableName = null;

    /**
     * The name of the conflict table.
     */
    private String conflictTableName = null;

    /**
     * The name of the inter schedule cost time table.
     */
    private String interScheduleCostTimeTableName = null;

    /**
     * The name of the SQLite database.
     */
    private String dbName = null;

    /**
     * The directory of the SQLite database.
     */
    private String dbDir = null;

    /**
     * The path of the SQLite database.
     */
    @Getter
    private String dbPath = null;

    /**
     * The SQL statement.
     */
    private String sql = null;

    /**
     * The prepared statement.
     */
    private PreparedStatement statement;

    /**
     * The last record conflict time.
     */
    private double lastRecordConflictTime = -1;

    /**
     * Construct a SqlRecordSimple with the default database name "LGDCloudSim.db".
     */
    public SqlRecordSimple() {
        this("LGDCloudSim.db");
    }

    /**
     * Construct a SqlRecordSimple with the given database name.
     *
     * @param dbName the name of the SQLite database
     */
    public SqlRecordSimple(String dbName) {
        this("./RecordDb", dbName, "userRequest", "instanceGroup", "instanceGroupGraph", "instance");
    }

    /**
     * Construct a SqlRecordSimple with the given database name, user request table name, instance group table name, instance group graph table name and instance table name.
     *
     * @param dbDir                       the directory of the SQLite database
     * @param dbName                      the name of the SQLite database
     * @param userRequestTableName        the name of the user request table
     * @param instanceGroupTableName      the name of the instance group table
     * @param instanceGroupGraphTableName the name of the instance group graph table
     * @param instanceTableName           the name of the instance table
     */
    public SqlRecordSimple(String dbDir, String dbName, String userRequestTableName, String instanceGroupTableName, String instanceGroupGraphTableName, String instanceTableName) {
        this.dbDir = dbDir;
        this.dbName = dbName;
        Path folder = Paths.get(this.dbDir);
        File dir = new File(this.dbDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        Path file = Paths.get(this.dbName);
        this.dbPath = folder.resolve(file).toString();
        this.userRequestTableName = userRequestTableName;
        this.instanceGroupTableName = instanceGroupTableName;
        this.instanceGroupGraphTableName = instanceGroupGraphTableName;
        this.instanceTableName = instanceTableName;
        this.conflictTableName = "conflict";
        this.interScheduleCostTimeTableName = "interScheduleCostTime";
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + this.dbPath);
            conn.setAutoCommit(false);
            LOGGER.info("Opened " + this.dbPath + " successfully");
            stmt = conn.createStatement();

            createUserRequestTable();
            createInstanceGroupTable();
            createGroupGraphTable();
            createInstanceTable();
            createConflictTable();
            createInterScheduleCostTimeTable();
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Set the path of the SQLite database.
     * @param dbPath the path of the SQLite database.
     */
    @Override
    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void recordUserRequestsSubmitInfo(List<UserRequest> userRequests) {
        try {
            statement = conn.prepareStatement("INSERT INTO " + this.userRequestTableName + " (id,belongDc,submitTime,instanceGroupNum) VALUES (?,?,?,?);");
            for (UserRequest userRequest : userRequests) {
                statement.setInt(1, userRequest.getId());
                statement.setInt(2, userRequest.getBelongDatacenterId());
                statement.setDouble(3, userRequest.getSubmitTime());
                statement.setInt(4, userRequest.getInstanceGroups().size());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recordUserRequestFinishInfo(UserRequest userRequest) {
        try {
            //设置userRequest的finishTime,state,failReason
            sql = "UPDATE " + this.userRequestTableName + " SET finishTime = " + userRequest.getFinishTime() + ", state = '" + UserRequest.stateToString(userRequest.getState()) + "', failReason = '" + userRequest.getFailReason() + "' WHERE id = " + userRequest.getId() + ";";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recordInstanceGroupsReceivedInfo(List requests) {
        if (!requests.isEmpty()) {
            try {
                if (requests.get(0) instanceof InstanceGroup) {
                    List<InstanceGroup> instanceGroups = requests;
                    statement = conn.prepareStatement("INSERT INTO " + this.instanceGroupTableName + " (id,userRequestId,retryTimes,receivedDc,receivedTime,instanceNum) VALUES (?,?,?,?,?,?);");
                    for (InstanceGroup instanceGroup : instanceGroups) {
                        addBatchInStatementForRecordInstanceGroupsReceivedInfo(instanceGroup);
                    }
                    statement.executeBatch();
                }
                if (requests.get(0) instanceof UserRequest) {
                    List<UserRequest> userRequests = requests;
                    statement = conn.prepareStatement("INSERT INTO " + this.instanceGroupTableName + " (id,userRequestId,retryTimes,receivedDc,receivedTime,instanceNum) VALUES (?,?,?,?,?,?);");
                    for (UserRequest userRequest : userRequests) {
                        for (InstanceGroup instanceGroup : userRequest.getInstanceGroups()) {
                            addBatchInStatementForRecordInstanceGroupsReceivedInfo(instanceGroup);
                        }
                    }
                    statement.executeBatch();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Add a batch in the statement for recording the instance groups received information.
     * @param instanceGroup the instance group
     * @throws SQLException the SQL exception
     */
    private void addBatchInStatementForRecordInstanceGroupsReceivedInfo(InstanceGroup instanceGroup) throws SQLException {
        statement.setInt(1, instanceGroup.getId());
        statement.setInt(2, instanceGroup.getUserRequest().getId());
        statement.setInt(3, instanceGroup.getRetryNum());
        statement.setInt(4, instanceGroup.getReceiveDatacenter().getId());
        statement.setDouble(5, instanceGroup.getReceivedTime());
        statement.setInt(6, instanceGroup.getInstances().size());
        statement.addBatch();
    }

    @Override
    public void recordInstanceGroupFinishInfo(InstanceGroup instanceGroup) {
        try {
            sql = "UPDATE " + this.instanceGroupTableName + " SET finishTime = " + instanceGroup.getFinishTime() + " WHERE id = " + instanceGroup.getId() + ";";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recordInstanceGroupGraphAllocateInfo(int srcDcId, int srcInstanceGroupId, int dstDcId, int dstInstanceGroupId, double bw, double startTime) {
        try {
            sql = "INSERT INTO " + this.instanceGroupGraphTableName + " (srcDcId,srcInstanceGroupId,dstDcId,dstInstanceGroupId,bw,startTime) VALUES (" + srcDcId + "," + srcInstanceGroupId + "," + dstDcId + "," + dstInstanceGroupId + "," + bw + "," + startTime + ");";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Record the instance group graph allocate information.
     * @param instanceGroups the instance groups.
     */
    public void recordInstanceGroupsGraph(List<InstanceGroup> instanceGroups) {
        try {
            Set<InstanceGroupEdge> recordEdges = new HashSet<>();
            statement = conn.prepareStatement("INSERT INTO " + this.instanceGroupGraphTableName + " (srcDcId,srcInstanceGroupId,dstDcId,dstInstanceGroupId,bw,startTime) VALUES (?,?,?,?,?,?);");

            for (InstanceGroup instanceGroup : instanceGroups) {
                List<InstanceGroup> dstInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getDstList(instanceGroup);
                for (InstanceGroup dst : dstInstanceGroups) {
                    InstanceGroupEdge edge = instanceGroup.getUserRequest().getInstanceGroupGraph().getEdge(instanceGroup, dst);
                    if (!recordEdges.contains(edge)) {
                        addEdgeToStatement(edge, instanceGroup.getReceivedTime());
                        recordEdges.add(edge);
                    }
                }

                List<InstanceGroup> srcInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getSrcList(instanceGroup);
                for (InstanceGroup src : srcInstanceGroups) {
                    InstanceGroupEdge edge = instanceGroup.getUserRequest().getInstanceGroupGraph().getEdge(src, instanceGroup);
                    if (!recordEdges.contains(edge)) {
                        addEdgeToStatement(edge, instanceGroup.getReceivedTime());
                        recordEdges.add(edge);
                    }
                }
            }

            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addEdgeToStatement(InstanceGroupEdge instanceGroupEdge, double clock) throws SQLException {
        if (instanceGroupEdge.getSrc().getReceiveDatacenter() != Datacenter.NULL && instanceGroupEdge.getDst().getReceiveDatacenter() != Datacenter.NULL) {
            statement.setInt(1, instanceGroupEdge.getSrc().getReceiveDatacenter().getId());
            statement.setInt(2, instanceGroupEdge.getSrc().getId());
            statement.setInt(3, instanceGroupEdge.getDst().getReceiveDatacenter().getId());
            statement.setInt(4, instanceGroupEdge.getDst().getId());
            statement.setDouble(5, instanceGroupEdge.getRequiredBw());
            statement.setDouble(6, clock);
            statement.addBatch();
        }
    }

    @Override
    public void recordInstanceGroupGraphReleaseInfoForFailedUserRequest(int srcInstanceGroupId, int dstInstanceGroupId) {
        try {
            sql = "DELETE FROM " + this.instanceGroupGraphTableName +
                    " WHERE (srcInstanceGroupId = " + srcInstanceGroupId +
                    " AND dstInstanceGroupId = " + dstInstanceGroupId + ");";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recordInstanceGroupGraphReleaseInfo(int srcInstanceGroupId, int dstInstanceGroupId, double finishTime) {
        try {
            sql = "UPDATE " + this.instanceGroupGraphTableName +
                    " SET finishTime = " + finishTime +
                    " WHERE (srcInstanceGroupId = " + srcInstanceGroupId +
                    " AND dstInstanceGroupId = " + dstInstanceGroupId + ");";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recordInstanceGroupAllInfo(InstanceGroup instanceGroup) {
        try {
            sql = "INSERT INTO " + this.instanceGroupTableName + " (id,userRequestId,retryTimes,receivedDc,receivedTime,finishTime,instanceNum) VALUES ("
                    + instanceGroup.getId() + "," + instanceGroup.getUserRequest().getId() + "," + instanceGroup.getRetryNum() + ","
                    + instanceGroup.getReceiveDatacenter().getId() + "," + instanceGroup.getReceivedTime() + ","
                    + instanceGroup.getFinishTime() + ","
                    + instanceGroup.getInstances().size()
                    + ");";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordInstanceCreateInfo(Instance instance) {
        try {
            sql = "INSERT INTO " + this.instanceTableName +
                    " (id,instanceGroupId,userRequestId,cpu,ram,storage,bw,lifeTime,retryTimes,datacenter,host,startTime) VALUES ("
                    + instance.getId() + "," + instance.getInstanceGroup().getId() + "," + instance.getUserRequest().getId() + ","
                    + instance.getCpu() + "," + instance.getRam() + "," + instance.getStorage() + "," + instance.getBw() + ","
                    + instance.getLifecycle() + "," + instance.getRetryNum() + "," + instance.getInstanceGroup().getReceiveDatacenter().getId() + "," + instance.getHost() + ","
                    + instance.getStartTime() + ");";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recordInstancesCreateInfo(Map<Integer, List<Instance>> instances) {
        try {
            statement = conn.prepareStatement("INSERT INTO " + this.instanceTableName +
                    "(id, instanceGroupId, userRequestId, cpu, ram, storage, bw, lifeTime, retryTimes, datacenter, host, startTime) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            //遍历instances的value
            for (List<Instance> instanceList : instances.values()) {
                for (Instance instance : instanceList) {
                    if (instance.getState() == UserRequest.RUNNING) {
                        statement.setInt(1, instance.getId());
                        statement.setInt(2, instance.getInstanceGroup().getId());
                        statement.setInt(3, instance.getUserRequest().getId());
                        statement.setDouble(4, instance.getCpu());
                        statement.setDouble(5, instance.getRam());
                        statement.setDouble(6, instance.getStorage());
                        statement.setDouble(7, instance.getBw());
                        statement.setDouble(8, instance.getLifecycle());
                        statement.setInt(9, instance.getRetryNum());
                        statement.setInt(10, instance.getInstanceGroup().getReceiveDatacenter().getId());
                        statement.setInt(11, instance.getHost());
                        statement.setDouble(12, instance.getStartTime());
                        statement.addBatch();
                    }
                }
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recordInstancesCreateInfo(List<InstanceGroup> instanceGroups) {
        try {
            statement = conn.prepareStatement("INSERT INTO " + this.instanceTableName +
                    "(id, instanceGroupId, userRequestId, cpu, ram, storage, bw, lifeTime, retryTimes, datacenter, host, startTime) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            //遍历instances的value
            for (InstanceGroup instanceGroup : instanceGroups) {
                for (Instance instance : instanceGroup.getInstances()) {
                    if (instance.getState() == UserRequest.RUNNING) {
                        statement.setInt(1, instance.getId());
                        statement.setInt(2, instance.getInstanceGroup().getId());
                        statement.setInt(3, instance.getUserRequest().getId());
                        statement.setDouble(4, instance.getCpu());
                        statement.setDouble(5, instance.getRam());
                        statement.setDouble(6, instance.getStorage());
                        statement.setDouble(7, instance.getBw());
                        statement.setDouble(8, instance.getLifecycle());
                        statement.setInt(9, instance.getRetryNum());
                        statement.setInt(10, instance.getInstanceGroup().getReceiveDatacenter().getId());
                        statement.setInt(11, instance.getHost());
                        statement.setDouble(12, instance.getStartTime());
                        statement.addBatch();
                    }
                }
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recordInstancesFinishInfo(List<Instance> instances) {
        try {
            statement = conn.prepareStatement("UPDATE " + this.instanceTableName + " SET finishTime = ? WHERE id = ?");
            for (Instance instance : instances) {
                statement.setDouble(1, instance.getFinishTime());
                statement.setInt(2, instance.getId());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recordInstancesAllInfo(List<Instance> instances) {
        try {
            statement = conn.prepareStatement("INSERT INTO " + this.instanceTableName +
                    "(id, instanceGroupId, userRequestId, cpu, ram, storage, bw, lifeTime, retryTimes, datacenter, host, startTime, finishTime) " + "VALUES (?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            for (Instance instance : instances) {
                statement.setInt(1, instance.getId());
                statement.setInt(2, instance.getInstanceGroup().getId());
                statement.setInt(3, instance.getUserRequest().getId());
                statement.setDouble(4, instance.getCpu());
                statement.setDouble(5, instance.getRam());
                statement.setDouble(6, instance.getStorage());
                statement.setDouble(7, instance.getBw());
                statement.setDouble(8, instance.getLifecycle());
                statement.setInt(9, instance.getRetryNum());
                statement.setInt(10, instance.getInstanceGroup().getReceiveDatacenter().getId());
                statement.setInt(11, instance.getHost());
                statement.setDouble(12, instance.getStartTime());
                statement.setDouble(13, instance.getFinishTime());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            stmt.close();
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the user request table.
     * The user request table has the following columns:
     * <ul>
     *     <li>id: int, primary key. It records the id of the user request.</li>
     *     <li>belongDc: int. It records the id of the data center where the user request belongs.</li>
     *     <li>submitTime: double. It records the submit time of the user request.</li>
     *     <li>finishTime: double. It records the finish time of the user request.</li>
     *     <li>instanceGroupNum: int. It records the number of the instance groups of the user request.</li>
     *     <li>state: char(10). It records the state of the user request.</li>
     *     <li>failReason: char(100). It records the fail reason of the user request.</li>
     * </ul>
     * @throws SQLException the SQL exception
     */
    private void createUserRequestTable() throws SQLException {
        sql = "DROP TABLE IF EXISTS " + this.userRequestTableName;
        stmt.executeUpdate(sql);
        sql = "CREATE TABLE IF NOT EXISTS " + this.userRequestTableName + " " +
                "(id INT PRIMARY KEY     NOT NULL," +
                " belongDc INT NOT NULL, " +
                " submitTime DOUBLE NOT NULL, " +
                " finishTime DOUBLE, " +
                " instanceGroupNum INT NOT NULL, " +
                " state CHAR(10) , " +
                " failReason CHAR(100))";
        stmt.executeUpdate(sql);
        sql = "DROP TABLE IF EXISTS " + this.instanceGroupTableName;
        stmt.executeUpdate(sql);
        conn.commit();
    }

    /**
     * Create the instance group table.
     * The instance group table has the following columns:
     * <ul>
     *     <li>id: int, primary key. It records the id of the instance group.</li>
     *     <li>userRequestId: int. It records the id of the user request where the instance group belongs.</li>
     *     <li>retryTimes: int. It records the retry times of the instance group.</li>
     *     <li>receivedDc: int. It records the id of the data center where the instance group is received.</li>
     *     <li>receivedTime: double. It records the receiving time of the instance group.</li>
     *     <li>finishTime: double. It records the finish time of the instance group.</li>
     *     <li>instanceNum: int. It records the number of the instances of the instance group.</li>
     * </ul>
     * @throws SQLException the SQL exception
     */
    private void createInstanceGroupTable() throws SQLException {
        sql = "DROP TABLE IF EXISTS " + this.instanceGroupTableName;
        stmt.executeUpdate(sql);
        sql = "CREATE TABLE IF NOT EXISTS " + this.instanceGroupTableName + " " +
                "(id INT PRIMARY KEY     NOT NULL," +
                " userRequestId INT NOT NULL, " +
                " retryTimes INT NOT NULL, " +
                " receivedDc INT NOT NULL," +
                " receivedTime DOUBLE NOT NULL," +
                " finishTime DOUBLE," +
                " instanceNum INT NOT NULL, " +
                " FOREIGN KEY(userRequestId) REFERENCES " + this.userRequestTableName + "(id))";
        stmt.executeUpdate(sql);
        sql = "DROP TABLE IF EXISTS " + this.instanceTableName;
        stmt.executeUpdate(sql);
        conn.commit();
    }

    /**
     * Create the instance group graph table.
     * The instance group graph table has the following columns:
     * <ul>
     *     <li>srcInstanceGroupId: int. It records the id of the source instance group.</li>
     *     <li>dstInstanceGroupId: int. It records the id of the destination instance group.</li>
     *     <li>srcDcId: int. It records the id of the source data center.</li>
     *     <li>dstDcId: int. It records the id of the destination data center.</li>
     *     <li>bw: double. It records the bandwidth of the instance group edge allocated.</li>
     *     <li>startTime: double. It records the start time of the instance group graph.</li>
     *     <li>finishTime: double. It records the finish time of the instance group graph.</li>
     * </ul>
     * @throws SQLException the SQL exception
     */
    private void createGroupGraphTable() throws SQLException {
        sql = "DROP TABLE IF EXISTS " + this.instanceGroupGraphTableName;
        stmt.executeUpdate(sql);
        sql = "CREATE TABLE IF NOT EXISTS " + this.instanceGroupGraphTableName + " " +
                "(srcInstanceGroupId INT NOT NULL, " +
                " dstInstanceGroupId INT NOT NULL, " +
                " srcDcId INT NOT NULL, " +
                " dstDcId INT NOT NULL, " +
                " bw DOUBLE NOT NULL, " +
                " startTime DOUBLE NOT NULL, " +
                " finishTime DOUBLE, " +
                " PRIMARY KEY (srcInstanceGroupId, dstInstanceGroupId), " +
                " FOREIGN KEY (srcInstanceGroupId) REFERENCES " + this.instanceGroupTableName + "(id), " +
                " FOREIGN KEY (dstInstanceGroupId) REFERENCES " + this.instanceGroupTableName + "(id))";
        stmt.executeUpdate(sql);
        conn.commit();
    }

    /**
     * Create the instance table.
     * The instance table has the following columns:
     * <ul>
     *     <li>id: int, primary key. It records the id of the instance.</li>
     *     <li>instanceGroupId: int. It records the id of the instance group where the instance belongs.</li>
     *     <li>userRequestId: int. It records the id of the user request where the instance belongs.</li>
     *     <li>cpu: int. It records the cpu of the instance.</li>
     *     <li>ram: int. It records the ram of the instance.</li>
     *     <li>storage: int. It records the storage of the instance.</li>
     *     <li>bw: int. It records the bandwidth of the instance.</li>
     *     <li>lifeTime: int. It records the life time of the instance.</li>
     *     <li>retryTimes: int. It records the retry times of the instance.</li>
     *     <li>datacenter: int. It records the id of the data center where the instance is received.</li>
     *     <li>host: int. It records the id of the host where the instance is placed.</li>
     *     <li>startTime: double. It records the start time of the instance.</li>
     *     <li>finishTime: double. It records the finish time of the instance.</li>
     * </ul>
     * @throws SQLException the SQL exception
     */
    private void createInstanceTable() throws SQLException {
        sql = "DROP TABLE IF EXISTS " + this.instanceTableName;
        stmt.executeUpdate(sql);
        sql = "CREATE TABLE IF NOT EXISTS " + this.instanceTableName + " " +
                "(id INT PRIMARY KEY     NOT NULL," +
                " instanceGroupId INT NOT NULL, " +
                " userRequestId INT NOT NULL, " +
                " cpu INT NOT NULL, " +
                " ram INT NOT NULL, " +
                " storage INT NOT NULL, " +
                " bw INT NOT NULL, " +
                " lifeTime INT NOT NULL, " +
                " retryTimes INT NOT NULL, " +
                " datacenter INT NOT NULL, " +
                " host INT NOT NULL, " +
                " startTime DOUBLE NOT NULL, " +
                " finishTime DOUBLE," +
                " FOREIGN KEY(instanceGroupId) REFERENCES " + this.instanceGroupTableName + "(id)," +
                " FOREIGN KEY(userRequestId) REFERENCES " + this.userRequestTableName + "(id))";
        stmt.executeUpdate(sql);
        conn.commit();
    }

    /**
     * Create the conflict table.
     * The conflict table has the following columns:
     * <ul>
     *     <li>time: double, primary key. It records the time.</li>
     *     <li>conflictSum: int. It records the sum of the conflicts at the time.</li>
     * </ul>
     * @throws SQLException
     */
    private void createConflictTable() throws SQLException {
        sql = "DROP TABLE IF EXISTS " + this.conflictTableName;
        stmt.executeUpdate(sql);
        sql = "CREATE TABLE IF NOT EXISTS " + this.conflictTableName + " " +
                "(time DOUBLE PRIMARY KEY NOT NULL," +
                " conflictSum INT NOT NULL) ";
        stmt.executeUpdate(sql);
        conn.commit();
    }


    private void createInterScheduleCostTimeTable() throws SQLException {
        sql = "DROP TABLE IF EXISTS " + this.interScheduleCostTimeTableName;
        stmt.executeUpdate(sql);
        sql = "CREATE TABLE IF NOT EXISTS " + this.interScheduleCostTimeTableName + " " +
                "(time DOUBLE PRIMARY KEY NOT NULL," +
                " costTime DOUBLE NOT NULL, " +
                " traversalTime INT NOT NULL) ";
        stmt.executeUpdate(sql);
        conn.commit();
    }

    @Override
    public void recordConflict(double time, int sum) {
        int tmpTime = (int) time / 10 * 10;
        try {
            if (lastRecordConflictTime != tmpTime) {
                sql = "INSERT INTO " + this.conflictTableName + " (time, conflictSum) VALUES (" + tmpTime + "," + sum + ");";
                stmt.executeUpdate(sql);
                lastRecordConflictTime = tmpTime;
            } else {
                sql = "UPDATE " + this.conflictTableName + " SET conflictSum = conflictSum + " + sum + " WHERE time = " + tmpTime + ";";
                stmt.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recordDatacentersInfo(List<Datacenter> datacenters) {

    }
    
    @Override
    public void recordDatacentersInfo(Datacenter datacenters) {

    }

    @Override
    public void recordDcNetworkInfo(NetworkTopology networkTopology) {

    }

    @Override
    public void recordDcNetworkInfo(Integer srcDcId, Integer dstDcId, double bw, double unitPrice) {

    }
}
