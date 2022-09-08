package com.chl.victory.web.controller.wm.weixin.thirdplatform;

import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.AuditResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.CommitDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.ReleaseResult;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.code.SubmitAuditDTO;
import com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.tester.GetTesterResult;
import com.chl.victory.web.aspect.IgnoreExperience;
import com.chl.victory.web.model.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.chl.victory.localservice.manager.LocalServiceManager.foundryMiniProgramLocalService;
import static com.chl.victory.webcommon.manager.RpcManager.foundryMiniProgram4CodeFacade;
import static com.chl.victory.webcommon.manager.RpcManager.foundryMiniProgram4TesterFacade;

/**
 * 小程序信息
 * @author ChenHailong
 * @date 2020/5/28 14:02
 **/
@Controller
@RequestMapping("/p/wm/weixin/thirdplatform/mini/code/")
public class MiniProgrameCodeController {

    /**
     * 体验码
     * @return
     * @throws com.chl.victory.serviceapi.exception.BusServiceException
     */
    @GetMapping(path = "testCode")
    @ResponseBody
    public byte[] testCode() throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ServiceResult<byte[]> qrCode4Test = foundryMiniProgram4CodeFacade
                .getQRCode4Test(sessionCache.getShopId(), sessionCache.getAppId());

        return qrCode4Test.getData();
    }

    /**
     * 当前发布的代码记录
     * @return
     * @throws BusServiceException
     */
    @GetMapping(path = "online", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result online() throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ReleaseResult online = null;
        String cache = foundryMiniProgramLocalService.getReleaseCache(sessionCache.getShopId(), sessionCache.getAppId());
        if (StringUtils.isNotBlank(cache)) {
            online = JSONObject.parseObject(cache, ReleaseResult.class);
        }

        return Result.SUCCESS(online);
    }

    /**
     * 当前审核代码记录
     * @return
     * @throws BusServiceException
     */
    @GetMapping(path = "audit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result audit() throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        AuditResult auditResult = null;
        String cache = foundryMiniProgramLocalService.getAuditCache(sessionCache.getShopId(), sessionCache.getAppId());
        if (StringUtils.isNotBlank(cache)) {
            auditResult = JSONObject.parseObject(cache, AuditResult.class);
            /*if (auditResult.getStatus() == 2){
                ServiceResult<AuditResult> lastAudit = foundryMiniProgram4CodeFacade
                        .getLastAudit(sessionCache.getShopId(), sessionCache.getAppId());
                if (lastAudit.getSuccess() && lastAudit.getData() != null && !lastAudit.getData().getStatus().equals(auditResult.getStatus())) {
                    auditResult.setStatus(lastAudit.getData().getStatus());
                    auditResult.setReason(lastAudit.getData().getReason());
                }
            }*/
        }

        return Result.SUCCESS(auditResult);
    }

    /**
     * 当前上传代码记录
     * @return
     * @throws BusServiceException
     */
    @GetMapping(path = "test", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result test() throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        CommitDTO commitDTO = null;
        String cache = foundryMiniProgramLocalService.getCommitCache(sessionCache.getShopId(), sessionCache.getAppId());
        if (StringUtils.isNotBlank(cache)) {
            commitDTO = JSONObject.parseObject(cache, CommitDTO.class);
        }

        if (null != commitDTO) {
            commitDTO.setExt_json(null);
        }

        return Result.SUCCESS(commitDTO);
    }

    /**
     * 体验者s
     * @return
     * @throws BusServiceException
     */
    @GetMapping(path = "testers", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result testers() throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ServiceResult<GetTesterResult> memberauth = foundryMiniProgram4TesterFacade
                .memberauth(sessionCache.getShopId(), sessionCache.getAppId());
        if (!memberauth.getSuccess()) {
            return Result.FAIL(memberauth.getMsg());
        }

        return Result.SUCCESS(memberauth.getData());
    }

    @PostMapping(path = "delTest", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result delTest() throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        foundryMiniProgramLocalService.delCommitCache(sessionCache.getShopId(), sessionCache.getAppId());

        return Result.SUCCESS();
    }

    @PostMapping(path = "unbindTester", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result unbindTester(@RequestParam("userId") String userId) throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        foundryMiniProgram4TesterFacade.unbindTester(sessionCache.getShopId(), sessionCache.getAppId(), userId);

        return Result.SUCCESS();
    }

    @PostMapping(path = "bindTester", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result bindTester(@RequestParam("wechatid") String wechatid) throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ServiceResult<GetTesterResult.Tester> testerServiceResult = foundryMiniProgram4TesterFacade
                .bindTester(sessionCache.getShopId(), sessionCache.getAppId(), wechatid);
        if (!testerServiceResult.getSuccess()) {
            return Result.FAIL(testerServiceResult.getMsg());
        }

        return Result.SUCCESS(testerServiceResult.getData());
    }

    /**
     * 提交审核
     * @return
     * @throws BusServiceException
     */
    @PostMapping(path = "commitAudit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result commitAudit() throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ServiceResult<AuditResult> auditResult;
        CommitDTO commitCache = null;
        String cache = foundryMiniProgramLocalService
                .getCommitCache(sessionCache.getShopId(), sessionCache.getAppId());
        if (null == cache) {
            return Result.FAIL("请上传代码");
        }
        commitCache = JSONObject.parseObject(cache, CommitDTO.class);

        @NotNull SubmitAuditDTO submitAuditDTO = new SubmitAuditDTO();
        submitAuditDTO.setVersion_desc(commitCache.getUser_version() + "|" + commitCache.getUser_desc());
        auditResult = foundryMiniProgram4CodeFacade
                .submitAudit(sessionCache.getShopId(), sessionCache.getAppId(), submitAuditDTO);
        if (!auditResult.getSuccess()) {
            return Result.FAIL(auditResult.getMsg());
        }

        return Result.SUCCESS(auditResult.getData());
    }

    /**
     * 提交发布
     * @return
     * @throws BusServiceException
     */
    @PostMapping(path = "commitRelease", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result commitRelease() throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ServiceResult<ReleaseResult> release;
        String cache = foundryMiniProgramLocalService
                .getAuditCache(sessionCache.getShopId(), sessionCache.getAppId());
        if (null == cache) {
            return Result.FAIL("请上传代码或提交审核");
        }

        release = foundryMiniProgram4CodeFacade.release(sessionCache.getShopId(), sessionCache.getAppId());
        if (!release.getSuccess()) {
            return Result.FAIL(release.getMsg());
        }

        return Result.SUCCESS(release.getData());
    }

    /**
     * 回滚
     * @return
     * @throws BusServiceException
     */
    @PostMapping(path = "revert", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result revert() throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        foundryMiniProgram4CodeFacade.revertcoderelease(sessionCache.getShopId(), sessionCache.getAppId());

        return Result.SUCCESS();
    }

    @PostMapping(path = "unAudit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result unAudit() throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        String cache = foundryMiniProgramLocalService
                .getAuditCache(sessionCache.getShopId(), sessionCache.getAppId());
        if (null == cache) {
            return Result.FAIL("没有找到审核版本");
        }

        ServiceResult undocodeaudit = foundryMiniProgram4CodeFacade
                .undocodeaudit(sessionCache.getShopId(), sessionCache.getAppId());
        if (undocodeaudit.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(undocodeaudit.getMsg());
    }

    /**
     * 上传代码
     */
    @PostMapping(path = "commitTest", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result commitTest(CommitDTO commitDTO) throws Exception {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ServiceResult commit = foundryMiniProgram4CodeFacade
                .commit(sessionCache.getShopId(), sessionCache.getAppId(), commitDTO);
        if (!commit.getSuccess()) {
            return Result.FAIL(commit.getMsg());
        }
        return test();
    }
}
