var NonDisponibile = {
	MODULE : "NonDisponibile ",
	htmlContent : "<div id='NonDisponibile'>Funzione non ancora disponibile</div>"

};

NonDisponibile.ExitND = function() {
	Log.alert(80, Storico.MODULE, "NonDisponibile.ExitND");

}


NonDisponibile.GestND = function() {
	Log.alert(80, Storico.MODULE, "NonDisponibile.GestND");
	$("#Content").html(NonDisponibile .htmlContent);
}

