var GasMain = {
	MODULE : "GasMain",
	SCREEN_LARGE : 1,	 // screen normale PC (oltre 1000x1000)
	SCREEN_SMALL : 2,  // screen iPad, iPhone
	userAgent : null,
	deviceSize : 0,
	screenRatio : 1,
	screenW : -1,
	screenH : -1,
	dataAttuale : null,
	dataMinima : null, // data sotto la quale considero errore di data
	timerTimeout : null,
	imgDisp : null,
	contatore : null // tipo contatore (0=3kW, 1=4.5kW, 2=6kW)
};

GasMain.aggiornaTimestamp = function() {
	GasMain.dataAttuale = GasGestDate.GetActualDate();
	// se data inferiore 1-1-2010 segnalo errore
	if (GasMain.dataAttuale.getTime() < GasMain.dataMinima)
		GasMain.VisError(GasInterfaceEnergyHome.ERR_CONN_AG);
	$("#Data").html(Utils.FormatDate(GasMain.dataAttuale, 2));
	$("#Ora").html(Utils.FormatDate(GasMain.dataAttuale, 3));
};

// ripulisco area che visualizza i messaggi di errore
GasMain.ResetError = function()
{
	$("#ErrorMsgDIV").text("");
};
// Visualizzo messaggio di errore
GasMain.VisError = function(err)
{
	$("#ErrorMsgDIV").text(Msg.visErr[err]);
};

GasMain.PowerLimitCb = function(val)
{
	GasMain.contatore = 0;
	// se null metto il default
	if (val == null)
		val = GasDefine.home["contatoreDefault"];
	// converto valore massimo contatore in indice 0:2 per gli array per la definizione dei limiti
	for (i = 0; i < GasDefine.home["tipoContatore"].length; i++)
		if (GasDefine.home["tipoContatore"][i] == val)
		{
			GasMain.contatore = i;
			break;
		}
	
	Log.alert(20, GasMain.MODULE, "Power limit = " + GasMain.contatore);
	GasMenu.Init('GasMainMenu', 'ContentMenu');
};

// dopo aver inizializzato l'ora fa il resto delle inizializzazioni
GasMain.InitValue = function() 
{
	GasMain.aggiornaTimestamp();
	// ogni minuto aggiorno data
	GasMain.timerTimeout = setInterval(function() {GasMain.aggiornaTimestamp();}, 60000);
	GasTracing.Init(GasTracing.HOME, GasMain.userAgent);
	
	// legge tipo contatore
	GasInterfaceEnergyHome.GetPowerLimit(GasMain.PowerLimitCb);
};

GasMain.onLoad = function() 
{
	var imgW, imgH;
	
	// se c'e' parametro cambio livello log
	var qs = new Querystring();
	var level = qs.get("level", "20");
	Log.setLevel(parseInt(level));
	
	$(document).ready(function() {
		GasMain.userAgent = navigator.userAgent;
		
		// Parse the current page's querystring
	    var qs = new Querystring();
	    
	    var ids = new Array();
	    var mode = qs.get("mode", "");
	    if ((mode != null) && (mode != "")) {
		    if (mode == "simul")
	    		GasInterfaceEnergyHome.mode = 0;
		    else if (mode == "demo")
		    	GasInterfaceEnergyHome.mode = 1;
		    else if (mode == "cost")
		    	GasInterfaceEnergyHome.mode = 3;
		    else 
		    	GasInterfaceEnergyHome.mode = 2;
	    } else {
	    	GasInterfaceEnergyHome.mode = 2;
	    }
				
		showSpinner();
		// se non precarico l'immagine di un elettrodomestico non funzionano gli
		// elettrodomestici
//	    GasMain.imgDisp = new Image();
//		GasMain.imgDisp.src = GasDefine.home["defaultDispImg"];
	
		// inizializzo connessione ad AG
		if (GasInterfaceEnergyHome.Init() == null)
			Log.alert(20, GasMain.MODULE, "onLoad: errore in connessione servizio");

		GasMain.dataMinima = new Date("January 1, 2010 00:00:00").getTime();
		GasGestDate.InitActualDate(GasMain.InitValue);
		
	});
};

GasMain.onUnload = function()
{
	if (GasGestDate.timerDate != null)
		clearTimeout(GasGestDate.timerDate);
	Log.alert(20, GasMain.Module, "GasMain.onUnload");
	// faccio out da sezione attuale
	GasTracing.Trace(null, GasTracing.OUT, null, null);
	GasTracing.Trace(GasTracing.HOME, GasTracing.OUT, null, null);
};