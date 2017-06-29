function onWebBridgeInit(callback) {
    if (window.WebViewJavascriptBridge) {
        callback(window.WebViewJavascriptBridge)
    } else {
        document.addEventListener('WebViewJavascriptBridgeReady', function () {
            callback(window.WebViewJavascriptBridge)
        }, false);
    }
}

var JSBridgeInit_Callback = [];

onWebBridgeInit(function (bridge) {
    bridge.init(function (message, responseCallback) {
        var data = {
            'Javascript Responds': '测试中文!'
        };
        responseCallback(data);
    });

    if (JSBridgeInit_Callback.length > 0) {
        __invokeSdkGetInitData(function (initData) {
            for (var i = 0; i < JSBridgeInit_Callback.length; i++) {
                var callback = JSBridgeInit_Callback[i];
                callback(initData);
            }
            JSBridgeInit_Callback = []; //清空
        });
    }
});


/**
 * 设备初始化函数
 * 返回初始化数据 { appVersion: 'v1.0', imei: '', registrationID: '' }
 */
function onJSBridgeInit(callback) {
    if (window.WebViewJavascriptBridge) {
        __invokeSdkGetInitData(function (initData) {
            callback(initData);
        });
    } else {
        JSBridgeInit_Callback.push(callback);
    }
}

/**
 * 注册事件监听
 */
function invokeSdkRegisterHandler(event, callback) {
    window.WebViewJavascriptBridge.registerHandler(event, function (result) {
        callback && callback(result);
    });
}

/**
 * toast
 */
function invokeSdkToast(msg) {
    window.WebViewJavascriptBridge.callHandler('invokeSdkToast', msg, null);
}

/**
 * 获取app的版本号
 */
function invokeSdkGetAppVersion(callback) {
    window.WebViewJavascriptBridge.callHandler('invokeSdkGetAppVersion', null, function (data) {
        callback(data);
    });
}

/**
 * 设置网络
 */
function invokeSdkSetupNetwork() {
    window.WebViewJavascriptBridge.callHandler('invokeSdkSetupNetwork', null, null);
}

/**
 * 设置imei
 */
function invokeSdkSetImei(imei, callback) {
    window.WebViewJavascriptBridge.callHandler('invokeSdkSetImei', imei, function (data) {
        callback(data);
    });
}

/**
 * 私有方法 获取初始化数据
 */
function __invokeSdkGetInitData(callback) {
    window.WebViewJavascriptBridge.callHandler('invokeSdkGetInitData', null, function (data) {
        callback(data);
    });
}

/**
 * 调用扫码枪
 * @result 扫码枪扫到的二维码
 * { status: 0, desc: '扫码成功', qrcode: ''}
 * { status: 1, desc: '扫码超时'}
 * { status: 2, desc: '扫码失败'}
 */
function invokeSdkOpenQrcodeDevice(callback) {
    window.WebViewJavascriptBridge.callHandler('invokeSdkOpenQrcodeDevice', null, function (data) {
        callback(data);
    });
}

/**
 * 发货
 * { status: 0, desc: '发货成功', progress: ''}
 * { status: 1, desc: '发货超时'}
 * { status: 2, desc: '发货失败'}
 * progress in [Delivery_Unknown((byte)0xFF),Delivery_Failure((byte)0x00),Delivery_Ing((byte)0x01),Delivery_Complete((byte)0x02)]
 */
function invokeSdkTakeGoods(orderNum, callback) {
    window.WebViewJavascriptBridge.callHandler('invokeSdkTakeGoods', {
        orderNum: orderNum
    }, function (data) {
        callback && callback(data);
    });
}

/**
 * 注册订单状态通知handler
 * @param callback(orderNum)
 */
function invokeSdkRegisterOrderStatusEvent(callback) {
    invokeSdkRegisterHandler("invokeSdkRegisterOrderStatusEvent", callback);
}

/**
 * 检查米是否充足
 * { status: 0, desc: '检查设备成功', stock: ''}
 * { status: 1, desc: '检查设备超时'}
 * { status: 2, desc: '检查设备失败'}
 * stock in [UNKNOWN("FF", "未知"),SUFFICIENT("00","充足"),INSUFFICIENT("01","不足"),PROMPT_REPLENISHMENT("02","提示补货"),FULL("03","满仓");]
 */
function invokeSdkCheckGoodsStatus(callback) {
    window.WebViewJavascriptBridge.callHandler('invokeSdkCheckGoodsStatus', null, function (result) {
        console.info(['调用了(invokeSdkCheckGoodsStatus)', "结果为: ", result]);
        callback && callback(angular.fromJson(result));
    });
}

/**
 * 打开底部菜单栏
 */
function invokeSdkShowNavigation(callback) {
    window.WebViewJavascriptBridge.callHandler('invokeSdkShowNavigation', null, callback);
}

/**
 * 关闭底部菜单
 */
function invokeSdkHideNavigation(callback) {
    window.WebViewJavascriptBridge.callHandler('invokeSdkHideNavigation', null, callback);
}

/**
 * 检测网络状态
 */
function invokeSdkGetNetState(callback) {
    window.WebViewJavascriptBridge.callHandler('invokeSdkGetNetState', null, function (result) {
        callback(angular.fromJson(result));
    });
}


function invokeSdkGetRegistrationID(callback) {
    window.WebViewJavascriptBridge.callHandler('invokeSdkGetRegistrationID', null, callback);
}

var JSBridge = {
    onJSBridgeInit: onJSBridgeInit,
    invokeSdkToast: invokeSdkToast,
    invokeSdkGetAppVersion: invokeSdkGetAppVersion,
    invokeSdkSetImei: invokeSdkSetImei,
    invokeSdkRegisterHandler: invokeSdkRegisterHandler,
    invokeSdkSetupNetwork: invokeSdkSetupNetwork,
    invokeSdkOpenQrcodeDevice: invokeSdkOpenQrcodeDevice,
    invokeSdkTakeGoods: invokeSdkTakeGoods,
    invokeSdkRegisterOrderStatusEvent: invokeSdkRegisterOrderStatusEvent,
    invokeSdkCheckGoodsStatus: invokeSdkCheckGoodsStatus,
    invokeSdkShowNavigation: invokeSdkShowNavigation,
    invokeSdkHideNavigation: invokeSdkHideNavigation,
    invokeSdkGetNetState: invokeSdkGetNetState,
    invokeSdkGetRegistrationID: invokeSdkGetRegistrationID
};

window.JSBridge = JSBridge;