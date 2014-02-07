var Elettrodomestici = {
	MODULE : "Elettrodomestici ",
	TIMER_UPDATE_ELETTR : 5000,
	categorie : null,
	locazioni : null,
	infoDisp : null,
	numDisp : 0,
	indDispositivo : -1,
	timerElettr : null,
	statoDiscon : "Non Collegato",
	statoOn : "Acceso",
	statoOff : "Spento",
	htmlContent : "<div id='TitoloElettrodomestici'>Monitoraggio elettrodomestici</div>" +
			"<div id='ElettrodomesticiDiv'></div>"

};

Elettrodomestici.ExitElettrodomestici = function() {
	Log.alert(90, Elettrodomestici.MODULE, "ExitElettrodomestici");
	if (Elettrodomestici.timerElettr != null)
	{
		clearTimeout(Elettrodomestici.timerElettr);
		Elettrodomestici.timerElettr = null;
	}
	InterfaceEnergyHome.Abort();
}

Elettrodomestici.compareConsumo = function(a, b) {
    var aVal, bVal;
    
    if (a.device_value == "nd")
    {
        if (b.device_value == "nd")
            return 0;
        else
            return 1;
    }
    else
        if (b.device_value == "nd")
            return -1;
        else
        {
        // estraggo valore kw e ordino
            i = a.device_value.indexOf(" ");
            aVal = a.device_value.substr(0,i);
            j = b.device_value.indexOf(" ");
            bVal = b.device_value.substr(0,j); 
            Log.alert(120, Elettrodomestici.MODULE, "compareConsumo: a = " + aVal + " b = " + bVal);
            if (a.device_value <= b.device_value)
                return 1;
            else
                return -1;
        }
}

//	"name": "SmartPlug 1", 
//	"category": { "name": "Lampadina", "icon": "lampadina.png" }, 
//	"device_state": 1, 
//	"device_state_avail": "true", 
//	"availability": 2, 
//	"pid": "zigbee.000D6F0000099F6B", 
//	"device_status": 0, 
//	"type": "ZBPowerMeterSwitchDriverImpl", 
//	"icon": "lampadina.png", 
//	"device_value": "nd" ,
//	"costo" : da mettere

Elettrodomestici.VisElettrodomestici = function() {
	var iconaHtml = "", nomeHtml = "", statoHtml = "", consumoHtml = "", locazioneHtml = "";	
	var h, w, left, dist, imgStato, imgDisp, stato;
	var dimImg, dimDiv;

	// calcolo dimensione celle in base al numero di dispositivi
	h = $("#IconaDisp").height();
	w = $("#IconaDisp").width();
	
	dim = Math.round(w / Elettrodomestici.numDisp + 1);
	if (dim > h) 
		dim = h;
	dist = (w - (dim * Elettrodomestici.numDisp)) / (Elettrodomestici.numDisp + 1);
	left = dist;

	// prima cella con titolo?
	Log.alert(80, Elettrodomestici.MODULE, "VisElettrodomestici: dim = " + dim + " w = " + w + " h = " + h + " content w = " + $("#Content").width());
	for (i = 0; i < Elettrodomestici.numDisp; i++)
	{	
		imgDisp = Elettrodomestici.infoDisp[i].icona;
		imgDisp = imgDisp.substr(0, imgDisp.indexOf("."));
		
		if (Elettrodomestici.infoDisp[i].avail != 2)
		{
			imgStato = "sfondo_dev.png";
			imgDisp += "_grigio.png";
			stato = Elettrodomestici.statoDiscon;
			statoHtml += "<div class='StatoDispTd' style='position:absolute;left:" + left + "px'><div class='StatoDispDiscon'>Disconnesso</div></div>";
		}
		else
	  		if ((Elettrodomestici.infoDisp[i].stato == 1) || (Elettrodomestici.infoDisp[i].stato == 4) || (Elettrodomestici.infoDisp[i].stato == 3))
			{
      	      	imgStato = "sfondo_dev_on.png";
				imgDisp += ".png";
				stato = Elettrodomestici.statoOn;
				statoHtml += "<div class='StatoDispTd' style='position:absolute;left:" + left + "px'><div class='StatoDispOn'>Acceso</div></div>";
			}
            	else
            	    	if (Elettrodomestici.infoDisp[i].stato == 0)
            	    	{
					imgStato = "sfondo_dev_off.png";
				    	imgDisp += "_grigio.png";
					stato = Elettrodomestici.statoOff;
					statoHtml += "<div class='StatoDispTd' style='position:absolute;left:" + left + "px'><div class='StatoDispOff'>Spento</div></div>";
				}
               	    	else
				{
                    		imgStato = "sfondo_dev.png";
		    		   	imgDisp += "_grigio.png";
					stato = Elettrodomestici.statoOff;
					statoHtml += "<div class='StatoDispTd' style='position:absolute;left:" + left + "px'><div class='StatoDispDiscon'> Disconnesso</div></div>";
			    	}
		Log.alert(80, Elettrodomestici.MODULE, "VisElettrodomestici: imgDisp = " + imgDisp);
		iconaHtml += "<div class='IconaDispTd'><img class='BgIconaStato' style='position:absolute;left:" + left + "px' src='Resources/Images/" + imgStato + "'>" +
				 "<img class='IconaDispImg' style='position:absolute;left:" + (left+dim*0.3) + "px;top:" + (dim*0.35) + "px' src='Resources/Images/Devices/" + imgDisp + "'></div>";
		nomeHtml += "<div class='NomeDispTd' style='position:absolute;left:" + left + "px' >" + Elettrodomestici.infoDisp[i].nome + "</div>";
		consumo = Math.round(Elettrodomestici.infoDisp[i].value);
		if (consumo == null)
			consumo = "";
		else
			consumo += " W";
		consumoHtml += "<div class='ConsumoDispDiv' style='position:absolute;left:" + left + "px'>" + consumo + "</div>";
		loc = "";
		if (Elettrodomestici.infoDisp[i].locazione != null)
		{
			if (Elettrodomestici.locazioni != null)
				loc = Elettrodomestici.locazioni[Elettrodomestici.infoDisp[i].locazione];
		}
		locazioneHtml += "<div class='LocazioneDiv' style='position:absolute;left:" + left + "px'>" + loc + "</div>";
		left = left + dim + dist;
	}
	$("#IconaDisp").html(iconaHtml);
	$(".BgIconaStato").width(dim);
	$(".BgIconaStato").height(dim);
	$(".IconaDispImg").width(dim * 0.4);
	$(".IconaDispImg").height(dim * 0.4);

	$("#NomeDisp").html(nomeHtml);
	$(".NomeDispTd").width(dim);
	$(".NomeDispTd").height($("#NomeDisp").height());

	$("#StatoDisp").html(statoHtml);
	$(".StatoDispTd").width(dim);
	$(".StatoDispTd").height($("#StatoDisp").height());

	$("#ConsumoDisp").html(consumoHtml);
	$(".ConsumoDispDiv").width(dim);
	$(".ConsumoDispDiv").height($("#ConsumoDisp").height());

	//$("#TitoloLocazione").html("<div class='TitoloTd' colspan=" + Elettrodomestici.numDisp + "><div>Dove si trova</div></div>");
	$("#Locazione").html(locazioneHtml );
	$(".LocazioneDiv").width(dim);
	$(".LocazioneDiv").height($("#Locazione").height());
}



/*****************************************************
 * Legge le informazioni sui devices e le visualizza
 *****************************************************/
Elettrodomestici.DatiElettrodomestici = function(lista) {
	
	Elettrodomestici.infoDisp = lista;

    	// ordino array in base al consumo attuale
    	//Elettrodomestici.infoDisp.sort(Elettrodomestici.compareConsumo);
	if ((lista != null) && (lista.length > 0))
	{
			$("#ElettrodomesticiDiv").html("<div id='IconaDisp'></div><div id='NomeDisp'></div><div id='NomeDisp'></div><div id='StatoDisp'></div><div id='TitoloLocazione'>Dove si trova</div><div id='Locazione'></div><div id='TitoloConsumoDisp'>Sta consumando</div><div id='ConsumoDisp'></div>");
    		// se l'ultimo elemento e' lo smartInfo non lo considero
			// lo inserisco solo perche' in alcuni casi puo; servire
			if (lista[lista.length-1].tipo == InterfaceEnergyHome.SMARTINFO_APP_TYPE)
				Elettrodomestici.numDisp = lista.length - 1;
			else
				Elettrodomestici.numDisp = lista.length;
    		Log.alert(80, Elettrodomestici.MODULE, "Trovati " + Elettrodomestici.numDisp + " dispositivi");
    		Elettrodomestici.VisElettrodomestici();
	}		
    else
    {
		Elettrodomestici.numDisp = 0;
		$("#ElettrodomesticiDiv").html("<div id='ElettrodomesticiVuoto'> Nessun elettrodomestico trovato</div>")
    }
	if (Elettrodomestici.timerElettr  == null)
		Elettrodomestici.timerElettr = setInterval("Elettrodomestici.GetElettrodomestici()", Elettrodomestici.TIMER_UPDATE_ELETTR);
}    

Elettrodomestici.GetElettrodomestici = function() {
	InterfaceEnergyHome.GetListaElettr(Elettrodomestici.DatiElettrodomestici);
}


Elettrodomestici.DatiLocazioni= function(lista) {
	Elettrodomestici.locazioni = lista;
	InterfaceEnergyHome.GetListaElettr(Elettrodomestici.DatiElettrodomestici);
}

Elettrodomestici.GestElettrodomestici = function() {
	// legge i dispositivi 
   	
	Log.alert(80, Elettrodomestici.MODULE, "Elettrodomestici.GestElettrodomestici");
	$("#Content").html(Elettrodomestici.htmlContent);
	// la prima volta leggo l'elenco delle locazioni perche' mi verra' dato solo il pid della locazione
	if (Elettrodomestici.locazioni == null)
		InterfaceEnergyHome.GetLocazioni(Elettrodomestici.DatiLocazioni);
	else	
		Elettrodomestici.GetElettrodomestici();
	

}

