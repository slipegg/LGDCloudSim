package org.example;
import java.util.Arrays;

public class ResourceDistribution {
    
    public static void main(String[] args) {
        int[] resources = {-5, -3, -1, 2, 3};
        int newResources = 40;
        
        int[] neededResources = distributeAllResources(resources, newResources);
        
        System.out.println("每个人需要增加的资源： " + Arrays.toString(neededResources));
    }

    public static int[] distributeResources(int[] resources, int newResources) {
        int n = resources.length;
        int[] result = new int[n];
        int[] sortedResources = Arrays.copyOf(resources, n);
        Arrays.sort(sortedResources);
        
        // 计算资源均衡到最大值所需的资源量
        int minResource = sortedResources[0];
        int maxResource = sortedResources[n - 1];
        int[] neededResources = new int[n];
        
        // 计算每个人需要增加的资源量
        for (int i = 0; i < n; i++) {
            neededResources[i] = maxResource - resources[i];
        }
        
        int totalRequired = Arrays.stream(neededResources).sum();
        
        if (newResources >= totalRequired) {
            // 新资源足够平衡资源
            for (int i = 0; i < n; i++) {
                result[i] = neededResources[i];
            }
            // 分配剩余资源
            int remainingResources = newResources - totalRequired;
            int equalShare = remainingResources / n;
            int remainder = remainingResources % n;

            for (int i = 0; i < n; i++) {
                result[i] += equalShare;
            }

            // 处理余数，将其分配给前几个
            for (int i = 0; i < remainder; i++) {
                result[i]++;
            }
        } else {
            // 新资源不足以完全平衡资源


        }
        
        return result;
    }

    public static int[] distributeAllResources(int[] resources, int newResources) {
        int[] result = new int[resources.length];
        if(newResources <= 0) {
            return result;
        }
        int consumeResource = 0;
        int currentLevelId = 0;
        int nextLevelId = getNextLevelId(currentLevelId, resources);
        while (nextLevelId < resources.length) {
            int diff = resources[nextLevelId] - resources[currentLevelId];
            consumeResource += diff * (nextLevelId);
            if (newResources < consumeResource) {
                break;
            }
            currentLevelId = nextLevelId;
            nextLevelId = getNextLevelId(currentLevelId, resources);
        }
        if (nextLevelId >= resources.length) {
            // 资源足够平衡
            int diff = newResources - consumeResource;
            int average = diff / resources.length;
            int remainder = diff % resources.length;
            for (int i = 0; i < resources.length; i++) {
                result[i] = average + resources[resources.length - 1] - resources[i];
                if (i < remainder) {
                    result[i]++;
                }
            }
        } else {
            // 资源不够平衡
            consumeResource = 0;
            for(int i = 0; i < currentLevelId; i++) {
                consumeResource += resources[currentLevelId] - resources[i];
            }
            int diff = newResources - consumeResource;
            int average = diff / (nextLevelId);
            int remainder = diff % (nextLevelId);
            for (int i = 0; i < nextLevelId; i++) {
                result[i] = average + resources[currentLevelId] - resources[i];
                if (i < remainder) {
                    result[i]++;
                }
            }
        }

        return result;
    }

    public static int getNextLevelId(int currentLevelId, int[] resources) {
        int nextLevelId ;
        for(nextLevelId = currentLevelId; nextLevelId < resources.length; nextLevelId++) {
            if (resources[nextLevelId] > resources[currentLevelId]) {
                return nextLevelId;
            }
        }
        return nextLevelId;
    }

}