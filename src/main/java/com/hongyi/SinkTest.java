package com.hongyi;

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

/**
 * @author User
 * @date 2020/11/19 13:21
 */
public class SinkTest {
    public static void main(String[] args) throws InterruptedException {
        String outputStream = "@sink(type = 'http', publisher.url = 'http://192.168.2.120:8888/get/alert',@map(type = 'json')) \n"+
                "define stream OutputStream( weight int, totalWeight long);";

        String inputStream ="@App:name(\"HelloWorldApp\")\n" +
                "define stream CargoStream (weight int);";

        String query = (
                "@info(name='HelloWorldQuery') " +
                        "from CargoStream " +
                        "select weight, sum(weight) as totalWeight " +
                        "insert into OutputStream;");
        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + outputStream  + query);
        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("CargoStream");
        inputHandler.send(new Object[]{50});
        inputHandler.send(new Object[]{20});
        inputHandler.send(new Object[]{30});

        siddhiAppRuntime.shutdown();

    }

}
