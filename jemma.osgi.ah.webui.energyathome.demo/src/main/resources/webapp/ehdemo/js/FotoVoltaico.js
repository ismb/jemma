var potenza = {
	value : null
};


var CostiConsumi = {
	MODULE : "CostiConsumi",
	CONSUMI : 1,
	COSTI : 2,
	FOTOVOLTAICO : 3,
	listaElettr : {}, // lista degli elettrodomestici per avere l'associazione
						// id:nome per la torta
	SmartInfo : null,
	CostoSmartinfo : null,
	notizie : [],
	notizieid : 0,
	mode : 2,

	// consumo

	consumoOdierno : null,
	consumoMedio : null,
	consumoPrevMese : null,
	consumoGiornaliero : null,
	timerConsumi : null,
	timerTestTime : null,
	datiProduzioneIAC : null,
	datiVenditaIAC : null,
	TIMER_UPDATE_CONSUMI : 600000, // Ogni 10'
	potenzaAttuale : {},
	produzioneAttuale : {},
	reteAttuale : {},
	timerPotenza : null,
	timerPotenza2 : null,
	timerPotenzaCC : null,
	TIMER_UPDATE_POWER_METER : 5000, // Ogni 5"
	TIMER_UPDATE_PROD_POWER_METER : 60000, // Ogni 1'
	timerBlink : null,
	TIMER_BLINK : 500, // Ogni mezzo secondo
	TIMER_SEMAPHORO : 5000, // Ogni mezzo secondo
	TIMER_CHANGE_TIMER : 900000, // Ogni 15'
	 
	MOLTFORDEMO: 10, //Moltiplicatore della produzione di energia per Demo 
 
	// costi
	costoOdierno : null,
	costoMedio : null,
	costoPrevMese : null,
	costoGiornaliero : null,
	energiaProdottaGiornaliero : null,
	energiaVendutaGiornaliero : null,
	suddivisioneCosti : null,
	timerCosti : null,
	TIMER_UPDATE_COSTI : 300000, // 5 minuti
	TIMER_UPDATE_PIE : 3000000, // Ogni mezz'ora
	TIMER_UPDATE_MIDDLE_VALUE : 3000000, // Ogni mezz'ora
	indicatoreImgSotto : Define.home["termSfondo"],
	indicatoreImgSopra : Define.home["termSopra"],
	imgChat : Define.home["iconaSugg"],
	tariffaImg : null,
	leftTariffaPos : 0,
	costoOdiernoImg : [ Define.home["costoVerde"], Define.home["costoGiallo"],
			Define.home["costoRosso"], Define.home["costoGrigio"] ],
	costoOdiernoMsg : [ Msg.home["costoVerde"], Msg.home["costoGiallo"],
			Msg.home["costoRosso"], Msg.home["costoVuoto"] ],

	hIndicatore : null,
	timerPowerMeter : null,
	dimConsumoImg : -1,
	leftConsumoImg : -1,
	topConsumoImg : -1,
	dimCostoImg : -1,
	leftCostoImg : -1,
	topCostoImg : -1,
	dimMaxDispImg : -1,
	maxConsumoElettr : null,
	dataInizio : -1,
	dataFine : -1,
	pathImgPower : DefinePath.imgPowerMeterPath,

	stackSemaphoro : [ [], [], [] ],
	flagSemaphoro : true,

	renderPieGraph : false,
	suddivisioneCostiRender : false,

	limitMaxKW : 100000,
// watt
	visGraficoCount: -1,
	setImgCount: -1
}

/* Inizializza la schermata */
CostiConsumi.Init = function() {
	indicatoreTermometro = 'images/termometro_iac.png';
	suffIndicatoreT = '_iac';
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'Init', 'Entro!');

	/* Se sono in IE devo arrotondare i bordi */
	if ($.browser.msie) {

		LazyScript.load("js/jquery/jquery.corner.js", function() {
			$("#CostoConsumoInfo,#CostoConsumoSintesi").corner();
			$("button").corner("3px");
		});

	}

	/** ***** Caricamento parti Consumi ******* */

	$('#PVConsumoIACIndicatoreImg').gaugePV({
		max : 2.0,
		mode : 'IAC'
	});
	$('#ConsumoIACIndicatoreImg').gaugePV({
		max : 2.0,
		mode : 'IAC'
	});

	$("#ConsumoIndicatoreImg").html("<span>" + Msg.home["consumi"] + "</span>");
	$('#ConsumoIndicatoreImg').gaugePV({
		max : 2.0
	});

	$("#CostoIndicatoreImg").html("<span>" + Msg.home["costi"] + "</span>");
	$('#CostoIndicatoreImg').gaugePV({
		max : 2.0,
		color : 'yellow'
	});

	$("#InfoFeedTitolo").append(Msg.home["suggerimenti"]);

	$("#LabelKWH").text(Msg.home["labelkWh"]);
	$("#LabelOra").text(Msg.home["labelOra"]);

	$("#CostoAttualeImg").hide();
	$("#DettaglioCosto").hide();
	$("#TariffaImgDiv").hide();
	$("#TariffaImg").hide();
	$("#TariffaPos").hide();

	var divFrecce = $("#FrecceFV");
	if (divFrecce.length == 0) {
		$(document.createElement('div')).addClass('divFrecce').attr('id',
				'FrecceFV').appendTo($("#CostoConsumoSintesi")).show();
		$(document.createElement('div')).attr('id', 'divFrecceProd').appendTo(
				$("#FrecceFV"));
		$(document.createElement('div')).attr('id', 'divFrecceConsumi')
				.appendTo($("#FrecceFV"));
		$(document.createElement('div')).attr('id', 'divFrecceRete').appendTo(
				$("#FrecceFV"));
		$(document.createElement('div')).attr('id', 'divCentro').appendTo(
				$("#FrecceFV"));
		$(document.createElement('img')).attr('id', 'imgCentro').attr('src',
				"./Resources/Images/center.png").appendTo($("#divCentro"));
	} else {
		$("#FrecceFV").show();
		$("#divFrecceProd").show();
		$("#divFrecceConsumi").show();
		$("#divFrecceRete").show();
		$("#divCentro").show();
	}

	if ($('#DettaglioGraficoProduzioneOdierno').length == 0) {
		$('#CostoConsumoInfo').append(
				$('#Grafico').clone().attr('id', 'Grafico2'));
		$('#Grafico2').children().each(function() {
			var tmpIdChild = $(this).attr('id');
			$(this).attr('id', tmpIdChild + '2');
			$(this).children().each(function() {
				var tmpIdChild = $(this).attr('id');
				$(this).attr('id', tmpIdChild + '2');
			});
		});
	}

	Menu.OnClickMainMenu(0);

	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'Init', 'Esco!');
}

CostiConsumi.GestFotoVoltaico = function() {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GestFotoVoltaico', 'Entro!');

	$("#CostiConsumi").show();

	if ((CostiConsumi.mode == CostiConsumi.CONSUMI)
			|| (CostiConsumi.mode == CostiConsumi.COSTI)) {
		CostiConsumi.mode = CostiConsumi.FOTOVOLTAICO;

		$("#TitoloCostiConsumi").html(Msg.home["titoloPV"]);

		$("#CostoAttualeImg").hide();
		$("#DettaglioCosto").hide();
		$("#TariffaImgDiv").hide();
		$("#TariffaPos").hide();

		$("#GraficoSuddivisioneCosti").hide();
		$("#PVConsumoIndicatoreImg").hide();
		$("#PVCostoIndicatoreImg").hide();
		$("#IndicatorePV").show();
		$("#ValConsumoAttuale").show();
		$("#ValProduzioneAttuale").show();
		$("#ValReteAttuale").show();
		$("#DettaglioSuddivisioneCosti").hide();
		if (navigator.userAgent.indexOf('MSIE 7.0') > -1) {
			// Sono in Internet Explorer 7.0
			var conMeterDiv = document.getElementById('ConsumoAttualeMeter');
			conMeterDiv.style.display = 'inline';

			var conMeterDiv = document.getElementById('ProduzioneAttualeMeter');
			conMeterDiv.style.display = 'inline';

			var conMeterDiv = document.getElementById('ReteAttualeMeter');
			conMeterDiv.style.display = 'inline';

			var graphConsOdiernoDiv = document
					.getElementById('GraficoConsumoOdierno');
			graphConsOdiernoDiv.style.display = 'inline';
		} else {
			$("#ConsumoAttualeMeter").show();
			$("#ConsumoAttualeMeter div").show();
			$("#ConsumoAttualeMeter img").show();

			$("#ProduzioneAttualeMeter").show();
			$("#ProduzioneAttualeMeter div").show();
			$("#ProduzioneAttualeMeter img").show();

			$("#ReteAttualeMeter").show();
			$("#ReteAttualeMeter div").show();
			$("#ReteAttualeMeter img").show();
			$("#GraficoConsumoOdierno").show();
		}
		$("#DettaglioConsumoMaggiore").show();
		$("#PVConsumoIACIndicatoreImg").show();

		$("#CostoConsumoAttualeTitolo").text(Msg.home["titoloConsumi"]);
		$("#ProduzioneAttualeTitolo").text(Msg.home["titoloProduzione"]);
		$("#ReteAttualeTitolo").text(Msg.home["titoloReteOut"]);
		$("#CostoTConsumoMaxTitolo").text(Msg.home["consumoMaggiore"]);

		$("#CostoConsumoAttuale").removeClass('CostoConsumoAttualeCONS');
		$("#CostoConsumoAttuale").addClass('CostoConsumoAttualePV');
		$("#ProduzioneAttualeTitolo").show();
		$("#ProduzioneAttuale").show();
		$("#ReteAttualeTitolo").show();
		$("#ReteAttuale").show();
		$("#FrecceFV").show();
		$("#divFrecceProd").show();
		$("#divFrecceConsumi").show();
		$("#divFrecceRete").show();
		$("#divCentro").show();
		$("#percIAC").show();
		$("#Grafico").show();
		$("#Grafico2").show();

		$("#CostoTConsumoMax").hide();

		$("#DatiCostoConsumo").hide();
		$("#Indicatore").hide();
		$("#DatiCosti").hide();
		$("#CostoConsumoOdierno").hide();
		$("#CostoConsumoPrevisto").hide();

		$("#IndicatoreTitoloPV").text(Msg.home["indicatoreIAC"]);
		$("#InfoFeed").hide();
		$("#InfoFeedTitolo").hide();
		$("#MarqueeContainer").hide();
	}
	showSpinner();
	if (CostiConsumi.timerPotenza == null) {
		setImgCount = 2;
		CostiConsumi.GetDatiPotenza();
		CostiConsumi.GetDatiProduzione();
	}
	CostiConsumi.visGraficoCount = 3;
	if (CostiConsumi.consumoGiornaliero == null) {
		CostiConsumi.GetDatiEnergiaProdotta();
		CostiConsumi.GetDatiConsumi();
		CostiConsumi.getDailyPVForecast();
	}

	CostiConsumi.GetDatiProduzioneIAC();
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GestFotoVoltaico', 'Esco!');
}

CostiConsumi.ExitFotoVoltaico = function() {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'ExitFotoVoltaico', 'Entro!');

	if (CostiConsumi.timerPotenza != null) {
		clearInterval(CostiConsumi.timerPotenza);
		CostiConsumi.timerPotenza = null;
	}
	if (CostiConsumi.timerPotenza2 != null) {
		clearInterval(CostiConsumi.timerPotenza2);
		CostiConsumi.timerPotenza2 = null;
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

	if(chartConsumi!=null)
		chartConsumi.destroy();
	if(chartVenduto!=null)
		chartVenduto.destroy();

	Main.ResetError();
	$("#CostiConsumi").hide();

	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'ExitFotoVoltaico', 'Esco!');
}

/*
 * Metodo che si occupa di eseguire la chiamata AJAX per prelevare la potenza
 * attuale di consumo
 */
CostiConsumi.GetDatiPotenza = function() {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GetDatiPotenza', 'Entro!');

	// non tolgo togliere messaggio errore da piattaforma
	if (InterfaceEnergyHome.visError != InterfaceEnergyHome.ERR_CONN_SERVER) {
		// Main.ResetError();
	}
	if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)) {
		try {
			//InterfaceEnergyHome.objService.getAttribute(CostiConsumi.DatiPotenzaAttuale, InterfaceEnergyHome.POTENZA_TOTALE);
			InterfaceEnergyHome.objService.getAppliancesConfigurations(CostiConsumi.DatiPotenzaAttuale);
		} catch (err) {
			if (Main.env == 0)
				console.log('exception in FotoVoltaico.js - in CostiConsumi.GetDatiPotenza method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetDatiPotenza", err);
		}
	} else {
		// per test
		if (CostiConsumi.potenzaAttuale.value == null) {
			CostiConsumi.potenzaAttuale.value = 0;
		}
		CostiConsumi.potenzaAttuale.value += 200;
		if (CostiConsumi.potenzaAttuale.value > (Define.home["tipoContatore"][Main.contatore] + 2000)) {
			CostiConsumi.potenzaAttuale.value = 0;
		}

		CostiConsumi.DatiPotenzaAttuale(CostiConsumi.potenzaAttuale, null);
	}
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GetDatiPotenza', 'Esco!');
}

/*
 * Metodo Positive CallBack di CostiConsumi.GetDatiPotenza
 */
CostiConsumi.DatiPotenzaAttuale = function(result, err) {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'DatiPotenzaAttuale', 'Entro!');

	if (err != null) {
		if (Main.env == 0)
			console.log('exception in FotoVoltaico.js - in CostiConsumi.DatiPotenzaAttuale method: ', err);
		InterfaceEnergyHome.GestErrorEH("DatiPotenzaAttuale", err);
	} else if (result != null) {
		if (InterfaceEnergyHome.mode == 0){
			CostiConsumi.potenzaAttuale.value = result.value;
		} else{
			//prelevare dato smart info
			$.each(result.list,function(indice, elettrodom) {
				if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.SMARTINFO_APP_TYPE) {
					if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_CATEGORY] == "12") {
						CostiConsumi.SmartInfo = elettrodom["map"];
						device_value = CostiConsumi.SmartInfo.device_value;
						if (device_value != undefined) {
							CostiConsumi.potenzaAttuale.value = device_value.list[0].value.value;
						}
					}
					if (Main.env == 0)
						console.log('COSTICONSUMI3', 'SmartInfo - '+CostiConsumi.SmartInfo);
				}
			});
		}
	} else {
		CostiConsumi.potenzaAttuale.value = null;
	}



	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'DatiPotenzaAttuale', 'Esco!');
	
	setImgCount--;
	if(setImgCount==0)
		CostiConsumi.GetDatiRete();
}

/*
 * Metodo che si occupa di eseguire la chiamata AJAX per prelevare la potenza
 * attuale di Produzione (dai pannelli solari)
 */
CostiConsumi.GetDatiProduzione = function() {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GetDatiProduzione', 'Entro!');

	// non tolgo togliere messaggio errore da piattaforma
	if (InterfaceEnergyHome.visError != InterfaceEnergyHome.ERR_CONN_SERVER) {
		// Main.ResetError();
	}
	if (InterfaceEnergyHome.mode > 0) {
		try {
			// Cosa mettere al posto di Total Power?
			InterfaceEnergyHome.objService.getAttribute(
					CostiConsumi.DatiProduzioneAttuale,
					InterfaceEnergyHome.PRODUZIONE_TOTALE);
		} catch (err) {
			if (Main.env == 0)
				console.log('exception in FotoVoltaico.js - in CostiConsumi.GetDatiProduzione method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetDatiProduzione", err);
		}
	} else if (InterfaceEnergyHome.mode == -1) {
		try {
			// Cosa mettere al posto di Total Power?
			//InterfaceEnergyHome.objService.getAppliancesConfigurations(CostiConsumi.DatiProduzioneAttuale);
			InterfaceEnergyHome.objService.getPropConfiguration(CostiConsumi.DatiProduzioneAttuale, "ProducedPower");
		} catch (err) {
			if (Main.env == 0)
				console.log('exception in FotoVoltaico.js - in CostiConsumi.GetDatiProduzione method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetDatiProduzione", err);
		}
	} else {
		// per test

		var powerProduction = '2';
		Main.contatoreProd = powerProduction;
		if (Main.env == 0)
			console.log('FotoVoltaico.js', 'GetDatiProduzione',
					Main.contatoreProd);
		switch (Main.contatoreProd) {
		case '0':
			CostiConsumi.produzioneAttuale.value = 1000;
			break;
		case '1':
			CostiConsumi.produzioneAttuale.value = 2000;
			break;
		case '2':
			CostiConsumi.produzioneAttuale.value = 3000;
			break;
		case '3':
			CostiConsumi.produzioneAttuale.value = 4000;
			break;
		case '4':
			CostiConsumi.produzioneAttuale.value = 5000;
			break;
		case '5':
			CostiConsumi.produzioneAttuale.value = 6000;
			break;
		default:
			CostiConsumi.produzioneAttuale.value = 0;
			break;
		}
		if (Main.env == 0)
			console.log('FotoVoltaico.js', 'GetDatiProduzione', CostiConsumi.produzioneAttuale.value);

		CostiConsumi.DatiProduzioneAttuale(CostiConsumi.produzioneAttuale, null);
	}
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GetDatiProduzione', 'Esco!');
}

/*
 * Metodo Positive CallBack di CostiConsumi.GetDatiProduzione
 */
CostiConsumi.DatiProduzioneAttuale = function(result, err) {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'DatiProduzioneAttuale', 'Entro!');
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'CostiConsumi.produzioneAttuale',
				CostiConsumi.produzioneAttuale.value);

	if (err != null) {
		if (Main.env == 0)
			console.log('exception in FotoVoltaico.js - in CostiConsumi.DatiProduzioneAttuale method: ', err);
		InterfaceEnergyHome.GestErrorEH("DatiProduzioneAttuale", err);
	} else if (result != null) {
		if (InterfaceEnergyHome.mode == -1) {
		//	$.each(result.list,function(indice, elettrodom) {
		//		if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.SMARTINFO_APP_TYPE) {
		//			if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_CATEGORY] == "14") {
		//				device_value = elettrodom["map"].device_value;
		//				if (device_value != undefined) {
		//					CostiConsumi.produzioneAttuale.value = device_value.list[1].value.value * CostiConsumi.MOLTFORDEMO; //aggiungo un moltiplicatore 10 per la demo
		//					console.log('FotoVoltaico', '==============> PRODUZIONE SmartInfo PV - ' + CostiConsumi.produzioneAttuale.value);
		//				}
		//			}
		//			if (Main.env == 0)
		//				console.log('FotoVoltaico', 'SmartInfo - '+CostiConsumi.SmartInfo);
		//		}
		//	});
			CostiConsumi.produzioneAttuale.value = result.list[0] * CostiConsumi.MOLTFORDEMO; //aggiungo un moltiplicatore 10 per la demo
		} else {
			CostiConsumi.produzioneAttuale.value = result.value * CostiConsumi.MOLTFORDEMO; //aggiungo un moltiplicatore 10 per la demo
		}
	} else {
		CostiConsumi.produzioneAttuale.value = null;
	}

	$("#divFrecceProd").html('');
	if (CostiConsumi.produzioneAttuale.value != 0) {

		$(document.createElement('img')).attr('id', 'imgFrecceProd').attr(
				'src', "./Resources/Images/line_hor_grey_dx.gif").appendTo(
				$("#divFrecceProd"));
	}
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'DatiProduzioneAttuale', 'Esco!');

	setImgCount--;
	if(setImgCount==0)
		CostiConsumi.GetDatiRete();
}

/*
 * Metodo che si occupa di eseguire il calcolo aritmetico per definire la
 * potenza attuale di consumo dalla rete o di immissione di potenza nella rete.
 */
CostiConsumi.GetDatiRete = function() {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GetDatiRete', 'Entro!');

	if (CostiConsumi.reteAttuale.value == null) {
		CostiConsumi.reteAttuale.value = 0;
	}

	CostiConsumi.reteAttuale.value = CostiConsumi.produzioneAttuale.value - CostiConsumi.potenzaAttuale.value;

	if (Main.env == 0)
		console.log(CostiConsumi.reteAttuale.value,
				CostiConsumi.produzioneAttuale.value,
				CostiConsumi.potenzaAttuale.value);
	if (CostiConsumi.reteAttuale.value < 0) {
		CostiConsumi.reteAttuale.positive = false;
		$("#ReteAttualeTitolo").text(Msg.home["titoloReteIn"]);
	} else {
		CostiConsumi.reteAttuale.positive = true;
		$("#ReteAttualeTitolo").text(Msg.home["titoloReteOut"]);
	}
	CostiConsumi.reteAttuale.value = Math.abs(CostiConsumi.reteAttuale.value);
	if (Main.env == 0)
		console.log(CostiConsumi.reteAttuale);

	CostiConsumi.DatiReteAttuale(CostiConsumi.reteAttuale, null);

	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GetDatiPotenza', 'Esco!');
}

/*
 * Metodo Positive CallBack di CostiConsumi.GetDatiRete (per uniformarmi ai due
 * precedenti)
 */
CostiConsumi.DatiReteAttuale = function(result, err) {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'DatiReteAttuale', 'Entro!');
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'CostiConsumi.reteAttuale',
				CostiConsumi.reteAttuale.value);

	if (err != null) {
		if (Main.env == 0)
			console.log('exception in FotoVoltaico.js - in CostiConsumi.DatiReteAttuale method: ', err);
		InterfaceEnergyHome.GestErrorEH("DatiProduzioneAttuale", err);
	} else if (result != null) {
		CostiConsumi.reteAttuale.value = result.value;
	} else {
		CostiConsumi.reteAttuale.value = null;
	}

	$("#divFrecceRete").html('');
	if (CostiConsumi.reteAttuale.value != 0) {
		var src = null;
		if (CostiConsumi.reteAttuale.positive) {
			src = './Resources/Images/line_hor_grey_dx.gif';
		} else {
			src = './Resources/Images/line_hor_grey_sx.gif';
		}
		$(document.createElement('img')).attr('id', 'imgFrecceRete').attr(
				'src', src).appendTo($("#divFrecceRete"));
	}

	$("#divFrecceConsumi").html('');
	if (CostiConsumi.potenzaAttuale.value != 0) {
		var src = null;
		// if (!CostiConsumi.reteAttuale.positive){
		// src = './Resources/Images/frecce-vert-arancio.gif';
		// } else {
		// src = './Resources/Images/frecce-vert-verde.gif';
		// }
		src = './Resources/Images/line_ver_grey.gif';
		$(document.createElement('img')).attr('id', 'imgFrecceConsumi').attr(
				'src', src).appendTo($("#divFrecceConsumi"));
	}

	CostiConsumi.SetConsumoImg();
	
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'DatiReteAttuale', 'Esco!');
}

CostiConsumi.GetDatiEnergiaProdotta = function() {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GetDatiEnergiaProdotta', 'Entro!');

	// Main.ResetError();

	start = new Date(Main.dataAttuale.getTime());
	start.setHours(0);
	end = Main.dataAttuale.getTime();

	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getAttributeData(
					CostiConsumi.DatiEnergiaProdottaGiornalieroCb,
					InterfaceEnergyHome.PID_TOTALE,
					InterfaceEnergyHome.PRODUZIONE, start.getTime(), end,
					InterfaceEnergyHome.HOUR, true, InterfaceEnergyHome.DELTA);
		} catch (err) {
			if (Main.env == 0)
				console.log(80, CostiConsumi.MODULE, "err = ");
			if (Main.env == 0)
				console.log(80, CostiConsumi.MODULE, err);
			if (Main.env == 0)
				console.log('exception in FotoVoltaico.js - in CostiConsumi.GetDatiEnergiaProdotta method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetDatiEnergiaProdotta", err);
		}
	} else {
		// per test, copio per il numero ore attuale
		//hours = Main.dataAttuale.getHours();
		//val = jQuery.extend(true, {}, EnergiaProdottaGiornalieroSimul);
		//val.list = val.list.slice(0, hours);
		
		InterfaceEnergyHome.objService.getPropConfiguration(CostiConsumi.DatiEnergiaProdottaGiornalieroCb, "EnergiaProdottaGiornalieroSimul");

		//CostiConsumi.DatiEnergiaProdottaGiornalieroCb(val, null);
	}
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GetDatiConsumi', 'Esco!');
}

CostiConsumi.DatiEnergiaProdottaGiornalieroCb = function(result, err) {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'DatiEnergiaProdottaGiornalieroCb',
				'Entro!');

	if (err != null) {
		InterfaceEnergyHome
				.GestErrorEH("DatiEnergiaProdottaGiornalieroCb", err);
	} else {
		CostiConsumi.energiaProdottaGiornaliero = null;
		if (result != null) {
			CostiConsumi.energiaProdottaGiornaliero = result.list;
		} else {
			var hours = Main.dataAttuale.getHours();
			var list = new Array(hours);
			for ( var index = 0; index < list.length; index++) {
				list[index] = 0;
			}
			CostiConsumi.energiaProdottaGiornaliero = list;
			// Imposto il messaggio di errore
			InterfaceEnergyHome.GestErrorEH("DatiEnergiaProdottaGiornalieroCb",
					{
						code : 1,
						msg : 'No Peak Production'
					});
		}
	}
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'DatiEnergiaProdottaGiornalieroCb', 'Esco!');
	
	CostiConsumi.visGraficoCount--;
	if(CostiConsumi.visGraficoCount==0)
		CostiConsumi.VisGrafico();
		
}

CostiConsumi.GetDatiConsumi = function() {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GetDatiConsumi', 'Entro!');

	// Main.ResetError();

	var start = new Date(Main.dataAttuale.getTime());
	start.setHours(0);
	var end = Main.dataAttuale.getTime();

	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getAttributeData(
					CostiConsumi.DatiConsumoGiornalieroCb,
					InterfaceEnergyHome.PID_TOTALE,
					InterfaceEnergyHome.CONSUMO, start.getTime(), end,
					InterfaceEnergyHome.HOUR, true, InterfaceEnergyHome.DELTA);
		} catch (err) {
			if (Main.env == 0)
				console.log(80, CostiConsumi.MODULE, "err = ");
			if (Main.env == 0)
				console.log(80, CostiConsumi.MODULE, err);
			if (Main.env == 0)
				console.log('exception in FotoVoltaico.js - in CostiConsumi.GetDatiConsumi method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetDatiConsumoGiornaliero", err);
		}
	} else {
		// per test, copio per il numero ore attuale
		//var hours = Main.dataAttuale.getHours();
		//var val = jQuery.extend(true, {}, ConsumoGiornaliero);
		//val.list = val.list.slice(0, hours);
		
		InterfaceEnergyHome.objService.getPropConfiguration(CostiConsumi.DatiConsumoGiornalieroCb, "EnergiaConsumataGiornalieroSimul");

		//CostiConsumi.DatiConsumoGiornalieroCb(val, null);
	}
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GetDatiConsumi', 'Esco!');
}

CostiConsumi.DatiConsumoGiornalieroCb = function(result, err) {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'DatiConsumoGiornalieroCb', 'Entro!');

	if (err != null) {
		InterfaceEnergyHome.GestErrorEH("DatiConsumoGiornaliero", err);
	}
	CostiConsumi.consumoGiornaliero = null;
	if ((err == null) && (result != null)) {
		CostiConsumi.consumoGiornaliero = result.list;
	}

	CostiConsumi.energiaVendutaGiornaliero = new Array();
	if (CostiConsumi.energiaProdottaGiornaliero) {
		for ( var index = 0; index < CostiConsumi.energiaProdottaGiornaliero.length; index++) {
			var dato = CostiConsumi.energiaProdottaGiornaliero[index];
			if (dato == null) {
				CostiConsumi.energiaVendutaGiornaliero[index] = 0;
			} else {
				CostiConsumi.energiaVendutaGiornaliero[index] = (CostiConsumi.consumoGiornaliero[index] - dato);
			}
		}
	}
	
	CostiConsumi.visGraficoCount--;
	if(CostiConsumi.visGraficoCount==0)
		CostiConsumi.VisGrafico();
	
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'DatiConsumoGiornalieroCb', 'Esco!');
}

CostiConsumi.VisGrafico = function() {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'VisGrafico', 'Entro!');
	
	var ifDataNull = true;

	var dataConsumi = CostiConsumi.consumoGiornaliero;
	var dataIAC = CostiConsumi.energiaProdottaGiornaliero;
	var dataForecast = CostiConsumi.forecastGiornaliero.slice(0);
	for (var index = 0; index < dataForecast.length; ++index) {
	    if(dataForecast[index] == null){
	    	dataForecast[index] = 0;
	    } else {
	    	dataForecast[index] = dataForecast[index] * CostiConsumi.MOLTFORDEMO;
	    }
	}
	var dataVenduta = new Array();
	var dataAcquistata = new Array();

	if (Main.env == 0)
		console.log('dataConsumi', dataConsumi);
	if (Main.env == 0)
		console.log('dataIAC', dataIAC);

	if (dataConsumi && dataIAC) {
		var tmp, tmpAcquistata;
		//faccio tutto in un unico each perch� dataConsumi e dataIAC hanno la stessa length
		$.each(dataConsumi, function(index, dato) {
			if (dato == null || dataIAC[index] == null) {
				dataConsumi[index] = 0;
				dataIAC[index] = 0;
			} else {
				ifDataNull = false;
				dataConsumi[index] = (dato  - dataIAC[index]) / 1000; //ci tolgo la prodotta perche' il gateway torna la somma, non il consumo
				//IMPORTANTE: questo blocco va fatto dopo il blocco dataConsumi perche' qui si aggiunge il *10 che se fatto prima
				//sballerebbe il calcolo dei consumi
				dataIAC[index] = (dataIAC[index] / 1000) * 2; //inserisco un fattore di moltiplicazione 10 per la demo
				
					
				hours = Main.dataAttuale.getHours();
				if (index >= hours){
					dataConsumi[index] = 0;
					dataIAC[index] = 0;
				}
					 
			}
		});
	}

	var cat = null;

	if (GestDate.DSTMarzo) {
		cat = [ 0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 ]
	} else if (GestDate.DSTOttobre) {
		cat = [ 0, 1, 2, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 ];
	}

	var serieConsumi = Msg.home['serieConsumi'];
	var serieIAC = Msg.home['serieIAC'];

	var maxContatore = 0;
	var maxContatoreProd = 0;
	var maxContatoreRete = 0;
	switch (Main.contatore) {
		case 0:
			maxContatore = 3.5;
			break;
		case 1:
			maxContatore = 5;
			break;
		case 2:
			maxContatore = 6.5;
			break;
	}
	switch (Main.contatoreProd) {
		case 0:
			maxContatoreProd = 2;
			break;
		case 1:
			maxContatoreProd = 3;
			break;
		case 2:
			maxContatoreProd = 4;
			break;
		case 3:
			maxContatoreRete = 5;
			break;
		case 4:
			maxContatoreRete = 6;
			break;
	}
	switch (Main.contatoreRete) {
		case 0:
			maxContatoreRete = 3.5;
			break;
		case 1:
			maxContatoreRete = 5;
			break;
		case 2:
			maxContatoreRete = 6.5;
			break;
	}
	
	if (ifDataNull) {
		if (Main.env == 0) console.log(40, CostiConsumi.MODULE, "VisGrafico : nessun dato");
		$("#DettaglioGraficoConsumoOdierno").html("<div id='ConsumoOdiernoVuoto'>" + Msg.home["noGrafStorico"] + "</div>");
		hideSpinner();
	} else {
		chartConsumi = new Highcharts.Chart({
					chart : {
						renderTo : 'DettaglioGraficoConsumoOdierno',
						type : 'column',
						spacingBottom : 10,
						style : {
							fontSize : '1.0em'
						},
						plotBackgroundColor : '#ECECEC ',
						plotShadow : true,
						plotBorderWidth : 1
					},
					credits : false,
					title : {
						text : Msg.home["consumoOdierno"],
						margin : 30,
						style : {
							fontFamily : 'Arial,sans-serif',
							fontWeight : "bold",
							color : 'green',
							fontSize : "1.1em"
						}
					},
					xAxis : {
						tickInterval : 2,
						min : 0,
						max : GestDate.DSTOttobre ? 24 : 23,
						title : {
							align : 'high',
							offset : 0,
							text : 'H',
							rotation : 0,
							offset : 15,
							style : {
								color : "black"
							}
						},
						categories : cat
					},
					yAxis : {
						min : 0,
						title : {
							align : 'high',
							offset : 0,
							text : ' KWh',
							rotation : 0,
							y : -10,
							style : {
								color : "black"
							}
						},
						labels : {
							formatter : function() {
								return Highcharts.numberFormat(this.value, 2);
							}
						}
					},
					legend : {
						enabled : true,
						align : 'right',
						x : 0,
						verticalAlign : 'top',
						y : 20,
						floating : true,
						backgroundColor : (Highcharts.theme && Highcharts.theme.legendBackgroundColorSolid)
								|| 'white',
						borderColor : '#CCC',
						borderWidth : 1,
						shadow : true
					},
					tooltip : {
						formatter : function() {
							var txt = '<b>' + this.series.name + '</b>: '
									+ Highcharts.numberFormat(this.y, 1)
									+ ' KWh<br/>' + Msg.home["time"] + this.x + ':00'
							return txt;
						}
					},
					series : [ {
						name : serieConsumi,
						data : dataConsumi,
						pointWidth : 10,
						color : "#3066f0"
					}, {
						name : serieIAC,
						data : dataIAC,
						pointWidth : 10,
						color : "#21e700"
					} ]
				});
	}

	

	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'VisGrafico', 'dataVenduta');
	if (Main.env == 0)
		console.log(dataVenduta);
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'VisGrafico', 'dataAcquistata');
	if (Main.env == 0)
		console.log(dataAcquistata);

	var serieForecast = Msg.home['serieForecast'];
	var serieAcquistata = Msg.home['serieAcquistata'];

	var maxContatoreBuySold = (maxContatoreProd > maxContatoreRete) ? maxContatoreProd
			: maxContatoreRete;

	chartVenduto = new Highcharts.Chart(
			{
				chart : {
					renderTo : 'DettaglioGraficoConsumoOdierno2',
					type : 'column',
					spacingBottom : 10,
					style : {
						fontSize : '1.0em'
					},
					plotBackgroundColor : '#ECECEC ',
					plotShadow : true,
					plotBorderWidth : 1
				},
				credits : false,
				title : {
					text : Msg.home["forecastOdierno"],
					margin : 30,
					style : {
						fontFamily : 'Arial,sans-serif',
						fontWeight : "bold",
						color : 'green',
						fontSize : "1.1em"
					}
				},
				xAxis : {
					tickInterval : 2,
					min : 0,
					max : GestDate.DSTOttobre ? 24 : 23,
					title : {
						align : 'high',
						offset : 0,
						text : 'H',
						rotation : 0,
						offset : 15,
						style : {
							color : "black"
						}
					},
					categories : cat
				},
				yAxis : {
					min : 0,
					title : {
						align : 'high',
						offset : 0,
						text : ' KWh',
						rotation : 0,
						y : -10,
						style : {
							color : "black"
						}
					},
					labels : {
						formatter : function() {
							return Highcharts.numberFormat(this.value, 2);
						}
					}
				},
				legend : {
					enabled : true,
					align : 'right',
					x : 0,
					verticalAlign : 'top',
					y : 20,
					floating : true,
					backgroundColor : (Highcharts.theme && Highcharts.theme.legendBackgroundColorSolid)
							|| 'white',
					borderColor : '#CCC',
					borderWidth : 1,
					shadow : true
				},
				tooltip : {
					formatter : function() {
						var txt = '<b>' + this.series.name + '</b>: '
								+ Highcharts.numberFormat(this.y, 2)
								+ ' KWh<br/>' + Msg.home["time"] + this.x + ':00'
						return txt;
					}
				},
				series : [ {
					name : serieForecast,
					data : dataForecast,
					color : "#FFCC00"
				} ]
			});
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'VisGrafico', 'Esco!');
}

CostiConsumi.GetDatiProduzioneIAC = function() {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GetDatiProduzioneIAC', 'Entro!');

	var myDate = new Date(Main.dataAttuale.getTime());
	myDate.setDate(myDate.getDate() - 7);// Mi sposto indietro di 7 giorni.
	var start = new Date(myDate.getTime());
	var end = new Date(Main.dataAttuale.getTime());
	if (Main.env == 0)
		console.log('start', start);
	if (Main.env == 0)
		console.log('end', end);
	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getAttributeData(
					CostiConsumi.GetDatiVenditaIAC,
					InterfaceEnergyHome.PID_TOTALE,
					InterfaceEnergyHome.PRODUZIONE, start.getTime(), end.getTime(), InterfaceEnergyHome.HOUR, true,
					InterfaceEnergyHome.DELTA);

		} catch (err) {
			InterfaceEnergyHome.GestErrorEH("GetDatiProduzioneIAC", err);
		}
	} else {
		// per test
		// var ind = Math.round(Math.random() * SuddivisioneConsumi.length);
		/*CostiConsumi.GetDatiVenditaIAC({
			list : PERCIAC2
		}, null);*/
		InterfaceEnergyHome.objService.getPropConfiguration(CostiConsumi.GetDatiVenditaIAC, "PercIAC2");
	}
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GetDatiProduzioneIAC', 'Esco');
}

CostiConsumi.GetDatiVenditaIAC = function(result, err) {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GetDatiVenditaIAC', 'Entro!');

	if (err != null) {
		if (Main.env == 0)
			console.log('exception in FotoVoltaico.js - in CostiConsumi.GetDatiVenditaIAC method: ', err);
		CostiConsumi.datiProduzioneIAC = null;
		InterfaceEnergyHome.GestErrorEH("DatiProduzioneAttuale", err);
	} else if (result != null) {
		CostiConsumi.datiProduzioneIAC = result.list;
	} else {
		CostiConsumi.datiProduzioneIAC = null;
	}

	// datiVenditaIAC

	var myDate = new Date(Main.dataAttuale.getTime());
	myDate.setDate(myDate.getDate() - 7);// Mi sposto indietro di 7 giorni.
	var start = new Date(myDate.getTime());
	var end = new Date(Main.dataAttuale.getTime());
	if (Main.env == 0)
		console.log('start', start);
	if (Main.env == 0)
		console.log('end', end);
	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getAttributeData(
					CostiConsumi.DatiPercIAC, InterfaceEnergyHome.PID_TOTALE,
					InterfaceEnergyHome.CONSUMO, start.getTime(), end.getTime(),
					InterfaceEnergyHome.HOUR, true, InterfaceEnergyHome.DELTA);

		} catch (err) {
			InterfaceEnergyHome.GestErrorEH("GetDatiVenditaIAC", err);
		}
	} else {
		// per test
		// var ind = Math.round(Math.random() * SuddivisioneConsumi.length);
		/*CostiConsumi.DatiPercIAC({
			list : PERCIAC
		}, null);*/
		InterfaceEnergyHome.objService.getPropConfiguration(CostiConsumi.DatiPercIAC, "PercIAC");
	}
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'GetDatiVenditaIAC', 'Esco');
}

CostiConsumi.DatiPercIAC = function(result, err) {
	var perc = 0;

	if (err != null) {
		CostiConsumi.datiVenditaIAC = null;
		InterfaceEnergyHome.GestErrorEH("DatiProduzioneAttuale", err);
	} else if (result != null) {
		CostiConsumi.datiVenditaIAC = result.list;
	} else {
		CostiConsumi.datiVenditaIAC = null;
	}
	var sumProduzioneOraria = 0;
	var sumDiffProdCons = 0;
	if ((CostiConsumi.datiProduzioneIAC != null)&& (CostiConsumi.datiVenditaIAC != null)) {
		$.each(CostiConsumi.datiProduzioneIAC, function(i, element) {
			if(CostiConsumi.datiVenditaIAC[i] != null && element != null) {
				element = element * 10; //aggiungo fattore di moltiplicazione del fotovoltaico per demo
				sumDiffProdCons += Math.max(0, element - CostiConsumi.datiVenditaIAC[i]);
				sumProduzioneOraria += element;
			}
		});
	}
	perc = 1 - (sumDiffProdCons / sumProduzioneOraria);
	if (isNaN(perc)) {
		perc = 0;
	}
	if (perc <= 0) {
		perc = 0;
	}
	if (perc > 1) {
		perc = 1;
	}

	$('#PVConsumoIACIndicatoreImg').gaugePV("valueIAC", perc * 2);

	if ($('#percIAC').length == 0) {
		$(document.createElement('div')).attr('id', 'percIAC').html(
				Math.floor(perc * 100) + ' %').appendTo($('#IndicatorePV'));
	} else {
		$('#percIAC').html(Math.floor(perc * 100) + ' %');
	}

	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'VisIndicatoreConsumi', 'Esco!');

	hideSpinner();
}


CostiConsumi.SetConsumoImg = function() {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'SetConsumoImg', 'Entro!');
	var val, valProd, valRete = null;
	var maxCont, maxProdCont, maxReteCont = null;

	$("#ValConsumoAttuale").html('');
	if (CostiConsumi.potenzaAttuale.value == null) {
		val = 0;
		$("#ValConsumoAttuale").html(Msg.home["datoNonDisponibile"]);
	} else {
		if (CostiConsumi.potenzaAttuale.value > CostiConsumi.limitMaxKW) {
			val = 0;
			$("#ValConsumoAttuale").html("n.a.");
		} else {
			val = CostiConsumi.potenzaAttuale.value;
			$("#ValConsumoAttuale").html((val / 1000.0).toFixed(3) + " kW");
		}
	}

	$("#ValProduzioneAttuale").html('');
	if (CostiConsumi.produzioneAttuale.value == null) {
		valProd = 0;
		$("#ValProduzioneAttuale").html(Msg.home["datoNonDisponibile"]);
	} else {
		if (CostiConsumi.produzioneAttuale.value > CostiConsumi.limitMaxKW) {
			valProd = 0;
			$("#ValProduzioneAttuale").html("n.a.");
		} else {
			valProd = CostiConsumi.produzioneAttuale.value;
			$("#ValProduzioneAttuale").html(
					(valProd / 1000.0).toFixed(3) + " kW");
		}
	}

	$("#ValReteAttuale").html('');
	if (CostiConsumi.reteAttuale.value == null) {
		valRete = 0;
		$("#ValReteAttuale").html(Msg.home["datoNonDisponibile"]);
	} else {
		if (CostiConsumi.reteAttuale.value > CostiConsumi.limitMaxKW) {
			valRete = 0;
			$("#ValReteAttuale").html("n.a.");
		} else {
			valRete = CostiConsumi.reteAttuale.value;
			$("#ValReteAttuale").html((valRete / 1000.0).toFixed(3) + " kW");
		}
	}

	val = val / 1000.0;
	valProd = valProd / 1000.0;
	valRete = valRete / 1000.0;

	maxCont = Define.home["limContatore"][Main.contatore];
	maxProdCont = Define.home["limContatoreProd"][Main.contatoreProd];
	maxReteCont = Define.home["limContatoreRete"][Main.contatoreRete];

	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'Init', 'maxCont=' + maxCont);
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'Init', 'maxProdCont=' + maxProdCont);
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'Init', 'contatoreProd='
				+ Main.contatoreProd);

	$('#ConsumoAttualeMeter').speedometer({
		max : maxCont
	});
	$('#ProduzioneAttualeMeter').speedometer({
		max : maxProdCont,
		mode : 'fotovoltaico'
	});
	$('#ReteAttualeMeter').speedometer({
		max : maxReteCont,
		mode : 'rete'
	});

	// segnalo sovraccarico (zona gialla) e sovraccarico grave(zona rossa) dello
	// speedometer
	// solo quando sto comprando dalla rete
	if (!CostiConsumi.reteAttuale.positive) {
		if (valRete > Define.home["contatoreOkRete"][Main.contatore]) {
			if (valRete > Define.home["contatoreWarnRete"][Main.contatore]) {
				$("#ValReteAttuale").css("color", "red");
			} else {
				$("#ValReteAttuale").css("color", "orange");
			}

			if (CostiConsumi.timerBlink == null) {
				$("#ValReteAttuale").addClass("invisibleDiv")
				CostiConsumi.timerBlink = setInterval(
						"CostiConsumi.BlinkVal()", CostiConsumi.TIMER_BLINK);
			}
		} else {
			clearInterval(CostiConsumi.timerBlink);
			CostiConsumi.timerBlink = null;
			$("#ValReteAttuale").css("color", "black");
			$("#ValReteAttuale").removeClass("invisibleDiv");
		}
	} else {
		clearInterval(CostiConsumi.timerBlink);
		CostiConsumi.timerBlink = null;
		$("#ValReteAttuale").css("color", "black");
		$("#ValReteAttuale").removeClass("invisibleDiv");
	}

	$('#ConsumoAttualeMeter').speedometer("valueCons", val, "kW");
	$('#ProduzioneAttualeMeter').speedometer("valuePV", valProd, "kW");
	$('#ReteAttualeMeter').speedometer("valueRete", valRete, "kW",
			CostiConsumi.reteAttuale.positive);

	if (CostiConsumi.mode == CostiConsumi.FOTOVOLTAICO) {
		if ($('#consigliTurnOn').length == 0) {
			$(document.createElement('div')).attr('id', 'consigliTurnOn')
					.appendTo($("#CostoConsumoSintesi")).show();
		}

		if (valProd > val) {
			var tmpValRete = valRete * 1000;
			$('#consigliTurnOn').show();
			if (tmpValRete < 50) {
				$('#consigliTurnOn').html(
						'<b>' + Msg.consigliConsumi[0] + '</b><br>'
								+ Msg.consigliConsumi[1]
								+ Msg.deviceConsConsumi[0]
								+ Msg.consigliConsumi[2]);
			} else if (tmpValRete < 200) {
				$('#consigliTurnOn').html(
						'<b>' + Msg.consigliConsumi[0] + '</b><br>'
								+ Msg.consigliConsumi[1]
								+ Msg.deviceConsConsumi[1]
								+ Msg.consigliConsumi[2]);
			} else if (tmpValRete < 500) {
				$('#consigliTurnOn').html(
						'<b>' + Msg.consigliConsumi[0] + '</b><br>'
								+ Msg.consigliConsumi[1]
								+ Msg.deviceConsConsumi[2]
								+ Msg.consigliConsumi[2]);
			} else if (tmpValRete < 1000) {
				$('#consigliTurnOn').html(
						'<b>' + Msg.consigliConsumi[0] + '</b><br>'
								+ Msg.consigliConsumi[1]
								+ Msg.deviceConsConsumi[3]
								+ Msg.consigliConsumi[2]);
			} else {
				$('#consigliTurnOn').html(
						'<b>' + Msg.consigliConsumi[0] + '</b><br>'
								+ Msg.consigliConsumi[1]
								+ Msg.deviceConsConsumi[4]
								+ Msg.consigliConsumi[2]);
			}
		} else {
			$('#consigliTurnOn').hide();
		}
	} else {
		if ($('#consigliTurnOn').length > 0) {
			$('#consigliTurnOn').hide();
		}
	}
	
	if (CostiConsumi.timerPotenza == null) {
		if (InterfaceEnergyHome.mode == -1) {
			CostiConsumi.timerPotenza = setInterval(
					"CostiConsumi.TimerPotenzaTick2()",
					CostiConsumi.TIMER_UPDATE_POWER_METER);
			CostiConsumi.timerPotenza2 = setInterval(
					"CostiConsumi.TimerPotenzaTick3()",
					CostiConsumi.TIMER_UPDATE_PROD_POWER_METER);
		} else {
			CostiConsumi.timerPotenza = setInterval(
					"CostiConsumi.TimerPotenzaTick()",
					CostiConsumi.TIMER_UPDATE_POWER_METER);
		}
		
		/*
		 * Congelato per inutilizzo if (testTime){ CostiConsumi.timerTestTime =
		 * setInterval("CostiConsumi.changeGestTimer()",
		 * CostiConsumi.TIMER_CHANGE_TIMER); }
		 */
	}

	CostiConsumi.gestModalWindow();

	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'SetConsumoImg', 'Esco!');
	
	//imposto timer di aggiornamento
}

CostiConsumi.BlinkVal = function() {
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'BlinkVal', 'Entro!');
	$("#ValReteAttuale").toggleClass("invisibleDiv");
	if (Main.env == 0)
		console.log('FotoVoltaico.js', 'BlinkVal', 'Esco!');

}

CostiConsumi.startMarquee = function() {
	// $jScroller.add("#MarqueeContainer", "#Marquee", "left", 5, 1);

	// Start Scroller
	// $jScroller.start();
}

CostiConsumi.gestModalWindow = function() {

	$("#CostoConsumoAttuale").click(
			function() {
				TINY.box.show({
					html : "<p class='titoloInfoBox'>"
							+ Msg.home['costoConsumoAttualeTitleBox']
							+ "</p><p>" + Msg.home['lblConsO'] + ": "
							+ CostiConsumi.consumoOdierno + " KWh</p><br>",
					animate : true,
					close : true,
					width : 305,
					height : 150
				})
			});

	$("#ProduzioneAttualeMeter").click(
			function() {
				TINY.box.show({
					html : "<p class='titoloInfoBox'>"
							+ Msg.home['costoProduzioneAttualeTitleBox']
							+ "</p><p>" + Msg.home['lblProdA'] + ": "
							+ CostiConsumi.produzioneAttuale.value + " KW</p>",
					animate : true,
					close : true,
					width : 305,
					height : 150
				})
			});

	$("#ReteAttualeMeter")
			.click(
					function() {
						TINY.box
								.show({
									html : "<p class='titoloInfoBox'>"
											+ Msg.home['costoReteAttualeTitleBox']
											+ "</p><p>"
											+ Msg.home['lblPotA']
											+ ": "
											+ (CostiConsumi.potenzaAttuale.value - CostiConsumi.produzioneAttuale.value)
											+ " KW</p>",
									animate : true,
									close : true,
									width : 305,
									height : 150
								})
					});
}

CostiConsumi.getDailyPVForecast = function() {
	if (InterfaceEnergyHome.mode > 0) {
		InterfaceEnergyHome.objService.getDailyPVForecast(CostiConsumi.getDailyPVForecastCB);
		InterfaceEnergyHome.objService.getDailyPVForecastDebug();
	} else{
		CostiConsumi.forecastGiornaliero = ForecastGiornaliero;
		CostiConsumi.getDailyPVForecastCB({list:CostiConsumi.forecastGiornaliero}, null);
	}
}

CostiConsumi.getDailyPVForecastCB = function(result, err) {
	CostiConsumi.forecastGiornaliero = null;
	if (err != null) {
		CostiConsumi.forecastGiornaliero = new Array(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
		InterfaceEnergyHome.GestErrorEH("forecastGiornaliero", err);
	}
	if ((err == null) && (result != null)) {
		CostiConsumi.forecastGiornaliero = result.list;
	}
	
	CostiConsumi.visGraficoCount--;
	if(CostiConsumi.visGraficoCount==0)
		CostiConsumi.VisGrafico();
}

//esecuzione di un ciclo di aggiornamento dei dati di potenza attuale in casa
CostiConsumi.TimerPotenzaTick = function(){
	setImgCount = 2;
	CostiConsumi.GetDatiPotenza();
	CostiConsumi.GetDatiProduzione();
}

//esecuzione di un ciclo di aggiornamento dei dati di potenza attuale in casa
CostiConsumi.TimerPotenzaTick2 = function(){
	setImgCount = 2;
	CostiConsumi.GetDatiPotenza();
}

//esecuzione di un ciclo di aggiornamento dei dati di potenza attuale in casa
CostiConsumi.TimerPotenzaTick3 = function(){
	setImgCount = 2;
	CostiConsumi.GetDatiProduzione();
}