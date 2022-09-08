package com.chl.victory.service.services.experience;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * 体验服务，例如是否体验店
 * @author ChenHailong
 * @date 2020/7/29 15:43
 **/
@Service
@ConfigurationProperties("experience.*")
@Setter
public class ExperienceService {
    Long shopId;

    public boolean isExperienceShop(Long shopId) {
        return this.shopId != null && this.shopId.equals(shopId);
    }
}
