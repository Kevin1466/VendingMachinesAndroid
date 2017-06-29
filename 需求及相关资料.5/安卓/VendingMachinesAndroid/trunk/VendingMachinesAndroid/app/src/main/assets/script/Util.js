Array.prototype.removeAt = function (index) {
    if (index > -1) {
        this.splice(index, 1);
    }
};

Array.prototype.remove = function (val) {
    var index = this.indexOf(val);
    if (index > -1) {
        this.splice(index, 1);
    }
};

angular.module('util', ['ngAnimate'])

    .filter('toRMB', [function () {
        return function (val) {
            if (val) {
                return val / 100;
            }
            return val;
        }
    }])

    .factory('timeoutInterceptor', ["$q", "$rootScope", function ($q, $rootScope) {
        return {
            request: function (config) {
                config.timeout = 1000 * 30;
                return config || $q.when(config)
            }
        };
    }])

    .directive('ourpalmLongTouch', function () {
        return {
            controller: ['$scope', '$rootScope', '$element', '$interval', '$attrs', '$timeout', function ($scope, $rootScope, $element, $interval, $attrs, $timeout) {
                var count = 0;
                var method = $element.attr('ourpalm-long-touch');
                var times = $element.attr('ourpalm-long-touch-times') || 10;
                var timer = null;
                console.log('当前页面存在ourpalm-long-touch指令,指令的touch时间为: ' + times + 's');
                $element.bind('touchstart', function () {
                    timer = $interval(function () {
                        count++;
                        //console.info('ourpalm-long-touch指令,当前指令touch时间为: ' + count + 's');
                        if (count > times) {
                            $timeout(function () {
                                $scope.$apply(method);
                            });
                            count = 0;
                            timer && $interval.cancel(timer);
                        }
                    }, 1000);
                });
                $element.bind('touchend', function () {
                    count = 0;
                    timer && $interval.cancel(timer);
                });
                $scope.$on('$destroy', function () {
                    timer && $interval.cancel(timer);
                });
            }]
        }
    })

    .directive('ourpalmTouchDelay', function () {
        return {
            controller: ['$scope', '$rootScope', '$element', '$interval', '$attrs', '$timeout', function ($scope, $rootScope, $element, $interval, $attrs, $timeout) {
                var count = 0;
                var method = $element.attr('ourpalm-touch-delay');
                var times = $element.attr('ourpalm-touch-delay-times') || 30;
                console.log('当前页面存在ourpalm-touch-delay指令,指令delay时间为:' + times + 's');
                var stopTimer = false;
                var timer = $interval(function () {
                    if (!stopTimer) {
                        count++;
                        //console.log('ourpalm-touch-delay指令,当前delay时间为:' + count + 's');
                        if (count > times) {
                            $timeout(function () {
                                $scope.$apply(method);
                            });
                            count = 0;
                        }
                    }
                }, 1000);

                $element.bind('touchstart', function () {
                    count = 0;
                    stopTimer = true;
                });
                $element.bind('touchend', function () {
                    count = 0;
                    stopTimer = false;
                });
                $scope.$on('$destroy', function () {
                    timer && $interval.cancel(timer);
                });
            }]
        }
    })

    .directive('ourpalmInputFocus', [function () {
        return {
            restrict: 'AE',
            scope: true,
            replace: true,
            template: '<div style="display:none;"><div class="ourpalm-input-focus-mask"></div><button class="ourpalm-input-focus-btn">完成</button></div>',
            link: function (scope, ele, attrs) {
                $(document).on('focus', ':text,:password', function () {
                    $(this).each(function () {
                        if (this.hasAttribute('readonly')) {
                            return;
                        }
                        $(this).addClass('ourpalm-input-focus');
                        $(ele).show();
                    });
                }).on('focusout', ':text,:password', function () {
                    $(this).each(function () {
                        $(this).removeClass('ourpalm-input-focus');
                        $(ele).hide();
                    });
                });
            }
        }
    }])

    .directive('ourpalmGoto', ['$timeout', '$window', function ($timeout, $window) {
        return {
            restrict: 'A',
            scope: true,
            link: function (scope, ele, attrs) {
                ele.on('click', function () {
                    window.location.hash = attrs.ourpalmGoto;
                });
            }
        }
    }])

    .directive('ourpalmReload', ['$timeout', '$window', function ($timeout, $window) {
        return {
            restrict: 'A',
            scope: true,
            link: function (scope, ele, attrs) {
                ele.on('click', function () {
                    window.location.reload();
                });
            }
        }
    }])

    .factory('toast', ['$rootScope', function ($rootScope) {
        var toast = function (val) {
            $rootScope.$broadcast('toast-new', val);
        };

        toast.hide = function () {
            $rootScope.$broadcast('toast-hide');
        };

        return toast;
    }])

    .directive('ourpalmToast', ['$sce', '$timeout', function ($sce, $timeout) {
        return {
            replace: true,
            restrict: 'EA',
            scope: true,
            link: function ($scope, $ele, $attrs) {
                $attrs.$set('ngShow', 'vm.isShow');
                $scope.$watch($attrs.ngShow, function (newValue, oldValue, scope) {
                    if (newValue) {
                        $ele.css('display', 'block');
                    } else {
                        $ele.css('display', 'none');
                    }
                });
            },
            template: '<div class="ourpalm-toast">{{vm.message}}</div>',
            controllerAs: 'vm',
            controller: ['$scope', function ($scope) {
                var vm = this;
                $scope.$on('toast-new', function (event, val) {
                    vm.isShow = false;
                    vm.timer && $timeout.cancel(vm.timer);
                    window.setTimeout(function () {
                        $timeout(function () {
                            vm.message = val;
                            vm.isShow = true;
                            vm.timer = $timeout(function () {
                                vm.isShow = false;
                                vm.message = '';
                                vm.timer = null;
                            }, 2000);
                        }, 10);
                    });
                });
                $scope.$on('toast-hide', function (event, val) {
                    vm.isShow = false;
                });
            }]
        }
    }])

    // .directive('ourpalmToast', ['$sce', '$timeout', function ($sce, $timeout) {
    //     return {
    //         replace: true,
    //         restrict: 'EA',
    //         scope: true,
    //         template: '<div><div ng-repeat="toast in toast_map" class="ourpalm-toast">{{toast.val}}</div></div>',
    //         link: function (scope, elements, attrs) {
    //             scope.toast_map = [];
    //             scope.$on('toast-new', function (event, val) {
    //                 var key = new Date().getTime() + Math.random();
    //                 var data = {key: key, val: val};
    //                 scope.toast_map.push(data);
    //                 $timeout(function () {
    //                     scope.toast_map.remove(data);
    //                 }, 3000);
    //             });
    //             scope.$on('toast-hide', function (event, val) {
    //                 console.info('toast hide');
    //                 scope.toast_map = [];
    //             });
    //         }
    //     }
    // }])

    .directive('ourpalmMessager', function ($timeout) {
            return {
                restrict: 'EA',
                scope: true,
                template: function (element, attrs) {
                    element.addClass('ourpalm-shake').css('display', 'none');
                    element.append('<b ng-bind="message"></b>');
                },
                link: function ($scope, $element, attrs) {
                    var isShow = false;
                    $scope.$on('messager-show', function (event, val) {
                        $scope.message = val;
                        $element.css('display', 'block');
                        if (!isShow) {
                            $timeout(function () {
                                $element.css('display', 'none');
                                isShow = false;
                            }, 3000);
                        }
                        isShow = true;
                    });
                }
            }
        }
    )

    .factory('messager', ['$rootScope', function ($rootScope) {
        return function (val) {
            $rootScope.$broadcast('messager-show', val);
        }
    }])

    /**
     * loading 指令
     */
    .directive('ourpalmAjaxLoading', function () {
        var count = 0;
        return {
            restrict: 'AE',
            replace: true,
            template: '<div style="display:none;"><div style="position:fixed;top:0px;left:0px;border:0px none;padding:0px;margin:0px;width:100%;height:100%;z-index:100;background:rgb(224, 236, 255) none repeat scroll 0% 0%;opacity:0.5;;"></div><img src="img/ajax_loading.gif" style="position:fixed;top:50%;left:50%;width:32px;height:32px;margin-top:-16px;margin-left:-16px;z-index:1001"/></div>',
            link: function ($scope, element, attrs) {
                $scope.$on('ajaxLoadingInterceptor:show', function () {
                    count++;
                    if (count > 0) {
                        element.css('display', 'block');
                    }
                });
                $scope.$on('ajaxLoadingInterceptor:hide', function () {
                    count--;
                    if (count <= 0) {
                        element.css('display', 'none');
                    }
                });
            }
        }
    })

    .factory('loading', ['$rootScope', function ($rootScope) {
        function show() {
            $rootScope.$broadcast("ajaxLoadingInterceptor:show")
        }

        function hide() {
            $rootScope.$broadcast("ajaxLoadingInterceptor:hide")
        }

        return {
            show: show,
            hide: hide
        }
    }])

    /**
     * 在 HttpModule 模块上添加 拦截器 用来给http请求 增加 mask loading
     * 通过 angular 指令 实现
     */
    .factory('ajaxLoadingInterceptor', ["$q", "$rootScope", 'loading', function ($q, $rootScope, loading) {
        return {
            request: function (config) {
                config.mask === true && loading.show();
                return config || $q.when(config)
            },
            response: function (response) {
                response.config.mask === true && loading.hide();
                return response || $q.when(response);
            },
            responseError: function (response) {
                response.config.mask === true && loading.hide();
                return $q.reject(response);
            }
        };
    }])

    /**
     * 统一的异常处理
     */
    .factory('httpErrorInterceptor', ['$q', 'toast', function ($q, toast) {
        return {
            responseError: function (response) {
                switch (response.status) {
                    case -1:
                        toast('网络异常,请稍后再试');
                        break;
                    case 200:
                        return $q.resolve(response);
                    case 404:
                        toast('请求地址不存在');
                        break;
                    case 500:
                        toast('服务器开小差了,请稍后再试');
                        break;
                    default:
                        toast('未知错误(' + response.status + ')')
                }
                return $q.reject(response);
            }
        }
    }])

    .directive('ourpalmBack', function () {
        return {
            restrict: 'AE',
            link: function (scope, element, attrs) {
                element.on('click', function () {
                    window.history.back();
                })
            }
        }
    })

    .service('Tool', ['$timeout', function ($timeout) {
        /**
         * 检查是否是空字符串
         */
        function isEmpty(val) {
            return !!val ? ('' == $.trim(val) ? true : false) : true;
        }

        /**
         * 通过 localstorage 保存结果
         */
        function storeSet(key, value) {
            if (window.localStorage) {
                try {
                    window.localStorage.setItem(key, value);
                } catch (err) {
                    console.info(err);
                }
            }
        }

        /**
         * 通过 localstorage 获取结果
         */
        function storeGet(key) {
            if (window.localStorage) {
                try {
                    return window.localStorage.getItem(key);
                } catch (err) {
                    console.info(err);
                }
                return null;
            }
            return null;
        }

        function str2Json(str) {
            if (typeof str == 'string') {
                try {
                    return JSON.parse(str);
                } catch (err) {
                    console.info(err);
                }
                return str;
            }
            return str;
        }

        function json2Str(json) {
            if (typeof json != 'string') {
                try {
                    return JSON.stringify(json);
                } catch (err) {
                    console.info(err);
                }
                return json;
            }
            return json;
        }

        function cancelTimeout(timeout) {
            if (timeout) {
                try {
                    $timeout.cancel(timeout);
                    console.info('cancel timeout success');
                } catch (err) {
                    console.info(err);
                }
            }
        }

        return {
            storeGet: storeGet,
            storeSet: storeSet,
            str2Json: str2Json,
            json2Str: json2Str,
            isEmpty: isEmpty,
            cancelTimeout: cancelTimeout
        }
    }]);