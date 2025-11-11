package ll;

import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
//import java.util.concurrent.locks.ReentrantLock;

class Task {
    private int id; // 任务ID
    private double size; // 任务大小
    private int APPid;
    //private long startime;
    //private long completetime;
    //private long executionTime;
    private Map<Task, Double> predecessors; // 前驱任务列表及边的大小
    private Map<Task, Double> successors;   // 后继任务列表及边的大小
    private double R;
    private int device_Id;
    //private Map<Task,Long> output_traDelay;
    //private Map<Task,Long> sucLocated_waitTime;
    //private long wait_pre_time;
    //private Map<Task,Long> waitPre_time;
    //private long ScheduleQueue_waitTime;
    //private long tra_delay;
    private long estimate_start_time;
    private long estimate_complete_time;
    //private long estimate_scheduleQueue_waitTime;
    //private long allocated_time;
    //private long arrival_time;
    //public final Object deviceObject;
    //public final Object arrivalObject;
    private int mobileDeviceId;
    //private boolean is_complete;
    private List<Integer> predicting_device_Id;
    private long predicting_complete_time;
    private int maxDelay_device_Id;
    public Semaphore allocate_semaphore = new Semaphore(0);
    public CountDownLatch wait_pre;

    Task(int id, double size, int APPid, int mobileDeviceId) {
        this.id = id;
        this.size = size;
        this.APPid = APPid;
        this.predecessors = new HashMap<>();
        this.successors = new HashMap<>();
        this.device_Id = -1;
        //this.output_traDelay = new HashMap<>();
        //this.sucLocated_waitTime = new HashMap<>();
        //this.allocated_time = 0;
        //this.arrival_time = 0;
        //this.startime = 0;
        //this.completetime = 0;
        //this.executionTime = 0;
        this.R = 0;
        //this.ScheduleQueue_waitTime = 0;
        //this.tra_delay = 0;
        this.estimate_start_time = 0;
        this.estimate_complete_time = 0;
        //this.estimate_scheduleQueue_waitTime = 0;
        //this.deviceObject = new Object();
        //this.arrivalObject = new Object();
        //this.waitPre_time = new HashMap<>();
        //this.wait_pre_time = 0;
        this.mobileDeviceId = mobileDeviceId;
        //this.is_complete = false;
        this.predicting_device_Id = new ArrayList<>();
        this.predicting_complete_time = 0;
        this.maxDelay_device_Id = 0;
    }

    // 添加前驱任务并记录边的大小
    public void addPredecessor(Task task, double edgeSize) {
        this.predecessors.put(task, edgeSize);
    }
    // 添加后继任务并记录边的大小
    public void addSuccessor(Task task, double edgeSize) {
        this.successors.put(task, edgeSize);
    }

    public List<Task> getPredecessors() {
        return new ArrayList<>(predecessors.keySet());
    }
    public List<Task> getSuccessors() {
        return new ArrayList<>(successors.keySet());
    }

    public Map<Task, Double> getPredecessorsMap() {
        return predecessors;
    }
    public Map<Task, Double> getSuccessorsMap() {
        return successors;
    }

    //public Map<Task, Long> getOutputTraDelay() { return output_traDelay; }
    //public void setOutput_traDelay(Task task, long output_traDelay) { this.output_traDelay.put(task,output_traDelay); }

    public void setDevice_Id(int device_Id) {
        this.device_Id = device_Id;
    }
    public int getDevice_Id() {
        return device_Id;
    }

    public double getSize() {
        return size;
    }
    public void setSize(long size) {
        this.size = size;
    }

    public int getAppid() {
        return APPid;
    }
    public void setAppid(int appid) {
        this.APPid = appid;
    }

    //public long getStarTime() {return startime;}
    //public void setStarTime(long time) {this.startime = time;}

    //public long getCompleteTime() {return completetime;}
    //public void setCompleteTime(long time) {this.completetime = time;}

    public double getR() {
        return R;
    }
    public void setR(double time) {
        this.R = time;
    }

    public void set_task_Id(int id) {
        this.id = id;
    }
    public int get_taskId() {
        return id;
    }

    //public void setExecutionTime(long executionTime) {this.executionTime = executionTime;}
    //public long getExecutionTime() {return executionTime;}

    //public void setScheduleQueue_waitTime(long queue_waitTime) { this.ScheduleQueue_waitTime = queue_waitTime; }
    //public long getScheduleQueue_waitTime() { return ScheduleQueue_waitTime; }

    //public void setSucLocated_waitTime(Task task,long waitTime) { this.sucLocated_waitTime.put(task,waitTime); }
    //public Map<Task, Long> getSucLocated_waitTime() { return sucLocated_waitTime; }

    //public void setTra_delay(long delay) { this.tra_delay = delay; }
    //public long getTra_delay() { return tra_delay; }

    public void setEstimate_start_time(long start_time) { this.estimate_start_time = start_time; }
    public long getEstimate_start_time() { return estimate_start_time; }

    public  void setEstimate_complete_time(long complete_time) { this.estimate_complete_time = complete_time; }
    public long getEstimate_complete_time() { return estimate_complete_time; }

    //public  void setEstimate_scheduleQueue_waitTime(long waitTime) { this.estimate_scheduleQueue_waitTime = waitTime; }
    //public long getEstimate_scheduleQueue_waitTime() { return estimate_scheduleQueue_waitTime; }

    //public void setAllocate_time(long allocate_time) { this.allocated_time = allocate_time; }
    //public long getAllocate_time() { return allocated_time; }

    //public void setArrival_time(long arrival_time) { this.arrival_time = arrival_time; }
    //public long getArrival_time() { return arrival_time; }

//    public void setWaitPre_time(Task task,long waitTime) { this.waitPre_time.put(task,waitTime); }
//    public Map<Task, Long> getWaitPre_time() { return waitPre_time; }
    //public void setWait_pre_time(long pre_time) { this.wait_pre_time = pre_time; }
    //public long getWait_pre_time() { return wait_pre_time; }

    public void setMobileDeviceId(int mobileDeviceId) { this.mobileDeviceId = mobileDeviceId; }
    public int getMobileDeviceId() { return mobileDeviceId; }

    //public boolean isComplete() { return is_complete; }
    //public void setIsComplete(boolean is_complete) { this.is_complete = is_complete; }

    public List<Integer> getPredicting_device_Id() { return predicting_device_Id; }
    public void addPredicting_device_Id(List<Integer> ids) { this.predicting_device_Id.addAll(ids); }
    public void setPredicting_device_Id(List<Integer> ids) { this.predicting_device_Id = ids; }

    public void setPredicting_complete_time(long time) { this.predicting_complete_time = time; }
    public long getPredicting_complete_time() { return predicting_complete_time; }

    public void setMaxDelay_device_Id(int id){ this.maxDelay_device_Id = id;}
    public int getMaxDelay_device_Id() { return maxDelay_device_Id; }


@Override
    public String toString() {
        return "Task{id=" + id + ", size=" + size + "}";
    }
}

