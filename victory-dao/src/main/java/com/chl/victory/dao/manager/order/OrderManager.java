package com.chl.victory.dao.manager.order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;

import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.BaseManager4Mybatis;
import com.chl.victory.dao.mapper.order.OrderDeliverMapper;
import com.chl.victory.dao.mapper.order.OrderMapper;
import com.chl.victory.dao.mapper.order.PayOrderMapper;
import com.chl.victory.dao.mapper.order.RefundOrderMapper;
import com.chl.victory.dao.mapper.order.SubOrderMapper;
import com.chl.victory.dao.model.StatusCountDO;
import com.chl.victory.dao.model.order.OrderDO;
import com.chl.victory.dao.model.order.OrderDeliverDO;
import com.chl.victory.dao.model.order.PayOrderDO;
import com.chl.victory.dao.model.order.RefundOrderDO;
import com.chl.victory.dao.model.order.SubOrderDO;
import com.chl.victory.dao.query.order.OrderDeliverQuery;
import com.chl.victory.dao.query.order.OrderQuery;
import com.chl.victory.dao.query.order.PayOrderQuery;
import com.chl.victory.dao.query.order.RefundOrderQuery;
import com.chl.victory.dao.query.order.SubOrderQuery;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 店铺订单数据层访问入口
 */
@Component
public class OrderManager extends BaseManager4Mybatis {

    @Resource
    OrderMapper orderMapper;

    @Resource
    SubOrderMapper subOrderMapper;

    @Resource
    PayOrderMapper payOrderMapper;

    @Resource
    OrderDeliverMapper orderDeliverMapper;

    @Resource
    RefundOrderMapper refundOrderMapper;

    public int saveOrder(OrderDO model) throws DaoManagerException {
        if (model.getId() == null) {
            if (model.getBuyerId() == null || model.getRealFee() == null || model.getStatus() == null
                    || model.getTotalFee() == null || model.getShopId() == null || model.getOperatorId() == null) {
                throw new DaoManagerException("缺少数据，如买家ID，订单交付ID，支付单ID，状态等");
            }
            return save(orderMapper, model, null, null, TableNameEnum.ORDER);
        }
        else {
            if (model.getShopId() == null) {
                throw new DaoManagerException("缺少数据，如店铺ID");
            }
            OrderQuery checkOnlyOne = new OrderQuery();
            checkOnlyOne.setId(model.getId());
            checkOnlyOne.setShopId(model.getShopId());
            return save(orderMapper, model, null, checkOnlyOne, TableNameEnum.ORDER);
        }
    }

    public List<OrderDO> selectOrders(OrderQuery query) throws DaoManagerException {
        return select(orderMapper, query);
    }

    public OrderDO selectOrder(OrderQuery query) throws DaoManagerException {
        return selectOne(orderMapper, query);
    }

    public int delOrder(OrderQuery query) throws DaoManagerException {
        return del(orderMapper, query);
    }

    public int saveSubOrder(SubOrderDO model) throws DaoManagerException {
        if (model.getId() == null) {
            if (model.getItemId() == null || model.getOrderId() == null || model.getPrice() == null
                    || model.getTotalFee() == null || model.getShopId() == null || model.getOperatorId() == null) {
                throw new DaoManagerException("缺少数据，如商品ID、订单ID、一口价等");
            }
            SubOrderQuery checkNotExist = new SubOrderQuery();
            checkNotExist.setItemId(model.getItemId());
            checkNotExist.setSkuId(model.getSkuId());
            checkNotExist.setOrderId(model.getOrderId());
            checkNotExist.setShopId(model.getShopId());
            return save(subOrderMapper, model, checkNotExist, null, TableNameEnum.SUB_ORDER);
        }
        else {
            if (model.getShopId() == null) {
                throw new DaoManagerException("缺少数据，如店铺ID");
            }
            SubOrderQuery checkOnlyOne = new SubOrderQuery();
            checkOnlyOne.setId(model.getId());
            checkOnlyOne.setShopId(model.getShopId());
            checkOnlyOne.setItemId(model.getItemId());
            checkOnlyOne.setOrderId(model.getOrderId());
            return save(subOrderMapper, model, null, checkOnlyOne, TableNameEnum.SUB_ORDER);
        }
    }

    public List<SubOrderDO> selectSubOrders(SubOrderQuery query) throws DaoManagerException {
        return select(subOrderMapper, query);
    }

    public SubOrderDO selectSubOrder(SubOrderQuery query) throws DaoManagerException {
        return selectOne(subOrderMapper, query);
    }

    public int delSubOrder(SubOrderQuery query) throws DaoManagerException {
        if (query.getId() == null && query.getShopId() == null && query.getOrderId() == null) {
            throw new DaoManagerException("不可同时缺少子订单ID、 主订单ID、店铺ID");
        }
        return del(subOrderMapper, query);
    }

    public int saveOrderDeliver(OrderDeliverDO model) throws DaoManagerException {
        if (model.getId() == null) {
            if (model.getMobile() == null || model.getOrderId() == null
                    /* || model.getShopId() == null*/
                    || model.getOperatorId() == null) {
                throw new DaoManagerException("缺少数据，如订单ID等");
            }
            OrderDeliverQuery checkNotExist = new OrderDeliverQuery();
            checkNotExist.setOrderId(model.getOrderId());
            checkNotExist.setShopId(model.getShopId());
            return save(orderDeliverMapper, model, checkNotExist, null, TableNameEnum.ORDER_DELIVER);
        }
        else {
            /*if (model.getShopId() == null) {
                throw new DaoManagerException("缺少数据，如店铺ID");
            }*/
            OrderDeliverQuery checkOnlyOne = new OrderDeliverQuery();
            checkOnlyOne.setId(model.getId());
            checkOnlyOne.setShopId(model.getShopId());
            checkOnlyOne.setOrderId(model.getOrderId());
            return save(orderDeliverMapper, model, null, checkOnlyOne, TableNameEnum.ORDER_DELIVER);
        }
    }

    public List<OrderDeliverDO> selectOrderDelivers(OrderDeliverQuery query) throws DaoManagerException {
        return select(orderDeliverMapper, query);
    }

    public OrderDeliverDO selectOrderDeliver(OrderDeliverQuery query) throws DaoManagerException {
        return selectOne(orderDeliverMapper, query);
    }

    public int delOrderDeliver(OrderDeliverQuery query) throws DaoManagerException {
        if (query.getId() == null && query.getOrderId() == null && query.getShopId() == null) {
            throw new DaoManagerException("不可同时缺少Id、主订单ID、店铺ID");
        }
        return del(orderDeliverMapper, query);
    }

    public int savePayOrder(PayOrderDO model) throws DaoManagerException {
        if (model.getId() == null) {
            if (/*model.getPayNo() == null
                || */model.getOrderId() == null || model.getStatus() == null || model.getShopId() == null
                    || model.getOperatorId() == null) {
                throw new DaoManagerException("缺少数据，如订单ID等");
            }
            PayOrderQuery checkNotExist = new PayOrderQuery();
            checkNotExist.setOrderId(model.getOrderId());
            checkNotExist.setShopId(model.getShopId());
            return save(payOrderMapper, model, checkNotExist, null, TableNameEnum.PAY_ORDER);
        }
        else {
            if (model.getShopId() == null) {
                throw new DaoManagerException("缺少数据，如店铺ID");
            }
            PayOrderQuery checkOnlyOne = new PayOrderQuery();
            checkOnlyOne.setId(model.getId());
            checkOnlyOne.setShopId(model.getShopId());
            checkOnlyOne.setOrderId(model.getOrderId());
            return save(payOrderMapper, model, null, checkOnlyOne, TableNameEnum.PAY_ORDER);
        }
    }

    public List<PayOrderDO> selectPayOrders(PayOrderQuery query) throws DaoManagerException {
        return select(payOrderMapper, query);
    }

    public PayOrderDO selectPayOrder(PayOrderQuery query) throws DaoManagerException {
        return selectOne(payOrderMapper, query);
    }

    public int delPayOrder(PayOrderQuery query) throws DaoManagerException {
        return del(payOrderMapper, query);
    }

    public int saveRefundOrder(RefundOrderDO model) throws DaoManagerException {
        if (model.getId() == null) {
            if (model.getOrderId() == null || model.getStatus() == null || model.getShopId() == null
                    || model.getOperatorId() == null) {
                throw new DaoManagerException("缺少数据，如订单ID等");
            }
            RefundOrderQuery checkNotExist = new RefundOrderQuery();
            checkNotExist.setOrderId(model.getOrderId());
            checkNotExist.setShopId(model.getShopId());
            return save(refundOrderMapper, model, checkNotExist, null, TableNameEnum.REFUND_ORDER);
        }
        else {
            if (model.getShopId() == null) {
                throw new DaoManagerException("缺少数据，如店铺ID");
            }
            RefundOrderQuery checkOnlyOne = new RefundOrderQuery();
            checkOnlyOne.setId(model.getId());
            checkOnlyOne.setShopId(model.getShopId());
            checkOnlyOne.setOrderId(model.getOrderId());
            return save(refundOrderMapper, model, null, checkOnlyOne, TableNameEnum.REFUND_ORDER);
        }
    }

    public List<RefundOrderDO> selectRefundOrders(RefundOrderQuery query) throws DaoManagerException {
        return select(refundOrderMapper, query);
    }

    public RefundOrderDO selectRefundOrder(RefundOrderQuery query) throws DaoManagerException {
        return selectOne(refundOrderMapper, query);
    }

    public int delRefundOrder(RefundOrderQuery query) throws DaoManagerException {
        return del(refundOrderMapper, query);
    }

    public Integer countOrder(OrderQuery orderQuery) throws DaoManagerException {
        return count(orderMapper, orderQuery);
    }
    public Integer countPayOrder(PayOrderQuery orderQuery) throws DaoManagerException {
        return count(payOrderMapper, orderQuery);
    }
    public Integer countDeliverOrder(OrderDeliverQuery orderQuery) throws DaoManagerException {
        return count(orderDeliverMapper, orderQuery);
    }

    public List<StatusCountDO> countStatus(OrderQuery query) {
        return countStatus(orderMapper, query);
    }

    public BigDecimal selectSaleTotal(OrderQuery orderQuery) {
        return orderMapper.saleTotal(orderQuery);
    }

    public Integer countMem(OrderQuery orderQuery) {
        return orderMapper.countMem(orderQuery);
    }

    public Integer countItem(SubOrderQuery orderQuery) {
        return subOrderMapper.countItem(orderQuery);
    }

    public List<OrderDO> closeTimeoutNotPayedOrder4OfflinePay(Date date) {
        List<OrderDO> orderDOS = orderMapper.selectNeedCloseTimeoutNotPayedOrder4OfflinePay(date);
        if (!CollectionUtils.isEmpty(orderDOS)) {
            orderMapper.closeTimeoutNotPayedOrder4OfflinePay(date);
        }
        return orderDOS;
    }
    public List<OrderDO> closeTimeoutNotPayedOrder4OnlinePay(Date date) {
        List<OrderDO> orderDOS = orderMapper.selectNeedCloseTimeoutNotPayedOrder4OnlinePay(date);
        if (!CollectionUtils.isEmpty(orderDOS)) {
            orderMapper.closeTimeoutNotPayedOrder4OnlinePay(date);
        }
        return orderDOS;
    }

    public List<Long> selectRefundIds(RefundOrderQuery refundOrderQuery) {
        return refundOrderMapper.selectRefundIds(refundOrderQuery);
    }
}
