//package ll;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//class ResourceMonitor {
//    private String simScenario;
//    private Map<Integer,Double> EdgeDevice_PowerMap;
//    private Map<Integer,Double> EdgeDevice_IdleMap;
//    private Map<Integer,Long> EdgeDevice_TaskQueue_lengthMap;
//
//    public ResourceMonitor(String simScenario) {
//        this.simScenario = simScenario;
//        EdgeDevice_PowerMap = new HashMap<>();
//        EdgeDevice_IdleMap = new HashMap<>();
//        EdgeDevice_TaskQueue_lengthMap = new HashMap<>();
//    }
//
//    public void initialize(){
//        List<EdgeDevice> edgeDevices = SimManager.getInstance().getEdgeDeviceGeneratorModel().getEdge_devices();
//        for(EdgeDevice edgeDevice : edgeDevices){
//            EdgeDevice_PowerMap.put(edgeDevice.getDeviceId(), edgeDevice.getMips());
//            EdgeDevice_IdleMap.put(edgeDevice.getDeviceId(), edgeDevice.getIdle());
//            EdgeDevice_TaskQueue_lengthMap.put(edgeDevice.getDeviceId(), edgeDevice.getTaskQueueLength());
//        }
//    }
//
//    public void startMonitor(){
//        new Thread(() -> {
//            while(true){
//                getEdgeTaskQueueLength();
//            }
//        }).start();
//    }
//
//    public synchronized void getEdgeTaskQueueLength(){
//        List<EdgeDevice> edgeDevices = SimManager.getInstance().getEdgeDeviceGeneratorModel().getEdge_devices();
//        for(EdgeDevice edgeDevice : edgeDevices){
//            EdgeDevice_TaskQueue_lengthMap.replace(edgeDevice.getDeviceId(), edgeDevice.getTaskQueueLength());
//            try {
//                Thread.sleep(1000); // 每隔1秒更新一次边缘设备任务队列大小
//            }
//            catch(Exception e){
//                e.printStackTrace();
//            }
//        }
//    }
//
//}

