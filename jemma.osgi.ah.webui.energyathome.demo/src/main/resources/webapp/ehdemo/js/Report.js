var Report = {
	MODULE : "Report",
	
	htmlContent : $(document.createElement('div'))
						.attr('id', 'Report')
						.attr('class', 'Content')
						.append($(document.createElement('div'))
									.attr('id', 'TitoloReport')
									.attr('class', 'ContentTitle')
									.css('text-align', 'center')
									.text(Msg.report["report"]))
						.append($(document.createElement('div'))
									.attr('id', 'ReportDatiCasa')
									.attr('class', 'ReportBGND')
									.append($(document.createElement('div'))
												.attr('id', 'TitoloEventiCasa')
												.attr('class', 'TitoloDettaglio')
												.text(Msg.report["TitoloEventiCasa"])
												.append($(document.createElement('span'))
															.text(Msg.report["carosello"][0])))
									.append($(document.createElement('div'))
												.attr('id', 'carousell')
												.width('100%').height('55%')))
						.append($(document.createElement('div')).attr('id', 'ReportDatiDettaglio')
																.attr('class', 'ReportBGND').css('left', '50%')
																.append($(document.createElement('div'))
																		.attr('id', 'TitoloElettrodomDettaglio')
																		.attr('class', 'TitoloDettaglio')
																		.text(Msg.report["TitoloElettrodomDettaglio"]))
																.append($(document.createElement('div'))
																		.attr('id', 'ReportInfo')
																		.text(Msg.report["TitoloElettrodomDettaglio"])
																		.append($(document.createElement('p')).attr('id', 'paragrafoInfo')
																											  .css('color', 'white').css('padding', '1%')))
																.append($(document.createElement('div')).attr('id', 'elettrodomDetail'))),
	indiceCarosello : 0,
	numEldo : 7,
	carouselImgWidth: 0,
	carouselImgHeight: 0,
}

Report.Init = function() {

	Highcharts.setOptions({
		colors : [ 'red', "#9EC200", '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4' ]
	});

	/* Creo il contenitore dei dati di report per la Gui e lo aggiungo alla pagina */
	var divReport = $("#Report");

	/* Controllo che il div di Report non sia gi� stato riempito. Se non esiste lo inizializzo, se gi� esiste lo visualizzo solamente */
	if (divReport.length == 0) {
		$("#Container").append(Report.htmlContent);
		
		Report.GetElettrodomestici();

		/* Struttura del contenuto di un elemento dell'accordion */
		
		var riga = $(document.createElement('table'))
						.attr('class', 'accordion-table')
						.height('30%')
						.append($(document.createElement('tbody'))
								.append($(document.createElement('tr'))
										.append($(document.createElement('td')))
										.append($(document.createElement('td')))
										.append($(document.createElement('td'))))
								.append($(document.createElement('tr'))
										.append($(document.createElement('td')).attr('id', 'posizioneclassifica').attr('class', 'altro'))
										.append($(document.createElement('td')).attr('id', 'mediacommunity').attr('class', 'secondo'))
										.append($(document.createElement('td')).attr('id', 'previsione').attr('class', 'altro'))));
		

		/* Aggiungo il Div che conterr� l'accordion */
		$(document.createElement('div')).attr('id', 'EventiCasa1').appendTo($("#ReportDatiCasa"));

		/* Imposto il contenuto dell'accordion */
		$("#EventiCasa1").append($(document.createElement('div')).attr('id', 'Dati1')
						 										 .append($(document.createElement('div')).attr('id', 'headerAccordion1').attr('class', 'headerAccordion')
						 												 								 .append($(document.createElement('a')).attr('href', '#')
						 												 										 							   .text(Msg.report["ConsumiFissi"] + " : ")
						 												 										 							   .append($(document.createElement('span')).text(ReportSim.consumiFissi))))
								 							   	 .append($(document.createElement('div')).attr('id', 'Dati11Content').height(100))
								 							   	 .append($(document.createElement('div')).attr('id', 'headerAccordion2').attr('class', 'headerAccordion')
								 							   			 								 .append($(document.createElement('a')).attr('href', '#')
								 							   			 										 							   .text(Msg.report["ConsumoFasciaEco"] + " : ")
								 							   			 										 							   .append($(document.createElement('span')).text(ReportSim.consumoAnno))))
								 							     .append($(document.createElement('div')).attr('id', 'Dati12Content').height(100))
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
									$(domElementCell).append(ReportSim.DatiSim[indexTable + 1][indexCell]);
								}
							}
						});

				});

		});

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
			speed: 0.3
		});
		/* Aggiungo il bordo all'immagine dell'oggetto selezionato nel carosello */

		$($("#carousell img")[Report.indiceCarosello]).addClass("selezionato");

		/* Configuro il bottone NEXT del carosello */
		$("#right-but").button({text : false, icons : {primary : "ui-icon-seek-next"}})
					   .click(function(){
						   Report.moveCarousel('right', false);
					   });
		/* Configuro il bottone PREV del carosello */
		$("#left-but").button({text : false, icons : {primary : "ui-icon-seek-prev"}})
					  .click(function(){
						  Report.moveCarousel('left', false);
					  });
		$("#carousell img").click(function(){
			var ccObj = $('#carousell').data();
			var cloudCarousellObj = ccObj.cloudcarousel;
			
				this.controlTimer = setTimeout( function(){
					var elettrIndex = 0;
					if (cloudCarousellObj.frontIndex != Report.indiceCarosello){
					
						if (cloudCarousellObj.frontIndex < 0){
							Report.indiceCarosello = (Report.numEldo) + cloudCarousellObj.frontIndex;
							Report.moveCarousel('left', true);
						} else {
							Report.indiceCarosello = cloudCarousellObj.frontIndex;
							Report.moveCarousel('right', true);
						}
						//$($("#carousell img")).removeClass("selezionato");
						//$($("#carousell img")[elettrIndex]).addClass("selezionato");	
					}
				}, 1000);
		});
		Report.VisConsumiFissiGraf();
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

Report.moveCarousel = function(click, bringToFront){
	/* Elimino il bordo che indica l'oggetto selezionato */
	$("#carousell img").removeClass("selezionato");

	/* Il primo header cambia nel caso venga selezionato un elettrodomestico oppure la casa */
	if (Report.indiceCarosello == 0) {
		var header1 = $("#Dati1 #headerAccordion a")[0];
		$(header1).html(Msg.report["Eldo"] + "<span></span>");
		$(header1).children("span").append(ReportSim.Eldo);
		/* Compilo il contenuto del primo header */
		$("#ReportDatiCasa table tr:nth-child(1)")
				.first().children("td").map(function(indexCell, domElementCell) {
					$(domElementCell).html(Msg.report["TitoliAccordion"][0][indexCell]);});
		$("#ReportDatiCasa table tr:nth-child(2)")
				.first().children("td").map(function(indexCell, domElementCell) {
					$(domElementCell).html(ReportSim.DatiSim[0][indexCell]);});
	}
	var tmpCheck = 0;
	if (bringToFront){
		tmpCheck = Report.indiceCarosello;
	} else {
		if (click == 'right'){
			tmpCheck = 1;
		} else {
			tmpCheck = Report.numEldo-1;
		}
	}
		
	if (Report.indiceCarosello == tmpCheck) {
		var header1 = $("#Dati1 #headerAccordion a")[0];
		$(header1).html(Msg.report["ConsumiFissi"] + "<span></span>");
		$(header1).children("span").append(ReportSim.consumiFissi);

		/* Compilo il contenuto del primo header */
		$("#ReportDatiCasa table tr:nth-child(1)")
				.first().children("td").map(function(indexCell, domElementCell) {
					$(domElementCell).html(Msg.report["TitoliAccordion"][1][indexCell]);});
		$("#ReportDatiCasa table tr:nth-child(2)")
				.first().children("td").map(function(indexCell, domElementCell) {
					$(domElementCell).html(ReportSim.DatiSim[1][indexCell]);});
	}
	/* Compilo i contenuti degli altri due header */
	$("#ReportDatiCasa table tr:odd:gt(0)").map(function(indexTable, ElementRow) {
					$(ElementRow).children("td")
						.map(function( indexCell, domElementCell) {
							$(domElementCell).html(ReportSim.DatiSim[indexTable + 2][indexCell]);});});
	
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
	$("#TitoloEventiCasa span").html(Msg.report["carosello"][Report.indiceCarosello]);
	$("#TitoloElettrodomDettaglio span").html(Msg.report["carosello"][Report.indiceCarosello]);
}

/* Funzione per la visualizzazione del grafico dei consumi fissi */
Report.VisConsumiFissiGraf = function() {

	$("#elettrodomDetail").html(null);
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
			});
}

/* Funzione per la visualizzazione del grafico dei consumi in fascia verde */
Report.VisFasciaGraf = function() {

	$("#elettrodomDetail").empty();

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

	//console.log(dataRosso);
	//console.log(dataVerde);
	//console.log(data);
	
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
				console.log(this);
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
	});
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

Report.GetElettrodomestici = function() {
	if (Main.env == 0) console.log('Report.js', 'GetElettrodomestici', 'Entro!');

	if (InterfaceEnergyHome.mode > 0) {
		try {
			InterfaceEnergyHome.objService.getAppliancesConfigurationsDemo(Report.DatiElettrodomesticiCB);
		} catch (err) {
			//if (Main.env == 0) console.log('exception in Report.js - in Report.GetElettrodomestici method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetMaxElettr", err);
		}
	} else {
		// per test
		var val;
		var indLista = 0;
		if (indLista == 0) {
			val = ListaElettr1;
			indLista = 1;
		} else {
			val = ListaElettr1;
			indLista = 0;
		}
		Report.DatiElettrodomesticiCB(val, null);
	}

	if (Main.env == 0) console.log('Report.js', 'GetElettrodomestici', 'Esco!');
}

Report.DatiElettrodomesticiCB = function(result, err) {
	if (Main.env == 0) console.log('Report.js', 'DatiElettrodomesticiCB', 'Entro!');

	var listEldo = {};
	//CostiConsumi.popSemaphoro('Report.GetElettrodomestici', 1);
	
	if (err != null){
		//if (Main.env == 0) console.log('exception in Report.js - in Report.DatiElettrodomesticiCB method: ', err);
		InterfaceEnergyHome.GestErrorEH("DatiElettrodomestici", err);
	}
	if ((err == null) && (result != null)) {
		$.each(result.list,function(indice, elettrodom) {
			if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.SMARTINFO_APP_TYPE) {
				if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE] == undefined) {
					elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE] = {};
					elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value = {value : 0};
				} else {
					var val = parseFloat(elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value);
					elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value = val;
				}
				//listEldo.home = elettrodom["map"];
				//if (Main.env == 0) console.log('COSTICONSUMI1', 'SmartInfo - ');
				//if (Main.env == 0) console.log(CostiConsumi.SmartInfo);
			} else {
				if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE] == undefined){
					elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value = 0;
				} else {
					var val = parseFloat(elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value);
					elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value = val;
				}
				//if (Main.env == 0) console.log('COSTICONSUMI1', 'Eldo - ');
				//if (Main.env == 0) console.log(CostiConsumi.listaElettr[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]]);
				//if (Main.env == 0) console.log(elettrodom["map"]);
			}
			listEldo[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
		});
	}
	
	Report.drawEldo(listEldo);
	if (Main.env == 0) console.log('Report.js', 'DatiElettrodomesticiCB', 'Esco!');
}

Report.drawEldo = function(listEldo){
	if (Main.env == 0) console.log('Report.js', 'drawEldo', 'Entro!');

	console.log(listEldo);
	for (element in listEldo){
		if(listEldo[element][InterfaceEnergyHome.ATTR_APP_CATEGORY] == "12"){
			$('#carousell').append($(document.createElement('img')).attr('id', 'imgCrslHome').attr('class', 'cloudcarousel')
																   .attr('src', 'Resources/Images/menu/home_verde.png').attr('alt', 'Casa').attr('title', 'Casa')
																   .attr('width', '20%'));
		} else {
			$('#carousell').append($(document.createElement('img')).attr('id', 'imgCrsl' + listEldo[element][InterfaceEnergyHome.ATTR_APP_NAME]).attr('class', 'cloudcarousel')
																   .attr('src', 'Resources/Images/Devices/' + listEldo[element][InterfaceEnergyHome.ATTR_APP_ICON]).attr('alt', listEldo[element][InterfaceEnergyHome.ATTR_APP_NAME])
																   .attr('title', listEldo[element][InterfaceEnergyHome.ATTR_APP_NAME]).attr('width', '18%'))
		}
	}
	/*
				   .append($(document.createElement('img')).attr('id', 'imgCrslForno').attr('class', 'cloudcarousel')
						   .attr('src', 'Resources/Images/carosello/fornoC.png')
						   .attr('alt', 'Forno').attr('title', 'Forno').attr('width', '18%'))
				   .append($(document.createElement('img')).attr('id', 'imgCrslRefr').attr('class', 'cloudcarousel')
						   .attr('src', 'Resources/Images/carosello/frigorifero_carosello.png')
						   .attr('alt', 'Frigo').attr('title', 'Frigo').attr('width', '18%'))
				   .append($(document.createElement('img')).attr('id', 'imgCrslPC').attr('class', 'cloudcarousel')
								.attr('src', 'Resources/Images/carosello/pczone_carosello.png')
								.attr('alt', 'PC').attr('title', 'PC').attr('width', '18%'))
				   .append($(document.createElement('img')).attr('id', 'imgCrslLamp').attr('class', 'cloudcarousel')
								.attr('src', 'Resources/Images/carosello/lampada_carosello.png')
								.attr('alt', 'Lampada').attr('title', 'Lampada').attr('width', '18%'))
				   .append($(document.createElement('img')).attr('id', 'imgCrslTV').attr('class', 'cloudcarousel')
								.attr('src', 'Resources/Images/carosello/tv_carosello.png')
								.attr('alt', 'Smart TV').attr('title', 'Smart TV').attr('width', '18%'))
				   .append($(document.createElement('img')).attr('id', 'imgCrslWash').attr('class', 'cloudcarousel')
								.attr('src', 'Resources/Images/carosello/lavatrice_carosello.png')
								.attr('alt', 'Lavatrice').attr('title', 'Lavatrice').attr('width', '18%'))
	*/
	$('#carousell').append($(document.createElement('button')).attr('id', 'left-but').attr('class', 'carouselLeft').css('position', 'absolute').css('top', '5%').css('left', '5%'));
	$('#carousell').append($(document.createElement('button')).attr('id', 'right-but').attr('class', 'carouselRight').css('position', 'absolute').css('top', '5%').css('left', '85%'));
	
	if (Main.env == 0) console.log('Report.js', 'drawEldo', 'Esco!');
}