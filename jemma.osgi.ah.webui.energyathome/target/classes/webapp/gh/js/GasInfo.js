var GasInfo = {
	MODULE : "GasInfo ",
	htmlTariffa : "<div id='TariffaTitolo' class='InfoTitolo' >" + Msg.trial["titoloTariffa"] + "</div>" +
		"<div id='InfoTariffa'><img id='InfoTariffaBackground' src='" + GasDefine.trial["sfondoTrial"] + "'>" +
		
		"<div id='TariffaImg1'><div class='TitoloImgTariffa'>" + Msg.trial["tariffaFeriale"] + "</div>" +
		"<img class='ImgTariffaInfo' src='" + GasDefine.trial["tariffaFeriale"] + "'><div class='TariffaOre'>" + Msg.trial["tariffaOra"] + "</div></div>" +

		"<div id='TariffaImg3'><div class='TitoloImgTariffa'>" + Msg.trial["tariffaSabato"] + "</div>" +
		"<img class='ImgTariffaInfo' src='" + GasDefine.trial["tariffaSabato"] + "'><div class='TariffaOre'>" + Msg.trial["tariffaOra"] + "</div></div>" +

		"<div id='TariffaImg2'><div class='TitoloImgTariffa'>" + Msg.trial["tariffaFestiva"] + "</div>" +
		"<img class='ImgTariffaInfo' src='" + GasDefine.trial["tariffaFestiva"] + "'><div class='TariffaOre'>" + Msg.trial["tariffaOra"] + "</div></div>" +

		"<div id='TariffaLegenda'><img id='ColoreTariffa1'src='" + GasDefine.trial["tariffaQVerde"] + "'>" +
		"<div id='LegendaTariffa1'>" + Msg.trial["tariffaBassa"] + "</div>" +
		"<img id='ColoreTariffa2'src='" + GasDefine.trial["tariffaQGiallo"] + "'>" +
		"<div id='LegendaTariffa2'>" + Msg.trial["tariffaMedia"] + "</div>" +
		"<img id='ColoreTariffa3'src='" + GasDefine.trial["tariffaQRosso"] + "'>" +
		"<div id='LegendaTariffa3'>" + Msg.trial["tariffaAlta"] + "</div></div></div></div>",
				
	htmlInformazioni : "<div id='InformazioniTitolo' class='InfoTitolo'>" + Msg.trial["titoloInfo"] + "</div><div id='Informazioni'>" +
		"<img id='InfoServizioBackground' src='" + GasDefine.trial["sfondoTrial"] + "'>" +
		"<div id='InformazioniTestoTitolo'>" + Msg.trial["titoloTestoInfo"] + "</div>" + 
			"<div id='InformazioniTesto'>" + 
				"<div id='InformazioniTestoRigaUno'>" +
					"<div id='InformazioniTestoColUno'>" +
						"<div id='InformazioniTestoColUnoLeft'>" + "<br /><br />" +
							Msg.trial["codCliente"] + ":<br /><br />" + 
							Msg.trial["codPDR"] + ":<br /><br />" + 
							Msg.trial["tipoContatore"] + ":<br /><br />" + 
							Msg.trial["numContatore"] + ":<br /><br />" + 
						"</div>" + 
						"<div id='InformazioniTestoColUnoRight'>" + "<br /><br />" +
							codiceCliente + "<br /><br />" +
							codicePDR + "<br /><br />" +
							tipoMeter + "<br /><br />" +
							numMeter + "<br /><br />" + 
						"</div>" +
					"</div>" + 
					"<div id='InformazioniTestoColDue'>" +
						"<div id='InformazioniTestoColDueLeft'>" + "<br /><br />" +
							Msg.trial["infoTotaleFornitura"] + ":<br /><br />" + 
							Msg.trial["dataAttivazione"] + ":<br /><br />" + 
							Msg.trial["consumoAnnuo"] + ":<br /><br />" + 
						"</div>" + 
						"<div id='InformazioniTestoColDueRight'>" + "<br /><br />" +
							tipoContr + "<br /><br />" +
							dataAtt + "<br /><br />" +
							consAnnuo + " smc<br /><br />" + 
						"</div>" +
					"</div>" + 
				"</div>" + 
				"<div id='InformazioniTestoRigaDue'>" +
					Msg.trial["servizioClienti"] + 
					"<span class='numericValue'>" + numVerde + "</span> " + 
					Msg.trial["orariServClienti"] + "<br /><br />" + 
				"</div>" + 
				"<div id='InformazioniTestoRigaTre'>" +
					"<div id='InformazioniTestoMsgTitolo'>" + "<br /><br />" +
						Msg.trial["messaggiSocieta"] +  
					":</div>" + 
					"<div id='InformazioniTestoMsgSpazio'>" + "<br /><br />" +

					"</div>" +
				"</div>" + 
			"</div>" + 
		"</div>"

};

GasInfo.ExitTariffa = function() {
	Log.alert(80, GasStorico.MODULE, "GasInfo.ExitTariffa");
};

GasInfo.GestTariffa = function() {
	Log.alert(80, GasStorico.MODULE, "GasInfo.GestTariffa");
	$("#Content").html(GasInfo.htmlTariffa);
};

GasInfo.ExitInformazioni = function() {
	Log.alert(80, GasStorico.MODULE, "GasInfo.ExitInformazioni");
};

GasInfo.GestInformazioni = function() {
	Log.alert(80, GasStorico.MODULE, "GasInfo.GestInformazioni");
	$("#Content").html(GasInfo.htmlInformazioni);
};

GasInfo.ExitContatti = function() {
	Log.alert(80, GasStorico.MODULE, "GasInfo.ExitContatti");
};

GasInfo.GestContatti = function() {
	Log.alert(80, GasStorico.MODULE, "GasInfo.GestContatti");
	$("#Content").html(GasInfo.htmlContatti);
};

GasInfo.ExitQuestionari = function() {
	Log.alert(80, GasStorico.MODULE, "GasInfo.ExitQuestionari");
};

GasInfo.GestQuestionari = function() {
	Log.alert(80, GasStorico.MODULE, "GasInfo.GestQuestionari");
	$("#Content").html(GasInfo.htmlQuestionari);
};
