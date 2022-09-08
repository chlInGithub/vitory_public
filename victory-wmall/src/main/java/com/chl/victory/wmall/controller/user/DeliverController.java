package com.chl.victory.wmall.controller.user;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

import com.chl.victory.common.redis.CacheService;
import com.chl.victory.localservice.DeliverLocalService;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.member.model.MemberDeliverDTO;
import com.chl.victory.serviceapi.member.query.MemberDeliverQueryDTO;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.wmall.model.Result;
import com.chl.victory.wmall.model.WmallSessionCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.chl.victory.webcommon.manager.RpcManager.memberFacade;

/**
 * @author chenhailong
 * @date 2019/9/23 11:04
 **/
@Slf4j
@RestController
@RequestMapping("/wmall/deliver/")
@Api(description = "为交付地址提供接口")
@Validated
public class DeliverController {

    @Resource
    CacheService cacheService;

    @Resource
    DeliverLocalService deliverLocalService;

    /**
     * 收货地址list
     * @return
     */
    @GetMapping("list")
    @ApiOperation(value = "收货地址list", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result list() {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        Long shopId = sessionCache.getShopId();

        MemberDeliverQueryDTO deliverQuery = new MemberDeliverQueryDTO();
        deliverQuery.setMemId(sessionCache.getUserId());
        deliverQuery.setShopId(shopId);
        ServiceResult<List<MemberDeliverDTO>> deliverResult = memberFacade.selectMemDeliver(deliverQuery);

        if (!deliverResult.getSuccess()) {
            return Result.FAIL(deliverResult.getMsg());
        }

        Long deliverId = deliverLocalService.getDefaultDeliverId(shopId, sessionCache.getUserId());

        List<ReceiveInfoVO> receiveInfoVOS = deliverResult.getData().stream().map(ReceiveInfoVO::transfer)
                .collect(Collectors.toList());

        if (deliverId != null) {
            receiveInfoVOS.stream().filter(item -> item.getId().equals(deliverId))
                    .forEach(item -> item.setDefaultSelected(true));
        }

        return Result.SUCCESS(receiveInfoVOS);
    }

    /**
     * 收货地址list
     * @return 收货地址记录ID
     */
    @PostMapping("save")
    @ApiOperation(value = "保存收货地址", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Long> save(@Validated ReceiveInfoVO receiveInfoVO) {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        MemberDeliverDTO memberDeliverDO = new MemberDeliverDTO();
        memberDeliverDO.setMemId(sessionCache.getUserId());
        memberDeliverDO.setShopId(sessionCache.getShopId());
        memberDeliverDO.setOperatorId(sessionCache.getUserId());
        memberDeliverDO.setId(receiveInfoVO.id);
        memberDeliverDO.setAddr(receiveInfoVO.getCity(), receiveInfoVO.address);
        memberDeliverDO.setName(receiveInfoVO.name);
        memberDeliverDO.setMobile(Long.valueOf(receiveInfoVO.getMobile()));
        memberDeliverDO.setCode(receiveInfoVO.getCode());
        ServiceResult<Long> deliverResult = memberFacade.saveMemDeliver(memberDeliverDO);

        if (!deliverResult.getSuccess()) {
            return Result.FAIL(deliverResult.getMsg());
        }

        if (receiveInfoVO.defaultSelected) {
            deliverLocalService
                    .setDefaultDeliverId(sessionCache.getShopId(), sessionCache.getUserId(), deliverResult.getData());
        }

        return Result.SUCCESS(deliverResult.getData());
    }

    /**
     * 收货地址list
     */
    @PostMapping("del")
    @ApiOperation(value = "保存收货地址", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result del(@NotNull @Positive Long id) {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        List<Long> ids = Arrays.asList(id);
        ServiceResult deliverResult = memberFacade.delMemDeliver(ids, sessionCache.getUserId());

        if (!deliverResult.getSuccess()) {
            return Result.FAIL(deliverResult.getMsg());
        }

        Long deliverId = deliverLocalService.getDefaultDeliverId(sessionCache.getShopId(), sessionCache.getUserId());
        if (id.equals(deliverId)) {
            deliverLocalService.delDefaultDeliverId(sessionCache.getShopId(), sessionCache.getUserId());
        }

        return Result.SUCCESS();
    }

    @Data
    public static class ReceiveInfoVO {

        private Long id;

        @NotEmpty
        private String name;

        @Pattern(regexp = "^1[3456789]\\d{9}$", message = "手机号格式错误")
        private String mobile;

        @NotEmpty
        private String city;

        @NotEmpty
        private String address;

        @NotEmpty
        private String code;

        private boolean defaultSelected;

        public static ReceiveInfoVO transfer(MemberDeliverDTO memberDeliverDO) {
            ReceiveInfoVO receiveInfoVO = new ReceiveInfoVO();
            receiveInfoVO.setId(memberDeliverDO.getId());
            receiveInfoVO.setName(memberDeliverDO.getName());
            receiveInfoVO.setMobile(memberDeliverDO.getMobile().toString());
            receiveInfoVO.setCity(memberDeliverDO.getCity());
            receiveInfoVO.setAddress(memberDeliverDO.getAddress());
            receiveInfoVO.setCode(memberDeliverDO.getCode());
            return receiveInfoVO;
        }
    }
}
