import time
import pandas as pd
import matplotlib.pyplot as plt
import sqlite3

# 定义数据库名称列表
db_names = ["noDelay", "heartbeat", "shadowResource"]

# 创建图形和坐标轴
fig, ax = plt.subplots(figsize=(12, 6))

# 为每个数据库绘制CPU利用率折线
for name in db_names:
    # 连接到SQLite数据库
    conn = sqlite3.connect(f'RecordDb/{name}.db')

    # 执行SQL查询
    query = """
    SELECT time, cpuUtilization
    FROM datacenterUtilization
    ORDER BY time
    """
    df = pd.read_sql_query(query, conn)

    # 关闭数据库连接
    conn.close()

    # 绘制CPU利用率折线
    line, = ax.plot(df['time'], df['cpuUtilization'], label=f'{name} CPU Utilization')

    # 获取最大利用率值及其索引
    max_cpu = df['cpuUtilization'].max()
    max_cpu_idx = df['cpuUtilization'].idxmax()

    # 在最大值处添加标注
    ax.annotate(f'Max {name}: {max_cpu:.2f}', 
                xy=(df['time'][max_cpu_idx], max_cpu),
                xytext=(10, 10), textcoords='offset points',
                ha='left', va='bottom',
                bbox=dict(boxstyle='round,pad=0.5', fc='yellow', alpha=0.5),
                arrowprops=dict(arrowstyle='->', connectionstyle='arc3,rad=0'))

# 设置图表标题和坐标轴标签
ax.set_title('Data Center CPU Utilization Comparison')
ax.set_xlabel('Time')
ax.set_ylabel('CPU Utilization')

# 添加图例
ax.legend()

# 设置y轴刻度间隔为0.05
ax.yaxis.set_major_locator(plt.MultipleLocator(0.05))
# 设置x轴刻度间隔为500
ax.xaxis.set_major_locator(plt.MultipleLocator(500))
# 显示网格线
ax.grid(True)

# 保存图形
plt.savefig(f'RecordDb/output/cpu_utilization_comparison_{int(time.time())}.png')

# 显示图形
plt.show()