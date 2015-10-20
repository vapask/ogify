ogifyApp.controller('MyOrdersController', function ($scope, UserProfile, ClickedOrder) {
    // TODO: put user profile into service, to avoid server spamming.
    $scope.user = UserProfile.get();

    $scope.user.$promise.then(function(user) {
        $scope.myOrders = UserProfile.getCreatedOrders({userId: user.userId});
    });

    $scope.maxDescriptionLength = 50;
    $scope.maxAddressLength = 20;

    $scope.setClickedOrder = function(order) {
        ClickedOrder.set(order);
    };

    $scope.onlyNew = function(order) {
        return order.status == 'New';
    };

    $scope.onlyRunning = function(order) {
        return order.status == 'Running';
    };

    $scope.onlyDone = function(order) {
        return order.status == 'Completed';
    };

    $scope.$on('createdNewOrderEvent', function(event, order) {
        $scope.myOrders.push(order);
    });

});
