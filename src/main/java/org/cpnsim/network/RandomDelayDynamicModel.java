package org.cpnsim.network;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class RandomDelayDynamicModel implements DelayDynamicModel {
    @Override
    public double getDynamicDelay(int srcId, int dstId, double delay, double time) {
        // Ensure that the dynamic delay of the same two region at the same time is the same
        long seed = hashMap(srcId, dstId, time);
        Random random = new Random(seed);
        return Math.max(delay + random.nextGaussian() * 4, 0);
    }

    private long hashMap(int srcId, int dstId, double time) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // 将参数转换为字节数组
            byte[] srcIdBytes = ByteBuffer.allocate(4).putInt(srcId).array();
            byte[] dstIdBytes = ByteBuffer.allocate(4).putInt(dstId).array();
            byte[] timeBytes = ByteBuffer.allocate(8).putDouble(time).array();

            // 合并字节数组
            byte[] combinedBytes = new byte[srcIdBytes.length + dstIdBytes.length + timeBytes.length];
            System.arraycopy(srcIdBytes, 0, combinedBytes, 0, srcIdBytes.length);
            System.arraycopy(dstIdBytes, 0, combinedBytes, srcIdBytes.length, dstIdBytes.length);
            System.arraycopy(timeBytes, 0, combinedBytes, srcIdBytes.length + dstIdBytes.length, timeBytes.length);

            // 计算哈希值
            byte[] hashBytes = digest.digest(combinedBytes);

            // 将字节数组转换为long类型
            long hashedLong = ByteBuffer.wrap(hashBytes).getLong();

            return hashedLong;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
