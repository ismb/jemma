//var CostiConsumi ={};

CostiConsumi.GestCosti = function() {

	$("#CostiConsumi").show();

	if ((CostiConsumi.mode == CostiConsumi.CONSUMI) || (CostiConsumi.mode == CostiConsumi.FOTOVOLTAICO)) {

		CostiConsumi.mode = CostiConsumi.COSTI;
		
		if (Main.enablePV){
			$("#ProduzioneAttualeTitolo").hide();
			$("#ProduzioneAttuale").hide();
			$("#ReteAttualeTitolo").hide();
			$("#ReteAttuale").hide();
			$("#FrecceFV").hide();
			$("#CostoAttualeImg").show();
			
			$("#CostoConsumoAttualeTitolo").css('top', '2%').css('left', '2%');
			$("#CostoConsumoAttuale").css('top', '2%').css('left', '2%');
			$("#CostoConsumoAttuale").css('border-bottom', '1px solid #0B0B96');

			$("#percIAC").hide();

			$("#CostoConsumoOdierno").show();
			$("#CostoConsumoPrevisto").show();
			
			$("#IndicatoreSopra").show();
			$("#IndicatoreMedia").show();
			$("#IndicatoreSotto").show();
			$("#IndicatorePaddingLeft").show();
			
			$("#IndicatoreTitolo").css('top', '5%').css('left', '55%');
			$("#Indicatore").css('top', '15%').css('left', '55%');
			$("#Indicatore").width('14%').height('150%');

			$("#IndicatoreTitolo").text(Msg.home["indicatoreCosti"]);

			$("#InfoFeed").show();
			$("#InfoFeedTitolo").show();
			$("#MarqueeContainer").hide();
		}

		$("#CostoAttualeImg").show();
		$("#DettaglioCosto").show();
		$("#TariffaImgDiv").show();
		$("#TariffaPos").show();
		$("#DettaglioSuddivisioneCosti").show();
		$('#CostoIndicatoreImg').show();

		$("#ValConsumoAttuale").hide();
		if (navigator.userAgent.indexOf('MSIE 7.0') > -1){
			//Sono in Internet Explorer 7.0
			var conMeterDiv = document.getElementById('ConsumoAttualeMeter');
				conMeterDiv.style.display = 'none';
			var graphConsOdiernoDiv = document.getElementById('GraficoConsumoOdierno');
				graphConsOdiernoDiv.style.display = 'none';
		} else {
			$("#ConsumoAttualeMeter").hide();
			$("#ConsumoAttualeMeter div").hide();
			$("#ConsumoAttualeMeter img").hide();
			$("#GraficoConsumoOdierno").hide();
		}
		$("#DettaglioConsumoMaggiore").hide();
		$('#ConsumoIndicatoreImg').hide();

		$("#CostoConsumoAttualeTitolo").text(Msg.home["titoloCosti"]);

		$("#CostoTConsumoMaxTitolo").text(Msg.home["tariffa"]);
		$("#InfoTitolo").text(Msg.home["spesaMensile"]);

	}

	dim = $("#CostoAttualeImg").height();
		  $("#CostoAttualeImg").width(dim);
	wDiv = $("#CostoConsumoAttuale").width();
		   $("#CostoAttualeImg").css("left", (wDiv - dim) / 2);

	// se festivo metto una barra diversa, l'immagine della tariffa la metto solo la prima volta
	if (CostiConsumi.tariffaImg == null) {
		CostiConsumi.tariffaImg = CostiConsumi.GetImgTariffa(Main.dataAttuale);

		coord = Utils.ResizeImg("TariffaImgDiv", CostiConsumi.tariffaImg, false, 2, 9);
		$("#TariffaImgDiv").html("<img id='TariffaImg' src='" + CostiConsumi.tariffaImg + "' width='" + coord[0] + "px' height='" + coord[1] + "px' style='position:absolute;top:" + coord[2] + "px;left:" + coord[3] + "px'>");
		CostiConsumi.leftTariffaPos = $("#TariffaImgDiv").position().left;
	}
	if (CostiConsumi.costoOdierno == null){
		txt = Msg.home["datoNonDisponibile"];
	} else {
		txt = (CostiConsumi.costoOdierno).toFixed(2) + " &euro;";
	}
	$("#DettaglioCostoConsumoOdierno").html('');
	$("#DettaglioCostoConsumoOdierno").html(Msg.home["costoFinora"] + ":<br><br><b>" + txt + "</b>");
	if (CostiConsumi.costoPrevMese == null){
		txt = Msg.home["datoNonDisponibile"];
	} else {
		txt = (CostiConsumi.costoPrevMese).toFixed(2) + " &euro;";
	}
	$("#DettaglioCostoConsumoPrevisto").html('');
	$("#DettaglioCostoConsumoPrevisto").html(Msg.home["costoPrevisto"] + ": <br><br><b>" + txt + "</b>");
	CostiConsumi.SetTariffa();
	Main.ResetError();
	if (CostiConsumi.costoOdierno == null){
		CostiConsumi.GetCostoOdierno();
	}
}

CostiConsumi.ExitCosti = function() {

	Main.ResetError();
	hideSpinner();
	
	if (CostiConsumi.timerCosti != null) {
		clearInterval(CostiConsumi.timerCosti);
		CostiConsumi.timerCosti = null;
	}
	InterfaceEnergyHome.Abort();
	CostiConsumi.tariffaImg = null;
	$("#CostiConsumi").hide();

}

/**************************************************************************************
 * mette check che indica posizione sulla barra della tariffa in base all'ora attuale *
 **************************************************************************************/
CostiConsumi.SetTariffa = function() {
	// calcolo posizionamento sulla barra del rettangolo che indica l'ora tiene conto di come e' fatta l'imamgine della tariffa
	var w = $("#TariffaImg").width();
	var ore = Main.dataAttuale.getHours();
	// ogni quadratino della tariffa e' largo 13px e la distanza e' 5 px per width img = 432
	var dimQ = (w / 432) * 13.13;
	var dimS = (w / 432) * 5;
	var val = (ore * dimQ) + (dimS * ore) + CostiConsumi.leftTariffaPos;
	$("#TariffaPos").css("left", Math.round(val) + "px");

}

/*******************************************************************************
 * avvia le richieste dei dati, che vengono fatte in sequenza perche' asincrone,
 * visualizzo i dati una volta sola quando li ho tutti
 ******************************************************************************/
CostiConsumi.GetCostoOdierno = function() {
	
	showSpinner();
	Main.ResetError();

	var start = Main.dataAttuale.getTime();
	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getAttributeData(
					CostiConsumi.DatiCostoOdiernoCb,
					InterfaceEnergyHome.PID_TOTALE, InterfaceEnergyHome.COSTO,
					start, start, InterfaceEnergyHome.DAY, true,
					InterfaceEnergyHome.DELTA);
		} catch (err) {
			InterfaceEnergyHome.GestErrorEH("GetCostoOdierno", err);
		}
	} else {
		// per test
		var indCostoOdierno = 0;
		indCostoOdierno += 1;
		if (indCostoOdierno == CostoOdierno.length){
			indCostoOdierno = 0;
		}

		var costoLista = CostoOdierno[indCostoOdierno];

		// prendo percentuale del costo in base all'ora
		var attuale = GestDate.GetActualDate();
		var oraAttuale = attuale.getHours();
		var minAttuale = attuale.getMinutes();
		var costo = 0;
		for (var i = 0; i < oraAttuale; i++) {
			costo += costoLista.list[i];
		}
		// aggiungo percentuale in base ai minuti dell'ora attuale
		costo += costoLista.list[oraAttuale] * (minAttuale / 60);
		var val = {"list" : [ costo ]};

		CostiConsumi.DatiCostoOdiernoCb(val, null);
	}

};

CostiConsumi.DatiCostoOdiernoCb = function(result, err) {

	if (result){
		CostiConsumi.costoOdierno = result.list[0];
	}

	if (err != null) {
		InterfaceEnergyHome.GestErrorEH("DatiCostoOdiernoCb", err);
	}

	if (CostiConsumi.costoOdierno != null) {
		txt = (CostiConsumi.costoOdierno).toFixed(2) + " &euro;";
	} else {
		txt = Msg.home["datoNonDisponibile"];
	}

	$("#DettaglioCostoConsumoOdierno").html('');
	$("#DettaglioCostoConsumoOdierno").html(Msg.home["costoFinora"] + ":<br><br><b>" + txt + "</b>");

	CostiConsumi.GetCostoGiornaliero();
};

CostiConsumi.GetCostoGiornaliero = function() {

	var start = new Date(Main.dataAttuale.getTime());
	start.setHours(0);
	var end = Main.dataAttuale.getTime();
	/**/
	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getAttributeData(
					CostiConsumi.DatiCostoGiornalieroCb,
					InterfaceEnergyHome.PID_TOTALE, InterfaceEnergyHome.COSTO,
					start.getTime(), end, InterfaceEnergyHome.HOUR, true,
					InterfaceEnergyHome.DELTA);
		} catch (err) {
			InterfaceEnergyHome.GestErrorEH("GetCostoGiornaliero", err);
		}
	} else {
		// per test, copio per il numero ore attuale
		var hours = Main.dataAttuale.getHours();
		var val = CostoGiornaliero;
		val.list = val.list.slice(0, hours);
		CostiConsumi.DatiCostoGiornalieroCb(val, null);
	}

}

CostiConsumi.DatiCostoGiornalieroCb = function(result, err) {

	if (result){
		CostiConsumi.costoGiornaliero = result.list;
	}
	if (err != null){
		InterfaceEnergyHome.GestErrorEH("DatiCostoGiornalieroCb", err);
	}
	CostiConsumi.GetCostoMedio();
}

CostiConsumi.GetCostoMedio = function() {
	var weekDay = Main.dataAttuale.getDay() + 1; // js comincia da 0, java da 1

	if (InterfaceEnergyHome.mode > 1){
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getWeekDayAverage(CostiConsumi.DatiCostoMedioCb, InterfaceEnergyHome.PID_TOTALE, InterfaceEnergyHome.COSTO, weekDay);
		} catch (err) {
			InterfaceEnergyHome.GestErrorEH("GetCostoMedio", err);
		}
	} else {
		//Prendo i dati da DataSimul.js
		CostiConsumi.DatiCostoMedioCb(CostoMedio, null);
	}

}

CostiConsumi.DatiCostoMedioCb = function(result, err) {

	if (result){
		CostiConsumi.costoMedio = result.list;
	}
	if (err != null){
		InterfaceEnergyHome.GestErrorEH("DatiCostoMedioCb", err);
	}
	hideSpinner();
	// ho i dati per visualizzare l'indicatore dei costi e il costo odierno
	CostiConsumi.VisIndicatoreCosti();
	CostiConsumi.GetSuddivisioneCosti();
}

/*******************************************************************************
 * funzione che calcola l'altezza della barra nel termometro dei costi calcolo
 * percentuale rispetto media Dal 29-11-2011: sommo i valori giornalieri fino
 * all'ora attuale o all'ultimo valore non null, sommo i valori medio per lo
 * stesso numero di ore poi faccio il confronto. Se un'ora del giornaliero e'
 * null non prendo il valore corrispondente della media, se una'ora della media
 * e' null non faccio il confronto
 ******************************************************************************/
CostiConsumi.VisIndicatoreCosti = function() {
	var medio, odierno, n, max, perc;

	perc = 0;
	odierno = null;
	medio = null;

	if ((CostiConsumi.costoMedio != null) && (CostiConsumi.costoGiornaliero != null) && (CostiConsumi.costoOdierno != null)) {
		n = 0;
		odierno = 0;
		medio = 0;
		max = CostiConsumi.costoGiornaliero.length - 1;
		// sommo consumo giornaliero e media per tutti valori non null
		// nel caso dovessi dare null per un valore mancante a meta' devo
		// partire dalla fine
		// considero a parte l'ultima ora (ora attuale)
		for (i = 0; i < max; i++) {
			if (CostiConsumi.costoGiornaliero[i] != null) {
				n++;
				// se media null per valore valido odierno non faccio confronto
				if (CostiConsumi.costoMedio[i] == null) {
					odierno = null;
					break;
				}
				medio += CostiConsumi.costoMedio[i];
				odierno += CostiConsumi.costoGiornaliero[i];
			}
		}
		// se l'ultimo valore non e' null prendo una percentuale della media in
		// base ai minuti attuali
		if ((medio != null) && (odierno != null)) {
			if (CostiConsumi.costoGiornaliero[max] != null) {
				if (CostiConsumi.costoMedio[max] != null) {
					min = GestDate.GetActualDate().getMinutes();
					medio += CostiConsumi.costoMedio[max] * (min / 60);
					odierno += CostiConsumi.costoGiornaliero[max];
				} else{
					medio = null;
				}
			}
			if (medio != null) {
				perc = odierno / medio;
				if (perc > 2){
					perc = 2;
				}
			}
		}
	}
	$('#CostoIndicatoreImg').gauge("value", perc);
	// calcolo come sono rispetto alla media (per differenze sotto 0.10�  considero uguale)
	if ((odierno == null) || (medio == null)){
		diffInd = 3;
	} else {
		diffCosto = odierno - medio;
		if (Math.abs(diffCosto) < 0.02){
			diffInd = 1;
		} else if (odierno > medio){
			diffInd = 2;
		} else {
			diffInd = 0;
		}
	}
	$("#CostoAttualeImg").attr("src", CostiConsumi.costoOdiernoImg[diffInd]);
	$("#DettaglioCosto").html(CostiConsumi.costoOdiernoMsg[diffInd]);
}

CostiConsumi.GetSuddivisioneCosti = function() {

	var myDate = new Date(Main.dataAttuale.getTime());
	myDate.setDate(myDate.getDate() -30);//Mi sposto indietro di 30 giorni.
	var start = new Date(myDate.getTime());
	var end = new Date(Main.dataAttuale.getTime());
	//console.log('start', start);
	//console.log('end', end);
	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getAttributeData(CostiConsumi.DatiSuddivisioneCostiCb, 
															InterfaceEnergyHome.COSTO, 
															start.getTime(), 
															end.getTime(), 
															InterfaceEnergyHome.DAY, true, 
															InterfaceEnergyHome.DELTA);
		} catch (err) {
			InterfaceEnergyHome.GestErrorEH("GetSuddivisioneCosti", err);
		}
	} else {
		// per test
		//var ind = Math.round(Math.random() * SuddivisioneCosti.length);
		CostiConsumi.DatiSuddivisioneCostiCb(SuddivisioneCosti, null);
	}
}

CostiConsumi.DatiSuddivisioneCostiCb = function(result, err) {

	var listaCosti = new Array();
	var costoTotale = 0;
	var altriCosti = 0;
	var CostoSmartinfo = 0;
	var controlSumListaCosti = 0;

	var retVal = null;
	if (err != null){
		InterfaceEnergyHome.GestErrorEH("DatiSuddivisioneCostiCb", err);
	}

	if (result != null) {
		/*
		 * Creo l'array di coppie nome-costo, escludendo lo smartInfo e
		 * calcolando il costo totale
		 */
		$.each(result.map,
				function(indexResult, element) {

					if (indexResult != CostiConsumi.SmartInfo[InterfaceEnergyHome.ATTR_APP_PID]) {

						if (CostiConsumi.listaElettr[indexResult]) {
							if (element.list.length > 0) {
								var sum = 0;
								for (var i = 0; i < element.list.length; i++){
									var value = element.list[i];
									if (value != null){
										sum += value;
									}
								};
								costoTotale += sum;
								if (sum > 0){
									controlSumListaCosti++;
								}
								listaCosti.push(new Array(CostiConsumi.listaElettr[indexResult][InterfaceEnergyHome.ATTR_APP_NAME], sum));
							}
						}
					} else {
						var sumSmartInfo = 0;
						for (var i = 0; i < element.list.length; i++){
							var vSmartInfo = element.list[i];
							if (vSmartInfo != null){
								sumSmartInfo += vSmartInfo;
							}
						};
						CostoSmartinfo += sumSmartInfo;
					}
				});

		if (CostoSmartinfo) {
			altriCosti = CostoSmartinfo - costoTotale;
		}

		if (altriCosti > 0){
			controlSumListaCosti++;
			listaCosti.push(new Array("Altro", altriCosti));
		}
		
		if (controlSumListaCosti == 0){
			//Se tutti gli elementi inseriti in listaCosti hanno valore 0, non mostro il grafico.
			listaCosti = new Array();
		}

		if (listaCosti.length > 0) {
			// Radialize the colors
			Highcharts.getOptions().colors = $.map(
					Highcharts.getOptions().colors, function(color) {
						return {radialGradient : {cx : 0.5, cy : 0.3, r : 0.7},
								stops : [[ 0, color ],
										 [ 1, Highcharts.Color(color).brighten(-0.3).get('rgb') ]] 
							    };
					});

			// Build the chart
			chart = new Highcharts.Chart({
				chart : {
					renderTo : 'DettaglioSuddivisioneCosti',
					plotBackgroundColor : null,
					plotBorderWidth : null,
					plotShadow : false
				},
				colors : [ '#F9F09B', '#F0CE20', '#FFEE66', '#F0BF0F', '#EFE38B', '#FF8100', '#F6C050', '#E1E140', '#FFBF18', '#F1F180' ],
				title : {
					text : ""
				},
				tooltip : {
					formatter : function() {
						var modificatore = '1'; 
						for(var i=0;i<2;i++)
						    modificatore += "0"; 
						modificatore = parseInt(modificatore,10)
						var valEuro = Math.round(this.y*(modificatore))/(modificatore); 
						
						return '<b>' + this.point.name + '</b>: ' + Math.floor(this.percentage) + ' % - ' + valEuro + ' euro ';
					}
				},
				plotOptions : {
					pie : {
						allowPointSelect : true,
						cursor : 'pointer',
						dataLabels : {
							enabled : true,
							color : '#000',
							connectorColor : '#000',
							formatter : function() {
								return '<b>' + this.point.name + '</b>:<br />' + Math.floor(this.percentage) + ' %';
							},
							overflow: 'justify',
							distance: 9,
							rotation: 0,
							style: {
								color: '#333333',
								fontSize: '9pt',
								padding: '5px'
							}
						}
					}
				},
				credits : false,
				series : [ {
					type : 'pie',
					name : 'Lista dei costi',
					data : listaCosti
				} ]
			});
		} else {
			$("#DettaglioSuddivisioneCosti").text("Dati non disponibili");
		}
	} else {
		$("#DettaglioSuddivisioneCosti").html("<div id='SuddivisioneCostiVuoto'>" + Msg.home["suddivisioneVuoto"] + "</div>");
	}
	CostiConsumi.GetCostoPrevisto();
}

CostiConsumi.GetCostoPrevisto = function() {
	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getForecast(CostiConsumi.DatiCostoPrevistoCb, InterfaceEnergyHome.PID_TOTALE, InterfaceEnergyHome.COSTO, Main.dataAttuale.getTime(), InterfaceEnergyHome.MONTH);
		} catch (err) {
			InterfaceEnergyHome.GestErrorEH("BackCostoPrevisto", err);
		}
	} else {
		// per test
		CostiConsumi.DatiCostoPrevistoCb(CostoPrevisto, null);
	}
}

CostiConsumi.DatiCostoPrevistoCb = function(result, err) {
	
	var txt;

	if (result){
		CostiConsumi.costoPrevMese = result;
	}

	if (err != null){
		InterfaceEnergyHome.GestErrorEH("DatiCostoPrevistoCb", err);
	}

	if (result != null) {
		txt = (CostiConsumi.costoPrevMese).toFixed(2) + " &euro;";
	} else {
		txt = Msg.home["datoNonDisponibile"];
	}

	$("#DettaglioCostoConsumoPrevisto").html('');
	$("#DettaglioCostoConsumoPrevisto").html(Msg.home["costoPrevisto"] + ": <br><br><b>" + txt + "</b>");
	hideSpinner();

}

/*******************************************************************************
 * crea la parte costante della grafica
 ******************************************************************************/
CostiConsumi.GetImgTariffa = function(data) {
	// controlla se sabato o domenica
	day = data.getDay();
	if ((day == 0) || (day == 6))
		img = Define.home["tariffaFestiva"];
	else
		img = Define.home["tariffaFeriale"];
	d = data.getDate();
	m = data.getMonth();
	// controlla se giorno di festa nazionale
	for (i = 0; i < Define.festivi.length; i++)
		if ((d == Define.festivi[i][0]) && (m == Define.festivi[i][1])) {
			img = Define.home["tariffaFestiva"];
			break;
		}
	return img;
}
