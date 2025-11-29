/*
 * Title:        EdgeCloudSim - Simulation Manager
 *
 * Description:
 * SimManager is an singleton class providing many abstract classeses such as
 * Network Model, Mobility Model, Edge Orchestrator to other modules
 * Critical simulation related information would be gathered via this class
 *
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package ll;

import jdk.jshell.spi.SPIResolutionException;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;


public class SimManager{
    private String simScenario;
    private String orchestratorPolicy;
    private int numOfMobileDevice;
    private ScenarioFactory scenarioFactory;
    private String useScenario;
    public List<Long> result;
    public CountDownLatch wait_complete;
    private volatile boolean running;
    public int OverDeadline;
    private double timeout_tolerance;

    private NetWork networkModel;
    private EdgeDeviceGeneratorModel edgeDeviceGeneratorModel;
    private LoadGeneratorModel loadGeneratorModel;
    private NativeEdgeDeviceGenerator nativeEdgeDeviceGenerator;

    private static SimManager instance = null;

    public SimManager(ScenarioFactory _scenarioFactory) {
        scenarioFactory = _scenarioFactory;
        result = new ArrayList<>();
        instance = this;
        this.OverDeadline = 0;
    }

    public static SimManager getInstance(){
        return instance;
    }

    public void setEdgeDeviceGeneratorModel() {
        System.out.println("Creating Devices...");
        edgeDeviceGeneratorModel = scenarioFactory.getDeviceGeneratorModel();
        edgeDeviceGeneratorModel.initialize();
        System.out.println("Done.");
        instance = this;
    }

    public void setNativeEdgeDeviceGenerator() {
        System.out.println("Creating NativeEdgeDevices...");
        nativeEdgeDeviceGenerator = scenarioFactory.getNativeEdgeDeviceGenerator();
        nativeEdgeDeviceGenerator.initialize();
        System.out.println("Done.");
        instance = this;
    }

    public void setLoadGeneratorModel() {
        System.out.println("Creating Loads...");
        loadGeneratorModel = scenarioFactory.getLoadGeneratorModel();
        loadGeneratorModel.initializeModel();
        System.out.println("Done.");
        instance = this;
    }

    public void setNetworkModel() {
        //Generate network model
        System.out.println("Creating Networks...");
        networkModel = scenarioFactory.getNetworkModel();
        networkModel.initialize();
        System.out.println("Done.");
        instance = this;
    }

    /**
     * Triggering CloudSim to start simulation
     */


    public void startSimulation(){
        //Starts the simulation

        System.out.println("start devices...");
        for(EdgeDevice edgeDevice : edgeDeviceGeneratorModel.getEdge_devices()){
            edgeDevice.startDevice();
        }
        System.out.println("device start done.");

        System.out.println("start scheduler...");
        for(Map.Entry<Integer,EdgeDevice> entry : nativeEdgeDeviceGenerator.getHaveSchedule_native().entrySet()){
            entry.getValue().scheduler.startDevice(orchestratorPolicy,timeout_tolerance);
        }
        System.out.println("schedule start done.");

        System.out.println("start mobile devices...");
        for(MobileDevice mobileDevice : loadGeneratorModel.getMobileDevices()){
            mobileDevice.startDevice();
        }
        System.out.println("mobile device start done.");

    }

    public String getSimulationScenario(){
        return simScenario;
    }

    public String getOrchestratorPolicy(){return orchestratorPolicy;}

    public ScenarioFactory getScenarioFactory(){return scenarioFactory;}

    public int getNumOfMobileDevice(){return numOfMobileDevice;}

    public String getUseScenario(){return useScenario;}

    public void setOrchestratorPolicy(String orchestratorPolicy){this.orchestratorPolicy = orchestratorPolicy;}

    public void setNumOfMobileDevice(int numOfMobileDevice){this.numOfMobileDevice = numOfMobileDevice;}

    public void setUseScenario(String useScenario){this.useScenario = useScenario;}

    public void setSimScenario(String simScenario){this.simScenario = simScenario;}

    public double getTimeout_tolerance(){return timeout_tolerance;}
    public void setTimeout_tolerance(double timeout_tolerance){this.timeout_tolerance = timeout_tolerance;}

    public NetWork getNetworkModel(){
        return networkModel;
    }

    public LoadGeneratorModel getLoadGeneratorModel(){
        return loadGeneratorModel;
    }

    public EdgeDeviceGeneratorModel getEdgeDeviceGeneratorModel(){
        return edgeDeviceGeneratorModel;
    }

    public NativeEdgeDeviceGenerator getNativeEdgeDeviceGenerator(){ return nativeEdgeDeviceGenerator; }

    // 停止线程的公共方法
    public void stopRunning() {
        running = false;
    }

    public void Running(){
        running = true;
    }

    public boolean isRunning() { return running; }

    public void updateLoad(){
        for(MobileDevice device : loadGeneratorModel.getMobileDevices()){
            for(APP app : device.getApp()){
                long currentTime = System.currentTimeMillis();
                app.setStartTime(currentTime + app.getOffsetTime());
                app.setDeadline(app.getStartTime() + app.getExcursionTime());
                app.setCompleteTime(0);
                app.setMakeSpan(0);
                for(Task task : app.getDag().getTasks()){
                    task.allocate_semaphore = new Semaphore(0);
                    task.wait_pre = new CountDownLatch(task.getPredecessors().size());
                    task.setPredicting_complete_time(0);
                    task.setMaxDelay_device_Id(0);
                    task.setPredicting_device_Id(new ArrayList<>());
                    task.setDevice_Id(-1);
                    task.setEstimate_complete_time(0);
                    task.setEstimate_start_time(0);
                }
            }
        }
    }
}

