var ConfMain = {
	MODULE : "Main",
	SCREEN_LARGE : 1, // screen normale PC (oltre 1000x1000)
	SCREEN_MEDIUM : 2, // screen con dim. max sotto 700
	SCREEN_SMALL : 3, // screen con dim. max sotto 500
	userAgent : null,
	deviceSize : 0,
	screenRatio : 1,
	screenW : -1,
	screenH : -1,
	dataAttuale : null
}

ConfMain.aggiornaTimestamp = function() {
	ConfMain.dataAttuale = GestDate.GetActualDate();
	$("#Data").html(Utils.FormatDate(ConfMain.dataAttuale, 2));
	$("#Ora").html(Utils.FormatDate(ConfMain.dataAttuale, 3));
}

ConfMain.onLoad = function() {
	// se c'e' parametro cambio livello log
	var qs = new Querystring();
	var level = qs.get("level", "20");
	Log.setLevel(parseInt(level));
	//console.log(20, "html", "Device size = " + ConfMain.deviceSize);

	$(document).ready(function() {
				ConfMain.userAgent = navigator.userAgent;

				// Parse the current page's querystring
				var qs = new Querystring()
				var ids = new Array();
				var mode = qs.get("mode", "");
				if ((mode != null) && (mode != "")) {
					if (mode == "simul")
						InterfaceEnergyHome.mode = 0;
					else if (mode == "demo")
						InterfaceEnergyHome.mode = 1;
					else
						InterfaceEnergyHome.mode = 2;
				} else {
					InterfaceEnergyHome.mode = 2;
				}

				InterfaceEnergyHome.Init();
				GestDate.InitActualDate();
				Tracing.Init(Tracing.CONFIG_HOME, ConfMain.userAgent);

				InterfaceEnergyHome.addListener("service.error", Configurazione.HandleError);

				Menu.Init('MainMenu', 'ContentMenu');

				ConfMain.aggiornaTimestamp();

				// verifico se un id utente e' gia' stato introdotto, nel qual caso passo subito alla pagina degli elettrodomestici
				// altrimenti aspetto che mi venga inserito e disabilito la configurazione degli elettrodomestici
				// InterfaceEnergyHome.getHapConnectionId(Configurazione.IdUtenteCb);

				hideSpinner();

				// ogni minuto aggiorno data
				timerTimeout = setInterval(function() {
					ConfMain.aggiornaTimestamp();
				}, 60000);
			});
}
/*

ConfMain.onLoadConfParam = function() {

	$(document).ready(function() {
				ConfMain.userAgent = navigator.userAgent;

				// Parse the current page's querystring
				var qs = new Querystring()
				var ids = new Array();
				var mode = qs.get("mode", "");
				InterfaceEnergyHome.mode = 2;

				InterfaceEnergyHome.Init();
				GestDate.InitActualDate();
				Tracing.Init(Tracing.CONFIG_HOME, ConfMain.userAgent);

				InterfaceEnergyHome.addListener("service.error", Configurazione.HandleError);

				Menu.Init('MainMenu', 'ContentMenu');
				readPropFile();

				ConfMain.aggiornaTimestamp();

				// verifico se un id utente e' gia' stato introdotto, nel qual caso passo subito alla pagina degli elettrodomestici
				// altrimenti aspetto che mi venga inserito e disabilito la configurazione degli elettrodomestici
				// InterfaceEnergyHome.getHapConnectionId(Configurazione.IdUtenteCb);

				hideSpinner();

				// ogni minuto aggiorno data
				timerTimeout = setInterval(function() {
					ConfMain.aggiornaTimestamp();
				}, 60000);
			});
	
	$("#ContentMain").hide();
	$("#Content").hide();
	$("#MainMenu").hide();
	$("#ContentMenu").hide();
}*/

ConfMain.onUnload = function() {
	if (GestDate.timerDate != null)
		clearInterval(GestDate.timerDate);

	InterfaceEnergyHome.removeListener("service.error", Configurazione.HandleError);

	Tracing.Trace(null, Tracing.OUT, null, null);
	Tracing.Trace(Tracing.CONFIG_HOME, Tracing.OUT, null, null);
}
