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
												.width('100%').height('48%')))
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
	indiceAccordion: 0,
	numEldo : 7,
	carouselImgWidth: 0,
	carouselImgHeight: 0,
	listaElettrodomestici: null,
	responseDataForApplianceReport: null,
	responseDataForAccordion: null,
	responseDataForAutoConsumptionReport: null,
	hag_id: null,
	userPV: false,
	numWeekBack: 0,
	singleStart: false
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
		Report.getDatiHome();
	} else {
		$("#Report").show();
	}
	
	$("#ReportInfo").empty();
	$("#ReportInfo").html(Msg.report["informaReport"][0]);
	$("#paragrafoInfo").empty();
	$("#paragrafoInfo").html(Msg.report["informaReport"][0]);
	
	/* Per ultimo nascondo lo spinner */
	Report.gestWeekBackButton();
	hideSpinner();
}

Report.getDatiHome = function(){
	Report.getHagID();
	var numWeekBack = Report.numWeekBack;
		
	Report.responseDataForAccordion = ReportServiceDemo;

	var settPrecString = Report.setDate();
	$("#TitoloReport").html(settPrecString);

	Report.gestResponseReportService();
}

Report.gestWeekBackButton = function() {
	
	var stopBackWeek = false;
	
	if (Report.singleStart == false){
		$("#Report").append($(document.createElement('button')).attr('id', 'leftWeekBut').attr('class', 'carouselLeft').css('position', 'absolute').css('top', '7%').css('left', '5%'))
					.append($(document.createElement('button')).attr('id', 'rightWeekBut').attr('class', 'carouselRight').css('position', 'absolute').css('top', '7%').css('right', '5%'));
	}
	
	if (Report.numWeekBack <= 0){
		$("#rightWeekBut").hide();
	} else {
		$("#rightWeekBut").show();
	}
	
	if (Report.responseDataForAccordion != null){
		var urlVars = Report.responseDataForAccordion;
		if ((urlVars['settPrecDate'][0][0] == '01') && (urlVars['settPrecDate'][0][1] == '07') && (urlVars['settPrecDate'][1][2] == '2013')){
			stopBackWeek = true;
		}
	}
	
	
	if (stopBackWeek){
		$("#leftWeekBut").hide();
	} else {
		$("#leftWeekBut").show();
	}
	
	if (Report.singleStart == false){
		$("#leftWeekBut").button({text : false, icons : {primary : "ui-icon-seek-prev"}})
						 .click(function(){
						 	Report.numWeekBack++;
						 	Report.getDatiHome();
						 	Report.getDatiEldo();
						 	Report.getAutoConsumptionData();
						 	Report.VisIAC();
						 });
		$("#rightWeekBut").button({text : false, icons : {primary : "ui-icon-seek-next"}})
						 .click(function(){
						 	Report.numWeekBack--;
						 	Report.getDatiHome();
						 	Report.getDatiEldo();
						 	Report.getAutoConsumptionData();
						 	Report.VisIAC();
						 });
	}
}

Report.gestResponseReportService = function() {

	var objData = Report.responseDataForAccordion;
	var consumiFissiData = [];
	var tab2Data = [];
	var valueIAC = 'n.d.';
	
	Report.gestData(objData, consumiFissiData, tab2Data);

	if (Report.singleStart == false){
		/* Struttura del contenuto di un elemento dell'accordion */
		var riga = $(document.createElement('table'))
						.attr('class', 'accordion-table')
						.height('40%').css('min-height', '100%')
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
		$("#EventiCasa1").append($(document.createElement('div')).attr('id', 'Dati1'));
		$("#Dati1").append($(document.createElement('div')).attr('id', 'headerAccordion1').attr('class', 'headerAccordion')
						 								   .append($(document.createElement('a')).attr('href', '#').attr('id', 'headerAccordionTitle1')
											 										 							   .text(Msg.report["ConsumiFissi"] + " : ")
											 										 							   .css('color', 'white')
											 										 							   .append($(document.createElement('span')).html(consumiFissiData[3]))));
		$("#Dati1").append($(document.createElement('div')).attr('id', 'Dati11Content'));
								 							   	 
		$("#Dati1").append($(document.createElement('div')).attr('id', 'headerAccordion2').attr('class', 'headerAccordion')
								 							   			 								 .append($(document.createElement('a')).attr('href', '#').attr('id', 'headerAccordionTitle2')
								 							   			 										 							   .text(Msg.report["ConsumoFasciaEco"] + " : ")
						 												 										 							   .css('color', 'white')
								 							   			 										 							   .append($(document.createElement('span')).html(tab2Data[3] + " (" + tab2Data[4] + ")"))));
		$("#Dati1").append($(document.createElement('div')).attr('id', 'Dati12Content'));
		$("#Dati1").append($(document.createElement('div')).attr('id', 'headerAccordion3').attr('class', 'headerAccordion')
								 							    		 								 .append($(document.createElement('a')).attr('href', '#').attr('id', 'headerAccordionTitle3')
								 							    		 										 							   .text(Msg.report["IAC"] + " : ")
						 												 										 							   .css('color', 'white')
								 							    		 										 							   .append($(document.createElement('span')).text(valueIAC))));
		$("#Dati1").append($(document.createElement('div')).attr('id', 'Dati13Content'));
	
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
				Report.gestAccordion(ui, false);
			}
		});

		/* Imposto il contenuto su ogni elemento dell'accordion */
		if (Report.singleStart == false){
			$(".ui-accordion-content").append(riga);
		}
	} else {
		$('#headerAccordionTitle1 span').html(consumiFissiData[3]);
		$('#headerAccordionTitle2 span').html(tab2Data[3] + " (" + tab2Data[4] + ")");
		$('#headerAccordionTitle3 span').html(ReportSim.IAC);
	}
	
	$("#ReportDatiCasa table").map(function(indexTable, domElementSection) {
		$(domElementSection).find("tr")
			.map(function(indexRow, domElementRow) {
				$(domElementRow).children("td")
					.map(function(indexCell, domElementCell) {
						switch (indexRow) {
							case 0: {
								// Inserimento titoli
								$(domElementCell).html(Msg.report["TitoliAccordion"][indexTable][indexCell]);
								break;
							}
							case 1: {
								// Inserimento dati
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
	
	if (Report.singleStart == false){	

		var lblApp = 'a';
		var lblIcon = 'i';
		var lstEl = 7;
		
		var icone = ['plug.png', 'tv.png', 'lvb2.png', 'microonde.png', 'lampada.png', 'forno.png', 'frigorifero.png', 'plug.png'];
		
		var listEldoForRequest = [];
		
		for (var iCounter = 0; iCounter <= lstEl; iCounter++){
			var idEldo = 'ah.app.123456789123' + iCounter;
			var iconaEldo = icone[iCounter];
	
			listEldoForRequest[iCounter] = {'idEldo': idEldo, 'nomeEldo': null, 'categoryEldo': null, 'locationEldo': null, 'iconaEldo': iconaEldo};
		}
		
		
		        var tmpCounter = -1;
		        var listaEldo = ListaElettr1.list;
		        for (var el in listaEldo){
		        	var singleEldo = listaEldo[el].map;
	        		if (singleEldo['category_pid'] == '14'){
	        			Report.userPV = true;
	        		}
		        	for (var iCount in listEldoForRequest){
		        		if (listEldoForRequest[iCount]['idEldo'] == singleEldo['appliance_pid']){
		        			listEldoForRequest[iCount]['nomeEldo'] = singleEldo['name'];
		        			listEldoForRequest[iCount]['categoryEldo'] = singleEldo['category_pid'];
		        			listEldoForRequest[iCount]['locationEldo'] = singleEldo['location_pid'];
		        			if (listEldoForRequest[iCount]['categoryEldo'] == '14'){
		        				tmpCounter = iCount; 
		        			}
		        		}
		        	}
		        }
		        
		        if (tmpCounter >= 0){
		        	listEldoForRequest.splice(tmpCounter, 1);
		        }
		        
		        Report.GetEldo(listEldoForRequest);
		
		if (!Report.userPV){
			$('#Dati13Content').hide();
			$('#headerAccordion3').hide();
		} else {
			
			//$('#Dati11Content').hide();
			//$('#headerAccordion1').hide();
			//$('#Dati12Content').hide();
			//$('#headerAccordion2').hide();
		}
	}
}

Report.gestData = function(objData, consumiFissiData, tab2Data){
	var tmp;
	for (var el in objData){
		if (el == 'min_power_rank'){
			/*if (Report.userPV){
				consumiFissiData[0] = 'n.d.';
			} else*/ if ((objData[el] == null) || (objData[el] < 0)){
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
			/*if (Report.userPV){
				consumiFissiData[1] = 'n.d.';
			} else*/ if ((Math.floor(objData[el]) <= 5) || (objData[el] == null)){
				consumiFissiData[1] = 'n.d.';
			} else {
				tmp = roundTo(objData[el], 1);
				consumiFissiData[1] = tmp + ' W fissi';
				//consumiFissiData[1] = Math.floor(objData[el]) + ' Wh per ora';
			}
		}
		if (el == 'yearly_min_power_cost'){
			/*if (Report.userPV){
				consumiFissiData[2] = 'n.d.';
			} else*/ if ((Math.floor(objData[el]) <= 5) || (objData[el] == null)){
				consumiFissiData[2] = 'n.d.';
			} else {
				tmp = roundTo(objData[el], 1);
				consumiFissiData[2] = tmp + ' &euro;';
				//consumiFissiData[2] = Math.floor(objData[el]) + ' &euro;';
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
				tmp = roundTo(objData[el], 1);
				tab2Data[1] = tmp + ' %';
				//tab2Data[1] = Math.floor(objData[el]) + ' %';
			}
		}
		if (el == 'weekly_avg_f23_percentage'){
			if (Report.userPV){
				tab2Data[2] = 'n.d.';
			} else if ((Math.floor(objData[el]) <= 0) || (objData[el] == null)){
				tab2Data[2] = 'n.d.';
			} else {
				tmp = roundTo(objData[el], 1);
				tab2Data[2] = tmp + ' %';
				//tab2Data[2] = Math.floor(objData[el]) + ' %';
			}
		}

		if (el == 'weekly_min_power'){
			/*if (Report.userPV){
				consumiFissiData[3] = 'non disponibile';
			} else*/ if ((Math.floor(objData[el]) <= 5) || (objData[el] == null)){
				consumiFissiData[3] = 'n.d.';
			} else {
				tmp = objData[el];
				tmp1 = roundTo(tmp, 1);
				tmp = tmp * 24 * 7;
				tmp = tmp / 1000;
				tmp = roundTo(tmp, 1);
				consumiFissiData[3] = tmp + ' kWh' + ' (' + tmp1 + ' W fissi)';
				//consumiFissiData[3] = Math.floor(objData[el]) + ' Wh per ora';
			}
		}

		if (el == 'yearly_cost_forecast'){
			if ((Math.floor(objData[el]) <= 50) || (objData[el] == null)){
				tab2Data[3] = 'n.d.';
			} else {
				tmp = roundTo(objData[el], 1);
				tab2Data[3] = tmp + ' &euro;';
				//tab2Data[3] = Math.floor(objData[el]) + ' &euro;';
			}
		}

		if (el == 'yearly_consumption_forecast'){
			if ((Math.floor(objData[el]) <= 5) || (objData[el] == null)){
				tab2Data[4] = 'n.d.';
			} else {
				tmp = objData[el] / 1000;
				tmp = roundTo(tmp, 1);
				tab2Data[4] = tmp + ' kWh';
			}
		}
	}
}

Report.gestAccordion = function(ui, flg){
	if (!flg){
		indiceAccordion = $.inArray(ui.newHeader[0],$("#Dati1 .headerAccordion"));
	} else {
		indiceAccordion = 0;
	}
			
	switch (indiceAccordion) {
		case 0: {
			indiceAccordion = (Report.indiceCarosello != 0) ? indiceAccordion + 1 : indiceAccordion;
			Report.indiceAccordion = indiceAccordion;
			Report.VisConsumiFissiGraf();
			break;
		}
		case 1: {
			indiceAccordion += 1;
			Report.indiceAccordion = indiceAccordion;
			Report.VisFasciaGraf();
			break;
		}
		case 2: {
			indiceAccordion += 2;
			Report.indiceAccordion = indiceAccordion;
			Report.VisIAC();
			break;
		}
		default: {
			Report.indiceAccordion = indiceAccordion;
			Report.VisUtilizzoGraf();
			break;
		}
	}
	
	$("#ReportInfo").html(Msg.report["informaReport"][indiceAccordion]);
	$("#paragrafoInfo").html(Msg.report["informaReport"][indiceAccordion]);
}

Report.gestReportPage = function() {

	/** ***************************************************************************************************** */
	/* Configurazione del carosello */
	if (Report.singleStart == false){
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
			speed: 0.3 //0.3 se si vuol far ruotare DT  // 0 per stare fermo
		});
		/* Aggiungo il bordo all'immagine dell'oggetto selezionato nel carosello */
	
		$($("#carousell img")[Report.indiceCarosello]).addClass("selezionato");
	
		/* Configuro il bottone NEXT del carosello */
		$("#right-but").button({text : false, icons : {primary : "ui-icon-seek-next"}})
					   .click(function(){
						   Report.moveCarousel('right', false); //decommentare per far ruotare DT
					   });
		/* Configuro il bottone PREV del carosello */
		$("#left-but").button({text : false, icons : {primary : "ui-icon-seek-prev"}})
					  .click(function(){
						  Report.moveCarousel('left', false); //decommentare per far ruotare DT
					  });
		$("#carousell img").click(function(){
			var ccObj = $('#carousell').data(); //decommentare per far ruotare DT
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
				}, 500);
		});
		Report.VisConsumiFissiGraf();
	}
	Report.getAutoConsumptionData();
    Report.moveCarousel(null, true);
}

Report.getAutoConsumptionData = function(){
	
	
	        Report.responseDataForAutoConsumptionReport = autoConsumptionReportService;
	        
			Report.injectAutoconsuptionDataInAccordion();
	
}

Report.moveCarousel = function(click, bringToFront){

	$('#Dati1').accordion().children('.ui-accordion-header:eq(1)').click();
	
	var objData = Report.responseDataForAccordion;
	var consumiFissiData = [];
	var tab2Data = [];
	var reportSheetEldo = null;
	
	Report.gestData(objData, consumiFissiData, tab2Data);
	
	/* Elimino il bordo che indica l'oggetto selezionato */
	$("#carousell img").removeClass("selezionato");
	
	var isEldo = 0; // 0 = Casa, 1 = Eldo
	
	var nomeEldo = Report.listaElettrodomestici[Report.indiceCarosello]['nomeCampo']; 
	if (Report.listaElettrodomestici[Report.indiceCarosello]['categoryCampo'] == '12'){
		nomeEldo = 'Casa';
	}
	
	for (var el in Report.responseDataForApplianceReport){
		if (Report.responseDataForApplianceReport[el]['appliance_pid'] == Report.listaElettrodomestici[Report.indiceCarosello]['idCampo']){
			reportSheetEldo = Report.responseDataForApplianceReport[el];
		}
	}
	
	/* Il primo header cambia nel caso venga selezionato un elettrodomestico oppure la casa (==0) */
	if (Report.indiceCarosello != 0) {
		isEldo = 1;
		if (Report.userPV){
			Report.hideIACAccordion();
		}
		var arrEldoNotSupported = [6,7,8,9,11,17];  //6:"Forno",7:"Ferro da stiro",8:"Frigorifero",9:"Lavastoviglie",11:"Lavatrice",17:"Modem/Router",
		
		if (reportSheetEldo == null){
			reportSheetEldo = {};
			reportSheetEldo['yearly_standby_cost'] = 'n.p.';
			reportSheetEldo['weekly_standby_consumption'] = 'n.p.';
			reportSheetEldo['mean_daily_on_time'] = 'n.p.';
			reportSheetEldo['weekly_avg_min_power'] = 'n.p.';
			reportSheetEldo['f23_percentage'] = 'n.p.';
			reportSheetEldo['f23_rank'] = 'n.p.';
			reportSheetEldo['weekly_avg_f23_percentage'] = 'n.p.';
			reportSheetEldo['yearly_cost_forecast'] = 'n.p.';
		} else {
			reportSheetEldo['yearly_standby_cost'] = (reportSheetEldo['yearly_standby_cost'] == null) ? 'n.p.' : reportSheetEldo['yearly_standby_cost'];
			reportSheetEldo['weekly_standby_consumption'] = (reportSheetEldo['weekly_standby_consumption'] == null) ?  'n.p.' : reportSheetEldo['weekly_standby_consumption'];
			reportSheetEldo['mean_daily_on_time'] = (reportSheetEldo['mean_daily_on_time'] == null) ?  'n.p.' : reportSheetEldo['mean_daily_on_time'];
			reportSheetEldo['weekly_avg_min_power'] = (reportSheetEldo['weekly_avg_min_power'] == null) ?  'n.p.' : reportSheetEldo['weekly_avg_min_power'];
			reportSheetEldo['f23_percentage'] = (reportSheetEldo['f23_percentage'] == null) ?  'n.p.' : (Math.floor(reportSheetEldo['f23_percentage']*10)/10);
			reportSheetEldo['f23_rank'] = (reportSheetEldo['f23_rank'] == null) ?  'n.p.' : reportSheetEldo['f23_rank'];
			reportSheetEldo['weekly_avg_f23_percentage'] = (reportSheetEldo['weekly_avg_f23_percentage'] == null) ?  'n.p.' : (Math.floor(reportSheetEldo['weekly_avg_f23_percentage']*10)/10);
			reportSheetEldo['yearly_cost_forecast'] = (reportSheetEldo['yearly_cost_forecast'] == null) ?  'n.p.' : reportSheetEldo['yearly_cost_forecast'];
		}
		
		var indexFound = arrEldoNotSupported.indexOf(Report.listaElettrodomestici[Report.indiceCarosello]['categoryCampo']);
		if (indexFound >= 0){
			reportSheetEldo['yearly_standby_cost'] = 'n.d.';
			reportSheetEldo['weekly_standby_consumption'] = 'n.d.';
		}
		
		var header1 = $("#Dati1 #headerAccordion1 a")[0];
		$(header1).html(Msg.report["TempoEldo"] + " : " + " <span></span>");
		if (reportSheetEldo['mean_daily_on_time'] == 'n.p.'){
			$(header1).children("span").html(reportSheetEldo['mean_daily_on_time']);
			reportSheetEldo['mean_daily_on_time'] = null;
		} else {
			$(header1).children("span").html(reportSheetEldo['mean_daily_on_time'] + ' ore al giorno');
		}
		
		var header2 = $("#Dati1 #headerAccordion2 a")[0];
		$(header2).html(Msg.report["ConsumoFasciaEco"] + " : " + " <span></span>");
		if (reportSheetEldo['f23_percentage'] == 'n.p.'){
			$(header2).children("span").html(reportSheetEldo['f23_percentage']);
			reportSheetEldo['f23_percentage'] = null;
		} else {
			var f23_percentage = roundTo(reportSheetEldo['f23_percentage'], 1);
			$(header2).children("span").html(f23_percentage + '%');
		}
		
		/* Compilo il contenuto del primo header */
		$("#ReportDatiCasa table tr:nth-child(1)")
				.first().children("td").map(function(indexCell, domElementCell) {
					$(domElementCell).html(Msg.report["TitoliAccordion"][2][indexCell]);
		});
		$("#ReportDatiCasa table tr:nth-child(2)")
				.first().children("td").map(function(indexCell, domElementCell) {
					if(indexCell == 0){
						if (reportSheetEldo['weekly_avg_min_power'] == 'n.p.'){
							$(domElementCell).html(reportSheetEldo['weekly_avg_min_power']);
							reportSheetEldo['weekly_avg_min_power'] = null;
						} else {
							$(domElementCell).html(reportSheetEldo['weekly_avg_min_power'] + ' ore/giorno');
						}
					} else if(indexCell == 1){
						if (reportSheetEldo['weekly_standby_consumption'] == 'n.p.'){
							$(domElementCell).html(reportSheetEldo['weekly_standby_consumption']);
							reportSheetEldo['weekly_standby_consumption'] = null;
						} else {
							var weekly_standby_consumption = roundTo(reportSheetEldo['weekly_standby_consumption'], 1);
							$(domElementCell).html(weekly_standby_consumption + ' KWh');
						}
					} else if(indexCell == 2){
						if (reportSheetEldo['yearly_standby_cost'] == 'n.p.'){
							$(domElementCell).html(reportSheetEldo['yearly_standby_cost']);
							reportSheetEldo['yearly_standby_cost'] = null;
						} else {
							var yearly_standby_cost = roundTo(reportSheetEldo['yearly_standby_cost'], 1);
							$(domElementCell).html(yearly_standby_cost + ' &euro;');
						}
					}
				});
		
		/* Compilo il contenuto del secondo header */
		$("#Dati12Content table tr:nth-child(1)")
				.first().children("td").map(function(indexCell, domElementCell) {
					$(domElementCell).html(Msg.report["TitoliAccordion"][3][indexCell]);
		});
		$("#Dati12Content table tr:nth-child(2)")
				.first().children("td").map(function(indexCell, domElementCell) {
					if(indexCell == 0){
						if (reportSheetEldo['f23_rank'] == 'n.p.'){
							$(domElementCell).html(reportSheetEldo['f23_rank']);
							reportSheetEldo['f23_rank'] = null;
						} else {
							$(domElementCell).html(reportSheetEldo['f23_rank'] + '&#176;');
						}
					} else if(indexCell == 1){
						if (reportSheetEldo['weekly_avg_f23_percentage'] == 'n.p.'){
							$(domElementCell).html(reportSheetEldo['weekly_avg_f23_percentage']);
							reportSheetEldo['weekly_avg_f23_percentage'] = null;
						} else {
							var weekly_avg_f23_percentage = roundTo(reportSheetEldo['weekly_avg_f23_percentage'], 1);
							$(domElementCell).html(weekly_avg_f23_percentage + '%');
						}
					} else if(indexCell == 2){
						if (reportSheetEldo['yearly_cost_forecast'] == 'n.p.'){
							$(domElementCell).html(reportSheetEldo['yearly_cost_forecast']);
							reportSheetEldo['yearly_cost_forecast'] = null;
						} else {
							var yearly_cost_forecast = roundTo(reportSheetEldo['yearly_cost_forecast'], 1);
							$(domElementCell).html(yearly_cost_forecast + ' &euro;');
						}
					}
				});
	} else {
		
		isEldo = 0;
		if (Report.userPV){
			Report.showIACAccordion();
		}

		var header1 = $("#Dati1 #headerAccordion1 a")[0];
		$(header1).html(Msg.report["ConsumiFissi"] + " : " + " <span></span>");
		$(header1).children("span").append(consumiFissiData[3]);
		
		var header2 = $("#Dati1 #headerAccordion2 a")[0];
		$(header2).html(Msg.report["PrevisioneSpesaAnnua"] + " : " + " <span></span>");
		$(header2).children("span").html(tab2Data[3] + " (" + tab2Data[4] + ")");

		/* Compilo il contenuto del primo header */
		$("#ReportDatiCasa table tr:nth-child(1)")
				.first().children("td").map(function(indexCell, domElementCell) {
					$(domElementCell).html(Msg.report["TitoliAccordion"][0][indexCell]);
		});
		$("#ReportDatiCasa table tr:nth-child(2)")
				.first().children("td").map(function(indexCell, domElementCell) {
					$(domElementCell).html(consumiFissiData[indexCell]);});

		/* Compilo il contenuto del primo header */
		$("#Dati12Content table tr:nth-child(1)")
				.first().children("td").map(function(indexCell, domElementCell) {
					$(domElementCell).html(Msg.report["TitoliAccordion"][1][indexCell]);
		});
		$("#Dati12Content table tr:nth-child(2)")
				.first().children("td").map(function(indexCell, domElementCell) {
					$(domElementCell).html(tab2Data[indexCell]);});
	}
	
	/*
	if (Report.userPV){
		if (isEldo == 1){
			$('#headerAccordion2').click();
		} else {
			$('#headerAccordion3').click();
		}
		Report.injectAutoconsuptionDataInAccordion();
	} else {
		if (isEldo == 1){
			$('#headerAccordion2').click();
		}
	}
	*/
		
	if (Report.userPV){
		if (isEldo == 1){
			$('#Dati1').accordion().children('.ui-accordion-header:eq(0)').hide();
			$('#Dati1').accordion().children('.ui-accordion-header:eq(1)').show();
			$('#Dati1').accordion().children('.ui-accordion-header:eq(2)').hide();
			$('#headerAccordion2').click();
		} else {
			
			$('#Dati1').accordion().children('.ui-accordion-header:eq(0)').show();
			$('#Dati1').accordion().children('.ui-accordion-header:eq(1)').show();
			$('#Dati1').accordion().children('.ui-accordion-header:eq(2)').show();
			$('#headerAccordion3').click();
		}
		Report.injectAutoconsuptionDataInAccordion();
	} else {
		if (isEldo == 1){
			
			$('#Dati1').accordion().children('.ui-accordion-header:eq(0)').hide();
			$('#Dati1').accordion().children('.ui-accordion-header:eq(1)').show();
			$('#headerAccordion2').click();
		} else {
			
			$('#Dati1').accordion().children('.ui-accordion-header:eq(0)').show();
			$('#Dati1').accordion().children('.ui-accordion-header:eq(1)').show();
			$('#headerAccordion1').click();
		}
	}
	
	
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
		
	if ((Report.indiceAccordion == 0) || (Report.indiceAccordion == 1)){
		if (Report.indiceCarosello == 0){
			$("#ReportInfo").html(Msg.report["informaReport"][0]);
		} else {
			$("#ReportInfo").html(Msg.report["informaReport"][1]);
		}		
		//$("#ReportInfo").html(Msg.report["informaReport"][isEldo]);
		//$("#paragrafoInfo").html(Msg.report["informaReport"][isEldo]);
	}
	
	Report.singleStart = true;
	
	Report.gestWeekBackButton();
}

Report.injectAutoconsuptionDataInAccordion = function(){
	if ((Report.responseDataForAutoConsumptionReport == null) || (
		(Report.responseDataForAutoConsumptionReport[0].autocons_rate == null) && 
		(Report.responseDataForAutoConsumptionReport[0].autocons_rank == null) && 
		(Report.responseDataForAutoConsumptionReport[0].weekly_mean_autoconsumption_rate == null) && 
		(Report.responseDataForAutoConsumptionReport[0].autocons_energy == null)
	)){
		$("#headerAccordionTitle3 span").html('n.d.');
		$("#Dati13Content table").map(function(indexTable, domElementSection) {
			$(domElementSection).find("tr")
				.map(function(indexRow, domElementRow) {
					$(domElementRow).children("td")
						.map(function(indexCell, domElementCell) {
							switch (indexRow) {
								case 0: {
									/* Inserimento titoli */
									$(domElementCell).html(Msg.report["TitoliAccordion"][4][indexCell]);
									break;
								}
								case 1: {
									/* Inserimento dati */
									if (indexCell == 0){
										$(domElementCell).html('n.d.');
									} else if (indexCell == 1){
										$(domElementCell).html('n.d.');
									} else { 
										$(domElementCell).html('n.d.');
									}
								}
							}
						});
	
				});
	
		});
	} else {
		if (Report.responseDataForAutoConsumptionReport[0].autocons_rate == null){
	    	$("#headerAccordionTitle3 span").html('n.p.');
	    } else {
			var autocons_rate = roundTo(Report.responseDataForAutoConsumptionReport[0].autocons_rate, 1);
	    	$("#headerAccordionTitle3 span").html(autocons_rate + '%');
	    	$("#Dati13Content table").map(function(indexTable, domElementSection) {
				$(domElementSection).find("tr")
					.map(function(indexRow, domElementRow) {
						$(domElementRow).children("td")
							.map(function(indexCell, domElementCell) {
								switch (indexRow) {
									case 0: {
										/* Inserimento titoli */
										$(domElementCell).html(Msg.report["TitoliAccordion"][4][indexCell]);
										break;
									}
									case 1: {
										/* Inserimento dati */
										if (indexCell == 0){
											$(domElementCell).html(Report.responseDataForAutoConsumptionReport[0].autocons_rank + '&#176;'); 
										} else if (indexCell == 1){
											var weekly_mean_autoconsumption_rate = roundTo(Report.responseDataForAutoConsumptionReport[0].weekly_mean_autoconsumption_rate, 1);
											$(domElementCell).html(weekly_mean_autoconsumption_rate + '%'); 
										} else { 
											var autocons_energy = roundTo(Report.responseDataForAutoConsumptionReport[0].autocons_energy, 1);
											$(domElementCell).html(autocons_energy + ' kWh');
										}
									}
								}
							});
		
					});
		
			});
	    }
	}
	
	/*if (Report.singleStart){
		if (Report.userPV){
			$('#headerAccordion3').click();
		}
	}*/
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

/*Report.VisCompensa = function() {

	$("#elettrodomDetail").empty();
	$("#elettrodomDetail").append($(document.createElement('p')).attr('height', '25%').css('color', 'green').text('COMPENSA CON'))
						  .append($(document.createElement('img')).attr('src', './Resources/Images/albero.png'))
						  .append($(document.createElement('img')).attr('src', './Resources/Images/albero.png'))
						  .append($(document.createElement('img')).attr('src', './Resources/Images/albero.png'))
						  .append($(document.createElement('img')).attr('src', './Resources/Images/albero.png'))
						  .append($(document.createElement('img')).attr('src', './Resources/Images/albero.png'))
						  .append($(document.createElement('img')).attr('src', './Resources/Images/albero.png'));

}*/

Report.VisIAC = function() {
	
	var idxInfoReport = 0;
	
	        
	        var arrEnergyBalance = energyBalanceReportService[0];
	        
	        var prod_energy = (arrEnergyBalance.prod_energy != null) ? roundTo(arrEnergyBalance.prod_energy, 1) + ' KWh': 'n.d.';
	        var sold_energy = (arrEnergyBalance.sold_energy != null) ? roundTo(arrEnergyBalance.sold_energy, 1) + ' KWh': 'n.d.';
	        var autocons_energy = (arrEnergyBalance.autocons_energy != null) ? roundTo(arrEnergyBalance.autocons_energy, 1) + ' KWh': 'n.d.';
	        var buy_energy = (arrEnergyBalance.buy_energy != null) ? roundTo(arrEnergyBalance.buy_energy, 1) + ' KWh': 'n.d.';
	        var used_energy = (arrEnergyBalance.used_energy != null) ? roundTo(arrEnergyBalance.used_energy, 1) + ' KWh': 'n.d.';
	        var perc_iac = (arrEnergyBalance.prod_energy != null) ? roundTo((arrEnergyBalance.autocons_energy / arrEnergyBalance.prod_energy)*100, 1) + '%': 'n.d.';
	        var perc_asuff = (arrEnergyBalance.used_energy != null) ? roundTo((arrEnergyBalance.autocons_energy / arrEnergyBalance.used_energy)*100, 1) + '%': 'n.d.';
	        
	        var observed_hours  = arrEnergyBalance.observed_hours * 1;
	        
	        var txtErrMsgEnergyBalance = (observed_hours < 96) ? 'Dati storici mancanti' : (observed_hours < 156) ? 'Dati storici non completi' : '';

	        $("#elettrodomDetail").empty(); 
			$("#elettrodomDetail").append($(document.createElement('div')).attr('height', '50px').css('color', 'green').text('BILANCIO ENERGETICO SETTIMANALE'))
								  .append($(document.createElement('div')).attr('height', '50px').css('color', 'red').text(txtErrMsgEnergyBalance))
								  .append($(document.createElement('table')).attr('id', 'IACTable').attr('width', '90%').css('border-radius', '20px').css('border', '2px solid #3a9948')
															  .append($(document.createElement('tr')).attr('width', '100%').css('background-color', '#3a9948').css('color', 'white').attr('height', '20px')
															  						.append($(document.createElement('td')).attr('width', '65%').text(Msg.report.bilancioEnergetico[0]))
															  						.append($(document.createElement('td')).attr('width', '35%').text(sold_energy))) 
															  .append($(document.createElement('tr')).attr('width', '100%').css('background-color', '#9ec200').css('color', 'white').attr('height', '20px')
															  						.append($(document.createElement('td')).attr('width', '65%').text(Msg.report.bilancioEnergetico[1]))
															  						.append($(document.createElement('td')).attr('width', '35%').text(autocons_energy)))
															  .append($(document.createElement('tr')).attr('width', '100%').css('background-color', '#3a9948').css('color', 'white').attr('height', '20px')
															  						.append($(document.createElement('td')).attr('width', '65%').text(Msg.report.bilancioEnergetico[2]))
															  						.append($(document.createElement('td')).attr('width', '35%').text(prod_energy)))
															  .append($(document.createElement('tr')).attr('width', '100%').css('background-color', '#9ec200').css('color', 'white').attr('height', '20px')
															  						.append($(document.createElement('td')).attr('width', '65%').text(Msg.report.bilancioEnergetico[3]))
															  						.append($(document.createElement('td')).attr('width', '35%').text(used_energy)))
															  .append($(document.createElement('tr')).attr('width', '100%').css('background-color', '#3a9948').css('color', 'white').attr('height', '20px')
															  						.append($(document.createElement('td')).attr('width', '65%').text(Msg.report.bilancioEnergetico[4]))
															  						.append($(document.createElement('td')).attr('width', '35%').text(buy_energy)))
															  .append($(document.createElement('tr')).attr('width', '100%').css('background-color', '#9ec200').css('color', 'white').attr('height', '20px')
															  						.append($(document.createElement('td')).attr('width', '65%').text(Msg.report.bilancioEnergetico[5]))
															  						.append($(document.createElement('td')).attr('width', '35%').text(perc_iac)))
															  .append($(document.createElement('tr')).attr('width', '100%').css('background-color', '#3a9948').css('color', 'white').attr('height', '20px')
												  									.append($(document.createElement('td')).attr('width', '65%').text(Msg.report.bilancioEnergetico[6]))
												  									.append($(document.createElement('td')).attr('width', '35%').text(perc_asuff))));
															  						
			
			switch (Report.indiceAccordion) {
				case 0: {
					Report.VisConsumiFissiGraf();
					if (Report.indiceCarosello == 0){
						idxInfoReport = 0;
					} else {
						idxInfoReport = 1;
					}
					$("#ReportInfo").html(Msg.report["informaReport"][idxInfoReport]);
					$("#paragrafoInfo").html(Msg.report["informaReport"][idxInfoReport]);
					break;
				}
				case 1: {
					Report.VisConsumiFissiGraf();
					if (Report.indiceCarosello == 0){
						idxInfoReport = 0;
					} else {
						idxInfoReport = 1;
					}
					$("#ReportInfo").html(Msg.report["informaReport"][idxInfoReport]);
					$("#paragrafoInfo").html(Msg.report["informaReport"][idxInfoReport]);
					break;
				}
				case 2: {
					Report.VisFasciaGraf();
					idxInfoReport = 2;
					$("#ReportInfo").html(Msg.report["informaReport"][idxInfoReport]);
					$("#paragrafoInfo").html(Msg.report["informaReport"][idxInfoReport]);
					break;
				}
				case 4: {
					
					idxInfoReport = 4;
					$("#ReportInfo").html(Msg.report["informaReport"][idxInfoReport]);
					$("#paragrafoInfo").html(Msg.report["informaReport"][idxInfoReport]);
					break;
				}
				default: {
					alert('errore!');
				}
			}
}

Report.hideIACAccordion = function(){
	
	$('#Dati1').accordion().children('.ui-accordion-header:eq(2)').hide();
			
	$("#Dati1").accordion( "resize" );
	if (Report.indiceAccordion > 2){
		$("#Dati1").accordion( "activate", 0 );
	
		Report.gestAccordion(null, true);
	}
}

Report.showIACAccordion = function(){
	
	$('#headerAccordion3').show();
	$('#Dati1').accordion().children('.ui-accordion-header:eq(2)').show();
	$("#Dati1").accordion( "resize" );
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
	
		//var urlVars = Report.getUrlVars();
		//Report.hag_id = 'hag-' + urlVars['hagID'];
	Report.hag_id = 'hag-0006';
}

Report.setDate = function(){
	
	var urlVars = Report.responseDataForAccordion;
	var startDate = urlVars['settPrecDate'][0][0] + ' ' + Msg.mesiCompleto[Math.floor(urlVars['settPrecDate'][0][1]) - 1] + ' ' + urlVars['settPrecDate'][1][2];
	var endDate = urlVars['settPrecDate'][1][0] + ' ' + Msg.mesiCompleto[Math.floor(urlVars['settPrecDate'][1][1]) - 1] + ' ' + urlVars['settPrecDate'][1][2];
	
	return Msg.report["report"] + startDate + Msg.report["reportAl"] + endDate;
}

Report.GetEldo = function(resEldo){
	
	Report.listaElettrodomestici = [];
	var iCounter;

	for (iCounter in resEldo){
		var iconaEldo = resEldo[iCounter]['iconaEldo'];
		var icon = iconaEldo.split(".");
		//iconaEldo = icon[0] + '_off' + '.png';  //Serve solo a fare il render dell'icona in grigio [inattivo]
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
			if (resEldo[iCounter]['categoryEldo'] != '14'){
				$('#carousell').append($(document.createElement('img')).attr('id', 'imgCrsl' + nomeEldo).attr('class', 'cloudcarousel')
															   		   .attr('src', 'Resources/Images/Devices/' + iconaEldo).attr('alt', idEldo)
															   		   .attr('title', nomeEldo).attr('width', '18%'));
															   
    		}
		}
		if (resEldo[iCounter]['categoryEldo'] != '14'){
			var nomeCampo = InterfaceEnergyHome.ATTR_APP_NAME;
			var idCampo = InterfaceEnergyHome.ATTR_APP_PID;
			var iconaCampo = InterfaceEnergyHome.ATTR_APP_ICON;
			var locationCampo = InterfaceEnergyHome.ATTR_APP_LOCATION;
			var categoryCampo = InterfaceEnergyHome.ATTR_APP_CATEGORY;
			Report.listaElettrodomestici[iCounter] = {nomeCampo: nomeEldo, idCampo: idEldo, iconaCampo: iconaEldo, locationCampo: locationEldo, categoryCampo: categoryEldo};
		}
	}
	
	//Report.numEldo = (listEldo.length - 1);
	Report.numEldo = Math.floor(iCounter) + 1;
	Report.getDatiEldo();
}
	
Report.getDatiEldo = function(){
	
	var numWeekBack = Report.numWeekBack;
	var numE;
	if (Report.userPV){
		 numE = Report.numEldo - 2;
	} else {
		numE = Report.numEldo - 1;
	}
		
	
	        Report.responseDataForApplianceReport = applianceReportServiceDemo;

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