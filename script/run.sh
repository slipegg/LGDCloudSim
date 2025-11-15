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
    echo "[$(current_time)] $1 finished. Runtime: ${runtime} seconds, Log saved to $OUTPUT_DIR/$1.log"
}

# 定义处理 SIGINT 信号的函数
cleanup() {
    echo "Stopping all tasks..."
    kill "${PIDS[@]}" 2>/dev/null
    wait "${PIDS[@]}" 2>/dev/null
    exit 1
}

# 定义存储 PIDs 的数组
PIDS=()

run_single_dc() {
    log_time "Random-100s-gap" $EXPERIMENT_PATH"/singleDatacenter/randomGang.json" $EXPERIMENT_PATH"/singleDatacenter/singleDCHostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_50.0_gap_100.0.csv" &
    PIDS+=($!)

    log_time "GPUBinPack-100s-gap" $EXPERIMENT_PATH"/singleDatacenter/gpuBinPackGang.json" $EXPERIMENT_PATH"/singleDatacenter/singleDCHostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_50.0_gap_100.0.csv" &
    PIDS+=($!)

    log_time "S0BinPack-100s-gap" $EXPERIMENT_PATH"/singleDatacenter/topologyBinPackGang.json" $EXPERIMENT_PATH"/singleDatacenter/singleDCHostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_50.0_gap_100.0.csv" &
    PIDS+=($!)

    log_time "TopoAlign-Random-100s-gap" $EXPERIMENT_PATH"/singleDatacenter/closTopologyRandom.json" $EXPERIMENT_PATH"/singleDatacenter/singleDCHostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_50.0_gap_100.0.csv" &
    PIDS+=($!)

    log_time "TopoAlign-100s-gap" $EXPERIMENT_PATH"/singleDatacenter/closTopology.json" $EXPERIMENT_PATH"/singleDatacenter/singleDCHostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_50.0_gap_100.0.csv" &
    PIDS+=($!)


    log_time "Random-75s-gap" $EXPERIMENT_PATH"/singleDatacenter/randomGang.json" $EXPERIMENT_PATH"/singleDatacenter/singleDCHostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_37.0_gap_75.0.csv" &
    PIDS+=($!)

    log_time "GPUBinPack-75s-gap" $EXPERIMENT_PATH"/singleDatacenter/gpuBinPackGang.json" $EXPERIMENT_PATH"/singleDatacenter/singleDCHostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_37.0_gap_75.0.csv" &
    PIDS+=($!)

    log_time "S0BinPack-75s-gap" $EXPERIMENT_PATH"/singleDatacenter/topologyBinPackGang.json" $EXPERIMENT_PATH"/singleDatacenter/singleDCHostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_37.0_gap_75.0.csv" &
    PIDS+=($!)

    log_time "TopoAlign-Random-75s-gap" $EXPERIMENT_PATH"/singleDatacenter/closTopologyRandom.json" $EXPERIMENT_PATH"/singleDatacenter/singleDCHostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_37.0_gap_75.0.csv" &
    PIDS+=($!)

    log_time "TopoAlign-75s-gap" $EXPERIMENT_PATH"/singleDatacenter/closTopology.json" $EXPERIMENT_PATH"/singleDatacenter/singleDCHostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_37.0_gap_75.0.csv" &
    PIDS+=($!)


    log_time "Random-50s-gap" $EXPERIMENT_PATH"/singleDatacenter/randomGang.json" $EXPERIMENT_PATH"/singleDatacenter/singleDCHostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_25.0_gap_50.0.csv" &
    PIDS+=($!)

    log_time "GPUBinPack-50s-gap" $EXPERIMENT_PATH"/singleDatacenter/gpuBinPackGang.json" $EXPERIMENT_PATH"/singleDatacenter/singleDCHostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_25.0_gap_50.0.csv" &
    PIDS+=($!)

    log_time "S0BinPack-50s-gap" $EXPERIMENT_PATH"/singleDatacenter/topologyBinPackGang.json" $EXPERIMENT_PATH"/singleDatacenter/singleDCHostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_25.0_gap_50.0.csv" &
    PIDS+=($!)

    log_time "TopoAlign-Random-50s-gap" $EXPERIMENT_PATH"/singleDatacenter/closTopologyRandom.json" $EXPERIMENT_PATH"/singleDatacenter/singleDCHostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_25.0_gap_50.0.csv" &
    PIDS+=($!)

    log_time "TopoAlign-50s-gap" $EXPERIMENT_PATH"/singleDatacenter/closTopology.json" $EXPERIMENT_PATH"/singleDatacenter/singleDCHostTopoConfig.csv" $EXPERIMENT_PATH"/job_sampled_time_144000_scale_25.0_gap_50.0.csv" &
    PIDS+=($!)
}

run_multi_dc() {
    # log_time "Random-29s-gap" $EXPERIMENT_PATH"/multiDC/gpuRandom.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_14.0_gap_29.0.csv" &
    # PIDS+=($!)

    # log_time "GPUFirst-29s-gap" $EXPERIMENT_PATH"/multiDC/gpuFirst.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_14.0_gap_29.0.csv" &
    # PIDS+=($!)

    # log_time "ScoreFirst-29s-gap" $EXPERIMENT_PATH"/multiDC/topologyScoreFirst.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_14.0_gap_29.0.csv" &
    # PIDS+=($!)

    # log_time "TopoAlign-29s-gap" $EXPERIMENT_PATH"/multiDC/closTopology.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_14.0_gap_29.0.csv" &
    # PIDS+=($!)

    
    # log_time "Random-22s-gap" $EXPERIMENT_PATH"/multiDC/gpuRandom.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_11.0_gap_22.0.csv" &
    # PIDS+=($!)

    # log_time "GPUFirst-22s-gap" $EXPERIMENT_PATH"/multiDC/gpuFirst.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_11.0_gap_22.0.csv" &
    # PIDS+=($!)

    # log_time "ScoreFirst-22s-gap" $EXPERIMENT_PATH"/multiDC/topologyScoreFirst.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_11.0_gap_22.0.csv" &
    # PIDS+=($!)

    # log_time "TopoAlign-22s-gap" $EXPERIMENT_PATH"/multiDC/closTopology.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_11.0_gap_22.0.csv" &
    # PIDS+=($!)

    
    # log_time "Random-15s-gap" $EXPERIMENT_PATH"/multiDC/gpuRandom.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_7.0_gap_15.0.csv" &
    # PIDS+=($!)

    # log_time "GPUFirst-15s-gap" $EXPERIMENT_PATH"/multiDC/gpuFirst.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_7.0_gap_15.0.csv" &
    # PIDS+=($!)

    # log_time "ScoreFirst-15s-gap" $EXPERIMENT_PATH"/multiDC/topologyScoreFirst.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_7.0_gap_15.0.csv" &
    # PIDS+=($!)

    # log_time "TopoAlign-15s-gap" $EXPERIMENT_PATH"/multiDC/closTopology.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_7.0_gap_15.0.csv" &
    # PIDS+=($!)

        
    # log_time "Random-12s-gap" $EXPERIMENT_PATH"/multiDC/gpuRandom.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_6.0_gap_12.0.csv" &
    # PIDS+=($!)

    log_time "GPUFirst-12s-gap" $EXPERIMENT_PATH"/multiDC/gpuFirst.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_6.0_gap_12.0.csv" &
    PIDS+=($!)

    log_time "ScoreFirst-12s-gap" $EXPERIMENT_PATH"/multiDC/topologyScoreFirst.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_6.0_gap_12.0.csv" &
    PIDS+=($!)

    log_time "TopoAlign-12s-gap" $EXPERIMENT_PATH"/multiDC/closTopology.json" $EXPERIMENT_PATH"/multiDC/HostTopoConfig.csv" $EXPERIMENT_PATH"/multiDC/job_sampled_time_144000_scale_6.0_gap_12.0.csv" &
    PIDS+=($!)
}

run_large_experiment() {
    scale="x5"
    types=("3D_DP" "3D_PP")
    replicate_nums=(2048 3072)
    for type in "${types[@]}"; do
        for replicate_num in "${replicate_nums[@]}"; do
            log_time "largescale_"$scale"_"$type"_"$replicate_num $EXPERIMENT_PATH"/largeScale/dcs/closTopology_"$scale".json" $EXPERIMENT_PATH"/largeScale/dcs/LargeDCHostTopoConfig_"$scale".csv" $EXPERIMENT_PATH"/largeScale/jobs/"$type"_"$replicate_num".csv" &
            wait "${PIDS[@]}"
            sleep 5
        done
    done
}

# run_single_dc
# run_multi_dc
run_large_experiment

# 捕捉 SIGINT 信号并调用 cleanup 函数
trap cleanup SIGINT

# 等待所有后台进程完成
wait "${PIDS[@]}"