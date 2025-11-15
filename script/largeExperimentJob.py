import pandas as pd
import os

# 读取原始CSV文件
original_df = pd.read_csv('/home/ljw/LGDCloudSim/src/main/resources/example/DLJTrace/largeScale/job_sample.csv')

# 定义instance_replicate及其对应的dp_dim和pp_dim
replicate_configs = {
    3072: (128, 24),
    4096: (128, 32),
    2048: (128, 16),
    1024: (64, 16),
    512: (64, 8),
    256: (32, 8),
    128: (32, 4),
    64: (16, 4),
    32: (8, 4),
    16: (8, 2),
    8: (4, 2)
}

# 定义strategy_type
strategy_types = ['3D_PP', '3D_DP']

# 输出目录
output_dir = '/home/ljw/LGDCloudSim/src/main/resources/example/DLJTrace/largeScale/jobs'
os.makedirs(output_dir, exist_ok=True)

# 为每个组合生成CSV文件
for instance_replicate, (dp_dim, pp_dim) in replicate_configs.items():
    for strategy_type in strategy_types:
        # 复制原始DataFrame
        df = original_df.copy()
        
        # 修改字段
        df['instance_replicate'] = instance_replicate
        df['strategy_type'] = strategy_type
        df['dp_dim'] = dp_dim
        df['pp_dim'] = pp_dim
        
        # 修改job_name以区分不同文件
        df['job_name'] = f"{df['job_name'].iloc[0]}_{instance_replicate}_{strategy_type.replace('_', '').lower()}"
        
        # 生成文件名
        filename = f"{strategy_type}_{instance_replicate}.csv"
        filepath = os.path.join(output_dir, filename)
        
        # 保存CSV文件
        df.to_csv(filepath, index=False)
        print(f"Generated: {filepath}")

print("All CSV files generated successfully!")