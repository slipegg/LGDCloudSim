import time
import pandas as pd
import matplotlib.pyplot as plt
import sqlite3
import itertools

# 定义数据库名称列表

# db_names = ["DLJTrace-sample", "DLJTrace_sampled_time_144000_scale_30.0_gap_50.0", "DLJTrace_sampled_time_144000_scale_30.0_gap_60.0", "DLJTrace_sampled_time_144000_scale_30.0_gap_75.0", "DLJTrace_sampled_time_144000_scale_30.0_gap_80.0", "DLJTrace_sampled_time_144000_scale_30.0_gap_100.0"]
# db_names = ["DLJTrace_sampled_time_144000_scale_25.0_gap_50.0", "DLJTrace_sampled_time_144000_scale_37.0_gap_75.0", "DLJTrace_sampled_time_144000_scale_50.0_gap_100.0"]
# db_names = ["DLJTrace_sampled_time_144000_scale_7.0_gap_15.0", "DLJTrace_sampled_time_144000_scale_11.0_gap_22.0", "DLJTrace_sampled_time_144000_scale_14.0_gap_29.0"]
# db_names = ["gpuRandom-29s-gap", "gpuFirst-29s-gap", "topologyScoreFirst-29s-gap", "closTopology-29s-gap"]
# db_names = ["gpuRandom-29s-gap", "gpuFirst-29s-gap", "topologyScoreFirst-29s-gap", "closTopology-29s-gap"]
# db_names = ["gpuRandom-22s-gap", "gpuFirst-22s-gap", "topologyScoreFirst-22s-gap", "closTopology-22s-gap"]
# db_names = ["gpuRandom-15s-gap", "gpuFirst-15s-gap", "topologyScoreFirst-15s-gap", "closTopology-15s-gap"]
db_names = ["closTopology-12s-gap", "topologyScoreFirst-12s-gap", "gpuFirst-12s-gap", "gpuRandom-12s-gap"]


# 定义颜色映射
# color_map = {'DLJTrace': 'blue'}

# 创建第一个图形（GPU利用率折线图）
fig1, ax1 = plt.subplots(figsize=(12, 6))

avg_utilizations = []

# 为每个数据库按 dcId 绘制多条 GPU 利用率折线（每个 db 一组颜色，同 db 内不同 dc 使用不同线型）
linestyles = [':', '--', '-', '-.']
color_cycle = plt.cm.tab10.colors
for idx, name in enumerate(db_names):
    # 连接到SQLite数据库
    conn = sqlite3.connect(f'RecordDb/{name}.db')

    # 执行SQL查询，包含 dcId
    query = """
    SELECT time, gpuUtilization, dcId
    FROM datacenterUtilization
    ORDER BY time, dcId
    """
    df = pd.read_sql_query(query, conn)
    # SELECT COUNT(*) FROM userRequest GROUP BY state;
    # 关闭数据库连接
    conn.close()

    if df.empty:
        continue

    # 将GPU利用率转换为百分比
    df['gpuUtilization'] = df['gpuUtilization'] * 100

    # 将时间从毫秒转换为小时
    df['time'] = df['time'] / (60 *60 * 1000)

    # 获取该数据库下的所有 dcId
    dc_ids = sorted(df['dcId'].unique())
    color = color_cycle[idx % len(color_cycle)]
    for j, dc in enumerate(dc_ids):
        df_dc = df[df['dcId'] == dc]
        # 如果某个 dc 的时间点未排序或有重复时间，可以按 time 排序
        df_dc = df_dc.sort_values('time')
        linestyle = linestyles[j % len(linestyles)]
        ax1.plot(df_dc['time'], df_dc['gpuUtilization'], label=f'{name} DC{dc}', color=color, linestyle=linestyle)

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
