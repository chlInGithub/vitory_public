package com.chl.victory;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author ChenHailong
 * @date 2019/8/5 15:03
 **/
public class IteratorTest {
    List<Integer> list;
    @Before
    public void before() {
        list = new ArrayList<>();
        list.addAll(Arrays.asList(1, 2, 3, 4, 5, 66, 76));
    }

    @Test
    public void testIterator() {
        Thread iteratorThread1 = new Thread(){
            public void run() {
                Iterator<Integer> iterator = list.iterator();
                System.out.println(" iterator ok");
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (iterator.hasNext()) {
                    // 允许iterator迭代过程remove集合元素，但不允许其他线程修改集合
                    System.out.println(this.hashCode() + " " + iterator.next());
                }
            }
        };
        Thread iteratorThread2 = new Thread(){
            public void run() {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Iterator<Integer> iterator = list.iterator();
                System.out.println(" iterator ok");
                while (iterator.hasNext()) {
                    System.out.println(this.hashCode() + " " + iterator.next());
                }
            }
        };
        Thread modifyThread = new Thread(){
            public void run() {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("doing modify");
                list.remove(1);
            }
        };

        iteratorThread1.start();
        iteratorThread2.start();
        modifyThread.start();

        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
