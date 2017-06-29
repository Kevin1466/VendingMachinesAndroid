angular.module('webapp')
    .config(['$routeProvider', '$httpProvider', function ($routeProvider, $httpProvider) {
        $httpProvider.interceptors.push('timeoutInterceptor');

        $routeProvider

            .when('/index', {
                templateUrl: 'views/index.html',
                controllerUrl: 'script/AllControllers',
                resolve: {
                    initImei: function (AppService) {
                        return AppService.initImei();
                    }
                }
            })
            .when('/order', {
                templateUrl: 'views/order.html',
                controllerUrl: 'script/AllControllers'
            })
            .when('/success', {
                templateUrl: 'views/success.html',
                controllerUrl: 'script/AllControllers'
            })
            .when('/error', {
                templateUrl: 'views/error.html',
                controllerUrl: 'script/AllControllers'
            })
            .when('/no_goods', {
                templateUrl: 'views/no_goods.html',
                controllerUrl: 'script/AllControllers'
            })
            .when('/outting_goods', {
                templateUrl: 'views/outting_goods.html',
                controllerUrl: 'script/AllControllers'
            })
            .when('/pay', {
                templateUrl: 'views/pay.html',
                controllerUrl: 'script/AllControllers'
            })
            .when('/take_goods', {
                templateUrl: 'views/take_goods.html',
                controllerUrl: 'script/AllControllers'
            })

            .when('/admin_login', {
                templateUrl: 'views/admin_login.html',
                controllerUrl: 'script/AllControllers'
            })
            .when('/admin_setting', {
                templateUrl: 'views/admin_setting.html',
                controllerUrl: 'script/AllControllers'
            })
            .when('/setting_imei', {
                templateUrl: 'views/setting_imei.html',
                controllerUrl: 'script/AllControllers'
            })
            .when('/goodsAdd', {
                templateUrl: 'views/goodsAdd.html',
                controllerUrl: 'script/AllControllers'
            })
            .when('/nowifi', {
                templateUrl: 'views/nowifi.html'
            })
            .otherwise({
                redirectTo: '/index'
            })

    }])

    .run(function (AppService, $rootScope, $timeout, toast) {
        $rootScope.$on('$locationChangeStart', function (event, url) {
            console.info(['$locationChangeStart', url]);
            toast.hide();
        });

        AppService.initImei();

        JSBridge.onJSBridgeInit(function () {
            //二维码扫码枪线程 状态变化
            JSBridge.invokeSdkRegisterHandler('onQrcodeDeviceStateChangeEvent', function (response) {
                console.info(response);
                $rootScope.$broadcast('onQrcodeDeviceStateChangeEvent', response);
            });

            //发货串口异步通知线程 异步通知发货是否成功
            JSBridge.invokeSdkRegisterHandler('onTakeGoodsStatusChangeEvent', function (response) {
                // $rootScope.$broadcast('onTakeGoodsStatusChangeEvent', angular.fromJson(response));
                console.info('onTakeGoodsStatusChangeEvent: ' + response);
                var result = angular.fromJson(response);
                if (result.status == 0) {
                    switch (result.progress) {
                        case 'Delivery_Ing':
                            break;
                        case 'Delivery_Complete':
                            window.location.hash = '#/success';
                            break;
                        case 'Delivery_Unknown':
                        case 'Delivery_Failure':
                            window.location.hash = '#/error';
                            break;
                    }
                } else {
                    window.location.hash = '#/error';
                }
            });

            //JPush推送当前订单已经完成
            JSBridge.invokeSdkRegisterHandler('invokeSdkRegisterOrderStatusEvent', function (orderNum) {
                console.info(orderNum);
                console.info('before orderNum type ' + (typeof  orderNum));
                $rootScope.$broadcast('invokeSdkRegisterOrderStatusEvent', orderNum);
            });
        });
    })
;

angular.bootstrap(document, ['webapp']);