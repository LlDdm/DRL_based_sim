/*
 * Title:        EdgeCloudSim - Scenario Factory
 *
 * Description:  Sample scenario factory providing the default
 *               instances of required abstract classes
 *
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package ll;

public class LScenarioFactory implements ScenarioFactory {
    private int numOfMobileDevice;
    private long loadGenerate_time;
    private long WARM_UP_PERIOD;
    private String simScenario;
    private String useScenario;

    LScenarioFactory(
                     long _loadGenerate_time,
                     long _WARM_UP_PERIOD) {
        loadGenerate_time = _loadGenerate_time;
        WARM_UP_PERIOD = _WARM_UP_PERIOD;
    }

    @Override
    public LoadGeneratorModel getLoadGeneratorModel() {
        return new LoadGeneratorModel(numOfMobileDevice, simScenario,useScenario,loadGenerate_time, WARM_UP_PERIOD);
    }

    @Override
    public EdgeDeviceGeneratorModel getDeviceGeneratorModel() {
        return new EdgeDeviceGeneratorModel();
    }

    @Override
    public NetWork getNetworkModel() {
        return new NetWork();
    }

//    @Override
//    public ResourceMonitor getResourceMonitor() {
//        return new ResourceMonitor(simScenario);
//    }

    @Override
    public NativeEdgeDeviceGenerator getNativeEdgeDeviceGenerator() {
        return new NativeEdgeDeviceGenerator();
    }

    @Override
    public void setNumOfMobileDevice(int numOfMobileDevice) {
        this.numOfMobileDevice = numOfMobileDevice;
    }

    @Override
    public void  setUseScenario(String useScenario) {
        this.useScenario = useScenario;
    }

    @Override
    public void  setSimScenario(String simScenario) {
        this.simScenario = simScenario;
    }
}


