package com.chl.victory.service.services.member;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.redis.CacheExpire;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.member.MemberManager;
import com.chl.victory.dao.manager.order.OrderManager;
import com.chl.victory.dao.model.member.MemberDeliverDO;
import com.chl.victory.dao.model.member.ShopMemberDO;
import com.chl.victory.dao.query.member.MemberDeliverQuery;
import com.chl.victory.dao.query.member.ShopMemberQuery;
import com.chl.victory.dao.query.order.OrderQuery;
import com.chl.victory.service.services.BaseService;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.exception.NotExistException;
import com.chl.victory.serviceapi.member.model.MemSummaryDTO;
import com.chl.victory.serviceapi.member.model.MemberDeliverDTO;
import com.chl.victory.serviceapi.member.model.ShopMemberDTO;
import com.chl.victory.serviceapi.member.query.MemberDeliverQueryDTO;
import com.chl.victory.serviceapi.member.query.ShopMemberQueryDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 会员业务服务接口
 * @author ChenHailong
 * @date 2019/5/9 11:05
 **/
@Service
@Slf4j
public class MemberService extends BaseService {

    @Resource
    MemberManager memberManager;

    @Resource
    OrderManager orderManager;

    @Resource
    CacheService cacheService;

    public ServiceResult saveMem(ShopMemberDTO shopMemberDTO) {
        ShopMemberDO shopMemberDO = toDO(shopMemberDTO, ShopMemberDO.class);
        try {
            memberManager.saveMember(shopMemberDO);
            return ServiceResult.success();
        } catch (DaoManagerException e) {
            log.error("saveMem|{}", shopMemberDO, e);
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public int saveMem(ShopMemberDO shopMemberDO) throws DaoManagerException {
        return memberManager.saveMember(shopMemberDO);
    }

    public ServiceResult<List<ShopMemberDTO>> selectMem(ShopMemberQueryDTO queryDTO) {
        try {
            ShopMemberQuery query = toQuery(queryDTO, ShopMemberQuery.class);
            List<ShopMemberDO> shopMemberDOS = memberManager.selectMembers(query);
            List<ShopMemberDTO> shopMemberDTOS = toDTOs(shopMemberDOS, ShopMemberDTO.class);
            return ServiceResult.success(shopMemberDTOS);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public List<ShopMemberDO> selectMem(ShopMemberQuery query) throws DaoManagerException {
            List<ShopMemberDO> shopMemberDOS = memberManager.selectMembers(query);
            return shopMemberDOS;
    }

    public ShopMemberDO assertMem(ShopMemberQuery query) throws BusServiceException {
        try {
            ShopMemberDO shopMemberDO = memberManager.selectMember(query);
            if (null == shopMemberDO) {
                throw new NotExistException("会员不存在");
            }
            return shopMemberDO;
        } catch (DaoManagerException e) {
            throw new BusServiceException(e);
        }
    }

    public ServiceResult saveMemDeliver(MemberDeliverDTO deliverDTO) {
        try {
            MemberDeliverDO deliverDO = toDO(deliverDTO, MemberDeliverDO.class);
            memberManager.saveDeliver(deliverDO);
            return ServiceResult.success(deliverDO.getId());
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult countMem(ShopMemberQuery query) {
        try {
            Integer count = memberManager.countMember(query);
            return ServiceResult.success(count);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult<List<MemberDeliverDTO>> selectMemDeliver(MemberDeliverQueryDTO queryDTO) {
        try {
            MemberDeliverQuery query = toQuery(queryDTO, MemberDeliverQuery.class);
            List<MemberDeliverDO> memberDeliverDOS = memberManager.selectDelivers(query);
            List<MemberDeliverDTO> memberDeliverDTOS = toDTOs(memberDeliverDOS, MemberDeliverDTO.class);
            return ServiceResult.success(memberDeliverDTOS);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }

    public ServiceResult delMemDeliver(List<Long> ids, Long memId) {
        MemberDeliverQuery deliverQuery = new MemberDeliverQuery();
        deliverQuery.setMemId(memId);
        for (Long id : ids) {
            deliverQuery.setId(id);
            try {
                memberManager.delDeliver(deliverQuery);
            } catch (DaoManagerException e) {
            }
        }
        return ServiceResult.success();
    }

    public String getMemSummary(@NotNull Long shopId) {
        String key = CacheKeyPrefix.DASHBOARD_MEM_SUMMARY_OF_SHOP;
        String cache = cacheService.hGet(key, shopId.toString(), String.class);

        if (null == cache) {
            try {
                ShopMemberQuery shopMemberQuery = new ShopMemberQuery();
                shopMemberQuery.setShopId(shopId);
                shopMemberQuery.setHasMobile(true);
                int total = memberManager.countMember(shopMemberQuery);

                Date firstDayOfCurrentMonth = DateUtils.truncate(new Date(), Calendar.MONTH);
                shopMemberQuery.setStartedCreatedTime(firstDayOfCurrentMonth);
                int currentMonthNewTotal = memberManager.countMember(shopMemberQuery);

                OrderQuery orderQuery = new OrderQuery();
                orderQuery.setShopId(shopId);
                orderQuery.setStartedCreatedTime(firstDayOfCurrentMonth);
                Integer currentMonthShoppingTotal = orderManager.countMem(orderQuery);

                MemSummaryDTO memSummaryDTO = new MemSummaryDTO();
                memSummaryDTO.setTotal(total);
                memSummaryDTO.setCurrentMonthShoppingTotal(currentMonthShoppingTotal);
                memSummaryDTO.setCurrentMonthNewTotal(currentMonthNewTotal);
                cache = JSONObject.toJSONString(memSummaryDTO);
            } catch (Exception e) {
                log.error("getMemSummary{}", shopId, e);
            }

            if (cache != null) {
                cacheService.hSet(key, shopId, cache, CacheExpire.DASHBOARD_EXPIRE);
            }
        }

        return cache;
    }

    public ServiceResult saveNickAndImg(@NotNull ShopMemberDTO shopMember){
        if (StringUtils.isEmpty(shopMember.getNick()) || StringUtils.isEmpty(shopMember.getAvatarUrl())) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK);
        }

        ShopMemberQuery shopMemberQuery = new ShopMemberQuery();
        shopMemberQuery.setShopId(shopMember.getShopId());
        shopMemberQuery.setThirdId(shopMember.getThirdId());
        shopMemberQuery.setId(shopMember.getId());
        List<ShopMemberDO> listServiceResult = null;
        try {
            listServiceResult = selectMem(shopMemberQuery);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }

        if (CollectionUtils.isEmpty(listServiceResult)) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "无匹配用户");
        }

        ShopMemberDO shopMemberDO = listServiceResult.get(0);

        if (StringUtils.isEmpty(shopMemberDO.getNick()) || !shopMember.getNick().equals(shopMemberDO.getNick()) || StringUtils
                .isEmpty(shopMemberDO.getAvatarUrl()) || !shopMember.getAvatarUrl().equals(shopMemberDO.getAvatarUrl())) {
            shopMemberDO.setNick(shopMember.getNick());
            shopMemberDO.setAvatarUrl(shopMember.getAvatarUrl());
            int saveResult;
            try {
                saveResult = saveMem(shopMemberDO);
            } catch (DaoManagerException e) {
                return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
            }
        }

        return ServiceResult.success();
    }

    /**
     * 用户记录是否最近 x 分钟 内创建
     * @param shopId
     * @param userId
     * @param xMin 正数
     * @return
     */
    public boolean newLastXMin(Long shopId, Long userId, Integer xMin) {
        ShopMemberQuery shopMemberQuery = new ShopMemberQuery();
        shopMemberQuery.setShopId(shopId);
        shopMemberQuery.setId(userId);
        shopMemberQuery.setStartedCreatedTime(DateUtils.addMinutes(new Date(), -xMin));
        try {
            Integer count = memberManager.countMember(shopMemberQuery);
            return count > 0;
        } catch (DaoManagerException e) {
            return false;
        }
    }
}
