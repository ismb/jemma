// leggo la data da AG la prima volta e memorizzo la differenza rispetto all'ora di sistema
// poi ogni minuto leggo l'ora di sistema e aggiungo la differenza
var GestDate = {
    MODULE : "GestDate",
    diffTime : 0,
    actualDate : null, // in milliseconds
    timerDate : null
}

// la prima volta legge l'ora attuale poi imposta un timer che ogni minuto va a rileggerlo
// se non funziona prendo l'ora di sistema, per adesso
GestDate.InitActualDate = function()
{
   	InterfaceEnergyHome.GetActualDate(GestDate.BackActualDate);
    GestDate.timerDate = setInterval("GestDate.UpdateDate()", 60000);
}

GestDate.GetActualDate = function()
{
	if (GestDate.actualDate == null)
		return new Date(); // se non e' ancora arrivata callback la prima volta
	return new Date(GestDate.actualDate);
}

// se AG non risponde per il momento prendo data di sistema
GestDate.BackActualDate = function(epoch, e)
{
	if (epoch == null || e != null)
	{
		GestDate.actualDate = (new Date()).getTime();
		GestDate.diffTime = 0;
		Log.alert(120, this.MODULE, "Ora di sistema = " + GestDate.actualDate.toString());
	}
	else
	{
		// memorizzo differenza rispetto ad ora di sistema
		GestDate.actualDate = epoch;
		GestDate.diffTime = epoch - (new Date()).getTime();
		Log.alert(120, this.MODULE, "Ora AG = " + GestDate.actualDate.toString());
	}
}
// chiede ora ad AG
GestDate.UpdateDate = function()
{
	GestDate.actualDate = (new Date()).getTime() + GestDate.diffTime; 
}
