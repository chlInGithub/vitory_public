/*通用性行为。
jQuery code：必须使用jQuery，不可使用$。
保持洁净：不要使用vue method。
*/
var log = function(c) {
	console.log(c)
	return false
}

var setDocumentTitle = function (title) {
    var val
    if (title == undefined) {
        val = app.shop.name
    }else {
        val = title
    }
    document.title = val
}

var AjaxUtil = {
	control: {
		doingAjaxCounter: 0,
		doneAjaxCounter: 0,
		beforeAjax: function() {
			if (AjaxUtil.control.doneAjaxCounter == AjaxUtil.control.doingAjaxCounter) {
				log("showPreloader")
				$.showPreloader();
			}
			AjaxUtil.control.doingAjaxCounter++
		},
		afterAjax: function() {
			AjaxUtil.control.doneAjaxCounter++
			if (AjaxUtil.control.doneAjaxCounter == AjaxUtil.control.doingAjaxCounter) {
				$.hidePreloader()
			}
		},
		successCallback: function(result, callback, failCallback) {
			//AjaxUtil.control.afterAjax()
			if (result.s === undefined) {
				$.toast("请求失败", 500)
				return false
			}

			if (!result.s) {
				if (result.c === 10001) {
					// 跳转注册页面
					return weixin.gotoLogin()
				}
				if (result.c === 10002) {
					// session超时
					return weixin.gotoFirst()
				}
				{
                    if (failCallback != undefined) {
                    	failCallback(result)
                    }else {
                        $.toast(result.m, 1000);
                    }
				}

				//return false
			}else {
                callback(result)

			}
		}
	},
	get: function(url, param, callback, failCallback) {
		AjaxUtil.control.beforeAjax()
		jQuery.get(url, param, function(result) {
            AjaxUtil.control.afterAjax()
			AjaxUtil.control.successCallback(result, callback)
		}, "json")
		.always(function() {
			log("always")
			AjaxUtil.control.afterAjax()
		})
			.fail(function (result) {
                AjaxUtil.control.afterAjax()
                if (failCallback != undefined) {
                    return failCallback(result);
                }
                $.toast("请求失败", 500)
            })
	},
	post: function(url, param, callback, failCallback) {
		AjaxUtil.control.beforeAjax()
		jQuery.post(url, param, function(result) {
            AjaxUtil.control.afterAjax()
			AjaxUtil.control.successCallback(result, callback, failCallback)
		}, "json")
		.always(function() {
			AjaxUtil.control.afterAjax()
		}).fail(function (result) {
            AjaxUtil.control.afterAjax()
            if (failCallback != undefined) {
                return failCallback(result);
            }
            $.toast("请求失败", 500)
        })
	}
} // end AjaxUtil

var initPage = function(pageCallback, needMoreShopInfo) {
	var data = {}
    if (true == needMoreShopInfo) {
		data.needMoreShopInfo = 1
    }
    app.loading = true
	AjaxUtil.get("/wmall/shop/simple", data, function(result) {
		app.m_modify_shop_user(result.d.shop, result.d.user)
        pageCallback()
        if (jQuery(".card-recommended-item-list").length > 0) {
			itemFuncs.getRecommendedItemList()
        }
        app.loading = false
	});

	setTimeout(function () {
        $.hidePreloader()
    }, 1000 * 10)
}

var itemFuncs = {
    getRecommendedItemList : function () {
        AjaxUtil.get("/wmall/item/list", {
            type: 2,
            pageSize: 10
        }, function (result) {
            app.recommended_item_list = JsonUtil.getJson(result.d);
            saleStrategyUtil.parseList(app.recommended_item_list)
        })
    },
	check_item_valid : function (itemList) {
        if (itemList == undefined || itemList.length < 1){
            return false
        }

        for (var i = 0; i < itemList.length; i++) {
            var item = itemList[i]
            if (item.minimum != undefined && item.count < item.minimum) {
                $.toast("商品购买数量小于起卖件数", 500)
                return false
            }
            if (item.inventory != undefined && item.count > item.inventory) {
                $.toast("商品库存不足", 500)
                return false
            }
        }
        return true
    }
}

/**
 * 将url param部分转换为json string
 */
var genJsonFromUrlParams = function () {
    var json = {}
    if (window.location.search.length > 1) {
        var params = window.location.search.substr(1).split('&')
        for (var index in params) {
            var kv = params[index].split("=")
            if (kv.length == 2 && StringUtil.isNotEmpty(kv[0]) && StringUtil.isNotEmpty(kv[1])) {
                json[kv[0]] = decodeURI(kv[1])
            }
        }
    }
    return json
}

var goHistroy = function () {
    var params = genJsonFromUrlParams();
    if (params.history != undefined) {
        var target  = params.history
        var more = ""
        var index = params.history.indexOf("0x26history0x3D")
        if (index != -1) {
            target = params.history.substring(0, index);
            more = params.history.substring(index+15, params.history.length);
        }

        var history = target.replace(new RegExp("0x3D", "gm"), '=').replace(new RegExp("0x3F", "gm"), '?').replace(new RegExp("0x26", "gm"), '&')
        if (index != -1) {
            history += "&history=" + more
        }
        window.location.href = history
    }
}

var StringUtil = {
    isNotEmpty : function (str) {
        return undefined != str && "" != str
    },
    abbreviatory : function (str, maxL) {
        if (!ObjectCommonUtil.isEmpty(str) && str.length > (undefined == maxL ? 10 : maxL)) {
            return str.substring(0,10) + '…'
        }
        return str
    },
    moneyDesc:function (money) {
        return "￥"+money+"元"
    },
    simplePrint: function (str) {
        if (StringUtil.isNotEmpty(str)) {
            return str
        }
        return '-'
    }
}

var JsonUtil = {
    getJson : function (d) {
        var data
        if (typeof(d ) == 'string') {
            data = $.parseJSON(d)
        }else {
            data = d
        }
        return data
    },
    toJson : function (d) {
        return JsonUtil.getJson(d)
    }
}

var ObjectCommonUtil = {
    isNotUndefined : function (o) {
        return undefined != o
    },
    isUndefined : function (o) {
        return undefined == o
    },
    isEmpty : function (o) {
        return undefined === o || null === o || '' === o
    }
}

var saleStrategyUtil = {
    parseList : function(itemListJson){
        for (var i = 0; i < itemListJson.length; i++) {
            itemListJson[i]["strategyTagHtml"] = saleStrategyUtil.getSaleStrategyTagHtml(itemListJson[i])
            itemListJson[i]["strategyDetailHtml"] = saleStrategyUtil.getSaleStrategyTagHtml(itemListJson[i])
        }
    },
    /**
     * 结果为 { presell : attrJson, minFee : attrJson, minCount : attrJson, maxCount : attrJson, ...}
     * @param strategyList
     */
    parse : function (itemJson) {
        if (ObjectCommonUtil.isNotUndefined(itemJson.strategyJson)){
            return itemJson.strategyJson
        }
        var result = {}
        if (ObjectCommonUtil.isNotUndefined(itemJson.saleStrategies)) {
            var strategyList = itemJson.saleStrategies;
            for (var i = 0; i < strategyList.length; i++) {
                var item = JsonUtil.toJson(strategyList[i]);
                if (item.strategyType == 1) {
                    result.presell = JsonUtil.toJson(item.attr)
                    continue
                }
                if (item.strategyType == 2) {
                    result.minFee = JsonUtil.toJson(item.attr)
                    continue
                }
                if (item.strategyType == 3) {
                    result.minCount = JsonUtil.toJson(item.attr)
                    continue
                }
                if (item.strategyType == 4) {
                    result.maxCount = JsonUtil.toJson(item.attr)
                    continue
                }
            }
        }
        itemJson["strategyJson"] = result
        log(itemJson)
    },
    getSaleStrategyTagHtml : function (itemJson) {
        var row = itemJson
        saleStrategyUtil.parse(row)
        var salestrategyHtml = ""
        if (ObjectCommonUtil.isNotUndefined(row.strategyJson.presell)) {
            salestrategyHtml += '<span class="salestrategy-tag salestrategy-tag-1">预售</span>'
        }
        if (ObjectCommonUtil.isNotUndefined(row.strategyJson.minFee)) {
            salestrategyHtml += '<span class="salestrategy-tag salestrategy-tag-2">订单最少'+row.strategyJson.minFee.minFee+'元</span>'
        }
        if (ObjectCommonUtil.isNotUndefined(row.strategyJson.minCount)) {
            salestrategyHtml += '<span class="salestrategy-tag salestrategy-tag-1">限购'+row.strategyJson.minCount.minCount+'件</span>'
        }
        if (ObjectCommonUtil.isNotUndefined(row.strategyJson.maxCount)) {
            salestrategyHtml += '<span class="salestrategy-tag salestrategy-tag-2">最多'+row.strategyJson.maxCount.maxCount+'件</span>'
        }
        return salestrategyHtml
    },
    getSaleStrategyDetailHtml : function (itemJson) {
        var row = itemJson
        saleStrategyUtil.parse(row)
        var salestrategyHtml = ""
        if (ObjectCommonUtil.isNotUndefined(row.strategyJson.presell)) {
            salestrategyHtml += '<div class="salestrategy-tag">预售'
            salestrategyHtml += '<span>|截止日期:'+row.strategyJson.presell.endTime+'</span>'
            salestrategyHtml += '<span>|发货日期:'+row.strategyJson.presell.sentTime+'</span>'
            salestrategyHtml += '</div><br/>'
        }
        if (ObjectCommonUtil.isNotUndefined(row.strategyJson.minFee)) {
            salestrategyHtml += '<div class="salestrategy-tag">订单实际金额最少'
            salestrategyHtml += '<span>'+row.strategyJson.minFee.minFee+'元</span>'
            salestrategyHtml += '</div><br/>'
        }
        if (ObjectCommonUtil.isNotUndefined(row.strategyJson.minCount)) {
            salestrategyHtml += '<div class="salestrategy-tag">订单中商品数量最少'
            salestrategyHtml += '<span>'+row.strategyJson.minCount.minCount+'件</span>'
            salestrategyHtml += '</div><br/>'
        }
        if (ObjectCommonUtil.isNotUndefined(row.strategyJson.maxCount)) {
            salestrategyHtml += '<div class="salestrategy-tag">订单中商品数量最多'
            salestrategyHtml += '<span>'+row.strategyJson.maxCount.maxCount+'件</span>'
            salestrategyHtml += '</div><br/>'
        }
        return salestrategyHtml
    }
}