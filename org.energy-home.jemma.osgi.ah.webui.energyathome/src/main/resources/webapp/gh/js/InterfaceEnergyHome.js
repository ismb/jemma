function DatiElettr () {
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
	MODE_DEMO : 1,
	MODE_FULL : 2,
	MODE_COST : 3,
	STATUS_OK : 0,
	STATUS_ERR : -1,
	STATUS_ERR_HTTP : -2,
	STATUS_EXCEPT : -3,
	HTTP_TIMEOUT : 7000,
	errMessage: null,
	errCode: 0,
	ERR_GENERIC : 0,
	ERR_CONN_AG : 1,
	ERR_CONN_SERVER : 2,
	ERR_DATA : 3,
	visError : null,
	
	serviceName : "it.telecomitalia.ah.greenathome.GreenAtHomeApplianceService",
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
	SMARTINFO_APP_TYPE : "it.telecomitalia.ah.zigbee.metering",
	POTENZA_TOTALE : "TotalPower",
	CONSUMO : "ah.eh.esp.Energy",
	COSTO : "ah.eh.esp.EnergyCost",
	LIMITI : "InstantaneousPowerLimit",
	AG_APP_EXCEPTION : "it.telecomitalia.ah.hac.ApplianceException",
	SERVER_EXCEPTION : "it.telecomitalia.ah.eh.esp.ESPException",
	

	// costanti per nome attributi
	ATTR_APP_NAME : "ah.app.name",
	ATTR_APP_TYPE : "ah.app.type",
	ATTR_APP_PID : "appliance.pid",
	ATTR_APP_ICON : "ah.icon",
	ATTR_APP_LOCATION: "ah.location.pid",
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
	backSuggerimento: null,
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
	if (InterfaceEnergyHome.jsonrpc == null) 
	{
		try
		{
			InterfaceEnergyHome.jsonrpc = new JSONRpcClient(OSGi.getServletURI());
			InterfaceEnergyHome.jsonrpc.http_max_spare = 4;
			JSONRpcClient.toplevel_ex_handler = function (e) {
				 Log.alert(20, "JSONRpcClient", "Eccezione in JSONRpcClient: " + e.message);
			};
		}
		catch(err)
		{
			InterfaceEnergyHome.GestErrorEH(null, err);
			return null;
		}
	}
	// ricerca servizio
	sReg = new Array();
    try {  	
    	sReg = InterfaceEnergyHome.jsonrpc.OSGi.find(InterfaceEnergyHome.serviceName);
    }
    catch (err) {
    	InterfaceEnergyHome.GestErrorEH(null, err);
		return null;
    }
    
    // fa bind al servizio
    if (sReg.list && sReg.list.length > 0)
    {
    	try {
    		InterfaceEnergyHome.jsonrpc.OSGi.bind(sReg.list[0]);
    		InterfaceEnergyHome.objService = sReg.list[0].map['service.id'];
    		return InterfaceEnergyHome.jsonrpc[InterfaceEnergyHome.objService];
    	}
    	catch(err) {
    		InterfaceEnergyHome.GestErrorEH(null, err);
    	}
		return null;
    }
   	
    return null;
} 

InterfaceEnergyHome.Init = function() 
{ 
	InterfaceEnergyHome.errMessage = null;
	InterfaceEnergyHome.errCode = 0;
	if (InterfaceEnergyHome.mode > 0)
		return InterfaceEnergyHome.objService = bindService(InterfaceEnergyHome.serviceName);
	else
		return 1;
}



// abortisce tutte le eventuali chiamate in corso
// per adesso annulla tutte le callback in modo che quando 
// la funzione ritorna non richiama la callback
// bisogna provare se si riescono ad abortire le chiamate
InterfaceEnergyHome.Abort = function ()
{
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
JSONRpcClient.Exception.CODE_ERR_CLIENT = 550;
JSONRpcClient.Exception.CODE_ERR_PARSE = 590;
JSONRpcClient.Exception.CODE_ERR_NOMETHOD = 591;
JSONRpcClient.Exception.CODE_ERR_UNMARSHALL = 592;
JSONRpcClient.Exception.CODE_ERR_MARSHALL = 593;
 */
InterfaceEnergyHome.GestErrorEH = function(func, err)
{
	var msg;
	InterfaceEnergyHome.visError = InterfaceEnergyHome.ERR_GENERIC;
	
	// mi puo' arrivare msg o message a seconda da dove arriva l'errore
	if (err.msg == undefined)
		msg = err.message;
	else
		msg = err.msg;
	if ((err.code == undefined) && (InterfaceEnergyHome.objService == null))
	{
		tmpMsg = "Service not found";
		InterfaceEnergyHome.visError = InterfaceEnergyHome.ERR_CONN_AG;
	}
	else
		if (err.code == JSONRpcClient.Exception.CODE_REMOTE_EXCEPTION) 
		{
			tmpMsg = msg + " " + err.name;
			if (err.name == InterfaceEnergyHome.SERVER_EXCEPTION)
				InterfaceEnergyHome.visError = InterfaceEnergyHome.ERR_CONN_SERVER;
			else
				InterfaceEnergyHome.visError = InterfaceEnergyHome.ERR_CONN_AG;
		}
		else
			if (((err.code == 0) && (msg == "")) || (err.code == JSONRpcClient.Exception.CODE_ERR_CLIENT))
			{
				tmpMsg = "Error in gateway connection";
				InterfaceEnergyHome.visError = InterfaceEnergyHome.ERR_CONN_AG;
			}
			else
			{
				if (err.code != null)
				{
					tmpMsg = err.code;
					if (msg != null)
						tmpMsg += " " + msg;
				}
				else
					tmpMsg = msg;
				InterfaceEnergyHome.visError = InterfaceEnergyHome.ERR_CONN_AG;
			}
	Log.alert(20, InterfaceEnergyHome.MODULE, func + " code = " + err.code + " errMsg = " + tmpMsg);
	// nel caso di errore in inizializzazione della connessione AG non posso fare trace perche'
	// non riesco a contattare l'AG
	if (func != null)
		Tracing.Trace(null, Tracing.ERR, Tracing.ERR_GENERIC, tmpMsg);
	// visualizzo l'errore 
	Main.VisError(InterfaceEnergyHome.visError);
}


InterfaceEnergyHome.BackGetLocazioni = function (result, err)
{
	if (InterfaceEnergyHome.backGetLocazioni != null)
	{	
		if (err != null)
		{
			retVal = null;
			// in realta' puo' essere richiamato anche da configuratore
			InterfaceEnergyHome.GestErrorEH("BackGetLocazioni", err);
		}
		else
		{
		// ritorna dizionario pid-nome
			retVal = new Array();
			if (result != null)
			{
				for (i = 0; i < result.length; i++)
				{
					pid = result[i][InterfaceEnergyHome.ATTR_LOCATION_PID];
					name = result[i][InterfaceEnergyHome.ATTR_LOCATION_NAME];
					retVal[pid] = name;
				}
			}
		}
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackGetLocazioni  val = "  + retVal);
		InterfaceEnergyHome.backGetLocazioni(retVal);    
	}
}

InterfaceEnergyHome.GetLocazioni = function (backFunc)
{
	InterfaceEnergyHome.backGetLocazioni = backFunc;
	Log.alert(80, InterfaceEnergyHome.MODULE, "GetLocazioni");
		if (InterfaceEnergyHome.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.getLocations(InterfaceEnergyHome.BackGetLocazioni);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackGetLocazioni(null, err);
		}
	}
	else
	{	
		// per test
		val = ListaLocazioni;
		InterfaceEnergyHome.BackGetLocazioni(val, null);
	}
}


//ritorna numero, con possibili decimali, che indica costo in euro, null se errore
InterfaceEnergyHome.BackSuggerimento = function (result, err)
{
	if (InterfaceEnergyHome.backSuggerimento != null)
	{
		if (err != null)
		{
			retVal = null;
			InterfaceEnergyHome.GestErrorEH("BackSuggerimento", err);
		}
		else
		{
	// eventuale trascodifica dato 
			retVal = result;
		}
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackSuggerimento val = "  + retVal);
	
		InterfaceEnergyHome.backSuggerimento(retVal);    
	}
}

InterfaceEnergyHome.GetSuggerimento = function (backFunc)
{
	InterfaceEnergyHome.backSuggerimento = backFunc;
	/**
	if (InterfaceEnergyHome.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.getAttributeData(InterfaceEnergyHome.BackSuggerimento, "Suggerimento");
		}
		catch (err)
		{
			InterfaceEnergyHome.BackSuggerimento (null, err);
		}
	}
	else 
	**/
	{	
		// per test
		if (Define.lang == "it")
			sugg = SuggerimentiIt;
		else
			sugg = SuggerimentiEn;
		ind = Math.round(Math.random() * (sugg.length));
		val = sugg[ind];
		Log.alert(80,  InterfaceEnergyHome.MODULE, "GetSuggerimento ind = " + ind + " val = " + val);
		InterfaceEnergyHome.BackSuggerimento(val, null);
	}
}


//ritorna numero, con possibili decimali, che indica costo in euro, null se errore
InterfaceEnergyHome.BackCostoOdierno = function (result, err)
{
	
	if (InterfaceEnergyHome.backCostoOdierno != null)
	{
		if (err != null)
			InterfaceEnergyHome.GestErrorEH("BackCostoOdierno", err);
		retVal = null;
		if ((err == null) && (result != null))
		{
			if (result.list.length > 0)
				// per come ho impostato la chiamata ho solo un valore
				retVal = result.list[0];
		}
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackCostoOdierno retVal = "  + retVal);
		InterfaceEnergyHome.backCostoOdierno(retVal);    
	}
}

InterfaceEnergyHome.GetCostoOdierno = function (backFunc)
{
	start = Main.dataAttuale.getTime();
	InterfaceEnergyHome.backCostoOdierno = backFunc;
	/**/
	if (InterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getAttributeData(InterfaceEnergyHome.BackCostoOdierno, 
					InterfaceEnergyHome.PID_TOTALE, InterfaceEnergyHome.COSTO, 
					start, start, InterfaceEnergyHome.DAY, true, InterfaceEnergyHome.DELTA);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackCostoOdierno(null, err);
		}
	}
	else
	/**/
	{		
		// per test
		indCostoOdierno += 1;
		if (indCostoOdierno == CostoOdierno.length)
			indCostoOdierno = 0;
		costoLista = CostoOdierno[indCostoOdierno];
		
		// prendo percentuale del costo in base all'ora
		attuale = GestDate.GetActualDate();
		oraAttuale = attuale.getHours();
		minAttuale = attuale.getMinutes();
		costo = 0;
		for (i = 0; i < oraAttuale; i++)
		{
			costo += costoLista.list[i];
		}
		// aggiungo percentuale in base ai minuti dell'ora attuale
		costo += costoLista.list[oraAttuale] * (minAttuale / 60);
		val = {"list":[costo]};
		Log.alert(80,  InterfaceEnergyHome.MODULE, "GetCostoOdierno val = " + val + " ind = " + ind);
		InterfaceEnergyHome.BackCostoOdierno(val, null);
	}
}

//ritorna numero, con possibili decimali, che indica costo in euro, null se errore
InterfaceEnergyHome.BackCostoPrevisto = function (result, err)
{
	
	if (InterfaceEnergyHome.backCostoPrevisto!= null)
	{
		if (err != null)
			InterfaceEnergyHome.GestErrorEH("BackCostoPrevisto", err);
		if ((err == null) && (result != null))
			retVal = result; // non c'e' il campo value ?!?
		else
			retVal = null;
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackCostoPrevisto retVal = "  + retVal);
		InterfaceEnergyHome.backCostoPrevisto(retVal);    
	}
}

InterfaceEnergyHome.GetCostoPrevisto = function (backFunc)
{
	InterfaceEnergyHome.backCostoPrevisto = backFunc;
	/**/
	if (InterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getForecast(InterfaceEnergyHome.BackCostoPrevisto, 
				InterfaceEnergyHome.PID_TOTALE, InterfaceEnergyHome.COSTO, Main.dataAttuale.getTime(), InterfaceEnergyHome.MONTH);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackCostoPrevisto(null, err);
		}
	}
	else
	/**/
	{		
		// per test
		Log.alert(80,  InterfaceEnergyHome.MODULE, "GetCostoPrevisto: ind = " + ind);
		val =  CostoPrevisto;
		InterfaceEnergyHome.BackCostoPrevisto(val, null);
	}
}	

//ritorna array di numeri, con possibili decimali, che indica il costo in euro, null se errore
//ogni valore corrisponde ad un'ora, partendo da 0
InterfaceEnergyHome.BackCostoGiornaliero = function (result, err)
{
	
	if (InterfaceEnergyHome.backCostoGiornaliero!= null)
	{	
		if (err != null)
			InterfaceEnergyHome.GestErrorEH("BackCostoGiornaliero", err);
		retVal = null;
		if ((err == null) && (result != null))
			retVal = result.list;
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackCostoGiornaliero retVal = "  + retVal);
		InterfaceEnergyHome.backCostoGiornaliero(retVal);    
	}
}

InterfaceEnergyHome.GetCostoGiornaliero = function (backFunc)
{
	InterfaceEnergyHome.backCostoGiornaliero = backFunc;
	start = new Date(Main.dataAttuale.getTime());
	start.setHours(0);
	end = Main.dataAttuale.getTime();
	/**/
	if (InterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getAttributeData(InterfaceEnergyHome.BackCostoGiornaliero, 
				InterfaceEnergyHome.PID_TOTALE,	InterfaceEnergyHome.COSTO, start.getTime(), end, 
				InterfaceEnergyHome.HOUR, true, InterfaceEnergyHome.DELTA);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackCostoGiornaliero(null, err);
		}
	}
	else
	/**/
	{		
		// per test, copio per il numero ore attuale
		hours = Main.dataAttuale.getHours();
		val = CostoGiornaliero;
		val.list = val.list.slice(0, hours);
		Log.alert(80,  InterfaceEnergyHome.MODULE, "GetCostoGiornaliero");
		InterfaceEnergyHome.BackCostoGiornaliero(val, null);
	}
}


//ritorna numero, con possibili decimali, che indica costo in euro, null se errore
InterfaceEnergyHome.BackCostoMedio = function (result, err)
{
	
	if (InterfaceEnergyHome.backCostoMedio!= null)
	{
		if (err != null)
			InterfaceEnergyHome.GestErrorEH("BackCostoMedio", err);
		if ((err != null) || (result == null))
			retVal = null;
		else
		{
			/** tolto 29-11-2011 : faccio calcolo nel chiamante perche' prendo tanti valori 
			 * quanti sono quelli del consumo giornaliero
			// trascodifica dato : mi viene ritornato il costo medio per ora nelle 24 ore
			// sommo il costo per le ore fino all'ora attuale
			attuale = GestDate.GetActualDate();
			oraAttuale = attuale.getHours();
			minAttuale = attuale.getMinutes();
			retVal = 0;
			// se c'e' anche solo un valore nullo ritorno null
			for (i = 0; i < oraAttuale; i++)
			{
				if (result.list[i] != null)
					retVal += result.list[i];
				else
				{
					retVal = null;
					break;
				}
			}
			// aggiungo percentuale in base ai minuti dell'ora attuale
			if ((result.list[oraAttuale] != null) && (retVal != null))
				retVal += result.list[oraAttuale] * (minAttuale / 60);
			else
				retVal = null;
				**/
			retVal = result.list;
		}
		Log.alert(80, InterfaceEnergyHome.MODULE, "BackCostoMedio val = "  + retVal);
		InterfaceEnergyHome.backCostoMedio(retVal);    
	}
}

InterfaceEnergyHome.GetCostoMedio = function (backFunc)
{
	weekDay = Main.dataAttuale.getDay() + 1; // js comincia da 0, java da 1
	InterfaceEnergyHome.backCostoMedio = backFunc;
	/**/
	if (InterfaceEnergyHome.mode > 1)// solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getWeekDayAverage(InterfaceEnergyHome.BackCostoMedio , 
					InterfaceEnergyHome.PID_TOTALE,	InterfaceEnergyHome.COSTO, weekDay);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackCostoMedio (null, err);
		}
	}
	else
	/**/
	{	
		// per test
		val = CostoMedio;  
		Log.alert(80,  InterfaceEnergyHome.MODULE, "GetCostoMedio : ind = " + ind + " val = " + val);
		InterfaceEnergyHome.BackCostoMedio(val, null);
	}
}


//ritorna array del tipo: [['Lavatrice', 25], ['Frigo', 9], ['PC', 5], ['Altro', 51] ]
//dove il numero e' la percentuale rispetto al totale 
InterfaceEnergyHome.BackSuddivisioneCosti = function (result, err)
{
	if (InterfaceEnergyHome.backSuddivisioneCosti != null)
	{	
		retVal = null;
		if (err != null)
			InterfaceEnergyHome.GestErrorEH("BackSuddivisioneCosti", err);
		if ((err == null) && (result != null))
		{
		// trascodifica dato 
			retVal = new Array();
			n = 0;
			ret = result.map;
			// converto le coppie "id": valore del map ricevuto (list)
			// in un'array del tipo: [['Lavatrice', 25], ['Frigo', 9], ['PC', 5], ['Altro', 51] ];
			for (el in ret)
			{
				val = ret[el];
				if (val != null)
				{
					if (val.list.length == 0)
						val = null;
					else
						val = val.list[0];
				}
				retVal[n] = new Array(el, val);
				n++;
			}
		}
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackSuddivisioneCosti val = "  + retVal);
		InterfaceEnergyHome.backSuddivisioneCosti(retVal);    
	}
}

InterfaceEnergyHome.GetSuddivisioneCosti = function (backFunc)
{
	start = new Date(Main.dataAttuale.getTime());
	//start.setDate(0);
	end = new Date(Main.dataAttuale.getTime());
	InterfaceEnergyHome.backSuddivisioneCosti = backFunc;
	/**/
	if (InterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getAttributeData(InterfaceEnergyHome.BackSuddivisioneCosti, InterfaceEnergyHome.COSTO, 
				start.getTime(), end.getTime(), InterfaceEnergyHome.MONTH, true, InterfaceEnergyHome.DELTA);

		}
		catch (err)
		{
			InterfaceEnergyHome.BackSuddivisioneCosti (null, err);
		}
	}
	else
	/**/
	{		
		// per test
		ind = Math.round(Math.random() * SuddivisioneCosti.length);
		Log.alert(80,  InterfaceEnergyHome.MODULE, "GetSuddivisioneCosti: ind = " + ind);
		val = SuddivisioneCosti;
		InterfaceEnergyHome.BackSuddivisioneCosti(val, null);
	}
}


//ritorna una struttura con pid, nome e valore dell'elettrodomestico che sta consumando di piu'
//leggo tutti gli elettrodomestici e vedo quale ha il valore (di consumo) maggiore
InterfaceEnergyHome.BackMaxElettr = function (result, err)
{
	if (InterfaceEnergyHome.backMaxElettr != null)
	{	
		if (err != null)
			InterfaceEnergyHome.GestErrorEH("BackMaxElettr", err);
		retVal = null;
		if ((err == null) && (result != null))
		{
		// eventuale trascodifica dato 
		// cerco l'elettrodomestico con consumo istantaneo maggiore, escluso smart info
			len = result.list.length;
			maxVal = -1;
			maxInd = -1;
			for (i = 0; i < len; i++)
			{
				res = result.list[i];
				//if ((res.map[InterfaceEnergyHome.ATTR_APP_STATE_AVAIL] == undefined) || 
				//	(res.map[InterfaceEnergyHome.ATTR_APP_STATE_AVAIL] == true))
				if ((res.map[InterfaceEnergyHome.ATTR_APP_AVAIL] == 2) && 
					(res.map[InterfaceEnergyHome.ATTR_APP_TYPE] != InterfaceEnergyHome.SMARTINFO_APP_TYPE))
				{
					if (res.map[InterfaceEnergyHome.ATTR_APP_VALUE] == undefined)
						val = 0;
					else
						val = parseFloat(res.map[InterfaceEnergyHome.ATTR_APP_VALUE].value.value);
					if (val > maxVal)
					{
						maxVal = val;
						maxInd = i;
					}
				}
			}	
			if (maxInd != -1)
			{
				retVal = new DatiElettr();
				retVal.nome = result.list[maxInd].map[InterfaceEnergyHome.ATTR_APP_NAME];;
				retVal.icona = result.list[maxInd].map[InterfaceEnergyHome.ATTR_APP_ICON];
				retVal.tipo = result.list[maxInd].map[InterfaceEnergyHome.ATTR_APP_TYPE];
				retVal.value = maxVal;
				Log.alert(80,  InterfaceEnergyHome.MODULE, "BackMaxElettr : nome = " + retVal.nome);
			}	
		}
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackMaxElettr retVal = "  + retVal);
		InterfaceEnergyHome.backMaxElettr(retVal);    
	}
}

InterfaceEnergyHome.GetMaxElettr = function (backFunc)
{
	InterfaceEnergyHome.backMaxElettr = backFunc;
	if (InterfaceEnergyHome.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.getAppliancesConfigurations(InterfaceEnergyHome.BackMaxElettr);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackMaxElettr(null, err);
		}
	}
	else
	{	
		// per test
		if (indLista == 0)
		{
			val = ListaElettr;
			//val = {"list":[]};
			indLista = 1;
		}	
		else
		{
			val = ListaElettr1;
			indLista = 0;
		}
		InterfaceEnergyHome.BackMaxElettr(val, null);
	}
}


//ritorna numero, con possibili decimali, che indica consumo in kWh (w ??), null se errore
InterfaceEnergyHome.BackConsumoOdierno = function (result, err)
{
	if (InterfaceEnergyHome.backConsumoOdierno != null)
	{
		if (err != null)
			InterfaceEnergyHome.GestErrorEH("BackConsumoOdierno", err);
		retVal = null;
		if ((err == null) && (result != null))
		{
			if (result.list.length > 0)
				retVal = result.list[0];
		}
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackConsumoOdierno retVal = "  + retVal);
		InterfaceEnergyHome.backConsumoOdierno(retVal);    
	}
}

InterfaceEnergyHome.GetConsumoOdierno = function (backFunc)
{
	start = Main.dataAttuale.getTime();
	InterfaceEnergyHome.backConsumoOdierno = backFunc;
	/**/
	if (InterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			var res = InterfaceEnergyHome.objService.getAttributeData(InterfaceEnergyHome.BackConsumoOdierno, 
				InterfaceEnergyHome.PID_TOTALE, InterfaceEnergyHome.CONSUMO, 
				start, start, InterfaceEnergyHome.DAY, true, InterfaceEnergyHome.DELTA);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackConsumoOdierno(null, err);
		}
	}
	else
	/**/
	{	
		// per test
		indConsumoOdierno += 1;
		if (indConsumoOdierno == ConsumoOdierno.length)
			indConsumoOdierno = 0;
		consumoLista = ConsumoOdierno[indConsumoOdierno];
		
		// prendo percentuale del costo in base all'ora
		attuale = GestDate.GetActualDate();
		oraAttuale = attuale.getHours();
		minAttuale = attuale.getMinutes();
		consumo = 0;
		for (i = 0; i < oraAttuale; i++)
		{
			consumo += consumoLista.list[i];
		}
		// aggiungo percentuale in base ai minuti dell'ora attuale
		consumo += consumoLista.list[oraAttuale] * (minAttuale / 60);
		val = {"list":[consumo]};
		InterfaceEnergyHome.BackConsumoOdierno(val, null);
	}
}

//ritorna numero, con possibili decimali, che indica consumo in kWh, null se errore
InterfaceEnergyHome.BackConsumoPrevisto = function (result, err)
{
	if (InterfaceEnergyHome.backConsumoPrevisto != null)
	{	
		if (err != null)
			InterfaceEnergyHome.GestErrorEH("BackConsumoPrevisto", err);
		retVal = null;
		if ((err == null) && (result != null))
				retVal = result;
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackConsumoPrevisto retVal = "  + retVal);
		InterfaceEnergyHome.backConsumoPrevisto(retVal);    
	}
}

InterfaceEnergyHome.GetConsumoPrevisto = function (backFunc)
{
	start = Main.dataAttuale.getTime();
	InterfaceEnergyHome.backConsumoPrevisto = backFunc;
	/**/
	if (InterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getForecast(InterfaceEnergyHome.BackConsumoPrevisto, 
				InterfaceEnergyHome.PID_TOTALE, InterfaceEnergyHome.CONSUMO, start, InterfaceEnergyHome.MONTH);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackConsumoPrevisto(null, err);
		}
	}
	else
	/**/
	{	
		// per test
		val =  ConsumoPrevisto;
		InterfaceEnergyHome.BackConsumoPrevisto(val, null);
	}
}

//ritorna array di numeri, con possibili decimali, che indica consumo in kWh, null se errore
//ogni valore corrisponde ad un'ora, partendo da 0
InterfaceEnergyHome.BackConsumoGiornaliero = function (result, err)
{
	
	if (InterfaceEnergyHome.backConsumoGiornaliero!= null)
	{	
		if (err != null)
			InterfaceEnergyHome.GestErrorEH("BackConsumoGiornaliero", err);
		retVal = null;
		if ((err == null) && (result != null))
			retVal = result.list;
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackConsumoGiornaliero retVal = "  + retVal);
		InterfaceEnergyHome.backConsumoGiornaliero(retVal);    
	}
}

InterfaceEnergyHome.GetConsumoGiornaliero = function (backFunc)
{
	InterfaceEnergyHome.backConsumoGiornaliero = backFunc;
	start = new Date(Main.dataAttuale.getTime());
	start.setHours(0);
	end = Main.dataAttuale.getTime();
	Log.alert(20,  InterfaceEnergyHome.MODULE, "GetConsumoGiornaliero: ini = " + start.toString() + 
					" fine = " + new Date(end).toString() + " attuale = " + Main.dataAttuale.toString());
	/**/
	if (InterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getAttributeData(InterfaceEnergyHome.BackConsumoGiornaliero , 
				InterfaceEnergyHome.PID_TOTALE,	InterfaceEnergyHome.CONSUMO, start.getTime(), end, 
				InterfaceEnergyHome.HOUR, true, InterfaceEnergyHome.DELTA);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackConsumoGiornaliero (null, err);
		}
	}
	else
	/**/
	{		
		// per test, copio per il numero ore attuale
		hours = Main.dataAttuale.getHours();
		val = ConsumoGiornaliero;
		val.list = val.list.slice(0, hours);
		Log.alert(80,  InterfaceEnergyHome.MODULE, "GetConsumoGiornaliero");
		InterfaceEnergyHome.BackConsumoGiornaliero(val, null);
	}
}

//ritorna array di numeri, con possibili decimali, che indica consumo in kWh
//ogni valore corrisponde ad un'ora, partendo da 0
InterfaceEnergyHome.BackConsumoMedio = function (result, err)
{
	
	if (InterfaceEnergyHome.backConsumoMedio != null)
	{	
		if (err != null)
			InterfaceEnergyHome.GestErrorEH("BackConsumoMedio", err);
		retVal = null;
		if ((err == null) && (result != null))
		{
			/** tolto 29-11-2011 : faccio calcolo nel chiamante perche' prendo tanti valori 
			 * quanti sono quelli del consumo giornaliero
			// trascodifica dato : mi viene ritornato il consumo medio per ora nelle 24 ore
			// sommo il consumo per le ore fino all'ora attuale
			attuale = GestDate.GetActualDate();
			oraAttuale = attuale.getHours();
			minAttuale = attuale.getMinutes();
			retVal = 0;
			for (i = 0; i < oraAttuale; i++)
			{
				if (result.list[i] != null)
					retVal += result.list[i];
				else
				{
					retVal = null;
					break;
				}
			}
			// aggiungo percentuale in base ai minuti dell'ora attuale
			if ((retVal != null) && (result.list[oraAttuale] != null))
				retVal += result.list[oraAttuale] * (minAttuale / 60);
			else
				retVal = null;
			**/
			retVal = result.list;
		}
		Log.alert(80, InterfaceEnergyHome.MODULE, "BackConsumoMedio val = "  + retVal);
		InterfaceEnergyHome.backConsumoMedio(retVal);    
	}
}

InterfaceEnergyHome.GetConsumoMedio = function (backFunc)
{
	weekDay = Main.dataAttuale.getDay() + 1; // js comincia da 0, java da 1
	InterfaceEnergyHome.backConsumoMedio = backFunc;
	/**/
	if (InterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getWeekDayAverage(InterfaceEnergyHome.BackConsumoMedio , InterfaceEnergyHome.PID_TOTALE, 
				InterfaceEnergyHome.CONSUMO, weekDay);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackConsumoMedio (null, err);
		}
	}
	else
	/**/
	{		
		// per test
		val = ConsumoMedio;
		Log.alert(80,  InterfaceEnergyHome.MODULE, "GetConsumoMedio : val = " + val);
		InterfaceEnergyHome.BackConsumoMedio(val, null);
	}
}

//ritorna numero, con possibili decimali, che indica consumo in kW, null se errore
InterfaceEnergyHome.BackPotenzaAttuale = function (result, err)
{
	if (InterfaceEnergyHome.backPotenzaAttuale != null)
	{
		if (err != null)
			InterfaceEnergyHome.GestErrorEH("BackPotenzaAttuale", err);

		if ((err == null) && (result != null))
			retVal = result.value;
		else
			retVal = null;
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackPotenzaAttuale result = "  + retVal);
		InterfaceEnergyHome.backPotenzaAttuale(retVal);    
	}
}

InterfaceEnergyHome.GetPotenzaAttuale = function (backFunc)
{
	Log.alert(80,  InterfaceEnergyHome.MODULE, "GetPotenzaAttuale");
	InterfaceEnergyHome.backPotenzaAttuale = backFunc;
	if (InterfaceEnergyHome.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.getAttribute(InterfaceEnergyHome.BackPotenzaAttuale, InterfaceEnergyHome.POTENZA_TOTALE);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackPotenzaAttuale(null, err);
		}
	}
	else
	{		
		// per test
		PotenzaAttuale.value += 200;
		if (PotenzaAttuale.value > (Define.home["tipoContatore"][Main.contatore]+2000))
			PotenzaAttuale.value = 0;
		InterfaceEnergyHome.BackPotenzaAttuale(PotenzaAttuale, null);
	}
}


//ritorna array di strutture
InterfaceEnergyHome.BackListaElettr = function (result, err)
{
	var smartInfo = null;
	
	if (InterfaceEnergyHome.backListaElettr != null)
	{	
		if (err != null)
		{
			retVal = null;
			InterfaceEnergyHome.GestErrorEH("BackListaElettr", err);
		}
		else
		{
		// trascodifica dato : ritorno array di strutture
		// maschera possibili cambiamenti di dati
		// ritorna solo dispositivi con device_state_avail a true
		// e lo smart info, che metto al fondo, perche' in alcuni casi puo' servire
			if (result!= null)
			{
				retVal = new Array();
				j = 0;
				for (i = 0; i < result.list.length; i++)
				{
					res = result.list[i];
				// escludo elementi con device_state_avail false
				// se non c'e' suppongo sia true
				// metto al fondo lo smart info, se c'e'
					if (res.map[InterfaceEnergyHome.ATTR_APP_STATE_AVAIL] == undefined)
						res.map[InterfaceEnergyHome.ATTR_APP_STATE_AVAIL] = false;
					// Nicola if ((res.map[InterfaceEnergyHome.ATTR_APP_STATE_AVAIL] == true) ||
					//		(res.map[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.SMARTINFO_APP_TYPE))
					//{
						elem = new DatiElettr();
						elem.id = res.map[InterfaceEnergyHome.ATTR_APP_PID]; 
						elem.nome = res.map[InterfaceEnergyHome.ATTR_APP_NAME];
						elem.categoria = res.map[InterfaceEnergyHome.ATTR_APP_CATEGORY];
						elem.locazione = res.map[InterfaceEnergyHome.ATTR_APP_LOCATION] ;
						elem.avail = res.map[InterfaceEnergyHome.ATTR_APP_AVAIL];
						elem.stato = res.map[InterfaceEnergyHome.ATTR_APP_STATE];
						elem.icona = res.map[InterfaceEnergyHome.ATTR_APP_ICON]; 
						elem.tipo = res.map[InterfaceEnergyHome.ATTR_APP_TYPE]; 
						if (res.map[InterfaceEnergyHome.ATTR_APP_VALUE] != undefined)
							elem.value = res.map[InterfaceEnergyHome.ATTR_APP_VALUE].value.value;
						if (elem.locazione == undefined)	
							elem.locazione = null;		 
						if (elem.categoria == undefined)
							elem.categoria = null;
						if (elem.avail == undefined)
							elem.avail = null;
						if (elem.stato == undefined)
							elem.stato = null;
						if (elem.icona == undefined)
							elem.icona = null;
						if (elem.tipo == InterfaceEnergyHome.SMARTINFO_APP_TYPE)
						{
							smartInfo = elem;
							// salvo pid totale
							InterfaceEnergyHome.PID_TOTALE = elem.id;
						}
						else
						{
							retVal[j] = elem;
							j++;
						}
					
				}
				// aggiungo al fondo lo smartInfo
				if (smartInfo != null)
					retVal[j] = smartInfo;
			}	
			else
				retVal = null;
		}
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackListaElettr val = "  + retVal);
		InterfaceEnergyHome.backListaElettr(retVal);    
	}
}

InterfaceEnergyHome.GetListaElettr = function (backFunc)
{
	InterfaceEnergyHome.backListaElettr = backFunc;
	Log.alert(80, InterfaceEnergyHome.MODULE, "GetListaElettr ");
	
	if (InterfaceEnergyHome.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.getAppliancesConfigurations(InterfaceEnergyHome.BackListaElettr);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackListaElettr(null, err);
		}
	}
	else
	{	
		// per test
		if (indLista == 0)
		{
			val = ListaElettr;
			indLista = 1;
		}	
		else
		{
			val = ListaElettr1;
			indLista = 0;
		}	
		InterfaceEnergyHome.BackListaElettr(val, null);
	}
}

//ritorna array di coppie di valori ["nome", "pid"]
InterfaceEnergyHome.BackElettrStorico = function (result, err)
{
	if (InterfaceEnergyHome.backElettrStorico != null)
	{	
		if (err != null)
		{
			retVal = null;
			InterfaceEnergyHome.GestErrorEH("BackElettrStorico", err);
		}
		else
		{
		// trascodifica dato : ritorno coppie ["nome", "pid"]
			retVal = new Array();
			if (result != null)
			{
				for (i = 0; i < result.list.length; i++)
				{
					retVal[i] = new Object();
					retVal[i].nome = result.list[i].map[InterfaceEnergyHome.ATTR_APP_NAME];
					retVal[i].pid = result.list[i].map[InterfaceEnergyHome.ATTR_APP_PID];
					retVal[i].tipo = result.list[i].map[InterfaceEnergyHome.ATTR_APP_TYPE];
				}
			}
		}
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackElettrStorico val = "  + retVal);
		InterfaceEnergyHome.backElettrStorico(retVal);    
	}	
}

InterfaceEnergyHome.GetElettrStorico = function (backFunc)
{
	InterfaceEnergyHome.backElettrStorico = backFunc;
	Log.alert(80, InterfaceEnergyHome.MODULE, "GetElettrStorico");
	
	/**/
	if (InterfaceEnergyHome.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.getAppliancesConfigurations(InterfaceEnergyHome.BackElettrStorico);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackElettrStorico(null, err);
		}
	}
	else
	/**/
	{	
		// per test
		val = ListaElettr;
		InterfaceEnergyHome.BackElettrStorico(val, null);
	}
}


//ritorna array di valori (costo o consumo)
InterfaceEnergyHome.BackStorico = function (result, err)
{
	
	if (InterfaceEnergyHome.backStorico != null)
	{	
		if (err != null)
			InterfaceEnergyHome.GestErrorEH("BackStorico", err);
		retVal = null;
		if ((err == null) && (result != null))
			retVal = result.list;
		Log.alert(20,  InterfaceEnergyHome.MODULE, "BackStorico retVal = "  + retVal);
		InterfaceEnergyHome.backStorico(retVal);    
	}
}

InterfaceEnergyHome.GetStorico = function (tipo, pid, dataInizio, dataFine, intervallo, backFunc)
{
	var paramTr, param1, param2;
	
	InterfaceEnergyHome.backStorico = backFunc;
	Log.alert(80, InterfaceEnergyHome.MODULE, "GetStorico");
	if (tipo == "Costo")
		param1 = InterfaceEnergyHome.COSTO;
	else
		param1 = InterfaceEnergyHome.CONSUMO;

	if (intervallo == 0)
	{
		param2 = InterfaceEnergyHome.HOUR;
		//dataInizio.setHours(0);
		//dataFine.setHours(23);
		//dataFine.setMinutes(30);
		paramTr = Tracing.QT_IERI;
	}
	else
		if (intervallo == 3)
		{
			param2 = InterfaceEnergyHome.MONTH;
			paramTr = Tracing.QT_ANNO;
		}
		else
		{
			param2 = InterfaceEnergyHome.DAY;
			if (intervallo == 1)
				paramTr = Tracing.QT_SETT;
			else
				paramTr = Tracing.QT_MESE;
		}
	Tracing.Trace(Tracing.HISTORY, Tracing.QUERY, paramTr, pid);
	/**/
	if (InterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getAttributeData(InterfaceEnergyHome.BackStorico, pid, param1, 
					dataInizio.getTime(), dataFine.getTime(), param2, true, InterfaceEnergyHome.DELTA);

		}
		catch (err)
		{
			InterfaceEnergyHome.BackStorico(null, err);
		}
	}
	else
	/**/
	{		
		// per test
		i = new Date(dataInizio.getTime(0));
		i.setHours(0);
		i.setMinutes(0);
		i.setSeconds(0);
		i.setMilliseconds(0);
		f = new Date(dataFine.getTime(0));
		f.setHours(0);
		f.setMinutes(0);
		f.setSeconds(0);
		f.setMilliseconds(0);
		diff = f - i;
		g = Math.floor(diff / (24*60*60*1000)) + 1;
		
		Log.alert(20, InterfaceEnergyHome.MODULE, "   diff = " + diff + "  giorni = " + g);
		if (intervallo == 0)
		{
			if (tipo == "Costo")
				val = StoricoCostoI;
			else
				val = StoricoConsumoI;
		}
		else
		if (intervallo == 1)
		{
			if (tipo == "Costo")
				val = {"list":StoricoCostoS.list.slice(0,g)};
			else
				val = {"list":StoricoConsumoS.list.slice(0,g)};
		}
		else
		if (intervallo == 2)
		{
			if (tipo == "Costo")
				val = {"list":StoricoCostoM.list.slice(0,g)};
			else
				val = {"list":StoricoConsumoM.list.slice(0,g)};
		}
		else
		{
			i = dataInizio.getMonth();
			f = dataFine.getMonth();
			g = f - i + 1;
			if (tipo == "Costo")
				val = {"list":StoricoCostoA.list.slice(0,g)};
			else
				val = {"list":StoricoConsumoA.list.slice(0,g)};
		}
		/** da rivedere !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		if (pid !="homeauto")
		{
			// se e' un singolo elettrodomestico prendo una percentuale
			// cerco percentuale
			perc = 100;
			for (j = 0; j < StoricoElettr.length; j++)
			{
				if (StoricoElettr[j].id == pid)
				{
					perc = StoricoElettr[j].perc;
					break;
				}	
			}
			valP = {"list":[]};
			for (j = 0; j < val.list.length; j++)
				valP.list[j] = val.list[j] * perc / 100;
			InterfaceEnergyHome.BackStorico(valP, null);
		}
		else
		**/
			InterfaceEnergyHome.BackStorico(val, null);
	}
}

// solo per catch eccezione
InterfaceEnergyHome.BackSendGuiLog = function(result, err)
{
	Log.alert(80, InterfaceEnergyHome.MODULE, "SendGuiLog:  " + err);
}

// solo se non in demo o simulazione
InterfaceEnergyHome.SendGuiLog = function(logText)
{
	if (InterfaceEnergyHome.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.sendGuiLog(InterfaceEnergyHome.BackSendGuiLog, logText);
		}
		catch (err)
		{
			Log.alert(80, InterfaceEnergyHome.MODULE, "SendGuiLog: " + logText);
		}
	}
}

//legge la data da AG

InterfaceEnergyHome.BackActualDate = function (result, err)
{
	
	if (InterfaceEnergyHome.backActualDate != null)
	{	
		if (err != null)
			InterfaceEnergyHome.GestErrorEH("BackActualDate", err);
		retVal = null;
		if ((err == null) && (result != null))
			retVal = result;
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackActualDate retVal = "  + retVal);
		InterfaceEnergyHome.backActualDate(retVal);    
	}
}

InterfaceEnergyHome.GetActualDate = function(backFunc)
{
	InterfaceEnergyHome.backActualDate = backFunc;
	if (InterfaceEnergyHome.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.currentTimeMillis(InterfaceEnergyHome.BackActualDate);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackActualDate(null, err);
		}
	}
	else 
		// per simulazione ritorno ora di sistema
		InterfaceEnergyHome.backActualDate(new Date().getTime());  
}

InterfaceEnergyHome.BackPowerLimit = function (result, err)
{
	
	if (InterfaceEnergyHome.backPowerLimit != null)
	{	
		
		if (err != null)
		//	InterfaceEnergyHome.GestErrorEH("BackPowerLimit", err);
			Log.alert(80,  InterfaceEnergyHome.MODULE, "Eccezione in GetPowerLimit: " + err.msg);
		if (result != null)
			retVal = result.value;
		else
			retVal = null;
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackPowerLimit retVal = "  + retVal);
		InterfaceEnergyHome.backPowerLimit(retVal);    
	}
}

InterfaceEnergyHome.GetPowerLimit = function(backFunc)
{
	InterfaceEnergyHome.backPowerLimit = backFunc;
	start = Main.dataAttuale.getTime();
	if (InterfaceEnergyHome.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.getAttribute(InterfaceEnergyHome.BackPowerLimit, InterfaceEnergyHome.LIMITI);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackPowerLimit(null, err);
		}
	}
	else
		InterfaceEnergyHome.BackPowerLimit(null, null);
}

InterfaceEnergyHome.BackInitialTime = function (result, err)
{
	
	if (InterfaceEnergyHome.backInitialTime != null)
	{	
		if (err != null)
			InterfaceEnergyHome.GestErrorEH("BackInitialTime", err);
		retVal = result;
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackInitialTime installDate = "  + new Date(retVal).toString());
		InterfaceEnergyHome.backInitialTime(retVal);    
	}
}

// legge data in cui e' stata fatta la configurazione iniziale
InterfaceEnergyHome.GetInitialTime = function(backFunc)
{
	InterfaceEnergyHome.backInitialTime = backFunc;
	if (InterfaceEnergyHome.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.getInitialConfigurationTime(InterfaceEnergyHome.BackInitialTime);
		}
		catch (err)
		{
			InterfaceEnergyHome.BackInitialTime(null, err);
		}
	}
	else
		InterfaceEnergyHome.BackInitialTime(new Date("September 15, 2011 23:59:00").getTime(), null);
}