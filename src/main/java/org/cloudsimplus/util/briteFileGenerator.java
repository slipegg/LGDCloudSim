package org.cloudsimplus.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class briteFileGenerator {
    private static Random random = new Random();

    public static void main(String[] args) {
        int nodeNum = 11;
        generateNode(nodeNum);
        generateAccessDelay(1, 10, 12, 2.5);
        generateEdges(0, 10, 20, 4, 1000000, 200000);
    }

    public static void generateNode(int nodeNum) {
        System.out.println("Nodes: id,  x, y");
        for (int i = 0; i < nodeNum; i++) {
            System.out.println(i);
        }
        System.out.println("");
    }

    public static void generateAccessDelay(int nodeIdMin, int nodeIdMax, double accessDelayMean, double accessDelayStd) {
        System.out.println("AccessDelay:source,target,access delay(ms)");
        for (int i = nodeIdMin; i <= nodeIdMax; i++) {
            for (int j = nodeIdMin; j <= nodeIdMax; j++) {
                if (i != j) {
                    double accessDelay = (Math.max((random.nextGaussian() * accessDelayStd + accessDelayMean), 5));
                    accessDelay = BigDecimal.valueOf(accessDelay).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    System.out.println(i + " " + j + " " + accessDelay);
                } else {
                    System.out.println(i + " " + j + " " + 0);
                }
            }
        }
        System.out.println("");
    }

    public static void generateEdges(int nodeIdMin, int nodeIdMax, double linkDelayMean, double linkDelayStd, double bwMean, double bwStd) {
        System.out.println("Edges: id, source, target, delay(ms), bandwidth");
        int id = 0;
        for (int i = nodeIdMin; i <= nodeIdMax; i++) {
            for (int j = i; j <= nodeIdMax; j++) {
                if (i != j) {
                    double linkDelay = (Math.max((random.nextGaussian() * linkDelayStd + linkDelayMean), 5));
                    linkDelay = BigDecimal.valueOf(linkDelay).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    double bw = (Math.max((random.nextGaussian() * bwStd + bwMean), 0));
                    bw = BigDecimal.valueOf(bw).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    System.out.println(id + " " + i + " " + j + " " + linkDelay + " " + bw);
                } else {
                    System.out.println(id + " " + i + " " + j + " " + 0 + " " + 999999999);
                }
                id++;
            }
        }
        System.out.println("");
    }
}
