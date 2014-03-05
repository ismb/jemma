// Apprise 1.5 by Daniel Raftery
// http://thrivingkings.com/apprise
//
// Button text added by Adam Bezulski
//

function apprise(string, args, callback) {
    var default_args =
    {
        'confirm': false, 		// Ok and Cancel buttons
        'verify': false,		// Yes and No buttons
        'input': false, 		// Text input (can be true or string for default text)
        'animate': false,		// Groovy animation (can true or number, default is 400)
        'textOk': 'Ok',		// Ok button default text
        'textCancel': 'Annulla',	// Cancel button default text
        'textYes': 'Si',		// Yes button default text
        'textNo': 'No'		// No button default text
    }

    if (args) {
        for (var index in default_args) {
            if (typeof args[index] == "undefined") args[index] = default_args[index];
        }
    }

    var aHeight = $(document).height();
    var aWidth = $(document).width();
    $('body').append("<div id=\"appriseToHide\" class=\"apprisehiding ui-widget-overlay ui-front\"></div>").append('<div class="appriseOverlay" id="aOverlay"></div>');
    $('.appriseOverlay').css('height', aHeight).css('width', aWidth).fadeIn(100);
    $('body').append('<div class="appriseOuter"></div>');
    $('.appriseOuter').append('<div class="appriseInner"></div>');
    $('.appriseInner').append(string);
    $('.appriseOuter').css("left", ( $(window).width() - $('.appriseOuter').width() ) / 2 + $(window).scrollLeft() + "px");
    $('.appriseOuter').css("top", ( $(window).height() - $('.appriseOuter').height() ) / 2 + $(window).scrollTop() + "px");
    var vtop = ($(window).height() - $('.appriseOuter').height() ) / 2 + $(window).scrollTop() + "px";
    if (args) {
        if (args['animate']) {
            var aniSpeed = args['animate'];
            if (isNaN(aniSpeed)) {
                aniSpeed = 400;
            }
            $('.appriseOuter').css('top', '-200px').show().animate({top: vtop}, aniSpeed);
        }
        else {
            $('.appriseOuter').css('top', vtop).fadeIn(200);
        }
    }
    else {
        $('.appriseOuter').css('top', vtop).fadeIn(200);
    }

    if (args) {
        if (args['input']) {
            if (typeof(args['input']) == 'string') {
                $('.appriseInner').append('<div class="aInput"><input type="text" class="aTextbox" t="aTextbox" value="' + args['input'] + '" /></div>');
            }
            else {
                $('.appriseInner').append('<div class="aInput"><input type="text" class="aTextbox" t="aTextbox" /></div>');
            }
            $('.aTextbox').focus();
        }
    }

    $('.appriseInner').append('<div class="aButtons"></div>');
    if (args) {
        if (args['confirm'] || args['input']) {
            $('.aButtons').append('<button value="ok">' + args['textOk'] + '</button>');
            $('.aButtons').append('<button value="cancel">' + args['textCancel'] + '</button>');
        }
        else if (args['verify']) {
            $('.aButtons').append('<button value="ok">' + args['textYes'] + '</button>');
            $('.aButtons').append('<button value="cancel">' + args['textNo'] + '</button>');
        }
        else if (args['newVerify']) {
            $('.aButtons').append('<button value="ok">' + args['textYes'] + '</button>');
            $('.aButtons').append('<button value="cancel">' + args['textNo'] + '</button>');
            $('.aButtons').append('<button value="no">' + args['textCancel'] + '</button>');
        }
        else {
            $('.aButtons').append('<button value="ok">' + args['textOk'] + '</button>');
        }
    }
    else {
        $('.aButtons').append('<button value="ok">Ok</button>');
    }

    $(document).keydown(function (e) {
        if ($('.appriseOverlay').is(':visible')) {
            if (e.keyCode == 13) {
                $('.aButtons > button[value="ok"]').click();
            }
            if (e.keyCode == 27) {
                $('.aButtons > button[value="cancel"]').click();
            }
        }
    });

    var aText = $('.aTextbox').val();
    if (!aText) {
        aText = false;
    }
    $('.aTextbox').keyup(function () {
        aText = $(this).val();
    });

    $('.aButtons > button').click(function () {
        $(".apprisehiding").remove();
        $('.appriseOverlay').remove();
        $('.appriseOuter').remove();
        if (callback) {
            var wButton = $(this).attr("value");
            if (wButton == 'ok') {
                if (args) {
                    if (args['input']) {
                        callback(aText);
                    }
                    else {
                        callback(true);
                    }
                }
                else {
                    callback(true);
                }
            }
            else if (wButton == 'cancel') {
                callback(false);
            }
            else if(wButton == 'no')
            {
                callback(null);
            }
        }
    });
}
