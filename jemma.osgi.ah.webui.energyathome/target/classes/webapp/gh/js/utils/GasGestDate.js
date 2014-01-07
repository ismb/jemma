// leggo la data da AG la prima volta e memorizzo la differenza rispetto all'ora di sistema
// poi ogni minuto leggo l'ora di sistema e aggiungo la differenza
var GasGestDate = {
    MODULE : "GasGestDate",
    initCallback : null,
    actualDate : null, // in milliseconds
    timerDate : null
};

// la prima volta legge l'ora attuale poi imposta un timer che ogni minuto va a rileggerlo
// se non funziona prendo l'ora di sistema, per adesso
GasGestDate.InitActualDate = function(callback)
{
	GasGestDate.initCallback = callback;
   	GasInterfaceEnergyHome.GetActualDate(GasGestDate.BackActualDate);
};

GasGestDate.GetActualDate = function()
{
	return GasGestDate.actualDate;
	
};

GasGestDate.BackActualDate = function(val)
{
	if (val == null)
		val = 0; 
	
	GasGestDate.actualDate = new Date(val);
		
	if (GasGestDate.timerDate == null)
		GasGestDate.timerDate = setInterval("GasGestDate.UpdateDate()", 60000);
		if (GasGestDate.initCallback != null)
		{
			GasGestDate.initCallback();
			GasGestDate.initCallback = null; // la richiamo solo la prima volta
		}
		
};

// chiede ora ad AG
GasGestDate.UpdateDate = function()
{
	GasInterfaceEnergyHome.GetActualDate(GasGestDate.BackActualDate);
};
