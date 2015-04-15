var MIN_YEAR = 60 * 24 * 365;

var Main = {
	MODULE : "Main",
	SCREEN_LARGE : 1, // screen normale PC (oltre 1000x1000)
	SCREEN_SMALL : 2, // screen iPad, iPhone
	userAgent : null,
	deviceSize : 0,
	screenRatio : 1,
	screenW : -1,
	screenH : -1,
	dataAttuale : null,
	dataMinima : null, // data sotto la quale considero errore di data
	timerTimeout : null,
	imgDisp : null,
	contatore : null,// tipo contatore (0 = 3kW, 1 = 4.5kW, 2 = 6kW)
	contatoreProd : null,// tipo contatore (0 = 0kw, 1 = 1kW, 2 = 2kW, 3 = 3kW, 4 = 4kW, 5 = 5kW, 6 = 6kW, 11 = 11kW)
	contatoreRete : null,// tipo contatore (0 = 3kW, 1 = 4.5kW, 2 = 6kW)
	idVisitato : null,
	userId: null,
	hagId: null,
	appIdSmartInfo: null,
	secretPassPhrase: "289quaoj0u823qejiak289uq3089sfoswfrwefij489fjqepiadmk",
	enablePV: null,
	env: 1  //tipo ambiente (0 = sviluppo con console.log, 1 = sviluppo senza console.log, 2 = produzione)
}
var indicatoreTermometro = null;
var suffIndicatoreT = '';
var graficoStorico = null;
var chartConsumi = null;
var chartVenduto = null;
var chartPie = null;

Main.aggiornaTimestamp = function() {
	Main.dataAttuale = GestDate.GetActualDate();
	// se data inferiore 1-1-2010 segnalo errore
	if (Main.dataAttuale.getTime() < Main.dataMinima){
		Main.VisError(InterfaceEnergyHome.ERR_CONN_AG);
	}
	
	$("#Data").html(Utils.FormatDate(Main.dataAttuale, 2));
	$("#Ora").html(Utils.FormatDate(Main.dataAttuale, 3));
	if ((Main.userAgent.indexOf('MSIE 6.0') > -1)   || 
		(Main.userAgent.indexOf('MSIE 6.0b') > -1)  || 
		(Main.userAgent.indexOf('MSIE 6.01') > -1)  || 
		(Main.userAgent.indexOf('MSIE 6.1') > -1)   || 
		(Main.userAgent.indexOf('MSIE 5.5b1') > -1) || 
		(Main.userAgent.indexOf('MSIE 5.50') > -1)  || 
		(Main.userAgent.indexOf('MSIE 5.5') > -1)   || 
		(Main.userAgent.indexOf('MSIE 5.23') > -1)  || 
		(Main.userAgent.indexOf('MSIE 5.22') > -1)  || 
		(Main.userAgent.indexOf('MSIE 5.21') > -1)  || 
		(Main.userAgent.indexOf('MSIE 5.2') > -1)   || 
		(Main.userAgent.indexOf('MSIE 5.17') > -1)  || 
		(Main.userAgent.indexOf('MSIE 5.16') > -1)  || 
		(Main.userAgent.indexOf('MSIE 5.15') > -1)  || 
		(Main.userAgent.indexOf('MSIE 5.14') > -1)  || 
		(Main.userAgent.indexOf('MSIE 5.13') > -1)  || 
		(Main.userAgent.indexOf('MSIE 5.12') > -1)  || 
		(Main.userAgent.indexOf('MSIE 5.05') > -1)  || 
		(Main.userAgent.indexOf('MSIE 5.01') > -1)  || 
		(Main.userAgent.indexOf('MSIE 5.0b1') > -1) || 
		(Main.userAgent.indexOf('MSIE 5.00') > -1)  || 
		(Main.userAgent.indexOf('MSIE 5.0') > -1)   || 
		(Main.userAgent.indexOf('MSIE 4.5') > -1)   || 
		(Main.userAgent.indexOf('MSIE 4.01') > -1)  || 
		(Main.userAgent.indexOf('MSIE 4.0') > -1)   || 
		(Main.userAgent.indexOf('MSIE 3.03') > -1)  || 
		(Main.userAgent.indexOf('MSIE 3.02') > -1)  || 
		(Main.userAgent.indexOf('MSIE 3.01') > -1)  || 
		(Main.userAgent.indexOf('MSIE 3.0B') > -1)  || 
		(Main.userAgent.indexOf('MSIE 3.0') > -1)   || 
		(Main.userAgent.indexOf('MSIE 2.0') > -1)){
		Main.VisError(Msg.home["oldBrowser"]);
	}
}

Main.PowerLimitCbRete = function(valRete) {
		
	Main.contatoreRete = 0;
	
	if (Main.env == 0) console.log(20, Main.MODULE, "Param in Main.PowerLimitCbRete = " + valRete);
	// se null metto il default
	if (valRete == null){
		valRete = Define.home["contatoreReteDefault"];
		Main.contatoreRete = 0;
	}
	
	var tipoContatoreRete = Define.home['tipoContatoreRete'];
	if (Main.env == 0) console.log(20, Main.MODULE, "tipoContatoreRete = " + tipoContatoreRete);
	// converto valore massimo contatore in indice 0:2 per gli array per la definizione dei limiti
	for (i = 0; i < tipoContatoreRete.length; i++){
		if (tipoContatoreRete[i] == valRete) {
			//registro l'indice dell'array
			Main.contatoreRete = i;
		}
	}
	
	InterfaceEnergyHome.GetPowerLimit(Main.PowerLimitCb);

	if (Main.env == 0) console.log(20, Main.MODULE, "Power limit Rete = " + Main.contatoreRete);
}

//Metodo che gestisce il contatore della produzione di energia proveniente dai pannelli solari
Main.PowerLimitCbFotoVoltaico = function(valProd) {
		
	Main.contatoreProd = 0;
	
	if (Main.env == 0) console.log(20, Main.MODULE, "Param in Main.PowerLimitCbFotoVoltaico = " + valProd);
	// se null metto il default
	if (valProd == null){
		valProd = Define.home["contatoreProdDefault"];
		Main.contatoreProd = 2;
	}
	
	var tipoContatoreProd = Define.home['tipoContatoreProd'];
	if (Main.env == 0) console.log(20, Main.MODULE, "tipoContatoreProd = " + tipoContatoreProd);
	// converto valore massimo contatore in indice 0:2 per gli array per la definizione dei limiti
	for (i = 0; i < tipoContatoreProd.length; i++){
		if (tipoContatoreProd[i] == valProd) {
			//registro l'indice dell'array
			Main.contatoreProd = i;
		}
	}

	InterfaceEnergyHome.GetPowerLimitRete(Main.PowerLimitCbRete);

	if (Main.env == 0) console.log(20, Main.MODULE, "Power limit Produzione = " + Main.contatoreProd);
}

//Metodo che gestisce i valori relativi al contatore dei consumi (sempre presente in tutte e due le modalitˆ)
Main.PowerLimitCb = function(val) {
	Main.contatore = 0;
	
	if (Main.env == 0) console.log(20, Main.MODULE, "Param in Main.PowerLimitCb = " + val);
	// se null metto il default
	if (val == null){
		val = Define.home["contatoreDefault"];
	}
	
	if (Main.env == 0) console.log(20, Main.MODULE, "Define.home['tipoContatore'] = " + Define.home["tipoContatore"]);
	// converto valore massimo contatore in indice 0:2 per gli array per la definizione dei limiti
	for (i = 0; i < Define.home["tipoContatore"].length; i++){
		if (Define.home["tipoContatore"][i] == val) {
			//registro l'indice dell'array
			Main.contatore = i;
		}
	}
	
	if (Main.env == 0) console.log(20, Main.MODULE, "Power limit = " + Main.contatore);

	if (Main.env == 0) console.log('Main.enablePV', Main.enablePV);
	Menu.Init('MainMenu', 'ContentMenu');
}

Main.IdUtenteCb = function(userId, e) {

	if (e != null) {
		var err = Msg.visErr[InterfaceEnergyHome.ERR_NO_USER];
		Configurazione.HandleError(err);
		$("#userID").html(Msg.home['labelCodiceUtente'] + ':' + err);
		return;
	}
	
	if (userId == null) {
		var err = Msg.visErr[InterfaceEnergyHome.ERR_NO_USER];
		$("#userID").html(Msg.home['labelCodiceUtente'] + ':' + err);
	} else {
		Main.userId = userId;
		Main.hagId = 'hag-'+Main.userId;
		$("#userID").html(Msg.home['labelCodiceUtente'] + ':' + userId);
	}
	
	if (Main.enablePV == null){
		if (InterfaceEnergyHome.mode > 0) {
			try {
				InterfaceEnergyHome.objService.getAttribute(Main.setEnablePV, InterfaceEnergyHome.PRESENZA_PRODUZIONE);
			} catch (err) {
				if (Main.env == 0) console.log('exception in FotoVoltaico.js - in CostiConsumi.GetDatiPotenza method: ', err);
				InterfaceEnergyHome.GestErrorEH("IdUtenteCb", err);
			}
		} else {
			// per test
			var qsPV = new Querystring();
			var qstringPV = qsPV.get("fotov", "true");
			
			if (Main.env == 0) console.log('qstringPV = ' + qstringPV);
			if (qstringPV == "true"){
				Main.setEnablePV({value: 3000}, false);
			}else{
				Main.setEnablePV({value: 0}, false);
			}
		}
	} else {
		// legge tipo contatore 
		if (Main.enablePV){
			InterfaceEnergyHome.GetPowerLimitFotoVoltaico(Main.PowerLimitCbFotoVoltaico);
			//InterfaceEnergyHome.GetPowerLimitRete(Main.PowerLimitCbRete);
		} else {
			InterfaceEnergyHome.GetPowerLimit(Main.PowerLimitCb);
		}
	}
	
}

Main.setEnablePV = function(result, e){
	if (result.value > 0){
		Main.enablePV = true;
		
		addCSSinDocument('css/FotoVoltaico.css');
		addCSSinDocument('js/tinybox2/style.css');
		addJavaScriptinDocument('js/FotoVoltaico.js?201305315125');
		addJavaScriptinDocument('js/utils/jscroller-0.4.js?201305315125');
		addJavaScriptinDocument('js/tinybox2/tinybox.js?201305315125');
		addJavaScriptinDocument('js/DefineMenu.js?201305315125');
		addJavaScriptinDocument('js/startApp.js?201305315125');
		setDefineMenu();
	} else {
		Main.enablePV = false;
		addCSSinDocument('css/CostiConsumi2.css');
		addJavaScriptinDocument('js/CostiConsumi1.js?201305315125');
		addJavaScriptinDocument('js/DefineMenu.js?201305315125');
		addJavaScriptinDocument('js/startApp.js?201305315125');
		setDefineMenu();
	}
}

// dopo aver inizializzato l'ora fa il resto delle inizializzazioni
Main.InitValue = function() {
	Main.aggiornaTimestamp();
	// ogni minuto aggiorno data
	Main.timerTimeout = setInterval(function() {
		Main.aggiornaTimestamp();
	}, 60000);
	Tracing.Init(Tracing.HOME, Main.userAgent);

	/*
	 * $.get('http://localhost/~davideasinari/Server_CORS/ind.php',
	 * function(data) { alert(data); });
	 */
	if (InterfaceEnergyHome.mode > 0) {
		try {
			//InterfaceEnergyHome.getHapConnectionId(Main.IdUtenteCb);
			InterfaceEnergyHome.objService.getHapConnectionId(Main.IdUtenteCb);
		} catch (err) {
			InterfaceEnergyHome.GestErrorEH("InitValue", err);
		}
	} else {
		Main.IdUtenteCb(IDUtente, null);
	}
}

Main.onLoad = function() {

	var imgW, imgH;

	// se c'e' parametro cambio livello log
	var qs = new Querystring();
	var level = qs.get("level", "20");
	Log.setLevel(parseInt(level));

	$(document).ready(function() {
		Main.userAgent = navigator.userAgent;

		// Parse the current page's querystring
		var qs = new Querystring();

		var ids = new Array();
		var mode = qs.get("mode", "");
		if ((mode != null) && (mode != "")) {
			if (mode == "simul") {
				LazyScript.load("js/DataSimul.js", function() {
					GestDate.InitActualDate(Main.InitValue);
				});
				InterfaceEnergyHome.mode = 0;
			} else if (mode == "demo"){
				InterfaceEnergyHome.mode = 1;
			} else if (mode == "cost"){
				InterfaceEnergyHome.mode = 3;
			} else if (mode == "noserver"){
				LazyScript.load("js/DataSimulNoServer.js", function() {
					GestDate.InitActualDate(Main.InitValue);
				});
				InterfaceEnergyHome.mode = -1;
			} else if(mode == "noservernodev"){
				LazyScript.load("js/DataSimulNoServer.js", function() {
					GestDate.InitActualDate(Main.InitValue);
				});
				InterfaceEnergyHome.mode = -2;
			} else {
				InterfaceEnergyHome.mode = 2;
			}
		} else {
			InterfaceEnergyHome.mode = 2;
		}

		showSpinner();
		// se non precarico l'immagine di un elettrodomestico non funzionano gli elettrodomestici
		Main.imgDisp = new Image();
		Main.imgDisp.src = Define.home["defaultDispImg"];

		// inizializzo connessione ad AG
		var interfaceLoad = InterfaceEnergyHome.Init();

		if (Main.env == 0) console.log(20, Main.Module, "interfaceLoad = ");
		if (Main.env == 0) console.log(20, Main.Module, interfaceLoad);
		if (interfaceLoad == null){
			if (Main.env == 0) console.log(20, Main.MODULE,"onLoad: errore in connessione servizio");
		}
			

		Main.dataMinima = new Date("January 1, 2010 00:00:00").getTime();

		if ((mode != "simul") && (mode != "noserver") && (mode!="noservernodev")){
			GestDate.InitActualDate(Main.InitValue);
		}
	});
}

Main.onUnload = function() {
	if (GestDate.timerDate != null)
		clearInterval(GestDate.timerDate);
	if (Main.env == 0) console.log(20, Main.Module, "Main.onUnload");
	// faccio out da sezione attuale
	Tracing.Trace(null, Tracing.OUT, null, null);
	Tracing.Trace(Tracing.HOME, Tracing.OUT, null, null);
}

MostraCostiConsumi = function() {
	if (Main.env == 0) console.log(20, Main.Module, "Main.MostraCostiConsumi");
	$("#Content").hide();
	$(".Content").show();
}

NascondiCostiConsumi = function() {
	if (Main.env == 0) console.log(20, Main.Module, "Main.NascondiCostiConsumi");
	$(".Content").hide();
	$("#Content").show();
}

//ripulisco area che visualizza i messaggi di errore
Main.ResetError = function() {
	$("#ErrorMsgDIV").text("");
}
//Visualizzo messaggio di errore
Main.VisError = function(err) {
	var oldTxt = $("#ErrorMsgDIV").text();
	//Visualizzo il nuovo messaggio di errore solo se non � giˆ presente
	if ((Msg.visErr[err] != oldTxt) && (oldTxt.indexOf(Msg.visErr[err]) < 0)){
		$("#ErrorMsgDIV").html(oldTxt + ' <br /> ' + Msg.visErr[err]);
	}
	
}
