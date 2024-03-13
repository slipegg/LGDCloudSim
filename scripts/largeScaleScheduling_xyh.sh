#!/bin/bash

# 版本信息：
# mvn 4.0.0
# java 17.0.10

homeDir="/home/xyh/LGDCloudSim"
scriptDir=$homeDir"/scripts"
javaFile=$homeDir"/src/main/java/org/example/largeScaleScheduling_xyh.java"
javaPkg="org.example.largeScaleScheduling_xyh"

# 定义三个字符串列表
list_testRequest=("complexRequest" "simpleRequest") # ("complexRequest" "simpleRequest")
list_testTime=("intermittent") # ("intermittent" "continued")
list_testAlgorithm=("1-heuristic" "2-HFRS" "3-RFHS" "4-random") # "1-heuristic" "2-HFRS" "3-RFHS" "4-random"

echo ""
echo ""
echo "当前时间是：$(date +'%Y-%m-%d %H:%M:%S')"
echo "【编译中...】"
cd $homeDir
mvn clean compile
echo "【编译完成！】"
echo ""
echo ""

# 记录开始时间（初始值为0）
start_seconds=$SECONDS

# 遍历每个列表并将元素作为参数传给Java程序
for testRequest in "${list_testRequest[@]}"; do
  for testTime in "${list_testTime[@]}"; do
    for testAlgorithm in "${list_testAlgorithm[@]}"; do
      
      echo "【运行 $testRequest-$testTime-$testAlgorithm 中...】"
      start_seconds_tmp=$SECONDS
       mvn exec:java -Dexec.mainClass="$javaPkg" -Dexec.args="$testRequest  $testTime $testAlgorithm" > $scriptDir/logs/$testRequest-$testTime-$testAlgorithm.log 2>&1 
      echo "【完成运行 $testRequest-$testTime-$testAlgorithm ！ 花费 $((SECONDS - start_seconds_tmp)) s。】"
      echo ""
    done
  done
done

echo "【完成所有运行！ 花费 $((SECONDS - start_seconds)) s。】"
echo ""
echo ""