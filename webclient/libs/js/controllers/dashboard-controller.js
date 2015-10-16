ogifyApp.controller('DashboardController', function ($rootScope, $scope, $filter, uiGmapGoogleMapApi,
                                                     $location, Order, myAddress, ClickedOrder,
                                                     UserProfile) {
    $scope.user = UserProfile.get();

    $scope.getOrdersLinks = function() {
        var showingOrdersIds = [];

        $scope.showingOrders.forEach(function(order, i, arr) {
            showingOrdersIds.push(order.id);
        });

        if(showingOrdersIds.length > 0) {
            $scope.ordersLinks = Order.getOrdersLinks({ordersIds: showingOrdersIds});
        }
    };
    
    $scope.selfMarker = {
        coords  : { latitude: 55.927106, longitude: 37.523662 },
        id: "currentPosition"
    };

    $rootScope.map = {
        center: { latitude: 55.927106, longitude: 37.523662 },
        zoom: 10,
        control: {}
    };
    
    var getMaxDescription = function() {
        return 50;
    }
    
    
    $scope.pageParameters = {
        descriptionLength: getMaxDescription()
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
            UserProfile.getExecutingOrders(user).$promise.then(function(data){
                $scope.executingOrders = data;
                $scope.showingOrders = data;
            });
        });
    };
    
    var switchToNearOrders = function(){
        Order.getNearMe($scope.map.center).$promise.then(function(data){
            $scope.showingOrders = data;
            $scope.getOrdersLinks();
        });
    };

    if ($location.path().indexOf('dashboard') > -1) {
        switchToNearOrders();
    } else {
        switchToInProgressOrders();
    }

    $scope.setClickedOrder = function(order){
        ClickedOrder.set(order);
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
                    id: "currentPosition"
                };
                $scope.selfMarker = selfMarker;
            });
        }
    });
    
    $scope.getExpireDate = function(order) {
        return $filter('date')(order.expireIn, 'd MMMM yyyy HH:mm');
    };
});
