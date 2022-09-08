/*vue混入，复用方法*/
var common_mixin = {
    data: function () {
        var data = {
            user: {},
            shop: {}
        }
        return data
    },
    methods: {
        m_get_img: function(imgId, paramType){
            return ImgUtil.getImgUrl(imgId, paramType)
           /* if (imgId === undefined) {
                return;
            }
            if (imgId.includes("/")) {
                return imgId;
            }
            return "/img/" + imgId;*/
        },
        m_abbreviatory : function (a, length) {
            return StringUtil.abbreviatory(a, length)
        },
        m_money_desc:function (a) {
            return StringUtil.moneyDesc(a)
        }
    }
}