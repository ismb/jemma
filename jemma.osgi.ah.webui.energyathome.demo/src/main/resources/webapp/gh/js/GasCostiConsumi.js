var GasCostiConsumi = {
	MODULE : "GasCostiConsumi",
	CONSUMI : 1,
	COSTI : 2,
	visType : 0,
	listaElettr : null, // lista degli elettrodomestici per avere l'associazione id:nome per la torta
	idSmartInfo : null,
	// consumo

	consumoOdierno : null,
	consumoMedio : null,
	consumoPrevMese : null,
	consumoGiornaliero : null,
	timerConsumi : null,
	TIMER_UPDATE_CONSUMI : 300000, 
	potenzaAttuale : null,
	consumoGiornoPrec : null,
	timerPotenza : null,
	TIMER_UPDATE_POWER_METER : 4000, 
	timerBlink : null,
	TIMER_BLINK : 500,
	
	// costi
	costoOdierno : null,
	costoMedio : null,
	costoPrevMese : null,
	costoGiornaliero : null, 
	suddivisioneCosti : null,
	timerCosti : null,
	TIMER_UPDATE_COSTI : 300000,  // 5 minuti
	indicatoreImgSotto : GasDefine.home["termSfondo"],
	indicatoreImgSopra : GasDefine.home["termSopra"],
	imgChat : GasDefine.home["iconaSugg"],
	tariffaImg : null,
	leftTariffaPos : 0,
	costoOdiernoImg : [ GasDefine.home["costoVerde"], GasDefine.home["costoGiallo"], 
	                    GasDefine.home["costoRosso"], GasDefine.home["costoGrigio"]],
	costoOdiernoMsg : [ Msg.home["costoVerde"], Msg.home["costoGiallo"], 
	                    Msg.home["costoRosso"], Msg.home["costoVuoto"] ],

	suggerimento : null,
	htmlContent : {
		"CostiVuoto" : "<div id='TitoloCosti' class='ContentTitle'>" + Msg.home["costi"] +  "</div>" + 
			"<div id='CostiVuoto'>" + Msg.home["costiVuoto"] + "</div>",

		"Costi" : "<div id='TitoloCosti' class='ContentTitle'>" + Msg.home["costi"] + "</div>" + 
		"<div id='CostoSintesi'><img id='CostoSintesiBg' src='" + GasDefine.home["sfondo_sx"] + "'>" +
		"<div id='CostoAttualeTitolo' class='TitoloDettaglio'>" + Msg.home["titoloCosti"] + "</div>" + 
		"<div id='CostoAttuale'>" + 
		"<img id='CostoAttualeImg' src='" + GasDefine.home["costoGrigio"] + "'>" + 
		"<div id='CostoAttualeEuro'></div>" + 
		"<span id='DettaglioCosto'></span></div>" + 
//		"<div id='CostoOdierno'><span id='DettaglioCostoOdierno'>" + Msg.home["costoFinora"] + "</span></div>" + 
		"<div id='CostoOdierno'><div id='DettaglioCostoOdierno'>" + 
		"<div id='DettaglioCostoOdiernoAl'></div><div id='DettaglioCostoOdiernoHaiSpeso'></div><div id='DettaglioCostoOdiernoF1'></div><div id='DettaglioCostoOdiernoF2'></div><div id='DettaglioCostoOdiernoF3'></div>" + 
		"</div></div>" + 
//		"<div id='CostoPrevisto'><span id='DettaglioCostoPrevisto'>" + Msg.home["costoPrevisto"] + "</span></div>" + 
		"<div id='CostoPrevisto'><div id='DettaglioCostoPrevisto'><div id='DettaglioCostoPrevistoTitle'></div><div id='DettaglioCostoPrevistoVal'></div></div></div>" + 
		"<div id='CostoIndicatoreTitolo' class='TitoloDettaglio'>" + Msg.home["indicatoreCosti"] + "</div>" + 
		"<div id='CostoIndicatore'><div id='IndicatoreSopra' class='IndicatoreTxt'>" + Msg.home["indicatoreSopra"] +  "</div>" + 
		"<div id='IndicatoreMedia' class='IndicatoreTxt'>"	+ Msg.home["indicatoreMedia"] + "</div> " + 
		"<div id='IndicatoreSotto' class='IndicatoreTxt'>" + Msg.home["indicatoreSotto"] + "</div>" + 
		"<div id='ConsumoIndicatorePaddingLeft'></div>" + 
		"<div id='CostoIndicatoreImg'></div></div></div>" +
		"<div id='CostoInfo'><img id='CostoInfoBg' src='" + GasDefine.home["sfondo_dx"] + "'>" +
		"<span id='CostoInfoTitolo' class='TitoloDettaglio'>"	+ Msg.home["spesaMensile"] + "</span>"	+ 
		"<div id='CostoSuddivisione'><div id='DettaglioSuddivisioneCosti'></div></div>" + 
		"<div id='CostoTariffa'><span id='CostoTitoloTariffa'>" + Msg.home["tariffa"]	+ "</span>"	+ 
		"<div id='TariffaImgDiv'><img id='TariffaImg' src='"	+ GasDefine.home["tariffaFeriale"] + "'></div>" + 
		"<div id='TariffeFascieImgDiv'>" +
			"<div id='TariffeFascieImgVerdeDiv'>" +
				"<img id='TariffeFascieImgVerde' src='" + GasDefine.trial["tariffaQVerde"] + "'>" +
				"<span id='TariffeFascieVerde'>" + Msg.trial["tariffaBassa"] + "</span>"	+
			"</div>" + 
			"<div id='TariffeFascieImgGiallaDiv'>" +
				"<img id='TariffeFascieImgGialla' src='" + GasDefine.trial["tariffaQGiallo"] + "'>" +
				"<span id='TariffeFascieGialla'>" + Msg.trial["tariffaMedia"] + "</span>"	+
			"</div>" + 
			"<div id='TariffeFascieImgRossaDiv'>" +
				"<img id='TariffeFascieImgRossa' src='" + GasDefine.trial["tariffaQRosso"] + "'>" +
				"<span id='TariffeFascieRossa'>" + Msg.trial["tariffaAlta"]	+ "</span>"	+
			"</div>" + 
		"</div>" + 
		"<div id='TariffaPos'>" +
		"</div></div>" + 
		"", // per concludere correttamente la riga

		"Consumi" : 
			"<div id='TitoloConsumi' class='ContentTitle'>"	+ Msg.home["consumi"] + "</div>" + 
			"<div id='ConsumoSintesi'><img id='ConsumoSintesiBg' src='" + GasDefine.home["sfondo_sx"] + "'>"  + 
			"<div id='ConsumoAttuale'>" +  
			"<div id='ConsumoAttualeTitolo' class='TitoloDettaglio'>" + Msg.home["titoloConsumi"] + "</div>" + 
			"<div id='ValConsumoAttuale'></div></div>" +
			"<div id='ConsumoOdierno'><div id='DettaglioConsumoOdierno'><div id='DettConsOggiTitle'></div><div id='DettConsOggiVal'></div></div></div>" +
			"<div id='ConsumoPrevisto'><div id='DettaglioConsumoPrevisto'><div id='DettConsPrevTitle'></div><div id='DettConsPrevVal'></div></div></div>" +
			"<div id='ConsumoIndicatoreTitolo' class='TitoloDettaglio'>" + Msg.home["indicatoreConsumi"] + "</div>" +
			"<div id='ConsumoIndicatore'><div id='IndicatoreSopra' class='IndicatoreTxt'>"	+ Msg.home["indicatoreSopra"] + "</div>" + 
			"<div id='IndicatoreMedia' class='IndicatoreTxt'>" + Msg.home["indicatoreMedia"] + "</div>" + 
			"<div id='IndicatoreSotto' class='IndicatoreTxt'>" + Msg.home["indicatoreSotto"] + "</div>" + "<div id='ConsumoIndicatorePaddingLeft'></div>" +
			"<div id='ConsumoIndicatoreImg'></div></div></div>" +
			"<div id='ConsumoInfo'><img id='ConsumoInfoBg' src='" + GasDefine.home["sfondo_dx"] + "'>"  +
			"<span id='ConsumoInfoTitolo' class='TitoloDettaglio'>" + Msg.home["consumoOdierno"] + "</span><div>" +
			"<div id='GraficoConsumoOdierno'><div id='LabelKWH'>" + Msg.home["labelkWh"] + "</div><div id='LabelOra'>" + Msg.home["labelOra"] + "</div>" +
			"<div id='DettaglioGraficoConsumoOdierno'></div></div>" + 
			"<div id='ConsumoMaggiore'><span id='ConsumoTitoloMaggiore'>"	+ Msg.home["consumoMaggiore"] + "</span>" + 
			"<div id='DettaglioConsumoMaggiore'></div></div>" +
			"<div id='ConsumoSuggerimento'><img src='" + GasDefine.home["sfondoSugg"] + "' id='SuggerimentoImg'>" +
			"<span id='ConsumoTitoloSuggerimento'>" + Msg.home["suggerimenti"] + "</span>" + 
			"<div id='DettaglioSuggerimentiConsumi'></div></div>"
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
	maxConsumoElettr : null,

	dataInizio : -1,
	dataFine : -1,
	// pathImg : "./Resources/Images/",
	pathImgPower : GasDefinePath.imgPowerMeterPath
};

/*******************************************************************************
 * Gestione Costi vuota per prima fase trial
 ******************************************************************************/

GasCostiConsumi.ExitCostiVuoto = function() {
	Log.alert(80, GasCostiConsumi.MODULE, "GasCostiConsumi.ExitCostiVuoto");
	$("#Content").html(null);
	//hideSpinner();
};

GasCostiConsumi.GestCostiVuoto = function() {
	Log.alert(80, GasCostiConsumi.MODULE, "GasCostiConsumi.GestCostiVuoto");
	$("#Content").html(GasCostiConsumi.htmlContent["CostiVuoto"]);
};

/*******************************************************************************
 * Gestione Costi
 ******************************************************************************/

GasCostiConsumi.ExitCosti = function() {
	// se simulazione o demo faccio vedere costi altrimento no
	if (GasInterfaceEnergyHome.mode == GasInterfaceEnergyHome.MODE_FULL)
		GasCostiConsumi.ExitCostiVuoto();
	else {
		Log.alert(80, GasCostiConsumi.MODULE, "GasCostiConsumi.ExitCosti");
		GasMain.ResetError();
		hideSpinner();
		if (GasCostiConsumi.timerCosti != null) {
			clearInterval(GasCostiConsumi.timerCosti);
			GasCostiConsumi.timerCosti = null;
		}
		GasInterfaceEnergyHome.Abort();
		GasCostiConsumi.tariffaImg = null;
		$("#Content").html(null);

	}
};

GasCostiConsumi.CalcCostoImg = function() {
	if (GasCostiConsumi.dimCostoImg == -1) {
		wDiv = $("#CostoAttuale").width();
		hDiv = $("#CostoAttuale").height();
		offsetTop = $("#CostoAttuale").offset().top;
		offsetLeft = $("#CostoAttuale").offset().left;

		// imposto dimensioni e offset img in px
		if (wDiv > hDiv)
			GasCostiConsumi.dimCostoImg = (hDiv * 0.4);
		else
			GasCostiConsumi.dimCostoImg = (wDiv * 0.4);
		GasCostiConsumi.topCostoImg = Math.floor((hDiv - GasCostiConsumi.dimCostoImg) / 2) * 0.9;
		GasCostiConsumi.leftCostoImg = Math.floor((wDiv - GasCostiConsumi.dimCostoImg) / 2);
	}
};

/*******************************************************************************
 * mette check che indica posizione sulla barra della tariffa in base all'ora
 * attuale
 ******************************************************************************/
GasCostiConsumi.SetTariffa = function() {
	// calcolo posizionamento sulla barra del rettangolo che indica l'ora
	// tiene conto di come e' fatta l'imamgine della tariffa
	w = $("#TariffaImg").width();
	ore = GasMain.dataAttuale.getHours();
	// ogni quadratino della tariffa e' largo 13px e la distanza e' 5 px per
	// width img = 432
	dimQ = (w / 432) * 13.13;
	dimS = (w / 432) * 5;
	val = (ore * dimQ) + (dimS * ore) + GasCostiConsumi.leftTariffaPos;
	$("#TariffaPos").css("left", Math.round(val) + "px");
	Log.alert(80, GasCostiConsumi.MODULE, "SetTariffa w = " + w + " left = "
			+ GasCostiConsumi.leftTariffaPos + " val = " + val);
};

/*******************************************************************************
 * scrive un suggerimento tipo indica se per i costi o i consumi divSuggerimento
 * indica il div in cui scrivere attualmente e' fisso, viene scritto solo la
 * prima volta che si accede alla pagina, ma potrebbe ruotare tra un insieme o,
 * in futuro, essere richiesto al server
 ******************************************************************************/
GasCostiConsumi.VisSuggerimento = function(divSuggerimento) {
	testo = GasCostiConsumi.suggerimento;
	Log.alert(80, GasCostiConsumi.MODULE, "GetSuggerimento = " + testo);
	$(divSuggerimento).html(testo);
	if (testo != null) {
		// se il testo e' su piu' righe diminuisce il font
		if (testo.indexOf("<br>") != -1) {
			$(divSuggerimento).css("padding-top", "5%");
			$(divSuggerimento).css("font-size", "0.8em");
		} else {
			$(divSuggerimento).css("padding-top", "5%");
			$(divSuggerimento).css("font-size", "1.1em");
		}
		Log.alert(80, GasCostiConsumi.MODULE, "GetSuggerimento : font-size = " + $(divSuggerimento).css("font-size"));
	}

};

// dati un id, trova nella listaElettr il nome
GasCostiConsumi.TrovaNomePerId = function(id) {
	if (GasCostiConsumi.listaElettr != null) {
		for (k = 0; k < GasCostiConsumi.listaElettr.length; k++) {
			if (id == GasCostiConsumi.listaElettr[k].id)
				return GasCostiConsumi.listaElettr[k].nome;
		}
	}
	return null;
};

// ordina in base al secondo valore dell'elemento (che e' un array di 2 valori)
// e per valori decrescenti
GasCostiConsumi.CompareVal = function(a, b) {
	var aVal, bVal;

	if (a[1] == b[1])
		return 0;
	else if (a[1] > b[1])
		return -1;
	else
		return 1;
};

/*******************************************************************************
 * Torta distribuzione costi Per adesso solo i 4 valori maggiori
 * Se un elemento ha meno del 5% non lo visualizzo
 ******************************************************************************/
GasCostiConsumi.VisSuddivisioneCosti = function() {
	var tot, sum, altro, indTot;
	
	if (GasCostiConsumi.suddivisioneCosti != null) {
		// ordino la lista per valori decrescenti, in testa avro' il consumo
		// totale (smart info)
		//GasCostiConsumi.suddivisioneCosti.sort(GasCostiConsumi.CompareVal);

		lista = GasCostiConsumi.suddivisioneCosti;
		sum = 0;
		tot = 0;
		altro = 0;
		indTot = -1;
		
		// PER SIMULAZIONE USO DIRETTAMENTE IL NOME
		// sostituisci id con nome e calcola somma valori
		// Dal 27-1-2012 prendo tutti i valori, taglio solo quelli minori del 2%
		// se c'e' smart plug quello e' il totale, per altri e' (totale - somma disp)
		// se non c'e' smart info non prendo il totale
		if (GasInterfaceEnergyHome.mode > 1) {
			for (i = 0; i < lista.length; i++) // i = 0
			{
				if (lista[i][0] == GasCostiConsumi.idSmartInfo)
				{
					if (lista[i][1] != null)
						tot = lista[i][1];
					indTot = i;
				}
				else
				{
					lista[i][0] = GasCostiConsumi.TrovaNomePerId(lista[i][0]);
					if ((lista[i][0] != null) && (lista[i][1] != null))
						sum += lista[i][1];
				}
			}
		} 
		else {
			for (i = 0; i < lista.length; i++) // i = 0
			{
				if (lista[i][0] == "SmartPlug")
				{
					if (lista[i][1] != null)
						tot = lista[i][1];
					indTot = i;
				}
				else
					if (lista[i][1] != null)
						sum += lista[i][1];
			}
		}
		// creo lista con elementi non null e con valore percentuale > 2%
		// se c'e' smart plug: altro = tot - sum
		// se non c'e' smart plug: altro = somma eventuali valori < 2%
		if (sum > 0) 
		{
			// nel giorno dell'installazione potrebbero non esserci valori
			//percMin = GasCostiConsumi.suddivisioneCosti[0][1] * 0.02;
			if (indTot != -1)
			{
				altro = tot - sum;
				// puo' esserci lo smart plug ma avere valore null (tot = 0), oppure non avere tutti i dati
				// e avere un valore < della somma del valore degli elettrodomestici
				// in questi casi metto 'dati mancanti'
				if (altro >= 0)
				{
					// sostituisco al consumo totale la differenza tra il totale e la somma degli elettrodomestici
					//lista[indTot] = new Array(Msg.home["altro"], tot - sum);
					percMin = tot * 0.02;
				}
				else
				{
					if (lista[indTot][1] == null) 
						Log.alert(30, GasCostiConsumi.MODULE, "VisSuddivisioneCosti: smart plug valore null");
					else
						Log.alert(30, GasCostiConsumi.MODULE, "VisSuddivisioneCosti: smart plug valore inferiore alla somma dei dispositivi");
					
					$("#DettaglioSuddivisioneCosti").html("<div id='SuddivisioneCostiVuoto'>" + 
							Msg.home["suddivisioneVuoto"] + "</div>");
					return;
				}
			}
			else
				percMin = sum * 0.02;
			
			listaCorta = new Array();
	
			j = 0;
			for (i = 0; i < lista.length; i++)
			{
				if ((lista[i][1] != null) && (i != indTot))
				{	// tolgo gli elementi con meno del 2%
					if (lista[i][1] > percMin)
					{
						listaCorta[j] = lista[i];
						j++;
					}
					else
						altro += lista[i][1];
				}
			}	
			// aggiungo altro 
			if (altro > 0)
				listaCorta[j] = new Array(Msg.home["altro"], altro);
				
			Log.alert(80, GasCostiConsumi.MODULE, "VisSuddivisioneCosti n = " + listaCorta.length);
			var optionsObj = {
				// seriesColors:
				seriesColors : [ "#F9F09B", "#F0CE20", "#FFEE66", "#F0BF0F",
				                 "#EFE38B", "#F6C050" ],
				grid : {
					drawBorder : false,
					shadow : false
				},
				seriesDefaults : {
					renderer : $.jqplot.PieRenderer,
					rendererOptions : {
						padding : 35,
						sliceMargin : 3,
						lineLabels : true,
						lineLabelsLineColor : '#202020'
					}
				}
			};

			$("#DettaglioSuddivisioneCosti").html("<div id='SuddivisioneCostiLeft'>" + "<br /><br />" +
					Msg.home["infoPeriodo"] + ":<br /><br />" + 
					Msg.home["infoLettura"] + ":<br /><br />" + 
					Msg.home["infoConsumo"] + ":<br /><br />" + 
					Msg.home["infoTotaleFornitura"] + ":<br /><br />" + 
					Msg.home["infoTotali"] + ":<br /><br />" + 
					Msg.home["infoTotaleBolletta"] + ":<br /><br />" + 
					Msg.home["infoDaPagare"] + ":<br /><br />" + 
					Msg.home["infoPagataIl"] + ":<br /><br />" + "</div>" + 
					"<div id='SuddivisioneCostiRight'>" + "<br /><br />" +
					inizioPeriodoRif + "-" + finePeriodoRif + "<br /><br />" +
					letturaFatt + "<br /><br />" +
					consumoUltimaBoll + " smc<br /><br />" +
					totFornituraGas + " €<br /><br />" +
					totOneriDiversi + " €<br /><br />" +
					totBolletta + " €<br /><br />" +
					totDaPagare + " €<br /><br />" +
					pagataIl + "<br /><br />" + "</div>");			
			
			try {
//				chart = $.jqplot('DettaglioSuddivisioneCosti', [ listaCorta ],
//						optionsObj);
			} catch (err) {
				Log.alert(40, GasCostiConsumi.MODULE, "VisSuddivisioneCosti = "
						+ err.toString());
			}
			return;
		}
	} 
	$("#DettaglioSuddivisioneCosti").html("<div id='SuddivisioneCostiVuoto'>" + 
				Msg.home["suddivisioneVuoto"] + "</div>");
	Log.alert(40, GasCostiConsumi.MODULE, "VisSuddivisioneCosti nessun dato");
	
};


/*******************************************************************************
 * funzione che calcola l'altezza della barra nel termometro dei costi 
 * calcolo percentuale rispetto media
 * Dal 29-11-2011: sommo i valori giornalieri fino all'ora attuale o 
 * all'ultimo valore non null, sommo i valori medio per lo stesso numero di ore 
 * poi faccio il confronto. Se un'ora del giornaliero e' null non prendo il valore 
 * corrispondente della media, se una'ora della media e' null non faccio il confronto
 ******************************************************************************/
GasCostiConsumi.VisIndicatoreCosti = function() {
	var medio, odierno, n, max;
	
	perc = 0;
	odierno = null;
	medio = null;
	/* calcolo costo odierno dalla somma del costo giornaliero
	if (GasCostiConsumi.costoGiornaliero != null)
	{
		GasCostiConsumi.costoOdierno = null;
		for (i= 0; i < GasCostiConsumi.costoGiornaliero.length; i++)
		{	
			if (GasCostiConsumi.costoGiornaliero[i] != null)
				if (GasCostiConsumi.costoOdierno == null)
					GasCostiConsumi.costoOdierno = GasCostiConsumi.costoGiornaliero[i];
				else
					GasCostiConsumi.costoOdierno += GasCostiConsumi.costoGiornaliero[i];
		}
	}
	else
		GasCostiConsumi.costoOdierno = null;
	*/
	if ((GasCostiConsumi.costoMedio != null) && (GasCostiConsumi.costoGiornaliero != null) && 
			(GasCostiConsumi.costoOdierno != null))
	{
		n = 0;
		odierno = 0;
		medio = 0;
		max = GasCostiConsumi.costoGiornaliero.length-1;
		// sommo consumo giornaliero e media per tutti valori non null
		// nel caso dovessi dare null per un valore mancante a meta' devo partire dalla fine
		// considero a parte l'ultima ora (ora attuale)
		for (i = 0; i < max; i++)
		{
			if (GasCostiConsumi.costoGiornaliero[i] != null)
			{
				n++;
				// se media null per valore valido odierno non faccio confronto
				if (GasCostiConsumi.costoMedio[i] == null)
				{
					odierno = null;
					break;
				}
				medio += GasCostiConsumi.costoMedio[i];
				odierno += GasCostiConsumi.costoGiornaliero[i];
			}
		}
		// se l'ultimo valore non e' null prendo una percentuale della media in base ai minuti attuali
		if ((medio != null) && (odierno != null))
		{
			if (GasCostiConsumi.costoGiornaliero[max] != null)
			{
				if (GasCostiConsumi.costoMedio[max] != null)
				{
					min = GasGestDate.GetActualDate().getMinutes();
					medio += GasCostiConsumi.costoMedio[max] * (min / 60);
					odierno += GasCostiConsumi.costoGiornaliero[max];
				}
				else
					medio = null;
			}
			if (medio != null)
			{
				perc = odierno / medio;
				if (perc > 2)
					perc = 2;
			}
		}
	}
	$('#CostoIndicatoreImg').gauge("value", perc);
	// calcolo come sono rispetto alla media (per differenze sotto 0.10 € considero uguale
	if ((odierno == null) || (medio == null))
		diffInd = 3;
	else
	{
		diffCosto = odierno - medio;
		if (Math.abs(diffCosto) < 0.02)
			diffInd = 1;
		else if (odierno > medio)
			diffInd = 2;
		else
			diffInd = 0;
	}
	$("#CostoAttualeImg").attr("src", GasCostiConsumi.costoOdiernoImg[diffInd]);
	$("#CostoAttualeEuro").html(costoEuro);
	$("#DettaglioCosto").html(GasCostiConsumi.costoOdiernoMsg[diffInd]);

	Log.alert(80, GasCostiConsumi.MODULE, "VisDatiCosti costoOdierno = "
			+ GasCostiConsumi.costoOdierno + " odierno = " + odierno + " costoMedio = " + odierno);
};


/*******************************************************************************
 * Chiamate per lettura dati necessari per la pagina dei costi
 ******************************************************************************/

GasCostiConsumi.DatiCostoPrevistoCb = function(val) {
	if (val != null)
		GasCostiConsumi.costoPrevMese = val;
	else
		GasCostiConsumi.costoPrevMese = null;
	Log.alert(80, GasCostiConsumi.MODULE, "DatiCostoPrevisto = " + val);
	if (GasCostiConsumi.costoPrevMese == null)
		txt = Msg.home["datoNonDisponibile"];
	else
		txt = (GasCostiConsumi.costoPrevMese).toFixed(2) + " €";
//	$("#DettaglioCostoPrevisto").html(
//			Msg.home["costoPrevisto"] + ": <br><b>"	+ txt + "</b>");
	$("#DettaglioCostoPrevistoTitle").html(Msg.home["costoPrevisto"] + ":");
	$("#DettaglioCostoPrevistoVal").html(txt);

	GasInterfaceEnergyHome.GetSuggerimento(GasCostiConsumi.DatiSuggerimentoCostiCb);
};

GasCostiConsumi.DatiSuddivisioneCostiCb = function(val) {
	// viene ritornato array di coppie di valori ["nome elettr", perc]
	GasCostiConsumi.suddivisioneCosti = val;
	hideSpinner();
	GasCostiConsumi.VisSuddivisioneCosti();
	
	showSpinner();
	GasInterfaceEnergyHome.GetCostoPrevisto(GasCostiConsumi.DatiCostoPrevistoCb);
	
};

GasCostiConsumi.DatiCostoMedioCb = function(val) {
	if (val != null)
		GasCostiConsumi.costoMedio = val;
	else
		GasCostiConsumi.costoMedio = null;
	hideSpinner();
	// ho i dati per visualizzare l'indicatore dei costi e il costo odierno
	GasCostiConsumi.VisIndicatoreCosti(); 
	/**
	if (GasCostiConsumi.costoOdierno == null)
		txt = Msg.home["datoNonDisponibile"];
	else
		txt = (GasCostiConsumi.costoOdierno).toFixed(2) + " €";
	$("#DettaglioCostoOdierno").html(
			Msg.home["costoFinora"] + ":<br><br><b>"+ txt + "</b>");
	**/
	Log.alert(80, GasCostiConsumi.MODULE, "DatiCostoMedio = " + val);
	showSpinner();
	GasInterfaceEnergyHome.GetSuddivisioneCosti(GasCostiConsumi.DatiSuddivisioneCostiCb);
};

GasCostiConsumi.DatiCostoGiornalieroCb = function(val) {
	if (val != null)
		GasCostiConsumi.costoGiornaliero = val;
	else
		GasCostiConsumi.costoGiornaliero = null;

	Log.alert(80, GasCostiConsumi.MODULE, "DatiCostoGiornaliero = " + val);
	GasInterfaceEnergyHome.GetCostoMedio(GasCostiConsumi.DatiCostoMedioCb);
};

GasCostiConsumi.DatiCostoOdiernoCb = function(val) {
	if (val != null)
		GasCostiConsumi.costoOdierno = val;
	else
		GasCostiConsumi.costoOdierno = null;
	
	if (GasCostiConsumi.costoOdierno == null)
		txt = Msg.home["datoNonDisponibile"];
	else
		txt = (GasCostiConsumi.costoOdierno).toFixed(2) + " €";
//	$("#DettaglioCostoOdierno").html(Msg.home["costoFinora"] + ":<br><br><b>"+ txt + "</b>");
	txtData = costoOdiernoData;
	txt1 = costoOdiernoFascia1;
	txt2 = costoOdiernoFascia2;
	txt3 = costoOdiernoFascia3;
	
//	$("#DettaglioCostoOdierno").html("<p>" + Msg.home["costoAl"] + ": <b>" + txtData + "</b><br></p>" + Msg.home["costoFinora"] + 
//			":" + 
//			"<p><b>" + 
//			txt1 + " €" + Msg.home["costoFascia1"] + "<img id='ConsumoVerdeImg' src='" + GasDefine.trial["tariffaQVerde"] + "'>"  + "<br />" +  
//			txt2 + " €" + Msg.home["costoFascia2"] + "<img id='ConsumoGialloImg' src='" + GasDefine.trial["tariffaQGiallo"] + "'>"  + "<br />" + 
//			txt3 + " €" + Msg.home["costoFascia3"] + "<img id='ConsumoRossoImg' src='" + GasDefine.trial["tariffaQRosso"] + "'>"  + "<br />" + 
//			"</b></p>");

	$("#DettaglioCostoOdiernoAl").html(Msg.home["costoAl"] + ": " + txtData);
	$("#DettaglioCostoOdiernoHaiSpeso").html(Msg.home["costoFinora"] + ":");
	$("#DettaglioCostoOdiernoF1").html(txt1 + " €" + Msg.home["costoFascia1"] + "<img id='ConsumoVerdeImg' src='" + GasDefine.trial["tariffaQVerde"] + "'>");
	$("#DettaglioCostoOdiernoF2").html(txt2 + " €" + Msg.home["costoFascia2"] + "<img id='ConsumoGialloImg' src='" + GasDefine.trial["tariffaQGiallo"] + "'>");
	$("#DettaglioCostoOdiernoF3").html(txt3 + " €" + Msg.home["costoFascia3"] + "<img id='ConsumoRossoImg' src='" + GasDefine.trial["tariffaQRosso"] + "'>");
	
	Log.alert(80, GasCostiConsumi.MODULE, "DatiCostoOdiernoCb = " + val);
	GasInterfaceEnergyHome.GetCostoGiornaliero(GasCostiConsumi.DatiCostoGiornalieroCb);
};

GasCostiConsumi.DatiSuggerimentoCostiCb = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate

	if (val != null)
		GasCostiConsumi.suggerimento = val;
	else
		Log.alert(80, GasCostiConsumi.MODULE, "DatiSuggerimento null");

	Log.alert(80, GasCostiConsumi.MODULE, "DatiSuggerimento = " + val);
	GasCostiConsumi.VisSuggerimento("#DettaglioSuggerimentiCosti");
	hideSpinner();
	// attualmente la posizione attuale sulla tariffa lo aggiorno insieme agli
	// altri dati
	GasCostiConsumi.SetTariffa();
	
	// se e' la prima volta creo il timer per la lettura dei dati
	// faccio setInterval anziche' setTimeout ogni volta perche'
	// se va male una chiamata riparto comunque
	// non ci sono problemi di accavallamento perche' l'intervallo e' molto alto
	if (GasCostiConsumi.timerCosti == null)
		GasCostiConsumi.timerCosti = setInterval("GasCostiConsumi.GetDatiCosti()",
				GasCostiConsumi.TIMER_UPDATE_COSTI);

};

GasCostiConsumi.DatiListaElettrodomesticiCb = function(val) {
	GasCostiConsumi.listaElettr = val;
	// salvo id smartInfo
	if (val != null) {
		for (i = 0; i < val.length; i++) {
			if (val[i].tipo == GasInterfaceEnergyHome.SMARTINFO_APP_TYPE) {
				GasCostiConsumi.idSmartInfo = val[i].id;
				break;
			}
		}
	}
	GasCostiConsumi.GetDatiCosti();
};

GasCostiConsumi.GetListaElettrodomestici = function() {
	if (GasCostiConsumi.listaElettr == null) {
		Log.alert(80, GasCostiConsumi.MODULE, "GetListaElettrodomestici");
		GasInterfaceEnergyHome.GetListaElettr(GasCostiConsumi.DatiListaElettrodomesticiCb);
	} else
		GasCostiConsumi.GetDatiCosti();
};

/*******************************************************************************
 * avvia le richieste dei dati, che vengono fatte in sequenza perche' asincrone,
 * visualizzo i dati una volta sola quando li ho tutti
 ******************************************************************************/
GasCostiConsumi.GetDatiCosti = function() {
	Log.alert(80, GasCostiConsumi.MODULE, "GetDatiCosti");
	GasMain.ResetError();
	
	GasInterfaceEnergyHome.GetCostoOdierno(GasCostiConsumi.DatiCostoOdiernoCb);
	//GasInterfaceEnergyHome.GetCostoGiornaliero(GasCostiConsumi.DatiCostoGiornalieroCb);
};

/*******************************************************************************
 * crea la parte costante della grafica
 ******************************************************************************/
GasCostiConsumi.GetImgTariffa = function(data)
{
	// controlla se sabato o domenica
	day = data.getDay();
	if ((day == 0) || (day == 6))
		img = GasDefine.home["tariffaFestiva"];
	else
		img = GasDefine.home["tariffaFeriale"];
	d = data.getDate();
	m = data.getMonth();
	// controlla se giorno di festa nazionale
	for (i = 0; i < GasDefine.festivi.length; i++)
		if ((d == GasDefine.festivi[i][0]) && (m == GasDefine.festivi[i][1]))
		{
			img = GasDefine.home["tariffaFestiva"];
			break;
		}
	return img;
};

GasCostiConsumi.VisCosti = function() {
	// Creo zona costo
	// rendo quadrate le immagini

	dim = $("#CostoAttualeImg").height();
	$("#CostoAttualeImg").width(dim);
	wDiv = $("#CostoAttuale").width();
	$("#CostoAttualeImg").css("left", (wDiv - dim) / 2); // lo centra

	// se festivo metto una barra diversa, l'immagine della tariffa la
	// metto solo la prima volta
	if (GasCostiConsumi.tariffaImg == null) {
		GasCostiConsumi.tariffaImg = GasCostiConsumi.GetImgTariffa(GasMain.dataAttuale);
		
		coord = Utils.ResizeImg("TariffaImgDiv", GasCostiConsumi.tariffaImg,
				false, 2, 9);
		$("#TariffaImgDiv").html(
				"<img id='TariffaImg' src='" + GasCostiConsumi.tariffaImg
						+ "' width='" + coord[0] + "px' height='" + coord[1]
						+ "px' style='position:absolute;top:" + coord[2]
						+ "px;left:" + coord[3] + "px'>");
		GasCostiConsumi.leftTariffaPos = $("#TariffaPos").position().left;
	}
	// metto a posto fumetto
//	$("#CostoFumettoImg").attr("src", GasCostiConsumi.imgChat);
//	Utils.ResizeImg("CostoFumettoImg", GasCostiConsumi.imgChat, true, 1, 9);
	
	
	if (GasCostiConsumi.costoOdierno == null)
		txt = Msg.home["datoNonDisponibile"];
	else
		txt = (GasCostiConsumi.costoOdierno).toFixed(2) + " €";
	
	txtData = costoOdiernoData;
	txt1 = costoOdiernoFascia1;
	txt2 = costoOdiernoFascia2;
	txt3 = costoOdiernoFascia3;
//	$("#DettaglioCostoOdierno").html(
//			Msg.home["costoFinora"] + ":<br><br><b>"+ txt + "</b>");
	$("#DettaglioCostoOdiernoAl").html(Msg.home["costoAl"] + txtData);
	$("#DettaglioCostoOdiernoHaiSpeso").html(Msg.home["costoFinora"]);
	$("#DettaglioCostoOdiernoF1").html(txt1 + " €" + Msg.home["costoFascia1"] + "<img id='ConsumoVerdeImg' src='" + GasDefine.trial["tariffaQVerde"] + "'>");
	$("#DettaglioCostoOdiernoF2").html(txt2 + " €" + Msg.home["costoFascia2"] + "<img id='ConsumoGialloImg' src='" + GasDefine.trial["tariffaQGiallo"] + "'>");
	$("#DettaglioCostoOdiernoF3").html(txt3 + " €" + Msg.home["costoFascia3"] + "<img id='ConsumoRossoImg' src='" + GasDefine.trial["tariffaQRosso"] + "'>");
	
	
	if (GasCostiConsumi.costoPrevMese == null)
		txt = Msg.home["datoNonDisponibile"];
	else
		txt = (GasCostiConsumi.costoPrevMese).toFixed(2) + " €";
//	$("#DettaglioCostoPrevisto").html(
//			Msg.home["costoPrevisto"] + ": <br><b>"	+ txt + "</b>");
	$("#DettaglioCostoPrevistoTitle").html(Msg.home["costoPrevisto"] + ":");
	$("#DettaglioCostoPrevistoVal").html(txt);
	
	if (GasCostiConsumi.suggerimento != null)
		GasCostiConsumi.VisSuggerimento("#DettaglioSuggerimentiCosti");
	
	GasCostiConsumi.SetTariffa();
	// dopo aver impostato la parte grafica costante faccio le richieste
	// per la parte di dati
	GasCostiConsumi.GetListaElettrodomestici();

};

/*******************************************************************************
 * gestisce la visualizzazione delal schermata dei costi
 ******************************************************************************/
GasCostiConsumi.GestCosti = function() {
	// se simulazione o demo faccio vedere costi altrimento no
	if (GasInterfaceEnergyHome.mode == GasInterfaceEnergyHome.MODE_FULL)
		GasCostiConsumi.GestCostiVuoto();
	else {
		showSpinner();
		GasCostiConsumi.visType = GasCostiConsumi.COSTI;
		$("#Content").html(GasCostiConsumi.htmlContent["Costi"]);
		Log.alert(80, GasCostiConsumi.MODULE, "GasCostiConsumi.GestCosti");

		$('#CostoIndicatoreImg').gauge( {
			max : 2.0,
			color : 'yellow'
		});
		GasCostiConsumi.VisCosti();

	}
};

/*******************************************************************************
 * **************************************************************************
 * Gestione Consumi
 * **************************************************************************
 ******************************************************************************/

GasCostiConsumi.ExitConsumi = function() {
	Log.alert(80, GasCostiConsumi.MODULE, "GasCostiConsumi.ExitConsumi");
	if (GasCostiConsumi.timerPotenza != null) {
		clearTimeout(GasCostiConsumi.timerPotenza);
		GasCostiConsumi.timerPotenza = null;
	}
	if (GasCostiConsumi.timerConsumi != null) {
		clearTimeout(GasCostiConsumi.timerConsumi);
		GasCostiConsumi.timerConsumi = null;
	}
	GasInterfaceEnergyHome.Abort();
	$("#Content").html(null);
	GasMain.ResetError();
	hideSpinner();
};

/*******************************************************************************
 * funzione che calcola l'altezza della barra nel termometro dei consumi 
 * calcolo percentuale rispetto media
 * Dal 29-11-2011: sommo i valori giornalieri fino all'ora attuale o 
 * all'ultimo valore non null, sommo i valori medio per lo stesso numero di ore 
 * poi faccio il confronto. Se un'ora del giornaliero e' null non prendo il valore 
 * corrispondente della media, se una'ora della media e' null non faccio il confronto
 ******************************************************************************/
GasCostiConsumi.VisIndicatoreConsumi = function() {
	var medio, odierno, n, max;
	
	perc = 0;
	odierno = null;
	medio = null;
	/** calcolo consumo odierno dalla somma del consumo giornaliero
	if (GasCostiConsumi.consumoGiornaliero != null)
	{
		GasCostiConsumi.consumoOdierno = null;
		for (i= 0; i < GasCostiConsumi.consumoGiornaliero.length; i++)
		{
			if (GasCostiConsumi.consumoGiornaliero[i] != null)
				if (GasCostiConsumi.consumoOdierno == null)
					GasCostiConsumi.consumoOdierno = GasCostiConsumi.consumoGiornaliero[i];
				else
					GasCostiConsumi.consumoOdierno += GasCostiConsumi.consumoGiornaliero[i];
		}
		if (GasCostiConsumi.consumoOdierno != null)
			GasCostiConsumi.consumoOdierno = GasCostiConsumi.consumoOdierno / 1000; // passo in kW
	}
	else
		GasCostiConsumi.consumoOdierno = null;
	**/
	Log.alert(80, GasCostiConsumi.MODULE, "VisIndicatoreConsumi: consumoOdierno = " + GasCostiConsumi.consumoOdierno);

	if ((GasCostiConsumi.consumoMedio != null) && (GasCostiConsumi.consumoGiornaliero != null)
			&& (GasCostiConsumi.consumoOdierno != null))
	{
		odierno = 0;
		medio = 0;
		n = 0;
		max = GasCostiConsumi.consumoGiornaliero.length-1;
		// sommo consumo giornaliero e media per tutti valori non null
		// nel caso dovessi dare null per un valore mancante a meta' devo partire dalla fine
		// considero a parte l'ultima ora (ora attuale)
		for (i = 0; i < max; i++)
		{
			if (GasCostiConsumi.consumoGiornaliero[i] != null)
			{
				n++;
				// se media null per valore valido odierno non faccio confronto
				if (GasCostiConsumi.consumoMedio[i] == null)
				{
					odierno = null;
					break;
				}
				medio += GasCostiConsumi.consumoMedio[i];
				odierno += GasCostiConsumi.consumoGiornaliero[i];
			}
		}
		// se l'ultimo valore non e' null prendo una percentuale della media in base ai minuti attuali
		if ((medio != null) && (odierno != null))
		{
			if (GasCostiConsumi.consumoGiornaliero[max] != null)
			{
				if (GasCostiConsumi.consumoMedio[max] != null)
				{
					min = GasGestDate.GetActualDate().getMinutes();
					medio += GasCostiConsumi.consumoMedio[max] * (min / 60);
					odierno += GasCostiConsumi.consumoGiornaliero[max];
				}
				else
					medio = null;
			}
			if (medio != null)
			{
				perc = odierno / medio;
				if (perc > 2)
					perc = 2;
			}
		}
		
	}
	Log.alert(80, GasCostiConsumi.MODULE, "VisIndicatoreConsumi: medio = " + medio + 
			" odierno = " + odierno + " perc = " + perc);
	$('#ConsumoIndicatoreImg').gauge("value", perc);
};

// visualizza elettrodomestico che in questo momento sta consumando di piu'
GasCostiConsumi.VisConsumoMaggiore = function() {
	
	if (GasCostiConsumi.maxConsumoElettr != null) {
		if (GasCostiConsumi.maxConsumoElettr.value == 0)
		{
			$("#DettaglioConsumoMaggiore").html("<span id='MsgConsumoMaggiore'></span>");
			$("#MsgConsumoMaggiore").text(Msg.home["maxDisp0"]);
			Log.alert(20, GasCostiConsumi.MODULE, "VisConsumoMaggiore : 0");
		}
		else
		{
			
			$("#DettaglioConsumoMaggiore").html("<span id='TestoConsumoMaggiore'></span><img id='ConsumoMaggioreImg' src=''>");
			Log.alert(80, GasCostiConsumi.MODULE, "VisConsumoMaggiore : "
				+ GasCostiConsumi.maxConsumoElettr.icona);
			// metto immagine del device che sta consumando di piu'
			$("#ConsumoMaggioreImg").attr("src",
					GasDefinePath.imgDispPath + GasCostiConsumi.maxConsumoElettr.icona);
			// il consumo e' in watt
			$("#TestoConsumoMaggiore").text(
					GasCostiConsumi.maxConsumoElettr.nome + " ("
					+ Math.round(GasCostiConsumi.maxConsumoElettr.value) + " W)");
			if (GasCostiConsumi.dimMaxDispImg == -1) {
				wDiv = $("#ConsumoMaggioreImg").width();
				hDiv = $("#ConsumoMaggioreImg").height();
				offsetTop = $("#ConsumoMaggioreImg").offset().top;
				offsetLeft = $("#ConsumoMaggioreImg").offset().left;

				// imposto dimensioni e offset img in px
				if (wDiv > hDiv)
					GasCostiConsumi.dimMaxDispImg = (hDiv * 0.9);
				else
					GasCostiConsumi.dimMaxDispImg = (wDiv * 0.9);
			}
			$("#ConsumoMaggioreImg").width(GasCostiConsumi.dimMaxDispImg);
			$("#ConsumoMaggioreImg").height(GasCostiConsumi.dimMaxDispImg);
		}
	} else {
		$("#DettaglioConsumoMaggiore").html("<span id='MsgConsumoMaggiore'></span>");
		$("#MsgConsumoMaggiore").text(Msg.home["noMaxDisp"]);
		Log.alert(20, GasCostiConsumi.MODULE, "VisConsumoMaggiore : null");
	}
};

GasCostiConsumi.VisGrafico = function() {
	var dati1 = new Array();

	Log.alert(80, GasCostiConsumi.MODULE, "VisGrafico");
	$.jqplot.config.enablePlugins = true;
	$("#DettaglioGraficoConsumoOdierno").html(null);
	barW = 0;
	max = GasDefine.home["limConsumoOra"][GasMain.contatore];  // TODO sistemare questo.
	if (GasCostiConsumi.consumoGiornaliero != null) 
		dati = GasCostiConsumi.consumoGiornaliero;
	else
		// creo array di dati vuoti
		dati = new Array(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);

	// 24 ore piu' un po' di spazio
	barW = $("#DettaglioGraficoConsumoOdierno").width() / 28; 
	
	// controllo se sto nel limite massimo altrimenti lo sposto
	for (i = 0; i < dati.length; i++) {
		dati1[i] = new Array();
//		dati1[i][1] = dati[i] / 1000;
//		dati1[i][1] = dati[i] * 10;
		dati1[i][1] = dati[i] * 1;
		dati1[i][0] = i + 0.5;
		if (dati1[i][1] >= max)
		{
			max = dati1[i][1] * 1.05;
			Log.alert(20, GasCostiConsumi.MODULE, "VisGrafico: max = " + max + " dato = " + dati1[i][1]);
		}
	}
	Log.alert(50, GasCostiConsumi.MODULE, "VisGrafico : num dati = "
			+ dati.length + " max = " + max);
	plot1 = $.jqplot("DettaglioGraficoConsumoOdierno", [ dati1 ], {
		title : {
			text : "Consumo del giorno precedente",// "Consumo Odierno (smc)", // title for the plot,
			textColor : '#3a8848',
			size : '0.8em',
			show : true
		},
		legend : {
			show : false
		},
		seriesDefaults : {
			color : "#0B0B96", // "#2020ff",
			renderer : $.jqplot.BarRenderer,
			rendererOptions : {
				barMargin : 2,
				barPadding : 0,
				barWidth : barW
			}

		},
		axes : {
			xaxis : {
				min : 0,
				max : 24,
				numberTicks : 13,
				tickOptions : {
					formatString : "%i",
					textColor : '#000000',
					fontSize : '0.8em'
				},
				label : 'ora'
			},
			yaxis : {
				min : 0,
//				max : max.toFixed(1),
				max : 1,
				numberTicks : 5,
				label : 'smc',
				tickOptions : {
					formatString : "%.1f",
					textColor : '#000000',
					fontSize : '0.8em'
				},
				autoscale : true
			}
		}
		});
		$('#DettaglioGraficoConsumoOdierno').bind('jqplotDataHighlight', 
            function (ev, seriesIndex, pointIndex, data) {
                //$('#info1').html('series: '+seriesIndex+', point: '+pointIndex+', data: '+data);
			$(".highlightbar").remove(); // rimuovo quello precedente
			html = "<div class='highlightbar'>" + data[1].toFixed(3) + " kWh</div>";
			$('#DettaglioGraficoConsumoOdierno').append(html);
			$(".highlightbar").css("left", pointIndex*barW + "px");
			distTop = (max - data[1]) * 150;
			Log.alert(20, GasCostiConsumi.MODULE, 'click -> series: '+seriesIndex+', point: '+pointIndex+
					', data: '+data+' top: '+distTop);
			
			$(".highlightbar").css("top", distTop + "px");
			}
        );
		$('#DettaglioGraficoConsumoOdierno').bind('jqplotDataUnhighlight', 
            function (ev) {
              $(".highlightbar").remove();
            }
        );
};
 
GasCostiConsumi.GetImgPower = function() {
	var indImg;
	
	if (GasCostiConsumi.potenzaAttuale == null)
		indImg = 0;
	else
		indImg = Math.floor(GasCostiConsumi.potenzaAttuale / 4000 * 81);

	// temporaneo per ovviare a parte rossa troppo piccola
//	if (indImg >= GasDefine.home.gauge.length - 1)
//		indImg = GasDefine.home.gauge.length - 1;
	var imgPower = GasDefine.home.gauge[indImg];
	Log.alert(20, GasCostiConsumi.MODULE, "GetImgPower: ind = " + indImg
			+ " power = " + GasCostiConsumi.potenzaAttuale + " img = " + imgPower);
	return GasCostiConsumi.pathImgPower + imgPower;
};

GasCostiConsumi.SetConsumoImg = function() {
	/**if (GasCostiConsumi.dimConsumoImg == -1) {
		hDiv = $("#ConsumoAttualeMeter").height();
		$("#ConsumoAttualeMeter").width(hDiv);
	}
	**/
// aggiorno il PowerMeter
	
	
	if (GasCostiConsumi.consumoGiornoPrec == null)
	{
		val = 0;
		$("#ValConsumoAttuale").html(Msg.home["datoNonDisponibile"]);
	}
	else
	{
		val = GasCostiConsumi.consumoGiornoPrec;
//		$("#ValConsumoAttuale").html((GasCostiConsumi.potenzaAttuale / 1000.0).toFixed(3) + " kW");
		$("#ValConsumoAttuale").html(GasCostiConsumi.consumoGiornoPrec + " smc");
	}
	//hDiv = $("#ConsumoAttualeMeter").height();
	//$("#ConsumoAttualeMeter").width(hDiv);
//	val = val / 1000.0;
	// segnalo sovraccarico (zona gialla) e sovraccarico grave(zona rossa) dello speedometer
//	if (val > GasDefine.home["contatoreOk"][GasMain.contatore])
//	{
//		if (val > GasDefine.home["contatoreWarn"][GasMain.contatore])
//			$("#ValConsumoAttuale").css("color", "red");
//		else
//			$("#ValConsumoAttuale").css("color", "orange");
//		if (GasCostiConsumi.timerBlink == null)
//		{
//			$("#ValConsumoAttuale").addClass("invisibleDiv")
//			GasCostiConsumi.timerBlink = setInterval("GasCostiConsumi.BlinkVal()",
//				GasCostiConsumi.TIMER_BLINK);
//		}
//	}
//	else
//	{
//		clearInterval(GasCostiConsumi.timerBlink);
//		GasCostiConsumi.timerBlink = null;
//		$("#ValConsumoAttuale").css("color", "black");
//		$("#ValConsumoAttuale").removeClass("invisibleDiv");
//	}
//	$('#ConsumoAttualeMeter').speedometer("value", val, "kW");

};

GasCostiConsumi.BlinkVal = function() {
	$("#ValConsumoAttuale").toggleClass("invisibleDiv");
	
};

GasCostiConsumi.DatiMaxElettr = function(elem) {
	GasCostiConsumi.maxConsumoElettr = elem;
	GasCostiConsumi.VisConsumoMaggiore();
	if (GasCostiConsumi.timerPotenza == null)
		GasCostiConsumi.timerPotenza = setInterval("GasCostiConsumi.GetDatiPotenza()",
				GasCostiConsumi.TIMER_UPDATE_POWER_METER);
};


GasCostiConsumi.DatiConsumoPrevistoCb = function(val) {
//	if (val != null)
//		GasCostiConsumi.consumoPrevisto = Math.round(val / 1000); // da w a kW
//	else 
//		GasCostiConsumi.consumoPrevisto = null;
	if (val != null)
		GasCostiConsumi.consumoPrevisto = val; 
	else 
		GasCostiConsumi.consumoPrevisto = null;
	Log.alert(80, GasCostiConsumi.MODULE, "DatiConsumoPrevisto val = " + val);
	if (GasCostiConsumi.consumoPrevisto != null)
		txt = Math.round(GasCostiConsumi.consumoPrevisto) + " smc";
	else
		txt = Msg.home["datoNonDisponibile"];
//	$("#DettaglioConsumoPrevisto").html(Msg.home["consumoPrevisto"] + ":<br><br> <b>" + txt + "</b>");
//	$("#DettaglioConsumoPrevisto").html(Msg.home["consumoPrevisto"] + ":<br><span id='DettConsPrevVal'>" + txt + "</span>");
	$("#DettConsPrevTitle").html(Msg.home["consumoPrevisto"] + ":");
	$("#DettConsPrevVal").html(txt);		

	GasInterfaceEnergyHome.GetSuggerimento(GasCostiConsumi.DatiSuggerimentoConsumi);
};

GasCostiConsumi.DatiConsumoMedioCb = function(val) {
	GasCostiConsumi.consumoMedio = val;
	Log.alert(80, GasCostiConsumi.MODULE, "DatiConsumoMedio = " + val);

	GasCostiConsumi.VisIndicatoreConsumi();
	/**
	if (GasCostiConsumi.consumoOdierno != null)
		txt = (GasCostiConsumi.consumoOdierno).toFixed(2) + " kWh";
	else
		txt = Msg.home["datoNonDisponibile"];
	$("#DettaglioConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + txt + "</b>");
	**/
	GasInterfaceEnergyHome.GetConsumoPrevisto(GasCostiConsumi.DatiConsumoPrevistoCb);
	
};

GasCostiConsumi.DatiConsumoOdiernoCb = function(val) {
	GasCostiConsumi.consumoOdierno = val;
	Log.alert(80, GasCostiConsumi.MODULE, "DatiConsumoOdiernoCb = " + val);

	if (GasCostiConsumi.consumoOdierno != null)
//		txt = (GasCostiConsumi.consumoOdierno / 1000).toFixed(2) + " smc";
//		txt = GasCostiConsumi.consumoOdierno + " smc";
		txt = tsUltimaLettura;
	else
		txt = Msg.home["datoNonDisponibile"];
//	$("#DettaglioConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + txt + "</b>");
	
//	$("#DettaglioConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><span id='DettConsOggiVal'>" + txt + "</span>");
	$("#DettConsOggiTitle").html(Msg.home["consumoFinora"] + ":");
	$("#DettConsOggiVal").html(txt);

	GasInterfaceEnergyHome.GetConsumoMedio(GasCostiConsumi.DatiConsumoMedioCb);
	
};

GasCostiConsumi.DatiConsumoGiornalieroCb = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	GasCostiConsumi.consumoGiornaliero = val;
	hideSpinner();
	Log.alert(80, GasCostiConsumi.MODULE, "DatiConsumoGiornaliero ");
	GasCostiConsumi.VisGrafico();
	
	showSpinner();
	GasInterfaceEnergyHome.GetConsumoOdierno(GasCostiConsumi.DatiConsumoOdiernoCb);
};

GasCostiConsumi.DatiSuggerimentoConsumi = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate

	if (val != null)
		GasCostiConsumi.suggerimento = val;
	else
		Log.alert(80, GasCostiConsumi.MODULE, "DatiSuggerimentoConsumi null");
	hideSpinner();
	Log.alert(80, GasCostiConsumi.MODULE, "DatiSuggerimentoConsumi = " + val);
	GasCostiConsumi.VisSuggerimento("#DettaglioSuggerimentiConsumi");
	// se e' la prima volta creo il timer per la lettura dei dati
	// faccio setInterval anziche' setTimeout ogni volta perche'
	// se va male una chiamata riparto comunque
	// non ci sono problemi di accavallamento perche' l'intervallo e' molto alto
	if (GasCostiConsumi.timerConsumi == null)
		GasCostiConsumi.timerConsumi = setInterval("GasCostiConsumi.GetDatiConsumi()",
				GasCostiConsumi.TIMER_UPDATE_CONSUMI);
};

GasCostiConsumi.DatiPotenzaAttuale = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	Log.alert(80, GasCostiConsumi.MODULE, "DatiPotenzaAttuale val = " + val);
	
	GasCostiConsumi.consumoGiornoPrec = val;
	
	GasCostiConsumi.SetConsumoImg();
	// se e' la prima volta creo il timer per la lettura dei dati
	// faccio setInterval anziche' setTimeout ogni volta perche'
	// se va male una chiamata riparto comunque
	// non ci sono problemi di accavallamento perche' l'intervallo e' molto alto
//	GasInterfaceEnergyHome.GetMaxElettr(GasCostiConsumi.DatiMaxElettr);  visualizza il consumo massimo fra i dispositivi 

};

GasCostiConsumi.GetDatiPotenza = function() {
	Log.alert(80, GasCostiConsumi.MODULE, "GetDatiPotenza");
	// non tolgo togliere messaggio errore da piattaforma
	if (GasInterfaceEnergyHome.visError != GasInterfaceEnergyHome.ERR_CONN_SERVER)
		GasMain.ResetError();
	GasInterfaceEnergyHome.GetPotenzaAttuale(GasCostiConsumi.DatiPotenzaAttuale);
};

/*******************************************************************************
 * avvia le richieste dei dati, che vengono fatte in sequenza perche' asincrone,
 * visualizzo i dati una volta sola quando li ho tutti
 ******************************************************************************/
GasCostiConsumi.GetDatiConsumi = function() {
	Log.alert(80, GasCostiConsumi.MODULE, "GetDatiConsumi");
	GasMain.ResetError();
	GasInterfaceEnergyHome.GetConsumoGiornaliero(GasCostiConsumi.DatiConsumoGiornalieroCb); // stampa il grafico dei consumi dell'ultimo giorno
	
};

/*******************************************************************************
 * crea la parte costante della grafica
 ******************************************************************************/
GasCostiConsumi.VisConsumi = function() {

	// metto a posto fumetto
//	$("#ConsumoFumettoImg").attr("src", GasCostiConsumi.imgChat);
//	Utils.ResizeImg("ConsumoFumettoImg", GasCostiConsumi.imgChat, true, 1, 9);
	
	if (GasCostiConsumi.consumoOdierno != null)
		txt = (GasCostiConsumi.consumoOdierno /1000).toFixed(3) + " kWh";
	else
		txt = Msg.home["datoNonDisponibile"];
//	txt = "28 Marzo 2012 alle ore 12:00 (ieri)"; // TODO raccattare i dati
//	$("#DettaglioConsumoOdierno").html(Msg.home["consumoFinora"] + "<br><br> <b>" + txt + "</b>");
	$("#DettConsOggiTitle").html(Msg.home["consumoFinora"] + ":");
	$("#DettConsOggiVal").html(txt);	
	
	if (GasCostiConsumi.consumoPrevisto != null)
		txt = GasCostiConsumi.consumoPrevisto + " smc";
	else
		txt = Msg.home["datoNonDisponibile"];
//	txt = "16200 standard metri cubi";
//	$("#DettaglioConsumoPrevisto").html(Msg.home["consumoPrevisto"] + ":<br><br> <b>" + txt + "</b>");
	$("#DettConsPrevTitle").html(Msg.home["consumoPrevisto"] + ":");
	$("#DettConsPrevVal").html(txt);	
	
	// Qui ci va la visualizzazione delle offerte/messaggi pubblicitari
//	if (GasCostiConsumi.suggerimento != null)
//		GasCostiConsumi.VisSuggerimento("#DettaglioSuggerimentiConsumi");
	// dopo aver impostato la parte grafica costante faccio le richieste
	// per la parte di dati
	
	GasCostiConsumi.GetDatiPotenza();

	GasCostiConsumi.GetDatiConsumi();
};

/*******************************************************************************
 * gestisce la visualizzazione delal schermata dei consumi
 ******************************************************************************/

GasCostiConsumi.GestConsumi = function() {
	GasCostiConsumi.visType = GasCostiConsumi.CONSUMI;
	
	showSpinner();
	$("#Content").html(GasCostiConsumi.htmlContent["Consumi"]);
	Log.alert(80, GasCostiConsumi.MODULE, "GasCostiConsumi.GestConsumi ");

	$('#ConsumoIndicatoreImg').gauge({
		max : 2.0
	});
	maxCont = GasDefine.home["limContatore"][GasMain.contatore];
	Log.alert(30, GasCostiConsumi.MODULE, "GestConsumi: max contatore = " + maxCont);
//	hDiv = $("#ConsumoAttualeMeter").height();
//	$("#ConsumoAttualeMeter").width(hDiv);
//	txt = "16151,939"; // TODO prendere il consumo
//	$('#ConsumoAttualeMeter').html("<br><br> <b>" + "<br>" + txt + "</br>" + Msg.home["labelGas"] + "</b>");

	GasCostiConsumi.VisConsumi();
};
