package com.chl.victory.wmall.controller.cart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

import com.chl.victory.common.redis.CacheKeyPrefix;
import com.chl.victory.common.redis.CacheService;
import com.chl.victory.localservice.CartLocalService;
import com.chl.victory.localservice.ItemLocalService;
import com.chl.victory.localservice.manager.LocalServiceManager;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.accesslimit.enums.AccessLimitTypeEnum;
import com.chl.victory.serviceapi.cart.model.CartComputeDTO;
import com.chl.victory.serviceapi.exception.AccessLimitException;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.item.enums.ItemStatusEnum;
import com.chl.victory.serviceapi.item.model.ItemDTO;
import com.chl.victory.serviceapi.item.model.SkuDTO;
import com.chl.victory.serviceapi.item.query.ItemQueryDTO;
import com.chl.victory.serviceapi.item.query.SkuQueryDTO;
import com.chl.victory.serviceapi.order.model.OrderFeeComputeDTO;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.wmall.model.Result;
import com.chl.victory.wmall.model.WmallSessionCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.chl.victory.localservice.manager.LocalServiceManager.itemLocalService;
import static com.chl.victory.webcommon.manager.RpcManager.accessLimitFacade;
import static com.chl.victory.webcommon.manager.RpcManager.cartFacade;
import static com.chl.victory.webcommon.manager.RpcManager.itemFacade;

/**
 * @author chenhailong
 * @date 2019/9/23 11:04
 **/
@RestController
@RequestMapping("/wmall/cart/")
@Api(description = "??????????????????????????????")
public class CartController {

    @Resource
    CacheService cacheService;
    @Resource
    CartLocalService cartLocalService;

    static Long castItemId(String itemId) throws BusServiceException {
        if (NumberUtils.isDigits(itemId)) {
            return Long.valueOf(itemId);
        }
        throw new BusServiceException("itemId????????????");
    }

    static Long castSkuId(String skuId) throws BusServiceException {
        if (NumberUtils.isDigits(skuId)) {
            return Long.valueOf(skuId);
        }
        throw new BusServiceException("skuId????????????");
    }

    /**
     * ?????????????????????????????????
     * @return
     */
    @GetMapping("compute")
    @ApiOperation(value = "????????????????????????????????????????????????????????????", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result compute(@Validated CartSelectedParam param) throws AccessLimitException {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        accessLimitFacade.checkAccessLimit(sessionCache.getShopId(), AccessLimitTypeEnum.WMALL_USER_SHOP_CART_COMPUTE,
                sessionCache.getThirdId() + CacheKeyPrefix.SEPARATOR + sessionCache.getUserId(), null);

        OrderFeeComputeDTO computeDTO = new OrderFeeComputeDTO();
        BeanUtils.copyProperties(param, computeDTO);
        computeDTO.setShopId(sessionCache.getShopId());
        computeDTO.setBuyerId(sessionCache.getUserId());

        ServiceResult<CartComputeDTO> cartComputeDTOServiceResult = cartFacade.computeForCart(computeDTO);

        if (!cartComputeDTOServiceResult.getSuccess()) {
            return Result.FAIL(cartComputeDTOServiceResult.getMsg());
        }

        if (cartComputeDTOServiceResult.getData() == null) {
            return Result.FAIL("???????????????");
        }

        CartComputeVO cartComputeVO = CartComputeVO.transfer(cartComputeDTOServiceResult.getData());

        accessLimitFacade.incrAccessLimit(sessionCache.getShopId(), AccessLimitTypeEnum.WMALL_USER_SHOP_CART_COMPUTE,
                sessionCache.getThirdId() + CacheKeyPrefix.SEPARATOR + sessionCache.getUserId());

        return Result.SUCCESS(cartComputeVO);
    }

    /**
     * ??????????????????????????????
     * @return
     */
    @GetMapping("items")
    @ApiOperation(value = "??????????????????????????????????????????", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result items(CartSelectedParam param) throws Exception {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        Map<String, Integer> cartItems = cartLocalService
                .getCartItems(sessionCache.getShopId(), sessionCache.getUserId());

        if (CollectionUtils.isEmpty(cartItems)) {
            return Result.SUCCESS();
        }

        if (param != null && !CollectionUtils.isEmpty(param.getItems())) {
            Map<String, Integer> temp = new HashMap<>();
            final Map<String, Integer> cartItemsBck = cartItems;
            param.getItems().stream().filter(key -> cartItemsBck.get(key) != null)
                    .forEach(key -> temp.put(key, cartItemsBck.get(key)));
            cartItems = temp;
        }

        List<CartItemVO> cartItemVOS = new ArrayList<>();
        for (HashMap.Entry<String, Integer> s : cartItems.entrySet()) {
            CartItemVO transfer = CartItemVO.transfer(s.getKey(), s.getValue());
            cartItemVOS.add(transfer);
        }

        cartItemVOS = cartItemVOS.stream().filter(this::fillupCartItem).collect(Collectors.toList());

        return Result.SUCCESS(cartItemVOS);
    }

    private boolean fillupCartItem(CartItemVO cartItemVO) {
        // ???item?????????
        ItemQueryDTO itemQuery = new ItemQueryDTO();
        itemQuery.setId(cartItemVO.getItemId());
        itemQuery.setShopId(SessionUtil.getSessionCache().getShopId());
        itemQuery.setNeedFillCacheData(true);
        itemQuery.setStatusStart(ItemStatusEnum.sale.getCode().byteValue());
        ServiceResult<List<ItemDTO>> itemResult = itemFacade.selectItems(itemQuery);
        if (!itemResult.getSuccess() || CollectionUtils.isEmpty(itemResult.getData())) {
            // throw new Exception("????????????" + cartItemVO.getItemId() + "??????");
            return false;
        }
        ItemDTO itemDTO = itemResult.getData().get(0);
        if (CollectionUtils.isEmpty(itemResult.getData()) || itemDTO == null) {
            // throw new Exception("????????????" + cartItemVO.getItemId() + "?????????");
            return false;
        }
        if (itemDTO.getInventory() == null || itemDTO.getInventory() < 1) {
            return false;
        }
        if (itemDTO.getStatus() != null && ItemStatusEnum.sellOut.equals(ItemStatusEnum.getByCode(itemDTO.getStatus().intValue()))) {
            return false;
        }

        ItemDTO itemDO = itemDTO;
        cartItemVO.setTitle(itemDO.getTitle());
        cartItemVO.setSaleStrategies(itemDO.getSaleStrategies());

        if (!ItemLocalService.isSKU(cartItemVO.getSkuId())) {
            cartItemVO.setSkuTitle(itemDO.getSubTitle());
            cartItemVO.setPrice(itemDO.getPrice().toString());
            cartItemVO.setInventory(itemDO.getInventory());
            // cartItemVO.setImg(itemDO.getFirstImg());
        }
        else {
            // ???sku?????????
            SkuQueryDTO query = new SkuQueryDTO();
            query.setItemId(cartItemVO.getItemId());
            query.setId(cartItemVO.getSkuId());
            query.setShopId(SessionUtil.getSessionCache().getShopId());
            ServiceResult<List<SkuDTO>> skuResult = itemFacade.selectSkus(query);
            if (!skuResult.getSuccess()) {
                // throw new Exception("????????????" + cartItemVO.getItemId() + "sku" + cartItemVO.getSkuId() + "??????");
                return false;
            }
            if (CollectionUtils.isEmpty(skuResult.getData()) || skuResult.getData().get(0) == null) {
                //throw new Exception("????????????" + cartItemVO.getItemId() + "sku" + cartItemVO.getSkuId() + "?????????");
                return false;
            }
            if (skuResult.getData().get(0).getInventory() == null || skuResult.getData().get(0).getInventory() < 1) {
                return false;
            }

            SkuDTO skuDO = skuResult.getData().get(0);
            // cartItemVO.setTitle(skuDO.getTitle());
            cartItemVO.setSkuTitle(skuDO.getTitle());
            cartItemVO.setPrice(skuDO.getPrice().toString());
            cartItemVO.setInventory(skuDO.getInventory());
            // cartItemVO.setImg(skuDO.getFirstImg());
        }

        cartItemVO.setImg(itemDO.getFirstImg());

        return true;
    }

    /**
     * ??????????????????????????????
     * @return
     */
    @PostMapping("addItem")
    @ApiOperation(value = "??????????????????????????????????????????????????????", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result addItem(@RequestParam @ApiParam(name = "??????ID", required = true) String itemId,
            @RequestParam @ApiParam(name = "SKUID", required = true) String skuId,
            @RequestParam @ApiParam(name = "????????????", required = true) Integer count) throws BusServiceException {
        if (null == count) {
            return Result.FAIL("??????????????????");
        }

        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        Long tempItemId = castItemId(itemId);
        Long tempSkuId = castSkuId(skuId);

        String field = tempItemId + "_" + tempSkuId;
        if (count > 0) {
            cartLocalService.addCartItem(sessionCache.getShopId(), sessionCache.getUserId(), field, count);
        }
        else {
            cartLocalService.delCartItem(sessionCache.getShopId(), sessionCache.getUserId(), field);
        }

        return Result.SUCCESS();
    }

    /**
     * ????????????????????????????????????
     */
    @Data
    public static class CartSelectedParam {

        @NotEmpty(message = "??????????????????") List<String> items;

        /**
         * ??????ID
         */
        String actId;

        /**
         * ?????????ID
         */
        String couId;
    }

    @Data
    public static class CartComputeVO {

        /**
         * ??????????????????(????????????)
         */
        private String total;

        /**
         * ???????????????????????????????????????????????????
         */
        private String coupon;

        private ComputedCouponDetail couponDetail;

        /**
         * ??????????????????
         */
        private Integer itemCount;

        public static CartComputeVO transfer(CartComputeDTO data) {
            CartComputeVO cartComputeVO = new CartComputeVO();
            cartComputeVO.setTotal(data.getRealFee().toString());
            cartComputeVO.setCoupon(data.getTotalFee().subtract(data.getRealFee()).toString());
            cartComputeVO.setItemCount(data.getItemCount());

            ComputedCouponDetail computedCouponDetail = new ComputedCouponDetail();
            computedCouponDetail.setTotalNoCoupon(data.getTotalFee().toString());
            computedCouponDetail.setCoupon(cartComputeVO.getCoupon());
            computedCouponDetail.setTotal(cartComputeVO.getTotal());

            List<CouponVO> couponVOS = new ArrayList<>();
            if (data.getActivityDTO() != null) {
                CouponVO activityCouponVO = new CouponVO();
                activityCouponVO.setDesc(data.getActivityDTO().getDesc());
                activityCouponVO.setAmount(data.getActivityDTO().getTotalDis());
                couponVOS.add(activityCouponVO);
                computedCouponDetail.setActId(data.getActivityDTO().getId());
            }

            if (data.getCouponsDTO() != null) {
                CouponVO couponVO = new CouponVO();
                couponVO.setDesc(data.getCouponsDTO().getDesc());
                couponVO.setAmount(data.getCouponsDTO().getDiscount());
                couponVOS.add(couponVO);
                computedCouponDetail.setCouId(data.getCouponsDTO().getId());
            }

            computedCouponDetail.setCoupons(couponVOS);
            cartComputeVO.setCouponDetail(computedCouponDetail);

            return cartComputeVO;
        }
    }

    @Data
    public static class ComputedCouponDetail {

        /**
         * ???????????????????????????
         */
        private String totalNoCoupon;

        /**
         * @see CartComputeVO#coupon
         */
        private String coupon;

        /**
         * ????????????????????????
         */
        private String total;

        /**
         * ?????????????????????
         */
        private List<CouponVO> coupons;

        /**
         * ???????????????ID
         */
        private Long actId;

        /**
         * ??????????????????ID
         */
        private Long couId;
    }

    /**
     * ????????????????????????????????????
     */
    @Data
    public static class CouponVO {

        private String desc;

        private String amount;
    }

    @Data
    public static class CartItemVO {

        private Long itemId;

        private Long skuId;

        private String title;

        private String skuTitle;

        private String price;

        private Integer count;

        private String img;

        /**
         * ?????????
         * minimum
         */
        private Integer minimum = 1;

        private Integer inventory = 0;

        private boolean checked = false;

        /**
         * ?????????????????? json
         */
        List<String> saleStrategies;

        public static CartItemVO transfer(String cartItem, Integer count) throws BusServiceException {
            Long itemId;
            Long skuId;
            try {
                String[] temp = cartItem.split("_");
                itemId = castItemId(temp[ 0 ]);
                skuId = castSkuId(temp[ 1 ]);
            } catch (Exception e) {
                throw new BusServiceException("???????????????????????????");
            }

            CartItemVO cartItemVO = new CartItemVO();
            cartItemVO.setItemId(itemId);
            cartItemVO.setSkuId(skuId);
            cartItemVO.setCount(count);
            return cartItemVO;
        }
    }

}
