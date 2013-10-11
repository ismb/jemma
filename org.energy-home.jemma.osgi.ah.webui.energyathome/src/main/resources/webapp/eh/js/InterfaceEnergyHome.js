/*************************************/
// variabili per test

var CostoOdierno = [0, 1, 1.4, 1.91, 2, 2.08, 3, 3.2, 4, 4.1, 4.9 ];
var CostoMedio = {"list":[0.1, 0.2, 0.1, 0.1, 0.2, 0.2, 0.5, 0.9, 1.1, 1.8, 0.2, 0.1, 1.0, 1.3, 1.5, 0.4, 0.6, 1.6, 2.0, 0.4, 0.2, 0.1, 0.1, 0.1]};
var CostoPrevisto = [0, 10, 14, 28, 32, 32.5, 41.2, 43.2, 54, 54.1, 24.9 ];
// formato dati da verificare
var SuddivisioneCosti = {"map": {"1": {"list":[12.571]},
	"2": {"list":[20.571]},
	"4": {"list":[21.571]},
	"0": {"list":[164.571]},
	"5": {"list":[2.571]},
	"3": {"list":[10.571]},
	"6": {"list":[14.571]}}};
//var SuddivisioneCosti = [['Lavatrice', 25], ['Frigo', 9], ['PC', 5], ['Altro', 51] ];
var PotenzaAttuale = {"value":0};
var ConsumoOdierno = [0, 200, 240, 2900, 12000, 6058, 4012, 5520, 5800, 6108, 3709];
var ConsumoMedio = {"list":[100, 100, 240, 291, 300, 308, 400, 520, 580, 610, 690, 2000, 190, 172, 150, 881, 512, 155, 200, 2100, 2000, 120, 100, 200]};
var ConsumoPrevisto = [300000, 322000, 280000, 412000];
var ConsumoGiornaliero = {"list":[100, 100, 100, 100, 300, 400, 600, 490, 2100, 1200, 1100, 1000, 2400, 280, 240, 820, 1000, 1800, 230, 340, 1302, 2800, 200, 130, 600]};
var StoricoElettr = [{"nome":"Lavatrice", "id" : "id1", "locazione":"Bagno", "status":1, "value":"2.1", "icona":"lavatrice_verde.png"}, 
	{"nome":"Frigorifero", "id" : "id2", "locazione":"Cucina", "status":1, "value":"0.3", "icona":"frigorifero_verde.png"}, 
	{"nome":"TV", "id" : "id3", "locazione":"Soggiorno", "status":1, "value":"2.1", "icona":"tv_verde.png"}, 
	{"nome":"Forno", "id" : "id4", "locazione":"Cucina", "status":0, "value":"0", "icona":"forno_verde.png"}];

var StoricoCostoI = {"list":[0.040,0.040,0.041,0.040,0.042,0.040,0.050,0.45,0.78,0.44,0.06,0.05,0.82,0.67,0.50,0.05,0.06,0.91,0.74,0.98,0.49,0.53,0.23,0.10]};
var StoricoConsumoI = {"list":[130, 130, 140, 130, 159, 130, 180, 123, 203, 122, 180, 140, 2240, 1830, 1050, 150, 180, 2800, 2100, 3400, 3040, 2130, 2430, 1130]}; 

var StoricoCostoS   = {"list":[1.0, 1.2, 1.8, 0.9, 2.0, 1.3, 1.4, 0.8]};
var StoricoConsumoS = {"list":[6200, 7200, 10100, 5400, 12200, 6400, 6900, 4900]};

var StoricoCostoM = {"list":[1.0, 1.2, 1.8, 0.9, 2.0, 1.3, 1.4, 0.8,1.0, 1.2, 1.8, 0.9, 2.0, 1.3, 1.4, 0.8, 1.0, 1.2, 1.8, 0.9, 2.0, 1.3, 1.4, 0.8, 2.3, 1.5, 1.8, 2.0, 1.2, 1.0, 3.0]};
var StoricoConsumoM = {"list":[6200, 7200, 10100, 5400, 12200, 6400, 6900, 4900, 6200, 7200, 10100, 5400, 12200, 6400, 6900, 
                         4900, 6200, 7200, 1000, 5400, 12200, 6400, 6900, 4900, 6200, 7200, 10100, 5400, 12200, 6400, 6900]}; 

var StoricoCostoA = {"list":[30.2, 32.9, 41.2, 34.0, 33.4, 36.0, 29.0, 28.4, 42.0, 38.9, 35.0, 43.0, 38.0]};
var StoricoConsumoA = {"list":[113000, 131000, 200300, 140000, 165000, 186000, 205000, 111000, 115000, 209000, 200000, 176000, 165000]}; 

var indLista = 0;

var Suggerimenti = ["Lava a basse temperature", "Usa di piu' la lavatrice in fascia serale", "Sfrutta la capienza massima del cestello", 
	"Spegni il forno prima del termine della cottura", "Non aprire il forno nel preriscaldamento",
	"Non introdurre cibi caldi in frigo", "Regola il termostato del frigo dai 4 gradi in su", "Spegni il condizionatore <br>un'ora prima di uscire dal locale"];

var ListaLocazioni = [{"pid":"1","name":"Cucina","iconName":"cucina.png"},{"pid":"2","name":"Bagno","iconName":"bagno.png"},
			{"pid":"3","name":"Soggiorno","iconName":"soggiorno.png"}, {"pid":"4","name":"Camera da letto","iconName":"camera.png"},
			{"pid":"5","name":"Altro","iconName":"altro.png"}];

var ListaElettr = {"list":[{"map":{"appliance.pid": "0", "ah.app.name":"SmartInfo", "ah.app.type":"it.telecomitalia.ah.zigbee.metering", 
					"ah.category.pid":"2", "ah.location.pid":"3", "ah.icon": "plug.png", "availability": 0, "device_state":0,
					"device_value":{"name":"power", "value": {"timestamp":0, "value":"0"}}}},
				{"map":{"appliance.pid": "1", "ah.app.name":"Rex", "ah.app.type":"com.indesit.ah.app.whitegood", 
					"ah.category.pid":"2", "ah.location.pid":"2", "ah.icon": "lavatrice.png", "availability": 2, "device_state":1,
					"device_value":{"name":"power", "value": {"timestamp":0, "value":"1500.33"}}}},
				{"map":{"appliance.pid": "2", "ah.app.name":"Tv", "ah.app.type":"com.indesit.ah.app.whitegood", 
					"ah.category.pid":"2", "ah.location.pid":"3", "ah.icon": "tv.png", "availability": 2, "device_state":0,
					"device_value":{"name":"power", "value": {"timestamp":0, "value":"0"}}}},
				{"map":{"appliance.pid": "3", "ah.app.name":"Zona PC", "ah.app.type":"com.indesit.ah.app.whitegood", 
					"ah.category.pid":"2", "ah.location.pid":"3", "ah.icon": "pczone.png", "availability": 2, "device_state":1,
					"device_value":{"name":"power", "value": {"timestamp":0, "value":"110"}}}},
				{"map":{"appliance.pid": "4", "ah.app.name":"Forno", "ah.app.type":"com.indesit.ah.app.whitegood", 
					"ah.category.pid":"2", "ah.location.pid":"1", "ah.icon": "forno.png", "availability": 2, "device_state":1,
					"device_value":{"name":"power", "value": {"timestamp":0, "value":"1200"}}}},
				{"map":{"appliance.pid": "5", "ah.app.name":"Ferro da stiro", "ah.app.type":"com.indesit.ah.app.whitegood", 
					"ah.category.pid":"2", "ah.location.pid":"5", "ah.icon": "ferro_stiro.png", "availability": 1, "device_state":0,
					"device_value":{"name":"power", "value": {"timestamp":0, "value":"0"}}}},
				{"map":{"appliance.pid": "6", "ah.app.name":"Frigorifero", "ah.app.type":"ah.app.frigorifero", 
					"ah.category.pid":"4", "ah.location.pid": "1", "ah.icon": "frigorifero.png", "availability": 2, "device_state":1, 
					"device_value":{"name":"power", "value": {"timestamp":0, "value":"95"}}}}
				  ]};

var ListaElettr1 = {"list":[{"map":{"appliance.pid": "0", "ah.app.name":"SmartInfo", "ah.app.type":"it.telecomitalia.ah.zigbee.metering", 
					"ah.category.pid":"2", "ah.location.pid":"3", "ah.icon": "plug.png", "availability": 0, "device_state":0,
					"device_value":{"name":"power", "value": {"timestamp":0, "value":"0"}}}},
				{"map":{"appliance.pid": "1", "ah.app.name":"Rex", "ah.app.type":"com.indesit.ah.app.whitegood", 
					"ah.category.pid":"2", "ah.location.pid":"2", "ah.icon": "lavatrice.png", "availability": 2, "device_state":1,
					"device_value":{"name":"power", "value": {"timestamp":0, "value":"92"}}}},
				{"map":{"appliance.pid": "2", "ah.app.name":"Tv", "ah.app.type":"com.indesit.ah.app.whitegood", 
					"ah.category.pid":"2", "ah.location.pid":"3", "ah.icon": "tv.png", "availability": 2, "device_state":0,
					"device_value":{"name":"power", "value": {"timestamp":0, "value":"0"}}}},
				{"map":{"appliance.pid": "3", "ah.app.name":"Zona PC", "ah.app.type":"com.indesit.ah.app.whitegood", 
					"ah.category.pid":"2", "ah.location.pid":"3", "ah.icon": "pczone.png", "availability": 2, "device_state":1,
					"device_value":{"name":"power", "value": {"timestamp":0, "value":"105"}}}},
				{"map":{"appliance.pid": "4", "ah.app.name":"Forno", "ah.app.type":"com.indesit.ah.app.whitegood", 
					"ah.category.pid":"2", "ah.location.pid":"1", "ah.icon": "forno.png", "availability": 2, "device_state":0,
					"device_value":{"name":"power", "value": {"timestamp":0, "value":"0"}}}},
				{"map":{"appliance.pid": "5", "ah.app.name":"Ferro da stiro", "ah.app.type":"com.indesit.ah.app.whitegood", 
					"ah.category.pid":"2", "ah.location.pid":"5", "ah.icon": "ferro_stiro.png", "availability": 1, "device_state":0,
					"device_value":{"name":"power", "value": {"timestamp":0, "value":"0"}}}},
				{"map":{"appliance.pid": "6", "ah.app.name":"Frigorifero", "ah.app.type":"ah.app.frigorifero", 
					"ah.category.pid":"4", "ah.location.pid": "1", "ah.icon": "frigorifero.png", "availability": 2, "device_state":1, 
					"device_value":{"name":"power", "value": {"timestamp":0, "value":"115"}}}}
				  ]};
/*************************************/

/**
 * @author Tilab
 * Wrapper che maschera le chiamate verso AG e il formato dei dati
 */

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
	MODULE : "InterfaceEnergyHome",
	mode : 0, // 0 : simulazione, 1 : solo AG, no piattaforma, > 1 rete completo
	STATUS_OK : 0,
	STATUS_ERR : -1,
	STATUS_ERR_HTTP : -2,
	STATUS_EXCEPT : -3,
	HTTP_TIMEOUT : 7000,
	errMessage: null,
	errCode: 0,
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
	// costanti per nomi attributi
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
	serviceName : "it.telecomitalia.ah.greenathome.GreenAtHomeApplianceService",
	objService : null,
	backCostoPrevisto : null,
	backCostoOdierno : null,
	backCostoMedio : null,
	backSuddivisioneCosti : null,
	backConsumoPrevisto : null,
	backConsumoOdierno : null,
	backConsumoMedio : null,
	backConsumoGiornaliero : null,
	backPotenzaAttuale : null,
	backMaxElettr : null,
	backElettrStorico : null,
	backStorico : null,
	backListaElettr : null
}

function bindService(name) {
	var sReg = OSGi.find(name);
	
	if (sReg) 
      	return OSGi.bind(sReg[0]);
      else {
      	Log.alert(40,  InterfaceEnergyHome.MODULE, "Servizio non trovato: " + name);
		return null;				
      }
}            


InterfaceEnergyHome.Init = function() 
{ 

	InterfaceEnergyHome.errMessage = null;
	InterfaceEnergyHome.errCode = 0;
	if (InterfaceEnergyHome.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService = bindService(InterfaceEnergyHome.serviceName);
			if (InterfaceEnergyHome.objService != null)
				return 1;
			else
				throw("Impossibile connettersi al servizio");
			
		}
		catch(err)
		{
			Log.alert(40,  InterfaceEnergyHome.MODULE, "Eccezione: " + err.message);
			InterfaceEnergyHome.errMessage = err.message;
			InterfaceEnergyHome.errCode =  InterfaceEnergyHome.STATUS_ERR;
			InterfaceEnergyHome.objService = null;
			return InterfaceEnergyHome.errCode;
		}
	}
	else
		return 1;
}

// abortisce tutte le eventuali chiamate in corso
// per adesso annulla tutte le callback in modo che quando 
// la funzione ritorna non richiama la callback
// bisogna provare se si riescono ad abortire le chiamate
InterfaceEnergyHome.Abort = function ()
{
	InterfaceEnergyHome.backCostoPrevisto = null;
	InterfaceEnergyHome.backCostoOdierno = null;
	InterfaceEnergyHome.backCostoMedio = null;
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

}

/*******************************************************************
 * Ogni funzione non puo' essere richiamata contemporaneamente da
 * da piu' parti nella pagina, per cui posso memorizzare la 
 * funzione di callback per ogni chiamata
 ******************************************************************/


// ritorna numero, con possibili decimali, che indica costo in euro, null se errore
InterfaceEnergyHome.BackSuggerimento = function (result, err)
{
	if (InterfaceEnergyHome.backSuggerimento != null)
	{
		if (err != null)
			retVal = null;
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
		ind = Math.round(Math.random() * (Suggerimenti.length));
		val = Suggerimenti[ind];
		Log.alert(80,  InterfaceEnergyHome.MODULE, "GetSuggerimento ind = " + ind + " val = " + val);
		InterfaceEnergyHome.BackSuggerimento(val, null);
	}
}


// ritorna numero, con possibili decimali, che indica costo in euro, null se errore
InterfaceEnergyHome.BackCostoOdierno = function (result, err)
{
	
	if (InterfaceEnergyHome.backCostoOdierno != null)
	{
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
		ind = Math.round(Math.random() * (CostoOdierno.length+1));
		Log.alert(80,  InterfaceEnergyHome.MODULE, "GetCostoOdierno ind = " + ind);
		val = {"list":[CostoOdierno[ind]]};
		InterfaceEnergyHome.BackCostoOdierno(val, null);
	}
}

// ritorna numero, con possibili decimali, che indica costo in euro, null se errore
InterfaceEnergyHome.BackCostoPrevisto = function (result, err)
{
	
	if (InterfaceEnergyHome.backCostoPrevisto!= null)
	{
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
		ind = Math.round(Math.random() * CostoPrevisto.length);
		Log.alert(80,  InterfaceEnergyHome.MODULE, "GetCostoPrevisto: ind = " + ind);
		val =  CostoPrevisto[ind];
		InterfaceEnergyHome.BackCostoPrevisto(val, null);
	}
}	

// ritorna numero, con possibili decimali, che indica costo in euro, null se errore
InterfaceEnergyHome.BackCostoMedio = function (result, err)
{
	
	if (InterfaceEnergyHome.backCostoMedio!= null)
	{
		if ((err != null) || (result == null))
			retVal = null;
		else
		{
			// trascodifica dato : mi viene ritornato il costo medio per ora nelle 24 ore
			// sommo il costo per le ore fino all'ora attuale
			attuale = GestDate.GetActualDate();
			oraAttuale = attuale.getHours();
			minAttuale = attuale.getMinutes();
			retVal = 0;
			for (i = 0; i < oraAttuale; i++)
			{
				retVal += result.list[i];
			}
			// aggiungo percentuale in base ai minuti dell'ora attuale
			retVal += result.list[oraAttuale] * (minAttuale / 60);
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


// ritorna array del tipo: [['Lavatrice', 25], ['Frigo', 9], ['PC', 5], ['Altro', 51] ]
// dove il numero e' la percentuale rispetto al totale 
InterfaceEnergyHome.BackSuddivisioneCosti = function (result, err)
{
	Log.alert(80,  InterfaceEnergyHome.MODULE, "BackSuddivisioneCosti val = "  + result);
	if (InterfaceEnergyHome.backSuddivisioneCosti != null)
	{	
		retVal = null;
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


// ritorna una struttura con pid, nome e valore dell'elettrodomestico che sta consumando di piu'
// leggo tutti gli elettrodomestici e vedo quale ha il valore (di consumo) maggiore
InterfaceEnergyHome.BackMaxElettr = function (result, err)
{
	if (InterfaceEnergyHome.backMaxElettr != null)
	{	
		retVal = null;
		if ((err == null) && (result != null))
		{
		// eventuale trascodifica dato 
		// cerco l'elettrodomestico con consumo istantaneo maggiore
			len = result.list.length;
			maxVal = -1;
			maxInd = -1;
			for (i = 0; i < len; i++)
			{
				res = result.list[i];
				if ((res.map[InterfaceEnergyHome.ATTR_APP_STATE_AVAIL] == undefined) || 
					(res.map[InterfaceEnergyHome.ATTR_APP_STATE_AVAIL] == true))
				{
					if (res.map[InterfaceEnergyHome.ATTR_APP_VALUE] == undefined)
						val = 0;
					else
						val = parseInt(res.map[InterfaceEnergyHome.ATTR_APP_VALUE].value.value);
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


// ritorna numero, con possibili decimali, che indica consumo in kWh (w ??), null se errore
InterfaceEnergyHome.BackConsumoOdierno = function (result, err)
{
	if (InterfaceEnergyHome.backConsumoOdierno != null)
	{
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
			InterfaceEnergyHome.objService.getAttributeData(InterfaceEnergyHome.BackConsumoOdierno, 
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
		ind = Math.round(Math.random() * (ConsumoOdierno.length));
		Log.alert(80,  InterfaceEnergyHome.MODULE, "GetConsumoOdierno ind = " + ind);
		val =  {"list":[ConsumoOdierno[ind]]};
		InterfaceEnergyHome.BackConsumoOdierno(val, null);
	}
}

// ritorna numero, con possibili decimali, che indica consumo in kWh, null se errore
InterfaceEnergyHome.BackConsumoPrevisto = function (result, err)
{
	if (InterfaceEnergyHome.backConsumoPrevisto != null)
	{	
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
		ind = Math.round(Math.random() * ConsumoPrevisto.length);
		Log.alert(80,  InterfaceEnergyHome.MODULE, "GetConsumoPrevisto: ind = " + ind);
		val =  ConsumoPrevisto[ind];
		InterfaceEnergyHome.BackConsumoPrevisto(val, null);
	}
}

// ritorna array di numeri, con possibili decimali, che indica consumo in kWh, null se errore
// ogni valore corrisponde ad un'ora, partendo da 0
InterfaceEnergyHome.BackConsumoGiornaliero = function (result, err)
{
	
	if (InterfaceEnergyHome.backConsumoGiornaliero!= null)
	{	
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

// ritorna array di numeri, con possibili decimali, che indica consumo in kWh
// ogni valore corrisponde ad un'ora, partendo da 0
InterfaceEnergyHome.BackConsumoMedio = function (result, err)
{
	
	if (InterfaceEnergyHome.backConsumoMedio != null)
	{	
		
		retVal = null;
		if ((err == null) && (result != null))
		{
			// trascodifica dato : mi viene ritornato il consumo medio per ora nelle 24 ore
			// sommo il consumo per le ore fino all'ora attuale
			attuale = GestDate.GetActualDate();
			oraAttuale = attuale.getHours();
			minAttuale = attuale.getMinutes();
			retVal = 0;
			for (i = 0; i < oraAttuale; i++)
			{
				retVal += result.list[i];
			}
			// aggiungo percentuale in base ai minuti dell'ora attuale
			retVal += result.list[oraAttuale] * (minAttuale / 60);
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

// ritorna numero, con possibili decimali, che indica consumo in kW, null se errore
InterfaceEnergyHome.BackPotenzaAttuale = function (result, err)
{
	Log.alert(80,  InterfaceEnergyHome.MODULE, "BackPotenzaAttuale result = "  + result);
	if (InterfaceEnergyHome.backPotenzaAttuale != null)
	{
		if ((err == null) && (result != null))
			retVal = result.value;
		else
			retVal = null;
		
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
		PotenzaAttuale.value += 50;
		if (PotenzaAttuale.value > 5000)
			PotenzaAttuale.value = 0;
		InterfaceEnergyHome.BackPotenzaAttuale(PotenzaAttuale, null);
	}
}


// ritorna array di strutture
InterfaceEnergyHome.BackListaElettr = function (result, err)
{
	var smartInfo = null;
	
	Log.alert(80,  InterfaceEnergyHome.MODULE, "BackListaElettr val = "  + result);
	if (InterfaceEnergyHome.backListaElettr != null)
	{	
		if (err != null)
			retVal = null;
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

			//retVal = result;
		}
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

// ritorna array di coppie di valori ["nome", "pid"]
InterfaceEnergyHome.BackElettrStorico = function (result, err)
{
	Log.alert(80,  InterfaceEnergyHome.MODULE, "BackElettrStorico val = "  + result);
	if (InterfaceEnergyHome.backElettrStorico != null)
	{	
		if (err != null)
			retVal = null;
		else
		{
		// trascodifica dato : ritorno coppie ["nome", "pid"]
			retVal = new Array();
			for (i = 0; i < result.length; i++)
			{
				retVal[i] = new Object();
				retVal[i].nome = result[i].nome;
				retVal[i].pid = result[i].id;
			}
		}
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
		val = StoricoElettr;
		InterfaceEnergyHome.BackElettrStorico(val, null);
	}
}


// ritorna array di valori (costo o consumo)
InterfaceEnergyHome.BackStorico = function (result, err)
{
	
	if (InterfaceEnergyHome.backStorico != null)
	{	
		retVal = null;
		if ((err == null) && (result != null))
				retVal = result.list;
		Log.alert(80,  InterfaceEnergyHome.MODULE, "BackStorico retVal = "  + retVal);
		InterfaceEnergyHome.backStorico(retVal);    
	}
}

InterfaceEnergyHome.GetStorico = function (tipo, pid, dataInizio, dataFine, intervallo, backFunc)
{
	InterfaceEnergyHome.backStorico = backFunc;
	Log.alert(80, InterfaceEnergyHome.MODULE, "GetStorico");
	if (tipo == "Costo")
		param1 = InterfaceEnergyHome.COSTO;
	else
		param1 = InterfaceEnergyHome.CONSUMO;

	if (intervallo == 0)
	{
		param2 = InterfaceEnergyHome.HOUR;
		dataInizio.setHours(0);
		dataFine.setHours(23);
		dataFine.setMinutes(30);
	}
	else
		if (intervallo == 3)
		param2 = InterfaceEnergyHome.MONTH;
	else
		param2 = InterfaceEnergyHome.DAY;


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
				val = StoricoCostoS;
			else
				val = StoricoConsumoS;
		}
		else
		if (intervallo == 2)
		{
			if (tipo == "Costo")
				val = StoricoCostoM;
			else
				val = StoricoConsumoM;
		}
		else
		{
			if (tipo == "Costo")
				val = StoricoCostoA;
			else
				val = StoricoConsumoA;
		}
		InterfaceEnergyHome.BackStorico(val, null);
	}
}

InterfaceEnergyHome.BackGetLocazioni = function (result, err)
{
	Log.alert(80,  InterfaceEnergyHome.MODULE, "BackGetLocazioni  val = "  + result);
	if (InterfaceEnergyHome.backGetLocazioni != null)
	{	
		if (err != null)
			retVal = null;
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

// legge la data da un feed (come per Cubo Vision)
InterfaceEnergyHome.GetActualDate = function()
{
    var objDate = new XMLHttpRequest();
    var actualDateStr = null;
    var actualDate = null;
    var alertTimer = null;
    
return new Date("Jul 27 2011, 14:30:00");
    if (objDate != null)
   {
        url = "http://device.alice.it/widget/finanza/listini/list?device=ctv&sn=000000001111";
        //url = InterfaceEnergyHome.urlServer + "widget/finanza/listini/list?device=ctv&sn=000000001111";
        objDate.open("GET", url, false);
        alertTimer = setTimeout(InterfaceEnergyHome.GestTimeout, InterfaceEnergyHome.HTTP_TIMEOUT);
        objDate.send(null);
        if (objDate.status == 200)
        {
            var xmlElement = objDate.responseXML;
            Log.alert(80, "GenerateActualDate", "xml = " + xmlElement);
            elem = xmlElement.getElementsByTagName("lastBuildDate");
            if (elem != null)
            {
                actualDateStr = elem[0].childNodes[0].nodeValue;
                ind = actualDateStr.indexOf("+");
                actualDateStr = actualDateStr.substring(0, ind);
                actualDate = new Date(actualDateStr);
                Log.alert(80, "GenerateActualDate", "ora = " + actualDate.toString());
             }
        }
        clearTimeout(alertTimer);
        return actualDate;
    }
}
