package com.chl.victory.task;

import com.chl.victory.webcommon.manager.RpcManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author ChenHailong
 * @date 2020/8/4 9:13
 **/
@Component
@Slf4j
public class ShortUrlTask {

    /**
     * 清除段字符串与数据模型关系，数据留存周期30天。
     */
    @Scheduled(cron = "0 0 1 1 * *")
    public void cleanExpired() {
        RpcManager.shareFacade.cleanExpired(30);
    }
}
