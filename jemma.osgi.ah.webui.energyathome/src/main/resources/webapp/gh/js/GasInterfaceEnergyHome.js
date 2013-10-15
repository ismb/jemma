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

var GasInterfaceEnergyHome = {
	jsonrpc : null,
	MODULE : "GasInterfaceEnergyHome",
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
};

function bindService(name) {
	
	// crea client
	if (GasInterfaceEnergyHome.jsonrpc == null) 
	{
		try
		{
			GasInterfaceEnergyHome.jsonrpc = new JSONRpcClient(OSGi.getServletURI());
			GasInterfaceEnergyHome.jsonrpc.http_max_spare = 4;
			JSONRpcClient.toplevel_ex_handler = function (e) {
				 Log.alert(20, "JSONRpcClient", "Eccezione in JSONRpcClient: " + e.message);
			};
		}
		catch(err)
		{
			GasInterfaceEnergyHome.GestErrorEH(null, err);
			return null;
		}
	}
	// ricerca servizio
	sReg = new Array();
    try {  	
    	sReg = GasInterfaceEnergyHome.jsonrpc.OSGi.find(GasInterfaceEnergyHome.serviceName);
    }
    catch (err) {
    	GasInterfaceEnergyHome.GestErrorEH(null, err);
		return null;
    }
    
    // fa bind al servizio
    if (sReg.list && sReg.list.length > 0)
    {
    	try {
    		GasInterfaceEnergyHome.jsonrpc.OSGi.bind(sReg.list[0]);
    		GasInterfaceEnergyHome.objService = sReg.list[0].map['service.id'];
    		return GasInterfaceEnergyHome.jsonrpc[GasInterfaceEnergyHome.objService];
    	}
    	catch(err) {
    		GasInterfaceEnergyHome.GestErrorEH(null, err);
    	}
		return null;
    }
   	
    return null;
} 

GasInterfaceEnergyHome.Init = function() 
{ 
	GasInterfaceEnergyHome.errMessage = null;
	GasInterfaceEnergyHome.errCode = 0;
	if (GasInterfaceEnergyHome.mode > 0)
		return GasInterfaceEnergyHome.objService = bindService(GasInterfaceEnergyHome.serviceName);
	else
		return 1;
};



// abortisce tutte le eventuali chiamate in corso
// per adesso annulla tutte le callback in modo che quando 
// la funzione ritorna non richiama la callback
// bisogna provare se si riescono ad abortire le chiamate
GasInterfaceEnergyHome.Abort = function ()
{
	GasInterfaceEnergyHome.backGetLocations = null;
	GasInterfaceEnergyHome.backCostoPrevisto = null;
	GasInterfaceEnergyHome.backCostoOdierno = null;
	GasInterfaceEnergyHome.backCostoMedio = null;
	GasInterfaceEnergyHome.backCostoGiornaliero = null;
	GasInterfaceEnergyHome.backSuddivisioneCosti = null;
	GasInterfaceEnergyHome.backConsumoPrevisto = null;
	GasInterfaceEnergyHome.backConsumoOdierno = null;
	GasInterfaceEnergyHome.backConsumoMedio = null;
	GasInterfaceEnergyHome.backConsumoGiornaliero = null;
	GasInterfaceEnergyHome.backPotenzaAttuale = null;
	GasInterfaceEnergyHome.backMaxElettr = null;
	GasInterfaceEnergyHome.backElettrStorico = null;
	GasInterfaceEnergyHome.backStorico = null;
	GasInterfaceEnergyHome.backListaElettr = null;
	GasInterfaceEnergyHome.backActualDate = null;
	GasInterfaceEnergyHome.backPowerLimit = null;
	GasInterfaceEnergyHome.backInitialTime = null;
};

/**
 * JSONRpcClient.Exception.CODE_REMOTE_EXCEPTION = 490;
JSONRpcClient.Exception.CODE_ERR_CLIENT = 550;
JSONRpcClient.Exception.CODE_ERR_PARSE = 590;
JSONRpcClient.Exception.CODE_ERR_NOMETHOD = 591;
JSONRpcClient.Exception.CODE_ERR_UNMARSHALL = 592;
JSONRpcClient.Exception.CODE_ERR_MARSHALL = 593;
 */
GasInterfaceEnergyHome.GestErrorEH = function(func, err)
{
	var msg;
	GasInterfaceEnergyHome.visError = GasInterfaceEnergyHome.ERR_GENERIC;
	
	// mi puo' arrivare msg o message a seconda da dove arriva l'errore
	if (err.msg == undefined)
		msg = err.message;
	else
		msg = err.msg;
	if ((err.code == undefined) && (GasInterfaceEnergyHome.objService == null))
	{
		tmpMsg = "Service not found";
		GasInterfaceEnergyHome.visError = GasInterfaceEnergyHome.ERR_CONN_AG;
	}
	else
		if (err.code == JSONRpcClient.Exception.CODE_REMOTE_EXCEPTION) 
		{
			tmpMsg = msg + " " + err.name;
			if (err.name == GasInterfaceEnergyHome.SERVER_EXCEPTION)
				GasInterfaceEnergyHome.visError = GasInterfaceEnergyHome.ERR_CONN_SERVER;
			else
				GasInterfaceEnergyHome.visError = GasInterfaceEnergyHome.ERR_CONN_AG;
		}
		else
			if (((err.code == 0) && (msg == "")) || (err.code == JSONRpcClient.Exception.CODE_ERR_CLIENT))
			{
				tmpMsg = "Error in gateway connection";
				GasInterfaceEnergyHome.visError = GasInterfaceEnergyHome.ERR_CONN_AG;
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
				GasInterfaceEnergyHome.visError = GasInterfaceEnergyHome.ERR_CONN_AG;
			}
	Log.alert(20, GasInterfaceEnergyHome.MODULE, func + " code = " + err.code + " errMsg = " + tmpMsg);
	// nel caso di errore in inizializzazione della connessione AG non posso fare trace perche'
	// non riesco a contattare l'AG
	if (func != null)
		GasTracing.Trace(null, GasTracing.ERR, GasTracing.ERR_GENERIC, tmpMsg);
	// visualizzo l'errore 
	GasMain.VisError(GasInterfaceEnergyHome.visError);
};


GasInterfaceEnergyHome.BackGetLocazioni = function (result, err)
{
	if (GasInterfaceEnergyHome.backGetLocazioni != null)
	{	
		if (err != null)
		{
			retVal = null;
			// in realta' puo' essere richiamato anche da configuratore
			GasInterfaceEnergyHome.GestErrorEH("BackGetLocazioni", err);
		}
		else
		{
		// ritorna dizionario pid-nome
			retVal = new Array();
			if (result != null)
			{
				for (i = 0; i < result.length; i++)
				{
					pid = result[i][GasInterfaceEnergyHome.ATTR_LOCATION_PID];
					name = result[i][GasInterfaceEnergyHome.ATTR_LOCATION_NAME];
					retVal[pid] = name;
				}
			}
		}
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackGetLocazioni  val = "  + retVal);
		GasInterfaceEnergyHome.backGetLocazioni(retVal);    
	}
};

GasInterfaceEnergyHome.GetLocazioni = function (backFunc)
{
	GasInterfaceEnergyHome.backGetLocazioni = backFunc;
	Log.alert(80, GasInterfaceEnergyHome.MODULE, "GetLocazioni");
		if (GasInterfaceEnergyHome.mode > 0)
	{
		try
		{
			GasInterfaceEnergyHome.objService.getLocations(GasInterfaceEnergyHome.BackGetLocazioni);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackGetLocazioni(null, err);
		}
	}
	else
	{	
		// per test
		val = ListaLocazioni;
		GasInterfaceEnergyHome.BackGetLocazioni(val, null);
	}
};


//ritorna numero, con possibili decimali, che indica costo in euro, null se errore
GasInterfaceEnergyHome.BackSuggerimento = function (result, err)
{
	if (GasInterfaceEnergyHome.backSuggerimento != null)
	{
		if (err != null)
		{
			retVal = null;
			GasInterfaceEnergyHome.GestErrorEH("BackSuggerimento", err);
		}
		else
		{
	// eventuale trascodifica dato 
			retVal = result;
		}
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackSuggerimento val = "  + retVal);
	
		GasInterfaceEnergyHome.backSuggerimento(retVal);    
	}
};

GasInterfaceEnergyHome.GetSuggerimento = function (backFunc)
{
	GasInterfaceEnergyHome.backSuggerimento = backFunc;
	/**
	if (GasInterfaceEnergyHome.mode > 0)
	{
		try
		{
			GasInterfaceEnergyHome.objService.getAttributeData(GasInterfaceEnergyHome.BackSuggerimento, "Suggerimento");
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackSuggerimento (null, err);
		}
	}
	else 
	**/
	{	
		// per test
		if (GasDefine.lang == "it")
			sugg = SuggerimentiIt;
		else
			sugg = SuggerimentiEn;
		ind = Math.round(Math.random() * (sugg.length));
		val = sugg[ind];
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "GetSuggerimento ind = " + ind + " val = " + val);
		GasInterfaceEnergyHome.BackSuggerimento(val, null);
	}
};


//ritorna numero, con possibili decimali, che indica costo in euro, null se errore
GasInterfaceEnergyHome.BackCostoOdierno = function (result, err)
{
	
	if (GasInterfaceEnergyHome.backCostoOdierno != null)
	{
		if (err != null)
			GasInterfaceEnergyHome.GestErrorEH("BackCostoOdierno", err);
		retVal = null;
		if ((err == null) && (result != null))
		{
			if (result.list.length > 0)
				// per come ho impostato la chiamata ho solo un valore
				retVal = result.list[0];
		}
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackCostoOdierno retVal = "  + retVal);
		GasInterfaceEnergyHome.backCostoOdierno(retVal);    
	}
};

GasInterfaceEnergyHome.GetCostoOdierno = function (backFunc)
{
	start = GasMain.dataAttuale.getTime();
	GasInterfaceEnergyHome.backCostoOdierno = backFunc;
	/**/
	if (GasInterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			GasInterfaceEnergyHome.objService.getAttributeData(GasInterfaceEnergyHome.BackCostoOdierno, 
					GasInterfaceEnergyHome.PID_TOTALE, GasInterfaceEnergyHome.COSTO, 
					start, start, GasInterfaceEnergyHome.DAY, true, GasInterfaceEnergyHome.DELTA);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackCostoOdierno(null, err);
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
		attuale = GasGestDate.GetActualDate();
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
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "GetCostoOdierno val = " + val + " ind = " + ind);
		GasInterfaceEnergyHome.BackCostoOdierno(val, null);
	}
};

//ritorna numero, con possibili decimali, che indica costo in euro, null se errore
GasInterfaceEnergyHome.BackCostoPrevisto = function (result, err)
{
	
	if (GasInterfaceEnergyHome.backCostoPrevisto!= null)
	{
		if (err != null)
			GasInterfaceEnergyHome.GestErrorEH("BackCostoPrevisto", err);
		if ((err == null) && (result != null))
			retVal = result; // non c'e' il campo value ?!?
		else
			retVal = null;
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackCostoPrevisto retVal = "  + retVal);
		GasInterfaceEnergyHome.backCostoPrevisto(retVal);    
	}
};

GasInterfaceEnergyHome.GetCostoPrevisto = function (backFunc)
{
	GasInterfaceEnergyHome.backCostoPrevisto = backFunc;
	/**/
	if (GasInterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			GasInterfaceEnergyHome.objService.getForecast(GasInterfaceEnergyHome.BackCostoPrevisto, 
				GasInterfaceEnergyHome.PID_TOTALE, GasInterfaceEnergyHome.COSTO, GasMain.dataAttuale.getTime(), GasInterfaceEnergyHome.MONTH);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackCostoPrevisto(null, err);
		}
	}
	else
	/**/
	{		
		// per test
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "GetCostoPrevisto: ind = " + ind);
		val =  CostoPrevisto;
		GasInterfaceEnergyHome.BackCostoPrevisto(val, null);
	}
};

//ritorna array di numeri, con possibili decimali, che indica il costo in euro, null se errore
//ogni valore corrisponde ad un'ora, partendo da 0
GasInterfaceEnergyHome.BackCostoGiornaliero = function (result, err)
{
	
	if (GasInterfaceEnergyHome.backCostoGiornaliero!= null)
	{	
		if (err != null)
			GasInterfaceEnergyHome.GestErrorEH("BackCostoGiornaliero", err);
		retVal = null;
		if ((err == null) && (result != null))
			retVal = result.list;
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackCostoGiornaliero retVal = "  + retVal);
		GasInterfaceEnergyHome.backCostoGiornaliero(retVal);    
	}
};

GasInterfaceEnergyHome.GetCostoGiornaliero = function (backFunc)
{
	GasInterfaceEnergyHome.backCostoGiornaliero = backFunc;
	start = new Date(GasMain.dataAttuale.getTime());
	start.setHours(0);
	end = GasMain.dataAttuale.getTime();
	/**/
	if (GasInterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			GasInterfaceEnergyHome.objService.getAttributeData(GasInterfaceEnergyHome.BackCostoGiornaliero, 
				GasInterfaceEnergyHome.PID_TOTALE,	GasInterfaceEnergyHome.COSTO, start.getTime(), end, 
				GasInterfaceEnergyHome.HOUR, true, GasInterfaceEnergyHome.DELTA);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackCostoGiornaliero(null, err);
		}
	}
	else
	/**/
	{		
		// per test, copio per il numero ore attuale
		hours = GasMain.dataAttuale.getHours();
		val = CostoGiornaliero;
		val.list = val.list.slice(0, hours);
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "GetCostoGiornaliero");
		GasInterfaceEnergyHome.BackCostoGiornaliero(val, null);
	}
};


//ritorna numero, con possibili decimali, che indica costo in euro, null se errore
GasInterfaceEnergyHome.BackCostoMedio = function (result, err)
{
	
	if (GasInterfaceEnergyHome.backCostoMedio!= null)
	{
		if (err != null)
			GasInterfaceEnergyHome.GestErrorEH("BackCostoMedio", err);
		if ((err != null) || (result == null))
			retVal = null;
		else
		{
			/** tolto 29-11-2011 : faccio calcolo nel chiamante perche' prendo tanti valori 
			 * quanti sono quelli del consumo giornaliero
			// trascodifica dato : mi viene ritornato il costo medio per ora nelle 24 ore
			// sommo il costo per le ore fino all'ora attuale
			attuale = GasGestDate.GetActualDate();
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
		Log.alert(80, GasInterfaceEnergyHome.MODULE, "BackCostoMedio val = "  + retVal);
		GasInterfaceEnergyHome.backCostoMedio(retVal);    
	}
};

GasInterfaceEnergyHome.GetCostoMedio = function (backFunc)
{
	weekDay = GasMain.dataAttuale.getDay() + 1; // js comincia da 0, java da 1
	GasInterfaceEnergyHome.backCostoMedio = backFunc;
	/**/
	if (GasInterfaceEnergyHome.mode > 1)// solo se anche piattaforma
	{
		try
		{
			GasInterfaceEnergyHome.objService.getWeekDayAverage(GasInterfaceEnergyHome.BackCostoMedio , 
					GasInterfaceEnergyHome.PID_TOTALE,	GasInterfaceEnergyHome.COSTO, weekDay);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackCostoMedio (null, err);
		}
	}
	else
	/**/
	{	
		// per test
		val = CostoMedio;  
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "GetCostoMedio : ind = " + ind + " val = " + val);
		GasInterfaceEnergyHome.BackCostoMedio(val, null);
	}
};


//ritorna array del tipo: [['Lavatrice', 25], ['Frigo', 9], ['PC', 5], ['Altro', 51] ]
//dove il numero e' la percentuale rispetto al totale 
GasInterfaceEnergyHome.BackSuddivisioneCosti = function (result, err)
{
	if (GasInterfaceEnergyHome.backSuddivisioneCosti != null)
	{	
		retVal = null;
		if (err != null)
			GasInterfaceEnergyHome.GestErrorEH("BackSuddivisioneCosti", err);
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
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackSuddivisioneCosti val = "  + retVal);
		GasInterfaceEnergyHome.backSuddivisioneCosti(retVal);    
	}
};

GasInterfaceEnergyHome.GetSuddivisioneCosti = function (backFunc)
{
	start = new Date(GasMain.dataAttuale.getTime());
	//start.setDate(0);
	end = new Date(GasMain.dataAttuale.getTime());
	GasInterfaceEnergyHome.backSuddivisioneCosti = backFunc;
	/**/
	if (GasInterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			GasInterfaceEnergyHome.objService.getAttributeData(GasInterfaceEnergyHome.BackSuddivisioneCosti, GasInterfaceEnergyHome.COSTO, 
				start.getTime(), end.getTime(), GasInterfaceEnergyHome.MONTH, true, GasInterfaceEnergyHome.DELTA);

		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackSuddivisioneCosti (null, err);
		}
	}
	else
	/**/
	{		
		// per test
		ind = Math.round(Math.random() * SuddivisioneCosti.length);
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "GetSuddivisioneCosti: ind = " + ind);
		val = SuddivisioneCosti;
		GasInterfaceEnergyHome.BackSuddivisioneCosti(val, null);
	}
};


//ritorna una struttura con pid, nome e valore dell'elettrodomestico che sta consumando di piu'
//leggo tutti gli elettrodomestici e vedo quale ha il valore (di consumo) maggiore
GasInterfaceEnergyHome.BackMaxElettr = function (result, err)
{
	if (GasInterfaceEnergyHome.backMaxElettr != null)
	{	
		if (err != null)
			GasInterfaceEnergyHome.GestErrorEH("BackMaxElettr", err);
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
				//if ((res.map[GasInterfaceEnergyHome.ATTR_APP_STATE_AVAIL] == undefined) || 
				//	(res.map[GasInterfaceEnergyHome.ATTR_APP_STATE_AVAIL] == true))
				if ((res.map[GasInterfaceEnergyHome.ATTR_APP_AVAIL] == 2) && 
					(res.map[GasInterfaceEnergyHome.ATTR_APP_TYPE] != GasInterfaceEnergyHome.SMARTINFO_APP_TYPE))
				{
					if (res.map[GasInterfaceEnergyHome.ATTR_APP_VALUE] == undefined)
						val = 0;
					else
						val = parseFloat(res.map[GasInterfaceEnergyHome.ATTR_APP_VALUE].value.value);
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
				retVal.nome = result.list[maxInd].map[GasInterfaceEnergyHome.ATTR_APP_NAME];;
				retVal.icona = result.list[maxInd].map[GasInterfaceEnergyHome.ATTR_APP_ICON];
				retVal.tipo = result.list[maxInd].map[GasInterfaceEnergyHome.ATTR_APP_TYPE];
				retVal.value = maxVal;
				Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackMaxElettr : nome = " + retVal.nome);
			}	
		}
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackMaxElettr retVal = "  + retVal);
		GasInterfaceEnergyHome.backMaxElettr(retVal);    
	}
};

GasInterfaceEnergyHome.GetMaxElettr = function (backFunc)
{
	GasInterfaceEnergyHome.backMaxElettr = backFunc;
	if (GasInterfaceEnergyHome.mode > 0)
	{
		try
		{
			GasInterfaceEnergyHome.objService.getAppliancesConfigurations(GasInterfaceEnergyHome.BackMaxElettr);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackMaxElettr(null, err);
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
		GasInterfaceEnergyHome.BackMaxElettr(val, null);
	}
};


//ritorna numero, con possibili decimali, che indica consumo in kWh (w ??), null se errore
GasInterfaceEnergyHome.BackConsumoOdierno = function (result, err)
{
	if (GasInterfaceEnergyHome.backConsumoOdierno != null)
	{
		if (err != null)
			GasInterfaceEnergyHome.GestErrorEH("BackConsumoOdierno", err);
		retVal = null;
		if ((err == null) && (result != null))
		{
			if (result.list.length > 0)
				retVal = result.list[0];
		}
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackConsumoOdierno retVal = "  + retVal);
		GasInterfaceEnergyHome.backConsumoOdierno(retVal);    
	}
};

GasInterfaceEnergyHome.GetConsumoOdierno = function (backFunc)
{
	start = GasMain.dataAttuale.getTime();
	GasInterfaceEnergyHome.backConsumoOdierno = backFunc;
	/**/
	if (GasInterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			var res = GasInterfaceEnergyHome.objService.getAttributeData(GasInterfaceEnergyHome.BackConsumoOdierno, 
				GasInterfaceEnergyHome.PID_TOTALE, GasInterfaceEnergyHome.CONSUMO, 
				start, start, GasInterfaceEnergyHome.DAY, true, GasInterfaceEnergyHome.DELTA);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackConsumoOdierno(null, err);
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
		attuale = GasGestDate.GetActualDate();
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
//		val = "10 Aprile 2012"; //TODO calcolarla
		GasInterfaceEnergyHome.BackConsumoOdierno(val, null);
	}
};

//ritorna numero, con possibili decimali, che indica consumo in kWh, null se errore
GasInterfaceEnergyHome.BackConsumoPrevisto = function (result, err)
{
	if (GasInterfaceEnergyHome.backConsumoPrevisto != null)
	{	
		if (err != null)
			GasInterfaceEnergyHome.GestErrorEH("BackConsumoPrevisto", err);
		retVal = null;
		if ((err == null) && (result != null))
				retVal = result;
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackConsumoPrevisto retVal = "  + retVal);
		GasInterfaceEnergyHome.backConsumoPrevisto(retVal);    
	}
};

GasInterfaceEnergyHome.GetConsumoPrevisto = function (backFunc)
{
	start = GasMain.dataAttuale.getTime();
	GasInterfaceEnergyHome.backConsumoPrevisto = backFunc;
	/**/
	if (GasInterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			GasInterfaceEnergyHome.objService.getForecast(GasInterfaceEnergyHome.BackConsumoPrevisto, 
				GasInterfaceEnergyHome.PID_TOTALE, GasInterfaceEnergyHome.CONSUMO, start, GasInterfaceEnergyHome.MONTH);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackConsumoPrevisto(null, err);
		}
	}
	else
	/**/
	{	
		// per test
		val =  ConsumoPrevisto;
		GasInterfaceEnergyHome.BackConsumoPrevisto(val, null);
	}
};

//ritorna array di numeri, con possibili decimali, che indica consumo in kWh, null se errore
//ogni valore corrisponde ad un'ora, partendo da 0
GasInterfaceEnergyHome.BackConsumoGiornaliero = function (result, err)
{
	
	if (GasInterfaceEnergyHome.backConsumoGiornaliero!= null)
	{	
		if (err != null)
			GasInterfaceEnergyHome.GestErrorEH("BackConsumoGiornaliero", err);
		retVal = null;
		if ((err == null) && (result != null))
			retVal = result.list;
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackConsumoGiornaliero retVal = "  + retVal);
		GasInterfaceEnergyHome.backConsumoGiornaliero(retVal);    
	}
};

GasInterfaceEnergyHome.GetConsumoGiornaliero = function (backFunc)
{
	GasInterfaceEnergyHome.backConsumoGiornaliero = backFunc;
	start = new Date(GasMain.dataAttuale.getTime());
	start.setHours(0);
	end = GasMain.dataAttuale.getTime();
	Log.alert(20,  GasInterfaceEnergyHome.MODULE, "GetConsumoGiornaliero: ini = " + start.toString() + 
					" fine = " + new Date(end).toString() + " attuale = " + GasMain.dataAttuale.toString());
	/**/
	if (GasInterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			GasInterfaceEnergyHome.objService.getAttributeData(GasInterfaceEnergyHome.BackConsumoGiornaliero , 
				GasInterfaceEnergyHome.PID_TOTALE,	GasInterfaceEnergyHome.CONSUMO, start.getTime(), end, 
				GasInterfaceEnergyHome.HOUR, true, GasInterfaceEnergyHome.DELTA);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackConsumoGiornaliero (null, err);
		}
	}
	else
	/**/
	{		
		// per test, copio per il numero ore attuale
//		hours = GasMain.dataAttuale.getHours(); non serve per il GAS
		val = ConsumoGiornaliero;
//		val.list = val.list.slice(0, hours);  non serve per il GAS
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "GetConsumoGiornaliero");
		GasInterfaceEnergyHome.BackConsumoGiornaliero(val, null);
	}
};

//ritorna array di numeri, con possibili decimali, che indica consumo in kWh
//ogni valore corrisponde ad un'ora, partendo da 0
GasInterfaceEnergyHome.BackConsumoMedio = function (result, err)
{
	
	if (GasInterfaceEnergyHome.backConsumoMedio != null)
	{	
		if (err != null)
			GasInterfaceEnergyHome.GestErrorEH("BackConsumoMedio", err);
		retVal = null;
		if ((err == null) && (result != null))
		{
			/** tolto 29-11-2011 : faccio calcolo nel chiamante perche' prendo tanti valori 
			 * quanti sono quelli del consumo giornaliero
			// trascodifica dato : mi viene ritornato il consumo medio per ora nelle 24 ore
			// sommo il consumo per le ore fino all'ora attuale
			attuale = GasGestDate.GetActualDate();
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
		Log.alert(80, GasInterfaceEnergyHome.MODULE, "BackConsumoMedio val = "  + retVal);
		GasInterfaceEnergyHome.backConsumoMedio(retVal);    
	}
};

GasInterfaceEnergyHome.GetConsumoMedio = function (backFunc)
{
	weekDay = GasMain.dataAttuale.getDay() + 1; // js comincia da 0, java da 1
	GasInterfaceEnergyHome.backConsumoMedio = backFunc;
	/**/
	if (GasInterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			GasInterfaceEnergyHome.objService.getWeekDayAverage(GasInterfaceEnergyHome.BackConsumoMedio , GasInterfaceEnergyHome.PID_TOTALE, 
				GasInterfaceEnergyHome.CONSUMO, weekDay);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackConsumoMedio (null, err);
		}
	}
	else
	/**/
	{		
		// per test
		val = ConsumoMedio;
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "GetConsumoMedio : val = " + val);
		GasInterfaceEnergyHome.BackConsumoMedio(val, null);
	}
};

//ritorna numero, con possibili decimali, che indica consumo in kW, null se errore
GasInterfaceEnergyHome.BackPotenzaAttuale = function (result, err)
{
	if (GasInterfaceEnergyHome.backPotenzaAttuale != null)
	{
		if (err != null)
			GasInterfaceEnergyHome.GestErrorEH("BackPotenzaAttuale", err);

		if ((err == null) && (result != null))
			retVal = result.value;
		else
			retVal = null;
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackPotenzaAttuale result = "  + retVal);
		GasInterfaceEnergyHome.backPotenzaAttuale(retVal);    
	}
};

GasInterfaceEnergyHome.GetPotenzaAttuale = function (backFunc)
{
	Log.alert(80,  GasInterfaceEnergyHome.MODULE, "GetPotenzaAttuale");
	GasInterfaceEnergyHome.backPotenzaAttuale = backFunc;
	if (GasInterfaceEnergyHome.mode > 0)
	{
		try
		{
			GasInterfaceEnergyHome.objService.getAttribute(GasInterfaceEnergyHome.BackPotenzaAttuale, GasInterfaceEnergyHome.POTENZA_TOTALE);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackPotenzaAttuale(null, err);
		}
	}
	else
	{		
		// per test
//		PotenzaAttuale.value += 200;
//		if (PotenzaAttuale.value > (GasDefine.home["tipoContatore"][GasMain.contatore]+2000))
//			PotenzaAttuale.value = 0;
		PotenzaAttuale.value = valPotenzaAttuale;
//		if (PotenzaAttuale.value > (GasDefine.home["tipoContatore"][GasMain.contatore]+2000))
//			PotenzaAttuale.value = 0;
		GasInterfaceEnergyHome.BackPotenzaAttuale(PotenzaAttuale, null);
	}
};


//ritorna array di strutture
GasInterfaceEnergyHome.BackListaElettr = function (result, err)
{
	var smartInfo = null;
	
	if (GasInterfaceEnergyHome.backListaElettr != null)
	{	
		if (err != null)
		{
			retVal = null;
			GasInterfaceEnergyHome.GestErrorEH("BackListaElettr", err);
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
					if (res.map[GasInterfaceEnergyHome.ATTR_APP_STATE_AVAIL] == undefined)
						res.map[GasInterfaceEnergyHome.ATTR_APP_STATE_AVAIL] = false;
					// Nicola if ((res.map[GasInterfaceEnergyHome.ATTR_APP_STATE_AVAIL] == true) ||
					//		(res.map[GasInterfaceEnergyHome.ATTR_APP_TYPE] == GasInterfaceEnergyHome.SMARTINFO_APP_TYPE))
					//{
						elem = new DatiElettr();
						elem.id = res.map[GasInterfaceEnergyHome.ATTR_APP_PID]; 
						elem.nome = res.map[GasInterfaceEnergyHome.ATTR_APP_NAME];
						elem.categoria = res.map[GasInterfaceEnergyHome.ATTR_APP_CATEGORY];
						elem.locazione = res.map[GasInterfaceEnergyHome.ATTR_APP_LOCATION] ;
						elem.avail = res.map[GasInterfaceEnergyHome.ATTR_APP_AVAIL];
						elem.stato = res.map[GasInterfaceEnergyHome.ATTR_APP_STATE];
						elem.icona = res.map[GasInterfaceEnergyHome.ATTR_APP_ICON]; 
						elem.tipo = res.map[GasInterfaceEnergyHome.ATTR_APP_TYPE]; 
						if (res.map[GasInterfaceEnergyHome.ATTR_APP_VALUE] != undefined)
							elem.value = res.map[GasInterfaceEnergyHome.ATTR_APP_VALUE].value.value;
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
						if (elem.tipo == GasInterfaceEnergyHome.SMARTINFO_APP_TYPE)
						{
							smartInfo = elem;
							// salvo pid totale
							GasInterfaceEnergyHome.PID_TOTALE = elem.id;
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
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackListaElettr val = "  + retVal);
		GasInterfaceEnergyHome.backListaElettr(retVal);    
	}
};

GasInterfaceEnergyHome.GetListaElettr = function (backFunc)
{
	GasInterfaceEnergyHome.backListaElettr = backFunc;
	Log.alert(80, GasInterfaceEnergyHome.MODULE, "GetListaElettr ");
	
	if (GasInterfaceEnergyHome.mode > 0)
	{
		try
		{
			GasInterfaceEnergyHome.objService.getAppliancesConfigurations(GasInterfaceEnergyHome.BackListaElettr);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackListaElettr(null, err);
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
		GasInterfaceEnergyHome.BackListaElettr(val, null);
	}
};

//ritorna array di coppie di valori ["nome", "pid"]
GasInterfaceEnergyHome.BackElettrStorico = function (result, err)
{
	if (GasInterfaceEnergyHome.backElettrStorico != null)
	{	
		if (err != null)
		{
			retVal = null;
			GasInterfaceEnergyHome.GestErrorEH("BackElettrStorico", err);
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
					retVal[i].nome = result.list[i].map[GasInterfaceEnergyHome.ATTR_APP_NAME];
					retVal[i].pid = result.list[i].map[GasInterfaceEnergyHome.ATTR_APP_PID];
					retVal[i].tipo = result.list[i].map[GasInterfaceEnergyHome.ATTR_APP_TYPE];
				}
			}
		}
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackElettrStorico val = "  + retVal);
		GasInterfaceEnergyHome.backElettrStorico(retVal);    
	}	
};

GasInterfaceEnergyHome.GetElettrStorico = function (backFunc)
{
	GasInterfaceEnergyHome.backElettrStorico = backFunc;
	Log.alert(80, GasInterfaceEnergyHome.MODULE, "GetElettrStorico");
	
	/**/
	if (GasInterfaceEnergyHome.mode > 0)
	{
		try
		{
			GasInterfaceEnergyHome.objService.getAppliancesConfigurations(GasInterfaceEnergyHome.BackElettrStorico);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackElettrStorico(null, err);
		}
	}
	else
	/**/
	{	
		// per test
		val = ListaElettr;
		GasInterfaceEnergyHome.BackElettrStorico(val, null);
	}
};


//ritorna array di valori (costo o consumo)
GasInterfaceEnergyHome.BackStorico = function (result, err)
{
	
	if (GasInterfaceEnergyHome.backStorico != null)
	{	
		if (err != null)
			GasInterfaceEnergyHome.GestErrorEH("BackStorico", err);
		retVal = null;
		if ((err == null) && (result != null))
			retVal = result.list;
		Log.alert(20,  GasInterfaceEnergyHome.MODULE, "BackStorico retVal = "  + retVal);
		GasInterfaceEnergyHome.backStorico(retVal);    
	}
};

GasInterfaceEnergyHome.GetStorico = function (tipo, pid, dataInizio, dataFine, intervallo, backFunc)
{
	var paramTr, param1, param2;
	
	GasInterfaceEnergyHome.backStorico = backFunc;
	Log.alert(80, GasInterfaceEnergyHome.MODULE, "GetStorico");
	if (tipo == "Costo")
		param1 = GasInterfaceEnergyHome.COSTO;
	else
		param1 = GasInterfaceEnergyHome.CONSUMO;

	if (intervallo == 0)
	{
		param2 = GasInterfaceEnergyHome.HOUR;
		//dataInizio.setHours(0);
		//dataFine.setHours(23);
		//dataFine.setMinutes(30);
		paramTr = GasTracing.QT_IERI;
	}
	else
		if (intervallo == 3)
		{
			param2 = GasInterfaceEnergyHome.MONTH;
			paramTr = GasTracing.QT_ANNO;
		}
		else
		{
			param2 = GasInterfaceEnergyHome.DAY;
			if (intervallo == 1)
				paramTr = GasTracing.QT_SETT;
			else
				paramTr = GasTracing.QT_MESE;
		}
	GasTracing.Trace(GasTracing.HISTORY, GasTracing.QUERY, paramTr, pid);
	/**/
	if (GasInterfaceEnergyHome.mode > 1) // solo se anche piattaforma
	{
		try
		{
			GasInterfaceEnergyHome.objService.getAttributeData(GasInterfaceEnergyHome.BackStorico, pid, param1, 
					dataInizio.getTime(), dataFine.getTime(), param2, true, GasInterfaceEnergyHome.DELTA);

		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackStorico(null, err);
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
		
		Log.alert(20, GasInterfaceEnergyHome.MODULE, "   diff = " + diff + "  giorni = " + g);
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
			GasInterfaceEnergyHome.BackStorico(valP, null);
		}
		else
		**/
			GasInterfaceEnergyHome.BackStorico(val, null);
	}
};

// solo per catch eccezione
GasInterfaceEnergyHome.BackSendGuiLog = function(result, err)
{
	Log.alert(80, GasInterfaceEnergyHome.MODULE, "SendGuiLog:  " + err);
};

// solo se non in demo o simulazione
GasInterfaceEnergyHome.SendGuiLog = function(logText)
{
	if (GasInterfaceEnergyHome.mode > 0)
	{
		try
		{
			GasInterfaceEnergyHome.objService.sendGuiLog(GasInterfaceEnergyHome.BackSendGuiLog, logText);
		}
		catch (err)
		{
			Log.alert(80, GasInterfaceEnergyHome.MODULE, "SendGuiLog: " + logText);
		}
	}
};

//legge la data da AG

GasInterfaceEnergyHome.BackActualDate = function (result, err)
{
	
	if (GasInterfaceEnergyHome.backActualDate != null)
	{	
		if (err != null)
			GasInterfaceEnergyHome.GestErrorEH("BackActualDate", err);
		retVal = null;
		if ((err == null) && (result != null))
			retVal = result;
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackActualDate retVal = "  + retVal);
		GasInterfaceEnergyHome.backActualDate(retVal);    
	}
};

GasInterfaceEnergyHome.GetActualDate = function(backFunc)
{
	GasInterfaceEnergyHome.backActualDate = backFunc;
	if (GasInterfaceEnergyHome.mode > 0)
	{
		try
		{
			GasInterfaceEnergyHome.objService.currentTimeMillis(GasInterfaceEnergyHome.BackActualDate);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackActualDate(null, err);
		}
	}
	else 
		// per simulazione ritorno ora di sistema
		GasInterfaceEnergyHome.backActualDate(new Date().getTime());  
};

GasInterfaceEnergyHome.BackPowerLimit = function (result, err)
{
	
	if (GasInterfaceEnergyHome.backPowerLimit != null)
	{	
		
		if (err != null)
		//	GasInterfaceEnergyHome.GestErrorEH("BackPowerLimit", err);
			Log.alert(80,  GasInterfaceEnergyHome.MODULE, "Eccezione in GetPowerLimit: " + err.msg);
		if (result != null)
			retVal = result.value;
		else
			retVal = null;
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackPowerLimit retVal = "  + retVal);
		GasInterfaceEnergyHome.backPowerLimit(retVal);    
	}
};

GasInterfaceEnergyHome.GetPowerLimit = function(backFunc)
{
	GasInterfaceEnergyHome.backPowerLimit = backFunc;
	start = GasMain.dataAttuale.getTime();
	if (GasInterfaceEnergyHome.mode > 0)
	{
		try
		{
			GasInterfaceEnergyHome.objService.getAttribute(GasInterfaceEnergyHome.BackPowerLimit, GasInterfaceEnergyHome.LIMITI);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackPowerLimit(null, err);
		}
	}
	else
		GasInterfaceEnergyHome.BackPowerLimit(null, null);
};

GasInterfaceEnergyHome.BackInitialTime = function (result, err)
{
	
	if (GasInterfaceEnergyHome.backInitialTime != null)
	{	
		if (err != null)
			GasInterfaceEnergyHome.GestErrorEH("BackInitialTime", err);
		retVal = result;
		Log.alert(80,  GasInterfaceEnergyHome.MODULE, "BackInitialTime installDate = "  + new Date(retVal).toString());
		GasInterfaceEnergyHome.backInitialTime(retVal);    
	}
};

// legge data in cui e' stata fatta la configurazione iniziale
GasInterfaceEnergyHome.GetInitialTime = function(backFunc)
{
	GasInterfaceEnergyHome.backInitialTime = backFunc;
	if (GasInterfaceEnergyHome.mode > 0)
	{
		try
		{
			GasInterfaceEnergyHome.objService.getInitialConfigurationTime(GasInterfaceEnergyHome.BackInitialTime);
		}
		catch (err)
		{
			GasInterfaceEnergyHome.BackInitialTime(null, err);
		}
	}
	else
		GasInterfaceEnergyHome.BackInitialTime(new Date("September 15, 2011 23:59:00").getTime(), null);
};