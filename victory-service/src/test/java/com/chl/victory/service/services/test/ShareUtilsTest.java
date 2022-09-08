package com.chl.victory.service.services.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import com.chl.victory.service.services.share.ShareUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * @author ChenHailong
 * @date 2020/10/29 10:18
 **/
public class ShareUtilsTest {
    @Test
    public void test() throws Exception {
        ShareUtils.ShareItemParam shareItemParam = new ShareUtils.ShareItemParam();
        shareItemParam.setCode(IOUtils.toByteArray(new FileInputStream(new File("D:\\qrcode\\share.jpg"))));
        shareItemParam.setImgKey("e3802774584055a620981a1cd8ec9b2d");
        shareItemParam.setUserImg("https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83eoHhjCfjpicq5Aynkhqcsr84GVtMSxB5AePA5JBnWqSyRUzZ6T5BMEVxJx68WqAtfN1HHFbKnibLD0A/132");
        byte[] shareImg4Item = ShareUtils.getShareImg4Item(shareItemParam);

        IOUtils.write(shareImg4Item, new FileOutputStream(new File("D:\\qrcode\\test.jpg")));
    }
    @Test
    public void test1() throws Exception {
        ShareUtils.ShareItemParam shareItemParam = new ShareUtils.ShareItemParam();
        shareItemParam.setCode(IOUtils.toByteArray(new FileInputStream(new File("D:\\qrcode\\share.jpg"))));
        shareItemParam.setUserImg("https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83eoHhjCfjpicq5Aynkhqcsr84GVtMSxB5AePA5JBnWqSyRUzZ6T5BMEVxJx68WqAtfN1HHFbKnibLD0A/132");
        byte[] shareImg4Item = ShareUtils.getShareImg4Shop(shareItemParam);

        IOUtils.write(shareImg4Item, new FileOutputStream(new File("D:\\qrcode\\testShop.jpg")));
    }
}
