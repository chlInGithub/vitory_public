package com.chl.victory.imgservice;

import com.chl.victory.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author ChenHailong
 * @date 2019/11/19 15:12
 **/
public class ZimgServiceTest extends BaseTest {
    @Resource
    ZimgService zimgService;

    @Test
    public void uploadImg() {
        File file = new File("C:\\Users\\Administrator.20180706-164526\\Desktop\\img.png");

        String md5 = null;
        try {
            md5 = zimgService.uploadImage(file);
        } catch (ZimgUploadException e) {
            e.printStackTrace();
        }

        System.out.println(md5);
    }
}
