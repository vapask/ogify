ogifyApp.controller('DashboardController', function ($rootScope, $scope, $filter, uiGmapGoogleMapApi,
                                                     $location, Order, myAddress, ClickedOrder,
                                                     UserProfile) {
    $scope.user = UserProfile.get();

    $scope.getOrdersLinks = function() {
        var showingOrdersIds = [];

        $scope.showingOrders.forEach(function(order) {
            showingOrdersIds.push(order.id);
        });

        if(showingOrdersIds.length > 0) {
            $scope.ordersLinks = Order.getOrdersLinks({ordersIds: showingOrdersIds});
        }
    };
    
    $scope.selfMarker = {
        coords  : { latitude: 55.927106, longitude: 37.523662 },
        id: "currentPosition",
        visible: false
    };

    $scope.markersEvents = {
        click: function(marker, eventName, model){
            $scope.setClickedOrder(model);
            $("#showOrderModal").modal();
        }
    };

    var mapChanged = function(map) {
        var bounds = map.getBounds();
        $scope.map.bounds.neLatitude = bounds.getNorthEast().lat();
        $scope.map.bounds.neLongitude = bounds.getNorthEast().lng();
        $scope.map.bounds.swLatitude = bounds.getSouthWest().lat();
        $scope.map.bounds.swLongitude = bounds.getSouthWest().lng();

        updateOrders();
    };

    $rootScope.map = {
        center: { latitude: 55.927106, longitude: 37.523662 },
        zoom: 10,
        bounds: {
            neLatitude: 55.95,
            neLongitude: 37.82,
            swLatitude: 55.76,
            swLongitude: 37.37
        },
        control: {},
        events: {
            zoom_changed: function (map) {
                $scope.doSpider = (map.getZoom() > 16);
            },
            dragend: mapChanged,
            idle: mapChanged
        }
    };

    $scope.clusterOptions = {
        gridSize: 60,
        ignoreHidden: true,
        minimumClusterSize: 2,
        maxZoom: 16
    };

    $scope.spiderOptions = {
        keepSpiderfied: true
    };

    $scope.doSpider = false;

    var getMaxOrdersInPage = function() {
        return Math.floor(Math.max((angular.element('.list-orders-height').height() - 2*angular.element('.row').height()) / (angular.element('#hidden-order').height() + angular.element('.row').height()), 1));
    };
    
    var getMaxPagesInBar = function() {
        return 9;
    };

    if (!!!$rootScope.pageParameters) {
        $rootScope.pageParameters = {
            pageSize: getMaxOrdersInPage(),
            pagesInBar: getMaxPagesInBar()
        };
    }
    
    $scope.$on('createdNewOrderEvent', function(event, order) {
    });
    $scope.$on('finishOrderEvent', function(event) {
        switchToInProgressOrders();
    });
    $scope.$on('takeOrderEvent', function(event) {
        switchToNearOrders();
    });
    
    var switchToInProgressOrders = function() {
        $scope.user.$promise.then(function(user) {
            UserProfile.getExecutingOrders({userId: user.userId}).$promise.then(function(data){
                $scope.executingOrders = data;
                $scope.showingOrders = data;
                $scope.totalPages = window.Math.ceil(data.length / $rootScope.pageParameters.pageSize);
                $scope.currentPage = {
                    page: 0,
                    pages: _.range(window.Math.min($scope.totalPages, $rootScope.pageParameters.pagesInBar))
                }
            });
        });
    };
    
    var switchToNearOrders = function(){
        Order.getNearMe($scope.map.bounds).$promise.then(function(data){
            $scope.showingOrders = data;
            $scope.getOrdersLinks();
            $scope.totalPages = window.Math.ceil(data.length / $rootScope.pageParameters.pageSize);
            $scope.currentPage = {
                page: 0,
                pages: _.range(window.Math.min($scope.totalPages, $rootScope.pageParameters.pagesInBar))
            }
        });
    };

    var updateOrders = function() {
        if ($location.path().indexOf('dashboard') > -1) {
            switchToNearOrders();
        } else {
            switchToInProgressOrders();
        }
    };
    updateOrders();

    $scope.setClickedOrder = function(order){
        ClickedOrder.set(order);
    };

    $scope.previousPage = function(currentPage){
        if (currentPage.page > 0) {
            currentPage.page -= 1;
            if (currentPage.page + 1 == currentPage.pages[0]) {
                Math = window.Math;
                currentPage.pages = _.range(Math.floor(currentPage.page / $rootScope.pageParameters.pagesInBar), 
                                       Math.min(Math.floor(currentPage.page / $rootScope.pageParameters.pagesInBar)
                                                           +$rootScope.pageParameters.pagesInBar,
                                                $scope.totalPages));
            }
        }
    };

    $scope.nextPage = function(currentPage){
        if (currentPage.page < $scope.totalPages - 1) {
            currentPage.page += 1;
            if (currentPage.page - 1 == currentPage.pages[currentPage.pages.length-1]) {
                currentPage.pages = _.range(currentPage.page,
                                       window.Math.min(currentPage.page + $rootScope.pageParameters.pagesInBar, $scope.totalPages));
            }
        }
    };

    $scope.setPage = function(currentPage, i){
        currentPage.page = i;
    };

    uiGmapGoogleMapApi.then(function(maps) {
        $scope.maps = maps;
        if(!!navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function(position) {
                $scope.map.center = { latitude: position.coords.latitude, longitude: position.coords.longitude };

                var geocoder = new google.maps.Geocoder();
                var myposition = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
                geocoder.geocode({'latLng': myposition},function(data, status) {
                    if(status == google.maps.GeocoderStatus.OK) {
                        myAddress.setAddress(
                            data[0].formatted_address,
                            position.coords.latitude,
                            position.coords.longitude
                        );
                    }
                });

                $scope.map.control.refresh($scope.map.center);
                $scope.map.zoom = 11;

                //personal marker init
                selfMarker = {
                    options: {
                        draggable: true,
                        animation: google.maps.Animation.DROP,
                        icon: 'libs/images/man_marker.png'
                    },
                    coords: {
                        latitude: position.coords.latitude,
                        longitude: position.coords.longitude
                    },
                    events: {
                        dragend: function (marker, eventName, args) {
                            var latitude = marker.getPosition().lat();
                            var longitude = marker.getPosition().lng();
                            var geocoder = new google.maps.Geocoder();
                            var myposition = new google.maps.LatLng(latitude, longitude);
                            geocoder.geocode({'latLng': myposition},function(data,status) {
                                if(status == google.maps.GeocoderStatus.OK) {
                                    myAddress.setAddress(
                                        data[0].formatted_address,
                                        latitude,
                                        longitude
                                    );
                                }
                            });
                        }
                    },
                    id: "currentPosition",
                    visible: true
                };
                $scope.selfMarker = selfMarker;
            });
        }
    });
    
    $scope.getExpireDate = function(order) {
        return $filter('date')(order.expireIn, 'd MMMM yyyy HH:mm');
    };
});
