function showSpinner() {
	if ($('#progress').length == 0) { 
		$('body').append($(document.createElement('div'))
								.attr('id', 'progress')
								.attr('class', 'boxProgress')
								.append($(document.createElement('img'))
											.attr('src', './js/other/ajax-loader.gif')));
	}
	
	$('#progress').show();
}

function showSpinnerConf() {
	if ($('#progress').length == 0) { 
		$('body').append('<div id="progress" class="boxProgress" ><img src="../js/other/ajax-loader.gif"></div>');
	}
	
	$('#progress').show();
}

function showSpinnerElettro() { 
	if ($('#progressElettro').length == 0) { 
		$('body').append($(document.createElement('div'))
								.attr('id', 'progressElettro')
								.attr('class', 'boxProgressElettro')
								.append($(document.createElement('img'))
											.attr('src', './js/other/ajax-loader.gif')));
	}
	
	$('#progressElettro').show();
}

function hideSpinner() {
	$('#progress').hide();
}

function hideSpinnerElettro() {
	$('#progressElettro').hide();
}

function updateField(id, value) {
   	if ($(id).text() != value) {
       	$(id).text(value);
   	}
}   

// fa blinking del tag li padre (fino a 2 livelli sopra) dell'item
// es. "li span" o "li a span" 
function updateFieldBlinking(id, value) {
	var t;
	var el;
	
   	if ($(id).text() != value) {
   		el = $(id).parent();
   		if (!($(el).is("li")))
   			el = $(el).parent(); 
       	$(id).text(value);
        $(el).addClass('blinking');
      	t = setTimeout(function (){$(el).removeClass('blinking'); }, 600);
   	}
}