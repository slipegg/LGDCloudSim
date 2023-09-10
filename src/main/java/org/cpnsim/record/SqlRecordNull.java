package org.cpnsim.record;

import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.UserRequest;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SqlRecordNull implements SqlRecord {

    public SqlRecordNull() {
    }

    @Override
    public String getDbPath() {
        return null;
    }

    @Override
    public void setDbPath(String dbPath) {

    }

    @Override
    public void recordUserRequestsSubmitinfo(List<UserRequest> userRequests) {

    }

    @Override
    public void recordUserRequestFinishInfo(UserRequest userRequest) {

    }

    @Override
    public void recordInstanceGroupsReceivedInfo(List<InstanceGroup> instanceGroups) {

    }

    @Override
    public void recordInstanceGroupFinishInfo(InstanceGroup instanceGroup) {

    }

    @Override
    public void recordInstanceGroupAllInfo(InstanceGroup instanceGroup) {

    }

    @Override
    public void recordInstanceGroupGraphAllocateInfo(int srcDcId, int srcInstanceGroupId, int dstDcId, int dstInstanceGroupId, double bw, double startTime) {

    }

    @Override
    public void recordInstanceGroupGraphReleaseInfo(int srcDcId, int dstDcId, double finishTime) {

    }

    @Override
    public void recordInstancesCreateInfo(Map<Integer, List<Instance>> instances) {

    }

    @Override
    public void recordInstancesFinishInfo(List<Instance> instances) {

    }

    @Override
    public void recordInstancesAllInfo(List<Instance> instances) {

    }

    @Override
    public void close() {

    }

    public Map<String,Map<String,Object>> query(String sql,String id1,String id2){
        String label1=null,label2=null;
        Map<String, Object> record = new HashMap<String, Object>();
        Map<String, Map<String, Object>> recordMap = new HashMap<>();

        String dbDir = "./RecordDb";
        String dbName = "cpnSim.db";
        Path folder = Paths.get(dbDir);
        File dir = new File(dbDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        Path file = Paths.get(dbName);
        String dbPath = folder.resolve(file).toString();
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            PreparedStatement statement=conn.prepareStatement(sql);
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
        return query("select * from userRequest","id",null);
    }
    public Map<String,Map<String,Object>> queryInstanceGroupTable(){
        return query("select * from instanceGroup","id",null);
    }
    public Map<String,Map<String,Object>> queryInstanceGroupGraphTable(){
        return query("select * from instanceGroupGraph","srcDcId","dstDcId");
    }
    public Map<String,Map<String,Object>> queryInstanceTable(){
        return query("select * from instance","id",null);
    }
}
