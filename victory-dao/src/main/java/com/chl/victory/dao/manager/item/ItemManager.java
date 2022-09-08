package com.chl.victory.dao.manager.item;

import java.util.List;
import javax.annotation.Resource;

import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.manager.BaseManager4Mybatis;
import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.mapper.item.CategoryMapper;
import com.chl.victory.dao.mapper.item.ItemMapper;
import com.chl.victory.dao.mapper.item.SkuMapper;
import com.chl.victory.dao.model.item.CategoryDO;
import com.chl.victory.dao.model.item.ItemDO;
import com.chl.victory.dao.model.item.SkuDO;
import com.chl.victory.dao.query.BaseQuery;
import com.chl.victory.dao.query.item.CategoryQuery;
import com.chl.victory.dao.query.item.ItemQuery;
import com.chl.victory.dao.query.item.SkuQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 店铺商品数据层访问入口
 * @author hailongchen9
 */
@Component
public class ItemManager extends BaseManager4Mybatis {

    @Resource
    ItemMapper itemMapper;

    @Resource
    SkuMapper skuMapper;

    @Resource
    CategoryMapper categoryMapper;

    /**
     * 保存item基本信息
     * @param model
     * @return
     * @throws DaoManagerException
     */
    public int saveItem(ItemDO model) throws DaoManagerException {
        if (model.getId() == null) {
            if (model.getLeafCateId() == null || model.getPrice() == null || model.getTagPrice() == null
                    || model.getStatus() == null || model.getShopId() == null || StringUtils.isEmpty(model.getTitle())
                    || StringUtils.isEmpty(model.getLogistics())) {
                throw new DaoManagerException("缺少数据，如标题、归属店铺ID、一口价、类目、状态等");
            }

            ItemQuery checkNotExist = new ItemQuery();
            checkNotExist.setShopId(model.getShopId());
            checkNotExist.setTitle(model.getTitle());
            return save(itemMapper, model, checkNotExist, null, TableNameEnum.ITEM);
        }
        else {
            if (model.getShopId() == null || model.getId() == null) {
                throw new DaoManagerException("缺少数据，如归属店铺ID等");
            }
            ItemQuery checkOnlyOne = new ItemQuery();
            checkOnlyOne.setId(model.getId());
            checkOnlyOne.setShopId(model.getShopId());
            return save(itemMapper, model, null, checkOnlyOne, TableNameEnum.ITEM);
        }
    }

    public List<ItemDO> selectItems(ItemQuery query) throws DaoManagerException {
        return select(itemMapper, query);
    }

    /**
     * @see BaseManager4Mybatis#selectOne(BaseMapper, BaseQuery)
     */
    public ItemDO selectItem(ItemQuery query) throws DaoManagerException {
        return selectOne(itemMapper, query);
    }

    public int delItem(ItemQuery query) throws DaoManagerException {
        if (query.getId() == null && query.getShopId() == null) {
            throw new DaoManagerException("不可同时缺少ID和归属店铺");
        }
        return del(itemMapper, query);
    }

    public int saveSku(SkuDO model) throws DaoManagerException {
        if (model.getId() == null) {
            if (model.getItemId() == null || model.getPrice() == null || model.getShopId() == null || StringUtils
                    .isEmpty(model.getTitle())) {
                throw new DaoManagerException("缺少数据，如标题、归属店铺ID、一口价等");
            }

            SkuQuery checkNotExist = new SkuQuery();
            checkNotExist.setShopId(model.getShopId());
            checkNotExist.setItemId(model.getItemId());
            checkNotExist.setTitle(model.getTitle());
            return save(skuMapper, model, checkNotExist, null, TableNameEnum.SKU);
        }
        else {
            if (model.getShopId() == null || model.getId() == null) {
                throw new DaoManagerException("缺少数据，如归属店铺ID等");
            }
            SkuQuery checkOnlyOne = new SkuQuery();
            checkOnlyOne.setId(model.getId());
            checkOnlyOne.setShopId(model.getShopId());
            return save(skuMapper, model, null, checkOnlyOne, TableNameEnum.SKU);
        }
    }

    public List<SkuDO> selectSkus(SkuQuery query) throws DaoManagerException {
        return select(skuMapper, query);
    }

    /**
     * @see BaseManager4Mybatis#selectOne(BaseMapper, BaseQuery)
     */
    public SkuDO selectSku(SkuQuery query) throws DaoManagerException {
        return selectOne(skuMapper, query);
    }

    public int delSku(SkuQuery query) throws DaoManagerException {
        if (query.getId() == null && query.getItemId() == null) {
            throw new DaoManagerException("不可同时缺少ID和itemId");
        }
        return del(skuMapper, query);
    }

    public int saveCategory(CategoryDO model) throws DaoManagerException {
        if (model.getId() == null) {
            if (model.getLevel() == null || model.getShopId() == null || StringUtils.isEmpty(model.getName())) {
                throw new DaoManagerException("缺少数据，如标题、归属店铺ID、级别等");
            }

            CategoryQuery checkNotExist = new CategoryQuery();
            checkNotExist.setShopId(model.getShopId());
            checkNotExist.setName(model.getName());
            checkNotExist.setLevel(model.getLevel());
            return save(categoryMapper, model, checkNotExist, null, TableNameEnum.CATEGORY);
        }
        else {
            if (model.getShopId() == null || model.getId() == null) {
                throw new DaoManagerException("缺少数据，如归属店铺ID等");
            }
            CategoryQuery checkOnlyOne = new CategoryQuery();
            checkOnlyOne.setId(model.getId());
            checkOnlyOne.setShopId(model.getShopId());
            return save(categoryMapper, model, null, checkOnlyOne, TableNameEnum.CATEGORY);
        }
    }

    public List<CategoryDO> selectCates(CategoryQuery query) throws DaoManagerException {
        return select(categoryMapper, query);
    }

    /**
     * @see BaseManager4Mybatis#selectOne(BaseMapper, BaseQuery)
     */
    public CategoryDO selectCate(CategoryQuery query) throws DaoManagerException {
        return selectOne(categoryMapper, query);
    }

    public int delCate(CategoryQuery query) throws DaoManagerException {
        if (query.getId() == null) {
            throw new DaoManagerException("缺少ID");
        }
        return del(categoryMapper, query);
    }

    public int countCate(CategoryQuery query) throws DaoManagerException {
        return count(categoryMapper, query);
    }

    public int countItem(ItemQuery query) throws DaoManagerException {
        return count(itemMapper, query);
    }

    public void deductInventory(SkuDO skuDO) throws DaoManagerException {
        ItemDO itemDO = new ItemDO();
        itemDO.setShopId(skuDO.getShopId());
        itemDO.setId(skuDO.getItemId());
        itemDO.setInventory(skuDO.getInventory());

        // mysql当前读。20线程，每线程执行100次，10000扣减到6000，符合预期，结果未出现数据混乱。
        int itemResult = itemMapper.deductInventory(itemDO);

        if (itemResult < 1) {
            throw new DaoManagerException("扣减库存失败");
        }
        if (skuDO.getId() > 0) {
            int skuResult = skuMapper.deductInventory(skuDO);
            if (skuResult < 1) {
                throw new DaoManagerException("扣减库存失败");
            }
        }
    }
    public boolean verifyDeductInventory(SkuDO skuDO){
        ItemDO itemDO = new ItemDO();
        itemDO.setShopId(skuDO.getShopId());
        itemDO.setId(skuDO.getItemId());
        itemDO.setInventory(skuDO.getInventory());

        int itemResult = itemMapper.verifyDeductInventory(itemDO);

        if (itemResult < 1) {
            return false;
        }

        if (skuDO.getId() > 0) {
            int skuResult = skuMapper.verifyDeductInventory(skuDO);
            if (skuResult < 1) {
                return false;
            }
        }

        return true;
    }

    public void addInventory(SkuDO skuDO) throws DaoManagerException {
        ItemDO itemDO = new ItemDO();
        itemDO.setShopId(skuDO.getShopId());
        itemDO.setId(skuDO.getItemId());
        itemDO.setInventory(skuDO.getInventory());

        // mysql当前读。20线程，每线程执行100次，10000扣减到6000，符合预期，结果未出现数据混乱。
        int itemResult = itemMapper.addInventory(itemDO);

        if (itemResult < 1) {
            throw new DaoManagerException("增加库存失败");
        }
        if (skuDO.getId() > 0) {
            int skuResult = skuMapper.addInventory(skuDO);
            if (skuResult < 1) {
                throw new DaoManagerException("增加库存失败");
            }
        }
    }

    public void addSales(SkuDO skuDO) {
        ItemDO itemDO = new ItemDO();
        itemDO.setShopId(skuDO.getShopId());
        itemDO.setId(skuDO.getItemId());
        itemDO.setSales(skuDO.getSales());

        // mysql当前读。20线程，每线程执行100次，10000扣减到6000，符合预期，结果未出现数据混乱。
        itemMapper.addSales(itemDO);

        if (skuDO.getId() > 0) {
            skuMapper.addSales(skuDO);
        }
    }
}
