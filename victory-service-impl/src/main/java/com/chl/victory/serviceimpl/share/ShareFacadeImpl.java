package com.chl.victory.serviceimpl.share;

import java.util.List;

import com.chl.victory.common.util.ExceptionUtil;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.share.ShareFacade;
import com.chl.victory.serviceapi.share.model.ShareDTO;
import com.chl.victory.serviceapi.share.model.ShareRecentDTO;
import com.chl.victory.serviceapi.shorturl.model.ShortUrlDTO;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.shareService;
import static com.chl.victory.service.services.ServiceManager.shortUrlService;

/**
 * @author ChenHailong
 * @date 2020/9/2 17:25
 **/
@DubboService
public class ShareFacadeImpl implements ShareFacade {

    @Override
    public ServiceResult<ShareDTO> shareItem(ShareDTO shareDTO) {
        try {
            return shareService.shareItem(shareDTO);
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }

    @Override
    public ShortUrlDTO get(String scene) {
        return shortUrlService.get(scene);
    }

    @Override
    public ServiceResult<String> shareRedirect(Long userId, Long shopId, String scene) {
        try {
            return ServiceResult.success(shareService.shareRedirect(userId, shopId, scene));
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }

    @Override
    public void visitShare(Long userId, Long shopId, String scene) {
        try {
            shareService.visitShare(userId, shopId, scene);
        } catch (Exception e) {
        }
    }

    @Override
    public void cleanExpired(int days) {
        shortUrlService.cleanExpired(days);
    }

    @Override
    public ServiceResult<List<ShareRecentDTO>> getShareRecents(Long shopId, Long userId) {
        try {
            return ServiceResult.success(shareService.getShareRecents(userId, shopId));
        } catch (Exception e) {
            return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, ExceptionUtil.trimExMsg(e));
        }
    }
}
