(function(){
  
	Renderer = function(canvas){
		  var canvas = $(canvas).get(0)
		  var ctx = canvas.getContext("2d");
		  var gfx = arbor.Graphics(canvas)
		  var particleSystem = null

		  var _vignette = null
		  var selected = null, nearest = null, _mouseP = null;
	
		  var that = {
			  init:function(system){
				  particleSystem = system
				  particleSystem.screenSize(canvas.width, canvas.height) 
				  particleSystem.screenPadding(80)
	
				  that.initMouseHandling()
			  },
	
			  redraw:function(){
				  if (!particleSystem) return
	
				  gfx.clear() // convenience ƒ: clears the whole canvas rect
	
				  // draw the nodes & save their bounds for edge drawing
				  var nodeBoxes = {}
				  particleSystem.eachNode(function(node, pt){
					  // node: {mass:#, p:{x,y}, name:"", data:{}}
					  // pt: {x:#, y:#} node position in screen coords
	
					  // determine the box size and round off the coords if we'll be
					  // drawing a text label (awful alignment jitter otherwise...)
					  //var label = (node.data.label0 + node.data.label1 + node.data.label2) || ''
					  var label = node.data.label || '';
					  var w = ctx.measureText(label).width + 10
					  if (!(""+label).match(/^[ \t]*$/)){
						  pt.x = Math.floor(pt.x)
						  pt.y = Math.floor(pt.y)
					  }else{
						  label = null
					  }
	
					  // draw a rectangle centered at pt
					  if (node.data.color) ctx.fillStyle = node.data.color
					  else ctx.fillStyle = "rgba(0,0,0,.2)"
						  
					  if (node.data.color=='none') ctx.fillStyle = "white"

			          if (node.data.alpha===0) return
					  if (node.data.shape=='dot'){
						  gfx.oval(pt.x-w/2, pt.y-w/2, w,w, {fill:ctx.fillStyle})
						  nodeBoxes[node.name] = [pt.x-w/2, pt.y-w/2, w,w]
					  }else{
						  gfx.rect(pt.x-w/2, pt.y-10, w,20, 4, {fill:ctx.fillStyle})
						  nodeBoxes[node.name] = [pt.x-w/2, pt.y-11, w, 22]
					  }
	
					  // draw the text
					  if (label){
						  ctx.font = "12px Helvetica"
						  ctx.textAlign = "center"
						  ctx.fillStyle = "white"
						  if (node.data.color=='none') ctx.fillStyle = '#333333'
						  ctx.fillText(label||"", pt.x, pt.y+4)
						  ctx.fillText(label||"", pt.x, pt.y+4)
					  }
				  })    			
	
	
				  // draw the edges
				  particleSystem.eachEdge(function(edge, pt1, pt2){
					  // edge: {source:Node, target:Node, length:#, data:{}}
					  // pt1: {x:#, y:#} source position in screen coords
					  // pt2: {x:#, y:#} target position in screen coords
	
					  var weight = edge.data.weight;
					  var color = edge.data.color;
					  var lgth = edge.data.lgth;
					  
					  if (!color || (""+color).match(/^[ \t]*$/)) color = null
	
					  // find the start point
					  var tail = intersect_line_box(pt1, pt2, nodeBoxes[edge.source.name])
					  var head = intersect_line_box(tail, pt2, nodeBoxes[edge.target.name])
	
					  ctx.save() 
					  	  ctx.beginPath()
					  	  ctx.lineWidth = (!isNaN(weight)) ? parseFloat(weight) : 1
			  			  ctx.strokeStyle = (color) ? color : "#cccccc"
		  				  ctx.fillStyle = null
	
		  				  ctx.moveTo(tail.x, tail.y)
		  				  ctx.lineTo(head.x, head.y)
		  				  ctx.stroke()
	  				  ctx.restore()
	
	  				  // draw an arrowhead if this is a -> style edge
	  				  if (edge.data.directed){
	  					  ctx.save()
	  					  	  // move to the head position of the edge we just drew
	  					      var wt = !isNaN(weight) ? parseFloat(weight) : 1
				    		  var arrowLength = 6 + wt
				    		  var arrowWidth = 2 + wt
				    		  ctx.fillStyle = (color) ? color : "#cccccc"
			    			  ctx.translate(head.x, head.y);
	  					      ctx.rotate(Math.atan2(head.y - tail.y, head.x - tail.x));
	
	  					      // delete some of the edge that's already there (so the point isn't hidden)
	  					      ctx.clearRect(-arrowLength/2,-wt/2, arrowLength/2,wt)
	
	  					      // draw the chevron
	  					      ctx.beginPath();
	  					      ctx.moveTo(-arrowLength, arrowWidth);
	  					      ctx.lineTo(0, 0);
	  					      ctx.lineTo(-arrowLength, -arrowWidth);
	  					      ctx.lineTo(-arrowLength * 0.8, -0);
	  					      ctx.closePath();
	  					      ctx.fill();
					      ctx.restore()
	  				  }
				  })
				  that._drawVignette()
		      },
		      
		      _drawVignette:function(){
		        var w = canvas.width
		        var h = canvas.height
		        var r = 20

		        if (!_vignette){
		          var top = ctx.createLinearGradient(0,0,0,r)
		          top.addColorStop(0, "#e0e0e0")
		          top.addColorStop(.7, "rgba(255,255,255,0)")

		          var bot = ctx.createLinearGradient(0,h-r,0,h)
		          bot.addColorStop(0, "rgba(255,255,255,0)")
		          bot.addColorStop(1, "white")

		          _vignette = {top:top, bot:bot}
		        }
		        
		        // top
		        ctx.fillStyle = _vignette.top
		        ctx.fillRect(0,0, w,r)

		        // bot
		        ctx.fillStyle = _vignette.bot
		        ctx.fillRect(0,h-r, w,r)
		      },

		      switchMode:function(e){
		        if (e.mode=='hidden'){
		          dom.stop(true).fadeTo(e.dt,0, function(){
		            if (sys) sys.stop()
		            $(this).hide()
		          })
		        }else if (e.mode=='visible'){
		          dom.stop(true).css('opacity',0).show().fadeTo(e.dt,1,function(){
		            that.resize()
		          })
		          if (sys) sys.start()
		        }
		      },
		      
		      switchSection:function(newSection){
		        var parent = sys.getEdgesFrom(newSection)[0].source
		        var children = $.map(sys.getEdgesFrom(newSection), function(edge){
		          return edge.target
		        })
		        
		        sys.eachNode(function(node){
		          if (node.data.shape=='dot') return // skip all but leafnodes

		          var nowVisible = ($.inArray(node, children)>=0)
		          var newAlpha = (nowVisible) ? 1 : 0
		          var dt = (nowVisible) ? .5 : .5
		          sys.tweenNode(node, dt, {alpha:newAlpha})

		          if (newAlpha==1){
		            node.p.x = parent.p.x + .05*Math.random() - .025
		            node.p.y = parent.p.y + .05*Math.random() - .025
		            node.tempMass = .001
		          }
		        })
		      },
		      
		      initMouseHandling:function(){
		          // no-nonsense drag and drop (thanks springy.js)
		        	selected = null;
		        	nearest = null;
		        	var dragged = null;
		          var oldmass = 1

		          $(canvas).mousedown(function(e){
		        		var pos = $(this).offset();
		        		var p = {x:e.pageX-pos.left, y:e.pageY-pos.top}
		        		selected = nearest = dragged = particleSystem.nearest(p);

		        		if (selected.node !== null){
		              dragged.node.tempMass = 50
		              dragged.node.fixed = true
		        		}
		        		return false
		        	});

		        	$(canvas).mousemove(function(e){
		            var old_nearest = nearest && nearest.node._id
		        		var pos = $(this).offset();
		        		var s = {x:e.pageX-pos.left, y:e.pageY-pos.top};

		        		nearest = particleSystem.nearest(s);
		            if (!nearest) return

		        		if (dragged !== null && dragged.node !== null){
		              var p = particleSystem.fromScreen(s)
		        			dragged.node.p = {x:p.x, y:p.y}
		              // dragged.tempMass = 10000
		        		}

		            return false
		        	});

		        	$(window).bind('mouseup',function(e){
		            if (dragged===null || dragged.node===undefined) return
		            dragged.node.fixed = false
		            dragged.node.tempMass = 100
		        		dragged = null;
		        		selected = null
		        		return false
		        	});
		        	      
		        }
		  }
	
		  // helpers for figuring out where to draw arrows (thanks springy.js)
		  var intersect_line_line = function(p1, p2, p3, p4){
			  var denom = ((p4.y - p3.y)*(p2.x - p1.x) - (p4.x - p3.x)*(p2.y - p1.y));
			  if (denom === 0) return false // lines are parallel
			  var ua = ((p4.x - p3.x)*(p1.y - p3.y) - (p4.y - p3.y)*(p1.x - p3.x)) / denom;
			  var ub = ((p2.x - p1.x)*(p1.y - p3.y) - (p2.y - p1.y)*(p1.x - p3.x)) / denom;
	
			  if (ua < 0 || ua > 1 || ub < 0 || ub > 1)  return false
			  return arbor.Point(p1.x + ua * (p2.x - p1.x), p1.y + ua * (p2.y - p1.y));
		  }
	
		  var intersect_line_box = function(p1, p2, boxTuple){
			  var p3 = {x:boxTuple[0], y:boxTuple[1]},
	          	  w = boxTuple[2],
	          	  h = boxTuple[3]
	
			  var tl = {x: p3.x, y: p3.y};
			  var tr = {x: p3.x + w, y: p3.y};
			  var bl = {x: p3.x, y: p3.y + h};
			  var br = {x: p3.x + w, y: p3.y + h};
	
			  return intersect_line_line(p1, p2, tl, tr) ||
	            	 intersect_line_line(p1, p2, tr, br) ||
	            	 intersect_line_line(p1, p2, br, bl) ||
	            	 intersect_line_line(p1, p2, bl, tl) ||
	            	 false
		  }
	
		  return that
	}
	
	 var Nav = function(elt){
		 var dom = $(elt);
		 var _path = null
		 var that = {
			 init:function(){
				 $(window).bind('popstate',that.navigate)
				 dom.find('> a').click(that.back)
				 $('.more').one('click',that.more)
	        
				 $('#docs dl:not(.datastructure) dt').click(that.reveal)
				 that.update()
				 return that
			 },
			 more:function(e){
				 $(this).removeAttr('href').addClass('less').html('&nbsp;').siblings().fadeIn()
		 		 $(this).next('h2').find('a').one('click', that.less)
	        
			 	 return false
	     	},
	     	less:function(e){
	     		var more = $(this).closest('h2').prev('a')
	     		$(this).closest('h2').prev('a').nextAll().fadeOut(function(){
	     			$(more).text('creation & use').removeClass('less').attr('href','#')
	     		})
	     		$(this).closest('h2').prev('a').one('click',that.more)
	        
	     		return false
	     	},
	     	reveal:function(e){
	     		$(this).next('dd').fadeToggle('fast')
	     		return false
	     	},
	     	back:function(){
	     		_path = "/"
     			if (window.history && window.history.pushState){
     				window.history.pushState({path:_path}, "", _path);
     			}
	     		that.update()
	     		return false
	     	},
	     	navigate:function(e){
	     		var oldpath = _path
	     		if (e.type=='navigate'){
	     			_path = e.path
	     			if (window.history && window.history.pushState){
	     				window.history.pushState({path:_path}, "", _path);
	     			}else{
	     				that.update()
	     			}
	     		}else if (e.type=='popstate'){
	     			var state = e.originalEvent.state || {}
	     			_path = state.path || window.location.pathname.replace(/^\//,'')
	     		}
	     		if (_path != oldpath) that.update()
	     	},
	     	update:function(){
	     		var dt = 'fast'
     			if (_path===null){
     				// this is the original page load. don't animate anything just jump
     				// to the proper state
     				_path = window.location.pathname.replace(/^\//,'')
     				dt = 0
     				dom.find('p').css('opacity',0).show().fadeTo('slow',1)
     			}

	     		switch (_path){
	     			case '':
	     			case '/':
	     				dom.find('p').text('a graph visualization library using web workers and jQuery')
	     				dom.find('> a').removeClass('active').attr('href','#')

	     				$('#docs').fadeTo('fast',0, function(){
	     					$(this).hide()
	     					$(that).trigger({type:'mode', mode:'visible', dt:dt})
	     				})
	     				document.title = "arbor.js"
     					break
	          
	     			case 'introduction':
	     			case 'reference':
	     				$(that).trigger({type:'mode', mode:'hidden', dt:dt})
	     				dom.find('> p').text(_path)
	     				dom.find('> a').addClass('active').attr('href','#')
	     				$('#docs').stop(true).css({opacity:0}).show().delay(333).fadeTo('fast',1)
	                    
	     				$('#docs').find(">div").hide()
	     				$('#docs').find('#'+_path).show()
	     				document.title = "arbor.js » " + _path
	     				break
	     		}
	        
	     	}
	    }
	    return that
	  }
})()