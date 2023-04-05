import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

public class QuickSort {
    public static void quickSort(int[] arr) {
        if (arr == null || arr.length == 0) {
            return;
        }
        Stack<Integer> stack = new Stack<>();
        stack.push(0);
        stack.push(arr.length - 1);
        while (!stack.isEmpty()) {
            int right = stack.pop();
            int left = stack.pop();
            int pivotIndex = partition(arr, left, right);
            if (left < pivotIndex - 1) {
                stack.push(left);
                stack.push(pivotIndex - 1);
            }
            if (right > pivotIndex + 1) {
                stack.push(pivotIndex + 1);
                stack.push(right);
            }
        }
    }

    private static int partition(int[] arr, int left, int right) {
        int pivot = arr[left];
        while (left < right) {
            while (left < right && arr[right] >= pivot) {
                right--;
            }
            arr[left] = arr[right];
            while (left < right && arr[left] <= pivot) {
                left++;
            }
            arr[right] = arr[left];
        }
        arr[left] = pivot;
        return left;
    }

    public static void main(String[] args) {
        int host_num = 5_000_000;
        int[] nums = new int[host_num * 4];
        for (int i = 0; i < host_num * 4; i++) {
            Random rand = new Random();
            nums[i] = rand.nextInt(10000);
        }
        double start = System.currentTimeMillis();
        Arrays.sort(nums);
//        quickSort(nums);
//        for (int num : nums) {
//            System.out.print(num + " ");
//        }
        double end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start) + "ms");
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory(); //总内存
        long freeMemory = runtime.freeMemory(); //空闲内存
        long usedMemory = totalMemory - freeMemory; //已用内存
        System.out.println("hostNum: " + host_num);
        System.out.println("totalMemory: " + totalMemory / 1000000 + " Mb");
        System.out.println("freeMemory: " + freeMemory / 1000000 + " Mb");
        System.out.println("usedMemory: " + usedMemory / 1000000 + " Mb");
    }
}