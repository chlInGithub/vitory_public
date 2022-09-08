package com.chl.victory.serviceapi;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/5/28 15:05
 **/
@Data
public class BaseDTO implements Serializable {

    /**
     * record ID,按照一定规则生成
     */
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
    Long shopId;

    /**
     * operating user id
     */
    Long operatorId;
}
