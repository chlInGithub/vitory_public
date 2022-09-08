package com.chl.victory.task;

import java.util.List;
import javax.annotation.PostConstruct;

import com.chl.victory.serviceapi.order.enums.RefundStatusEnum;
import com.chl.victory.serviceapi.order.query.RefundOrderQueryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import static com.chl.victory.webcommon.manager.RpcManager.orderFacade;

/**
 * @author ChenHailong
 * @date 2020/1/8 9:34
 **/
@Component
@Slf4j
public class OrderTask {

    @PostConstruct
    public void postConstructTest() {}

    /**
     * 每5分钟，检查待付款是否超时
     */
    @Scheduled(cron = "0 0/5 * * * *")
    public void closeTimeoutNotPayedOrder() {
        orderFacade.closeTimeoutNotPayedOrder4OnlinePay(10);
        orderFacade.closeTimeoutNotPayedOrder4OfflinePay(6 * 60);
    }

    /**
     * 每5分钟，检查退款中的订单，向第三方支付平台发起退款申请
     */
    @Scheduled(cron = "0 0/5 * * * *")
    public void refund() {
        List<Long> refundOrderIdS;
        RefundOrderQueryDTO refundOrderQuery = new RefundOrderQueryDTO();
        refundOrderQuery.setStatus(RefundStatusEnum.agree.getCode().byteValue());
        refundOrderIdS = orderFacade.selectRefundIds(refundOrderQuery);
        if (!CollectionUtils.isEmpty(refundOrderIdS)) {
            for (Long refundId : refundOrderIdS) {
                try {
                    orderFacade.applyFor3rdPlatFormRefundWithNxLock(refundId);
                } catch (Exception e) {
                    log.error("applyFor3rdPlatFormRefundWithNxLock{}", refundId, e);
                }
            }
        }
    }

    /**
     * 每5分钟，检查已向第三方支付平台发起退款申请的退款单，查询退款结果，处理
     */
    @Scheduled(cron = "0 0/5 * * * *")
    public void dealRefundResult() {
        List<Long> refundOrderIdS;
        RefundOrderQueryDTO refundOrderQuery = new RefundOrderQueryDTO();
        refundOrderQuery.setStatus(RefundStatusEnum.applied_for_3th_pay_platform.getCode().byteValue());
        refundOrderIdS = orderFacade.selectRefundIds(refundOrderQuery);
        if (!CollectionUtils.isEmpty(refundOrderIdS)) {
            for (Long refundId : refundOrderIdS) {
                try {
                    orderFacade.dealRefundedResult(refundId);
                } catch (Exception e) {
                    log.error("dealRefundedResult{}", refundId, e);
                }
            }
        }
    }

    /**
     * 预售截止处理：商品销售数量未达标，则退款
     */
    @Scheduled(cron = "0 30 0 * * *")
    public void dealPresellAfterEnd() {
        orderFacade.dealPresellAfterEnd();
    }
}
