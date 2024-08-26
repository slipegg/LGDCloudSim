-- SQLite
-- SELECT * FROM userRequest WHERE state != 'SUCCESS' OR state IS NULL;
-- SELECT * FROM userRequest WHERE failReason != '';
SELECT * FROM userRequest WHERE finishTime - submitTime < 1000 AND failReason != '';
-- SELECT * FROM datacenterUtilization WHERE cpuUtilization <= 0;