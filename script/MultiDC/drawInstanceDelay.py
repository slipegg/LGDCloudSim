import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import os
import sqlite3
import time
import matplotlib.patches as mpatches
from matplotlib.legend import Legend

# 定义数据库名称列表
def getDBName(scheduler: str, gap: int) -> str:
    return f"{scheduler}-{gap}s-gap"

# Multi DC
schedulers = ["GPUFirst", "ScoreFirst", "TopoAlign"]
gaps = [12, 15, 22]

# 收集数据
data = {}
for gap in gaps:
    data[gap] = {}
    for scheduler in schedulers:
        name = getDBName(scheduler, gap)
        conn = sqlite3.connect(f'RecordDb/{name}.db')
        query = """
            SELECT AVG(i.startTime - ur.submitTime) AS average_submit_time_delay
            FROM instance i
            JOIN userRequest ur ON i.userRequestId = ur.id
            WHERE i.startTime IS NOT NULL AND ur.submitTime IS NOT NULL;
        """
        df = pd.read_sql_query(query, conn)
        conn.close()
        # 存储延迟值（转换为秒）
        delay_seconds = df['average_submit_time_delay'][0] / 1000 if not df.empty else 0
        data[gap][scheduler] = delay_seconds

def drawDelayBar():
    fig, ax = plt.subplots(figsize=(12, 8))
    
    bar_width = 0.25  # 条形宽度
    gap_width = len(schedulers) * bar_width + 0.2  # 增加间距
    x_positions = np.arange(len(gaps)) * gap_width
    
    hatch_list = ['/', '\\', 'x']  # 为每个scheduler分配不同的条纹
    
    for i, scheduler in enumerate(schedulers):
        for j, gap in enumerate(gaps):
            delay = data[gap][scheduler]
            x_pos = x_positions[j] + i * bar_width
            ax.bar(x_pos, delay, width=bar_width, hatch=hatch_list[i], edgecolor='black', linewidth=0.5,
                   label=scheduler if i == 0 and j == 0 else "")
    
    ax.set_xticks(x_positions + (len(schedulers) - 1) * bar_width / 2)
    ax.set_xticklabels([str(g) for g in gaps])
    ax.set_xlabel('Gap (s)')
    ax.set_ylabel('Average Submit Time Delay (s)')
    ax.set_title('Average Submit Time Delay by Gap and Scheduler')
    
    # 添加legend for schedulers (hatches)
    scheduler_handles = [mpatches.Patch(hatch=hatch_list[i], edgecolor='black', facecolor='white', label=scheduler) for i, scheduler in enumerate(schedulers)]
    leg = Legend(ax, handles=scheduler_handles, labels=schedulers, title='Schedulers', loc='upper center', bbox_to_anchor=(0.5, 1.05))
    ax.add_artist(leg)
    
    plt.tight_layout()
    img_name = f'RecordDb/output/average_submit_time_delay_bar_{int(time.time())}.png'
    plt.savefig(img_name)
    plt.close()
    print(f"Average submit time delay bar chart saved at {img_name}")

# 绘制条形图
drawDelayBar()

# 打印数值
print("Average Submit Time Delay (s):")
print("Gap (s)\tScheduler\t\tDelay (s)")
print("-" * 40)
for gap in gaps:
    for scheduler in schedulers:
        delay = data[gap][scheduler]
        print(f"{gap}\t{scheduler}\t\t{delay:.4f}")
