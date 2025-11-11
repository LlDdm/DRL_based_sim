package ll;

import java.util.List;

public class AppTransferThread extends Thread {
    private APP app;
    private EdgeDevice nativeEdgeDevice;
    private double[] thisLocation;
    private double[] otherLocation;
    private int connetionTyoe;
    private double mobile_uploadSpeed;

    public AppTransferThread(APP app, EdgeDevice nativeEdgeDevice, double[] thisLocation,double[] otherLocation,
                             int connetionTyoe, double mobile_uploadSpeed) {
        this.app = app;
        this.nativeEdgeDevice = nativeEdgeDevice;
        this.thisLocation = thisLocation;
        this.otherLocation = otherLocation;
        this.connetionTyoe = connetionTyoe;
        this.mobile_uploadSpeed = mobile_uploadSpeed;
    }

    @Override
    public void run() {
        //System.out.println("开始传输app mobile: " + app.getMobileDeviceId() + " app: " + app.getAppid() + " 本地边缘设备： " + nativeEdgeDevice.getDeviceId());
        double delay = 0;
        double app_inputSize = app.getInputsize();
        NetWork netWork_model = SimManager.getInstance().getNetworkModel();
        double distance_delay = calculateDistance(thisLocation, otherLocation)* 1000 / 299792458;

        // 模拟传输延迟
        delay = switch (connetionTyoe) {
            case 0 -> (app_inputSize  / netWork_model.getLAN_BW() + distance_delay);
            case 1 -> (app_inputSize  / netWork_model.getWLAN_BW() + distance_delay);
            case 2 -> (app_inputSize  / netWork_model.getGSM_BW() + distance_delay);
            default -> delay;
        };

        // 模拟上传下载延迟
//        delay += (app_inputSize *1000 / mobile_uploadSpeed +  (app_inputSize * 1000) / nativeEdgeDevice.getDownloadspeed());
        delay += app_inputSize  / mobile_uploadSpeed +  app_inputSize / nativeEdgeDevice.getDownloadspeed();
//        delay =  Math.ceil(delay);

        try {
            Thread.sleep((long)delay);  // 转换为毫秒
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        nativeEdgeDevice.scheduler.addApp(app);
        //System.out.println("app传输完成 mobile: " + app.getMobileDeviceId() + " app: " + app.getAppid() + " 本地边缘设备： " + nativeEdgeDevice.getDeviceId());
    }

    public static double calculateDistance(double[] this_location, double[] other_location) {
        double dLat = this_location[0] - other_location[0];
        double dLon = this_location[1] - other_location[1];


        return Math.sqrt(Math.pow(dLat,2) + Math.pow(dLon, 2)) * 1000;  // Returns the distance in kilometers
    }
}

