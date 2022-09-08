package com.chl.victory.serviceimpl.test.order;

import java.util.Arrays;

import javax.annotation.Resource;

import com.chl.victory.service.services.order.OrderService;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.exception.BusServiceException;
import com.chl.victory.serviceapi.order.enums.DeliverTypeEnum;
import com.chl.victory.serviceapi.order.model.OrderCreateDTO;
import com.chl.victory.serviceimpl.test.BaseTest;
import org.junit.Assert;
import org.junit.Test;

public class OrderServiceTest extends BaseTest {
    @Resource
    OrderService orderService;

    @Test
    public void testDealPresellAfterEnd(){
        orderService.dealPresellAfterEnd();
    }

    
    @Test
    public void test() throws BusServiceException {
        Long refundId = 3420071415135410002L;
        for (int i=0; i< 5; i++) {
            orderService.dealRefundedResult(refundId);
        }
    }

    @Test
    public void testApplyFor3rdPlatFormRefundWithNxLock(){
        Long refundId = 3420071011231710001L;

        for (int i = 0; i < 5; i++) {
            try {
                ServiceResult serviceResult = orderService.applyFor3rdPlatFormRefundWithNxLock(refundId);
                System.out.println(serviceResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void createOrder() throws Exception {
        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setBuyerId(4520191010134645798L);
        orderCreateDTO.setShopId(1120190521153045430L);
        orderCreateDTO.setDeliverId("111");
        orderCreateDTO.setDeliverType(DeliverTypeEnum.noLogistics.getCode());
        orderCreateDTO.setItems(Arrays.asList("2020190522144958478_2120190523140350504_1","2020190522144958478_0_1"));
        orderCreateDTO.setNote("notenotexxxxx");
        orderCreateDTO.setCouId("4720191016175127927");
        orderCreateDTO.setActId("4820191022161341031");
        orderCreateDTO.setReferTotal("84.80");
        ServiceResult serviceResult = orderService.createOrderWithNxLock(orderCreateDTO);
        System.out.println(serviceResult);
        Assert.assertTrue(serviceResult.getSuccess());
    }
}