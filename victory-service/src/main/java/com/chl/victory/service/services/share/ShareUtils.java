package com.chl.victory.service.services.share;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import com.chl.victory.core.util.ImageUtils;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ChenHailong
 * @date 2020/10/28 13:38
 **/
public class ShareUtils {

/*    static Font font;
    static {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File("C:\\Windows\\Fonts\\微软雅黑"));
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    static Map<String, Image> imageCache = Collections.synchronizedMap(new LinkedHashMap<>(10, 2, true));

    static {
        try {
            getFromImage();
            getItemTextImage();
            getShopTextImage();
        } catch (Exception e) {
            throw new Error("get bytes for share", e);
        }
    }

    private static Image getShopTextImage() throws Exception {
        return getImage("ef3ffc15765a5b2d0ca2c8eb09c928cd", null, null);
    }

    private static Image getItemTextImage() throws Exception {
        return getImage("dd4473b1effb211bd23a3d9551ed87a8", null, null);
    }

    private static Image getFromImage() throws Exception {
        return getImage("d6aacd1047954dafbed765a25d2f36b1", null, null);
    }

    private static Image getUserImage(String userImg) throws Exception {
        if (StringUtils.isBlank(userImg)) {
            return null;
        }
        Image image = imageCache.get(userImg);
        if (null == image) {
            byte[] sharerImg = getImageBytesFromUrl(userImg);
            Image scaledInstance4Sharer = ImageUtils.gen(sharerImg, 40, 40);
            image = ImageUtils.borderRadius(scaledInstance4Sharer, 40, 40, 40);
            imageCache.put(userImg, image);
        }
        return image;
    }

    public static byte[] getShareImg4Item(ShareItemParam shareItem) throws Exception {
        int width = 300;
        int height = 400;

        String imgKey = shareItem.imgKey;
        int width4Item = width;
        int height4Item = width4Item;
        Image scaledInstance4Item = getImage(imgKey, width4Item, height4Item);

        byte[] bytes4Share = shareItem.code;
        int width4Code = height - width;
        int height4Code = width4Code;
        Image scaledInstance4Share = ImageUtils.gen(bytes4Share, width4Code, height4Code);

        Image sharerImage = getUserImage(shareItem.getUserImg());

        List<ImageUtils.JoinData> joinDataList = new ArrayList<>();
        joinDataList.add(new ImageUtils.JoinData(scaledInstance4Item, 0, 0));
        joinDataList.add(new ImageUtils.JoinData(scaledInstance4Share, width - width4Code, height4Item));
        joinDataList.add(new ImageUtils.JoinData(getItemTextImage(), 0, height4Item));
        joinDataList.add(new ImageUtils.JoinData(getFromImage(), 0, height4Item + 50));
        if (null != sharerImage) {
            joinDataList.add(new ImageUtils.JoinData(sharerImage, 35, height4Item + 50));
        }

        Image joinImage = ImageUtils.join(width, height, joinDataList);

        byte[] bytes = ImageUtils.getBytes(joinImage);
        return bytes;
    }

    public static byte[] getShareImg4Shop(ShareItemParam shareItem) throws Exception {
        int width = 300;
        int height = 150;

        byte[] bytes4Share = shareItem.code;
        int width4Code = width - height;
        int height4Code = width4Code;
        Image scaledInstance4Share = ImageUtils.gen(bytes4Share, width4Code, height4Code);

        Image sharerImage = getUserImage(shareItem.getUserImg());

        List<ImageUtils.JoinData> joinDataList = new ArrayList<>();
        joinDataList.add(new ImageUtils.JoinData(getShopTextImage(), 0, 10));
        joinDataList.add(new ImageUtils.JoinData(scaledInstance4Share, width - width4Code, 0));
        joinDataList.add(new ImageUtils.JoinData(getFromImage(), 10, 100));
        if (null != sharerImage) {
            joinDataList.add(new ImageUtils.JoinData(sharerImage, 45, 100));
        }

        Image joinImage = ImageUtils.join(width, height, joinDataList);

        byte[] bytes = ImageUtils.getBytes(joinImage);
        return bytes;
    }

    private static Image getImage(String imgKey, Integer width, Integer height) throws Exception {
        Image image = imageCache.get(imgKey);
        if (null == image) {
            String imgPrefix = "https://wmall.5jym.com/img/";
            String imgUrl = imgPrefix + imgKey + (width != null ? ("?w=" + width + "&h=" + height) : "");

            byte[] bytes4Item = getImageBytesFromUrl(imgUrl);
            image = ImageUtils.gen(bytes4Item);

            imageCache.put(imgKey, image);
        }

        return image;
    }

    private static byte[] getImageBytesFromFile(String s) throws IOException {
        byte[] bytes = FileUtils.readFileToByteArray(new File(s));
        return bytes;
    }

    private static byte[] getImageBytesFromUrl(String url) throws IOException, URISyntaxException {
        byte[] bytes = IOUtils.toByteArray(new URI(url));
        return bytes;
    }

    @Data
    public static class ShareItemParam {

        String imgKey;

        byte[] code;

        String title;

        String nick;

        String userImg;
    }
}
