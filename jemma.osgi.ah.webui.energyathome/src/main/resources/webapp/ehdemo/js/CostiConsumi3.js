
var potenza = {
	value : null
};

CostiConsumi.GestConsumi = function() {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'GestConsumi', 'Entro!');

	$("#CostiConsumi").show();

	if ((CostiConsumi.mode == CostiConsumi.COSTI) || (CostiConsumi.mode == CostiConsumi.FOTOVOLTAICO)) {

		CostiConsumi.mode = CostiConsumi.CONSUMI;
		$("#TitoloCostiConsumi").html(Msg.home["titoloConsumi"]);

		$('#consigliTurnOn').hide();
		
		if (Main.enablePV){
			$("#ProduzioneAttualeTitolo").hide();
			$("#ProduzioneAttuale").hide();
			$("#ReteAttualeTitolo").hide();
			$("#ReteAttuale").hide();
			$("#divFrecceProd").hide();
			$("#divFrecceConsumi").hide();
			$("#divFrecceRete").hide();
			$("#FrecceFV").hide();
			$("#CostoAttualeImg").show();
			
			$("#CostoConsumoAttualeTitolo").css('top', '2%').css('left', '2%');
			$("#CostoConsumoAttuale").css('top', '2%').css('left', '2%');
			$("#CostoConsumoAttuale").css('border-bottom', '1px solid #0B0B96');

			$("#percIAC").hide();

			$("#Grafico").hide();
			$("#Grafico2").hide();

			$("#CostoTConsumoMax").show();

			$("#CostoConsumoOdierno").show();
			$("#CostoConsumoPrevisto").show();
			
			$("#IndicatoreSopra").show();
			$("#IndicatoreMedia").show();
			$("#IndicatoreSotto").show();
			$("#IndicatorePaddingLeft").show();
			
			$("#IndicatoreTitolo").css('top', '5%').css('left', '55%').width('40%').height('10%');
			$("#Indicatore").css('top', '15%').css('left', '55%');
			$("#Indicatore").width('17%').height('150%');

			$('#ConsumoIndicatoreImg').css('top', '15%').width('80%');
			$("#IndicatoreSopra").css('top', '0%');
			$("#IndicatoreMedia").css('top', '25%');
			$("#IndicatoreSotto").css('top', '50%');
			$("#IndicatorePaddingLeft").css('top', '0%');

			$("#IndicatoreTitolo").text(Msg.home["indicatoreCosti"]);

			$("#InfoFeed").show();
			$("#InfoFeedTitolo").hide();
			$("#MarqueeContainer").hide();
		}

		$("#CostoAttualeImg").hide();
		$("#DettaglioCosto").show();
		$("#TariffaImgDiv").hide();
		$("#TariffaPos").hide();
		$("#DettaglioSuddivisioneCosti").show();
		$('#ConsumoIndicatoreImg').show();
		$('#ConsumoIACIndicatoreImg').hide();

		$("#ValConsumoAttuale").show();
		if (navigator.userAgent.indexOf('MSIE 7.0') > -1){
			//Sono in Internet Explorer 7.0
			var conMeterDiv = document.getElementById('ConsumoAttualeMeter');
			conMeterDiv.style.display = 'inline';
			var graphConsOdiernoDiv = document.getElementById('GraficoConsumoOdierno');
			graphConsOdiernoDiv.style.display = 'inline';
		} else {
			$("#ConsumoAttualeMeter").show();
			$("#ConsumoAttualeMeter div").show();
			$("#ConsumoAttualeMeter img").show();
			$("#GraficoConsumoOdierno").show();
		}
		$("#DettaglioConsumoMaggiore").show();
		$("#ConsumoIndicatoreImg").show();

		$("#CostoConsumoAttualeTitolo").text(Msg.home["titoloConsumi"]);
		$("#CostoTConsumoMaxTitolo").text(Msg.home["consumoMaggiore"]);
		$("#InfoTitolo").text(Msg.home["consumoMensile"]);
	}

	if (CostiConsumi.timerPotenzaCC == null){
		CostiConsumi.GetDatiPotenzaCC();
	}
	if (CostiConsumi.consumoGiornaliero == null){
		CostiConsumi.GetDatiConsumiCC();
	} else {
		$("#DettaglioCostoConsumoOdierno").html('');
		if (CostiConsumi.consumoOdierno != null){
			if (Main.env == 0) console.log('exception in CostiConsumi3.js - in CostiConsumi.GestConsumi 1 method: ');
			$("#DettaglioCostoConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + CostiConsumi.consumoOdierno + " kWh </b>");
		} else {
			$("#DettaglioCostoConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + Msg.home["datoNonDisponibile"] + "</b>");
		}
		$("#DettaglioCostoConsumoPrevisto").html('');
		if (CostiConsumi.consumoPrevisto != null){
			if (Main.env == 0) console.log('exception in CostiConsumi3.js - in CostiConsumi.GestConsumi 2 method: ');
			$("#DettaglioCostoConsumoPrevisto").html(Msg.home["consumoPrevisto"] + ":<br><br> <b>" + CostiConsumi.consumoPrevisto + " kWh </b>");
		} else {
			$("#DettaglioCostoConsumoPrevisto").html(Msg.home["consumoPrevisto"] + ":<br><br> <b>" + Msg.home["datoNonDisponibile"] + "</b>");
		}
	}
	if (Main.env == 0) console.log('CostiConsumi3.js', 'GestConsumi', 'Esco!');
}

CostiConsumi.ExitConsumi = function() {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'ExitConsumi', 'Entro!');

	if (CostiConsumi.timerPotenzaCC != null) {
		clearInterval(CostiConsumi.timerPotenzaCC); 
	 	CostiConsumi.timerPotenzaCC = null; 
	} 
	if (CostiConsumi.timerPowerMeter != null) {
		clearInterval(CostiConsumi.timerPowerMeter); 
	  	CostiConsumi.timerPowerMeter = null; 
	}
	if (CostiConsumi.timerConsumi != null) {
		clearInterval(CostiConsumi.timerConsumi); 
	  	CostiConsumi.timerConsumi = null; 
	}
	if (CostiConsumi.timerBlink != null) {
		clearInterval(CostiConsumi.timerBlink); 
	  	CostiConsumi.timerBlink = null; 
	}
	  
	CostiConsumi.consumoGiornaliero = null;
	CostiConsumi.suddivisioneCostiRender = false;
	
	if(chartPie!=null)
		chartPie.destroy();

	Main.ResetError();
	$("#CostiConsumi").hide();

	if (Main.env == 0) console.log('CostiConsumi3.js', 'ExitConsumi', 'Esco!');
}

CostiConsumi.GetDatiPotenzaCC = function() {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'GetDatiPotenzaCC', 'Entro!');

	// non tolgo togliere messaggio errore da piattaforma
	if (InterfaceEnergyHome.visError != InterfaceEnergyHome.ERR_CONN_SERVER){
		Main.ResetError();
	}
	if (InterfaceEnergyHome.mode > 0) {
		try {
			//InterfaceEnergyHome.objService.getAttribute(CostiConsumi.DatiPotenzaAttuale, InterfaceEnergyHome.POTENZA_TOTALE);
			InterfaceEnergyHome.objService.getAppliancesConfigurations(CostiConsumi.DatiPotenzaAttualeCC);
		} catch (err) {
			if (Main.env == 0) console.log('exception in CostiConsumi3.js - in CostiConsumi.GetDatiPotenzaCC method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetDatiPotenzaCC", err);
		}
	} else {
		// per test
		if (CostiConsumi.potenzaAttuale.value == null){
			CostiConsumi.potenzaAttuale.value = 0;
		}
		CostiConsumi.potenzaAttuale.value += 200;
		if (CostiConsumi.potenzaAttuale.value > (Define.home["tipoContatore"][Main.contatore] + 2000)){
			CostiConsumi.potenzaAttuale.value = 0;
		}
		CostiConsumi.DatiPotenzaAttualeCC(CostiConsumi.potenzaAttuale, null);
	}
	if (Main.env == 0) console.log('CostiConsumi3.js', 'GetDatiPotenzaCC', 'Esco!');
}

CostiConsumi.DatiPotenzaAttualeCC = function(result, err) {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'DatiPotenzaAttualeCC', 'Entro!');

	if (err != null){
		if (Main.env == 0) console.log('exception in CostiConsumi3.js - in CostiConsumi.DatiPotenzaAttualeCC method: ', err);
		InterfaceEnergyHome.GestErrorEH("DatiPotenzaAttualeCC", err);
	} else if (result != null){
		//CostiConsumi.potenzaAttuale.value = result.value;
		//prelevare dato smart info
		$.each(result.list,function(indice, elettrodom) {
			if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.SMARTINFO_APP_TYPE) {
				if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_CATEGORY] == "12") {
					CostiConsumi.SmartInfo = elettrodom["map"];
					device_value = CostiConsumi.SmartInfo.device_value;
					if (device_value != undefined) {
						CostiConsumi.potenzaAttuale.value = device_value.value.value;
					}
				}
				if (Main.env == 0)
					console.log('COSTICONSUMI3', 'SmartInfo - '+CostiConsumi.SmartInfo);
			}
		});
	} else {
		CostiConsumi.potenzaAttuale.value = null;
	}
	CostiConsumi.SetConsumoImgCC();
	if (Main.env == 0) console.log('CostiConsumi3.js', 'DatiPotenzaAttualeCC', 'Esco!');
}

CostiConsumi.GetElettrodomestici = function() {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'GetElettrodomestici', 'Entro!');

	if (InterfaceEnergyHome.mode > 0) {
		try {
			InterfaceEnergyHome.objService.getAppliancesConfigurations(CostiConsumi.DatiElettrodomesticiCB);
		} catch (err) {
			if (Main.env == 0) console.log('exception in CostiConsumi3.js - in CostiConsumi.GetElettrodomestici method: ', err);
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
		CostiConsumi.DatiElettrodomesticiCB(val, null);
	}

	if (Main.env == 0) console.log('CostiConsumi3.js', 'GetElettrodomestici', 'Esco!');
}

CostiConsumi.DatiElettrodomesticiCB = function(result, err) {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'DatiElettrodomesticiCB', 'Entro!');

	if (err != null){
		if (Main.env == 0) console.log('exception in CostiConsumi3.js - in CostiConsumi.DatiElettrodomesticiCB method: ', err);
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
				if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_CATEGORY] == "12"){
					CostiConsumi.SmartInfo = elettrodom["map"];
				}
				if (Main.env == 0) console.log('COSTICONSUMI1', 'SmartInfo - ');
				if (Main.env == 0) console.log(CostiConsumi.SmartInfo);
			} else {
				if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE] == undefined){
					elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value = 0;
				} else {
					var val = parseFloat(elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value);
					elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value = val;
				}
				CostiConsumi.listaElettr[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
				if (Main.env == 0) console.log('COSTICONSUMI1', 'Eldo - ');
				if (Main.env == 0) console.log(CostiConsumi.listaElettr[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]]);
				if (Main.env == 0) console.log(elettrodom["map"]);
			}
		});
	}
	
	CostiConsumi.DatiMaxElettr();
	if (Main.env == 0) console.log('CostiConsumi3.js', 'DatiElettrodomesticiCB', 'Esco!');
}

CostiConsumi.DatiMaxElettr = function() {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'DatiMaxElettr', 'Entro!');

	// eventuale trascodifica dato cerco l'elettrodomestico con consumo istantaneo maggiore, escluso smart info
	var listaFiltrata = $.map(CostiConsumi.listaElettr,function(elettro, index) {
															if (elettro[InterfaceEnergyHome.ATTR_APP_AVAIL] == 2) {
																return elettro;
															}});
	listaFiltrata.sort(function(a, b) {
		var firstElettrConsumo = a[InterfaceEnergyHome.ATTR_APP_VALUE].value.value;
		var secondElettrConsumo = b[InterfaceEnergyHome.ATTR_APP_VALUE].value.value;
		//Se uno dei due elettrodomestici in sort � una lavatrice (whitegood) e il consumo � sotto a 1W, normalizzo a 0
		if (a[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WHITEGOOD_APP_TYPE) {
			firstElettrConsumo = (firstElettrConsumo < 1) ? 0 : firstElettrConsumo;
		}
		if (b[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WHITEGOOD_APP_TYPE) {
			secondElettrConsumo = (secondElettrConsumo < 1) ? 0 : secondElettrConsumo;
		}
		return secondElettrConsumo - firstElettrConsumo;
	})
	CostiConsumi.maxConsumoElettr = listaFiltrata[0];
	CostiConsumi.VisConsumoMaggiore();
	if (Main.env == 0) console.log('CostiConsumi3.js', 'DatiMaxElettr', 'Esco!');
}

// visualizza elettrodomestico che in questo momento sta consumando di piu'
CostiConsumi.VisConsumoMaggiore = function() {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'VisConsumoMaggiore', 'Entro!');

	if (CostiConsumi.maxConsumoElettr != null) {
		if (CostiConsumi.maxConsumoElettr[InterfaceEnergyHome.ATTR_APP_VALUE].value.value == 0) {
			$("#DettaglioConsumoMaggiore").html("<span id='MsgConsumoMaggiore'></span>");
			$("#MsgConsumoMaggiore").text(Msg.home["maxDisp0"]);
		} else {
			$("#DettaglioConsumoMaggiore").html("<span id='TestoConsumoMaggiore'></span><img id='ConsumoMaggioreImg' src=''>");

			// metto immagine del device che sta consumando di piu'
			$("#ConsumoMaggioreImg").attr("src",DefinePath.imgDispPath + CostiConsumi.maxConsumoElettr[InterfaceEnergyHome.ATTR_APP_ICON]);
			// il consumo e' in watt
			$("#TestoConsumoMaggiore").text(CostiConsumi.maxConsumoElettr[InterfaceEnergyHome.ATTR_APP_NAME] + " (" + Math.round(CostiConsumi.maxConsumoElettr[InterfaceEnergyHome.ATTR_APP_VALUE].value.value) + " W)");
			if (CostiConsumi.dimMaxDispImg == -1) {
				wDiv = $("#ConsumoMaggioreImg").width();
				hDiv = $("#ConsumoMaggioreImg").height();

				// imposto dimensioni e offset img in px
				if (wDiv > hDiv){
					CostiConsumi.dimMaxDispImg = (hDiv * 0.9);
				} else {
					CostiConsumi.dimMaxDispImg = (wDiv * 0.9);
				}
			}
			$("#ConsumoMaggioreImg").width(CostiConsumi.dimMaxDispImg);
			$("#ConsumoMaggioreImg").height(CostiConsumi.dimMaxDispImg);
		}
	} else {
		$("#DettaglioConsumoMaggiore").html("<span id='MsgConsumoMaggiore'></span>");
		$("#MsgConsumoMaggiore").text(Msg.home["noMaxDisp"]);
	}
	
	if (!CostiConsumi.suddivisioneCostiRender){
		CostiConsumi.GetSuddivisioneConsumi();
	}

	if (Main.env == 0) console.log('CostiConsumi3.js', 'VisConsumoMaggiore', 'Esco!');
}

CostiConsumi.GetDatiConsumiCC = function() {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'GetDatiConsumiCC', 'Entro!');

	Main.ResetError();

	start = new Date(Main.dataAttuale.getTime());
	start.setHours(0);
	end = Main.dataAttuale.getTime();

	if (InterfaceEnergyHome.mode > 1){ 
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getAttributeData(CostiConsumi.DatiConsumoGiornalieroCbCC,
															InterfaceEnergyHome.PID_TOTALE, 
															InterfaceEnergyHome.CONSUMO, 
															start.getTime(), end,
															InterfaceEnergyHome.HOUR, true, 
															InterfaceEnergyHome.DELTA);
		} catch (err) {
			if (Main.env == 0) console.log(80, CostiConsumi.MODULE, "err = ");
			if (Main.env == 0) console.log(80, CostiConsumi.MODULE, err);
			if (Main.env == 0) console.log('exception in CostiConsumi3.js - in CostiConsumi.GetDatiConsumiCC method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetDatiConsumoGiornaliero", err);
		}
	} else {
		// per test, copio per il numero ore attuale
		hours = Main.dataAttuale.getHours();
		val = ConsumoGiornaliero;
		val.list = val.list.slice(0, hours);

		CostiConsumi.DatiConsumoGiornalieroCbCC(val, null);
	}
	if (Main.env == 0) console.log('CostiConsumi3.js', 'GetDatiConsumiCC', 'Esco!');
}

CostiConsumi.DatiConsumoGiornalieroCbCC = function(result, err) {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'DatiConsumoGiornalieroCbCC', 'Entro!');

	if (err != null){
		InterfaceEnergyHome.GestErrorEH("DatiConsumoGiornaliero", err);
	}
	CostiConsumi.consumoGiornaliero = null;
	if ((err == null) && (result != null)){
		CostiConsumi.consumoGiornaliero = result.list;
	}

	CostiConsumi.VisIndicatoreConsumiCC();
	CostiConsumi.GetConsumoOdiernoCC();
	if (Main.env == 0) console.log('CostiConsumi3.js', 'DatiConsumoGiornalieroCbCC', 'Esco!');
}

CostiConsumi.GetConsumoOdiernoCC = function() {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'GetConsumoOdierno', 'Entro!');
	var start = Main.dataAttuale.getTime();
	var indConsumoOdierno = 0;
	var attuale, oraAttuale, minAttuale, consumo, val, consumoLista;

	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			var res = InterfaceEnergyHome.objService.getAttributeData(
					CostiConsumi.DatiConsumoOdiernoCbCC,
					InterfaceEnergyHome.PID_TOTALE,
					InterfaceEnergyHome.CONSUMO, start, start,
					InterfaceEnergyHome.DAY, true, InterfaceEnergyHome.DELTA);
		} catch (err) {
			if (Main.env == 0) console.log(20, CostiConsumi.MODULE, "error: ");
			if (Main.env == 0) console.log(20, CostiConsumi.MODULE, err);
			if (Main.env == 0) console.log('exception in CostiConsumi3.js - in CostiConsumi.GetConsumoOdiernoCC method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetConsumoOdiernoCC", err);
		}
	} else {
		// per test
		indConsumoOdierno += 1;
		if (indConsumoOdierno == ConsumoOdierno.length){
			indConsumoOdierno = 0;
		}
		consumoLista = ConsumoOdierno[indConsumoOdierno];

		// prendo percentuale del costo in base all'ora
		attuale = GestDate.GetActualDate();
		oraAttuale = attuale.getHours();
		minAttuale = attuale.getMinutes();
		consumo = 0;
		for (var i = 0; i < oraAttuale; i++) {
			consumo += consumoLista.list[i];
		}
		// aggiungo percentuale in base ai minuti dell'ora attuale
		consumo += consumoLista.list[oraAttuale] * (minAttuale / 60);
		val = {"list" : [ consumo ]};
		CostiConsumi.DatiConsumoOdiernoCbCC(val, null);
	}

	if (Main.env == 0) console.log('CostiConsumi3.js', 'GetConsumoOdiernoCC', 'Esco!');
}

CostiConsumi.DatiConsumoOdiernoCbCC = function(result, err) {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'DatiConsumoOdiernoCbCC', 'Entro!');

	if (err != null){
		InterfaceEnergyHome.GestErrorEH("DatiConsumoOdiernoCbCC", err);
	}
	$("#DettaglioCostoConsumoOdierno").html('');
	if (result){
		if (result.list[0] != null) {
			CostiConsumi.consumoOdierno = (result.list[0] / 1000).toFixed(1);
			$("#DettaglioCostoConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + CostiConsumi.consumoOdierno + " kWh </b>");
		} else {
			CostiConsumi.consumoOdierno = null;
			$("#DettaglioCostoConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + Msg.home["datoNonDisponibile"] + " </b>");
		}
	} else {
		CostiConsumi.consumoOdierno = null;
		$("#DettaglioCostoConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + Msg.home["datoNonDisponibile"] + " </b>");
	}

	CostiConsumi.GetConsumoMedioCC();
	if (Main.env == 0) console.log('CostiConsumi3.js', 'DatiConsumoOdiernoCbCC', 'Esco!');
}

CostiConsumi.GetConsumoMedioCC = function() {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'GetConsumoMedioCC', 'Entro!');

	var weekDay = Main.dataAttuale.getDay() + 1; // js comincia da 0, java da 1
	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getWeekDayAverage(
					CostiConsumi.DatiConsumoMedioCbCC,
					InterfaceEnergyHome.PID_TOTALE,
					InterfaceEnergyHome.CONSUMO, weekDay);
		} catch (err) {
			if (Main.env == 0) console.log(20, CostiConsumi.MODULE, "error: ");
			if (Main.env == 0) console.log(20, CostiConsumi.MODULE, err);
			if (Main.env == 0) console.log('exception in CostiConsumi3.js - in CostiConsumi.GetConsumoMedio method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetConsumoMedio", err);
		}
	} else {
		// per test
		var val = ConsumoMedio;
		CostiConsumi.DatiConsumoMedioCbCC(val, null);
	}
	if (Main.env == 0) console.log('CostiConsumi3.js', 'GetConsumoMedioCC', 'Esco!');
}

CostiConsumi.DatiConsumoMedioCbCC = function(result, err) {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'DatiConsumoMedioCbCC', 'Entro!');
	
	if (err != null)
		InterfaceEnergyHome.GestErrorEH("DatiConsumoMedioCC", err);

	if ((err == null) && (result != null)) {
		CostiConsumi.consumoMedio = result.list;
	}

	CostiConsumi.VisIndicatoreConsumiCC();
	CostiConsumi.GetConsumoPrevistoCC();

	if (Main.env == 0) console.log('CostiConsumi3.js', 'DatiConsumoMedioCbCC', 'Esco');
}

CostiConsumi.VisIndicatoreConsumiCC = function() {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'VisIndicatoreConsumiCC', 'Entro!');

	var arrayMedio = new Array();
	if(CostiConsumi.consumoGiornaliero && CostiConsumi.consumoMedio){
		arrayMedio = CostiConsumi.consumoMedio.slice(0, CostiConsumi.consumoGiornaliero.length);
	}

	var perc = 0;
	var Totodierno = null;
	var Totmedio = null;
	
	if ((CostiConsumi.consumoMedio != null) && (CostiConsumi.consumoGiornaliero != null) && (CostiConsumi.consumoOdierno != null)) {
		Totodierno = 0;
		Totmedio = 0;

		$.each(CostiConsumi.consumoGiornaliero, function(index, consumo) {
			if (consumo != null) {
				Totodierno += consumo;
				Totmedio += arrayMedio[index];
			}
		});
		if (Totodierno != null && Totmedio > 0) {
			//perc = Totodierno * 1000 / Totmedio;
			perc = Totodierno / Totmedio;
			
			if (Main.env == 0) console.log('CostiConsumi3.js', 'VisIndicatoreConsumiCC', 'Totmedio = ' + Totmedio);
			if (Main.env == 0) console.log('CostiConsumi3.js', 'VisIndicatoreConsumiCC', 'Totodierno = ' + Totodierno);
			if (Main.env == 0) console.log('CostiConsumi3.js', 'VisIndicatoreConsumiCC', 'perc = ' + perc);
			
			if (perc > 2){
				perc = 2;
			}
		}
		$('#ConsumoIndicatoreImg').gauge("value", perc);
	}
	if (Main.env == 0) console.log('CostiConsumi3.js', 'VisIndicatoreConsumiCC', 'Esco!');
}

CostiConsumi.GetConsumoPrevistoCC = function() {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'GetConsumoPrevistoCC', 'Entro!');

	var start = Main.dataAttuale.getTime();

	if (InterfaceEnergyHome.mode > 1){ 
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getForecast(CostiConsumi.DatiConsumoPrevistoCbCC, InterfaceEnergyHome.PID_TOTALE,
													   InterfaceEnergyHome.CONSUMO, start, InterfaceEnergyHome.MONTH);
		} catch (err) {
			if (Main.env == 0) console.log(20, CostiConsumi.MODULE, "error: ");
			if (Main.env == 0) console.log(20, CostiConsumi.MODULE, err);
			if (Main.env == 0) console.log('exception in CostiConsumi3.js - in CostiConsumi.GetConsumoPrevistoCC method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetConsumoPrevistoCC", err);
		}
	} else {
		// per test
		var val = ConsumoPrevisto;
		CostiConsumi.DatiConsumoPrevistoCbCC(val, null);
	}

	if (Main.env == 0) console.log('CostiConsumi3.js', 'GetConsumoPrevistoCC', 'Esco!');
}

CostiConsumi.DatiConsumoPrevistoCbCC = function(result, err) {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'DatiConsumoPrevistoCbCC', 'Entro!');
	var txt;

	if (err != null){
		if (Main.env == 0) console.log(20, CostiConsumi.MODULE, "error: ");
		if (Main.env == 0) console.log(20, CostiConsumi.MODULE, err);
		InterfaceEnergyHome.GestErrorEH("DatiConsumoPrevistoCbCC", err);
	}

	if ((err == null) && (result != null)){
		CostiConsumi.consumoPrevisto = Math.round(result / 1000); // da w a kW
		txt = Math.round(CostiConsumi.consumoPrevisto) + " kWh";
	} else {
		CostiConsumi.consumoPrevisto = null;
		txt = Msg.home["datoNonDisponibile"];
	}

	$("#DettaglioCostoConsumoPrevisto").html('');
	$("#DettaglioCostoConsumoPrevisto").html(Msg.home["consumoPrevisto"] + ":<br><br> <b>" + txt + "</b>");

	//hideSpinner();
	$("#CostiConsumi").css("z-index", "10");
	
	/* Verifico la connessione al server e carico gli RSS feed */
	if (InterfaceEnergyHome.mode == 0 || InterfaceEnergyHome.visError == InterfaceEnergyHome.ERR_CONN_SERVER) {
		CostiConsumi.InitfeedSim();
	} else {
		var script = document.createElement("script");
		script.src = "https://www.google.com/jsapi?callback=CostiConsumi.loadFeed";
		script.type = "text/javascript";
		document.body.appendChild(script);
	}

	if (CostiConsumi.timerPotenzaCC == null){
		CostiConsumi.timerPotenzaCC = setInterval("CostiConsumi.GetDatiPotenzaCC()", CostiConsumi.TIMER_UPDATE_POWER_METER);
	}
	
	if (Main.env == 0) console.log('CostiConsumi3.js', 'DatiConsumoPrevistoCbCC', 'Esco!');
}

CostiConsumi.SetConsumoImgCC = function() {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'SetConsumoImgCC', 'Entro!');

	$("#ValConsumoAttuale").html('');
	if (CostiConsumi.potenzaAttuale.value == null) {
		val = 0;
		$("#ValConsumoAttuale").html(Msg.home["datoNonDisponibile"]);
	} else {
		val =CostiConsumi.potenzaAttuale.value;
		$("#ValConsumoAttuale").html(Math.floor(val) + " W"); //.toFixed(3)
	}

	val = val / 1000.0;

	// segnalo sovraccarico (zona gialla) e sovraccarico grave(zona rossa) dello speedometer
	if (val > Define.home["contatoreOk"][Main.contatore]) {
		if (val > Define.home["contatoreWarn"][Main.contatore]){
			$("#ValConsumoAttuale").css("color", "red");
		} else {
			$("#ValConsumoAttuale").css("color", "orange");
		}
		
		if (CostiConsumi.timerBlink == null) {
			$("#ValConsumoAttuale").addClass("invisibleDiv")
			CostiConsumi.timerBlink = setInterval("CostiConsumi.BlinkVal()", CostiConsumi.TIMER_BLINK);
		}
	} else {
		clearInterval(CostiConsumi.timerBlink);
		CostiConsumi.timerBlink = null;
		$("#ValConsumoAttuale").css("color", "black");
		$("#ValConsumoAttuale").removeClass("invisibleDiv");
	}
	
	$('#ConsumoAttualeMeter').speedometer("value", val, "kW");
	CostiConsumi.GetElettrodomestici();

	if (Main.env == 0) console.log('CostiConsumi3.js', 'SetConsumoImgCC', 'Esco!');
}

CostiConsumi.BlinkVal = function() {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'BlinkVal', 'Entro!');
	$("#ValConsumoAttuale").toggleClass("invisibleDiv");
	if (Main.env == 0) console.log('CostiConsumi3.js', 'BlinkVal', 'Esco!');

}

CostiConsumi.GetSuddivisioneConsumi = function() {

	showSpinner();
	var myDate = new Date(Main.dataAttuale.getTime());
	myDate.setDate(myDate.getDate() -30);//Mi sposto indietro di 30 giorni.
	var start = new Date(myDate.getTime());
	var end = new Date(Main.dataAttuale.getTime());

	CostiConsumi.suddivisioneCostiRender = true;
	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getAttributeData(CostiConsumi.DatiSuddivisioneConsumiCb, 
															InterfaceEnergyHome.CONSUMO, 
															start.getTime(), 
															end.getTime(), 
															InterfaceEnergyHome.DAY, true, 
															InterfaceEnergyHome.DELTA);
		} catch (err) {
			InterfaceEnergyHome.GestErrorEH("GetSuddivisioneConsumi", err);
		}
	} else {
		// per test
		//var ind = Math.round(Math.random() * SuddivisioneConsumi.length);
		CostiConsumi.DatiSuddivisioneConsumiCb(SuddivisioneConsumi, null);
	}
}

CostiConsumi.DatiSuddivisioneConsumiCb = function(result, err) {

	var listaConsumi = new Array();
	var consumiTotale = 0;
	var altriConsumi = 0;
	var ConsumiSmartinfo = 0;
	var controlSumListaConsumi = 0;

	var retVal = null;
	if (err != null){
		InterfaceEnergyHome.GestErrorEH("DatiSuddivisioneConsumiCb", err);
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
								consumiTotale += sum;
								if (sum > 0){
									controlSumListaConsumi++;
								}
								listaConsumi.push(new Array(CostiConsumi.listaElettr[indexResult][InterfaceEnergyHome.ATTR_APP_NAME], sum));
							}
						}
					} else {
						var sumSmartInfo = 0;
						if (Main.env == 0) console.log('sumSmartInfo', sumSmartInfo);
						for (var i = 0; i < element.list.length; i++){
							var vSmartInfo = element.list[i];
							if (Main.env == 0) console.log('vSmartInfo', vSmartInfo);
							if (vSmartInfo != null){
								sumSmartInfo += vSmartInfo;
								if (Main.env == 0) console.log('sumSmartInfo', sumSmartInfo);
							}
						};
						ConsumiSmartinfo += sumSmartInfo;
						if (Main.env == 0) console.log('ConsumiSmartinfo', ConsumiSmartinfo);
					}
				});

		if (ConsumiSmartinfo) {
			altriConsumi = ConsumiSmartinfo - consumiTotale;
			if (Main.env == 0) console.log('altriConsumi', altriConsumi);
		}

		if (altriConsumi > 0){
			controlSumListaConsumi++;
			listaConsumi.push(new Array("Altro", altriConsumi));
		} else {
			controlSumListaConsumi++;
			listaConsumi.push(new Array("Altro", 0));
		}
		
		if (controlSumListaConsumi == 0){
			//Se tutti gli elementi inseriti in listaConsumi hanno valore 0, non mostro il grafico.
			listaConsumi = new Array();
		}
		if (Main.env == 0) console.log('listaConsumi', listaConsumi);
		
		$("#Grafico").show();
		$("#GraficoConsumoOdierno").hide();
		$("#DettaglioSuddivisioneCosti").show();

		if (listaConsumi.length > 0) {
			// Radialize the colors
				CostiConsumi.renderPieGraph = true;
				Highcharts.getOptions().colors = $.map(
						Highcharts.getOptions().colors, function(color) {
							return {radialGradient : {cx : 0.5, cy : 0.3, r : 0.7},
									stops : [[ 0, color ],
											 [ 1, Highcharts.Color(color).brighten(-0.3).get('rgb') ]] 
								    };
						});
	
				// Build the chart
				chartPie = new Highcharts.Chart({
					chart : {
						renderTo : 'DettaglioSuddivisioneCosti',
						events: {
			                load: function(event) {
			                	hideSpinner();
			                }
			            }, 
						plotBackgroundColor : null,
						plotBorderWidth : null,
						plotShadow : false
					},
					colors : [ '#9ba4f9', '#003f8f', '#5362f5', '#00868f', '#8de2ff', '#06f1ff', '#00cfdc', '#005b79', '#0032c6', '#0095c6' ],
					title : {
						text : ""
					},
					tooltip : {
						formatter : function() {
							/*var modificatore = '1'; 
							for(var i=0;i<2;i++)
							    modificatore += "0"; 
							modificatore = parseInt(modificatore,10)
							var valEuro = Math.round(this.y*(modificatore))/(modificatore); 
							*/
							return '<b>' + this.point.name + '</b>: ' + Math.floor(this.percentage) + ' % - ' + Math.floor(this.y / 1000) + ' kWh ';
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
						name : 'Lista dei consumi',
						data : listaConsumi
					} ]
				});
		} else {
			$("#DettaglioSuddivisioneCosti").text("Dati non disponibili");
		}
	} else {
		$("#DettaglioSuddivisioneCosti").html("<div id='SuddivisioneCostiVuoto'>" + Msg.home["suddivisioneVuoto"] + "</div>");
	}
}

/** Funzione lanciata al caricamento dello script google per gli RSS * */

CostiConsumi.loadFeed = function() {
	if (Main.env == 0) console.log('CostiConsumi3.js', 'loadFeed', 'Entro!');
	google.load("feeds", "1", {"callback" : CostiConsumi.launchFeed});
	if (Main.env == 0) console.log('CostiConsumi3.js', 'loadFeed', 'Esco!');
}

CostiConsumi.launchFeed = function() {
	CostiConsumi.Initfeed(0);
}

/*******************************************************************************
 * gestisce il caricamento degli RSS feed nell'array CostiConsumi.notizie
 ******************************************************************************/

CostiConsumi.Initfeed = function(channel) {
	var feed;

	/* Se i feed sono giˆ stati caricati non viene inoltrata un altra richiesta */
	if (channel == 0 && CostiConsumi.notizie.length != 0) {

		CostiConsumi.caricafeed();
	} else {
		/* Questa funzione viene richiamata un numero di volte pari al numero di canali che si vuole caricare e qui si differenzia il feed da caricare in base al canale*/
		switch (channel) {
		case 0: {
			feed = new google.feeds.Feed("http://energyhomenews.wordpress.com/feed/ ");
			break;
		}
		case 1: {
			feed = new google.feeds.Feed("http://www.rsspect.com/rss/energyathome.xml ");
			break;
		}
		default:
			break;
		}
		feed.setNumEntries(10);

		/* Una volta settato il canale si caricano i feed e viene chiamata una funzione di callbak una volta caricati*/
		feed.load(function(result) {

					if (!result.error) {
						/* salvo i feed nella variabile CostiConsumi.notizie 
						 * la prima news � selezionata random, dalla seconda in poi vengono inserite nello stesso ordine con cui vengono ricevute */
						//var randIndex = Math.floor((Math.random() * result.feed.entries.length) + 1);
						var randIndex = Math.floor((Math.random() * result.feed.entries.length));
						
						var entryRand = result.feed.entries[randIndex];
						var itemRand = {
								title : entryRand.title,
								link : entryRand.link,
								description : entryRand.contentSnippet
							}
					    CostiConsumi.notizie.push(itemRand);
						
						for ( var i = 0; i < result.feed.entries.length; i++) {
							if(i != randIndex){
								var entry = result.feed.entries[i];
								var item = {
									title : entry.title,
									link : entry.link,
									description : entry.contentSnippet
								}
								CostiConsumi.notizie.push(item);
							}
						}
					}
					/* Se ho caricato il primo canale allora chiamo la funzione per caricare il secondo*/
					if (channel == 0) {
						CostiConsumi.Initfeed(1);
					} else {
						/* se ho caricato il secondo canale, nascondo lo spinner e carico i feed nell'html*/
						//hideSpinner();
						CostiConsumi.caricafeed();

						$("#backNews").click(
										function() {
											CostiConsumi.notizieid = CostiConsumi.notizieid - 2;
											if (CostiConsumi.notizieid < 0){
												CostiConsumi.notizieid = CostiConsumi.notizie.length - 2;
											}
											CostiConsumi.caricafeed();});

						$("#nextNews").click(
										function() {
											CostiConsumi.notizieid = CostiConsumi.notizieid + 2;
											if (CostiConsumi.notizieid >= CostiConsumi.notizie.length){
												CostiConsumi.notizieid = 0;
											}
											CostiConsumi.caricafeed();});
					}

		});
	}
}

/* Funziona che visualizza gli RSS feed contenuti nella variabile notizie */

CostiConsumi.caricafeed = function() {

	$(".dettaglioNews,.titoloNews").removeAttr("threedots");
	$(".threedots_ellipsis").remove();

	altezza_news = Math.floor(($("#InfoFeedDettaglio").height() - 1 - (Math.floor($("#InfoFeedDettaglio").width() * 0.01) * 2)) / 2);

	$("#PrimaNews").css("height", altezza_news);
	$("#SecondaNews").css("height", altezza_news);

	$("#PrimaNews .titoloNews .ellipsis_text").html(CostiConsumi.notizie[CostiConsumi.notizieid]["title"]);
	$("#PrimaNews a").attr("href",CostiConsumi.notizie[CostiConsumi.notizieid]["link"]);
	$("#PrimaNews .dettaglioNews .ellipsis_text ").html(CostiConsumi.notizie[CostiConsumi.notizieid]["description"]);

	$("#SecondaNews .titoloNews .ellipsis_text").html(CostiConsumi.notizie[CostiConsumi.notizieid + 1]["title"]);
	$("#SecondaNews a").attr("href",CostiConsumi.notizie[CostiConsumi.notizieid + 1]["link"]);
	$("#SecondaNews .dettaglioNews .ellipsis_text").html(CostiConsumi.notizie[CostiConsumi.notizieid + 1]["description"]);

	var diffContenitore_Notizie = $("#InfoFeedDettaglio").outerHeight(true) - 68 - ((Math.floor($("InfoFeedDettaglio").width() * 0.01)) * 5);

	if (diffContenitore_Notizie < 0) {
		$("#SecondaNews").remove();
		$("#PrimaNews").css("position", "absolute").css("top", "25%").css("border", "0px");
	}

	$(".titoloNews").ThreeDots({max_rows : 1});
	$(".dettaglioNews").ThreeDots();

	$("#InfoFeedTitolo").show();
}

/*******************************************************************************
 * gestisce la simulazione degli RSS feed nel div Suggerimenti
 ******************************************************************************/

CostiConsumi.InitfeedSim = function() {
	
	var NotizieSimul = [{	description : "Sale al 20,3% la percentuale di elettricitˆ convertita da ogni singola cella fotovoltaica. E ora la primatista Suntech punta al É",
							link : "http://gogreen.virgilio.it/news/green-design/fotovoltaico-pannello-record-efficienza_6276.html?pmk=rss",
							title : "Fotovoltaico: ecco il pannello con il record di efficienza"},
						{	description : "Un volumetto scaricabile online ricco di consigli utili per risparmiare dai 700 ai 1000 euro all'anno in bolletta con piccoli ...",
							link : "http://gogreen.virgilio.it/news/green-trends/eco-risparmio-arriva-manuale-ridurre-costi-acqua-luce-gas_6274.html?pmk=rss",
							title : "Eco risparmio: arriva il manuale per ridurre i costi di acqua, luce e gas"},
						{	description : "In piazza le associazioni delle rinnovabili. hanno chiesto al governo, come un appello pubblicato sui giornali, di rivedere il ...",
							link : "http://gogreen.virgilio.it/news/green-economy/rinnovabili-mobilitazione-durera_6273.html?pmk=rss",
							title : "Rinnovabili, la mobilitazione partita da Roma e sui giornali durerˆ"},
						{	description : "L'appuntamento � il 28 aprile alle 15 presso i Fori Imperiali. L'obiettivo finale � quello di ottenere pi� sicurezza per i ...",
							link : "http://gogreen.virgilio.it/eventi/salvaciclisti_6272.html?pmk=rss",
							title : "Salvaciclisti"},
						{	description : "A ridosso della decisione itaiana di prorogare o meno la sospensione dell'impiego di alcuni tipi di agrofarmaci, si pubblica la ...",
							link : "http://gogreen.virgilio.it/news/ambiente-energia/pesticidi-api-governo-decide-sospensioni_6271.html?pmk=rss",
							title : "Pesticidi e api: il governo decide sulla sospensione degli agrofarmaci"},
						{	description : "Estrarre lo shale gas, grande alternativa al petrolio in questa fase in cui il prezzo del barile � caro, genera piccoli sismi ...",
							link : "http://gogreen.virgilio.it/news/ambiente-energia/terremoti-locali-estrazione-scisto_6270.html?pmk=rss",
							title : "Terremoti: a generare quelli locali � pure l'estrazione dello scisto"},
						{	description : "Confermato il taglio degli incentivi del 32-36% e il registro obbligatorio per gli impianti di potenza superiore ai 12 ...",
							link : "http://gogreen.virgilio.it/news/ambiente-energia/quinto-conto-energia-testo-decreto.html?pmk=rss",
							title : "Quinto conto energia, il testo del decreto"},
						{	description : "Lanciata dalla Philips Usa, fa luce per 60 watt consumando da 10 e tende a durare due decadi. Il prodotto rivoluzionario ...",
							link : "http://gogreen.virgilio.it/news/green-design/lampadina-eco-rivoluzionaria-dura-20-anni-costa-46-euro_6267.html?pmk=rss",
							title : "Lampadina eco: dura 20 anni e consuma poco, ma per ora costa 46 euro"
						}];

	CostiConsumi.notizie = NotizieSimul;
	CostiConsumi.caricafeed();
	//hideSpinner();
	$("#backNews").click(function() {
		CostiConsumi.notizieid = CostiConsumi.notizieid - 2;
		if (CostiConsumi.notizieid < 0)
			CostiConsumi.notizieid = 8;

		CostiConsumi.caricafeed();
	});

	$("#nextNews").click(function() {
		CostiConsumi.notizieid = CostiConsumi.notizieid + 2;
		if (CostiConsumi.notizieid >= 10)
			CostiConsumi.notizieid = 0;

		CostiConsumi.caricafeed();
	});

}