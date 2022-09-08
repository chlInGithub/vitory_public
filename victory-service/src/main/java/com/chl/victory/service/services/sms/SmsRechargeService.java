package com.chl.victory.service.services.sms;

import java.util.List;
import javax.annotation.Resource;

import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.sms.SmsRechargeManager;
import com.chl.victory.dao.model.order.OrderDO;
import com.chl.victory.dao.model.order.OrderDeliverDO;
import com.chl.victory.dao.model.order.SubOrderDO;
import com.chl.victory.dao.model.sms.SmsRechargeOrderDO;
import com.chl.victory.dao.query.sms.SmsRechargeOrderQuery;
import com.chl.victory.service.services.BaseService;
import com.chl.victory.service.services.order.OrderService;
import com.chl.victory.serviceapi.ServiceFailTypeEnum;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.order.enums.OrderStatusEnum;
import com.chl.victory.serviceapi.order.enums.PayTypeEnum;
import org.springframework.stereotype.Service;

/**
 * @author ChenHailong
 * @date 2019/5/9 10:37
 **/
@Service
public class SmsRechargeService extends BaseService {

    @Resource
    SmsRechargeManager smsRechargeManager;

    @Resource
    OrderService orderService;

    /**
     * 仅支持充值订单创建
     * @param smsRechargeOrderDO
     * @param mainOrder
     * @param deliverDO
     * @param subOrderDOS
     * @return
     */
    public ServiceResult createSmsRecharge(SmsRechargeOrderDO smsRechargeOrderDO, OrderDO mainOrder,
            OrderDeliverDO deliverDO, List<SubOrderDO> subOrderDOS) {
        if (smsRechargeOrderDO.getId() != null || mainOrder.getId() != null) {
            return ServiceResult.fail(ServiceFailTypeEnum.PARAM_INVALID, "不支持更新操作");
        }

        // 先创建订单，后创建充值单
        return transactionTemplate.execute(status -> {
            // TODO 支付方式未定
            ServiceResult serviceResult = orderService
                    .createOrder(mainOrder, deliverDO, subOrderDOS, null, PayTypeEnum.wechart.getCode());
            if (!serviceResult.getSuccess()) {
                return serviceResult;
            }

            try {
                smsRechargeOrderDO.setOrderId(mainOrder.getId());
                smsRechargeManager.saveSmsRecharge(smsRechargeOrderDO);
                return ServiceResult.success();
            } catch (DaoManagerException e) {
                status.setRollbackOnly();
                return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
            }
        });
    }

    public ServiceResult updateSmsRecharge(Long id, Long shopId, OrderStatusEnum orderStatusEnum) {
        SmsRechargeOrderQuery selectQuery = new SmsRechargeOrderQuery();
        selectQuery.setId(id);
        selectQuery.setShopId(shopId);
        SmsRechargeOrderDO hisSmsRechargeOrderDO;
        try {
            hisSmsRechargeOrderDO = smsRechargeManager.selectSmsRecharge(selectQuery);
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
        if (null == hisSmsRechargeOrderDO) {
            return ServiceResult.fail(ServiceFailTypeEnum.BUS_CHECK_BREAK, "不存在待更新记录");
        }

        return transactionTemplate.execute(status -> {
            ServiceResult serviceResult = null;
            try {
                serviceResult = orderService
                        .updateOrder(hisSmsRechargeOrderDO.getOrderId(), shopId, null, orderStatusEnum, null);
            } catch (BusServiceException e) {
                return ServiceResult.fail(ServiceFailTypeEnum.OTHER_EX, e.getMessage());
            }
            if (!serviceResult.getSuccess()) {
                return serviceResult;
            }

            SmsRechargeOrderDO updateDO = new SmsRechargeOrderDO();
            updateDO.setId(id);
            updateDO.setShopId(shopId);
            updateDO.setStatus(orderStatusEnum.getCode().byteValue());
            try {
                smsRechargeManager.saveSmsRecharge(updateDO);
                return ServiceResult.success();
            } catch (DaoManagerException e) {
                status.setRollbackOnly();
                return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
            }
        });
    }

    public ServiceResult<List<SmsRechargeOrderDO>> selectSmsRecharges(SmsRechargeOrderQuery query) {
        try {
            return ServiceResult.success(smsRechargeManager.selectSmsRecharges(query));
        } catch (DaoManagerException e) {
            return ServiceResult.fail(ServiceFailTypeEnum.DAO_EX, trimExMsg(e));
        }
    }
}
