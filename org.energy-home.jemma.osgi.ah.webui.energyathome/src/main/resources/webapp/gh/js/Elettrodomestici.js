var Elettrodomestici = {
	MODULE : "Elettrodomestici ",
	TIMER_UPDATE_ELETTR : 4000,
	categorie : null,
	locazioni : null,
	infoDisp : null,
	numDisp : 0,
	indDispositivo : -1,
	timerElettr : null,
	defaultImg : Define.home["defaultDispImg"],
	suffixStato : ["_acceso.png", "_spento.png", "_disconnesso.png"],
	classStato : ["DispStatoAcceso", "DispStatoSpento", "DispStatoDisconn"],
	classNome : ["DispNome", "DispNome", "DispNomeDisconn"],
	classValue : ["DispValueAcceso", "DispValueSpento", "DispValueDisconn"],
		htmlContent : "<div class='ContentTitle' >" + Msg.home["titoloDispositivi"] + "</div>" +
			"<div id='ElettrodomesticiDiv'><div id='DispositiviDiv'></div>" +
			"<div id='SepConsumo'><span id='SepConsumo1'></span><span id='TitoloSepConsumo'>" + 
			Msg.home["titoloConsumoDisp"] + "</span><span id='SepConsumo2'></span></div>" +
			"<div id='ConsumoDiv'></div><div id='SepLocazione'><span id='SeparLocazione1'></span>" +
			"<span id='TitoloSeparLocazione'>" + Msg.home["titoloLocDisp"] + "</span><span id='SeparLocazione2'></span></div>" +
			"<div id='LocazioneDiv'></div></div>"
};

Elettrodomestici.ExitElettrodomestici = function() {
	Log.alert(90, Elettrodomestici.MODULE, "ExitElettrodomestici");
	hideSpinner();
	Main.ResetError();
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
	var imgH, imgW;
    
	imgH = Main.imgDisp.height;
	imgW = Main.imgDisp.width;
	rapp = imgW / imgH;
	dispositiviHtml = "";
	consumoHtml = "";
	locazioneHtml = "";
	// creo prima html poi metto dimensioni e distanze
	for (i = 0; i < Elettrodomestici.numDisp; i++)
	{	
		// metto icona in base allo stato
		imgDisp = Elettrodomestici.infoDisp[i].icona;
		imgDisp = imgDisp.substr(0, imgDisp.indexOf("."));
		if (Elettrodomestici.infoDisp[i].avail != 2)
			indStato = 2;
		else
			if ((Elettrodomestici.infoDisp[i].stato == 1) || (Elettrodomestici.infoDisp[i].stato == 4) || (Elettrodomestici.infoDisp[i].stato == 3))
				indStato = 0;
			else
				indStato = 1;
		imgSrc = DefinePath.imgDispPath + imgDisp + Elettrodomestici.suffixStato[indStato];
		stato = Msg.home.statoDisp[indStato];
		dispositiviHtml += "<div id='DispElem" + i + "' class='DispElem' >" +
			"<img id='DispImg" + i + "' width='" + imgW + "px' height='" + imgH + "px' src='" + imgSrc + "' class='DispImg'>" +
			"<div id='DispNome" + i + "' class='" + Elettrodomestici.classNome[indStato] + "'>" + Elettrodomestici.infoDisp[i].nome + "</div>" +
			"<div id='DispStato" + i + "' class='" + Elettrodomestici.classStato[indStato] + "'>" + stato + "</div></div>";
		
		// metto consumo
		consumo = Math.round(Elettrodomestici.infoDisp[i].value);
		if ((indStato == 2) || (consumo == null))
			consumo = "0 W";
		else
			consumo += " W";
		consumoHtml += "<div id='DispConsumo" + i + "' class='" + Elettrodomestici.classValue[indStato] + "'>" + consumo + "</div>";
		
		// metto locazione
		loc = "";
		if (Elettrodomestici.infoDisp[i].locazione != null)
		{
			if (Elettrodomestici.locazioni != null)
			{
				loc = Elettrodomestici.locazioni[Elettrodomestici.infoDisp[i].locazione];
				loc = Lang.Convert(loc, Msg.locazioni); 
			}
		}
		Log.alert(80, Elettrodomestici.MODULE, "VisElettrodomestici : nome = " +  Elettrodomestici.infoDisp[i].nome +
						" loc = " + loc);
		locazioneHtml += "<div id='DispLoc" + i + "' class='DispLocazione'>" + loc + "</div>";
		left = left + w + dist;
		
	}	
	$("#DispositiviDiv").html(dispositiviHtml);
	$("#ConsumoDiv").html(consumoHtml);
	$("#LocazioneDiv").html(locazioneHtml);
	
	coord = Utils.ResizeImg("DispositiviDiv", Define.home["defaultDispImg"], false, 1, 9);
	h = $("#DispositiviDiv").height();
	w = Math.round(h * rapp);
	dist = Math.round(($("#DispositiviDiv").width() - (w * Elettrodomestici.numDisp)) / (Elettrodomestici.numDisp + 1));
	left = dist;
	//alert($("#DispositiviDiv").width() + " w=" + w + " h=" + h + " dist=" + dist + " rapp=" + rapp);
	for (i = 0; i < Elettrodomestici.numDisp; i++)
	{
		$("#DispElem" + i).width(w+"px");
		$("#DispElem" + i).height(h+"px");
		$("#DispElem" + i).css("left", left + "px");
		$("#DispConsumo" + i).width(w+"px");
		$("#DispConsumo" + i).css("left", left + "px");
		$("#DispLoc" + i).width(w+"px");
		$("#DispLoc" + i).css("left", left + "px");
		left = left + dist + w;
	}
	
}



/*****************************************************
 * Legge le informazioni sui devices e le visualizza
 *****************************************************/
Elettrodomestici.DatiElettrodomestici = function(lista) {
	hideSpinner();
	Elettrodomestici.infoDisp = lista;
    	// ordino array in base al consumo attuale
    	//Elettrodomestici.infoDisp.sort(Elettrodomestici.compareConsumo);
	if ((lista != null) && (lista.length > 0))
	{
		//$("#ElettrodomesticiDiv").html("<div id='IconaDisp'></div><div id='NomeDisp'></div><div id='NomeDisp'></div><div id='StatoDisp'></div><div id='TitoloLocazione'>Dove si trova</div><div id='Locazione'></div><div id='TitoloConsumoDisp'>Sta consumando</div><div id='ConsumoDisp'></div>");
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
		$("#ElettrodomesticiDiv").html("<img id='ElettrVuotoBg' src='" + Define.home["sfondo_sx"] + 
				"'><div id='ElettrodomesticiVuoto'>" + Msg.home["nessunDisp"] + "</div>");
    }
	// per evitare accavallamento nel caso di tempi lunghi di risposta faccio setTimeout anziche' setInterval
	//if (Elettrodomestici.timerElettr  == null)
	//	Elettrodomestici.timerElettr = setInterval("Elettrodomestici.GetElettrodomestici()", Elettrodomestici.TIMER_UPDATE_ELETTR);
	Elettrodomestici.timerElettr = setTimeout("Elettrodomestici.GetElettrodomestici()", Elettrodomestici.TIMER_UPDATE_ELETTR);
}    

Elettrodomestici.GetElettrodomestici = function() {
	Main.ResetError();
	InterfaceEnergyHome.GetListaElettr(Elettrodomestici.DatiElettrodomestici);
}


Elettrodomestici.DatiLocazioni= function(lista) {
	Elettrodomestici.locazioni = lista;
	InterfaceEnergyHome.GetListaElettr(Elettrodomestici.DatiElettrodomestici);
}

Elettrodomestici.GestElettrodomestici = function() {
	// legge i dispositivi 
   	showSpinner();
	Log.alert(80, Elettrodomestici.MODULE, "Elettrodomestici.GestElettrodomestici");
	$("#Content").html(Elettrodomestici.htmlContent);
		// la prima volta leggo l'elenco delle locazioni perche' mi verra' dato solo il pid della locazione
	if (Elettrodomestici.locazioni == null)
		InterfaceEnergyHome.GetLocazioni(Elettrodomestici.DatiLocazioni);
	else	
		Elettrodomestici.GetElettrodomestici();
	

}

