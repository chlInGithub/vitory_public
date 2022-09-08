package com.chl.victory.web.controller.wm.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.redis.CacheExpire;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import com.chl.victory.localservice.manager.LocalServiceManager;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.serviceapi.item.model.ItemDTO;
import com.chl.victory.serviceapi.item.query.ItemQueryDTO;
import com.chl.victory.serviceapi.member.model.ShopMemberDTO;
import com.chl.victory.serviceapi.member.query.ShopMemberQueryDTO;
import com.chl.victory.serviceapi.order.model.OrderDTO;
import com.chl.victory.serviceapi.order.query.OrderQueryDTO;
import com.chl.victory.web.model.Result;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.chl.victory.webcommon.manager.RpcManager.itemFacade;
import static com.chl.victory.webcommon.manager.RpcManager.memberFacade;
import static com.chl.victory.webcommon.manager.RpcManager.orderFacade;

/**
 * @author hailongchen9
 */
@Controller
@RequestMapping("/p/wm/dashboard/")
public class DashboardController {

    @Resource
    CacheService cacheService;

    /**
     * 销售额 订单 统计汇总
     * @return
     */
    @GetMapping(path = "saleOrderSummary", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result saleOrderSummary() {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        String saleAndOrderSummaryDTO = orderFacade.getSaleAndOrderSummary(sessionCache.getShopId());
        return Result.SUCCESS(saleAndOrderSummaryDTO);
    }

    /**
     * 会员 统计汇总
     * @return
     */
    @GetMapping(path = "memSummary", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result memSummary() {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        String memSummaryDTO = memberFacade.getMemSummary(sessionCache.getShopId());
        return Result.SUCCESS(memSummaryDTO);
    }

    /**
     * 商品 统计汇总
     * @return
     */
    @GetMapping(path = "itemSummary", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result itemSummary() {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        String itemSummaryDTO = itemFacade.getItemSummary(sessionCache.getShopId());
        return Result.SUCCESS(itemSummaryDTO);
    }

    /**
     * 热销商品top
     * @return
     */
    @GetMapping(path = "hotItems", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result hotItems() {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        String key = CacheKeyPrefix.DASHBOARD_HOT_ITEMS_OF_SHOP;
        String cache = cacheService.hGet(key, sessionCache.getShopId().toString(), String.class);

        if (null == cache) {
            // todo 放到一个接口中
            List<Long> hotItemIds = LocalServiceManager.itemLocalService.getHotItemIds(sessionCache.getShopId());
            ItemQueryDTO itemQuery = new ItemQueryDTO();
            itemQuery.setShopId(sessionCache.getShopId());
            itemQuery.setIds(hotItemIds);
            itemQuery.setJustOutline(true);
            ServiceResult<List<ItemDTO>> selectItems = itemFacade.selectItems(itemQuery);
            List<ItemVO> vos = null;
            if (selectItems.getSuccess() && !CollectionUtils.isEmpty(selectItems.getData())) {
                vos = selectItems.getData().stream().map(item -> {
                    ItemVO vo = new ItemVO();
                    vo.setId(item.getId());
                    vo.setTitle(item.getTitle());
                    vo.setPrice(item.getPrice());
                    vo.setSales(item.getSales());
                    return vo;
                }).sorted((o1, o2) -> -(o1.getSales().compareTo(o2.getSales()))).collect(Collectors.toList());
            }

            if (!CollectionUtils.isEmpty(vos)) {
                cache = JSONObject.toJSONString(vos);
                cacheService.hSet(key, sessionCache.getShopId(), cache, CacheExpire.DASHBOARD_EXPIRE);
            }
        }
        return Result.SUCCESS(cache);
    }

    /**
     * 最新订单top
     * @return
     */
    @GetMapping(path = "orders", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result orders() {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        String key = CacheKeyPrefix.DASHBOARD_NEW_ORDERS_OF_SHOP;
        String cache = cacheService.hGet(key, sessionCache.getShopId().toString(), String.class);

        if (null == cache) {
            OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
            orderServiceQuery.setShopId(sessionCache.getShopId());
            orderServiceQuery.setPageIndex(0);
            orderServiceQuery.setPageSize(5);
            orderServiceQuery.setOrderColumn("created_time");
            orderServiceQuery.setDesc(true);
            orderServiceQuery.setNeedBuyerInfo(true);
            ServiceResult<List<OrderDTO>> selectMains = orderFacade.selectMains(orderServiceQuery);

            List<OrderVO> vos = null;
            if (selectMains.getSuccess() && !CollectionUtils.isEmpty(selectMains.getData())) {
                vos = selectMains.getData().stream().map(item -> {
                    OrderVO orderVO = new OrderVO();
                    orderVO.setId(item.getId());
                    orderVO.setNick(item.getBuyer().getNick());
                    orderVO.setMobile(item.getBuyer().getMobile());
                    orderVO.setRealFee(item.getRealFee());
                    orderVO.setStatus(item.getStatus().getDesc());
                    return orderVO;
                }).collect(Collectors.toList());
            }

            if (!CollectionUtils.isEmpty(vos)) {
                cache = JSONObject.toJSONString(vos);
                cacheService.hSet(key, sessionCache.getShopId(), cache, CacheExpire.DASHBOARD_EXPIRE);
            }
        }

        return Result.SUCCESS(cache);
    }

    /**
     * 新用户top
     * @return
     */
    @GetMapping(path = "newMems", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result newMems() {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        String key = CacheKeyPrefix.DASHBOARD_NEW_MEMS_OF_SHOP;
        String cache = cacheService.hGet(key, sessionCache.getShopId().toString(), String.class);

        if (null == cache) {
            ShopMemberQueryDTO shopMemberQuery = new ShopMemberQueryDTO();
            shopMemberQuery.setShopId(sessionCache.getShopId());
            shopMemberQuery.setPageIndex(0);
            shopMemberQuery.setPageSize(5);
            shopMemberQuery.setOrderColumn("created_time");
            shopMemberQuery.setDesc(true);
            shopMemberQuery.setHasMobile(true);
            ServiceResult<List<ShopMemberDTO>> selectMem = memberFacade.selectMem(shopMemberQuery);

            List<MemVO> vos = null;
            if (selectMem.getSuccess() && !CollectionUtils.isEmpty(selectMem.getData())) {
                vos = selectMem.getData().stream().map(item -> {
                    MemVO vo = new MemVO();
                    vo.setNick(item.getNick());
                    vo.setMobile(item.getMobile());
                    return vo;
                }).collect(Collectors.toList());
            }

            if (!CollectionUtils.isEmpty(vos)) {
                cache = JSONObject.toJSONString(vos);
                cacheService.hSet(key, sessionCache.getShopId(), cache, CacheExpire.DASHBOARD_EXPIRE);
            }
        }

        return Result.SUCCESS(cache);
    }

    @Data
    public static class ItemVO {

        private Long id;

        private String title;

        private Integer sales;

        private BigDecimal price;
    }

    @Data
    public static class OrderVO {

        Long id;

        String nick;

        Long mobile;

        String status;

        BigDecimal realFee;
    }

    @Data
    public static class MemVO {

        String nick;

        Long mobile;
    }
}
