package com.chl.victory.serviceapi.member;

import java.util.List;
import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.member.model.MemberDeliverDTO;
import com.chl.victory.serviceapi.member.model.ShopMemberDTO;
import com.chl.victory.serviceapi.member.query.MemberDeliverQueryDTO;
import com.chl.victory.serviceapi.member.query.ShopMemberQueryDTO;

/**
 * @author ChenHailong
 * @date 2020/8/24 17:01
 **/
public interface MemberFacade {

    String getMemSummary(@NotNull Long shopId);

    ServiceResult<List<ShopMemberDTO>> selectMem(ShopMemberQueryDTO query);

    ServiceResult<List<MemberDeliverDTO>> selectMemDeliver(MemberDeliverQueryDTO deliverQuery);

    ServiceResult<Long> saveMemDeliver(MemberDeliverDTO memberDeliverDO);

    ServiceResult delMemDeliver(List<Long> ids, Long userId);

    ServiceResult saveNickAndImg(ShopMemberDTO shopMember);

    ServiceResult save(ShopMemberDTO shopMemberDO);
}
