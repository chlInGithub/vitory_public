package com.chl.victory.web.controller.wm.sms;

import com.chl.victory.web.aspect.IgnoreExperience;
import com.chl.victory.web.model.PageParam;
import com.chl.victory.web.model.PageResult;
import com.chl.victory.web.model.Result;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/p")
public class SmsController {
    /**
     * 充值记录list
     */
    @GetMapping(path = "/wm/sms/listCharge", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public PageResult listCharge(SmsQuery param){
        List<SmsCharge> charges = new ArrayList<>();
        charges.add(SmsCharge.mock());
        charges.add(SmsCharge.mock());
        return PageResult.SUCCESS(charges, param.getDraw(), param.getPageIndex(), 10000);
    }

    /**
     * 发送短信list
     */
    @GetMapping(path = "/wm/sms/list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public PageResult list(SmsQuery param){
        System.out.println(param);
        List<Sms> smss = new ArrayList<>();
        smss.add(Sms.mock());
        smss.add(Sms.mock());
        return PageResult.SUCCESS(smss, param.getDraw(), param.getPageIndex(), 10000);
    }

    /**
     * 目前可用短信数量
     */
    @GetMapping(path = "/wm/sms/count", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result count(){
        return Result.SUCCESS(10);
    }

    /**
     * 短信套餐
     */
    @GetMapping(path = "/wm/sms/sets", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result sets(){
        List<SmsSet> sets = new ArrayList<>();
        sets.add(SmsSet.mock());
        sets.add(SmsSet.mock());
        sets.add(SmsSet.mock());
        return Result.SUCCESS(sets);
    }

    /**
     * 创建充值订单
     */
    @PostMapping(path = "/wm/sms/charge", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @IgnoreExperience
    public Result charge(Integer id){
        System.out.println(id);
        return Result.SUCCESS();
    }

    @Data
    public static class SmsSet{
        private Long id;
        private String desc;
        private BigDecimal fee;
        public static SmsSet mock(){
            SmsSet smsSet = new SmsSet();
            smsSet.setDesc("超值套餐1");
            smsSet.setFee(new BigDecimal("100"));
            smsSet.setId(1L);
            return  smsSet;
        }
    }

    @Data
    public static class SmsQuery extends PageParam {
        String start;
        String end;
        Integer status;
    }

    @Data
    public static class Sms {
        public Long id;
        public String mobile;
        public String msg;
        public String time;
        public Integer status;
        public static Sms mock(){
            Sms msg = new Sms();
            msg.setMobile("1232345654");
            msg.setMsg("msg");
            msg.setStatus(1);
            msg.setTime("2019-01-01 01:01:23");
            return msg;
        }
    }
    @Data
    public static class SmsCharge {
        public Long id;
        /**
         * 充值时间
         */
        public String payTime;
        /**
         * 充值前可用条数
         */
        public Integer beforeNum;
        /**
         * 充值后可用条数
         */
        public Integer afterNum;
        /**
         * 充值条数
         */
        public Integer chargeNum;
        /**
         * 充值金额
         */
        public BigDecimal chargeFee;
        /**
         * 充值类型,短信充值套餐类型，如新用户赠送，套餐1，套餐2……，需要有个表存储具体内容
         */
        public String type;
        /**
         * 状态
         */
        public Integer status;
        public static SmsCharge mock(){
            SmsCharge msg = new SmsCharge();
            msg.setId(123L);
            msg.setPayTime("2019-01-01 01:01:23");
            msg.setBeforeNum(10);
            msg.setAfterNum(20);
            msg.setChargeNum(10);
            msg.setChargeFee(new BigDecimal("1.00"));
            msg.setType("充值套餐1");
            msg.setStatus(1);
            return msg;
        }
    }
}
