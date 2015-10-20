/**
 * Created by melge on 12.07.2015.
 */

var BASE_PATH = '/rest';
var AUTH_PATH = '/auth';
var PROFILE_PATH = '/user';
var ORDER_PATH = '/orders';

var ogifyServices = angular.module('ogifyServices', ['ngResource']);

ogifyServices.factory('AuthInterceptor', ['$q', '$rootScope', function ($q, $rootScope) {
    return {
        'response': function (response) {
            if (response.config.url != BASE_PATH + AUTH_PATH + '/getRequestUri') {
                $rootScope.authenticated = true;
            }

            return response;
        },
        'responseError': function (response) {
            if (response.status == 401) {
                $rootScope.authenticated = false;
            }

            return $q.reject(response);
        }
    };
}]);

/**
 * Resource for call to getRequestUri method on server.
 *
 * Social network (sn) must be provided, or vk will be used instead.
 * @type {Object}
 */
ogifyServices.factory('AuthResource', ['$resource', '$rootScope',
    function ($resource, $rootScope, AuthInterceptor) {
        return $resource(BASE_PATH + AUTH_PATH + '/getRequestUri', {}, {
            getVkUri: {method: 'GET', params: {sn: 'vk'}},
            getFacebookUri: {method: 'GET', params: {sn: 'facebook'}},
            authenticationStatus: {
                method: 'GET', url: BASE_PATH + AUTH_PATH + '/isAuthenticated', params: {},
                interceptor: AuthInterceptor
            }
        });
    }
]);

ogifyServices.factory('UserProfile', ['$resource', 'AuthInterceptor',
    function ($resource, AuthInterceptor) {
        return $resource(BASE_PATH + PROFILE_PATH, {}, {
            getCurrentUser: {method: 'GET', params: {}, interceptor: AuthInterceptor},
            get: {url: BASE_PATH + PROFILE_PATH + '/:userId', method: 'GET'},
            getFriends: {url: BASE_PATH + PROFILE_PATH + '/:userId/friends', isArray: true, params: {
                userId: '@userId'}},
            getExecutingOrders: {url: BASE_PATH + PROFILE_PATH + '/:userId/executing', isArray: true, params: {
                userId: '@userId'}},
            getCreatedOrders: {url: BASE_PATH + PROFILE_PATH + '/:userId/created', isArray: true, params: {
                userId: '@userId'}}
        });
    }
]);

ogifyServices.factory('Order', ['$resource',
    function($resource) {
        return $resource(BASE_PATH + ORDER_PATH, {}, {
            get: {url: BASE_PATH + ORDER_PATH + '/:orderId', method: 'GET', params: {orderId: '@orderId'}},
            create: {method: 'POST'},
            getNearMe: {url: BASE_PATH + ORDER_PATH + '/near', method: 'GET',
                params: {neLatitude: '', neLongitude: '', swLatitude: '', swLongitude: ''}, isArray: true},
            getDoneOrders: {url: BASE_PATH + ORDER_PATH + '/done', method: 'GET',
                params: {latitude: '', longitude: ''}},
            getMyOrders: {method: 'GET', isArray: true},
            getToExecution: {url: BASE_PATH + ORDER_PATH + '/:orderId' +'/getToExecution', method: 'PUT', 
                params: {orderId: '@orderId'}},
            getItemsList: {url: BASE_PATH + ORDER_PATH + '/:orderId' + 'items', method: 'GET', isArray: true},    
            getOrdersLinks: {url: BASE_PATH + ORDER_PATH + '/socialLinks', method: 'GET'},
            changeStatus: {url: BASE_PATH + ORDER_PATH + '/:orderId' +'/status', method: 'PUT', 
                params: {orderId: '@orderId'}},
            denyOrderExecution: {url: BASE_PATH + ORDER_PATH + '/:orderId' +'/executor', method: 'DELETE', 
                params: {orderId: '@orderId'}}
        });
}]);
