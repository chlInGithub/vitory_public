package com.chl.victory.serviceapi.weixin.model;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/8/25 15:22
 **/
@Data
public class InfoAndEventDTO  implements Serializable {
    @NotEmpty String msgSignature;
    @NotEmpty String timestamp;
    @NotEmpty String nonce;
    @NotEmpty String postdata;
}
