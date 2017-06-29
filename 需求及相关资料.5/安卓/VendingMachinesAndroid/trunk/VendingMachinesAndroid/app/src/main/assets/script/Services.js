var isLocalTest = 0;

angular.module('webapp', ['ngRoute', 'ngTouch', 'util']);

angular.module('webapp')

    .config(['$httpProvider', function ($httpProvider) {
        $httpProvider.interceptors.push('ajaxLoadingInterceptor');
        $httpProvider.interceptors.push('httpErrorInterceptor');
        //设置全局超时
        $httpProvider.defaults.timeout = 3000;
    }])

    .service('AppService', ['$http', '$timeout', '$q', 'Tool', function ($http, $timeout, $q, Tool) {
        var config = {
            mask: true, //是否添加遮罩层
            cache: false, //是否启用 angular http 缓存
            store: false, //是否使用 自定义存储 存储http响应结果 store存储在localstorage中
            handlerErrorCode: true //是否处理统一的错误吗
        };

        var __imei = null; //设备imei
        var __appVersion = null; //包版本
        var __registrationID = null; //jpush的id
        var init = false;

        var baseUrl = 'http://test2.haokaishi365.com/';
        if (isLocalTest==1){
            baseUrl ='http://192.168.1.15:8080/';
        }

        var pictureUrl = baseUrl + 'api/ad/list';
        var goodsListUrl = baseUrl + 'goods/list';
        var placeOrderUrl = baseUrl + 'api/order/place';
        var orderStatusUrl = baseUrl + 'api/order/orderStatus';
        var takeGoodsUrl = baseUrl + 'api/order/takeGoods';
        var adminLoginUrl = baseUrl + 'api/admin/login';
        var registerDeviceUrl = baseUrl + 'api/device/register';
        var qrCodeTakeGodosUrl = baseUrl + 'api/order/takeGoods/qrCode';
        var vipcardTakeGodosUrl = baseUrl + 'api/order/pay/vipcard/qrCode';
        var replenishUrl = baseUrl + 'api/admin/replenish/confirm'; //管理员确认补货


        var __adminData = {}; // {"admin_token": "3239813f38434523baac1d231371941b"}

        function initImei() {
            var deferred = $q.defer();
            if (init) {
                $timeout(function () {
                    deferred.resolve();
                });
            } else {
                console.info("调用设备获取imei");
                JSBridge.onJSBridgeInit(function (__initData) {
                    var initData = JSON.parse(__initData);
                    __imei = initData.imei;
                    __appVersion = initData.appVersion;
                    init = true;
                    deferred.resolve();

                });

//                __imei = 0;
//                _appVersion = '0.01';
//                   init = true;
//                    deferred.resolve();
            }
            return deferred.promise;
        }

        /**
         * 封装http get请求
         */
        function $get(url, data, conf) {
            data = $.extend({}, data);
            conf = $.extend({}, config, conf);
            if (!conf.cache && !conf.store) {
                data.t = new Date().getTime()
            }
            data = $.param(data);
            var deferred = $q.defer();
            var resultUrl = url + '?' + data;
            console.info(resultUrl);
            $http.get(resultUrl, conf).success(function (result) {
                console.info('success: ' + JSON.stringify(result));
                //判断是否要处理 store
                if (conf.store) {
                    try {
                        Tool.storeSet(resultUrl, Tool.json2Str(result));
                    } catch (err) {
                        console.info(err);
                    }
                }

                //判断是否要处理统一错误码
                if (conf.handlerErrorCode) {
                    if (result.code != '0') { //错误码处理
                        result.msg = result.msg || '网络异常,请稍后再试';
                        deferred.reject(result);
                    } else {
                        deferred.resolve(result);
                    }
                } else {
                    deferred.resolve(result);
                }
            }).error(function (result) {
                //判断是否需要从缓存中恢复
                if (conf.store) {
                    var storeResult = null;
                    try {
                        storeResult = Tool.str2Json(Tool.storeGet(resultUrl));
                    } catch (err) {
                        console.info(err);
                    }
                    if (storeResult) {
                        //判断是否要处理统一错误码
                        if (conf.handlerErrorCode) {
                            if (storeResult.code != '0') { //错误码处理
                                storeResult.msg = storeResult.msg || '网络异常,请稍后再试';
                                deferred.reject(storeResult);
                            } else {
                                deferred.resolve(storeResult);
                            }
                        } else {
                            deferred.resolve(storeResult);
                        }
                    } else {
                        deferred.reject(result);
                    }
                } else {
                    deferred.reject(result);
                }
            });
            return deferred.promise;
        }

        /**
         * 封装http post请求
         */
        function $post(url, data, conf) {
            data = $.extend(data, {});
            conf = $.extend({}, config, conf);
            var deferred = $q.defer();
            $http.post(url, data, conf).success(function (result) {
                if (result.code != '0') { //错误码处理
                    result.msg = result.msg || '网络异常,请稍后再试';
                    deferred.reject(result);
                } else {
                    deferred.resolve(result);
                }
            }).error(function (result) {
                deferred.reject(result);
            });
            return deferred.promise;
        }

        function getPictureList() {
            return $get(pictureUrl, {imei: getImei()}, {cache: false, store: false});
        }

        function getGoodsList() {
            return $get(goodsListUrl, {imei: getImei()}, {cache: false, store: false});
        }

        function placeOrder(goodsNum, goodsId) {
            return $get(placeOrderUrl, {
                goodsNum: goodsNum,
                goodsId: goodsId,
                imei: getImei()
            });
        }

        function checkOrderStatus(orderNum) {
            return $get(orderStatusUrl, {
                orderNum: orderNum,
                imei: getImei()
            });
        }

        function takeGoods(orderNum, takeGoodsCode) {
            return $get(takeGoodsUrl, {
                imei: getImei(),
                orderNum: orderNum,
                takeGoodsCode: takeGoodsCode
            })
        }

        function adminLogin(username, password) {
            return $get(adminLoginUrl, {
                username: username,
                password: password,
                imei: getImei()
            });
        }

        function setAdminData(adminData) {
            __adminData = adminData;
        }

        function getAdminData() {
            return __adminData;
        }

        function getImei() {
            return __imei;
        }

        function getAppVersion() {
            return __appVersion;
        }

        function registerDevice(newImei) {
            var deferred = $q.defer();
            JSBridge.invokeSdkGetRegistrationID(function (registrationID) {
                $get(registerDeviceUrl, {
                    appVersion: getAppVersion(),
                    imei: newImei,
                    registrationID: registrationID
                }).then(function (result) {
                    JSBridge.invokeSdkSetImei(newImei, function () {
                        __imei = newImei;
                        deferred.resolve(result);
                    });
                }, function (result) {
                    deferred.reject(result);
                });
            });
            return deferred.promise;
        }

        /**
         * 二维码取货
         */
        function takeGoodsByQrcode(qrcode) {
            return $get(qrCodeTakeGodosUrl, {
                data: qrcode,
                imei: getImei()
            });
        }

        /**
         * 会员卡取货
         */
        function takeGoodsByVipcard(qrcode, orderNum) {
            return $get(vipcardTakeGodosUrl, {
                data: qrcode,
                orderNum: orderNum,
                imei: getImei()
            });
        }

        /**
         * 管理员确认补货
         */
        function replenish(stockStatus,num) {
            return $get(replenishUrl, {
                stockStatus: stockStatus,
                num: num,
                admin_token: __adminData.admin_token,
                imei: getImei()
            }, {}, {cache: false});
        }

        return {
            initImei: initImei,
            getImei: getImei,

            //保存admin登录后的信息
            setAdminData: setAdminData,
            getAdminData: getAdminData,

            getPictureList: getPictureList,
            getGoodsList: getGoodsList,
            placeOrder: placeOrder,
            checkOrderStatus: checkOrderStatus,
            takeGoods: takeGoods,
            adminLogin: adminLogin,
            registerDevice: registerDevice,
            getAppVersion: getAppVersion,
            takeGoodsByQrcode: takeGoodsByQrcode,
            takeGoodsByVipcard: takeGoodsByVipcard,
            replenish: replenish

        }
    }]);
