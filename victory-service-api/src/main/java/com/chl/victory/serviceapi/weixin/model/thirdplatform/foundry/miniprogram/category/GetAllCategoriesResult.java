package com.chl.victory.serviceapi.weixin.model.thirdplatform.foundry.miniprogram.category;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;
import com.chl.victory.serviceapi.weixin.model.BaseResult;
import lombok.Data;

/**
 * @author ChenHailong
 * @date 2020/5/27 17:46
 **/
@Data
public class GetAllCategoriesResult extends BaseResult implements Serializable {
    Categories_list categories_list;
    @Data
    public static class Categories_list implements Serializable{
        Category[] categories;
    }

    @Data
    public static class Category implements Serializable{
        Integer id;
        Integer[] children;
        String name;
        Integer level;
        Integer father;

        /**
         * 是否为敏感类目（1 为敏感类目，需要提供相应资质审核；0 为非敏感类目，无需审核）
         */
        Integer sensitive_type;

        /**
         * sensitive_type 为 1 的类目需要提供的资质证明
         */
        Qualify qualify;

    }

    @Data
    public static class Qualify implements Serializable{
        Exter[] exter_list;
    }
    @Data
    public static class Exter implements Serializable{
        Inner[] inner_list;
    }

    @Data
    public static class Inner implements Serializable{
        String name;

        /**
         * 资质文件示例
         */
        String url;
    }

    public static void main(String[] args) {
        /*String temp = "{\n" + "  \"errcode\": 0,\n" + "  \"errmsg\": \"ok\",\n" + "  \"categories_list\": {\n"
                + "    \"categories\": [\n" + "      {\n" + "        \"id\": 0,\n" + "        \"children\": [1, 402],\n"
                + "        \"qualify\": {\n" + "          \"exter_list\": [],\n" + "          \"remark\": \"\"\n"
                + "        }\n" + "      }\n" + "    ]\n" + "  }\n" + "}";
        GetAllCategoriesResult getAllCategoriesResult = JSONObject.parseObject(temp, GetAllCategoriesResult.class);*/


        Inner inner = new Inner();
        inner.setName("1111");
        Inner[] inners = new Inner[]{inner, inner};
        Exter exter = new Exter();
        exter.setInner_list(inners);
        Exter[] exters = new Exter[]{exter, exter};
        Qualify qualify = new Qualify();
        qualify.setExter_list(exters);

        String temp = "{\"exter_list\":[{\"inner_list\":[{\"name\":\"汽车厂商：《营业执照》\",\"url\":\"\"},{\"name\":\"《工信部道路机动车辆生产企业准入许可》\",\"url\":\"\"}]},{\"inner_list\":[{\"name\":\"汽车经销商\\/4s店：《营业执照》\",\"url\":\"\"},{\"name\":\"《厂商授权销售文件》\",\"url\":\"\"},{\"name\":\"《工信部道路机动车辆生产企业准入许可》\",\"url\":\"\"}]},{\"inner_list\":[{\"name\":\"下属子\\/分公司：《营业执照》\",\"url\":\"\"},{\"name\":\"《工信部道路机动车辆生产企业准入许可》\",\"url\":\"\"},{\"name\":\"《股权关系证明函》（含双方盖章）\",\"url\":\"\"}]}]}";
        Qualify qualify1 = JSONObject.parseObject(temp, Qualify.class);

        System.out.println(JSONObject.toJSONString(qualify));
    }
}
