var MainHome = {
	MODULE : "MainHome",
	potenzaAttuale: null,
	consumoGiornoPrec: null
};

MainHome.SetConsumoEnergy = function() {
	if (MainHome.potenzaAttuale == null)
	{
		val = 0;
		$("#ElettricitaVal").html(Msg.home["datoNonDisponibile"]);
	}
	else
	{
		val = MainHome.potenzaAttuale;
		$("#ElettricitaVal").html(MainHome.potenzaAttuale + " kWh");
	}
};

MainHome.SetConsumoGas = function() {
	if (MainHome.consumoGiornoPrec == null)
	{
		val = 0;
		$("#GasVal").html(Msg.home["datoNonDisponibile"]);
	}
	else
	{
		val = MainHome.consumoGiornoPrec;
		$("#GasVal").html(MainHome.consumoGiornoPrec + " smc");
	}
};

MainHome.SetConsumoAcqua = function() {
	$("#AcquaVal").html("347 mc");
};

MainHome.DatiPotenzaAttuale = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	Log.alert(80, MainHome.MODULE, "DatiPotenzaAttuale val = " + val);
	
	MainHome.potenzaAttuale = val;
	// viene cambiato perche' e` una versione simulata
	MainHome.potenzaAttuale = 200;
	
	MainHome.SetConsumoEnergy();
};

MainHome.DatiGasAttuale = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	Log.alert(80, MainHome.MODULE, "DatiGasAttuale val = " + val);
	
	MainHome.consumoGiornoPrec = val;
	
	MainHome.SetConsumoGas();
};


MainHome.onLoad = function() 
{
//	// se c'e' parametro cambio livello log
//	var qs = new Querystring();
//	var level = qs.get("level", "20");
//	Log.setLevel(parseInt(level));
//	
	$(document).ready(function() {
//		GasMain.userAgent = navigator.userAgent;
//		
//		// Parse the current page's querystring
//	    var qs = new Querystring();
//	    
//	    var ids = new Array();
//	    var mode = qs.get("mode", "");
//	    if ((mode != null) && (mode != "")) {
//		    if (mode == "simul")
//	    		GasInterfaceEnergyHome.mode = 0;
//		    else if (mode == "demo")
//		    	GasInterfaceEnergyHome.mode = 1;
//		    else if (mode == "cost")
//		    	GasInterfaceEnergyHome.mode = 3;
//		    else 
//		    	GasInterfaceEnergyHome.mode = 2;
//	    } else {
//	    	GasInterfaceEnergyHome.mode = 2;
//	    }
//				
//		showSpinner();

		// inizializzo connessione ad AG
		if (GasInterfaceEnergyHome.Init() == null)
			Log.alert(20, MainHome.MODULE, "MainHome.onLoad: errore in connessione servizio");

//		GasMain.dataMinima = new Date("January 1, 2010 00:00:00").getTime();
//		GasGestDate.InitActualDate(GasMain.InitValue);
		GasInterfaceEnergyHome.GetPotenzaAttuale(MainHome.DatiPotenzaAttuale);
		GasInterfaceEnergyHome.GetPotenzaAttuale(MainHome.DatiGasAttuale);
		MainHome.SetConsumoAcqua();
	});
};

MainHome.onUnload = function()
{
//	if (GasGestDate.timerDate != null)
//		clearTimeout(GasGestDate.timerDate);
	Log.alert(20, MainHome.Module, "MainHome.onUnload");
	// faccio out da sezione attuale
//	GasTracing.Trace(null, GasTracing.OUT, null, null);
//	GasTracing.Trace(GasTracing.HOME, GasTracing.OUT, null, null);
};