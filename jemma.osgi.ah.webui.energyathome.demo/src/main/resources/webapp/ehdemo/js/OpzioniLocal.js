var OpzioniLocal = {
	MODULE : "OpzioniLocal ",
	limitPowerProduction: null

};

OpzioniLocal.ExitOpzioniLocal = function() {
	//Log.alert(80, Storico.MODULE, "Community.ExitForum");
	$("#OpzioniLocal").hide();

}


OpzioniLocal.GestOpzioniLocal = function() {
	//Log.alert(80, Storico.MODULE, "Community.GestForum");
	/* Creo il contenitore dei dati di report per la Gui e lo aggiungo alla pagina */
	var divOpzioniLocal = $("#OpzioniLocal");
	
    /* Controllo che il div di Report non sia gi� stato riempito. 
     * Se non esiste lo inizializzo, se gi� esiste lo visualizzo solamente */
	
	if (divOpzioniLocal.length == 0){
		var opt0 = "<option value='-1'>0 kW</option>";
		var opt1 = "<option value='0'>1 kW</option>";
		var opt2 = "<option value='1'>2 kW</option>";
		var opt3 = "<option value='2'>3 kW</option>";
		var opt4 = "<option value='3'>4 kW</option>";
		var opt5 = "<option value='4'>5 kW</option>";
		var opt6 = "<option value='5'>6 kW</option>";
		if (Main.contatoreProd == null){
			opt0 = "<option value='-1' selected='selected'>0 kW</option>";
		}
		if (Main.contatoreProd == 0){
			opt1 = "<option value='0' selected='selected'>1 kW</option>";
		}
		if (Main.contatoreProd == 1){
			opt2 = "<option value='1' selected='selected'>2 kW</option>";
		}
		if (Main.contatoreProd == 2){
			opt3 = "<option value='2' selected='selected'>3 kW</option>";
		}
		if (Main.contatoreProd == 3){
			opt4 = "<option value='3' selected='selected'>4 kW</option>";
		}
		if (Main.contatoreProd == 4){
			opt5 = "<option value='4' selected='selected'>5 kW</option>";
		}
		if (Main.contatoreProd == 5){
			opt6 = "<option value='5' selected='selected'>6 kW</option>";
		}
		var htmlContent = "<div id='ForumTitolo' class='CommunityTitolo' >" + Msg.opzLocal["titoloForum"] + "</div>"
				 		+ "<div id='Forum'>" + Msg.opzLocal["forumMsg"] + "</div>"
						+ "<div id='DatiUtente' class='Form'>"
						+ "		<div>"
						+ "			<label>" + Msg.config["maxProduzione"] + ": </label>"
						+ "			<select id='MaxProduction'>"
										+ "" + opt0 + "" + 		
										+ "" + opt1 + "" + 		
										+ "" + opt2 + "" + 		
										+ "" + opt3 + "" + 		
										+ "" + opt4 + "" + 	
										+ "" + opt5 + "" + 	
										+ "" + opt6 + "" +  			
						+ "			</select>"
						+ "			<div style='clear:both'></div>"
						+ "		</div>"
						+ "</div>"
						+ "<input type='button' class='ButtonConf' id='ButtonSalvaUtente' name='SalvaUtente' value='" + Msg.config["Salva"] + "'>"
						+ "<input type='reset' class='ButtonConf' id='ButtonAnnullaUtente' name='AnnullaUtente' value='" + Msg.config["Annulla"] + "'>";
	
		$("#Container").append('<div id="OpzioniLocal" class="Content"></div>');
		$("#OpzioniLocal").html(htmlContent);
		
	}
	$("#OpzioniLocal").show();

	$("#NomeUtente").val(Main.userId);
	$("#ButtonSalvaUtente").click(OpzioniLocal.SalvaConfigLocal);
}

OpzioniLocal.setAttributeCb = function(result, e) {
	if (e != null) {
		//KO!
		Utils.scriviCookie('MaxPowerProduction', 0, MIN_YEAR);
	} else {
		//ok registro i cookie
		Utils.scriviCookie('MaxPowerProduction', OpzioniLocal.limitPowerProduction, MIN_YEAR);
	}

	hideSpinner();
	TINY.box.show({html:"<p class='titoloInfoBox'>LA PAGINA VERRA' RICARITA TRA 5 SECONDI<br>Altrimenti fai click col mouse.</p>",
		   animate:true,
		   close:true,
		   width: 305,
		   height: 50,
		   close:false,
		   autohide:5,
		   closejs:function(){
			   location.reload();
		   }});
}

OpzioniLocal.SalvaConfigLocal = function() {
	OpzioniLocal.limitPowerProduction = $("#MaxProduction").val();

	showSpinner();
	if (InterfaceEnergyHome.mode > 0) {
		//InterfaceEnergyHome.objService.setAttribute(OpzioniLocal.setAttributeCb, "InstantaneousPowerProductionLimit", OpzioniLocal.limitPowerProduction);
		//InterfaceEnergyHome.setAttribute(OpzioniLocal.setAttributeCb, "InstantaneousPowerProductionLimit", OpzioniLocal.limitPowerProduction);
		OpzioniLocal.setAttributeCb(null, null);
	} else {
		OpzioniLocal.setAttributeCb(null, null);
	}
}