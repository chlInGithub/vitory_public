package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.baseinfo;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/27 16:39
 **/
@Data
public class SetNickNameDTO  implements Serializable {
    @NotNull
    String nick_name;
    String naming_other_stuff_1;
    String naming_other_stuff_2;
    String naming_other_stuff_3;
    String naming_other_stuff_4;
    String naming_other_stuff_5;
}
