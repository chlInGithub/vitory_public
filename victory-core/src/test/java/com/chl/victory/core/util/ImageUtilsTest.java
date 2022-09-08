package com.chl.victory.core.util;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;

import org.junit.Test;

/**
 * @author ChenHailong
 * @date 2020/11/11 15:39
 **/
public class ImageUtilsTest {
    @Test
    public void test() throws Exception {
        Image image1 = ImageUtils.gen(new FileInputStream(new File("D:\\qrcode\\qrcode.jpg")));
        int height = image1.getHeight(null);
        int width = image1.getWidth(null);
        ImageUtils.JoinData joinData1 = new ImageUtils.JoinData(image1, 0, 0);
        ImageUtils.JoinData joinData2 = new ImageUtils.JoinData("11111111111111111111111", 0, height - 10);
        Image join = ImageUtils.join(width, height, Arrays.asList(joinData1, joinData2));
        ImageUtils.write(join, null, new FileOutputStream(new File("D:\\qrcode\\mark.jpg")));
    }
}
