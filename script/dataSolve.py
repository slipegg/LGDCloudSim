import os
import sqlite3

# 获取当前工作目录
current_dir = os.getcwd()
print("当前工作目录：", current_dir)

# 获取当前脚本所在目录的绝对路径
script_dir = os.path.dirname(os.path.abspath(__file__))

# 改变当前工作目录
os.chdir(script_dir)
print("新的工作目录：", os.getcwd())

#===========================================================#
#                          方法参数                          #
#===========================================================#
testRequest = "simpleRequest";
# testRequest = "complexRequest";
testTime = "intermittent";
# testTime = "continued";
testAlgorithm = "1-heuristic";
# testAlgorithm = "2-HFRS"; # heuristicFiltering-randomScoring
# testAlgorithm = "3-RFHS"; # randomFiltering-heuristicScoring
# testAlgorithm = "4-random";
#===========================================================#
DBNAME = testTime+"."+testRequest+"."+testAlgorithm+".db";
#===========================================================#
#                          SQL 语句                          #
#===========================================================#
CREATE_VIEW_SQL = """
CREATE VIEW IF NOT EXISTS instanceDelay AS
SELECT instance.id,userRequest.submitTime AS submitTime,instance.startTime-userRequest.submitTime AS delay
FROM instance LEFT JOIN userRequest on instance.userRequestId = userRequest.id  Where instance.startTime >= 0;
"""
SCHEDULE_TIME_SQL = """
SELECT submitTime,AVG(delay),Max(delay),Min(delay) FROM instanceDelay GROUP BY submitTime;
"""
SCHEDULE_SUCCESS_RATE_SQL = """
SELECT
    submitTime,
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


# 连接到你的SQLite数据库文件
conn = sqlite3.connect('../RecordDb/'+DBNAME)
# 创建一个游标对象用于执行SQL命令
cursor = conn.cursor()

cursor.execute(CREATE_VIEW_SQL)
conn.commit()  # 提交视图创建操作

cursor.execute(SCHEDULE_TIME_SQL)
data = cursor.fetchall()  # 获取所有查询结果
print(data)

cursor.execute(SCHEDULE_SUCCESS_RATE_SQL)
data = cursor.fetchall()  # 获取所有查询结果
print(data)

cursor.execute(USED_RESOURCE_SQL)
data = cursor.fetchall()  # 获取所有查询结果
print(data)


# 绘图

import matplotlib.pyplot as plt

# 将原始数据转换为适合绘图的格式
months, sales = zip(*data)

# 绘制折线图
plt.figure(figsize=(10, 5))
plt.plot(months, sales, label='Monthly Sales')
plt.xlabel('Month')
plt.ylabel('Total Sales')
plt.title('Monthly Sales Over Time')
plt.legend()
plt.show()

# 绘制柱状图
plt.figure(figsize=(10, 5))
plt.bar(months, sales, label='Monthly Sales')
plt.xlabel('Month')
plt.ylabel('Total Sales')
plt.title('Monthly Sales Bar Chart')
plt.xticks(rotation=90)  # 如果月份太长可旋转标签
plt.legend()
plt.show()