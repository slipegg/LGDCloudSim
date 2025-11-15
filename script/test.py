import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import os
import sqlite3

db_names = [
    "Random-12s-gap",
    "GPUFirst-12s-gap",
    "ScoreFirst-12s-gap",
    "TopoAlign-12s-gap"
]

query = """
    SELECT AVG(i.startTime - ur.submitTime) AS average_submit_time_delay
        FROM instance i
        JOIN userRequest ur ON i.userRequestId = ur.id
        WHERE i.startTime IS NOT NULL AND ur.submitTime IS NOT NULL;
"""

for db_name in db_names:
    conn = sqlite3.connect(f'RecordDb/{db_name}.db')
    print(f"Results for {db_name}:")
    df = pd.read_sql_query(query, conn)
    print("Average Submit Time Delay (s):", df['average_submit_time_delay'][0]/1000)
    conn.close()
    print()
