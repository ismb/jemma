var Trial = {
	MODULE : "Trial ",
	htmlTariffa : "<div id='TariffaTitolo' class='TrialTitolo' >" + Msg.trial["titoloTariffa"] + "</div>" +
		"<div id='TariffaTrial'><img id='TariffaTrialBackground' src='" + Define.trial["sfondoTrial"] + "'>" +
		"<div id='TariffaImg1'><div class='TitoloImgTariffa'>" + Msg.trial["tariffaFeriale"] + "</div>" +
		"<img class='ImgTariffaTrial' src='" + Define.trial["tariffaFeriale"] + "'><div class='TariffaOre'>" + Msg.trial["tariffaOra"] + "</div></div>" +
		"<div id='TariffaImg2'><div class='TitoloImgTariffa'>" + Msg.trial["tariffaFestiva"] + "</div>" +
		"<img class='ImgTariffaTrial' src='" + Define.trial["tariffaFestiva"] + "'><div class='TariffaOre'>" + Msg.trial["tariffaOra"] + "</div></div>" +
		"<div id='TariffaLegenda'><img id='ColoreTariffa1'src='" + Define.trial["tariffaQVerde"] + "'>" +
		"<div id='LegendaTariffa1'>" + Msg.trial["tariffaBassa"] + "</div>" +
		"<!--img id='ColoreTariffa2'src='" + Define.trial["tariffaQGiallo"] + "'>" +
		"<div id='LegendaTariffa2'>" + Msg.trial["tariffaMedia"] + "</div-->" +
		"<img id='ColoreTariffa3'src='" + Define.trial["tariffaQRosso"] + "'>" +
		"<div id='LegendaTariffa3'>" + Msg.trial["tariffaAlta"] + "</div></div></div></div>",
				
	htmlInformazioni : "<div id='InformazioniTitolo' class='TrialTitolo'>" + Msg.trial["titoloInfo"] + "</div><div id='Informazioni'>" +
		"<img id='InfoTrialBackground' src='" + Define.trial["sfondoTrial"] + "'>" +
		"<div id='InformazioniTestoTitolo' style='font-size:1vw;'><a target='_blank' href='http://www.energy-home.it/SitePages/Activities/Trial.aspx'>" + Msg.trial["titoloTestoInfo"] + "</a>"+
		"<br/><br/><div id='MembersEH' style='position: absolute;'>"+Msg.trial["membriEH"]+"</div></div>"+
		"<img id='InformazioniTesto' align='middle' src='" + Define.trial["imgAssociati"] + "'/></div>",
	
	htmlContatti :	"<div id='ContattiTitolo' class='TrialTitolo'>" + Msg.trial["titoloContatti"] + "</div><div id='Contatti'>" +
		"<img id='ContattiTrialBackground' src='" + Define.trial["sfondoTrial"] + "'>" +
		"<div id='ContattiTestoTitolo'>" + Msg.trial["titoloTestoContatti"] + "</div>" +
		"<div id='ContattiTel'><span class='ContattiP'>Tel : </span>" + Msg.trial["telNum"] + "</div>" +
		"<!--div id='ContattiFax'><span class='ContattiP'>Fax : </span>" + Msg.trial["faxNum"] + "</div-->" +
		"<div id='ContattiMail'><span class='ContattiP'>E-mail : </span>" + Msg.trial["email"] + "</div></div>",
	
	htmlQuestionari : "<div id='QuestionariTitolo' class='TrialTitolo'>" + Msg.trial["titoloQuestionari"] + "</div><div id='Questionari'>" +
		"<div id='QuestionariRef'>" + Msg.trial["noQuestionari"] + "</div></div>"

};

Trial.ExitTariffa = function() {
	//Log.alert(80, Storico.MODULE, "Trial.ExitTariffa");
	$("#Tariffa").hide();
}

Trial.GestTariffa = function() {
	//Log.alert(80, Storico.MODULE, "Trial.GestTariffa");
	/* Creo il contenitore dei dati di report per la Gui e lo aggiungo alla pagina */
	var divTariffa = $("#Tariffa");
	
    /* Controllo che il div di Report non sia gi� stato riempito. 
     * Se non esiste lo inizializzo, se gi� esiste lo visualizzo solamente */
	
	if (divTariffa.length == 0){
	
	$("#Container").append('<div id="Tariffa" class="Content"></div>');
	$("#Tariffa").html(Trial.htmlTariffa);
	
}
	
	$("#Tariffa").show();
	
}

Trial.ExitInformazioni = function() {
	//Log.alert(80, Storico.MODULE, "Trial.ExitInformazioni");
	$("#Info").hide();
}

Trial.GestInformazioni = function() {
	//Log.alert(80, Storico.MODULE, "Trial.GestInformazioni");
	
	/* Creo il contenitore dei dati di report per la Gui e lo aggiungo alla pagina */
	var divInfo = $("#Info");
	
    /* Controllo che il div di Report non sia gi� stato riempito. 
     * Se non esiste lo inizializzo, se gi� esiste lo visualizzo solamente */
	
	if (divInfo.length == 0){
	
	$("#Container").append('<div id="Info" class="Content"></div>');
	$("#Info").html(Trial.htmlInformazioni);
	
}
	
	$("#Info").show();
}

Trial.ExitContatti = function() {
	//Log.alert(80, Storico.MODULE, "Trial.ExitContatti");
	$("#Contat").hide();
}

Trial.GestContatti = function() {
	//Log.alert(80, Storico.MODULE, "Trial.GestContatti");
	
	/* Creo il contenitore dei dati di report per la Gui e lo aggiungo alla pagina */
	var divContatti = $("#Contat");
	
    /* Controllo che il div di Report non sia gi� stato riempito. 
     * Se non esiste lo inizializzo, se gi� esiste lo visualizzo solamente */
	
	if (divContatti.length == 0){
	
	$("#Container").append('<div id="Contat" class="Content"></div>');
	$("#Contat").html(Trial.htmlContatti);
	
}
	
	$("#Contat").show();
	
}

Trial.ExitQuestionari = function() {
	//Log.alert(80, Storico.MODULE, "Trial.ExitQuestionari");
	
	$("#Questionari").hide();
}

Trial.GestQuestionari = function() {
	//Log.alert(80, Storico.MODULE, "Trial.GestQuestionari");
	
	/* Creo il contenitore dei dati di report per la Gui e lo aggiungo alla pagina */
	var divQuestionari = $("#Questionari");
	
    /* Controllo che il div di Report non sia gi� stato riempito. 
     * Se non esiste lo inizializzo, se gi� esiste lo visualizzo solamente */
	
	if (divQuestionari.length == 0){
	
	$("#Container").append('<div id="Questionari" class="Content"></div>');
	$("#Questionari").html(Trial.htmlQuestionari);
	
}
	
	$("#Questionari").show();
	
}
