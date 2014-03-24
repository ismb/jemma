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
	mode : 0, // 0 : simulazione, 1 : rete
	MODE_SIMUL : 0,
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
	WHITEGOOD_APP_TYPE : "com.indesit.ah.app.whitegood",
	SMARTINFO_APP_TYPE : "it.telecomitalia.ah.zigbee.metering",
	SMARTPLUG_APP_TYPE : "it.telecomitalia.ah.zigbee.smartplug",
	POTENZA_TOTALE : "TotalPower", //potenza totale consumata in casa 
	PRODUZIONE_TOTALE : "ProducedPower", //potenza istantanea generata
	RETE_TOTALE : "SoldPower", //potenza istantanea  venduta alla rete (meglio usare nella gui solo i precedenti due valori, e ricavare per differenza questo, così si garantisce che i valori sono coerenti anche se le richieste json partono in istanti differenti)
	PRESENZA_PRODUZIONE : "PeakProducedPower", //potenza di picco degli impianti fotovoltaici (vale 0 se l’utente non ha nessun impianto fotovoltaico) e deve essere aggiunta alla gui di configurazione
	CONSUMO : "ah.eh.esp.Energy",
	COSTO : "ah.eh.esp.EnergyCost",
	PRODUZIONE : "ah.eh.esp.ProducedEnergy",
	SOLD : "ah.eh.esp.SoldEnergy",
	LIMITI : "InstantaneousPowerLimit",
	AG_APP_EXCEPTION : "it.telecomitalia.ah.hac.ApplianceException",
	SERVER_EXCEPTION : "it.telecomitalia.ah.eh.esp.ESPException",

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