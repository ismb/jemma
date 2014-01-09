var CostiConsumi = {
	MODULE : "CostiConsumi",
	CONSUMI : 1,
	COSTI : 2,
	visType : 0,
	listaElettr : null, // lista degli elettrodomestici per avere l'associazione id:nome per la torta
	idSmartInfo : null,
// consumo
	consumoOdierno : 0,
	consumoMedio : 0,
	consumoPrevMese : 0,
	timerConsumi: null,
	TIMER_UPDATE_CONSUMI : 15000, //600000, // 10 minuti
	potenzaAttuale : 0,	
	timerPotenza: null,
	TIMER_UPDATE_POWER_METER : 2000, //600000, // 10 minuti

// costi
	costoOdierno : 0,
	costoMedio : 0,
	costoPrevMese : 0,
	suddivisioneCosti : null,
	timerCosti: null,
	TIMER_UPDATE_COSTI : 15000, //600000, // 10 minuti
	costoOdiernoImg : ["Resources/Images/costo-verde.png", "Resources/Images/costo-giallo.png", "Resources/Images/costo-rosso.png"],
	costoOdiernoMsg : ["<b>Bravo!<br>Sei sotto la tua media giornaliera</b>", "<b>Bene!<br>Sei in linea con i tuoi costi giornalieri</b>", "<b>Attenzione!<br>Sei sopra la tua media giornaliera</b>"],

	suggerimento : null,
	htmlContent : {
		"Costi" : "<div id='TitoloCosti' class='ContentTitle'>Costi</div><div id='ContenutoCosti'>" +
		"<div id='Costi'><div id='CostoSintesi'><img id='CostoImgBackground' src='Resources/Images/sfondo_top_arancio.png'><img id='CostoImg' src=''>" +
			"<span class='TitoloDivCosti'>Quanto stai spendendo</span><span id='DettaglioCosto'></span></div>" +
			"<div id='CostoOdierno'><img id='CostoOdiernoBackground' src='Resources/Images/sfondo_middle_arancio.png'><span id='DettaglioCostoOdierno'></span></div>" +
			"<div id='CostoMensile'><img id='CostoMensileBackground' src='Resources/Images/sfondo_bottom_arancio.png'><span id='DettaglioCostoMensile'></span></div></div>" +
		"<div id='IndicatoreCosti'><div id='TitoloIndicatoreCosti'>Come stai andando</div><div id='IndicatoreCostiDiv'><img id='IndicatoreCostiBackground' src='Resources/Images/indicatore_arancio.png'><img id='IndicatoreCostiBarra' src='Resources/Images/gradiente-arancio.png'></div></div>" + 
		"<div id='InfoCosti'><div id='SuddivisioneCosti'><img id='SuddivisioneCostiBackground' src='Resources/Images/sfondo_top_arancio.png'><span class='TitoloDivCosti'>I tuoi costi questo mese</span><div id='DettaglioSuddivisioneCosti'></div></div>" +
			"<div id='Tariffa'><span class='TitoloDivCosti'>La tua tariffa</span><img id='TariffaBackground' src='Resources/Images/sfondo_middle_arancio.png'><div id='DettaglioTariffa'><div id='DettaglioTariffaCheck'></div><img src='Resources/Images/tariffa_feriale.png' id='TariffaImg'></div></div>" +
			"<div id='SuggerimentiCosti'><span class='TitoloDivCosti'>Suggerimenti d'uso</span><img id='SuggerimentiCostiBackground' src='Resources/Images/sfondo_bottom_arancio.png'><span id='DettaglioSuggerimentiCosti'></span></div></div>",
		
		"Consumi" : "<div id='TitoloConsumi' class='ContentTitle'>Consumi</div><div id='ContenutoConsumi'>" +
		"<div id=Consumi><div id='ConsumoSintesi'><img id='ConsumoImgBackground' src='Resources/Images/sfondo_top_blu.png'><div id='ConsumoOdiernoImg'></div>" +
			"<span class='TitoloDivConsumi'>Quanto stai consumando</span><span id='DettaglioConsumo'></span></div>" +
			"<div id='ConsumoOdierno'><img id='ConsumoOdiernoBackground' src='Resources/Images/sfondo_middle_blu.png'><span id='DettaglioConsumoOdierno'></span></div>" +
			"<div id='ConsumoMensile'><img id='ConsumoMensileBackground' src='Resources/Images/sfondo_bottom_blu.png'><span id='DettaglioConsumoMensile'></span></div></div>" +
		"<div id='IndicatoreConsumi'><div id='TitoloIndicatoreConsumi'>Come stai andando</div><div id='IndicatoreConsumiDiv'><img id='IndicatoreConsumiBackground' src='Resources/Images/indicatore_blu.png'><img id='IndicatoreConsumiBarra' src='Resources/Images/gradiente-blu.png'></div></div>" + 
		"<div id='InfoConsumi'><div id='GraficoConsumoOdierno'><img id='GraficoConsumoOdiernoBackground' src='Resources/Images/sfondo_top_blu.png'><span class='TitoloDivConsumi'>Il tuo consumo oggi</span><div id='DettaglioGraficoConsumoOdierno'></div></div>" +
			"<div id='ConsumoMaggiore'><span class='TitoloDivConsumi'>Cosa sta consumando di più</span><img id='ConsumoMaggioreBackground' src='Resources/Images/sfondo_middle_blu.png'><div id='DettaglioConsumoMaggiore'><span id='TestoConsumoMaggiore'></span><img id='ConsumoMaggioreImg' src=''></div></div>" +
			"<div id='SuggerimentiConsumi'><span class='TitoloDivConsumi'>Suggerimenti d'uso</span><img id='SuggerimentiConsumiBackground' src='Resources/Images/sfondo_bottom_blu.png'><span id='DettaglioSuggerimentiConsumi'></span></div></div>"
	},
	hIndicatore : null,
	timerPowerMeter : null,
	dimConsumoImg : -1,
	leftConsumoImg : -1,
	topConsumoImg : -1,
	dimCostoImg : -1,
	leftCostoImg : -1,
	topCostoImg : -1,
	dimMaxDispImg : -1,
	leftMaxDispImg : -1,
	topMaxDispImg : -1,
	maxConsumoElettr : null,

	dataInizio : -1,
	dataFine : -1,
	pathImg : "Resources/Images/",
	pathImgPower : "Resources/Images/PowerMeter/",
	powerMeterImg : ["Gauge 04 freccia-30_220.png", "Gauge 04 freccia-24_220.png", "Gauge 04 freccia-18_220.png", "Gauge 04 freccia-12_220.png", 
            "Gauge 04 freccia-06_220.png", "Gauge 04 freccia+00_220.png", "Gauge 04 freccia+06_220.png", "Gauge 04 freccia+12_220.png", 
            "Gauge 04 freccia+18_220.png", "Gauge 04 freccia+24_220.png", "Gauge 04 freccia+30_220.png", "Gauge 04 freccia+36_220.png", 
            "Gauge 04 freccia+42_220.png", "Gauge 04 freccia+48_220.png", "Gauge 04 freccia+54_220.png", "Gauge 04 freccia+60_220.png", 
            "Gauge 04 freccia+66_220.png", "Gauge 04 freccia+72_220.png", "Gauge 04 freccia+78_220.png", "Gauge 04 freccia+84_220.png",
            "Gauge 04 freccia+90_220.png", "Gauge 04 freccia+96_220.png", "Gauge 04 freccia+102_220.png", "Gauge 04 freccia+108_220.png",
            "Gauge 04 freccia+114_220.png", "Gauge 04 freccia+120_220.png", "Gauge 04 freccia+126_220.png", "Gauge 04 freccia+132_220.png", 
            "Gauge 04 freccia+138_220.png", "Gauge 04 freccia+144_220.png", "Gauge 04 freccia+150_220.png", "Gauge 04 freccia+156_220.png", 
            "Gauge 04 freccia+162_220.png", "Gauge 04 freccia+168_220.png", "Gauge 04 freccia+174_220.png", "Gauge 04 freccia+180_220.png", 
            "Gauge 04 freccia+186_220.png", "Gauge 04 freccia+192_220.png", "Gauge 04 freccia+198_220.png", "Gauge 04 freccia+204_220.png", 
            "Gauge 04 freccia+210_220.png", "Gauge 04 freccia+216_220.png", "Gauge 04 freccia+222_220.png", "Gauge 04 freccia+228_220.png"]
	

}



/***************************************************************************
				Gestione Costi
***************************************************************************/

CostiConsumi.ExitCosti = function() {
	Log.alert(80, CostiConsumi.MODULE, "CostiConsumi.ExitCosti");
	if (CostiConsumi.timerCosti != null)
	{
		clearInterval(CostiConsumi.timerCosti);
		CostiConsumi.timerCosti = null;
	}
	InterfaceEnergyHome.Abort();
	$("#Content").html(null);
}

CostiConsumi.CalcCostoImg = function() {
      if (CostiConsumi.dimCostoImg  == -1)
	{
		wDiv = $("#CostoSintesi").width();
	      hDiv = $("#CostoSintesi").height();
		offsetTop = $("#CostoSintesi").offset().top;
		offsetLeft = $("#CostoSintesi").offset().left;
		Log.alert(80, CostiConsumi.MODULE, "CalcCostoImg : w = " + wDiv + " h = " + hDiv + " top = " + offsetTop + " left = " + offsetLeft);
	
		// imposto dimensioni e offset img in px       
		if (wDiv > hDiv)	
			CostiConsumi.dimCostoImg = (hDiv * 0.4);
		else
			CostiConsumi.dimCostoImg = (wDiv * 0.4); 
		CostiConsumi.topCostoImg = Math.floor((hDiv - CostiConsumi.dimCostoImg ) / 2) * 0.9;
		CostiConsumi.leftCostoImg = Math.floor((wDiv - CostiConsumi.dimCostoImg ) / 2);
	}
}




/************************************************
 * mette check che indica posizione sulla 
 * barra della tariffa in base all'ora attuale
 ************************************************/
CostiConsumi.SetTariffa = function ()
{
	// se sabato o domenica metto una barra diversa
	day = Main.dataAttuale.getDay();
	if ((day == 0) || (day == 6))
		$("#TariffaImg").attr("src", "Resources/Images/tariffa_festivo.png");
	else
		$("#TariffaImg").attr("src", "Resources/Images/tariffa_feriale.png");
	t = Math.round($("#DettaglioTariffaCheck").offset().top);
	t1 = Math.round($("#DettaglioTariffa").offset().top);
	w = Math.round($("#DettaglioTariffaCheck").width());
	h = Math.round($("#DettaglioTariffaCheck").height());
	// calcolo posizionamento sulla barra del simbolo di check in base al rapporto tra i minuti attuali e quelli di un giorno
	minutes = Main.dataAttuale.getHours() * 60 + Main.dataAttuale.getMinutes() - 20; // per migliorare posizionamento
	rapp = (1 / (1440 / minutes)).toFixed(2);
	val = Math.round((w * rapp) - (h / 4));
	htmlCheck = "<img src='./Resources/Images/OK.png' style='position:absolute;left:" + val + "px;top:" + (t-t1) + "px' width='" +
			h + "px' height='" + h + "px'>";
	$("#DettaglioTariffaCheck").html(htmlCheck);
	Log.alert(80, CostiConsumi.MODULE, "SetTariffa html = " + htmlCheck);
}


/***************************************************
 * scrive un suggerimento
 * tipo indica se per i costi o i consumi
 * divSuggerimento indica il div in cui scrivere
 * attualmente e' fisso, viene scritto solo la
 * prima volta che si accede alla pagina,
 * ma potrebbe ruotare tra un insieme o, 
 * in futuro, essere richiesto al server
***************************************************/
CostiConsumi.VisSuggerimento = function(divSuggerimento) {
	testo = CostiConsumi.suggerimento;	
	Log.alert(80, CostiConsumi.MODULE, "GetSuggerimento = " + testo);
	$(divSuggerimento).html(testo);
	if (testo != null)
	{
		// se il testo e' su piu' righe diminuisce il font
		if (testo.indexOf("<br>") != -1)
		{
			/**
			$(divSuggerimento).css("top", "35%");
			fs = $(divSuggerimento).css("font-size");
			fs.replace("px", ""); // tolgo px al fondo
			$(divSuggerimento).css("font-size", Math.round(parseInt(fs)*0.8) + "px");
			**/
			$(divSuggerimento).css("top", "35%");
			$(divSuggerimento).css("font-size", "0.8em");
		}
		else
		{
			$(divSuggerimento).css("top", "50%");
			$(divSuggerimento).css("font-size", "1.0em");
		
		}
		Log.alert(80, CostiConsumi.MODULE, "GetSuggerimento : font-size = " + $(divSuggerimento).css("font-size"));
	}
}
// dati un id, trova nella listaElettr il nome
CostiConsumi.TrovaNomePerId = function(id) {
	if (CostiConsumi.listaElettr != null) {
		for (k = 0; k < CostiConsumi.listaElettr.length; k++)
		{
			if (id == CostiConsumi.listaElettr[k].id)
				return CostiConsumi.listaElettr[k].nome;
		}
	}
	return null;
}

// ordina in base al secondo valore dell'elemento (che e' un array di 2 valori)
// e per valori decrescenti
CostiConsumi.CompareVal = function(a, b) {
    var aVal, bVal;
    
    if (a[1] == b[1])
    	return 0;
    else
    	if (a[1] > b[1])
    		return -1;
    	else
    		return 1;
}


/***************************************
 * Torta distribuzione costi 
 * Per adesso solo i 4 valori maggiori
 ***************************************/
CostiConsumi.VisSuddivisioneCosti = function() {

	if (CostiConsumi.suddivisioneCosti != null)
	{
		// ordino la lista per valori decrescenti, in testa avro' il consumo totale (smart info)
		CostiConsumi.suddivisioneCosti.sort(CostiConsumi.CompareVal);
		// creo lista di 4 elementi per il grafico della torta
		if (CostiConsumi.suddivisioneCosti.length < 5)
			n = CostiConsumi.suddivisioneCosti.length - 1;
		else 
			n = 4;
		lista = CostiConsumi.suddivisioneCosti.slice(1,n+1);
		sum = 0;
		// sostituisci id con nome e calcola somma 4 valori 
		for (i = 0; i < lista.length; i++)
		{
			lista[i][0] = CostiConsumi.TrovaNomePerId(lista[i][0]);
			sum +=  lista[i][1];
		}
		// aggiungo elemento con altro (totale - somma 4 elettr)
		lista[lista.length] = new Array("Altro", CostiConsumi.suddivisioneCosti[0][1] - sum);
		
		Log.alert(80, CostiConsumi.MODULE, "VisSuddivisioneCosti n = " + lista.length);
 	    var optionsObj = {
  			seriesColors: ["#ABD037","#e0e000","#3a8848","#f08000","#80f080"],
   			grid: {
				drawBorder: false,
				shadow: false
   			},
   			seriesDefaults: {
      			renderer: $.jqplot.PieRenderer,
      			rendererOptions: { 
					padding: 30,
					sliceMargin: 3,
					lineLabels: true, 
					lineLabelsLineColor: '#202020' 
				}
   			}
		};
 	   
		chart = $.jqplot('DettaglioSuddivisioneCosti', [lista], optionsObj);
	}
	else
		Log.alert(40, CostiConsumi.MODULE, "VisSuddivisioneCosti nessun dato");
}


/*****************************************************************************
 * funzione che calcola l'altezza della barra nel termometro dei costi
 * suppongo costo mi arrivi in assoluto, calcolo percentuale rispetto media
*****************************************************************************/
CostiConsumi.VisIndicatoreCosti = function() {
	perc = CostiConsumi.costoOdierno / CostiConsumi.costoMedio;
	if (perc > 2)
		perc = 2;

	valH = CostiConsumi.hIndicatore  * perc;
	$("#IndicatoreCostiBarra").height(valH + "px");
	Log.alert(480, CostiConsumi.MODULE, "SetIndicatoreCosti costoMedio = " + CostiConsumi.costoMedio + " perc = " + perc);
}

/*********************************
 * Visualizza i dati variabili 
 * della schermata dei costi
 *******************************/
CostiConsumi.VisDatiCosti = function() {
	$("#DettaglioCostoOdierno").html("Oggi finora hai speso <b>" + CostiConsumi.costoOdierno + " €</b>");
	$("#DettaglioCostoMensile").html("Previsione spesa mensile <b>" + CostiConsumi.costoPrevMese + " €</b>");

	// calcolo come sono rispetto alla media (per differenze sotto 0.10 € considero uguale
	diffCosto = CostiConsumi.costoOdierno - CostiConsumi.costoMedio;
	if (Math.abs(diffCosto) < 0.1)
		diffInd = 1;
	else
		if (CostiConsumi.costoOdierno > CostiConsumi.costoMedio)
			diffInd = 2;
		else
			diffInd = 0;
	$("#CostoImg").attr("src", CostiConsumi.costoOdiernoImg[diffInd]);
	$("#DettaglioCosto").html(CostiConsumi.costoOdiernoMsg[diffInd]);

	CostiConsumi.VisIndicatoreCosti();
	CostiConsumi.VisSuddivisioneCosti();
	CostiConsumi.VisSuggerimento("#DettaglioSuggerimentiCosti");
	// attualmente il check sulla tariffa lo aggirno insieme agli altri dati
	CostiConsumi.SetTariffa();
}


/*************************************************************
* Chiamate per lettura dati necessari per la pagina dei costi
**************************************************************/
CostiConsumi.DatiSuddivisioneCosti = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	if (val != null)
	// viene ritornato array di coppie di valori ["nome elettr", perc]
		CostiConsumi.suddivisioneCosti = val;

	// ho letto tutto adesso visualizzo
	CostiConsumi.VisDatiCosti();

	// se e' la prima volta creo il timer per la lettura dei dati
	// faccio setInterval anziche' setTimeout ogni volta perche'
	// se va male una chiamata riparto comunque
	// non ci sono problemi di accavallamento perche' l'intervallo e' molto alto
	if (CostiConsumi.timerCosti == null)
		CostiConsumi.timerCosti = setInterval("CostiConsumi.GetDatiCosti()", CostiConsumi.TIMER_UPDATE_COSTI);

}

CostiConsumi.DatiCostoMedio = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	if (val != null)
		CostiConsumi.costoMedio = val;
	else
		Log.alert(80, CostiConsumi.MODULE, "DatiCostoMedio null");
	InterfaceEnergyHome.GetSuddivisioneCosti(CostiConsumi.DatiSuddivisioneCosti);
}

CostiConsumi.DatiCostoPrevisto = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	if (val != null)
		CostiConsumi.costoPrevMese = (val).toFixed(2);
	else
		Log.alert(80, CostiConsumi.MODULE, "DatiCostoPrevisto null");

	InterfaceEnergyHome.GetCostoMedio(CostiConsumi.DatiCostoMedio);
}

CostiConsumi.DatiCostoOdierno = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	
	if (val != null)
		CostiConsumi.costoOdierno = (val).toFixed(2);
	else
		Log.alert(80, CostiConsumi.MODULE, "DatiCostoOdierno null");

	Log.alert(80, CostiConsumi.MODULE, "DatiCostoOdierno = " + val);
	InterfaceEnergyHome.GetCostoPrevisto(CostiConsumi.DatiCostoPrevisto);
}

CostiConsumi.DatiSuggerimentoCosti = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	
	if (val != null)
		CostiConsumi.suggerimento = val;
	else
		Log.alert(80, CostiConsumi.MODULE, "DatiSuggerimento null");

	Log.alert(80, CostiConsumi.MODULE, "DatiSuggerimento = " + val);
	InterfaceEnergyHome.GetCostoOdierno(CostiConsumi.DatiCostoOdierno);

}

CostiConsumi.DatiListaElettrodomestici = function(val) {
	CostiConsumi.listaElettr = val;
	if (val!= null)
	{
		// salvo id smartInfo
		for (i = 0; i < val.length; i++)
		{
			if (val[i].tipo == InterfaceEnergyHome.SMARTINFO_APP_TYPE)
			{
				CostiConsumi.idSmartInfo = val[i].id;
				break;
			}
		}
	}
	CostiConsumi.GetDatiCosti();
}


CostiConsumi.GetListaElettrodomestici = function() {
	if (CostiConsumi.listaElettr == null)
	{
		Log.alert(80, CostiConsumi.MODULE, "GetListaElettrodomestici");
		InterfaceEnergyHome.GetListaElettr(CostiConsumi.DatiListaElettrodomestici);
	}
	else
		CostiConsumi.GetDatiCosti();
}

/**************************************************
 * avvia le richieste dei dati, che vengono 
 * fatte in sequenza perche' asincrone, visualizzo 
 * i dati una volta sola quando li ho tutti 
 **************************************************/
CostiConsumi.GetDatiCosti = function() {
	Log.alert(80, CostiConsumi.MODULE, "GetDatiCosti");
	InterfaceEnergyHome.GetSuggerimento(CostiConsumi.DatiSuggerimentoCosti);
}

/*********************************************
 * crea la parte costante della grafica
 *********************************************/
CostiConsumi.VisCosti = function() {
	// Creo zona costo
	
	CostiConsumi.CalcCostoImg();
	$("#CostoImg").width(CostiConsumi.dimCostoImg);
	$("#CostoImg").height(CostiConsumi.dimCostoImg);	
	$("#CostoImg").css("left", CostiConsumi.leftCostoImg);	
	$("#CostoImg").css("top", CostiConsumi.topCostoImg);
	if (CostiConsumi.hIndicatore == null)
		CostiConsumi.hIndicatore = $("#IndicatoreCostiBarra").height();
	CostiConsumi.SetTariffa();
	
	// dopo aver impostato la parte grafica costante faccio le richieste
	// per la parte di dati
	CostiConsumi.GetListaElettrodomestici();
	
}

/*********************************************************
 * gestisce la visualizzazione delal schermata dei costi
 *********************************************************/
CostiConsumi.GestCosti = function() {
	CostiConsumi.visType = CostiConsumi.COSTI;
	$("#Content").html(CostiConsumi.htmlContent["Costi"]);
	Log.alert(80, CostiConsumi.MODULE, "CostiConsumi.GestCosti");
	
	CostiConsumi.VisCosti();
}
	


/***************************************************************************
 ***************************************************************************
				Gestione Consumi
***************************************************************************
***************************************************************************/


CostiConsumi.ExitConsumi = function() {
	Log.alert(80, CostiConsumi.MODULE, "CostiConsumi.ExitConsumi");
	if (CostiConsumi.timerPotenza != null)
	{
		clearTimeout(CostiConsumi.timerPotenza);
		CostiConsumi.timerPotenza = null;
	}
	if (CostiConsumi.timerConsumi != null)
	{
		clearTimeout(CostiConsumi.timerConsumi);
		CostiConsumi.timerConsumi = null;
	}
	InterfaceEnergyHome.Abort();
	$("#Content").html(null);
}

/*****************************************************************************
 * funzione che calcola l'altezza della barra nel termometro dei costi
 * suppongo costo mi arrivi in assoluto, calcolo percentuale rispetto media
*****************************************************************************/
CostiConsumi.VisIndicatoreConsumi = function() {
	perc = CostiConsumi.consumoOdierno / CostiConsumi.consumoMedio;
	if (perc > 2)
		perc = 2;

	valH = CostiConsumi.hIndicatore  * perc;
	$("#IndicatoreConsumiBarra").height(valH + "px");
	Log.alert(80, CostiConsumi.MODULE, "SetIndicatoreConsumi consumoMedio = " + CostiConsumi.consumoMedio + " perc = " + perc);
}


// visualizza elettrodomestico che in questo momento sta consumando di piu'
CostiConsumi.VisConsumoMaggiore = function()
{
	if (CostiConsumi.maxConsumoElettr != null)
	{
		Log.alert(80, CostiConsumi.MODULE, "VisConsumoMaggiore : " + CostiConsumi.maxConsumoElettr.icona);
		// metto immagine del device che sta consumando di piu'
		$("#ConsumoMaggioreImg").attr("src", CostiConsumi.pathImg + "/Devices/" + CostiConsumi.maxConsumoElettr.icona);
		// il consumo e' in watt
		$("#TestoConsumoMaggiore").text(CostiConsumi.maxConsumoElettr.nome + " (" + Math.round(CostiConsumi.maxConsumoElettr.value) + " W)");
		
		$("#ConsumoMaggioreImg").width(CostiConsumi.dimMaxDispImg);
		$("#ConsumoMaggioreImg").height(CostiConsumi.dimMaxDispImg);	
	}
}

CostiConsumi.VisGrafico = function() {
	var dati1 = new Array();

	Log.alert(80, CostiConsumi.MODULE, "VisGrafico");
	$.jqplot.config.enablePlugins = true;
	$("#DettaglioGraficoConsumoOdierno").html(null);
	barW = 0;
	
	if (CostiConsumi.consumoGiornaliero != null)
	{
		dati = CostiConsumi.consumoGiornaliero ;
		barW = $("#DettaglioGraficoConsumoOdierno").width() / 26; //24 ore piu' un po' di spazio
		Log.alert(50, CostiConsumi.MODULE, "VisStorico : num dati = " + dati.length + " barW = " + barW);
		for (i = 0; i < dati.length; i++)
		{
			dati1[i] = new Array();
			dati1[i][1] = (dati[i] / 1000).toFixed(2);
			dati1[i][0] = i + 0.5;
		}
	

    // Can specify a custom tick Array.
    // Ticks should match up one for each y value (category) in the series.

	      plot1 = $.jqplot("DettaglioGraficoConsumoOdierno", [dati1], {
			title: {
	        		text: "",//"Consumo Odierno (kWh)",   // title for the plot,
				textColor: '#3a8848',
				size: '0.8em',
				show: true
	    		},
	    		legend: {
	            	show: false
	      	},
			seriesDefaults:{
				color:"#2020ff",
				renderer:$.jqplot.BarRenderer,
				rendererOptions: {barMargin: 2, barPadding:0, barWidth:barW}
	
			},
	    		axes: {
				xaxis:{
					min: 0,
					max: 24,
					numberTicks: 13,
					tickOptions:{formatString:"%i", textColor:'#000000'}, 
					label: 'Ora'
					}, 
				yaxis: {
					min: 0,
					max: 5,
					numberTicks: 6,
					label: '',
					tickOptions:{formatString:"%i", textColor:'#000000', fontSize:'0.8em'}, 
					autoscale:true
				}
			}
	    });
	}
}

CostiConsumi.GetImgPower = function() {
   	var indImg = Math.floor((CostiConsumi.potenzaAttuale + 100) / 103.5);
    
	// temporaneo per ovviare a parte rossa troppo piccola
    	if (indImg >= CostiConsumi.powerMeterImg.length-1)
      	indImg = CostiConsumi.powerMeterImg.length - 2;
    	var imgPower = CostiConsumi.powerMeterImg[indImg];
    	Log.alert(80, CostiConsumi.MODULE, "GetImgPower: ind = " + indImg +
            " power = " + CostiConsumi.potenzaAttuale + " img = " + imgPower);
    	return  CostiConsumi.pathImgPower + imgPower;
}

CostiConsumi.SetConsumoImg = function() {
	$("#ConsumoOdiernoImg").html("<span id='ValConsumoAttuale'></span><img id='PowerMeterImg' src='Resources/Images/PowerMeter/Gauge_sfondo_4.png'><img id='PowerMeterArrow' src='./Resources/Images/PowerMeter/Gauge 04 freccia-18.png'><img id='PowerMeterCenter' src='./Resources/Images/PowerMeter/Gauge_center_220.png'>");
      if (CostiConsumi.dimConsumoImg  == -1)
	{
		wDiv = $("#ConsumoOdiernoImg").width();
	      hDiv = $("#ConsumoOdiernoImg").height();
		offsetTop = $("#ConsumoOdiernoImg").offset().top;
		offsetLeft = $("#ConsumoOdiernoImg").offset().left;
		Log.alert(80, CostiConsumi.MODULE, "CalcConsumoImg : w = " + wDiv + " h = " + hDiv + " top = " + offsetTop + " left = " + offsetLeft);
	
		// imposto dimensioni e offset img in px       
		if (wDiv > hDiv)	
			CostiConsumi.dimConsumoImg = (hDiv * 0.6);
		else
			CostiConsumi.dimConsumoImg = (wDiv * 0.6); 
		CostiConsumi.topConsumoImg = Math.floor((hDiv - CostiConsumi.dimConsumoImg ) / 2) * 0.8;
		CostiConsumi.leftConsumoImg = Math.floor((wDiv - CostiConsumi.dimConsumoImg ) / 2);
	}

	$("#PowerMeterImg").width(CostiConsumi.dimConsumoImg);
	$("#PowerMeterImg").height(CostiConsumi.dimConsumoImg);	
	$("#PowerMeterImg").css("left", CostiConsumi.leftConsumoImg);	
	$("#PowerMeterImg").css("top", CostiConsumi.topConsumoImg);
	$("#PowerMeterCenter").width(CostiConsumi.dimConsumoImg);
	$("#PowerMeterCenter").height(CostiConsumi.dimConsumoImg);
	$("#PowerMeterCenter").css("left", CostiConsumi.leftConsumoImg);	
	$("#PowerMeterCenter").css("top", CostiConsumi.topConsumoImg);
	$("#PowerMeterArrow").width(Math.floor(CostiConsumi.dimConsumoImg*0.8));
	$("#PowerMeterArrow").height(Math.floor(CostiConsumi.dimConsumoImg*0.8));
	$("#PowerMeterArrow").css("left", Math.floor(CostiConsumi.leftConsumoImg*1.15));	
	$("#PowerMeterArrow").css("top", Math.floor(CostiConsumi.topConsumoImg*1.30));

	// disegno lo speedometer
	imgPower = CostiConsumi.GetImgPower();
      
    $("#PowerMeterArrow").attr("src", imgPower);
	$("#ValConsumoAttuale").html((CostiConsumi.potenzaAttuale / 1000.0).toFixed(2) + " kW");

}

CostiConsumi.VisDatiConsumi = function()
{
	Log.alert(80, CostiConsumi.MODULE, "VisDatiConsumi ");
	CostiConsumi.VisIndicatoreConsumi();
	CostiConsumi.VisSuggerimento("#DettaglioSuggerimentiConsumi");
 	
	$("#DettaglioConsumoOdierno").html("Oggi finora hai consumato <b>" + CostiConsumi.consumoOdierno + " kWh</b>");
	$("#DettaglioConsumoMensile").html("Previsione consumo mensile <b>" + CostiConsumi.consumoPrevisto + " kWh</b>");
	CostiConsumi.VisGrafico();
}
	
CostiConsumi.DatiMaxElettr = function(elem) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	if (elem!= null)
		CostiConsumi.maxConsumoElettr = elem;
	else
		Log.alert(80, CostiConsumi.MODULE, "DatiMaxElettr null");
	CostiConsumi.VisConsumoMaggiore();
	if (CostiConsumi.timerPotenza == null)
		CostiConsumi.timerPotenza = setInterval("CostiConsumi.GetDatiPotenza()", CostiConsumi.TIMER_UPDATE_POWER_METER);
}

CostiConsumi.DatiConsumoGiornaliero = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	
	CostiConsumi.consumoGiornaliero = val;
	Log.alert(80, CostiConsumi.MODULE, "DatiConsumoGiornaliero ");

	CostiConsumi.VisDatiConsumi();
	// se e' la prima volta creo il timer per la lettura dei dati
	// faccio setInterval anziche' setTimeout ogni volta perche'
	// se va male una chiamata riparto comunque
	// non ci sono problemi di accavallamento perche' l'intervallo e' molto alto
	if (CostiConsumi.timerConsumi == null)
		CostiConsumi.timerConsumi = setInterval("CostiConsumi.GetDatiConsumi()", CostiConsumi.TIMER_UPDATE_CONSUMI);
	
}

CostiConsumi.DatiConsumoMedio = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	
	if (val != null)
		CostiConsumi.consumoMedio = (val / 1000).toFixed(2); // da w a kW
	else
		Log.alert(80, CostiConsumi.MODULE, "DatiConsumoMedio null");

	InterfaceEnergyHome.GetConsumoGiornaliero(CostiConsumi.DatiConsumoGiornaliero);
}

CostiConsumi.DatiConsumoPrevisto = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	Log.alert(80, CostiConsumi.MODULE, "DatiConsumoPrevisto val = " + val);

	if (val != null)
		CostiConsumi.consumoPrevisto = (val / 1000).toFixed(2); // da w a kW
	else
		Log.alert(80, CostiConsumi.MODULE, "DatiConsumoPrevisto null");
	InterfaceEnergyHome.GetConsumoMedio(CostiConsumi.DatiConsumoMedio);
}

CostiConsumi.DatiConsumoOdierno = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	Log.alert(80, CostiConsumi.MODULE, "DatiConsumoOdierno val = " + val);
	if (val != null)
		CostiConsumi.consumoOdierno = (val / 1000).toFixed(2); // da w a kW
	else
		Log.alert(80, CostiConsumi.MODULE, "DatiConsumoOdierno null");
	InterfaceEnergyHome.GetConsumoPrevisto(CostiConsumi.DatiConsumoPrevisto);
}

CostiConsumi.DatiSuggerimentoConsumi = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	
	if (val != null)
		CostiConsumi.suggerimento = val;
	else
		Log.alert(80, CostiConsumi.MODULE, "DatiSuggerimentoConsumi null");

	Log.alert(80, CostiConsumi.MODULE, "DatiSuggerimentoConsumi = " + val);
	InterfaceEnergyHome.GetConsumoOdierno(CostiConsumi.DatiConsumoOdierno);
}

CostiConsumi.DatiPotenzaAttuale = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	Log.alert(80, CostiConsumi.MODULE, "DatiPotenzaAttuale val = " + val);
	if (val != null)
	{
		CostiConsumi.potenzaAttuale = val;
		// aggiorno il PowerMeter
		imgPower = CostiConsumi.GetImgPower();
           	$("#PowerMeterArrow").attr("src", imgPower);
		$("#ValConsumoAttuale").html((CostiConsumi.potenzaAttuale / 1000.0).toFixed(2) + " kW");

	}
	else
		Log.alert(80, CostiConsumi.MODULE, "DatiPotenzaAttuale null");

	// se e' la prima volta creo il timer per la lettura dei dati
	// faccio setInterval anziche' setTimeout ogni volta perche'
	// se va male una chiamata riparto comunque
	// non ci sono problemi di accavallamento perche' l'intervallo e' molto alto
	InterfaceEnergyHome.GetMaxElettr(CostiConsumi.DatiMaxElettr);
	

}

CostiConsumi.GetDatiPotenza = function() {
	Log.alert(80, CostiConsumi.MODULE, "GetDatiPotenza");
	InterfaceEnergyHome.GetPotenzaAttuale(CostiConsumi.DatiPotenzaAttuale);
}



/**************************************************
 * avvia le richieste dei dati, che vengono 
 * fatte in sequenza perche' asincrone, visualizzo 
 * i dati una volta sola quando li ho tutti 
 **************************************************/
CostiConsumi.GetDatiConsumi= function() {
	Log.alert(80, CostiConsumi.MODULE, "GetDatiConsumi");
	InterfaceEnergyHome.GetSuggerimento(CostiConsumi.DatiSuggerimentoConsumi);
}


/*********************************************
 * crea la parte costante della grafica
 *********************************************/
CostiConsumi.VisConsumi = function() {
	
	CostiConsumi.SetConsumoImg();
	// il termometro per costi e consumi e' uguale
	if (CostiConsumi.hIndicatore == null)
		CostiConsumi.hIndicatore = $("#IndicatoreCostiBarra").height();
	if (CostiConsumi.dimMaxDispImg  == -1)
	{
		wDiv = $("#ConsumoMaggioreImg").width();
	      hDiv = $("#ConsumoMaggioreImg").height();
		offsetTop = $("#ConsumoMaggioreImg").offset().top;
		offsetLeft = $("#ConsumoMaggioreImg").offset().left;
		Log.alert(80, CostiConsumi.MODULE, "CalcMaxDispImg : w = " + wDiv + " h = " + hDiv + " top = " + offsetTop + " left = " + offsetLeft);
	
		// imposto dimensioni e offset img in px       
		if (wDiv > hDiv)	
			CostiConsumi.dimMaxDispImg = (hDiv * 0.8);
		else
			CostiConsumi.dimMaxDispImg = (wDiv * 0.8); 
		CostiConsumi.topMaxDispImg = Math.floor((hDiv - CostiConsumi.dimMaxDispImg ) / 2);
		CostiConsumi.leftMaxDispImg = Math.floor((wDiv - CostiConsumi.dimMaxDispImg ) / 2); // lo metto a sinistra
		Log.alert(80, CostiConsumi.MODULE, "VisConsumi : dim = " + CostiConsumi.dimMaxDispImg + " top = " + CostiConsumi.topMaxDispImg + " left = " + CostiConsumi.leftMaxDispImg);
	}
	// dopo aver impostato la parte grafica costante faccio le richieste
	// per la parte di dati
	CostiConsumi.GetDatiPotenza();

	CostiConsumi.GetDatiConsumi();
}

/*********************************************************
 * gestisce la visualizzazione delal schermata dei consumi
 *********************************************************/

CostiConsumi.GestConsumi = function() {
	CostiConsumi.visType = CostiConsumi.CONSUMI;
	$("#Content").html(CostiConsumi.htmlContent["Consumi"]);
	Log.alert(80, CostiConsumi.MODULE, "CostiConsumi.GestConsumi ");
	
	CostiConsumi.VisConsumi ();
}
	


