package ll;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class Cloud extends EdgeDevice {
    private final ExecutorService executorService;

    public Cloud(int id , double mips, double latitude, double longitude, double downloadSpeed, double uploadSpeed, long Idle,
                 int attractiveness) {
        super(id, mips, latitude, longitude, attractiveness, downloadSpeed, uploadSpeed, Idle);
        this.executorService = Executors.newFixedThreadPool(4); // 创建线程池

    }

    @Override
    // 监听并处理任务
    public void listenForTasks() {
        try {
            Task task = getTaskQueue().take();  // 等待队列中任务到来

            //long startTime = System.currentTimeMillis();
            try {
                // 等待前驱任务到达
                task.wait_pre.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            //long endTime = System.currentTimeMillis();
            //task.setWait_pre_time(endTime - startTime);

            executorService.submit(() -> startTask(task));  // 开始执行任务
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

//    @Override
//    // 执行任务
//    public void startTask(Task task) {
//        double delay =  task.getSize() / getMips() + getIdle();
//
//        //long startTime = System.currentTimeMillis();
//        //task.setStarTime(startTime);
//
//        try {
//            // 模拟任务执行
//            Thread.sleep((long) delay);  // 假设每个任务执行的时间与任务长度成正比
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            System.out.println("Task was interrupted,APP: " + task.getAppid() + " task: " + task.get_taskId());
//            return;
//        }
//
//        //long endTime = System.currentTimeMillis();
//        //task.setCompleteTime(endTime);
//        //task.setExecutionTime(delay - getIdle());
//
//        sentOutputs(task); // 传输输出数据
//    }

//    @Override
//    //添加任务
//    public void addTask(Task task) {
//        try {
//            getTaskQueue().put(task);
//        }catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    @Override
//    public void addTaskSets(Task task) {}
//
//    @Override
//    public long getQueueTask_EstimateMaxComplete() { return 0; }
//
//    @Override
//    public HashSet<Task> getTaskSets() {return null;}

}
