package org.cpnsim.record;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.UserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlRecord {
    Logger LOGGER = LoggerFactory.getLogger(SqlRecord.class.getSimpleName());
    private Connection conn = null;
    private Statement stmt = null;
    private String userRequestTableName = null;
    private String instanceGroupTableName = null;
    private String instanceGroupGraphTableName = null;
    private String instanceTableName = null;
    private String dbName = null;
    private String dbDir = null;
    @Getter
    @Setter
    private String dbPath = null;
    private String sql = null;

    public SqlRecord() {
//        this("./RecordDb", "scaleCloudsimRecord-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".db", "userRequest", "instanceGroup", "instance");
        this("./RecordDb", "cpnSim.db", "userRequest", "instanceGroup", "instanceGroupGraph", "instance");
    }


    public SqlRecord(String dbDir, String dbName, String userRequestTableName, String instanceGroupTableName, String instanceGroupGraphTableName, String instanceTableName) {
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
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public void recordUserRequestSubmitInfo(List<UserRequest> userRequests) {
        for (UserRequest userRequest : userRequests) {
            recordUserRequestSubmitInfo(userRequest);
        }
    }

    public void recordUserRequestSubmitInfo(UserRequest userRequest) {
        try {
            sql = "INSERT INTO " + this.userRequestTableName + " (id,belongDc,submitTime,instanceGroupNum,successInstanceGroupNum) " +
                    "VALUES (" + userRequest.getId() + ", " + userRequest.getBelongDatacenterId() + ", " + userRequest.getSubmitTime() + ", " + userRequest.getInstanceGroups().size() + ", " + "0);";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordUserRequestFinishInfo(UserRequest userRequest) {
        try {
            //设置userRequest的finishTime,state,failReason
            sql = "UPDATE " + this.userRequestTableName + " SET finishTime = " + userRequest.getFinishTime() + ", state = '" + UserRequest.stateToString(userRequest.getState()) + "', failReason = '" + userRequest.getFailReason() + "' WHERE id = " + userRequest.getId() + ";";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordUserRequestFinishInfo(UserRequestRecord userRequest) {
        try {
            //设置userRequest的finishTime,state,failReason
            sql = "UPDATE " + this.userRequestTableName + " SET finishTime = " + userRequest.getFinishTime() + ", state = '" + UserRequest.stateToString(UserRequest.SUCCESS) + "', successInstanceGroupNum = " + userRequest.getSuccessInstanceGroupNum() + " WHERE id = " + userRequest.getId() + ";";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUserRequestSuccessInfo(UserRequestRecord userRequest) {
        try {
            //设置userRequest的finishTime,state,failReason
            sql = "UPDATE " + this.userRequestTableName + " SET successInstanceGroupNum = " + userRequest.getSuccessInstanceGroupNum() + " WHERE id = " + userRequest.getId() + ";";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordInstanceGroupReceivedInfo(List<InstanceGroup> instanceGroups) {
        for (InstanceGroup instanceGroup : instanceGroups) {
            recordInstanceGroupReceivedInfo(instanceGroup);
        }
    }

    public void recordInstanceGroupReceivedInfo(InstanceGroup instanceGroup) {
        try {
            sql = "INSERT INTO " + this.instanceGroupTableName + " (id,userRequestId,retryTimes,receivedDc,receivedTime,instanceNum,successInstanceNum)"
                    + " VALUES (" + instanceGroup.getId() + "," + instanceGroup.getUserRequest().getId() + "," + instanceGroup.getRetryNum() + "," + instanceGroup.getReceiveDatacenter().getId()
                    + "," + instanceGroup.getReceivedTime() + "," + instanceGroup.getInstanceList().size() + "," + 0 + ");";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordInstanceGroupFinishInfo(InstanceGroup instanceGroup) {
        try {
            sql = "UPDATE " + this.instanceGroupTableName + " SET finishTime = " + instanceGroup.getFinishTime() + " WHERE id = " + instanceGroup.getId() + ";";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordInstanceGroupFinishInfo(InstanceGroupRecord instanceGroup) {
        try {
            sql = "UPDATE " + this.instanceGroupTableName + " SET finishTime = " + instanceGroup.getFinishTime() + ", successInstanceNum = " + instanceGroup.getSuccessInstanceNum() + " WHERE id = " + instanceGroup.getId() + ";";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateInstanceGroupSuccessInfo(InstanceGroupRecord instanceGroup) {
        try {
            sql = "UPDATE " + this.instanceGroupTableName + " SET successInstanceNum = " + instanceGroup.getSuccessInstanceNum() + " WHERE id = " + instanceGroup.getId() + ";";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordInstanceGroupGraphAllocateInfo(int srcDcId, int srcInstanceGroupId, int dstDcId, int dstInstanceGroupId, double bw, double startTime) {
        try {
            sql = "INSERT INTO " + this.instanceGroupGraphTableName + " (srcDcId,srcInstanceGroupId,dstDcId,dstInstanceGroupId,bw,startTime) VALUES (" + srcDcId + "," + srcInstanceGroupId + "," + dstDcId + "," + dstInstanceGroupId + "," + bw + "," + startTime + ");";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getAllLinkedFinishGroupId(int instanceGroupId) {
        Map<String, List<Integer>> linkedFinishGroupId = new HashMap<>();
        linkedFinishGroupId.put("src", new ArrayList<>());
        linkedFinishGroupId.put("dst", new ArrayList<>());
        String sql = "SELECT DISTINCT srcInstanceGroupId" +
                " FROM " + this.instanceGroupGraphTableName +
                " WHERE dstInstanceGroupId = " + instanceGroupId + " AND srcInstanceGroupId IN (" +
                " SELECT id" +
                " FROM " + this.instanceGroupTableName +
                " WHERE id = " + this.instanceGroupGraphTableName + ".srcInstanceGroupId AND finishTime IS NOT NULL" +
                ")";
        //执行这个sql语句，并把结果放到linkedFinishGroupId的src中
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                linkedFinishGroupId.get("src").add(rs.getInt("srcInstanceGroupId"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sql = "SELECT DISTINCT dstInstanceGroupId" +
                " FROM " + this.instanceGroupGraphTableName +
                " WHERE srcInstanceGroupId = " + instanceGroupId + " AND dstInstanceGroupId IN (" +
                " SELECT id" +
                " FROM " + this.instanceGroupTableName +
                " WHERE id = " + this.instanceGroupGraphTableName + ".dstInstanceGroupId AND finishTime IS NOT NULL" +
                ")";
        //执行这个sql语句，并把结果放到linkedFinishGroupId的dst中
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                linkedFinishGroupId.get("dst").add(rs.getInt("dstInstanceGroupId"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //打印linkedFinishGroupId
//        System.out.println("linkedFinishGroupId: " + linkedFinishGroupId);
    }

    public void recordInstanceGroupGraphReleaseInfo(int instanceGroupId, double finishTime) {
        getAllLinkedFinishGroupId(instanceGroupId);
        try {
            sql = "UPDATE " + this.instanceGroupGraphTableName +
                    " SET finishTime = " + finishTime +
                    " WHERE (srcInstanceGroupId = " + instanceGroupId +
                    " AND dstInstanceGroupId IN (SELECT id FROM " + this.instanceGroupTableName +
                    " WHERE id = " + this.instanceGroupGraphTableName + ".dstInstanceGroupId AND finishTime IS NOT NULL)) OR (dstInstanceGroupId = " + instanceGroupId +
                    " AND srcInstanceGroupId IN (SELECT id FROM " + this.instanceGroupTableName +
                    " WHERE id = " + this.instanceGroupGraphTableName + ".srcInstanceGroupId AND finishTime IS NOT NULL))";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, Map<Integer, Double>> getReleaseBw(int instanceGroupId) {
        Map<Integer, Map<Integer, Double>> releaseBw = new HashMap<>();
        //sql语句为：SELECT DISTINCT srcInstanceGroupId, dstInstanceGroupId, bw
        //FROM instanceGroupGraph
        //         LEFT JOIN instanceGroup AS src ON instanceGroupGraph.srcInstanceGroupId = src.id AND src.finishTime IS NOT NULL
        //         LEFT JOIN instanceGroup AS dst ON instanceGroupGraph.dstInstanceGroupId = dst.id AND dst.finishTime IS NOT NULL
        //WHERE (srcInstanceGroupId = instanceGroupId AND dst.finishTime IS NOT NULL)
        //   OR (dstInstanceGroupId = instanceGroupId AND src.finishTime IS NOT NULL)
        sql = "SELECT srcInstanceGroupId, dstInstanceGroupId, srcDcId, dstDcId, bw" +
                " FROM " + this.instanceGroupGraphTableName +
                " LEFT JOIN " + this.instanceGroupTableName + " AS src ON " + this.instanceGroupGraphTableName + ".srcInstanceGroupId = src.id AND src.finishTime IS NOT NULL" +
                " LEFT JOIN " + this.instanceGroupTableName + " AS dst ON " + this.instanceGroupGraphTableName + ".dstInstanceGroupId = dst.id AND dst.finishTime IS NOT NULL" +
                " WHERE (" + this.instanceGroupGraphTableName + ".srcInstanceGroupId = " + instanceGroupId + " AND dst.finishTime IS NOT NULL)" +
                " OR (" + this.instanceGroupGraphTableName + ".dstInstanceGroupId = " + instanceGroupId + " AND src.finishTime IS NOT NULL)";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int srcInstanceGroupId = rs.getInt("srcDcId");
                int dstInstanceGroupId = rs.getInt("dstDcId");
                double bw = rs.getDouble("bw");
                if (!releaseBw.containsKey(srcInstanceGroupId)) {
                    releaseBw.put(srcInstanceGroupId, new HashMap<>());
                }
                if (releaseBw.get(srcInstanceGroupId).containsKey(dstInstanceGroupId)) {
                    bw += releaseBw.get(srcInstanceGroupId).get(dstInstanceGroupId);
                }
                releaseBw.get(srcInstanceGroupId).put(dstInstanceGroupId, bw);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return releaseBw;
    }

    public void recordInstanceGroupAllInfo(InstanceGroup instanceGroup) {
        try {
            sql = "INSERT INTO " + this.instanceGroupTableName + " (id,userRequestId,retryTimes,receivedDc,receivedTime,finishTime,instanceNum,successInstanceNum) VALUES ("
                    + instanceGroup.getId() + "," + instanceGroup.getUserRequest().getId() + "," + instanceGroup.getRetryNum() + ","
                    + instanceGroup.getReceiveDatacenter().getId() + "," + instanceGroup.getReceivedTime() + ","
                    + instanceGroup.getFinishTime() + "," +
                    +instanceGroup.getInstanceList().size() + "," +
                    +0 + ");";
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

    public void recordInstanceFinishInfo(Instance instance) {
        try {
            sql = "UPDATE " + this.instanceTableName + " SET finishTime = " + instance.getFinishTime() + " WHERE id = " + instance.getId() + ";";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordInstanceFinishInfo(InstanceRecord instance) {
        try {
            sql = "UPDATE " + this.instanceTableName + " SET finishTime = " + instance.getFinishTime() + " WHERE id = " + instance.getId() + ";";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordInstanceAllInfo(Instance instance) {
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

    public UserRequestRecord getUserRequestRecord(int userRequestId) {
        String sql = "SELECT * FROM " + this.userRequestTableName + " WHERE id = " + userRequestId + ";";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int id = rs.getInt("id");
                int instanceGroupNum = rs.getInt("instanceGroupNum");
                int successInstanceGroupNum = rs.getInt("successInstanceGroupNum");
                return new UserRequestRecord(id, instanceGroupNum, successInstanceGroupNum);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public InstanceRecord getInstanceRecord(int instanceId) {
        String sql = "SELECT * FROM " + this.instanceTableName + " WHERE id = " + instanceId + ";";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int id = rs.getInt("id");
                int instanceGroupId = rs.getInt("instanceGroupId");
                int userRequestId = rs.getInt("userRequestId");
                int cpu = rs.getInt("cpu");
                int ram = rs.getInt("ram");
                int storage = rs.getInt("storage");
                int bw = rs.getInt("bw");
                int lifeTime = rs.getInt("lifeTime");
                int datacenter = rs.getInt("datacenter");
                int host = rs.getInt("host");
                double startTime = rs.getDouble("startTime");
                return new InstanceRecord(id, instanceGroupId, userRequestId, cpu, ram, storage, bw, datacenter, host, lifeTime, startTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public InstanceGroupRecord getInstanceGroupRecord(int instanceGroupId) {
        String sql = "SELECT * FROM " + this.instanceGroupTableName + " WHERE id = " + instanceGroupId + ";";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int id = rs.getInt("id");
                int userRequestId = rs.getInt("userRequestId");
//                int retryTimes = rs.getInt("retryTimes");
//                int receivedDc = rs.getInt("receivedDc");
//                double receivedTime = rs.getDouble("receivedTime");
//                double finishTime = rs.getDouble("finishTime");
                int instanceNum = rs.getInt("instanceNum");
                int successInstanceNum = rs.getInt("successInstanceNum");
                return new InstanceGroupRecord(id, userRequestId, instanceNum, successInstanceNum);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        try {
            stmt.close();
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                " successInstanceGroupNum INT NOT NULL, " +
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
                " successInstanceNum INT NOT NULL, " +
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
}
