/**
 * Created by melge on 12.07.2015.
 */

var ogifyApp = angular.module('ogifyApp', ['ogifyServices', 'ngRoute', 'ngCookies', 'uiGmapgoogle-maps', 'angulartics',
    'angulartics.google.analytics', 'ngSanitize']);

ogifyApp.service('myAddress', function () {
    var address = {
        latitude: 0.0,
        longitude: 0.0,
        plainAddress: ''
    };
    return {
        getAddress: function () {
            return address;
        },
        setAddress: function(textAddress, latitude, longitude) {
            address.plainAddress = textAddress;
            address.latitude = latitude;
            address.longitude = longitude;
        }
    };
});

ogifyApp.config(function ($routeProvider, uiGmapGoogleMapApiProvider) {
    $routeProvider
        .when('/current', {
            templateUrl: 'templates/current.html'
        }).when('/dashboard', {
            templateUrl: 'templates/dashboard.html',
            controller: 'DashboardController'
        }).when('/profile/:userId', {
            templateUrl: 'templates/user-profile.html',
            controller: 'ProfilePageController',
            resolve: {
                // Hide detail order modal when we going to profile page
                // TODO: Rewrite it in more common way
                hideModal: function () {
                    angular.element('#showOrderModal').modal('hide');
                    return true;
                }
            }
        }).when('/my-orders', {
            templateUrl: 'templates/my-orders.html',
            controller: 'MyOrdersController'
        }).when('/in-progress', {
            templateUrl: 'templates/in-progress.html',
            controller: 'DashboardController'
        }).otherwise({
            redirectTo: '/dashboard'
        });

    uiGmapGoogleMapApiProvider.configure({
        key: 'AIzaSyB3JGdwrXd_unNoKWm8wLWzWO2NTjMZuHA',
        v: '3.17',
        libraries: 'weather,geometry,visualization',
        language: 'ru'
    });
});

ogifyApp.run(function ($rootScope, $http, $cookies, $window, $timeout) {
    $rootScope.navBarTemplateUri = 'templates/navbar/navbar.html';
    $rootScope.createOrderTemplateUri = 'templates/new-order.html';
    $rootScope.showOrderTemplateUri = 'templates/order-details.html'
    $rootScope.landingUri = '/landing';

    if(($cookies.get('sId') == undefined || $cookies.get('ogifySessionSecret') == undefined)
        && $window.location.hostname != 'localhost') {
        $window.location.replace($rootScope.landingUri);
    }

    var timeoutPromise;

    $rootScope.$watch(function () {
        return $http.pendingRequests.length > 0;
    }, function (v) {
        if (v) {

            timeoutPromise = $timeout(waitingDialog.show, 1500);
            //waitingDialog.show();
        } else {
            $timeout.cancel(timeoutPromise);
            waitingDialog.hide();
        }
    });
});

ogifyApp.controller('NavBarController', function ($scope, $window, $cookies, $location, AuthResource, UserProfile) {

    $scope.modalWindowTemplateUri = 'templates/navbar/auth-modal.html';

    //$scope.authenticationStatus = AuthResource.authenticationStatus();

    $scope.authVk = function () {
        AuthResource.getVkUri(function (data) {
            $window.location.href = data.requestUri;
        });
    };

    $scope.logoutSN = function () {
        var cookiesPath = {path: "/"};
        $cookies.remove("JSESSIONID", cookiesPath);
        $cookies.remove("ogifySessionSecret", cookiesPath);
        $cookies.remove("sId", cookiesPath);

        $window.location.reload();
    };

    $scope.updateOrderData = function() {
    };

    $scope.user = UserProfile.getCurrentUser();
    
    $scope.getClass = function (partOfPath) {
        if ($location.path().indexOf(partOfPath) > -1) {
            return 'active';
        } else {
            return '';
        }
    }
});

ogifyApp.controller('CreateOrderModalController', function ($rootScope, $scope, $filter, Order,
                                                            myAddress) {
    // Init telephone input
    $scope.telephoneInput = angular.element("#telephoneNumber");
    $scope.telephoneInput.intlTelInput({
        defaultCountry: "ru",
        preferredCountries: ["ru", "by", "ua"]
    });
    
    $scope.telephoneInput.blur(function () {
        if(this.value.length < 1) {
            this.classList.remove('iti-invalid-key');
            return;
        }

        var isNumberValid = $scope.telephoneInput.intlTelInput("isValidNumber");
        if(!isNumberValid) {
            this.classList.add('iti-invalid-key');
        } else {
            this.classList.remove('iti-invalid-key');
        }
    });

    $scope.telephoneInput.keyup(function(){
        var isNumberValid = $scope.telephoneInput.intlTelInput("isValidNumber");
        var error = $scope.telephoneInput.intlTelInput("getValidationError");
        if(!isNumberValid &&
            (error == intlTelInputUtils.validationError.TOO_LONG
            || error == intlTelInputUtils.validationError.IS_POSSIBLE)) {
            this.classList.add('iti-invalid-key');
        } else {
            this.classList.remove('iti-invalid-key');
        }
    });

    $scope.order = {
        expireDate: $filter('date')(new Date(), 'dd.MM.yyyy'),
        expireTime: $filter('date')(new Date(), 'H:MM'),
        reward: '',
        address: myAddress.getAddress(),
        namespace: 'FriendsOfFriends',
        description:'',
        items: [{}]
    };

    $scope.alerts = {warning: [], error: []};

    $scope.showAlert = function(message, type) {
        var alert = {message: message};
        $scope.alerts[type] = [alert];
    };

    $scope.hideAlert = function() {
        $scope.alerts.warning = [];
        $scope.alerts.error = [];
    };

    $scope.chooseTime = function() {
        var input = angular.element('#expire_in_time').clockpicker();
        input.clockpicker('show');
    };

    $scope.addToList = function() {
        $scope.order.items.push({});
    };

    $scope.itemInList = function(index) {
        return (index != $scope.order.items.length - 1);
    };

    $scope.removeFromList = function(index) {
        $scope.order.items.splice(index, 1);
    };

    $scope.createOrder = function() {
        var last_item_index = $scope.order.items.length - 1;
        var $last_item = $scope.order.items[last_item_index];
        if(!$last_item.comment) {
            $scope.order.items.splice(last_item_index, 1);
        }

        var newOrder = {
            items: $scope.order.items,
            expireIn: parseDate($scope.order.expireDate, $scope.order.expireTime).getTime(),
            latitude: myAddress.getAddress().latitude,
            longitude: myAddress.getAddress().longitude,
            reward: $scope.order.reward,
            telephoneNumber: $scope.telephoneInput[0].value.length > 0 ?
                $scope.telephoneInput.intlTelInput("getNumber") : null,
            status: 'New',
            owner: null,
            executor: null,
            address: $scope.order.address.plainAddress,
            doneAt: null,
            id: null,
            createdAt: null,
            namespace: $scope.order.namespace,
            description: $scope.order.description
        };

        var MAX_TEXT_SIZE = 200;
        var restrictions = [
            {
                isAppearing: newOrder.description.length > MAX_TEXT_SIZE,
                message: "Слишком длинное описание заказа"
            },
            {
                isAppearing: newOrder.reward.length > MAX_TEXT_SIZE,
                message: "Слишком длинное описание вознаграждения"
            },
            {
                isAppearing: newOrder.address.length > MAX_TEXT_SIZE,
                message: "Слишком длинный адрес"
            },
            {
                isAppearing: !$scope.telephoneInput.intlTelInput("isValidNumber")
                && $scope.telephoneInput[0].value.length > 0,
                message: "Некорректный номер телефона"
            }
        ];

        for (var i in restrictions) {
            if (restrictions[i].isAppearing) {
                $scope.showAlert(restrictions[i].message, 'warning');
                return;
            }
        }

        newOrder = Order.create(newOrder,
            function(successResponse) {
                angular.element('#createOrderModal').modal('hide');
                $scope.hideAlert();
                $scope.order = {
                    expireDate: $filter('date')(new Date(), 'dd.MM.yyyy'),
                    expireTime: $filter('date')(new Date(), 'hh:mm'),
                    reward: '',
                    address: myAddress.getAddress(),
                    namespace: 'FriendsOfFriends',
                    description:'',
                    items: [{}]
                };
                $rootScope.$broadcast('createdNewOrderEvent', newOrder);
            },
            function(errorResponse) {
                $scope.showAlert("Неизвестная техническая ошибка: попробуйте позже", 'error');
            }
        );
    };
});

ogifyApp.factory('ClickedOrder', function() {
    var ClickedOrder = {};
    ClickedOrder.order = {
        description: null,
        reward: null,
        items: [],
        address: null,
        expireIn: null,
        owner: {photoUri: null, fullName: null},
        executor: {photoUri: null, fullName: null}
    };
    ClickedOrder.set = function(order) {
        ClickedOrder.order = order;
    };
    return ClickedOrder;
});

ogifyApp.controller('ShowOrderModalController', function ($scope, $rootScope, $filter, ClickedOrder, Order, $interval) {
    $scope.timer = 60;
    var stop;
    $scope.startTimer = function() {
      // Don't start a new fight if we are already fighting
      if ( angular.isDefined(stop) ) return;

      stop = $interval(function() {
        if ($scope.timer > 0) {
          $scope.timer = $scope.timer - 1;
        } else {
          $scope.stopTimer();
        }
      }, 1000);
    };

    $scope.stopTimer = function() {
      if (angular.isDefined(stop)) {
        $interval.cancel(stop);
        stop = undefined;
      }
    };

    $scope.$on('$destroy', function() {
      // Make sure that the interval is destroyed too
      $scope.stopTimer();
    });

    $scope.getOrder = function() {
        return ClickedOrder.order;
    };
    $scope.getDescription = function() {
        return ClickedOrder.order.description;
    };
    $scope.getItemList = function() {
        return ClickedOrder.order.items;
    };
    $scope.itemsEmpty = function() {
        return (ClickedOrder.order.items.length == 0);
    };
    $scope.getOwnerName = function() {
        return ClickedOrder.order.owner.fullName;
    };
    $scope.getExecutorName = function() {
        return ClickedOrder.order.executor.fullName;
    };
    $scope.getOwnerPhotoUrl = function() {
        return ClickedOrder.order.owner.photoUri;
    };
    $scope.getExecutorPhotoUrl = function() {
        return ClickedOrder.order.executor.photoUri;
    };
    $scope.getAddress = function() {
        return ClickedOrder.order.address;
    };
    $scope.getReward = function() {
        return ClickedOrder.order.reward;
    };
    $scope.getStatus = function() {
        return ClickedOrder.order.status;
    };
    $scope.userTakesTask = function() {
        Order.getToExecution({orderId: ClickedOrder.order.id}, function(successResponse) {
                angular.element('#showOrderModal').modal('hide');
                $rootScope.$broadcast('takeOrderEvent');
            },
            function(errorResponse) {

        });
        $scope.startTimer();
    };
    $scope.orderToDone = function() {
        Order.changeStatus({orderId: ClickedOrder.order.id}, 2, function(successResponse) {
                angular.element('#showOrderModal').modal('hide');
                $rootScope.$broadcast('finishOrderEvent');
            },
            function(errorResponse) {
        });
    };
    $scope.cancelOrder = function() {
        Order.denyOrderExecution({orderId: ClickedOrder.order.id}, function(successResponse) {
                angular.element('#showOrderModal').modal('hide');
                $rootScope.$broadcast('finishOrderEvent');
            },
            function(errorResponse) {
        });
    };
    $scope.getExpireDate = function() {
        return $filter('date')(ClickedOrder.order.expireIn, 'd MMMM yyyy');
    };
    $scope.getExpireTime = function() {
        return $filter('date')(ClickedOrder.order.expireIn, 'HH:mm');
    };
})
.directive('myCurrentTime', ['$interval', 'dateFilter',
      function($interval, dateFilter) {
        // return the directive link function. (compile function not needed)
        return function(scope, element, attrs) {
          var stopTime; // so that we can cancel the time updates

          // used to update the UI
          function updateTime() {
            element.text(dateFilter(new Date()));
          }

          // watch the expression, and update the UI on change.
          scope.$watch(attrs.myCurrentTime, function(value) {
            updateTime();
          });

          stopTime = $interval(updateTime, 1000);

          // listen on DOM destroy (removal) event, and cancel the next UI update
          // to prevent updating time after the DOM element was removed.
          element.on('$destroy', function() {
            $interval.cancel(stopTime);
          });
        }
      }]);
