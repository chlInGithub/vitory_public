package com.chl.victory.imgservice;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.util.HttpClientUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Service
@Slf4j
@ConfigurationProperties("zimg")
public class ZimgService {
    private static final String uploadPath = "/upload";
    private static final String deletePath = "/admin";
    private String server;


    public String uploadImage(MultipartFile multipartFile, String mark) throws Exception {


        String url = server + uploadPath;
        String s = HttpClientUtil.postMultipartFileToImage(url, multipartFile);
        ZimgResult zimgResult = JSONObject.parseObject(s, ZimgResult.class);
        if (zimgResult.isRet()) {
            return zimgResult.getInfo().getMd5();
        }
        return "";
    }

    public String uploadImage(MultipartFile multipartFile) throws Exception {
        String url = server + uploadPath;
        String s = HttpClientUtil.postMultipartFileToImage(url, multipartFile);
        ZimgResult zimgResult = JSONObject.parseObject(s, ZimgResult.class);
        if (zimgResult.isRet()) {
            return zimgResult.getInfo().getMd5();
        }
        return "";
    }

    public List<String> uploadImage(List<MultipartFile> multipartFiles) throws Exception {
        List<String> list = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            list.add(uploadImage(multipartFile));
        }
        return list;
    }

    public String uploadImage(File file) throws ZimgUploadException {
        String url = server + uploadPath;
        try {
            String s = HttpClientUtil.postFileToImage(url, file);
            ZimgResult zimgResult = JSONObject.parseObject(s, ZimgResult.class);
            if (zimgResult.isRet()) {
                return zimgResult.getInfo().getMd5();
            }
        } catch (Exception e) {
            throw new ZimgUploadException(e);
        }
        return null;
    }

    public String uploadImage(byte[] imgBytes) throws ZimgUploadException {
        String url = server + uploadPath;
        try {
            String s = HttpClientUtil.postFileToImage(url, imgBytes);
            ZimgResult zimgResult = JSONObject.parseObject(s, ZimgResult.class);
            if (zimgResult.isRet()) {
                return zimgResult.getInfo().getMd5();
            }
        } catch (Exception e) {
            throw new ZimgUploadException(e);
        }
        return null;
    }

    /**
     * 需要服务器开启远程修改权限 按自己需要修改 admin_rule='allow 127.0.0.1' 这一项
     */
    public boolean deleteImage(String md5) throws Exception {
        String url = server + deletePath;
        Map<String, String> params = new HashMap<>(2);
        params.put("md5", md5);
        params.put("t", "1");
        String s = HttpClientUtil.doGet(url, params, null);
        if (StringUtils.isEmpty(s)) {
            return false;
        }
        // 图片不存在，认为删除成功。
        if (s.indexOf("Successful") != -1 || s.indexOf("Image Not Found") != -1) {
            return true;
        }

        return false;
    }

    @Data
    public static class ZimgResult {
        private boolean ret;
        private ZimgResultInfo info;
        private ZimgResultError error;
    }

    @Data
    public static class ZimgResultError {
        private int code;
        private String message;
    }

    @Data
    public static class ZimgResultInfo {
        private String md5;
        private String size;
    }
}
