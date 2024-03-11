#!/bin/bash

javaFile="../src/main/java/org/example/largeScaleScheduling_xyh.java"

# 定义三个字符串列表
list_testRequest=("simpleRequest" "complexRequest")
list_testTime=("intermittent") # ("intermittent" "continued")
list_testAlgorithm=("1-heuristic" "2-HFRS" "3-RFHS" "4-random")

# 遍历每个列表并将元素作为参数传给Java程序
for testRequest in "${list_A[@]}"; do
  for testTime in "${list_B[@]}"; do
    for testAlgorithm in "${list_C[@]}"; do
      java $javaFile "$testRequest" "$testTime" "$testAlgorithm" > $testRequest-$testTime-$testAlgorithm.log 2>&1 
    done
  done
done