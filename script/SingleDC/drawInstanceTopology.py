import time
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import sqlite3
import matplotlib.patches as mpatches
from matplotlib.legend import Legend

# 定义数据库名称列表
def getDBName(scheduler: str, gap: int) -> str:
    return f"{scheduler}-{gap}s-gap"

# Single DC
schedulers = ["Random", "GPUBinPack", "S0BinPack", "TopoAlign-Random", "TopoAlign"]
gaps = [50, 75, 100]

# 收集数据
data = {}
for gap in gaps:
    data[gap] = {}
    for scheduler in schedulers:
        name = getDBName(scheduler, gap)
        conn = sqlite3.connect(f'RecordDb/{name}.db')
        query = """
        SELECT topologyType, Sum(sameHostNum) as sum_sameHostNum, Sum(sameS0Num) as sum_sameS0Num, Sum(sameS1Num) as sum_sameS1Num, Sum(sameS2Num) as sum_sameS2Num, Sum(sameS3Num) as sum_sameS3Num
        FROM instanceTopology
        Group BY topologyType
        """
        df = pd.read_sql_query(query, conn)
        conn.close()
        # 将其存储为dict
        data[gap][scheduler] = df.set_index('topologyType').to_dict(orient='index')
        

# 获取所有topologyType
topologyTypes = set()
for gap in gaps:
    for scheduler in schedulers:
        topologyTypes.update(data[gap][scheduler].keys())
topologyTypes = sorted(list(topologyTypes))

def drawSpreadBar(topologyType: str):
    fig, ax = plt.subplots(figsize=(12, 8))
    
    bar_width = 0.2  # 调整宽度以适应5个调度器
    gap_width = len(schedulers) * bar_width + 0.2  # 增加间距
    x_positions = np.arange(len(gaps)) * gap_width
    
    stacks = ['sum_sameHostNum', 'sum_sameS0Num', 'sum_sameS1Num', 'sum_sameS2Num', 'sum_sameS3Num']
    colors = ['#1F77B4', '#2BA02B', '#FF7D0B', '#D62729', 'purple']
    hatch_list = ['/', '\\', '|', '-', 'x']  # 为5个scheduler分配不同的条纹
    
    for i, scheduler in enumerate(schedulers):
        for j, gap in enumerate(gaps):
            if topologyType in data[gap][scheduler]:
                vals = data[gap][scheduler][topologyType]
                x_pos = x_positions[j] + i * bar_width
                bottom = 0
                for k, stack in enumerate(stacks):
                    height = vals.get(stack, 0)
                    ax.bar(x_pos, height, bottom=bottom, width=bar_width, color=colors[k], 
                           hatch=hatch_list[i], edgecolor='black', linewidth=0.5,
                           label=stack if i == 0 and j == 0 and k == 0 else "")
                    bottom += height
    
    ax.set_xticks(x_positions + (len(schedulers) - 1) * bar_width / 2)
    ax.set_xticklabels([str(g) for g in gaps])
    ax.set_xlabel('Gap (s)')
    ax.set_ylabel('Nums')
    ax.set_title(f'Topology Type: {topologyType}')
    
    # 添加legend for stacks (colors)
    # 反转stacks和colors的顺序
    stacks = stacks[::-1]
    colors = colors[::-1]
    stack_handles = [mpatches.Patch(color=colors[k], label=stack) for k, stack in enumerate(stacks)]
    leg1 = Legend(ax, handles=stack_handles, labels=stacks, title='Sum Types', loc='upper center', bbox_to_anchor=(0.3, 1.05))
    ax.add_artist(leg1)
    
    # 添加legend for schedulers (hatches)
    scheduler_handles = [mpatches.Patch(hatch=hatch_list[i], edgecolor='black', facecolor='white', label=scheduler) for i, scheduler in enumerate(schedulers)]
    leg2 = Legend(ax, handles=scheduler_handles, labels=schedulers, title='Schedulers', loc='upper center', bbox_to_anchor=(0.7, 1.05))
    ax.add_artist(leg2)
    
    plt.tight_layout()
    img_name = f'RecordDb/output/{topologyType}_stacked_bar_{int(time.time())}.png'
    plt.savefig(img_name)
    plt.close()
    print(f"Stacked bar chart for {topologyType} saved at {img_name}")

# 绘制每个topologyType的图
for topo in topologyTypes:
    drawSpreadBar(topo)
