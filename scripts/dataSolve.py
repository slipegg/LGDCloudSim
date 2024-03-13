import os
import sqlite3
import matplotlib.pyplot as plt
import pandas as pd


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
dirPrefix = "test-20240312/"
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
CREATE_VIEW_SQL = """
CREATE VIEW IF NOT EXISTS instanceDelay AS
SELECT instance.id,userRequest.submitTime AS submitTime,instance.startTime-userRequest.submitTime AS delay
FROM instance LEFT JOIN userRequest on instance.userRequestId = userRequest.id  Where instance.startTime >= 0;
"""
SCHEDULE_TIME_SQL = """
SELECT submitTime/10,AVG(delay),Max(delay),Min(delay) FROM instanceDelay GROUP BY submitTime;
"""
SCHEDULE_SUCCESS_RATE_SQL = """
SELECT
    submitTime/10,
    SUM(CASE WHEN state is null THEN 1 ELSE 0 END) AS successNum, COUNT(*) AS sumNum
FROM
    userRequest
GROUP BY
    submitTime;
"""
USED_RESOURCE_SQL = """
SELECT instanceGroup.receivedDc, COUNT(*), SUM(instance.cpu),SUM(instance.ram) FROM instance LEFT JOIN instanceGroup on instance.instanceGroupId = instanceGroup.id  Where instance.finishTime is null  AND instanceGroup.receivedDc!=-1
GROUP BY instanceGroup.receivedDc;
"""
#===========================================================#

data = {}

for testRequest in testRequestList:
    if data[testRequest] == None:
        data[testRequest] = {}
    for testTime in testTimeList:
        if data[testRequest][testTime] == None:
            data[testRequest][testTime] = {}
        for testAlgorithm in testAlgorithmList:
            if data[testRequest][testTime][testAlgorithm] == None:
                data[testRequest][testTime][testAlgorithm] = {}
            DBNAME = dirPrefix+testTime+"."+testRequest+"."+testAlgorithm+".db"
            # 连接到你的SQLite数据库文件
            conn = sqlite3.connect('../RecordDb/'+DBNAME)
            # 创建一个游标对象用于执行SQL命令
            cursor = conn.cursor()

            cursor.execute(CREATE_VIEW_SQL)
            conn.commit()  # 提交视图创建操作

            cursor.execute(SCHEDULE_TIME_SQL)
            data[testRequest][testTime][testAlgorithm]["schedule_time"] = schedule_time_data = cursor.fetchall()  # 获取所有查询结果

            cursor.execute(SCHEDULE_SUCCESS_RATE_SQL)
            data[testRequest][testTime][testAlgorithm]["schedule_success_rate"] = schedule_success_rate_data = cursor.fetchall()

            cursor.execute(USED_RESOURCE_SQL)
            data[testRequest][testTime][testAlgorithm]["used_resource"] = used_resource_data = cursor.fetchall()



for testRequest in testRequestList:
    for testTime in testTimeList:
        for testAlgorithm in testAlgorithmList:
            #===========================================================#
            #                          调度时延                          #
            #===========================================================#
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


#===========================================================#
#                          调度成功率                          #
#===========================================================#
cursor.execute(SCHEDULE_SUCCESS_RATE_SQL)
schedule_success_rate_data = cursor.fetchall()  # 获取所有查询结果
print("Schedule success rate data: ")
print(schedule_success_rate_data)
#===========================================================#


#===========================================================#
#                          资源利用率                          #
#===========================================================#
cursor.execute(USED_RESOURCE_SQL)
used_resource_data = cursor.fetchall()  # 获取所有查询结果
print("Used resource data: ")
print(used_resource_data)
#===========================================================#

