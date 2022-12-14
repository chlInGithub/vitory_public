package com.chl.victory.web.controller.wm.order;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.chl.victory.common.constants.DateConstants;
import com.chl.victory.common.util.BigDecimalUtil;
import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.webcommon.model.SessionCache;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.member.model.ShopMemberDTO;
import com.chl.victory.serviceapi.order.enums.DeliverTypeEnum;
import com.chl.victory.serviceapi.order.enums.OrderStatusEnum;
import com.chl.victory.serviceapi.order.enums.PayOrderStatusEnum;
import com.chl.victory.serviceapi.order.enums.PayTypeEnum;
import com.chl.victory.serviceapi.order.enums.RefundStatusEnum;
import com.chl.victory.serviceapi.order.enums.RefundTypeEnum;
import com.chl.victory.serviceapi.order.model.ActivityDTO;
import com.chl.victory.serviceapi.order.model.CouponsDTO;
import com.chl.victory.serviceapi.order.model.OrderDTO;
import com.chl.victory.serviceapi.order.model.OrderDeliverDTO;
import com.chl.victory.serviceapi.order.model.OrderStatusStatisDTO;
import com.chl.victory.serviceapi.order.model.PayOrderDTO;
import com.chl.victory.serviceapi.order.model.PointsCashDTO;
import com.chl.victory.serviceapi.order.model.RefundDTO;
import com.chl.victory.serviceapi.order.model.StatusFlowNodeDTO;
import com.chl.victory.serviceapi.order.model.SubOrderDTO;
import com.chl.victory.serviceapi.order.query.OrderDeliverQueryDTO;
import com.chl.victory.serviceapi.order.query.OrderQueryDTO;
import com.chl.victory.serviceapi.order.query.PayOrderQueryDTO;
import com.chl.victory.web.aspect.IgnoreExperience;
import com.chl.victory.web.model.PageParam;
import com.chl.victory.web.model.PageResult;
import com.chl.victory.web.model.Result;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.chl.victory.webcommon.manager.RpcManager.orderFacade;

/**
 * ???????????????
 * @author hailongchen9
 */
@Controller
@RequestMapping("/p")
public class OrderController {

    static Random randomId = new Random(Long.MAX_VALUE);

    /**
     * ??????????????????
     * @param orderId
     * @param note
     * @return
     */
    @PostMapping(path = "/wm/order/modifyNote", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result modifyNote(@RequestParam("orderId") Long orderId, @RequestParam("note") String note) {
        ServiceResult serviceResult = orderFacade
                .updateOrderNote(orderId, note, SessionUtil.getSessionCache().getShopId());
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    /**
     * ??????
     * @param orderId
     * @return
     */
    @PostMapping(path = "/wm/order/sent", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result sent(@RequestParam("orderId") Long orderId, @RequestParam("logisticsCP") String logisticsCP,
            @RequestParam("logisticsNo") String logisticsNo) {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ServiceResult serviceResult = orderFacade
                .sent(orderId, sessionCache.getShopId(), sessionCache.getUserId(), logisticsCP, logisticsNo);
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    @PostMapping(path = "/wm/order/del", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result del(@RequestParam("orderId") Long orderId) throws BusServiceException {
        ServiceResult serviceResult = orderFacade
                .delOrder(Arrays.asList(orderId), SessionUtil.getSessionCache().getShopId());
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    @PostMapping(path = "/wm/order/okRefund", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result okRefund(@RequestParam("orderId") Long orderId, @RequestParam("refundId") Long refundId,
            @RequestParam("note") String note) throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();

        ServiceResult serviceResult = orderFacade
                .okRefund(orderId, refundId, note, sessionCache.getShopId(), sessionCache.getUserId());
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    @PostMapping(path = "/wm/order/refuseRefund", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result refuseRefund(@RequestParam("orderId") Long orderId, @RequestParam("refundId") Long refundId,
            @RequestParam("note") String note) throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();

        ServiceResult serviceResult = orderFacade
                .refuseRefund(orderId, refundId, note, sessionCache.getShopId(), sessionCache.getUserId());
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    @PostMapping(path = "/wm/order/close", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result close(@RequestParam("orderId") Long orderId) throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();
        ServiceResult serviceResult = orderFacade
                .updateOrder(orderId, sessionCache.getShopId(), sessionCache.getUserId(), OrderStatusEnum.close, null);
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    /**
     * ?????????????????????
     * @param orderId
     * @return
     * @throws BusServiceException
     */
    @PostMapping(path = "/wm/order/confirmOfflinePayed", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result confirmOfflinePayed(@RequestParam("orderId") Long orderId) throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();

        PayOrderQueryDTO payQuery = new PayOrderQueryDTO();
        payQuery.setOrderId(orderId);
        payQuery.setShopId(sessionCache.getShopId());
        payQuery.setType(PayTypeEnum.offline.getCode().byteValue());
        payQuery.setStatus(PayOrderStatusEnum.newed.getVal());
        ServiceResult<Integer> countPayOrder = orderFacade.countPayOder(payQuery);
        if (!countPayOrder.getSuccess()) {
            return Result.FAIL(countPayOrder.getMsg());
        }

        if (countPayOrder.getData() == null || countPayOrder.getData() < 1) {
            return Result.FAIL("?????????????????????????????????????????????");
        }

        ServiceResult serviceResult = orderFacade
                .confirmPayedWithNxLock(orderId, sessionCache.getShopId(), sessionCache.getUserId(), "{desc:'????????????'}");
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    /**
     * ????????????
     * @param orderId
     * @return
     * @throws BusServiceException
     */
    @PostMapping(path = "/wm/order/taken", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result taken(@RequestParam("orderId") Long orderId) throws BusServiceException {
        SessionCache sessionCache = SessionUtil.getSessionCache();

        OrderDeliverQueryDTO deliverQuery = new OrderDeliverQueryDTO();
        deliverQuery.setOrderId(orderId);
        deliverQuery.setType(DeliverTypeEnum.noLogistics.getCode().byteValue());
        ServiceResult<Integer> countOrder = orderFacade.countDeliverOrder(deliverQuery);
        if (!countOrder.getSuccess()) {
            return Result.FAIL(countOrder.getMsg());
        }

        if (countOrder.getData() == null || countOrder.getData() < 1) {
            return Result.FAIL("?????????????????????????????????");
        }

        ServiceResult serviceResult = orderFacade
                .deliveryCompleted(orderId, sessionCache.getShopId(), sessionCache.getUserId(), "{desc:'?????????????????????'}");
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    @GetMapping(path = "/wm/order/detail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result orderDetail(@RequestParam("orderId") Long orderId) {
        OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
        orderServiceQuery.setId(orderId);
        orderServiceQuery.setShopId(SessionUtil.getSessionCache().getShopId());
        orderServiceQuery.needAllInfo();
        ServiceResult<List<OrderDTO>> orderResult = orderFacade.selectMains(orderServiceQuery);

        if (!orderResult.getSuccess()) {
            return Result.FAIL(orderResult.getMsg());
        }

        if (CollectionUtils.isEmpty(orderResult.getData())) {
            return Result.FAIL("???????????????");
        }

        OrderInfo orderInfo;
        orderInfo = OrderInfo.transfer(orderResult.getData().get(0));
        return Result.SUCCESS(orderInfo);
    }

    @GetMapping(path = "/wm/order/list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public PageResult orderList(OrderParam param) {
        OrderQueryDTO orderServiceQuery;
        try {
            orderServiceQuery = prepareOrderServiceQuery(param);
        } catch (Exception e) {
            return PageResult.FAIL(e.getLocalizedMessage(), 0, 0, 0, 0);
        }

        ServiceResult<Integer> countResult = orderFacade.countOrder(orderServiceQuery);
        if (!countResult.getSuccess()) {
            return PageResult.FAIL(countResult.getMsg(), 0, param.getDraw(), param.getPageIndex(), 0);
        }
        if (countResult.getData() < 1) {
            return PageResult.SUCCESS(Collections.EMPTY_LIST, param.getDraw(), param.getPageIndex(), 0);
        }

        orderServiceQuery.needAllInfo();
        orderServiceQuery.setNeedStatusFlow(false);
        ServiceResult<List<OrderDTO>> orderResult = orderFacade.selectMains(orderServiceQuery);
        if (!orderResult.getSuccess()) {
            return PageResult.FAIL(orderResult.getMsg(), orderResult.getFailType().getType(), param.getDraw(),
                    param.getPageIndex(), 0);
        }
        List<OrderInfo> orderInfos;
        if (CollectionUtils.isEmpty(orderResult.getData())) {
            return PageResult.FAIL("???????????????", 0, param.getDraw(), param.getPageIndex(), 0);
        }

        orderInfos = orderResult.getData().stream().map(OrderInfo::transfer).collect(Collectors.toList());

        return PageResult.SUCCESS(orderInfos, param.getDraw(), param.getPageIndex(), countResult.getData());
    }

    private OrderQueryDTO prepareOrderServiceQuery(OrderParam param) throws Exception {
        if (null == param) {
            throw new Exception("????????????");
        }

        OrderQueryDTO orderServiceQuery = new OrderQueryDTO();
        BeanUtils.copyProperties(param, orderServiceQuery);
        if (null != param.getOrderId()) {
            orderServiceQuery.setId(param.getOrderId());
        }
        if (null != param.getDeliveryType()) {
            orderServiceQuery.setDeliverType(param.getDeliveryType().byteValue());
        }
        if (StringUtils.isNotBlank(param.getEnd())) {
            try {
                orderServiceQuery.setEndedCreatedTime(DateUtils.parseDate(param.getEnd(), DateConstants.format1));
            } catch (ParseException e) {
                throw new Exception("??????????????????");
            }
        }
        if (StringUtils.isNotBlank(param.getStart())) {
            try {
                orderServiceQuery.setStartedCreatedTime(DateUtils.parseDate(param.getStart(), DateConstants.format1));
            } catch (ParseException e) {
                throw new Exception("??????????????????");
            }
        }
        if (null != param.getPayType()) {
            orderServiceQuery.setPayType(param.getPayType().byteValue());
        }
        if (null != param.getStatus()) {
            orderServiceQuery.setStatus(param.getStatus().byteValue());
        }
        orderServiceQuery.setId(param.getOrderId());
        orderServiceQuery.setShopId(SessionUtil.getSessionCache().getShopId());
        return orderServiceQuery;
    }

    @GetMapping(path = "/wm/order/statusCount", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result statusCount(OrderParam param) {
        OrderQueryDTO orderServiceQuery;
        try {
            orderServiceQuery = prepareOrderServiceQuery(param);
        } catch (Exception e) {
            return Result.FAIL(e.getLocalizedMessage());
        }

        ServiceResult<List<OrderStatusStatisDTO>> countStatusResult = orderFacade.countStatus(orderServiceQuery);
        if (!countStatusResult.getSuccess()) {
            return Result.FAIL(countStatusResult.getMsg());
        }

        if (CollectionUtils.isEmpty(countStatusResult.getData())) {
            return Result.FAIL("???????????????????????????");
        }

        List<OrderStatusAndCount> orderStatusAndCounts = new ArrayList<>();
        Integer total = 0;
        for (OrderStatusStatisDTO orderStatusStatisDTO : countStatusResult.getData()) {
            OrderStatusAndCount orderStatusAndCount = OrderStatusAndCount.transfer(orderStatusStatisDTO);
            orderStatusAndCounts.add(orderStatusAndCount);
            total += orderStatusAndCount.getCount();
        }
        OrderStatusAndCount orderStatusAndCount4All = new OrderStatusAndCount("all", total);
        orderStatusAndCounts.add(orderStatusAndCount4All);

        return Result.SUCCESS(orderStatusAndCounts);
    }

    @Data
    public static class OrderParam extends PageParam {

        Long orderId;

        /**
         * ??????????????????-??????
         */
        String start;

        /**
         * ??????????????????-??????
         */
        String end;

        /**
         * ????????????
         */
        Integer payType;

        /**
         * ????????????
         */
        Integer deliveryType;

        /**
         * ????????????
         */
        Integer status;

        /**
         * ??????ID
         */
        Long buyerId;

        /**
         * ????????????
         */
        String buyerName;

        /**
         * ???????????????
         */
        Long buyerMobile;

        /**
         * ??????ID
         */
        Long itemId;

        Integer presell;
    }

    @Data
    public static class OrderStatusAndCount {

        String status;

        Integer count;

        public OrderStatusAndCount(String status, int count) {
            this.status = status;
            this.count = count;
        }

        public static List<OrderStatusAndCount> mock() {
            List<OrderStatusAndCount> list = new ArrayList<>();
            list.add(new OrderStatusAndCount("all", 200));
            list.add(new OrderStatusAndCount("new", 100));
            list.add(new OrderStatusAndCount("payed", 100));
            return list;
        }

        public static OrderStatusAndCount transfer(OrderStatusStatisDTO orderStatusStatisDTO) {
            OrderStatusAndCount orderStatusAndCount = new OrderStatusAndCount(
                    orderStatusStatisDTO.getOrderStatusEnum().getType(), orderStatusStatisDTO.getCount());
            return orderStatusAndCount;
        }
    }

    @Data
    public static class OrderInfo {

        String orderId;

        String createTime;

        Pay pay;

        List<SubOrder> subs;

        BuyerInfo buyer;

        DeliverInfo deliver;

        Integer status;

        String statusDesc;

        List<StatusNode> statusFlow;

        Refund refund;

        String buyerMsg;

        String note;

        /**
         * ?????????????????????
         */
        String total;

        /**
         * ?????????????????????????????????????????????????????????
         */
        List<Activity> activities;

        /**
         * ???????????????????????????
         */
        List<Coupons> coupons;

        /**
         * ??????????????????
         */
        PointsCash pointsCash;

        /**
         * ??????????????????
         */
        String sum;

        public static OrderInfo mock() {
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setOrderId(randomId.nextLong() + "");
            orderInfo.setCreateTime("2019-01-30 19:59");

            orderInfo.setPay(new Pay());
            orderInfo.getPay().setType(0);
            orderInfo.getPay().setPayNo("payNoxxxxxxx");

            orderInfo.setSubs(new ArrayList<>());
            SubOrder subOrder = new SubOrder();
            subOrder.setId("" + randomId.nextLong());
            subOrder.setItemId("itemId11111111111");
            subOrder.setItemTitle("????????????????????????????????????????????????");
            subOrder.setItemImg("9f095912ce790cba76c34e90e5547e40");
            subOrder.setSkuId("skuId" + randomId.nextLong());
            subOrder.setSkuTitle("sku??????sku??????sku??????sku??????sku??????");
            subOrder.setPrice("123.80");
            subOrder.setCount(1);
            subOrder.setTotal("123.80");
            orderInfo.getSubs().add(subOrder);
            SubOrder subOrder2 = new SubOrder();
            subOrder2.setId("" + randomId.nextLong());
            subOrder.setItemId("222222");
            subOrder2.setItemTitle("????????????????????????????????????????????????");
            subOrder2.setItemImg("9f095912ce790cba76c34e90e5547e40");
            subOrder2.setSkuId("2222");
            subOrder2.setSkuTitle("sku??????sku??????sku??????sku??????sku??????");
            subOrder2.setPrice("423.80");
            subOrder2.setCount(1);
            subOrder2.setTotal("423.80");
            orderInfo.getSubs().add(subOrder2);

            orderInfo.getSubs().get(0).setRefund(new Refund());
            orderInfo.getSubs().get(0).getRefund().setCause("????????????????????????????????????????????????????????????");
            orderInfo.getSubs().get(0).getRefund().setType((byte) 0);

            orderInfo.setBuyer(new BuyerInfo());
            orderInfo.getBuyer().setNick("????????????");
            orderInfo.getBuyer().setId("buyerId124");
            orderInfo.getBuyer().setMobile("1234353423");
            orderInfo.getBuyer().setName("buyerName");

            orderInfo.setDeliver(new DeliverInfo());
            orderInfo.getDeliver().setName("???????????????");
            orderInfo.getDeliver().setMobile("12345676534");
            orderInfo.getDeliver().setAddr("addrxxxxxxxxx");
            orderInfo.getDeliver().setDeliverType(0);
            orderInfo.getDeliver().setFreight("0");
            orderInfo.getDeliver().setLogisticsCP("??????");
            orderInfo.getDeliver().setLogisticsNo("lNoxxxxxxx");

            orderInfo.setSum("234.32");
            orderInfo.setTotal("264.32");

            orderInfo.setActivities(new ArrayList<>());
            Activity activity = new Activity();
            activity.setDesc("????????????10???");
            activity.setDiscount("10.00");
            orderInfo.getActivities().add(activity);
            Activity activity2 = new Activity();
            activity2.setDesc("???100???10???");
            activity2.setDiscount("10.00");
            orderInfo.getActivities().add(activity2);

            orderInfo.setCoupons(new ArrayList<>());
            Coupons coupons = new Coupons();
            coupons.setDesc("?????????1");
            coupons.setDiscount("1.00");
            orderInfo.getCoupons().add(coupons);

            orderInfo.setPointsCash(new PointsCash());
            orderInfo.getPointsCash().setUsed(1000);
            orderInfo.getPointsCash().setCash("10.00");

            orderInfo.setStatusDesc("?????????");
            orderInfo.setStatus(10);

            orderInfo.setStatusFlow(new ArrayList<>());
            StatusNode statusNode = new StatusNode();
            statusNode.setStatus(10);
            statusNode.setTime("2019-02-01 10:00:00");
            orderInfo.getStatusFlow().add(statusNode);
            StatusNode statusNode2 = new StatusNode();
            statusNode2.setStatus(20);
            statusNode2.setTime("2019-02-02 10:00:00");
            orderInfo.getStatusFlow().add(statusNode2);

            orderInfo.setBuyerMsg("buyerMsgxxxxxx");
            orderInfo.setNote("notexxxxxx");

            return orderInfo;
        }

        public static OrderInfo transfer(OrderDTO orderDTO) {
            OrderInfo orderInfo = new OrderInfo();
            if (!CollectionUtils.isEmpty(orderDTO.getSubOrders())) {
                orderInfo
                        .setSubs(orderDTO.getSubOrders().stream().map(SubOrder::transfer).collect(Collectors.toList()));
            }
            orderInfo.setSum(BigDecimalUtil.toHalfDownString(orderDTO.getRealFee(), 2));
            orderInfo.setTotal(BigDecimalUtil.toHalfDownString(orderDTO.getTotalFee(), 2));
            orderInfo.setStatus(orderDTO.getStatus().getCode());
            orderInfo.setStatusDesc(orderDTO.getStatus().getDesc());
            orderInfo.setCreateTime(DateFormatUtils.format(orderDTO.getCreatedTime(), DateConstants.format1));
            orderInfo.setOrderId(orderDTO.getId().toString());
            if (!CollectionUtils.isEmpty(orderDTO.getActivity())) {
                orderInfo.setActivities(
                        orderDTO.getActivity().stream().map(Activity::transfer).collect(Collectors.toList()));
            }
            if (!CollectionUtils.isEmpty(orderDTO.getCoupons())) {
                orderInfo
                        .setCoupons(orderDTO.getCoupons().stream().map(Coupons::transfer).collect(Collectors.toList()));
            }
            orderInfo.setBuyer(BuyerInfo.transfer(orderDTO.getBuyer()));
            orderInfo.setBuyerMsg(orderDTO.getBuyerMsg());
            orderInfo.setNote(orderDTO.getNote());
            orderInfo.setDeliver(DeliverInfo.transfer(orderDTO.getOrderDeliver()));
            orderInfo.setPay(Pay.transfer(orderDTO.getPayOrder()));
            orderInfo.setPointsCash(PointsCash.transfer(orderDTO.getPointsCash()));
            if (!CollectionUtils.isEmpty(orderDTO.getStatusFlow())) {
                orderInfo.setStatusFlow(
                        orderDTO.getStatusFlow().stream().map(StatusNode::transfer).collect(Collectors.toList()));
            }
            if (orderDTO.getRefund() != null) {
                orderInfo.setRefund(Refund.transfer(orderDTO.getRefund()));
            }
            return orderInfo;
        }
    }

    @Data
    public static class Refund {

        String createdTime;

        String id;

        /**
         * ?????? 1????????????  2?????????   3??????
         */
        Byte type;

        String typeDesc;

        Byte status;

        String statusDesc;

        /**
         * ????????????
         */
        String cause;

        String applyFee;

        public static Refund transfer(RefundDTO refund) {
            Refund refundVO = new Refund();
            BeanUtils.copyProperties(refund, refundVO);
            refundVO.setId(refund.getId().toString());
            refundVO.setApplyFee(refund.getApplyFee().toString());
            refundVO.setStatusDesc(RefundStatusEnum.getByCode(refund.getStatus().intValue()).getDesc());
            refundVO.setTypeDesc(RefundTypeEnum.getByCode(refund.getType().intValue()).getDesc());
            refundVO.setCreatedTime(DateFormatUtils.format(refund.getCreatedTime(), "yyyy-MM-dd HH:mm"));
            return refundVO;
        }
    }

    @Data
    public static class Pay {

        public String id;

        /**
         * ???????????? 0 wechart; 1 alipay 2??????
         */
        public Integer type;

        public String desc;

        /**
         * ???????????????
         */
        public String payNo;

        /**
         * ????????????
         */
        public String sum;

        public static Pay transfer(PayOrderDTO payOrder) {
            if (null == payOrder) {
                return null;
            }
            Pay pay = new Pay();
            pay.setId(payOrder.getId().toString());
            pay.setPayNo(payOrder.getPayNo());
            if (payOrder.getType() != null) {
                pay.setType(payOrder.getType().getCode());
            }
            pay.setDesc(payOrder.getDesc());
            pay.setSum(BigDecimalUtil.toHalfUpString(payOrder.getPayFee(), 2));
            return pay;
        }
    }

    @Data
    public static class Coupons {

        public String id;

        public String desc;

        /**
         * ?????????????????????
         */
        public String discount;

        /**
         * ????????????????????????????????????
         */
        public String before;

        /**
         * ????????????????????????????????????
         */
        public String after;

        public static Coupons transfer(CouponsDTO couponsDTO) {
            if (null == couponsDTO) {
                return null;
            }
            Coupons coupons = new Coupons();
            BeanUtils.copyProperties(couponsDTO, coupons);
            return coupons;
        }
    }

    /**
     * ????????????
     */
    @Data
    public static class PointsCash {

        /**
         * ????????????????????????
         */
        public Integer used;

        /**
         * ??????????????????
         */
        public Integer balance;

        /**
         * ????????????????????????
         */
        public String cash;

        public static PointsCash transfer(PointsCashDTO pointsCashDTO) {
            if (null == pointsCashDTO) {
                return new PointsCash();
            }
            PointsCash pointsCash = new PointsCash();
            BeanUtils.copyProperties(pointsCashDTO, pointsCash);
            return pointsCash;
        }
    }

    /**
     * ????????????
     */
    @Data
    public static class Activity {

        public Integer id;

        public String desc;

        /**
         * ?????????????????????????????????
         */
        public String meet;

        /**
         * ???????????????????????????????????????
         */
        public String discount;

        /**
         * ????????????????????????????????????
         */
        public String totalDis;

        /**
         * ????????????????????????????????????
         */
        public String before;

        /**
         * ?????????????????????????????????
         */
        public String after;

        public static Activity transfer(ActivityDTO activityDTO) {
            if (null == activityDTO) {
                return null;
            }
            Activity activity = new Activity();
            BeanUtils.copyProperties(activityDTO, activity);
            return activity;
        }
    }

    @Data
    public static class SubOrder {

        public String id;

        public String itemId;

        public String itemTitle;

        public String itemImg;

        public String skuId;

        public String skuTitle;

        /**
         * ?????????
         */
        public String price;

        public Integer count;

        /**
         * ????????????
         */
        public String total;

        public Refund refund;

        public String presell;

        public static SubOrder transfer(SubOrderDTO subOrderDTO) {
            if (null == subOrderDTO) {
                return null;
            }
            SubOrder subOrder = new SubOrder();
            BeanUtils.copyProperties(subOrderDTO, subOrder);
            subOrder.setItemId(subOrderDTO.getItemId().toString());
            subOrder.setSkuId(subOrderDTO.getSkuId().toString());
            subOrder.setCount(subOrderDTO.getCount().intValue());
            subOrder.setItemImg(subOrderDTO.getItemImg());
            subOrder.setPrice(BigDecimalUtil.toHalfDownString(subOrderDTO.getPrice(), 2));
            subOrder.setTotal(BigDecimalUtil.toHalfDownString(subOrderDTO.getTotalFee(), 2));
            return subOrder;
        }
    }

    @Data
    public static class StatusNode {

        public Integer status;

        public String time;

        public static StatusNode transfer(StatusFlowNodeDTO statusFlowNode) {
            if (null == statusFlowNode || null == statusFlowNode.getStatus() || null == statusFlowNode.getTime()) {
                return new StatusNode();
            }
            StatusNode statusNode = new StatusNode();
            statusNode.setStatus(statusFlowNode.getStatus().getCode());
            statusNode.setTime(DateFormatUtils.format(statusFlowNode.getTime(), DateConstants.format1));
            return statusNode;
        }
    }

    /**
     * ????????????
     */
    @Data
    public static class BuyerInfo {

        /**
         * ??????ID
         */
        public String id;

        /**
         * ????????????
         */
        public String nick;

        /**
         * ????????????
         */
        public String name;

        /**
         * ???????????????
         */
        public String mobile;

        public static BuyerInfo transfer(ShopMemberDTO buyerDTO) {
            if (null == buyerDTO) {
                return new BuyerInfo();
            }
            BuyerInfo buyerInfo = new BuyerInfo();
            if (null != buyerDTO.getId()) {
                buyerInfo.setId(buyerDTO.getId().toString());
            }
            if (null != buyerDTO.getMobile()) {
                buyerInfo.setMobile(buyerDTO.getMobile().toString());
            }
            buyerInfo.setNick(buyerDTO.getNick());
            return buyerInfo;
        }
    }

    /**
     * ?????????????????????+??????
     */
    @Data
    public static class DeliverInfo {

        /**
         * ????????????
         */
        public String addr;

        /**
         * ???????????????
         */
        public String name;

        /**
         * ??????????????????
         */
        public String mobile;

        /**
         * ????????????
         */
        public Integer deliverType;

        /**
         * ??????????????????
         */
        public String logisticsCP;

        /**
         * ????????????
         */
        public String logisticsNo;

        /**
         * ??????
         */
        public String freight;

        public static DeliverInfo transfer(OrderDeliverDTO orderDeliverDTO) {
            if (null == orderDeliverDTO || orderDeliverDTO.getType() == null) {
                return new DeliverInfo();
            }
            DeliverInfo deliverInfo = new DeliverInfo();
            BeanUtils.copyProperties(orderDeliverDTO, deliverInfo);
            deliverInfo.setAddr(orderDeliverDTO.getCity() + " " + orderDeliverDTO.getAddress());
            if (orderDeliverDTO.getMobile() != null) {
                deliverInfo.setMobile(orderDeliverDTO.getMobile().toString());
            }
            deliverInfo.setLogisticsCP(orderDeliverDTO.getLogisticsCp());
            deliverInfo.setDeliverType(orderDeliverDTO.getType().getCode());
            if (orderDeliverDTO.getFreight() != null) {
                deliverInfo.setFreight(BigDecimalUtil.toHalfUpString(orderDeliverDTO.getFreight(), 2));
            }
            return deliverInfo;
        }
    }
}
