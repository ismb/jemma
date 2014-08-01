var Community = {
	MODULE : "Community ",
	htmlContent : "<div id='ForumTitolo' class='CommunityTitolo' >" + Msg.community["titoloForum"] + "</div>" +
			"<div id='Forum'>" + Msg.community["forumMsg"] + "</div>" +
			"<a id='ForumHref' target='_blank' href='" + Define.community["forumLink"] +
			"'>" + Msg.community["forumText"]+ "</a>"

};

Community.ExitForum = function() {
	//Log.alert(80, Storico.MODULE, "Community.ExitForum");
	$("#Community").hide();

}


Community.GestForum = function() {
	//Log.alert(80, Storico.MODULE, "Community.GestForum");
	/* Creo il contenitore dei dati di report per la Gui e lo aggiungo alla pagina */
	var divCommunity = $("#Community");
	
    /* Controllo che il div di Report non sia gi� stato riempito. 
     * Se non esiste lo inizializzo, se gi� esiste lo visualizzo solamente */
	
	if (divCommunity.length == 0){
	
	$("#Container").append('<div id="Community" class="Content"></div>');
	$("#Community").html(Community.htmlContent);
	
}
	$("#Community").show();
}

