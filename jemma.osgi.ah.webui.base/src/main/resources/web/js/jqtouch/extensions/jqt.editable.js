( function($) {

	if ($.jQTouch) {
		$.jQTouch.addExtension( function Editable(jQT) {
			$.fn.removeTouch = function(){
				this.each(function(i, el){
					$(el).unbind('touchstart touchmove touchend touchcancel', function(){
						//we pass the original event object because the jQuery event
						//object is normalized to w3c specs and does not provide the TouchList
						handleTouch(event);
					});
				});
			}
			
			$.fn.addTouch = function()
			{
				this.each(function(i, el){
					
					$(el).bind('touchstart touchmove touchend touchcancel', function(){
						//we pass the original event object because the jQuery event
						//object is normalized to w3c specs and does not provide the TouchList
						handleTouch(event);
					});
					
				});

				var handleTouch = function(event)
				{
					var touches = event.changedTouches,
					first = touches[0],
					type = '';
					
					switch(event.type)
					{
						case 'touchstart':
							type = 'mousedown';
							break;
							
						case 'touchmove':
							type = 'mousemove';
							break;        
							
						case 'touchend':
							type = 'mouseup';
							break;
							
						default:
							return;
					}
					var simulatedEvent = document.createEvent('MouseEvent');
					simulatedEvent.initMouseEvent(type, true, true, window, 1, first.screenX, first.screenY, first.clientX, first.clientY,false, false, false, false, 0/*left*/, null);               												 
					first.target.dispatchEvent(simulatedEvent);
					
					event.preventDefault();
				};
			};						
			
			
			var editMode = false;
			var deleteDisplayed = false;
			var deletedCallback = null;
			var addedCallback = null;
			var imgMinus, imgPlus, imgMove;
			$(document).ready(function(){
				imgMinus = $('<img class="minus" src="/js/jqtouch/themes/apple/img/minus.png\"></img>');
				imgPlus = $('<img class="plus" src="/js/jqtouch/themes/apple/img/plus.png"></img>');
				imgMove = $('<img class="move" src="/js/jqtouch/themes/apple/img/handle.png"></img>');

				initEditable();
			});
			
			
			
			// visualizza o meno le icone di delete/add a secopnda del valore 
			// della variabile editMode
			function editList() {
				if (editMode) {
					$("ul.editable li img.minus").css("webkitTransform", "rotate(0deg)");
					$("ul.editable li img.minus").css("display", "inline-block");
					$("ul.editable li img.move").css("display", "inline-block");
					$("ul.editable li img.minus").removeClass("deletePriority");
					$("ul.editable li.plus").css("display", "block");
					$(".cont").css("width", "0px");
					$('ul.editable').sortable('enable');
					$('ul.editable li img.move').addTouch();
					
				} else {
					$("ul.editable li img.minus").css("display", "none");
					$("ul.editable li img.move").css("display", "none")
					$("ul.editable li.plus").css("display", "none");
					$(".cont").css("width", "0px");
					$('ul.editable').sortable('disable');
					$('ul.editable li img.move').removeTouch();
				}
			}
				
				
			function initEditable() {
				// aggiunge alle righe il segno meno e il tasto delete
				$("ul.editable li").prepend(imgMinus);
				$("ul.editable li").append(imgMove);
				$('ul.editable').sortable();
				$('ul.editable').sortable('disable');
				$('ul.editable li img.move').addTouch();
				
				$('<span class="cont drag"><span class="test">Delete</span></span>').appendTo($("ul.editable li"));

				$("img.minus").click( function() {
					if (!$(this).is(".deletePriority")) {
						$(this).css("webkitTransform", "rotate(-90deg)");
						$(this).parent().find("img.move").css("display", "none");
						$('ul.editable li img.move').removeTouch();
						$(this).parent().find(".cont").css("width", "60px");
						$(this).addClass("deletePriority");
					} else {
						$(this).css("webkitTransform", "rotate(0deg)");
						$(this).parent().find("img.move").css("display", "inline-block");
						$(this).parent().find("img.move").addTouch();
						$(this).parent().find(".cont").css("width", "0px");
						$(this).removeClass("deletePriority");
					}
				});
				
				$(".cont").click( function(e) {
						res = true;
						
						if (deletedCallback) {
							if ($(this).parent().is('li')) {
								res = deletedCallback($(this).parent().attr('id'));
							}							
						}
						
						if (res) {
							// removes the row!
							if ($(this).parent().is('li')) {
								$(this).parent().remove();
							}
						}
				});
				editMode = false;
			}
			
			// richiamata per aggiungere elemento 'add' in  testa alla lista
			function setAdd(addHref, labelAdd) {	
				// se ho passato il parametro setto div a cui puntare per add
				if ((addHref != null) && (addHref != undefined))
					plusLine = '<li class="arrow plus"><a href="' + addHref 
					+ '">' + labelAdd + '</a></li>';
				else
					plusLine = '<li class="arrow plus"><a>'  
					+ labelAdd + '</a></li>';
				$("ul.editable").prepend(plusLine);
				$("ul.editable li.plus a").prepend(imgPlus);
				$("ul.editable li.plus").css("display", "none");
				$(".plus").click( function() {
					if ($(this).parent().is('ul')) {
						if (addedCallback) {
							addedCallback($(this).parent().attr('id'));
						}
					}
				});

			}
			
			function setListener(l) {
				listener = l;
			}
			
			function getEditMode() {
				return editMode;
			}
			
			function setEditMode(value) {
				if ((value == true) || (value == false))
					editMode = value;
			}
			
			function setDeletedCallback(callback) {
				deletedCallback = callback;
			}
			
			function setAddedCallback(callback) {
				addedCallback = callback;
			}

			return {
				initEditable: initEditable,
				editList: editList,
				setAdd: setAdd,
				setListener : setListener,
				getEditMode: getEditMode,
				setEditMode: setEditMode,
				setDeletedCallback: setDeletedCallback,
				setAddedCallback: setAddedCallback
			}
		});
	}
})(jQuery);
		


