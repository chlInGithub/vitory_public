package com.chl.victory.wmall.controller.order;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.constants.DateConstants;
import com.chl.victory.localservice.OrderLocalService;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.accesslimit.enums.AccessLimitTypeEnum;
import com.chl.victory.serviceapi.exception.AccessLimitException;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.merchant.model.SaleStrategyDTO;
import com.chl.victory.serviceapi.order.enums.DeliverTypeEnum;
import com.chl.victory.serviceapi.order.enums.OrderStatusEnum;
import com.chl.victory.serviceapi.order.enums.RefundStatusEnum;
import com.chl.victory.serviceapi.order.enums.RefundTypeEnum;
import com.chl.victory.serviceapi.order.model.OrderCreateDTO;
import com.chl.victory.serviceapi.order.model.OrderDTO;
import com.chl.victory.serviceapi.order.model.RefundDTO;
import com.chl.victory.serviceapi.order.model.SubOrderDTO;
import com.chl.victory.serviceapi.order.query.OrderQueryDTO;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.wmall.model.PageParam;
import com.chl.victory.wmall.model.PageResult;
import com.chl.victory.wmall.model.Result;
import com.chl.victory.wmall.model.WmallSessionCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static com.chl.victory.webcommon.manager.RpcManager.accessLimitFacade;
import static com.chl.victory.webcommon.manager.RpcManager.orderFacade;

/**
 * @author chenhailong
 * @date 2019/9/23 11:04
 **/
@RestController
@RequestMapping("/wmall/order/")
@Api(description = "??????????????????")
@Slf4j
public class OrderController {

    @Resource
    OrderLocalService orderLocalService;

    /**
     * ????????????
     * @return ?????????ID
     */
    @PostMapping(value = "new", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiOperation(value = "????????????", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Long> create(@Validated OrderInfo4Create orderInfo4Create) throws Exception {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        accessLimitFacade.checkAccessLimit(sessionCache.getShopId(), AccessLimitTypeEnum.WMALL_USER_SHOP_ORDER_CREATE,
                sessionCache.getUserId() + "", AccessLimitTypeEnum.WMALL_USER_SHOP_ORDER_CREATE.getDesc());
        accessLimitFacade.checkAccessLimit(sessionCache.getShopId(), AccessLimitTypeEnum.WMALL_SHOP_CREATE_ORDER, null,
                AccessLimitTypeEnum.WMALL_SHOP_CREATE_ORDER.getDesc());

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        BeanUtils.copyProperties(orderInfo4Create, orderCreateDTO);
        orderCreateDTO.setShopId(sessionCache.getShopId());
        orderCreateDTO.setBuyerId(sessionCache.getUserId());
        orderCreateDTO.setBuyerImg(sessionCache.getThirdImg());
        orderCreateDTO.setAppId(sessionCache.getAppId());

        ServiceResult<Long> serviceResult = orderFacade.createOrderWithNxLock(orderCreateDTO);

        if (serviceResult.getSuccess()) {
            accessLimitFacade
                    .incrAccessLimit(sessionCache.getShopId(), AccessLimitTypeEnum.WMALL_USER_SHOP_ORDER_CREATE,
                            sessionCache.getUserId() + "");
            accessLimitFacade
                    .incrAccessLimit(sessionCache.getShopId(), AccessLimitTypeEnum.WMALL_SHOP_CREATE_ORDER, null);

            return Result.SUCCESS(serviceResult.getData());
        }

        return Result.FAIL(serviceResult.getMsg());
    }

    @PostMapping(value = "del", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiOperation(value = "????????????", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result del(@NotNull Long orderId) throws BusServiceException {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        orderFacade.delOrderAndCach2C(sessionCache.getShopId(), sessionCache.getUserId(), orderId);
        return Result.SUCCESS();
    }

    @PostMapping(value = "success", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiOperation(value = "????????????", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result success(@NotNull Long orderId) throws BusServiceException {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();
        orderFacade.deliveryCompleted(orderId, sessionCache.getShopId(), sessionCache.getUserId(), "{desc:'??????????????????'}");
        return Result.SUCCESS();
    }

    @PostMapping(value = "refundApply", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiOperation(value = "????????????", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result refundApply(@Validated RefundApply refundApply) throws BusServiceException {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        RefundDTO refundDTO = new RefundDTO();
        BeanUtils.copyProperties(refundApply, refundDTO);
        refundDTO.setShopId(sessionCache.getShopId());
        refundDTO.setBuyerId(sessionCache.getUserId());
        refundDTO.setApplyFee(new BigDecimal(refundApply.applyFee));

        ServiceResult<Long> serviceResult = orderFacade.createRefundWithNxLock(refundDTO);
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }

        return Result.FAIL(serviceResult.getMsg());
    }

    /**
     * ????????????
     * @return ?????????ID
     */
    @GetMapping(value = "detail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiOperation(value = "????????????", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result detail(@NotNull Long orderId) throws BusServiceException {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        String orderDetailCache = orderLocalService
                .getOrderDetailCache(sessionCache.getShopId(), sessionCache.getUserId(), orderId);
        if (StringUtils.isBlank(orderDetailCache)) {
            OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
            orderServiceQuery.setShopId(sessionCache.getShopId());
            orderServiceQuery.setBuyerId(sessionCache.getUserId());
            orderServiceQuery.setId(orderId);
            orderServiceQuery.setNeedDeliverInfo(true);
            orderServiceQuery.setNeedPayOrder(true);
            orderServiceQuery.setNeedCoupons(true);
            orderServiceQuery.setNeedSubOrder(true);
            orderServiceQuery.setNeedRefund(true);
            ServiceResult<OrderDTO> orderDTOServiceResult = orderFacade.selectMainOrder(orderServiceQuery);
            if (!orderDTOServiceResult.getSuccess()) {
                return Result.FAIL(orderDTOServiceResult.getMsg());
            }
            OrderDTO orderDTO = orderDTOServiceResult.getData();

            MainOrderVO mainOrderVO = transfer2OrderVO(orderDTO);
            orderDetailCache = JSONObject.toJSONString(mainOrderVO);

            orderLocalService.saveOrderDetailCache(sessionCache.getShopId(), sessionCache.getUserId(), orderId,
                    orderDetailCache);
        }

        return Result.SUCCESS(orderDetailCache);
    }

    /**
     * ????????????
     * @return ?????????ID
     */
    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiOperation(value = "????????????", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResult list(OrderParam param) throws BusServiceException {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        Integer pageIndex = param.getPageIndex() == null ? 0 : param.getPageIndex();
        Integer pageSize = param.getPageSize() == null ? 10 : param.getPageSize();
        String statusField = param.getStatus() == null ?
                ("all_" + pageIndex + "_" + pageSize) :
                (param.getStatus() + "_" + pageIndex + "_" + pageSize);
        String orderListCache = orderLocalService
                .getOrderListCache(sessionCache.getShopId(), sessionCache.getUserId(), statusField);
        Integer orderTotalCache = orderLocalService
                .getOrderTotalCache(sessionCache.getShopId(), sessionCache.getUserId(), statusField);

        if (StringUtils.isBlank(orderListCache)) {
            OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
            orderServiceQuery.setShopId(sessionCache.getShopId());
            orderServiceQuery.setBuyerId(sessionCache.getUserId());
            orderServiceQuery.setStatus(param.getStatus());
            orderServiceQuery.setNeedSubOrder(true);
            orderServiceQuery.setNeedPayOrder(true);
            orderServiceQuery.setPageIndex(pageIndex);
            orderServiceQuery.setPageSize(pageSize);

            ServiceResult<List<OrderDTO>> orderDTOResult = orderFacade.selectMains(orderServiceQuery);
            if (!orderDTOResult.getSuccess()) {
                if (ServiceFailTypeEnum.NOT_EXIST.equals(orderDTOResult.getFailType())) {
                    return PageResult.SUCCESS("[]", 0, param.getPageIndex(), orderTotalCache);
                }
                return PageResult.FAIL("??????????????????", 0, 0, param.getPageIndex(), orderTotalCache);
            }
            ServiceResult<Integer> totalResult = orderFacade.countOrder(orderServiceQuery);
            if (!totalResult.getSuccess()) {
                return PageResult.FAIL("????????????????????????", 0, 0, param.getPageIndex(), orderTotalCache);
            }

            List<OrderDTO> orderDTOs = orderDTOResult.getData();
            Integer total = totalResult.getData();

            List<MainOrderVO> mainOrderVOs = new ArrayList<>();
            if (!CollectionUtils.isEmpty(orderDTOs)) {
                mainOrderVOs = orderDTOs.stream().map(OrderController::transfer2OrderVO).collect(Collectors.toList());
            }

            orderListCache = JSONObject.toJSONString(mainOrderVOs);
            orderTotalCache = total;

            orderLocalService.saveOrderListCache(sessionCache.getShopId(), sessionCache.getUserId(), statusField,
                    orderListCache);
            orderLocalService
                    .saveOrderTotalCache(sessionCache.getShopId(), sessionCache.getUserId(), statusField, orderTotalCache);
        }

        return PageResult.SUCCESS(orderListCache, 0, param.getPageIndex(), orderTotalCache);
    }

    /**
     * ????????????count
     */
    @GetMapping(value = "status", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiOperation(value = "????????????count", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result statusCount() {
        WmallSessionCache sessionCache = (WmallSessionCache) SessionUtil.getSessionCache();

        String val = orderLocalService.getStatusCountCache(sessionCache.getShopId(), sessionCache.getUserId());
        if (StringUtils.isBlank(val)) {
            List<StatusCountVO> statusCountVOS = new ArrayList<>();

            OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
            orderServiceQuery.setShopId(sessionCache.getShopId());
            orderServiceQuery.setBuyerId(sessionCache.getUserId());
            Arrays.asList(OrderStatusEnum.newed.getCode(), OrderStatusEnum.payed.getCode(),
                    OrderStatusEnum.sent.getCode(), OrderStatusEnum.askRefund.getCode()).forEach(status -> {
                orderServiceQuery.setStatus(status.byteValue());
                ServiceResult<Integer> countOrder = orderFacade.countOrder(orderServiceQuery);
                if (countOrder != null && countOrder.getSuccess() && countOrder.getData() != null) {
                    statusCountVOS.add(new StatusCountVO(status, countOrder.getData()));
                }
            });

            val = JSONObject.toJSONString(statusCountVOS);
            // todo ????????????????????????????????????????????????
            orderLocalService.saveStatusCountCache(sessionCache.getShopId(), sessionCache.getUserId(), val);

        }
        return Result.SUCCESS(val);

    }

    public static MainOrderVO transfer2OrderVO(OrderDTO orderDTO) {
        String createTime = DateFormatUtils.format(orderDTO.getCreatedTime(), DateConstants.format1);
        Integer status = orderDTO.getStatus().getCode();
        String statusDesc = orderDTO.getStatus().getDesc();

        List<SubOrderVO> subOrderVOS = null;
        if (!CollectionUtils.isEmpty(orderDTO.getSubOrders())) {
            subOrderVOS = orderDTO.getSubOrders().stream().map(item -> {
                SubOrderVO subOrderVO = new SubOrderVO();
                BeanUtils.copyProperties(item, subOrderVO);
                return subOrderVO;
            }).collect(Collectors.toList());
        }

        DeliverVO deliverVO = null;
        if (orderDTO.getOrderDeliver() != null) {
            deliverVO = new DeliverVO();
            BeanUtils.copyProperties(orderDTO.getOrderDeliver(), deliverVO);
            deliverVO.setTypeDesc(
                    DeliverTypeEnum.noLogistics.equals(orderDTO.getOrderDeliver().getType()) ? "??????" : "??????/??????");
            deliverVO.setType(orderDTO.getOrderDeliver().getType().getCode());
        }

        List<OrderActivityVO> activity = null;
        if (!CollectionUtils.isEmpty(orderDTO.getActivity())) {
            activity = orderDTO.getActivity().stream().map(item -> {
                OrderActivityVO orderActivityVO = new OrderActivityVO();
                BeanUtils.copyProperties(item, orderActivityVO);
                return orderActivityVO;
            }).collect(Collectors.toList());
        }

        List<OrderCouponsVO> coupons = null;
        if (!CollectionUtils.isEmpty(orderDTO.getCoupons())) {
            coupons = orderDTO.getCoupons().stream().map(item -> {
                OrderCouponsVO couponsVO = new OrderCouponsVO();
                BeanUtils.copyProperties(item, couponsVO);
                return couponsVO;
            }).collect(Collectors.toList());
        }

        OrderPointsCashVO pointsCash = null;
        if (orderDTO.getPointsCash() != null) {
            pointsCash = new OrderPointsCashVO();
            BeanUtils.copyProperties(orderDTO.getPointsCash(), pointsCash);
        }

        PayVO payVO = null;
        if (orderDTO.getPayOrder() != null) {
            payVO = new PayVO();
            payVO.setId(orderDTO.getPayOrder().getId());
            payVO.setStatus(orderDTO.getPayOrder().getStatus().getCode());
            payVO.setTypeDesc(orderDTO.getPayOrder().getType().getDesc());
            payVO.setType(orderDTO.getPayOrder().getType().getCode());
        }

        RefundVO refundVO = null;
        if (orderDTO.getRefund() != null) {
            refundVO = new RefundVO();
            BeanUtils.copyProperties(orderDTO.getRefund(), refundVO);
            refundVO.setApplyFee(orderDTO.getRefund().getApplyFee().toString());
            refundVO.setStatusDesc(RefundStatusEnum.getByCode(orderDTO.getRefund().getStatus().intValue()).getDesc());
            refundVO.setTypeDesc(RefundTypeEnum.getByCode(orderDTO.getRefund().getType().intValue()).getDesc());
        }

        MainOrderVO mainOrderVO = new MainOrderVO();
        mainOrderVO.setId(orderDTO.getId());
        mainOrderVO.setCreateTime(createTime);
        mainOrderVO.setStatus(status);
        mainOrderVO.setStatusDes(statusDesc);
        mainOrderVO.setSubOrderVOS(subOrderVOS);
        mainOrderVO.setDeliverVO(deliverVO);
        mainOrderVO.setTotalFee(orderDTO.getTotalFee());
        mainOrderVO.setActivity(activity);
        mainOrderVO.setCoupons(coupons);
        mainOrderVO.setPointsCash(pointsCash);
        mainOrderVO.setPayVO(payVO);
        mainOrderVO.setRealFee(orderDTO.getRealFee());
        mainOrderVO.setNote(orderDTO.getNote());
        mainOrderVO.setRefundVO(refundVO);

        boolean checkStatus = status == 20 || status == 30 || status == 40;
        boolean checkOther = true;
        if (checkStatus) {
            for (SubOrderDTO subOrderDTO : orderDTO.getSubOrders()) {
                if (StringUtils.isNotBlank(subOrderDTO.getPresell())) {
                    SaleStrategyDTO.PreSellAttr preSellAttr = JSONObject
                            .parseObject(subOrderDTO.getPresell(), SaleStrategyDTO.PreSellAttr.class);
                    Date now = new Date();
                    Date endTime;
                    Date lastSentTime;
                    try {
                        endTime = DateUtils.parseDate(preSellAttr.getEndTime(), DateConstants.format1);
                        lastSentTime = DateUtils
                                .addDays(DateUtils.parseDate(preSellAttr.getSentTime(), DateConstants.format1), 5);
                    } catch (ParseException e) {
                        log.error("subOrder_presell_time_error|{}", subOrderDTO.getId(), e);
                        checkOther = false;
                        break;
                    }
                    if (now.after(endTime) && now.before(lastSentTime)) {
                        checkOther = false;
                    }
                }
            }
        }
        mainOrderVO.setCanRefund(checkStatus && checkOther);

        return mainOrderVO;
    }

    @Data
    @AllArgsConstructor
    public static class StatusCountVO {

        Integer code;

        Integer count;
    }

    @Data
    public static class OrderParam extends PageParam {

        private Byte status;
    }

    @Data
    public static class MainOrderVO {

        Long id;

        String createTime;

        String payTime;

        Integer status;

        String statusDes;

        List<SubOrderVO> subOrderVOS;

        DeliverVO deliverVO;

        /**
         * ????????????????????????????????????????????????
         * total_fee
         */
        private BigDecimal totalFee;

        /**
         * ????????????
         * activity
         */
        private List<OrderActivityVO> activity;

        /**
         * ???????????????
         * coupons
         */
        private List<OrderCouponsVO> coupons;

        /**
         * ?????????????????????json??????
         * points_cash
         */
        private OrderPointsCashVO pointsCash;

        private PayVO payVO;

        /**
         * ???????????????????????????????????????-??????-??????-???????????????
         * real_fee
         */
        private BigDecimal realFee;

        /**
         * ??????
         * note
         */
        private String note;

        private RefundVO refundVO;

        private boolean canRefund;
    }

    @Data
    public static class RefundVO {

        Long orderId;

        Long id;

        Byte type;

        String typeDesc;

        String cause;

        String applyFee;

        String statusDesc;
    }

    @Data
    public static class OrderPointsCashVO {

        /**
         * ????????????????????????
         */
        public Integer used;

        /**
         * ????????????????????????
         */
        public String cash;
    }

    @Data
    public static class OrderCouponsVO {

        /**
         * ??????ID
         */
        public String id;

        public String desc;

        /**
         * ?????????????????????
         */
        public String discount;
    }

    @Data
    public static class OrderActivityVO {

        /**
         * ??????ID
         */
        public Long id;

        public String desc;

        /**
         * ????????????????????????????????????
         */
        public String totalDis;
    }

    @Data
    public static class DeliverVO {

        /**
         * ???????????????????????????member_deliver?????????????????????????????????????????????????????????????????????????????????
         * mobile
         */
        private Long mobile;

        /**
         * ?????????
         * name
         */
        private String name;

        /**
         * ????????????
         * addr
         */
        private String address;

        private String city;

        /**
         * ????????????,0:???????????????;1:????????????
         * type
         */
        private String typeDesc;
        private Integer type;

        /**
         * ??????????????????
         * logistics_cp
         */
        private String logisticsCp;

        /**
         * ???????????????
         * logistics_no
         */
        private String logisticsNo;

        /**
         * ??????
         * freight
         */
        private BigDecimal freight;
    }

    @Data
    public static class PayVO {

        Long id;

        String typeDesc;

        Integer status;

        Integer type;
    }

    @Data
    public static class SubOrderVO {

        private Long itemId;

        private String itemTitle;

        private String itemImg;

        /**
         * skuID
         * sku_id
         */
        private Long skuId;

        private String skuTitle;

        /**
         * ?????????
         * price
         */
        private BigDecimal price;

        /**
         * ??????
         * count
         */
        private Byte count;

        /**
         * ??????????????????????????????????????????
         * total_fee
         */
        private BigDecimal totalFee;

        private String presell;
    }

    @Data
    public static class RefundApply {

        @NotNull Long orderId;

        Long id;

        @NotNull Byte type;

        @NotEmpty String cause;

        @NotEmpty String applyFee;
    }

    @Data
    public static class OrderInfo4Create {

        /**
         * ?????? ???????????????itemId_skuId_count
         */
        @NotEmpty List<String> items;

        /**
         * ????????????ID
         */
        String actId;

        /**
         * ?????????ID
         */
        String couId;

        /**
         * ????????????ID
         */
        @NotBlank String deliverId;

        /**
         * ??????????????????????????????
         * @see DeliverTypeEnum
         */
        @NotNull Integer deliverType;

        /**
         * ????????????
         */
        String deliverFee;

        /**
         * ??????
         */
        String note;

        /**
         * ???????????????????????????????????????????????????????????????????????????
         */
        String referTotal;

        /**
         * {@link com.chl.victory.serviceapi.order.enums.PayTypeEnum}
         */
        @NotNull Integer payType;
    }
}
