package com.hongyi;

import com.lmax.disruptor.SleepingWaitStrategy;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.Arrays;

/**
 * @author User
 * @date 2020/11/19 13:53
 */
public class AggergationTest {
    public static void main(String[] args) throws InterruptedException {
        String requst ="define stream Request (requestTimestamp long, message string);";

        String aggergation =  "@store(type='rdbms' , jdbc.url=\"jdbc:mysql://localhost:3306/fraudDB?useUnicode=true&serverTimezone=UTC&characterEncoding=utf8&useSSL=false\",\n" +
                "            username=\"root\",\n" +
                "            password=\"zhang@123\" ,\n" +
                "            jdbc.driver.name=\"com.mysql.jdbc.Driver\")\n" +
                "define aggregation ApiAgg\n" +
                "from Request\n" +
                "select requestTimestamp, message\n" +
                "group by requestTimestamp\n" +
                "aggregate by requestTimestamp every seconds...years;";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(requst + aggergation);
        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("Request");
        inputHandler.send(new Object[]{System.currentTimeMillis(),"第四次"});
        Thread.sleep(3000);
        inputHandler.send(new Object[]{System.currentTimeMillis(),"第二次"});
        Thread.sleep(3000);
        inputHandler.send(new Object[]{System.currentTimeMillis(),"第六次"});
        Thread.sleep(15000);

        //查询
        String query = "from ApiAgg " +
                " within 1601514000000L, 1605767493356L per 'months' " +
                "select requestTimestamp, message ;";
        Event[] query1 = siddhiAppRuntime.query(query);
        System.out.println(Arrays.toString(query1));

    }
}
