
var potenza = {
	value : null
};

var CostiConsumi = {
	MODULE : "CostiConsumi",
	CONSUMI : 1,
	COSTI : 2,
	FOTOVOLTAICO : 3,
	listaElettr : {}, // lista degli elettrodomestici per avere l'associazione id:nome per la torta
	SmartInfo : null,
	CostoSmartinfo : null,
	notizie : null,
	notizieid : 0,
	mode : 2,

	// consumo

	consumoOdierno : null,
	consumoMedio : null,
	consumoPrevMese : null,
	consumoGiornaliero : null,
	timerConsumi : null,
	TIMER_UPDATE_CONSUMI : 600000, //Ogni 10'
	potenzaAttuale : {},
	timerPotenza : null,
	TIMER_UPDATE_POWER_METER : 5000, //Ogni 5"
	timerBlink : null,
	TIMER_BLINK : 500, // Ogni mezzo secondo
	TIMER_SEMAPHORO: 5000, //Ogni mezzo secondo

	// costi
	costoOdierno : null,
	costoMedio : null,
	costoPrevMese : null,
	costoGiornaliero : null,
	suddivisioneCosti : null,
	timerCosti : null,
	TIMER_UPDATE_COSTI : 300000, // 5 minuti
	TIMER_UPDATE_PIE: 3000000, //Ogni mezz'ora
	TIMER_UPDATE_MIDDLE_VALUE: 3000000, //Ogni mezz'ora
	indicatoreImgSotto : Define.home["termSfondo"],
	indicatoreImgSopra : Define.home["termSopra"],
	imgChat : Define.home["iconaSugg"],
	tariffaImg : null,
	leftTariffaPos : 0,
	costoOdiernoImg : [ Define.home["costoVerde"], Define.home["costoGiallo"], Define.home["costoRosso"], Define.home["costoGrigio"] ],
	costoOdiernoMsg : [ Msg.home["costoVerde"], Msg.home["costoGiallo"], Msg.home["costoRosso"], Msg.home["costoVuoto"] ],

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
	
	stackSemaphoro: [[], [], []],
	flagSemaphoro: true
}

/* Inizializza la schermata secondo i consumi o i costi */
CostiConsumi.Init = function() {
	
	indicatoreTermometro = 'images/termometro_sopra.png';

	if (Main.env == 0) console.log('CostiConsumi1.js', 'Init', 'Entro!'); 

	CostiConsumi.notizie = new Array();

	/* Se sono in IE devo arrotondare i bordi */
	if ($.browser.msie) {

		LazyScript.load("js/jquery/jquery.corner.js", function() {
			$("#InfoFeed,#CostoConsumoInfo,#CostoConsumoSintesi").corner();
			$("button").corner("3px");
		});

	}

	$("#backNews").button({
		text : false,
		icons : {
			primary : "ui-icon-seek-prev"
		}
	});

	$("#nextNews").button({
		text : false,
		icons : {
			primary : "ui-icon-seek-next"
		}
	});

	$("#IndicatoreTitolo").text(Msg.home["indicatoreConsumi"]);
	$("#IndicatoreSopra").text(Msg.home["indicatoreSopra"]);
	$("#IndicatoreMedia").text(Msg.home["indicatoreMedia"]);
	$("#IndicatoreSotto").text(Msg.home["indicatoreSotto"]);

	$("#InfoFeedTitolo").append(Msg.home["suggerimenti"]);

	/** ***** Caricamento parti Consumi ******* */

	$('#ConsumoIndicatoreImg').gauge();
	$('#ConsumoIndicatoreImg').gauge({
		max : 2.0
	});
	$('#CostoIndicatoreImg').gauge();
	$('#CostoIndicatoreImg').gauge({
		max : 2.0,
		color : 'yellow'
	});

	maxCont = Define.home["limContatore"][Main.contatore];

	$('#ConsumoAttualeMeter').speedometer({
		max : maxCont
	});
	/* FV ToDo: gestire i tre meter in InternetExplorer*/
	if (navigator.userAgent.indexOf('MSIE 7.0') > -1){
		//Sono in Internet Explorer 7.0
		var conMeterDiv = document.getElementById('ConsumoAttualeMeter');
			conMeterDiv.style.height = 220;
			conMeterDiv.style.width = 220;
			conMeterDiv.style.top = 30;
			conMeterDiv.style.left = 0;

		var tmpDiv = $("#ConsumoAttualeMeter div");
	} else {
		var hDiv = $("#ConsumoAttualeMeter").height();
		if ((hDiv == null) || (hDiv <= 0)){
			hDiv == 157;
			$("#ConsumoAttualeMeter").height(hDiv);
		}
		$("#ConsumoAttualeMeter").width(hDiv);
	}

	$("#LabelKWH").text(Msg.home["labelkWh"]);
	$("#LabelOra").text(Msg.home["labelOra"]);

	/** ************************************************************************* */

	$("#CostoAttualeImg").hide();
	$("#DettaglioCosto").hide();
	$("#TariffaImgDiv").hide();
	$("#TariffaImg").hide();
	$("#TariffaPos").hide();
	$("#DettaglioSuddivisioneCosti").hide();
	
	/**** Mod necessaria dopo aggiunta FotoVoltaico ***/
	$("#ProduzioneAttualeTitolo").hide();
	$("#ProduzioneAttuale").hide();
	$("#ReteAttualeTitolo").hide();
	$("#ReteAttuale").hide();
	/**** Mod necessaria dopo aggiunta FotoVoltaico ***/

	Menu.OnClickMainMenu(0);

	if (Main.env == 0) console.log('CostiConsumi1.js', 'Init', 'Esco!');
}

CostiConsumi.GestConsumi = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'GestConsumi', 'Entro!');

	$("#CostiConsumi").show();

	if ((CostiConsumi.mode == CostiConsumi.COSTI) || (CostiConsumi.mode == CostiConsumi.FOTOVOLTAICO)) {

		CostiConsumi.mode = CostiConsumi.CONSUMI;
		
		$("#CostoAttualeImg").hide();
		$("#DettaglioCosto").hide();
		$("#TariffaImgDiv").hide();
		$("#TariffaPos").hide();
		
		$("#DettaglioSuddivisioneCosti").hide();
		$("#CostoIndicatoreImg").hide();

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
	}

	/* Vecchio Entry Point del sistema */
	/**/
	if (CostiConsumi.timerPotenza == null){
		CostiConsumi.GetDatiPotenza();
	}
	if (CostiConsumi.consumoGiornaliero == null){
		CostiConsumi.GetDatiConsumi();
	} else {
		$("#DettaglioCostoConsumoOdierno").html('');
		if (CostiConsumi.consumoOdierno != null){
			//if (Main.env == 0) console.log('exception in CostiConsumi1.js - in CostiConsumi.GestConsumi 1 method: ');
			$("#DettaglioCostoConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + CostiConsumi.consumoOdierno + " kWh </b>");
		} else {
			$("#DettaglioCostoConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + Msg.home["datoNonDisponibile"] + "</b>");
		}
		$("#DettaglioCostoConsumoPrevisto").html('');
		if (CostiConsumi.consumoPrevisto != null){
			//if (Main.env == 0) console.log('exception in CostiConsumi1.js - in CostiConsumi.GestConsumi 2 method: ');
			$("#DettaglioCostoConsumoPrevisto").html(Msg.home["consumoPrevisto"] + ":<br><br> <b>" + CostiConsumi.consumoPrevisto + " kWh </b>");
		} else {
			$("#DettaglioCostoConsumoPrevisto").html(Msg.home["consumoPrevisto"] + ":<br><br> <b>" + Msg.home["datoNonDisponibile"] + "</b>");
		}
	}
	
	/* Nuovo Entry Point */
	/*
	CostiConsumi.pushSemaphoro('CostiConsumi.GetConsumoMedio', 1);
	
	CostiConsumi.pushSemaphoro('CostiConsumi.GetDatiConsumi', 0);
	CostiConsumi.pushSemaphoro('CostiConsumi.GetConsumoOdierno', 0);
	CostiConsumi.pushSemaphoro('CostiConsumi.GetConsumoPrevisto', 0);

	CostiConsumi.pushSemaphoro('CostiConsumi.GetDatiPotenza', 2);
	CostiConsumi.pushSemaphoro('CostiConsumi.GetElettrodomestici', 2);
	
	CostiConsumi.gestSemaphoro();
	*/
	if (Main.env == 0) console.log('CostiConsumi1.js', 'GestConsumi', 'Esco!');
}

CostiConsumi.ExitConsumi = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'ExitConsumi', 'Entro!');

	if (CostiConsumi.timerPotenza != null) {
		clearInterval(CostiConsumi.timerPotenza); 
	  	CostiConsumi.timerPotenza = null; 
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

	Main.ResetError();
	$("#CostiConsumi").hide();

	if (Main.env == 0) console.log('CostiConsumi1.js', 'ExitConsumi', 'Esco!');
}

CostiConsumi.GetDatiPotenza = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'GetDatiPotenza', 'Entro!');

	// non tolgo togliere messaggio errore da piattaforma
	if (InterfaceEnergyHome.visError != InterfaceEnergyHome.ERR_CONN_SERVER){
		Main.ResetError();
	}
	if (InterfaceEnergyHome.mode > 0) {
		try {
			InterfaceEnergyHome.objService.getAttribute(CostiConsumi.DatiPotenzaAttuale, "TotalPower");
		} catch (err) {
			//if (Main.env == 0) console.log('exception in CostiConsumi1.js - in CostiConsumi.GetDatiPotenza method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetDatiPotenza", err);
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
		CostiConsumi.DatiPotenzaAttuale(CostiConsumi.potenzaAttuale, null);
	}
	if (Main.env == 0) console.log('CostiConsumi1.js', 'GetDatiPotenza', 'Esco!');
}

CostiConsumi.DatiPotenzaAttuale = function(result, err) {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'DatiPotenzaAttuale', 'Entro!');

	//CostiConsumi.popSemaphoro('CostiConsumi.GetDatiPotenza', 1);

	if (err != null){
		//if (Main.env == 0) console.log('exception in CostiConsumi1.js - in CostiConsumi.DatiPotenzaAttuale method: ', err);
		InterfaceEnergyHome.GestErrorEH("DatiPotenzaAttuale", err);
	} else if (result != null){
		CostiConsumi.potenzaAttuale.value = result.value;
	} else {
		CostiConsumi.potenzaAttuale.value = null;
	}
	CostiConsumi.SetConsumoImg();
	//CostiConsumi.pushSemaphoro('CostiConsumi.GetDatiPotenza', 1);
	//CostiConsumi.trueFlagSemaphoro();
	if (Main.env == 0) console.log('CostiConsumi1.js', 'DatiPotenzaAttuale', 'Esco!');
}

CostiConsumi.GetElettrodomestici = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'GetElettrodomestici', 'Entro!');

	if (InterfaceEnergyHome.mode > 0) {
		try {
			InterfaceEnergyHome.objService.getAppliancesConfigurations(CostiConsumi.DatiElettrodomesticiCB);
		} catch (err) {
			//if (Main.env == 0) console.log('exception in CostiConsumi1.js - in CostiConsumi.GetElettrodomestici method: ', err);
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

	if (Main.env == 0) console.log('CostiConsumi1.js', 'GetElettrodomestici', 'Esco!');
}

CostiConsumi.DatiElettrodomesticiCB = function(result, err) {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'DatiElettrodomesticiCB', 'Entro!');

	//CostiConsumi.popSemaphoro('CostiConsumi.GetElettrodomestici', 1);
	
	if (err != null){
		//if (Main.env == 0) console.log('exception in CostiConsumi1.js - in CostiConsumi.DatiElettrodomesticiCB method: ', err);
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
				CostiConsumi.SmartInfo = elettrodom["map"];
				Main.appIdSmartInfo = elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID];
				//if (Main.env == 0) console.log('COSTICONSUMI1', 'SmartInfo - ');
				//if (Main.env == 0) console.log(CostiConsumi.SmartInfo);
			} else {
				if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE] == undefined){
					elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value = 0;
				} else {
					var val = parseFloat(elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value);
					elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value = val;
				}
				CostiConsumi.listaElettr[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
				//if (Main.env == 0) console.log('COSTICONSUMI1', 'Eldo - ');
				//if (Main.env == 0) console.log(CostiConsumi.listaElettr[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]]);
				//if (Main.env == 0) console.log(elettrodom["map"]);
			}
		});
	}
	
	CostiConsumi.DatiMaxElettr();
	if (Main.env == 0) console.log('CostiConsumi1.js', 'DatiElettrodomesticiCB', 'Esco!');
}

CostiConsumi.DatiMaxElettr = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'DatiMaxElettr', 'Entro!');

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
	if (Main.env == 0) console.log('CostiConsumi1.js', 'DatiMaxElettr', 'Esco!');
}

// visualizza elettrodomestico che in questo momento sta consumando di piu'
CostiConsumi.VisConsumoMaggiore = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'VisConsumoMaggiore', 'Entro!');

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
				//offsetTop = $("#ConsumoMaggioreImg").offset().top;
				//offsetLeft = $("#ConsumoMaggioreImg").offset().left;

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

	//CostiConsumi.pushSemaphoro('CostiConsumi.GetElettrodomestici', 1);
	//CostiConsumi.trueFlagSemaphoro();
	if (Main.env == 0) console.log('CostiConsumi1.js', 'VisConsumoMaggiore', 'Esco!');
}

CostiConsumi.GetDatiConsumi = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'GetDatiConsumi', 'Entro!');

	Main.ResetError();

	start = new Date(Main.dataAttuale.getTime());
	start.setHours(0);
	end = Main.dataAttuale.getTime();

	if (InterfaceEnergyHome.mode > 1){ 
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getAttributeData(CostiConsumi.DatiConsumoGiornalieroCb,
															InterfaceEnergyHome.PID_TOTALE, 
															InterfaceEnergyHome.CONSUMO, 
															start.getTime(), end,
															InterfaceEnergyHome.HOUR, true, 
															InterfaceEnergyHome.DELTA);
		} catch (err) {
			//if (Main.env == 0) console.log(80, CostiConsumi.MODULE, "err = ");
			//if (Main.env == 0) console.log(80, CostiConsumi.MODULE, err);
			//if (Main.env == 0) console.log('exception in CostiConsumi1.js - in CostiConsumi.GetDatiConsumi method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetDatiConsumoGiornaliero", err);
		}
	} else {
		// per test, copio per il numero ore attuale
		hours = Main.dataAttuale.getHours();
		val = ConsumoGiornaliero;
		val.list = val.list.slice(0, hours);

		CostiConsumi.DatiConsumoGiornalieroCb(val, null);
	}
	if (Main.env == 0) console.log('CostiConsumi1.js', 'GetDatiConsumi', 'Esco!');
}

CostiConsumi.DatiConsumoGiornalieroCb = function(result, err) {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'DatiConsumoGiornalieroCb', 'Entro!');

	//CostiConsumi.popSemaphoro('CostiConsumi.GetDatiConsumi', 1);

	if (err != null){
		InterfaceEnergyHome.GestErrorEH("DatiConsumoGiornaliero", err);
	}
	CostiConsumi.consumoGiornaliero = null;
	if ((err == null) && (result != null)){
		CostiConsumi.consumoGiornaliero = result.list;
	}
	if (Main.env == 0) console.log('CostiConsumi.consumoGiornaliero', CostiConsumi.consumoGiornaliero);

	hideSpinner();
	CostiConsumi.VisGrafico();
	CostiConsumi.VisIndicatoreConsumi();
	
	//CostiConsumi.pushSemaphoro('CostiConsumi.GetDatiConsumi', 1);
	//CostiConsumi.trueFlagSemaphoro();
	showSpinner();
	CostiConsumi.GetConsumoOdierno();
	if (Main.env == 0) console.log('CostiConsumi1.js', 'DatiConsumoGiornalieroCb', 'Esco!');
}

CostiConsumi.VisGrafico = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'VisGrafico', 'Entro!');
	if (Main.env == 0) console.log('CostiConsumi.consumoGiornaliero', CostiConsumi.consumoGiornaliero);
	
	var dataY;
	if (CostiConsumi.consumoGiornaliero != null){
		dataY = CostiConsumi.consumoGiornaliero.slice(0);
	}
	if (Main.env == 0) console.log('dataY', dataY);
	
	if(dataY){
		$.each(dataY, function(index, dato) {
			dataY[index] = dato / 1000;
		});
	}
	var cat = null;

	if (GestDate.DSTMarzo) {
		cat = [ 0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 ]
	} else if (GestDate.DSTOttobre) {
		cat = [ 0, 1, 2, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 ];
	}

	Highcharts.setOptions({
		colors : [ "#0B0B96", '#50B432', '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4' ]
	});
	chart = new Highcharts.Chart({
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
				color : '#202020',
				fontSize : "1.0em"
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
				text : ' kWh',
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
			enabled : false
		},
		tooltip : {
			formatter : function() {
				var strFormatter = '';
				if (this.y > 1){
					strFormatter = 'Consumo : <b>' + Highcharts.numberFormat(this.y, 2) + ' kWh</b><br/>' + 'alle ore: ' + this.x + ':00';
				} else {
					var valueInWatt = this.y * 1000;
					strFormatter = 'Consumo : <b>' + Highcharts.numberFormat(valueInWatt, 0) + ' Wh</b><br/>' + 'alle ore: ' + this.x + ':00';
				}
				return strFormatter;
			}
		},
		series : [ {
			name : 'Population',
			data : dataY
		} ],
		plotOptions : {
			column : {
				borderWidth : 0,
				color : "#0B0B96",
				pointPadding : 0,
				groupPadding : 0.1
			}
		}
	});
	if (Main.env == 0) console.log('CostiConsumi1.js', 'VisGrafico', 'Esco!');
}

CostiConsumi.GetConsumoOdierno = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'GetConsumoOdierno', 'Entro!');
	var start = Main.dataAttuale.getTime();
	var indConsumoOdierno = 0;
	var attuale, oraAttuale, minAttuale, consumo, val, consumoLista;

	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			var res = InterfaceEnergyHome.objService.getAttributeData(
					CostiConsumi.DatiConsumoOdiernoCb,
					InterfaceEnergyHome.PID_TOTALE,
					InterfaceEnergyHome.CONSUMO, start, start,
					InterfaceEnergyHome.DAY, true, InterfaceEnergyHome.DELTA);
		} catch (err) {
			//if (Main.env == 0) console.log(20, CostiConsumi.MODULE, "error: ");
			//if (Main.env == 0) console.log(20, CostiConsumi.MODULE, err);
			//if (Main.env == 0) console.log('exception in CostiConsumi1.js - in CostiConsumi.GetConsumoOdierno method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetConsumoOdierno", err);
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
		CostiConsumi.DatiConsumoOdiernoCb(val, null);
	}

	if (Main.env == 0) console.log('CostiConsumi1.js', 'GetConsumoOdierno', 'Esco!');
}

CostiConsumi.DatiConsumoOdiernoCb = function(result, err) {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'DatiConsumoOdiernoCb', 'Entro!');

	//CostiConsumi.popSemaphoro('CostiConsumi.GetConsumoOdierno', 1);

	if (err != null){
		InterfaceEnergyHome.GestErrorEH("DatiConsumoOdiernoCb", err);
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

	//CostiConsumi.pushSemaphoro('CostiConsumi.GetConsumoOdierno', 1);
	//CostiConsumi.trueFlagSemaphoro();
	CostiConsumi.GetConsumoMedio();
	if (Main.env == 0) console.log('CostiConsumi1.js', 'DatiConsumoOdiernoCb', 'Esco!');
}

CostiConsumi.GetConsumoMedio = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'GetConsumoMedio', 'Entro!');

	var weekDay = Main.dataAttuale.getDay() + 1; // js comincia da 0, java da 1
	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getWeekDayAverage(
					CostiConsumi.DatiConsumoMedioCb,
					InterfaceEnergyHome.PID_TOTALE,
					InterfaceEnergyHome.CONSUMO, weekDay);
		} catch (err) {
			//if (Main.env == 0) console.log(20, CostiConsumi.MODULE, "error: ");
			//if (Main.env == 0) console.log(20, CostiConsumi.MODULE, err);
			//if (Main.env == 0) console.log('exception in CostiConsumi1.js - in CostiConsumi.GetConsumoMedio method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetConsumoMedio", err);
		}
	} else {
		// per test
		var val = ConsumoMedio;
		CostiConsumi.DatiConsumoMedioCb(val, null);
	}
	if (Main.env == 0) console.log('CostiConsumi1.js', 'GetConsumoMedio', 'Esco!');
}

/** ***************************************************************************** */

CostiConsumi.DatiConsumoMedioCb = function(result, err) {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'DatiConsumoMedioCb', 'Entro!');
	
	//CostiConsumi.popSemaphoro('CostiConsumi.GetConsumoMedio', 1);

	if (err != null)
		InterfaceEnergyHome.GestErrorEH("DatiConsumoMedio", err);

	if ((err == null) && (result != null)) {
		CostiConsumi.consumoMedio = result.list;
	}

	CostiConsumi.VisIndicatoreConsumi();
	CostiConsumi.GetConsumoPrevisto();

	//CostiConsumi.pushSemaphoro('CostiConsumi.GetConsumoMedio', 1);
	//CostiConsumi.trueFlagSemaphoro();
	if (Main.env == 0) console.log('CostiConsumi1.js', 'DatiConsumoMedioCb', 'Esco');
}

CostiConsumi.VisIndicatoreConsumi = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'VisIndicatoreConsumi', 'Entro!');

	var arrayMedio = new Array();
	if(CostiConsumi.consumoGiornaliero && CostiConsumi.consumoMedio){
		arrayMedio = CostiConsumi.consumoMedio.slice(0, CostiConsumi.consumoGiornaliero.length);
	}

	var perc = 0;
	var Totodierno = null;
	var Totmedio = null;
	
	if (Main.env == 0) console.log('CostiConsumi.consumoMedio', CostiConsumi.consumoMedio);
	if (Main.env == 0) console.log('CostiConsumi.consumoGiornaliero', CostiConsumi.consumoGiornaliero);
	if (Main.env == 0) console.log('CostiConsumi.consumoOdierno', CostiConsumi.consumoOdierno);

	if ((CostiConsumi.consumoMedio != null) && (CostiConsumi.consumoGiornaliero != null) && (CostiConsumi.consumoOdierno != null)) {
		Totodierno = 0;
		Totmedio = 0;

		$.each(CostiConsumi.consumoGiornaliero, function(index, consumo) {
			if (consumo != null) {
				Totodierno += consumo;
				Totmedio += arrayMedio[index];
			}
		});
		
		if (Main.env == 0) console.log('Totodierno', Totodierno);
		if (Main.env == 0) console.log('Totmedio', Totmedio);
		
		if (Totodierno != null && Totmedio > 0) {
			//perc = Totodierno * 1000 / Totmedio;
			perc = Totodierno / Totmedio;
			if (perc > 2){
				perc = 2;
			}
			if (Main.env == 0) console.log('perc', perc);
		}
		$('#ConsumoIndicatoreImg').gauge("value", perc);
	}
	if (Main.env == 0) console.log('CostiConsumi1.js', 'VisIndicatoreConsumi', 'Esco!');
}

CostiConsumi.GetConsumoPrevisto = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'GetConsumoPrevisto', 'Entro!');

	var start = Main.dataAttuale.getTime();

	if (InterfaceEnergyHome.mode > 1){ 
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getForecast(CostiConsumi.DatiConsumoPrevistoCb, InterfaceEnergyHome.PID_TOTALE,
													   InterfaceEnergyHome.CONSUMO, start, InterfaceEnergyHome.MONTH);
		} catch (err) {
			//if (Main.env == 0) console.log(20, CostiConsumi.MODULE, "error: ");
			//if (Main.env == 0) console.log(20, CostiConsumi.MODULE, err);
			//if (Main.env == 0) console.log('exception in CostiConsumi1.js - in CostiConsumi.GetConsumoPrevisto method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetConsumoPrevisto", err);
		}
	} else {
		// per test
		var val = ConsumoPrevisto;
		CostiConsumi.DatiConsumoPrevistoCb(val, null);
	}

	if (Main.env == 0) console.log('CostiConsumi1.js', 'GetConsumoPrevisto', 'Esco!');
}

CostiConsumi.DatiConsumoPrevistoCb = function(result, err) {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'DatiConsumoPrevistoCb', 'Entro!');
	var txt;
	//CostiConsumi.popSemaphoro('CostiConsumi.GetConsumoPrevisto', 1);

	if (err != null){
		//if (Main.env == 0) console.log(20, CostiConsumi.MODULE, "error: ");
		//if (Main.env == 0) console.log(20, CostiConsumi.MODULE, err);
		InterfaceEnergyHome.GestErrorEH("DatiConsumoPrevisto", err);
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

	hideSpinner();
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

	if (CostiConsumi.timerPotenza == null){
		CostiConsumi.timerPotenza = setInterval("CostiConsumi.GetDatiPotenza()", CostiConsumi.TIMER_UPDATE_POWER_METER);
	}
	
	//CostiConsumi.pushSemaphoro('CostiConsumi.GetConsumoPrevisto', 1);
	//CostiConsumi.trueFlagSemaphoro();
	if (Main.env == 0) console.log('CostiConsumi1.js', 'DatiConsumoPrevistoCb', 'Esco!');
}

CostiConsumi.SetConsumoImg = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'SetConsumoImg', 'Entro!');

	$("#ValConsumoAttuale").html('');
	if (CostiConsumi.potenzaAttuale.value == null) {
		val = 0;
		$("#ValConsumoAttuale").html(Msg.home["datoNonDisponibile"]);
	} else {
		val = CostiConsumi.potenzaAttuale.value;
		$("#ValConsumoAttuale").html((val / 1000.0).toFixed(3) + " kW");
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

	if (Main.env == 0) console.log('CostiConsumi1.js', 'SetConsumoImg', 'Esco!');
}

CostiConsumi.BlinkVal = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'BlinkVal', 'Entro!');
	$("#ValConsumoAttuale").toggleClass("invisibleDiv");
	if (Main.env == 0) console.log('CostiConsumi1.js', 'BlinkVal', 'Esco!');

}

/** Funzione lanciata al caricamento dello script google per gli RSS * */

CostiConsumi.loadFeed = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'loadFeed', 'Entro!');
	google.load("feeds", "1", {"callback" : CostiConsumi.launchFeed});
	if (Main.env == 0) console.log('CostiConsumi1.js', 'loadFeed', 'Esco!');
}

CostiConsumi.launchFeed = function() {
	CostiConsumi.Initfeed(0);
}

/*******************************************************************************
 * gestisce il caricamento degli RSS feed nell'array CostiConsumi.notizie
 ******************************************************************************/

CostiConsumi.Initfeed = function(channel) {
	var feed;
	
	$("#InfoFeed").show();
	$("#InfoFeedTitolo").show();
	$("#InfoFeedDettaglio").show();

	/* Se i feed sono gi� stati caricati non viene inoltrata un altra richiesta */
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

					//if (!result.error) {
					//	/* salvo i feed nella variabile CostiConsumi.notizie */
					//	for ( var i = 0; i < result.feed.entries.length; i++) {
					//		var entry = result.feed.entries[i];
					//		var item = {
					//			title : entry.title,
					//			link : entry.link,
					//			description : entry.contentSnippet
					//		}
					//		CostiConsumi.notizie.push(item);
					//	}
					//}
					if (!result.error) {
						/* salvo i feed nella variabile CostiConsumi.notizie 
						 * la prima news � selezionata random, dalla seconda in poi vengono inserite nello stesso ordine con cui vengono ricevute */
						var randIndex = Math.floor(Math.random() * result.feed.entries.length);
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
						hideSpinner();
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

}

/*******************************************************************************
 * gestisce la simulazione degli RSS feed nel div Suggerimenti
 ******************************************************************************/

CostiConsumi.InitfeedSim = function() {

	CostiConsumi.notizie = NotizieSimul;
	CostiConsumi.caricafeed();
	hideSpinner();
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
/*
CostiConsumi.trueFlagSemaphoro = function(){
	CostiConsumi.flagSemaphoro = true;
}

CostiConsumi.falseFlagSemaphoro = function(){
	CostiConsumi.flagSemaphoro = false;
}

//Inserisco la funzione nell'array corrispondente
CostiConsumi.pushSemaphoro = function(nAjaxFnz, nStack){
	if (Main.env == 0) console.log('CostiConsumi1.js', 'pushSemaphoro', 'nAjaxFnz='+nAjaxFnz);

	var arrayToUse = CostiConsumi.stackSemaphoro[nStack];
	
	var myDate = new Date();
	if (Main.env == 0) console.log('CostiConsumi1.js', 'pushSemaphoro', 'arrayToUse.length = '+arrayToUse.length);
	
	if (arrayToUse.length == 0){
		myDate.setDate(myDate.getMinutes()) //Subito!
		if (Main.env == 0) console.log('CostiConsumi1.js', 'pushSemaphoro', 'Subito!!');
	} else {
		if (nStack == 0){
			myDate.setDate(myDate.getMinutes() + 10);//Mi sposto in avanti di 10 minuti
			if (Main.env == 0) console.log('CostiConsumi1.js', 'pushSemaphoro', 'Mi sposto in avanti di 10 minuti');
		} else if (nStack == 1){
			myDate.setDate(myDate.getMinutes() + 30);//Mi sposto in avanti di 30 minuti
			if (Main.env == 0) console.log('CostiConsumi1.js', 'pushSemaphoro', 'Mi sposto in avanti di 10 minuti');
		} else{
			myDate.setDate(myDate.getSeconds() + 5);//Mi sposto in avanti di 5"
			if (Main.env == 0) console.log('CostiConsumi1.js', 'pushSemaphoro', 'Mi sposto in avanti di 5"');
		}
	}
	
	arrayToUse.push(new Array(nAjaxFnz, myDate.getTime()));
}

//Elimino la funzione dall'array
CostiConsumi.popSemaphoro = function(nAjaxFnz, nStack){

	var arrayToUse = CostiConsumi.stackSemaphoro[nStack];
	var found = false;
	var iCounter = 0;
	while (found = false){
		var leaf = arrayToUse[iCounter];
		if (leaf[0] == nAjaxFnz){
			arrayToUse.shift();
			found = true;
		}
		iCounter++;
	}
}

//In baase al timestamp con cui � stato registrato, eseguo la funzione
CostiConsumi.executeSemaphoro = function(nStack){
	
	if (Main.env == 0) console.log('CostiConsumi1.js', 'execute Semaphoro', 'Entro con param nStack='+nStack);
	if (Main.env == 0) console.log('CostiConsumi1.js', 'execute Semaphoro', 'Array corrispondente = ');

	var arrayToUse = CostiConsumi.stackSemaphoro[nStack];
	if (Main.env == 0) console.log(arrayToUse);
	if (Main.env == 0) console.log('CostiConsumi1.js', 'execute Semaphoro', 'CostiConsumi.flagSemaphoro = ' + CostiConsumi.flagSemaphoro);
	var found = false;
	var iCounter = 0;
	while (found = true){
		var leaf = arrayToUse[iCounter];
		if (Main.env == 0) console.log('CostiConsumi1.js', 'execute Semaphoro', 'leaf=');
		if (Main.env == 0) console.log(leaf);
		var myDate = new Date();
		if (Main.env == 0) console.log('CostiConsumi1.js', 'execute Semaphoro', 'myDate='+myDate.getTime());
		if ((leaf[1] < myDate.getTime()) && (CostiConsumi.flagSemaphoro)){
			if (Main.env == 0) console.log('CostiConsumi1.js', 'execute Semaphoro', 'TROVATO!!!!!!');
			
			CostiConsumi.falseFlagSemaphoro();
			eval(leaf[0]+'()');
			found = true;
			//CostiConsumi.pushSemaphoro(leaf[0], nStack);
		}
		iCounter++;
		if (iCounter > arrayToUse.length){
			if (Main.env == 0) console.log('CostiConsumi1.js', 'execute Semaphoro', 'ERRORE: EXIT!!!!!!');
			break;
		}
	}
}

CostiConsumi.gestSemaphoro = function(){

	CostiConsumi.executeSemaphoro(0);
	CostiConsumi.executeSemaphoro(1);
	CostiConsumi.executeSemaphoro(2);
	
	CostiConsumi.timerSemaphoro10m = setInterval("CostiConsumi.executeSemaphoro(0)", CostiConsumi.TIMER_UPDATE_CONSUMI);
	CostiConsumi.timerSemaphoro30m = setInterval("CostiConsumi.executeSemaphoro(1)", CostiConsumi.TIMER_UPDATE_MIDDLE_VALUE);
	CostiConsumi.timerSemaphoro5s = setInterval("CostiConsumi.executeSemaphoro(2)", CostiConsumi.TIMER_UPDATE_POWER_METER);
	
}*/
