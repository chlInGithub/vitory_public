package com.chl.victory;

import org.junit.Test;

/**
 * @author ChenHailong
 * @date 2019/5/16 17:27
 **/
public class StringTest {
    @Test
    public void testCompare(){
        System.out.println("12345_0".compareTo("12345"));
        System.out.println("12345_0".compareTo("12345_1"));
        System.out.println("12345_1".compareTo("12345_2"));
    }
}
