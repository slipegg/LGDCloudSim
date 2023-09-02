package org.cpnsim.record;

import lombok.Getter;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.UserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SqlRecordSimple implements SqlRecord {
    Logger LOGGER = LoggerFactory.getLogger(SqlRecordSimple.class.getSimpleName());
    private Connection conn = null;
    private Statement stmt = null;
    private String userRequestTableName = null;
    private String instanceGroupTableName = null;
    private String instanceGroupGraphTableName = null;
    private String instanceTableName = null;
    private String dbName = null;
    private String dbDir = null;
    @Getter
    private String dbPath = null;
    private String sql = null;
    private PreparedStatement statement;

    public SqlRecordSimple() {
//        this("./RecordDb", "scaleCloudsimRecord-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".db", "userRequest", "instanceGroup", "instance");
        this("./RecordDb", "cpnSim.db", "userRequest", "instanceGroup", "instanceGroupGraph", "instance");
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

    @Override
    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void recordUserRequestsSubmitinfo(List<UserRequest> userRequests) {
        try {
            statement = conn.prepareStatement("INSERT INTO " + this.userRequestTableName + " (id,belongDc,submitTime,instanceGroupNum,successInstanceGroupNum) VALUES (?,?,?,?,?);");
            for (UserRequest userRequest : userRequests) {
                statement.setInt(1, userRequest.getId());
                statement.setInt(2, userRequest.getBelongDatacenterId());
                statement.setDouble(3, userRequest.getSubmitTime());
                statement.setInt(4, userRequest.getInstanceGroups().size());
                statement.setInt(5, 0);
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
            statement = conn.prepareStatement("INSERT INTO " + this.instanceGroupTableName + " (id,userRequestId,retryTimes,receivedDc,receivedTime,instanceNum,successInstanceNum) VALUES (?,?,?,?,?,?,?);");
            for (InstanceGroup instanceGroup : instanceGroups) {
                statement.setInt(1, instanceGroup.getId());
                statement.setInt(2, instanceGroup.getUserRequest().getId());
                statement.setInt(3, instanceGroup.getRetryNum());
                statement.setInt(4, instanceGroup.getReceiveDatacenter().getId());
                statement.setDouble(5, instanceGroup.getReceivedTime());
                statement.setInt(6, instanceGroup.getInstanceList().size());
                statement.setInt(7, 0);
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

    @Override
    public void recordInstanceGroupGraphReleaseInfo(int srcDcId, int dstDcId, double finishTime) {
        try {
            sql = "UPDATE " + this.instanceGroupGraphTableName +
                    " SET finishTime = " + finishTime +
                    " WHERE (srcDcId = " + srcDcId +
                    " AND dstDcId = " + dstDcId + ");";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
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

    public Map<String,Map<String,Object>> query(String sql,String id1,String id2){
        String label1=null,label2=null;
        Map<String, Object> record = new HashMap<String, Object>();
        Map<String, Map<String, Object>> recordMap = new HashMap<>();

        try {
            statement=conn.prepareStatement(sql);
            ResultSet resultSet=statement.executeQuery();
            // 获取元数据
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            for (int i=0; i < resultSetMetaData.getColumnCount(); i++){
                String columnLabel = resultSetMetaData.getColumnLabel(i + 1);
                if (Objects.equals(columnLabel,id1)){
                    label1 = columnLabel;
                }else if (Objects.equals(columnLabel,id2)){
                    label2 = columnLabel;
                }
            }

            // 打印一列的列名
            while (resultSet.next()) {
                //获取数据表中满足要求的一行数据，并放入Map中
                for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
                    String columnLabel = resultSetMetaData.getColumnLabel(i + 1);
                    Object columnValue = resultSet.getObject(columnLabel);
                    // System.out.println(columnLabel);
                    record.put(columnLabel, columnValue);
                }

                String key;
                if (label2==null){
                    key=resultSet.getString(label1);
                }else{
                    key= resultSet.getString(label1)+"-"+resultSet.getString(label2);
                }
                recordMap.put(key,record);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return recordMap;
    }

    public Map<String,Map<String,Object>> queryUserRequestTable(){
        return query("select * from "+this.userRequestTableName,"id",null);
    }
    public Map<String,Map<String,Object>> queryInstanceGroupTable(){
        return query("select * from "+this.instanceGroupTableName,"id",null);
    }
    public Map<String,Map<String,Object>> queryInstanceGroupGraphTable(){
        return query("select * from "+this.instanceGroupGraphTableName,"srcDcId","dstDcId");
    }
    public Map<String,Map<String,Object>> queryInstanceTable(){
        return query("select * from "+this.instanceTableName,"id",null);
    }
}
