import pandas as pd
import matplotlib.pyplot as plt
import sqlite3

# 连接到SQLite数据库
name = "heartbeat"
conn = sqlite3.connect('RecordDb/'+name+'.db')

# 执行SQL查询
query = """
SELECT time, cpuUtilization, ramUtilization
FROM datacenterUtilization
ORDER BY time
"""
df = pd.read_sql_query(query, conn)

# 关闭数据库连接
conn.close()

# 创建图形和坐标轴
fig, ax = plt.subplots(figsize=(10, 6))

# 绘制CPU利用率折线
cpu_line, = ax.plot(df['time'], df['cpuUtilization'], label='CPU Utilization')

# 绘制RAM利用率折线
ram_line, = ax.plot(df['time'], df['ramUtilization'], label='RAM Utilization')

# 获取最大利用率值及其索引
max_cpu = df['cpuUtilization'].max()
max_ram = df['ramUtilization'].max()
max_cpu_idx = df['cpuUtilization'].idxmax()
max_ram_idx = df['ramUtilization'].idxmax()

# 在最大值处添加标注
ax.annotate(f'Max CPU: {max_cpu:.2f}', 
            xy=(df['time'][max_cpu_idx], max_cpu),
            xytext=(10, 10), textcoords='offset points',
            ha='left', va='bottom',
            bbox=dict(boxstyle='round,pad=0.5', fc='yellow', alpha=0.5),
            arrowprops=dict(arrowstyle='->', connectionstyle='arc3,rad=0'))

ax.annotate(f'Max RAM: {max_ram:.2f}', 
            xy=(df['time'][max_ram_idx], max_ram),
            xytext=(10, -10), textcoords='offset points',
            ha='left', va='top',
            bbox=dict(boxstyle='round,pad=0.5', fc='yellow', alpha=0.5),
            arrowprops=dict(arrowstyle='->', connectionstyle='arc3,rad=0'))

# 设置图表标题和坐标轴标签
ax.set_title('Data Center Resource Utilization')
ax.set_xlabel('Time')
ax.set_ylabel('Utilization')

# 添加图例
ax.legend()

# 显示网格线
ax.grid(True)

# 保存图形
plt.savefig(name + 'resource_utilization.png')

# 显示图形
plt.show()