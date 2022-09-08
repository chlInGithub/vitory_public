package com.chl.victory.dao.manager.merchant;

import java.util.Base64;
import java.util.List;
import javax.annotation.Resource;

import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.BaseManager4TkMybatis;
import com.chl.victory.dao.mapper.merchant.ShopAppMapper;
import com.chl.victory.dao.model.merchant.ShopAppDO;
import org.springframework.stereotype.Component;

/**
 * 店铺-app 关系
 * @author ChenHailong
 * @date 2019/4/30 14:30
 **/
@Component
public class ShopAppManager extends BaseManager4TkMybatis {

    @Resource
    ShopAppMapper shopAppMapper;

    public List<ShopAppDO> selectApps(ShopAppDO query) throws DaoManagerException {
        return select(shopAppMapper, query);
    }

    public int count(ShopAppDO query) throws DaoManagerException {
        return count(shopAppMapper, query);
    }

    public int save(ShopAppDO model) throws DaoManagerException {
        ShopAppDO checkForInsert = new ShopAppDO();
        checkForInsert.setShopId(model.getShopId());
        checkForInsert.setAppId(model.getAppId());
        checkForInsert.setThirdType(model.getThirdType());
        int save = save(shopAppMapper, model, checkForInsert, checkForInsert, TableNameEnum.SHOP_APP);
        return save;
    }

    public int del(ShopAppDO query) throws DaoManagerException {
        int del = del(shopAppMapper, query);
        return del;
    }

    /**
     * 微信支付 证书
     * @return
     * @throws DaoManagerException
     */
    public int saveWXPayCert(Long shopId, String appId, byte[] cert) throws DaoManagerException {
        try{
            ShopAppDO shopAppDO = new ShopAppDO();
            shopAppDO.setShopId(shopId);
            shopAppDO.setAppId(appId);
            String encode = Base64.getEncoder().encodeToString(cert);
            shopAppDO.setWxPayCert(encode);
            return shopAppMapper.updateWXPayCert(shopAppDO);
        }catch (Exception e){
            throw new DaoManagerException(e);
        }
    }

    public byte[] selectWXPayCert(Long shopId, String appId) throws DaoManagerException {
        try{
            ShopAppDO shopAppDO = new ShopAppDO();
            shopAppDO.setShopId(shopId);
            shopAppDO.setAppId(appId);
            String payCert = shopAppMapper.selectWXPayCert(shopAppDO);
            return Base64.getDecoder().decode(payCert);
        }catch (Exception e){
            throw new DaoManagerException(e);
        }
    }

    public int existWXPayCert(Long shopId, String appId) throws DaoManagerException {
        try{
            ShopAppDO shopAppDO = new ShopAppDO();
            shopAppDO.setShopId(shopId);
            shopAppDO.setAppId(appId);
            return shopAppMapper.countWXPayCert(shopAppDO);
        }catch (Exception e){
            throw new DaoManagerException(e);
        }
    }

    public void saveStyle(ShopAppDO shopAppDO) throws DaoManagerException {
        try{
            shopAppMapper.updateStyle(shopAppDO);
        }catch (Exception e){
            throw new DaoManagerException(e);
        }
    }

    public String selectStyle(ShopAppDO shopAppDO) throws DaoManagerException {
        try{
            return shopAppMapper.selectStyle(shopAppDO);
        }catch (Exception e){
            throw new DaoManagerException(e);
        }
    }
}
