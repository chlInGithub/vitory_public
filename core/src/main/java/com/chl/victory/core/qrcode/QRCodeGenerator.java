package com.chl.victory.core.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Hashtable;

public class QRCodeGenerator {
    public static void generate(String content){
        //TODO
        System.out.println("Here is QRCodeGenerator.generate");

        System.out.println(content.length());
        OutputStream outputStream = null;
        try {
            int qrCodeSize = getRrCodeSize(content);
            outputStream = new FileOutputStream(new File("D:\\qrcode\\qrcode.jpg"));
            createQrCode(outputStream,
                    content,qrCodeSize,"JPEG");
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != outputStream){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                outputStream = null;
            }
        }
        //String result = readQrCode(new FileInputStream(new File("d:\\qrcode\\qrcode.jpg")));
    }

    private static int getRrCodeSize(String content) {
        if (content.length() <= 30){
            return 750;
        }
        if (content.length() <= 70){
            return 850;
        }
        if (content.length() <= 100){
                return 950;
        }
        if (content.length() <= 300){
                return 1200;
        }
        return 1200;
    }

    public static boolean createQrCode(OutputStream outputStream, String content, int qrCodeSize, String imageFormat) throws WriterException, IOException {
        Hashtable<EncodeHintType, Object> hintMap = new Hashtable<>();
        // 纠错级别
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        // 留白
        hintMap.put(EncodeHintType.MARGIN, 5);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        // QR码位矩阵
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);

        // 使BufferedImage勾画QRCode  (matrixWidth 是行二维码像素点)
        int matrixWidth = bitMatrix.getWidth();
        int width = matrixWidth - 200;
        BufferedImage image = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        // 使用比特矩阵画并保存图像
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < matrixWidth; i++){
            for (int j = 0; j < matrixWidth; j++){
                if (bitMatrix.get(i, j)){
                    graphics.fillRect(i-100, j-100, 1, 1);
                }
            }
        }

        // 增值服务：中心添加图片
        int nullSpace = matrixWidth/8;
        int center = matrixWidth/2 - nullSpace/2 - 100;
        graphics.setColor(Color.WHITE);
        graphics.fillRect(center, center, nullSpace, nullSpace);

        return ImageIO.write(image, imageFormat, outputStream);
    }
}
