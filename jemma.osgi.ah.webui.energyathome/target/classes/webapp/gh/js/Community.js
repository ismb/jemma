var Community = {
	MODULE : "Community ",
	htmlContent : "<div id='ForumTitolo' class='CommunityTitolo' >" + Msg.community["titoloForum"] + "</div>" +
			"<div id='Forum'>" + Msg.community["forumMsg"] + "</div>" +
			"<a id='ForumHref' target='_blank' href='" + Define.community["forumLink"] +
			"'>" + Msg.community["forumText"]+ "</a>"

};

Community.ExitForum = function() {
	Log.alert(80, Storico.MODULE, "Community.ExitForum");

}


Community.GestForum = function() {
	Log.alert(80, Storico.MODULE, "Community.GestForum");
	$("#Content").html(Community.htmlContent);
}

