var Storico = {
	MODULE : "Storico",
	installData : null,
	primaData : null,
	ultimaData : null,
	dataInizio : null,
	dataFine : null,
	dispositivi : null,
	tipoUltimoPeriodo : null,
	periodoScelto : null,
	dispositivoScelto : null,
	datiCostoNulli : false,
	datiConsumoNulli : false,
	enableResize : true,
	plot1 : null,
	datiElettr : null,
	datiCosto : null,
	datiConsumo : null,
	datiProduzione : null,
	titoloGraph : null,
	COSTI_CONSUMI : 0,
	SOLO_CONSUMI : 1,
	GIORNO : 0,
	SETTIMANA : 1,
	MESE : 2,
	ANNO : 3,
	LIMITALERTGIORNO: 24, 
	LIMITALERTSETTIMANA: 7, 
	LIMITALERTMESE: 31, 
	LIMITALERTANNO: 12, 
	LIMITPARAM: 0.3,

	htmlContent : "<div class='ContentTitle'>" + Msg.home["titoloStorico"] + "</div>"
			+ "<div id='Storico'>"
			+ "		<img id='StoricoBg' src='" + Define.home["sfondo_sx"] + "'>"
			+ "		<div id='MsgStorico' style='display:none'>" + Msg.home["datiMancanti"] + "</div>"
			+ "		<div id='LabelStoricokWh'>kWh</div>"
			+ "		<div id='LabelStoricoEuro'>€</div>"
			+ "		<div id='Prec'>"
			+ "			<img id='PrecImg' src='" + Define.home["frecciaPrec"] + "'><div id='LabelPrec'></div>"
			+ "		</div>"
			+ "		<div id='Succ'>"
			+ "			<img id='SuccImg' src='" + Define.home["frecciaSucc"] + "'><div id='LabelSucc'></div>"
			+ "		</div>"
			+ "		<div id='StoricoGraphContainer'><div id='StoricoGraph'></div></div>"
			+ "		<div id='SepStorico'></div>"
			+ "		<div id='StoricoScelta'>"
			+ "			<div id='TitoloSceltaPeriodo'>" + Msg.home["periodoStorico"] + "</div>"
			+ "			<div id='SceltaPeriodo'>"
			+ "				<input class='ButtonScelta' name='Periodo' type='radio' checked='checked' value='0'>" + Msg.home["giornoStorico"] + "<br>"
			+ "				<input class='ButtonScelta' name='Periodo' type='radio' value='1'>" + Msg.home["settStorico"] + "<br>"
			+ "				<input class='ButtonScelta' name='Periodo' type='radio' value='2'>" + Msg.home["meseStorico"] + "<br>"
			+ "				<input class='ButtonScelta' name='Periodo' type='radio' value='3'>" + Msg.home["annoStorico"]
			+ "			</div>"
			+ "			<div id='TitoloSceltaDispositivo'>" + Msg.home["dispStorico"]+ "</div>"
			+ "			<div id='SceltaDispositivo'></div>" 
			+ "		</div>"
			+ "</div>"

};

Storico.ExitStorico = function() {
	//hideSpinner();
	Main.ResetError();
	Storico.datiCosto = null;
	Storico.datiConsumo = null;
	if (Main.env == 0) console.log(20, Storico.MODULE, "ExitStorico");
	if (Storico.enableResize){
		$(window).unbind('resize', Storico.ResizePlot);
	}

	InterfaceEnergyHome.Abort();
	$("#Content").hide();
}

Storico.DateIsSame = function(valDate, val1Date) {
	d1 = valDate.getDate();
	m1 = valDate.getMonth();
	y1 = valDate.getFullYear();
	// oggi = GestDate.GetActualDate();
	d2 = val1Date.getDate();
	m2 = val1Date.getMonth();
	y2 = val1Date.getFullYear();
	if ((d1 == d2) & (m1 == m2) && (y1 == y2)){
		return true;
	} else {
		return false;
	}
}

// controllo se la data specificata rientra tra la data di installazione e
// quella attuale
Storico.DateIsValid = function(valDate, periodo) {
	// nel caso di giorno tengo conto dell'ora, altrimenti no
	if (periodo == Storico.GIORNO) {
		ora = new Date(GestDate.GetActualDate().getTime());
		var a = new Date(valDate.getTime());
		// se giorno di installazione non segnalo errore
		// if (Storico.DateIsSame(tmp, Storico.installData))
		// return true;
		a.setMinutes(0); // fine dell'ora attuale
		// controllo per data di installazione
		a.setHours(a.getHours() + 1);
		if (a.getTime() < Storico.installData.getTime())
			return false;
		// controllo per ora attuale
		// aumento ancora di 1 perche' potrie avere dati incompleti per le
		// ultime 2 ore
		a.setHours(a.getHours() + 1);
		if (a.getTime() > ora)
			return false;

		return true;
	} else {
		// nel caso di giorno il giorno di installazione e quello attuale
		// possono avere
		// dati nulli e non devo segnalare errore
		tmpI = new Date(valDate.getTime());
		tmpI.setHours(0);
		if (tmpI.getTime() < Storico.installData.getTime())
			return false;
		tmpI.setDate(tmpI.getDate() + 1);
		domani = new Date(GestDate.GetActualDate().getTime());
		domani.setDate(domani.getDate() + 1);
		domani.setHours(0);
		domani.setMinutes(0);
		if (tmpI.getTime() < domani.getTime())
			return true;
	}
	return false;
}

// se tipo = 0 visualizza costi e consumi
// se tipo = 1 visualizza i consumi
Storico.VisStorico = function(tipo) {
	var tickers = new Array();
	var rapp, barW, max;
	var dati1 = null;
	var dati2 = null;
	var maxConsumo = 0;
	var maxCosto = 0;
	var yOffset = 0;
	//Variabile che mi serve per capire se sto analizzando i dati del giorno corrente oppure no
	var limitOdierno = null;

	var cat = new Array();
	var daysOfWeek = Msg.daysOfWeek;
	var XTitleText = "";

	Storico.datiConsumoNulli = false;
	Storico.datiCostoNulli = false;
	$.jqplot.config.enablePlugins = true;
	$('#StoricoGraph').html(null);
	$("#LabelStoricokWh").hide();
	$("#LabelStoricoEuro").hide();
	// tipo = Storico.SOLO_CONSUMI;
	var toDay = new Date(GestDate.GetActualDate().getTime());
	var valTicker = new Date(Storico.dataInizio.getTime());
	valTicker.setHours(0);
	valTicker.setMinutes(0);
	valTicker.setSeconds(0);
	aTicks = 0;
	// se smart info metto tutti
	if (Storico.GetDispTipo(Storico.dispositivoScelto) == InterfaceEnergyHome.SMARTINFO_APP_TYPE){
		disp = Msg.home["tuttiStorico"];
	} else {
		disp = Storico.dispositivoScelto;
	}

	if (Storico.periodoScelto == Storico.GIORNO) {
		labelX = Msg.home["oraLabelStorico"];
		titolo = Msg.home["giornoGrafStorico"] + ": " + Storico.dataInizio.getDate() + "  "
				+ Msg.mesiAbbrev[Storico.dataInizio.getMonth()].toUpperCase()
				+ " " + Storico.dataInizio.getFullYear() + "  -  " + Msg.home["dispGrafStorico"] + ": " + disp;
		nTickskWh = 6;
		nTicksE = 6;
		formatStr = "%.2f";

		var timeToDay = Math.round(toDay.getFullYear().toString() + toDay.getMonth().toString() + toDay.getDate().toString());
		var timeValTicker = Math.round(valTicker.getFullYear().toString() + valTicker.getMonth().toString() + valTicker.getDate().toString());
		
		if (timeToDay == timeValTicker){
			limitOdierno = toDay.getHours();
		} else {
			limitOdierno = null;
		}
	} else if (Storico.periodoScelto == Storico.ANNO) {
		titolo = Msg.home["daGrafStorico"] + " "
				+ Msg.mesiAbbrev[Storico.dataInizio.getMonth()].toUpperCase()
				+ " " + Storico.dataInizio.getFullYear() + " "
				+ Msg.home["aGrafStorico"] + " "
				+ Msg.mesiAbbrev[Storico.dataFine.getMonth()].toUpperCase()
				+ " " + Storico.dataFine.getFullYear() + "  -  "
				+ Msg.home["dispGrafStorico"] + ": " + disp;
		labelX = Msg.home["meseLabelStorico"];
		nTickskWh = 8;
		nTicksE = 8;
		formatStr = "%.1f";

		if ((Storico.dataInizio.getFullYear() == toDay.getFullYear()) && 
			(toDay.getFullYear() == Storico.dataFine.getFullYear())){
			limitOdierno = toDay.getMonth() + 1;
		} else {
			limitOdierno = null;
		}
	} else {
		titolo = Msg.home["daGrafStorico"] + " " + Storico.dataInizio.getDate() + " "
				+ Msg.mesiAbbrev[Storico.dataInizio.getMonth()].toUpperCase() + " " 
				+ Storico.dataInizio.getFullYear() + " "
				+ Msg.home["aGrafStorico"] + " " + Storico.dataFine.getDate() + " "
				+ Msg.mesiAbbrev[Storico.dataFine.getMonth()].toUpperCase() + " " 
				+ Storico.dataFine.getFullYear() + "  -  "
				+ Msg.home["dispGrafStorico"] + ": " + disp;
		if (Storico.periodoScelto == Storico.MESE){
			aTicks = -80;
		}
		labelX = Msg.home["giornoLabelStorico"];
		nTickskWh = 6;
		nTicksE = 6;
		formatStr = "%.1f";

		if (Storico.periodoScelto == Storico.MESE){
			if ((Storico.dataInizio.getTime() < toDay.getTime()) && (toDay.getTime() < Storico.dataFine.getTime())){
				limitOdierno = toDay.getDate();
			} else {
				limitOdierno = null;
			}
		} else {
			if ((Storico.dataInizio.getTime() < toDay.getTime()) && (toDay.getTime() < Storico.dataFine.getTime())){
				limitOdierno = toDay.getDay();
			} else {
				limitOdierno = null;
			}
		}
	}
	// non prevedo solo costo per il momento
	if (Storico.datiConsumo == null) {
		$("#MsgStorico").hide();
		if (Main.env == 0) console.log(40, Storico.MODULE, "VisStorico : nessun dato");
		$("#StoricoGraph").html("<div id='StoricoVuotoTitolo'>" + titolo + "</div>" + "<div id='StoricoVuoto'>" + Msg.home["noGrafStorico"] + "</div>");
		hideSpinner();
		return;
	}

	if (Storico.datiConsumo != null){
		dati1 = Storico.datiConsumo.slice(0); // copio array perche' lo modifico (per simulazione)
		if (Main.env == 0) console.log(40, Storico.MODULE, "VisStorico : dati1 da Storico.datiConsumo = " + dati1);
	}
	if (Main.enablePV){
		if (Storico.datiProduzione != null){
			dati2 = Storico.datiProduzione.slice(0);
			if (Main.env == 0) console.log(40, Storico.MODULE, "VisStorico : dati2 da Storico.datiProduzione = " + dati2);
		}
	} else {
		if (Storico.datiCosto != null){
			dati2 = Storico.datiCosto.slice(0);
			if (Main.env == 0) console.log(40, Storico.MODULE, "VisStorico : dati2 da Storico.datiCosto = " + dati2);
		}
	}
	
	//filtro con jquery.grep e se il risultato � < al 30% visualizzo il div col msg di errore
	var consumiNullGrep = jQuery.grep(dati1, function(valueDati1){
		return (valueDati1 != null);
	});
	
	if (dati2 != null){
		var costiNullGrep = jQuery.grep(dati2, function(valueDati2){
			return (valueDati2 != null);
		});
	}
	
	if (Main.env == 0) console.log('dati1', dati1);
	if (Main.env == 0) console.log('dati2', dati2);
	
	// creo array con i dati e creo le label per i tickers
	// controllo se i dati sono nei limiti altrimenti li sposto (massimo valore + 5%)
	// se dato nullo lo segnalo (lo ignoro se prima della data di installazione o da oggi in poi)
	for (i = 0; i < dati1.length; i++) {
		// se uno dei 2 e' null metto a null anche l'altro
		if ((dati1[i] == null) && (limitOdierno) && (i < limitOdierno)) {
			dati1[i] = 0;
			/*if (dati2[i] == null) {
				dati2[i] = 0;
			}*/
			if (Storico.DateIsValid(valTicker, Storico.periodoScelto)){
				Storico.datiConsumoNulli = true;
			}
		} else {
			if ((limitOdierno) && (i >= limitOdierno)){
				/*if (dati2[i] == null) {
					dati2[i] = 0;
				}*/
				dati1[i] = 0;
			} else {
				var controllo = dati1[i] / 10;
				if (controllo > maxConsumo){
					maxConsumo = controllo;
				}

				//Arrotondo al centesimo per i consumi (KWh)
				dati1[i] = Utils.RoundTo(dati1[i] / 1000, 2); // da w a KWh
			}
		}
		
		if ((tipo == Storico.COSTI_CONSUMI) && (dati2 != null)) {
			if ((dati2[i] == null) && (limitOdierno) && (i < limitOdierno)) {
				dati2[i] = 0;
				//dati1[i] = 0;
				if (Storico.DateIsValid(valTicker, Storico.periodoScelto)){
					Storico.datiCostoNulli = true;
				}
			} else {
				if ((limitOdierno) && (i >= limitOdierno)){
					dati2[i] = 0;
					//dati1[i] = 0;
				} else {

					if (!Main.enablePV){
						//Arrotondo al millesimo di euro
						dati2[i] = Utils.RoundTo(dati2[i], 3);
						if (dati2[i] > maxCosto){
							maxCosto = dati2[i];
						}
					}

					if (Main.enablePV){
						dati2[i] = Utils.RoundTo(dati2[i] / 1000, 2); // da w a KWh
					}
				}
			}
		}
		ruota = 0;
		min = 0;
		// label ticks diverso a seconda di cosa sto visualizzando
		if (Storico.periodoScelto == Storico.GIORNO) {
			var hour = valTicker.getHours() + i;
			cat.push(hour.toString());
			if (Main.env == 0) console.log(80, Storico.MODULE, "VisStorico : ora = " + tickers[i]);
			ruota = 0;
			min = 0;
			max = 23;
			yOffset = 20;
			XTitleText = Msg.storico[0];
			// var ho = valTicker.getHours()+1;
			// valTicker.setHours(ho); // giorno
		} else if (Storico.periodoScelto == Storico.SETTIMANA) {
			var day = valTicker.getDate();
			var month = valTicker.getMonth();
			// tickers[i] = day + "-" + month;
			valTicker.setDate(valTicker.getDate() + 1); // settimana
			cat.push(daysOfWeek[i] + "<br />(" + day + "-" + Msg.mesiCompleto[month] + ")");
			ruota = -45;
			yOffset = 17;
			XTitleText = Msg.storico[1];
		} else if (Storico.periodoScelto == Storico.MESE) {
			tickers[i] = valTicker.getDate() + "-" + (valTicker.getMonth() + 1);

			ruota = -45;
			cat.push(valTicker.getDate() + " - " + (valTicker.getMonth() + 1));
			min = 1;
			max = 31;
			yOffset = 30;
			valTicker.setDate(valTicker.getDate() + 1); // mese
			XTitleText = Msg.storico[2];
		} else {
			cat.push(Msg.mesiAbbrev[valTicker.getMonth()] + "-" + (valTicker.getFullYear() - 2000));
			valTicker.setMonth(valTicker.getMonth() + 1); // anno
			ruota = -45;
			min = 1;
			max = 12;
			yOffset = 30;
			XTitleText = Msg.storico[3];
		}
	}
	
	if (Main.env == 0) console.log('dati1', dati1);
	if (Main.env == 0) console.log('dati2', dati2);

	maxConsumo = Math.ceil(maxConsumo) / 100;
	maxConsumo = maxConsumo.toFixed(2);

	if (tipo == Storico.COSTI_CONSUMI) {
		barW = Math.round($('#StoricoGraph').width() * 0.4 / (dati1.length + 2));
		rapp = maxConsumo / maxCosto;
		if (Main.env == 0) console.log(20, Storico.MODULE, "VisStorico: maxConsumo = " + maxConsumo + " maxCosto = " + maxCosto + " rapp = " + rapp);
		$("#LabelStoricokWh").show();
		$("#LabelStoricoEuro").show();
	} else {
		// per adesso per il trial gestisco solo consumo
		$("#LabelStoricoEuro").hide();
		$("#LabelStoricokWh").show();
		barW = Math.round($('#StoricoGraph').width() * 0.8 / (dati1.length + 2));
		rapp = 1;
	}
	
	// Calcolo il valore del 30% (considerando anche se stiamo visualizzando i dati del giorno corrente, a quel punto si calcola il 30%
	// delle ore/giorni/mesi che sono effettivamente trascorsi)
	var limitAlertMsg = 0;
	var toDay = new Date(GestDate.GetActualDate().getTime());
	var isToDayToDay = (Storico.dataInizio.getDate() == toDay.getDate()) && (Storico.dataInizio.getMonth() == toDay.getMonth()) && (Storico.dataInizio.getFullYear() == toDay.getFullYear());
	switch (Storico.periodoScelto) {
		case Storico.GIORNO:{
			limitAlertMsg = Storico._setLimitAlertMsg(toDay.getHours(), Storico.LIMITALERTGIORNO, toDay);
			break;
		}
		case Storico.SETTIMANA:{
			limitAlertMsg = Storico._setLimitAlertMsg(toDay.getDay(), Storico.LIMITALERTSETTIMANA, toDay);
			break;
		}
		case Storico.MESE:{
			limitAlertMsg = Storico._setLimitAlertMsg(toDay.getDate(), Storico.LIMITALERTMESE, toDay);
			break;
		}
		case Storico.ANNO:{
			limitAlertMsg = Storico._setLimitAlertMsg(toDay.getMonth() + 1, Storico.LIMITALERTANNO, toDay);
			break;
		}
		default:{
			// ??????????
			//alert('ERRORE!');
			break;
		}
	}

	if (consumiNullGrep.length < limitAlertMsg){
		Storico.datiConsumoNulli = true;
	}
	
	if (dati2 != null){
		if (costiNullGrep.length < limitAlertMsg){
			Storico.datiCostoNulli = true;
		}
	}
	// Gestione giorno DST
	if (GestDate.DSTMarzo || GestDate.DSTOttobre) {
		var numero_ore = dati1.length;
		if (numero_ore != 24) {

			switch (numero_ore) {

				case 23: {
	
					tickers.splice(2, 1);
					tickers.push("23");
					break;
				}
				case 25: {
	
					tickers.pop();
					tickers.splice(2, 0, "2");
					break;
				}
				default:{
					break;
				}
			}
		}
	}

	Highcharts.setOptions({
		colors : [ 'blue', "#21e700", '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4' ]
	});

	var titleX = Msg.titleGraphStorico[0];
	var titleY1 = Msg.titleGraphStorico[1];
	var titleY2 = Msg.titleGraphStorico[2];
	
	if (Main.enablePV){

		Highcharts.setOptions({
			colors : [ 'blue', "#21e700", '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4' ]
		});
		if ((Storico.device == 0) || (Storico.device == null)){
			graficoStorico = new Highcharts.Chart({
				chart : {renderTo : 'StoricoGraph',
						 events: {
							 load: function(event) {
								 hideSpinner();
							 }
			             }, 
						 type : 'column'},
				title : {text : titolo,
						 textAlign : 'left',
						 show : true},
						 subtitle : {text : ''},
				credits : false,
				xAxis : { labels : { rotation : ruota,
									 align : 'center',
									 style : { font : 'normal 10px Verdana, sans-serif'},
									 y : yOffset},
						  tickInterval : 1,
						  ticks : tickers,
						  title : { align : 'middle',
							 	    text : XTitleText,
							 	    rotation : 0,
							 	    offset : 45,
							 	    style : { color : "black"}},
						 categories : cat},
				yAxis : [ { min : 0,
							title : { text : titleX,
									  style : { color : 'blue'}},
							labels : { formatter : function() {return this.value + 'KWh';},
							style : { color : 'blue'}}}, 
						  { gridLineWidth: 1,
							min: 0,
							title : { text : titleY2,
									  style : { color : '#21e700'}},
							labels : { formatter : function() { return this.value + ' KWh';},
							style : {color : 'green'}},
							opposite : true}],
				legend : { layout : 'vertical',
						   backgroundColor : '#FFFFFF',
						   align : 'left',
						   verticalAlign : 'top',
						   x : 100,
						   y : 20,
						   floating : true,
						   shadow : true},
				tooltip : { formatter : function() { return '' + this.x + ': ' + Math.round(this.y * 1000) / 1000 + ' KWh';}},
				plotOptions : { column : { pointPadding : 0.2,
										   borderWidth : 0}},
				series : [{ name : titleX,
							data : dati1}, 
						  { name : titleY2,
							data : dati2}]});
		} else {
			graficoStorico = new Highcharts.Chart({
				chart : { renderTo : 'StoricoGraph',
						  events: {
							 load: function(event) {
								 hideSpinner();
							 }
			              }, 
						  type : 'column'},
				title : { text : titolo,
						  textAlign : 'left',
						  show : true},
				subtitle : { text : ''},
				credits : false,
				xAxis : { labels : { rotation : ruota,
									 align : 'center',
									 style : { font : 'normal 10px Verdana, sans-serif'},
									 y : yOffset},
						  tickInterval : 1,
						  ticks : tickers,
						  title : { align : 'middle',
							  		text : XTitleText,
							  		rotation : 0,
							  		offset : 45,
							  		style : {color : "black"}},
						  categories : cat},
				yAxis : [{ min : 0,
						   title : { text : titleX,
							   		 style : { color : 'blue'}},
						   labels : {formatter : function() { return this.value + 'KWh';},
							   		 style : {color : 'blue'}}}],
				legend : { layout : 'vertical',
						   backgroundColor : '#FFFFFF',
						   align : 'left',
						   verticalAlign : 'top',
						   x : 100,
						   y : 20,
						   floating : true,
						   shadow : true},
				tooltip : { formatter : function() { return '' + this.x + ': ' + Math.round(this.y * 1000) / 1000 + ' KWh';}},
				plotOptions : { column : { pointPadding : 0.2,
										   borderWidth : 0}},
				series : [{ name : titleX,
							data : dati1}]});
		}
	} else {

		Highcharts.setOptions({
			colors : [ 'blue', "#FFA500", '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4' ]
		});

		graficoStorico = new Highcharts.Chart({
			chart : {
				renderTo : 'StoricoGraph',
				events: {
					 load: function(event) {
						 hideSpinner();
					 }
	            }, 
				type : 'column'
			},
			title : {
				text : titolo,
				textAlign : 'left',
				show : true
			},
			subtitle : {
				text : ''
			},
			credits : false,
			xAxis : {
				labels : {
					rotation : ruota,
					align : 'center',
					style : {
						font : 'normal 10px Verdana, sans-serif'
					},
					y : yOffset

				},
				tickInterval : 1,
				ticks : tickers,
				title : {
					align : 'middle',
					text : XTitleText,
					rotation : 0,
					offset : 45,
					style : {
						color : "black"
					}
				},
				categories : cat
			},
			yAxis : [ {
				min : 0,
				title : {
					text : 'Consumi ',
					style : {
						color : 'blue'
					}
				},
				labels : {
					formatter : function() {
						return this.value + 'KWh';
					},
					style : {
						color : 'blue'
					}
				}
			}, { // Secondary yAxis
				gridLineWidth: 1,
				min: 0,
	            title : {
					text : 'Costi',
					style : {
						color : '#FFA500'
					}
				},
				labels : {
					formatter : function() {
						return this.value + ' Euro';
					},
					style : {
						color : '#FFA500'
					}
				},
				opposite : true
			} ],
			legend : {
				layout : 'vertical',
				backgroundColor : '#FFFFFF',
				align : 'left',
				verticalAlign : 'top',
				x : 100,
				y : 20,
				floating : true,
				shadow : true
			},
			tooltip : {
				formatter : function() {
					return '' + this.x + ': ' + Math.round(this.y * 1000) / 1000 + (this.series.name == 'Costi' ? ' Euro' : ' KWh');
				}
			},
			plotOptions : {
				column : {
					pointPadding : 0.2,
					borderWidth : 0
				}
			},
			series : [{
				name : 'Consumi',
				data : dati1

			}, {
				name : 'Costi',
				data : dati2,
				yAxis : 1

			}]
		});
	}
	
	
	if (Storico.DateIsSame(Storico.dataInizio, Storico.installData)){
		$("#MsgStorico").hide();
	} else if ((Storico.datiCostoNulli) || (Storico.datiConsumoNulli)) {
		$("#MsgStorico").show();
	} else {
		$("#MsgStorico").hide();
	}
}

Storico.DatiConsumoStorico = function(val) {

	if (Main.env == 0) console.log('datiConsumi Consumi dal server', val);
	Storico.datiConsumo = val;
	if (val == null){
		if (Main.env == 0) console.log(80, Storico.MODULE, "DatiConsumoStorico null");
	}
	//hideSpinner();
	if (Main.env == 0) console.log(80, Storico.MODULE, "DatiConsumoStorico = " + val);
	// Nella prima fase del trial gestisco solo i dati del consumo
	if (InterfaceEnergyHome.mode == InterfaceEnergyHome.MODE_FULL){
		Storico.VisStorico(0);
	} else {
		Storico.VisStorico(0);
	}
}

Storico.DatiCostoStorico = function(val) {
	if (Main.env == 0) console.log('datiCosti Costi dal server', val);
	Storico.datiCosto = val;
	if (val == null){
		if (Main.env == 0) console.log(80, Storico.MODULE, "DatiCostoStorico null");
	}

	if (Main.env == 0) console.log(80, Storico.MODULE, "DatiCostoStorico = " + val);
	InterfaceEnergyHome.GetStorico('Consumo', Storico.device, Storico.dataInizio, Storico.dataFine, Storico.periodoScelto, Storico.DatiConsumoStorico);
}

Storico.DatiProduzioneStorico = function(val) {
	if (Main.env == 0) console.log('datiCosti Costi dal server', val);
	/*
	if (val != null){
		for (var i=0;i<val.length;i++)
			val[i] = val[i] * 10;
	}*/
	
	Storico.datiProduzione = val;
	if (val == null){
		if (Main.env == 0) console.log(80, Storico.MODULE, "datiProduzioneStorico null");
	}

	if (Main.env == 0) console.log(80, Storico.MODULE, "DatiCostoStorico = " + val);
	InterfaceEnergyHome.GetStorico('Consumo', Storico.device, Storico.dataInizio, Storico.dataFine, Storico.periodoScelto, Storico.DatiConsumoStorico);
}

/*******************************************************************************
 * Legge i dati del costo e del consumo relativi al periodo e dispositivo
 * selezionato
 ******************************************************************************/
Storico.GetStorico = function() {
	Main.ResetError();
	showSpinner();
	if (Main.env == 0) console.log(20, Storico.MODULE, "Dispositivo= " + Storico.dispositivoScelto + " Periodo = " + Storico.periodoScelto);
	if (Main.env == 0) console.log(20, Storico.MODULE, "============ GetStorico: inizio = " + Storico.dataInizio.toString() + " fine = " + Storico.dataFine.toString());

	// imposta id del dispositivo selezionato
	Storico.device = Storico.GetDispId(Storico.dispositivoScelto);
	if (Main.env == 0) console.log(80, Storico.MODULE, "Periodo: pid = " + Storico.device + " inizio = " + Storico.dataInizio.toString() + " fine = " + Storico.dataFine.toString());

	if (Main.enablePV){
		//ToDo: evitare se device diverso da SmartInfo
		if ((Storico.device == 0) || (Storico.device == null)){
			InterfaceEnergyHome.GetStorico('Produzione', Storico.device, Storico.dataInizio, Storico.dataFine, Storico.periodoScelto, Storico.DatiProduzioneStorico);
		} else {
			Storico.DatiProduzioneStorico(null);
		}
	} else {
		InterfaceEnergyHome.GetStorico('Costo', Storico.device, Storico.dataInizio, Storico.dataFine, Storico.periodoScelto, Storico.DatiCostoStorico);
	}
}

Storico.GetDispId = function(nomeElettr) {
	var rtnResult = null;
	if (Storico.datiElettr != null){
		for (i = 0; i < Storico.datiElettr.length; i++){
			if (Storico.datiElettr[i].nome == nomeElettr){
				if (Storico.datiElettr[i].tipo == InterfaceEnergyHome.SMARTINFO_APP_TYPE){
					rtnResult =  null;
				} else {
					if (InterfaceEnergyHome.mode == -1) {
						rtnResult = 1;
					} else {
						rtnResult = Storico.datiElettr[i].pid;
					}
				}
			}
		}
	}
	return rtnResult;
}

Storico.GetDispTipo = function(nomeElettr) {
	var rtnResult = null;
	if (Storico.datiElettr != null){
		for (i = 0; i < Storico.datiElettr.length; i++){
			if (Storico.datiElettr[i].nome == nomeElettr){
				rtnResult =  Storico.datiElettr[i].tipo;
			}
		}
	}
	return rtnResult;
}

// se cambio in breve tempo sia periodo che dispositivo (o che date con le frecce)
// potrei lanciare una chiamata prima che sia finita quella precedente bisogna vedere cosa succede cambio le date in base al periodo precedente e al tipo di periodo scelto
// non devo mettere ora 23 nel caso di richiesta di giorno perche' quando c'e' l'ora legale altrimenti passo al giorno dopo
Storico.SceltaPeriodo = function() {
	Storico.periodoScelto = parseInt($("input[type=radio][name='Periodo']:checked").val());
	if (Main.env == 0) console.log(20, Storico.MODULE, "========== SceltaPeriodo: precedente = " + Storico.tipoUltimoPeriodo + " nuovo = " + Storico.periodoScelto);
	if (Storico.periodoScelto != Storico.tipoUltimoPeriodo) {
		$("#Prec").show();
		$("#Succ").show();
		switch (Storico.periodoScelto) {
			case Storico.GIORNO:
				// prendo sempre l'ultimo giorno del periodo attualmente visualizzato
				// a meno che il giorno non sia maggiore di ieri
				ieri = new Date(GestDate.GetActualDate().getTime());
				ieri.setHours(0);
				if (Storico.dataFine.getTime() >= ieri.getTime()) {
					// prendo ieri
					Storico.dataFine = new Date(ieri.getTime());
					Storico.dataFine.setDate(Storico.dataFine.getDate() - 1);
				}
				Storico.dataFine.setHours(23);
				Storico.dataFine.setMinutes(59);
				Storico.dataInizio = new Date(Storico.dataFine.getTime());
				Storico.dataInizio.setHours(0);
				Storico.dataInizio.setMinutes(0);
				break;
			case Storico.SETTIMANA:
				// prendo settimana da lunedi' a domenica (N.B. 0 = domenica, 1 = lunedi')
				// prendo la settimana in cui cade l'ultimo giorno data fine <= ieri
				ieri = new Date(GestDate.GetActualDate().getTime());
				ieri.setDate(ieri.getDate() - 1);
				ieri.setHours(0);
				ieri.setMinutes(0);
				ieri.setSeconds(0);
				ieri.setMilliseconds(0);
				if ((Storico.dataFine.getTime()) >= ieri.getTime()){
					Storico.dataFine = new Date(ieri.getTime());
				}
				Storico.dataFine.setHours(12);
				day = Storico.dataFine.getDay();
				// se domenica resta giorno finale della settimana
				if (day != 0) {
					// ultimo giorno = differenza tra domenica e il giorno in questione
					Storico.dataFine.setDate(Storico.dataFine.getDate() + (7 - day));
				}
				Storico.dataInizio = new Date(Storico.dataFine.getTime());
				Storico.dataInizio.setDate(Storico.dataInizio.getDate() - 6);
				Storico.dataInizio.setHours(0);
				Storico.dataFine.setHours(12);
				break;
			case Storico.MESE:
				if (Storico.tipoUltimoPeriodo == Storico.ANNO) {
					// prendo dicembre o ultimo mese
					Storico.dataInizio = new Date(Storico.dataFine.getTime());
					Storico.dataInizio.setMonth(11);
					Storico.dataInizio.setDate(1);
					Storico.dataInizio.setHours(0);
					Storico.dataInizio.setMinutes(0);
				} else {
					// prendo il mese in cui cade l'ultimo giorno selezionato
					Storico.dataInizio = new Date(Storico.dataFine.getTime());
					Storico.dataInizio.setDate(1); // primo giorno del mese
				}
				if (Storico.dataInizio.getTime() > GestDate.GetActualDate().getTime()) {
					Storico.dataInizio = new Date(GestDate.GetActualDate().getTime());
					Storico.dataInizio.setDate(1);
				}
				// ultimo giorno del mese (giorno prima del primo giorno del mese
				// successivo)
				Storico.dataFine = new Date(Storico.dataInizio.getTime());
				Storico.dataFine.setMonth(Storico.dataFine.getMonth() + 1);
				Storico.dataFine.setDate(Storico.dataFine.getDate() - 1);
				Storico.dataInizio.setHours(0);
				Storico.dataFine.setHours(12);
				break;
	
			case Storico.ANNO:
				// prendo sempre l'anno dell'ultimo giorno selezionato
				Storico.dataInizio = new Date(Storico.dataFine.getTime());
				Storico.dataInizio.setDate(1);
				Storico.dataInizio.setMonth(0);
				Storico.dataFine = new Date(Storico.dataInizio.getTime());
				Storico.dataFine.setDate(31);
				Storico.dataFine.setMonth(11);
				Storico.dataInizio.setHours(0);
				Storico.dataFine.setHours(12);
				break;

		}
		// controllo di non superare i limiti temporali
		if (Storico.dataInizio.getTime() < Storico.installData.getTime()){
			$("#Prec").hide();
		}

		// data fine <= ieri
		ieri = new Date(GestDate.GetActualDate().getTime());
		// ieri.setDate(ieri.getDate()-1);
		ieri.setHours(0);
		ieri.setMinutes(0);
		ieri.setSeconds(0);
		ieri.setMilliseconds(0);
		if ((Storico.dataFine.getTime()) >= ieri.getTime()){
			$("#Succ").hide();
		}
		Storico.tipoUltimoPeriodo = Storico.periodoScelto;
		Storico.GetStorico();
	} else {
		if (Main.env == 0) console.log(80, Storico.MODULE, "SceltaPeriodo: non ho cambiato tipo di periodo");
	}
}

// cambio solo dispositivo, lascio le date attuali
Storico.SceltaDispositivo = function() {
	Storico.dispositivoScelto = $("input[type=radio][name='Dispositivo']:checked").val();
	Storico.GetStorico();
}

// imposta periodo precedente in base al tipo di periodo scelto ed esegue richiesta
Storico.Precedente = function() {
	if (Main.env == 0) console.log(20, Storico.MODULE, "============ Precedente: periodoScelto = " + Storico.periodoScelto);
	switch (Storico.periodoScelto) {
		case Storico.GIORNO:
			Storico.dataInizio.setDate(Storico.dataInizio.getDate() - 1);
			Storico.dataFine.setDate(Storico.dataFine.getDate() - 1);
			break;
		case Storico.SETTIMANA:
			Storico.dataInizio.setDate(Storico.dataInizio.getDate() - 7);
			Storico.dataFine.setDate(Storico.dataFine.getDate() - 7);
			break;
		case Storico.MESE:
			Storico.dataInizio.setMonth(Storico.dataInizio.getMonth() - 1);
			// ultimo giorno del mese precedente
			Storico.dataFine = new Date(Storico.dataFine.getFullYear(),
			Storico.dataFine.getMonth(), 0, 12, 00, 00);
			break;
		case Storico.ANNO:
			Storico.dataInizio.setFullYear(Storico.dataInizio.getFullYear() - 1);
			// ultimo giorno dell'anno precedente
			Storico.dataFine.setFullYear(Storico.dataFine.getFullYear() - 1);
			Storico.dataFine.setMonth(11);
			Storico.dataFine.setDate(31);
			break;
	}
	$("#Succ").show();
	// se data inizio e' prima data possibile nascondo freccia precedente
	if (Storico.dataInizio.getTime() <= Storico.installData.getTime()) {
		// Storico.dataInizio.setTime(Storico.primaData.getTime());
		$("#Prec").hide();
	} else {
		$("#Prec").show();
	}
	Storico.GetStorico();
}

// imposta periodo successivo in base al tipo di periodo scelto ed esegue richiesta
Storico.Successivo = function() {
	
	if (Main.env == 0) console.log(20, Storico.MODULE, "=========== Successivo: periodoScelto = " + Storico.periodoScelto);
	switch (Storico.periodoScelto) {
		case Storico.GIORNO:
			Storico.dataInizio.setDate(Storico.dataInizio.getDate() + 1);
			Storico.dataFine.setDate(Storico.dataFine.getDate() + 1);
			
			break;
		case Storico.SETTIMANA:
			Storico.dataInizio.setDate(Storico.dataInizio.getDate() + 7);
			Storico.dataFine.setDate(Storico.dataFine.getDate() + 7);
			break;
		case Storico.MESE:
			Storico.dataInizio.setMonth(Storico.dataInizio.getMonth() + 1);
			Storico.dataFine = new Date(Storico.dataFine.getFullYear(), Storico.dataFine.getMonth() + 2, 0, 12, 59, 59);
			break;
		case Storico.ANNO:
			Storico.dataInizio.setFullYear(Storico.dataInizio.getFullYear() + 1);
			Storico.dataFine.setFullYear(Storico.dataFine.getFullYear() + 1);
			break;
	}
	$("#Prec").show();
	// se data fine e' ieri nascondo freccia successivo
	ieri = new Date(GestDate.GetActualDate().getTime());
	// ieri.setDate(ieri.getDate()-1);
	ieri.setHours(0);
	ieri.setMinutes(0);
	ieri.setSeconds(0);
	ieri.setMilliseconds(0);
	if ((Storico.dataFine.getTime()) >= ieri.getTime()){
		$("#Succ").hide();
	} else {
		$("#Succ").show();
	}
	console.debug(Storico.periodoScelto)
	console.debug(Storico.dataInizio);
	console.debug(Storico.dataFine);
	Storico.GetStorico();
}

// la prima volta default tutti e giorno, poi seleziona gli ultimi valori
// selezionati
Storico.VisScelta = function() {
	var listDisp = "", tmp = "", tutti;

	// imposto smart info per tutti
	/*tutti = $(document.createElement('input')).attr('name', 'Dispositivo')
											  .attr('type', 'radio')
											  .attr('checked', 'checked')
											  .attr('value', Msg.home["tuttiStorico"])
											  .append(Msg.home["tuttiStorico"]);*/
	tutti = "<input class='ButtonScelta' name='Dispositivo' type='radio' checked='checked' value='" + Msg.home["tuttiStorico"] + "'>" + Msg.home["tuttiStorico"];
	
	if (Storico.datiElettr != null && Storico.datiElettr.length > 0) {
		for (i = 0; i < Storico.datiElettr.length; i++) {
			// creo elenco dei dispositivi selezionabili
			if (Storico.datiElettr[i].tipo == InterfaceEnergyHome.SMARTINFO_APP_TYPE) {
				/*tutti = $(document.createElement('input')).attr('name', 'Dispositivo')
														  .attr('type', 'radio')
														  .attr('class', 'ButtonScelta')
														  .attr('checked', 'checked')
														  .attr('value', Storico.datiElettr[i].nome)
														  .append(Msg.home["tuttiStorico"]);*/
				tutti = "<input class='ButtonScelta' name='Dispositivo' type='radio' checked='checked' value='" + Storico.datiElettr[i].nome + "'>" + Msg.home["tuttiStorico"];
				// Storico.dispositivoScelto = Storico.datiElettr[i].nome;
			} else {
				/*tutti = $(document.createElement('input')).attr('name', 'Dispositivo')
														  .attr('type', 'radio')
														  .attr('class', 'ButtonScelta')
														  .attr('checked', 'checked')
														  .attr('value', Storico.datiElettr[i].nome)
														  .append(Storico.datiElettr[i].nome);*/
				tmp = tmp + "<br><input class='ButtonScelta' name='Dispositivo' type='radio' value='" + Storico.datiElettr[i].nome + "'>" + Storico.datiElettr[i].nome;
			}
		}
		listaDisp = tutti + tmp;
		$("#SceltaDispositivo").html(listaDisp);
	} else {
		// $("#SceltaDispositivo").html("<input class='ButtonScelta' name='Dispositivo' type='radio' checked='checked' value='" + Msg.home["tuttiStorico"] + "'>" + Msg.home["tuttiStorico"]);
		//InterfaceEnergyHome.objService.getNoServerCustomDevice(Storico.GestStoricoList);
		Storico.GestStoricoList(fakeValues.noServerCustomDevice, null);
	}
	
	Storico.periodoScelto = Storico.GIORNO;
	Storico.tipoUltimoPeriodo = Storico.GIORNO;
	Storico.dispositivoScelto = Msg.home["tuttiStorico"];
	if (Main.env == 0) console.log(80, Storico.MODULE, "dispositivoScelto = " + Storico.dispositivoScelto + " periodoScelto = " + Storico.periodoScelto);

	// metto data inizio e fine = ieri
	Storico.dataFine = new Date(GestDate.GetActualDate().getTime());
	Storico.dataFine.setDate(Storico.dataFine.getDate() - 1);
	Storico.dataFine.setHours(23);
	Storico.dataFine.setMinutes(59);
	Storico.dataInizio = new Date(Storico.dataFine.getTime());
	Storico.dataInizio.setHours(0);
	Storico.dataInizio.setMinutes(0);

	// gestisco caso giorno installazione == oggi, non ho dati nello storico
	if ((Storico.dataInizio.getTime() < Storico.installData.getTime()) && (Storico.dataFine.getTime() < Storico.installData.getTime())) {
		$("#Prec").hide();
		$("#Succ").hide();
		if (Main.env == 0) console.log(40, Storico.MODULE, "VisStorico : nessun dato");
		$("#StoricoGraph").html($(document.createElement('div')).attr('id', 'StoricoVuoto').text(Msg.home["noGrafStorico"]));
		//$("#StoricoGraph").html("<div id='StoricoVuoto'>" + Msg.home["noGrafStorico"] + "</div>");
		hideSpinner();
		return;
	}

	// imposta gestione scelta
	$("input[type=radio][name='Periodo']").change(Storico.SceltaPeriodo);
	$("input[type=radio][name='Dispositivo']").change(Storico.SceltaDispositivo);

	// gestisce frecce
	// per funzionare su iPad devo mettere un div sopra l'immagine della freccia
	// se non non e' cliccabile
	$("#Prec").click(Storico.Precedente);
	$("#Succ").click(Storico.Successivo);
	if (Storico.dataInizio.getTime() < Storico.installData.getTime()){
		$("#Prec").hide();
		// $("#Succ").hide();
	}

	// visualizza grafico per valori di default
	Storico.GetStorico();
}

Storico.GestStoricoList = function(res, err)
{
	if(!err) {
		if(res.list.length > 0) {

			var tmpArr = new Array();
			
			var listaDisp = "<input class='ButtonScelta' name='Dispositivo' type='radio' checked='checked' value='" + Msg.home["tuttiStorico"] + "'>" + Msg.home["tuttiStorico"] ;
			var arrElettr = new Array();
			$.each(res.list, function(indice, elettrodom) {		
				var nomeTmp = elettrodom["map"]["nome"];								
				if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_CATEGORY] != "12" &&
						elettrodom["map"][InterfaceEnergyHome.ATTR_APP_CATEGORY] != "14") 
				{
					// Qui aggiungo gli elementi della choice associati ai device fake ...
					listaDisp += "<br><input class='ButtonScelta' name='Dispositivo' type='radio' value='" + nomeTmp + "'>" + nomeTmp;
					
				}
				var tmpObj = new Object();
				tmpObj.nome = nomeTmp;
				tmpObj.pid = elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID];
				tmpObj.tipo = elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE];
				tmpArr.push(tmpObj);
			});
			
			Storico.DatiElettr(tmpArr);
			
			$("#SceltaDispositivo").html(listaDisp);
			
			Storico.periodoScelto = Storico.GIORNO;
			Storico.tipoUltimoPeriodo = Storico.GIORNO;
			Storico.dispositivoScelto = Msg.home["tuttiStorico"];
			if (Main.env == 0) console.log(80, Storico.MODULE, "dispositivoScelto = " + Storico.dispositivoScelto + " periodoScelto = " + Storico.periodoScelto);

			// metto data inizio e fine = ieri
			Storico.dataFine = new Date(GestDate.GetActualDate().getTime());
			Storico.dataFine.setDate(Storico.dataFine.getDate() - 1);
			Storico.dataFine.setHours(23);
			Storico.dataFine.setMinutes(59);
			Storico.dataInizio = new Date(Storico.dataFine.getTime());
			Storico.dataInizio.setHours(0);
			Storico.dataInizio.setMinutes(0);

			// gestisco caso giorno installazione == oggi, non ho dati nello storico
			if ((Storico.dataInizio.getTime() < Storico.installData.getTime()) && (Storico.dataFine.getTime() < Storico.installData.getTime())) {
				$("#Prec").hide();
				$("#Succ").hide();
				if (Main.env == 0) console.log(40, Storico.MODULE, "VisStorico : nessun dato");
				$("#StoricoGraph").html($(document.createElement('div')).attr('id', 'StoricoVuoto').text(Msg.home["noGrafStorico"]));
				//$("#StoricoGraph").html("<div id='StoricoVuoto'>" + Msg.home["noGrafStorico"] + "</div>");
				hideSpinner();
				return;
			}

			// imposta gestione scelta
			$("input[type=radio][name='Periodo']").change(Storico.SceltaPeriodo);
			$("input[type=radio][name='Dispositivo']").change(Storico.SceltaDispositivo);

			// gestisce frecce
			// per funzionare su iPad devo mettere un div sopra l'immagine della freccia
			// se non non e' cliccabile
			//$("#Prec").click(Storico.Precedente);
			//$("#Succ").click(Storico.Successivo);
			if (Storico.dataInizio.getTime() < Storico.installData.getTime()){
				$("#Prec").hide();
				// $("#Succ").hide();
			}

			// visualizza grafico per valori di default
			Storico.GetStorico();
		}
	}	
}

/*******************************************************************************
 * Elenco elettrodomestici per storico Potrebbe essere diverso da quello normale ?
 ******************************************************************************/
Storico.DatiElettr = function(val) {
	if (Main.env == 0) console.log(80, Storico.MODULE, "Storico.DatiElettr ");
	Storico.datiElettr = val;
	//hideSpinner();
	Storico.VisScelta();
}

Storico.DatiInizio = function(val) {
	if (Main.env == 0) console.log(80, Storico.MODULE, "Storico.DatiElettr ");
	Storico.installData = new Date(val);
	showSpinner();
	// leggo lista elettrodomestici
	InterfaceEnergyHome.GetElettrStorico(Storico.DatiElettr);
}

Storico.ResizePlot = function(event) {
	if (Storico.plot1 != null) {
		Storico.plot1.replot();
	}
}

/*******************************************************************************
 * Gestisce lo storico dei dati Crea la parte statica di visualizzazione
 ******************************************************************************/
Storico.GestStorico = function() {
	if (Main.env == 0) console.log(80, Storico.MODULE, "Storico.GestStorico");
	/*
	 * Creo il contenitore dei dati di report per la Gui e lo aggiungo alla
	 * pagina
	 */
	var divStorico = $("#Content");

	/*
	 * Controllo che il div di Report non sia giˆ stato riempito. Se non esiste
	 * lo inizializzo, se giˆ esiste lo visualizzo solamente
	 */

	if (divStorico.length == 0) {

		$("#Container").append($(document.createElement('div')).attr('id', 'Content'));

		$("#Content").html(Storico.htmlContent);
		$("#Content").show();

		// FIXME: must not bind on window but on the specific div. But it
		// doesn't seems to work!!!
		// FIXME: it doesn't work on IE7 so it has been commented out.
		if (Storico.enableResize){
			$(window).bind('resize', Storico.ResizePlot);
		}

		// leggo la data di installazione e i dispositivi una sola volta
		if (Storico.installData == null){
			InterfaceEnergyHome.GetInitialTime(Storico.DatiInizio);
		} else {
			Storico.VisScelta();
		}
	} else {
		$("#Content").show();
	}

}

Storico._setLimitAlertMsg = function(limitToDay, LimitOtherDay, toDay){
	
	var limitAlertMsg, isToDayToDay;
	switch (LimitOtherDay){
		case Storico.LIMITALERTGIORNO: 
			isToDayToDay = (Storico.dataInizio.getDate() == toDay.getDate()) && (Storico.dataInizio.getMonth() == toDay.getMonth()) && (Storico.dataInizio.getFullYear() == toDay.getFullYear());
			break;
		case Storico.LIMITALERTSETTIMANA:
			if ((Storico.dataInizio.getTime() < toDay.getTime()) && (toDay.getTime() < Storico.dataFine.getTime())){
				isToDayToDay = true;
			} else {
				isToDayToDay = false;
			}
			break;
		case Storico.LIMITALERTMESE:
			if ((Storico.dataInizio.getTime() < toDay.getTime()) && (toDay.getTime() < Storico.dataFine.getTime())){
				isToDayToDay = true;
			} else {
				isToDayToDay = false;
			}
			break;
		case Storico.LIMITALERTANNO:
			if ((Storico.dataInizio.getFullYear() == toDay.getFullYear()) && (toDay.getFullYear() == Storico.dataFine.getFullYear())){
				isToDayToDay = true;
			} else {
				isToDayToDay = false;
			}
			break;
	}
	
	if (isToDayToDay) {
		var tmpValue = limitToDay * Storico.LIMITPARAM;
		limitAlertMsg = (limitToDay - tmpValue);
	} else {
		var tmpValue = LimitOtherDay * Storico.LIMITPARAM;
		limitAlertMsg = (LimitOtherDay - tmpValue);
	}
	return Math.round(limitAlertMsg);
}