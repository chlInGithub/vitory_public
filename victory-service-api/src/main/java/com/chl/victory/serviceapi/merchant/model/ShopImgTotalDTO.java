package com.chl.victory.serviceapi.merchant.model;

import java.io.Serializable;

import com.chl.victory.serviceapi.BaseDTO;
import lombok.Data;

@Data
public class ShopImgTotalDTO extends BaseDTO implements Serializable {
    /**
     * 图片大小，单位byte
     * size
     */
    private Long size;
}