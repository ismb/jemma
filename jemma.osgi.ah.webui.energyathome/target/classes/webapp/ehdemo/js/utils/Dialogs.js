(function($) {
	var methods = {
		init : function(options) {
			var t = $(this);
			var opts = $.extend( {}, $.fn.speedometer.defaults, options);
			
			t.data('options', opts);

			return this.each(function() {
					$(this).bind('resize.speedometer', methods.repaint);
					methods.paint(t, opts);
			});
		},

		paint : function(elem, opts) {
			var parent = elem.get(0);
			
			// in IE7 the remove child has problems a safe version should be used
			// another issue is that the removeChild is defined in IE7 only
			// if the node has actually a child!
			if (parent.childNodes.length > 0)
				parent.removeChild(parent.firstChild);

			var container = document.createElement('div');
			container.style.position = 'relative';
			container.style.width = '100%';
			container.style.height = '100%';
			container.style.background = opts.background;

			parent.appendChild(container);

			var img1 = document.createElement('img');
			img1.src = srcPathSpeedometerAZXC + 'images/gauge_sfondo.png';
			img1.style.position = 'absolute';
			img1.style.width = '100%';
			img1.style.height = '100%';
			img1.style.left = '0px';
			img1.style.top = '0px';
			//img1.style.zIndex = -3;

			var img2 = document.createElement('img');
			img2.src = '';
			img2.style.position = 'absolute';
			img2.style.width = '100%';
			img2.style.left = '0px';
			//img2.style.zIndex = -2;
			
			if (!opts.rotate) {
				img2.style.height = '100%';
			}
			else {
				img2.style.height = '80%';		
				img2.style.top = '7%';
			}

			var img3 = document.createElement('img');
			img3.src = srcPathSpeedometerAZXC + 'images/gauge_center.png';
			img3.style.position = 'absolute';
			img3.style.width = '100%';
			img3.style.height = '100%';
			img3.style.left = '0px';
			img3.style.top = '0px';
			//img3.style.zIndex = -1;
			
			var textDiv = document.createElement('div');
			textDiv.style.position = 'absolute';
			textDiv.style.width = '100%';
			textDiv.style.height = '18%';
			textDiv.style.left = '0px';
			textDiv.style.bottom = '1%';
	
			var span = document.createElement('span');
			span.style.position = 'absolute';
			span.style.textAlign = 'center';
			span.style.left = '0px';
			span.style.right = '0px';
			span.style.top = '25%';
			span.style.fontWeight = 'bold';
			span.innerHTML = "Micola";
			
			textDiv.appendChild(span);

			container.appendChild(img1);
			container.appendChild(img2);
			container.appendChild(img3);
			container.appendChild(textDiv);
		},

		getImgPower : function(v, max) {
		
			var imgSrc = '';
			
			if (v < 0) 
				v = 0;
			
			var indImg = Math.floor(v / max * 81) + 1;

			// temporaneo per ovviare a parte rossa troppo piccola
			if (indImg > 81)
				indImg = 81;
			
			var imgPower = indImg + '-' + '01.png'
			return 'images/' + imgPower;
		},

		repaint : function() {
		},

		value : function(v, uom) {

			var opts = $(this).data('options');

			return this.each(function() {
				if (v < 0) {
					v = 0;
				}
				if (v > opts.max) {
					v = opts.max;
				}
				
				if (!opts.rotate) {
					var img = methods.getImgPower(v, opts.max);
					this.firstChild.children[1].src = srcPathSpeedometerAZXC + img;				
				}
				else {
					var maxAngle = 240;
					var minAngle = -30;
					angle = (v * maxAngle) / opts.max + minAngle;
					this.firstChild.children[1].src = srcPathSpeedometerAZXC + opts.gauge;
					$(this.firstChild.children[1]).rotate(angle);
				}
				this.firstChild.children[3].firstChild.innerHTML = v.toFixed(3) + " " + uom;
			});
		},

		destroy : function() {
			return this.each(function() {
				$(window).unbind('.gauge');
				this.removeChild(this.firstChild);
			})
		}
	};

	$.fn.speedometer = function(method) {
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(
					arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			$.error('Method ' + method + ' does not exist on jQuery.speedometer');
		}
	};

	$.fn.speedometer.defaults = {
		foreground : 'transparent',
		background : 'transparent',
		color : 'blue',
		granularity : 80,
		gauge : 'images/lancetta.png',
		rotate : false,
		unit : "",
		max : 4000
	};
})(jQuery);

var scr = document.getElementsByTagName('script');
var srcPathDialogsAZXC = scr[scr.length-1].src; 
var pos = srcPathDialogsAZXC.lastIndexOf("/");
srcPathDialogsAZXC = srcPathDialogsAZXC.substring(0, pos + 1);
