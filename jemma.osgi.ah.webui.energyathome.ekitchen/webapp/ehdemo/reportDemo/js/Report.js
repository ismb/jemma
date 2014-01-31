var Report = {
	MODULE : "Report",
	
	htmlContent : $(document.createElement('div'))
						.attr('id', 'Report')
						.attr('class', 'Content')
						.append($(document.createElement('div'))
									.attr('id', 'TitoloReport')
									.css('color', 'white')
									.attr('class', 'ContentTitle')
									.css('text-align', 'center'))
						.append($(document.createElement('div'))
									.attr('id', 'ReportDatiCasa')
									.attr('class', 'ReportBGND')
									.append($(document.createElement('div'))
												.attr('id', 'TitoloEventiCasa')
												.attr('class', 'TitoloDettaglio')
												.css('color', 'white')
												.text(Msg.report["TitoloEventiCasa"])
												.append($(document.createElement('span')).css('color', 'white')
															.text(Msg.report["carosello"][0])))
									.append($(document.createElement('div'))
												.attr('id', 'carousell')
												.width('100%').height('55%')))
						.append($(document.createElement('div')).attr('id', 'ReportDatiDettaglio')
																.attr('class', 'ReportBGND').css('left', '50%')
																.append($(document.createElement('div'))
																		.attr('id', 'TitoloElettrodomDettaglio')
																		.css('color', 'white')
																		.attr('class', 'TitoloDettaglio')
																		.text(Msg.report["TitoloElettrodomDettaglio"])
																		.append($(document.createElement('span')).css('color', 'white')
																					.text(Msg.report["carosello"][0])))
																.append($(document.createElement('div'))
																		.attr('id', 'ReportInfo')
																		//.text(Msg.report["TitoloElettrodomDettaglio"])
																		.append($(document.createElement('p')).attr('id', 'paragrafoInfo')
																											  .css('color', 'white').css('padding', '1%')))
																.append($(document.createElement('div')).attr('id', 'elettrodomDetail'))),
	indiceCarosello : 0,
	numEldo : 7,
	carouselImgWidth: 0,
	carouselImgHeight: 0,
	listaElettrodomestici: null,
	hag_id: null,
	userPV: false
}

Report.Init = function() {

	Highcharts.setOptions({
		colors : [ 'red', "#9EC200", '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4' ]
	});
	
	/* Creo il contenitore dei dati di report per la Gui e lo aggiungo alla pagina */
	var divReport = $("#Report");

	/* Controllo che il div di Report non sia già stato riempito. Se non esiste lo inizializzo, se già esiste lo visualizzo solamente */
	if (divReport.length == 0) {
		$("#ReportContainer").append(Report.htmlContent);
		
		Main = {};
		Main.env = 0;
		//Report.GetElettrodomestici();
		Report.getHagID();
		
		var getData = 'hag_id=' + Report.hag_id;
	
		$.ajax({
		    type: "GET",
		    data: getData,
		    beforeSend: function(x) {
		        if(x && x.overrideMimeType) {
		            x.overrideMimeType("application/json;charset=UTF-8");
		        }
		    },
		    url: 'reportService.php',
		    success: function(response) {
		        // 'data' is a JSON object which we can access directly.
		        // Evaluate the data.success member and do something appropriate...
		        Report.responseDataForAccordion = response;
	
				var settPrecString = Report.setDate();
				$("#TitoloReport").html(settPrecString);
	
		        Report.gestResponseReportService();
		    },
		    error: function(err) {
		        // 'data' is a JSON object which we can access directly.
		        // Evaluate the data.success member and do something appropriate...
		        console.log(err);
		    }
		});
	} else {
		$("#Report").show();
	}
	$("#ReportInfo").empty();
	$("#ReportInfo").html(Msg.report["informaReport"][0]);
	$("#paragrafoInfo").empty();
	$("#paragrafoInfo").html(Msg.report["informaReport"][0]);
	/* Per ultimo nascondo lo spinner */
	hideSpinner();
}

Report.gestResponseReportService = function() {

	var objData = Report.responseDataForAccordion;
	var consumiFissiData = [];
	var tab2Data = [];
	for (var el in objData){
		if (el == 'min_power_rank'){
			if ((objData[el] == null) || (objData[el] < 0)){
				consumiFissiData[0] = 'n.d.';
			//} else if (objData[el] <= 3){
			//	var cl = Math.floor(objData[el]);
			//	cl--;
			//	consumiFissiData[0] = Msg.report['posizione'][cl];
			} else {
				consumiFissiData[0] = objData[el] + '&#176;';
			}
		}
		if (el == 'weekly_avg_min_power'){
			if ((Math.floor(objData[el]) <= 5) || (objData[el] == null)){
				consumiFissiData[1] = 'n.d.';
			} else {
				consumiFissiData[1] = Math.floor(objData[el]) + ' W';
			}
		}
		if (el == 'yearly_min_power_cost'){
			if ((Math.floor(objData[el]) <= 5) || (objData[el] == null)){
				consumiFissiData[2] = 'n.d.';
			} else {
				consumiFissiData[2] = Math.floor(objData[el]) + ' &euro;';
			}
		}
		
		if (el == 'f23_rank'){
			if ((objData[el] == null) || (objData[el] < 0)){
				tab2Data[0] = 'n.d.';
			//} else if (objData[el] <= 3){
			//	var cl = Math.floor(objData[el]);
			//	cl--;
			//	tab2Data[0] = Msg.report['posizione'][cl];
			} else {
				tab2Data[0] = objData[el] + '&#176;';
			}
		}
		if (el == 'f23_percentage'){
			if ((Math.floor(objData[el]) <= 0) || (objData[el] == null)){
				tab2Data[1] = 'n.d.';
			} else {
				tab2Data[1] = Math.floor(objData[el]) + ' %';
			}
		}
		if (el == 'weekly_avg_f23_percentage'){
			if ((Math.floor(objData[el]) <= 0) || (objData[el] == null)){
				tab2Data[2] = 'n.d.';
			} else {
				tab2Data[2] = Math.floor(objData[el]) + ' %';
			}
		}

		if (el == 'weekly_min_power'){
			if ((Math.floor(objData[el]) <= 5) || (objData[el] == null)){
				consumiFissiData[3] = 'n.d.';
			} else {
				consumiFissiData[3] = Math.floor(objData[el]) + ' W';
			}
		}

		if (el == 'yearly_cost_forecast'){
			if ((Math.floor(objData[el]) <= 50) || (objData[el] == null)){
				tab2Data[3] = 'n.d.';
			} else {
				tab2Data[3] = Math.floor(objData[el]) + ' &euro;';
			}
		}
	}

	/* Struttura del contenuto di un elemento dell'accordion */
	
	var riga = $(document.createElement('table'))
					.attr('class', 'accordion-table')
					.height('30%').css('min-height', '100%')
					.append($(document.createElement('tbody'))
							.append($(document.createElement('tr'))
									.append($(document.createElement('td')))
									.append($(document.createElement('td')))
									.append($(document.createElement('td'))))
							.append($(document.createElement('tr'))
									.append($(document.createElement('td')).attr('id', 'posizioneclassifica').attr('class', 'altro'))
									.append($(document.createElement('td')).attr('id', 'mediacommunity').attr('class', 'secondo'))
									.append($(document.createElement('td')).attr('id', 'previsione').attr('class', 'altro'))));
	

	/* Aggiungo il Div che conterrà l'accordion */
	$(document.createElement('div')).attr('id', 'EventiCasa1').appendTo($("#ReportDatiCasa"));

	/* Imposto il contenuto dell'accordion */
	$("#EventiCasa1").append($(document.createElement('div')).attr('id', 'Dati1')
					 										 .append($(document.createElement('div')).attr('id', 'headerAccordion1').attr('class', 'headerAccordion')
					 												 								 .append($(document.createElement('a')).attr('href', '#')
					 												 										 							   .text(Msg.report["ConsumiFissi"] + " : ")
					 												 										 							   .css('color', 'white')
					 												 										 							   .append($(document.createElement('span')).html(consumiFissiData[3]))))
							 							   	 .append($(document.createElement('div')).attr('id', 'Dati11Content')) //.height(100)
							 							   	 .append($(document.createElement('div')).attr('id', 'headerAccordion2').attr('class', 'headerAccordion')
							 							   			 								 .append($(document.createElement('a')).attr('href', '#')
							 							   			 										 							   .text(Msg.report["ConsumoFasciaEco"] + " : ")
					 												 										 							   .css('color', 'white')
							 							   			 										 							   .append($(document.createElement('span')).html(tab2Data[3]))))
							 							     .append($(document.createElement('div')).attr('id', 'Dati12Content')) //.height(100)
							 							     /*.append($(document.createElement('div')).attr('id', 'headerAccordion3').attr('class', 'headerAccordion')
							 							    		 								 .append($(document.createElement('a')).attr('href', '#')
							 							    		 										 							   .text(Msg.report["CO2"] + " : ")
							 							    		 										 							   .append($(document.createElement('span')).text(ReportSim.C02))))
							 							     .append($(document.createElement('div')).attr('id', 'Dati13Content').height(100))*/
	);

	/* Inizializzo il bottone Info a destra dell'header dell'accordion */
	$(".headerAccordion button").button({
		icons : {primary : "ui-icon-info"},
		text : false
	});

	//$("#ReportInfo").css("border", "#ABD037 solid 2px").css("background", "#fff");

	$("#Dati1").accordion({
		fillSpace : true,
		header : '.headerAccordion',
		changestart : function(event, ui) {
			indiceAccordion = $.inArray(ui.newHeader[0],$("#Dati1 .headerAccordion"));
			switch (indiceAccordion) {
				case 0: {
					Report.VisConsumiFissiGraf();
					indiceAccordion = (Report.indiceCarosello != 0) ? indiceAccordion + 1 : indiceAccordion;
					break;
				}
				case 1: {
					Report.VisFasciaGraf();
					indiceAccordion += 1;
					break;
				}
				case 2: {
					Report.VisCompensa();
					indiceAccordion += 1;
					break;
				}
				default: {
					Report.VisUtilizzoGraf();
					break;
				}
			}
			
			$("#ReportInfo").html(Msg.report["informaReport"][indiceAccordion]);
			$("#paragrafoInfo").html(Msg.report["informaReport"][indiceAccordion]);
		}
	});

	/* Imposto il contenuto su ogni elemento dell'accordion */
	$(".ui-accordion-content").append(riga);
	$("#ReportDatiCasa table").map(function(indexTable, domElementSection) {
		$(domElementSection).find("tr")
			.map(function(indexRow, domElementRow) {
				$(domElementRow).children("td")
					.map(function(indexCell, domElementCell) {
						switch (indexRow) {
							case 0: {
								/* Inserimento titoli */
								$(domElementCell).append(Msg.report["TitoliAccordion"][indexTable + 1][indexCell]);
								break;
							}
							case 1: {
								/* Inserimento dati */
								if (indexTable == 0){
									$(domElementCell).html(consumiFissiData[indexCell]); 
								} else { 
									$(domElementCell).html(tab2Data[indexCell]);
								}
							}
						}
					});

			});

	});
		
	var urlVars = Report.getUrlVars();
	
	var lblApp = urlVars['app'];
	var lblIcon = urlVars['icon'];
	var lstEl = urlVars['last'];
	
	var listEldoForRequest = [];
	
	for (var iCounter = 0; iCounter <= lstEl; iCounter++){
		var idEldo = 'ah.app.' + urlVars[lblApp+iCounter];
		var iconaEldo = urlVars[lblIcon+iCounter];

		listEldoForRequest[iCounter] = {'idEldo': idEldo, 'nomeEldo': null, 'categoryEldo': null, 'locationEldo': null, 'iconaEldo': iconaEldo};
	}
	
	var eldoRequestString = 'last='+lstEl;
	
	for (var el in listEldoForRequest){
		eldoRequestString += '&eldo'+el+'='+listEldoForRequest[el]['idEldo'];
	}
	eldoRequestString += '&hag_id=' + Report.hag_id;
	
	$.ajax({
	    type: "GET",
	    data: eldoRequestString,
	    beforeSend: function(x) {
	        if(x && x.overrideMimeType) {
	            x.overrideMimeType("application/json;charset=UTF-8");
	        }
	    },
	    url: 'appliancesService.php',
	    success: function(response) {
	        // 'data' is a JSON object which we can access directly.
	        // Evaluate the data.success member and do something appropriate...
	        for (var el in response){
	        	for (var iCounter in listEldoForRequest){
	        		if (listEldoForRequest[iCounter]['idEldo'] == response[el]['appliance_pid']){
	        			listEldoForRequest[iCounter]['nomeEldo'] = response[el]['name'];
	        			listEldoForRequest[iCounter]['categoryEldo'] = response[el]['category_pid'];
	        			listEldoForRequest[iCounter]['locationEldo'] = response[el]['location_pid'];
	        		}
	        		if (response[el]['category_pid'] == '14'){
	        			Report.userPV = true;
	        		}
	        	}
	        }
	        Report.GetEldo(listEldoForRequest);
	    },
	    error: function(err) {
	        // 'data' is a JSON object which we can access directly.
	        // Evaluate the data.success member and do something appropriate...
	        console.log(err);
	    }
	});
}

Report.gestReportPage = function() {

	/** ***************************************************************************************************** */
	/* Configurazione del carosello */
	var centroCaroselloH = $("#carousell").width() / 2;
	var centroCaroselloV = $("#carousell").height() / 4;

	$("#carousell").CloudCarousel({
		xPos : centroCaroselloH,
		yPos : centroCaroselloV,
		yRadius: 60,
		buttonLeft : $("#left-but"),
		buttonRight : $("#right-but"),
		mouseWheel : false,
		bringToFront : true,
		reflHeight: 45,
		speed: 0 //0.3 se si vuol far ruotare DT
	});
	/* Aggiungo il bordo all'immagine dell'oggetto selezionato nel carosello */

	$($("#carousell img")[Report.indiceCarosello]).addClass("selezionato");

	/* Configuro il bottone NEXT del carosello */
	$("#right-but").button({text : false, icons : {primary : "ui-icon-seek-next"}})
				   .click(function(){
					   //Report.moveCarousel('right', false); //decommentare per far ruotare DT
				   });
	/* Configuro il bottone PREV del carosello */
	$("#left-but").button({text : false, icons : {primary : "ui-icon-seek-prev"}})
				  .click(function(){
					  //Report.moveCarousel('left', false); //decommentare per far ruotare DT
				  });
	$("#carousell img").click(function(){
		/*var ccObj = $('#carousell').data(); //decommentare per far ruotare DT
		var cloudCarousellObj = ccObj.cloudcarousel;
		
			this.controlTimer = setTimeout( function(){
				if (cloudCarousellObj.frontIndex != Report.indiceCarosello){
				
					if (cloudCarousellObj.frontIndex < 0){
						Report.indiceCarosello = (Report.numEldo) + cloudCarousellObj.frontIndex;
						Report.moveCarousel('left', true);
					} else {
						Report.indiceCarosello = cloudCarousellObj.frontIndex;
						Report.moveCarousel('right', true);
					}
				}
			}, 500);*/
	});
	Report.VisConsumiFissiGraf();
	Report.moveCarousel(null, true);
}

Report.moveCarousel = function(click, bringToFront){

	
	var objData = Report.responseDataForAccordion;
	var consumiFissiData = [];
	var tab2Data = [];
	
	for (var el in objData){
		if (el == 'min_power_rank'){
			if (Report.userPV){
				consumiFissiData[0] = 'n.d.';
			} else if ((objData[el] == null) || (objData[el] < 0)){
				consumiFissiData[0] = 'n.d.';
			//} else if (objData[el] <= 3){
			//	var cl = Math.floor(objData[el]);
			//	cl--;
			//	consumiFissiData[0] = Msg.report['posizione'][cl];
			} else {
				consumiFissiData[0] = objData[el] + '&#176;';
			}
		}
		if (el == 'weekly_avg_min_power'){
			if (Report.userPV){
				consumiFissiData[1] = 'n.d.';
			} else if ((Math.floor(objData[el]) <= 5) || (objData[el] == null)){
				consumiFissiData[1] = 'n.d.';
			} else {
				consumiFissiData[1] = Math.floor(objData[el]) + ' W';
			}
		}
		if (el == 'yearly_min_power_cost'){
			if (Report.userPV){
				consumiFissiData[2] = 'n.d.';
			} else if ((Math.floor(objData[el]) <= 5) || (objData[el] == null)){
				consumiFissiData[2] = 'n.d.';
			} else {
				consumiFissiData[2] = Math.floor(objData[el]) + ' &euro;';
			}
		}
		
		if (el == 'f23_rank'){
			if (Report.userPV){
				tab2Data[0] = 'n.d.';
			} else if ((objData[el] == null) || (objData[el] < 0)){
				tab2Data[0] = 'n.d.';
			//} else if (objData[el] <= 3){
			//	var cl = Math.floor(objData[el]);
			//	cl--;
			//	tab2Data[0] = Msg.report['posizione'][cl];
			} else {
				tab2Data[0] = objData[el] + '&#176;';
			}
		}
		if (el == 'f23_percentage'){
			if (Report.userPV){
				tab2Data[1] = 'n.d.';
			} else if ((Math.floor(objData[el]) <= 0) || (objData[el] == null)){
				tab2Data[1] = 'n.d.';
			} else {
				tab2Data[1] = Math.floor(objData[el]) + ' %';
			}
		}
		if (el == 'weekly_avg_f23_percentage'){
			if (Report.userPV){
				tab2Data[2] = 'n.d.';
			} else if ((Math.floor(objData[el]) <= 0) || (objData[el] == null)){
				tab2Data[2] = 'n.d.';
			} else {
				tab2Data[2] = Math.floor(objData[el]) + ' %';
			}
		}

		if (el == 'weekly_min_power'){
			if (Report.userPV){
				consumiFissiData[3] = 'non disponibile';
			} else if ((Math.floor(objData[el]) <= 5) || (objData[el] == null)){
				consumiFissiData[3] = 'n.d.';
			} else {
				consumiFissiData[3] = Math.floor(objData[el]) + ' W';
			}
		}

		if (el == 'yearly_cost_forecast'){
			if (Report.userPV){
				tab2Data[3] = 'non disponibile';
			} else if ((Math.floor(objData[el]) <= 50) || (objData[el] == null)){
				tab2Data[3] = 'n.d.';
			} else {
				tab2Data[3] = Math.floor(objData[el]) + ' &euro;';
			}
		}
	}
	
	/* Elimino il bordo che indica l'oggetto selezionato */
	$("#carousell img").removeClass("selezionato");
	
	var isEldo = 0; // 0 = Casa, 1 = Eldo
	
	var nomeEldo = Report.listaElettrodomestici[Report.indiceCarosello]['nomeCampo']; 
	if (Report.listaElettrodomestici[Report.indiceCarosello]['categoryCampo'] == '12'){
		nomeEldo = 'Casa';
	}
	
	/* Il primo header cambia nel caso venga selezionato un elettrodomestico oppure la casa (==0) */
	if (Report.indiceCarosello != 0) {
		isEldo = 1;

		var header1 = $("#Dati1 #headerAccordion1 a")[0];
		$(header1).html(Msg.report["ConsumiFissi"] + " : " + " <span></span>");
		$(header1).children("span").append(' Dato non disponibile');
		
		var header2 = $("#Dati1 #headerAccordion2 a")[0];
		$(header2).html(Msg.report["ConsumoFasciaEco"] + " : " + " <span></span>");
		$(header2).children("span").html(' Dato non disponibile');
		
		/* Compilo il contenuto del primo header */
		$("#ReportDatiCasa table tr:nth-child(1)")
				.first().children("td").map(function(indexCell, domElementCell) {
					$(domElementCell).html(Msg.report["TitoliAccordion"][0][indexCell]);});
		$("#ReportDatiCasa table tr:nth-child(2)")
				.first().children("td").map(function(indexCell, domElementCell) {
					$(domElementCell).html('Dato non disponibile');});
		
	} else {
		isEldo = 0;

		var header1 = $("#Dati1 #headerAccordion1 a")[0];
		$(header1).html(Msg.report["ConsumiFissi"] + " : " + " <span></span>");
		$(header1).children("span").append(consumiFissiData[3]);
		
		var header2 = $("#Dati1 #headerAccordion2 a")[0];
		$(header2).html(Msg.report["ConsumoFasciaEco"] + " : " + " <span></span>");
		$(header2).children("span").html(tab2Data[3]);

		/* Compilo il contenuto del primo header */
		$("#ReportDatiCasa table tr:nth-child(1)")
				.first().children("td").map(function(indexCell, domElementCell) {
					$(domElementCell).html(Msg.report["TitoliAccordion"][1][indexCell]);});
		$("#ReportDatiCasa table tr:nth-child(2)")
				.first().children("td").map(function(indexCell, domElementCell) {
					$(domElementCell).html(consumiFissiData[indexCell]);});
	}

	/* Compilo i contenuti degli altri due header */
	$("#ReportDatiCasa table tr:odd:gt(0)").map(function(indexTable, ElementRow) {
		if (indexTable < 2){
			$(ElementRow).children("td")
				.map(function( indexCell, domElementCell) { 
					if (isEldo == 1){
						$(domElementCell).html('Dato non disponibile');
					} else {
						$(domElementCell).html(tab2Data[indexCell]);
					}
					
				});
		}
	});
	
	// Incremento o decremento l'indice che mi riporta all'oggetto selezionato dal carosello
	if (!bringToFront){
		if (click == 'right'){
			Report.indiceCarosello -= 1;
			if (Report.indiceCarosello < 0){
				Report.indiceCarosello = (Report.numEldo-1);
			}
		} else {
			Report.indiceCarosello += 1;
			if (Report.indiceCarosello > (Report.numEldo-1)){
				Report.indiceCarosello = 0;
			}
		}
	}

	/* Aggiungo il bordo all'immagine dell'oggetto selezionato dal carosello */
	$($("#carousell img")[Report.indiceCarosello]).addClass("selezionato");
	$("#TitoloEventiCasa span").html(nomeEldo); //Msg.report["carosello"][Report.indiceCarosello]);
	$("#TitoloElettrodomDettaglio span").html(nomeEldo); //Msg.report["carosello"][Report.indiceCarosello]);
				
	$("#ReportInfo").html(Msg.report["informaReport"][isEldo]);
	$("#paragrafoInfo").html(Msg.report["informaReport"][isEldo]);
}

/* Funzione per la visualizzazione del grafico dei consumi fissi */
Report.VisConsumiFissiGraf = function() {

	$("#elettrodomDetail").html(null); 
	$("#elettrodomDetail").append($(document.createElement('img')).attr('src', './Resources/Images/StandyBy.jpg').attr('width', '100%').css('max-height', '100%').attr('height', '100%'));
	/*
	var ConsumiFissi = new Array();
	for (i = 0; i < 25; i++) {
		ConsumiFissi.push(Math.random() * 11);
	}

	// Create the chart
	Highcharts.setOptions({
		lang: {
			months: ['Gennaio', 'Febbraio', 'Marzo', 'Aprile', 'Maggio', 'Giugno', 'Luglio', 'Agosto', 'Settembre', 'Ottobre', 'Novembre', 'Dicembre'],
			//shortMonths: ['Gen', 'Feb', 'Mar', 'Apr', 'Mag', 'Giu', 'Lug', 'Ago', 'Set', 'Ott', 'Nov', 'Dic'],
			shortMonths: ['Gennaio', 'Febbraio', 'Marzo', 'Aprile', 'Maggio', 'Giugno', 'Luglio', 'Agosto', 'Settembre', 'Ottobre', 'Novembre', 'Dicembre'],
			weekdays: ['Domenica', 'Lunedi\'', 'Martedi\'', 'Mercoledi\'', 'Giovedi\'', 'Venerdi\'', 'Sabato']
		}
	});
	chart = new Highcharts.StockChart({
				chart : {renderTo : 'elettrodomDetail'},
				xAxis : {dateTimeLabelFormats : {
							second : '%H:%M:%S',
							minute : '%H:%M',
							hour : '%y',
							day : '%b %e',
							week : '%b %e',
							month : '%b \'%y',
							year : '%Y'
						}},
				rangeSelector : {enabled : true},
				title : {text : 'Consumi Fissi'},
				tooltip: {animation : true},
				series : [{
							name : 'Consumi Fissi',
							data : (function() {
								// generate an array of random data
								var data = [], time = (new Date(2012, 7, 1, 0, 0, 0)).getTime(), i;
								for (i = 1; i <= 365 * 2; i++) {
									data.push([ time + i * 1000 * 60 * 60 * 24, Math.round(Math.random() * 100) ]);
								}
								return data;
							})(),
							type : 'areaspline',
							threshold : null,
							tooltip : {valueDecimals : 2},
							fillColor : {linearGradient : {
											x1 : 0,
											y1 : 0,
											x2 : 0,
											y2 : 1},
										 stops : [ [ 0, Highcharts.getOptions().colors[0] ], [ 1, 'rgba(0,0,0,0)' ] ]}
				}]
			});*/
}

/* Funzione per la visualizzazione del grafico dei consumi in fascia verde */
Report.VisFasciaGraf = function() {

	$("#elettrodomDetail").html(null);
	$("#elettrodomDetail").append($(document.createElement('img')).attr('src', './Resources/Images/consumi_elettrici_in_europa.png').attr('width', '100%').css('max-height', '100%').attr('height', '100%'));
	/*

	Highcharts.setOptions({
		colors : [ 'red', "#9EC200", '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4' ]
	});

	var dataVerde = new Array();
	var dataRosso = new Array();
	var data = new Array();

	dataRosso[0] = 0;
	dataVerde[0] = 0;

	for (var i = 1; i <= 31; i++) {
		var time = (new Date(2012, 7, 0, 0, 0, 0)).getTime();
		var day = time + i * 1000 * 60 * 60 * 24;
		var rnd1 = Math.round(Math.random() * 1000);
		var rnd2 = Math.round(Math.random() * 1000);
		
		var rnd_a = Math.round(Math.random() * 100);
		var rnd_b = 100 - rnd_a;
		
		if (rnd_a > rnd_b) {
			data.push([day, rnd_a, rnd_b]);
		} else if (rnd_a < rnd_b) {
			data.push([day, rnd_b, rnd_a]);
		} else {
			data.push([day, rnd_b+1, rnd_a-1]);
		} 
		
		dataRosso[i] = rnd1 / 100;
		dataVerde[i] = rnd2 / 100;
	}

	chartFasce = new Highcharts.Chart({
		chart : {
			renderTo : 'elettrodomDetail',
			type : 'column',
			plotBackgroundColor : '#ECECEC ',
		},
		title : {text : 'Consumi in fascia verde'},
		xAxis : {
			tickInterval : 2,
			min : 1,
			max : 31,
			title : {
				align : 'high',
				text : 'Giorni',
				rotation : 0,
				offset : 15,
				style : {color : "black"}},
		},
		yAxis : {
			min : 0,
			title : {text : '% consumo per fascia'}
		},
		tooltip : {
			formatter : function() {
				return '' + this.series.name + ': ' + this.y + ' (' + Math.round(this.percentage) + '%)';
			}
		},
		plotOptions : {
			column : {stacking : 'percent'}
		},
		series : [ {
						name : 'fascia rossa',
						data : dataRosso
					}, {
						name : 'fascia verde',
						data : dataVerde
					} 
		]
	});*/
}

Report.VisCompensa = function() {

	$("#elettrodomDetail").empty();
	$("#elettrodomDetail").append($(document.createElement('p')).attr('height', '25%').css('color', 'green').text('COMPENSA CON'))
						  .append($(document.createElement('img')).attr('src', './Resources/Images/albero.png'))
						  .append($(document.createElement('img')).attr('src', './Resources/Images/albero.png'))
						  .append($(document.createElement('img')).attr('src', './Resources/Images/albero.png'))
						  .append($(document.createElement('img')).attr('src', './Resources/Images/albero.png'))
						  .append($(document.createElement('img')).attr('src', './Resources/Images/albero.png'))
						  .append($(document.createElement('img')).attr('src', './Resources/Images/albero.png'));

}

Report.VisUtilizzoGraf = function() {
	$("#elettrodomDetail").html(null);
}

/* Funzione di uscita dai contenuti di Report */
Report.Exit = function() {
	//console.log(80, Report.MODULE, "Report.Exit");

	$("#Report").hide();
	Main.ResetError();
	hideSpinner();
}

Report.getHagID = function(){
	
		var urlVars = Report.getUrlVars();
		Report.hag_id = 'hag-' + urlVars['hagID'];
}

Report.setDate = function(){
	
	var urlVars = Report.responseDataForAccordion;
	var startDate = urlVars['settPrecDate'][0][0] + ' ' + Msg.mesiCompleto[Math.floor(urlVars['settPrecDate'][0][1]) - 1];
	var endDate = urlVars['settPrecDate'][1][0] + ' ' + Msg.mesiCompleto[Math.floor(urlVars['settPrecDate'][1][1]) - 1];
	
	return Msg.report["report"] + startDate + Msg.report["reportAl"] + endDate;
}

Report.GetEldo = function(resEldo){
	
	Report.listaElettrodomestici = [];

	for (var iCounter in resEldo){
		var iconaEldo = resEldo[iCounter]['iconaEldo'];
		var icon = iconaEldo.split(".");
		iconaEldo = icon[0] + '_off' + '.png';
		var nomeEldo = resEldo[iCounter]['nomeEldo'];
		var idEldo = resEldo[iCounter]['idEldo'];
		var locationEldo = resEldo[iCounter]['locationEldo'];
		var categoryEldo = resEldo[iCounter]['categoryEldo'];

		if (resEldo[iCounter]['categoryEldo'] == null) {
			$('#carousell').append($(document.createElement('img')).attr('id', 'imgCrslHome').attr('class', 'cloudcarousel')
															   .attr('src', 'Resources/Images/menu/home_grigio.png').attr('alt', 'SmartInfo non attivo').attr('title', 'Casa')
															   .attr('width', '20%'));
		} else if (resEldo[iCounter]['categoryEldo'] == '12') {
			$('#carousell').append($(document.createElement('img')).attr('id', 'imgCrslHome').attr('class', 'cloudcarousel')
															   .attr('src', 'Resources/Images/menu/home_verde.png').attr('alt', idEldo).attr('title', 'Casa')
															   .attr('width', '20%'));
		} else {
			$('#carousell').append($(document.createElement('img')).attr('id', 'imgCrsl' + nomeEldo).attr('class', 'cloudcarousel')
															   .attr('src', 'Resources/Images/Devices/' + iconaEldo).attr('alt', idEldo)
															   .attr('title', nomeEldo).attr('width', '18%'))
		}
		var nomeCampo = InterfaceEnergyHome.ATTR_APP_NAME;
		var iconaCampo = InterfaceEnergyHome.ATTR_APP_ICON;
		var locationCampo = InterfaceEnergyHome.ATTR_APP_LOCATION;
		var categoryCampo = InterfaceEnergyHome.ATTR_APP_CATEGORY;
		Report.listaElettrodomestici[iCounter] = {nomeCampo: nomeEldo, iconaCampo: iconaEldo, locationCampo: locationEldo, categoryCampo: categoryEldo};
	}
	
	//Report.numEldo = (listEldo.length - 1);
	Report.numEldo = Math.floor(iCounter) + 1;
	
	Report.gestReportPage();
}

Report.getUrlVars = function(){
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}