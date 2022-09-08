package com.chl.victory.dao.model;

import lombok.Data;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

/**
 * @author liangchen
 */
@Data
public class BaseDO {
    /**
     * record ID,按照一定规则生成
     */
    @Id
    Long id;
    /**
     * record created time
     */
    Date createdTime;
    /**
     * record modified time
     */
    Date modifiedTime;

    /**
     * belong to shop ID
     */
    @NotNull
    @Positive
    Long shopId;

    /**
     * operating user id
     */
    Long operatorId;
}
