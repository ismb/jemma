(function($) {
	var methods = {
		init : function(options) {
			var t = $(this);  
			var opts = $.extend({}, $.fn.gaugePV.defaults, options);
			
			
			t.data('options', opts);
			
			return this.each(function() {
				//$(this).bind('resize', methods.repaint);
				if (opts.mode == 'IAC'){
					methods.paintIAC(t, opts);
				} else {
					methods.paint(t, opts);
				}
			});
		},
		
		paint : function(elem, opts) {			
		    var parent = elem.get(0);
			
			this.border = 1;
			var gaugePV = document.createElement('div');
			
			gaugePV.style.position = 'relative';
			gaugePV.style.bottom = '7%';
			gaugePV.style.width = '100%';
			gaugePV.style.height = '100%';
			gaugePV.style.background = opts.background;
			
			parent.appendChild(gaugePV);
			
			this.border = 0.033;
			
			this.img1 = document.createElement('img');
			this.img1.src = srcPathGaugePV + 'images/termometro_base_iac.png';
			this.img1.style.position = 'absolute';
			this.img1.style.width = '100%';
			this.img1.style.height = '100%';
			this.img1.style.left = '0px';
			this.img1.style.top = '0px';
			
			this.img2 = document.createElement('img');
			var imagename = '';
			if (opts.color == 'blue') {
				imagename = 'images/gradient_blu_iac.png';
			}
			else {
				imagename = 'images/gradient_giallo_iac.png';
			}
			this.img2.src = srcPathGaugePV + imagename;
			this.img2.style.position = 'absolute';
			this.img2.style.width = '92%';
			this.img2.style.height = '80%';
			this.img2.style.left = '3.6%';	
			this.img2.style.bottom = '3%';
			
			this.img3 = document.createElement('img');
			this.img3.src = srcPathGaugePV + 'images/termometro_PV.png';
			this.img3.style.position = 'absolute';
			this.img3.style.width = '100%';
			this.img3.style.height = '100%';
			this.img3.style.left = '0px';
			this.img3.style.top = '0px';
			
			if (opts.lock) {
				var ratio = this.img3.height / this.img3.width;
				//gauge.style.width = (parent.clientHeight / ratio) + "px";
			}
			
			gaugePV.appendChild(this.img1);
			gaugePV.appendChild(this.img2);
			gaugePV.appendChild(this.img3);	    
		},
		
		paintIAC : function(elem, opts) {			
		    var parent = elem.get(0);
			
			this.border = 1;
			var gaugePV = document.createElement('div');
			
			gaugePV.style.position = 'relative';
			gaugePV.style.width = '100%';
			gaugePV.style.height = '100%';
			gaugePV.style.background = opts.background;
			
			parent.appendChild(gaugePV);
			
			this.border = 0.033;
			
			this.img1 = document.createElement('img');
			this.img1.src = srcPathGaugePV + 'images/termometro_base'+suffIndicatoreT+'.png';
			this.img1.style.position = 'absolute';
			this.img1.style.width = '100%';
			this.img1.style.height = '80%';
			this.img1.style.left = '10px';
			this.img1.style.top = '10px';
			
			this.img2 = document.createElement('img');
			this.img2.src = srcPathGaugePV + 'images/gradient_grey'+suffIndicatoreT+'.png';
			this.img2.style.position = 'absolute';
			this.img2.style.width = '89.3%';
			this.img2.style.height = '80%';
			this.img2.style.left = '7.2%';	
			this.img2.style.bottom = '12%'; //'3.4%';
			
			this.img3 = document.createElement('img');
			this.img3.src = srcPathGaugePV + 'images/gradient_verde'+suffIndicatoreT+'.png';
			this.img3.style.position = 'absolute';
			this.img3.style.width = '89.3%';
			this.img3.style.height = '80%';
			this.img3.style.left = '7.2%';	
			this.img3.style.bottom = '12%'; //'3.4%';
			
			this.img4 = document.createElement('img');
			this.img4.src = srcPathGaugePV + indicatoreTermometro;
			this.img4.style.position = 'absolute';
			this.img4.style.width = '100%';
			this.img4.style.height = '80%';
			this.img4.style.left = '0px';
			this.img4.style.top = '10px';
			
			if (opts.lock) {
				var ratio = this.img4.height / this.img4.width;
				//gauge.style.width = (parent.clientHeight / ratio) + "px";
			}
			
			gaugePV.appendChild(this.img1);
			gaugePV.appendChild(this.img2);
			gaugePV.appendChild(this.img3);
			gaugePV.appendChild(this.img4);	    
		},

		repaint : function(elem) {
		},
		
		value: function(v) {
			if (isNaN(v)) 
				return;
			
			var opts = $(this).data('options');
			
			return this.each(function() {
				if (v < 0)  {
					v = 0;
				}
				if (v > opts.max) {
					v = opts.max;
				}
				//alert(v+ " "+opts.max);
				//v=1;
				//var border = this.children[1].children[1].style.bottom;
				//var height = ((this.clientHeight - 2 * border) * v) / opts.max;
				var width = (v * 93) / opts.max;
				this.children[1].children[1].style.width = width + '%';
				opts.value = v;
			});
		},
		
		valueIAC: function(v) {
			
			if (isNaN(v)) 
				return;
			
			var opts = $(this).data('options');
			
			return this.each(function() {
				if (v < 0)  {
					v = 0;
				}
				if (v > opts.max) {
					//v = opts.max;
				}
				var border = this.children[0].children[2].style.bottom;
				//var height = ((this.clientHeight - 2 * border) * v) / opts.max;
				var width = (v * 89.3) / opts.max;
				this.children[0].children[2].style.width = width + '%';
				opts.value = v;
			});
		},

		destroy : function() {
			return this.each(function() {
				$(this).unbind('repaint');
			})
		}
	};

	$.fn.gaugePV = function(method) {
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(
					arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			$.error('Method ' + method + ' does not exist on jQuery.gauge');
		}

	};

	$.fn.gaugePV.defaults = {
		foreground : 'transparent',
		background : 'transparent',
		color : 'blue',
		lock : true,
		max : 200
	};
})(jQuery);

var scrPV = document.getElementsByTagName('script');
var srcPathGaugePV = scrPV[scrPV.length-1].src; 
var posPV = srcPathGaugePV.lastIndexOf("/");
srcPathGaugePV = srcPathGaugePV.substring(0, posPV + 1);