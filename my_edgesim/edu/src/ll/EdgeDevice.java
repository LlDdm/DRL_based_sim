package ll;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;


class EdgeDevice{
    private int deviceId;
    private double mips;   // 处理能力
    private final double[] location = new double[2];          // 纬度     // 经度
    private int attractiveness;
    private BlockingQueue<Task> taskQueue;    // 任务队列
    private AtomicLong taskQueueLength;
    private double downloadspeed;
    private double uploadspeed;
    private long Idle;
    private long QueueTask_EstimateMaxComplete;
    private HashSet<Task> taskSets;
    Scheduler scheduler;
    private final ReentrantLock lock;


    public EdgeDevice(int deviceId, double mips, double latitude, double longitude,
                      int attractiveness, double downloadspeed, double uploadspeed, long Idle) {
        this.deviceId = deviceId;
        this.mips = mips;
        this.location[0] = latitude;
        this.location[1] = longitude;
        this.attractiveness = attractiveness;
        this.taskQueue = new LinkedBlockingQueue<>();
        this.taskQueueLength = new AtomicLong(0);
        this.downloadspeed = downloadspeed;
        this.uploadspeed = uploadspeed;
        this.Idle = Idle;
        this.QueueTask_EstimateMaxComplete = 0;
        this.taskSets = new HashSet<>();
        this.lock = new ReentrantLock();
    }

    public int getDeviceId() {
        return deviceId;
    }

    public double getMips() {
        return mips;
    }

    public void addTask(Task task) {
        try {
            taskQueue.put(task);
            taskQueueLength.addAndGet((long)task.getSize());
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void setAttractiveness(int attractiveness) {
        this.attractiveness = attractiveness;
    }
    public int getAttractiveness() {
        return attractiveness;
    }

    public double[] getlocation() {
        return location;
    }

    public BlockingQueue<Task> getTaskQueue() {return taskQueue;}

    public double getDownloadspeed() {
        return downloadspeed;
    }
    public double getUploadspeed() {
        return uploadspeed;
    }

    public long getIdle() { return Idle; }

    public long getTaskQueueLength() { return taskQueueLength.get(); }
    public AtomicLong getTaskQueueLengthAtomic() { return taskQueueLength; }

    public long getQueueTask_EstimateMaxComplete() { return QueueTask_EstimateMaxComplete; }
    public void setQueueTask_EstimateMaxComplete(long queueTask_EstimateMaxComplete) { QueueTask_EstimateMaxComplete = queueTask_EstimateMaxComplete; }

    public HashSet<Task> getTaskSets() {return taskSets;}
    public void addTaskSets(Task task) {
        lock.lock();
        try {
            this.taskSets.add(task);
            if (task.getEstimate_complete_time() > QueueTask_EstimateMaxComplete) {
                QueueTask_EstimateMaxComplete = task.getEstimate_complete_time();
            }
        } finally {
            lock.unlock();
        }
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    // 启动边缘设备，开始监听任务
    public void startDevice() {
        new Thread(() -> {
            while (SimManager.getInstance().isRunning()) {
                if(!taskQueue.isEmpty())
                    listenForTasks();  // 持续监听任务
                else
                    Thread.yield();
            }
        }).start();
    }

    // 监听并处理任务
    public  void listenForTasks() {
        try {
            Task task = taskQueue.take();

            //long startTime = System.currentTimeMillis();
            try {
                // 等待前驱任务到达
                task.wait_pre.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            //long endTime = System.currentTimeMillis();
            //task.setWait_pre_time(endTime - startTime);

            startTask(task);  // 开始执行任务
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // 执行任务
    public void startTask(Task task) {

        double delay =  task.getSize() * 1000 / mips + getIdle();

        //long startTime = System.currentTimeMillis();
        //task.setStarTime(startTime);

        try {
            // 模拟任务执行
            Thread.sleep((long) delay);  // 假设每个任务执行的时间与任务长度成正比
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Task was interrupted,APP: " + task.getAppid() + " task: " + task.get_taskId());
            return;
        }

        //long endTime = System.currentTimeMillis();
        //task.setCompleteTime(endTime);
        //task.setExecutionTime(delay - getIdle());

        taskQueueLength.addAndGet(-(long)task.getSize());
        taskSets.remove(task);
       // System.out.println("Device： " + deviceId + " complete mobile: " + task.getMobileDeviceId() + " APP: " + task.getAppid() + " task: " + task.get_taskId());
        sentOutputs(task); // 传输输出数据
    }

    // 发送任务输出数据到后继任务的设备
    public void sentOutputs(Task task) {
        List<Task> sucTasks = task.getSuccessors();
        for (Task sucTask : sucTasks) {
            if(sucTask.get_taskId() == -2) {
                EndTaskThread endTaskThread = new EndTaskThread(task, sucTask, this);
                endTaskThread.start();
            } else if (sucTask.getDevice_Id() != deviceId) {
                new Thread(() -> startSent(task, sucTask)).start();
            } else {
                sucTask.wait_pre.countDown();
                //task.setOutput_traDelay(sucTask, 0);
            }
        }
    }

    public void startSent(Task currentTask, Task suctask) {
        if (suctask.getDevice_Id() == -1) {
            //long startTime = System.currentTimeMillis();
            try {
                // 等待后继任务分配设备
                suctask.allocate_semaphore.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            //long etime = System.currentTimeMillis();
            //currentTask.setSucLocated_waitTime(suctask, etime - startTime);
        }//else {
            //currentTask.setSucLocated_waitTime(suctask, 0);
        //}
        OutputTransferThread outputTransferThread = new OutputTransferThread(currentTask, suctask,this);
        outputTransferThread.start();

    }

}

