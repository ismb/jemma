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

var InterfaceEnergyHome2 = {
	MODULE : "InterfaceEnergyHome2",
	mode : 2, // 0 : simulazione, 1 : rete
	MODE_SIMUL : 0,
	MODE_DEMO : 1,
	MODE_FULL : 2,
	MODE_COST : 3,
	DISCONNECTED : 0,
	CONNECTING : 1,
	CONNECTED: 2,
	STATUS_OK : 0,
	STATUS_ERR : -1,
	STATUS_ERR_HTTP : -2,
	STATUS_EXCEPT : -3,
	HTTP_TIMEOUT : 7000,
	errMessage: null,
	errCode: 0,
	status: 0,
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
	SMARTINFO_APP_TYPE : "org.energy_home.jemma.ah.zigbee.metering",
	POTENZA_TOTALE : "TotalPower",
	CONSUMO : "ah.eh.esp.Energy",
	COSTO : "ah.eh.esp.EnergyCost",

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

	backGetInquiredDevices : null,
	backInstallAppliance : null,
	backListaAppliances : null,
	backGetCategories : null,
	backGetLocations : null,
	backModificaDispositivo  : null,
	backEliminaDispositivo : null,
	backStatoConnessione : null,
	backSuggerimento: null,
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
	backListaElettr : null,
	_listeners: []
}

function bindService(name) {
	var sReg = OSGi.find(name);

	if (sReg && sReg.length > 0) {
      	return OSGi.bind(sReg[0]);
	}
    else {
      	Log.alert(40,  InterfaceEnergyHome2.MODULE, "Servizio non trovato: " + name);
		return null;				
    }
} 

InterfaceEnergyHome2.Init = function() 
{ 
	InterfaceEnergyHome2.errMessage = null;
	InterfaceEnergyHome2.errCode = 0;
	
	if (InterfaceEnergyHome2.mode > 0)
	{
		InterfaceEnergyHome2.attachToService();
	}
	else {
		// creates fake methods!
		
		//InterfaceEnergyHome.objService = new Array();
		var service = InterfaceEnergyHome.objService;
		
		service.getHapLastUploadTime = function(callback) {
			timestamp = new Date().getTime() - 100000;
			callback(timestamp, null);
		}
		
		service.setHapConnectionId = function(callback, connectionId) {
			callback(null);
		}
		
		service.getHapConnectionId = function(callback) {
			callback("pippo", null);
		}
		
		service.currentTimeMillis = function(callback) {
			timestamp = new Date().getTime() - 100000;
			callback(timestamp, null);
		}
		
		service.installAppliance = function(callback, appliance) {
			ListaElettr.list[ListaElettr.list.length] = appliance;
			Log.alert(80,  InterfaceEnergyHome2.MODULE, "InstallAppliance = " + appliance);
			callback(0, null);
		}
		
		service.updateAppliance = function(callback, appliance) {
			id = appliance.map[InterfaceEnergyHome2.ATTR_APP_PID];
			for (i = 0; i < ListaElettr.length; i++)
				if (ListaElettr[i].map[InterfaceEnergyHome2.ATTR_APP_PID] == pid)
				{
					ListaElettr[i] = appliance;
				}
			
			Log.alert(80,  InterfaceEnergyHome2.MODULE, "ModificaDispositivo = " + id);
			callback(appliance, null);
		}
		
		service.getAppliancesConfigurationsDemo = function(callback) {
			callback(ListaElettr, null);
		}
		
		service.startInquiry = function(callback, timeout) {
			callback(null);
		}
		
		service.stopInquiry = function(callback) {
			callback(null);
		}
 	}
	
	return;
}

InterfaceEnergyHome2.reconnect = function() {
	
	// do not allow to call the attachToService when the system is already polling
	if (InterfaceEnergyHome2.status == InterfaceEnergyHome2.CONNECTING) 
		return;
	
	if (InterfaceEnergyHome2.status != InterfaceEnergyHome2.DISCONNECTED) {
		InterfaceEnergyHome2.status = InterfaceEnergyHome2.DISCONNECTED;
		InterfaceEnergyHome2.notifyListeners("service.connection", InterfaceEnergyHome2.status);
		InterfaceEnergyHome2.attachToService();
	}
}

InterfaceEnergyHome2.attachToService = function() {
	try {
		//InterfaceEnergyHome.objService = bindService(InterfaceEnergyHome2.serviceName);
	}
	catch(e) {
		//InterfaceEnergyHome.objService = null;
	}
	if (InterfaceEnergyHome.objService != null) {
		Log.alert(0, InterfaceEnergyHome2.MODULE, "attached to OSGi service " +  InterfaceEnergyHome2.serviceName);
		
		if (InterfaceEnergyHome2.status != InterfaceEnergyHome2.CONNECTED) {
			InterfaceEnergyHome2.status = InterfaceEnergyHome2.CONNECTED;
			InterfaceEnergyHome2.notifyListeners("service.connection", InterfaceEnergyHome2.status);
		}
		return;
	}
	else {
		Log.alert(0, InterfaceEnergyHome2.MODULE, "OSGi service " +  InterfaceEnergyHome2.serviceName + " not available");			
		InterfaceEnergyHome2.status = InterfaceEnergyHome2.CONNECTING;
		Log.alert(40,  InterfaceEnergyHome2.MODULE, "Connecting");
		InterfaceEnergyHome2.timerAttach = setTimeout("InterfaceEnergyHome2.attachToService()", 5000);
	}
}

// abortisce tutte le eventuali chiamate in corso
// per adesso annulla tutte le callback in modo che quando 
// la funzione ritorna non richiama la callback
// bisogna provare se si riescono ad abortire le chiamate
InterfaceEnergyHome2.Abort = function ()
{
	InterfaceEnergyHome2.backGetInquiredDevices = null;
	InterfaceEnergyHome2.backListaAppliances = null;
	InterfaceEnergyHome2.backGetCategories = null;
	InterfaceEnergyHome2.backGetLocations = null;
	InterfaceEnergyHome2.backEliminaDispositivo = null;
	InterfaceEnergyHome2.backModificaDispositivo = null;
	InterfaceEnergyHome2.backStatoConnessione = null;
	InterfaceEnergyHome2.backStatoConnessione = null;
	InterfaceEnergyHome2.backCostoPrevisto = null;
	InterfaceEnergyHome2.backCostoOdierno = null;
	InterfaceEnergyHome2.backCostoMedio = null;
	InterfaceEnergyHome2.backSuddivisioneCosti = null;
	InterfaceEnergyHome2.backConsumoPrevisto = null;
	InterfaceEnergyHome2.backConsumoOdierno = null;
	InterfaceEnergyHome2.backConsumoMedio = null;
	InterfaceEnergyHome2.backConsumoGiornaliero = null;
	InterfaceEnergyHome2.backPotenzaAttuale = null;
	InterfaceEnergyHome2.backMaxElettr = null;
	InterfaceEnergyHome2.backElettrStorico = null;
	InterfaceEnergyHome2.backStorico = null;
	InterfaceEnergyHome2.backListaElettr = null;
}

InterfaceEnergyHome2.addListener = function(type, listener){
    if (typeof InterfaceEnergyHome2._listeners[type] == "undefined"){
    	InterfaceEnergyHome2._listeners[type] = [];
    }
    InterfaceEnergyHome2._listeners[type].push(listener);
}

InterfaceEnergyHome2.notifyListeners = function(type, event){

    if (!type) {
        throw new Error("Event object missing 'type' property.");
    }

    if (InterfaceEnergyHome2._listeners[type] instanceof Array){
        var listeners = this._listeners[type];
        for (var i=0, len=listeners.length; i < len; i++){
            listeners[i](event);
        }
    }
}

InterfaceEnergyHome2.removeListener = function(type, listener){
    if (InterfaceEnergyHome2._listeners[type] instanceof Array){
        var listeners = InterfaceEnergyHome2._listeners[type];
        for (var i=0, len=listeners.length; i < len; i++){
            if (listeners[i] === listener){
                listeners.splice(i, 1);
                break;
            }
        }
    }
}

InterfaceEnergyHome2.HandleError = function(func, err)
{	
	tmpMsg = "";
	if ((err.code == 0) && (err.msg == "")) {
		tmpMsg = "Error in gateway connection";
	}
	else if (err.code != null) {
		if (err.code == 0) {
			// it seems that if the connection fails the jabsorb returns this error code
			tmpMsg = "CONNECTION_ERROR";
		}
		else {
			tmpMsg = err.msg;
			if (err.msg != undefined)
				tmpMsg += " " + err.msg;
		}
	}
	
	Log.alert(20, InterfaceEnergyHome2.MODULE, func + " code = " + err.code + " errMsg = " + tmpMsg);
	Tracing.Trace(null, Tracing.ERR, Tracing.ERR_GENERIC, tmpMsg);
	InterfaceEnergyHome2.errMessage = tmpMsg;
	InterfaceEnergyHome2.errCode = InterfaceEnergyHome2.STATUS_ERR;
	
	var msgCode;
	
	if (err instanceof TypeError) {
		InterfaceEnergyHome2.attachToService();
	}
	else if (err.code == JSONRpcClient.Exception.CODE_REMOTE_EXCEPTION) {
		// this is a remote exception
		if (err.msg == "hap service not bound") {
			msgCode = "errorPlatform";
		}
	}
	else if ((err.code == 0) || (err.code == JSONRpcClient.Exception.CODE_ERR_NOMETHOD)) {
		// it seems that if the connection fails the jabsorb returns an exception with this error code
		if (InterfaceEnergyHome2.status != InterfaceEnergyHome2.DISCONNECTED) {
			InterfaceEnergyHome2.reconnect();
		}
	}
	InterfaceEnergyHome2.notifyListeners("service.error", err);
}

InterfaceEnergyHome2.GestErrorEH = function(func, err)
{
	InterfaceEnergyHome2.HandleError(func, err);
	return;
	
	if ((code == 0) && (msg == ""))
		tmpMsg = "Error in gateway connection";
	else
		if (code != null)
		{
			tmpMsg = code;
			if (msg != null)
					tmpMsg += " " + msg;
		}
		else
			tmpMsg = msg;
	
	
	
	Log.alert(20, InterfaceEnergyHome2.MODULE, func + " code = " + code + " errMsg = " + tmpMsg);
	Tracing.Trace(null, Tracing.ERR, Tracing.ERR_GENERIC, tmpMsg);
	InterfaceEnergyHome2.errMessage = tmpMsg;
	InterfaceEnergyHome2.errCode = InterfaceEnergyHome2.STATUS_ERR;
	
	InterfaceEnergyHome2.notifyListeners("service.error", err);
}

InterfaceEnergyHome2.getHapConnectionId = function (callback)
{
	try {
		InterfaceEnergyHome.objService.getHapConnectionId(callback);
	}
	catch (e) {
		callback(null, e);
	}
}

InterfaceEnergyHome2.setHapConnectionId = function (callback, id)
{
	try	{
		InterfaceEnergyHome.objService.setHapConnectionId(callback, id);
	}
	catch (e)	{
		callback(null, e);
	}
}

InterfaceEnergyHome2.getAttribute = function (callback, name)
{
	try	{
		InterfaceEnergyHome.objService.getAttribute(callback, name);
	}
	catch (e)	{
		callback(null, e);
	}
}

InterfaceEnergyHome2.setAttribute = function (callback, name, value)
{
	try	{
		InterfaceEnergyHome.objService.setAttribute(callback, name, value);
	}
	catch (e)	{
		callback(null, e);
	}
}

// ritorna array di strutture
InterfaceEnergyHome2.BackListaAppliances = function (result, err)
{
	if (InterfaceEnergyHome2.backListaAppliances != null)
	{	
		if (err != null)
		{
			retVal = null;
			InterfaceEnergyHome2.HandleError("BackListaAppliances", err);
		}
		else
		{
		// trascodifica dato : ritorno di strutture
			retVal = result.list;
			Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackListaAppliances val = "  + retVal);
			InterfaceEnergyHome2.backListaAppliances(retVal);  
		}
  
	}
}

InterfaceEnergyHome2.GetListaAppliances = function (backFunc)
{
	InterfaceEnergyHome2.backListaAppliances = backFunc;
	Log.alert(20, InterfaceEnergyHome2.MODULE, "GetListaAppliances");
	try
	{
		InterfaceEnergyHome.objService.getAppliancesConfigurationsDemo(InterfaceEnergyHome2.BackListaAppliances);
	}
	catch (err)
	{
		InterfaceEnergyHome2.BackListaAppliances(null, err);
	}

}

InterfaceEnergyHome2.BackGetCategories = function (result, err)
{
	if (InterfaceEnergyHome2.backGetCategories != null)
	{	
		if (err != null)
		{
			retVal = null;
			InterfaceEnergyHome2.HandleError("BackGetCategories", err); 
		}
		else
		{
			// ritorna dizionario pid-nome
			retVal = new Array();
			if (result != null)
			{
				for (i = 0; i < result.length; i++)
				{
					pid = result[i][InterfaceEnergyHome2.ATTR_CATEGORY_PID];
					name = result[i][InterfaceEnergyHome2.ATTR_CATEGORY_NAME];
					retVal[pid] = name;
				}
			}
		}
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackGetCategories val = "  + retVal);
		InterfaceEnergyHome2.backGetCategories(retVal);    
	}
}

InterfaceEnergyHome2.GetCategorie = function (backFunc, pid)
{
	InterfaceEnergyHome2.backGetCategories= backFunc;
	Log.alert(80, InterfaceEnergyHome2.MODULE, "GetCategorie");
	
	if (InterfaceEnergyHome2.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.getCategories(InterfaceEnergyHome2.BackGetCategories, pid);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackGetCategories(null, err);
		}
	}
	else
	{	
		// per test
		val = ListaCategorie;
		InterfaceEnergyHome2.BackGetCategories(val, null);
	}
}

InterfaceEnergyHome2.startInquiry = function (callback, timeout) {
	try {
		InterfaceEnergyHome.objService.startInquiry(callback, timeout);
	}
	catch (e) {
		callback(null, e);
	}
}

InterfaceEnergyHome2.stopInquiry = function (callback) {
	try {
		InterfaceEnergyHome.objService.stopInquiry(callback);
	}
	catch (e) {
		callback(null, e);
	}
}

// ritorna lista degli smart plug trovati o null se non ce ne sono
InterfaceEnergyHome2.BackInquiredDevices = function (result, err)
{
	if (InterfaceEnergyHome2.backInquiredDevices != null)
	{
		if (err != null)
		{
			retVal = null;
			InterfaceEnergyHome2.HandleError("BackInquiredDevices", err);
		}
		else
		{
			if (result == null)
				retVal = null;
			else
				retVal = result.list;
		}
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackInquiredDevices: val = "  + retVal);
		InterfaceEnergyHome2.backInquiredDevices(retVal);    
	}
}

InterfaceEnergyHome2.GetInquiredDevices = function (backFunc)
{
	InterfaceEnergyHome2.backInquiredDevices = backFunc;
	Log.alert(80,  InterfaceEnergyHome2.MODULE, "GetInquiredDevices");
	if (InterfaceEnergyHome2.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.getInquiredDevices(InterfaceEnergyHome2.BackInquiredDevices);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackInquiredDevices(null, err);
		}
	}
	else
	{	
		// per test
		var val = new InquiredDevicesVuoto();
		i = parseInt(ListaElettr.list.length) + 1;
		val.list[0].map[InterfaceEnergyHome2.ATTR_APP_PID] = i + ""; 
		InterfaceEnergyHome2.BackInquiredDevices(val, null);
	}
}

// ritorna 0 se inserimento ok, -1 se errore
InterfaceEnergyHome2.BackInstallAppliance = function (result, err)
{
	if (InterfaceEnergyHome2.backInstallAppliance != null)
	{
		if (err != null)
		{
			retVal = -1;
			InterfaceEnergyHome2.HandleError("BackInstallAppliance ", err);
		}
		else
			retVal = 0;
		
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackInstallAppliance  val = "  + retVal);
	
		InterfaceEnergyHome2.backInstallAppliance(retVal);    
	}
}

InterfaceEnergyHome2.InstallAppliance = function (backFunc, elem)
{
	InterfaceEnergyHome2.backInstallAppliance = backFunc;
	
	try
	{
		InterfaceEnergyHome.objService.installAppliance(InterfaceEnergyHome2.BackInstallAppliance, elem);
	}
	catch (err)
	{
		InterfaceEnergyHome2.BackInstallAppliance(null, err);
	}
}

// ritorna 0 se inserimento ok, -1 se errore
InterfaceEnergyHome2.BackModificaDispositivo = function (result, err)
{
	if (InterfaceEnergyHome2.backModificaDispositivo != null)
	{
		if (err != null)
		{
			retVal = -1;
			InterfaceEnergyHome2.HandleError("BackModificaDispositivo", err);
		}
		else
			retVal = 0;
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackModificaDispositivo val = "  + retVal);	
		InterfaceEnergyHome2.backModificaDispositivo(retVal);    
	}
}

InterfaceEnergyHome2.ModificaDispositivo = function (backFunc, elem)
{
	InterfaceEnergyHome2.backModificaDispositivo  = backFunc;

	try
	{
		InterfaceEnergyHome.objService.updateAppliance(InterfaceEnergyHome2.BackModificaDispositivo, elem);
	}
	catch (err)
	{
		InterfaceEnergyHome2.BackModificaDispositivo(null, err);
	}
}

InterfaceEnergyHome2.BackEliminaDispositivo = function (result, err)
{
	if (InterfaceEnergyHome2.backEliminaDispositivo  != null)
	{	
		if (err != null)
		{
			retVal = null;
			InterfaceEnergyHome2.HandleError("BackEliminaDispositivo", err);
		}
		else
		{
		// eventuale trascodifica dato 
			retVal = 0;
		}
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackEliminaDispositivo val = "  + retVal);
		InterfaceEnergyHome2.backEliminaDispositivo(retVal);    
	}
}

InterfaceEnergyHome2.EliminaDispositivo = function (backFunc, pid)
{
	InterfaceEnergyHome2.backEliminaDispositivo = backFunc;
	Log.alert(80, InterfaceEnergyHome2.MODULE, "EliminaDispositivo");
	
	if (InterfaceEnergyHome2.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.removeDevice(InterfaceEnergyHome2.BackEliminaDispositivo, pid);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackEliminaDispositivo(null, err);
		}
	}
	else
	{	
		// per test
		var tmpLista =  {"list":[]};
		for (i = 0; i < ListaElettr.list.length; i++)
		{
			if (ListaElettr.list[i].map[InterfaceEnergyHome2.ATTR_APP_PID] != pid)
			{
				tmpLista.list[tmpLista.list.length] = ListaElettr.list[i];
			}
		}
		ListaElettr = tmpLista;
		InterfaceEnergyHome2.BackEliminaDispositivo(ListaElettr, null);
	}
}

InterfaceEnergyHome2.getHapLastUploadTime = function (callback) {
	try {
		InterfaceEnergyHome.objService.getHapLastUploadTime(callback);
	}
	catch (e) {
		callback(null, e);
	}
}

InterfaceEnergyHome2.BackStatoConnessione = function (result, err)
{
	if (InterfaceEnergyHome2.backStatoConnessione!= null)
	{	
		if (err != null)
		{
			retVal = null;
			InterfaceEnergyHome2.HandleError("BackStatoConnessione", err);
		}
		else
		{
		// eventuale trascodifica dato 
			retVal = result;
		}
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackStatoConnessione val = "  + retVal);
		InterfaceEnergyHome2.backStatoConnessione(retVal);    
	}
}

InterfaceEnergyHome2.GetStatoConnessione = function (backFunc)
{
	InterfaceEnergyHome2.backStatoConnessione= backFunc;
	Log.alert(80, InterfaceEnergyHome2.MODULE, "GetStatoConnessione");
	
	if (InterfaceEnergyHome2.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.isHapClientConnected(InterfaceEnergyHome2.BackStatoConnessione);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackStatoConnessione(null, err);
		}
	}
	else
	{	
		// per test
		ind = Math.round(Math.random() * StatoConnessione.length);
		val = StatoConnessione[ind];
		InterfaceEnergyHome2.BackStatoConnessione(val, null);
	}
}

//uguale per configuratore e trial main
InterfaceEnergyHome2.BackGetLocazioni = function (result, err)
{
	if (InterfaceEnergyHome2.backGetLocazioni != null)
	{	
		if (err != null)
		{
			retVal = null;
			// in realta' puo' essere richiamato anche da configuratore
			InterfaceEnergyHome2.GestErrorEH("BackGetLocazioni", err);
		}
		else
		{
		// ritorna dizionario pid-nome
			retVal = new Array();
			if (result != null)
			{
				for (i = 0; i < result.length; i++)
				{
					pid = result[i][InterfaceEnergyHome2.ATTR_LOCATION_PID];
					name = result[i][InterfaceEnergyHome2.ATTR_LOCATION_NAME];
					retVal[pid] = name;
				}
			}
		}
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackGetLocazioni  val = "  + retVal);
		InterfaceEnergyHome2.backGetLocazioni(retVal);    
	}
}

InterfaceEnergyHome2.GetLocazioni = function (backFunc)
{
	InterfaceEnergyHome2.backGetLocazioni = backFunc;
	Log.alert(80, InterfaceEnergyHome2.MODULE, "GetLocazioni");
	
	if (InterfaceEnergyHome2.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.getLocations(InterfaceEnergyHome2.BackGetLocazioni);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackGetLocazioni(null, err);
		}
	}
	else
	{	
		// per test
		val = ListaLocazioni;
		InterfaceEnergyHome2.BackGetLocazioni(val, null);
	}
}


//ritorna numero, con possibili decimali, che indica costo in euro, null se errore
InterfaceEnergyHome2.BackSuggerimento = function (result, err)
{
	if (InterfaceEnergyHome2.backSuggerimento != null)
	{
		if (err != null)
		{
			retVal = null;
			InterfaceEnergyHome2.GestErrorEH("BackSuggerimento", err);
		}
		else
		{
	// eventuale trascodifica dato 
			retVal = result;
		}
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackSuggerimento val = "  + retVal);
	
		InterfaceEnergyHome2.backSuggerimento(retVal);    
	}
}

InterfaceEnergyHome2.GetSuggerimento = function (backFunc)
{
	InterfaceEnergyHome2.backSuggerimento = backFunc;
	
	/**
	if (InterfaceEnergyHome2.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.getAttributeData(InterfaceEnergyHome2.BackSuggerimento, "Suggerimento");
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackSuggerimento (null, err);
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
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "GetSuggerimento ind = " + ind + " val = " + val);
		InterfaceEnergyHome2.BackSuggerimento(val, null);
	}
}


//ritorna numero, con possibili decimali, che indica costo in euro, null se errore
InterfaceEnergyHome2.BackCostoOdierno = function (result, err)
{
	
	if (InterfaceEnergyHome2.backCostoOdierno != null)
	{
		if (err != null)
			InterfaceEnergyHome2.GestErrorEH("BackCostoOdierno", err);
		retVal = null;
		if ((err == null) && (result != null))
		{
			if (result.list.length > 0)
				// per come ho impostato la chiamata ho solo un valore
				retVal = result.list[0];
		}
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackCostoOdierno retVal = "  + retVal);
		InterfaceEnergyHome2.backCostoOdierno(retVal);    
	}
}

InterfaceEnergyHome2.GetCostoOdierno = function (backFunc)
{
	start = Main.dataAttuale.getTime();
	InterfaceEnergyHome2.backCostoOdierno = backFunc;
	/**/
	if (InterfaceEnergyHome2.mode > 1) // solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getAttributeData(InterfaceEnergyHome2.BackCostoOdierno, 
					InterfaceEnergyHome2.PID_TOTALE, InterfaceEnergyHome2.COSTO, 
					start, start, InterfaceEnergyHome2.DAY, true, InterfaceEnergyHome2.DELTA);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackCostoOdierno(null, err);
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
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "GetCostoOdierno val = " + val + " ind = " + ind);
		InterfaceEnergyHome2.BackCostoOdierno(val, null);
	}
}

//ritorna numero, con possibili decimali, che indica costo in euro, null se errore
InterfaceEnergyHome2.BackCostoPrevisto = function (result, err)
{
	
	if (InterfaceEnergyHome2.backCostoPrevisto!= null)
	{
		if (err != null)
			InterfaceEnergyHome2.GestErrorEH("BackCostoPrevisto", err);
		if ((err == null) && (result != null))
			retVal = result; // non c'e' il campo value ?!?
		else
			retVal = null;
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackCostoPrevisto retVal = "  + retVal);
		InterfaceEnergyHome2.backCostoPrevisto(retVal);    
	}
}

InterfaceEnergyHome2.GetCostoPrevisto = function (backFunc)
{
	InterfaceEnergyHome2.backCostoPrevisto = backFunc;
	/**/
	if (InterfaceEnergyHome2.mode > 1) // solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getForecast(InterfaceEnergyHome2.BackCostoPrevisto, 
				InterfaceEnergyHome2.PID_TOTALE, InterfaceEnergyHome2.COSTO, Main.dataAttuale.getTime(), InterfaceEnergyHome2.MONTH);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackCostoPrevisto(null, err);
		}
	}
	else
	/**/
	{		
		// per test
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "GetCostoPrevisto: ind = " + ind);
		val =  CostoPrevisto;
		InterfaceEnergyHome2.BackCostoPrevisto(val, null);
	}
}	

//ritorna numero, con possibili decimali, che indica costo in euro, null se errore
InterfaceEnergyHome2.BackCostoMedio = function (result, err)
{
	
	if (InterfaceEnergyHome2.backCostoMedio!= null)
	{
		if (err != null)
			InterfaceEnergyHome2.GestErrorEH("BackCostoMedio", err);
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
		Log.alert(80, InterfaceEnergyHome2.MODULE, "BackCostoMedio val = "  + retVal);
		InterfaceEnergyHome2.backCostoMedio(retVal);    
	}
}

InterfaceEnergyHome2.GetCostoMedio = function (backFunc)
{
	weekDay = Main.dataAttuale.getDay() + 1; // js comincia da 0, java da 1
	InterfaceEnergyHome2.backCostoMedio = backFunc;
	/**/
	if (InterfaceEnergyHome2.mode > 1)// solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getWeekDayAverage(InterfaceEnergyHome2.BackCostoMedio , 
					InterfaceEnergyHome2.PID_TOTALE,	InterfaceEnergyHome2.COSTO, weekDay);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackCostoMedio (null, err);
		}
	}
	else
	/**/
	{	
		// per test
		val = CostoMedio;  
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "GetCostoMedio : ind = " + ind + " val = " + val);
		InterfaceEnergyHome2.BackCostoMedio(val, null);
	}
}


//ritorna array del tipo: [['Lavatrice', 25], ['Frigo', 9], ['PC', 5], ['Altro', 51] ]
//dove il numero e' la percentuale rispetto al totale 
InterfaceEnergyHome2.BackSuddivisioneCosti = function (result, err)
{
	if (InterfaceEnergyHome2.backSuddivisioneCosti != null)
	{	
		retVal = null;
		if (err != null)
			InterfaceEnergyHome2.GestErrorEH("BackSuddivisioneCosti", err);
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
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackSuddivisioneCosti val = "  + retVal);
		InterfaceEnergyHome2.backSuddivisioneCosti(retVal);    
	}
}

InterfaceEnergyHome2.GetSuddivisioneCosti = function (backFunc)
{
	start = new Date(Main.dataAttuale.getTime());
	//start.setDate(0);
	end = new Date(Main.dataAttuale.getTime());
	InterfaceEnergyHome2.backSuddivisioneCosti = backFunc;
	/**/
	if (InterfaceEnergyHome2.mode > 1) // solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getAttributeData(InterfaceEnergyHome2.BackSuddivisioneCosti, InterfaceEnergyHome2.COSTO, 
				start.getTime(), end.getTime(), InterfaceEnergyHome2.MONTH, true, InterfaceEnergyHome2.DELTA);

		}
		catch (err)
		{
			InterfaceEnergyHome2.BackSuddivisioneCosti (null, err);
		}
	}
	else
	/**/
	{		
		// per test
		ind = Math.round(Math.random() * SuddivisioneCosti.length);
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "GetSuddivisioneCosti: ind = " + ind);
		val = SuddivisioneCosti;
		InterfaceEnergyHome2.BackSuddivisioneCosti(val, null);
	}
}


//ritorna una struttura con pid, nome e valore dell'elettrodomestico che sta consumando di piu'
//leggo tutti gli elettrodomestici e vedo quale ha il valore (di consumo) maggiore
InterfaceEnergyHome2.BackMaxElettr = function (result, err)
{
	if (InterfaceEnergyHome2.backMaxElettr != null)
	{	
		if (err != null)
			InterfaceEnergyHome2.GestErrorEH("BackMaxElettr", err);
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
				//if ((res.map[InterfaceEnergyHome2.ATTR_APP_STATE_AVAIL] == undefined) || 
				//	(res.map[InterfaceEnergyHome2.ATTR_APP_STATE_AVAIL] == true))
				if ((res.map[InterfaceEnergyHome2.ATTR_APP_AVAIL] == 2) && 
					(res.map[InterfaceEnergyHome2.ATTR_APP_TYPE] != InterfaceEnergyHome2.SMARTINFO_APP_TYPE))
				{
					if (res.map[InterfaceEnergyHome2.ATTR_APP_VALUE] == undefined)
						val = 0;
					else
						val = parseFloat(res.map[InterfaceEnergyHome2.ATTR_APP_VALUE].value.value);
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
				retVal.nome = result.list[maxInd].map[InterfaceEnergyHome2.ATTR_APP_NAME];;
				retVal.icona = result.list[maxInd].map[InterfaceEnergyHome2.ATTR_APP_ICON];
				retVal.tipo = result.list[maxInd].map[InterfaceEnergyHome2.ATTR_APP_TYPE];
				retVal.value = maxVal;
				Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackMaxElettr : nome = " + retVal.nome);
			}	
		}
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackMaxElettr retVal = "  + retVal);
		InterfaceEnergyHome2.backMaxElettr(retVal);    
	}
}

InterfaceEnergyHome2.GetMaxElettr = function (backFunc)
{
	InterfaceEnergyHome2.backMaxElettr = backFunc;
	if (InterfaceEnergyHome2.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.getAppliancesConfigurationsDemo(InterfaceEnergyHome2.BackMaxElettr);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackMaxElettr(null, err);
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
		InterfaceEnergyHome2.BackMaxElettr(val, null);
	}
}


//ritorna numero, con possibili decimali, che indica consumo in kWh (w ??), null se errore
InterfaceEnergyHome2.BackConsumoOdierno = function (result, err)
{
	if (InterfaceEnergyHome2.backConsumoOdierno != null)
	{
		if (err != null)
			InterfaceEnergyHome2.GestErrorEH("BackConsumoOdierno", err);
		retVal = null;
		if ((err == null) && (result != null))
		{
			if (result.list.length > 0)
				retVal = result.list[0];
		}
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackConsumoOdierno retVal = "  + retVal);
		InterfaceEnergyHome2.backConsumoOdierno(retVal);    
	}
}

InterfaceEnergyHome2.GetConsumoOdierno = function (backFunc)
{
	start = Main.dataAttuale.getTime();
	InterfaceEnergyHome2.backConsumoOdierno = backFunc;
	/**/
	if (InterfaceEnergyHome2.mode > 1) // solo se anche piattaforma
	{
		try
		{
			var res = InterfaceEnergyHome.objService.getAttributeData(InterfaceEnergyHome2.BackConsumoOdierno, 
				InterfaceEnergyHome2.PID_TOTALE, InterfaceEnergyHome2.CONSUMO, 
				start, start, InterfaceEnergyHome2.DAY, true, InterfaceEnergyHome2.DELTA);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackConsumoOdierno(null, err);
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
		InterfaceEnergyHome2.BackConsumoOdierno(val, null);
	}
}

//ritorna numero, con possibili decimali, che indica consumo in kWh, null se errore
InterfaceEnergyHome2.BackConsumoPrevisto = function (result, err)
{
	if (InterfaceEnergyHome2.backConsumoPrevisto != null)
	{	
		if (err != null)
			InterfaceEnergyHome2.GestErrorEH("BackConsumoPrevisto", err);
		retVal = null;
		if ((err == null) && (result != null))
				retVal = result;
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackConsumoPrevisto retVal = "  + retVal);
		InterfaceEnergyHome2.backConsumoPrevisto(retVal);    
	}
}

InterfaceEnergyHome2.GetConsumoPrevisto = function (backFunc)
{
	start = Main.dataAttuale.getTime();
	InterfaceEnergyHome2.backConsumoPrevisto = backFunc;
	/**/
	if (InterfaceEnergyHome2.mode > 1) // solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getForecast(InterfaceEnergyHome2.BackConsumoPrevisto, 
				InterfaceEnergyHome2.PID_TOTALE, InterfaceEnergyHome2.CONSUMO, start, InterfaceEnergyHome2.MONTH);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackConsumoPrevisto(null, err);
		}
	}
	else
	/**/
	{	
		// per test
		val =  ConsumoPrevisto;
		InterfaceEnergyHome2.BackConsumoPrevisto(val, null);
	}
}

//ritorna array di numeri, con possibili decimali, che indica consumo in kWh, null se errore
//ogni valore corrisponde ad un'ora, partendo da 0
InterfaceEnergyHome2.BackConsumoGiornaliero = function (result, err)
{
	
	if (InterfaceEnergyHome2.backConsumoGiornaliero!= null)
	{	
		if (err != null)
			InterfaceEnergyHome2.GestErrorEH("BackConsumoGiornaliero", err);
		retVal = null;
		if ((err == null) && (result != null))
			retVal = result.list;
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackConsumoGiornaliero retVal = "  + retVal);
		InterfaceEnergyHome2.backConsumoGiornaliero(retVal);    
	}
}

InterfaceEnergyHome2.GetConsumoGiornaliero = function (backFunc)
{
	InterfaceEnergyHome2.backConsumoGiornaliero = backFunc;
	start = new Date(Main.dataAttuale.getTime());
	start.setHours(0);
	end = Main.dataAttuale.getTime();
	/**/
	if (InterfaceEnergyHome2.mode > 1) // solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getAttributeData(InterfaceEnergyHome2.BackConsumoGiornaliero , 
				InterfaceEnergyHome2.PID_TOTALE,	InterfaceEnergyHome2.CONSUMO, start.getTime(), end, 
				InterfaceEnergyHome2.HOUR, true, InterfaceEnergyHome2.DELTA);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackConsumoGiornaliero (null, err);
		}
	}
	else
	/**/
	{		
		// per test, copio per il numero ore attuale
		hours = Main.dataAttuale.getHours();
		val = ConsumoGiornaliero;
		val.list = val.list.slice(0, hours);
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "GetConsumoGiornaliero");
		InterfaceEnergyHome2.BackConsumoGiornaliero(val, null);
	}
}

//ritorna array di numeri, con possibili decimali, che indica consumo in kWh
//ogni valore corrisponde ad un'ora, partendo da 0
InterfaceEnergyHome2.BackConsumoMedio = function (result, err)
{
	
	if (InterfaceEnergyHome2.backConsumoMedio != null)
	{	
		if (err != null)
			InterfaceEnergyHome2.GestErrorEH("BackConsumoMedio", err);
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
		Log.alert(80, InterfaceEnergyHome2.MODULE, "BackConsumoMedio val = "  + retVal);
		InterfaceEnergyHome2.backConsumoMedio(retVal);    
	}
}

InterfaceEnergyHome2.GetConsumoMedio = function (backFunc)
{
	weekDay = Main.dataAttuale.getDay() + 1; // js comincia da 0, java da 1
	InterfaceEnergyHome2.backConsumoMedio = backFunc;
	/**/
	if (InterfaceEnergyHome2.mode > 1) // solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getWeekDayAverage(InterfaceEnergyHome2.BackConsumoMedio , InterfaceEnergyHome2.PID_TOTALE, 
				InterfaceEnergyHome2.CONSUMO, weekDay);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackConsumoMedio (null, err);
		}
	}
	else
	/**/
	{		
		// per test
		val = ConsumoMedio;
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "GetConsumoMedio : val = " + val);
		InterfaceEnergyHome2.BackConsumoMedio(val, null);
	}
}

//ritorna numero, con possibili decimali, che indica consumo in kW, null se errore
InterfaceEnergyHome2.BackPotenzaAttuale = function (result, err)
{
	if (InterfaceEnergyHome2.backPotenzaAttuale != null)
	{
		if (err != null)
			InterfaceEnergyHome2.GestErrorEH("BackPotenzaAttuale", err);

		if ((err == null) && (result != null)){
			$.each(result.list,function(indice, elettrodom) {
				if (elettrodom["map"][InterfaceEnergyHome2.ATTR_APP_TYPE] == InterfaceEnergyHome2.SMARTINFO_APP_TYPE) {
					if (elettrodom["map"][InterfaceEnergyHome2.ATTR_APP_CATEGORY] == "12") {
						CostiConsumi.SmartInfo = elettrodom["map"];
						device_value = elettrodom["map"].device_value;
						if (device_value != undefined) {
							retVal = device_value.value.value;
						}
					}
				}
			});
		} else
			retVal = null;
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackPotenzaAttuale result = "  + retVal);
		InterfaceEnergyHome2.backPotenzaAttuale(retVal);    
	}
}

InterfaceEnergyHome2.GetPotenzaAttuale = function (backFunc)
{
	Log.alert(80,  InterfaceEnergyHome2.MODULE, "GetPotenzaAttuale");
	InterfaceEnergyHome2.backPotenzaAttuale = backFunc;
	if (InterfaceEnergyHome2.mode > 0)
	{
		try
		{
			//InterfaceEnergyHome.objService.getAttribute(InterfaceEnergyHome2.BackPotenzaAttuale, InterfaceEnergyHome2.POTENZA_TOTALE);
			InterfaceEnergyHome.objService.getAppliancesConfigurationsDemo(InterfaceEnergyHome2.BackPotenzaAttuale);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackPotenzaAttuale(null, err);
		}
	}
	else
	{		
		// per test
		PotenzaAttuale.value += 50;
		if (PotenzaAttuale.value > 5000)
			PotenzaAttuale.value = 0;
		InterfaceEnergyHome2.BackPotenzaAttuale(PotenzaAttuale, null);
	}
}


//ritorna array di strutture
InterfaceEnergyHome2.BackListaElettr = function (result, err)
{
	var smartInfo = null;
	
	if (InterfaceEnergyHome2.backListaElettr != null)
	{	
		if (err != null)
		{
			retVal = null;
			InterfaceEnergyHome2.GestErrorEH("BackListaElettr", err);
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
					if (res.map[InterfaceEnergyHome2.ATTR_APP_STATE_AVAIL] == undefined)
						res.map[InterfaceEnergyHome2.ATTR_APP_STATE_AVAIL] = false;
					// Nicola if ((res.map[InterfaceEnergyHome2.ATTR_APP_STATE_AVAIL] == true) ||
					//		(res.map[InterfaceEnergyHome2.ATTR_APP_TYPE] == InterfaceEnergyHome2.SMARTINFO_APP_TYPE))
					//{
						elem = new DatiElettr();
						elem.id = res.map[InterfaceEnergyHome2.ATTR_APP_PID]; 
						elem.nome = res.map[InterfaceEnergyHome2.ATTR_APP_NAME];
						elem.categoria = res.map[InterfaceEnergyHome2.ATTR_APP_CATEGORY];
						elem.locazione = res.map[InterfaceEnergyHome2.ATTR_APP_LOCATION] ;
						elem.avail = res.map[InterfaceEnergyHome2.ATTR_APP_AVAIL];
						elem.stato = res.map[InterfaceEnergyHome2.ATTR_APP_STATE];
						elem.icona = res.map[InterfaceEnergyHome2.ATTR_APP_ICON]; 
						elem.tipo = res.map[InterfaceEnergyHome2.ATTR_APP_TYPE]; 
						if (res.map[InterfaceEnergyHome2.ATTR_APP_VALUE] != undefined)
							elem.value = res.map[InterfaceEnergyHome2.ATTR_APP_VALUE].value.value;
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
						if (elem.tipo == InterfaceEnergyHome2.SMARTINFO_APP_TYPE)
						{
							smartInfo = elem;
							// salvo pid totale
							InterfaceEnergyHome2.PID_TOTALE = elem.id;
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
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackListaElettr val = "  + retVal);
		InterfaceEnergyHome2.backListaElettr(retVal);    
	}
}

InterfaceEnergyHome2.GetListaElettr = function (backFunc)
{
	InterfaceEnergyHome2.backListaElettr = backFunc;
	Log.alert(80, InterfaceEnergyHome2.MODULE, "GetListaElettr ");
	
	if (InterfaceEnergyHome2.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.getAppliancesConfigurationsDemo(InterfaceEnergyHome2.BackListaElettr);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackListaElettr(null, err);
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
		InterfaceEnergyHome2.BackListaElettr(val, null);
	}
}

//ritorna array di coppie di valori ["nome", "pid"]
InterfaceEnergyHome2.BackElettrStorico = function (result, err)
{
	if (InterfaceEnergyHome2.backElettrStorico != null)
	{	
		if (err != null)
		{
			retVal = null;
			InterfaceEnergyHome2.GestErrorEH("BackElettrStorico", err);
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
					retVal[i].nome = result.list[i].map[InterfaceEnergyHome2.ATTR_APP_NAME];
					retVal[i].pid = result.list[i].map[InterfaceEnergyHome2.ATTR_APP_PID];
					retVal[i].tipo = result.list[i].map[InterfaceEnergyHome2.ATTR_APP_TYPE];
				}
			}
		}
		Log.alert(80,  InterfaceEnergyHome2.MODULE, "BackElettrStorico val = "  + retVal);
		InterfaceEnergyHome2.backElettrStorico(retVal);    
	}	
}

InterfaceEnergyHome2.GetElettrStorico = function (backFunc)
{
	InterfaceEnergyHome2.backElettrStorico = backFunc;
	Log.alert(80, InterfaceEnergyHome2.MODULE, "GetElettrStorico");
	
	/**/
	if (InterfaceEnergyHome2.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.getAppliancesConfigurationsDemo(InterfaceEnergyHome2.BackElettrStorico);
		}
		catch (err)
		{
			InterfaceEnergyHome2.BackElettrStorico(null, err);
		}
	}
	else
	/**/
	{	
		// per test
		val = ListaElettr;
		InterfaceEnergyHome2.BackElettrStorico(val, null);
	}
}


//ritorna array di valori (costo o consumo)
InterfaceEnergyHome2.BackStorico = function (result, err)
{
	
	if (InterfaceEnergyHome2.backStorico != null)
	{	
		if (err != null)
			InterfaceEnergyHome2.GestErrorEH("BackStorico", err);
		retVal = null;
		if ((err == null) && (result != null))
			retVal = result.list;
		Log.alert(20,  InterfaceEnergyHome2.MODULE, "BackStorico retVal = "  + retVal);
		InterfaceEnergyHome2.backStorico(retVal);    
	}
}

InterfaceEnergyHome2.GetStorico = function (tipo, pid, dataInizio, dataFine, intervallo, backFunc)
{
	var paramTr, param1, param2;
	
	InterfaceEnergyHome2.backStorico = backFunc;
	Log.alert(80, InterfaceEnergyHome2.MODULE, "GetStorico");
	if (tipo == "Costo")
		param1 = InterfaceEnergyHome2.COSTO;
	else
		param1 = InterfaceEnergyHome2.CONSUMO;

	if (intervallo == 0)
	{
		param2 = InterfaceEnergyHome2.HOUR;
		dataInizio.setHours(0);
		dataFine.setHours(23);
		dataFine.setMinutes(30);
		paramTr = Tracing.QT_IERI;
	}
	else
		if (intervallo == 3)
		{
			param2 = InterfaceEnergyHome2.MONTH;
			paramTr = Tracing.QT_ANNO;
		}
		else
		{
			param2 = InterfaceEnergyHome2.DAY;
			if (intervallo == 1)
				paramTr = Tracing.QT_SETT;
			else
				paramTr = Tracing.QT_MESE;
		}
	Tracing.Trace(Tracing.HISTORY, Tracing.QUERY, paramTr, pid);
	/**/
	if (InterfaceEnergyHome2.mode > 1) // solo se anche piattaforma
	{
		try
		{
			InterfaceEnergyHome.objService.getAttributeData(InterfaceEnergyHome2.BackStorico, pid, param1, 
					dataInizio.getTime(), dataFine.getTime(), param2, true, InterfaceEnergyHome2.DELTA);

		}
		catch (err)
		{
			InterfaceEnergyHome2.BackStorico(null, err);
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
		
		Log.alert(20, InterfaceEnergyHome2.MODULE, "   diff = " + diff + "  giorni = " + g);
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
			InterfaceEnergyHome2.BackStorico(valP, null);
		}
		else
		**/
			InterfaceEnergyHome2.BackStorico(val, null);
	}
}
// solo per catch eccezione
InterfaceEnergyHome2.BackSendGuiLog = function(result, err)
{
	Log.alert(20, InterfaceEnergyHome2.MODULE, "SendGuiLog: eccezione " + err);
}

// solo se non in demo o simulazione
InterfaceEnergyHome2.SendGuiLog = function(logText)
{
	if (InterfaceEnergyHome2.mode > 0)
	{
		try
		{
			InterfaceEnergyHome.objService.sendGuiLog(InterfaceEnergyHome2.BackSendGuiLog, logText);
		}
		catch (err)
		{
			Log.alert(80, InterfaceEnergyHome2.MODULE, "SendGuiLog: " + logText);
		}
	}
}

InterfaceEnergyHome2.GetActualDate = function(backFunc)
{
	try
	{
		InterfaceEnergyHome.objService.currentTimeMillis(backFunc);
	}
	catch (e)
	{
		backFunc(backFunc, e)
	}
}

