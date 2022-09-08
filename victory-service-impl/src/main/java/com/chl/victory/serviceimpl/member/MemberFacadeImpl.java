package com.chl.victory.serviceimpl.member;

import java.util.List;
import javax.validation.constraints.NotNull;

import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.member.MemberFacade;
import com.chl.victory.serviceapi.member.model.MemberDeliverDTO;
import com.chl.victory.serviceapi.member.model.ShopMemberDTO;
import com.chl.victory.serviceapi.member.query.MemberDeliverQueryDTO;
import com.chl.victory.serviceapi.member.query.ShopMemberQueryDTO;
import org.apache.dubbo.config.annotation.DubboService;

import static com.chl.victory.service.services.ServiceManager.memberService;

/**
 * @author ChenHailong
 * @date 2020/9/1 17:44
 **/
@DubboService
public class MemberFacadeImpl implements MemberFacade {

    @Override
    public String getMemSummary(@NotNull Long shopId) {
        return memberService.getMemSummary(shopId);
    }

    @Override
    public ServiceResult<List<ShopMemberDTO>> selectMem(ShopMemberQueryDTO query) {
        return memberService.selectMem(query);
    }

    @Override
    public ServiceResult<List<MemberDeliverDTO>> selectMemDeliver(MemberDeliverQueryDTO deliverQuery) {
        return memberService.selectMemDeliver(deliverQuery);
    }

    @Override
    public ServiceResult<Long> saveMemDeliver(MemberDeliverDTO memberDeliverDTO) {
        return memberService.saveMemDeliver(memberDeliverDTO);
    }

    @Override
    public ServiceResult delMemDeliver(List<Long> ids, Long userId) {
        return memberService.delMemDeliver(ids, userId);
    }

    @Override
    public ServiceResult saveNickAndImg(ShopMemberDTO shopMember) {
        return memberService.saveNickAndImg(shopMember);
    }

    @Override
    public ServiceResult save(ShopMemberDTO shopMemberDTO) {
        return memberService.saveMem(shopMemberDTO);
    }
}
