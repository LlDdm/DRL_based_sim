//package ll;
//
//import java.util.List;
//
//public class TaskTransferThread extends Thread {
//    private Task task;
//    private double[] thisLocation;
//    private double[] otherLocation;
//    private EdgeDevice device;
//    private int Attractiveness;
//    private double uploadSpeed;
//
//    public TaskTransferThread(Task task, EdgeDevice device, double[] thisLocation,
//                             int Attractiveness, double uploadSpeed) {
//        this.device = device;
//        this.thisLocation = thisLocation;
//        this.otherLocation = device.getlocation();
//        this.Attractiveness = Attractiveness;
//        this.uploadSpeed = uploadSpeed;
//        this.task = task;
//    }
//
//    @Override
//    public void run() {
//        long delay;
//        long taskSize = task.getSize() * 8;
//        NetWork netWork_model = SimManager.getInstance().getNetworkModel();
//        long distance_delay = (long) (calculateDistance(thisLocation, otherLocation) / 299792458 * 1000);
//
//        // 模拟传输延迟
//        if(Attractiveness == device.getAttractiveness()){
//            delay = (taskSize  / netWork_model.getLAN_BW()) + distance_delay;
//        }
//        else if(device.getDeviceId() != 0){
//            delay = (taskSize  / netWork_model.getMAN_BW()) + distance_delay;
//        }
//        else {
//            delay = (taskSize  / netWork_model.getWAN_BW()) + distance_delay;
//        }
//
//        // 模拟上传下载延迟
//        delay += (long) (taskSize *1000 / uploadSpeed + (double) (taskSize * 1000) / device.getDownloadspeed());
//
//        try {
//            Thread.sleep(delay);  // 毫秒
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        task.setTra_delay(delay);
//        device.addTask(task);
//        task.setArrival_time(System.currentTimeMillis());
//    }
//
//    public static double calculateDistance(double[] this_location, double[] other_location) {
//        double dLat = this_location[0] - other_location[0];
//        double dLon = this_location[1] - other_location[1];
//
//
//        return Math.sqrt(Math.pow(dLat,2) + Math.pow(dLon, 2)) * 1000;  // Returns the distance in kilometers
//    }
//}
//
//
