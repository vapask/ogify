var REQUEST_URL = "http://ogify-miptsail.rhcloud.com";

function getOrdersById(id) {
	$.ajax({
        type: "GET",
        url: REQUEST_URL + "/rest/orders/",
        data: {order_id: id},
        success: function(result) {

            console.log(result);
        },
        error: function() {
            /**
             * nothing to do
             */
        }
    });
}

function authViaKey() {
    var key = $('#betaKey').val();
    $.ajax({
        type: "GET",
        url: '/rest/auth/getRequestUri?betaKey=' + key,
        success: function(result) {
            window.location.href = result["requestUri"];
        },
        error: function(result) {
            $('#loginError').show();
        }
    });
}

function logInSN(opts) {
    $.ajax({
        type: "GET",
        url: '/rest/auth/getRequestUri?=' + opts.sn,
        success: function(result) {
            var url = result["requestUri"];
            window.location.href = url;
        },
        error: function() {
            /**
             * nothing to do
             */
        }
    });
}
