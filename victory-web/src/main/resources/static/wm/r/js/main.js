/**
 * 业务无关
 * [top businessiness menu(顶部业务菜单)、menu tree(左侧菜单树)、tab panel(右侧内容块)] 三者交互
 * 动态加载top businessiness menu并绑定tab相关事件 -> 动态生成menu tree并绑定点击事件
 * 支持随意切换坐标(top businessiness menu，menu tree，tab panel)
 * 支持tab或new window场景
 * 支持页面高度随内容动态调整
 **/
var test = {mock:false}

var walkSimpleJson = function () {
    var json = {'name': 'nv', 'age': 13}
    for (var e in json) {
        log(e)
        log(json[e])
    }
    json['newE'] = 'newEv'
    log(json)
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
                json[kv[0]] = kv[1]
            }
        }
    }
    return json
}

/**
 * 将参数追加到url
 * @param url
 * @param paramN
 * @param paramV
 * @returns {string|*}
 */
var appendParamToUrl = function (url, paramN, paramV) {
    url += (url.indexOf("?") == -1 ? "?" : "&" ) + paramN + "=" + paramV
    return url
}

/**
 * 遍历json ele并追加到url作为参数
 * @param url
 * @param paramJson
 * @returns {string|*}
 */
var appendParamJsonToUrl = function (url, paramJson) {
    var paraStr = ''
    for (var k in paramJson) {
        log(k)
        var v = paramJson[k]
        log(v)
        if (!ObjectCommonUtil.isEmpty(v)) {
            paraStr += k+"="+v+"&"
        }
    }
    url += (url.indexOf("?") == -1 ? "?" : "&" ) + paraStr
    return url
}


/**
 * 重置页面高度
 * @param menuId
 */
var resizeContentHeight = function (menuId) {
    log('resize')
    // 左侧菜单栏高度
    var sidebarHeight = $(".admin-sidebar").height()
    //log(sidebarHeight)
    // menuId对应的panel高度
    var panelHeight
    if (menuId == undefined || menuId == "") {
        //$('.topbar .top-menu ul .active a').attr('aria-controls')
        menuId = $(".admin-main .main-panel .active .daohang .daohang-tabs-menu .am-active").data("menuid")
    }
    panelHeight = $(".admin-main .main-panel .active .daohang .daohang-panels .menu-" + menuId).height()
    var tabsHeight = $(".admin-main .main-panel .active .daohang .daohang-tabs-menu").height() + panelHeight
    //log(tabsHeight)
    //log(window.innerHeight - $("header").height() - $("footer").height())
    var max = Math.max(sidebarHeight, tabsHeight, window.innerHeight - $("header").height() - $("footer").height())
    //log($("header").height() + " " + window.innerHeight)
    //log(max)
    $(".daohang").css("height", max)
}
/**
 * 在其他地方切换到某叶子菜单，包括topbar menu，main-panel，内部menutab, 页面参数设置
 * @param businessTypeParam
 * @param level2menuId
 * @param paramJson
 */
var openTabMenu = function (businessTypeParam, level2menuId, paramJson) {
    if (ObjectCommonUtil.isUndefined(paramJson)) {
        paramJson = {}
    }
    var businessType, level1MenuId, finded = false;
    var menuJson = _context.menuJson
    // level2menuId 有数据则处理
    if (undefined != level2menuId && "" != level2menuId) {
        for (var businessi = 0; businessi < menuJson.length & !finded; businessi++) {
            var business = menuJson[businessi]
            for (var level1i = 0; level1i < business.menus.length & !finded; level1i++) {
                var level1Menu = business.menus[level1i]
                for (var level2i = 0; level2i < level1Menu.menus.length & !finded; level2i++) {
                    var level2Menu = level1Menu.menus[level2i]
                    if (level2Menu.menuId == level2menuId) {
                        businessType = business.menuId
                        level1MenuId = level1Menu.menuId
                        finded = true
                        break
                    }
                }
            }
        }
    }
    // 如果没有找到level2menuId的层级数据，则使用入参businessType
    if (undefined == level1MenuId && undefined != businessTypeParam) {
        businessType = businessTypeParam
        finded = true
    }
    if (finded) {
        if (undefined != level1MenuId) {
            $("#main-tab-" + businessType).data('hasTargetLeaf', true)
        }
        $("#main-tab-" + businessType).tab("show")
        if (undefined != level1MenuId) {
            paramJson['menuId']  = level2menuId
            jumpContextData.setJumpContextData(paramJson)
            $(".main-panel-" + businessType + "-content .sideMenu h5[data-menuid='" + level1MenuId + "']").click()
            $(".main-panel-" + businessType + "-content .sideMenu .submenu[data-menuid='" + level2menuId + "']").click()
        }
    } else {
        $(".admin-header .top-menu li:first a").tab("show")
    }

    goTop()
}

var closeCurrentTabMenu = function () {
    $(".main-panel .active .daohang-tabs-menu .am-active .am-icon-close").click()
}

/**
 * 从浏览器当前url获取参数名为name的参数数据
 * @param name
 * @returns {*}
 */
var getParamFromUrl = function (name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);//search,查询？后面的参数，并匹配正则
    if (r != null) {
        return unescape(r[2]);
    }
    return null;
}

var canLog = true
var log = function (v) {
    if (canLog) {
        console.log(v)
    }
}

// 全局性数据,
// menuJson 菜单信息
// businessType2FormSubmitUrl{业务类型:{formId_SubmitId:url}}
var _context = {
    menuJson:{},
    businessType2FormSubmitUrl:{},
    cate:{},
    needRefreshCate: true
}

//
/**
 * 使用复用菜单方式的panel加载完成后，对menu进行事件绑定
 * @param newContentClass
 */
var afterReUsedMenuPanelLoad = function (newContentClass) {
    var rootDiv = $("." + newContentClass);
    // parent菜单 绑定事件
    var menuBindEvent = function () {
        // 鼠标进入事件：下滑显示
        rootDiv.find(".sideMenu").on("click", "h5", function () {
            if ($(this).hasClass("on")) {
                return false;
            }
            var menuId = $(this).data("menuid");
            rootDiv.find(".sideMenu .on").removeClass("on");
            rootDiv.find(".sideMenu ul:not(" + menuId + ")").slideUp()
            $(this).addClass("on")
            rootDiv.find(".sideMenu ." + menuId).slideDown(resizeContentHeight)
        })
    }

    // 末梢子菜单 绑定事件
    var subMenuBindEventHandler = function () {
        // 动态添加标签页
        rootDiv.find('.sideMenu').on('click', ".submenu", function (e) {
            if ($(this).data("targettype") == "blank") {
                // 判断真实点击
                if (e.hasOwnProperty('originalEvent')) {
                    // Probably a real click.
                    window.open(window.location.origin + window.location.pathname + "?menuId=" + $(this).data("menuid"), "_blank")
                    return false
                }
            }

            addTab($(this));
        });
    }

    var loadMenu = function () {
        // TODO 调用接口获取menu数据，组织成menu

        menuBindEvent()
        subMenuBindEventHandler()
    }

    loadMenu()

    //
    /**
     * panel加载url页面
     * @param menuId
     * @param url
     */
    var loadPanelContent = function (menuId, url) {
        var clazz = "menu-" + menuId
        url = appendParamToUrl(url, "tv", new Date().getTime())
        rootDiv.find(".am-tabs .am-tabs-bd ." + clazz).load(url, function (response, status, xhr) {
            if (JsonUtil.isJson(response)) {
                castErrorMsg(response)
            }

            needGoLogin(response)

            resizeContentHeight(menuId)

            /*var paramJson = jumpContextData.getJumpContextData()
            log('loadPanelContent')
            log(paramJson)
            if (menuId == paramJson['menuId']) {
                for (var k in paramJson){
                    $('.menu-' + menuId).find("[name='"+k+"']").val(paramJson[k])
                }
            }
            jumpContextData.removeJumpContextData()*/
        });
    }

    //
    /**
     * tab 绑定事件
     */
    var tabMenuBindEventHandler = function () {
        // tab click事件，激活后，重新设置页面高度
        $nav.on("click", "li", function () {
            resizeContentHeight($(this).data("menuid"))
        })

        // 移除标签页
        $nav.on('click', '.am-icon-close', function () {
            var $item = $(this).closest('li');
            var index = $nav.children('li').index($item);

            $item.remove();
            $bd.find('.am-tab-panel').eq(index).remove();
            tabCounter--;

            $tab.tabs('open', index > 0 ? index - 1 : index + 1);
            $tab.tabs('refresh');

            resizeContentHeight()
            return false;
        });
    }

    //
    /**
     * 创建tab与panel并激活
     * @param queryObjectOfThis
     * @returns {boolean}
     */
    var addTab = function (queryObjectOfThis) {
        var url = queryObjectOfThis.data("url")
        var menuId = queryObjectOfThis.data("menuid")
        var clazz = "menu-" + menuId
        if (rootDiv.find(".am-tabs .tabs-nav ." + clazz).length > 0) {
            log("exist : " + clazz)

            if (StringUtil.isNotEmpty(jumpContextData.getJumpContextData()['menuId'])){
                rootDiv.find(".am-tabs .tabs-nav ." + clazz + " .am-icon-close").click()
                log('jump to ' + menuId + ',close exist tab')
            }else {
                // open tab
                var tabIndex = rootDiv.find(".am-tabs .tabs-nav ." + clazz).data("index")
                log("tabIndex : " + tabIndex)
                $tab.tabs('open', tabIndex)
                return false;
            }
        }

        // new + open tab
        var tabIndex = tabCounter++;
        var content = '<div class="am-tab-panel ' + clazz + '">动态插入的标签内容' + tabIndex + '</div>';
        var nav = '<li class="' + clazz + '" data-menuid="' + menuId + '" data-index="' + tabIndex + '"><a type="button" class="am-btn am-btn-default am-radius am-btn-xs">' + queryObjectOfThis.text() + '<span class="am-icon-close"></span></a></li>';
        $nav.append(nav);
        $bd.append(content);
        $tab.tabs('refresh');
        $tab.tabs('open', tabIndex)
        loadPanelContent(menuId, url)
    }


    var tabCounter = 0;
    var $tab = rootDiv.find('#doc-tab-demo-1');
    var $nav = $tab.find('.am-tabs-nav');
    var $bd = $tab.find('.am-tabs-bd');
    var initTabCount = $tab.find(".tabs-nav li").length;
    tabCounter += initTabCount > 0 ? (initTabCount - 1) : 0;
    tabMenuBindEventHandler()

    // 加载首个tab panel 内容
    /*if ($nav.find("li").length > 0){
        loadPanelContent($nav.find("li:first").data("menuid"), "dashboard.html")
    }*/
}

//
/**
 * 业务类型对应的菜单信息
 * @param businessType
 * @returns {*}
 */
var getMenuInfo = function (businessType) {
    var menuJson = _context.menuJson
    for (var i = 0; i < menuJson.length; i++) {
        if (menuJson[i].menuId == businessType) {
            return menuJson[i]
        }
    }
}

//
/**
 * 在新panelcontent中追加menus菜单
 * @param businessType
 * @param panelContentClass
 */
var genAndAppendMenus = function (businessType, panelContentClass) {
    var menuInfo = getMenuInfo(businessType)

    var menuEle = ""
    for (var i = 0; i < menuInfo.menus.length; i++) {
        var level1menu = menuInfo.menus[i]
        if (level1menu.show) {
            menuEle += "<div><h5 data-menuid='" + level1menu.menuId + "'><em></em>" + level1menu.name + "</h5><ul class='" + level1menu.menuId + "'>";
        }else {
            menuEle += "<div style='display: none;'><h5 data-menuid='" + level1menu.menuId + "'><em></em>" + level1menu.name + "</h5><ul class='" + level1menu.menuId + "'>";
        }
        for (var j = 0; j < level1menu.menus.length; j++) {
            var level2menu = level1menu.menus[j]
            if (level2menu.show) {
                menuEle += "<li class='submenu' data-url='" + businessType+"/"+level1menu.menuId+"/"+level2menu.url + "' data-menuid='" + level2menu.menuId + "' data-targettype='" + level2menu.target + "'>" + level2menu.name + "</li>";
            }else {
                menuEle += "<li style='display: none;' class='submenu' data-url='" + businessType+"/"+level1menu.menuId+"/"+level2menu.url + "' data-menuid='" + level2menu.menuId + "' data-targettype='" + level2menu.target + "'>" + level2menu.name + "</li>";
            }
        }
        menuEle += "</ul></div>"
    }

    $("." + panelContentClass + " .sideMenu").append(menuEle)
}

var needGoLogin = function (data) {
    if (typeof data != 'string'){
        data = data.responseText
    }
    if(data.indexOf('noLogin') > -1 || data.indexOf("loginPage") > -1){
        window.location.href = window.location.origin + '/wm/login.html'
        return false
    }
}

var AjaxUtil = {
    config:{/*async: false, */dataType: "json"},
    /**
     * 默认ajax config:{dataType: "json", headers:{}}
     * mask 是否需要显示蒙版,默认true
     * param{url:,config:{},doneCallback:function,failCallback:function,mask:true/false}
     */
    doAjax:function (param) {
        var url = param.url
        var config = JsonUtil.simpleFit(AjaxUtil.config, param.config)
        var doneCallback = param.doneCallback
        var failCallback = param.failCallback
        if (ObjectCommonUtil.isEmpty(param.mask)){
            param.mask = true
        }
        if (ObjectCommonUtil.isNotUndefined(param.data)) {
            config.data = param.data
        }
        AjaxUtil.pre(param)
        $.ajax(url, config)
            .done(function (data) {
                AjaxUtil.after(param)
                if (typeof doneCallback == 'function'){
                    doneCallback(data)
                }
            })
            .fail(function (data) {
                log(" fail " + data)
                AjaxUtil.after(param)

                needGoLogin(data)

                /*if(data.responseText.indexOf('noLogin') > -1 || data.responseText.indexOf("loginPage") > -1){
                    window.location.href = window.location.origin + '/wm/login.html'
                    return false
                }*/
                if (typeof failCallback == 'function'){
                    failCallback(data)
                }
            })
    },
    pre : function (param) {
        log('pre')
        log(param)
        if (param.mask) {
            showMask()
        }
    },
   after : function (param) {
        log('after')
        log(param)
       if (param.mask) {
           closeMask()
       }
    }
}

var showMask = function () {
    $('#mask-before-response').modal()
}
var closeMask = function () {
    $('#mask-before-response').modal('close')
}

// 加载所有menu信息
var menusData
var doReady = function (paramJson) {
    resizeContentHeight()

    var menuUrl = paramJson.menuUrl
    var mockMenusJsonStr = paramJson.mockMenusJsonStr
    var submitUrl = paramJson.submitUrl
    AjaxUtil.doAjax({
        url:menuUrl,
        doneCallback:function (data) {
            menusData = data
            if (test.mock){
                data.d = mockMenusJsonStr
            }
            if (typeof(data.d ) == 'string') {
                _context.menuJson = $.parseJSON(data.d)
            }else {
                _context.menuJson = data.d
            }
            log("done " + data)
            getMenusAjaxCallback()
        },
        failCallback:function (data) {
            menusData = data.responseJSON
            log("fail " + data)
            getMenusAjaxCallback()
        },
        mask: true
    })

    CateUtils.load()

    var getMenusAjaxCallback = function () {
        // 菜单数据加载失败，则提示并阻止后续步骤
        var menuJson = _context.menuJson
        if (undefined == menuJson || "" == menuJson) {
            castErrorMsg("缺少菜单数据，请确认已购买套餐。")
            throw new DOMException()
        }

        // 动态增加topbar menu
        $.each(menuJson, function (index, data) {
            log(index + " data " + data)
            var businessType = data.menuId;
            var mainPanelId = "main-panel-" + businessType;
            var target = data.target
            var newBar = "<li role=\"presentation\"><a class='my-top-tab' href=\"#" + mainPanelId + "\" id=\"main-tab-" + businessType + "\" aria-controls=\"" + mainPanelId + "\" role=\"tab\" data-toggle=\"tab\" data-loadtype=\"reUsedMenuDiv\" data-businesstype=\"" + businessType + "\" data-targettype=\"" + target + "\">" + data.name + "</a></li>"
            $(".topbar .top-menu .nav").append(newBar)
            $(".admin-main .main-panel").append("<div role=\"tabpanel\" class=\"tab-pane\" id=\"" + mainPanelId + "\"></div>")
        })

        // topbar menu tab 事件
        $('.topbar .my-top-tab').on('click', function (e) {
            if ($(this).parent().hasClass('active')) {
                return false;
            }
            var target = $(this).data("targettype")
            if (target == 'blank') {
                window.open(window.location.origin + window.location.pathname + "?businessType=" + $(this).data("businesstype"), "_blank")
                return false
            }
        })
        $('.topbar .my-top-tab').on('shown.bs.tab', function (e) {
            addSubmitUrl(submitUrl)
            if (!$(e.target.hash).data("hasload")) {
                if ($(this).data("loadtype") == "loadUrl") {
                    // 直接在topbar对应的panel中加载url
                    var url = $(this).data("url")
                    appendParamToUrl(url, "vt", new Date().getTime())
                    $(e.target.hash).load(url, function (response, status, xhr) {
                        if (JsonUtil.isJson(response)) {
                            castErrorMsg(response)
                        }

                        needGoLogin(response)

                        $(e.target.hash).data("hasload", true)
                    });
                } else if ($(this).data("loadtype") == "reUsedMenuDiv") {
                    // 在topbar对应的panel中菜单与菜单对应panel
                    // 复用menu-content div
                    // 设置menu
                    // menu绑定事件
                    var newPanelContent = $(".menu-content-template").clone(false, false)
                    var newClass = $(this).attr("aria-controls") + "-content"
                    newPanelContent.removeClass("menu-content-template").addClass(newClass).css("display", "")
                    newPanelContent.appendTo($(e.target.hash))

                    genAndAppendMenus($(this).data("businesstype"), newClass)

                    afterReUsedMenuPanelLoad(newClass)

                    // 是否有目标叶子菜单
                    if(ObjectCommonUtil.isUndefined($(this).data('hasTargetLeaf'))){
                        $("." + newClass + " .sideMenu h5:first").click()
                        $("." + newClass + " .sideMenu .submenu:first").click()
                    }

                    $(e.target.hash).data("hasload", true)
                } else {
                    log("loadtype " + $(this).data("loadtype"))
                }
            }
            resizeContentHeight()
        })

        bindEleEvent()

        jumpContextData.initJumpContextData()

        // 从url获取目标menu坐标以及参数
        openTabMenu(getParamFromUrl("businessType"), getParamFromUrl("menuId"), genJsonFromUrlParams())

        resizeContentHeight()
        log('readyMenu done')
    }

    fileUploadUtils.initCloseEvent()
}
/**
 *
 * 类目相关操作
 */
var CateUtils = {
    isNotSelected: function(val){
        return ObjectCommonUtil.isEmpty(val) || -1 == val
    },
    selected: function(parentSelector, selectedVal){
        var parent
        if (typeof parentSelector === 'string') {
            parent = $(parentSelector)
        }else {
            parent = parentSelector
        }
        parent.find('.cate-select').next('.am-selected').find('ul li').each(function () {
            if ($(this).data('value') == selectedVal){
                $(this).click()
            }
        })
    },
    clear: function(parentSelector){
        var parent
        if (typeof parentSelector === 'string') {
            parent = $(parentSelector)
        }else {
            parent = parentSelector
        }
        parent.find('.cate-select').next('.am-selected').find('ul li').eq(0).click()
    },
    refreshCateSelect: function(){
        var selecter = '.cate-select'
        if (!ObjectCommonUtil.isEmpty(selecter)){
            var select = $(selecter)
            if (ObjectCommonUtil.isNotUndefined(select)) {
                select.children().remove()
                select.append("<option value='-1'>不选择</option>")
                for (var index in _context.cate){
                    var ls = ''
                    if (_context.cate[index]['level'] > 1){
                        for (var i = 2; i<= _context.cate[index]['level']; i++) {
                            ls += '&nbsp;&nbsp;'
                        }
                    }
                    select.append("<option value='"+_context.cate[index]['id']+"'>"+ls+_context.cate[index]['name']+"</option>")
                }
                select.selected({searchBox: 1,btnStyle:'primary',btnSize:'sm',maxHeight: 200})

                $(selecter).on("change", function () {
                    if($(this).val() == -1){
                        $(this).val("")
                    }
                })
            }
        }
    },
    /**confirmed
     * 重新加载类目信息，可根据seleter定为select并刷新其options
     */
    load : function () {
        if (_context.needRefreshCate) {
            AjaxUtil.doAjax({
                url: '/p/wm/cate/simpleList',
                doneCallback:function(result){
                    if (!ObjectCommonUtil.isEmpty(result.d)){
                        _context.cate = result.d
                        CateUtils.refreshCateSelect()
                        _context.needRefreshCate = false
                    }else {
                        castErrorMsg("加载类目信息失败")
                        throw new DOMException()
                    }
                },
                failCallback:function (data) {
                    castErrorMsg(data)
                    throw new DOMException()
                }
            })
        }else {
            CateUtils.refreshCateSelect()
        }

    }
}

/**
 * 页面跳转url参数作为context，作用为跳转页面可以获取到参数，使用见doReady 和 addTab,
 * 每个功能页面执行最初js时都可以get一下，以便判断是否为其他页面跳过来的
 * @type {{contextData: {}, getJumpContextData: (function(): ({}|jumpContextData.contextData|*)), removeJumpContextData: (function(): {}), initJumpContextData: jumpContextData.initJumpContextData}}
 */
var jumpContextData = {
    contextData:{},
    getJumpContextData:function () {
        log('getJumpContextData')
        log(this.contextData)
        var data = this.contextData
        return data
    },
    removeJumpContextData:function () {
        log('removeJumpContextData')
        return this.contextData = {}
    },
    initJumpContextData:function () {
        this.contextData = genJsonFromUrlParams()
        log('initJumpContextData')
        log(this.contextData)
    },
    setJumpContextData: function (data) {
        this.contextData = data
        log('setJumpContextData')
        log(this.contextData)
    },
    /**
     * 功能页面首先应执行该扩展函数
     * @param func
     */
    dealJumpContextData:function (func) {
        func(this.contextData)
        this.removeJumpContextData()
    }
}

//
/**
 * 其他元素绑定事件
 */
var bindEleEvent = function () {
    $('header').on('click', '#admin-fullscreen', function() {
        log($.AMUI.fullscreen.enabled)
        if ($.AMUI.fullscreen.enabled) {
            $.AMUI.fullscreen.toggle();
        }
    })

    //$('[data-toggle="tooltip"]').tooltip({trigger:'hover'})
    $(document).on('mouseenter', '[data-toggle="tooltip"]', function () {
        $(this).tooltip('show')
    })

    bindEvent4LoadContent()

    // 具有class jumped 按钮或链接绑定点击事件，决定如何跳转，如新页面、本页新tab等。须要的属性：data-targettype='blank/openTabMenu' data-menuid='叶子菜单menuid' data-param='paramJson'
    $('.admin-main').on('click', '.jumped', function () {
        if ($(this).data("targettype") == "blank") {
            // 新页面打开
            var paramJson = JsonUtil.toJson($(this).data("param"));
            paramJson['menuId'] = $(this).data("menuid")
            var url = window.location.origin + window.location.pathname
            url = appendParamJsonToUrl(url, paramJson)
            window.open(url, "_blank")
            return false
        }

        if ($(this).data("targettype") == "openTabMenu") {
            openTabMenu(null, $(this).data("menuid"), JsonUtil.toJson($(this).data("param")));
            return false
        }

    })
    /**
     * 元素点击后显示modal并设置modal中输入框数据.
     * 要求modal中输入框元素name与元素data('context')中数据key一致
     * 元素需添加class openModal
     * 元素和modal在同一个.loadedContent范围内
     */
    $('.admin-main').on('click', '.openModal', function () {
        // 验证是否有modalid数据
        if (ObjectCommonUtil.isEmpty($(this).data('modalid'))) {
            castErrorMsg('该按钮缺少modalid数据')
            return false
        }
        var loadedContent = $(this).parents('.loadedContent:first')
        if (ObjectCommonUtil.isUndefined(loadedContent)){
            return false
        }
        var modal = loadedContent.find('#'+$(this).data('modalid'))
        if (ObjectCommonUtil.isUndefined(modal)){
            castErrorMsg('该按钮关联的弹出框不存在')
            return false
        }

        modal.find('.form-control').each(function () {
            $(this).val("")
        })

        if (!ObjectCommonUtil.isEmpty($(this).data('context'))){
            var context = $(this).data('context')
            log(context)
            for (var key in context){
                log(key)
                FormUtil.fillVal(modal, key, context[key])
                /*modal.find('[name="'+ key +'"]').val(context[key])
                log(modal.find('[name="'+ key +'"]').val())
                log( modal.find('[name="'+ key +'"]'))*/
            }
        }
    })

    /**
     * 元素点击出现确认框，确认后ajax post行为，需要为元素设置data param 为json，通过data('param', {xxxx})
     */
    $('.admin-main').on('click', '.confirmed', function () {
        // 获取元素相关属性
        var optId = $(this).data('optid')
        if (ObjectCommonUtil.isEmpty(optId)) {
            castErrorMsg("刚点击的按钮缺少配置optId，无法执行。")
            return false
        }
        var msg = $(this).data('msg')
        if (ObjectCommonUtil.isEmpty(msg)) {
            castErrorMsg("刚点击的按钮缺少配置msg，无法执行。")
            return false
        }

        var param
        if (typeof $(this).data('genParam') == 'function') {
            param = $(this).data('genParam')(this)
        }
        if (ObjectCommonUtil.isEmpty(param)){
            param = $(this).data('param')
        }
        log(param)
        if (ObjectCommonUtil.isEmpty(param)) {
            castErrorMsg("刚点击的按钮缺少数据param，无法执行。")
            return false
        }
        $(this).data('param', param)

        var paramDesc = ''
        if (typeof $(this).data('genParamDesc') == 'function') {
            paramDesc = $(this).data('genParamDesc')()
        }

        var url = getSubmitUrl($(this).parents('.loadedContent:first').attr('id'), optId)
        if (ObjectCommonUtil.isEmpty(url)) {
            castErrorMsg("刚点击的按钮缺少配置url，无法执行。")
            return false
        }
        $(this).data('url', url)
        // 设置confirmModel显示内容
        $('#confirmModel').find('.am-modal-hd:first #msg').html(msg)
        $('#confirmModel').find('.am-modal-bd:first #param').val(StringUtil.isNotEmpty(paramDesc) ? paramDesc : JsonUtil.toString(param))
        //$('#confirmModel').find('.am-modal-bd:first').html(msg + "<br/>" + JsonUtil.toString(param))
        // 显示
        $('#confirmModel').modal({
            relatedTarget: this,
            onConfirm: function(options) {
                var url = $(this.relatedTarget).data('url')
                AjaxUtil.doAjax({
                    url:appendParamJsonToUrl(url, $(this.relatedTarget).data('param')),
                    config:{method:'POST'},
                    doneCallback: $(this.relatedTarget).data('doneCallback')
                })
            },
            onCancel: function() {}
        })
    })
}

/**
 * 具体table checked checkbox的数据，使用,分割
 * @param tableJqueryObject
 * @returns {string}
 */
var getCheckedValsFromTable = function (tableJqueryObject) {
    var vals = []
    if ( tableJqueryObject.find('.tableCheckEle:checked').length > 0){
        tableJqueryObject.find('.tableCheckEle:checked').each(function () {
            vals.push($(this).val())
        })
    }
    if (vals.length > 0){
        return vals.join(',')
    }
    return ''
}
var cleanCheckedFromTable = function (tableJqueryObject) {
    if ( tableJqueryObject.find('.tableCheckEle:checked').length > 0){
        tableJqueryObject.find('.tableCheckEle:checked').each(function () {
            $(this).click()
        })
    }
    return false
}

var cleanForm = function (selector) {
    $(selector + " .form-control").val("")
}

//
/**
 * 当前top menu对应的业务类型
 * @returns {jQuery}
 */
var getCurrentBusType = function () {
    return $('.admin-header .top-menu .nav .active a').data('businesstype')
}

//
/**
 * 抛出错误信息
 * @param data
 */
var castErrorMsg = function (data) {
    if (data.m != undefined) {
        data = data.m
    }
    if (data.responseText != undefined) {
       data = data.responseText
    }

    var msg = data
    if (typeof  msg == 'string'){
        if (JsonUtil.isJson(msg)){
            var json = JSON.parse(msg)
            if (ObjectCommonUtil.isNotUndefined(json.errors)) {
                msg = unescape(json.errors[0].defaultMessage.replace(/\\\\u/g, "%u"))
            }else if (ObjectCommonUtil.isNotUndefined(json.m)) {
                msg = unescape(json.m)
            }
        }
    } else {
        msg = JSON.stringify(msg)
    }
    //var msg = unescape(JSON.stringify(data).replace(/\\\\u/g, "%u").replace(/\\"/g, ""))

    msgAlert(msg)
}

var showInfo = function (info) {
    var html = info.content
    if (ObjectCommonUtil.isNotUndefined(info.url)) {
        var desc = ObjectCommonUtil.isUndefined(info.urlDesc) ? '链接' : info.urlDesc
        html += '<a href="' + info.url + '" target="_blank">' + desc + '</a>'
    }
    $('#alert-info .am-modal-hd').text(info.title);
    $('#alert-info .am-modal-bd').html(html)
    $('#alert-info').modal('toggle');
}

var msgAlert = function (msg) {
    $('#err-msg-alert .am-modal-bd').text(msg)
    $('#err-msg-alert').modal("open");
    return false
}

var castSuccessMsg = function (data) {
    if (typeof  data == 'string'){
        return msgAlert(data)
    }
    return msgAlert(JSON.stringify(data))
}

/**
 * form 通用处理
 * form数据获取通用方法
 * form绑定submit callback，支持根据返回值个性化处理
 * form绑定verify fun，支持表单验证扩展
 * .submit click事件：[表单验证接口]->获取form数据->获取form url并将返回值绑定到form->调用form submit回调函数
 * 获取并维护businessType formId submitId url关系
* */
// 获取form 数据,返回json形式
var getFormDataJson = function (pageId, fromId) {
    var json = {}
    $('#'+pageId).find("#" + fromId + " .form-control").each(function (i) {
        log(i + " " + $(this).attr('name') + " " + $(this).val())
        if((this.type == 'radio' || this.type == 'checkbox') && !this.checked){
            return
        }

        if (undefined != $(this).val() && "" != $(this).val()){
            json[$(this).attr('name')] = $(this).val()
        }
    })
    return json
}
/**
 form 添加回调，用于个性化扩展。
 {
 pageId: pageId
 formId: formId
 submitId: submitId
 post:true 使用method post;默认get
 formVerify: form submit数据验证
 submitDataAppend: submit提交数据补充处理
 submitResultDeal: submit请求结果处理,结果为请求返回的原始数据
 headers:
 }
  **/
var bindFormCallback = function (param) {
    if (undefined == param){
        return false
    }
    var pageId = param.pageId
    var formId = param.formId
    var submitId = param.submitId
    var post = param.post
    var formVerify = param.formVerify
    var submitDataAppend = param.submitDataAppend
    var submitResultCallback = param.submitResultDeal
    if (post){
        $('#' + pageId).find(('#' + formId)).data('post_' + submitId,post)
    }
    if (undefined != param.headers) {
        $('#' + pageId).find(('#' + formId)).data('headers_' + submitId,param.headers)
    }
    if (undefined != formVerify) {
        $('#' + pageId).find(('#' + formId)).data('verifyFun_' + submitId,formVerify)
    }
    if (undefined != submitDataAppend) {
        $('#' + pageId).find(('#' + formId)).data('dataAppendFun_' + submitId,submitDataAppend)
    }
    if (undefined != submitResultCallback) {
        $('#' + pageId).find(('#' + formId)).data('callbackForSubmit_' + submitId,submitResultCallback)
    }
    log('formId callback')
}
/**
 *
 * @param param
 */
var bindNavTabCallback = function (param) {
    $('#'+param.pageId).find(('#'+param.navId)).data('navShown', param.navShown)
}

/**
 *
 * 功能页面，标签页且每个标签页对应一个table，公共方法
 */
var tablesInNavTab = {
    /**
     * 展示或重新加载table
     * @param navTabListId 标签页列表Id
     */
    showOrReloadTable:function (navTabListId) {
        var activeTab = $('#'+navTabListId+' .active a')
        if (activeTab.length == 0){
            $('#'+navTabListId+' li:first a').tab('show')
        } else {
            var tablePanelId = activeTab.attr('aria-controls')
            $('#'+navTabListId+' li a').data('hasLoad') ? $('#'+navTabListId+' li a').data('hasLoad', false) : null;
            $('#'+tablePanelId + " table:first").DataTable().ajax.reload()
        }
    }
}

/**
 * 为.confirmed元素绑定特定数据
 * selector 添加.confirmed的元素选择器，'.xxx .confirmed'
 * doneCallback  点击确认并ajax请求完成后，自定义执行内容
 * genParam  自定义数据获取方式；未设置，则data('param')获取
 */
var bindConfirmedCallback = function (param) {
    $(param.selector).each(function () {
        if (ObjectCommonUtil.isNotUndefined(param.doneCallback)) {
            $(this).data('doneCallback', param.doneCallback)
        }
        if (ObjectCommonUtil.isNotUndefined(param.genParam)) {
            $(this).data('genParam', param.genParam)
        }
        if (ObjectCommonUtil.isNotUndefined(param.genParamDesc)) {
            $(this).data('genParamDesc', param.genParamDesc)
        }
    })
}

//
/**
 * businessTpey维度查询submit url关系并追加到context
 * @param businessTypeSubmitUrl 获取opturls数据的url
 * @returns {boolean}
 */
var addSubmitUrl = function (businessTypeSubmitUrl) {
    var businessType2FormSubmitUrl = _context.businessType2FormSubmitUrl
    if (undefined == getCurrentBusType() || (undefined != businessType2FormSubmitUrl[getCurrentBusType()] && "" != businessType2FormSubmitUrl[getCurrentBusType()])){
        return false
    }
    var url = appendParamToUrl(businessTypeSubmitUrl, 'businessType', getCurrentBusType())
    AjaxUtil.doAjax({
        url:url,
        config : {
            async : false
        },
        doneCallback:function (data) {
            if (data.s && data.d != undefined && data.d != ''){
                if (undefined == businessType2FormSubmitUrl[getCurrentBusType()]){
                    if (test.mock){
                        data.d = "{\"orderList_query\":\"order/statusCount\",\"orderList_list\":\"order/list\",\"orderList_orderDel\":\"order/del\",\"orderDetail_query\":\"order/detail\",\"orderDetail_submitNote\":\"order/modifyNote\",\"cateList_query\":\"cate/list\",\"cateList_cateDel\":\"cate/del\",\"cateList_saveCate\":\"cate/save\",\"itemList_query\":\"item/spaceCount\",\"itemList_list\":\"item/list\",\"itemList_del\":\"item/del\",\"itemDetail_query\":\"item/detail\",\"itemEdit_query\":\"item/detail\",\"itemEdit_save\":\"item/save\",\"shopOverview_query\":\"shop/query\",\"shopSetting_save\":\"shop/save\",\"shopSetting_query\":\"shop/query\",\"smsList_query\":\"sms/list\",\"smsRechargeList_query\":\"sms/listCharge\",\"smsRecharge_query\":\"sms/count\",\"smsRecharge_sets\":\"sms/sets\",\"smsRecharge_charge\":\"sms/charge\"}"
                    }
                    if (typeof(data.d ) == 'string') {
                        businessType2FormSubmitUrl[getCurrentBusType()] = $.parseJSON(data.d)
                    }else {
                        businessType2FormSubmitUrl[getCurrentBusType()] = data.d
                    }
                    log(businessType2FormSubmitUrl)
                }
            }else {
                castErrorMsg("缺少获取到业务类型-submitUrl关系 " + data.m)
            }
        },
        failCallback:function (data) {
            //menusData = data.responseJSON
            castErrorMsg(data)
        }
    })
}

//
/**
 * pageId submit-url关系数据
 * @param pageId
 * @param submitId
 * @returns {*}
 */
var getSubmitUrl = function (pageId, optId) {
    var businessType2FormSubmitUrl = _context.businessType2FormSubmitUrl
    if (undefined != businessType2FormSubmitUrl[getCurrentBusType()]) {
        return '/p/wm/' + businessType2FormSubmitUrl[getCurrentBusType()][pageId + '_' + optId]
    }
}
//
/**
 * 为功能页面.loadedContent绑定事件
 */
var bindEvent4LoadContent = function () {
    var submitCommonFun = function () {
        var pageId = $(this).parents(".loadedContent:first").attr("id")
        var formId = $(this).parents("form:first").attr("id")
        var submitId = $(this).attr('id')
        log('pageId ' + pageId)
        log('formId ' + formId)
        log('submitId ' + submitId)
        // 获取对应的url
        var url = getSubmitUrl(pageId, submitId)
        if (undefined == url){
            //castErrorMsg("缺少submitUrl")
            return false
        }
        // 获取form data并补充
        var data = getFormDataJson(pageId, formId)
        var dataAppendFun = $('#' + pageId).find(('#' + formId)).data('dataAppendFun_' + submitId)
        if (typeof dataAppendFun == "function") {
            dataAppendFun(data)
        }
        log(data)
        // 验证
        var verifyFun = $('#' + pageId).find(('#' + formId)).data('verifyFun_' + submitId)
        if (typeof verifyFun == "function") {
            if (!verifyFun(data)){
                castErrorMsg("请补全必填信息")
                return false
            }
        }
        var config = {}
        var isPost = $('#' + pageId).find(('#' + formId)).data('post_' + submitId)
        if (isPost){
            //config = {method:'POST'}
            config.method = 'POST'
        }
        if (ObjectCommonUtil.isNotUndefined($('#' + pageId).find(('#' + formId)).data('headers_' + submitId))){
            //config = {method:'POST'}
            config.headers = $('#' + pageId).find(('#' + formId)).data('headers_' + submitId)
        }

        // ajax访问url并调用form submit callback

        var result
        AjaxUtil.doAjax({
            url: url/*isPost ? url : appendParamJsonToUrl(url, data)*/,
            data:data,
            config:config,
            doneCallback:function (data) {
                log('doAjsx doneCallback')
                result = data
                log(result)
                if (undefined == result){
                    return false
                }

                if (!result.s) {
                    castErrorMsg(result.m)
                    return false
                }

                var callback = $('#' + formId).data('callbackForSubmit_'+submitId)
                if (typeof callback == "function") {
                    callback(result)
                }else {
                    log('formId ' + formId + " 无submit callback")
                }

                resizeContentHeight()
            },
            failCallback:function (data) {
                //menusData = data.responseJSON
                return castErrorMsg(data)
            }
        })

        return false
    }
    // 为所有form submit绑定点击事件，form参数->请求获取数据->返回数据
    $(".admin-main").on("click", ".loadedContent form .commonSubmit", submitCommonFun)

    // 为功能页面中nav tab绑定事件
    $(".admin-main").on('shown.bs.tab', '.loadedContent a[data-toggle="tab"]', function (e) {
        var navUlId = $(this).parents('ul:first').attr('id')
        var shownFun = $('#'+navUlId).data('navShown')
        if (typeof shownFun == 'function'){
            shownFun(e.target, e.relatedTarget)
        }
        resizeContentHeight()
    })

    /**
     * 为table中checkbox绑定点击事件,完成全选等
     */
    var bindEvent4CheckAllAndEle = function () {
        $(".admin-main").on("click", ".loadedContent .tableCheckAll", function () {
            var tableCheckAll = $(this)
            var table = tableCheckAll.parents('table:first')
            var checkEles = table.find('.tableCheckEle')
            var checked = tableCheckAll[0].checked
            log('checkAll check  val : ' + checked)
            checkEles.each(function () {
                this.checked = checked
            })
            log(getCheckedValsFromTable(table))
        })

        $(".admin-main").on("click", ".loadedContent .tableCheckEle", function () {
            var tableCheckEle = $(this)
            var table = tableCheckEle.parents('table:first')
            var checkEles = table.find('.tableCheckEle')
            var checkAll = table.find('.tableCheckAll')
            var checked = tableCheckEle[0].checked
            log('checkELe check  val : ' + checked)
            if (checked && table.find('.tableCheckEle:checked').length == checkEles.length) {
                checkAll[0].checked = true
            }else {
                checkAll[0].checked = false
            }
            log(getCheckedValsFromTable(table))
        })
    }

    bindEvent4CheckAllAndEle()


}

////////////数据验证util/////////////////////////////////
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

var JsonUtil = {
    isJson: function(str){
        try {
            JSON.parse(str)
        }catch (e) {
            return false
        }
        return true
    },
    /**
     * 整理单层json(如表单提交的数据)，例如删除empty数据的项
     **/
    simpleFix : function (json) {
        log(json)
        var deleteKey = []
        for (var i in json){
            if (ObjectCommonUtil.isEmpty(json[i])) {
                log(json[i])
                deleteKey.push(i)
            }
        }
        if (deleteKey.length > 0){
            for (var i in deleteKey) {
                delete json[deleteKey[i]]
            }
        }
        log(json)
        return json
    },
    simpleFit:function (json1, json2) {
        var json = {}
        if(ObjectCommonUtil.isNotUndefined(json1)){
            for (var key in json1){
                json[key] = json1[key]
            }
        }
        if(ObjectCommonUtil.isNotUndefined(json2)){
            for (var key in json2){
                json[key] = json2[key]
            }
        }

        return json
    },
    toString:function (json) {
        var param = ''
        for (var key in json){
            param += (key + "=" + json[key] + " ")
        }
        return param
    },
    toJson: function (data) {
        if (typeof(data) == 'string') {
            return $.parseJSON(data)
        }else {
            return data
        }
    }
}

var fileUploadUtils = {
    initCloseEvent:function(){
        $(document).on('click', '.uploaded-imgs .uploaded-img .am-icon-close', function () {
            var that = $(this)
            var imgs = $(this).parent().parent().find('.uploaded-img')
            imgs.each(function (index, ele) {
                if($(ele)[0] == that.parent()[0]){
                    var id = $(ele).data("md5")
                    log(id);
                    var delResult = false
                    // 在图片管理中取删除图片
                    /*AjaxUtil.doAjax({
                        url: "/p/wm/img/del",
                        data: {
                            id: id
                        },
                        config : {
                            async : false,
                            method : 'POST'
                        },
                        doneCallback : function(d) {
                            if (d.s && d.d) {
                                delResult = true;
                            } else {
                                castErrorMsg(d)
                                delResult = false
                            }
                        },
                        failCallback : function (d) {
                            castErrorMsg(d)
                            delResult = false
                        }
                    })*/
                    delResult = true

                    if (!delResult) {
                        return false
                    }
                    var imgsArray = $(this).parent().parent().find('input[name="imgs"]').val().split(',');
                    imgsArray.splice(index, 1)
                    $(this).parent().parent().find('input[name="imgs"]').val(imgsArray.join(','))
                    $(this).parent().parent().find('.uploaded-imgs').data('imgsNum', imgsArray.length)
                    $(ele).remove()
                    return false
                }
            })
        })
    },
    clearImgs: function (selector) {
        var currentFileUpload = $(selector)
        var imgsArray = []
        currentFileUpload.find('input[name="imgs"]').val(imgsArray.join(','))
        currentFileUpload.find('.uploaded-imgs').data('imgsNum', imgsArray.length)
        var imgs = currentFileUpload.find('.uploaded-img')
        imgs.remove()
    },
    /**
     * 参数举例
     * {
     * selecter:'#fileupload',
     * url:'/wm/img/upload',
     * doneCallback:function () {
            log('个性化')
        }
        },
        limitMax:n,
        isImage: true,
     */
    bind:function (param) {
        $(param.selecter).parent().parent().parent().find('.uploaded-imgs').data('imgsNum', 0)
        if (ObjectCommonUtil.isNotUndefined(param.isImage) && param.isImage == false) {
            $(param.selecter).fileupload({
                url: param.url,
                dataType: 'json',
                add: function(e, data){
                    var hasUploadedImgNum = $(param.selecter).parent().parent().parent().find('.uploaded-imgs').data('imgsNum')
                    log(hasUploadedImgNum)
                    var limitMax = ObjectCommonUtil.isNotUndefined(param.limitMax) ? param.limitMax : 1
                    log(data.files.length + hasUploadedImgNum)
                    if (data.files.length + hasUploadedImgNum > limitMax) {
                        castErrorMsg('最多上传'+limitMax+'个文件数量')
                        return false
                    }
                    for (var index in data.files) {
                        if (ObjectCommonUtil.isNotUndefined(param.suffix)) {
                            var searchR = data.files[index].name.search("." +param.suffix);
                            log(searchR)
                            if (searchR < 0){
                                castErrorMsg("只可以上传这些文件，如" + param.suffix)
                                return false
                            }
                        }

                        var maxSize = ObjectCommonUtil.isUndefined(param.size) ? 102400 :  param.size

                        if (data.files[index].size > maxSize) {
                            castErrorMsg("文件大小已经超过"+ maxSize/1024 + "KB")
                            return false
                        }
                    }

                    showMask()

                    param.files = data.files

                    if (data.autoUpload || (data.autoUpload !== false &&
                        $(this).fileupload('option', 'autoUpload'))) {
                        data.process().done(function () {
                            data.submit();
                        });
                    }
                },
                done: function (e, data) {
                    closeMask()

                    var files = param.files
                    param.files = undefined

                    if(!data._response.result.s){
                        return msgAlert(data._response.result.m)
                    }
                    if (ObjectCommonUtil.isNotUndefined(data._response.result)) {
                        if (typeof param.doneCallback == 'function'){
                            param.doneCallback(data._response.result)
                            if (param.appendImg) {
                                fileUploadUtils.appendFiles(param.selecter, files)
                            }
                        } else {
                            log('默认输出，上传完毕')
                            fileUploadUtils.appendFiles(param.selecter, files)
                        }
                    }
                },
                progress: function (e, data) {
                    var progress = parseInt(data.loaded / data.total * 100, 10);
                    log(progress)
                },
                fail: function (e, data) {
                    log(data)
                    closeMask()
                }
            })
        }else {
            $(param.selecter).fileupload({
                url: param.url,
                dataType: 'json',
                add: function(e, data){
                    var hasUploadedImgNum = $(param.selecter).parent().parent().parent().find('.uploaded-imgs').data('imgsNum')
                    log(hasUploadedImgNum)
                    var limitMax = ObjectCommonUtil.isNotUndefined(param.limitMax) ? param.limitMax : 8
                    log(data.files.length + hasUploadedImgNum)
                    if (data.files.length + hasUploadedImgNum > limitMax) {
                        castErrorMsg('最多上传'+limitMax+'个文件数量')
                        return false
                    }
                    for (var index in data.files) {
                        log(data.files[index].name)

                        /*var reader = new FileReader();
                        reader.onload = function (e) {
                            var data = e.target.result;
                            //加载图片获取图片真实宽度和高度
                            var image = new Image();
                            image.onload=function(){
                                var width = image.width;
                                var height = image.height;
                                log("img " + width +  " " + height)
                                return false;
                                // isAllow = width >= Max_Width && height >= Max_Height;
                                // showTip2(isAllow);
                            }
                            image.src= data;
                        };
                        reader.readAsDataURL( data.files[index]);*/



                        var searchR = data.files[index].name.search(/(\.|\/)(gif|jpe?g|png)$/i)
                        log(searchR)
                        if (searchR < 0){
                            castErrorMsg("只可以上传图片")
                            return false
                        }

                        var maxSize = ObjectCommonUtil.isUndefined(param.size) ? 102400 :  param.size

                        if (data.files[index].size > maxSize) {
                            castErrorMsg("图片大小已经超过"+ maxSize/1024 + "KB")
                            return false
                        }
                    }

                    showMask()

                    if (data.autoUpload || (data.autoUpload !== false &&
                        $(this).fileupload('option', 'autoUpload'))) {
                        data.process().done(function () {
                            data.submit();
                        });
                    }
                },
                done: function (e, data) {
                    closeMask()

                    if(!data._response.result.s){
                        return msgAlert(data._response.result.m)
                    }
                    if (ObjectCommonUtil.isNotUndefined(data._response.result)) {
                        if (typeof param.doneCallback == 'function'){
                            param.doneCallback(data._response.result)
                            if (param.appendImg) {
                                fileUploadUtils.appendImgs(param.selecter, data._response.result.d)
                            }
                        } else {
                            log('默认输出，上传完毕')
                            fileUploadUtils.appendImgs(param.selecter, data._response.result.d)
                            /*if (StringUtil.isNotEmpty(data._response.result.d)){
                                var uploads = $(param.selecter).parent().parent().parent().find('.uploaded-imgs')
                                if (uploads.length > 0){
                                    var imgs = uploads.find('input[name="imgs"]')
                                    var imgsVal = uploads.find('input[name="imgs"]').val()
                                    var imgsArray = []
                                    if (StringUtil.isNotEmpty(imgsVal)) {
                                        imgsArray = imgsVal.split(',')
                                    }
                                    imgsArray.push(data._response.result.d)
                                    imgs.val(imgsArray.join(','))
                                    uploads.append('<div class="uploaded-img"><img class="item-img float-left"><span class="am-icon-close"></span></div>')
                                    uploads.data('imgsNum', uploads.data('imgsNum')+1)
                                }
                            }*/

                        }
                    }
                    /*$.each(data.result.files, function (index, file) {
                        $('<p/>').text(file.name).appendTo('#files');
                    });*/
                },
                progress: function (e, data) {
                    var progress = parseInt(data.loaded / data.total * 100, 10);
                    log(progress)
                    /* $('#progress .progress-bar').css(
                         'width',
                         progress + '%'
                     );*/
                },
                fail: function (e, data) {
                    log(data)
                    closeMask()
                }
            })
        }
    },
    appendFiles:function (selector, files) {

        if (typeof selector === 'string') {
            selector = $(selector)
        }

        if (ObjectCommonUtil.isNotUndefined(files) && files.length > 0){
            var uploads = selector.parent().parent().parent().find('.uploaded-imgs')
            if (uploads.length > 0){
                var imgs = uploads.find('input[name="imgs"]')
                var imgsVal = uploads.find('input[name="imgs"]').val()
                var imgsArray = []
                var exists = false
                if (StringUtil.isNotEmpty(imgsVal)) {
                    imgsArray = imgsVal.split(',')
                    for (var i=0; i<files.length; i++) {
                        imgsArray.push(files[i].name)
                    }
                    var newImg = $('<div class="uploaded-img"><span class="float-left">files[i].name</span></a><span class="am-icon-close"></span></div>');
                    uploads.append(newImg);
                }
                imgs.val(imgsArray.join(','))
                uploads.data('imgsNum', uploads.data('imgsNum')+files.length)
            }
        }
    },
    appendImgs:function (selector, imgMd5) {

        if (typeof selector === 'string') {
            selector = $(selector)
        }

        if (StringUtil.isNotEmpty(imgMd5)){
            var uploads = selector.parent().parent().parent().find('.uploaded-imgs')
            if (uploads.length > 0){
                var imgs = uploads.find('input[name="imgs"]')
                var imgsVal = uploads.find('input[name="imgs"]').val()
                var imgsArray = []
                var exists = false
                if (StringUtil.isNotEmpty(imgsVal)) {
                    imgsArray = imgsVal.split(',')
                    for (var i=0; i<imgsArray.length; i++) {
                        if (imgsArray[i] == imgMd5){
                            exists = true
                            break
                        }
                    }
                }
                if (exists) {
                    //castSuccessMsg("已在本页上传相同图片")
                    return false;
                }
                imgsArray.push(imgMd5);
                var imgLowUrl = ImgUtil.getImgUrl(imgMd5, 2)
                var imgUrl = ImgUtil.getImgUrl(imgMd5)
                imgs.val(imgsArray.join(','))
                var newImg = $('<div class="uploaded-img"><a href="'+ imgUrl +'" target="_blank"><img src="' + imgLowUrl + '" class="item-img float-left"/></a><span class="am-icon-close"></span></div>');
                newImg.data("md5", imgMd5)
                uploads.append(newImg);
                //uploads.append('<div class="uploaded-img"><img src="' + imgUrl + '" class="item-img float-left"><span class="am-icon-close"></span></div>')
                uploads.data('imgsNum', uploads.data('imgsNum')+1)
            }
        }
    }
}

var ImgUtil = {
    imgParamType : [
        {
            name: "item_list_img",
            code: 1,
            param: "q=1"
        },
        {
            name: "upload_img",
            code: 2,
            param: "q=10"
        },
        {
            name: "order_list_item_img",
            code: 3,
            param: "q=1"
        },
        {
            name: "quality_10_img",
            code: 4,
            param: "q=10"
        }
    ],
    //imgUrlPre : 'http://182.92.234.77:4869/',
    imgUrlPre : '/img/',
    getImgUrl : function (imgMd5, paramTypeCode) {
        if (paramTypeCode != undefined) {
            var param
            for (var index = 0; index < ImgUtil.imgParamType.length; index++) {
                var type = ImgUtil.imgParamType[index]
                if (paramTypeCode == type.code) {
                    param = type.param
                    break
                }
            }
            if (param != undefined) {
                imgMd5 = imgMd5 + "?" + param;
            }
        }
        return ImgUtil.imgUrlPre + imgMd5;
    },
    getImgLowUrl : function (imgMd5) {
        return ImgUtil.imgUrlPre + imgMd5 + "?w=100&h=100&q=5";
    }
}

var FormUtil = {
    tokenMap: {},
    genToken: function (pageId) {
        AjaxUtil.doAjax({
            url: '/p/wm/ft/g',
            config: {
                async: false
            },
            doneCallback: function (d) {
                if (d.s) {
                    FormUtil.tokenMap[pageId] = d.d
                } else {
                    castErrorMsg(d.m)
                }
            }
        })
    },
    getToken: function (pageId) {
        var token = FormUtil.tokenMap.pageId
        if (ObjectCommonUtil.isUndefined(token)) {
            return msgAlert("似乎没有表单token，尝试重新打开tab或刷新。")
        }
        return token
    },
    /*
    formSelector + name 定位元素，并根据元素类型进行赋值
    * */
    fillVal : function (formSelector, eleName, val) {

        var eles
        if (typeof formSelector === 'string') {
            eles = $(formSelector)
        }else {
            eles = formSelector
        }

        if (eleName == 'imgs'){
            if (!ObjectCommonUtil.isEmpty(val) ){
                var imgs = val.split(',')
                for (var index in imgs){
                    fileUploadUtils.appendImgs(formSelector.find('#fileupload'), imgs[index])
                }
            }
        }else if (eleName == 'img'){
            if (!ObjectCommonUtil.isEmpty(val) ){
                fileUploadUtils.appendImgs(formSelector.find('#fileupload'), val)
            }
        }


        eles = eles.find('.form-control[name="'+ eleName +'"]:first')
        if (eles.length > 0){
            if (eles[0].nodeName == 'INPUT'){
                if(eles[0].type == 'radio' || eles[0].type == 'checkbox'){
                    eles.each(function () {
                        if ($(this).val() == val) {
                            $(this).attr('checked', true)
                        }
                    })
                }else {
                    eles.val(val)
                }
            }else if (eles[0].nodeName == 'TEXTAREA'){
                eles.val(val)
            }else if (eles[0].nodeName == 'SELECT') {
                log("SELECT")
                if (eles.hasClass('data-am-selected')) {
                    CateUtils.selected(formSelector, val)
                    /*使用amazeui select*/
                    /*eles.next('.am-selected').find('ul li').each(function () {
                        if ($(this).data('value') == val){
                            $(this).click()
                        }
                    })*/
                }else {
                    eles.find('option').each(function () {
                        if ($(this).val() == val){
                            $(this).attr("selected", true)
                        }
                    })
                }
            }
        }
    }
}

var goTop = function () {
    $('.foods a').click()
}

var notLoad = function (ele) {
    return undefined == ele.data('hasLoad') || false == ele.data('hasLoad')
}

var canMovedItemGroup = {
    initMovedItemGroup2 : function (pageId) {

        var getItemIdsOfActivity4PageDataJson = function (start, pageSize, pageId) {
            log("getItemIds " + pageId + " " + start + " " + pageSize)
            var currentIds = getCurrentItemsArrayOfActivity(pageId)
            var ids = currentIds.slice(start, start + pageSize)
            log(ids)
            return {
                ids: ids.join(","),
                total: currentIds.length
            }
        };

        var getCurrentItemsValOfActivity = function (pageId) {
            var dom = $("#" + pageId + " textarea[name='itemIds']")
            var currentVal = dom.val()
            return currentVal
        }
        var getMaxLimitOfActivity = function (pageId) {
            var max = $("#" + pageId + " textarea[name='itemIds']").data('max')
            if (undefined == max) {
                max = 1000
            }
            return max
        }

        var getCurrentItemsArrayOfActivity = function (pageId) {
            var currentVal = getCurrentItemsValOfActivity(pageId)
            var currentItemsOfActivity = ObjectCommonUtil.isUndefined(currentVal) || !StringUtil.isNotEmpty(currentVal) ? [] : currentVal.split(",").filter(function (val) {
                return val.trim() != ""
            })
            return currentItemsOfActivity
        }

        var setValForItemsOfActivity = function (ids, pageId) {
            var dom = $("#" + pageId + " textarea[name='itemIds']")
            dom.val(ids)
        }

        var addToItemsOfActivity = function (ids, pageId) {
            if (!StringUtil.isNotEmpty(ids)) {
                return castErrorMsg("请选择商品ID")
            }

            // 排重
            var currentVal = getCurrentItemsValOfActivity(pageId);
            ids = ids.split(",").filter(function (item) {
                return -1 == currentVal.indexOf(item)
            })
            if (ids.length < 1) {
                return castErrorMsg("商品ID已在集合中")
            }
            // append
            var currentItemsOfActivity = getCurrentItemsArrayOfActivity(pageId);

            var max = getMaxLimitOfActivity(pageId)
            if (currentItemsOfActivity.length >= max) {
                return castErrorMsg("集合中商品数量已达上限" + max)
            }

            currentItemsOfActivity = currentItemsOfActivity.concat(ids).sort();
            if (currentItemsOfActivity.length > max) {
                return castErrorMsg("选的商品太多了,集合元素数量上限为" + max)
            }

            setValForItemsOfActivity(currentItemsOfActivity.join(","), pageId)
            $("#" + pageId + " #" + pageId + "Form .panel-itemIds textarea[name='itemIds']").change()
        };

        var removeFromItemsOfActivity = function (ids, pageId) {
            if (!StringUtil.isNotEmpty(ids)) {
                return castErrorMsg("请选择商品ID")
            }

            var currentItemsOfActivity = getCurrentItemsArrayOfActivity(pageId);
            //ids = ids.join(",")
            currentItemsOfActivity = currentItemsOfActivity.filter(function (value) {
                return -1 == ids.indexOf(value)
            })

            setValForItemsOfActivity(currentItemsOfActivity.join(","), pageId)
            $("#" + pageId + " #"+pageId+"Form .panel-itemIds textarea[name='itemIds']").change()

        };

        $("#"+pageId+" #queryAlternative").on('click', function () {
            var alternativeItemTable = $('#'+pageId+' .all-items table:first')
            if (notLoad(alternativeItemTable)) {
                alternativeItemTable.dataTable( {
                    language: {
                        info: "第 _PAGE_ 页，共 _PAGES_ 页，共 _TOTAL_ 条",
                        /*翻页*/
                        paginate: {
                            first: 'first',
                            previous: '<',
                            next:     '>',
                            last: 'last'
                        },
                        /*条数*/
                        // lengthMenu: '<div style="float: right;" class="col-lg-6 col-md-6 col-sm-6 input-group input-group-sm"><span class="input-group-addon">每页条数</span><select class="form-control" name="payType"><option value="10">10</option><option value="30">30</option><option value="50">50</option></select></div>',
                        lengthMenu: '',
                        /*无数据时显示内容*/
                        zeroRecords: "无数据",
                        infoFiltered: ''
                    },
                    select:{
                        style:     'none',
                        className: 'none'
                    },
                    //info:false,
                    // 删除斑马线效果
                    /*stripeClasses: [ ],*/
                    // 禁用search
                    searching: false,
                    // 禁用列排序
                    ordering: false,
                    // 从server 接口获取数据
                    processing: true,
                    serverSide: true,
                    ajax: {
                        url: '/p/wm/item/list',
                        data: function (d) {
                            var data = getFormDataJson(pageId, 'alternativeItemListForm')
                            data.pageIndex = d.start/d.length
                            log(data.pageIndex)
                            data.dataStart = d.start
                            log(data.dataStart)
                            data.pageSize = d.length
                            data.draw = d.draw
                            return JsonUtil.simpleFix(data)
                        }
                    },
                    columns:[
                        {data:'id',render:function (data, type, row, meta) {
                                var ele =   '<div style="margin-left: 40%;">' +
                                    '<input value="'+row.id+'" type="checkbox" class="tableCheckEle float-left"/>' +
                                    '</div>'
                                return ele
                            }}
                        ,{data:'id',render:function (data, type, row, meta) {
                                var ele =   '<div>' +
                                    '<img class="float-left item-img" name="itemImg" src="'+ImgUtil.getImgUrl(row.img)+'"/>' +
                                    '<div style="margin-left:5px" class="float-left">' +
                                    '<div name="itemTitle" data-toggle="tooltip" data-placement="top" title="'+row.title+'">'+StringUtil.abbreviatory(row.title,20)+'<b style="color:red;margin-left: 3rem;">('+ row.statusDesc +')</b></div>' +
                                    '<div name="itemId">'+row.id+'</div>'
                                '</div>' +
                                '</div>'
                                return ele
                            }}],
                    // 使用测试数据
                    //data: [/*{itemId:'1223433342',price:'3.45',count:23,total:'78.56'}*/],
                    createdRow: function(row, data, dataIndex, cells){
                    },
                    drawCallback:function (){
                        // draw完成后回调
                        resizeContentHeight()
                    }
                } )
                alternativeItemTable.data('hasLoad', true)
            }else {
                log("reload")
                alternativeItemTable.DataTable().clear().ajax.reload()
            }
            return false
        })
        $("#"+pageId+" #"+pageId+"Form .panel-itemIds textarea[name='itemIds']").on('change', function () {
            var activityItemTable = $('#'+pageId+' .items-of-activity table:first')
            if (notLoad(activityItemTable)) {
                activityItemTable.dataTable( {
                    language: {
                        info: "第 _PAGE_ 页，共 _PAGES_ 页，共 _TOTAL_ 条",
                        /*翻页*/
                        paginate: {
                            first: 'first',
                            previous: '<',
                            next:     '>',
                            last: 'last'
                        },
                        /*条数*/
                        //lengthMenu: '<div style="float: right;" class="col-lg-2 col-md-2 col-sm-2 input-group input-group-sm"><span class="input-group-addon">每页条数</span><select class="form-control" name="payType"><option value="10">10</option><option value="30">30</option><option value="50">50</option></select></div>',
                        lengthMenu: '',
                        /*无数据时显示内容*/
                        zeroRecords: "无数据",
                        infoFiltered: ''
                    },
                    select:{
                        style:     'none',
                        className: 'none'
                    },
                    //info:false,
                    // 删除斑马线效果
                    /*stripeClasses: [ ],*/
                    // 禁用search
                    searching: false,
                    // 禁用列排序
                    ordering: false,
                    // 从server 接口获取数据
                    processing: true,
                    serverSide: true,
                    ajax: {
                        url: '/p/wm/item/list',
                        data: function (d) {
                            var data = getItemIdsOfActivity4PageDataJson(d.start, d.length, pageId)
                            if (ObjectCommonUtil.isEmpty(data.ids)) {
                                data.pageIndex = 0
                                data.dataStart = 0
                                data.pageSize = 0
                                data.draw = d.draw
                            }else {
                                data.pageIndex = d.start/d.length;
                                log(data.pageIndex)
                                data.dataStart = d.start
                                log(data.dataStart)
                                data.pageSize = d.length
                                data.draw = d.draw
                            }
                            return JsonUtil.simpleFix(data)
                        }
                    },
                    columns:[
                        {data:'id',render:function (data, type, row, meta) {
                                var ele =   '<div style="margin-left: 40%;">' +
                                    '<input value="'+row.id+'" type="checkbox" class="tableCheckEle float-left"/>' +
                                    '</div>'
                                return ele
                            }}
                        ,{data:'id',render:function (data, type, row, meta) {
                                var ele =   '<div>' +
                                    '<img class="float-left item-img" name="itemImg" src="'+ImgUtil.getImgUrl(row.img)+'"/>' +
                                    '<div style="margin-left:5px" class="float-left">' +
                                    '<div name="itemTitle" data-toggle="tooltip" data-placement="top" title="'+row.title+'">'+StringUtil.abbreviatory(row.title,20)+'<b style="color:red;margin-left: 3rem;">('+ row.statusDesc +')</b></div>' +
                                    '<div name="itemId">'+row.id+'</div>'
                                '</div>' +
                                '</div>'
                                return ele
                            }}],
                    // 使用测试数据
                    //data: [/*{itemId:'1223433342',price:'3.45',count:23,total:'78.56'}*/],
                    createdRow: function(row, data, dataIndex, cells){
                    },
                    drawCallback:function (){
                        // draw完成后回调
                        resizeContentHeight()
                    }
                } )
                activityItemTable.data('hasLoad', true)
            }else {
                log("reload")
                activityItemTable.DataTable().clear().ajax.reload()
            }
            return false
        })

        $("#"+pageId+" #addToItemsOfActivity").on('click', function () {
            var ids = getCheckedValsFromTable($('#'+pageId+' .all-items table:first'))
            cleanCheckedFromTable($('#'+pageId+' .all-items table:first'))
            addToItemsOfActivity(ids, pageId)
        })
        $("#"+pageId+" #removeFromItemsOfActivity").on('click', function () {
            var ids = getCheckedValsFromTable($('#'+pageId+' .items-of-activity table:first'))
            cleanCheckedFromTable($('#'+pageId+' .items-of-activity table:first'))
            removeFromItemsOfActivity(ids, pageId)
        })
    },
    showAlternativeItems : function (pageId) {
        $("#"+pageId+" #queryAlternative").click()
    },
    showItems4Detail : function (pageId) {
        var getItemIdsOfActivity4PageDataJson = function (start, pageSize) {
            var currentIds = getCurrentItemsArrayOfActivity(pageId)
            var ids = currentIds.slice(start, start + pageSize)
            return {
                ids: ids.join(","),
                total: currentIds.length
            }
        };
        var getCurrentItemsValOfActivity = function (pageId) {
            var dom = $("#"+pageId+" textarea[name='itemIds']")
            var currentVal = dom.val()
            return currentVal
        }

        var getCurrentItemsArrayOfActivity = function (pageId) {
            var currentVal = getCurrentItemsValOfActivity(pageId)
            var currentItemsOfActivity = ObjectCommonUtil.isUndefined(currentVal) || !StringUtil.isNotEmpty(currentVal) ? [] : currentVal.split(",").filter(function (val) {
                return val.trim() != ""
            })
            return currentItemsOfActivity
        }

        var showItems = function () {
            var activityItemTable = $('#'+pageId+' .items-of-activity table:first')
            if (notLoad(activityItemTable)) {
                activityItemTable.dataTable( {
                    language: {
                        info: "第 _PAGE_ 页，共 _PAGES_ 页，共 _TOTAL_ 条",
                        /*翻页*/
                        paginate: {
                            first: 'first',
                            previous: '<',
                            next:     '>',
                            last: 'last'
                        },
                        /*条数*/
                        //lengthMenu: '<div style="float: right;" class="col-lg-2 col-md-2 col-sm-2 input-group input-group-sm"><span class="input-group-addon">每页条数</span><select class="form-control" name="payType"><option value="10">10</option><option value="30">30</option><option value="50">50</option></select></div>',
                        lengthMenu: '',
                        /*无数据时显示内容*/
                        zeroRecords: "无数据",
                        infoFiltered: ''
                    },
                    select:{
                        style:     'none',
                        className: 'none'
                    },
                    //info:false,
                    // 删除斑马线效果
                    /*stripeClasses: [ ],*/
                    // 禁用search
                    searching: false,
                    // 禁用列排序
                    ordering: false,
                    // 从server 接口获取数据
                    processing: true,
                    serverSide: true,
                    ajax: {
                        url: '/p/wm/item/list',
                        data: function (d) {
                            var data = getItemIdsOfActivity4PageDataJson(d.start, d.length, pageId)
                            data.pageIndex = d.start/d.length
                            log(data.pageIndex)
                            data.dataStart = d.start
                            log(data.dataStart)
                            data.pageSize = d.length
                            data.draw = d.draw
                            return JsonUtil.simpleFix(data)
                        }
                    },
                    columns:[
                        {data:'id',render:function (data, type, row, meta) {
                                var ele =   '<div>' +
                                    '<img class="float-left item-img" name="itemImg" src="'+ImgUtil.getImgUrl(row.img)+'"/>' +
                                    '<div style="margin-left:5px" class="float-left">' +
                                    '<div name="itemTitle" data-toggle="tooltip" data-placement="top" title="'+row.title+'">'+StringUtil.abbreviatory(row.title,20)+'<b style="color:red;margin-left: 3rem;">('+ row.statusDesc +')</b></div>' +
                                    '<div name="itemId">'+row.id+'</div>'
                                '</div>' +
                                '</div>'
                                return ele
                            }}],
                    // 使用测试数据
                    //data: [/*{itemId:'1223433342',price:'3.45',count:23,total:'78.56'}*/],
                    createdRow: function(row, data, dataIndex, cells){
                    },
                    drawCallback:function (){
                        // draw完成后回调
                        resizeContentHeight()
                    }
                } )
                activityItemTable.data('hasLoad', true)
            }else {
                log("reload")
                activityItemTable.DataTable().clear().ajax.reload()
            }
            return false
        };

        showItems()
    }
}

var saleStrategyUtil = {
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
    getSaleStrategyShowHtml : function (itemJson) {
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



