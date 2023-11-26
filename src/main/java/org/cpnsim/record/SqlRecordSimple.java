package org.cpnsim.record;

import lombok.Getter;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.InstanceGroupEdge;
import org.cpnsim.request.UserRequest;
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

public class SqlRecordSimple implements SqlRecord {
    Logger LOGGER = LoggerFactory.getLogger(SqlRecordSimple.class.getSimpleName());
    private Connection conn = null;
    private Statement stmt = null;
    private String userRequestTableName = null;
    private String instanceGroupTableName = null;
    private String instanceGroupGraphTableName = null;
    private String instanceTableName = null;
    private String conflictTableName = null;
    private String dbName = null;
    private String dbDir = null;
    @Getter
    private String dbPath = null;
    private String sql = null;
    private PreparedStatement statement;
    private double lastRecordConflictTime = -1;
    private double instanceDelaySum = 0.0;
    private long instanceNum = 0L;
    private double interScheduleTime = 0.0;

    public SqlRecordSimple() {
//        this("./RecordDb", "scaleCloudsimRecord-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".db", "userRequest", "instanceGroup", "instance");
        this("cpnSim.db");
    }

    public SqlRecordSimple(String dbName) {
        this("./RecordDb", dbName, "userRequest", "instanceGroup", "instanceGroupGraph", "instance");
    }

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
        conflictTableName = "conflict";
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
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    @Override
    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void recordUserRequestsSubmitinfo(List<UserRequest> userRequests) {
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
    public void recordInstanceGroupsReceivedInfo(List<InstanceGroup> instanceGroups) {
        try {
            statement = conn.prepareStatement("INSERT INTO " + this.instanceGroupTableName + " (id,userRequestId,retryTimes,receivedDc,receivedTime,instanceNum) VALUES (?,?,?,?,?,?);");
            for (InstanceGroup instanceGroup : instanceGroups) {
                statement.setInt(1, instanceGroup.getId());
                statement.setInt(2, instanceGroup.getUserRequest().getId());
                statement.setInt(3, instanceGroup.getRetryNum());
                statement.setInt(4, instanceGroup.getReceiveDatacenter().getId());
                statement.setDouble(5, instanceGroup.getReceivedTime());
                statement.setInt(6, instanceGroup.getInstances().size());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                    + instance.getLifeTime() + "," + instance.getRetryNum() + "," + instance.getInstanceGroup().getReceiveDatacenter().getId() + "," + instance.getHost() + ","
                    + instance.getStartTime() + ");";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recordInstancesCreateInfo(Map<Integer, List<Instance>> instances) {
        try {
            statement = conn.prepareStatement("INSERT INTO instance " +
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
                        statement.setDouble(8, instance.getLifeTime());
                        statement.setInt(9, instance.getRetryNum());
                        statement.setInt(10, instance.getInstanceGroup().getReceiveDatacenter().getId());
                        statement.setInt(11, instance.getHost());
                        statement.setDouble(12, instance.getStartTime());
                        statement.addBatch();
                    }
                    instanceDelaySum += instance.getStartTime() - instance.getInstanceGroup().getReceivedTime();
                    instanceNum++;
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
            statement = conn.prepareStatement("INSERT INTO instance " +
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
                        statement.setDouble(8, instance.getLifeTime());
                        statement.setInt(9, instance.getRetryNum());
                        statement.setInt(10, instance.getInstanceGroup().getReceiveDatacenter().getId());
                        statement.setInt(11, instance.getHost());
                        statement.setDouble(12, instance.getStartTime());
                        statement.addBatch();
                    }
                    instanceDelaySum += instance.getStartTime() - instance.getInstanceGroup().getReceivedTime();
                    instanceNum++;
                }
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordInstanceFinishInfo(Instance instance) {
        try {
            sql = "UPDATE " + this.instanceTableName + " SET finishTime = " + instance.getFinishTime() + " WHERE id = " + instance.getId() + ";";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recordInstancesFinishInfo(List<Instance> instances) {
        try {
            statement = conn.prepareStatement("UPDATE instance SET finishTime = ? WHERE id = ?");
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
            statement = conn.prepareStatement("INSERT INTO instance " +
                    "(id, instanceGroupId, userRequestId, cpu, ram, storage, bw, lifeTime, retryTimes, datacenter, host, startTime, finishTime) " + "VALUES (?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            for (Instance instance : instances) {
                statement.setInt(1, instance.getId());
                statement.setInt(2, instance.getInstanceGroup().getId());
                statement.setInt(3, instance.getUserRequest().getId());
                statement.setDouble(4, instance.getCpu());
                statement.setDouble(5, instance.getRam());
                statement.setDouble(6, instance.getStorage());
                statement.setDouble(7, instance.getBw());
                statement.setDouble(8, instance.getLifeTime());
                statement.setInt(9, instance.getRetryNum());
                statement.setInt(10, instance.getInstanceGroup().getReceiveDatacenter().getId());
                statement.setInt(11, instance.getHost());
                statement.setDouble(12, instance.getStartTime());
                statement.setDouble(13, instance.getFinishTime());
                statement.addBatch();
                if (instance.getStartTime() == -1) {
                    instanceDelaySum += instance.getFinishTime() - instance.getInstanceGroup().getReceivedTime();
                    instanceNum++;
                }
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordInstancesAllInfo(Instance instance) {
        try {
            sql = "INSERT INTO " + this.instanceTableName +
                    " (id,instanceGroupId,userRequestId,cpu,ram,storage,bw,lifeTime,retryTimes,datacenter,host,startTime,finishTime) VALUES ("
                    + instance.getId() + "," + instance.getInstanceGroup().getId() + "," + instance.getUserRequest().getId() + ","
                    + instance.getCpu() + "," + instance.getRam() + "," + instance.getStorage() + "," + instance.getBw() + ","
                    + instance.getLifeTime() + "," + instance.getRetryNum() + "," + instance.getInstanceGroup().getReceiveDatacenter().getId() + "," + instance.getHost() + ","
                    + instance.getStartTime() + "," + instance.getFinishTime() + ");";
            stmt.executeUpdate(sql);
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

    @Override
    public void addInterScheduleTime(double interScheduleTime) {
        this.interScheduleTime += interScheduleTime;
    }

    @Override
    public double getInterScheduleTime() {
        return interScheduleTime;
    }

    private void createUserRequestTable() throws SQLException {
        sql = "DROP TABLE IF EXISTS " + this.userRequestTableName;
        stmt.executeUpdate(sql);
        //创建一个这种格式的表id:int,belongDc:int,submitTime:double,finishTime:double,state:char(10),failReason:char(100);
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

    private void createConflictTable() throws SQLException {
        sql = "DROP TABLE IF EXISTS " + this.conflictTableName;
        stmt.executeUpdate(sql);
        sql = "CREATE TABLE IF NOT EXISTS " + this.conflictTableName + " " +
                "(time DOUBLE PRIMARY KEY NOT NULL," +
                " conflictSum INT NOT NULL) ";
        stmt.executeUpdate(sql);
        conn.commit();
    }

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
    public double getAvgInstanceSubmitDelay() {
        System.out.println("instanceDelaySum: " + instanceDelaySum);
        System.out.println("instanceNum: " + instanceNum);
        return instanceDelaySum / instanceNum;
    }
}
