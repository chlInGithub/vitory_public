package com.chl.victory.serviceimpl.test.dao.order;

import com.chl.victory.dao.manager.order.OrderManager;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.model.order.OrderDO;
import com.chl.victory.dao.model.order.OrderDeliverDO;
import com.chl.victory.dao.model.order.PayOrderDO;
import com.chl.victory.dao.model.order.SubOrderDO;
import com.chl.victory.dao.query.order.OrderDeliverQuery;
import com.chl.victory.dao.query.order.OrderQuery;
import com.chl.victory.dao.query.order.PayOrderQuery;
import com.chl.victory.dao.query.order.SubOrderQuery;
import com.chl.victory.serviceimpl.test.BaseTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OrderManagerTest extends BaseTest {
    @Resource
    OrderManager orderManager;

    static Long orderId;
    static Long orderDeliverId;
    static Long payOrderId;
    static List<Long> subOrderIds = new ArrayList<>();
    static int subOrderNum;

    @Test
    public void test01_saveOrder() throws DaoManagerException {
        OrderDO orderDO = new OrderDO();
        orderDO.setCreatedTime(new Date());
        orderDO.setBuyerId(101L);
        orderDO.setRealFee(new BigDecimal("200"));
        orderDO.setStatus((byte)10);
        orderDO.setTotalFee(new BigDecimal("234"));
        orderDO.setShopId(1120190521153045430L);
        orderDO.setOperatorId(1L);
        orderDO.setShopService(false);
        int result = orderManager.saveOrder(orderDO);
        Assert.assertEquals(1, result);
        orderId = orderDO.getId();

        OrderDeliverDO orderDeliverDO = new OrderDeliverDO();
        orderDeliverDO.setOrderId(orderId);
        orderDeliverDO.setMobile(12323421232L);
        orderDeliverDO.setShopId(1120190521153045430L);
        orderDeliverDO.setOperatorId(1L);
        orderDeliverDO.setType((byte)0);
        result = orderManager.saveOrderDeliver(orderDeliverDO);
        Assert.assertEquals(1, result);
        orderDeliverId = orderDeliverDO.getId();

        PayOrderDO payOrderDO = new PayOrderDO();
        payOrderDO.setOrderId(orderId);
        payOrderDO.setStatus(true);
        payOrderDO.setCheck(true);
        payOrderDO.setType((byte)1);
        payOrderDO.setShopId(1120190521153045430L);
        payOrderDO.setOperatorId(1L);
        payOrderDO.setPayFee(new BigDecimal("200"));
        payOrderDO.setPayNo("payNo");
        result = orderManager.savePayOrder(payOrderDO);
        Assert.assertEquals(1, result);
        payOrderId = payOrderDO.getId();

        subOrderNum = 4;
        for (int i = 0; i < subOrderNum; i++) {
            SubOrderDO subOrderDO = new SubOrderDO();
            subOrderDO.setOrderId(orderId);
            subOrderDO.setShopId(1120190521153045430L);
            subOrderDO.setItemId(1L+i);
            subOrderDO.setPrice(new BigDecimal(100+i));
            subOrderDO.setCount((byte)1);
            subOrderDO.setOperatorId(1L);
            subOrderDO.setTotalFee(subOrderDO.getPrice().multiply(new BigDecimal(subOrderDO.getCount())).setScale(2));
            result = orderManager.saveSubOrder(subOrderDO);
            Assert.assertEquals(1, result);
            subOrderIds.add(subOrderDO.getId());
        }

        orderDO.setOrderDeliverId(orderDeliverId);
        orderDO.setPayId(payOrderId);
        result = orderManager.saveOrder(orderDO);
        Assert.assertEquals(1, result);

    }

    @Test
    public void test02_upateOrder() throws DaoManagerException {
        OrderDO orderDO = new OrderDO();
        orderDO.setShopId(1L);
        orderDO.setStatus((byte)20);
        orderDO.setId(orderId);
        int result = orderManager.saveOrder(orderDO);
        Assert.assertEquals(1, result);
    }


    @Test
    public void test03_selectOrders() throws DaoManagerException {
        OrderQuery orderQuery = new OrderQuery();
        orderQuery.setId(orderId);
        orderQuery.setShopId(1L);
        List<OrderDO> orderDOS = orderManager.selectOrders(orderQuery);
        Assert.assertNotNull(orderDOS);
        Assert.assertTrue(orderDOS.size() > 0);

        SubOrderQuery subOrderQuery = new SubOrderQuery();
        subOrderQuery.setOrderId(orderId);
        subOrderQuery.setShopId(1L);
        List<SubOrderDO> subOrderDOS = orderManager.selectSubOrders(subOrderQuery);
        Assert.assertNotNull(subOrderDOS);
        Assert.assertTrue(subOrderDOS.size() > 0);
        Assert.assertEquals(subOrderNum, subOrderDOS.size());

        PayOrderQuery payOrderQuery = new PayOrderQuery();
        payOrderQuery.setShopId(1L);
        payOrderQuery.setOrderId(orderId);
        PayOrderDO payOrderDO = orderManager.selectPayOrder(payOrderQuery);
        Assert.assertNotNull(payOrderDO);
        Assert.assertEquals(payOrderId, payOrderDO.getId());

        OrderDeliverQuery orderDeliverQuery = new OrderDeliverQuery();
        orderDeliverQuery.setShopId(1L);
        orderDeliverQuery.setOrderId(orderId);
        OrderDeliverDO orderDeliverDO = orderManager.selectOrderDeliver(orderDeliverQuery);
        Assert.assertNotNull(orderDeliverDO);
        Assert.assertEquals(orderDeliverId, orderDeliverDO.getId());
    }

    @Test
    public void test04_delOrder() throws DaoManagerException {
        OrderDeliverQuery orderDeliverQuery = new OrderDeliverQuery();
        orderDeliverQuery.setShopId(1L);
        orderDeliverQuery.setOrderId(orderId);
        orderDeliverQuery.setId(orderDeliverId);
        int result = orderManager.delOrderDeliver(orderDeliverQuery);
        Assert.assertEquals(1, result);

        PayOrderQuery payOrderQuery = new PayOrderQuery();
        payOrderQuery.setShopId(1L);
        payOrderQuery.setOrderId(orderId);
        payOrderQuery.setId(payOrderId);
        result = orderManager.delPayOrder(payOrderQuery);
        Assert.assertEquals(1, result);

        SubOrderQuery subOrderQuery = new SubOrderQuery();
        subOrderQuery.setOrderId(orderId);
        subOrderQuery.setShopId(1L);
        List<SubOrderDO> subOrderDOS = orderManager.selectSubOrders(subOrderQuery);
        Assert.assertNotNull(subOrderDOS);
        Assert.assertTrue(subOrderDOS.size() > 0);
        Assert.assertEquals(subOrderNum, subOrderDOS.size());
        for (SubOrderDO subOrderDO : subOrderDOS) {
            subOrderQuery.setId(subOrderDO.getId());
            result = orderManager.delSubOrder(subOrderQuery);
            Assert.assertEquals(1, result);
        }

        OrderQuery orderQuery = new OrderQuery();
        orderQuery.setId(orderId);
        orderQuery.setShopId(1L);
        orderQuery.setId(orderId);
        result = orderManager.delOrder(orderQuery);
        Assert.assertEquals(1, result);
    }
}