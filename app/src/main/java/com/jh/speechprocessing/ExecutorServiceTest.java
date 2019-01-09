//package com.jh.speechprocessing;
//
//import org.junit.Test;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * Email: 1004260403@qq.com
// * Created by jinhui on 2019/1/9.
// *
// *
// *  @Test： 需要引入库: implementation 'org.testng:testng:6.9.6'
// * 运行例子打印程序:
// *
// * pool-1-thread-1:1547004627253
// * pool-1-thread-2:1547004627253
// * pool-1-thread-3:1547004627253
// * pool-1-thread-4:1547004627253
// * pool-1-thread-4:1547004628256
// * pool-1-thread-3:1547004628256
// * pool-1-thread-2:1547004628256
// * pool-1-thread-1:1547004628256
// * pool-1-thread-4:1547004629256
// * pool-1-thread-2:1547004629256
// */
//public class ExecutorServiceTest {
//
//    @Test
//    public void simpleUsage() throws Exception {
//        ExecutorService executorService = Executors.newFixedThreadPool(4);
//        for (int i = 0; i < 10; i++) {
//            executorService.submit(new Runnable() {
//                @Override
//                public void run() {
//                    System.out.println(
//                            Thread.currentThread().getName()
//                    + ":" + System.currentTimeMillis());
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
//        Thread.sleep(4* 1000);
//
//
//
//
//    }
//}
