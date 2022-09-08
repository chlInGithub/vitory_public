var ImgParamType = [
    {
        name: "top_tab_shop_img",
        code: 1,
        param: "w=40&h=40&q=10"
    },
    {
        name: "item_list_img",
        code: 2,
        param: "w=300&h=130"
    }
]
/*vue混入，复用方法*/
var common_mixin = {
    data: function () {
        var data = {
            shop: {},
            user: {},
            recommended_item_list: [],
            loading: true,
            history: []
        }
        return data
    },
    methods: {
        m_get_history: function(){
            var current = window.location.href
            var historys = current.split("history")
            if (historys.length > 5) {
                var lastHistory = historys[historys.length - 1]
                var newHistorys = historys.slice(0, 4);
                newHistorys.push(lastHistory)
                current = newHistorys.join("history")
            }
            return "&history="+current.replace(new RegExp("\\?", "gm"), '0x3F').replace(new RegExp("&", "gm"), '0x26').replace(new RegExp("=", "gm"), '0x3D')
        },
        /**
         * @param imgId
         * @param paramTypeCode {@link ImgParamType.code}
         * @returns {string|*}
         */
        m_get_img: function(imgId, paramTypeCode){

            if (imgId === undefined) {
                return;
            }

            var url;
            if (imgId.includes("/")) {
                url = imgId;
            }else {
                url = "/img/" + imgId;
            }

            if (paramTypeCode != undefined) {
                var param
                for (var index = 0; index < ImgParamType.length; index++) {
                    var type = ImgParamType[index]
                    if (paramTypeCode == type.code) {
                        param = type.param
                        break
                    }
                }

                if (param != undefined) {
                    url = url + "?" + param;
                }
            }

            return url;
        },
        m_modify_shop_user: function (shop_object, user_object) {
            this.shop = shop_object
            this.user = user_object
            // 小程序webview会显示网页的title
            // document.title = this.shop.name
        },
		
		m_go_order_list: function (param) {
            if (param == undefined) {
                param = ""
            }
		    window.location.href = "orderlist.html?" + param + this.m_get_history();
		},
        m_go_coupon_list: function () {
		    window.location.href = "couponlist.html?" + this.m_get_history()
		},
		m_go_me: function () {
            if (this.m_need_go_login()) {
                return;
            }
		    window.location.href = "me.html?" + this.m_get_history()
		},
		m_go_cart: function () {
            if (this.m_need_go_login()) {
                return;
            }
		    window.location.href = "cart.html?" + this.m_get_history()
		},
		m_go_shop: function () {
		    window.location.href = "shop.html"
		},
        m_go_item_detail: function (item_id) {
            window.location.href = "itemdetail.html?id=" + item_id + this.m_get_history()
        },
        m_go_order_detail: function (order_id) {
            if (this.m_need_go_login()) {
                return;
            }
            window.location.href = "orderdetail.html?orderId=" + order_id + this.m_get_history()
        },
        m_go_settle: function (params) {
            if (this.m_need_go_login()) {
                return;
            }
            window.location.href = "settle.html?items="+params + this.m_get_history()
        },
        m_add_cart_from_item_list: function(itemId, existSku){
            if (existSku) {
                this.m_go_item_detail(itemId)
                // $.toast("商品有多种款式哦，请到详情页加购吧!", 1000)
            }else {
                this.m_add_cart(itemId)
            }
        },
        m_need_go_login: function(){
            if (this.user.name == undefined || this.user.id == undefined || this.user.hasPhone == false) {
                var data = {
                    callback : null,
                    shop: this.shop
                }
                weixin.gotoLogin(data)
                return true;
            }
            return false;
        },
        m_add_cart: function (itemId, skuId, count, needResultToast) {
			if (this.m_need_go_login()) {
                return;
			}

            if (itemId == undefined) {
                return;
            }
            if (skuId == undefined || skuId < 0) {
                skuId = 0
            }
            if (count == undefined || count < 0) {
                count= 1
            }
            if (needResultToast == undefined) {
                needResultToast = true
            }
            if (needResultToast) {
                $.showPreloader("处理中");
            }

			AjaxUtil.post("/wmall/cart/addItem", {itemId:itemId,skuId:skuId,count:count}, function(result) {
                if (needResultToast){
                    $.toast("加购成功!", 500)
                }
			})

            if (needResultToast){
                setTimeout(function () {
                    $.hidePreloader()
                }, 100);
            }

        },
        m_buy_now: function (item_id) {
            if (this.m_need_go_login()) {
                return;
            }
            log("m_buy_now " + item_id)
        },

        m_go_activity: function (activity_id, activity_type) {
            log(activity_id + "  " + activity_type)
            this.m_go_item_list("activityId="+activity_id)
        },
        /*领取优惠券*/
        m_getting_coupon: function (coupon_id, coupon_got) {
            if (this.m_need_go_login()) {
                return;
            }

            if (coupon_got === 1) {
                $.toast("已领取!", 500)
                return
            }

            if (this.user.id == undefined || this.user.hasPhone == false) {
                var data = {
                    callback : null,
                    shop: this.shop
                }
                weixin.gotoLogin(data)
                return;
            }

            $.showPreloader("处理中")

            var couponList = this.couponList
            AjaxUtil.get("/wmall/user/gainCoupon", {couId:coupon_id}, function(result) {
                $.hidePreloader()
                if (result.s){
                    $.toast("领取成功!", 500)
                    for (var i = 0; i < couponList.length; i++) {
                        var coupon = couponList[i]
                        if (coupon.id == coupon_id) {
                            coupon.got = 1
                            break
                        }
                    }
                }else {
                    $.toast("领取失败，请稍后尝试!", 500)
                }
            })

        },
        m_go_coupon_item_list: function (coupon_id) {
            this.m_go_item_list("couponId="+coupon_id)
        },
        m_go_activity_item_list: function (activity_id) {
            this.m_go_item_list("activityId="+activity_id)
        },
        /*前往商品列表*/
        m_go_item_list: function (query) {
            window.location.href = 'itemlist.html?' +  query + this.m_get_history()
        },
    }
}