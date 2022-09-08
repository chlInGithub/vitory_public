package com.chl.victory.serviceapi;

import java.io.Serializable;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2019/6/5 14:52
 **/
@Data
public class StatusCountDTO  implements Serializable {
    Integer status;
    Integer count;
}
