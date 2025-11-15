import pandas as pd
import matplotlib.pyplot as plt

# 读取数据（跳过标题行，手动命名列）
df = pd.read_csv('script/LargeScale/data.csv', sep='\t', skiprows=1, names=['replicate', 'hosts', '3D_PP', '3D_DP'])

# 填充 replicate 列（前向填充）
df['replicate'] = df['replicate'].ffill()

# 转换数据类型
df['hosts'] = df['hosts'].astype(int)
df['3D_PP'] = df['3D_PP'].astype(float)
df['3D_DP'] = df['3D_DP'].astype(float)

# 绘制图
plt.figure(figsize=(10, 6))

# 定义颜色和线型
colors = {'x1': 'blue', 'x3': 'green', 'x5': 'red'}
linestyles = {'3D_PP': '--', '3D_DP': ':'}  # 虚线类型

for rep in df['replicate'].unique():
    if pd.isna(rep):  # 跳过 NaN replicate
        continue
    subset = df[df['replicate'] == rep]
    for strategy in ['3D_PP', '3D_DP']:
        plt.plot(subset['hosts'], subset[strategy], 
                 color=colors[rep], linestyle=linestyles[strategy], 
                 label=f'{rep} - {strategy}')

plt.xlabel('Number of Requested Hosts')
plt.ylabel('Scheduling Time (ms)')
plt.title('Scheduling Time Comparison by Scale and Strategy')
plt.grid(True)
plt.legend()
plt.xticks(rotation=45)
plt.tight_layout()
plt.savefig('scheduling_time_plot.png')  # 保存图
print("Saved plot to scheduling_time_plot.png")
# plt.show()
