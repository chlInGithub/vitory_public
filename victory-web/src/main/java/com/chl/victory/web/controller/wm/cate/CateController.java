package com.chl.victory.web.controller.wm.cate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.chl.victory.webcommon.util.SessionUtil;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.item.model.CategoryDTO;
import com.chl.victory.serviceapi.item.query.CategoryQueryDTO;
import com.chl.victory.web.aspect.IgnoreExperience;
import com.chl.victory.web.model.PageParam;
import com.chl.victory.web.model.PageResult;
import com.chl.victory.web.model.Result;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.chl.victory.webcommon.manager.RpcManager.itemFacade;

/**
 * 商家自定义类目处理器
 * @author hailongchen9
 */
@Controller
@RequestMapping("/p")
public class CateController {

    @PostMapping(path = "/wm/cate/del", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result del(@RequestParam("id") Long id) {
        ServiceResult serviceResult = itemFacade.delCate(SessionUtil.getSessionCache().getShopId(), id);
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    /**
     * @param id
     * @param show {@link com.chl.victory.common.enums.YesNoEnum#code}
     * @return
     */
    @PostMapping(path = "/wm/cate/show", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result show(@RequestParam("id") Long id, @RequestParam("show") Integer show) {
        ServiceResult serviceResult = itemFacade.modifyCateShow(SessionUtil.getSessionCache().getShopId(), id, show);
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    /**
     * 保存类目信息
     * @param pId 上级类目ID
     * @param name 类目名称
     * @param id 类目ID，非null时update，null是insert
     * @return
     */
    @PostMapping(path = "/wm/cate/save", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result save(@RequestParam(name = "parent", required = false) Long pId,
            @RequestParam(name = "name") String name, @RequestParam(name = "imgs", required = false) String imgs,
            @RequestParam(name = "id", required = false) Long id) throws Exception {
        Assert.hasText(name, "请填写类目名称");

        int pLevel = 0;
        if (null != pId) {
            if (pId == -1) {
                pId = null;
            }
            else {
                CategoryQueryDTO categoryQuery = new CategoryQueryDTO();
                categoryQuery.setId(pId);
                categoryQuery.setShopId(SessionUtil.getSessionCache().getShopId());
                List<CategoryDTO> categoryDOS = itemFacade.selectCates(categoryQuery).getData();

                if (CollectionUtils.isEmpty(categoryDOS) || categoryDOS.size() > 1) {
                    throw new Exception("上级类目不存在");
                }

                pLevel = categoryDOS.get(0).getLevel();
            }
        }

        if (pId == null && StringUtils.isBlank(imgs)) {
            return Result.FAIL("一级类目必须设置图片");
        }

        if (pLevel >= 3) {
            return Result.FAIL("仅支持三级类目");
        }

        CategoryDTO categoryDO = new CategoryDTO();
        categoryDO.setName(name);
        categoryDO.setParentId(pId);
        categoryDO.setId(id);
        categoryDO.setLevel((byte) (pLevel + 1));
        categoryDO.setOperatorId(SessionUtil.getSessionCache().getUserId());
        categoryDO.setShopId(SessionUtil.getSessionCache().getShopId());
        categoryDO.setImg(imgs);
        ServiceResult serviceResult = itemFacade.saveCate(categoryDO);
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        else {
            return Result.FAIL(serviceResult.getMsg());
        }
    }

    @GetMapping(path = "/wm/cate/simpleList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result list() {
        PageResult pageResult = list(null);
        if (pageResult.isS()) {
            return Result.SUCCESS(pageResult.getData());
        }
        return Result.FAIL(pageResult.getError());
    }

    @GetMapping(path = "/wm/cate/list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public PageResult list(CateQuery param) {
        if (null == param) {
            param = new CateQuery();
        }

        CategoryQueryDTO categoryQuery = new CategoryQueryDTO();
        BeanUtils.copyProperties(param, categoryQuery);
        categoryQuery.setShopId(SessionUtil.getSessionCache().getShopId());
        Integer cateCount = itemFacade.countCate(categoryQuery).getData();
        if (cateCount < 1) {
            return PageResult.SUCCESS(Collections.EMPTY_LIST, param.getDraw(), param.getPageIndex(), 0);
        }

        List<CategoryDTO> categoryDOS = itemFacade.selectCates(categoryQuery).getData();
        if (CollectionUtils.isEmpty(categoryDOS)) {
            return PageResult.FAIL("没有查询到记录", 0, param.getDraw(), param.getPageIndex(), 10000);
        }

        List<Cate> cates = reOrder(categoryDOS);

        return PageResult.SUCCESS(cates, param.getDraw(), param.getPageIndex(), cateCount);
    }

    /**
     * 按照层级重新排序
     * @param categoryDOS
     * @return
     */
    private List<Cate> reOrder(List<CategoryDTO> categoryDOS) {
        Map<Long, List<CategoryDTO>> parentIdMapCates3Level = categoryDOS.stream()
                .filter(categoryDO -> categoryDO.getLevel() == 3)
                .collect(Collectors.groupingBy(CategoryDTO::getParentId));
        Map<Long, List<CategoryDTO>> parentIdMapCates2Level = categoryDOS.stream()
                .filter(categoryDO -> categoryDO.getLevel() == 2)
                .collect(Collectors.groupingBy(CategoryDTO::getParentId));
        List<CategoryDTO> cates1Level = categoryDOS.stream().filter(categoryDO -> categoryDO.getLevel() == 1)
                .collect(Collectors.toList());

        List<Cate> cates = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cates1Level)) {
            cates1Level.stream().forEach(l1 -> {
                Cate cateL1 = Cate.transfer(l1);
                cates.add(cateL1);
                List<CategoryDTO> categorysL2 = parentIdMapCates2Level.remove(cateL1.getId());
                if (!CollectionUtils.isEmpty(categorysL2)) {
                    categorysL2.stream().forEach(l2 -> {
                        Cate cateL2 = Cate.transfer(l2);
                        cates.add(cateL2);
                        List<CategoryDTO> categorysL3 = parentIdMapCates3Level.remove(cateL2.getId());
                        if (!CollectionUtils.isEmpty(categorysL3)) {
                            categorysL3.stream().forEach(l3 -> {
                                Cate cateL3 = Cate.transfer(l3);
                                cates.add(cateL3);
                            });
                        }
                    });
                }
            });
        }

        if (!parentIdMapCates2Level.isEmpty()) {
            parentIdMapCates2Level.values().stream().flatMap(List::stream).forEach(l2 -> {
                Cate cateL2 = Cate.transfer(l2);
                cates.add(cateL2);
                List<CategoryDTO> categorysL3 = parentIdMapCates3Level.remove(cateL2.getId());
                if (!CollectionUtils.isEmpty(categorysL3)) {
                    categorysL3.stream().forEach(l3 -> {
                        Cate cateL3 = Cate.transfer(l3);
                        cates.add(cateL3);
                    });
                }
            });
        }

        if (!parentIdMapCates3Level.isEmpty()) {
            parentIdMapCates3Level.values().stream().flatMap(List::stream).forEach(l3 -> {
                Cate cateL3 = Cate.transfer(l3);
                cates.add(cateL3);
            });
        }

        return cates;
    }

    @Data
    public static class CateQuery extends PageParam {

        String name;
    }

    /**
     * 类目层级最多为3层
     */
    @Data
    public static class Cate {

        public Long id;

        public String name;

        public String modifyTime;

        public Integer level;

        public Long parent;

        public String img;

        public Integer show;

        public List<Cate> subs;

        public static Cate transfer(CategoryDTO categoryDO) {
            Cate cate = new Cate();
            cate.setId(categoryDO.getId());
            cate.setLevel(categoryDO.getLevel().intValue());
            cate.setModifyTime(DateFormatUtils.format(categoryDO.getModifiedTime(), "yyyy-MM-dd hh:mm"));
            cate.setName(categoryDO.getName());
            cate.setImg(categoryDO.getImg());
            cate.setShow(categoryDO.getShow());
            cate.setParent(null == categoryDO.getParentId() ? -1 : categoryDO.getParentId());
            return cate;
        }

    }
}
