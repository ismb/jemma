var Tracing = {
		MODULE: "Tracing",
		IN: "IN",
		OUT: "OUT",
		QUERY: "QUERY",
		ERR: "ERROR",
		WARN: "WARN",
		CONFIG_HOME: "CONFIG_HOME",
		CONFIG_USER: "CONFIG_USER",
		CONFIG_APPLIANCES: "CONFIG_APPLIANCES",
		HOME: "HOME",
		COSTS: "COSTS",
		CONSUMPTIONS: "CONSUMPTIONS",
		APPLIANCES: "APPLIANCES",
		HISTORY: "HISTORY",
		OVERLOAD: "OVERLOAD",
		TARIFF: "TARIFF",
		INFO: "INFO",
		CONTACT: "CONTACT",
		FORUM: "FORUM",
		FORMS: "FORMS",
		REPORT: "REPORT",
		ERR_GENERIC: 1,
		QT_IERI: 1, // ieri 
		QT_SETT: 2, // ultima settimana
		QT_MESE: 3, // ultimo mese
		QT_ANNO: 4, // ultimo anno
		userAgent: null,
		sessionId: null,
		ipAddress: null,
		ultimaSezione: null 
}

Tracing.Init = function(sezione, ua) {
	Tracing.userAgent = ua;
	// per adesso il sessionId e' il time stamp dell'ora locale di sistema
	if (Tracing.sessionId == null)
		Tracing.sessionId = new Date().getTime();
	Tracing.ipAddress = window.location.hostname;
	Tracing.Trace(sezione, Tracing.IN, ua, null);
	Tracing.ultimaSezione = sezione;
	//console.log(20, Tracing.MODULE, "Init: ip = " + Tracing.ipAddress + " sessionId = " + Tracing.sessionId + " ua = " + ua);
}

// La sezione e' null nel caso di errore perche' potrei non sapere da quale
// sezione arriva l'errore
// memorizzo l'ultima sezione, cosi la inserisco nel caso di errore
// param c'e' solo in qualche caso, nel caso di errore e' il codice di errore
// param1 e' il messaggio nel caso di errore, il pid nella query dello storico
Tracing.Trace = function (sezione, mode, param, param1) {
	tmp = sezione;
	if (sezione != null)
		Tracing.ultimaSezione = sezione;
	else
		tmp = Tracing.ultimaSezione;
		
	testo = tmp + ":" + mode;
	if (param != null)
		testo += ":" + param;
	if (param1 != null)
	{
		// tolgo eventuali :
		param2 = param1.replace(/:/g, "-");
		testo += ":" + param2;
	}
	txt = Tracing.ipAddress + ":" + Tracing.sessionId + ":" + testo;
	//console.log(20, Tracing.MODULE , txt);
	InterfaceEnergyHome.SendGuiLog(txt);
} 