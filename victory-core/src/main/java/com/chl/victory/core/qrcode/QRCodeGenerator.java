package com.chl.victory.core.qrcode;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.chl.victory.core.util.ImageUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.io.IOUtils;

public class QRCodeGenerator {

    /**
     * 二维码 size:500*500
     * @param content
     * @return return null when ex
     */
    public static byte[] generate(String content) {
        try {
            BufferedImage image = createQrCode(content);
            return ImageUtils.getBytes(image);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 二维码 + 中间图片 size:500*500
     * @param content
     * @param centerImgBytes
     * @return
     */
    public static byte[] generate(String content, byte[] centerImgBytes) {
        byte[] bytes = null;
        try {
            BufferedImage image = QRCodeGenerator.createQrCode(content);
            int width = image.getWidth();
            int imgWidth = 85;

            Image img = ImageUtils.gen(centerImgBytes, imgWidth, imgWidth);
            img = ImageUtils.borderRadius(img, imgWidth, imgWidth, imgWidth/4);

            List<ImageUtils.JoinData> joinDataList = new ArrayList<>();
            joinDataList.add(new ImageUtils.JoinData(image, 0, 0));
            joinDataList.add(new ImageUtils.JoinData(img, width / 2 - imgWidth/2, width / 2 - imgWidth/2));
            Image join = ImageUtils.join(width, width, joinDataList);

            bytes = ImageUtils.getBytes(join);
        } catch (Exception e) {
        }
        return bytes;
    }

    public static BufferedImage createQrCode(String content) throws WriterException, IOException {
        // 通过多次尝试(二维码中间添加图片，图片模糊程度，是否影响识别等)，500宽高较合适
        Integer qrCodeSize = 500;

        Hashtable<EncodeHintType, Object> hintMap = new Hashtable<>();
        // 纠错级别
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        // 留白
        hintMap.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        // QR码位矩阵
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);

        // 使BufferedImage勾画QRCode  (matrixWidth 是行二维码像素点)
        int width = bitMatrix.getWidth();
        BufferedImage image = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        // 抗锯齿，但是没看出效果
        // graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.fillRect(0, 0, width, width);
        // 使用比特矩阵画并保存图像
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                if (bitMatrix.get(i, j)) {
                    graphics.setColor(Color.BLACK);
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }

        graphics.dispose();

        return image;
    }
}
