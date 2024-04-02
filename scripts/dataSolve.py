import os
import sqlite3
import matplotlib.pyplot as plt
import pandas as pd
import csv
import numpy as np

# 获取当前工作目录
current_dir = os.getcwd()
# print("当前工作目录：", current_dir)

# 获取当前脚本所在目录的绝对路径
script_dir = os.path.dirname(os.path.abspath(__file__))

# 改变当前工作目录
os.chdir(script_dir)
# print("新的工作目录：", os.getcwd())

#===========================================================#
#                          方法参数                          #
#===========================================================#
dirPrefix = "test-20240326/"
#===========================================================#
testRequestList = ["simpleRequest","complexRequest"]
testTimeList = ["intermittent"]
testAlgorithmList = ["1-heuristic","2-HFRS","3-RFHS","4-random"]
#===========================================================#
# testRequest = "simpleRequest"
# testRequest = "complexRequest"
#===========================================================#
# testTime = "intermittent"
# testTime = "continued"
#===========================================================#
# testAlgorithm = "1-heuristic"
# testAlgorithm = "2-HFRS" # heuristicFiltering-randomScoring
# testAlgorithm = "3-RFHS" # randomFiltering-heuristicScoring
# testAlgorithm = "4-random"
#===========================================================#
# DBNAME = dirPrefix+testTime+"."+testRequest+"."+testAlgorithm+".db"
#===========================================================#
#                          SQL 语句                          #
#===========================================================#
DELETE_VIEW_SQL = """
DROP VIEW IF EXISTS instanceDelay;
"""
CREATE_VIEW_SQL = """
CREATE VIEW IF NOT EXISTS instanceDelay AS
SELECT 
    instance.id, 
    userRequest.submitTime AS submitTime, 
    instance.startTime-userRequest.submitTime AS delay, 
    instanceGroup.interScheduleEndTime-userRequest.submitTime AS interScheduleTime, 
    instanceGroup.receivedTime-instanceGroup.InterScheduleEndTime AS transToDCTime, 
    instance.intraScheduleEndTime-instanceGroup.receivedTime AS intraScheduleTime, 
    instance.startTime-instance.IntraScheduleEndTime AS allocateTime
FROM instance 
LEFT JOIN instanceGroup on instance.instanceGroupId = instanceGroup.id 
LEFT JOIN userRequest on instance.userRequestId = userRequest.id 
Where instance.startTime >= 0;
"""
#===========================================================#
# 总调度时延
SCHEDULE_TIME_SQL = """
SELECT submitTime/10,AVG(delay),Max(delay),Min(delay) FROM instanceDelay GROUP BY submitTime;
"""
# 数据中心间调度时延
INTER_SCHEDULE_TIME_SQL = """
SELECT submitTime,AVG(interScheduleTime),Max(interScheduleTime),Min(interScheduleTime) FROM instanceDelay GROUP BY submitTime;
"""
# 发送到数据中心时延
TRANS_TIME_SQL = """
SELECT submitTime,AVG(transToDCTime),Max(transToDCTime),Min(transToDCTime) FROM instanceDelay GROUP BY submitTime;
"""
# 数据中心内调度时延
INTRA_SCHEDULE_TIME_SQL = """
SELECT submitTime,AVG(intraScheduleTime),Max(intraScheduleTime),Min(intraScheduleTime) FROM instanceDelay GROUP BY submitTime;
"""
#===========================================================#
# 调度成功率
SCHEDULE_SUCCESS_RATE_SQL = """
SELECT
    submitTime,
    SUM(CASE WHEN state is null THEN 1 ELSE 0 END) AS successNum, COUNT(*) AS sumNum, CAST(SUM(CASE WHEN state IS NULL THEN 1 ELSE 0 END) AS REAL) / COUNT(*) * 100.0 AS successRate
FROM
    userRequest
GROUP BY
    submitTime;
"""
#===========================================================#
# 总体CPU、RAM资源使用情况
TOTAL_USED_RESOURCE_SQL = """
SELECT
    SUM(instance.cpu) AS usedCPU, 
    -- 25600000 AS sumCPU, 
    datacenterSum.sumCPU AS sumCPU, 
    CAST(SUM(instance.cpu) AS REAL)/datacenterSum.sumCPU*100.0 AS CPURate, 
    -- 51200000 AS sumRAM, 
    SUM(instance.ram) AS usedRAM, 
    datacenterSum.sumRAM AS sumRAM, 
    CAST(SUM(instance.ram) AS REAL)/datacenterSum.sumRAM*100.0 AS RAMRate
FROM 
    instance 
LEFT JOIN 
    instanceGroup on instance.instanceGroupId = instanceGroup.id  
LEFT JOIN
    (
        SELECT SUM(cpu) AS sumCPU, SUM(ram) AS sumRAM 
        FROM datacenter
    ) AS datacenterSum
WHERE 
    instance.finishTime is null  AND instanceGroup.receivedDc!=-1;
"""
# 总体带宽资源使用情况
TOTAL_USED_BW_RESOURCE_SQL = """
SELECT 
    IFNULL(SUM(instanceGroupGraph.bw), 0) / 2.0 AS usedBW, 
    -- 43675042.45 AS sumBW, 
    -- CAST(IFNULL(SUM(instanceGroupGraph.bw), 0) AS REAL) / 2.0 / 43675042.45 * 100.0 AS BWRate
    dcNetworkSum.sumBW AS sumBW, 
    CAST(IFNULL(SUM(instanceGroupGraph.bw), 0) AS REAL) / 2.0 / dcNetworkSum.sumBW * 100.0 AS BWRate
FROM 
    instanceGroupGraph 
LEFT JOIN 
    instanceGroup AS srcInstanceGroup ON instanceGroupGraph.srcInstanceGroupId = srcInstanceGroup.id
LEFT JOIN 
    instanceGroup AS dstInstanceGroup ON instanceGroupGraph.dstInstanceGroupId = dstInstanceGroup.id
LEFT JOIN
    (
        SELECT SUM(bw) AS sumBW 
        FROM dcNetwork 
        WHERE srcDatacenterId != dstDatacenterId
    ) AS dcNetworkSum
WHERE 
    srcInstanceGroup.receivedDc != -1 AND dstInstanceGroup.receivedDc != -1 AND instanceGroupGraph.srcDcId != instanceGroupGraph.dstDcId;
"""
# 各数据中心CPU、RAM资源使用情况
USED_RESOURCE_SQL = """
SELECT
    instanceGroup.receivedDc AS dcId, 
    SUM(instance.cpu) AS usedCPU, 
    datacenterSum.sumCPU AS sumCPU, 
    CAST(SUM(instance.cpu) AS REAL)/datacenterSum.sumCPU*100.0 AS CPURate, 
    SUM(instance.ram) AS usedRAM, 
    datacenterSum.sumRAM AS sumRAM, 
    CAST(SUM(instance.ram) AS REAL)/datacenterSum.sumRAM*100.0 AS RAMRate
FROM 
    instance 
LEFT JOIN 
    instanceGroup on instance.instanceGroupId = instanceGroup.id  
LEFT JOIN
    (
        SELECT datacenter.id AS id, SUM(cpu) AS sumCPU, SUM(ram) AS sumRAM 
        FROM datacenter
        GROUP BY datacenter.id
    ) AS datacenterSum
    ON instanceGroup.receivedDc = datacenterSum.id
WHERE 
    instance.finishTime is null AND instanceGroup.receivedDc!=-1
GROUP BY
    instanceGroup.receivedDc;
"""
# 各公网链路带宽资源使用情况
USED_BW_RESOURCE_SQL = """
SELECT 
    instanceGroupGraph.srcDcId AS srcDcId, 
    instanceGroupGraph.dstDcId AS dstDcId,
    IFNULL(SUM(instanceGroupGraph.bw), 0) / 2.0 AS usedBW, 
    dcNetworkSum.sumBW AS sumBW, 
    CAST(IFNULL(SUM(instanceGroupGraph.bw), 0) AS REAL) / 2.0 / dcNetworkSum.sumBW * 100.0 AS BWRate
FROM 
    instanceGroupGraph 
LEFT JOIN 
    instanceGroup AS srcInstanceGroup ON instanceGroupGraph.srcInstanceGroupId = srcInstanceGroup.id
LEFT JOIN 
    instanceGroup AS dstInstanceGroup ON instanceGroupGraph.dstInstanceGroupId = dstInstanceGroup.id
LEFT JOIN
    (
        SELECT srcDatacenterId, dstDatacenterId, SUM(bw) AS sumBW 
        FROM dcNetwork 
        GROUP BY srcDatacenterId, dstDatacenterId
    ) AS dcNetworkSum 
    ON instanceGroupGraph.srcDcId = dcNetworkSum.srcDatacenterId AND instanceGroupGraph.dstDcId = dcNetworkSum.dstDatacenterId
WHERE 
    srcInstanceGroup.receivedDc != -1 AND dstInstanceGroup.receivedDc != -1 AND instanceGroupGraph.srcDcId != instanceGroupGraph.dstDcId
GROUP BY
    instanceGroupGraph.srcDcId, instanceGroupGraph.dstDcId;
"""
#===========================================================#

data = {}

# 定义一个函数来执行查询并导出结果
def execute_and_export(data, filename):
    with open(filename, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerows(data)

# 获取数据
def get_data():
    for testRequest in testRequestList:
        if testRequest not in data:
            data[testRequest] = {}
        for testTime in testTimeList:
            if testTime not in data[testRequest]:
                data[testRequest][testTime] = {}
            for testAlgorithm in testAlgorithmList:
                if testAlgorithm not in data[testRequest][testTime]:
                    data[testRequest][testTime][testAlgorithm] = {}
                
                DBNAME_PREF = dirPrefix+testTime+"."+testRequest+"."+testAlgorithm

                print("connect to database: ", '../RecordDb/'+DBNAME_PREF+".db")

                # 连接到你的SQLite数据库文件
                conn = sqlite3.connect('../RecordDb/'+DBNAME_PREF+".db")
                # 创建一个游标对象用于执行SQL命令
                cursor = conn.cursor()

                cursor.execute(DELETE_VIEW_SQL)
                cursor.execute(CREATE_VIEW_SQL)
                conn.commit()  # 提交视图创建操作
                print("View created successfully")

                # 总调度时延
                cursor.execute(SCHEDULE_TIME_SQL)
                data[testRequest][testTime][testAlgorithm]["schedule_time"] = schedule_time_data = cursor.fetchall()  # 获取所有查询结果
                execute_and_export(schedule_time_data, '../RecordDb/'+DBNAME_PREF+'.schedule_time_data.csv')
                print("Get schedule time data successfully")

                # 数据中心间调度时延
                cursor.execute(INTER_SCHEDULE_TIME_SQL)
                data[testRequest][testTime][testAlgorithm]["inter_schedule_time"] = inter_schedule_time_data = cursor.fetchall()  # 获取所有查询结果
                execute_and_export(inter_schedule_time_data, '../RecordDb/'+DBNAME_PREF+'.inter_schedule_time_data.csv')
                print("Get inter schedule time data successfully")

                # 发送到数据中心时延
                cursor.execute(TRANS_TIME_SQL)
                data[testRequest][testTime][testAlgorithm]["trans_time"] = trans_time_data = cursor.fetchall()  # 获取所有查询结果
                execute_and_export(trans_time_data, '../RecordDb/'+DBNAME_PREF+'.trans_time_data.csv')
                print("Get trans time data successfully")

                # 数据中心内调度时延
                cursor.execute(INTRA_SCHEDULE_TIME_SQL)
                data[testRequest][testTime][testAlgorithm]["intra_schedule_time"] = intra_schedule_time_data = cursor.fetchall()  # 获取所有查询结果
                execute_and_export(intra_schedule_time_data, '../RecordDb/'+DBNAME_PREF+'.intra_schedule_time_data.csv')
                print("Get intra schedule time data successfully")
                
                # 调度成功率
                cursor.execute(SCHEDULE_SUCCESS_RATE_SQL)
                data[testRequest][testTime][testAlgorithm]["schedule_success_rate"] = schedule_success_rate_data = cursor.fetchall()  # 获取所有查询结果
                execute_and_export(schedule_success_rate_data, '../RecordDb/'+DBNAME_PREF+'.schedule_success_rate_data.csv')
                print("Get schedule success rate data successfully")

                # 总体CPU、RAM资源使用情况
                cursor.execute(TOTAL_USED_RESOURCE_SQL)
                data[testRequest][testTime][testAlgorithm]["total_used_resource"] = total_used_resource_data = cursor.fetchall()  # 获取所有查询结果
                execute_and_export(total_used_resource_data, '../RecordDb/'+DBNAME_PREF+'.total_used_resource_data.csv')
                print("Get total used resource data successfully")

                # 总体带宽资源使用情况
                cursor.execute(TOTAL_USED_BW_RESOURCE_SQL)
                data[testRequest][testTime][testAlgorithm]["total_used_bw_resource"] = total_used_bw_resource_data = cursor.fetchall()  # 获取所有查询结果
                execute_and_export(total_used_bw_resource_data, '../RecordDb/'+DBNAME_PREF+'.total_used_bw_resource_data.csv')
                print("Get total used bw resource data successfully")

                # 各数据中心CPU、RAM资源使用情况
                cursor.execute(USED_RESOURCE_SQL)
                data[testRequest][testTime][testAlgorithm]["used_resource"] = used_resource_data = cursor.fetchall()
                execute_and_export(used_resource_data, '../RecordDb/'+DBNAME_PREF+'.used_resource_data.csv')
                print("Get used resource data successfully")

                # 各公网链路带宽资源使用情况
                cursor.execute(USED_BW_RESOURCE_SQL)
                data[testRequest][testTime][testAlgorithm]["used_bw_resource"] = used_bw_resource_data = cursor.fetchall()
                execute_and_export(used_bw_resource_data, '../RecordDb/'+DBNAME_PREF+'.used_bw_resource_data.csv')
                print("Get used bw resource data successfully")

                print()

# 处理数据
def process_data():
    for testRequest in testRequestList:
        for testTime in testTimeList:

            schedule_time_data = {}
            inter_schedule_time_data = {}
            trans_time_data = {}
            intra_schedule_time_data = {}
            schedule_success_rate_data = {}
            total_used_resource_data = {}
            total_used_bw_resource_data = {}
            used_resource_data = {}
            used_bw_resource_data = {}

            # 整合各个算法数据
            for testAlgorithm in testAlgorithmList:
                schedule_time_data[testAlgorithm] = (data[testRequest][testTime][testAlgorithm]["schedule_time"])
                inter_schedule_time_data[testAlgorithm] = (data[testRequest][testTime][testAlgorithm]["inter_schedule_time"])
                trans_time_data[testAlgorithm] = (data[testRequest][testTime][testAlgorithm]["trans_time"])
                intra_schedule_time_data[testAlgorithm] = (data[testRequest][testTime][testAlgorithm]["intra_schedule_time"])
                schedule_success_rate_data[testAlgorithm] = (data[testRequest][testTime][testAlgorithm]["schedule_success_rate"])
                total_used_resource_data[testAlgorithm] = (data[testRequest][testTime][testAlgorithm]["total_used_resource"])
                total_used_bw_resource_data[testAlgorithm] = (data[testRequest][testTime][testAlgorithm]["total_used_bw_resource"])
                used_resource_data[testAlgorithm] = (data[testRequest][testTime][testAlgorithm]["used_resource"])
                used_bw_resource_data[testAlgorithm] = (data[testRequest][testTime][testAlgorithm]["used_bw_resource"])

            # 总调度时延（批次，每种算法最大时延）
            output = []
            for i in range(len(schedule_time_data[testAlgorithmList[0]])):
                Delay = [i+1]
                for algorithm in testAlgorithmList:
                    Delay.append(schedule_time_data[algorithm][i][2])
                output.append(Delay)
            execute_and_export(output, '../RecordDb/'+dirPrefix+testTime+'.'+testRequest+'.SUM.schedule_time_data.csv')
            print("process schedule time data successfully")

            # 数据中心间调度时延
            output = []
            for i in range(len(inter_schedule_time_data[testAlgorithmList[0]])):
                Delay = [i+1]
                for algorithm in testAlgorithmList:
                    Delay.append(inter_schedule_time_data[algorithm][i][2])
                output.append(Delay)
            execute_and_export(output, '../RecordDb/'+dirPrefix+testTime+'.'+testRequest+'.SUM.inter_schedule_time_data.csv')
            print("process inter schedule time data successfully")

            # 调度结果传输时延
            output = []
            for i in range(len(trans_time_data[testAlgorithmList[0]])):
                Delay = [i+1]
                for algorithm in testAlgorithmList:
                    Delay.append(trans_time_data[algorithm][i][2])
                output.append(Delay)
            execute_and_export(output, '../RecordDb/'+dirPrefix+testTime+'.'+testRequest+'.SUM.trans_time_data.csv')
            print("process trans time data successfully")

            # 数据中心内调度时延
            output = []
            for i in range(len(intra_schedule_time_data[testAlgorithmList[0]])):
                Delay = [i+1]
                for algorithm in testAlgorithmList:
                    Delay.append(intra_schedule_time_data[algorithm][i][2])
                output.append(Delay)
            execute_and_export(output, '../RecordDb/'+dirPrefix+testTime+'.'+testRequest+'.SUM.intra_schedule_time_data.csv')
            print("process intra schedule time data successfully")

            # 调度成功率
            output = []
            for i in range(len(schedule_success_rate_data[testAlgorithmList[0]])):
                successRate = [i+1]
                for algorithm in testAlgorithmList:
                    successRate.append(schedule_success_rate_data[algorithm][i][3])
                output.append(successRate)
            execute_and_export(output, '../RecordDb/'+dirPrefix+testTime+'.'+testRequest+'.SUM.schedule_success_rate_data.csv')
            print("process schedule success rate data successfully")

            # 资源整体利用率
            output = []
            cpuUtilization = []
            ramUtilization = []
            bwUtilization = []
            for algorithm in testAlgorithmList:
                cpuUtilization.append(total_used_resource_data[algorithm][0][2])
                ramUtilization.append(total_used_resource_data[algorithm][0][5])
                bwUtilization.append(total_used_bw_resource_data[algorithm][0][2])
            output.append(cpuUtilization)
            output.append(ramUtilization)
            output.append(bwUtilization)
            execute_and_export(output, '../RecordDb/'+dirPrefix+testTime+'.'+testRequest+'.SUM.total_used_resource_data.csv')
            print("process total used resource data successfully")

            # 各数据中心资源利用率
            output = []
            cpuUtilization = []
            ramUtilization = []
            bwUtilization = []
            for algorithm in testAlgorithmList:
                tmp_cpuUtilization = []
                tmp_ramUtilization = []
                tmp_bwUtilization = []
                for sub_data in used_resource_data[algorithm]:
                    tmp_cpuUtilization.append(sub_data[3])
                    tmp_ramUtilization.append(sub_data[6])
                for sub_data in used_bw_resource_data[algorithm]:
                    tmp_bwUtilization.append(sub_data[4])
                maxLen = max(len(tmp_cpuUtilization), len(tmp_ramUtilization), len(tmp_bwUtilization))
                tmp_cpuUtilization += [None] * (maxLen-len(tmp_cpuUtilization))
                tmp_ramUtilization += [None] * (maxLen-len(tmp_ramUtilization))
                tmp_bwUtilization += [None] * (maxLen-len(tmp_bwUtilization))
                cpuUtilization.append(tmp_cpuUtilization)
                ramUtilization.append(tmp_ramUtilization)
                bwUtilization.append(tmp_bwUtilization)
            output = [item for sublist in zip(cpuUtilization, ramUtilization, bwUtilization) for item in sublist]
            output = list(map(list, zip(*output)))
            execute_and_export(output, '../RecordDb/'+dirPrefix+testTime+'.'+testRequest+'.SUM.used_resource_data.csv')
            print("process used resource data successfully")



# 绘图
def draw_figures():
    for testRequest in testRequestList:
        for testTime in testTimeList:
            for testAlgorithm in testAlgorithmList:
                #===========================================================#
                #                          调度时延                          #
                #===========================================================#
                schedule_time_data = data[testRequest][testTime][testAlgorithm]["schedule_time"]
                # schedule_success_rate_data = data[testRequest][testTime][testAlgorithm]["schedule_success_rate"]
                # used_resource_data = data[testRequest][testTime][testAlgorithm]["used_resource"]
                # 获取数据
                print("Schedule time data: ")
                print(schedule_time_data)
                # 绘图
                # 将原始数据转换为适合绘图的格式
                submitTime, Avg_delay, Max_delay, Min_delay = zip(*schedule_time_data)
                df = pd.DataFrame({'Time': submitTime, 'Avg Delay': Avg_delay, 'Max Delay': Max_delay, 'Min Delay': Min_delay})
                # 绘制折线图
                plt.plot(df['Time'], df['Min Delay'], marker='o', label='Min Delay')
                plt.plot(df['Time'], df['Avg Delay'], marker='s', label='Average Delay')
                plt.plot(df['Time'], df['Max Delay'], marker='^', label='Max Delay')
                # 添加图例
                plt.legend()
                # 添加标题和轴标签
                plt.title('Scheduling Delay Over Time')
                plt.xlabel('Time')
                plt.ylabel('Delay (ms)')
                # 美化
                plt.xticks(rotation=45) # 旋转x轴标签，提高可读性
                plt.tight_layout() # 自动调整子图参数, 使之填充整个图像区域
                # 保存图表为图片，而不是显示
                plt.savefig('/home/xyh/LGDCloudSim/scripts/figs/scheduling_delay_over_time-'+testTime+'-'+testRequest+'-'+testAlgorithm+'.png', dpi=300) # 指定路径和文件名
                # plt.show()
                #===========================================================#

def main():
    get_data()
    process_data()
    # draw_figures()

if __name__ == '__main__':
    main()
