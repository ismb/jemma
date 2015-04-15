var NonDisponibile = {
	MODULE : "NonDisponibile ",
	htmlContentND : "<div id='NonDisponibileContainer' class='Content'><div id='NonDisponibile'>" + Msg.home["nonDisponibile"] + "</div></div>",
	htmlContentNDUser : "<div id='NonDisponibileContainer' class='Content'><div id='NonDisponibile'>" + Msg.home["nonDisponibileUtente"] + "</div></div>"

};

NonDisponibile.ExitND = function() {
	Log.alert(80, NonDisponibile.MODULE, "NonDisponibile.ExitND");
	$("#NonDisponibileContainer").hide();

}


NonDisponibile.GestND = function() {
	Log.alert(80, NonDisponibile.MODULE, "NonDisponibile.GestND");
	var divNonDisp = $("#NonDisponibileContainer");
	if (divNonDisp.length == 0){
	$("#Container").append(NonDisponibile .htmlContentND);}
	$("#NonDisponibileContainer").show();
}

NonDisponibile.GestNDUser = function() {
	Log.alert(80, NonDisponibile.MODULE, "NonDisponibile.GestNDUser");
	var divNonDisp = $("#NonDisponibileContainer");
	if (divNonDisp.length == 0){
	$("#Container").append(NonDisponibile .htmlContentNDUser );}
	$("#NonDisponibileContainer").show();
}


