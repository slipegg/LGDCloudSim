import time
import pandas as pd
import matplotlib.pyplot as plt
import sqlite3

# db_names = ["closTopology-50s-gap", "closTopologyWithoutSort-50s-gap", "topologyBinPackGang-50s-gap", "gpuBinPackGang-50s-gap", "randomGang-50s-gap"]
# db_names = ["gpuRandom-15s-gap", "gpuFirst-15s-gap", "topologyScoreFirst-15s-gap", "closTopology-15s-gap"]
# db_names = ["gpuRandom-22s-gap", "gpuFirst-22s-gap", "topologyScoreFirst-22s-gap", "closTopology-22s-gap"]
db_names = ["GPUFirst-12s-gap", "ScoreFirst-12s-gap", "TopoAlign-12s-gap"]

color_map = {
    "TopoAlign": "#F94141",
    "ScoreFirst": "#267F58",
    "GPUFirst": "#296BBB"
}

def get_name_from_db_name(db_name):
    names = db_name.split('-')
    names = names[:-2]  # 去掉最后两个部分
    return '-'.join(names)

def draw_clos_topology_score_for_each(db_name):
    # 创建第一个图形（GPU利用率折线图）
    fig1, ax1 = plt.subplots(figsize=(12, 6))
    ax2 = ax1.twinx()  # 创建共享x轴的双y轴

    avg_utilizations = []

    # 定义颜色和线型
    color = 'blue'  # fixed for db
    linestyles = ['-', '--', '-.', ':', (0, (3, 1, 1, 1))]  # for dc

    # 为每个数据库绘制GPU利用率折线
    # 连接到SQLite数据库
    conn = sqlite3.connect(f'RecordDb/{db_name}.db')

    # 执行SQL查询，包含 dcId
    query = """
    SELECT time, topologyScoreSum, s0Pct30TopologyScore, s0Pct60TopologyScore, s0Pct90TopologyScore, s0MeanTopologyScore, dcId
    FROM datacenterUtilization
    Where time < 50*60*60*1000
    ORDER BY time, dcId
    """
    df = pd.read_sql_query(query, conn)

    # 关闭数据库连接
    conn.close()

    if df.empty:
        return

    # 将时间从毫秒转换为小时
    df['time'] = df['time'] / (60 *60 * 1000)

    # 获取所有 dcId
    dc_ids = sorted(df['dcId'].unique())

    # 为每个 dcId 绘制
    for dc_id in dc_ids:
        df_dc = df[df['dcId'] == dc_id]
        linestyle = linestyles[int(dc_id) % len(linestyles)]

        # 在ax1（左y轴）上绘制S0分数
        ax1.plot(df_dc['time'], df_dc['s0Pct30TopologyScore'], color=color, linestyle=linestyle, marker='o', markersize=2, label=f'DC{dc_id} S0 Pct30')
        ax1.plot(df_dc['time'], df_dc['s0Pct60TopologyScore'], color=color, linestyle=linestyle, marker='s', markersize=2, label=f'DC{dc_id} S0 Pct60')
        ax1.plot(df_dc['time'], df_dc['s0Pct90TopologyScore'], color=color, linestyle=linestyle, marker='^', markersize=2, label=f'DC{dc_id} S0 Pct90')
        ax1.plot(df_dc['time'], df_dc['s0MeanTopologyScore'], color=color, linestyle=linestyle, marker='d', markersize=2, label=f'DC{dc_id} S0 Mean')

    # 设置轴标签
    ax1.set_xlabel('Time (hours)')
    ax1.set_ylabel('S0 Topology Scores')
    ax2.set_ylabel('Topology Score Sum')

    # 设置x轴范围
    ax1.set_xlim(0, 50)

    # 合并legend
    handles1, labels1 = ax1.get_legend_handles_labels()
    handles2, labels2 = ax2.get_legend_handles_labels()
    ax1.legend(handles1 + handles2, labels1 + labels2, loc='upper left')

    # 设置网格
    ax1.grid(True)


    # 保存第一个图形
    plt.title(f'{db_name} Topology Score Over Time')
    img_name1 = f'RecordDb/output/topology_score_comparison_{db_name}_{int(time.time())}.png'
    plt.savefig(img_name1)
    plt.close(fig1)
    print(f"{db_name} Topology Score折线图已保存在 {img_name1}")

def draw_topologyScoreSum_comparison():
    # 创建图形
    fig, ax = plt.subplots(figsize=(12, 6))

    # 定义颜色和线型
    colors = ['red', 'blue', 'green', 'orange', 'purple']  # for db
    linestyles = ['-', '--', '-.', ':', (0, (3, 1, 1, 1))]  # for dc

    # 为每个数据库绘制Topology Score Sum折线，按 dcId 分组
    for db_idx, db_name in enumerate(db_names):
        color = colors[db_idx % len(colors)]
        
        # 连接到SQLite数据库
        conn = sqlite3.connect(f'RecordDb/{db_name}.db')

        # 执行SQL查询，包含 dcId
        query = """
        SELECT time, topologyScoreSum, dcId
        FROM datacenterUtilization
        Where time < 50*60*60*1000
        ORDER BY time, dcId
        """
        df = pd.read_sql_query(query, conn)

        # 关闭数据库连接
        conn.close()

        if df.empty:
            continue

        # 将时间从毫秒转换为小时
        df['time'] = df['time'] / (60 * 60 * 1000)

        # 获取该数据库下的所有 dcId
        dc_ids = sorted(df['dcId'].unique())

        # 为每个 dcId 绘制折线
        for dc_id in dc_ids:
            df_dc = df[df['dcId'] == dc_id]
            linestyle = linestyles[int(dc_id) % len(linestyles)]
            ax.plot(df_dc['time'], df_dc['topologyScoreSum'], color=color, linestyle=linestyle, label=f'{db_name} DC{dc_id}')

    # 设置轴标签和标题
    ax.set_xlabel('Time (hours)')
    ax.set_ylabel('Topology Score Sum')
    ax.set_title('Topology Score Sum Comparison Over Time')
    ax.set_xlim(0, 50)
    ax.legend()
    ax.grid(True)

    # 保存图形
    img_name = f'RecordDb/output/topology_score_sum_comparison_{int(time.time())}.png'
    plt.savefig(img_name)
    plt.close(fig)
    print(f"Topology Score Sum比较折线图已保存在 {img_name}")

def draw_total_topology_score_sum_comparison():
    # 创建图形
    fig, ax = plt.subplots(figsize=(12, 6))

    # 定义颜色
    colors = ['red', 'blue', 'green', 'orange', 'purple']  # for db

    # 为每个数据库绘制总 Topology Score Sum 折线
    for db_idx, db_name in enumerate(db_names):
        color = color_map[get_name_from_db_name(db_name)]
        
        # 连接到SQLite数据库
        conn = sqlite3.connect(f'RecordDb/{db_name}.db')

        # 执行SQL查询，包含 dcId
        query = """
        SELECT time, topologyScoreSum, dcId
        FROM datacenterUtilization
        Where time < 50*60*60*1000
        ORDER BY time, dcId
        """
        df = pd.read_sql_query(query, conn)

        # 关闭数据库连接
        conn.close()

        if df.empty:
            continue

        # 将时间从毫秒转换为小时
        df['time'] = df['time'] / (60 * 60 * 1000)

        # 按时间分组，计算 topologyScoreSum 的总和
        df_total = df.groupby('time')['topologyScoreSum'].sum().reset_index()

        # 绘制总和折线
        ax.plot(df_total['time'], df_total['topologyScoreSum'], color=color, label=f'{get_name_from_db_name(db_name)}')

    # 设置轴标签和标题
    ax.set_xlabel('Time (hours)')
    ax.set_ylabel('Total Topology Score Sum')
    ax.set_title('Total Topology Score Sum Comparison Over Time')
    ax.set_xlim(0, 50)
    ax.legend()
    ax.grid(True)

    # 保存图形
    img_name = f'RecordDb/output/total_topology_score_sum_comparison_{int(time.time())}.png'
    plt.savefig(img_name)
    plt.close(fig)
    print(f"Total Topology Score Sum比较折线图已保存在 {img_name}")

def calculate_topology_score_stats(db_name):
    conn = sqlite3.connect(f'RecordDb/{db_name}.db')
    query = """
    SELECT time, topologyScoreSum
    FROM datacenterUtilization
    WHERE time < 40*60*60*1000
    ORDER BY time
    """
    df = pd.read_sql_query(query, conn)
    conn.close()
    
    if df.empty:
        print(f"No data for {db_name}")
        return
    
    # 平均值
    mean_val = df['topologyScoreSum'].mean()
    # 中位数
    median_val = df['topologyScoreSum'].median()
    # 最接近40h的记录
    if not df.empty:
        final_val = df.iloc[-1]['topologyScoreSum']  # 最后一条记录
    else:
        final_val = 0.0
    
    print(f"{db_name}:")
    print(f"  Average topologyScoreSum (0-40h): {mean_val}")
    print(f"  Median topologyScoreSum (0-40h): {median_val}")
    print(f"  Final topologyScoreSum at ~40h: {final_val}")
    print()

def show_table():
    # 计算统计值
    results = {}
    for db_name in db_names:
        conn = sqlite3.connect(f'RecordDb/{db_name}.db')
        query = """
        SELECT time, topologyScoreSum, dcId
        FROM datacenterUtilization
        WHERE time < 40*60*60*1000
        ORDER BY time, dcId
        """
        df = pd.read_sql_query(query, conn)
        conn.close()
        
        if df.empty:
            results[db_name] = {}
            continue
        
        dc_ids = sorted(df['dcId'].unique())
        results[db_name] = {}
        for dc_id in dc_ids:
            df_dc = df[df['dcId'] == dc_id]
            if df_dc.empty:
                results[db_name][dc_id] = {'mean': 0.0, 'median': 0.0, 'final': 0.0}
                continue
            # 平均值
            mean_val = df_dc['topologyScoreSum'].mean()
            # 中位数
            median_val = df_dc['topologyScoreSum'].median()
            # 最接近40h的记录
            final_val = df_dc.iloc[-1]['topologyScoreSum'] if not df_dc.empty else 0.0
            
            results[db_name][dc_id] = {'mean': mean_val, 'median': median_val, 'final': final_val}

    # 打印表格
    print("Topology Score Stats (0-40h):")
    print(f"{'DB Name':<35}{'DC':<5}{'Average':>15}{'Median':>15}{'Final':>15}")
    for db_name in db_names:
        for dc_id in sorted(results[db_name].keys()):
            mean = results[db_name][dc_id]['mean']
            median = results[db_name][dc_id]['median']
            final = results[db_name][dc_id]['final']
            print(f"{db_name:<35}{dc_id:<5}{mean:>15.2f}{median:>15.2f}{final:>15.2f}")

    # 打印汇总
    print("\nSummary per DB (Sum of all DCs):")
    print(f"{'DB Name':<35}{'Sum Average':>15}{'Sum Median':>15}{'Sum Final':>15}")
    for db_name in db_names:
        sum_mean = sum(results[db_name][dc]['mean'] for dc in results[db_name])
        sum_median = sum(results[db_name][dc]['median'] for dc in results[db_name])
        sum_final = sum(results[db_name][dc]['final'] for dc in results[db_name])
        print(f"{db_name:<35}{sum_mean:>15.2f}{sum_median:>15.2f}{sum_final:>15.2f}")

# for db_name in db_names:
#     draw_clos_topology_score_for_each(db_name)

# draw_topologyScoreSum_comparison()

draw_total_topology_score_sum_comparison()

show_table()
