var Trial = {
	MODULE : "Trial ",
	htmlTariffa : "<div id='TariffaTrial'><div id='TariffaTitolo' class='TrialTitolo'>La tariffa che stai usando</div>" +
				"<div id='TariffaTrialImg'><img id='TariffaTrialBackground' src='Resources/Images/sfondo_tariffa_trial.png'>" +
				"<div id='TariffaImg1'></div><div id='TariffaImg2'></div></div><div id='TariffaLegenda'>" +
				"</div></div>",
	htmlInformazioni : "<div id='Informazioni'><div id='InformazioniTitolo' class='TrialTitolo'>Cosa devi sapere sul trial</div>" +
					"<div id='InformazioniTesto'></div></div>",
	htmlContatti : "<div id='Contatti'><div id='ContattiTitolo' class='TrialTitolo'>Se vuoi contattarci</div><div id='ContattiTel'><span class='ContattiP'>Tel : </span>011 2280000</div>" +
				"<div id='ContattiFax'><span class='ContattiP'>Fax : </span>011 2280001</div><div id='ContattiMail'><span class='ContattiP'>E-mail : </span>trial@telecomitalia.it</div></div>",
	htmlQuestionari : "<div id='Questionari'><div id='QuestionariTitolo' class='TrialTitolo'>Se vuoi contattarci</div>" +
					"<div id='QuestionariRef'>Attualmente non sono presenti questionari</div></div>"

};

Trial.ExitTariffa = function() {
	Log.alert(80, Storico.MODULE, "Trial.ExitTariffa");
}

Trial.GestTariffa = function() {
	Log.alert(80, Storico.MODULE, "Trial.GestTariffa");
	$("#Content").html(Trial.htmlTariffa);
	$("#TariffaImg1").html("<div class='TitoloImgTariffa'>Giorni feriali</div><img class='ImgTariffaTrial' src='Resources/Images/tariffa_feriale.png'>");
	$("#TariffaImg2").html("<div class='TitoloImgTariffa'>Giorni festivi</div><img class='ImgTariffaTrial' src='Resources/Images/tariffa_festivo.png'>");
	//$("#TariffaLegenda").html("<img id='LegendaTrialBackground' src='Resources/Images/sfondo_legenda_trial.png'><div id='LegendaTariffa1'>Tariffa bassa</div><div id='LegendaTariffa2'>Tariffa media</div><div id='LegendaTariffa3'>Tariffa alta</div>");
	$("#TariffaLegenda").html("<img id='ColoreTariffa1'src='Resources/Images/qverde.png'><div id='LegendaTariffa1'>Tariffa bassa (F3)</div>" +
			"<img id='ColoreTariffa2'src='Resources/Images/qgiallo.png'><div id='LegendaTariffa2'>Tariffa media (F2)</div>" +
			"<img id='ColoreTariffa3'src='Resources/Images/qrosso.png'><div id='LegendaTariffa3'>Tariffa alta (F1)</div>");
	$("#ColoreTariffa1").css("height", $("#ColoreTariffa1").css("width")); // rendo quadrato
	$("#ColoreTariffa2").css("height", $("#ColoreTariffa2").css("width")); // rendo quadrato
	$("#ColoreTariffa3").css("height", $("#ColoreTariffa3").css("width")); // rendo quadrato
}

Trial.ExitInformazioni = function() {
	Log.alert(80, Storico.MODULE, "Trial.ExitInformazioni");
}

Trial.GestInformazioni = function() {
	Log.alert(80, Storico.MODULE, "Trial.GestInformazioni");
	$("#Content").html(Trial.htmlInformazioni);
	$("#InformazioniTesto").html("Il trial Energy@Home avra' la durata di un anno circa e ti consentira' di controllare i tuoi consumi elettrici.<br><br>Ti suggeriamo di guardare con attenzione le tariffe in uso e di provare a risparmiare, consumando nelle fasce a minor costo.<br><br>Ogni giorno potrai avere un suggerimento d'uso e nello storico controllare l'andamento dei tuoi consumi e della tua spesa.");
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
