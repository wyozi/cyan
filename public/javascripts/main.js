jQuery(document).ready(function($) {
    // Make table cells with a single link child clickable.
    $("td > a:only-child").parent()
        .addClass("clickable-cell")
        .click(function() {
            window.document.location = $(this).children("a").attr("href");
        });

    // Add tooltip functionality
    $('[data-toggle="tooltip"]').tooltip();

    // Convert all datetimes to "x ago"
    $("time.timeago").timeago();
});