package com.chl.victory.serviceimpl.test.dao.member;

import com.chl.victory.dao.manager.member.MemberManager;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.model.member.MemberDeliverDO;
import com.chl.victory.dao.model.member.ShopMemberDO;
import com.chl.victory.dao.query.member.MemberDeliverQuery;
import com.chl.victory.dao.query.member.ShopMemberQuery;
import com.chl.victory.serviceimpl.test.BaseTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.annotation.Resource;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MemberManagerTest extends BaseTest {
    @Resource
    MemberManager memberManager;

    @Test
    public void test01_saveDeliver() throws DaoManagerException {
        MemberDeliverDO memberDeliverDO = new MemberDeliverDO();
        memberDeliverDO.setAddr("收货地址");
        memberDeliverDO.setMemId(1L);
        memberDeliverDO.setMobile(12323452345L);
        memberDeliverDO.setName("收件人");
        Assert.assertEquals(1, memberManager.saveDeliver(memberDeliverDO));

        MemberDeliverQuery memberDeliverQuery = new MemberDeliverQuery();
        memberDeliverQuery.setMemId(1L);
        Assert.assertTrue(memberManager.selectDelivers(memberDeliverQuery).size() > 0);

        memberDeliverQuery.setId(memberDeliverDO.getId());
        Assert.assertEquals(1, memberManager.delDeliver(memberDeliverQuery));
    }


    @Test
    public void test04_saveMember() throws DaoManagerException {
        ShopMemberDO model = new ShopMemberDO();
        model.setNick("nick");
        model.setShopId(1120190521153045430L);
        model.setMobile(12323452345L);
        Assert.assertEquals(1, memberManager.saveMember(model));

        ShopMemberQuery query = new ShopMemberQuery();
        query.setShopId(1120190521153045430L);
        Assert.assertTrue(memberManager.selectMembers(query).size() > 0);

        query.setId(model.getId());
        Assert.assertEquals(1, memberManager.delMember(query));
    }
}