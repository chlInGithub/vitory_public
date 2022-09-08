package com.chl.victory.serviceapi.member.query;

import java.io.Serializable;

import com.chl.victory.serviceapi.BaseQuery;
import lombok.Data;

/**
 *
 * @author hailongchen9
 */
@Data
public class MemberDeliverQueryDTO extends BaseQuery implements Serializable {
    /**
     * 店铺会员ID
     * mem_id
     */
    private Long memId;
}