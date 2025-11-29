/*
 * Title:        EdgeCloudSim - Simulation Settings class
 *
 * Description:
 * SimSettings provides system wide simulation settings. It is a
 * singleton class and provides all necessary information to other modules.
 * If you need to use another simulation setting variable in your
 * config file, add related getter method in this class.
 *
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package ll;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SimSettings {
    private static SimSettings instance = null;

    public static final double CLIENT_ACTIVITY_START_TIME = 5;

    //delimiter for output file.
    public static final String DELIMITER = ";";

    public int Attractiveness_NUM;
    public double InAndOut_ratio;

    private long SIMULATION_TIME; //minutes unit in properties file
    private long LOAD_GENERATE_TIME;
    private long WARM_UP_PERIOD; //minutes unit in properties file

    private int MIN_NUM_OF_MOBILE_DEVICES;
    private int MAX_NUM_OF_MOBILE_DEVICES;
    private int MOBILE_DEVICE_COUNTER_SIZE;

    private double BANDWITH_WLAN; //Mbps unit in properties file
    private double BANDWITH_MAN; //Mbps unit in properties file
    private double BANDWITH_WAN; //Mbps unit in properties file
    private double BANDWITH_GSM; //Mbps unit in properties file
    private double BANDWITH_LAN;

    private String[] SIMULATION_SCENARIOS;
    private String[] ORCHESTRATOR_POLICIES;
    private String[] USE_SCENARIOS;

    private double[][] appLookUpTable;
    private String[] appNames;

    private double[][] edgeDeviceLookUpTable;
    private String[] edgeDeviceNames;

    private final double[][] connectionTypeLookUpTable = new double[3][1];;

    private final double[] areaRange = new double[2];;

    private SimSettings() {
        Attractiveness_NUM = 0;
        edgeDeviceLookUpTable = null;
        edgeDeviceNames = null;
        appLookUpTable = null;
        appNames = null;
    }

    public static SimSettings getInstance() {
        if(instance == null) {
            instance = new SimSettings();
        }
        return instance;
    }


    public boolean initialize(String propertiesFile, String edgeDevicesFile, String applicationsFile){
        boolean result = false;
        InputStream input = null;
        try {
            input = new FileInputStream(propertiesFile);

            // load a properties file
            Properties prop = new Properties();
            prop.load(input);

            SIMULATION_TIME = 60 * Long.parseLong(prop.getProperty("simulation_time")); //seconds
            LOAD_GENERATE_TIME = Long.parseLong(prop.getProperty("load_generate_time"));
            WARM_UP_PERIOD =  Long.parseLong(prop.getProperty("warm_up_period")); //seconds

            MIN_NUM_OF_MOBILE_DEVICES = Integer.parseInt(prop.getProperty("min_number_of_mobile_devices"));
            MAX_NUM_OF_MOBILE_DEVICES = Integer.parseInt(prop.getProperty("max_number_of_mobile_devices"));
            MOBILE_DEVICE_COUNTER_SIZE = Integer.parseInt(prop.getProperty("mobile_device_counter_size"));

            BANDWITH_WLAN =  Double.parseDouble(prop.getProperty("wlan_bandwidth"));
            BANDWITH_MAN =  Double.parseDouble(prop.getProperty("man_bandwidth", "0"));
            BANDWITH_WAN =  Double.parseDouble(prop.getProperty("wan_bandwidth", "0"));
            BANDWITH_GSM =   Double.parseDouble(prop.getProperty("gsm_bandwidth", "0"));
            BANDWITH_LAN =  Double.parseDouble(prop.getProperty("lan_bandwidth", "0"));

            ORCHESTRATOR_POLICIES = prop.getProperty("orchestrator_policies").split(",");

            SIMULATION_SCENARIOS = prop.getProperty("simulation_scenarios").split(",");

            USE_SCENARIOS = prop.getProperty("Use_scenarios").split(",");

            connectionTypeLookUpTable[0][0] = Double.parseDouble(prop.getProperty("lanType_percentage", "0"));
            connectionTypeLookUpTable[1][0] = Double.parseDouble(prop.getProperty("wlanType_percentage", "0"));
            connectionTypeLookUpTable[2][0] = Double.parseDouble(prop.getProperty("GSM_percentage", "0"));

            areaRange[0] = Double.parseDouble(prop.getProperty("length", "0"));
            areaRange[1] = Double.parseDouble(prop.getProperty("width", "0"));

        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                    result = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        parseApplicationsXML(applicationsFile);
        parseEdgeDevicesXML(edgeDevicesFile);

        return result;
    }

    public double[][] getconnectiontypeLookUpTable(){
        return connectionTypeLookUpTable;
    }

    /**
     * returns simulation time (in seconds unit) from properties file
     */
    public long getSimulationTime()
    {
        return SIMULATION_TIME;
    }

    /**
     * returns warm up period (in seconds unit) from properties file
     */
    public double getWarmUpPeriod()
    {
        return WARM_UP_PERIOD;
    }

    public long getLOAD_GENERATE_TIME(){ return LOAD_GENERATE_TIME; }

    /**
     * returns WLAN bandwidth (in Mbps unit) from properties file
     */
    public double getWlanBandwidth()
    {
        return BANDWITH_WLAN;
    }

    public double getLanBandwidth(){return BANDWITH_LAN;}

    public double getGSMBandwidth(){return BANDWITH_GSM;}

    /**
     * returns MAN bandwidth (in Mbps unit) from properties file
     */
    public double getManBandwidth()
    {
        return BANDWITH_MAN;
    }

    /**
     * returns WAN bandwidth (in Mbps unit) from properties file
     */
    public double getWanBandwidth()
    {
        return BANDWITH_WAN;
    }

    /**
     * returns GSM bandwidth (in Mbps unit) from properties file
     */
    public double getGsmBandwidth()
    {
        return BANDWITH_GSM;
    }

    /**
     * returns the minimum number of the mobile devices used in the simulation
     */
    public int getMinNumOfMobileDev()
    {
        return MIN_NUM_OF_MOBILE_DEVICES;
    }

    /**
     * returns the maximum number of the mobile devices used in the simulation
     */
    public int getMaxNumOfMobileDev()
    {
        return MAX_NUM_OF_MOBILE_DEVICES;
    }


    /**
     * returns the number of increase on mobile devices
     * while iterating from min to max mobile device
     */
    public int getMobileDevCounterSize()
    {
        return MOBILE_DEVICE_COUNTER_SIZE;
    }

    /**
     * returns simulation screnarios as string
     */
    public String[] getSimulationScenarios()
    {
        return SIMULATION_SCENARIOS;
    }

    public String[] getUseScenarios(){
        return USE_SCENARIOS;
    }

    /**
     * returns orchestrator policies as string
     */
    public String[] getOrchestratorPolicies()
    {
        return ORCHESTRATOR_POLICIES;
    }

    //获取模拟场景的区域范围（KM）
    public double[] getAreaRange()
    {
        return areaRange;
    }

    public double[][] getAppLookUpTable()
    {
        return appLookUpTable;
    }

    public void setAPP_CCR(double CCR){
        for(int i=0; i<appLookUpTable.length; i++){
            appLookUpTable[i][9] = CCR;
        }
    }

    public double[] getAppProperties(String appName) {
        double[] result = null;
        int index = -1;
        for (int i = 0; i< appNames.length; i++) {
            if (appNames[i].equals(appName)) {
                index = i;
                break;
            }
        }

        if(index >= 0 && index < appLookUpTable.length)
            result = appLookUpTable[index];

        return result;
    }

    public String getAppName(int appType)
    {
        return appNames[appType];
    }

    public double[][] getEdgedeviceLookUpTable()
    {
        return edgeDeviceLookUpTable;
    }
    public String getEdgedeviceName(int device_id)
    {
        return edgeDeviceNames[device_id];
    }

    private void isAttributePresent(Element element, String key) {
        String value = element.getAttribute(key);
        if (value.isEmpty() || value == null){
            throw new IllegalArgumentException("Attribute '" + key + "' is not found in '" + element.getNodeName() +"'");
        }
    }

    private void isElementPresent(Element element, String key) {
        try {
            String value = element.getElementsByTagName(key).item(0).getTextContent();
            if (value.isEmpty() || value == null){
                throw new IllegalArgumentException("Element '" + key + "' is not found in '" + element.getNodeName() +"'");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Element '" + key + "' is not found in '" + element.getNodeName() +"'");
        }
    }

    private Boolean checkElement(Element element, String key) {
        Boolean result = true;
        try {
            String value = element.getElementsByTagName(key).item(0).getTextContent();
            if (value.isEmpty() || value == null){
                result = false;
            }
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    private void parseApplicationsXML(String filePath)
    {
        Document doc = null;
        try {
            File devicesFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(devicesFile);
            doc.getDocumentElement().normalize();

            String[] mandatoryAttributes = {
                    "usage_percentage", //usage percentage [0-100]
                    "poisson_interarrival", //poisson mean (sec)
                    "active_period", //active period (sec)
                    "idle_period", //idle period (sec)
                    "data_upload", //avg data upload (KB)
                    "data_download", //avg data download (KB)
                    "app_length", //avg task length (MI)
                    "avg_num_task",//app的平均任务数量
                    "shape_factor",//DAG形状因子
                    "app_CCR",//计算传输比
                    "ev_ds"//正态分布均值方差比
            }; //delay_sensitivity [0-1]

            NodeList appList = doc.getElementsByTagName("application");
            appLookUpTable = new double[appList.getLength()]
                    [mandatoryAttributes.length];

            appNames = new String[appList.getLength()];
            for (int i = 0; i < appList.getLength(); i++) {
                Node appNode = appList.item(i);

                Element appElement = (Element) appNode;
                isAttributePresent(appElement, "name");
                String appName = appElement.getAttribute("name");
                appNames[i] = appName;

                for(int m=0; m<mandatoryAttributes.length; m++){
                    isElementPresent(appElement, mandatoryAttributes[m]);
                    appLookUpTable[i][m] = Double.parseDouble(appElement.
                            getElementsByTagName(mandatoryAttributes[m]).item(0).getTextContent());
                }
            }
        } catch (Exception e) {
            System.out.println("Edge Devices XML cannot be parsed! Terminating simulation...");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void parseEdgeDevicesXML(String filePath) {
        Document doc;
        try {
            File devicesFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(devicesFile);
            doc.getDocumentElement().normalize();

            String[] mandatoryAttributes = {
                    "id",
                    "mips",
                    "idle",
                    "latitude",
                    "longitude",
                    "attractiveness",
                    "downloadspeed",
                    "uploadspeed",
                    "radius"
            };

            NodeList deviceList = doc.getElementsByTagName("device");
            edgeDeviceLookUpTable = new double[deviceList.getLength()]
                    [mandatoryAttributes.length];

            Set<Double> attractivenessType = new HashSet<>();

            edgeDeviceNames = new String[deviceList.getLength()];
            for (int i = 0; i < deviceList.getLength(); i++) {
                Node deviceNode = deviceList.item(i);

                Element deviceElement = (Element) deviceNode;
                isAttributePresent(deviceElement, "name");
                String deviceName = deviceElement.getAttribute("name");
                edgeDeviceNames[i] = deviceName;

                for (int m = 0; m < mandatoryAttributes.length; m++) {
                    isElementPresent(deviceElement, mandatoryAttributes[m]);
                    edgeDeviceLookUpTable[i][m] = Double.parseDouble(deviceElement.
                            getElementsByTagName(mandatoryAttributes[m]).item(0).getTextContent());
                }
                if(!attractivenessType.contains(edgeDeviceLookUpTable[i][5])){
                    attractivenessType.add(edgeDeviceLookUpTable[i][5]);
                    Attractiveness_NUM++;
                }
            }
        } catch (Exception e) {
            System.out.println("Edge Devices XML cannot be parsed! Terminating simulation...");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
