var NonDisponibile = {
	MODULE : "NonDisponibile ",
	htmlContent : "<div id='NonDisponibileContainer' class='Content'><div id='NonDisponibile'>" + Msg.home["nonDisponibile"] + "</div></div>"

};

NonDisponibile.ExitND = function() {
	Log.alert(80, NonDisponibile.MODULE, "NonDisponibile.ExitND");
	$("#NonDisponibileContainer").hide();

}


NonDisponibile.GestND = function() {
	Log.alert(80, NonDisponibile.MODULE, "NonDisponibile.GestND");
	var divNonDisp = $("#NonDisponibileContainer");
	if (divNonDisp.length == 0){
	$("#Container").append(NonDisponibile .htmlContent);}
	$("#NonDisponibileContainer").show();
}

