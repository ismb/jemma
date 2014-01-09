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
		"<div id='InformazioniTestoTitolo'>" + Msg.trial["titoloTestoInfo"] + "</div><div id='InformazioniTesto'>" + Msg.trial["testoInfo"] + "</div></div>",
	
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
	Log.alert(80, Storico.MODULE, "Trial.ExitTariffa");
}

Trial.GestTariffa = function() {
	Log.alert(80, Storico.MODULE, "Trial.GestTariffa");
	$("#Content").html(Trial.htmlTariffa);
}

Trial.ExitInformazioni = function() {
	Log.alert(80, Storico.MODULE, "Trial.ExitInformazioni");
}

Trial.GestInformazioni = function() {
	Log.alert(80, Storico.MODULE, "Trial.GestInformazioni");
	$("#Content").html(Trial.htmlInformazioni);
}

Trial.ExitContatti = function() {
	Log.alert(80, Storico.MODULE, "Trial.ExitContatti");
}

Trial.GestContatti = function() {
	Log.alert(80, Storico.MODULE, "Trial.GestContatti");
	$("#Content").html(Trial.htmlContatti);
}

Trial.ExitQuestionari = function() {
	Log.alert(80, Storico.MODULE, "Trial.ExitQuestionari");
}

Trial.GestQuestionari = function() {
	Log.alert(80, Storico.MODULE, "Trial.GestQuestionari");
	$("#Content").html(Trial.htmlQuestionari);
}
