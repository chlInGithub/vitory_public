package com.chl.victory.task;

import javax.annotation.PostConstruct;
import com.chl.victory.webcommon.manager.RpcManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author ChenHailong
 * @date 2020/1/8 9:34
 **/
@Component
public class WeixinTask {

    @PostConstruct
    public void postConstructTest() {}

    /**
     * 1小时30分执行一次
     */
    @Scheduled(initialDelay = 0, fixedDelay = 90 * 60 * 1000)
    public void refreshToken() {
        RpcManager.miniProgramFacade.refreshAccessToken4Shops(null);
    }


    @Scheduled(cron = "0 0 0/1 * * *")
    public void checkAuditResult() {
        RpcManager.foundryMiniProgram4CodeFacade.checkAuditResult();
    }


    @Scheduled(cron = "0 0 0/1 * * *")
    public void checkNickNameAuditResult() {
        RpcManager.foundryMiniProgram4BasicInfoFacade.checkNickNameAuditResult();
    }


}
