package ll;

public class FirstTaskTransferThread extends Thread {
    private Task task;
    private Task sucTask;
    private MobileDevice mobileDevice;

    public FirstTaskTransferThread( Task task, Task sucTask, MobileDevice mobileDevice) {
        this.task = task;
        this.sucTask = sucTask;
        this.mobileDevice = mobileDevice;
    }

    @Override
    public void run() {
        EdgeDevice sucDevice = SimManager.getInstance().getEdgeDeviceGeneratorModel().getEdge_devices().get(sucTask.getDevice_Id());
        if (sucDevice == null) {
            throw new RuntimeException("未找到对应的设备");
        }
        double delay1;
        double delay2 = 0;
        double outputSize = task.getSuccessorsMap().get(sucTask);
        NetWork netWork_model = SimManager.getInstance().getNetworkModel();
        EdgeDevice nativeEdge = SimManager.getInstance().getNativeEdgeDeviceGenerator().getNativeDevicesMap().get(mobileDevice.getDevice_attractiveness());
        double distance1_delay = calculateDistance(mobileDevice.getDevice_location(), nativeEdge.getlocation())*1000 / 299792458;
        // 移动设备到本地边缘设备的传输延迟
        double BW1 =netWork_model.mobileBW.get(mobileDevice);
        delay1 = outputSize * BW1 / 1000 + distance1_delay;
        // 移动设备的上传延迟+本地边缘设备的下载延迟
        //delay1 +=   (outputSize * 4) / nativeEdge.getDownloadspeed() + (double) (outputSize * 4) / mobileDevice.getUploadSpeed();

        // 本地边缘设备到目的边缘设备的传输延迟
        if(nativeEdge != sucDevice) {
            double distance2_delay = calculateDistance(nativeEdge.getlocation(), sucDevice.getlocation())*1000 / 299792458;
            double BW2 = netWork_model.BWmap.get(sucDevice).get(nativeEdge);
            delay2 = outputSize * BW2 / 1000 + distance2_delay;

            // 本地边缘设备的上传延迟+目的边缘设备的下载延迟
            //delay2 += (outputSize * 4 / sucDevice.getDownloadspeed() + outputSize * 4 / nativeEdge.getUploadspeed());
        }

        delay2 += delay1;
//        delay2 = (long) Math.ceil(delay2);
        //task.setOutput_traDelay(sucTask, delay);
        try {
            // 模拟网络传输延迟
            Thread.sleep((long) delay2);  // 毫秒
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 任务到达标志
        sucTask.wait_pre.countDown();
    }

    public static double calculateDistance(double[] this_location, double[] other_location) {
        double dLat = this_location[0] - other_location[0];
        double dLon = this_location[1] - other_location[1];


        return Math.sqrt(Math.pow(dLat,2) + Math.pow(dLon, 2)) * 1000;  // Returns the distance in kilometers
    }

}
