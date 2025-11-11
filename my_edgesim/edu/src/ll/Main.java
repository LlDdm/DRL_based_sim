/*
 * Title:        EdgeCloudSim - Main Application
 *
 * Description:  Main application for Sample App2
 *
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package ll;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Main {

    /**
     * Creates main() to run this example
     */
    public static void main(String[] args) {
        Map<Integer, Map<String, Double>> deviceNum_condition_avgCompleteRatio = new HashMap<>();
        Map<Integer, Map<String, Double>> deviceNum_condition_averageMakeSpan = new HashMap<>();
        //Map<Integer, Map<String, Double>> deviceNum_condition_avgAppNum = new HashMap<>();
        Map<String, Map<Integer, Double>> condition_InAndOut_avgAppNum = new HashMap<>();
        Map<String,Map<Integer, Map<String, Double>>> resultStyl_DeviceNum =  new HashMap<>();

        Map<String, Map<Integer, Double>> condition_deviceNum_avgCompleteRatio = new HashMap<>();
        Map<String, Map<Integer, Double>> condition_deviceNum_averageMakeSpan = new HashMap<>();
        //Map<String, Map<Integer, Double>> condition_deviceNum_avgAppNum = new HashMap<>();

        Map<Double,Map<String, Double>> CCR_condition_avgCompleteRatio = new HashMap<>();
        Map<Double,Map<String, Double>> CCR_condition_averageMakeSpan = new HashMap<>();
        //Map<Double,Map<String, Double>> CCR_condition_avgAppNum = new HashMap<>();
        Map<String, Map<Double, Double>> condition_CCR_avgAppNum = new HashMap<>();
        Map<String,Map<Double, Map<String, Double>>> resultStyl_CCR =  new HashMap<>();

        Map<String, Map<Double, Double>> condition_CCR_avgCompleteRatio = new HashMap<>();
        Map<String, Map<Double, Double>> condition_CCR_averageMakeSpan = new HashMap<>();
        //Map<String, Map<Double, Double>> condition_CCR_avgAppNum = new HashMap<>();

        int iterationNumber = 1;
        String configFile = "";
        //String outputFolder = "";
        String edgeDevicesFile = "";
        String applicationsFile = "";
        if (args.length == 5) {
            configFile = args[0];
            edgeDevicesFile = args[1];
            applicationsFile = args[2];
            //outputFolder = args[3];
            iterationNumber = Integer.parseInt(args[4]);
        } else {
            System.out.println("Simulation setting file, output folder and iteration number are not provided! Using default ones...");
            configFile = "my_edgesim/edu/property/scenario.properties";
            applicationsFile = "my_edgesim/edu/property/applications.xml";
            edgeDevicesFile = "my_edgesim/edu/property/devices.XML";
            //outputFolder = "sim_results/ite" + iterationNumber;
        }

        //load settings from configuration file
        SimSettings SS = SimSettings.getInstance();
        if (!SS.initialize(configFile, edgeDevicesFile, applicationsFile)) {
            System.out.println("cannot initialize simulation settings!");
            System.exit(0);
        }

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date SimulationStartDate = Calendar.getInstance().getTime();
        String now = df.format(SimulationStartDate);
        System.out.println("Simulation started at " + now);
        System.out.println("----------------------------------------------------------------------");

        // Generate EdgeCloudsim Scenario Factory
        ScenarioFactory sampleFactory = new LScenarioFactory(SS.getLOAD_GENERATE_TIME(), (long) SS.getWarmUpPeriod());

        // Generate EdgeCloudSim Simulation Manager
        SimManager manager = new SimManager(sampleFactory);

        manager.setEdgeDeviceGeneratorModel();

        manager.setNativeEdgeDeviceGenerator();

        manager.setNetworkModel();


        for (int k = 0; k < SS.getSimulationScenarios().length; k++) {
            String simScenario = SS.getSimulationScenarios()[k];
            sampleFactory.setSimScenario(simScenario);
            manager.setSimScenario(simScenario);

            for (int l = 0; l < SS.getUseScenarios().length; l++) {
                String useScenario = SS.getUseScenarios()[l];
                sampleFactory.setUseScenario(useScenario);
                manager.setUseScenario(useScenario);

//                double max_timeout_tolerance;
//                double init_timeout_tolerance;

                int max_mobileDeviceNum = SS.getMaxNumOfMobileDev();
//                int MobileDevice_counterSize = SS.getMobileDevCounterSize();

//                if(useScenario.equals("OFF")) {
//                    init_timeout_tolerance = 0.1;
//                    max_timeout_tolerance = 0.1;
//                    max_mobileDeviceNum = 30;
//                    MobileDevice_counterSize = 5;
//                }
//                else {
//                    max_timeout_tolerance = 0.1;
//                    init_timeout_tolerance = 0.1;
//                }
//                max_timeout_tolerance = 0.1;
//                init_timeout_tolerance = 0.1;
//                Map<Integer, Double> avgLoop_AppNum = new HashMap<>();

//                for (int j = SS.getMinNumOfMobileDev(); j <= max_mobileDeviceNum; j += MobileDevice_counterSize) {
//                    sampleFactory.setNumOfMobileDevice(j);
//                    manager.setNumOfMobileDevice(j);
                sampleFactory.setNumOfMobileDevice(max_mobileDeviceNum);
                manager.setNumOfMobileDevice(max_mobileDeviceNum);

                Map<Integer, Double> avgLoop_AppNum = new HashMap<>();

//                    Map<String, Double> condition_averageMakeSpan = new HashMap<>();
//                    Map<String, Double> condition_avgCompleteRatio = new HashMap<>();
//                    Map<String, Double> condition_avgAppNum = new HashMap<>();

//                    Map<String, Double> avgLoop_MakeSpan = new HashMap<>();
//                    Map<String, Double> avgLoop_CompleteRatio = new HashMap<>();
//                    avgLoop_AppNum.putIfAbsent(j,0.0);
//                    double avg_appNum = 0;

                for (double InAndOut = 50; InAndOut <= 100; InAndOut += 10) {
                    SS.InAndOut_ratio = InAndOut;

                    Map<String, Double> avgLoop_MakeSpan = new HashMap<>();
                    Map<String, Double> avgLoop_CompleteRatio = new HashMap<>();
                    avgLoop_AppNum.putIfAbsent((int) InAndOut, 0.0);
                    double avg_appNum = 0;

                    for (int h = 0; h < 2; h++) {
                        manager.setLoadGeneratorModel();
                        manager.getNetworkModel().generateMobileBW();
                        int App_num = manager.getLoadGeneratorModel().getApp_num();
                        avg_appNum += App_num;
                        avgLoop_AppNum.replace((int) InAndOut, avg_appNum);

                        String condition = null;
//                            for (double Timeout_tolerance = init_timeout_tolerance; Timeout_tolerance <= max_timeout_tolerance; Timeout_tolerance += 0.2) {
//                            manager.setTimeout_tolerance(Timeout_tolerance);

                        for (int i = 0; i < SS.getOrchestratorPolicies().length; i++) {
                            String orchestratorPolicy = SS.getOrchestratorPolicies()[i];
                            manager.setOrchestratorPolicy(orchestratorPolicy);

                            condition = simScenario + "_" + orchestratorPolicy + "_" + useScenario + "_InAndOut";

                            avgLoop_MakeSpan.putIfAbsent(condition + "_AM", 0.0);
                            avgLoop_CompleteRatio.putIfAbsent(condition + "_AC", 0.0);

//                                double average_makeSpan = 0;
//                                double avg_completeRatio = 0;
                            //double avg_AppNum = 0;

                            System.out.println(condition + "_CCR:0.5" + "_InAndOut:" + InAndOut);
                            System.out.println("warm up period: " + SS.getWarmUpPeriod() + " sec - #appNuM: " + App_num);


                            double average_makeSpan_of_loop;
                            double completeRatio_of_loop;

                            long makeSpan = 0;

                            manager.result.clear();
                            manager.OverDeadline = 0;

//                                manager.setLoadGeneratorModel();
//                                manager.getNetworkModel().generateMobileBW();

                            Date ScenarioStartDate = Calendar.getInstance().getTime();
                            now = df.format(ScenarioStartDate);
                            System.out.println("loop:" + h + "_" + condition + " -APPNum:" + App_num + "_InAndOut:" +InAndOut + " -CCR: 0.5 -Scenario started at " + now);

                            manager.wait_complete = new CountDownLatch(App_num);
                            manager.Running();

                            // Start simulation
                            manager.startSimulation();

                            try {
                                manager.wait_complete.await();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }

                            manager.stopRunning();

                            Date ScenarioEndDate = Calendar.getInstance().getTime();
                            now = df.format(ScenarioEndDate);
                            System.out.println("Scenario finished at " + now + ". It took " + SimUtils.getTimeDifference(ScenarioStartDate, ScenarioEndDate));
//                                System.out.println("----------------------------------------------------------------------");

                            for (long result : manager.result)
                                makeSpan += result;

                            int resultSize = manager.result.size();
                            completeRatio_of_loop = (double) manager.OverDeadline / resultSize;
                            average_makeSpan_of_loop = (double) makeSpan / resultSize;
                            System.out.println("CR:" + completeRatio_of_loop);
                            System.out.println("MS:" + average_makeSpan_of_loop);
                            System.out.println("----------------------------------------------------------------------");

                            completeRatio_of_loop += avgLoop_CompleteRatio.get(condition + "_AC");
                            avgLoop_CompleteRatio.replace(condition + "_AC", completeRatio_of_loop);

                            average_makeSpan_of_loop += avgLoop_MakeSpan.get(condition + "_AM");
                            avgLoop_MakeSpan.replace(condition + "_AM", average_makeSpan_of_loop);

                            manager.updateLoad();
//                                avg_completeRatio /= 10;
//                                average_makeSpan /= 10;
//                                avg_AppNum /= 10;
//
//                                condition_avgCompleteRatio.put(condition + "_AC", avg_completeRatio);
//                                condition_averageMakeSpan.put(condition + "_AM", average_makeSpan);
//                                condition_avgAppNum.put(condition + "_AN", avg_AppNum);
//
//                                System.out.println("condition_DeviceNum" + j + "_average_completeRatio: " + condition_avgCompleteRatio);
//                                System.out.println("condition_DeviceNum" + j + "_average_makeSpan: " + condition_averageMakeSpan);
//                                System.out.println("condition_DeviceNum" + j + "_average_appNum: " + condition_avgAppNum);
//                                System.out.println("Condition: " + condition + "done!");
                        }//End of orchestrators loop
//                            }//end of Timeout loop
                    }// end of h loop
//                    }

                    for (Map.Entry<String, Double> entry : avgLoop_MakeSpan.entrySet()) {
                        double sum = entry.getValue();
                        entry.setValue(sum / 2);
                    }
                    for (Map.Entry<String, Double> entry : avgLoop_CompleteRatio.entrySet()) {
                        double sum = entry.getValue();
                        entry.setValue(sum / 2);
                    }

                    System.out.println("InAndOut:" + InAndOut + "_average_completeRatio: " + avgLoop_CompleteRatio);
                    System.out.println("InAndOut:" + InAndOut + "_average_makeSpan: " + avgLoop_MakeSpan);
                    System.out.println("InAndOut:" + InAndOut + "done!");

                    deviceNum_condition_avgCompleteRatio.put((int) InAndOut, avgLoop_CompleteRatio);
                    resultStyl_DeviceNum.put("avgCompleteRatio", deviceNum_condition_avgCompleteRatio);

                    deviceNum_condition_averageMakeSpan.put((int) InAndOut, avgLoop_MakeSpan);
                    resultStyl_DeviceNum.put("avgMakeSpan", deviceNum_condition_averageMakeSpan);

                }//End of InAndOut loop

                for (Map.Entry<Integer, Double> entry : avgLoop_AppNum.entrySet()) {
                    double sum = entry.getValue();
                    entry.setValue(sum / 2);
                }
                condition_InAndOut_avgAppNum.put(simScenario + useScenario, avgLoop_AppNum);

            }//End of use scenarios loop
        }//End of scenarios loop


        //CCR
        int Devices_Num = SS.getMaxNumOfMobileDev();
        sampleFactory.setNumOfMobileDevice(Devices_Num);
        manager.setNumOfMobileDevice(Devices_Num);

        String simScenario = SS.getSimulationScenarios()[0];
        sampleFactory.setSimScenario(simScenario);
        manager.setSimScenario(simScenario);

        String useScenario = SS.getUseScenarios()[0];
        sampleFactory.setUseScenario(useScenario);
        manager.setUseScenario(useScenario);

        SS.InAndOut_ratio = 80;

        Map<Double, Double> avgLoop_AppNum = new HashMap<>();

        for (double CCR = 0.2; CCR <= 1; CCR += 0.2) {
            SS.setAPP_CCR(CCR);

//            Map<String, Double> condition_averageMakeSpan = new HashMap<>();
//            Map<String, Double> condition_avgCompleteRatio = new HashMap<>();
//            Map<String, Double> condition_avgAppNum= new HashMap<>();

            Map<String, Double> avgLoop_MakeSpan = new HashMap<>();
            Map<String, Double> avgLoop_CompleteRatio = new HashMap<>();
            avgLoop_AppNum.putIfAbsent(CCR, 0.0);
            double avg_appNum = 0;

            for (int h = 0; h < 2; h++) {
                manager.setLoadGeneratorModel();
                manager.getNetworkModel().generateMobileBW();
                int App_num = manager.getLoadGeneratorModel().getApp_num();
                avg_appNum += App_num;
                avgLoop_AppNum.replace(CCR, avg_appNum);

                String condition = null;
//                for (double Timeout_tolerance = 0.1; Timeout_tolerance <= 0.1; Timeout_tolerance += 0.2) {
//                    manager.setTimeout_tolerance(Timeout_tolerance);

                for (int i = 0; i < SS.getOrchestratorPolicies().length; i++) {
                    String orchestratorPolicy = SS.getOrchestratorPolicies()[i];
                    manager.setOrchestratorPolicy(orchestratorPolicy);

                    condition = simScenario + "_" + orchestratorPolicy + "_" + useScenario + "_CCR_";

                    avgLoop_MakeSpan.putIfAbsent(condition + "_AM", 0.0);
                    avgLoop_CompleteRatio.putIfAbsent(condition + "_AC", 0.0);

//                        double average_makeSpan = 0;
//                        double avg_completeRatio = 0;
                    //double avg_AppNum = 0;

                    System.out.println(condition + "_InAndOut:80" + "_CCR:" + CCR);
                    System.out.println("warm up period: " + SS.getWarmUpPeriod() + " sce - #appNum: " + App_num);

                    double average_makeSpan_of_loop;
                    double completeRatio_of_loop;

                    long makeSpan = 0;
                    manager.result.clear();
                    manager.OverDeadline = 0;

                    Date ScenarioStartDate = Calendar.getInstance().getTime();
                    now = df.format(ScenarioStartDate);
                    System.out.println("loop:" + h + "_"+condition + " -appNum:" + App_num + "_CCR:" + CCR + "-Scenario started at " + now);

                    manager.wait_complete = new CountDownLatch(App_num);
                    manager.Running();

                    // Start simulation
                    manager.startSimulation();

                    try {
                        manager.wait_complete.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    manager.stopRunning();

                    Date ScenarioEndDate = Calendar.getInstance().getTime();
                    now = df.format(ScenarioEndDate);
                    System.out.println("Scenario finished at " + now + ". It took " + SimUtils.getTimeDifference(ScenarioStartDate, ScenarioEndDate));
//                        System.out.println("----------------------------------------------------------------------");

                    for (long result : manager.result)
                        makeSpan += result;


                    int resultSize = manager.result.size();
                    completeRatio_of_loop = (double) manager.OverDeadline / resultSize;
                    average_makeSpan_of_loop = (double) makeSpan / resultSize;
                    System.out.println("CR:" + completeRatio_of_loop);
                    System.out.println("MS:" + average_makeSpan_of_loop);
                    System.out.println("----------------------------------------------------------------------");

                    completeRatio_of_loop += avgLoop_CompleteRatio.get(condition + "_AC");
                    avgLoop_CompleteRatio.replace(condition + "_AC", completeRatio_of_loop);

                    average_makeSpan_of_loop += avgLoop_MakeSpan.get(condition + "_AM");
                    avgLoop_MakeSpan.replace(condition + "_AM", average_makeSpan_of_loop);

//                            avg_completeRatio += completeRatio_of_loop;
//                            average_makeSpan += average_makeSpan_of_loop;
//                            avg_AppNum += resultSize;

                    manager.updateLoad();
                    //End of avg loop


//                        avg_completeRatio /= 10;
//                        average_makeSpan /= 10;
//                        avg_AppNum /= 10;
//
//                        condition_avgCompleteRatio.put(condition + "_AC", avg_completeRatio);
//                        condition_averageMakeSpan.put(condition + "_AM", average_makeSpan);//这里因为类型限制，直接将CCR*10转换为整数
//                        condition_avgAppNum.put(condition + "_AN", avg_AppNum);
//
//                        System.out.println("condition_CCR:" + CCR + "_avgCompleteRatio: " + condition_avgCompleteRatio);
//                        System.out.println("condition_CCR:" + CCR + "_average_makeSpan: " + condition_averageMakeSpan);
//                        System.out.println("condition_CCR:" + CCR + "_avgAppNum: " + condition_avgAppNum);
//                        System.out.println("Condition: " + condition + "done!");
                }//End of orchestrators loop
//                }//end of time loop
            }//end of h loop
            for (Map.Entry<String, Double> entry : avgLoop_MakeSpan.entrySet()) {
                double sum = entry.getValue();
                entry.setValue(sum / 2);
            }
            for (Map.Entry<String, Double> entry : avgLoop_CompleteRatio.entrySet()) {
                double sum = entry.getValue();
                entry.setValue(sum / 2);
            }

            System.out.println("CCR:" + CCR + "_average_completeRatio: " + avgLoop_CompleteRatio);
            System.out.println("CCR:" + CCR + "_average_makeSpan: " + avgLoop_MakeSpan);
            System.out.println("CCR:" + CCR + "done!");

            CCR_condition_avgCompleteRatio.put(CCR, avgLoop_CompleteRatio);
            resultStyl_CCR.put("avgCompleteRatio", CCR_condition_avgCompleteRatio);

            CCR_condition_averageMakeSpan.put(CCR, avgLoop_MakeSpan);
            resultStyl_CCR.put("avgMakeSpan", CCR_condition_averageMakeSpan);

//            CCR_condition_avgAppNum.put(CCR, avgLoop_AppNum);
//            resultStyl_CCR.put("avgAppNum",CCR_condition_avgAppNum);
//            condition_CCR_avgAppNum.put(simScenario + useScenario,avgLoop_AppNum);
        }//End of CCR loop
        for (Map.Entry<Double, Double> entry : avgLoop_AppNum.entrySet()) {
            double sum = entry.getValue();
            entry.setValue(sum / 2);
        }
        condition_CCR_avgAppNum.put(simScenario + useScenario, avgLoop_AppNum);


        // 行列转换
        for(Map.Entry<String, Map<Integer, Map<String, Double>>> entry : resultStyl_DeviceNum.entrySet()) {
            for (Map.Entry<Integer, Map<String, Double>> entry1 : entry.getValue().entrySet()) {
                int deviceNum = entry1.getKey();
                Map<String, Double> condition_result = entry1.getValue();


                for (Map.Entry<String, Double> condition_entry : condition_result.entrySet()) {
                    String condition = condition_entry.getKey();
                    double result = condition_entry.getValue();

                    if(entry.getKey().equals("avgCompleteRatio")){
                        condition_deviceNum_avgCompleteRatio.putIfAbsent(condition, new HashMap<>());
                        condition_deviceNum_avgCompleteRatio.get(condition).put(deviceNum, result);
                    }else {
                        condition_deviceNum_averageMakeSpan.putIfAbsent(condition, new HashMap<>());
                        condition_deviceNum_averageMakeSpan.get(condition).put(deviceNum, result);
                    }
                }
            }
        }

        for(Map.Entry<String, Map<Double, Map<String, Double>>> entry : resultStyl_CCR.entrySet()) {
            for (Map.Entry<Double, Map<String, Double>> entry1 : entry.getValue().entrySet()) {
                double CCR = entry1.getKey();
                Map<String, Double> condition_result = entry1.getValue();


                for (Map.Entry<String, Double> condition_entry : condition_result.entrySet()) {
                    String condition = condition_entry.getKey();
                    double result = condition_entry.getValue();

                    if(entry.getKey().equals("avgCompleteRatio")){
                        condition_CCR_avgCompleteRatio.putIfAbsent(condition, new HashMap<>());
                        condition_CCR_avgCompleteRatio.get(condition).put(CCR, result);
                    }else{
                        condition_CCR_averageMakeSpan.putIfAbsent(condition, new HashMap<>());
                        condition_CCR_averageMakeSpan.get(condition).put(CCR, result);
                    }
                }
            }
        }

        // 创建输出excel文件
        Workbook workbook = new XSSFWorkbook();

        // 写入文件
        try (workbook; FileOutputStream fileOut = new FileOutputStream("result.xlsx")) {
            try {
                for (Map.Entry<String, Map<Integer, Double>> entry : condition_deviceNum_avgCompleteRatio.entrySet()) {
                    create_DeviceNumSheet(workbook, entry.getKey(), entry.getValue());
                }
                for (Map.Entry<String, Map<Integer, Double>> entry : condition_deviceNum_averageMakeSpan.entrySet()) {
                    create_DeviceNumSheet(workbook, entry.getKey(), entry.getValue());
                }
                for (Map.Entry<String, Map<Integer, Double>> entry : condition_InAndOut_avgAppNum.entrySet()) {
                    create_DeviceNumSheet(workbook, entry.getKey(), entry.getValue());
                }
                for (Map.Entry<String, Map<Double, Double>> entry : condition_CCR_avgCompleteRatio.entrySet()) {
                    create_CCRSheet(workbook, entry.getKey(), entry.getValue());
                }
                for (Map.Entry<String, Map<Double, Double>> entry : condition_CCR_averageMakeSpan.entrySet()) {
                    create_CCRSheet(workbook, entry.getKey(), entry.getValue());
                }
                for (Map.Entry<String, Map<Double, Double>> entry : condition_CCR_avgAppNum.entrySet()) {
                    create_CCRSheet(workbook, entry.getKey(), entry.getValue());
                }

                workbook.write(fileOut);
                System.out.println("结果已保存到：result.xlsx！");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Date SimulationEndDate = Calendar.getInstance().getTime();
        now = df.format(SimulationEndDate);
        System.out.println("Simulation finished at " + now + ". It took " + SimUtils.getTimeDifference(SimulationStartDate, SimulationEndDate));
    }

    private static void create_DeviceNumSheet(Workbook workbook, String sheetName, Map<Integer, Double> data) {
        // 检查sheetName是否已存在，若存在则生成唯一的名字
        if (workbook.getSheet(sheetName) != null) {
            int count = 1;
            String newSheetName = sheetName;
            // 如果sheet已存在，循环添加数字后缀直到唯一
            while (workbook.getSheet(newSheetName) != null) {
                newSheetName = sheetName + "_" + count++;
            }
            sheetName = newSheetName; // 修改为新的唯一sheetName
        }

        // 创建工作表
        Sheet sheet = workbook.createSheet(sheetName);

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("deviceNum");
        headerRow.createCell(1).setCellValue("value");

        // 填充数据
        int rowNum = 1;
        for (Map.Entry<Integer, Double> entry : data.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
    }

    private static void create_CCRSheet(Workbook workbook, String sheetName, Map<Double, Double> data) {
        // 检查sheetName是否已存在，若存在则生成唯一的名字
        if (workbook.getSheet(sheetName) != null) {
            int count = 1;
            String newSheetName = sheetName;
            // 如果sheet已存在，循环添加数字后缀直到唯一
            while (workbook.getSheet(newSheetName) != null) {
                newSheetName = sheetName + "_" + count++;
            }
            sheetName = newSheetName; // 修改为新的唯一sheetName
        }

        // 创建工作表
        Sheet sheet = workbook.createSheet(sheetName);

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("CCR");
        headerRow.createCell(1).setCellValue("value");

        // 填充数据
        int rowNum = 1;
        for (Map.Entry<Double, Double> entry : data.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
    }

}
