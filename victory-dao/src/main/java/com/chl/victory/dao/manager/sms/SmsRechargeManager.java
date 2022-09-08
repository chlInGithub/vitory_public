package com.chl.victory.dao.manager.sms;

import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.BaseManager4Mybatis;
import com.chl.victory.dao.mapper.sms.SmsRechargeOrderMapper;
import com.chl.victory.dao.model.sms.SmsRechargeOrderDO;
import com.chl.victory.dao.query.sms.SmsRechargeOrderQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 短信充值数据层访问入口
 * @author hailongchen9
 */
@Component
public class SmsRechargeManager extends BaseManager4Mybatis {
    @Resource
    SmsRechargeOrderMapper smsRechargeOrderMapper;

    /**
     * 短信充值订单基本信息，需要业务service处理响应的订单order
     * @param model
     * @return
     * @throws DaoManagerException
     */
    public int saveSmsRecharge(SmsRechargeOrderDO model) throws DaoManagerException {
        SmsRechargeOrderQuery checkOnlyOne = null;
        SmsRechargeOrderQuery checkNotExist = null;
        if (model.getId() == null){
            if (model.getShopId() == null
                    || model.getOrderId() == null
                    || model.getOperatorId() == null
                    || model.getStatus() == null){
                throw new DaoManagerException("缺少数据，如归属店铺ID、关联订单ID等");
            }
            checkNotExist = new SmsRechargeOrderQuery();
            checkNotExist.setShopId(model.getShopId());
            checkNotExist.setOrderId(model.getOrderId());
        }else {
            checkOnlyOne = new SmsRechargeOrderQuery();
            checkOnlyOne.setId(model.getId());
            checkOnlyOne.setShopId(model.getShopId());
            checkOnlyOne.setOrderId(model.getOrderId());
        }
        return save(smsRechargeOrderMapper, model, checkNotExist, checkOnlyOne, TableNameEnum.SMS_RECHARGE_ORDER);
    }

    public List<SmsRechargeOrderDO> selectSmsRecharges(SmsRechargeOrderQuery query) throws DaoManagerException {
        return select(smsRechargeOrderMapper, query);
    }
    public SmsRechargeOrderDO selectSmsRecharge(SmsRechargeOrderQuery query) throws DaoManagerException {
        return selectOne(smsRechargeOrderMapper, query);
    }
    public int delSmsRecharge(SmsRechargeOrderQuery query) throws DaoManagerException {
        return del(smsRechargeOrderMapper, query);
    }
}
