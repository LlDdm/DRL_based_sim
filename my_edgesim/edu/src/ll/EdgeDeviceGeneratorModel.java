package ll;

import java.util.ArrayList;
import java.util.List;

public class EdgeDeviceGeneratorModel {
    private List<EdgeDevice> edge_devices;
    public double avg_speed;

    public EdgeDeviceGeneratorModel() {
        edge_devices = new ArrayList<>();
    }

    public void initialize(){
        double [][] EdgeDevicesLookUpTable = SimSettings.getInstance().getEdgedeviceLookUpTable();
        for(int i=0; i < EdgeDevicesLookUpTable.length; i++){
            if(i == 0)
                edge_devices.add(new Cloud((int)EdgeDevicesLookUpTable[i][0], EdgeDevicesLookUpTable[i][1], EdgeDevicesLookUpTable[i][3],
                        EdgeDevicesLookUpTable[i][4], (long) EdgeDevicesLookUpTable[i][6], (long) EdgeDevicesLookUpTable[i][7], (long) EdgeDevicesLookUpTable[i][2],
                        (int) EdgeDevicesLookUpTable[i][5]));
            else{
                edge_devices.add(new EdgeDevice((int)EdgeDevicesLookUpTable[i][0], EdgeDevicesLookUpTable[i][1], EdgeDevicesLookUpTable[i][3],
                        EdgeDevicesLookUpTable[i][4], (int) EdgeDevicesLookUpTable[i][5], (long) EdgeDevicesLookUpTable[i][6], (long) EdgeDevicesLookUpTable[i][7],
                        (int) EdgeDevicesLookUpTable[i][2]));
            }
            avg_speed += EdgeDevicesLookUpTable[i][1];
        }

        avg_speed = avg_speed / EdgeDevicesLookUpTable.length;
    }

    public List<EdgeDevice> getEdge_devices() {
        return edge_devices;
    }
}
