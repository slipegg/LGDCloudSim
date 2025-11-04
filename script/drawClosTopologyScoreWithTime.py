import time
import pandas as pd
import matplotlib.pyplot as plt
import sqlite3

db_names = ["closTopology-50s-gap", "closTopologyWithoutSort-50s-gap", "topologyBinPackGang-50s-gap", "gpuBinPackGang-50s-gap", "randomGang-50s-gap"]

def draw_clos_topology_score_for_each(db_name):
    # 创建第一个图形（GPU利用率折线图）
    fig1, ax1 = plt.subplots(figsize=(12, 6))
    ax2 = ax1.twinx()  # 创建共享x轴的双y轴

    avg_utilizations = []

    # 为每个数据库绘制GPU利用率折线
    # 连接到SQLite数据库
    conn = sqlite3.connect(f'RecordDb/{db_name}.db')

    # 执行SQL查询
    query = """
    SELECT time, topologyScoreSum, s0Pct30TopologyScore, s0Pct60TopologyScore, s0Pct90TopologyScore, s0MeanTopologyScore
    FROM datacenterUtilization
    Where time < 50*60*60*1000
    ORDER BY time
    """
    df = pd.read_sql_query(query, conn)

    # 关闭数据库连接
    conn.close()

    # 将时间从毫秒转换为小时
    df['time'] = df['time'] / (60 *60 * 1000)

    # 在ax1（左y轴）上绘制S0分数
    ax1.plot(df['time'], df['s0Pct30TopologyScore'], label=f'S0 Pct30', linestyle='-', marker='o', markersize=2)
    ax1.plot(df['time'], df['s0Pct60TopologyScore'], label=f'S0 Pct60', linestyle='--', marker='s', markersize=2)
    ax1.plot(df['time'], df['s0Pct90TopologyScore'], label=f'S0 Pct90', linestyle='-.', marker='^', markersize=2)
    ax1.plot(df['time'], df['s0MeanTopologyScore'], label=f'S0 Mean', linestyle=':', marker='d', markersize=2)
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

    # 为每个数据库绘制Topology Score Sum折线
    for db_name in db_names:
        # 连接到SQLite数据库
        conn = sqlite3.connect(f'RecordDb/{db_name}.db')

        # 执行SQL查询
        query = """
        SELECT time, topologyScoreSum
        FROM datacenterUtilization
        Where time < 50*60*60*1000
        ORDER BY time
        """
        df = pd.read_sql_query(query, conn)

        # 关闭数据库连接
        conn.close()

        # 将时间从毫秒转换为小时
        df['time'] = df['time'] / (60 *60 * 1000)

        # 绘制Topology Score Sum折线
        ax.plot(df['time'], df['topologyScoreSum'], label=f'{db_name} Topology Score Sum')

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



for db_name in db_names:
    draw_clos_topology_score_for_each(db_name)

draw_topologyScoreSum_comparison()

# 计算统计值
results = {}
for db_name in db_names:
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
        results[db_name] = {'mean': 0.0, 'median': 0.0, 'final': 0.0}
        continue
    
    # 平均值
    mean_val = df['topologyScoreSum'].mean()
    # 中位数
    median_val = df['topologyScoreSum'].median()
    # 最接近40h的记录
    final_val = df.iloc[-1]['topologyScoreSum'] if not df.empty else 0.0
    
    results[db_name] = {'mean': mean_val, 'median': median_val, 'final': final_val}

# 打印表格
print("Topology Score Stats (0-40h):")
print(f"{'DB Name':<35}{'Average':>20}{'Median':>20}{'Final':>20}")
for db_name in db_names:
    mean = results[db_name]['mean']
    median = results[db_name]['median']
    final = results[db_name]['final']
    print(f"{db_name:<35}{mean:>20.2f}{median:>20.2f}{final:>20.2f}")
