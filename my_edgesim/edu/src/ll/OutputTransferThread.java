package ll;
import java.util.*;

public class OutputTransferThread extends Thread {

        private Task task;
        private Task sucTask;
        private EdgeDevice srcEdgeDevice;

        public OutputTransferThread(Task task,Task sucTask,EdgeDevice srcEdgeDevice) {
            this.task = task;
            this.sucTask = sucTask;
            this.srcEdgeDevice = srcEdgeDevice;
        }

        @Override
        public void run() {
            EdgeDevice dstEdgeDevice = SimManager.getInstance().getEdgeDeviceGeneratorModel().getEdge_devices().get(sucTask.getDevice_Id());
            double delay;
            double outputSize = task.getSuccessorsMap().get(sucTask);
            NetWork netWork_model = SimManager.getInstance().getNetworkModel();

            double delay1 = 0;
            if (dstEdgeDevice.getAttractiveness() != srcEdgeDevice.getAttractiveness()) {
                EdgeDevice nativeEdge = SimManager.getInstance().getNativeEdgeDeviceGenerator().getNativeDevicesMap().get(srcEdgeDevice.getAttractiveness());
                if(srcEdgeDevice.getDeviceId() != nativeEdge.getDeviceId()){
                    double distance_delay1 = calculateDistance(nativeEdge.getlocation(),srcEdgeDevice.getlocation())*1000 / 299792458;
                    double BW1 = netWork_model.BWmap.get(srcEdgeDevice).get(nativeEdge);
                    delay1 = outputSize * BW1 / 1000 + distance_delay1;
                    //delay1 += (outputSize * 4) / srcEdgeDevice.getUploadspeed() + (double) (outputSize * 4) / nativeEdge.getDownloadspeed());
                    srcEdgeDevice = nativeEdge;
                }
            }

            double distance_delay = calculateDistance(srcEdgeDevice.getlocation(), dstEdgeDevice.getlocation())*1000 / 299792458;
            double BW = netWork_model.BWmap.get(srcEdgeDevice).get(dstEdgeDevice);
            delay = outputSize  * BW / 1000 + distance_delay + delay1;

            //delay += (long)((double) (outputSize * 4) / srcEdgeDevice.getUploadspeed() + (double) (outputSize * 4) / dstEdgeDevice.getDownloadspeed());// 模拟上传下载延迟

//            delay = (long) Math.ceil(delay);
            //task.setOutput_traDelay( sucTask, delay);
            try {
                // 模拟网络传输延迟
                Thread.sleep((long) delay);  // 毫秒
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


