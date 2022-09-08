package com.chl.victory.localservice.manager;

import java.lang.reflect.Field;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.chl.victory.localservice.CartLocalService;
import com.chl.victory.localservice.DeliverLocalService;
import com.chl.victory.localservice.FoundryMiniProgramLocalService;
import com.chl.victory.localservice.InfoLocalService;
import com.chl.victory.localservice.ItemLocalService;
import com.chl.victory.localservice.OrderLocalService;
import com.chl.victory.localservice.SaleStrategyLocalService;
import com.chl.victory.localservice.ShopActivityLocalService;
import com.chl.victory.localservice.ShopCouponsLocalService;
import com.chl.victory.localservice.SmartCodeLocalService;
import com.chl.victory.localservice.SmartCodePowerLocalService;
import com.chl.victory.localservice.TemplateIdLocalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author ChenHailong
 * @date 2020/8/24 16:02
 **/
@Component
@Slf4j
public class LocalServiceManager {

    public static CartLocalService cartLocalService;

    public static DeliverLocalService deliverLocalService;

    public static OrderLocalService orderLocalService;

    public static ShopActivityLocalService shopActivityLocalService;

    public static ShopCouponsLocalService shopCouponsLocalService;

    public static InfoLocalService infoLocalService;
    public static ItemLocalService itemLocalService;

    public static FoundryMiniProgramLocalService foundryMiniProgramLocalService;

    public static SaleStrategyLocalService saleStrategyLocalService;

    public static SmartCodeLocalService smartCodeLocalService;

    public static SmartCodePowerLocalService smartCodePowerLocalService;

    public static TemplateIdLocalService templateIdLocalService;

    @Resource
    TemplateIdLocalService _templateIdLocalService;

    @Resource
    SmartCodePowerLocalService _smartCodePowerLocalService;

    @Resource
    FoundryMiniProgramLocalService _foundryMiniProgramLocalService;

    @Resource
    SmartCodeLocalService _smartCodeLocalService;

    @Resource
    SaleStrategyLocalService _saleStrategyLocalService;

    @Resource
    ItemLocalService _itemLocalService;
    @Resource
    InfoLocalService _infoLocalService;

    @Resource
    CartLocalService _cartLocalService;

    @Resource
    DeliverLocalService _deliverLocalService;

    @Resource
    OrderLocalService _orderLocalService;

    @Resource
    ShopActivityLocalService _shopActivityLocalService;

    @Resource
    ShopCouponsLocalService _shopCouponsLocalService;

    @PostConstruct
    public void init() {
        try {
            Field[] fields = getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                String fieldname = fields[ i ].getName();
                if (fieldname.startsWith("_")) {
                    String sfieldname = fieldname.substring(1);
                    Field sfield = getClass().getDeclaredField(sfieldname);
                    sfield.setAccessible(true);
                    sfield.set(this, fields[ i ].get(this));
                }
            }
        } catch (IllegalArgumentException e) {
            log.error("", e);
        } catch (NoSuchFieldException e) {
            log.error("", e);
        } catch (SecurityException e) {
            log.error("", e);
        } catch (IllegalAccessException e) {
            log.error("", e);
        }
    }
}
