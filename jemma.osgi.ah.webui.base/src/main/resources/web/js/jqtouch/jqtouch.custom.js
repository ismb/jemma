function showSpinner() {
	if ($('#progress').length == 0) {
		$('body').append('<div id="progress">Attendere...</div>');
	}
	$('#progress').show();
}

function hideSpinner() {
	$('#progress').hide();
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

function convertToKWh(value) {
	return (value / 1000);
}

var NOT_AVAILABLE_STRING = "nd";
var NOT_ASSIGNED = "Non assegnato";
