import sqlite3
import time
import pandas as pd

# name = "noDelay"
# name = "heartbeat"
name = "shadowResource"
# 连接到SQLite数据库
conn = sqlite3.connect('RecordDb/'+name+'.db')

# 执行SQL查询
query = """
SELECT id, state
FROM userRequest
"""
df = pd.read_sql_query(query, conn)

# 关闭数据库连接
conn.close()

# 计算总请求数
total_requests = len(df)

# 计算失败的请求（状态不是'SUCCESS'的请求）
failed_requests = df[df['state'] != 'SUCCESS']
failed_count = len(failed_requests)

# 计算失败率
failure_rate = failed_count / total_requests

import os

# 确保output文件夹存在
output_dir = 'RecordDb/output'
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

# 将结果写入文件
file_path = f'RecordDb/output/{name}_success_rate'+str(time.time())+'.txt'
with open(file_path, 'w', encoding='utf-8') as f:
    f.write(f"总请求数: {total_requests}\n")
    f.write(f"失败请求数: {failed_count}\n")
    f.write(f"失败率: {failure_rate:.2%}\n")
# 并将结果打印到控制台
print(f"总请求数: {total_requests}")
print(f"失败请求数: {failed_count}")
print(f"失败率: {failure_rate:.2%}")

print(f"结果已写入 {file_path} 文件")