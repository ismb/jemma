var Main = {
	MODULE : "Main",
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
}

Main.aggiornaTimestamp = function() {
	Main.dataAttuale = GestDate.GetActualDate();
	// se data inferiore 1-1-2010 segnalo errore
	if (Main.dataAttuale.getTime() < Main.dataMinima)
		Main.VisError(InterfaceEnergyHome.ERR_CONN_AG);
	$("#Data").html(Utils.FormatDate(Main.dataAttuale, 2));
	$("#Ora").html(Utils.FormatDate(Main.dataAttuale, 3));
}

// ripulisco area che visualizza i messaggi di errore
Main.ResetError = function()
{
	$("#ErrorMsgDIV").text("");
}
// Visualizzo messaggio di errore
Main.VisError = function(err)
{
	$("#ErrorMsgDIV").text(Msg.visErr[err]);
}

Main.PowerLimitCb = function(val)
{
	Main.contatore = 0;
	// se null metto il default
	if (val == null)
		val = Define.home["contatoreDefault"];
	// converto valore massimo contatore in indice 0:2 per gli array per la definizione dei limiti
	for (i = 0; i < Define.home["tipoContatore"].length; i++)
		if (Define.home["tipoContatore"][i] == val)
		{
			Main.contatore = i;
			break;
		}
	
	Log.alert(20, Main.MODULE, "Power limit = " + Main.contatore);
	Menu.Init('MainMenu', 'ContentMenu');
}

// dopo aver inizializzato l'ora fa il resto delle inizializzazioni
Main.InitValue = function() 
{
	Main.aggiornaTimestamp();
	// ogni minuto aggiorno data
	Main.timerTimeout = setInterval(function() {Main.aggiornaTimestamp();}, 60000);
	Tracing.Init(Tracing.HOME, Main.userAgent);
	
	// legge tipo contatore
	InterfaceEnergyHome.GetPowerLimit(Main.PowerLimitCb);
}

Main.onLoad = function() 
{
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
		    if (mode == "simul")
	    		InterfaceEnergyHome.mode = 0;
		    else if (mode == "demo")
		    	InterfaceEnergyHome.mode = 1;
		    else if (mode == "cost")
		    	InterfaceEnergyHome.mode = 3;
		    else 
		    	InterfaceEnergyHome.mode = 2;
	    }
	    else {
	    	InterfaceEnergyHome.mode = 2;
	    }
				
		showSpinner();
		// se non precarico l'immagine di un elettrodomestico non funzionano gli
		// elettrodomestici
	    Main.imgDisp = new Image();
		Main.imgDisp.src = Define.home["defaultDispImg"];
	
		// inizializzo connessione ad AG
		if (InterfaceEnergyHome.Init() == null)
			Log.alert(20, Main.MODULE, "onLoad: errore in connessione servizio");

		Main.dataMinima = new Date("January 1, 2010 00:00:00").getTime();
		GestDate.InitActualDate(Main.InitValue);
		
	});
}

Main.onUnload = function()
{
	if (GestDate.timerDate != null)
		clearTimeout(GestDate.timerDate);
	Log.alert(20, Main.Module, "Main.onUnload");
	// faccio out da sezione attuale
	Tracing.Trace(null, Tracing.OUT, null, null);
	Tracing.Trace(Tracing.HOME, Tracing.OUT, null, null);
}