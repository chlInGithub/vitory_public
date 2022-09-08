package com.chl.victory.serviceimpl.test.dao.item;

import com.chl.victory.dao.manager.item.ItemManager;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.model.item.CategoryDO;
import com.chl.victory.dao.model.item.ItemDO;
import com.chl.victory.dao.model.item.SkuDO;
import com.chl.victory.dao.query.item.CategoryQuery;
import com.chl.victory.dao.query.item.ItemQuery;
import com.chl.victory.dao.query.item.SkuQuery;
import com.chl.victory.serviceimpl.test.BaseTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ItemManagerTest extends BaseTest {
    @Resource
    ItemManager itemManager;

    static List<Long> cateIds = new ArrayList<>();
    static Long leafCateId;
    static Long itemId;
    static int skuNum;

    //@Test
    public void test01_saveCate(int i) throws DaoManagerException {
        CategoryDO model = new CategoryDO();
        model.setName("蔬菜"+i);
        model.setLevel((byte)1);
        model.setShopId(1L);
        model.setOperatorId(1L);
        int result = itemManager.saveCategory(model);
        Assert.assertEquals(1, result);
        cateIds.add(model.getId());

        CategoryDO model2 = new CategoryDO();
        model2.setName("白菜"+i);
        model2.setLevel((byte)2);
        model2.setShopId(1L);
        model2.setParentId(model.getId());
        model2.setOperatorId(1L);
        result = itemManager.saveCategory(model2);
        Assert.assertEquals(1, result);
        leafCateId = model2.getId();
        cateIds.add(model2.getId());

    }
    @Test
    public void test02_updateCate() throws DaoManagerException {
        CategoryDO model2 = new CategoryDO();
        model2.setId(leafCateId);
        model2.setName("白菜modified");
        model2.setShopId(1L);
        int result = itemManager.saveCategory(model2);
        Assert.assertEquals(1, result);
        Assert.assertEquals(leafCateId, model2.getId());
    }
    @Test
    public void test03_selectCate() throws DaoManagerException {
        CategoryQuery query = new CategoryQuery();
        query.setShopId(2L);
        List<CategoryDO> categoryDOS = itemManager.selectCates(query);
        Assert.assertNotNull(categoryDOS);
        Assert.assertTrue(categoryDOS.size() < 1);

        CategoryQuery query2 = new CategoryQuery();
        query2.setShopId(1L);
        categoryDOS = itemManager.selectCates(query2);
        Assert.assertNotNull(categoryDOS);
        Assert.assertTrue(categoryDOS.size() >= 1);
    }
    //@Test
    public void test04_saveItem(int temp) throws DaoManagerException {
        ItemDO itemDO = new ItemDO();
        itemDO.setDetailHtml("<html></html>");
        itemDO.setKey("key1 key2");
        itemDO.setLeafCateId(leafCateId);
        itemDO.setPrice(new BigDecimal("99.9"));
        itemDO.setTagPrice(new BigDecimal("199.9"));
        itemDO.setStatus((byte)10);
        itemDO.setTitle("商品标题xxxxx" + temp);
        itemDO.setLogistics("xxxx");
        itemDO.setShopId(1L);
        itemDO.setOperatorId(1L);
        int result = itemManager.saveItem(itemDO);
        Assert.assertEquals(1, result);
        itemId = itemDO.getId();

        skuNum = 3;
        for (int i = 0; i < skuNum; i++) {
            SkuDO skuDO = new SkuDO();
            skuDO.setItemId(itemId);
            skuDO.setPrice(itemDO.getPrice());
            skuDO.setTagPrice(itemDO.getTagPrice());
            skuDO.setTitle("sku标题"+i);
            skuDO.setShopId(1L);
            skuDO.setOperatorId(1L);
            result = itemManager.saveSku(skuDO);
            Assert.assertEquals(1, result);
        }
    }
    @Test
    public void test05_updateItem() throws DaoManagerException {
        ItemDO itemDO = new ItemDO();
        itemDO.setDetailHtml("<html>xxx</html>");
        itemDO.setKey("key1 key2 key3");
        itemDO.setLeafCateId(leafCateId);
        itemDO.setPrice(new BigDecimal("98.9"));
        itemDO.setStatus((byte)10);
        itemDO.setTitle("商品标题xxxxx");
        itemDO.setShopId(1L);
        itemDO.setOperatorId(1L);
        itemDO.setId(itemId);
        int result = itemManager.saveItem(itemDO);
        Assert.assertEquals(1, result);

        SkuQuery skuQuery = new SkuQuery();
        skuQuery.setItemId(itemId);
        skuQuery.setShopId(2L);
        List<SkuDO> skuDOS = itemManager.selectSkus(skuQuery);
        Assert.assertNotNull(skuDOS);
        Assert.assertTrue(skuDOS.size() < 1);

        skuQuery = new SkuQuery();
        skuQuery.setItemId(itemId);
        skuQuery.setShopId(1L);
        skuDOS = itemManager.selectSkus(skuQuery);
        Assert.assertNotNull(skuDOS);
        Assert.assertTrue(skuDOS.size() == skuNum);

        for (int i = 0; i < skuDOS.size() - 1; i++) {
            SkuDO skuDO = skuDOS.get(i);
            skuDO.setItemId(itemId);
            skuDO.setPrice(itemDO.getPrice());
            skuDO.setTitle(skuDO.getTitle()+ "modified");
            result = itemManager.saveSku(skuDO);
            Assert.assertEquals(1, result);
        }

        SkuDO skuDO = skuDOS.get(skuDOS.size() - 1);
        SkuQuery skuQuery1 = new SkuQuery();
        skuQuery1.setId(skuDO.getId());
        result = itemManager.delSku(skuQuery1);
        Assert.assertEquals(1,result);

        skuNum--;
    }
    @Test
    public void test06_selectItem() throws DaoManagerException {
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.setShopId(2L);
        //itemQuery.setId(itemId);
        List<ItemDO> itemDOS = itemManager.selectItems(itemQuery);
        Assert.assertNotNull(itemDOS);
        Assert.assertTrue(itemDOS.size() < 1);

        itemQuery = new ItemQuery();
        itemQuery.setShopId(1L);
        //itemQuery.setId(itemId);
        itemDOS = itemManager.selectItems(itemQuery);
        Assert.assertNotNull(itemDOS);
        Assert.assertTrue(itemDOS.size() > 1);
    }
    @Test
    public void test07_delItem() throws DaoManagerException {
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.setId(itemId);
        int result = itemManager.delItem(itemQuery);
        Assert.assertEquals(1, result);

        SkuQuery skuQuery = new SkuQuery();
        skuQuery.setItemId(itemId);
        List<SkuDO> skuDOS = itemManager.selectSkus(skuQuery);
        for (SkuDO skuDO : skuDOS) {
            skuQuery.setId(skuDO.getId());
            result = itemManager.delSku(skuQuery);
            Assert.assertEquals(1, result);
        }
    }
    @Test
    public void test08_delCate() throws DaoManagerException {
        for (Long cateId : cateIds) {
            CategoryQuery query = new CategoryQuery();
            query.setId(cateId);
            int result = itemManager.delCate(query);
            Assert.assertEquals(1, result);
        }
    }
    @Test
    public void test09_selectIds() throws DaoManagerException {
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.setIds(Arrays.asList(1L,2L,4L,2020190522144958478L));
        List<ItemDO> itemDOS = itemManager.selectItems(itemQuery);
        Assert.assertNotNull(itemDOS);

        itemQuery = new ItemQuery();
        itemQuery.setIds(new ArrayList<>());
        itemDOS = itemManager.selectItems(itemQuery);
        Assert.assertNotNull(itemDOS);
    }

    @Test
    public void test10_testPerform() throws DaoManagerException {
        long s = System.nanoTime();
        for (int i = 0; i < 500; i++) {
            test01_saveCate(i);
        }
        long t = System.nanoTime() - s;
        System.out.println("saveCate cost time : " + t);

        s = System.nanoTime();
        for (int i = 0; i < 500; i++) {
            test03_selectCate();
        }
        t = System.nanoTime() - s;
        System.out.println("saveItem cost time : " + t);

        s = System.nanoTime();
        for (int i = 0; i < 500; i++) {
            test04_saveItem(i);
        }
        t = System.nanoTime() - s;
        System.out.println("saveItem cost time : " + t);

        s = System.nanoTime();
        for (int i = 0; i < 500; i++) {
            test06_selectItem();
        }
        t = System.nanoTime() - s;
        System.out.println("saveItem cost time : " + t);
    }
}