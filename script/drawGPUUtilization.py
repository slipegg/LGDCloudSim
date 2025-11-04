import time
import pandas as pd
import matplotlib.pyplot as plt
import sqlite3

# 定义数据库名称列表

# db_names = ["DLJTrace-sample", "DLJTrace_sampled_time_144000_scale_30.0_gap_50.0", "DLJTrace_sampled_time_144000_scale_30.0_gap_60.0", "DLJTrace_sampled_time_144000_scale_30.0_gap_75.0", "DLJTrace_sampled_time_144000_scale_30.0_gap_80.0", "DLJTrace_sampled_time_144000_scale_30.0_gap_100.0"]
db_names = ["DLJTrace_sampled_time_144000_scale_25.0_gap_50.0", "DLJTrace_sampled_time_144000_scale_37.0_gap_75.0", "DLJTrace_sampled_time_144000_scale_50.0_gap_100.0"]
# 定义颜色映射
# color_map = {'DLJTrace': 'blue'}

# 创建第一个图形（GPU利用率折线图）
fig1, ax1 = plt.subplots(figsize=(12, 6))

avg_utilizations = []

# 为每个数据库绘制GPU利用率折线
for name in db_names:
    # 连接到SQLite数据库
    conn = sqlite3.connect(f'RecordDb/{name}.db')

    # 执行SQL查询
    query = """
    SELECT time, gpuUtilization
    FROM datacenterUtilization
    ORDER BY time
    """
    df = pd.read_sql_query(query, conn)

    # 关闭数据库连接
    conn.close()

    # 将GPU利用率转换为百分比
    df['gpuUtilization'] = df['gpuUtilization'] * 100

    # 将时间从毫秒转换为小时
    df['time'] = df['time'] / (60 *60 * 1000)

    # 绘制GPU利用率折线，使用颜色映射
    line, = ax1.plot(df['time'], df['gpuUtilization'], label=f'{name} GPU Utilization')#, color=color_map[name]

# 设置第一个图的标题和坐标轴标签
ax1.set_title('Data Center GPU Utilization Comparison')
ax1.set_xlabel('Time (hours)')
ax1.set_ylabel('GPU Utilization (%)')
# ax1.set_ylim(60, 100)
ax1.set_xlim(0, 50)
ax1.legend()
ax1.yaxis.set_major_locator(plt.MultipleLocator(5))  # 设置y轴刻度间隔为5%
ax1.grid(True)

# 保存第一个图形
img_name1 = f'RecordDb/output/gpu_utilization_comparison_{int(time.time())}.png'
plt.savefig(img_name1)
plt.close(fig1)
print(f"GPU利用率折线图已保存在 {img_name1}")
