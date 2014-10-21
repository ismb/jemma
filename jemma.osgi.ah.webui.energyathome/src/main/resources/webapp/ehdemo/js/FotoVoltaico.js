
var potenza = {
	value : null
};

var CostiConsumi = {
	MODULE : "CostiConsumi",
	CONSUMI : 1,
	COSTI : 2,
	FOTOVOLTAICO: 3,
	listaElettr : {}, // lista degli elettrodomestici per avere l'associazione id:nome per la torta
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
	timerTestTime: null,
	datiProduzioneIAC: null,
	datiVenditaIAC: null,
	TIMER_UPDATE_CONSUMI : 600000, //Ogni 10'
	potenzaAttuale : {},
	produzioneAttuale : {},
	reteAttuale : {},
	timerPotenza : null,
	timerPotenzaCC : null,
	TIMER_UPDATE_POWER_METER : 5000, //Ogni 5"
	timerBlink : null,
	TIMER_BLINK : 500, // Ogni mezzo secondo
	TIMER_SEMAPHORO: 5000, //Ogni mezzo secondo
	TIMER_CHANGE_TIMER: 900000, //Ogni 15'

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
	flagSemaphoro: true,
	
	renderPieGraph: false,
	suddivisioneCostiRender: false,
	
	limitMaxKW : 100000 // watt
}

/* Inizializza la schermata */
CostiConsumi.Init = function() {

	indicatoreTermometro = 'images/termometro_iac.png';
	suffIndicatoreT = '_iac';
	if (Main.env == 0) console.log('FotoVoltaico.js', 'Init', 'Entro!');

	/* Se sono in IE devo arrotondare i bordi */
	if ($.browser.msie) {

		LazyScript.load("js/jquery/jquery.corner.js", function() {
			$("#CostoConsumoInfo,#CostoConsumoSintesi").corner();
			$("button").corner("3px");
		});

	}

	/******* Caricamento parti Consumi ********/

	$('#ConsumoIACIndicatoreImg').gauge({
		max : 2.0,
		mode: 'IAC'
	});
	$('#ConsumoIndicatoreImg').gauge({
		max : 2.0
	});
	
	$('#CostoIndicatoreImg').gauge({
		max : 2.0,
		color : 'yellow'
	});

	/* FV ToDo: gestire i tre meter in InternetExplorer*/
	if (navigator.userAgent.indexOf('MSIE 7.0') > -1){
		//Sono in Internet Explorer 7.0
		/** Codice non sempre perfettamente funzionante
		 * var conMeterDiv = document.getElementById('ConsumoAttualeMeter');
		 *  conMeterDiv.style.height = '150px';
		 * 	conMeterDiv.style.width = '170px';
		 * 	conMeterDiv.style.top = '30%';
		 * 	conMeterDiv.style.left = '0%';
		 * var prodMeterDiv = document.getElementById('ProduzioneAttualeMeter');
		 * 	prodMeterDiv.style.height = '150px';
		 * 	prodMeterDiv.style.width = '170px';
		 * 	prodMeterDiv.style.top = '30%';
		 * 	prodMeterDiv.style.left = '-10%';
		 * var reteMeterDiv = document.getElementById('ReteAttualeMeter');
		 * 	reteMeterDiv.style.height = '150px';
		 * 	reteMeterDiv.style.width = '170px';
		 * 	reteMeterDiv.style.top = '30%';
		 * 	reteMeterDiv.style.left = '-20%';
		 **/
			
		$("#ConsumoAttualeMeter").height('150px').width('170px');
		$("#ProduzioneAttualeMeter").height('150px').width('170px');
		$("#ReteAttualeMeter").height('150px').width('170px');

	} else {
		var hDiv = $("#ConsumoAttualeMeter").height();
	   $("#ConsumoAttualeMeter").width(hDiv);
		
		$("#ProduzioneAttualeMeter").height(hDiv);
		$("#ProduzioneAttualeMeter").width(hDiv);

		$("#ReteAttualeMeter").height(hDiv);
		$("#ReteAttualeMeter").width(hDiv);
	}

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
		$(document.createElement('div')).addClass('divFrecce').attr('id', 'FrecceFV').appendTo($("#CostoConsumoSintesi")).show();
		$(document.createElement('div')).attr('id', 'divFrecceProd').appendTo($("#FrecceFV"));
		$(document.createElement('div')).attr('id', 'divFrecceConsumi').appendTo($("#FrecceFV"));
		$(document.createElement('div')).attr('id', 'divFrecceRete').appendTo($("#FrecceFV"));
	} else {
		$("#FrecceFV").show();
		$("#divFrecceProd").show();
		$("#divFrecceConsumi").show();
		$("#divFrecceRete").show();
	}
	
	if ($('#DettaglioGraficoProduzioneOdierno').length == 0){
		$('#CostoConsumoInfo').append($('#Grafico').clone().attr('id', 'Grafico2'));
		$('#Grafico2').children().each(function(){
			var tmpIdChild = $(this).attr('id');
			$(this).attr('id', tmpIdChild + '2');
			$(this).children().each(function(){
				var tmpIdChild = $(this).attr('id');
				$(this).attr('id', tmpIdChild + '2');
			});
		});
		
		var tmpHeight = $('#DettaglioGraficoConsumoOdierno').height();
		$('#DettaglioGraficoConsumoOdierno2').height(tmpHeight);
	}

	/*var divMarqueeContainer = $("#MarqueeContainer");
	var divMarquee = $("#Marquee");
	if (divMarquee.length == 0) { 
		var divMarquee = $(document.createElement('div')).attr('id', 'Marquee').show();
		$(document.createElement('div')).attr('id', 'MarqueeContainer').append(divMarquee).appendTo($("#CostoConsumoInfo")).show();
	} else {
		$("#Marquee").show();
		$("#MarqueeContainer").show();
	}*/
	
	Menu.OnClickMainMenu(0);

	if (Main.env == 0) console.log('FotoVoltaico.js', 'Init', 'Esco!');
}

CostiConsumi.GestFotoVoltaico = function() {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GestFotoVoltaico', 'Entro!');
	
	$("#CostiConsumi").show();

	if ((CostiConsumi.mode == CostiConsumi.CONSUMI) || (CostiConsumi.mode == CostiConsumi.COSTI)) {
		CostiConsumi.mode = CostiConsumi.FOTOVOLTAICO;
		
		$("#TitoloCostiConsumi").html(Msg.home["titoloPV"]);

		$("#CostoAttualeImg").hide();
		$("#DettaglioCosto").hide();
		$("#TariffaImgDiv").hide();
		$("#TariffaPos").hide();
		
		$("#DettaglioSuddivisioneCosti").hide();
		$("#ConsumoIndicatoreImg").hide();
		$("#CostoIndicatoreImg").hide();
	
		$("#ValConsumoAttuale").show();
		$("#ValProduzioneAttuale").show();
		$("#ValReteAttuale").show();
		$("#DettaglioSuddivisioneCosti").hide();
		if (navigator.userAgent.indexOf('MSIE 7.0') > -1){
			//Sono in Internet Explorer 7.0
			var conMeterDiv = document.getElementById('ConsumoAttualeMeter');
			conMeterDiv.style.display = 'inline';

			var conMeterDiv = document.getElementById('ProduzioneAttualeMeter');
			conMeterDiv.style.display = 'inline';

			var conMeterDiv = document.getElementById('ReteAttualeMeter');
			conMeterDiv.style.display = 'inline';
			
			var graphConsOdiernoDiv = document.getElementById('GraficoConsumoOdierno');
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
		$("#ConsumoIACIndicatoreImg").show(); 

		$("#CostoConsumoAttualeTitolo").css('top', '46%').css('left', '30%').css('z-index', '100');
		$("#CostoConsumoAttuale").css('top', '40%').css('left', '30%');
	
		$("#CostoConsumoAttualeTitolo").text(Msg.home["titoloConsumiPV"]);
		$("#ProduzioneAttualeTitolo").text(Msg.home["titoloProduzione"]);
		$("#ReteAttualeTitolo").text(Msg.home["titoloReteOut"]);
		$("#CostoTConsumoMaxTitolo").text(Msg.home["consumoMaggiore"]);

		$("#CostoConsumoAttuale").css('border-bottom', '0px');

		$("#ProduzioneAttualeTitolo").show();
		$("#ProduzioneAttuale").show();
		$("#ReteAttualeTitolo").show();
		$("#ReteAttuale").show();
		$("#FrecceFV").show();
		$("#divFrecceProd").show();
		$("#divFrecceConsumi").show();
		$("#divFrecceRete").show();
		$("#percIAC").show();

		$("#Grafico").show();
		$("#Grafico2").show();
		
		$("#CostoTConsumoMax").hide();
		
		$("#CostoConsumoOdierno").hide();
		$("#CostoConsumoPrevisto").hide();
		
		$("#IndicatoreSopra").hide();
		$("#IndicatoreMedia").hide();
		$("#IndicatoreSotto").hide();
		$("#IndicatorePaddingLeft").hide();
		$("#IndicatoreTitolo").text(Msg.home["indicatoreIAC"]);
		$("#IndicatoreSopra").text(Msg.home["indicatoreSopra"]);
		$("#IndicatoreMedia").text(Msg.home["indicatoreMedia"]);
		$("#IndicatoreSotto").text(Msg.home["indicatoreSotto"]);

		$("#IndicatoreTitolo").css('top', '78%').css('left', '0%').width('60%').height('10%');
		$("#Indicatore").css('top', '78%').css('left', '0%').css('z-index', '100');
		$("#Indicatore").width('90%').height('50%');

		$("#InfoFeed").hide();
		$("#InfoFeedTitolo").hide();
		$("#MarqueeContainer").hide();
	}

	if (CostiConsumi.timerPotenza == null){
		CostiConsumi.GetDatiPotenza();
	}
	if (CostiConsumi.consumoGiornaliero == null){
		//CostiConsumi.GetDatiConsumi();
		CostiConsumi.GetDatiEnergiaProdotta();
	} else {
		$("#DettaglioCostoConsumoOdierno").html('');
		if (CostiConsumi.consumoOdierno != null){
			if (Main.env == 0) console.log('exception in FotoVoltaico.js - in CostiConsumi.GestConsumi 1 method: ');
			$("#DettaglioCostoConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + CostiConsumi.consumoOdierno + " kWh </b>");
		} else {
			$("#DettaglioCostoConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + Msg.home["datoNonDisponibile"] + "</b>");
		}
		$("#DettaglioCostoConsumoPrevisto").html('');
		if (CostiConsumi.consumoPrevisto != null){
			if (Main.env == 0) console.log('exception in FotoVoltaico.js - in CostiConsumi.GestConsumi 2 method: ');
			$("#DettaglioCostoConsumoPrevisto").html(Msg.home["consumoPrevisto"] + ":<br><br> <b>" + CostiConsumi.consumoPrevisto + " kWh </b>");
		} else {
			$("#DettaglioCostoConsumoPrevisto").html(Msg.home["consumoPrevisto"] + ":<br><br> <b>" + Msg.home["datoNonDisponibile"] + "</b>");
		}
	}
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GestFotoVoltaico', 'Esco!');
}

CostiConsumi.ExitFotoVoltaico = function() {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'ExitFotoVoltaico', 'Entro!');

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

	chartConsumi.destroy();
	chartVenduto.destroy();

	Main.ResetError();
	$("#CostiConsumi").hide();

	if (Main.env == 0) console.log('FotoVoltaico.js', 'ExitFotoVoltaico', 'Esco!');
}

/*
 * Metodo che si occupa di eseguire la chiamata AJAX per prelevare la potenza attuale di consumo
 */
CostiConsumi.GetDatiPotenza = function() {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiPotenza', 'Entro!');

	// non tolgo togliere messaggio errore da piattaforma
	if (InterfaceEnergyHome.visError != InterfaceEnergyHome.ERR_CONN_SERVER){
		//Main.ResetError();
	}
	if (InterfaceEnergyHome.mode > 0) {
		try {
			//InterfaceEnergyHome.objService.getAttribute(CostiConsumi.DatiPotenzaAttuale, InterfaceEnergyHome.POTENZA_TOTALE);
			InterfaceEnergyHome.objService.getAppliancesConfigurations(CostiConsumi.DatiPotenzaAttuale);
		} catch (err) {
			if (Main.env == 0) console.log('exception in FotoVoltaico.js - in CostiConsumi.GetDatiPotenza method: ', err);
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
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiPotenza', 'Esco!');
}

/*
 * Metodo Positive CallBack di CostiConsumi.GetDatiPotenza
 */
CostiConsumi.DatiPotenzaAttuale = function(result, err) {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiPotenzaAttuale', 'Entro!');

	/*if (err != null){
		if (Main.env == 0) console.log('exception in FotoVoltaico.js - in CostiConsumi.DatiPotenzaAttuale method: ', err);
		InterfaceEnergyHome.GestErrorEH("DatiPotenzaAttuale", err);
	} else if (result != null){
		CostiConsumi.potenzaAttuale.value = result.value;
	} else {
		CostiConsumi.potenzaAttuale.value = null;
	}*/
	if (err != null) {
		if (Main.env == 0)
			console
					.log(
							'exception in FotoVoltaico.js - in CostiConsumi.DatiPotenzaAttuale method: ',
							err);
		InterfaceEnergyHome.GestErrorEH("DatiPotenzaAttuale", err);
	} else if (result != null) {
		if (InterfaceEnergyHome.mode == 0) {
			CostiConsumi.potenzaAttuale.value = result.value;
		} else{
			//prelevare dato smart info
			$.each(result.list,function(indice, elettrodom) {
				if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.SMARTINFO_APP_TYPE) {
					if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_CATEGORY] == "12") {
						CostiConsumi.SmartInfo = elettrodom["map"];
						device_value = CostiConsumi.SmartInfo.device_value;
						if (device_value != undefined) {
							CostiConsumi.potenzaAttuale.value = Math.floor(device_value.value.value);
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
	
	$("#divFrecceConsumi").html('');
	
	CostiConsumi.GetDatiProduzione();
		
	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiPotenzaAttuale', 'Esco!');
}

/*
 * Metodo che si occupa di eseguire la chiamata AJAX per prelevare la potenza attuale di Produzione (dai pannelli solari)
 */
CostiConsumi.GetDatiProduzione = function() {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiProduzione', 'Entro!');

	// non tolgo togliere messaggio errore da piattaforma
	if (InterfaceEnergyHome.visError != InterfaceEnergyHome.ERR_CONN_SERVER){
		//Main.ResetError();
	}
	if (InterfaceEnergyHome.mode > 0) {
		try {
			//Cosa mettere al posto di Total Power?
			InterfaceEnergyHome.objService.getAttribute(CostiConsumi.DatiProduzioneAttuale, InterfaceEnergyHome.PRODUZIONE_TOTALE);
		} catch (err) {
			if (Main.env == 0) console.log('exception in FotoVoltaico.js - in CostiConsumi.GetDatiProduzione method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetDatiProduzione", err);
		}
	} else {
		// per test

		var powerProduction = '2';
		Main.contatoreProd = powerProduction;
		if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiProduzione', Main.contatoreProd);
		switch (Main.contatoreProd){
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
		if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiProduzione', CostiConsumi.produzioneAttuale.value);
		
		CostiConsumi.DatiProduzioneAttuale(CostiConsumi.produzioneAttuale, null);
	}
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiProduzione', 'Esco!');
}

/*
 * Metodo Positive CallBack di CostiConsumi.GetDatiProduzione
 */
CostiConsumi.DatiProduzioneAttuale = function(result, err) {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiProduzioneAttuale', 'Entro!');
	if (Main.env == 0) console.log('FotoVoltaico.js', 'CostiConsumi.produzioneAttuale', CostiConsumi.produzioneAttuale.value);

	if (err != null){
		if (Main.env == 0) console.log('exception in FotoVoltaico.js - in CostiConsumi.DatiProduzioneAttuale method: ', err);
		InterfaceEnergyHome.GestErrorEH("DatiProduzioneAttuale", err);
	} else if (result != null){
		CostiConsumi.produzioneAttuale.value = result.value;
	} else {
		CostiConsumi.produzioneAttuale.value = null;
	}
	
	$("#divFrecceProd").html('');
	if (CostiConsumi.produzioneAttuale.value != 0){
		$(document.createElement('img')).attr('id', 'imgFrecceProd')
										.attr('src', './Resources/Images/frecce-oriz-verde-2.gif')
										.appendTo($("#divFrecceProd"));
	}

	CostiConsumi.GetDatiRete();
	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiProduzioneAttuale', 'Esco!');
}


/*
 * Metodo che si occupa di eseguire il calcolo aritmetico per definire la potenza attuale di consumo dalla rete
 * o di immissione di potenza nella rete. 
 */
CostiConsumi.GetDatiRete = function() {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiRete', 'Entro!');

	if (CostiConsumi.reteAttuale.value == null){
		CostiConsumi.reteAttuale.value = 0;
	}
	
	CostiConsumi.reteAttuale.value = CostiConsumi.produzioneAttuale.value - CostiConsumi.potenzaAttuale.value;
	if (Main.env == 0) console.log(CostiConsumi.reteAttuale.value, CostiConsumi.produzioneAttuale.value, CostiConsumi.potenzaAttuale.value);
	if (CostiConsumi.reteAttuale.value < 0){
		CostiConsumi.reteAttuale.positive = false;
		$("#ReteAttualeTitolo").text(Msg.home["titoloReteIn"]);
	} else {
		CostiConsumi.reteAttuale.positive = true;
		$("#ReteAttualeTitolo").text(Msg.home["titoloReteOut"]);
	}
	CostiConsumi.reteAttuale.value = Math.abs(CostiConsumi.reteAttuale.value);
	if (Main.env == 0) console.log(CostiConsumi.reteAttuale);
	
	CostiConsumi.DatiReteAttuale(CostiConsumi.reteAttuale, null);
	
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiPotenza', 'Esco!');
}

/*
 * Metodo Positive CallBack di CostiConsumi.GetDatiRete (per uniformarmi ai due precedenti)
 */
CostiConsumi.DatiReteAttuale = function(result, err) {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiReteAttuale', 'Entro!');
	if (Main.env == 0) console.log('FotoVoltaico.js', 'CostiConsumi.reteAttuale', CostiConsumi.reteAttuale.value);

	if (err != null){
		if (Main.env == 0) console.log('exception in FotoVoltaico.js - in CostiConsumi.DatiReteAttuale method: ', err);
		InterfaceEnergyHome.GestErrorEH("DatiProduzioneAttuale", err);
	} else if (result != null){
		CostiConsumi.reteAttuale.value = result.value;
	} else {
		CostiConsumi.reteAttuale.value = null;
	}
	
	$("#divFrecceRete").html('');
	if (CostiConsumi.reteAttuale.value != 0){
		var src = null;
		if (CostiConsumi.reteAttuale.positive){
			src = './Resources/Images/frecce-oriz-arancio-1.gif';
		} else {
			src = './Resources/Images/frecce-oriz-rosso-1.gif';
		}
		$(document.createElement('img')).attr('id', 'imgFrecceRete')
										.attr('src', src)
										.appendTo($("#divFrecceRete"));
	}

	if (CostiConsumi.potenzaAttuale.value != 0){
		var src = null;
		if (!CostiConsumi.reteAttuale.positive){
			src = './Resources/Images/frecce-vert-arancio.gif';
		} else {
			src = './Resources/Images/frecce-vert-verde.gif';
		}
		$(document.createElement('img')).attr('id', 'imgFrecceConsumi')
										.attr('src', src)
										.appendTo($("#divFrecceConsumi"));
	}
	
	
	CostiConsumi.SetConsumoImg();
	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiReteAttuale', 'Esco!');
}

CostiConsumi.GetDatiEnergiaProdotta = function() {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiEnergiaProdotta', 'Entro!');

	//Main.ResetError();

	start = new Date(Main.dataAttuale.getTime());
	start.setHours(0);
	end = Main.dataAttuale.getTime();

	if (InterfaceEnergyHome.mode > 1){ 
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getAttributeData(CostiConsumi.DatiEnergiaProdottaGiornalieroCb,
															InterfaceEnergyHome.PID_TOTALE, 
															InterfaceEnergyHome.PRODUZIONE, 
															start.getTime(), end,
															InterfaceEnergyHome.HOUR, true, 
															InterfaceEnergyHome.DELTA);
		} catch (err) {
			if (Main.env == 0) console.log(80, CostiConsumi.MODULE, "err = ");
			if (Main.env == 0) console.log(80, CostiConsumi.MODULE, err);
			if (Main.env == 0) console.log('exception in FotoVoltaico.js - in CostiConsumi.GetDatiEnergiaProdotta method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetDatiEnergiaProdotta", err);
		}
	} else {
		// per test, copio per il numero ore attuale
		hours = Main.dataAttuale.getHours();
		val = jQuery.extend(true, {}, EnergiaProdottaGiornalieroSimul);
		val.list = val.list.slice(0, hours);

		CostiConsumi.DatiEnergiaProdottaGiornalieroCb(val, null);
	}
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiConsumi', 'Esco!');
}

CostiConsumi.DatiEnergiaProdottaGiornalieroCb = function(result, err) {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiEnergiaProdottaGiornalieroCb', 'Entro!');

	if (err != null){
		InterfaceEnergyHome.GestErrorEH("DatiEnergiaProdottaGiornalieroCb", err);
	} else {
		CostiConsumi.energiaProdottaGiornaliero = null;
		if (result != null){
			CostiConsumi.energiaProdottaGiornaliero = result.list;
		} else {
			var hours = Main.dataAttuale.getHours();
			var list = new Array(hours);
			for (var index = 0; index < list.length; index++){
				list[index] = 0;
			}
			CostiConsumi.energiaProdottaGiornaliero = list;
			//Imposto il messaggio di errore
			InterfaceEnergyHome.GestErrorEH("DatiEnergiaProdottaGiornalieroCb", {code: 1, msg: 'No Peak Production'});
		}
	}

	CostiConsumi.GetDatiConsumi();
	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiEnergiaProdottaGiornalieroCb', 'Esco!');
}

CostiConsumi.GetDatiConsumi = function() {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiConsumi', 'Entro!');

	//Main.ResetError();

	var start = new Date(Main.dataAttuale.getTime());
	start.setHours(0);
	var end = Main.dataAttuale.getTime();

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
			if (Main.env == 0) console.log(80, CostiConsumi.MODULE, "err = ");
			if (Main.env == 0) console.log(80, CostiConsumi.MODULE, err);
			if (Main.env == 0) console.log('exception in FotoVoltaico.js - in CostiConsumi.GetDatiConsumi method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetDatiConsumoGiornaliero", err);
		}
	} else {
		// per test, copio per il numero ore attuale
		var hours = Main.dataAttuale.getHours();
		var val = jQuery.extend(true, {}, ConsumoGiornaliero);
		val.list = val.list.slice(0, hours);

		CostiConsumi.DatiConsumoGiornalieroCb(val, null);
	}
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiConsumi', 'Esco!');
}

CostiConsumi.DatiConsumoGiornalieroCb = function(result, err) {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiConsumoGiornalieroCb', 'Entro!');

	if (err != null){
		InterfaceEnergyHome.GestErrorEH("DatiConsumoGiornaliero", err);
	}
	CostiConsumi.consumoGiornaliero = null;
	if ((err == null) && (result != null)){
		CostiConsumi.consumoGiornaliero = result.list;
	}
	
	CostiConsumi.energiaVendutaGiornaliero = new Array();
	if (CostiConsumi.energiaProdottaGiornaliero){
		for (var index = 0; index < CostiConsumi.energiaProdottaGiornaliero.length; index++) {
			var dato = CostiConsumi.energiaProdottaGiornaliero[index];
			if (dato == null){
				CostiConsumi.energiaVendutaGiornaliero[index] = 0;
			} else {
				CostiConsumi.energiaVendutaGiornaliero[index] = (CostiConsumi.consumoGiornaliero[index] - dato);
			}
		}
	}

	hideSpinner();
	CostiConsumi.VisGrafico();
	//CostiConsumi.VisIndicatoreConsumi();
	
	showSpinner();
	CostiConsumi.GetConsumoOdierno();
	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiConsumoGiornalieroCb', 'Esco!');
}

CostiConsumi.VisGrafico = function() {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'VisGrafico', 'Entro!');

	var dataConsumi = CostiConsumi.consumoGiornaliero;
	var dataIAC = CostiConsumi.energiaProdottaGiornaliero;
	var dataVenduta = new Array();
	var dataAcquistata = new Array();
	var newdataConsumi = new Array(); //Questa mi serve per visualizzare nel grafico il dato del consumo - l'energia comprata (cos“ il totale visualizzato � il totale dell'energia consumata)
	
	if (Main.env == 0) console.log('dataConsumi', dataConsumi);
	if (Main.env == 0) console.log('dataIAC', dataIAC);
	
	if(dataIAC){
		$.each(dataIAC, function(index, dato) {
			if (dato == null){
				dataIAC[index] = 0;
			} else {
				dataIAC[index] = (dato / 1000);
			}
		});
	}
	
	if(dataConsumi){
		var tmp, tmpAcquistata;
		$.each(dataConsumi, function(index, dato) {

			if (dato == null){
				tmp = 0;
			} else {
				tmp = (dato / 1000);
			}
			dataConsumi[index] = tmp;
			
			tmpAcquistata = tmp - dataIAC[index];
			if (tmpAcquistata < 0){
				//Ho prodotto di pi� di quanto ho consumato
				dataVenduta[index] = Math.abs(tmpAcquistata);
				dataAcquistata[index] = 0;
			} else {
				//Ho comprato potenza dalla rete
				dataVenduta[index] = 0;
				dataAcquistata[index] = tmpAcquistata;
			}
			newdataConsumi[index] = dataConsumi[index] - dataAcquistata[index];
		});
	}
	
	var cat = null;

	if (GestDate.DSTMarzo) {
		cat = [ 0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 ]
	} else if (GestDate.DSTOttobre) {
		cat = [ 0, 1, 2, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 ];
	}

	var serieConsumi = Msg.home['serieConsumiPV'];
	var serieIAC = Msg.home['serieIAC'];
	
	if (Main.env == 0) console.log('FotoVoltaico.js', 'VisGrafico', 'dataVenduta');
	if (Main.env == 0) console.log(dataVenduta);
	if (Main.env == 0) console.log('FotoVoltaico.js', 'VisGrafico', 'dataAcquistata');
	if (Main.env == 0) console.log(dataAcquistata);

	var serieVenduta = Msg.home['serieVenduta'];
	var serieAcquistata = Msg.home['serieAcquistata'];
	
	var maxContatore = 0; 
	var maxContatoreProd = 0; 
	var maxContatoreRete = 0; 
	switch (Main.contatore){
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
	switch (Main.contatoreProd){
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
	switch (Main.contatoreRete){
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
	
	var maxContatoreBuySold = (maxContatoreProd > maxContatoreRete) ? maxContatoreProd : maxContatoreRete;

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
			text : Msg.home["consumoOdiernoPV"],
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
				text : 'ore',
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
			//max : maxContatore,
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
			enabled : true,
            align: 'right',
            x: 0,
            verticalAlign: 'top',
            y: 20,
            floating: true,
            backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColorSolid) || 'white',
            borderColor: '#CCC',
            borderWidth: 1,
            shadow: true
		},
		tooltip : {
			formatter : function() {

				var txt = '';
				//console.log(this);
				//if (this.y > 1){
				//	txt = 'Consumo : <b>' + Highcharts.numberFormat(this.y, 2) + ' kWh</b><br/>' + 'alle ore: ' + this.x + ':00';
				//} else {
					//var valueInWatt = this.y * 1000;
					//txt = '<b>'+this.series.name +'</b>: ' + Highcharts.numberFormat(valueInWatt, 0) + ' Wh<br/>' + 'alle ore: ' + this.x + ':00'
				//}
				txt = '<b>'+this.series.name +'</b>: ' + Highcharts.numberFormat(this.y, 1) + ' kWh<br/>' + 'alle ore: ' + this.x + ':00';
				return txt;
			}
		},
        plotOptions: {
            column: {
                stacking: 'normal'
            }
        },
		series : [{
					name : serieConsumi,
					data : newdataConsumi,
					color: "#3066f0",
					stack: 'in'
				  },{
					name : serieAcquistata,
					data : dataAcquistata,
					color: "#ff0022",
					stack: 'in'
				  },{
					name : serieVenduta,
					data : dataVenduta,
					color: "#FFCC00",
					stack: 'out'
				  }]
	}); 
	
	chartVenduto = new Highcharts.Chart({
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
			text : Msg.home["produzoneOdierno"],
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
			//max : maxContatoreBuySold,
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
			enabled : true,
            align: 'right',
            x: 0,
            verticalAlign: 'top',
            y: 20,
            floating: true,
            backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColorSolid) || 'white',
            borderColor: '#CCC',
            borderWidth: 1,
            shadow: true
		},
		tooltip : {
			formatter : function() {

				var txt = '';
				txt = '<b>'+this.series.name +'</b>: ' + Highcharts.numberFormat(this.y, 1) + ' kWh<br/>' + 'alle ore: ' + this.x + ':00';
				return txt;
			}
		},
		series : [{
					name : serieIAC,
					data : dataIAC,
					color: "#21e700"
				  }]
	});
	
	var startInstance = new Date();
	var endInstance = new Date();
	startInstance.setHours(0);
	startInstance.setMinutes(0);
	endInstance.setHours(23);
	endInstance.setMinutes(0);
	startInstance = startInstance.getTime();
	endInstance = endInstance.getTime();
	
	if (InterfaceEnergyHome.mode > 0) {
		var localAddress = $(location).attr('host');
		var actualHagID = 'hag-'+Main.userId;
		var protocol = "http://";
		var initialURL;
		
		if (localAddress == "trial.energy-home.it"){
			protocol = "https://";
			initialURL = protocol + localAddress + "/proxy";
		} else {
			initialURL = protocol + localAddress;
		}
	
		var currentUrl = initialURL + "/HAP/SC/SB/SCLS/" + actualHagID + "/CS/ALL/1/ah.eh.esp.hourlyReceivedEnergyForecast/CIS?startInstanceId="+startInstance+"&endInstanceId="+endInstance;
		var dataFC = new Array();
		
		if (Main.env > 0) {
			$.ajax({
				type: "GET",
				dataType: "xml",
			    url: currentUrl,   // URL DA MODIFICARE PER DEVELOPER DT 
			    success: function(data) {
			        // 'data' is a JSON object which we can access directly.
			        // Evaluate the data.success member and do something appropriate...
			    	
			    	$(data).find("Content").each(
			    		function(){
			    			var value = $(this).find('Value').text();
			    			value = value / 1000;
			    			dataFC.push(value);
			    		}
			    	);
			    	CostiConsumi.VisGraficoForCast(serieIAC, dataIAC, cat, dataFC);
			    },
			    error: function(err) {
			    	CostiConsumi.VisGraficoForCast(serieIAC, dataIAC, cat, null);
			    }
			});
		} else {
			CostiConsumi.VisGraficoForCast(serieIAC, dataIAC, cat, PrevisioneEnergiaProdotta);
		}
	} else {
		CostiConsumi.VisGraficoForCast(serieIAC, dataIAC, cat, PrevisioneEnergiaProdotta);
	}
	
	if (Main.env == 0) console.log('FotoVoltaico.js', 'VisGrafico', 'Esco!');
}

CostiConsumi.VisGraficoForCast = function(serieIAC, dataIAC, cat, dataFC){

	chartVenduto = new Highcharts.Chart({
		chart : {
			renderTo : 'DettaglioGraficoConsumoOdierno2',
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
			text : Msg.home["produzoneOdierno"],
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
				text : 'ore',
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
			//max : maxContatoreBuySold,
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
			enabled : true,
            align: 'right',
            x: 0,
            verticalAlign: 'top',
            y: 20,
            floating: true,
            backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColorSolid) || 'white',
            borderColor: '#CCC',
            borderWidth: 1,
            shadow: true
		},
		tooltip : {
			formatter : function() {

				var txt = '';
				txt = '<b>'+this.series.name +'</b>: ' + Highcharts.numberFormat(this.y, 1) + ' kWh<br/>' + 'alle ore: ' + this.x + ':00';
				return txt;
			}
		},
		series : [{
					type : 'column',
					name : serieIAC,
					data : dataIAC,
					color: "#21e700"
				  }, {
	                type: 'spline',
	                name: 'Previsione',
	                lineWidth: 0.5,
	                data: dataFC,
	                color: 'blue',
	                marker: {
	                	lineWidth: 0.5,
	                	lineColor: 'blue',
	                	fillColor: 'white'}
				  }]
	});
}

CostiConsumi.GetConsumoOdierno = function() {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetConsumoOdierno', 'Entro!');
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
			if (Main.env == 0) console.log(20, CostiConsumi.MODULE, "error: ");
			if (Main.env == 0) console.log(20, CostiConsumi.MODULE, err);
			if (Main.env == 0) console.log('exception in FotoVoltaico.js - in CostiConsumi.GetConsumoOdierno method: ', err);
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

	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetConsumoOdierno', 'Esco!');
}

CostiConsumi.DatiConsumoOdiernoCb = function(result, err) {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiConsumoOdiernoCb', 'Entro!');

	if (err != null){
		InterfaceEnergyHome.GestErrorEH("DatiConsumoOdiernoCb", err);
	}
	$("#DettaglioCostoConsumoOdierno").html('');
	
	/* Congelato per inutilizzo
	if (result){
		if (result.list[0] != null) {
			CostiConsumi.consumoOdierno = (result.list[0] / 1000).toFixed(1);
			//$("#DettaglioCostoConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + CostiConsumi.consumoOdierno + " KWh </b>");
			$("#Marquee").append('<strong>Consumo Odierno</strong>:' + CostiConsumi.consumoOdierno + ' KWh');
		} else {
			CostiConsumi.consumoOdierno = null;
			//$("#DettaglioCostoConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + Msg.home["datoNonDisponibile"] + " </b>");
			$("#Marquee").append('<strong>Consumo Odierno</strong>:' + Msg.home["datoNonDisponibile"] + ' KWh');
		}
	} else {
		CostiConsumi.consumoOdierno = null;
		//$("#DettaglioCostoConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + Msg.home["datoNonDisponibile"] + " </b>");
		$("#Marquee").append('<strong>Consumo Odierno</strong>:' + Msg.home["datoNonDisponibile"] + ' KWh');
	}
	*/

	CostiConsumi.GetConsumoMedio();
	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiConsumoOdiernoCb', 'Esco!');
}

CostiConsumi.GetConsumoMedio = function() {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetConsumoMedio', 'Entro!');

	var weekDay = Main.dataAttuale.getDay() + 1; // js comincia da 0, java da 1
	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getWeekDayAverage(
					CostiConsumi.DatiConsumoMedioCb,
					InterfaceEnergyHome.PID_TOTALE,
					InterfaceEnergyHome.CONSUMO, weekDay);
		} catch (err) {
			if (Main.env == 0) console.log(20, CostiConsumi.MODULE, "error: ");
			if (Main.env == 0) console.log(20, CostiConsumi.MODULE, err);
			if (Main.env == 0) console.log('exception in FotoVoltaico.js - in CostiConsumi.GetConsumoMedio method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetConsumoMedio", err);
		}
	} else {
		// per test
		var val = ConsumoMedio;
		CostiConsumi.DatiConsumoMedioCb(val, null);
	}
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetConsumoMedio', 'Esco!');
}

/** ***************************************************************************** */

CostiConsumi.DatiConsumoMedioCb = function(result, err) {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiConsumoMedioCb', 'Entro!');

	if (err != null)
		InterfaceEnergyHome.GestErrorEH("DatiConsumoMedio", err);

	if ((err == null) && (result != null)) {
		CostiConsumi.consumoMedio = result.list;
	}

	CostiConsumi.GetDatiProduzioneIAC();
	CostiConsumi.GetConsumoPrevisto();

	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiConsumoMedioCb', 'Esco');
}

CostiConsumi.GetDatiProduzioneIAC = function() {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiProduzioneIAC', 'Entro!');
	
	var myDate = new Date(Main.dataAttuale.getTime());
	myDate.setDate(myDate.getDate() - 7);//Mi sposto indietro di 7 giorni.
	var start = new Date(myDate.getTime());
	var end = new Date(Main.dataAttuale.getTime());
	if (Main.env == 0) console.log('start', start);
	if (Main.env == 0) console.log('end', end);
	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getAttributeData(CostiConsumi.GetDatiVenditaIAC, 
															InterfaceEnergyHome.PID_TOTALE,
															InterfaceEnergyHome.PRODUZIONE, 
															start.getTime(), 
															end.getTime(), 
															InterfaceEnergyHome.DAY, true, 
															InterfaceEnergyHome.DELTA);
			
		} catch (err) {
			InterfaceEnergyHome.GestErrorEH("GetDatiProduzioneIAC", err);
		}
	} else {
		// per test
		//var ind = Math.round(Math.random() * SuddivisioneConsumi.length);
		CostiConsumi.GetDatiVenditaIAC({list: PERCIAC2}, null);
	}
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiProduzioneIAC', 'Esco');
}

CostiConsumi.GetDatiVenditaIAC = function(result, err) {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiVenditaIAC', 'Entro!');
	
	if (err != null){
		if (Main.env == 0) console.log('exception in FotoVoltaico.js - in CostiConsumi.GetDatiVenditaIAC method: ', err);
		CostiConsumi.datiProduzioneIAC = null;
		InterfaceEnergyHome.GestErrorEH("DatiProduzioneAttuale", err);
	} else if (result != null){
		CostiConsumi.datiProduzioneIAC = result.list;
	} else {
		CostiConsumi.datiProduzioneIAC = null;
	}
	
	//datiVenditaIAC
	
	var myDate = new Date(Main.dataAttuale.getTime());
	myDate.setDate(myDate.getDate() - 7);//Mi sposto indietro di 7 giorni.
	var start = new Date(myDate.getTime());
	var end = new Date(Main.dataAttuale.getTime());
	if (Main.env == 0) console.log('start', start);
	if (Main.env == 0) console.log('end', end);
	if (InterfaceEnergyHome.mode > 1) {
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getAttributeData(CostiConsumi.DatiPercIAC, 
															InterfaceEnergyHome.PID_TOTALE,
															InterfaceEnergyHome.SOLD, 
															start.getTime(), 
															end.getTime(), 
															InterfaceEnergyHome.DAY, true, 
															InterfaceEnergyHome.DELTA);
			
		} catch (err) {
			InterfaceEnergyHome.GestErrorEH("GetDatiVenditaIAC", err);
		}
	} else {
		// per test
		// var ind = Math.round(Math.random() * SuddivisioneConsumi.length);
		CostiConsumi.DatiPercIAC({list: PERCIAC}, null);
	}
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetDatiVenditaIAC', 'Esco');
}

CostiConsumi.DatiPercIAC = function(result, err){
	var perc = 0;
	
	//SOLO PER TEST
	//perc = 68;
	if (err != null){
		if (Main.env == 0) console.log('exception in FotoVoltaico.js - in CostiConsumi.GetDatiVenditaIAC method: ', err);
		CostiConsumi.datiVenditaIAC = null;
		InterfaceEnergyHome.GestErrorEH("DatiProduzioneAttuale", err);
	} else if (result != null){
		CostiConsumi.datiVenditaIAC = result.list;
	} else {
		CostiConsumi.datiVenditaIAC = null;
	}
	
	var sumDatiProduzioneIAC = 0;
	var sumDatiVenditaIAC = 0;
	if ((CostiConsumi.datiProduzioneIAC != null) && (CostiConsumi.datiVenditaIAC != null)){
		$.each(CostiConsumi.datiProduzioneIAC,
				function(indexResult, element) {
					sumDatiProduzioneIAC += element;
		});
		$.each(CostiConsumi.datiVenditaIAC,
				function(indexResult, element) {
					sumDatiVenditaIAC += element;
		});
	} else {
		perc = 0;
	}
	
	
	perc = (sumDatiProduzioneIAC - sumDatiVenditaIAC);
	// test per valori negativi
	//perc = perc * (-1);
	if (sumDatiProduzioneIAC == 0){
		perc = 0;
	} else {
		perc = perc / sumDatiProduzioneIAC;
	}
	if (perc <= 0){
		perc = 0;
	}
	
	$('#ConsumoIACIndicatoreImg').gauge("valueIAC", perc * 2);
	
	if ($('#percIAC').length == 0){
		$(document.createElement('div')).attr('id', 'percIAC').html(Math.floor(perc * 100)+' %').appendTo($('#CostoConsumoSintesi'));
	} else {
		$('#percIAC').html(Math.floor(perc * 100)+' %');
	}
	
	if (Main.env == 0) console.log('FotoVoltaico.js', 'VisIndicatoreConsumi', 'Esco!');
}

CostiConsumi.GetConsumoPrevisto = function() {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetConsumoPrevisto', 'Entro!');

	var start = Main.dataAttuale.getTime();

	if (InterfaceEnergyHome.mode > 1){ 
		// solo se anche piattaforma
		try {
			InterfaceEnergyHome.objService.getForecast(CostiConsumi.DatiConsumoPrevistoCb, InterfaceEnergyHome.PID_TOTALE,
													   InterfaceEnergyHome.CONSUMO, start, InterfaceEnergyHome.MONTH);
		} catch (err) {
			if (Main.env == 0) console.log(20, CostiConsumi.MODULE, "error: ");
			if (Main.env == 0) console.log(20, CostiConsumi.MODULE, err);
			if (Main.env == 0) console.log('exception in FotoVoltaico.js - in CostiConsumi.GetConsumoPrevisto method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetConsumoPrevisto", err);
		}
	} else {
		// per test
		var val = ConsumoPrevisto;
		CostiConsumi.DatiConsumoPrevistoCb(val, null);
	}

	if (Main.env == 0) console.log('FotoVoltaico.js', 'GetConsumoPrevisto', 'Esco!');
}

CostiConsumi.DatiConsumoPrevistoCb = function(result, err) {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiConsumoPrevistoCb', 'Entro!');
	var txt;

	if (err != null){
		if (Main.env == 0) console.log(20, CostiConsumi.MODULE, "error: ");
		if (Main.env == 0) console.log(20, CostiConsumi.MODULE, err);
		InterfaceEnergyHome.GestErrorEH("DatiConsumoPrevisto", err);
	}

	if ((err == null) && (result != null)){
		CostiConsumi.consumoPrevisto = Math.round(result / 1000); // da w a kW
		txt = Math.round(CostiConsumi.consumoPrevisto) + " KWh";
	} else {
		CostiConsumi.consumoPrevisto = null;
		txt = Msg.home["datoNonDisponibile"];
	}

	$("#DettaglioCostoConsumoPrevisto").html('');
	$("#DettaglioCostoConsumoPrevisto").html(Msg.home["consumoPrevisto"] + ":<br><br> <b>" + txt + "</b>");

	/* Congelato per inutilizzo
	   $("#Marquee").append(' - <strong>Consumo Previsto</strong>:' + txt);
	// CostiConsumi.startMarquee();
	*/

	hideSpinner();
	$("#CostiConsumi").css("z-index", "10");

	if (CostiConsumi.timerPotenza == null){

		CostiConsumi.timerPotenza = setInterval("CostiConsumi.GetDatiPotenza()", CostiConsumi.TIMER_UPDATE_POWER_METER);
		/* Congelato per inutilizzo
		   if (testTime){
			CostiConsumi.timerTestTime = setInterval("CostiConsumi.changeGestTimer()", CostiConsumi.TIMER_CHANGE_TIMER);
		}
		*/
	}
	
	if (Main.env == 0) console.log('FotoVoltaico.js', 'DatiConsumoPrevistoCb', 'Esco!');
}

CostiConsumi.SetConsumoImg = function() {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'SetConsumoImg', 'Entro!');
	var val, valProd, valRete = null;
	var maxCont, maxProdCont, maxReteCont = null;
	
	$("#ValConsumoAttuale").html('');
	if (CostiConsumi.potenzaAttuale.value == null) {
		val = 0;
		$("#ValConsumoAttuale").html(Msg.home["datoNonDisponibile"]);
	} else {
		if (CostiConsumi.potenzaAttuale.value > CostiConsumi.limitMaxKW){
			val = 0;
			$("#ValConsumoAttuale").html("n.a.");
		} else {
			val = CostiConsumi.potenzaAttuale.value;
			$("#ValConsumoAttuale").html(val + " W");
			//$("#ValConsumoAttuale").html((val / 1000.0).toFixed(3) + " kW");
		}
	}
	
	$("#ValProduzioneAttuale").html('');
	if (CostiConsumi.produzioneAttuale.value == null) {
		valProd = 0;
		$("#ValProduzioneAttuale").html(Msg.home["datoNonDisponibile"]);
	} else {
		if (CostiConsumi.produzioneAttuale.value > CostiConsumi.limitMaxKW){
			valProd = 0;
			$("#ValProduzioneAttuale").html("n.a.");
		} else {
			valProd = CostiConsumi.produzioneAttuale.value;
			$("#ValProduzioneAttuale").html(valProd  + " W");
			//$("#ValProduzioneAttuale").html((valProd / 1000.0).toFixed(3) + " kW");
		}
	}

	$("#ValReteAttuale").html('');
	if (CostiConsumi.reteAttuale.value == null) {
		valRete = 0;
		$("#ValReteAttuale").html(Msg.home["datoNonDisponibile"]);
	} else {
		if (CostiConsumi.reteAttuale.value > CostiConsumi.limitMaxKW){
			valRete = 0;
			$("#ValReteAttuale").html("n.a.");
		} else {
			valRete = CostiConsumi.reteAttuale.value;
			$("#ValReteAttuale").html(valRete + " W");
			//$("#ValReteAttuale").html((valRete / 1000.0).toFixed(3) + " kW");
		}
	}

	val = val / 1000.0;
	valProd = valProd / 1000.0;
	valRete = valRete / 1000.0;
	
	maxCont = Define.home["limContatore"][Main.contatore];
	maxProdCont = Define.home["limContatoreProd"][Main.contatoreProd];
	maxReteCont = Define.home["limContatoreRete"][Main.contatoreRete];
	
	if (Main.env == 0) console.log('FotoVoltaico.js', 'Init', 'maxCont='+maxCont);
	if (Main.env == 0) console.log('FotoVoltaico.js', 'Init', 'maxProdCont='+maxProdCont);
	if (Main.env == 0) console.log('FotoVoltaico.js', 'Init', 'contatoreProd='+Main.contatoreProd);

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

	// segnalo sovraccarico (zona gialla) e sovraccarico grave(zona rossa) dello speedometer
	// solo quando sto comprando dalla rete
	if (!CostiConsumi.reteAttuale.positive){
		if (valRete > Define.home["contatoreOkRete"][Main.contatore]) {
			if (valRete > Define.home["contatoreWarnRete"][Main.contatore]){
				$("#ValReteAttuale").css("color", "red");
			} else {
				$("#ValReteAttuale").css("color", "orange");
			}
			
			if (CostiConsumi.timerBlink == null) {
				$("#ValReteAttuale").addClass("invisibleDiv")
				CostiConsumi.timerBlink = setInterval("CostiConsumi.BlinkVal()", CostiConsumi.TIMER_BLINK);
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
	
	$('#ConsumoAttualeMeter').speedometer("value", val, "kW");
	$('#ProduzioneAttualeMeter').speedometer("value", valProd, "kW");
	$('#ReteAttualeMeter').speedometer("value", valRete, "kW");
	
	if (CostiConsumi.mode == CostiConsumi.FOTOVOLTAICO){
		if ($('#consigliTurnOn').length == 0) {
			$(document.createElement('div')).attr('id', 'consigliTurnOn').appendTo($("#CostoConsumoSintesi")).show();
		}
		
		if (valProd > val){
			var tmpValRete = valRete * 1000;
			$('#consigliTurnOn').show();
			if (tmpValRete < 50){
				$('#consigliTurnOn').html('<b>' + Msg.consigliConsumi[0] + '</b><br>' + Msg.consigliConsumi[1] + Msg.deviceConsConsumi[0] + Msg.consigliConsumi[2]);
			} else if (tmpValRete < 200){
				$('#consigliTurnOn').html('<b>' + Msg.consigliConsumi[0] + '</b><br>' + Msg.consigliConsumi[1] + Msg.deviceConsConsumi[1] + Msg.consigliConsumi[2]);
			} else if (tmpValRete < 500){
				$('#consigliTurnOn').html('<b>' + Msg.consigliConsumi[0] + '</b><br>' + Msg.consigliConsumi[1] + Msg.deviceConsConsumi[2] + Msg.consigliConsumi[2]);
			} else if (tmpValRete < 1000){
				$('#consigliTurnOn').html('<b>' + Msg.consigliConsumi[0] + '</b><br>' + Msg.consigliConsumi[1] + Msg.deviceConsConsumi[3] + Msg.consigliConsumi[2]);
			} else {
				$('#consigliTurnOn').html('<b>' + Msg.consigliConsumi[0] + '</b><br>' + Msg.consigliConsumi[1] + Msg.deviceConsConsumi[4] + Msg.consigliConsumi[2]);
			}
		} else {
			$('#consigliTurnOn').hide();
		}
	} else {
		if ($('#consigliTurnOn').length > 0) {
			$('#consigliTurnOn').hide();
		}
	}
	
	CostiConsumi.gestModalWindow();

	if (Main.env == 0) console.log('FotoVoltaico.js', 'SetConsumoImg', 'Esco!');
}

CostiConsumi.BlinkVal = function() {
	if (Main.env == 0) console.log('FotoVoltaico.js', 'BlinkVal', 'Entro!');
	$("#ValReteAttuale").toggleClass("invisibleDiv");
	if (Main.env == 0) console.log('FotoVoltaico.js', 'BlinkVal', 'Esco!');

}

CostiConsumi.startMarquee = function() {
	//$jScroller.add("#MarqueeContainer", "#Marquee", "left", 5, 1);

	// Start Scroller
	//$jScroller.start();
}

CostiConsumi.gestModalWindow = function(){

	
	$("#CostoConsumoAttuale").click(function(){ 
			TINY.box.show({html:"<p class='titoloInfoBox'>"+Msg.home['costoConsumoAttualeTitleBox']+"</p><p>"+Msg.home['lblConsO']+": "+CostiConsumi.consumoOdierno+" KWh</p><br>",
						   animate:true,
						   close:true,
						   width: 305,
						   height: 150})});
	

	
	$("#ProduzioneAttualeMeter").click(function(){
			TINY.box.show({html:"<p class='titoloInfoBox'>"+Msg.home['costoProduzioneAttualeTitleBox']+"</p><p>"+Msg.home['lblProdA']+": "+CostiConsumi.produzioneAttuale.value+" KW</p>",
						   animate:true,
						   close:true,
						   width: 305,
						   height: 150})});
	

	
	$("#ReteAttualeMeter").click(function(){
			TINY.box.show({html:"<p class='titoloInfoBox'>"+Msg.home['costoReteAttualeTitleBox']+"</p><p>"+Msg.home['lblPotA']+": "+ (CostiConsumi.potenzaAttuale.value - CostiConsumi.produzioneAttuale.value)+" KW</p>",
						   animate:true,
						   close:true,
						   width: 305,
						   height: 150})});
}

/* congelato per inutilizzo
CostiConsumi.changeGestTimer = function(){
	//  in questo punto devo cambiare il valore del timer per la lettura della potenza, portarla a 15'
	//  e visualizzare un messaggio all'utente.
	CostiConsumi.timerPotenza = setInterval("CostiConsumi.GetDatiPotenza()", CostiConsumi.TIMER_CHANGE_TIMER);
	var err = {};
	err.msg = "L'aggiornamento verrˆ effettuato ogni 15', per ripristinare l'aggiornamento al minuto, aggiornare la pagina.";
	InterfaceEnergyHome.GestErrorEH("changeGestTimer", err);
}
*/

/* Vecchio grafico che pu˜ tornare utile
   Visualizza solo il consumo e la produzione

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
	//max : maxContatore,
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
	enabled : true,
    align: 'right',
    x: 0,
    verticalAlign: 'top',
    y: 20,
    floating: true,
    backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColorSolid) || 'white',
    borderColor: '#CCC',
    borderWidth: 1,
    shadow: true
},
tooltip : {
	formatter : function() {

		var txt = '';
		if (this.y > 1){
			txt = 'Consumo : <b>' + Highcharts.numberFormat(this.y, 2) + ' kWh</b><br/>' + 'alle ore: ' + this.x + ':00';
		} else {
			var valueInWatt = this.y * 1000;
			txt = '<b>'+this.series.name +'</b>: ' + Highcharts.numberFormat(valueInWatt, 0) + ' Wh<br/>' + 'alle ore: ' + this.x + ':00'
		}
		return txt;
	}
},
series : [{
			name : serieConsumi,
			data : dataConsumi,
			color: "#3066f0"
		  },{
			name : serieIAC,
			data : dataIAC,
			color: "#21e700"
		  }]
});
*/