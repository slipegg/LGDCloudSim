import time
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import sqlite3
import matplotlib.patches as mpatches
from matplotlib.legend import Legend

# SELECT COUNT(*) FROM userRequest GROUP BY state;

# Multi DC
schedulers = ["Random", "GPUFirst", "ScoreFirst", "TopoAlign"]
gaps = [12]

# 收集数据
data = {}
for gap in gaps:
    data[gap] = {}
    for scheduler in schedulers:
        name = f"{scheduler}-{gap}s-gap"
        conn = sqlite3.connect(f'RecordDb/{name}.db')
        query = """
            SELECT state, COUNT(*) as count FROM userRequest GROUP BY state;
        """
        df = pd.read_sql_query(query, conn)
        conn.close()
        # 计算成功率
        success_count = df[df['state'] == 'SUCCESS']['count'].sum() if 'SUCCESS' in df['state'].values else 0
        failed_count = df[df['state'] == 'FAILED']['count'].sum() if 'FAILED' in df['state'].values else 0
        total_count = success_count + failed_count
        success_rate = success_count / total_count if total_count > 0 else 0
        data[gap][scheduler] = success_rate

def drawSuccessRateBar():
    fig, ax = plt.subplots(figsize=(12, 8))
    
    bar_width = 0.25  # 增加宽度以稍微隔开
    gap_width = len(schedulers) * bar_width + 0.2  # 增加间距
    x_positions = np.arange(len(gaps)) * gap_width
    
    hatch_list = ['/', '\\', '|', '-']  # 为每个scheduler分配不同的条纹
    
    for i, scheduler in enumerate(schedulers):
        for j, gap in enumerate(gaps):
            success_rate = data[gap][scheduler]
            x_pos = x_positions[j] + i * bar_width
            ax.bar(x_pos, success_rate, width=bar_width, hatch=hatch_list[i], edgecolor='black', linewidth=0.5,
                   label=scheduler if i == 0 and j == 0 else "")
    
    ax.set_xticks(x_positions + (len(schedulers) - 1) * bar_width / 2)
    ax.set_xticklabels([str(g) for g in gaps])
    ax.set_xlabel('Gap (s)')
    ax.set_ylabel('Success Rate')
    ax.set_title('Success Rate by Gap and Scheduler')
    ax.set_ylim(0, 1)  # 成功率在0到1之间
    
    # 添加legend for schedulers (hatches)
    scheduler_handles = [mpatches.Patch(hatch=hatch_list[i], edgecolor='black', facecolor='white', label=scheduler) for i, scheduler in enumerate(schedulers)]
    leg = Legend(ax, handles=scheduler_handles, labels=schedulers, title='Schedulers', loc='upper center', bbox_to_anchor=(0.5, 1.05))
    ax.add_artist(leg)
    
    plt.tight_layout()
    img_name = f'RecordDb/output/success_rate_bar_{int(time.time())}.png'
    plt.savefig(img_name)
    plt.close()
    print(f"Success rate bar chart saved at {img_name}")

# 绘制成功率图
drawSuccessRateBar()

# 打印成功率
print("Success Rates:")
print("Gap (s)\tScheduler\t\tSuccess Rate")
print("-" * 50)
for gap in gaps:
    for scheduler in schedulers:
        success_rate = data[gap][scheduler]
        print(f"{gap}\t{scheduler}\t\t{success_rate:.4f}")
