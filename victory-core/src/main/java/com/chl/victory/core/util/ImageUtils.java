package com.chl.victory.core.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/10/29 14:39
 **/
public class ImageUtils {

    public static Image gen(InputStream inputStream, String watermark) throws Exception {
        Image image = gen(inputStream);
        if (null == image) {
            return null;
        }
        if (watermark == null || watermark.trim().length() == 0) {
            return image;
        }

        int height = image.getHeight(null);
        int width = image.getWidth(null);
        ImageUtils.JoinData joinData1 = new ImageUtils.JoinData(image, 0, 0);
        ImageUtils.JoinData joinData2 = new ImageUtils.JoinData(watermark, 10, height - 10);
        Image join = ImageUtils.join(width, height, Arrays.asList(joinData1, joinData2));
        return join;
    }

    public static Image gen(InputStream inputStream) throws Exception {
        if (null == inputStream) {
            return null;
        }
        Image image = ImageIO.read(inputStream);
        return image;
    }

    public static Image gen(byte[] bytes) throws Exception {
        if (null == bytes) {
            return null;
        }
        Image image = ImageIO.read(new ByteArrayInputStream(bytes));
        return image;
    }
    /**
     * 通过bytes生成image，根据width和height对image进行缩放。
     * @param bytes
     * @param width
     * @param height
     * @return
     * @throws Exception
     */
    public static Image gen(byte[] bytes, Integer width, Integer height) throws Exception {
        if (null == bytes) {
            return null;
        }
        Image image = ImageIO.read(new ByteArrayInputStream(bytes));
        if (null == width || null == height) {
            return image;
        }

        int tempWidth = ((BufferedImage) image).getWidth();
        if (tempWidth == width) {
            return image;
        }
        image = image.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
        return image;
    }

    /**
     * 拼接多个图片，生成一张图
     * @param width
     * @param height
     * @param joinDatas
     * @return
     */
    public static Image join(Integer width, Integer height, List<JoinData> joinDatas) {
        if (null == width || null == height || null == joinDatas || joinDatas.isEmpty()) {
            return null;
        }

        int top = 0;
        int left = 0;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.fillRect(top, left, width, height);

        for (JoinData joinData : joinDatas) {
            Integer x = joinData.getX();
            Integer y = joinData.getY();
            Object content = joinData.getContent();
            if (content instanceof Image){
                graphics.drawImage((Image) content, x, y, null);
            }
            else if (content instanceof String) {
                graphics.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 5));
                Color oldColor = graphics.getColor();
                graphics.setColor(Color.LIGHT_GRAY);
                graphics.drawString((String)content, x, y);
                graphics.setColor(oldColor);
            }
        }

        graphics.dispose();

        return image;
    }

    /**
     * @param <T> T Image or String
     */
    @Data
    @AllArgsConstructor
    public static class JoinData<T>{
        T content;
        Integer x;
        Integer y;
    }

    public static byte[] getBytes(Image image) throws Exception {
        if (null == image) {
            return null;
        }

        String imageFormat = "JPEG";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            write(image, imageFormat, outputStream);
            return outputStream.toByteArray();
        } finally {
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
                outputStream = null;
            }
        }
    }

    /**
     * <b>注意调用方法后关闭outputStream</b>
     * @param image
     * @param imageFormat
     * @param outputStream
     * @throws Exception
     */
    public static void write(Image image, String imageFormat, OutputStream outputStream) throws Exception {
        ImageIO.write((RenderedImage) image, null == imageFormat ? "JPEG" : imageFormat, outputStream);
    }

    /**
     * 图片弧形边框
     * @param srcImage
     * @param width
     * @param height
     * @param cornerRadius
     * @return
     */
    public static Image borderRadius(Image srcImage, Integer width, Integer height, Integer cornerRadius) {
        if (null == srcImage || null == width || null == height || null == cornerRadius) {
            return null;
        }

        int w = width;
        int h = height;
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);
        g2.setComposite(AlphaComposite.SrcIn);
        g2.drawImage(srcImage, 0, 0, w, h, null);
        g2.dispose();
        return output;
    }
}
