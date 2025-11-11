package ll;

import java.util.List;

public class EndTaskThread extends Thread {
    private Task task;
    private Task sucTask;
    private EdgeDevice edgeDevice;

    public EndTaskThread(Task task, Task sucTask, EdgeDevice edgeDevice) {
        this.task = task;
        this.sucTask = sucTask;
        this.edgeDevice = edgeDevice;
    }

    @Override
    public void run() {
        //System.out.println("开始传输最后任务 mobile: " + task.getMobileDeviceId() + " APP: " + task.getAppid() + " task: " + task.get_taskId());
        double delay;
        double delay1=0;
        double outputSize = task.getSuccessorsMap().get(sucTask);
        NetWork netWork_model = SimManager.getInstance().getNetworkModel();
        List<MobileDevice> mobileDevices = SimManager.getInstance().getLoadGeneratorModel().getMobileDevices();
        MobileDevice suc_mobile_device = mobileDevices.get(sucTask.getMobileDeviceId());
        EdgeDevice suc_nativeEdge = SimManager.getInstance().getNativeEdgeDeviceGenerator().getNativeDevicesMap().get(suc_mobile_device.getDevice_attractiveness());

        if(edgeDevice.getDeviceId() != suc_nativeEdge.getDeviceId()) {

            double delay2 = 0;
            if (suc_nativeEdge.getAttractiveness() != edgeDevice.getAttractiveness()) {
                EdgeDevice src_nativeEdge = SimManager.getInstance().getNativeEdgeDeviceGenerator().getNativeDevicesMap().get(edgeDevice.getAttractiveness());
                if(src_nativeEdge.getDeviceId() != edgeDevice.getDeviceId()){
                    double distance_delay2 = calculateDistance(edgeDevice.getlocation(),src_nativeEdge.getlocation())*1000 / 299792458;
                    double BW2 = netWork_model.BWmap.get(edgeDevice).get(src_nativeEdge);
                    delay2 = outputSize * BW2 / 1000 + distance_delay2;
                    //delay2 += outputSize  / edgeDevice.getUploadspeed() +  outputSize  / src_nativeEdge.getDownloadspeed();
                    edgeDevice = src_nativeEdge;
                }
            }

            double distance1_delay =  calculateDistance(edgeDevice.getlocation(), suc_nativeEdge.getlocation())*1000 / 299792458;
            // 边缘设备到本地边缘设备的传输延迟
            double BW1 = netWork_model.BWmap.get(edgeDevice).get(suc_nativeEdge);
            delay1 += outputSize * BW1 / 1000 + distance1_delay + delay2;

            // 边缘设备上传延迟+本地边缘设备下载延迟
            //delay1 += (outputSize * 4 / suc_nativeEdge.getDownloadspeed() + outputSize * 4 / edgeDevice.getUploadspeed());
        }

        double distance_delay = calculateDistance(suc_nativeEdge.getlocation(), suc_mobile_device.getDevice_location())*1000 / 299792458;
        double BW = netWork_model.mobileBW.get(suc_mobile_device);
        delay = outputSize * BW / 1000 + distance_delay + delay1;

        // 移动设备下载延迟+本地边缘设备的上传延迟
        //delay += (outputSize * 4 / suc_nativeEdge.getUploadspeed() + outputSize * 4 / suc_mobile_device.getDownloadSpeed()) + delay1;
//        delay = (long) Math.ceil(delay);

        //task.setOutput_traDelay(sucTask, delay);
        try {
            // 模拟网络传输延迟
            Thread.sleep((long) delay);  // 毫秒
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 任务到达标志
        //System.out.println("最终任务 mobile: " + task.getMobileDeviceId() + " APP: " + task.getAppid() + " task: " + task.get_taskId() + " Pre_countDown:" + sucTask.wait_pre);
        sucTask.wait_pre.countDown();
        //System.out.println("最终任务 mobile: " + task.getMobileDeviceId() + " APP: " + task.getAppid() + " task: " + task.get_taskId() + " Suc_countDown:" + sucTask.wait_pre);
        //System.out.println("最终任务传输完成 mobile: " + task.getMobileDeviceId() + " APP: " + task.getAppid() + " task: " + task.get_taskId());
    }

    public static double calculateDistance(double[] this_location, double[] other_location) {
        double dLat = this_location[0] - other_location[0];
        double dLon = this_location[1] - other_location[1];


        return Math.sqrt(Math.pow(dLat,2) + Math.pow(dLon, 2)) * 1000;  // Returns the distance in kilometers
    }

}
