package com.chl.victory.dao.query.member;

import com.chl.victory.dao.query.BaseQuery;
import lombok.Data;

/**
 *
 * @author hailongchen9
 */
@Data
public class MemberDeliverQuery extends BaseQuery {
    /**
     * 店铺会员ID
     * mem_id
     */
    private Long memId;
}