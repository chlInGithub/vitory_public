package com.chl.victory.serviceapi.share;

import java.util.List;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.share.model.ShareDTO;
import com.chl.victory.serviceapi.share.model.ShareRecentDTO;
import com.chl.victory.serviceapi.shorturl.model.ShortUrlDTO;

/**
 * @author ChenHailong
 * @date 2020/8/28 10:09
 **/
public interface ShareFacade {

    ServiceResult<ShareDTO> shareItem(ShareDTO shareDTO);

    ShortUrlDTO get(String scene);

    /**
     * for webview版本，内部已包括visitShare逻辑
     * @param userId
     * @param shopId
     * @param scene
     * @return
     */
    ServiceResult<String> shareRedirect(Long userId, Long shopId, String scene);

    /**
     * 通过分享访问app后的统计行为
     * @param userId
     * @param shopId
     * @param scene
     * @return
     */
    void visitShare(Long userId, Long shopId, String scene);

    void cleanExpired(int days);

    ServiceResult<List<ShareRecentDTO>> getShareRecents(Long shopId, Long userId);
}
