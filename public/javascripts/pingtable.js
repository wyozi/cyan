function fetchContents(link, divId) {
    $.ajax({
        url: link,
        success: function(response) {
            $('#' + divId).text(response);
        }
    });
    return '<div id="'+ divId +'">Loading...</div>';
}

$(document).ready(function() {

    $('a[data-backend-query-popover]').popover({
        html: true,
        trigger: 'hover',
        placement: 'bottom',
        content: function() {
            var divId =  "tmp-id-" + $.now();
            var query = $(this).data("backend-query-popover");
            var productId = $(this).data("backend-productid");
            var license = $(this).data("backend-license");
            var queryRoute = jsAdminRoutes.controllers.admin.BackendController.view(query, productId, license);
            return fetchContents(queryRoute.url, divId);
        }
    });

    $('a[data-backend-query-replace]').each(function() {
        var query = $(this).data("backend-query-replace");
        var replaceFormat = $(this).data("backend-query-replace-format");
        var queryRoute = jsAdminRoutes.controllers.admin.BackendController.view(query, null, null);

        var params = ($(this).data("backend-query-params") || "");

        var that = $(this);

        $.ajax({
            url: queryRoute.url + "&" + params,
            success: function(response) {
                var data = JSON.parse(response);
                that.attr("title", that.text());

                var newContent = (replaceFormat || "[no format provided]").replace(/{([^}]*)}/g, function(match, tag) {
                    if (tag in data) {
                        return data[tag];
                    } else {
                        return '[{' + tag + '} not provided]';
                    }
                });
                that.text(newContent);
                that.html(that.html().replace(/\n/g,"<br>")); // line breaks to <br>
            }
        });
    });
});