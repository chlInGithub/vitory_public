package com.chl.victory.web.controller.wm;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import com.chl.victory.web.model.Result;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;

/**
 *
 */
@Controller
@RequestMapping("/p")
public class MenuController {

    @Resource
    CacheService cacheService;

    /**
     * businessType 关联的submitUrl
     */
    @GetMapping(path = "/wm/submits", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result submits(@RequestParam(name = "businessType", required = false) String businessType){
        //TODO 从session获取sellerid

        String key = CacheKeyPrefix.SUBMIT_URL;
        String cache = cacheService.get(key, String.class);

        if (cache == null || cache.isEmpty()) {
            Map<String, String> mockSubmits = mockSubmits();
            cacheService.save(key, mockSubmits, 3600 * 24);
            cache = JSONObject.toJSONString(mockSubmits);
        }

        return Result.SUCCESS(cache);
    }

    private Map<String, String> mockSubmits() {
        Map<String, String> menuSubmitsMap = new HashMap<>();
        menuSubmitsMap.put("orderList_query", "order/statusCount");
        menuSubmitsMap.put("orderList_list", "order/list");
        menuSubmitsMap.put("orderList_orderClose", "order/close");
        menuSubmitsMap.put("orderList_orderTaken", "order/taken");
        menuSubmitsMap.put("orderList_confirmOfflinePayed", "order/confirmOfflinePayed");
        menuSubmitsMap.put("orderDetail_query", "order/detail");
        menuSubmitsMap.put("orderDetail_submitNote", "order/modifyNote");
        menuSubmitsMap.put("orderDetail_sent", "order/sent");
        menuSubmitsMap.put("orderDetail_okRefund", "order/okRefund");
        menuSubmitsMap.put("orderDetail_refuseRefund", "order/refuseRefund");

        menuSubmitsMap.put("cateList_query", "cate/list");
        menuSubmitsMap.put("cateList_cateDel", "cate/del");
        menuSubmitsMap.put("cateList_saveCate", "cate/save");
        menuSubmitsMap.put("cateList_cateShow", "cate/show");

        menuSubmitsMap.put("itemList_query", "item/spaceCount");
        menuSubmitsMap.put("itemList_list", "item/list");
        menuSubmitsMap.put("itemList_del", "item/del");
        menuSubmitsMap.put("itemList_shelf", "item/shelf");
        menuSubmitsMap.put("itemList_soldOut", "item/soldOut");
        menuSubmitsMap.put("itemDetail_query", "item/detail");
        menuSubmitsMap.put("itemEdit_query", "item/detail");
        menuSubmitsMap.put("itemEdit_save", "item/save");
        menuSubmitsMap.put("itemRangeEdit_query", "item/getIdsOfType");
        menuSubmitsMap.put("itemRangeEdit_save", "item/saveIdsOfType");

        menuSubmitsMap.put("activityList_list", "activity/list");
        menuSubmitsMap.put("activityList_del", "activity/del");
        menuSubmitsMap.put("activityList_valid", "activity/valid");
        menuSubmitsMap.put("activityList_invalid", "activity/invalid");
        menuSubmitsMap.put("activityDetail_query", "activity/detail");
        menuSubmitsMap.put("activityEdit_query", "activity/detail");
        menuSubmitsMap.put("activityEdit_save", "activity/save");

        menuSubmitsMap.put("saleStrategyList_list", "salestrategy/list");
        menuSubmitsMap.put("saleStrategyList_del", "salestrategy/del");
        menuSubmitsMap.put("presellDetail_query", "salestrategy/detail");
        menuSubmitsMap.put("presellEdit_query", "salestrategy/detail");
        menuSubmitsMap.put("presellEdit_save", "salestrategy/savePresell");
        menuSubmitsMap.put("minFeeDetail_query", "salestrategy/detail");
        menuSubmitsMap.put("minFeeEdit_query", "salestrategy/detail");
        menuSubmitsMap.put("minFeeEdit_save", "salestrategy/saveMinFee");
        menuSubmitsMap.put("minCountDetail_query", "salestrategy/detail");
        menuSubmitsMap.put("minCountEdit_query", "salestrategy/detail");
        menuSubmitsMap.put("minCountEdit_save", "salestrategy/saveMinCount");
        menuSubmitsMap.put("maxCountDetail_query", "salestrategy/detail");
        menuSubmitsMap.put("maxCountEdit_query", "salestrategy/detail");
        menuSubmitsMap.put("maxCountEdit_save", "salestrategy/saveMaxCount");

        menuSubmitsMap.put("couponList_list", "coupon/list");
        menuSubmitsMap.put("couponList_del", "coupon/del");
        menuSubmitsMap.put("couponList_valid", "coupon/valid");
        menuSubmitsMap.put("couponList_invalid", "coupon/invalid");
        menuSubmitsMap.put("couponDetail_query", "coupon/detail");
        menuSubmitsMap.put("couponEdit_query", "coupon/detail");
        menuSubmitsMap.put("couponEdit_save", "coupon/save");

        menuSubmitsMap.put("shopOverview_query", "shop/query");
        menuSubmitsMap.put("shopSetting_save", "shop/save");
        menuSubmitsMap.put("shopSetting_query", "shop/query");
        menuSubmitsMap.put("shopSetting_saveOfFreightFree", "shop/saveFreightFree");
        menuSubmitsMap.put("shopSetting_queryFreightFree", "shop/queryFreightFree");
        menuSubmitsMap.put("shopSetting_saveOfDeliveryArea", "shop/saveDeliveryArea");
        menuSubmitsMap.put("shopSetting_queryDeliveryArea", "shop/queryDeliveryArea");
        menuSubmitsMap.put("shopSetting_saveOfPayType", "shop/savePayType");
        menuSubmitsMap.put("shopSetting_queryPayType", "shop/queryPayType");
        menuSubmitsMap.put("shopSetting_saveOfDeliveryType", "shop/saveDeliveryType");
        menuSubmitsMap.put("shopSetting_queryDeliveryType", "shop/queryDeliveryType");
        menuSubmitsMap.put("modifyPw_save", "shop/modifyPw");
        menuSubmitsMap.put("picMan_query", "img/query");
        menuSubmitsMap.put("picMan_del", "img/del");
        menuSubmitsMap.put("smsList_query", "sms/list");
        menuSubmitsMap.put("smsRechargeList_query", "sms/listCharge");
        menuSubmitsMap.put("smsRecharge_query", "sms/count");
        menuSubmitsMap.put("smsRecharge_sets", "sms/sets");
        menuSubmitsMap.put("smsRecharge_charge", "sms/charge");

        menuSubmitsMap.put("miniProgramSetting_queryAuthorizedApps", "weixin/thirdplatform/auth/queryAuthorizedApps");
        menuSubmitsMap.put("miniProgramSetting_chooseApp", "weixin/thirdplatform/auth/chooseApp");
        menuSubmitsMap.put("miniProgramSetting_authUrl", "weixin/thirdplatform/auth/authUrl");
        menuSubmitsMap.put("miniProgramSetting_fastRegister", "weixin/thirdplatform/auth/fastRegister");
        menuSubmitsMap.put("miniProgramSetting_query", "weixin/thirdplatform/mini/info/query");
        menuSubmitsMap.put("miniProgramSetting_modifySignature", "weixin/thirdplatform/mini/info/modifySignature");
        menuSubmitsMap.put("miniProgramSetting_modifyNick", "weixin/thirdplatform/mini/info/modifyNick");
        menuSubmitsMap.put("miniProgramSetting_uploadMedia", "weixin/thirdplatform/mini/info/uploadMedia");
        menuSubmitsMap.put("miniProgramSetting_modifyHeadImage", "weixin/thirdplatform/mini/info/modifyHeadImage");
        menuSubmitsMap.put("miniProgramSetting_queryDomains", "weixin/thirdplatform/mini/info/queryDomains");
        menuSubmitsMap.put("miniProgramSetting_allCates", "weixin/thirdplatform/mini/info/allCates");
        menuSubmitsMap.put("miniProgramSetting_modifyCate", "weixin/thirdplatform/mini/info/modifyCate");
        menuSubmitsMap.put("miniProgramSetting_setedCates", "weixin/thirdplatform/mini/info/setedCates");
        menuSubmitsMap.put("miniProgramSetting_pay", "weixin/thirdplatform/mini/info/pay");

        menuSubmitsMap.put("miniProgramStyleSetting_style", "weixin/thirdplatform/mini/info/style");
        menuSubmitsMap.put("miniProgramStyleSetting_queryStyle", "weixin/thirdplatform/mini/info/queryStyle");

        menuSubmitsMap.put("miniProgramCodeSetting_commitTest", "weixin/thirdplatform/mini/code/commitTest");
        menuSubmitsMap.put("miniProgramCodeSetting_test", "weixin/thirdplatform/mini/code/test");
        menuSubmitsMap.put("miniProgramCodeSetting_testCode", "weixin/thirdplatform/mini/code/testCode");
        menuSubmitsMap.put("miniProgramCodeSetting_delTest", "weixin/thirdplatform/mini/code/delTest");
        menuSubmitsMap.put("miniProgramCodeSetting_audit", "weixin/thirdplatform/mini/code/audit");
        menuSubmitsMap.put("miniProgramCodeSetting_commitAudit", "weixin/thirdplatform/mini/code/commitAudit");
        menuSubmitsMap.put("miniProgramCodeSetting_commitRelease", "weixin/thirdplatform/mini/code/commitRelease");
        menuSubmitsMap.put("miniProgramCodeSetting_unAudit", "weixin/thirdplatform/mini/code/unAudit");
        menuSubmitsMap.put("miniProgramCodeSetting_online", "weixin/thirdplatform/mini/code/online");
        menuSubmitsMap.put("miniProgramCodeSetting_testers", "weixin/thirdplatform/mini/code/testers");
        menuSubmitsMap.put("miniProgramCodeSetting_unbindTester", "weixin/thirdplatform/mini/code/unbindTester");
        menuSubmitsMap.put("miniProgramCodeSetting_bindTester", "weixin/thirdplatform/mini/code/bindTester");

        return menuSubmitsMap;
    }


    @GetMapping(path = "/wm/menus", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result menus(@RequestParam(name = "sellerid", required = false) String sellerId){
        //TODO 从session获取sellerid

        String key = CacheKeyPrefix.MENUS;
        String menuCache = cacheService.get(key);

        if (StringUtils.isBlank(menuCache)) {
            List<Menu> menus = mockMenu();
            menuCache = JSONObject.toJSONString(menus);
            cacheService.save(key, menuCache, 3600 * 24);
        }

        return Result.SUCCESS(menuCache);
    }
    
    private List<Menu> mockMenu() {
        List<Menu> menus = new ArrayList<>();

        Menu shop = new Menu();
        menus.add(shop);
        shop.setMenuId("shop");
        shop.setName("店铺");
        shop.setOrder(1);
        shop.setTarget("blank");
        shop.setMenus(new ArrayList<>());
        Menu shopMan = new Menu();
        shop.getMenus().add(shopMan);
        shopMan.setMenuId("shopMan");
        shopMan.setName("店铺管理");
        shopMan.setOrder(1);
        shopMan.setMenus(new ArrayList<>());
        Menu shopOverview = new Menu();
        shopMan.getMenus().add(shopOverview);
        shopOverview.setMenuId("shopOverview");
        shopOverview.setName("店铺概览");
        shopOverview.setUrl("shopOverview.html");

        Menu picMan = new Menu();
        shop.getMenus().add(picMan);
        picMan.setMenuId("picMan");
        picMan.setName("图片管理");
        picMan.setOrder(1);
        picMan.setMenus(new ArrayList<>());
        Menu picListMan = new Menu();
        picMan.getMenus().add(picListMan);
        picListMan.setMenuId("picMan");
        picListMan.setName("图片管理");
        picListMan.setUrl("picMan.html");
        Menu picUpload = new Menu();
        picMan.getMenus().add(picUpload);
        picUpload.setMenuId("picUploadMan");
        picUpload.setName("图片上传");
        picUpload.setUrl("picUploadMan.html");


        Menu item = new Menu();
        menus.add(item);
        item.setMenuId("item");
        item.setName("商品");
        item.setOrder(2);
        item.setTarget("blank");
        item.setMenus(new ArrayList<>());
        Menu cateMan = new Menu();
        item.getMenus().add(cateMan);
        cateMan.setMenuId("cateMan");
        cateMan.setName("类目管理");
        cateMan.setOrder(2);
        cateMan.setMenus(new ArrayList<>());
        Menu cateList = new Menu();
        cateMan.getMenus().add(cateList);
        cateList.setMenuId("cateList");
        cateList.setOrder(1);
        cateList.setName("类目列表");
        cateList.setUrl("cateList.html");

        Menu itemMan = new Menu();
        item.getMenus().add(itemMan);
        itemMan.setMenuId("itemMan");
        itemMan.setName("商品管理");
        itemMan.setOrder(1);
        itemMan.setMenus(new ArrayList<>());
        Menu itemList = new Menu();
        itemMan.getMenus().add(itemList);
        itemList.setMenuId("itemList");
        itemList.setOrder(1);
        itemList.setName("商品列表");
        itemList.setUrl("itemList.html");
        Menu itemDetail = new Menu();
        itemMan.getMenus().add(itemDetail);
        itemDetail.setMenuId("itemDetail");
        itemDetail.setOrder(2);
        itemDetail.setName("商品详情");
        itemDetail.setTarget("blank");
        itemDetail.setUrl("itemDetail.html");
        itemDetail.setShow(false);
        Menu itemEdit = new Menu();
        itemMan.getMenus().add(itemEdit);
        itemEdit.setMenuId("itemEdit");
        itemEdit.setOrder(3);
        itemEdit.setName("商品发布");
        itemEdit.setUrl("itemEdit.html");
        itemEdit.setShow(false);
        Menu itemRangeEdit = new Menu();
        itemMan.getMenus().add(itemRangeEdit);
        itemRangeEdit.setMenuId("itemRange");
        itemRangeEdit.setOrder(3);
        itemRangeEdit.setName("圈定商品");
        itemRangeEdit.setUrl("itemRange.html");
        itemRangeEdit.setShow(false);

        Menu activityMan = new Menu();
        item.getMenus().add(activityMan);
        activityMan.setMenuId("activityMan");
        activityMan.setName("店铺活动管理");
        activityMan.setOrder(3);
        activityMan.setMenus(new ArrayList<>());
        Menu activityList = new Menu();
        activityMan.getMenus().add(activityList);
        activityList.setMenuId("activityList");
        activityList.setOrder(1);
        activityList.setName("店铺活动列表");
        activityList.setUrl("activityList.html");
        Menu activityDetail = new Menu();
        activityMan.getMenus().add(activityDetail);
        activityDetail.setMenuId("activityDetail");
        activityDetail.setOrder(2);
        activityDetail.setName("店铺活动详情");
        activityDetail.setUrl("activityDetail.html");
        activityDetail.setShow(false);
        Menu activityEdit = new Menu();
        activityMan.getMenus().add(activityEdit);
        activityEdit.setMenuId("activityEdit");
        activityEdit.setOrder(3);
        activityEdit.setName("店铺活动发布");
        activityEdit.setUrl("activityEdit.html");
        activityEdit.setShow(false);

        Menu couponMan = new Menu();
        item.getMenus().add(couponMan);
        couponMan.setMenuId("couponMan");
        couponMan.setName("优惠券管理");
        couponMan.setOrder(4);
        couponMan.setMenus(new ArrayList<>());
        Menu couponList = new Menu();
        couponMan.getMenus().add(couponList);
        couponList.setMenuId("couponList");
        couponList.setOrder(1);
        couponList.setName("优惠券列表");
        couponList.setUrl("couponList.html");
        Menu couponDetail = new Menu();
        couponMan.getMenus().add(couponDetail);
        couponDetail.setMenuId("couponDetail");
        couponDetail.setOrder(2);
        couponDetail.setName("优惠券详情");
        couponDetail.setUrl("couponDetail.html");
        couponDetail.setShow(false);
        Menu couponEdit = new Menu();
        couponMan.getMenus().add(couponEdit);
        couponEdit.setMenuId("couponEdit");
        couponEdit.setOrder(3);
        couponEdit.setName("优惠券发布");
        couponEdit.setUrl("couponEdit.html");
        couponEdit.setShow(false);

        Menu saleStrategyMan = new Menu();
        item.getMenus().add(saleStrategyMan);
        saleStrategyMan.setMenuId("saleStrategyMan");
        saleStrategyMan.setName("销售策略管理");
        saleStrategyMan.setOrder(5);
        saleStrategyMan.setMenus(new ArrayList<>());
        Menu saleStrategyList = new Menu();
        saleStrategyMan.getMenus().add(saleStrategyList);
        saleStrategyList.setMenuId("saleStrategyList");
        saleStrategyList.setOrder(1);
        saleStrategyList.setName("销售策略列表");
        saleStrategyList.setUrl("saleStrategyList.html");
        Menu presellDetail = new Menu();
        saleStrategyMan.getMenus().add(presellDetail);
        presellDetail.setMenuId("presellDetail");
        presellDetail.setOrder(2);
        presellDetail.setName("预售详情");
        presellDetail.setUrl("presellDetail.html");
        presellDetail.setShow(false);
        Menu presellEdit = new Menu();
        saleStrategyMan.getMenus().add(presellEdit);
        presellEdit.setMenuId("presellEdit");
        presellEdit.setOrder(3);
        presellEdit.setName("预售发布");
        presellEdit.setUrl("presellEdit.html");
        presellEdit.setShow(false);
        Menu minFeeDetail = new Menu();
        saleStrategyMan.getMenus().add(minFeeDetail);
        minFeeDetail.setMenuId("minFeeDetail");
        minFeeDetail.setOrder(2);
        minFeeDetail.setName("订单实际金额下限详情");
        minFeeDetail.setUrl("minFeeDetail.html");
        minFeeDetail.setShow(false);
        Menu minFeeEdit = new Menu();
        saleStrategyMan.getMenus().add(minFeeEdit);
        minFeeEdit.setMenuId("minFeeEdit");
        minFeeEdit.setOrder(3);
        minFeeEdit.setName("订单实际金额下限发布");
        minFeeEdit.setUrl("minFeeEdit.html");
        minFeeEdit.setShow(false);
        Menu minCountDetail = new Menu();
        saleStrategyMan.getMenus().add(minCountDetail);
        minCountDetail.setMenuId("minCountDetail");
        minCountDetail.setOrder(2);
        minCountDetail.setName("限购-最少数量详情");
        minCountDetail.setUrl("minCountDetail.html");
        minCountDetail.setShow(false);
        Menu minCountEdit = new Menu();
        saleStrategyMan.getMenus().add(minCountEdit);
        minCountEdit.setMenuId("minCountEdit");
        minCountEdit.setOrder(3);
        minCountEdit.setName("限购-最小数量发布");
        minCountEdit.setUrl("minCountEdit.html");
        minCountEdit.setShow(false);
        Menu maxCountDetail = new Menu();
        saleStrategyMan.getMenus().add(maxCountDetail);
        maxCountDetail.setMenuId("maxCountDetail");
        maxCountDetail.setOrder(2);
        maxCountDetail.setName("限购-最大数量详情");
        maxCountDetail.setUrl("maxCountDetail.html");
        maxCountDetail.setShow(false);
        Menu maxCountEdit = new Menu();
        saleStrategyMan.getMenus().add(maxCountEdit);
        maxCountEdit.setMenuId("maxCountEdit");
        maxCountEdit.setOrder(3);
        maxCountEdit.setName("限购-最大数量发布");
        maxCountEdit.setUrl("maxCountEdit.html");
        maxCountEdit.setShow(false);

        Menu order = new Menu();
        menus.add(order);
        order.setMenuId("order");
        order.setName("订单");
        order.setOrder(3);
        order.setTarget("blank");
        order.setMenus(new ArrayList<>());
        Menu orderMan = new Menu();
        order.getMenus().add(orderMan);
        orderMan.setMenuId("orderMan");
        orderMan.setName("订单管理");
        orderMan.setOrder(1);
        orderMan.setMenus(new ArrayList<>());
        Menu orderList = new Menu();
        orderMan.getMenus().add(orderList);
        orderList.setMenuId("orderList");
        orderList.setName("订单列表");
        orderList.setOrder(1);
        orderList.setUrl("orderList.html");
        Menu orderDetail = new Menu();
        orderMan.getMenus().add(orderDetail);
        orderDetail.setMenuId("orderDetail");
        orderDetail.setName("订单详情");
        orderDetail.setOrder(2);
        orderDetail.setUrl("orderDetail.html");
        orderDetail.setShow(false);

        Menu config = new Menu();
        menus.add(config);
        config.setMenuId("config");
        config.setName("配置中心");
        config.setOrder(1);
        config.setTarget("blank");
        config.setMenus(new ArrayList<>());
        Menu shopConfig = new Menu();
        config.getMenus().add(shopConfig);
        shopConfig.setMenuId("shopConfig");
        shopConfig.setName("店铺配置");
        shopConfig.setOrder(1);
        shopConfig.setMenus(new ArrayList<>());
        Menu shopSetting = new Menu();
        shopConfig.getMenus().add(shopSetting);
        shopSetting.setMenuId("shopSetting");
        shopSetting.setName("店铺配置");
        shopSetting.setOrder(1);
        shopSetting.setUrl("shopSetting.html");
        Menu modifyPw = new Menu();
        shopConfig.getMenus().add(modifyPw);
        modifyPw.setMenuId("modifyPw");
        modifyPw.setName("修改密码");
        modifyPw.setOrder(2);
        modifyPw.setUrl("modifyPw.html");

        Menu miniProgrameConfig = new Menu();
        config.getMenus().add(miniProgrameConfig);
        miniProgrameConfig.setMenuId("miniProgrameConfig");
        miniProgrameConfig.setName("小程序配置");
        miniProgrameConfig.setOrder(2);
        miniProgrameConfig.setMenus(new ArrayList<>());
        Menu miniProgrameSetting = new Menu();
        miniProgrameConfig.getMenus().add(miniProgrameSetting);
        miniProgrameSetting.setMenuId("miniProgrameSetting");
        miniProgrameSetting.setName("小程序配置");
        miniProgrameSetting.setOrder(1);
        miniProgrameSetting.setUrl("miniProgrameSetting.html");

        return menus;
    }

    @Data
    public static class Menu{
        String menuId;
        String url;
        String name;
        /**
         * 功能页面html版本，url?v=version，以便浏览器及时更新
         */
        Integer version;
        int order;
        String target;
        List<Menu> menus;

        boolean show = true;

    }



}
