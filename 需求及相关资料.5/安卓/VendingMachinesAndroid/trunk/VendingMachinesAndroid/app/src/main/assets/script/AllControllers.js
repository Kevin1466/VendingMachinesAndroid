angular.module('webapp')

    .controller('IndexController', ['AppService', function (AppService) {
        var vm = this;
        vm.images = [];

        AppService.getPictureList().then(function (result) {
            vm.images = result.data;
        }, function () {
            console.info("getPictureList 失败");
            window.location.hash = '#/nowifi';
        });
    }])

    .controller('OrderController', ['$scope', 'AppService', 'toast', '$rootScope', 'loading', 'Tool', function ($scope, AppService, toast, $rootScope, loading, Tool) {
        var vm = this;
        vm.index = 0;
        vm.goodsList = [];
        vm.currentPayCount = 0;

        vm.longTouchEvent = function () {
            window.location.hash = '#/admin_login';
        };

        vm.delay = function () {
            window.location.hash = '#/index';
        };

        /* 获取商品列表 */
        AppService.getGoodsList().then(function (result) {
            vm.goodsList = result.data;
        });

        $scope.$on('onCheckGoodsStatusChangeEvent', function (result) {
            loading.hide();
        });

        vm.selectGoods = function (index, goodsListIndex, goodsNumberOptionIndex) {
            vm.index = index;
            vm.goodsListIndex = goodsListIndex;
            vm.goodsNumberOptionIndex = goodsNumberOptionIndex;
            var currentGoods = vm.goodsList[goodsListIndex];
            vm.currentPayCount = currentGoods.price * currentGoods.goodsNumOptions[goodsNumberOptionIndex];
        };

        vm.buy = function () {
            var currentGoods = vm.goodsList[vm.goodsListIndex];
            var goodsId = currentGoods.goodsId;
            var goodsNum = currentGoods.goodsNumOptions[vm.goodsNumberOptionIndex];

            //检查是否有货
            JSBridge.invokeSdkCheckGoodsStatus(function (result) {
                switch (result.status) {
                    case 0:
                        break;
                    case 1:
                    case 2:
                        return toast('当前米量不足,请下次再来');
                }

                //下单
                AppService.placeOrder(goodsNum, goodsId).then(function (result) {
                    $rootScope.PayCtl_$$_Data = result.data;
                    window.location.hash = '#/pay';
                }, function (result) {
                    toast(result.msg);
                });
            });
        };
    }])

    .controller('PayController', ['$scope', '$rootScope', '$timeout', 'AppService', 'toast', 'Tool', function ($scope, $rootScope, $timeout, AppService, toast, Tool) {
        var vm = this;
        var orderInfo = $rootScope.PayCtl_$$_Data || {};
        vm.defaultPayType = 2;
        vm.orderNum = orderInfo.orderNum;
        vm.qrcodeProgress = false;
        vm.goodsId = orderInfo.goodsId;
        vm.goodsNum = orderInfo.goodsNum;
        vm.totalPrice = orderInfo.totalPrice;
        vm.zhifubaoQrcode = '';
        vm.weixinQrcode = '';

        var isPayQrcode = false;
        var timer = null;
        var timer2 = null;

        $timeout(function () {
            vm.selectPayType(2);
        });

        vm.delay = function () {
            window.location.hash = '#/index';
        };

        vm.closeQrcode = function () {
            vm.qrcodeProgress = false;
            isPayQrcode = false;
            Tool.cancelTimeout(timer2);
            toast.hide();
        };

        /**
         * 监听jpush回来的订单支付完成信息
         */
        $scope.$on('invokeSdkRegisterOrderStatusEvent', function (event, orderNum) {
            if (orderNum != vm.orderNum) {
                return;
            }

            toast('支付成功');
            JSBridge.invokeSdkTakeGoods(vm.orderNum);
            window.location.hash = '#/outting_goods';
        });


        /**
         * 扫描会员卡二维码取货
         */
        vm.qrcodeVipCard = function () {
            vm.qrcodeProgress = true;
            toast('请扫描您的会员卡');

            openQrcodeDevice();
        };

        function openQrcodeDevice() {
            console.info('打开扫描枪');
            isPayQrcode = true;
            JSBridge.invokeSdkOpenQrcodeDevice(function (response) {
                console.info("获取到的二维码为: " + response);
                console.info('二维码扫描器页面是否打开: ' + (isPayQrcode ? '已打开' : '未打开'));
                if (!isPayQrcode) {
                    return;
                }

                var result = Tool.str2Json(response);
                switch (result.status) {
                    case 0: //扫码成功
                        AppService.takeGoodsByVipcard(result.qrcode, vm.orderNum).then(function (result) {
                            if (result.data.orderStatus != 'PAY_SUCCESS') {
                                return isPayQrcode && toast('当前订单未支付或已取货');
                            }

                            //调用SDK发货
                            JSBridge.invokeSdkTakeGoods(vm.orderNum);
                            $rootScope.SuccessCtl_$$_RemainSum = result.data.remainingSum; //会员卡所剩余额
                            $rootScope.SuccessCtl_$$_CardScore = result.data.cardScore; //会员卡积分
                            window.location.hash = '#/outting_goods';
                        }, function (result) {
                            isPayQrcode && toast(result.msg);
                        });
                        break;
                    case 1: //扫码超时
                    case 2: //扫码失败
                        console.log(vm.qrcodeProgress);
                        if (vm.qrcodeProgress) {
                            isPayQrcode && toast(result.desc);
                        }
                        vm.qrcodeProgress = false;
                        isPayQrcode = false;
                        break;
                }

                timer2 = $timeout(function () {
                    openQrcodeDevice();
                }, 1000)
            });
        }


        vm.selectPayType = function (type) {
            isPayQrcode = false;
            vm.defaultPayType = type;
            vm.getQrcord(type);
        };

        vm.getQrcord = function (type) {
            AppService.checkOrderStatus(vm.orderNum).then(function (result) {
                if (result.data != 'NEW') {
                    toast('订单已支付或订单已过期,请重新下单');
                    timer = $timeout(function () {
                        window.history.back();
                    }, 3000);
                } else {
                    switch (type) {
                        case 1: //支付宝
                            vm.zhifubaoQrcode = ['http://test2.haokaishi365.com/api/qrCode?code=', encodeURIComponent(orderInfo.alipayQrcode), '&t=', new Date().getTime()].join('');
                            console.info('订单号为:' + vm.orderNum + vm.zhifubaoQrcode);
                            break;
                        case 2: //微信支付
                            vm.weixinQrcode = ['http://test2.haokaishi365.com/api/qrCode?code=', encodeURIComponent(orderInfo.weixinPayQrcode), '&t=', new Date().getTime()].join('');
                            console.info('订单号为:' + vm.orderNum + vm.weixinQrcode);
                            break;
                    }
                }
            });
        };

        $scope.$on("$destroy", function () {
            Tool.cancelTimeout(timer);
            Tool.cancelTimeout(timer2);
        });
    }])

    .controller('TakeGoodsController', ['$scope', 'Tool', 'toast', 'AppService', 'loading', '$timeout', function ($scope, Tool, toast, AppService, loading, $timeout) {
        var vm = this;
        vm.orderNum = '';
        vm.code = '';
        vm.qrcodeProgress = false; //扫描二维码
        var timer = null;
        var isPayQrcode = false;

        vm.delay = function () {
            window.location.hash = '#/index';
        };

        vm.closeQrcode = function () {
            vm.qrcodeProgress = false;
            Tool.cancelTimeout(timer);
            isPayQrcode = false;
            toast.hide();
        };

        /**
         * 二维码取货
         */
        vm.openQrcodeDevice = function () {
            vm.qrcodeProgress = true;
            toast('请扫描您的二维码');
            isPayQrcode = true;
            openQrcodeDevice();
        };

        function openQrcodeDevice() {
            console.info('打开扫描枪');
            JSBridge.invokeSdkOpenQrcodeDevice(function (response) {
                console.info("获取到的二维码为: " + response);
                console.info('二维码扫描器页面是否打开: ' + (isPayQrcode ? '已打开' : '未打开'));

                if (!isPayQrcode) {
                    return;
                }

                var result = Tool.str2Json(response);
                switch (result.status) {
                    case 0: //扫码成功
                        toast('扫码成功');
                        AppService.takeGoodsByQrcode(result.qrcode).then(function (result) {
                            console.info("根据二维码获取的订单信息为: " + Tool.json2Str(result));
                            //调用SDK发货
                            JSBridge.invokeSdkTakeGoods(result.data.orderNum);
                            window.location.hash = '#/outting_goods';
                        }, function (result) {
                            toast(result.msg);
                            $timeout(function () {
                                vm.qrcodeProgress = false;
                                isPayQrcode = false;
                            });
                        });
                        break;
                    case 1: //扫码超时
                    case 2: //扫码失败
                        console.log(vm.qrcodeProgress);
                        if (vm.qrcodeProgress) {
                            isPayQrcode && toast(result.desc);
                        }
                        vm.qrcodeProgress = false;
                        isPayQrcode = false;
                        break;
                }

                timer = $timeout(function () {
                    isPayQrcode && openQrcodeDevice();
                }, 1000);
            });
        }

        vm.confirm = function () {
            if (Tool.isEmpty(vm.orderNum)) {
                return toast('订单号不能为空');
            }
            if (Tool.isEmpty(vm.code)) {
                return toast('取货码不能为空');
            }
            AppService.takeGoods(vm.orderNum, vm.code).then(function (result) {
                console.info('*********' + Tool.json2Str(result));

                if (result.data.orderStatus != 'PAY_SUCCESS') {
                    return toast('当前订单未支付或已取货');
                }
                //调用SDK发货
                JSBridge.invokeSdkTakeGoods(vm.orderNum);
                window.location.hash = '#/outting_goods';
            }, function (result) {
                toast(result.msg);
            });
        };

        $scope.$on("$destroy", function () {
            Tool.cancelTimeout(timer);
        });
    }])

    .controller('AdminLoginController', ['Tool', 'toast', 'AppService', 'loading', function (Tool, toast, AppService, loading) {
        var vm = this;
        vm.username = '';
        vm.password = '';

        JSBridge.invokeSdkShowNavigation();

        vm.login = function () {
//            if (Tool.isEmpty(vm.username)) {
//                return toast('用户名不能为空');
//            }
            if (Tool.isEmpty(vm.password)) {
                return toast('密码不能为空');
            }

            AppService.adminLogin(vm.username, vm.password).then(function (result) {
                AppService.setAdminData(result.data);
                window.location.hash = '#/admin_setting';
            }, function (result) {
                result && toast(result.msg);
            });
        };

        vm.logout = function () {
            loading.show();
            JSBridge.invokeSdkHideNavigation(function () {
                loading.hide();
                window.location.hash = '#/order';
            });
        }
    }])

    .controller('AdminSettingController', ['toast', 'AppService', 'loading', function (toast, AppService, loading) {
        var vm = this;

        vm.logout = function () {
            AppService.setAdminData({});
            loading.show();
            JSBridge.invokeSdkHideNavigation(function () {
                loading.hide();
                window.location.hash = '#/order';
            });
        };

    }])

    .controller('SettingImeiController', ['Tool', 'toast', 'AppService', function (Tool, toast, AppService) {
        var vm = this;
        vm.imei = AppService.getImei();

        vm.settingImei = function () {
            if (Tool.isEmpty(vm.imei)) {
                return toast('机器编号不能为空');
            }

            AppService.registerDevice(vm.imei).then(function (result) {
                toast('设置成功');
            }, function (result) {
                console.info(result);
                toast(result.msg || '设置失败');
            });

        }
    }])

    .controller('AddGoodsController', ['Tool', 'toast', 'AppService', 'loading', function (Tool, toast,  AppService,loading) {
        var vm = this;
        //vm.imei = AppService.getImei();
        vm.num = '';

        vm.confirm = function () {
//            if (Tool.isEmpty(vm.imei)) {
//                return toast('机器编号不能为空');
//            }
            if (Tool.isEmpty(vm.num)) {
                return toast('补货数量不能为空');
            }

             if (isNaN(vm.num)) {
                return toast('补货数量必须为数字');
            }


            loading.show();
            JSBridge.invokeSdkCheckGoodsStatus(function (result) {
                loading.hide();
                if (result.status == 0) {
                    switch (result.stock) {
                        case 'FF': //未知
                            return toast('查询米量失败,请检查设备是否正常');
                        case '00': //充足
                            return tipReplenish('充足','00',vm.num);
                        case '01': //不足
                            return tipReplenish('不足', '01',vm.num);
                        case '02': //提示补货
                            return tipReplenish('提示补货', '02',vm.num);
                        case '03': //满仓
                            return tipReplenish('满仓', '03',vm.num);
                    }
                } else {
                    toast('查询米量失败,请检查设备是否正常');
                }
            });

        }

        function tipReplenish(msg, code, num) {

            if ( confirm('查询当前米量为: ' + msg + ',您确认要提交补货数量以及状态吗?')) {
                AppService.replenish(code,num).then(function (result) {
                        toast('补货成功');
                  }, function (result) {
                        toast(result.msg);
                });
            }

        }


    }])


    .controller('NoGoodsController', function () {
        var vm = this;
        vm.delay = function () {
            window.location.hash = '#/index';
        }
    })

    .controller('OuttingGoodsController', function () {

    })

    .controller('SuccessController', ['$rootScope', function ($rootScope) {
        var vm = this;
        vm.remainingSum = $rootScope.SuccessCtl_$$_RemainSum;
        vm.cardScore = $rootScope.SuccessCtl_$$_CardScore;
        $rootScope.SuccessCtl_$$_RemainSum = '';
        $rootScope.SuccessCtl_$$_CardScore = null;

        vm.delay = function () {
            window.location.hash = '#/order';
        };
    }])

    .controller('ErrorController', function () {
        var vm = this;
        vm.delay = function () {
            window.location.hash = '#/order';
        };
    })

    .controller('NoWifiController', function (toast, $timeout) {
        var vm = this;
        vm.checkNetwork = function () {
            JSBridge.invokeSdkGetNetState(function (result) {
                console.info('network status: ' + result);
                if (result) {
                    console.info('检测有网,调回主页');
                    $timeout(function () {
                        window.location.hash = '#/index';
                    });
                } else {
                    console.info('当前没有网络');
                    $timeout(function () {
                        toast('当前没有网络');
                    });
                }
            });
        };

        vm.longTouchEvent = function () {
            JSBridge.invokeSdkSetupNetwork();
        };
    })
;