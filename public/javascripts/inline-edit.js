
$.fn.inlineEditable = function(opt) {
    var type = opt.type;

    function spawnEditor(target, onDone) {
        target.hide();

        var isArea = type === "textarea";

        var wrapper;
        if (isArea) {
            wrapper = $("<div>");
        } else {
            wrapper = $("<span>")
                .addClass("form-inline");
        }

        var input;
        if (isArea) {
            input = $("<textarea>")
                .attr("rows", 4)
                .addClass("form-control")
                .val(target.text());
        } else {
            input = $("<input>")
                .addClass("form-control")
                .val(target.text());
        }

        var btnOk = $("<button>")
            .text("OK")
            .addClass("btn")
            .addClass("btn-success")
            .click(function() {
                var data = {};
                data[opt.postDataKey] = input.val();
                $.extend(data, opt.extraData);
                $.post(opt.url, data, function(data) {
                    target.text(input.val());

                    wrapper.remove();
                    target.show();

                    onDone();
                });
            });

        var btnCancel = $("<button>")
            .text("Cancel")
            .addClass("btn")
            .addClass("btn-danger")
            .click(function() {
                wrapper.remove();
                target.show();

                onDone();
            });

        input
            .css("border-top-right-radius", 0)
            .css("border-bottom-right-radius", 0)
            .appendTo(wrapper);
        btnOk
            .css("border-radius", 0)
            .appendTo(wrapper);
        btnCancel
            .css("border-top-left-radius", 0)
            .css("border-bottom-left-radius", 0)
            .appendTo(wrapper);
        wrapper.insertAfter(target);
    }

    this.click(function() {
        var activator;
        var target;

        if (opt.target) {
            target = opt.target;
            activator = $(this);

            activator.hide();
        } else {
            target = $(this);
        }

        spawnEditor(target, function() {
            activator.show();
        });
    });
};