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
	ORE : -1,
	GIORNO : 0,
	SETTIMANA : 1,
	MESE : 2,
	ANNO : 3,
	LIMIT_ALERT_ORE: 60, 
	LIMIT_ALERT_GIORNO: 24, 
	LIMIT_ALERT_SETTIMANA: 7, 
	LIMIT_ALERT_MESE: 31, 
	LIMIT_ALERT_ANNO: 12, 
	LIMITPARAM: 0.3
};

Storico.ExitStorico = function() {
	
	if (Main.env == 0) console.log(20, Storico.MODULE, "ExitStorico");
	Main.ResetError();
	Storico.datiCosto = null;
	Storico.datiConsumo = null;
	if (Storico.enableResize){
		$(window).unbind('resize', Storico.ResizePlot);
	}

	InterfaceEnergyHome.Abort();
	$("#Content").hide();
}

Storico.DateIsSame = function(xDate, yDate) {
	
	var day1 = xDate.getDate();
	var month1 = xDate.getMonth();
	var year1 = xDate.getFullYear();
	
	// oggi = GestDate.GetActualDate();
	var day2 = yDate.getDate();
	var month2 = yDate.getMonth();
	var year2 = yDate.getFullYear();
	
	if ((day1 == day2) & (month1 == month2) && (year1 == year2)){
		return true;
	} else {
		return false;
	}
}

// controllo se la data specificata rientra tra la data di installazione e quella attuale
Storico.DateIsValid = function(valDate, periodo) {
	
	var tmpI, domani, ora, a;
	// nel caso di giorno tengo conto dell'ora, altrimenti no
	if (periodo == Storico.GIORNO) {
		ora = new Date(GestDate.GetActualDate().getTime());
		a = new Date(valDate.getTime());
		a.setMinutes(0); 
		// fine dell'ora attuale
		
		// controllo per data di installazione
		a.setHours(a.getHours() + 1);
		if (a.getTime() < Storico.installData.getTime())
			return false;
		
		// controllo per ora attuale aumento ancora di 1 perche' potrie avere dati incompleti per le ultime 2 ore
		a.setHours(a.getHours() + 1);
		if (a.getTime() > ora)
			return false;

		return true;
	} else {
		// nel caso di giorno il giorno di installazione e quello attuale
		// possono avere dati nulli e non devo segnalare errore
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
	var plotBandsArr = new Array();
	var rapp, barW, max, disp;
	var dati1 = null;
	var dati2 = null;
	var maxConsumo = 0;
	var maxCosto = 0;
	var totConsumo = 0;
	var totCosto = 0;
	var yOffset = 0;
	//Variabile che mi serve per capire se sto analizzando i dati del giorno corrente oppure no
	var limitOdierno = null;
	var timeToDay, timeValTicker;
	var nTickskWh, nTicksE, formatStr;

	var cat = new Array();
	var daysOfWeek = Msg.daysOfWeek;
	var daysOfWeekCompleto = Msg.daysOfWeekCompleto;
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
	var aTicks = 0;
	// se smart info metto tutti
	if (Storico.GetDispTipo(Storico.dispositivoScelto) == InterfaceEnergyHome.SMARTINFO_APP_TYPE){
		disp = Msg.home["tuttiStorico"];
	} else {
		disp = Msg.home["dispGrafStorico"] + ": " + Storico.dispositivoScelto;
	}

	if (Storico.periodoScelto == Storico.ORE) {

		valTicker = new Date(Storico.dataInizio.getTime());
		valTicker.setHours(valTicker.getHours() + 1);
		
		labelX = Msg.home["minutiLabelStorico"];
		titolo = Msg.home["oreGrafStorico"] + ": " + (Storico.dataInizio.getHours()) + ":00  "
				+ (Storico.dataInizio.getHours() + 1) + ":00 " + (Storico.dataInizio.getHours() + 2) + ":00 di " + Storico.dataInizio.getDate() + "  "
				+ Msg.mesiAbbrev[Storico.dataInizio.getMonth()].toUpperCase()
				+ " " + Storico.dataInizio.getFullYear() + "  -  " + disp;
		nTickskWh = 6;
		nTicksE = 6;
		formatStr = "%.2f";

		timeToDay = Math.round(toDay.getFullYear().toString() + toDay.getMonth().toString() + toDay.getDate().toString() + toDay.getHours().toString());
		timeValTicker = Math.round(valTicker.getFullYear().toString() + valTicker.getMonth().toString() + valTicker.getDate().toString() + valTicker.getHours().toString());
		
		if (timeToDay == timeValTicker){
			limitOdierno = toDay.getMinutes();
		} else {
			limitOdierno = null;
		}
		
	} else if (Storico.periodoScelto == Storico.GIORNO) {
		labelX = Msg.home["oraLabelStorico"];
		titolo = Msg.home["giornoGrafStorico"] + ": " + Storico.dataInizio.getDate() + "  "
				+ Msg.mesiAbbrev[Storico.dataInizio.getMonth()].toUpperCase()
				+ " " + Storico.dataInizio.getFullYear() + "  -  " + disp;
		nTickskWh = 6;
		nTicksE = 6;
		formatStr = "%.2f";

		timeToDay = Math.round(toDay.getFullYear().toString() + toDay.getMonth().toString() + toDay.getDate().toString());
		timeValTicker = Math.round(valTicker.getFullYear().toString() + valTicker.getMonth().toString() + valTicker.getDate().toString());
		
		if (timeToDay == timeValTicker){
			limitOdierno = toDay.getHours();
		} else {
			limitOdierno = null;
		}
	} else if (Storico.periodoScelto == Storico.ANNO) {
		labelX = Msg.home["meseLabelStorico"];
		titolo = Msg.home["daGrafStorico"] + " "
				+ Msg.mesiAbbrev[Storico.dataInizio.getMonth()].toUpperCase()
				+ " " + Storico.dataInizio.getFullYear() + " "
				+ Msg.home["aGrafStorico"] + " "
				+ Msg.mesiAbbrev[Storico.dataFine.getMonth()].toUpperCase()
				+ " " + Storico.dataFine.getFullYear() + "  -  " + disp;
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
		labelX = Msg.home["giornoLabelStorico"];
		titolo = Msg.home["daGrafStorico"] + " " + Storico.dataInizio.getDate() + " "
				+ Msg.mesiAbbrev[Storico.dataInizio.getMonth()].toUpperCase() + " " 
				+ Storico.dataInizio.getFullYear() + " "
				+ Msg.home["aGrafStorico"] + " " + Storico.dataFine.getDate() + " "
				+ Msg.mesiAbbrev[Storico.dataFine.getMonth()].toUpperCase() + " " 
				+ Storico.dataFine.getFullYear() + "  -  " + disp;
		if (Storico.periodoScelto == Storico.MESE){
			aTicks = -80;
		}
		nTickskWh = 6;
		nTicksE = 6;
		formatStr = "%.1f";

		if (Storico.periodoScelto == Storico.MESE){
			if ((Storico.dataInizio.getTime() < toDay.getTime()) && (toDay.getTime() < Storico.dataFine.getTime())){
				limitOdierno = toDay.getDate();
			} else {
				limitOdierno = null;
			}
		} else { //SETTIMANA
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
	

	var festaNazionale = -1;
	var iMese = 0;
	var arrFestDay = new Array();
	
	//festaNazionale = Define.festivi.indexOf([valTicker.getDate(), valTicker.getMonth()]);
	// controlla se giorno di festa nazionale
	for (i = 0; i < Define.festivi.length; i++){
		if ((valTicker.getDate() == Define.festivi[i][0]) && (valTicker.getMonth() == Define.festivi[i][1])){
			festaNazionale = 0;
			break;
		}
	}
	
	// creo array con i dati e creo le label per i tickers
	// controllo se i dati sono nei limiti altrimenti li sposto (massimo valore + 5%)
	// se dato nullo lo segnalo (lo ignoro se prima della data di installazione o da oggi in poi)
	for (i = 0; i < dati1.length; i++) {
		// se uno dei 2 e' null metto a null anche l'altro
		if ((dati1[i] == null) && (limitOdierno) && (i < limitOdierno)) {
			dati1[i] = 0;
			if (Storico.DateIsValid(valTicker, Storico.periodoScelto)){
				Storico.datiConsumoNulli = true;
			}
		} else {
			if ((limitOdierno) && (i > limitOdierno)){
				dati1[i] = 0;
			} else {
				var controllo = dati1[i] / 10;
				if (controllo > maxConsumo){
					maxConsumo = controllo;
				}
				//Arrotondo al centesimo per i consumi (kWh)
				dati1[i] = Utils.RoundTo(dati1[i] / 1000, 2); // da w a kWh
				totConsumo += dati1[i];
			}
		}
		if ((tipo == Storico.COSTI_CONSUMI) && (dati2 != null)) {
			if ((dati2[i] == null) && (limitOdierno) && (i < limitOdierno)) {
				dati2[i] = 0;
				if (Storico.DateIsValid(valTicker, Storico.periodoScelto)){
					Storico.datiCostoNulli = true;
				}
			} else {
				if ((limitOdierno) && (i > limitOdierno)){
					dati2[i] = 0;
				} else {
					if (Main.enablePV){
						dati2[i] = Utils.RoundTo(dati2[i] / 1000, 2); // da w a kWh
						totCosto += dati2[i];
					} else {
						//Arrotondo al millesimo di euro
						dati2[i] = Utils.RoundTo(dati2[i], 3);
						if (dati1[i] == 0){
							dati2[i] = 0;
						}
						if (dati2[i] > maxCosto){
							maxCosto = dati2[i];
						}
						totCosto += dati2[i];
					}
				}
			}
		}
		ruota = 0;
		min = 0;
		
		var festDay = 0;
		// label ticks diverso a seconda di cosa sto visualizzando
		if (Storico.periodoScelto == Storico.ORE) {
			var minute = (valTicker.getMinutes() + i) * 2;
			minute = minute % 60;
			if (minute < 10){
				minute = '0' + minute;
			}
			if (i%2 == 0){
				cat.push(minute);
			} else {
				cat.push('');
			}
		} else if (Storico.periodoScelto == Storico.GIORNO) {
			var hour = valTicker.getHours() + i;
			if (hour < 10){
				hour = '0' + hour.toString();
			} else {
				hour = hour.toString()
			}
			cat.push(hour);
			if ((valTicker.getDay() == 0) || (valTicker.getDay() == 6) || (festaNazionale >= 0)){
				festDay = 1;
			}
		} else if (Storico.periodoScelto == Storico.SETTIMANA) {
			var day = valTicker.getDate();
			var month = valTicker.getMonth();
			valTicker.setDate(valTicker.getDate() + 1); // settimana
			
			var tmpArray = new Array(valTicker.getDate(), valTicker.getMonth());
			var iSett = 0;
			festaNazionale = -1;
			while ((festaNazionale < 0) && (iSett < Define.festivi.length)){
				if ((Define.festivi[iSett][0] == tmpArray[0]) && (Define.festivi[iSett][1] == tmpArray[1])){
					festaNazionale = 0;
				}
				iSett++;
			}

			if ((valTicker.getDay() == 0) || (valTicker.getDay() == 6) || (festaNazionale >= 0)){
				if (valTicker.getDay() != 0){
					arrFestDay[valTicker.getDay()] = {'value': 1, 'day': valTicker.getDay()-1};
				} else {
					arrFestDay[valTicker.getDay()] = {'value': 1, 'day': 6};
				}
			} else {
				arrFestDay[valTicker.getDay()] = {'value': 0, 'day': valTicker.getDay()-1};
			}
			cat.push(daysOfWeek[i] + "<br />(" + day + "-" + Msg.mesiCompleto[month] + ")");
		} else if (Storico.periodoScelto == Storico.MESE) {
			tickers[i] = valTicker.getDate() + "-" + (valTicker.getMonth() + 1);
			
			var tmpArray = new Array(valTicker.getDate(), valTicker.getMonth());
			var iSett = 0;
			festaNazionale = -1;
			while ((festaNazionale < 0) && (iSett < Define.festivi.length)){
				if ((Define.festivi[iSett][0] == tmpArray[0]) && (Define.festivi[iSett][1] == tmpArray[1])){
					festaNazionale = 0;
				}
				iSett++;
			}

			if ((valTicker.getDay() == 0) || (valTicker.getDay() == 6) || (festaNazionale >= 0)){
				arrFestDay[iMese] = {'value': 1, 'day': iMese};
			} else {
				arrFestDay[iMese] = {'value': 0, 'day': iMese};
			}
			
			cat.push(valTicker.getDate());
			valTicker.setDate(valTicker.getDate() + 1); // mese
			iMese++;
		} else {
			cat.push(Msg.mesiCompleto[valTicker.getMonth()]);
			valTicker.setMonth(valTicker.getMonth() + 1); // anno
		}
	}
	

	// label ticks diverso a seconda di cosa sto visualizzando
	if (Storico.periodoScelto == Storico.ORE) {
		max = 23;
		yOffset = 20;
		XTitleText = Msg.storico[0];
	} else if (Storico.periodoScelto == Storico.GIORNO) {
		max = 23;
		yOffset = 20;
		XTitleText = Msg.storico[1];
		if (festDay == 1){
			plotBandsArr = [{ from: -0.5, to: 24, color: 'rgba(0,255,0,0.05)'}]
		} else {
			plotBandsArr = [{ from: -0.5, to: 7.5, color: 'rgba(0,255,0,0.05)'},
			                { from: 7.5, to: 18.5, color: 'rgba(255,0,0,0.05)'},
			                { from: 18.5, to: 24, color: 'rgba(0,255,0,0.05)'}];
		}
	} else if (Storico.periodoScelto == Storico.SETTIMANA) {
		ruota = -45;
		yOffset = 17;
		XTitleText = Msg.storico[2];
		
		for(i = 0; i < arrFestDay.length; i++){
			if (arrFestDay[i].value == 1){
				plotBandsArr.push({ from: (arrFestDay[i].day-0.5), to: (arrFestDay[i].day+0.5), color: 'rgba(0,255,0,0.05)'});
			} else {
				plotBandsArr.push({ from: (arrFestDay[i].day-0.5), to: (arrFestDay[i].day+0.5), color: 'rgba(255,0,0,0.05)'});
			}
		}
	} else if (Storico.periodoScelto == Storico.MESE) {
		ruota = -45;
		min = 1;
		max = 31;
		yOffset = 30;
		XTitleText = Msg.storico[3];
		
		for(i = 0; i < arrFestDay.length; i++){
			if (arrFestDay[i].value == 1){
				plotBandsArr.push({ from: (arrFestDay[i].day-0.5), to: (arrFestDay[i].day+0.5), color: 'rgba(0,255,0,0.05)'});
			} else {
				plotBandsArr.push({ from: (arrFestDay[i].day-0.5), to: (arrFestDay[i].day+0.5), color: 'rgba(255,0,0,0.05)'});
			}
		}
	} else {
		ruota = -45;
		min = 1;
		max = 12;
		yOffset = 30;
		XTitleText = Msg.storico[4];
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
			limitAlertMsg = Storico._setLimitAlertMsg(toDay.getHours(), Storico.LIMIT_ALERT_GIORNO, toDay);
			break;
		}
		case Storico.SETTIMANA:{
			limitAlertMsg = Storico._setLimitAlertMsg(toDay.getDay(), Storico.LIMIT_ALERT_SETTIMANA, toDay);
			break;
		}
		case Storico.MESE:{
			limitAlertMsg = Storico._setLimitAlertMsg(toDay.getDate(), Storico.LIMIT_ALERT_MESE, toDay);
			break;
		}
		case Storico.ANNO:{
			limitAlertMsg = Storico._setLimitAlertMsg(toDay.getMonth() + 1, Storico.LIMIT_ALERT_ANNO, toDay);
			break;
		}
		default:{
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
	
	if (Storico.periodoScelto == Storico.ORE) {
		
		Highcharts.setOptions({
			colors : [ 'blue', "#21e700", '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4' ]
		});
			graficoStorico = new Highcharts.Chart({
					chart: {renderTo : 'StoricoGraph',
						    events: {load: function(event) {
					 						hideSpinner();}}, 
				 			type: 'areaspline',
			            	spacingBottom: 30},
			        title: {text : titolo,
						 	textAlign : 'left',
						 	show : true},
			        subtitle: {text : ''},
			        legend: {layout: 'vertical',
			            	 align: 'left',
			            	 verticalAlign: 'top',
			            	 x: 150,
			            	 y: 100,
			            	 floating: true,
			            	 borderWidth: 1,
			            	 backgroundColor: '#FFFFFF'},
			         xAxis : { labels : { rotation : ruota,
								 		  align : 'center',
								 		  style : { font : 'normal 10px Verdana, sans-serif'},
								 		  y : yOffset},
							   tickInterval : 1,
							   ticks : tickers,
							   useHTML : false,
							   title : { align : 'middle',
								   		 text : XTitleText,
								   		 rotation : 0,
								   		 offset : 45,
								   		 style : { color : "black"}},
							   categories : cat,
					           plotBands: plotBandsArr},
					 yAxis : [ { min : 0,
						 	     title : { text : titleX,
						 	    	 	   style : { color : 'blue'}},
						 	     labels : { formatter : function() {return this.value + 'kWh';},
						 	    	 	    style : { color : 'blue'}}}, 
						 	   { gridLineWidth: 1,
						 	     min: 0,
						 	     title : { text : titleY1,
						 	    	       style : { color : '#21e700'}},
						 	     labels : { formatter : function() { return this.value + ' kWh';},
						 	    	        style : {color : 'green'}},
						 	     opposite : true}],
			         tooltip: {formatter: function() {
			                				return '<b>'+ this.series.name +'</b><br/>'+this.x +': '+ this.y;}},
			         plotOptions: {areaspline: {fillOpacity: 0.5}},
			         credits: {enabled: false},
			         series: [{ name : titleX + ' ' + Utils.RoundTo(totConsumo, 2) + ' kWh',
							    data : dati1}, 
							  { name : titleY1 + ' ' + Utils.RoundTo(totCosto, 2) + ' Euro',
							    data : dati2}]});
	} else {
		if (Main.enablePV){
			Highcharts.setOptions({
				colors : [ 'blue', "#21e700", '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4' ]
			});
			if ((Storico.device == 0) || (Storico.device == null)){
				graficoStorico = new Highcharts.Chart({
					chart : {renderTo : 'StoricoGraph',
							 events: {load: function(event) {
									 			hideSpinner();}}, 
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
							  useHTML : true,
							  title : { align : 'middle',
								 	    text : XTitleText,
								 	    rotation : 0,
								 	    offset : 45,
								 	    style : { color : "black"}},
							 categories : cat,
				             plotBands: plotBandsArr},
					yAxis : [ { min : 0,
								title : { text : titleX,
										  style : { color : 'blue'}},
								labels : { formatter : function() {return this.value + ' kWh';},
								style : { color : 'blue'}}}, 
							  { gridLineWidth: 1,
								min: 0,
								title : { text : titleY2,
										  style : { color : '#21e700'}},
								labels : { formatter : function() { return this.value + ' kWh';},
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
					tooltip : { 
						formatter : function() {
	
											var str = '';
											switch (this.series.xAxis.userOptions.title.text){
											case 'Ora del giorno':
												str = 'Alle ' + this.x + ':00 ';
												break;
											case 'Giorno della settimana':
												var arrDays = this.x.split("<br />");
												var idx = daysOfWeek.indexOf(arrDays[0]);
												var giornoToView = daysOfWeekCompleto[idx];
												str = '' + giornoToView + ' ';
												break;
											case 'Giorno del mese':
												str = 'Il giorno ' + this.x + ' ';
												break;
											case 'Mese dell\'anno':
												str = 'Il mese di ' + this.x + ' ';
												break;
											}
											if (this.series.index == 0){
												if (this.y > 1){
													str = str + ' hai consumato ' + Highcharts.numberFormat(this.y, 2) + ' kWh';
												} else {
													str = str + ' hai consumato ' + Math.round(this.y * 1000) + ' Wh';
												}
											} else {
												if (this.y > 1){
													str = str + ' hai prodotto ' + Highcharts.numberFormat(this.y, 2) + ' kWh';
												} else {
													str = str + ' hai prodotto ' + Math.round(this.y * 1000) + ' Wh';
												}
											}
											return str;
									}
							  },
					plotOptions : { column : { pointPadding : 0.2,
											   borderWidth : 0},
								    series: {cursor: 'pointer',
										     point: {events: {click: function() {
										         Storico.tipoUltimoPeriodo = Storico.periodoScelto;
										         var tmpPeriodo = Storico.periodoScelto - 1;
										         var dataInizio = Storico.dataInizio;
										         var dataFine = Storico.dataFine;
										         if (Storico.periodoScelto == Storico.SETTIMANA){
										        	 var arrDays = this.category.split("<br />");
										        	 var dayOfMonth = arrDays[1].substring(1);
										        	 dayOfMonth = dayOfMonth.substring(0, dayOfMonth.length - 1);
										        	 var tmpDayOfMonth = dayOfMonth.split("-");
										        	 var myDayOfMonth = parseInt(tmpDayOfMonth[0]);
										        	 dataInizio.setDate(myDayOfMonth);
													 dataInizio.setHours(0);
										        	 dataFine.setDate(myDayOfMonth);
										        	 dataFine.setHours(23);
										         } else if (Storico.periodoScelto == Storico.MESE){
										        	 tmpPeriodo--;
										        	 dataInizio.setDate(this.x + 1);
													 dataInizio.setHours(0);
										        	 dataFine.setDate(this.x + 1);
										        	 dataFine.setHours(23);
										         } else if (Storico.periodoScelto == Storico.ANNO){
										        	 dataInizio.setMonth(this.x);
										        	 dataInizio.setDate(1);
													 dataInizio.setHours(0);
										        	 dataFine.setMonth(this.x);
										        	 if (dataFine.getDate() < 31){
										        		 dataFine.setDate(dataFine.getDate() - 1);
											        	 dataFine.setHours(12);
										        	 }
										         }
										         if (Storico.periodoScelto > 0){
										        	 Storico.SceltaPeriodo(event, tmpPeriodo, dataInizio, dataFine);
										         }
										       }}}
					}},
					series : [{ name : titleX + ' ' + Utils.RoundTo(totConsumo, 2) + ' kWh',
								data : dati1}, 
							  { name : titleY2 + ' ' + Utils.RoundTo(totCosto, 2) + ' kWh',
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
							  useHTML : true,
							  title : { align : 'middle',
								  		text : XTitleText,
								  		rotation : 0,
								  		offset : 45,
								  		style : {color : "black"}},
							  categories : cat,
					          plotBands: plotBandsArr},
					yAxis : [{ min : 0,
							   title : { text : titleX,
								   		 style : { color : 'blue'}},
							   labels : {formatter : function() { return this.value + ' kWh';},
								   		 style : {color : 'blue'}}}],
					legend : { layout : 'vertical',
							   backgroundColor : '#FFFFFF',
							   align : 'left',
							   verticalAlign : 'top',
							   x : 100,
							   y : 20,
							   floating : true,
							   shadow : true},
					tooltip : { formatter : function() {
												var str = '';
												switch (this.series.xAxis.userOptions.title.text){
												case 'Ora del giorno':
													str = 'Alle ' + this.x + ':00 ';
													break;
												case 'Giorno della settimana':
													var arrDays = this.x.split("<br />");
													var idx = daysOfWeek.indexOf(arrDays[0]);
													var giornoToView = daysOfWeekCompleto[idx];
													str = '' + giornoToView + ' ';
													break;
												case 'Giorno del mese':
													str = 'Il giorno ' + this.x + ' ';
													break;
												case 'Mese dell\'anno':
													str = 'Il mese di ' + this.x + ' ';
													break;
												}
												if (this.series.index == 0){
													if (this.y > 1){
														str = str + ' hai consumato ' + Highcharts.numberFormat(this.y, 2) + ' kWh';
													} else {
														str = str + ' hai consumato ' + Math.round(this.y * 1000) + ' Wh';
													}
												} else {
													str = str + ' hai speso ' + Highcharts.numberFormat(this.y, 2) + ' Euro';
												}
												return str;}},
							plotOptions : { column : { pointPadding : 0.2,
													   borderWidth : 0},
										    series: {cursor: 'pointer',
												     point: {events: {click: function() {
												         Storico.tipoUltimoPeriodo = Storico.periodoScelto;
												         var tmpPeriodo = Storico.periodoScelto - 1;
												         var dataInizio = Storico.dataInizio;
												         var dataFine = Storico.dataFine;
												         if (Storico.periodoScelto == Storico.SETTIMANA){
												        	 var arrDays = this.category.split("<br />");
												        	 var dayOfMonth = arrDays[1].substring(1);
												        	 dayOfMonth = dayOfMonth.substring(0, dayOfMonth.length - 1);
												        	 var tmpDayOfMonth = dayOfMonth.split("-");
												        	 var myDayOfMonth = parseInt(tmpDayOfMonth[0]);
												        	 dataInizio.setDate(myDayOfMonth);
															 dataInizio.setHours(0);
												        	 dataFine.setDate(myDayOfMonth);
												        	 dataFine.setHours(23);
												         } else if (Storico.periodoScelto == Storico.MESE){
												        	 tmpPeriodo--;
												        	 dataInizio.setDate(this.x + 1);
															 dataInizio.setHours(0);
												        	 dataFine.setDate(this.x + 1);
												        	 dataFine.setHours(23);
												         } else if (Storico.periodoScelto == Storico.ANNO){
												        	 dataInizio.setMonth(this.x);
												        	 dataInizio.setDate(1);
															 dataInizio.setHours(0);
												        	 dataFine.setMonth(this.x);
												        	 if (dataFine.getDate() < 31){
												        		 dataFine.setDate(dataFine.getDate() - 1);
													        	 dataFine.setHours(12);
												        	 }
												         }
												         if (Storico.periodoScelto > 0){
												        	 Storico.SceltaPeriodo(event, tmpPeriodo, dataInizio, dataFine);
												         }
												       }}}
							}},
							series : [{ name : titleX + ' ' + Utils.RoundTo(totConsumo, 2) + ' kWh',
										data : dati1}]});
			}
		} else {
			Highcharts.setOptions({
				colors : [ 'blue', "#FFA500", '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4' ]
			});
			graficoStorico = new Highcharts.Chart({
				chart : {renderTo : 'StoricoGraph',
						 events: {load: function(event) {hideSpinner();}}, 
						 type : 'column'},
				title : {text : titolo,
						 textAlign : 'left',
						 show : true},
				subtitle : {text : ''},
				credits : false,
				xAxis : {labels : {rotation : ruota,
								   align : 'center',
								   style : {font : 'normal 10px Verdana, sans-serif'},
								   y : yOffset},
						 tickInterval : 1,
						 ticks : tickers,
						 title : {align : 'middle',
							 	  text : XTitleText,
							 	  rotation : 0,
							 	  offset : 45,
							 	  style : {color : "black"}},
						 categories : cat,
			             plotBands: plotBandsArr},
				yAxis : [{min : 0,
						  title : {text : 'Consumi ',
								   style : {color : 'blue'}},
						  labels : {formatter : function() {return this.value + ' kWh';},
								    style : {color : 'blue'}}
						 },{gridLineWidth: 1,
							min: 0,
				            title : {text : 'Costi',
									 style : {color : '#FFA500'}},
							labels : {formatter : function() {return this.value + ' Euro';},
									  style : {color : '#FFA500'}},
							opposite : true
						 }],
				legend : {layout : 'vertical',
						  backgroundColor : '#FFFFFF',
						  align : 'left',
						  verticalAlign : 'top',
						  x : 100,
						  y : 20,
						  floating : true,
						  shadow : true},
				tooltip : {formatter : function() { 
											var str = '';
											switch (this.series.xAxis.userOptions.title.text){
												case 'Ora del giorno':
													 str = 'Alle ' + this.x + ':00 ';
													 break;
												case 'Giorno della settimana':
													 var arrDays = this.x.split("<br />");
													 var idx = daysOfWeek.indexOf(arrDays[0]);
													 var giornoToView = daysOfWeekCompleto[idx];
													 str = '' + giornoToView + ' ';
													 break;
												case 'Giorno del mese':
													 str = 'Il giorno ' + this.x + ' ';
													 break;
												case 'Mese dell\'anno':
													 str = 'Il mese di ' + this.x + ' ';
													 break;
											}
											if (this.series.index == 0){
												if (this.y > 1){
													str = str + ' hai consumato ' + Highcharts.numberFormat(this.y, 2) + ' kWh';
												} else {
													str = str + ' hai consumato ' + Math.round(this.y * 1000) + ' Wh';
												}
											} else {
												str = str + ' hai speso ' + Highcharts.numberFormat(this.y, 2) + ' Euro';
											}
											return str;
							}},
						plotOptions : { column : { pointPadding : 0.2,
											   borderWidth : 0},
								    series: {cursor: 'pointer',
										     point: {events: {click: function() {
										         Storico.tipoUltimoPeriodo = Storico.periodoScelto;
										         var tmpPeriodo = Storico.periodoScelto - 1;
										         var dataInizio = Storico.dataInizio;
										         var dataFine = Storico.dataFine;
										         if (Storico.periodoScelto == Storico.SETTIMANA){
										        	 var arrDays = this.category.split("<br />");
										        	 var dayOfMonth = arrDays[1].substring(1);
										        	 dayOfMonth = dayOfMonth.substring(0, dayOfMonth.length - 1);
										        	 var tmpDayOfMonth = dayOfMonth.split("-");
										        	 var myDayOfMonth = parseInt(tmpDayOfMonth[0]);
										        	 dataInizio.setDate(myDayOfMonth);
													 dataInizio.setHours(0);
										        	 dataFine.setDate(myDayOfMonth);
										        	 dataFine.setHours(23);
										         } else if (Storico.periodoScelto == Storico.MESE){
										        	 tmpPeriodo--;
										        	 dataInizio.setDate(this.x + 1);
													 dataInizio.setHours(0);
										        	 dataFine.setDate(this.x + 1);
										        	 dataFine.setHours(23);
										         } else if (Storico.periodoScelto == Storico.ANNO){
										        	 dataInizio.setMonth(this.x);
										        	 dataInizio.setDate(1);
													 dataInizio.setHours(0);
										        	 dataFine.setMonth(this.x);
										        	 if (dataFine.getDate() < 31){
										        		 dataFine.setDate(dataFine.getDate() - 1);
											        	 dataFine.setHours(12);
										        	 }
										         }
										         if (Storico.periodoScelto > 0){
										        	 Storico.SceltaPeriodo(event, tmpPeriodo, dataInizio, dataFine);
										         }
										       }}}
					}},
					series : [{name : titleX + ' ' + Utils.RoundTo(totConsumo, 2) + ' kWh',
						   data : dati1
						  },{
						   name : titleY1 + ' ' + Utils.RoundTo(totCosto, 2) + ' Euro',
						   data : dati2,
						   yAxis : 1}]
			});
		}
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
					rtnResult =  Storico.datiElettr[i].pid;
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
Storico.SceltaPeriodo = function(event, newValue, newDataInizio, newDataFine) {  //Storico.periodoScelto
	
	if (newValue == null){
		Storico.periodoScelto = parseInt($("input[type=radio][name='Periodo']:checked").val());
	} else {
		Storico.periodoScelto = newValue; 
		var radios = $('input:radio[name=Periodo]');
		radios.prop('checked', false);
		radios.filter('[value='+newValue+']').prop('checked', true);
	}
	
	if (Main.env == 0) console.log(20, Storico.MODULE, "========== SceltaPeriodo: precedente = " + Storico.tipoUltimoPeriodo + " nuovo = " + Storico.periodoScelto);
	if (Storico.periodoScelto != Storico.tipoUltimoPeriodo) {
		$("#Prec").show();
		$("#Succ").show();
		switch (Storico.periodoScelto) {
			case Storico.ORE:
				Storico.dataFine = new Date(GestDate.GetActualDate().getTime());
				Storico.dataFine.setHours(Storico.dataFine.getHours() + 1);
				Storico.dataFine.setMinutes(59);
				Storico.dataInizio = new Date(GestDate.GetActualDate().getTime());
				Storico.dataInizio.setHours(Storico.dataInizio.getHours() - 1);
				Storico.dataInizio.setMinutes(0);
				break;
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
				if ((newDataInizio != null) && (newDataFine != null)){
					Storico.dataInizio = newDataInizio;
					Storico.dataFine = newDataFine;
				} else {
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
					// ultimo giorno del mese (giorno prima del primo giorno del mese successivo)
					Storico.dataFine = new Date(Storico.dataInizio.getTime());
					Storico.dataFine.setMonth(Storico.dataFine.getMonth() + 1);
					Storico.dataFine.setDate(Storico.dataFine.getDate() - 1);
					Storico.dataInizio.setHours(0);
					Storico.dataFine.setHours(12);
				}
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
		case Storico.ORE:
			 Storico.dataInizio.setHours(Storico.dataInizio.getHours() - 1);
			 Storico.dataFine.setHours(Storico.dataFine.getHours() - 1);
			 break;
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
		case Storico.ORE:
			 Storico.dataInizio.setHours(Storico.dataInizio.getHours() + 1);
			 Storico.dataFine.setHours(Storico.dataFine.getHours() + 1);
			 break;
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
	if (Storico.periodoScelto != Storico.ORE){
		ieri.setHours(0);
	} else {
		ieri.setHours(ieri.getHours() + 1);
	}
	ieri.setMinutes(0);
	ieri.setSeconds(0);
	ieri.setMilliseconds(0);
	
	if ((Storico.dataFine.getTime()) >= ieri.getTime()){
		$("#Succ").hide();
	} else {
		$("#Succ").show();
	}
	Storico.GetStorico();
}

// la prima volta default tutti e giorno, poi seleziona gli ultimi valori selezionati
Storico.VisScelta = function() {
	var tmp;

	// imposto smart info per tutti, utile per evitare errori quando si visualizza la pagina dello storico senza aver installato lo SmartInfo
	var tutti = $(document.createElement('input')).attr('name', 'Dispositivo').attr('class', 'ButtonScelta').attr('id', 'rd_Disp_SI').attr('type', 'radio').attr('checked', 'checked').attr('value', Msg.home["tuttiStorico"]);
	var lblForTutti = $(document.createElement('label')).attr('for', 'rd_Disp_SI').html(Msg.home["tuttiStorico"]);
	
	if (Storico.datiElettr != null) {
		for (i = 0; i < Storico.datiElettr.length; i++) {
			// creo elenco dei dispositivi selezionabili
			if (Storico.datiElettr[i].tipo == InterfaceEnergyHome.SMARTINFO_APP_TYPE) {
				$("#rd_Disp_SI").attr('value', Storico.datiElettr[i].nome);
			} else {
				$("#SceltaDispositivo").append($(document.createElement('br')));
				tmp = $(document.createElement('input')).attr('name', 'Dispositivo').attr('id', 'rd_Disp_'+i).attr('type', 'radio').attr('class', 'ButtonScelta').attr('value', Storico.datiElettr[i].nome).appendTo($("#SceltaDispositivo"));
				
				lblForTmp = $(document.createElement('label')).attr('for', 'rd_Disp_'+i).html(Storico.datiElettr[i].nome).appendTo($('#SceltaDispositivo'));
			}
		}
	}
	$("#SceltaDispositivo").prepend(lblForTutti);
	$("#SceltaDispositivo").prepend(tutti);
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
		hideSpinner();
		return;
	}

	// imposta gestione scelta
	$("input[type=radio][name='Periodo']").change(Storico.SceltaPeriodo);
	$("input[type=radio][name='Dispositivo']").change(Storico.SceltaDispositivo);

	// gestisco frecce
	// per funzionare su iPad devo mettere un div sopra l'immagine della freccia se non non e' cliccabile
	$("#Prec").click(Storico.Precedente);
	$("#Succ").click(Storico.Successivo);
	if (Storico.dataInizio.getTime() < Storico.installData.getTime()){
		$("#Prec").hide();
	}

	// visualizza grafico per valori di default
	Storico.GetStorico();
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
	
	// Creo il contenitore dei dati di report per la Gui e lo aggiungo alla pagina
	var divContent = $("#Content");

	/*
	 * Controllo che il div di Report non sia gi� stato riempito. Se non esiste
	 * lo inizializzo, se gi� esiste lo visualizzo solamente
	 */

	if (divContent.length == 0) {

		$("#Container").append($(document.createElement('div')).attr('id', 'Content'));
		divContent = $("#Content");
		
		var divContentTitle = $(document.createElement('div')).attr('class', 'ContentTitle').html(Msg.home["titoloStorico"]).appendTo(divContent);
		var divStorico = $(document.createElement('div')).attr('id', 'Storico').appendTo(divContent);
		var imgStoricoBg = $(document.createElement('img')).attr('id', 'StoricoBg').attr('src', Define.home["sfondo_sx"]).appendTo(divStorico);
		var divMsgStorico = $(document.createElement('div')).attr('id', 'MsgStorico').hide().appendTo(divStorico);
		var divLblkWhStorico = $(document.createElement('div')).attr('id', 'LabelStoricokWh').appendTo(divStorico);
		var divLblEuroStorico = $(document.createElement('div')).attr('id', 'LabelStoricoEuro').appendTo(divStorico);
		var divPrec = $(document.createElement('div')).attr('id', 'Prec').appendTo(divStorico).append($(document.createElement('img')).attr('src', Define.home["frecciaPrec"])).append($(document.createElement('div')).attr('id', 'LabelPrec'));
		var divSucc = $(document.createElement('div')).attr('id', 'Succ').appendTo(divStorico).append($(document.createElement('img')).attr('src', Define.home["frecciaSucc"])).append($(document.createElement('div')).attr('id', 'LabelSucc'));
		var divStoricoGraphContainer = $(document.createElement('div')).attr('id', 'StoricoGraphContainer').appendTo(divStorico).append($(document.createElement('div')).attr('id', 'StoricoGraph'));
		var divSepStorico = $(document.createElement('div')).attr('id', 'SepStorico').appendTo(divStorico);
		var divStoricoScelta = $(document.createElement('div')).attr('id', 'StoricoScelta').appendTo(divStorico);
		var divTitoloSceltaPeriodo = $(document.createElement('div')).attr('id', 'TitoloSceltaPeriodo').html(Msg.home["periodoStorico"]).appendTo(divStoricoScelta);
		var divSceltaPeriodo = $(document.createElement('div')).attr('id', 'SceltaPeriodo').appendTo(divStoricoScelta);
		//var rdOre = $(document.createElement('input')).attr('name', 'Periodo').attr('id', 'rd_Periodo_Ore').attr('type', 'radio').attr('class', 'ButtonScelta').attr('value', '-1').appendTo(divSceltaPeriodo);
		//var lblForrdOre = $(document.createElement('label')).attr('for', 'rd_Periodo_Ore').html(Msg.home["oreStorico"]).appendTo(divSceltaPeriodo);
		//	divSceltaPeriodo.append($(document.createElement('br')));
		var rdGiorno = $(document.createElement('input')).attr('name', 'Periodo').attr('id', 'rd_Periodo_Giorno').attr('type', 'radio').attr('checked', 'checked').attr('class', 'ButtonScelta').attr('value', '0').appendTo(divSceltaPeriodo);
		var lblForrdGiorno = $(document.createElement('label')).attr('for', 'rd_Periodo_Giorno').html(Msg.home["giornoStorico"]).appendTo(divSceltaPeriodo);
			divSceltaPeriodo.append($(document.createElement('br')));
		var rdSettimana = $(document.createElement('input')).attr('name', 'Periodo').attr('id', 'rd_Periodo_Sett').attr('type', 'radio').attr('class', 'ButtonScelta').attr('value', '1').appendTo(divSceltaPeriodo);
		var lblForrdSettimana = $(document.createElement('label')).attr('for', 'rd_Periodo_Sett').html(Msg.home["settStorico"]).appendTo(divSceltaPeriodo);
			divSceltaPeriodo.append($(document.createElement('br')));
		var rdMese = $(document.createElement('input')).attr('name', 'Periodo').attr('id', 'rd_Periodo_Mese').attr('type', 'radio').attr('class', 'ButtonScelta').attr('value', '2').appendTo(divSceltaPeriodo);
		var lblForrdMese = $(document.createElement('label')).attr('for', 'rd_Periodo_Mese').html(Msg.home["meseStorico"]).appendTo(divSceltaPeriodo);
			divSceltaPeriodo.append($(document.createElement('br')));
		var rdAnno = $(document.createElement('input')).attr('name', 'Periodo').attr('id', 'rd_Periodo_Anno').attr('type', 'radio').attr('class', 'ButtonScelta').attr('value', '3').appendTo(divSceltaPeriodo);
		var lblForrdAnno = $(document.createElement('label')).attr('for', 'rd_Periodo_Anno').html(Msg.home["annoStorico"]).appendTo(divSceltaPeriodo);
			divSceltaPeriodo.append($(document.createElement('br')));
		var divTitoloSceltaDispositivo = $(document.createElement('div')).attr('id', 'TitoloSceltaDispositivo').html(Msg.home["periodoStorico"]).appendTo(divStoricoScelta);
		var divSceltaDispositivo = $(document.createElement('div')).attr('id', 'SceltaDispositivo').appendTo(divStoricoScelta);

		divContent.show();

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
		divContent.show();
	}

}

Storico._setLimitAlertMsg = function(limitToDay, LimitOtherDay, toDay){
	
	var limitAlertMsg, isToDayToDay;
	switch (LimitOtherDay){
		case Storico.LIMIT_ALERT_GIORNO: 
			isToDayToDay = (Storico.dataInizio.getDate() == toDay.getDate()) && (Storico.dataInizio.getMonth() == toDay.getMonth()) && (Storico.dataInizio.getFullYear() == toDay.getFullYear());
			break;
		case Storico.LIMIT_ALERT_SETTIMANA:
			if ((Storico.dataInizio.getTime() < toDay.getTime()) && (toDay.getTime() < Storico.dataFine.getTime())){
				isToDayToDay = true;
			} else {
				isToDayToDay = false;
			}
			break;
		case Storico.LIMIT_ALERT_MESE:
			if ((Storico.dataInizio.getTime() < toDay.getTime()) && (toDay.getTime() < Storico.dataFine.getTime())){
				isToDayToDay = true;
			} else {
				isToDayToDay = false;
			}
			break;
		case Storico.LIMIT_ALERT_ANNO:
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