package com.chl.victory.dao.manager.fee;

import java.util.List;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.chl.victory.common.enums.YesNoEnum;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.BaseManager4TkMybatis;
import com.chl.victory.dao.mapper.fee.FeeRuleMapper;
import com.chl.victory.dao.mapper.fee.OrderFeeMapper;
import com.chl.victory.dao.model.fee.FeeRuleDO;
import com.chl.victory.dao.model.fee.OrderFeeDO;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

/**
 * @author ChenHailong
 * @date 2020/8/6 16:52
 **/
@Component
public class FeeManager extends BaseManager4TkMybatis {
    @Resource
    FeeRuleMapper feeRuleMapper;
    @Resource
    OrderFeeMapper orderFeeMapper;

    public FeeRuleDO getValidUniformRule(@NotNull Long shopId) throws DaoManagerException {
        FeeRuleDO query = new FeeRuleDO();
        query.setShopId(shopId);
        query.setUniform(YesNoEnum.Yes.getCode().byteValue());
        query.setValid(YesNoEnum.Yes.getCode().byteValue());
        FeeRuleDO feeRuleDO = (FeeRuleDO) selectOne(feeRuleMapper, query);
        return feeRuleDO;
    }

    /*public int countUniformRule(@NotNull Long shopId) throws DaoManagerException {
        FeeRuleDO query = new FeeRuleDO();
        query.setShopId(shopId);
        query.setUniform(YesNoEnum.Yes.getCode().byteValue());
        query.setValid(YesNoEnum.Yes.getCode().byteValue());
        int count = count(feeRuleMapper, query);
        return count;
    }*/

    public FeeRuleDO getValidNotUniformRule(@NotNull Long shopId, @NotNull Long itemId) throws DaoManagerException {
        FeeRuleDO query = new FeeRuleDO();
        query.setShopId(shopId);
        query.setItemId(itemId);
        query.setUniform(YesNoEnum.No.getCode().byteValue());
        query.setValid(YesNoEnum.Yes.getCode().byteValue());
        FeeRuleDO feeRuleDO = (FeeRuleDO) selectOne(feeRuleMapper, query);
        return feeRuleDO;
    }

    /*public int countNotUniformRule(@NotNull Long shopId, @NotNull Long itemId) throws DaoManagerException {
        FeeRuleDO query = new FeeRuleDO();
        query.setShopId(shopId);
        query.setItemId(itemId);
        query.setUniform(YesNoEnum.No.getCode().byteValue());
        int count = count(feeRuleMapper, query);
        return count;
    }*/

    public FeeRuleDO getValidNotUniformRule(Long shopId) {
        Example example = new Example(FeeRuleDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("shopId", shopId);
        criteria.andIsNull("itemId");
        criteria.andEqualTo("uniform", YesNoEnum.No.getCode().byteValue());
        criteria.andEqualTo("valid", YesNoEnum.Yes.getCode().byteValue());
        FeeRuleDO feeRuleDO = feeRuleMapper.selectOneByExample(example);
        return feeRuleDO;
    }

    /*public int countNotUniformRule(Long shopId) {
        Example example = new Example(FeeRuleDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("shopId", shopId);
        criteria.andIsNull("itemId");
        criteria.andEqualTo("uniform", YesNoEnum.No.getCode().byteValue());
        int count = feeRuleMapper.selectCountByExample(example);
        return count;
    }*/

    public Integer countOrderFee(@NotNull Long shopId, @NotNull Long orderId, @NotNull Long subOrderId, @NotNull Long itemId)
            throws DaoManagerException {
        OrderFeeDO query = new OrderFeeDO();
        query.setShopId(shopId);
        query.setOrderId(orderId);
        query.setSubId(subOrderId);
        query.setItemId(itemId);
        int count = count(orderFeeMapper, query);
        return count;
    }

    public int saveOrderFee(OrderFeeDO orderFeeDO) throws DaoManagerException {
        OrderFeeDO checkInsert = new OrderFeeDO();
        checkInsert.setShopId(orderFeeDO.getShopId());
        checkInsert.setOrderId(orderFeeDO.getOrderId());
        checkInsert.setSubId(orderFeeDO.getSubId());
        checkInsert.setItemId(orderFeeDO.getItemId());

        OrderFeeDO checkUpdate = new OrderFeeDO();
        checkUpdate.setId(orderFeeDO.getId());
        checkUpdate.setShopId(orderFeeDO.getShopId());
        checkUpdate.setOrderId(orderFeeDO.getOrderId());
        checkUpdate.setSubId(orderFeeDO.getSubId());
        checkUpdate.setItemId(orderFeeDO.getItemId());
        int save = save(orderFeeMapper, orderFeeDO, checkInsert, checkUpdate, TableNameEnum.ORDER_FEE);
        return save;
    }

    public int saveFeeRule(FeeRuleDO feeRuleDO) throws DaoManagerException {
        feeRuleDO.setValid(YesNoEnum.Yes.getCode().byteValue());

        Example example = new Example(FeeRuleDO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("shopId", feeRuleDO.getShopId());
        if (feeRuleDO.getUniform().equals(YesNoEnum.No.getCode().byteValue())) {
            if (feeRuleDO.getItemId() == null) {
                criteria.andIsNull("itemId");
            }
            else {
                criteria.andEqualTo("itemId", feeRuleDO.getItemId());
            }
        }
        criteria.andEqualTo("uniform", feeRuleDO.getUniform());
        criteria.andEqualTo("valid", feeRuleDO.getVal());
        feeRuleMapper.deleteByExample(example);

        FeeRuleDO checkInsert = new FeeRuleDO();
        checkInsert.setShopId(feeRuleDO.getShopId());
        checkInsert.setItemId(feeRuleDO.getItemId());
        checkInsert.setUniform(feeRuleDO.getUniform());
        int save = save(feeRuleMapper, feeRuleDO, checkInsert, null, TableNameEnum.FEE_RULE);
        return save;
    }

    public List<OrderFeeDO> selectOrderFee(OrderFeeDO query) throws DaoManagerException {
        List<OrderFeeDO> list = select(orderFeeMapper, query);
        return list;
    }

}
