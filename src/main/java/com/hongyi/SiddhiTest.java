package com.hongyi;

import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author User
 * @date 2020/11/12 10:32
 */
public class SiddhiTest {

    private static final Logger LOG = Logger.getLogger(AggregationTestCase.class);
    private AtomicInteger inEventCount;
    private AtomicInteger removeEventCount;
    private boolean eventArrived;
    private List<Object[]> inEventsList;
    private List<Object[]> removeEventsList;
    @BeforeMethod
    public void init() {
        inEventCount = new AtomicInteger(0);
        removeEventCount = new AtomicInteger(0);
        eventArrived = false;
        inEventsList = new ArrayList<>();
        removeEventsList = new ArrayList<>();
    }
    @Test
    public static  void siddhitest() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();

        String stockStream = "" +
                " define stream stockStream (arrival long, symbol string, price float, volume int); ";

        String query = "" +
                " @info(name = 'query3') " +
                " define aggregation stockAggregation " +
                " from stockStream " +
                " select sum(price) as sumPrice " +
                " group by price " +
                " aggregate every sec, min, hour, day";

         siddhiManager.createSiddhiAppRuntime(stockStream + query);

        LOG.info("incrementalStreamProcessorTest2");
        SiddhiManager siddhiManager2 = new SiddhiManager();

        String stockStream2 = "" +
                " define stream stockStream (arrival long, symbol string, price float, volume int); ";

        String query2 = "" +
                " @info(name = 'query2') " +
                " define aggregation stockAggregation " +
                " from stockStream " +
                " select sum(price) as sumPrice " +
                " aggregate every sec ... min";

        siddhiManager.createSiddhiAppRuntime(stockStream2 + query2);

        LOG.info("incrementalStreamProcessorTest3");
        SiddhiManager siddhiManager3 = new SiddhiManager();

        String stockStream3 = "" +
                " define stream stockStream (arrival long, symbol string, price float, volume int); ";

        String query3 = "" +
                " @info(name = 'query3') " +
                " define aggregation stockAggregation " +
                " from stockStream " +
                " select sum(price) as sumPrice " +
                " group by price " +
                " aggregate every sec, min, hour, day";

        siddhiManager.createSiddhiAppRuntime(stockStream3 + query3);

        LOG.info("incrementalStreamProcessorTest4");
        SiddhiManager siddhiManager4 = new SiddhiManager();

        String stockStream4 = "" +
                " define stream stockStream (arrival long, symbol string, price float, volume int); ";

        String query4 = "" +
                " @info(name = 'query3') " +
                " define aggregation stockAggregation " +
                " from stockStream " +
                " select sum(price) as sumPrice " +
                " group by price, volume " +
                " aggregate every sec, min, hour, day";

        siddhiManager.createSiddhiAppRuntime(stockStream4 + query4);

        //添加事件
        LOG.info("incrementalStreamProcessorTest5");
        SiddhiManager siddhiManager5 = new SiddhiManager();

        String stockStream5 =
                "define stream stockStream (symbol string, price float, lastClosingPrice float, volume long , " +
                        "quantity int, timestamp long);";
        String query5 = " define aggregation stockAggregation " +
                "from stockStream " +
                "select symbol, avg(price) as avgPrice, sum(price) as totalPrice, (price * quantity) " +
                "as lastTradeValue  " +
                "group by symbol " +
                "aggregate by timestamp every sec...hour ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(stockStream5 + query5);

        InputHandler stockStreamInputHandler = siddhiAppRuntime.getInputHandler("stockStream");
        siddhiAppRuntime.start();
        System.out.println(System.currentTimeMillis());
        stockStreamInputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1605151824418L});
        stockStreamInputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1605151825418L});

        stockStreamInputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1605151826418L});
        stockStreamInputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1605151823118L});

        stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1605151823518L});
        stockStreamInputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1605151823018L});
        Thread.sleep(100);

        Event[] events = siddhiAppRuntime.query("from stockAggregation " +
                "within \"2016-06-06 12:00:00 +05:30\", \"2020-11-13 12:00:00 +05:30\" " +
                "per \"seconds\"");
        EventPrinter.print(events);
        System.out.println(("events+++++++++++++++++++++++"+Arrays.toString(events)));
        AssertJUnit.assertEquals(5, events.length);
        List<Object[]> eventsOutputList = new ArrayList<>();
        for (Event event : events) {
            eventsOutputList.add(event.getData());
        }
        List<Object[]> expected = Arrays.asList(
                new Object[]{1496289952000L, "WSO2", 80.0, 160.0, 1600f},
                new Object[]{1496289950000L, "WSO2", 60.0, 120.0, 700f},
                new Object[]{1496289954000L, "IBM", 100.0, 200.0, 9600f}
        );
        AssertJUnit.assertEquals("In events matched", false,
                SiddhiTestHelper.isUnsortedEventsMatch(eventsOutputList, expected));
        siddhiAppRuntime.shutdown();
    }
}
