#!/bin/bash

# 设置工作目录
WORK_DIR="/home/ljw/LGDCloudSim"
cd $WORK_DIR

# Java 可执行文件路径
JAVA_EXEC="/usr/lib/jvm/java-17-openjdk-amd64/bin/java"

# 类路径文件
CP_FILE="/tmp/cp_c1cr8sete6ep1dhue3yysyuvb.argfile"

# 主类
MAIN_CLASS="org.example.DLJTrace.DLJTraceTest"

# 实验路径
EXPERIMENT_PATH="$WORK_DIR/src/main/resources/example/DLJTrace"

# 输出目录
OUTPUT_DIR="$WORK_DIR/RecordDb/logs"
mkdir -p $OUTPUT_DIR

# 获取当前时间的函数
current_time() {
    echo $(date +"%Y-%m-%d %H:%M:%S")
}

# 记录开始和结束时间的函数
log_time() {
    start_time=$(date +%s)

    # 执行命令并捕获其 PID
    $JAVA_EXEC @$CP_FILE $MAIN_CLASS "$@" 2>&1 &
    local pid=$!

    # 等待进程完成
    wait $pid

    end_time=$(date +%s)
    runtime=$((end_time - start_time))
    echo "[$(current_time)] $1 finished. Runtime: ${runtime} seconds"
}

# 定义存储 PIDs 的数组
PIDS=()

log_time "randomGang-100s-gap" $EXPERIMENT_PATH"/datacenter/randomGang.json" $EXPERIMENT_PATH"/HostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_50.0_gap_100.0.csv" &
PIDS+=($!)

log_time "gpuBinPackGang-100s-gap" $EXPERIMENT_PATH"/datacenter/gpuBinPackGang.json" $EXPERIMENT_PATH"/HostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_50.0_gap_100.0.csv" &
PIDS+=($!)

log_time "topologyBinPackGang-100s-gap" $EXPERIMENT_PATH"/datacenter/topologyBinPackGang.json" $EXPERIMENT_PATH"/HostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_50.0_gap_100.0.csv" &
PIDS+=($!)

log_time "closTopology-100s-gap" $EXPERIMENT_PATH"/datacenter/closTopology.json" $EXPERIMENT_PATH"/HostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_50.0_gap_100.0.csv" &
PIDS+=($!)


log_time "randomGang-75s-gap" $EXPERIMENT_PATH"/datacenter/randomGang.json" $EXPERIMENT_PATH"/HostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_37.0_gap_75.0.csv" &
PIDS+=($!)

log_time "gpuBinPackGang-75s-gap" $EXPERIMENT_PATH"/datacenter/gpuBinPackGang.json" $EXPERIMENT_PATH"/HostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_37.0_gap_75.0.csv" &
PIDS+=($!)

log_time "topologyBinPackGang-75s-gap" $EXPERIMENT_PATH"/datacenter/topologyBinPackGang.json" $EXPERIMENT_PATH"/HostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_37.0_gap_75.0.csv" &
PIDS+=($!)

log_time "closTopology-75s-gap" $EXPERIMENT_PATH"/datacenter/closTopology.json" $EXPERIMENT_PATH"/HostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_37.0_gap_75.0.csv" &
PIDS+=($!)


log_time "randomGang-50s-gap" $EXPERIMENT_PATH"/datacenter/randomGang.json" $EXPERIMENT_PATH"/HostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_25.0_gap_50.0.csv" &
PIDS+=($!)

log_time "gpuBinPackGang-50s-gap" $EXPERIMENT_PATH"/datacenter/gpuBinPackGang.json" $EXPERIMENT_PATH"/HostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_25.0_gap_50.0.csv" &
PIDS+=($!)

log_time "topologyBinPackGang-50s-gap" $EXPERIMENT_PATH"/datacenter/topologyBinPackGang.json" $EXPERIMENT_PATH"/HostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_25.0_gap_50.0.csv" &
PIDS+=($!)

log_time "closTopology-50s-gap" $EXPERIMENT_PATH"/datacenter/closTopology.json" $EXPERIMENT_PATH"/HostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_25.0_gap_50.0.csv" &
PIDS+=($!)

# 定义处理 SIGINT 信号的函数
cleanup() {
    echo "Stopping all tasks..."
    kill "${PIDS[@]}" 2>/dev/null
    wait "${PIDS[@]}" 2>/dev/null
    exit 1
}

# 捕捉 SIGINT 信号并调用 cleanup 函数
trap cleanup SIGINT

# 等待所有后台进程完成
wait "${PIDS[@]}"