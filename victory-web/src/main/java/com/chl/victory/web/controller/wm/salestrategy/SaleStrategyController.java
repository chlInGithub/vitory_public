package com.chl.victory.web.controller.wm.salestrategy;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.common.constants.DateConstants;
import com.chl.victory.common.enums.merchant.SaleStrategyTypeEnum;
import com.chl.victory.common.util.ValidationUtil;
import com.chl.victory.localservice.manager.LocalServiceManager;
import com.chl.victory.serviceapi.ServiceResult;
import com.chl.victory.serviceapi.accesslimit.enums.AccessLimitTypeEnum;
import com.chl.victory.serviceapi.merchant.model.SaleStrategyDTO;
import com.chl.victory.serviceapi.merchant.model.SaleStrategyDTO.MaxCountAttr;
import com.chl.victory.serviceapi.merchant.model.SaleStrategyDTO.MinCountAttr;
import com.chl.victory.web.aspect.IgnoreExperience;
import com.chl.victory.web.model.PageParam;
import com.chl.victory.web.model.PageResult;
import com.chl.victory.web.model.Result;
import com.chl.victory.webcommon.manager.RpcManager;
import com.chl.victory.webcommon.util.SessionUtil;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.chl.victory.localservice.manager.LocalServiceManager.orderLocalService;
import static com.chl.victory.localservice.manager.LocalServiceManager.saleStrategyLocalService;
import static com.chl.victory.webcommon.manager.RpcManager.saleStrategyFacade;

/**
 * 销售策略控制器
 * @author hailongchen9
 */
@Controller
@RequestMapping("/p/wm/salestrategy/")
public class SaleStrategyController {

    @PostMapping(path = "savePresell", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result savePresell(@Validated PresellSaleStrategyVO vo) {
        ValidationUtil.validate(vo);
        ValidationUtil.validate(vo.getAttr());
        return save(vo);
    }

    @PostMapping(path = "saveMinCount", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result saveMinCount(@Validated MinCountSaleStrategyVO vo) {
        ValidationUtil.validate(vo);
        ValidationUtil.validate(vo.getAttr());
        return save(vo);
    }

    @PostMapping(path = "saveMaxCount", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result saveMaxCount(@Validated MaxCountSaleStrategyVO vo) {
        ValidationUtil.validate(vo);
        ValidationUtil.validate(vo.getAttr());
        return save(vo);
    }

    private Result save(BaseSaleStrategyVO vo) {
        if (vo.getId() == null) {
            Long shopId = SessionUtil.getSessionCache().getShopId();
            SaleStrategyDTO query = new SaleStrategyDTO();
            query.setShopId(shopId);
            ServiceResult<Integer> result = saleStrategyFacade.count(query);
            if (result.getSuccess() && result.getData() != null) {
                RpcManager.accessLimitFacade
                        .doAccessLimit(shopId, result.getData(), AccessLimitTypeEnum.WM_SHOP_SALE_STRATEGY_TOTAL,
                                AccessLimitTypeEnum.WM_SHOP_SALE_STRATEGY_TOTAL.getDesc());
            }
        }

        SaleStrategyDTO model = vo.transfer();

        List<Long> itemIds = null;
        if (StringUtils.isNotBlank(vo.getItemIds())) {
            itemIds = Arrays.stream(vo.getItemIds().split(",")).filter(NumberUtils::isDigits).map(NumberUtils::toLong)
                    .collect(Collectors.toList());
        }
        ServiceResult serviceResult = saleStrategyFacade.save(model, itemIds);

        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        else {
            return Result.FAIL(serviceResult.getMsg());
        }
    }

    @PostMapping(path = "saveMinFee", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result saveMinFee(@Validated MinFeeSaleStrategyVO vo) {
        ValidationUtil.validate(vo);
        ValidationUtil.validate(vo.getAttr());
        return save(vo);
    }

    @PostMapping(path = "del", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result del(@RequestParam("id") Long id) {
        @NotNull @Positive Long shopId = SessionUtil.getSessionCache().getShopId();
        ServiceResult serviceResult = saleStrategyFacade.del(id, shopId);
        if (serviceResult.getSuccess()) {
            return Result.SUCCESS();
        }
        return Result.FAIL(serviceResult.getMsg());
    }

    @GetMapping(path = "detail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result detail(@RequestParam("id") Long id) {
        Long shopId = SessionUtil.getSessionCache().getShopId();
        SaleStrategyDTO query = new SaleStrategyDTO();
        query.setShopId(shopId);
        query.setId(id);
        ServiceResult<List<SaleStrategyDTO>> listServiceResult = saleStrategyFacade.selectSaleStrategy(query);

        if (!listServiceResult.getSuccess()) {
            return Result.FAIL(listServiceResult.getMsg());
        }
        if (CollectionUtils.isEmpty(listServiceResult.getData())) {
            return Result.SUCCESS();
        }

        SaleStrategyDTO model = listServiceResult.getData().get(0);

        Set<Long> itemIds = saleStrategyLocalService.getItemIdsOfStrategy(shopId, model.getId());

        SaleStrategyVO vo = SaleStrategyVO.transfer(model);

        if (SaleStrategyTypeEnum.preSale.getCode().equals(model.getStrategyType())) {
            Map<Long, Integer> allPresellItemTotal = orderLocalService.getAllPresellItemTotal(shopId);
            if (!CollectionUtils.isEmpty(allPresellItemTotal)) {
                List<String> saleData = allPresellItemTotal.keySet().stream().map(key -> "商品ID " + key + "，销量 " + allPresellItemTotal.get(key)).collect(Collectors.toList());
                vo.setSaleData(saleData);
            }
        }
        if (!CollectionUtils.isEmpty(itemIds)) {
            vo.setItemIds(StringUtils.join(itemIds, ","));
        }

        return Result.SUCCESS(vo);
    }

    @GetMapping(path = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public PageResult list(@NonNull SaleStrategyParam param) {
        SaleStrategyDTO query = SaleStrategyParam.stranfer(param);
        // count
        ServiceResult<Integer> countResult = saleStrategyFacade.count(query);
        if (!countResult.getSuccess()) {
            return PageResult.FAIL(countResult.getMsg(), countResult.getFailType().getType(), param.getDraw(),
                    param.getPageIndex(), 0);
        }
        if (countResult.getData() == 0) {
            return PageResult.SUCCESS(Collections.EMPTY_LIST, param.getDraw(), param.getPageIndex(), 0);
        }
        // models
        ServiceResult<List<SaleStrategyDTO>> listResult = saleStrategyFacade.selectSaleStrategy(query);
        if (!listResult.getSuccess()) {
            return PageResult.FAIL(listResult.getMsg(), listResult.getFailType().getType(), param.getDraw(),
                    param.getPageIndex(), 0);
        }

        // 不对其中异常进行处理，因为数据正确的话应不出现异常
        List<SaleStrategyVO> itemInfos = listResult.getData().stream().filter(itemDO -> null != itemDO)
                .map(SaleStrategyVO::transfer).collect(Collectors.toList());

        return PageResult.SUCCESS(itemInfos, param.getDraw(), param.getPageIndex(), countResult.getData());
    }

    @Data
    public static class SaleStrategyParam extends PageParam {

        Long id;

        private Byte strategyType;

        public static SaleStrategyDTO stranfer(SaleStrategyParam param) {
            SaleStrategyDTO query = new SaleStrategyDTO();
            BeanUtils.copyProperties(param, query);
            query.setShopId(SessionUtil.getSessionCache().getShopId());
            return query;
        }
    }

    @Data
    public static class SaleStrategyVO {

        /**
         * record ID,按照一定规则生成
         */
        Long id;

        /**
         * record modified time
         */
        String modifiedTime;

        /**
         * belong to shop ID
         */
        Long shopId;

        /**
         * 参与活动的商品集合
         */
        String itemIds;

        /**
         * 营销策略类型
         * strategy_type
         */
        @NotNull(message = "请填写类型")
        private Byte strategyType;

        /**
         * 属性,json格式
         * attr
         */
        @NotEmpty(message = "请填写具体配置")
        private String attr;

        private List<String> saleData;

        private Integer payType;

        public static SaleStrategyDTO transfer(SaleStrategyVO editInfo) {
            SaleStrategyDTO model = new SaleStrategyDTO();
            BeanUtils.copyProperties(editInfo, model);
            model.setShopId(SessionUtil.getSessionCache().getShopId());
            model.setOperatorId(SessionUtil.getSessionCache().getUserId());
            return model;
        }

        public static SaleStrategyVO transfer(SaleStrategyDTO model) {
            SaleStrategyVO edit = new SaleStrategyVO();
            BeanUtils.copyProperties(model, edit);
            edit.setModifiedTime(DateFormatUtils.format(model.getModifiedTime(), DateConstants.format1));
            return edit;
        }
    }

    @Data
    public static class BaseSaleStrategyVO {

        /**
         * record ID,按照一定规则生成
         */
        Long id;

        /**
         * 参与活动的商品集合
         */
        String itemIds;

        /**
         * 营销策略类型
         * strategy_type
         */
        @NotNull(message = "请填写类型")
        private Byte strategyType;

        /**
         * 支持的支付方式
         */
        private Integer payType;

        public SaleStrategyDTO transfer() {
            SaleStrategyDTO model = new SaleStrategyDTO();
            BeanUtils.copyProperties(this, model);
            model.setShopId(SessionUtil.getSessionCache().getShopId());
            model.setOperatorId(SessionUtil.getSessionCache().getUserId());
            return model;
        }
    }

    @Data
    public static class PresellSaleStrategyVO extends BaseSaleStrategyVO {

        /**
         * 属性
         * attr
         */
        @NotNull(message = "请填写具体配置")
        private SaleStrategyDTO.PreSellAttr attr;

        @Override
        public SaleStrategyDTO transfer() {
            SaleStrategyDTO model = super.transfer();
            String attr = JSONObject.toJSONString(this.attr);
            model.setAttr(attr);
            return model;
        }
    }

    @Data
    public static class MinFeeSaleStrategyVO extends BaseSaleStrategyVO {

        /**
         * 属性
         * attr
         */
        @NotNull(message = "请填写具体配置")
        private SaleStrategyDTO.MinFeeAttr attr;

        @Override
        public SaleStrategyDTO transfer() {
            SaleStrategyDTO model = super.transfer();
            String attr = JSONObject.toJSONString(this.attr);
            model.setAttr(attr);
            return model;
        }
    }

    @Data
    public static class MinCountSaleStrategyVO extends BaseSaleStrategyVO {

        /**
         * 属性
         * attr
         */
        @NotNull(message = "请填写具体配置")
        private MinCountAttr attr;

        @Override
        public SaleStrategyDTO transfer() {
            SaleStrategyDTO model = super.transfer();
            String attr = JSONObject.toJSONString(this.attr);
            model.setAttr(attr);
            return model;
        }
    }

    @Data
    public static class MaxCountSaleStrategyVO extends BaseSaleStrategyVO {

        /**
         * 属性
         * attr
         */
        @NotNull(message = "请填写具体配置")
        private MaxCountAttr attr;

        @Override
        public SaleStrategyDTO transfer() {
            SaleStrategyDTO model = super.transfer();
            String attr = JSONObject.toJSONString(this.attr);
            model.setAttr(attr);
            return model;
        }
    }
}
