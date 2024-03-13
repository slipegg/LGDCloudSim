-- SQLite
CREATE VIEW IF NOT EXISTS instanceDelay AS
SELECT instance.id,userRequest.submitTime AS submitTime,instance.startTime-userRequest.submitTime AS delay
FROM instance LEFT JOIN userRequest on instance.userRequestId = userRequest.id  Where instance.startTime >= 0;


SELECT submitTime,AVG(delay),Max(delay),Min(delay) FROM instanceDelay GROUP BY submitTime;


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


SELECT
    submitTime,
    SUM(CASE WHEN state is null THEN 1 ELSE 0 END) AS successNum, COUNT(*) AS sumNum
FROM
    userRequest
GROUP BY
    submitTime;


SELECT instanceGroup.receivedDc, COUNT(*), SUM(instance.cpu),SUM(instance.ram) FROM instance LEFT JOIN instanceGroup on instance.instanceGroupId = instanceGroup.id  Where instance.finishTime is null  AND instanceGroup.receivedDc!=-1
GROUP BY instanceGroup.receivedDc;