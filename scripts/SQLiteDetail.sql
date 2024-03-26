-- SQLite
DROP VIEW IF EXISTS instanceDelay;
CREATE VIEW IF NOT EXISTS instanceDelay AS
SELECT 
    instance.id, 
    userRequest.submitTime AS submitTime, 
    instance.startTime-userRequest.submitTime AS delay, 
    instanceGroup.interScheduleEndTime-userRequest.submitTime AS interScheduleTime, 
    instanceGroup.receivedTime-instanceGroup.InterScheduleEndTime AS transToDCTime, 
    instance.intraScheduleEndTime-instanceGroup.receivedTime AS intraScheduleTime, 
    instance.startTime-instance.IntraScheduleEndTime AS allocateTime
FROM instance 
LEFT JOIN instanceGroup on instance.instanceGroupId = instanceGroup.id 
LEFT JOIN userRequest on instance.userRequestId = userRequest.id 
Where instance.startTime >= 0;

-- 总体调度时延
SELECT submitTime,AVG(delay),Max(delay),Min(delay) FROM instanceDelay GROUP BY submitTime;

-- 数据中心间调度时延
SELECT submitTime,AVG(interScheduleTime),Max(interScheduleTime),Min(interScheduleTime) FROM instanceDelay GROUP BY submitTime;

-- 发送到数据中心时延
SELECT submitTime,AVG(transToDCTime),Max(transToDCTime),Min(transToDCTime) FROM instanceDelay GROUP BY submitTime;

-- 数据中心内调度时延
SELECT submitTime,AVG(intraScheduleTime),Max(intraScheduleTime),Min(intraScheduleTime) FROM instanceDelay GROUP BY submitTime;

-- 为实例分配资源时延
SELECT submitTime,AVG(allocateTime),Max(allocateTime),Min(allocateTime) FROM instanceDelay GROUP BY submitTime;


-- 调度成功率
SELECT
    submitTime,
    SUM(CASE WHEN state is null THEN 1 ELSE 0 END) AS successNum, COUNT(*) AS sumNum, CAST(SUM(CASE WHEN state IS NULL THEN 1 ELSE 0 END) AS REAL) / COUNT(*) * 100.0 AS successRate
FROM
    userRequest
GROUP BY
    submitTime;


-- 调度失败原因
SELECT submitTime, COUNT(*) AS sumNum, failReason
FROM
    userRequest
WHERE
    state = 'FAILED' 
GROUP BY
    submitTime, failReason
HAVING
    COUNT(*) > 10
ORDER BY 
    submitTime ASC, sumNum DESC;


-- CPU、RAM资源使用情况
SELECT SUM(instance.cpu) AS usedCPU, 25600000 AS sumCPU, CAST(SUM(instance.cpu) AS REAL)/25600000 AS CPURate, SUM(instance.ram) AS usedRAM, 51200000 AS sumRAM, CAST(SUM(instance.ram) AS REAL)/51200000*100.0 AS RAMRate
FROM 
    instance LEFT JOIN instanceGroup on instance.instanceGroupId = instanceGroup.id  
Where 
    instance.finishTime is null  AND instanceGroup.receivedDc!=-1;

-- 带宽使用情况
SELECT 
    IFNULL(SUM(instanceGroupGraph.bw), 0) / 2.0 AS usedBW, 
    43675042.45 AS sumBW, 
    CAST(IFNULL(SUM(instanceGroupGraph.bw), 0) AS REAL) / 2.0 / 43675042.45 * 100.0 AS BWRate
FROM 
    instanceGroupGraph 
LEFT JOIN 
    instanceGroup AS srcInstanceGroup ON instanceGroupGraph.srcInstanceGroupId = srcInstanceGroup.id
LEFT JOIN 
    instanceGroup AS dstInstanceGroup ON instanceGroupGraph.dstInstanceGroupId = dstInstanceGroup.id
WHERE 
    srcInstanceGroup.receivedDc != -1 AND dstInstanceGroup.receivedDc != -1 AND instanceGroupGraph.srcDcId != instanceGroupGraph.dstDcId;