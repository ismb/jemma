function isNumber (o) {
  return ! isNaN (o-0);
}

(function($) {
	var methods = {
		init : function(options) {
			var t = $(this);
			var opts = $.extend( {}, $.fn.speedometer.defaults, options);
			
			t.data('options', opts);

			return this.each(function() {
					$(this).bind('resize.speedometer', methods.repaint);
					if (opts.mode == 'rete'){
						methods.paintRE(t, opts);
					} else if (opts.mode == 'fotovoltaico'){
						methods.paintFV(t, opts);
					} else {
						methods.paint(t, opts);
					}
					
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
			//continer.setAttribute('id', 'speedOMeterDiv');
			container.style.position = 'relative';
			if (navigator.userAgent.indexOf('MSIE 7.0') > -1){
				container.style.width = '50%';
				container.style.height = '50%';
				container.style.left = '0px';
				container.style.top = '10px';
				
			} else {
				container.style.width = '100%';
				container.style.height = '100%';
				
			}
			container.style.background = opts.background;

			parent.appendChild(container);

			var img1 = document.createElement('img');
			
			var img = '';
			
			if (opts.max == 4.0) {
				img = srcPathSpeedometerAZXC + 'images/contatore_3kw.png';
			} else if (opts.max == 6.0) {
				img = srcPathSpeedometerAZXC + 'images/contatore_4.5kw.png';
			} else if (opts.max == 8.0) {
				img = srcPathSpeedometerAZXC + 'images/contatore_6kw.png';
			} else {
				img = srcPathSpeedometerAZXC + 'images/gauge_sfondo.png';
			}
			
			img1.src = img;
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
			span.innerHTML = "";
			
			textDiv.appendChild(span);

			container.appendChild(img1);
			container.appendChild(img2);
			container.appendChild(img3);
			container.appendChild(textDiv);
			
			if (Main.env == 0) console.log(parent);
		},

		paintFV : function(elem, opts) {
			var parent = elem.get(0);
			
			// in IE7 the remove child has problems a safe version should be used
			// another issue is that the removeChild is defined in IE7 only if the node has actually a child!
			if (parent.childNodes.length > 0)
				parent.removeChild(parent.firstChild);

			var container = document.createElement('div');
			//continer.setAttribute('id', 'speedOMeterDiv');
			container.style.position = 'relative';
			if (navigator.userAgent.indexOf('MSIE 7.0') > -1){
				container.style.width = '50%';
				container.style.height = '50%';
				container.style.left = '-60px';
				container.style.top = '10px';

			} else {
				container.style.width = '100%';
				container.style.height = '100%';
				
			}
			container.style.background = opts.background;

			parent.appendChild(container);

			var img1 = document.createElement('img');
			
			var img = null;
			switch(opts.max){
				case 1.25:
					img = srcPathSpeedometerAZXC + 'images/contatore_1kw_prod.gif';
					break;
				case 2.5:
					img = srcPathSpeedometerAZXC + 'images/contatore_2kw_prod.gif';
					break;
				case 4:
					img = srcPathSpeedometerAZXC + 'images/contatore_3kw_prod.gif';
					break;
				case 5:
					img = srcPathSpeedometerAZXC + 'images/contatore_4kw_prod.gif';
					break;
				case 6.25:
					img = srcPathSpeedometerAZXC + 'images/contatore_5kw_prod.gif';
					break;
				case 7.5:
					img = srcPathSpeedometerAZXC + 'images/contatore_6kw_prod.gif';
					break;
				case 14.5:
					img = srcPathSpeedometerAZXC + 'images/contatore_11kw_prod.png';
					break;
				default:
					img = srcPathSpeedometerAZXC + 'images/contatore_2kw_prod.gif';
					break;
			}
			
			
			img1.src = img;
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
			span.innerHTML = "";
			
			textDiv.appendChild(span);

			container.appendChild(img1);
			container.appendChild(img2);
			container.appendChild(img3);
			container.appendChild(textDiv);
			
			if (Main.env == 0) console.log(parent);
		},

		paintRE : function(elem, opts) {
			var parent = elem.get(0);
			
			// in IE7 the remove child has problems a safe version should be used
			// another issue is that the removeChild is defined in IE7 only
			// if the node has actually a child!
			if (parent.childNodes.length > 0)
				parent.removeChild(parent.firstChild);

			var container = document.createElement('div');
			//continer.setAttribute('id', 'speedOMeterDiv');
			container.style.position = 'relative';
			if (navigator.userAgent.indexOf('MSIE 7.0') > -1){
				container.style.width = '50%';
				container.style.height = '50%';
				container.style.left = '-60px';
				container.style.top = '10px';
				
			} else {
				container.style.width = '100%';
				container.style.height = '100%';
				
			}
			container.style.background = opts.background;

			parent.appendChild(container);

			var img1 = document.createElement('img');
			
			var img = '';
			
			if (opts.max == 4.0) {
				img = srcPathSpeedometerAZXC + 'images/contatore_3kw_rete.png';
			} else if (opts.max == 6.0) {
				img = srcPathSpeedometerAZXC + 'images/contatore_4.5kw_rete.png';
			} else if (opts.max == 8.0) {
				img = srcPathSpeedometerAZXC + 'images/contatore_6kw_rete.png';
			} else {
				img = srcPathSpeedometerAZXC + 'images/gauge_sfondo.png';
			}
			
			img1.src = img;
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
			span.innerHTML = "";
			
			textDiv.appendChild(span);

			container.appendChild(img1);
			container.appendChild(img2);
			container.appendChild(img3);
			container.appendChild(textDiv);
			
			if (Main.env == 0) console.log(parent);
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

		value : function(newValue, uom) {

			var opts = $(this).data('options');

			return this.each(function() {
				if (isNumber(newValue))
					v = newValue;
				else 
					v = 0;
				
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
				
				if (opts.showValue) {
					if (!isNumber(newValue)) {
						this.firstChild.children[3].firstChild.innerHTML = newValue;
					}
					else {
						this.firstChild.children[3].firstChild.innerHTML = newValue.toFixed(3) + " " + uom;
					}
				}
			});
		},
		
		set_max : function(v) {
			var opts = $(this).data('options');
			opts.max = v;
		},

		destroy : function() {
			return this.each(function() {
				$(window).unbind('.speedometer');
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
		showValue : false,
		unit : "",
		max : 4000
	};
})(jQuery);

var scr = document.getElementsByTagName('script');
var srcPathSpeedometerAZXC = scr[scr.length-1].src; 
var pos = srcPathSpeedometerAZXC.lastIndexOf("/");
srcPathSpeedometerAZXC = srcPathSpeedometerAZXC.substring(0, pos + 1);
