import time
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import sqlite3

# 定义数据库名称列表
db_names = ["DLJTrace_sampled_time_144000_scale_25.0_gap_50.0", "DLJTrace_sampled_time_144000_scale_37.0_gap_75.0", "DLJTrace_sampled_time_144000_scale_50.0_gap_100.0"]

# 收集数据
data = {}
for name in db_names:
    conn = sqlite3.connect(f'RecordDb/{name}.db')
    query = """
    SELECT topologyType, Avg(scheduledScore) as avg_scheduledScore
    FROM instanceTopology
    GROUP BY topologyType
    """
    df = pd.read_sql_query(query, conn)
    conn.close()
    # 将其存储为dict
    data[name] = df.set_index('topologyType')['avg_scheduledScore'].to_dict()

# 绘制分组条形图
fig, ax = plt.subplots(figsize=(10, 6))
bar_width = 0.35
x = np.arange(len(db_names))

# 获取类型列表
types = list(data[db_names[0]].keys())  # 假设所有数据库有相同的类型

for i, typ in enumerate(types):
    values = [data[name].get(typ, 0) for name in db_names]
    ax.bar(x + i * bar_width, values, bar_width, label=f'Type {typ}')

ax.set_xlabel('Database')
ax.set_ylabel('Average Scheduled Score')
ax.set_title('Average Scheduled Score by Database and Topology Type')
ax.set_xticks(x + bar_width / 2)
ax.set_xticklabels(db_names, rotation=45, ha='right')
ax.legend()

# 保存图形
img_name = f'RecordDb/output/scheduled_score_grouped_bar_{int(time.time())}.png'
plt.savefig(img_name)
plt.close()
print(f"Scheduled Score分组条形图已保存在 {img_name}")
