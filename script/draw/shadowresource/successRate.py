import sqlite3
import pandas as pd

name = "noDelay"
# name = "heartbeat"
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

# 将结果写入文件
with open(f'{name}_success_rate.txt', 'w', encoding='utf-8') as f:
    f.write(f"总请求数: {total_requests}\n")
    f.write(f"失败请求数: {failed_count}\n")
    f.write(f"失败率: {failure_rate:.2%}\n")

print(f"结果已写入 results_{name}.txt 文件")