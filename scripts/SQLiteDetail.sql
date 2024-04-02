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
SELECT
    SUM(instance.cpu) AS usedCPU, 
    -- 25600000 AS sumCPU, 
    datacenterSum.sumCPU AS sumCPU, 
    CAST(SUM(instance.cpu) AS REAL)/datacenterSum.sumCPU*100.0 AS CPURate, 
    -- 51200000 AS sumRAM, 
    SUM(instance.ram) AS usedRAM, 
    datacenterSum.sumRAM AS sumRAM, 
    CAST(SUM(instance.ram) AS REAL)/datacenterSum.sumRAM*100.0 AS RAMRate
FROM 
    instance 
LEFT JOIN 
    instanceGroup on instance.instanceGroupId = instanceGroup.id  
LEFT JOIN
    (
        SELECT SUM(cpu) AS sumCPU, SUM(ram) AS sumRAM 
        FROM datacenter
    ) AS datacenterSum
WHERE 
    instance.finishTime is null  AND instanceGroup.receivedDc!=-1;

-- 带宽使用情况
SELECT 
    IFNULL(SUM(instanceGroupGraph.bw), 0) / 2.0 AS usedBW, 
    -- 43675042.45 AS sumBW, 
    -- CAST(IFNULL(SUM(instanceGroupGraph.bw), 0) AS REAL) / 2.0 / 43675042.45 * 100.0 AS BWRate
    dcNetworkSum.sumBW AS sumBW, 
    CAST(IFNULL(SUM(instanceGroupGraph.bw), 0) AS REAL) / 2.0 / dcNetworkSum.sumBW * 100.0 AS BWRate
FROM 
    instanceGroupGraph 
LEFT JOIN 
    instanceGroup AS srcInstanceGroup ON instanceGroupGraph.srcInstanceGroupId = srcInstanceGroup.id
LEFT JOIN 
    instanceGroup AS dstInstanceGroup ON instanceGroupGraph.dstInstanceGroupId = dstInstanceGroup.id
LEFT JOIN
    (
        SELECT SUM(bw) AS sumBW 
        FROM dcNetwork 
        WHERE srcDatacenterId != dstDatacenterId
    ) AS dcNetworkSum
WHERE 
    srcInstanceGroup.receivedDc != -1 AND dstInstanceGroup.receivedDc != -1 AND instanceGroupGraph.srcDcId != instanceGroupGraph.dstDcId;


-- 每个数据中心的资源使用情况
SELECT
    instanceGroup.receivedDc AS dcId, 
    SUM(instance.cpu) AS usedCPU, 
    datacenterSum.sumCPU AS sumCPU, 
    CAST(SUM(instance.cpu) AS REAL)/datacenterSum.sumCPU*100.0 AS CPURate, 
    SUM(instance.ram) AS usedRAM, 
    datacenterSum.sumRAM AS sumRAM, 
    CAST(SUM(instance.ram) AS REAL)/datacenterSum.sumRAM*100.0 AS RAMRate
FROM 
    instance 
LEFT JOIN 
    instanceGroup on instance.instanceGroupId = instanceGroup.id  
LEFT JOIN
    (
        SELECT datacenter.id AS id, SUM(cpu) AS sumCPU, SUM(ram) AS sumRAM 
        FROM datacenter
        GROUP BY datacenter.id
    ) AS datacenterSum
    ON instanceGroup.receivedDc = datacenterSum.id
WHERE 
    instance.finishTime is null AND instanceGroup.receivedDc!=-1
GROUP BY
    instanceGroup.receivedDc;


-- 每条网络的带宽使用情况
SELECT 
    instanceGroupGraph.srcDcId AS srcDcId, 
    instanceGroupGraph.dstDcId AS dstDcId,
    IFNULL(SUM(instanceGroupGraph.bw), 0) / 2.0 AS usedBW, 
    dcNetworkSum.sumBW AS sumBW, 
    CAST(IFNULL(SUM(instanceGroupGraph.bw), 0) AS REAL) / 2.0 / dcNetworkSum.sumBW * 100.0 AS BWRate
FROM 
    instanceGroupGraph 
LEFT JOIN 
    instanceGroup AS srcInstanceGroup ON instanceGroupGraph.srcInstanceGroupId = srcInstanceGroup.id
LEFT JOIN 
    instanceGroup AS dstInstanceGroup ON instanceGroupGraph.dstInstanceGroupId = dstInstanceGroup.id
LEFT JOIN
    (
        SELECT srcDatacenterId, dstDatacenterId, SUM(bw) AS sumBW 
        FROM dcNetwork 
        GROUP BY srcDatacenterId, dstDatacenterId
    ) AS dcNetworkSum 
    ON instanceGroupGraph.srcDcId = dcNetworkSum.srcDatacenterId AND instanceGroupGraph.dstDcId = dcNetworkSum.dstDatacenterId
WHERE 
    srcInstanceGroup.receivedDc != -1 AND dstInstanceGroup.receivedDc != -1 AND instanceGroupGraph.srcDcId != instanceGroupGraph.dstDcId
GROUP BY
    instanceGroupGraph.srcDcId, instanceGroupGraph.dstDcId;