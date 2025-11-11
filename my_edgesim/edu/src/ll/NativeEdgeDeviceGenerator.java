package ll;

import java.util.*;

public class NativeEdgeDeviceGenerator {
    private final Map<Integer, EdgeDevice> NativeDevicesMap;
    private final Random rand = new Random();
    private Map<Integer, List<EdgeDevice>> edgeDeviceCluster;
    private Map<Integer, EdgeDevice> haveSchedule_native;

    public NativeEdgeDeviceGenerator() {
        this.NativeDevicesMap = new HashMap<>();
        this.edgeDeviceCluster = new HashMap<>();
        this.haveSchedule_native = new HashMap<>();
    }

    public void initialize(){
        List<EdgeDevice> devices = SimManager.getInstance().getEdgeDeviceGeneratorModel().getEdge_devices();
        //List<EdgeDevice> edgeDevices = devices.subList(1, devices.size());
        int attractiveness_num = SimSettings.getInstance().Attractiveness_NUM;
        for(int i=0; i<attractiveness_num; i++){
            List<EdgeDevice> Edges = new ArrayList<>();
            for(EdgeDevice edge : devices){
                if(edge.getAttractiveness() == i)
                    Edges.add(edge);
            }
            edgeDeviceCluster.put(i, Edges);
        }

        for(int i=0; i<attractiveness_num; i++){
            int ran = rand.nextInt(edgeDeviceCluster.get(i).size());
//            ran -= 1;
            EdgeDevice edge =  edgeDeviceCluster.get(i).get(ran);
            if(edge.getDeviceId() != 0) {
                Scheduler scheduler = new Scheduler(edge);
                edge.setScheduler(scheduler);
                haveSchedule_native.put(i, edge);
            }
            NativeDevicesMap.put(i, edge);
        }
    }

    public Map<Integer, EdgeDevice> getNativeDevicesMap() {
        return NativeDevicesMap;
    }

    public Map<Integer,EdgeDevice> getHaveSchedule_native() { return haveSchedule_native; }

    public Map<Integer, List<EdgeDevice>> getEdgeDeviceCluster() {return edgeDeviceCluster;}
}
