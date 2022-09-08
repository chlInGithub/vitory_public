package com.chl.victory.web.controller.wm.ueditor;

import com.alibaba.fastjson.JSONObject;
import com.baidu.ueditor.ActionEnter;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 *
 */
//@Controller
public class UeditorController{

    @Autowired
    HttpServletRequest request;
    @Autowired
    WebApplicationContext webApplicationContext;
    /**
     * 获取ueditor配置文件
     */
    //@GetMapping(path = "/uditor/config")
    //@ResponseBody
    public Object config(@RequestParam(name = "action", required = false) String action){
        /*try {
            System.out.println(webApplicationContext.getResource("classpath:static").getFile().getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(webApplicationContext.getServletContext().getResourcePaths("/"));
        System.out.println(webApplicationContext.getServletContext().getRealPath("/static/wm/r/ueditor/jsp"));
        //String realPath =  webApplicationContext.getServletContext().getRealPath("/");
        String realPath =  "D:/work/gitRep/victory/web/target/classes";
        ActionEnter actionEnter = new ActionEnter(request, realPath, "/static/wm/r/ueditor/jsp/config.json");
        String config  = actionEnter.exec();
        //config = "{\"videoMaxSize\":102400000,\"videoActionName\":\"uploadvideo\",\"fileActionName\":\"uploadfile\",\"fileManagerListPath\":\"/ueditor/jsp/upload/file/\",\"imageCompressBorder\":1600,\"imageManagerAllowFiles\":[\".png\",\".jpg\",\".jpeg\",\".gif\",\".bmp\"],\"imageManagerListPath\":\"/ueditor/jsp/upload/image/\",\"fileMaxSize\":51200000,\"fileManagerAllowFiles\":[\".png\",\".jpg\",\".jpeg\",\".gif\",\".bmp\",\".flv\",\".swf\",\".mkv\",\".avi\",\".rm\",\".rmvb\",\".mpeg\",\".mpg\",\".ogg\",\".ogv\",\".mov\",\".wmv\",\".mp4\",\".webm\",\".mp3\",\".wav\",\".mid\",\".rar\",\".zip\",\".tar\",\".gz\",\".7z\",\".bz2\",\".cab\",\".iso\",\".doc\",\".docx\",\".xls\",\".xlsx\",\".ppt\",\".pptx\",\".pdf\",\".txt\",\".md\",\".xml\"],\"fileManagerActionName\":\"listfile\",\"snapscreenInsertAlign\":\"none\",\"scrawlActionName\":\"uploadscrawl\",\"videoFieldName\":\"upfile\",\"imageCompressEnable\":true,\"videoUrlPrefix\":\"\",\"fileManagerUrlPrefix\":\"\",\"catcherAllowFiles\":[\".png\",\".jpg\",\".jpeg\",\".gif\",\".bmp\"],\"imageManagerActionName\":\"listimage\",\"snapscreenPathFormat\":\"/ueditor/jsp/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}\",\"scrawlPathFormat\":\"/ueditor/jsp/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}\",\"scrawlMaxSize\":2048000,\"imageInsertAlign\":\"none\",\"catcherPathFormat\":\"/ueditor/jsp/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}\",\"catcherMaxSize\":2048000,\"snapscreenUrlPrefix\":\"\",\"imagePathFormat\":\"/ueditor/jsp/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}\",\"imageManagerUrlPrefix\":\"\",\"scrawlUrlPrefix\":\"\",\"scrawlFieldName\":\"upfile\",\"imageMaxSize\":2048000,\"imageAllowFiles\":[\".png\",\".jpg\",\".jpeg\",\".gif\",\".bmp\"],\"snapscreenActionName\":\"uploadimage\",\"catcherActionName\":\"catchimage\",\"fileFieldName\":\"upfile\",\"fileUrlPrefix\":\"\",\"imageManagerInsertAlign\":\"none\",\"catcherLocalDomain\":[\"127.0.0.1\",\"localhost\",\"img.baidu.com\"],\"filePathFormat\":\"/ueditor/jsp/upload/file/{yyyy}{mm}{dd}/{time}{rand:6}\",\"videoPathFormat\":\"/ueditor/jsp/upload/video/{yyyy}{mm}{dd}/{time}{rand:6}\",\"fileManagerListSize\":20,\"imageActionName\":\"uploadimage\",\"imageFieldName\":\"upfile\",\"imageUrlPrefix\":\"\",\"scrawlInsertAlign\":\"none\",\"fileAllowFiles\":[\".png\",\".jpg\",\".jpeg\",\".gif\",\".bmp\",\".flv\",\".swf\",\".mkv\",\".avi\",\".rm\",\".rmvb\",\".mpeg\",\".mpg\",\".ogg\",\".ogv\",\".mov\",\".wmv\",\".mp4\",\".webm\",\".mp3\",\".wav\",\".mid\",\".rar\",\".zip\",\".tar\",\".gz\",\".7z\",\".bz2\",\".cab\",\".iso\",\".doc\",\".docx\",\".xls\",\".xlsx\",\".ppt\",\".pptx\",\".pdf\",\".txt\",\".md\",\".xml\"],\"catcherUrlPrefix\":\"\",\"imageManagerListSize\":20,\"catcherFieldName\":\"source\",\"videoAllowFiles\":[\".flv\",\".swf\",\".mkv\",\".avi\",\".rm\",\".rmvb\",\".mpeg\",\".mpg\",\".ogg\",\".ogv\",\".mov\",\".wmv\",\".mp4\",\".webm\",\".mp3\",\".wav\",\".mid\"]}";
        JSONObject jsonObject = JSONObject.parseObject(config);
        config = JSONObject.toJSONString(jsonObject);
        return jsonObject;*/
        return null;
    }
}
