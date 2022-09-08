var weixin = {
	gotoLogin : function(data){
        if (data === undefined) {
        	data = {}
        }
		if(data.needCallback && (data.callback == null || data.callback === undefined)){
			data.callback = window.location.href;
		}
		//data = JSON.stringify(data)
        log(data)
		// 参数不能用json
		wx.miniProgram.redirectTo(
			{
				url:'../login/login?' + "shopId=" + data.shop.id + "&shopName=" + data.shop.name + "&shopImg=" + data.shop.img + "&callback=" + data.callback ,
				fail: function(){
					$.toast("跳转登录失败", 500)
				}
			}
		)
	},
	gotoFirst : function(data){
        /*if (data === undefined) {
        	data = {}
        }
		if(data.callback == null || data.callback === undefined){
			data.callback = window.location.href;
		}*/
		//data = JSON.stringify(data)
        log("goFirst")
		// 参数不能用json
		wx.miniProgram.redirectTo(
			{
				url:'../first/first',
				fail: function(){
					$.toast("重返首页失败", 500)
				}
			}
		)
	},
    gotoPay : function(data){
        if (data === undefined || data.prepayId == undefined || data.orderId == undefined || data.shopId == undefined
            || data.total == undefined || data.nonceStr == undefined || data.paySign == undefined) {
            $.toast("数据错误", 1000)
			return false
        }

        // data.callback = window.location.protocol+"//"+window.location.host+"/wmall/orderdetail.html";
        data.callback = "https://"+window.location.host+"/wmall/wxpay/checkPayed";

        //data = JSON.stringify(data)
        log(data)
        // 参数不能用json
        wx.miniProgram.redirectTo(
            {
                url:'../pay/pay?'
				+ "orderId=" + data.orderId
				+ "&shopName=" + data.shopName
				+ "&shopId=" + data.shopId
				+ "&tId=" + data.tId
				+ "&total=" + data.total
				+ "&timeStamp=" + data.timeStamp
				+ "&nonceStr=" + data.nonceStr
				+ "&prepay_id=" + data.prepayId
				+ "&signType=" + data.signType
				+ "&paySign=" + data.paySign
				+ "&callback=" + data.callback ,
                fail: function(){
                    $.toast("跳转支付页失败", 500)
                }
            }
        )
    }
}