// leggo la data da AG la prima volta e memorizzo la differenza rispetto all'ora di sistema
// poi ogni minuto leggo l'ora di sistema e aggiungo la differenza
var GestDate = {
    MODULE : "GestDate",
    initCallback : null,
    actualDate : null, // in milliseconds
    timerDate : null,
    DSTMarzo : false,
    DSTOttobre : false
}

// la prima volta legge l'ora attuale poi imposta un timer che ogni minuto va a rileggerlo
// se non funziona prendo l'ora di sistema, per adesso
GestDate.InitActualDate = function(callback){
	/* Calcolo i giorni DST */
	
	/* Marzo */
	
	dstdate = new Date(2012,2,31,0,1);
	tmpday = dstdate.getDay();
	dstdate.setDate(dstdate.getDate()-tmpday);
	GestDate.DSTMarzoDate = dstdate.toLocaleDateString();
	
	dstdate = new Date(2012,9,31,0,1);
	tmpday = dstdate.getDay();
	dstdate.setDate(dstdate.getDate()-tmpday);
	GestDate.DSTOttobreDate = dstdate.toLocaleDateString();
	
	GestDate.initCallback = callback;
   	
	if (InterfaceEnergyHome.mode != 0){
		InterfaceEnergyHome.GetActualDate(GestDate.BackActualDate);
	} else {
   		//initDataSimul = new Date(2012,2,25,5,0);
		//initDataSimul = new Date(2012,5,21,18,59);
		//initDataSimul = new Date(2012,9,28,5,0);
   		initDataSimul = DataSim;
   		
   		GestDate.BackActualDate(initDataSimul.getTime());
   	}
}

GestDate.GetActualDate = function(){
	return GestDate.actualDate;
	
}

GestDate.BackActualDate = function(val){
	if (val == null){
		val = 0; 
	}
	
	GestDate.actualDate = new Date(val);
		
	if (GestDate.timerDate == null){
		
		if (GestDate.actualDate.toLocaleDateString() == GestDate.DSTMarzoDate)
	   		GestDate.DSTMarzo = true;
	   	if (GestDate.actualDate.toLocaleDateString() == GestDate.DSTOttobreDate)
	   		GestDate.DSTOttobre = true; 
		GestDate.timerDate = setInterval("GestDate.UpdateDate()", 60000);
		if (GestDate.initCallback != null)
		{
			GestDate.initCallback();
			GestDate.initCallback = null; // la richiamo solo la prima volta
		}
	}
}

// chiede ora ad AG
GestDate.UpdateDate = function(){
	if (InterfaceEnergyHome.mode != 0)
	   InterfaceEnergyHome.GetActualDate(GestDate.BackActualDate);
	else
	   GestDate.actualDate.setTime(GestDate.actualDate.getTime()+60000);
		
}
