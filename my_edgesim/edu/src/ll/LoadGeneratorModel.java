package ll;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.poi.ss.formula.functions.T;

public class LoadGeneratorModel {
    private final Random rng;
    protected int numberOfMobileDevices;
    //protected long simulationTime;
    private long loadGenerate_Time;
    private long AppStart_time_offset;
    private int App_num;

    protected String simScenario;
    private String useScenario;

    private List<MobileDevice> mobileDevices;

//    private int[][] mobileDeviceCluster_num;

    public LoadGeneratorModel(int _numberOfMobileDevices, String _simScenario, String _useScenario,long _loudGenerate_time,
                              long AppStart_time_offset) {
        this.numberOfMobileDevices =  _numberOfMobileDevices;
        //this.simulationTime = _simulationTime;
        this.simScenario = _simScenario;
        this.rng = ThreadLocalRandom.current();
        this.mobileDevices = new ArrayList<>();
//        this.mobileDeviceCluster_num = new int[SimSettings.getInstance().Attractiveness_NUM][3];
        this.useScenario = _useScenario;
        this.loadGenerate_Time = _loudGenerate_time;
        this.AppStart_time_offset = AppStart_time_offset;
        this.App_num = 0;
    }

    public List<MobileDevice> getMobileDevices() {
        return mobileDevices;
    }

//    public int[][] getMobileDeviceCluster() { return mobileDeviceCluster_num; }

    public String getUseScenario() { return useScenario; }

    private int generateNormalint(int mean, int stdDev){
        return (int) Math.round(rng.nextGaussian() * stdDev + mean);
    }

    private long generateNormalLong(long mean, long stdDev) {
        return Math.round(rng.nextGaussian() * stdDev + mean);
    }

    private int generateUniformInt(int min, int max) {return rng.nextInt(min, max + 1);}

    public int getApp_num(){return App_num;}


    public void initializeModel() {
        double [][] APPlookuptable = SimSettings.getInstance().getAppLookUpTable();
        double avg_comPow = SimManager.getInstance().getEdgeDeviceGeneratorModel().avg_speed;
        double avg_tra = SimManager.getInstance().getNetworkModel().avg_traSpeed;
        double InAndOut = SimSettings.getInstance().InAndOut_ratio;

        if (APPlookuptable == null || APPlookuptable.length == 0)
            throw new IllegalStateException("App lookup table is not initialized or is empty.");

        long base_line_start_time = System.currentTimeMillis();

        //Each mobile device utilizes an app type (task type)
        for(int i=0; i<numberOfMobileDevices; i++) {
            List<APP> apps = new ArrayList<>();
            int app_id = 0;
            int randomAppType = -1;
            double appTypeSelector = SimUtils.getRandomDoubleNumber(0, 100);
            double appTypePercentage = 0;
            for (int j = 0; j < APPlookuptable.length; j++) {
                appTypePercentage += APPlookuptable[j][0];
                if (appTypeSelector <= appTypePercentage) {
                    randomAppType = j;
                    break;
                }
            }
            if (randomAppType == -1) {
                System.out.println("Impossible is occurred! no random task type!");
                continue;
            }
            String AppName = SimSettings.getInstance().getAppName(randomAppType);

            //随机连接方式：0,lan or 1,wlan or 2,GSM
            int randomConnectionType = 1;
//            double connectionTypeSelector = SimUtils.getRandomDoubleNumber(0, 100);
//            double connectionTypePercentage = 0;
//            for (int j = 0; j < SimSettings.getInstance().getconnectiontypeLookUpTable().length; j++) {
//                connectionTypePercentage += SimSettings.getInstance().getconnectiontypeLookUpTable()[j][0];
//                if (connectionTypeSelector <= connectionTypePercentage) {
//                    randomconnectionType = j;
//                    break;
//                }
//            }
//            if (randomconnectionType == -1) {
//                System.out.println("Impossible is occurred! no random connection type!");
//                continue;
//            }


            double poissonMean = APPlookuptable[randomAppType][1];
            double activePeriod = APPlookuptable[randomAppType][2] ;
//            double idlePeriod = APPlookuptable[randomAppType][3];
//            double activePeriodStartTime = SimUtils.getRandomDoubleNumber(
//                    SimSettings.CLIENT_ACTIVITY_START_TIME ,
//                    (SimSettings.CLIENT_ACTIVITY_START_TIME + activePeriod));  //active period starts shortly after the simulation started 5 - 45
//            double virtualTime = activePeriodStartTime;
            double virtualTime = SimUtils.getRandomDoubleNumber(
                    SimSettings.CLIENT_ACTIVITY_START_TIME ,
                    (SimSettings.CLIENT_ACTIVITY_START_TIME + activePeriod));  //active period starts shortly after the simulation started 5 - 45

            ExponentialDistribution ps = new ExponentialDistribution(poissonMean);

            //                      180
            while (virtualTime < loadGenerate_Time) {

                double interval = ps.sample();
//                System.out.println(interval);

                while (interval <= 0) {
                    System.out.println("Impossible is occurred! interval is " + interval + " for device " + i + " time " + virtualTime);
                    interval = ps.sample();
                }

                //SimLogger.printLine(virtualTime + " -> " + interval + " for device " + i + " time ");
                virtualTime += interval;

//                if (virtualTime > activePeriodStartTime + activePeriod) {
//                    activePeriodStartTime = activePeriodStartTime + activePeriod + idlePeriod;
//                    virtualTime = activePeriodStartTime;
//                    continue;
//                }

//                double appinputSize = APPlookuptable[randomAppType][4];
//                double inputSizeBias = 1000;
//                appinputSize = rng.nextGaussian() * inputSizeBias + appinputSize;

                double avg_task_size = APPlookuptable[randomAppType][6];
//                double sizeBias = 20;
//                avg_task_size = avg_task_size + sizeBias * rng.nextGaussian();

//                double appoutputSize = APPlookuptable[randomAppType][5];
//                double outSizeBias = 1000;
//                appoutputSize = rng.nextGaussian() * outSizeBias + appoutputSize;

                int avg_task_num = (int) APPlookuptable[randomAppType][7];
//                int task_num_Bias = avg_task_num / 10;
//                avg_task_num = rng.nextInt(avg_task_num - task_num_Bias, avg_task_num + task_num_Bias + 1);

//                long avg_task_size = ((long)applength / avg_task_num);

                double CCR = APPlookuptable[randomAppType][9];

//                double avg_comPow = SimManager.getInstance().getEdgeDeviceGeneratorModel().avg_speed;

                double avg_comTime = avg_task_size * 1000 / avg_comPow; //单位MS

//                long total_tra_size = (long) (CCR * applength);

                double avg_task_tra_Time = CCR * avg_comTime;

//                double ev_ds =  APPlookuptable[randomAppType][10];

                double shape_factor = APPlookuptable[randomAppType][8];

                int max_width = (int) (Math.sqrt(avg_task_num) * shape_factor);

//                double avg_tra = SimManager.getInstance().getNetworkModel().avg_traSpeed;

                double avg_tra_size = (avg_task_tra_Time / avg_tra) * 1000; //单位KB

                double appinputSize = avg_tra_size * InAndOut;
//                double inputSizeBias = 0.05 * appinputSize;
//                appinputSize = rng.nextGaussian() * inputSizeBias + appinputSize;

                double appoutputSize = avg_tra_size * InAndOut;
//                double outSizeBias = 0.05 * appoutputSize;
//                appoutputSize = rng.nextGaussian() * outSizeBias + appoutputSize;

                DAG dag = generateDAG(avg_task_num, max_width, avg_task_size,avg_tra_size , appinputSize, appoutputSize, app_id,i);
                dag.computeCriticalPath();
                dag.setTasksMap();

                // 设置任务前驱同步信号量
                for(Task task : dag.getTasks()){
                    if(task.get_taskId() != -1) {
                        task.wait_pre = new CountDownLatch(task.getPredecessors().size());
                        //System.out.println("mobile:" + task.getMobileDeviceId() + " APP:" + task.getAppid() + " task:" + task.get_taskId() + " wait:" + task.wait_pre);
                    }
                }

                long start_time;
                long offsetTime;

                if(Objects.equals(useScenario, "OFF")){
                    offsetTime = (long) ((virtualTime + AppStart_time_offset) * 1000);
                }else {
                    offsetTime = System.currentTimeMillis() - base_line_start_time + (long) ((virtualTime + AppStart_time_offset) * 1000);
                }

                start_time = base_line_start_time + offsetTime ;


                long execution_time = generateUniformInt(15, 120) * 1000L;

                long end_time = start_time + execution_time;

                APP app = new APP(app_id, AppName, start_time,end_time, execution_time,offsetTime,appinputSize, appoutputSize, dag, CCR, shape_factor, i);
                apps.add(app);
                app_id++;
                App_num++;

                if(Objects.equals(useScenario, "OFF"))
                    break;
            }

            //移动设备位置随机设置
            double[][] edgeDeviceLookUpTable = SimSettings.getInstance().getEdgedeviceLookUpTable();
            int random_edgedevice = rng.nextInt(1, edgeDeviceLookUpTable.length);

            double moniledevice_latitude = edgeDeviceLookUpTable[random_edgedevice][3]; // 纬度
            moniledevice_latitude = SimUtils.getRandomDoubleNumber(moniledevice_latitude - 1, moniledevice_latitude + 1);
            double mobiledevice_longitude = edgeDeviceLookUpTable[random_edgedevice][4];
            mobiledevice_longitude = SimUtils.getRandomDoubleNumber(mobiledevice_longitude - 1, mobiledevice_longitude + 1);
            int mobiledevice_attractiveness = (int) edgeDeviceLookUpTable[random_edgedevice][5];

            mobileDevices.add(new MobileDevice(apps, moniledevice_latitude, mobiledevice_longitude, mobiledevice_attractiveness,
                    randomConnectionType, i, (long) edgeDeviceLookUpTable[random_edgedevice][6], (long) edgeDeviceLookUpTable[random_edgedevice][7]));

//            mobileDeviceCluster_num[mobiledevice_attractiveness][randomConnectionType]++;
        }

    }

    public DAG generateDAG(int taskNum, int maxWidth, double avg_task_size, double avg_task_tra_size,
                           double appinputSize, double appoutputSize, int appid, int mobileDeviceId) {
        List<Task> taskList = new ArrayList<>();
        DAG dag = new DAG();

        // 1. 创建虚拟节点
        Task startTask = new Task(-1, 0, appid, mobileDeviceId);  // 起点
        Task endTask = new Task(-2, 0, appid, mobileDeviceId);    // 汇点
        dag.addTask(startTask);
        dag.addTask(endTask);

        double task_sizeBias = 20;
        // 2. 创建任务节点
        for (int i = 0; i < taskNum; i++) {
            double task_size = rng.nextGaussian() * task_sizeBias + avg_task_size;
            Task task = new Task(i, task_size, appid, mobileDeviceId);
            taskList.add(task);
            dag.addTask(task);
        }

        // 3. 在每层之间生成随机的依赖关系
        List<List<Task>> layers = new ArrayList<>();
        int currentLayer = 0;
        int currentTask_num = 0;

        // 在DAG的深度范围内生成层次
        while (currentTask_num < taskNum) {
            List<Task> layer = new ArrayList<>();
            int layerWidth = generateUniformInt(1, maxWidth);; // 每层的宽度随机
            for (int i = 0; i < layerWidth; i++) {
                Task task = taskList.get(currentTask_num + i);
                layer.add(task);
                if(currentTask_num + i + 1 == taskNum)
                    break;
            }
            currentTask_num += layerWidth;
            layers.add(layer);
            currentLayer++;
        }

        List<List<Task>> suctasks= new ArrayList<>(layers);
        List<Task> lastTasks = new ArrayList<>();
//        List<Task> sucTasks = new ArrayList<>();

        // 4. 生成依赖关系：确保每个节点有至少1个,最多有3各前驱节点
        for (int i = 0; i < layers.size()-1 ; i++) {
            List<Task> currentLayerTasks = layers.get(i);
            List<Task> sucLayerTasks = layers.get(i+1);
            suctasks.remove(currentLayerTasks);
            suctasks.remove(sucLayerTasks);

            // 为当前层的任务添加依赖于下一层任务的关系
            for (Task currTask : currentLayerTasks) {
                int suc_num = 0;
                while (suc_num == 0) {
                        for (Task sucTask : sucLayerTasks) {
                            if (rng.nextBoolean()) {
                                double bias = 50;
                                double edgeSize = rng.nextGaussian(avg_task_tra_size, bias);
                                sucTask.addPredecessor(currTask, edgeSize);
                                currTask.addSuccessor(sucTask, edgeSize);
                                suc_num++;
                            }
                            if(suc_num == 2)
                                break;
                        }
                    if(suc_num >= 1)
                        break;
                }

                // 按概率为当前任务添加一个跨层的后继
                if(!suctasks.isEmpty()) {
                    if (suc_num < 3) {
                        for (List<Task> sucTask_layer : suctasks) {
                            for (Task sucTask : sucTask_layer) {
                                if (rng.nextBoolean()) {
                                    double bias = 50;
                                    double edgeSize = rng.nextGaussian(avg_task_tra_size, bias);
                                    sucTask.addPredecessor(currTask, edgeSize);
                                    currTask.addSuccessor(sucTask, edgeSize);
                                    suc_num++;
                                }
                                if (suc_num == 3)
                                    break;
                            }
                            if (suc_num == 3)
                                break;
                        }
                    }
                }
            }

            // 防止一些没选到的任务
            for(Task sucTask : sucLayerTasks) {
                if(sucTask.getPredecessors().isEmpty()){
                    lastTasks.add(sucTask);
                }
            }
        }

        // 5. 将起点连接到第一层任务，汇点连接到最后一层任务
        List<Task> firstLayer = layers.get(0);
        firstLayer.addAll(lastTasks);
//        List<Double>allocatedSizes = randomsize(firstLayer.size(), appinputSize);
//        int i = 0;
        for (Task task : firstLayer) {
            double bias = 0.05 * appinputSize;
            double edgeSize = rng.nextGaussian(appinputSize, bias);
            startTask.addSuccessor(task, edgeSize);
            task.addPredecessor(startTask, edgeSize);
//            i++;
        }

        List<Task> lastLayer = layers.get(layers.size() -1 );
//        lastTasks.addAll(lastLayer);
        List<Task> preTasks = new ArrayList<>(lastLayer);

        //随机选取跨层的末尾任务
        int preTaskNum = 0;
        layers.remove(lastLayer);
        for(List<Task> layer : layers) {
            for(Task task : layer) {
                if (rng.nextBoolean()) {
                    preTasks.add(task);
                    preTaskNum++;
                }
                if(preTaskNum ==2)
                    break;
            }
            if(preTaskNum ==2)
                break;
        }
//        for(int k=0; k<2;k++){
//            Task preTask =  preTasks.get(rng.nextInt(preTasks.size()));
//            if(!lastTasks.contains(preTask)) {
//                if (Math.random() < (double) 1 /taskNum ) {
//                    lastTasks.add(preTask);
//                }
//            }
//        }

//        allocatedSizes = randomsize(preTasks.size(),appoutputSize);
//        i = 0;
        for (Task task : preTasks) {
            double bias = 0.05 * appoutputSize;
            double edgeSize = rng.nextGaussian(appoutputSize, bias);
            task.addSuccessor(endTask, edgeSize);
            endTask.addPredecessor(task, edgeSize);
//            i++;
        }
        // 设置任务前驱输出同步量

        dag.setDepth(currentLayer + 1);
        dag.setMaxWidth(maxWidth);

        // 6. 返回DAG及任务列表
        return dag;
    }

    //为起始点与第一层节点之间的边和最后一层节点与结束节点之间的边生成随机大小的传输数据
    public List<Double> randomsize(int length, double in_or_out_size){
        List<Double> randomSizes = new ArrayList<>();

        while (length > 1) {  // 保证最后一个部分分配时可以直接填充剩余值
            // 计算每个部分的允许范围
            double maxSize = in_or_out_size * 70 / 100;

            // 确保 maxSize 至少为 1
            if (maxSize < 1) {
                maxSize = 1;
            }

            // 生成一个在 minSize 到 maxSize 之间的随机数
            double num = rng.nextDouble(1, maxSize + 1 );

            // 确保生成的 num 在剩余分配的大小范围内
            if (num > in_or_out_size - (length - 1)) {
                num = in_or_out_size - (length - 1); // 确保最后一个值不会大于剩余值
            }

            // 更新长度和剩余的总大小
            length--;
            in_or_out_size -= num;

            // 将生成的随机数加入列表
            randomSizes.add(num);
        }

        // 最后一个部分直接分配剩余的全部大小
        randomSizes.add(in_or_out_size);

        return randomSizes;
    }

}