function DatiElettr() {
	this.id = null;
	this.nome = null;
	this.categoria = null;
	this.locazione = null;
	this.avail = 0;
	this.stato = null;
	this.value = null;
	this.icona = null;
	this.tipo = null;
} 

var InterfaceEnergyHome = {
	jsonrpc : null,
	MODULE : "InterfaceEnergyHome",
	mode : 2, // 0 : simulazione, 1 : rete
	MODE_SIMUL : 0,
	MODE_SIMUL_NO_SERVER : -1,
	MODE_DEMO : 1,
	MODE_FULL : 2,
	MODE_COST : 3,
	STATUS_OK : 0,
	STATUS_ERR : -1,
	STATUS_ERR_HTTP : -2,
	STATUS_EXCEPT : -3,
	HTTP_TIMEOUT : 7000,
	errMessage : null,
	errCode : 0,
	ERR_GENERIC : 0,
	ERR_CONN_AG : 1,
	ERR_CONN_SERVER : 2,
	ERR_DATA : 3,
	ERR_NO_SMARTINFO : 4,
	ERR_NO_USER : 5,
	ERR_NO_PRODUCTION : 6,
	visError : null,

	serviceName : "org.energy_home.jemma.ah.greenathome.GreenAtHomeApplianceService",
	objService : null,
	// costanti per parametri chiamate
	MINUTE : 0,
	HOUR : 1,
	DAY : 2,
	MONTH : 3,
	YEAR : 4,
	LAST : 0,
	FIRST : 1,
	MAX : 2,
	MIN : 3,
	AVG : 4,
	DELTA : 5,
	PID_TOTALE : null,
	//TODO: check merge, do these declarations need to be renamed?
	//WHITEGOOD_APP_TYPE : "com.indesit.ah.app.whitegood",
	WHITEGOOD_APP_TYPE : "org.energy_home.jemma.ah.zigbee.whitegood",
	//SMARTINFO_APP_TYPE : "it.telecomitalia.ah.zigbee.metering",
	SMARTINFO_APP_TYPE : "org.energy_home.jemma.ah.zigbee.metering",
	//SMARTPLUG_APP_TYPE : "it.telecomitalia.ah.zigbee.smartplug",
	SMARTPLUG_APP_TYPE : "org.energy_home.jemma.ah.zigbee.smartplug",
	LOCKDOOR_APP_TYPE : "org.energy_home.jemma.ah.zigbee.lockdoor", 
	WINDOWCOVERING_APP_TYPE : "org.energy_home.jemma.ah.zigbee.windowcovering", 
	TEMPERATURE_SENSOR_APP_TYPE : "org.energy_home.jemma.ah.zigbee.temperature_humidity",
	THERMOSTAT_SENSOR_APP_TYPE : "org.energy_home.jemma.ah.zigbee.thermostat", 
	TEMPERATURE_URMET_SENSOR_APP_TYPE : "org.energy_home.jemma.ah.zigbee.urmet.temperature_humidity",
	//To verify
	THERMOSTAT_SENSOR_APP_TYPE_2 : "org.energy_home.jemma.ah.zigbee.generic-ah.ep.zigbee.thermostat",
	LOCKDOOR_APP_TYPE_2 : "org.energy_home.jemma.ah.zigbee.generic-ah.ep.zigbee.DoorLock", 
	TEMPERATURE_SENSOR_APP_TYPE_2 : "org.energy_home.jemma.ah.zigbee.generic-ah.ep.zigbee.temperature_humidity",
	WINDOWCOVERING_APP_TYPE_2 : "org.energy_home.jemma.ah.zigbee.generic-ah.ep.zigbee.WindowCovering", 
	 
	POTENZA_TOTALE : "TotalPower", //potenza totale consumata in casa 
	PRODUZIONE_TOTALE : "ProducedPower", //potenza istantanea generata
	RETE_TOTALE : "SoldPower", //potenza istantanea  venduta alla rete (meglio usare nella gui solo i precedenti due valori, e ricavare per differenza questo, cos� si garantisce che i valori sono coerenti anche se le richieste json partono in istanti differenti)
	PRESENZA_PRODUZIONE : "PeakProducedPower", //potenza di picco degli impianti fotovoltaici (vale 0 se l�utente non ha nessun impianto fotovoltaico) e deve essere aggiunta alla gui di configurazione
	CONSUMO : "ah.eh.esp.Energy",
	COSTO : "ah.eh.esp.EnergyCost",
	PRODUZIONE : "ah.eh.esp.ProducedEnergy",
	SOLD : "ah.eh.esp.SoldEnergy",
	LIMITI : "InstantaneousPowerLimit",
	AG_APP_EXCEPTION : "org.energy_home.jemma.ah.hac.ApplianceException",
	SERVER_EXCEPTION : "org.energy_home.jemma.ah.eh.esp.ESPException",

	// costanti per nome attributi
	ATTR_APP_NAME : "ah.app.name",
	ATTR_APP_TYPE : "ah.app.type",
	ATTR_APP_PID : "appliance.pid",
	ATTR_APP_ICON : "ah.icon",
	ATTR_APP_LOCATION : "ah.location.pid",
	ATTR_APP_CATEGORY : "ah.category.pid",
	ATTR_APP_AVAIL : "availability",
	ATTR_APP_STATE_AVAIL : "device_state_avail",
	ATTR_APP_STATE : "device_state",
	ATTR_APP_VALUE : "device_value",
	ATTR_APP_VALUE_NAME : "name",
	ATTR_APP_VALUE_VALUE : "value",
	ATTR_LOCATION_PID : "pid",
	ATTR_LOCATION_NAME : "name",
	ATTR_LOCATION_ICON : "iconName",
	ATTR_CATEGORY_PID : "pid",
	ATTR_CATEGORY_NAME : "name",
	ATTR_CATEGORY_ICON : "iconName",

	backGetLocations : null,
	backSuggerimento : null,
	backCostoPrevisto : null,
	backCostoOdierno : null,
	backCostoMedio : null,
	backCostoGiornaliero : null,
	backSuddivisioneCosti : null,
	backConsumoPrevisto : null,
	backConsumoOdierno : null,
	backConsumoMedio : null,
	backConsumoGiornaliero : null,
	backPotenzaAttuale : null,
	backMaxElettr : null,
	backElettrStorico : null,
	backStorico : null,
	backListaElettr : null,
	backActualDate : null,
	backPowerLimit : null,
	backInitialTime : null
}

function bindService(name) {
	
	// crea client
	if (InterfaceEnergyHome.jsonrpc == null) {
		try {
			InterfaceEnergyHome.jsonrpc = new JSONRpcClient("/demo/JSON-RPC");
			InterfaceEnergyHome.jsonrpc.http_max_spare = 4;
			JSONRpcClient.toplevel_ex_handler = function(e) {
			};
		} catch (err) {
			console.log("ERRORE! => ");
			console.log(err);
			InterfaceEnergyHome.GestErrorEH(null, err);
			return null;
		}
	}
	// ricerca servizio
	sReg = new Array();
	try {
		sReg = InterfaceEnergyHome.jsonrpc.OSGi.find(InterfaceEnergyHome.serviceName);
	} catch (err) {
		console.log("ERRORE! => ");
		console.log(err);
		InterfaceEnergyHome.GestErrorEH(null, err);
		return null;
	}

	// fa bind al servizio
	if (sReg.list && sReg.list.length > 0) {
		try {
			InterfaceEnergyHome.jsonrpc.OSGi.bind(sReg.list[0]);
			InterfaceEnergyHome.objService = sReg.list[0].map['service.id'];
			return InterfaceEnergyHome.jsonrpc[InterfaceEnergyHome.objService];
		} catch (err) {
			console.log("ERRORE! => ");
			console.log(err);
			InterfaceEnergyHome.GestErrorEH(null, err);
		}
		return null;
	}
	return null;
}

InterfaceEnergyHome.Init = function() {
	try {
		InterfaceEnergyHome.errMessage = null;
		InterfaceEnergyHome.errCode = 0;
		if ((InterfaceEnergyHome.mode > 0) || ((InterfaceEnergyHome.mode == -1) || (InterfaceEnergyHome.mode == -2))){
			return InterfaceEnergyHome.objService = bindService(InterfaceEnergyHome.serviceName);
		} else {
			return 1;
		}
	} catch (err) {
		console.log("ERRORE! => ");
		console.log(err);
		InterfaceEnergyHome.GestErrorEH(null, err);
	}
	return 1;
}

// abortisce tutte le eventuali chiamate in corso
// per adesso annulla tutte le callback in modo che quando
// la funzione ritorna non richiama la callback
// bisogna provare se si riescono ad abortire le chiamate
InterfaceEnergyHome.Abort = function() {
	InterfaceEnergyHome.backGetLocations = null;
	InterfaceEnergyHome.backCostoPrevisto = null;
	InterfaceEnergyHome.backCostoOdierno = null;
	InterfaceEnergyHome.backCostoMedio = null;
	InterfaceEnergyHome.backCostoGiornaliero = null;
	InterfaceEnergyHome.backSuddivisioneCosti = null;
	InterfaceEnergyHome.backConsumoPrevisto = null;
	InterfaceEnergyHome.backConsumoOdierno = null;
	InterfaceEnergyHome.backConsumoMedio = null;
	InterfaceEnergyHome.backConsumoGiornaliero = null;
	InterfaceEnergyHome.backPotenzaAttuale = null;
	InterfaceEnergyHome.backMaxElettr = null;
	InterfaceEnergyHome.backElettrStorico = null;
	InterfaceEnergyHome.backStorico = null;
	InterfaceEnergyHome.backListaElettr = null;
	InterfaceEnergyHome.backActualDate = null;
	InterfaceEnergyHome.backPowerLimit = null,
	InterfaceEnergyHome.backInitialTime = null
}
/**
 * JSONRpcClient.Exception.CODE_REMOTE_EXCEPTION = 490;
 * JSONRpcClient.Exception.CODE_ERR_CLIENT = 550;
 * JSONRpcClient.Exception.CODE_ERR_PARSE = 590;
 * JSONRpcClient.Exception.CODE_ERR_NOMETHOD = 591;
 * JSONRpcClient.Exception.CODE_ERR_UNMARSHALL = 592;
 * JSONRpcClient.Exception.CODE_ERR_MARSHALL = 593;
 */
InterfaceEnergyHome.GestErrorEH = function(func, err) {
	//TODO: check merge, following line was commented in 3.3.0
	//hideSpinner();
	var msg;
	InterfaceEnergyHome.visError = InterfaceEnergyHome.ERR_GENERIC;
	
	// mi puo' arrivare msg o message a seconda da dove arriva l'errore
	msg = (err.msg == undefined) ? msg = err.message : msg = err.msg;
	
	if ((err.code == undefined) && (InterfaceEnergyHome.objService == null)) {
		tmpMsg = "Service not found";
		InterfaceEnergyHome.visError = InterfaceEnergyHome.ERR_CONN_AG;
	} else if (err.code == JSONRpcClient.Exception.CODE_REMOTE_EXCEPTION) {
		tmpMsg = msg + " " + err.name;
		//console.log('err.code', err.code);
		if (err.msg == "Invalid appliance pid"){
			InterfaceEnergyHome.visError = InterfaceEnergyHome.ERR_NO_SMARTINFO;
		} else if (err.name == InterfaceEnergyHome.SERVER_EXCEPTION){
			InterfaceEnergyHome.visError = InterfaceEnergyHome.ERR_CONN_SERVER;
		} else {
			InterfaceEnergyHome.visError = InterfaceEnergyHome.ERR_CONN_AG;
		}
	} else if (((err.code == 0) && (msg == "")) || (err.code == JSONRpcClient.Exception.CODE_ERR_CLIENT)) {
		tmpMsg = "Error in gateway connection";
		InterfaceEnergyHome.visError = InterfaceEnergyHome.ERR_CONN_AG;
	} else if (err.code == 1) {
		tmpMsg = "Error in peak production";
		InterfaceEnergyHome.visError = InterfaceEnergyHome.ERR_NO_PRODUCTION;
	} else {
		if (err.code != null) {
			tmpMsg = err.code;
			if (msg != null){
				tmpMsg += " " + msg;
			}
		} else {
			tmpMsg = msg;
		}
		InterfaceEnergyHome.visError = InterfaceEnergyHome.ERR_CONN_AG;
	}
	//console.log(20, InterfaceEnergyHome.MODULE, func + " code = " + err.code + " errMsg = " + tmpMsg);
	// nel caso di errore in inizializzazione della connessione AG non posso
	// fare trace perche' non riesco a contattare l'AG
	if (func != null){
		Tracing.Trace(null, Tracing.ERR, Tracing.ERR_GENERIC, tmpMsg);
	} 
	
	console.log("ERRORE! => ");
	console.log(err);
	
	// visualizzo l'errore
	Main.VisError(InterfaceEnergyHome.visError);
}

InterfaceEnergyHome.BackGetLocazioni = function(result, err) {
	if (InterfaceEnergyHome.backGetLocazioni != null) {
		if (err != null) {
			retVal = null;
			// in realta' puo' essere richiamato anche da configuratore
			InterfaceEnergyHome.GestErrorEH("BackGetLocazioni", err);
		} else {
			// ritorna dizionario pid-nome
			retVal = new Array();
			if (result != null) {
				for (i = 0; i < result.length; i++) {
					pid = result[i][InterfaceEnergyHome.ATTR_LOCATION_PID];
					name = result[i][InterfaceEnergyHome.ATTR_LOCATION_NAME];
					retVal[pid] = name;
				}
			}
		}
		//console.log(80, InterfaceEnergyHome.MODULE, "BackGetLocazioni  val = " + retVal);
		InterfaceEnergyHome.backGetLocazioni(retVal);
	}
}

InterfaceEnergyHome.GetLocazioni = function(backFunc) {
	InterfaceEnergyHome.backGetLocazioni = backFunc;
	//console.log(80, InterfaceEnergyHome.MODULE, "GetLocazioni");
	if (InterfaceEnergyHome.mode > 0) {
		try {
			InterfaceEnergyHome.objService.getLocations(InterfaceEnergyHome.BackGetLocazioni);
		} catch (err) {
			console.log("ERRORE! => ");
			console.log(err);
			InterfaceEnergyHome.BackGetLocazioni(null, err);
		}
	} else {
		// per test
		val = ListaLocazioni;
		InterfaceEnergyHome.BackGetLocazioni(val, null);
	}
}

// ritorna array di coppie di valori ["nome", "pid"]
InterfaceEnergyHome.BackElettrStorico = function(result, err) {
	if (InterfaceEnergyHome.backElettrStorico != null) {
		if (err != null) {
			retVal = null;
			InterfaceEnergyHome.GestErrorEH("BackElettrStorico", err);
		} else {
			// trascodifica dato : ritorno coppie ["nome", "pid"]
			retVal = new Array();
			if (result != null) {
				var j = 0;
				for (i = 0; i < result.list.length; i++) {
					if ((result.list[i].map[InterfaceEnergyHome.ATTR_APP_CATEGORY] != "44") && 
						(result.list[i].map[InterfaceEnergyHome.ATTR_APP_CATEGORY] != "40") && 
						(result.list[i].map[InterfaceEnergyHome.ATTR_APP_CATEGORY] != "45") && 
						(result.list[i].map[InterfaceEnergyHome.ATTR_APP_CATEGORY] != "36") && 
						(result.list[i].map[InterfaceEnergyHome.ATTR_APP_CATEGORY] != "41") && 
						(result.list[i].map[InterfaceEnergyHome.ATTR_APP_CATEGORY] != "35") && 
						(result.list[i].map[InterfaceEnergyHome.ATTR_APP_CATEGORY] != "34")){
						
						retVal[j] = new Object();
						retVal[j].nome = result.list[i].map[InterfaceEnergyHome.ATTR_APP_NAME];
						retVal[j].pid = result.list[i].map[InterfaceEnergyHome.ATTR_APP_PID];
						retVal[j].tipo = result.list[i].map[InterfaceEnergyHome.ATTR_APP_TYPE];
						j++;
					}
				}
			}
		}
		//console.log(80, InterfaceEnergyHome.MODULE, "BackElettrStorico val = " + retVal);
		InterfaceEnergyHome.backElettrStorico(retVal);
	}
}

InterfaceEnergyHome.GetElettrStorico = function(backFunc) {
	InterfaceEnergyHome.backElettrStorico = backFunc;
	//console.log(80, InterfaceEnergyHome.MODULE, "GetElettrStorico");

	if ((InterfaceEnergyHome.mode > 0) || ((InterfaceEnergyHome.mode == -1) || (InterfaceEnergyHome.mode == -2))) {
		try {
			InterfaceEnergyHome.objService.getAppliancesConfigurationsDemo(InterfaceEnergyHome.BackElettrStorico);
		} catch (err) {
			console.log("ERRORE! => ");
			console.log(err);
			InterfaceEnergyHome.BackElettrStorico(null, err);
		}
	} else {
		// per test
		val = ListaElettr1;
		InterfaceEnergyHome.BackElettrStorico(val, null);
	}
}

// ritorna array di valori (costo o consumo)
InterfaceEnergyHome.BackStorico = function(result, err) {

	if (InterfaceEnergyHome.backStorico != null) {
		if (err != null){
			InterfaceEnergyHome.GestErrorEH("BackStorico", err);
		}
		retVal = null;
		if ((err == null) && (result != null)){
			retVal = result.list;
		}
		//console.log(20, InterfaceEnergyHome.MODULE, "BackStorico retVal = " + retVal);
		InterfaceEnergyHome.backStorico(retVal);
	}
}

InterfaceEnergyHome.GetStorico = function(tipo, pid, dataInizio, dataFine, intervallo, backFunc) {
	var paramTr, param1, param2;

	InterfaceEnergyHome.backStorico = backFunc;
	var lbl1ForNoServberP = null;
	var lbl2ForNoServberP = null;
	var lbl3ForNoServberP = (pid == null) ? 'SI' : 'DEV';
	//console.log(80, InterfaceEnergyHome.MODULE, "GetStorico");
	if (tipo == "Costo"){
		lbl1ForNoServberP = null;
		param1 = InterfaceEnergyHome.COSTO;
	} else if (tipo == "Produzione"){
		lbl1ForNoServberP = 'Production';
		param1 = InterfaceEnergyHome.PRODUZIONE;
	} else{
		lbl1ForNoServberP = 'Energy';
		param1 = InterfaceEnergyHome.CONSUMO;
	}
	//TODO: check merge, following commented block was not in 3.3.0
/*	if (intervallo == -1) {
		param2 = InterfaceEnergyHome.MINUTE;
		// dataInizio.setHours(0);
		// dataFine.setHours(23);
		// dataFine.setMinutes(30);
		paramTr = Tracing.QT_IERI;
	} else if (intervallo == 0) {*/
	if (intervallo == 0) {
		lbl2ForNoServberP = 'DAY';
		param2 = InterfaceEnergyHome.HOUR;
		paramTr = Tracing.QT_IERI;
	} else if (intervallo == 3) {
		lbl2ForNoServberP = 'YEAR';
		param2 = InterfaceEnergyHome.MONTH;
		paramTr = Tracing.QT_ANNO;
	} else {
		param2 = InterfaceEnergyHome.DAY;
		if (intervallo == 1){
			lbl2ForNoServberP = 'WEEK';
			paramTr = Tracing.QT_SETT;
		} else {
			lbl2ForNoServberP = 'MONTH';
			paramTr = Tracing.QT_MESE;
		}
	}
	//Tracing.Trace(Tracing.HISTORY, Tracing.QUERY, paramTr, pid);
	// solo se anche piattaforma
	if (InterfaceEnergyHome.mode > 1) {
		try {
			InterfaceEnergyHome.objService.getAttributeData(InterfaceEnergyHome.BackStorico, pid, param1, 
															dataInizio.getTime(), dataFine.getTime(), param2, 
															true, InterfaceEnergyHome.DELTA);
		} catch (err) {
			console.log("ERRORE! => ");
			console.log(err);
			InterfaceEnergyHome.BackStorico(null, err);
		}
	} else {
		// per test
		
		try {
			//InterfaceEnergyHome.objService.getPropStoricoConfiguration(InterfaceEnergyHome.BackStorico, pid, param1, paramTr);
			var tmpIndex = lbl3ForNoServberP+lbl1ForNoServberP+lbl2ForNoServberP;
			InterfaceEnergyHome.BackStorico(fakeValues.Storico.map[tmpIndex], null);
		} catch (err) {
			console.log("ERRORE! => ");
			console.log(err);
			InterfaceEnergyHome.BackStorico(null, err);
		}
	}
}

// solo per catch eccezione
InterfaceEnergyHome.BackSendGuiLog = function(result, err) {
	//console.log(80, InterfaceEnergyHome.MODULE, "SendGuiLog:  " + err);
}

// solo se non in demo o simulazione
InterfaceEnergyHome.SendGuiLog = function(logText) {
	if (InterfaceEnergyHome.mode > 0) {
		try {
			InterfaceEnergyHome.objService.sendGuiLog(InterfaceEnergyHome.BackSendGuiLog, logText);
		} catch (err) {
			console.log("ERRORE! => ");
			console.log(err);
			//console.log(80, InterfaceEnergyHome.MODULE, "SendGuiLog: " + logText);
		}
	}
}

// legge la data da AG

InterfaceEnergyHome.BackActualDate = function(result, err) {

	if (InterfaceEnergyHome.backActualDate != null) {
		if (err != null){
			InterfaceEnergyHome.GestErrorEH("BackActualDate", err);
		}
		retVal = null;
		if ((err == null) && (result != null)){
			if ((InterfaceEnergyHome.mode > 0) || ((InterfaceEnergyHome.mode == -1) || (InterfaceEnergyHome.mode == -2))) {
				retVal = result;
			} else {
				retVal = Math.floor(result.list[0]);
			}
		}
		//console.log(80, InterfaceEnergyHome.MODULE, "BackActualDate retVal = " + retVal);
		InterfaceEnergyHome.backActualDate(retVal);
	}
}

InterfaceEnergyHome.GetActualDate = function(backFunc) {
	InterfaceEnergyHome.backActualDate = backFunc;
	if (InterfaceEnergyHome.mode > 0) {
		try {
			InterfaceEnergyHome.objService.currentTimeMillis(InterfaceEnergyHome.BackActualDate);
		} catch (err) {
			console.log("ERRORE! => ");
			console.log(err);
			InterfaceEnergyHome.BackActualDate(null, err);
		}
	} else{
		if ((InterfaceEnergyHome.mode == -1) || (InterfaceEnergyHome.mode == -2)) {
			InterfaceEnergyHome.backActualDate(new Date().getTime());
		} else {
			InterfaceEnergyHome.backActualDate(new Date().getTime());
		}
		// per simulazione ritorno ora di sistema
		//InterfaceEnergyHome.backActualDate(new Date().getTime());
		//InterfaceEnergyHome.objService.getPropConfiguration(InterfaceEnergyHome.BackActualDate, "ActualDate");
	}
}

InterfaceEnergyHome.GetPowerLimitFotoVoltaico = function(backFunc) {
	InterfaceEnergyHome.backPowerLimitFotoVoltaico = backFunc;
	start = Main.dataAttuale.getTime();
	if (InterfaceEnergyHome.mode > 0) {
		try {
			InterfaceEnergyHome.objService.getAttribute(InterfaceEnergyHome.BackPowerLimitFotoVoltaico, InterfaceEnergyHome.PRESENZA_PRODUZIONE);
		} catch (err) {
			console.log("ERRORE! => ");
			console.log(err);
			InterfaceEnergyHome.BackPowerLimitFotoVoltaico(null, err);
		}
	} else {
		InterfaceEnergyHome.BackPowerLimitFotoVoltaico({value: 3000}, null);
	}
}

InterfaceEnergyHome.BackPowerLimitFotoVoltaico = function(result, err) {
	if (InterfaceEnergyHome.backPowerLimitFotoVoltaico != null) {
		if (err != null){
			// InterfaceEnergyHome.GestErrorEH("BackPowerLimit", err);
			//console.log(80, InterfaceEnergyHome.MODULE, "Eccezione in GetPowerLimit: " + err.msg);
		}
		if (result != null){
			retVal = result.value;
		} else {
			retVal = null;
		}
		//console.log(80, InterfaceEnergyHome.MODULE, "BackPowerLimit retVal = " + retVal);
		InterfaceEnergyHome.backPowerLimitFotoVoltaico(retVal);
	}
}

InterfaceEnergyHome.GetPowerLimitRete = function(backFunc) {
	InterfaceEnergyHome.backPowerLimitRete = backFunc;
	start = Main.dataAttuale.getTime();
	if (InterfaceEnergyHome.mode > 0) {
		try {
			InterfaceEnergyHome.objService.getAttribute(InterfaceEnergyHome.BackPowerLimitRete, InterfaceEnergyHome.LIMITI);
		} catch (err) {
			console.log("ERRORE! => ");
			console.log(err);
			InterfaceEnergyHome.BackPowerLimitRete(null, err);
		}
	} else {
		InterfaceEnergyHome.BackPowerLimitRete(null, null);
	}
}

InterfaceEnergyHome.BackPowerLimitRete = function(result, err) {
	if (InterfaceEnergyHome.backPowerLimitRete != null) {
		if (err != null){
			// InterfaceEnergyHome.GestErrorEH("BackPowerLimit", err);
			//console.log(80, InterfaceEnergyHome.MODULE, "Eccezione in GetPowerLimit: " + err.msg);
		}
		if (result != null){
			retVal = result.value;
		} else {
			retVal = null;
		}
		//console.log(80, InterfaceEnergyHome.MODULE, "BackPowerLimit retVal = " + retVal);
		InterfaceEnergyHome.backPowerLimitRete(retVal);
	}
}

InterfaceEnergyHome.GetPowerLimit = function(backFunc) {
	InterfaceEnergyHome.backPowerLimit = backFunc;
	start = Main.dataAttuale.getTime();
	if (InterfaceEnergyHome.mode > 0) {
		try {
			InterfaceEnergyHome.objService.getAttribute(InterfaceEnergyHome.BackPowerLimit, InterfaceEnergyHome.LIMITI);
		} catch (err) {
			console.log("ERRORE! => ");
			console.log(err);
			InterfaceEnergyHome.BackPowerLimit(null, err);
		}
	} else {
		InterfaceEnergyHome.BackPowerLimit(null, null);
	}
}

InterfaceEnergyHome.BackPowerLimit = function(result, err) {
	if (InterfaceEnergyHome.backPowerLimit != null) {
		if (err != null){
			// InterfaceEnergyHome.GestErrorEH("BackPowerLimit", err);
			//console.log(80, InterfaceEnergyHome.MODULE, "Eccezione in GetPowerLimit: " + err.msg);
		}
		if (result != null){
			retVal = result.value;
		} else {
			retVal = null;
		}
		//console.log(80, InterfaceEnergyHome.MODULE, "BackPowerLimit retVal = " + retVal);
		InterfaceEnergyHome.backPowerLimit(retVal);
	}
}

InterfaceEnergyHome.BackInitialTime = function(result, err) {

	if (InterfaceEnergyHome.backInitialTime != null) {
		if (err != null) {
			InterfaceEnergyHome.GestErrorEH("BackInitialTime", err);
		}
		retVal = result;
		//console.log(80, InterfaceEnergyHome.MODULE, "BackInitialTime installDate = " + new Date(retVal).toString());
		InterfaceEnergyHome.backInitialTime(retVal);
	}
}

// legge data in cui e' stata fatta la configurazione iniziale
InterfaceEnergyHome.GetInitialTime = function(backFunc) {
	InterfaceEnergyHome.backInitialTime = backFunc;
	if (InterfaceEnergyHome.mode > 0) {
		try {
			InterfaceEnergyHome.objService.getInitialConfigurationTime(InterfaceEnergyHome.BackInitialTime);
		} catch (err) {
			console.log("ERRORE! => ");
			console.log(err);
			InterfaceEnergyHome.BackInitialTime(null, err);
		}
	} else
		InterfaceEnergyHome.BackInitialTime(new Date("September 15, 2011 23:59:00").getTime(), null);
}
