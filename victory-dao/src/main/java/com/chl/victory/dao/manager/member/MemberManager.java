package com.chl.victory.dao.manager.member;

import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.BaseManager4Mybatis;
import com.chl.victory.dao.mapper.member.MemberDeliverMapper;
import com.chl.victory.dao.mapper.member.ShopMemberMapper;
import com.chl.victory.dao.model.member.MemberDeliverDO;
import com.chl.victory.dao.model.member.ShopMemberDO;
import com.chl.victory.dao.query.member.MemberDeliverQuery;
import com.chl.victory.dao.query.member.ShopMemberQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 会员数据访问入口
 */
@Component
public class MemberManager extends BaseManager4Mybatis {
    @Resource
    MemberDeliverMapper memberDeliverMapper;
    @Resource
    ShopMemberMapper shopMemberMapper;

    public int saveDeliver(MemberDeliverDO model) throws DaoManagerException {
        if (model.getId() == null){
            if (model.getMobile() == null){
                throw new DaoManagerException("缺少数据，如手机号等");
            }
            return save(memberDeliverMapper, model, null, null, TableNameEnum.MEMBER_DELIVER);
        }else {
            if ( model.getShopId() == null){
                throw new DaoManagerException("缺少数据，如店铺ID");
            }
            MemberDeliverQuery checkOnlyOne = new MemberDeliverQuery();
            checkOnlyOne.setId(model.getId());
            checkOnlyOne.setShopId(model.getShopId());
            return save(memberDeliverMapper, model, null, checkOnlyOne, TableNameEnum.MEMBER_DELIVER);
        }
    }

    public List<MemberDeliverDO> selectDelivers(MemberDeliverQuery query) throws DaoManagerException {
        return select(memberDeliverMapper, query);
    }
    public MemberDeliverDO selectDeliver(MemberDeliverQuery query) throws DaoManagerException {
        return selectOne(memberDeliverMapper, query);
    }
    public int delDeliver(MemberDeliverQuery query) throws DaoManagerException {
        return del(memberDeliverMapper, query);
    }


    public int saveMember(ShopMemberDO model) throws DaoManagerException {
        if (model.getId() == null){
            if ((model.getMobile() == null && model.getOpenId() == null)
                    || model.getShopId() == null
            ){
                throw new DaoManagerException("缺少数据，如手机号、店铺ID等");
            }
            ShopMemberQuery checkNotExist = new ShopMemberQuery();
            checkNotExist.setShopId(model.getShopId());
            //checkNotExist.setMobile(model.getMobile());
            checkNotExist.setThirdId(model.getThirdId());
            checkNotExist.setOpenId(model.getOpenId());
            return save(shopMemberMapper, model, checkNotExist, null, TableNameEnum.SHOP_MEMBER);
        }else {
            if ( model.getShopId() == null){
                throw new DaoManagerException("缺少数据，如店铺ID");
            }
            ShopMemberQuery checkOnlyOne = new ShopMemberQuery();
            //checkOnlyOne.setId(model.getId());
            checkOnlyOne.setShopId(model.getShopId());
            checkOnlyOne.setThirdId(model.getThirdId());
            checkOnlyOne.setOpenId(model.getOpenId());
            //checkOnlyOne.setMobile(model.getMobile());
            return save(shopMemberMapper, model, null, checkOnlyOne, TableNameEnum.SHOP_MEMBER);
        }
    }

    public List<ShopMemberDO> selectMembers(ShopMemberQuery query) throws DaoManagerException {
        return select(shopMemberMapper, query);
    }
    public ShopMemberDO selectMember(ShopMemberQuery query) throws DaoManagerException {
        return selectOne(shopMemberMapper, query);
    }
    public int delMember(ShopMemberQuery query) throws DaoManagerException {
        return del(shopMemberMapper, query);
    }
    public int countMember(ShopMemberQuery query) throws DaoManagerException {
        return count(shopMemberMapper, query);
    }
}
