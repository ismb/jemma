
;(function($) {
    $.dialog = function(message, options) {
    	var defaults = {
    		buttons:  [],
    		rounded: true
    	};
    	
    	var settings = {};
    	
    	var plugin = this;
    	
    	plugin.init = function() {
    		this.settings = $.extend({}, defaults, options);
    		this.settings.message = message;
    		
    		$('.dialog').remove();
    		
    		plugin.isIE6 = ($.browser.msie && parseInt($.browser.version, 10) == 6) || false;
    		
    		if (plugin.isIE6)  {
    			plugin.settings.useButtons = false;
    		}
    		else {
    			plugin.settings.useButtons = true;
    		}
    		
    		plugin.main = jQuery('<div>', {
    			'class': 'dialog'
    		}).css({
    			'position': 'absolute',
    			'visibility': 'hidden',
    			'z-index': 3000
    		});
    		
    		if (plugin.settings.rounded) {
    			plugin.main.addClass('roundedBorders');
    		}
    		
    		if (plugin.settings.title != undefined)  {
        		// add the title
        		plugin.title = jQuery('<div>', {
        			'class': 'dialogTitle'
        		}).appendTo(plugin.main);
        		
        		plugin.title.html(plugin.settings.title);
        		
        		if (plugin.settings.rounded) {
        			plugin.title.addClass('topRoundedBorders');
        		}
    		}
    		
    		// add the message
    		plugin.message = jQuery('<div>', {
    			'class': 'dialogMessage'
    		}).appendTo(plugin.main);
    		
    		plugin.message.html(plugin.settings.message);
    		
    		if (plugin.settings.buttons.length > 0) {
    		
	    		// creates the button bar div
	    		plugin.buttons = jQuery('<div>', {
	    			'class': 'dialogButtonBar'
	    		}).appendTo(plugin.main);
	    		
	    		if (plugin.settings.rounded) {
	    			plugin.buttons.addClass('bottomRoundedBorders');
	    		}
	    		    				
	    		for (i = 0; i < this.settings.buttons.length; i++)  {
	    			var button;
	    			
	    			plugin.settings.useButtons = false;
	    			
	    			var float = 'none';
	    			if (plugin.settings.buttonPos == 'left') {
	    				float = 'left';
	    			}
	    			else if (plugin.settings.buttonPos == 'right') {
	    				float = 'right';
	    			}
	    			
	    			var value = this.settings.buttons[i];
	    			
	    			if (plugin.settings.useButtons)  {
		    			button = jQuery('<input>',  {
		    				'type': 'button',
		    				'value': value,
		    				'name': value
		    			}).appendTo(plugin.buttons); 
	    			}
	    			else {
		    			button = jQuery('<a>',  {
	                        'href':     'javascript:void(0)'
		    			}).css({
		    				'float': float
		    			}).appendTo(plugin.buttons).html(value);    				
	    			}
	    			
	                button.bind('click', function() {
	                	if (plugin.settings.useButtons)
	                		plugin.buttonPressed(this.value);
	                	else 
	                		plugin.buttonPressed($(this).html());
	                });
	    		}
	    		
	    		if (!plugin.settings.useButtons)  {
	                jQuery('<div>', {
	                    'style':    'clear:both'
	                }).appendTo(plugin.buttons);
	    		}
    		}
    		
            $(window).bind('resize', plugin.paint);
    		
    		plugin.main.appendTo('body');
    		plugin.paint();
    		
    		if (plugin.settings.buttons.length == 0) {
    			plugin.main.fadeOut(2000, plugin.close);
    		}
    	}
    	
    	plugin.buttonPressed = function(value) {
    		if (plugin.settings.exit != undefined) {
    			plugin.settings.exit(value);
    		}
    		
    		plugin.main.fadeOut(500, plugin.close);
    	}
    	
    	plugin.close = function () {
    		plugin.main.remove();
    	}
    	
    	plugin.paint = function() {
    		
            // get the viewport width and height
            var viewport_width = $(window).width();
            var viewport_height = $(window).height();  
            
           
    		// position the dialog in the center of the viewport
    		plugin.main.css({
    			'top': (viewport_height - plugin.main.height()) / 2,
    			'left': (viewport_width - plugin.main.width())/ 2,
    			'visibility': 'visible'
    		});
    	}
    	
    	return this.init();
    }
})(jQuery);
