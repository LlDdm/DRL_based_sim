package ll;

import org.apache.commons.math3.analysis.function.Max;
import org.apache.poi.ss.formula.functions.T;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

class Scheduler {
    private PriorityBlockingQueue<APP> apps;
    EdgeDevice this_edgeDevice;


    public Scheduler(EdgeDevice this_edgeDevice) {
        this.apps = new PriorityBlockingQueue<>();
        this.this_edgeDevice = this_edgeDevice;
    }

    public void addApp(APP app) {
        apps.put(app);
    }

    // 启动调度器
    public void startDevice(String orchestratorPolicy,double timeout_tolerance) {
        // 接收APP,并调度
        new Thread(() -> {
            while (SimManager.getInstance().isRunning()) {
                if(!apps.isEmpty()) {
                    listenForApps(orchestratorPolicy,timeout_tolerance);  // 持续监听app
                }else {
                    Thread.yield();
                }
            }
        }).start();
    }

    // 将收到的app的b-level排序存入调度队列中
    private void listenForApps(String orchestratorPolicy,double timeout_tolerance) {
            try {
                APP app = apps.take();// 获取队列中的app
                List<Task>sortedTask = B_levelSort(app);
                sortedTask.removeIf(task -> task.get_taskId() == -1 || task.get_taskId() == -2);
                scheduleTasks(orchestratorPolicy,sortedTask,app,timeout_tolerance);
            }catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
    }

    private void scheduleTasks(String orchestratorPolicy, List<Task>sortedTask,APP app,double timeout_tolerance) {
        List<EdgeDevice> edgeDevices = SimManager.getInstance().getEdgeDeviceGeneratorModel().getEdge_devices();
        NetWork netWork = SimManager.getInstance().getNetworkModel();
        List<MobileDevice> mobileDevices = SimManager.getInstance().getLoadGeneratorModel().getMobileDevices();
        MobileDevice this_mobileDevice = mobileDevices.get(app.getMobileDeviceId());
        Map<Integer, EdgeDevice> deviceMap = edgeDevices.stream().collect(Collectors.toMap(EdgeDevice::getDeviceId, EdgeDevice -> EdgeDevice));
        Map<Integer,EdgeDevice> nativeEdge = SimManager.getInstance().getNativeEdgeDeviceGenerator().getNativeDevicesMap();

        switch (orchestratorPolicy) {
            case "COFE":
                allocateTasksToDevices_COFE(deviceMap, sortedTask, netWork, this_mobileDevice, app,nativeEdge);
                break;
            case "DCDS":
                allocateTasksToDevices_DCDS(deviceMap, sortedTask, netWork, this_mobileDevice, app,nativeEdge);
                break;
            case "LL":
                allocateTasksToDevices_LL(deviceMap, sortedTask, netWork, this_mobileDevice, app, timeout_tolerance,nativeEdge);
                break;
            case "RAN":
                allocateTasksToDevices_RAN(deviceMap, sortedTask);
                break;
            default:
                System.out.println("没找到对应算法");
                break;
        }
    }

    // 为任务选择合适的设备
    private void allocateTasksToDevices_COFE(Map<Integer, EdgeDevice> devices, List<Task> sorted_tasks, NetWork netWork, MobileDevice mobileDevice,
                                             APP app,Map<Integer,EdgeDevice> nativeDevice) {
        EdgeDevice cloud = devices.get(0);
//        devices.remove(0);
        //一次对一个应用的所有任务进行调度
        for(Task task : sorted_tasks) {
            DAG dag = app.getDag();
            EdgeDevice targetDevice = null;
            List<EdgeDevice> matchedDevices = new ArrayList<>(); // 存储满足第一步要求的设备
            List<Task> preTasks = task.getPredecessors();
            long Min_EstimateComplete_time = Long.MAX_VALUE;

            // COFE第一步，找最小前延迟的设备
            for (Map.Entry<Integer, EdgeDevice> entry : devices.entrySet()) {
                //计算前延迟
                long Max_PreOutputPrepared_time = 0;
                for (Task pre : preTasks) {
                    double tra_delay;
                    if (pre.get_taskId() == -1) {
                        tra_delay = calculate_MtoE_Delay(pre,task,mobileDevice,entry.getValue(),nativeDevice,netWork);
                    } else {
                        EdgeDevice preDevice;
//                        if(pre.getDevice_Id() == 0)
//                            preDevice = cloud;
//                        else
                        preDevice = devices.get(pre.getDevice_Id());
                        tra_delay = calculate_EtoE_Delay(pre,task,preDevice,entry.getValue(),nativeDevice,netWork);
                    }
                    long pre_outputPrepared_time = pre.getEstimate_complete_time() + (long)tra_delay;
                    if (pre_outputPrepared_time > Max_PreOutputPrepared_time) {
                        Max_PreOutputPrepared_time = pre_outputPrepared_time;
                    }
                }

                //计算任务的估计开始时间，选择数据等待时间和队列等待时间的最大值
                task.setEstimate_start_time(Math.max(entry.getValue().getQueueTask_EstimateMaxComplete(), Max_PreOutputPrepared_time));
                //估计完成时间
                long task_estimate_complete_time = task.getEstimate_start_time() + (long)(task.getSize() / entry.getValue().getMips()) + entry.getValue().getIdle();
                task.setEstimate_complete_time(task_estimate_complete_time);

                if (task_estimate_complete_time <= Min_EstimateComplete_time) {
                    if (task_estimate_complete_time < Min_EstimateComplete_time) {
                        Min_EstimateComplete_time = task_estimate_complete_time;

                        if (matchedDevices.isEmpty()) {
                            matchedDevices.add(entry.getValue());
                        } else {
                            matchedDevices.clear();
                            matchedDevices.add(entry.getValue());
                        }
                    } else {
                        matchedDevices.add(entry.getValue());
                    }
                }
            }

//            if(task.getEstimate_complete_time() > app.getDeadline()) {
////                targetDevice = cloud;
//            }else {
            // COFE第二步和第三步
            if (matchedDevices.size() == 1) {
                targetDevice = matchedDevices.get(0);
            } else if (matchedDevices.size() > 1) {
                matchedDevices.remove(cloud);
                long Min_Idle = Long.MAX_VALUE;
                EdgeDevice min_Idle_device = null;
                int flag = 0;
                for (EdgeDevice edgedevice : matchedDevices) {
                    for (Task preTask : preTasks) {
                        if (edgedevice.getTaskSets().contains(preTask) && dag.getCriticalTasks().contains(preTask) && dag.getCriticalTasks().contains(task)) {
                            targetDevice = edgedevice;
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 1) {
                        break;
                    }
                    if (edgedevice.getIdle() < Min_Idle) {
                        Min_Idle = edgedevice.getIdle();
                        min_Idle_device = edgedevice;
                    }
                }
                if (flag == 0) {
                    targetDevice = min_Idle_device;
                }
            }
//            }

            // 将任务交给传输线程传输给目标设备
            assert targetDevice != null;
            task.setDevice_Id(targetDevice.getDeviceId());
            targetDevice.addTask(task);
            task.allocate_semaphore.release();
            targetDevice.addTaskSets(task);
            //System.out.println("mobile:" + task.getMobileDeviceId() + " APP:" + task.getAppid() + " task:" + task.get_taskId() + " device:" + targetDevice.getDeviceId());
        }
    }

    private void allocateTasksToDevices_DCDS(Map<Integer, EdgeDevice> devices, List<Task> sorted_tasks, NetWork netWork, MobileDevice mobileDevice, APP app,
                                             Map<Integer,EdgeDevice> nativeDevice) {
        EdgeDevice cloud = devices.get(0);
//        devices.remove(0);

        for (Task task : sorted_tasks) {
            EdgeDevice targetDevice = null;
            List<Task> preTasks = task.getPredecessors();
            long Min_EstimateComplete_time_and_averageDelay = Long.MAX_VALUE;

            // 找最小前延迟的设备
            for (Map.Entry<Integer, EdgeDevice> entry : devices.entrySet()) {
                long Max_PreOutputPrepared_time = 0;
                for (Task pre : preTasks) {
                    double tra_delay;
                    if (pre.get_taskId() == -1) {
                        tra_delay = calculate_MtoE_Delay(pre,task,mobileDevice,entry.getValue(),nativeDevice,netWork);
                    } else {
                        EdgeDevice preDevice;
//                        if(pre.getDevice_Id() == 0)
//                            preDevice = cloud;
//                        else
                        preDevice = devices.get(pre.getDevice_Id());
                        tra_delay = calculate_EtoE_Delay(pre,task,preDevice,entry.getValue(),nativeDevice,netWork);
                    }
                    long pre_outputPrepared_time = pre.getEstimate_complete_time() + (long)tra_delay;
                    if (pre_outputPrepared_time > Max_PreOutputPrepared_time) {
                        Max_PreOutputPrepared_time = pre_outputPrepared_time;
                    }
                }

                //获取任务的估计开始时间时间和估计完成时间
                task.setEstimate_start_time(Math.max(entry.getValue().getQueueTask_EstimateMaxComplete(), Max_PreOutputPrepared_time));
                long task_estimate_complete_time = task.getEstimate_start_time() + (long)(task.getSize() / entry.getValue().getMips()) + entry.getValue().getIdle();
                task.setEstimate_complete_time(task_estimate_complete_time);

                //计算当前所选设备到其他所有设备的平均传输延迟
                double OutputAverageTra_delay = 0;
//                double distance;
//                long BW;
//                if (task.getSuccessors().contains(app.getendTask())) {
//                    long EtoN_delay;
//                    if (entry.getValue().getDeviceId() == this_edgeDevice.getDeviceId()) {
//                        EtoN_delay = 0;
//                    } else {
//                        distance = calculateDistance(this_edgeDevice.getlocation(), entry.getValue().getlocation());
//                        BW = netWork.BWmap.get(targetDevice).get(entry.getValue());
//                        EtoN_delay = EstimateTra_delay(task, app.getendTask(), distance, BW, this_edgeDevice.getDownloadspeed(), entry.getValue().getUploadspeed());
//                    }
//                    OutputAverageTra_delay += EtoN_delay + MtoN_delay;
//                }
//                // 移除云
//                Map<Integer,EdgeDevice> edgeServer = new HashMap<>();
//                for(Map.Entry<Integer, EdgeDevice> entry1 : devices.entrySet())
//                    if(entry1.getKey() != 0)
//                        edgeServer.put(entry1.getKey(), entry1.getValue());

                for (Map.Entry<Integer, EdgeDevice> other_entry : devices.entrySet()) {
                    OutputAverageTra_delay += calculate_avg_delay(entry.getValue(),other_entry.getValue(),nativeDevice,netWork);
                }
                OutputAverageTra_delay = OutputAverageTra_delay / devices.size();

                long estimateComplete_time_and_average_delay = task_estimate_complete_time + (long)OutputAverageTra_delay;
                if (estimateComplete_time_and_average_delay < Min_EstimateComplete_time_and_averageDelay) {
                    Min_EstimateComplete_time_and_averageDelay = estimateComplete_time_and_average_delay;
                    targetDevice = entry.getValue();
                }
            }

//            if(task.getEstimate_complete_time() > app.getDeadline())
//                targetDevice = cloud;

            // 将任务交给传输线程传输给目标设备
            assert targetDevice != null;
            task.setDevice_Id(targetDevice.getDeviceId());
            targetDevice.addTask(task);
            task.allocate_semaphore.release();
            targetDevice.addTaskSets(task);
        }
    }

    private void allocateTasksToDevices_LL(Map<Integer, EdgeDevice> devices, List<Task> sorted_tasks, NetWork netWork,MobileDevice mobileDevice,
                                           APP app,double timeout_tolerance,Map<Integer,EdgeDevice> nativeDevice) {
        EdgeDevice cloud = devices.get(0);
//        devices.remove(0);
        //反向预测每个任务可能被分配到的设备
        backward_predicting(sorted_tasks, devices, netWork, nativeDevice,this_edgeDevice,app);
        Collections.reverse(sorted_tasks);

//        if(isAppOverdue(devices,sorted_tasks,app,mobileDevice,netWork,timeout_tolerance,nativeDevice)){
//            System.out.println("mobileDevice:" + mobileDevice.getDeviceId() + " app:" + app.getAppid() + " 可能超过截止时间，应提交给云执行");
//            for(Task task : sorted_tasks){
//                task.setDevice_Id(cloud.getDeviceId());
//                cloud.addTask(task);
//                task.allocate_semaphore.release();
//                cloud.addTaskSets(task);
//            }
//            return;
//        }

        for (Task task : sorted_tasks) {
            DAG dag = app.getDag();
            EdgeDevice targetDevice = null;
            List<EdgeDevice> matchedDevices = new ArrayList<>(); // 存储满足第一步要求的设备
            List<Task> preTasks = task.getPredecessors();
            long Min_EstimateComplete_time_and_averageDelay = Long.MAX_VALUE;

            //获取当前任务的直接后继任务的预测服务器
            Map<Integer, EdgeDevice> suc_PredictingDevices = getSuc_PredictingDevices(task, devices);

            // 第一步，找当前任务估计完成时间+输出数据平均传输时间最小的设备
            for (Map.Entry<Integer, EdgeDevice> entry : devices.entrySet()) {
                long Max_PreOutputPrepared_time = Long.MIN_VALUE;
                for (Task pre : preTasks) {
                    double tra_delay;
                    if (pre.get_taskId() == -1) {
                        tra_delay = calculate_MtoE_Delay(pre,task,mobileDevice,entry.getValue(),nativeDevice,netWork);
                    } else {
                        EdgeDevice preDevice = devices.get(pre.getDevice_Id());
                        tra_delay = calculate_EtoE_Delay(pre,task,preDevice,entry.getValue(),nativeDevice,netWork);
                    }
                    long pre_outputPrepared_time = pre.getEstimate_complete_time() + (long)tra_delay;
                    if (pre_outputPrepared_time > Max_PreOutputPrepared_time) {
                        Max_PreOutputPrepared_time = pre_outputPrepared_time;
                    }
                }

                task.setEstimate_start_time(Math.max(entry.getValue().getQueueTask_EstimateMaxComplete(), Max_PreOutputPrepared_time));
                long task_estimate_complete_time = task.getEstimate_start_time() + (long)(task.getSize() / entry.getValue().getMips()) + entry.getValue().getIdle();
                task.setEstimate_complete_time(task_estimate_complete_time);

                //与直接后继任务的预测设备输出延迟最小
                double OutputAverageTra_delay = 0;

//                if (task.getSuccessors().contains(app.getendTask())) {
//                    long EtoN_delay;
//                    if (entry.getValue().getDeviceId() == this_edgeDevice.getDeviceId()) {
//                        EtoN_delay = 0;
//                    } else {
//                        distance = calculateDistance(this_edgeDevice.getlocation(), entry.getValue().getlocation());
//                        BW = netWork.BWmap.get(this_edgeDevice).get(entry.getValue());
//                        EtoN_delay = EstimateTra_delay(task, app.getendTask(), distance, BW, this_edgeDevice.getDownloadspeed(), entry.getValue().getUploadspeed());
//                    }
//                    OutputAverageTra_delay += EtoN_delay + MtoN_delay;
//                    sucDevice_num += 1;
//                }
                for (Map.Entry<Integer, EdgeDevice> other_entry : suc_PredictingDevices.entrySet())
                        OutputAverageTra_delay += calculate_avg_delay(entry.getValue(),other_entry.getValue(),nativeDevice,netWork);

                OutputAverageTra_delay = OutputAverageTra_delay / suc_PredictingDevices.size();


                long estimateComplete_time_and_average_delay = task_estimate_complete_time + (long)OutputAverageTra_delay;

                if (estimateComplete_time_and_average_delay <= Min_EstimateComplete_time_and_averageDelay) {
                    if (estimateComplete_time_and_average_delay < Min_EstimateComplete_time_and_averageDelay) {
                        Min_EstimateComplete_time_and_averageDelay = estimateComplete_time_and_average_delay;
                        if (!matchedDevices.isEmpty()) {
                            matchedDevices.clear();
                        }
                        matchedDevices.add(entry.getValue());

                    } else {
                        matchedDevices.add(entry.getValue());
                    }
                }
            }


            // 利用COFE第二步和第三步优化
            if (matchedDevices.size() == 1) {
                targetDevice = matchedDevices.get(0);
            } else if (matchedDevices.size() > 1) {
                matchedDevices.remove(cloud);
                long Min_Idle = Long.MAX_VALUE;
                EdgeDevice min_Idle_device = null;
                int flag = 0;
                for (EdgeDevice edgedevice : matchedDevices) {
                    for (Task preTask : preTasks) {
                        if (edgedevice.getTaskSets().contains(preTask) && dag.getCriticalTasks().contains(preTask) && dag.getCriticalTasks().contains(task)) {
                            targetDevice = edgedevice;
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 1) {
                        break;
                    }
                    if (edgedevice.getIdle() < Min_Idle) {
                        Min_Idle = edgedevice.getIdle();
                        min_Idle_device = edgedevice;
                    }
                }
                if (flag == 0) {
                    targetDevice = min_Idle_device;
                }
            }

            // 将任务交给传输线程传输给目标设备
            assert targetDevice != null;
            task.setDevice_Id(targetDevice.getDeviceId());
            targetDevice.addTask(task);
            task.allocate_semaphore.release();
            targetDevice.addTaskSets(task);
        }
    }

    public void allocateTasksToDevices_RAN(Map<Integer,EdgeDevice> devices, List<Task> sorted_tasks) {
        devices.remove(0);
        for(Task task : sorted_tasks) {
            int rand_deviceID = (int)(Math.random()*devices.size() + 1);
            EdgeDevice targetDevice = devices.get(rand_deviceID);
            assert targetDevice != null;
            task.setDevice_Id(targetDevice.getDeviceId());
            targetDevice.addTask(task);
            task.allocate_semaphore.release();
            targetDevice.addTaskSets(task);
        }
    }

    private double calculate_EtoE_Delay(Task srcTask,Task dstTask,EdgeDevice srcDevice,EdgeDevice dstDevice,Map<Integer,EdgeDevice> nativeEdge,NetWork netWork) {
        double delay1 = 0;
        double delay2 = 0;
        double outputSize = srcTask.getSuccessorsMap().get(dstTask);
        if(srcDevice != dstDevice) {
            if(srcDevice.getAttractiveness() != dstDevice.getAttractiveness()) {
                EdgeDevice nativeServer = nativeEdge.get(srcDevice.getAttractiveness());
                if(srcDevice != nativeServer) {
                    double distance_delay2 = calculateDistance_delay(srcDevice.getlocation(),nativeServer.getlocation());
                    double BW2 = netWork.BWmap.get(srcDevice).get(nativeServer);
//                    delay2 = outputSize / BW2 + distance_delay2 + outputSize * 4 / srcDevice.getUploadspeed() + outputSize * 4 / nativeServer.getDownloadspeed();
                    delay2 = outputSize * BW2 / 1000 + distance_delay2;
                    srcDevice = nativeServer;
                }
            }

            double distance1 = calculateDistance_delay(srcDevice.getlocation(),dstDevice.getlocation());
            double BW1 = netWork.BWmap.get(srcDevice).get(dstDevice);
            delay1 = outputSize * BW1/1000 + distance1; // + outputSize * 4 / srcDevice.getUploadspeed() + outputSize * 4 / dstDevice.getDownloadspeed();
            delay1 += delay2;
        }
        return delay1;
    }

    private double calculate_avg_delay(EdgeDevice srcDevice,EdgeDevice dstDevice,Map<Integer,EdgeDevice> nativeEdge,NetWork netWork) {
        double delay1 = 0;
        double delay2 = 0;
        double outputSize = 10000;
        if(srcDevice != dstDevice) {
            if(srcDevice.getAttractiveness() != dstDevice.getAttractiveness()) {
                EdgeDevice nativeServer = nativeEdge.get(srcDevice.getAttractiveness());
                if(srcDevice != nativeServer) {
                    double distance_delay2 = calculateDistance_delay(srcDevice.getlocation(),nativeServer.getlocation());
                    double BW2 = netWork.BWmap.get(srcDevice).get(nativeServer);
                    delay2 = outputSize * BW2/1000 + distance_delay2;//+ outputSize * 4 / srcDevice.getUploadspeed() + outputSize * 4 / nativeServer.getDownloadspeed();
                    srcDevice = nativeServer;
                }
            }

            double distance1 = calculateDistance_delay(srcDevice.getlocation(),dstDevice.getlocation());
            double BW1 = netWork.BWmap.get(srcDevice).get(dstDevice);
            delay1 = outputSize * BW1/1000 + distance1; //+ outputSize * 4 / srcDevice.getUploadspeed() + outputSize * 4 / dstDevice.getDownloadspeed();
            delay1 += delay2;
        }
        return delay1;
    }

    private double calculate_MtoE_Delay(Task srcTask,Task dstTask,MobileDevice mobileDevice,EdgeDevice dstDevice,Map<Integer,EdgeDevice> nativeEdge,NetWork netWork){
        double delay1 = 0;
        double delay2 = 0;
        double outputSize = srcTask.getSuccessorsMap().get(dstTask);
        EdgeDevice nativeServer = nativeEdge.get(mobileDevice.getDevice_attractiveness());

        double distance_delay1 = calculateDistance_delay(mobileDevice.getDevice_location(),nativeServer.getlocation());
        double BW1 = netWork.mobileBW.get(mobileDevice);
        delay1 = outputSize * BW1/1000 + distance_delay1;// + outputSize * 4 /mobileDevice.getUploadSpeed() + outputSize * 4 / nativeServer.getDownloadspeed();

        if(nativeServer != dstDevice) {
            if(nativeServer.getAttractiveness() != dstDevice.getAttractiveness()) {
                double distance_delay2 = calculateDistance_delay(nativeServer.getlocation(), dstDevice.getlocation());
                double BW2 = netWork.BWmap.get(nativeServer).get(dstDevice);
                delay2 = outputSize * BW2/1000 + distance_delay2;// + outputSize * 4 / nativeServer.getUploadspeed() + outputSize * 4 / dstDevice.getDownloadspeed();
            }
        }

        delay1 += delay2;
        return delay1;
    }

    private double calculate_EtoM_Delay(Task srcTask,Task endTask,EdgeDevice srcDevice,MobileDevice mobileDevice,Map<Integer,EdgeDevice> nativeEdge,NetWork netWork){
        double delay1 = 0;
        double delay2 = 0;
        double delay3 = 0;
        double outputSize = srcTask.getSuccessorsMap().get(endTask);
        EdgeDevice nativeServer = nativeEdge.get(srcDevice.getAttractiveness());
        EdgeDevice nativeServer_M = nativeEdge.get(mobileDevice.getDevice_attractiveness());

        if(srcDevice != nativeServer) {
            double distance_delay1 = calculateDistance_delay(srcDevice.getlocation(),nativeServer.getlocation());
            double BW1 = netWork.BWmap.get(nativeServer).get(srcDevice);
            delay1 = outputSize * BW1/1000 + distance_delay1;// + outputSize * 4 / srcDevice.getUploadspeed() + outputSize * 4 / nativeServer.getDownloadspeed();
        }

        if(nativeServer != nativeServer_M){
            double distance_delay2 = calculateDistance_delay(nativeServer.getlocation(),nativeServer_M.getlocation());
            double BW2 = netWork.BWmap.get(nativeServer).get(nativeServer_M);
            delay2 = outputSize / BW2 + distance_delay2;// + outputSize * 4 / nativeServer.getUploadspeed() + outputSize * 4 / nativeServer_M.getDownloadspeed();
        }

        double distance_delay3 = calculateDistance_delay(mobileDevice.getDevice_location(),nativeServer_M.getlocation());
        double BW3 = netWork.mobileBW.get(mobileDevice);
        delay3 = outputSize / BW3 + distance_delay3;// + outputSize * 4 /nativeServer_M.getUploadspeed() + outputSize * 4 / mobileDevice.getDownloadSpeed();

        delay1 += delay2 + delay3;
        return delay1;
    }

    private double calculateDistance_delay(double[] this_location, double[] other_location) {
        double dLat = this_location[0] - other_location[0];
        double dLon = this_location[1] - other_location[1];

        return Math.ceil(Math.sqrt(Math.pow(dLat,2) + Math.pow(dLon, 2)) * 1000 *1000 / 299792458);  // Returns the distance in kilometers
    }

//    private long EstimateTra_delay(Task predecessor, Task thisTask, double distance, double BW, long downloadSpeed, long uploadSpeed){
//        long outputSize = predecessor.getSuccessorsMap().get(thisTask);
//        return (long) (outputSize / BW + distance / 299792458 * 1000 + (double) (outputSize * 1000) / downloadSpeed + (double) (outputSize * 1000) / uploadSpeed);
//
//    }

//    private long EstimateTra_avgDelay(double distance, double BW, long downloadSpeed, long uploadSpeed){
//        return (long) (1000 / BW + distance / 299792458 * 1000 + (double) (1000 * 1000) / downloadSpeed + (double) (1000 * 1000) / uploadSpeed);
//    }

    private Map<Integer,EdgeDevice> getSuc_PredictingDevices(Task task,Map<Integer,EdgeDevice> devices){
        Map<Integer,EdgeDevice> result = new HashMap<>();
        for(Task suc : task.getSuccessors()){
            for(int id : suc.getPredicting_device_Id()){
                result.put(id,devices.get(id));
            }
        }
        return result;
    }

    public List<Task> B_levelSort(APP app) {
        List<Task> revisedTasks = app.getDag().getTpoSort();
        Collections.reverse(revisedTasks);

        for (Task task : revisedTasks) {
            if (task.getSuccessors() == null || task.getSuccessors().isEmpty()) {
                task.setR(0);
            }else {
                List<Task> successors = task.getSuccessors();
                double R = Long.MIN_VALUE;
                for(Task successor : successors) {
                    if(successor.getR() + task.getSuccessorsMap().get(successor) > R) {
                        R = successor.getR() + task.getSuccessorsMap().get(successor);
                    }
                }
                task.setR(R);
            }
        }

        revisedTasks.sort(Comparator.comparing(Task::getR).reversed());

        return revisedTasks;
    }

    public void backward_predicting(List<Task> sorted_tasks,Map<Integer, EdgeDevice> devices, NetWork netWork,
                                    Map<Integer,EdgeDevice> nativeEdge,EdgeDevice this_nativeEdge,APP app) {
        Collections.reverse(sorted_tasks);
        List<Integer> entTask_predicting = new ArrayList<>();
        entTask_predicting.add(this_nativeEdge.getDeviceId());
        app.getendTask().addPredicting_device_Id(entTask_predicting);

        for(Task task : sorted_tasks) {
            double tra_delay = 0;
            int sucDevice_num = 0;
            double Min_delay = Double.MAX_VALUE;
            List<Integer> matchedDevices_id = new ArrayList<>();

//            for(Task su : task.getSuccessors()){
//                for (int ignored : su.getPredicting_device_Id()) {
//                    sucDevice_num++;
//                }
//            }

            for(Map.Entry<Integer, EdgeDevice> entry : devices.entrySet()){
                //task.setPredicting_complete_time(entry.getValue().getQueueTask_EstimateMaxComplete() + (long)(task.getSize() / entry.getValue().getMips()) + entry.getValue().getIdle());
                task.setPredicting_complete_time((long) (task.getSize() / entry.getValue().getMips()) + entry.getValue().getIdle());
                for(Task suc : task.getSuccessors()) {
                    for (int deviceID : suc.getPredicting_device_Id()) {
                        EdgeDevice Pred_sucDevice = devices.get(deviceID);
                        tra_delay += calculate_EtoE_Delay(task,suc,entry.getValue(),Pred_sucDevice,nativeEdge,netWork);
                        sucDevice_num++;
                    }
                }

                tra_delay = tra_delay / sucDevice_num;
                if(tra_delay <= Min_delay) {
                    if(tra_delay < Min_delay){
                        Min_delay = tra_delay;
                        if(!matchedDevices_id.isEmpty()){
                            matchedDevices_id.clear();
                        }
                        matchedDevices_id.add(entry.getValue().getDeviceId());
                    }
                    else {
                        matchedDevices_id.add(entry.getValue().getDeviceId());
                    }
                }
            }
            task.addPredicting_device_Id(matchedDevices_id);
        }
    }

//    public boolean isAppOverdue(Map<Integer,EdgeDevice> devices,List<Task> sorted_tasks,APP app,MobileDevice mobileDevice,NetWork netWork,
//                                double timeout_tolerance,Map<Integer,EdgeDevice> nativeEdge) {
//        for(Task task : sorted_tasks) {
//            Map<Integer,EdgeDevice> predictingDevices = new HashMap<>();
//            for(int id : task.getPredicting_device_Id()){
//                predictingDevices.put(id,devices.get(id));
//            }
//            long Max_predicting_complete_time = Long.MIN_VALUE;
//
//            for(Map.Entry<Integer, EdgeDevice> entry : predictingDevices.entrySet()) {
//                long Max_PreOutputPrepared_time = Long.MIN_VALUE;
//                for (Task pre : task.getPredecessors()) {
//                    long tra_delay;
//                    if (pre.get_taskId() == -1) {
//                        tra_delay = calculate_MtoE_Delay(pre,task,mobileDevice,entry.getValue(),nativeEdge,netWork);
//                    } else {
//                        EdgeDevice preEdgeDevice = devices.get(pre.getMaxDelay_device_Id());
//                        tra_delay = calculate_EtoE_Delay(pre,task,preEdgeDevice,entry.getValue(),nativeEdge,netWork);
//                    }
//
//                    long pre_outputPrepared_time = pre.getPredicting_complete_time() + tra_delay;
//                    if (pre_outputPrepared_time > Max_PreOutputPrepared_time) {
//                        Max_PreOutputPrepared_time = pre_outputPrepared_time;
//                    }
//                }
//
//                //计算任务的预测完成时间
//                long task_predicting_complete_time = (long) (Math.max(entry.getValue().getQueueTask_EstimateMaxComplete(), Max_PreOutputPrepared_time) +
//                        Math.ceil(task.getSize()  / entry.getValue().getMips()) + entry.getValue().getIdle());
//                if (task_predicting_complete_time > Max_predicting_complete_time) {
//                    Max_predicting_complete_time = task_predicting_complete_time;
//                    task.setMaxDelay_device_Id(entry.getValue().getDeviceId());
//                }
//            }
//            task.setPredicting_complete_time(Max_predicting_complete_time);
//        }
//
//        Task endTask = app.getendTask();
//        long max_end_traDelay = Long.MIN_VALUE;
//        for(Task pre : endTask.getPredecessors()) {
//            long tra_delay;
//            EdgeDevice preEdgeDevice = devices.get(pre.getMaxDelay_device_Id());
//            tra_delay = calculate_EtoM_Delay(pre,endTask,preEdgeDevice,mobileDevice,nativeEdge,netWork);
//            if(tra_delay + pre.getPredicting_complete_time() > max_end_traDelay) {
//                max_end_traDelay = tra_delay + pre.getPredicting_complete_time();
//            }
//        }
//        endTask.setPredicting_complete_time(max_end_traDelay);
//
//        return app.getDeadline()*(1+timeout_tolerance) < endTask.getPredicting_complete_time();
//    }

}

