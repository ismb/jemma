// modulo per generate data corretta se non la ricevo dal sistem
// Anzichè usare new Date per avere l'ora attuale si richiama GestDate.GetActualDate() per avere data e ora attuale
// Leggo la data da internet solo su chiamata della InitActualDate
// Aggiorna la data ogni minuto

var GestDate = {
    MODULE : "GestDate",
    systemTime : true,
    actualDate : null,
    timerDate : null
}

GestDate.InitActualDate = function()
{
    if (GestDate.timerDate != null)
        clearTimeout(GestDate.timerDate);
    // potrebbe essere diverso da 0 perchè acceso da un po'
    if ((new Date()).getTime() < new Date("Apr 30 2011 23:59:00").getTime()) // Feb 01 1970
    {
        GestDate.systemTime = false;
        GestDate.actualDate = InterfaceEnergyHome.GetActualDate();
        //GestDate.actualDate = new Date("May 3 2011 15:00:00");
        Log.alert(60, this.MODULE, "Ora non presente: la leggo = " + GestDate.actualDate.toString());
        GestDate.timerDate = setInterval("GestDate.UpdateDate()", 60000);
    }
    else
        Log.alert(60, this.MODULE, "Ora di sistema presente");
}


GestDate.GetActualDate = function()
{
    if (GestDate.systemTime)
        return new Date();
    else
    {
        if (GestDate.actualDate == null)
            GestDate.InitActualDate();
        return new Date(GestDate.actualDate.getTime());
    }
}

GestDate.UpdateDate = function()
{
    GestDate.actualDate.setMinutes(GestDate.actualDate.getMinutes()+1);
    Log.alert(120, this.MODULE, "Update date = " + GestDate.actualDate.toString());
}