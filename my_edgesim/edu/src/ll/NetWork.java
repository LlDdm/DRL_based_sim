package ll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.lang.Math.max;

class NetWork {
    private double WAN_BW;
    private double MAN_BW;
    private double GSM_BW;
    private double WLAN_BW;
    private double LAN_BW;
    public double avg_traSpeed;

    public Map<EdgeDevice,Map<EdgeDevice,Double>> BWmap = new HashMap<>();
    public Map<MobileDevice,Double> mobileBW = new HashMap<>();
    private Random rand = new Random();

    public NetWork() {}

    public double getWAN_BW() {return WAN_BW;}
    public void setWAN_BW(double WAN_BW) {
        this.WAN_BW = WAN_BW;
    }

    public double getMAN_BW() {
        return MAN_BW ;
    }
    public void setMAN_BW(double MAN_BW) {
        this.MAN_BW = MAN_BW;
    }

    public double getLAN_BW() {return LAN_BW ;}
    public void setLAN_BW(double LAN_BW) {
        this.LAN_BW = LAN_BW;
    }

    public double getWLAN_BW() {
        return WLAN_BW ;
    }
    public void setWLAN_BW(double wlanBw) {
        this.WLAN_BW = wlanBw;
    }

    public double getGSM_BW() { return GSM_BW; }
    public void setGSM_BW(double GSM_BW) {
        this.GSM_BW = GSM_BW;
    }


    public void initialize(){
        this.MAN_BW =  SimSettings.getInstance().getManBandwidth();
        this.WAN_BW =  SimSettings.getInstance().getWanBandwidth();
        this.GSM_BW =  SimSettings.getInstance().getGsmBandwidth();
        this.WLAN_BW =  SimSettings.getInstance().getWlanBandwidth();
        this.LAN_BW = SimSettings.getInstance().getLanBandwidth();
        List<EdgeDevice> edgeDevices = SimManager.getInstance().getEdgeDeviceGeneratorModel().getEdge_devices();

        // 初始化二维 map 并填充对称的带宽数据
        for(EdgeDevice edgeDevice: edgeDevices){
            BWmap.putIfAbsent(edgeDevice, new HashMap<>());
            for(EdgeDevice edgeDevice1: edgeDevices) {
                if (BWmap.get(edgeDevice).get(edgeDevice1) == null) {
                    BWmap.putIfAbsent(edgeDevice1, new HashMap<>());
                    double BW;
                    // 判断设备之间的带宽类型
                    if (edgeDevice.getDeviceId() == 0 || edgeDevice1.getDeviceId() == 0) {
                        BW = getWAN_BW();  // 获取 WAN 带宽
                    } else if (edgeDevice.getAttractiveness() == edgeDevice1.getAttractiveness()) {
                        BW = getLAN_BW();  // 获取 LAN 带宽
                    } else {
                        BW = getMAN_BW();  // 获取 MAN 带宽
                    }

                    // 设置对称的带宽数据
                    BWmap.get(edgeDevice).put(edgeDevice1, BW);
                    BWmap.get(edgeDevice1).put(edgeDevice, BW);  // 保证对称性
                }
            }
        }
        avg_traSpeed = (MAN_BW + WAN_BW + LAN_BW) / 3.0;

    }

    public void generateMobileBW(){
        for(MobileDevice mobileDevice : SimManager.getInstance().getLoadGeneratorModel().getMobileDevices()){
            if(mobileDevice.getConnectionType() == 0){
                mobileBW.put(mobileDevice,getLAN_BW());
            }else if(mobileDevice.getConnectionType() == 1){
                mobileBW.put(mobileDevice,getWLAN_BW());
            }else if(mobileDevice.getConnectionType() == 2){
                mobileBW.put(mobileDevice,getGSM_BW());
            }else
                mobileBW.put(mobileDevice,Double.MAX_VALUE);
        }
    }

//    private int generateNormalint(int mean, int stdDev){
//        return (int) Math.round(rand.nextGaussian() * stdDev + mean);
//    }
}

