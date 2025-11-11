/*
 * Title:        EdgeCloudSim - Scenarion Factory interface
 *
 * Description:
 * ScenarioFactory responsible for providing customizable components
 * such as  Network Model, Mobility Model, Edge Orchestrator.
 * This interface is very critical for using custom models on EdgeCloudSim
 * This interface should be implemented by EdgeCloudSim users
 *
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package ll;


public interface ScenarioFactory {
    /**
     * provides abstract Load Generator Model
     */
    public LoadGeneratorModel getLoadGeneratorModel();

    public EdgeDeviceGeneratorModel getDeviceGeneratorModel();

    /**
     * provides abstract Network Model
     */
    public NetWork getNetworkModel();

//    public ResourceMonitor getResourceMonitor();

    public NativeEdgeDeviceGenerator getNativeEdgeDeviceGenerator();

    public void setNumOfMobileDevice(int numOfMobileDevice);


    void  setUseScenario(String useScenario);

    void  setSimScenario(String simScenario);
}
