package com.chl.victory.core.qrcode;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.chl.victory.core.util.ImageUtils;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * @author ChenHailong
 * @date 2020/7/20 17:52
 **/
public class QRCodeTest {

    @Test
    public void test1() throws Exception {
        String content;
        byte[] imgBytes;
        String savePath;

        // 博予官网
        /*content = "https://by.5jym.com";
        imgBytes = IOUtils.toByteArray(new FileInputStream(new File("D:\\qrcode\\bykj.png")));
        savePath = "D:\\qrcode\\by-site.jpg";*/

        // 博予小商店 宣传页
        /*content = "https://by.5jym.com/promotion.html";
        imgBytes = IOUtils.toByteArray(new FileInputStream(new File("D:\\qrcode\\wmall-promotion.png")));
        savePath = "D:\\qrcode\\wmall-promotion-page.jpg";*/

        //
        content = "https://by.5jym.com/salepartner.html";
        imgBytes = IOUtils.toByteArray(new FileInputStream(new File("D:\\qrcode\\wmall-salepartner.png")));
        savePath = "D:\\qrcode\\wmall-salepartner-page.jpg";

        byte[] bytes = QRCodeGenerator.generate(content, imgBytes);
        IOUtils.write(bytes, new FileOutputStream(new File(savePath)));
    }

    @Test
    public void test() throws Exception {
        byte[] generate = QRCodeGenerator.generate(
                "http://by.5jym.com123456783451113333333333331111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111");
        IOUtils.write(generate, new FileOutputStream(new File("D:\\qrcode\\qrcode.jpg")));
    }
}
