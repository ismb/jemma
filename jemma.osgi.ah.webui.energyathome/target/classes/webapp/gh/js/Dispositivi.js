var Dispositivi = {
	MODULE : "Dispositivi ",
	TIMER_UPDATE_ELETTR : 4000,
	categorie : null,
	locazioni : null,
	infoDisp : null,
	numDisp : 0,
	indDispositivo : -1,
	timerElettr : null,
	defaultImg : GasDefine.home["defaultDispImg"],
	suffixStato : ["_acceso.png", "_spento.png", "_disconnesso.png"],
	classStato : ["DispStatoAcceso", "DispStatoSpento", "DispStatoDisconn"],
	classNome : ["DispNome", "DispNome", "DispNomeDisconn"],
	classValue : ["DispValueAcceso", "DispValueSpento", "DispValueDisconn"],
		htmlContent : "<div class='ContentTitle' >" + Msg.home["titoloDispositivi"] + "</div>" +
			"<div id='DispositiviPresentiDiv'>" +
				"<div id='Column1Div'>" + 
					"<div id='ContatoreDiv'>" + 
						"<div id='contatoreTitolo' class='nomeDispositivi' >" + Msg.home["contatoreTitolo"] + "</div>" +
						"<img id='contatoreImg' src='" + GasDefine.home["contatore"] + "'>" + 
						"<div id='contatoreVal' class='valDispositivi' >" + Msg.home["contatoreVal"] +
							":<span class='numericValue'>" + stForn + "</span>" +
						"</div>" +
					"</div>" +
				"</div>" +
				"<div id='Column2Div'>" + 
					"<div id='CaldaiaDiv'>" + 
						"<div id='caldaiaTitolo' class='nomeDispositivi' >" + Msg.home["caldaiaTitolo"] + "</div>" +
						"<img id='caldaiaImg' src='" + GasDefine.home["caldaia"] + "'>" + 
						"<div id='caldaiaVal' class='valDispositivi' >" + Msg.home["caldaiaVal"] + 
						":<span class='numericValue'>" + stCald + "</span>" +
						"</div>" +
					"</div>" +
				"</div>" +
				"<div id='Column3Div'>" + 
					"<div id='sensoreCO2Div'>" + 
						"<div id='sensoreCO2Titolo' class='nomeDispositivi' >" + Msg.home["sensoreCO2Titolo"] + "</div>" +
						"<div id='sensoreCO2FigDiv'><img id='sensoreCO2Img' src='" + GasDefine.home["sensoreGen"] + "'></div>" +
						"<div id='sensoreCO2Val' class='valDispositivi' >" + Msg.home["sensoreCO2Val"] + 
							":<span class='numericValue'>" + senCO2 + " PPM</span>" +
						"</div>" +
					"</div>" +
					"<div id='sensoreFugaGasDiv'>" + 
						"<div id='sensoreFugaGasTitolo' class='nomeDispositivi' >" + Msg.home["sensoreFugaGasTitolo"] + "</div>" +
						"<div id='sensoreFugaGasFigDiv'><img id='sensoreFugaGasImg' src='" + GasDefine.home["sensoreGen"] + "'></div>" + 
						"<div id='sensoreFugaGasVal' class='valDispositivi' >" + Msg.home["sensoreFugaGasVal"] + 
							":<span class='numericValue'>" + senFugaBatt + "%</span>" +
						"</div>" +
					"</div>" +
				"</div>" +
				"<div id='Column4Div'>" + 
					"<div id='sensoreCODiv'>" + 
						"<div id='sensoreCOTitolo' class='nomeDispositivi' >" + Msg.home["sensoreCOTitolo"] + "</div>" +
						"<div id='sensoreCOFigDiv'><img id='sensoreCOImg' src='" + GasDefine.home["sensoreGen"] + "'></div>" +
						"<div id='sensoreCOVal' class='valDispositivi' >" + Msg.home["sensoreCOVal"] + 
							":<span class='numericValue'>" + senCO + " PPM</span>" +
						"</div>" +
					"</div>" +
					"<div id='termostatoDiv'>" + 
						"<div id='termostatoTitolo' class='nomeDispositivi' >" + Msg.home["termostatoTitolo"] + "</div>" +
						"<div id='termostatoFigDiv'><img id='termostatoImg' src='" + GasDefine.home["termostato"] + "'></div>" + 
						"<div id='termostatoVal' class='valDispositivi' >" + Msg.home["termostatoVal"] + 
							":<span class='numericValue'>" + temper + "&ordm;C</span>" +
						"</div>" +
					"</div>" +
				"</div>" +
			"</div>"
};

Dispositivi.ExitDispositivi = function() {
	Log.alert(90, Dispositivi.MODULE, "ExitDispositivi");
	hideSpinner();
	GasMain.ResetError();
	if (Dispositivi.timerElettr != null)
	{
		clearTimeout(Dispositivi.timerElettr);
		Dispositivi.timerElettr = null;
	}
	GasInterfaceEnergyHome.Abort();
};

Dispositivi.compareConsumo = function(a, b) {
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
            Log.alert(120, Dispositivi.MODULE, "compareConsumo: a = " + aVal + " b = " + bVal);
            if (a.device_value <= b.device_value)
                return 1;
            else
                return -1;
        }
};

Dispositivi.VisDispositivi = function() {
	var iconaHtml = "", nomeHtml = "", statoHtml = "", consumoHtml = "", locazioneHtml = "";	
	var h, w, left, dist, imgStato, imgDisp, stato;
	var imgH, imgW;
    
	imgH = GasMain.imgDisp.height;
	imgW = GasMain.imgDisp.width;
	rapp = imgW / imgH;
	dispositiviHtml = "";
	consumoHtml = "";
	locazioneHtml = "";
	// creo prima html poi metto dimensioni e distanze
	for (i = 0; i < Dispositivi.numDisp; i++)
	{	
		// metto icona in base allo stato
		imgDisp = Dispositivi.infoDisp[i].icona;
		imgDisp = imgDisp.substr(0, imgDisp.indexOf("."));
		if (Dispositivi.infoDisp[i].avail != 2)
			indStato = 2;
		else
			if ((Dispositivi.infoDisp[i].stato == 1) || (Dispositivi.infoDisp[i].stato == 4) || (Dispositivi.infoDisp[i].stato == 3))
				indStato = 0;
			else
				indStato = 1;
		imgSrc = GasDefinePath.imgDispPath + imgDisp + Dispositivi.suffixStato[indStato];
		stato = Msg.home.statoDisp[indStato];
		dispositiviHtml += "<div id='DispElem" + i + "' class='DispElem' >" +
			"<img id='DispImg" + i + "' width='" + imgW + "px' height='" + imgH + "px' src='" + imgSrc + "' class='DispImg'>" +
			"<div id='DispNome" + i + "' class='" + Dispositivi.classNome[indStato] + "'>" + Dispositivi.infoDisp[i].nome + "</div>" +
			"<div id='DispStato" + i + "' class='" + Dispositivi.classStato[indStato] + "'>" + stato + "</div></div>";
		
		// metto consumo
		consumo = Math.round(Dispositivi.infoDisp[i].value);
		if ((indStato == 2) || (consumo == null))
			consumo = "0 W";
		else
			consumo += " W";
		consumoHtml += "<div id='DispConsumo" + i + "' class='" + Dispositivi.classValue[indStato] + "'>" + consumo + "</div>";
		
		// metto locazione
		loc = "";
		if (Dispositivi.infoDisp[i].locazione != null)
		{
			if (Dispositivi.locazioni != null)
			{
				loc = Dispositivi.locazioni[Dispositivi.infoDisp[i].locazione];
				loc = Lang.Convert(loc, Msg.locazioni); 
			}
		}
		Log.alert(80, Dispositivi.MODULE, "VisDispositivi : nome = " +  Dispositivi.infoDisp[i].nome +
						" loc = " + loc);
		locazioneHtml += "<div id='DispLoc" + i + "' class='DispLocazione'>" + loc + "</div>";
		left = left + w + dist;
		
	}	
	$("#DispositiviDiv").html(dispositiviHtml);
	$("#ConsumoDiv").html(consumoHtml);
	$("#LocazioneDiv").html(locazioneHtml);
	
	coord = Utils.ResizeImg("DispositiviDiv", GasDefine.home["defaultDispImg"], false, 1, 9);
	h = $("#DispositiviDiv").height();
	w = Math.round(h * rapp);
	dist = Math.round(($("#DispositiviDiv").width() - (w * Dispositivi.numDisp)) / (Dispositivi.numDisp + 1));
	left = dist;
	//alert($("#DispositiviDiv").width() + " w=" + w + " h=" + h + " dist=" + dist + " rapp=" + rapp);
	for (i = 0; i < Dispositivi.numDisp; i++)
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
	
};



/*****************************************************
 * Legge le informazioni sui devices e le visualizza
 *****************************************************/
Dispositivi.DatiDispositivi = function(lista) {
	hideSpinner();
	Dispositivi.infoDisp = lista;
    	// ordino array in base al consumo attuale
    	//Dispositivi.infoDisp.sort(Dispositivi.compareConsumo);
	if ((lista != null) && (lista.length > 0))
	{
		//$("#ElettrodomesticiDiv").html("<div id='IconaDisp'></div><div id='NomeDisp'></div><div id='NomeDisp'></div><div id='StatoDisp'></div><div id='TitoloLocazione'>Dove si trova</div><div id='Locazione'></div><div id='TitoloConsumoDisp'>Sta consumando</div><div id='ConsumoDisp'></div>");
		// se l'ultimo elemento e' lo smartInfo non lo considero
		// lo inserisco solo perche' in alcuni casi puo; servire
		if (lista[lista.length-1].tipo == GasInterfaceEnergyHome.SMARTINFO_APP_TYPE)
			Dispositivi.numDisp = lista.length - 1;
		else
			Dispositivi.numDisp = lista.length;
		Log.alert(80, Dispositivi.MODULE, "Trovati " + Dispositivi.numDisp + " dispositivi");
//		Dispositivi.VisDispositivi(); // TODO: qui si interviene per visualizare i dispositivi!!!!!!!!!!!!!!!!!!!!11111!!!!!
    	
	}		
    else
    {
		Dispositivi.numDisp = 0;
		$("#ElettrodomesticiDiv").html("<img id='ElettrVuotoBg' src='" + GasDefine.home["sfondo_sx"] + 
				"'><div id='ElettrodomesticiVuoto'>" + Msg.home["nessunDisp"] + "</div>");
    }
	// per evitare accavallamento nel caso di tempi lunghi di risposta faccio setTimeout anziche' setInterval
	//if (Dispositivi.timerElettr  == null)
	//	Dispositivi.timerElettr = setInterval("Dispositivi.GetDispositivi()", Dispositivi.TIMER_UPDATE_ELETTR);
	Dispositivi.timerElettr = setTimeout("Dispositivi.GetDispositivi()", Dispositivi.TIMER_UPDATE_ELETTR);
};    

Dispositivi.GetDispositivi = function() {
	GasMain.ResetError();
	GasInterfaceEnergyHome.GetListaElettr(Dispositivi.DatiDispositivi);
};


Dispositivi.DatiLocazioni= function(lista) {
	Dispositivi.locazioni = lista;
	GasInterfaceEnergyHome.GetListaElettr(Dispositivi.DatiDispositivi);
};

Dispositivi.GestDispositivi = function() {
	// legge i dispositivi 
   	showSpinner();
	Log.alert(80, Dispositivi.MODULE, "Dispositivi.GestDispositivi");
	$("#Content").html(Dispositivi.htmlContent);
		// la prima volta leggo l'elenco delle locazioni perche' mi verra' dato solo il pid della locazione
	if (Dispositivi.locazioni == null)
		GasInterfaceEnergyHome.GetLocazioni(Dispositivi.DatiLocazioni);
	else	
		Dispositivi.GetDispositivi();
};
