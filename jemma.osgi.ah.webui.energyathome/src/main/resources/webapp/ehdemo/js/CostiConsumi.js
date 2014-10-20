var CostiConsumi = {
	MODULE : "CostiConsumi",
	CONSUMI : 1,
	COSTI : 2,
	visType : 0,
	listaElettr : null, // lista degli elettrodomestici per avere l'associazione id:nome per la torta
	idSmartInfo : null,
	notizie: null,
	notizieid : 0,
	simulaRSS : false,
	
	// consumo

	consumoOdierno : null,
	consumoMedio : null,
	consumoPrevMese : null,
	consumoGiornaliero : null,
	timerConsumi : null,
	TIMER_UPDATE_CONSUMI : 300000, 
	potenzaAttuale : null,
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
	indicatoreImgSotto : Define.home["termSfondo"],
	indicatoreImgSopra : Define.home["termSopra"],
	imgChat : Define.home["iconaSugg"],
	tariffaImg : null,
	leftTariffaPos : 0,
	costoOdiernoImg : [ Define.home["costoVerde"], Define.home["costoGiallo"], 
	                    Define.home["costoRosso"], Define.home["costoGrigio"]],
	costoOdiernoMsg : [ Msg.home["costoVerde"], Msg.home["costoGiallo"], 
	                    Msg.home["costoRosso"], Msg.home["costoVuoto"] ],

	suggerimento : null,
	htmlContent : {

		"Costi" : "<div id='TitoloCosti' class='ContentTitle'>" + Msg.home["costi"] + "</div>" + 
		"<div id='CostoSintesi'> "+
		"<div id='CostoAttualeTitolo' class='TitoloDettaglio'>" + Msg.home["titoloCosti"] + "</div>" + 
		"<div id='CostoAttuale'>" + 
		"<img id='CostoAttualeImg' src='" + Define.home["costoGrigio"] + "'><span id='DettaglioCosto'></span></div>" + 
		"<div id='CostoOdierno'><span id='DettaglioCostoOdierno'>" + Msg.home["costoFinora"] + "</span></div>" + 
		"<div id='CostoPrevisto'><span id='DettaglioCostoPrevisto'>" + Msg.home["costoPrevisto"] + "</span></div>" + 
		"<div id='CostoIndicatoreTitolo' class='TitoloDettaglio'>" + Msg.home["indicatoreCosti"] + "</div>" + 
		"<div id='CostoIndicatore'><div id='IndicatoreSopra' class='IndicatoreTxt'>" + Msg.home["indicatoreSopra"] +  "</div>" + 
		"<div id='IndicatoreMedia' class='IndicatoreTxt'>"	+ Msg.home["indicatoreMedia"] + "</div> " + 
		"<div id='IndicatoreSotto' class='IndicatoreTxt'>" + Msg.home["indicatoreSotto"] + "</div>" + 
		"<div id='ConsumoIndicatorePaddingLeft'></div>" + 
		"<div id='CostoIndicatoreImg'></div></div></div>" +
		"<div id='CostoInfo'> "+
		"<span id='CostoInfoTitolo' class='TitoloDettaglio'>"	+ Msg.home["spesaMensile"] + "</span>"	+ 
		"<div id='CostoSuddivisione'><div id='DettaglioSuddivisioneCosti'></div></div>" + 
		"<div id='CostoTariffa'><span id='CostoTitoloTariffa'>" + Msg.home["tariffa"]	+ "</span>"	+ 
		"<div id='TariffaImgDiv'><img id='TariffaImg' src='"	+ Define.home["tariffaFeriale"] + "'></div><div id='TariffaPos'></div></div>" + 
		"<div id='ConsumoSuggerimento'><img src='" + Define.home["sfondoSugg"] + "' id='SuggerimentoImg'>" +
		"<div id='ConsumoTitoloSuggerimento'>" + Msg.home["suggerimenti"] +
		"<button id='nextNews'>next</button><button id='backNews'>go back</button>"+
		"</div>" + 
		"<div id='DettaglioSuggerimentiConsumi'>" +
		"<div id='PrimaNews'>"+
		"<a target = '_blank'><div class ='titoloNews'><span class='ellipsis_text'></span></div></a>"+
		"<div class='dettaglioNews'><span class='ellipsis_text'></span></div>"+
		"</div>"+
		"<div id='SecondaNews'>"+
		"<a target = '_blank'><div class ='titoloNews'><span class='ellipsis_text'></span></div></a>"+
		"<div class='dettaglioNews'><span class='ellipsis_text'></span></div>"+
		"</div>"+
		"</div></div>"
		,

		"Consumi" : 
			"<div id='TitoloConsumi' class='ContentTitle'>"	+ Msg.home["consumi"] + 
			"</div>" + 
			"<div id='ConsumoSintesi'>" +
			  
			  "<div id='ConsumoAttualeTitolo' class='TitoloDettaglio'>" + Msg.home["titoloConsumi"] +
			  "</div>" + 
			  "<div id='ConsumoAttuale'>" +
			     "<span id='ValConsumoAttuale'>" +
			     "</span>" +
			     "<div id='ConsumoAttualeMeter'>" +
			     "</div>" +
			  "</div>" +
			  "<div id='ConsumoOdierno'>" +
			     "<span id='DettaglioConsumoOdierno'>" +
			     "</span>" +
			  "</div>" +
			  "<div id='ConsumoPrevisto'>" +
			     "<span id='DettaglioConsumoPrevisto'>" +
			     "</span>" +
			  "</div>" +
			  "<div id='ConsumoIndicatoreTitolo' class='TitoloDettaglio'>" + Msg.home["indicatoreConsumi"] + 
			  "</div>" +
			  "<div id='ConsumoIndicatore'>" +
			     "<div id='IndicatoreSopra' class='IndicatoreTxt'>"	+ Msg.home["indicatoreSopra"] + 
			     "</div>" + 
			     "<div id='IndicatoreMedia' class='IndicatoreTxt'>" + Msg.home["indicatoreMedia"] + 
			     "</div>" + 
			     "<div id='IndicatoreSotto' class='IndicatoreTxt'>" + Msg.home["indicatoreSotto"] + 
			     "</div>" + 
			     "<div id='ConsumoIndicatorePaddingLeft'>" +
			     "</div>" +
			     "<div id='ConsumoIndicatoreImg'>" +
			     "</div>" +
			  "</div>" +
			"</div>" +
			"<div id='ConsumoInfo'>" +
			   "<span id='ConsumoInfoTitolo' class='TitoloDettaglio'>" + Msg.home["consumoOdierno"] +
			   "</span>" +
			   
			      "<div id='GraficoConsumoOdierno'>" +
			         "<div id='LabelKWH'>" + Msg.home["labelkWh"] + 
			         "</div>" +
			         "<div id='LabelOra'>" + Msg.home["labelOra"] + 
			         "</div>" +
			         "<div id='DettaglioGraficoConsumoOdierno'>" +
			         "</div>" +
			      "</div>" + 
			   "<div id='ConsumoMaggiore'>" +
			      "<span id='ConsumoTitoloMaggiore'>"	+ Msg.home["consumoMaggiore"] + 
			      "</span>" + 
			 
			      "<div id='DettaglioConsumoMaggiore'>" +
			      "</div>" +
			   "</div>" +
			   "<div id='ConsumoSuggerimento'>" +
			   "<div id='ConsumoTitoloSuggerimento'>" + Msg.home["suggerimenti"] +
			      "<button id='nextNews'>next</button>" +
			      "<button id='backNews'>go back</button>"+
			   "</div>" + 
			   "<div id='DettaglioSuggerimentiConsumi'>" +
			      "<div id='PrimaNews'>"+
			         "<a target = '_blank'>" +
			         "<div class ='titoloNews'>" +
			    
			               "<span class='ellipsis_text'>" +
			               "</span>" +
			             "</div>" +
			         "</a>"+
			         "<div class='dettaglioNews'>" +
			            "<span class='ellipsis_text'>" +
			            "</span>" +
			         "</div>"+
			      "</div>"+
			      "<div id='SecondaNews'>"+
			         "<a target = '_blank'>" +
			            "<div class ='titoloNews'>" +
			               "<span class='ellipsis_text'>" +
			               "</span>" +
			            "</div>" +
			         "</a>"+
			         "<div class='dettaglioNews'>" +
			            "<span class='ellipsis_text'>" +
			            "</span>" +
			         "</div>"+
			      "</div>"+
			   "</div>" +
			"</div>"
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
	pathImgPower : DefinePath.imgPowerMeterPath
}



/*******************************************************************************
 * Gestione Costi
 ******************************************************************************/

/*******************************************************************************
 * gestisce la visualizzazione delal schermata dei costi
 ******************************************************************************/
CostiConsumi.GestCosti = function() {
	
		showSpinner();
		CostiConsumi.visType = CostiConsumi.COSTI;
		$("#Content").html(CostiConsumi.htmlContent["Costi"]);
		Log.alert(80, CostiConsumi.MODULE, "CostiConsumi.GestCosti");

		$('#CostoIndicatoreImg').gauge( {
			max : 2.0,
			color : 'yellow'
		});
		
		if ($.browser.msie){
			$('#ConsumoSuggerimento,#CostoInfo,#CostoSintesi').corner();
			$('button').corner('3px');}
		
		$( "#backNews" ).button({
			text: false,
			icons: {
				primary: "ui-icon-seek-prev"
			}
		}).click(function(){
			CostiConsumi.notizieid=CostiConsumi.notizieid-2;
			if(CostiConsumi.notizieid<0)
				CostiConsumi.notizieid = CostiConsumi.notizie.length-2;
			    
			CostiConsumi.caricafeed();
		}
		
		
		);
		
		
		$( "#nextNews" ).button({
			text: false,
			icons: {
				primary: "ui-icon-seek-next"
			}
		}).click(function(){
			CostiConsumi.notizieid=CostiConsumi.notizieid+2;
			if(CostiConsumi.notizieid>=CostiConsumi.notizie.length)
				CostiConsumi.notizieid = 0;
			
			CostiConsumi.caricafeed();
		}
		
		
		);
		
		CostiConsumi.VisCosti();

	
}


CostiConsumi.ExitCosti = function() {
	
		Log.alert(80, CostiConsumi.MODULE, "CostiConsumi.ExitCosti");
		Main.ResetError();
		hideSpinner();
		if (CostiConsumi.timerCosti != null) {
			clearInterval(CostiConsumi.timerCosti);
			CostiConsumi.timerCosti = null;
		}
		InterfaceEnergyHome.Abort();
		CostiConsumi.tariffaImg = null;
		$("#Content").html(null);

	
}

CostiConsumi.VisCosti = function() {
	

	dim = $("#CostoAttualeImg").height();
	$("#CostoAttualeImg").width(dim);
	wDiv = $("#CostoAttuale").width();
	$("#CostoAttualeImg").css("left", (wDiv - dim) / 2);

	// se festivo metto una barra diversa, l'immagine della tariffa la
	// metto solo la prima volta
	if (CostiConsumi.tariffaImg == null) {
		CostiConsumi.tariffaImg = CostiConsumi.GetImgTariffa(Main.dataAttuale);
		
		coord = Utils.ResizeImg("TariffaImgDiv", CostiConsumi.tariffaImg,
				false, 2, 9);
		$("#TariffaImgDiv").html(
				"<img id='TariffaImg' src='" + CostiConsumi.tariffaImg
						+ "' width='" + coord[0] + "px' height='" + coord[1]
						+ "px' style='position:absolute;top:" + coord[2]
						+ "px;left:" + coord[3] + "px'>");
		CostiConsumi.leftTariffaPos = $("#TariffaPos").position().left;
	}
	// metto a posto fumetto
	$("#CostoFumettoImg").attr("src", CostiConsumi.imgChat);
	Utils.ResizeImg("CostoFumettoImg", CostiConsumi.imgChat, true, 1, 9);
	
	
	if (CostiConsumi.costoOdierno == null)
		txt = Msg.home["datoNonDisponibile"];
	else
		txt = (CostiConsumi.costoOdierno).toFixed(2) + " �";
	$("#DettaglioCostoOdierno").html(
			Msg.home["costoFinora"] + ":<br><br><b>"+ txt + "</b>");
	
	if (CostiConsumi.costoPrevMese == null)
		txt = Msg.home["datoNonDisponibile"];
	else
		txt = (CostiConsumi.costoPrevMese).toFixed(2) + " �";
	$("#DettaglioCostoPrevisto").html(
			Msg.home["costoPrevisto"] + ": <br><br><b>"	+ txt + "</b>");
	
	
	CostiConsumi.SetTariffa();
	// dopo aver impostato la parte grafica costante faccio le richieste
	// per la parte di dati
	CostiConsumi.GetListaElettrodomestici();

}

CostiConsumi.CalcCostoImg = function() {
	if (CostiConsumi.dimCostoImg == -1) {
		wDiv = $("#CostoAttuale").width();
		hDiv = $("#CostoAttuale").height();
		offsetTop = $("#CostoAttuale").offset().top;
		offsetLeft = $("#CostoAttuale").offset().left;

		// imposto dimensioni e offset img in px
		if (wDiv > hDiv)
			CostiConsumi.dimCostoImg = (hDiv * 0.4);
		else
			CostiConsumi.dimCostoImg = (wDiv * 0.4);
		CostiConsumi.topCostoImg = Math.floor((hDiv - CostiConsumi.dimCostoImg) / 2) * 0.9;
		CostiConsumi.leftCostoImg = Math.floor((wDiv - CostiConsumi.dimCostoImg) / 2);
	}
}

/*******************************************************************************
 * mette check che indica posizione sulla barra della tariffa in base all'ora
 * attuale
 ******************************************************************************/
CostiConsumi.SetTariffa = function() {
	// calcolo posizionamento sulla barra del rettangolo che indica l'ora
	// tiene conto di come e' fatta l'imamgine della tariffa
	w = $("#TariffaImg").width();
	ore = Main.dataAttuale.getHours();
	// ogni quadratino della tariffa e' largo 13px e la distanza e' 5 px per
	// width img = 432
	dimQ = (w / 432) * 13.13;
	dimS = (w / 432) * 5;
	val = (ore * dimQ) + (dimS * ore) + CostiConsumi.leftTariffaPos;
	$("#TariffaPos").css("left", Math.round(val) + "px");
	Log.alert(80, CostiConsumi.MODULE, "SetTariffa w = " + w + " left = "
			+ CostiConsumi.leftTariffaPos + " val = " + val);
}

/*******************************************************************************
 * scrive un suggerimento tipo indica se per i costi o i consumi divSuggerimento
 * indica il div in cui scrivere attualmente e' fisso, viene scritto solo la
 * prima volta che si accede alla pagina, ma potrebbe ruotare tra un insieme o,
 * in futuro, essere richiesto al server
 ******************************************************************************/


// dati un id, trova nella listaElettr il nome
CostiConsumi.TrovaNomePerId = function(id) {
	if (CostiConsumi.listaElettr != null) {
		for (k = 0; k < CostiConsumi.listaElettr.length; k++) {
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
	else if (a[1] > b[1])
		return -1;
	else
		return 1;
}

/*******************************************************************************
 * Torta distribuzione costi Per adesso solo i 4 valori maggiori
 * Se un elemento ha meno del 5% non lo visualizzo
 ******************************************************************************/
CostiConsumi.VisSuddivisioneCosti = function() {
	var tot, sum, altro, indTot;
	
	if (CostiConsumi.suddivisioneCosti != null) {
		// ordino la lista per valori decrescenti, in testa avro' il consumo
		// totale (smart info)
		//CostiConsumi.suddivisioneCosti.sort(CostiConsumi.CompareVal);

		lista = CostiConsumi.suddivisioneCosti;
		sum = 0;
		tot = 0;
		altro = 0;
		indTot = -1;
		
		// PER SIMULAZIONE USO DIRETTAMENTE IL NOME
		// sostituisci id con nome e calcola somma valori
		// Dal 27-1-2012 prendo tutti i valori, taglio solo quelli minori del 2%
		// se c'e' smart plug quello e' il totale, per altri e' (totale - somma disp)
		// se non c'e' smart info non prendo il totale
		if (InterfaceEnergyHome.mode > 1) {
			for (i = 0; i < lista.length; i++) // i = 0
			{
				if (lista[i][0] == CostiConsumi.idSmartInfo)
				{
					if (lista[i][1] != null)
						tot = lista[i][1];
					indTot = i;
				}
				else
				{
					lista[i][0] = CostiConsumi.TrovaNomePerId(lista[i][0]);
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
			//percMin = CostiConsumi.suddivisioneCosti[0][1] * 0.02;
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
						Log.alert(30, CostiConsumi.MODULE, "VisSuddivisioneCosti: smart plug valore null");
					else
						Log.alert(30, CostiConsumi.MODULE, "VisSuddivisioneCosti: smart plug valore inferiore alla somma dei dispositivi");
					
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
				
			Log.alert(80, CostiConsumi.MODULE, "VisSuddivisioneCosti n = " + listaCorta.length);
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
			try {
				chart = $.jqplot('DettaglioSuddivisioneCosti', [ listaCorta ],
						optionsObj);
			} catch (err) {
				Log.alert(40, CostiConsumi.MODULE, "VisSuddivisioneCosti = "
						+ err.toString());
			}
			return;
		}
	} 
	$("#DettaglioSuddivisioneCosti").html("<div id='SuddivisioneCostiVuoto'>" + 
				Msg.home["suddivisioneVuoto"] + "</div>");
	Log.alert(40, CostiConsumi.MODULE, "VisSuddivisioneCosti nessun dato");
	
}


/*******************************************************************************
 * funzione che calcola l'altezza della barra nel termometro dei costi 
 * calcolo percentuale rispetto media
 * Dal 29-11-2011: sommo i valori giornalieri fino all'ora attuale o 
 * all'ultimo valore non null, sommo i valori medio per lo stesso numero di ore 
 * poi faccio il confronto. Se un'ora del giornaliero e' null non prendo il valore 
 * corrispondente della media, se una'ora della media e' null non faccio il confronto
 ******************************************************************************/
CostiConsumi.VisIndicatoreCosti = function() {
	var medio, odierno, n, max;
	
	perc = 0;
	odierno = null;
	medio = null;
	/* calcolo costo odierno dalla somma del costo giornaliero
	if (CostiConsumi.costoGiornaliero != null)
	{
		CostiConsumi.costoOdierno = null;
		for (i= 0; i < CostiConsumi.costoGiornaliero.length; i++)
		{	
			if (CostiConsumi.costoGiornaliero[i] != null)
				if (CostiConsumi.costoOdierno == null)
					CostiConsumi.costoOdierno = CostiConsumi.costoGiornaliero[i];
				else
					CostiConsumi.costoOdierno += CostiConsumi.costoGiornaliero[i];
		}
	}
	else
		CostiConsumi.costoOdierno = null;
	*/
	if ((CostiConsumi.costoMedio != null) && (CostiConsumi.costoGiornaliero != null) && 
			(CostiConsumi.costoOdierno != null))
	{
		n = 0;
		odierno = 0;
		medio = 0;
		max = CostiConsumi.costoGiornaliero.length-1;
		// sommo consumo giornaliero e media per tutti valori non null
		// nel caso dovessi dare null per un valore mancante a meta' devo partire dalla fine
		// considero a parte l'ultima ora (ora attuale)
		for (i = 0; i < max; i++)
		{
			if (CostiConsumi.costoGiornaliero[i] != null)
			{
				n++;
				// se media null per valore valido odierno non faccio confronto
				if (CostiConsumi.costoMedio[i] == null)
				{
					odierno = null;
					break;
				}
				medio += CostiConsumi.costoMedio[i];
				odierno += CostiConsumi.costoGiornaliero[i];
			}
		}
		// se l'ultimo valore non e' null prendo una percentuale della media in base ai minuti attuali
		if ((medio != null) && (odierno != null))
		{
			if (CostiConsumi.costoGiornaliero[max] != null)
			{
				if (CostiConsumi.costoMedio[max] != null)
				{
					min = GestDate.GetActualDate().getMinutes();
					medio += CostiConsumi.costoMedio[max] * (min / 60);
					odierno += CostiConsumi.costoGiornaliero[max];
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
	// calcolo come sono rispetto alla media (per differenze sotto 0.10 � considero uguale
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
	$("#CostoAttualeImg").attr("src", CostiConsumi.costoOdiernoImg[diffInd]);
	$("#DettaglioCosto").html(CostiConsumi.costoOdiernoMsg[diffInd]);

	Log.alert(80, CostiConsumi.MODULE, "VisDatiCosti costoOdierno = "
			+ CostiConsumi.costoOdierno + " odierno = " + odierno + " costoMedio = " + odierno);
}


/*******************************************************************************
 * Chiamate per lettura dati necessari per la pagina dei costi
 ******************************************************************************/

CostiConsumi.DatiCostoPrevistoCb = function(val) {
	hideSpinner();
	if (val != null)
		CostiConsumi.costoPrevMese = val;
	else
		CostiConsumi.costoPrevMese = null;
	Log.alert(80, CostiConsumi.MODULE, "DatiCostoPrevisto = " + val);
	if (CostiConsumi.costoPrevMese == null)
		txt = Msg.home["datoNonDisponibile"];
	else
		txt = (CostiConsumi.costoPrevMese).toFixed(2) + " �";
	$("#DettaglioCostoPrevisto").html(
			Msg.home["costoPrevisto"] + ": <br><br><b>"	+ txt + "</b>");

	//InterfaceEnergyHome.GetSuggerimento(CostiConsumi.DatiSuggerimentoCostiCb);
	
	/* Verifico la connessione al server e carico gli RSS feed */
	//InterfaceEnergyHome.GetSuggerimento(CostiConsumi.DatiSuggerimentoConsumi);
	if (InterfaceEnergyHome.mode == 0 || InterfaceEnergyHome.visError == InterfaceEnergyHome.ERR_CONN_SERVER){
		
		CostiConsumi.InitfeedSim()
	}
	else{
		CostiConsumi.Initfeed(0);
	}
}

CostiConsumi.DatiSuddivisioneCostiCb = function(val) {
	// viene ritornato array di coppie di valori ["nome elettr", perc]
	CostiConsumi.suddivisioneCosti = val;
	hideSpinner();
	CostiConsumi.VisSuddivisioneCosti();
	
	showSpinner();
	InterfaceEnergyHome.GetCostoPrevisto(CostiConsumi.DatiCostoPrevistoCb);
	
}

CostiConsumi.DatiCostoMedioCb = function(val) {
	if (val != null)
		CostiConsumi.costoMedio = val;
	else
		CostiConsumi.costoMedio = null;
	hideSpinner();
	// ho i dati per visualizzare l'indicatore dei costi e il costo odierno
	CostiConsumi.VisIndicatoreCosti(); 
	/**
	if (CostiConsumi.costoOdierno == null)
		txt = Msg.home["datoNonDisponibile"];
	else
		txt = (CostiConsumi.costoOdierno).toFixed(2) + " �";
	$("#DettaglioCostoOdierno").html(
			Msg.home["costoFinora"] + ":<br><br><b>"+ txt + "</b>");
	**/
	Log.alert(80, CostiConsumi.MODULE, "DatiCostoMedio = " + val);
	//showSpinner();
	InterfaceEnergyHome.GetSuddivisioneCosti(CostiConsumi.DatiSuddivisioneCostiCb);
}

CostiConsumi.DatiCostoGiornalieroCb = function(val) {
	if (val != null)
		CostiConsumi.costoGiornaliero = val;
	else
		CostiConsumi.costoGiornaliero = null;

	Log.alert(80, CostiConsumi.MODULE, "DatiCostoGiornaliero = " + val);
	InterfaceEnergyHome.GetCostoMedio(CostiConsumi.DatiCostoMedioCb);
}

CostiConsumi.DatiCostoOdiernoCb = function(val) {
	if (val != null)
		CostiConsumi.costoOdierno = val;
	else
		CostiConsumi.costoOdierno = null;
	
	if (CostiConsumi.costoOdierno == null)
		txt = Msg.home["datoNonDisponibile"];
	else
		txt = (CostiConsumi.costoOdierno).toFixed(2) + " �";
	$("#DettaglioCostoOdierno").html(Msg.home["costoFinora"] + ":<br><br><b>"+ txt + "</b>");
	Log.alert(80, CostiConsumi.MODULE, "DatiCostoOdiernoCb = " + val);
	InterfaceEnergyHome.GetCostoGiornaliero(CostiConsumi.DatiCostoGiornalieroCb);
}
CostiConsumi.DatiSuggerimentoCostiCb = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate

	if (val != null)
		CostiConsumi.suggerimento = val;
	else
		Log.alert(80, CostiConsumi.MODULE, "DatiSuggerimento null");

	Log.alert(80, CostiConsumi.MODULE, "DatiSuggerimento = " + val);
	
	hideSpinner();
	// attualmente la posizione attuale sulla tariffa lo aggiorno insieme agli
	// altri dati
	CostiConsumi.SetTariffa();
	
	// se e' la prima volta creo il timer per la lettura dei dati
	// faccio setInterval anziche' setTimeout ogni volta perche'
	// se va male una chiamata riparto comunque
	// non ci sono problemi di accavallamento perche' l'intervallo e' molto alto
	if (CostiConsumi.timerCosti == null)
		CostiConsumi.timerCosti = setInterval("CostiConsumi.GetDatiCosti()",
				CostiConsumi.TIMER_UPDATE_COSTI);

}

CostiConsumi.DatiListaElettrodomesticiCb = function(val) {
	CostiConsumi.listaElettr = val;
	// salvo id smartInfo
	if (val != null) {
		for (i = 0; i < val.length; i++) {
			if (val[i].tipo == InterfaceEnergyHome.SMARTINFO_APP_TYPE) {
				CostiConsumi.idSmartInfo = val[i].id;
				break;
			}
		}
	}
	CostiConsumi.GetDatiCosti();
}

CostiConsumi.GetListaElettrodomestici = function() {
	if (CostiConsumi.listaElettr == null) {
		Log.alert(80, CostiConsumi.MODULE, "GetListaElettrodomestici");
		InterfaceEnergyHome.GetListaElettr(CostiConsumi.DatiListaElettrodomesticiCb);
	} else
		CostiConsumi.GetDatiCosti();
}

/*******************************************************************************
 * avvia le richieste dei dati, che vengono fatte in sequenza perche' asincrone,
 * visualizzo i dati una volta sola quando li ho tutti
 ******************************************************************************/
CostiConsumi.GetDatiCosti = function() {
	Log.alert(80, CostiConsumi.MODULE, "GetDatiCosti");
	Main.ResetError();
	
	InterfaceEnergyHome.GetCostoOdierno(CostiConsumi.DatiCostoOdiernoCb);
	//InterfaceEnergyHome.GetCostoGiornaliero(CostiConsumi.DatiCostoGiornalieroCb);
}

/*******************************************************************************
 * crea la parte costante della grafica
 ******************************************************************************/
CostiConsumi.GetImgTariffa = function(data)
{
	// controlla se sabato o domenica
	day = data.getDay();
	if ((day == 0) || (day == 6))
		img = Define.home["tariffaFestiva"];
	else
		img = Define.home["tariffaFeriale"];
	d = data.getDate();
	m = data.getMonth();
	// controlla se giorno di festa nazionale
	for (i = 0; i < Define.festivi.length; i++)
		if ((d == Define.festivi[i][0]) && (m == Define.festivi[i][1]))
		{
			img = Define.home["tariffaFestiva"];
			break;
		}
	return img;
}


/*******************************************************************************
 * **************************************************************************
 * Gestione Consumi
 * **************************************************************************
 ******************************************************************************/

/*******************************************************************************
 * gestisce la visualizzazione delal schermata dei consumi
 ******************************************************************************/

CostiConsumi.GestConsumi = function() {
   
	
	CostiConsumi.visType = CostiConsumi.CONSUMI;
	CostiConsumi.notizie = new Array();
	
	
	showSpinner();
	$("#Content").html(CostiConsumi.htmlContent["Consumi"]);
	

	$( "#backNews" ).button({
		text: false,
		icons: {
			primary: "ui-icon-seek-prev"
		}
	}).click(function(){
		CostiConsumi.notizieid=CostiConsumi.notizieid-2;
		if(CostiConsumi.notizieid<0)
			CostiConsumi.notizieid = 8;
		    
		    CostiConsumi.caricafeed();
	}
	
	
	);
	
	
	$( "#nextNews" ).button({
		text: false,
		icons: {
			primary: "ui-icon-seek-next"
		}
	}).click(function(){
		CostiConsumi.notizieid=CostiConsumi.notizieid+2;
		if(CostiConsumi.notizieid>=10)
			CostiConsumi.notizieid = 0;
		
		CostiConsumi.caricafeed();
	}
	
	
	);
	
    /* Se sono in IE devo arrotondare i bordi */
	
	if ($.browser.msie){
		$('#ConsumoSuggerimento,#ConsumoInfo,#ConsumoSintesi').corner();
		$('button').corner('3px');}
	
	
	
	
	Log.alert(80, CostiConsumi.MODULE, "CostiConsumi.GestConsumi ");

	$('#ConsumoIndicatoreImg').gauge({
		max : 2.0
	});
	maxCont = Define.home["limContatore"][Main.contatore];
	Log.alert(30, CostiConsumi.MODULE, "GestConsumi: max contatore = " + maxCont);
	hDiv = $("#ConsumoAttualeMeter").height();
	$("#ConsumoAttualeMeter").width(hDiv);
	$('#ConsumoAttualeMeter').speedometer({
		max : maxCont
	});

	CostiConsumi.VisConsumi();
}


CostiConsumi.ExitConsumi = function() {
	Log.alert(80, CostiConsumi.MODULE, "CostiConsumi.ExitConsumi");
	if (CostiConsumi.timerPotenza != null) {
		clearInterval(CostiConsumi.timerPotenza);
		CostiConsumi.timerPotenza = null;
	}
	if (CostiConsumi.timerConsumi != null) {
		clearInterval(CostiConsumi.timerConsumi);
		CostiConsumi.timerConsumi = null;
	}
	InterfaceEnergyHome.Abort();
	$("#Content").html(null);
	Main.ResetError();
	hideSpinner();
}

/*******************************************************************************
 * crea la parte costante della grafica
 ******************************************************************************/
CostiConsumi.VisConsumi = function() {

	// metto a posto fumetto
	//$("#ConsumoFumettoImg").attr("src", CostiConsumi.imgChat);
	//Utils.ResizeImg("ConsumoFumettoImg", CostiConsumi.imgChat, true, 1, 9);
	
	if (CostiConsumi.consumoOdierno != null)
		txt = (CostiConsumi.consumoOdierno /1000).toFixed(3) + " kWh";
	else
		txt = Msg.home["datoNonDisponibile"];
	$("#DettaglioConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + txt + "</b>");
	
	if (CostiConsumi.consumoPrevisto != null)
		txt = CostiConsumi.consumoPrevisto + " kWh";
	else
		txt = Msg.home["datoNonDisponibile"];
	$("#DettaglioConsumoPrevisto").html(Msg.home["consumoPrevisto"] + ":<br><br> <b>" + txt + "</b>");
	
	// dopo aver impostato la parte grafica costante faccio le richieste
	// per la parte di dati
	
	CostiConsumi.GetDatiPotenza();

	CostiConsumi.GetDatiConsumi();
}

/*******************************************************************************
 * funzione che calcola l'altezza della barra nel termometro dei consumi 
 * calcolo percentuale rispetto media
 * Dal 29-11-2011: sommo i valori giornalieri fino all'ora attuale o 
 * all'ultimo valore non null, sommo i valori medio per lo stesso numero di ore 
 * poi faccio il confronto. Se un'ora del giornaliero e' null non prendo il valore 
 * corrispondente della media, se una'ora della media e' null non faccio il confronto
 ******************************************************************************/
CostiConsumi.VisIndicatoreConsumi = function() {
	var medio, odierno, n, max;
	
	perc = 0;
	odierno = null;
	medio = null;
	/** calcolo consumo odierno dalla somma del consumo giornaliero
	if (CostiConsumi.consumoGiornaliero != null)
	{
		CostiConsumi.consumoOdierno = null;
		for (i= 0; i < CostiConsumi.consumoGiornaliero.length; i++)
		{
			if (CostiConsumi.consumoGiornaliero[i] != null)
				if (CostiConsumi.consumoOdierno == null)
					CostiConsumi.consumoOdierno = CostiConsumi.consumoGiornaliero[i];
				else
					CostiConsumi.consumoOdierno += CostiConsumi.consumoGiornaliero[i];
		}
		if (CostiConsumi.consumoOdierno != null)
			CostiConsumi.consumoOdierno = CostiConsumi.consumoOdierno / 1000; // passo in kW
	}
	else
		CostiConsumi.consumoOdierno = null;
	**/
	Log.alert(80, CostiConsumi.MODULE, "VisIndicatoreConsumi: consumoOdierno = " + CostiConsumi.consumoOdierno);

	if ((CostiConsumi.consumoMedio != null) && (CostiConsumi.consumoGiornaliero != null)
			&& (CostiConsumi.consumoOdierno != null))
	{
		odierno = 0;
		medio = 0;
		n = 0;
		max = CostiConsumi.consumoGiornaliero.length-1;
		// sommo consumo giornaliero e media per tutti valori non null
		// nel caso dovessi dare null per un valore mancante a meta' devo partire dalla fine
		// considero a parte l'ultima ora (ora attuale)
		for (i = 0; i < max; i++)
		{
			if (CostiConsumi.consumoGiornaliero[i] != null)
			{
				n++;
				// se media null per valore valido odierno non faccio confronto
				if (CostiConsumi.consumoMedio[i] == null)
				{
					odierno = null;
					break;
				}
				medio += CostiConsumi.consumoMedio[i];
				odierno += CostiConsumi.consumoGiornaliero[i];
			}
		}
		// se l'ultimo valore non e' null prendo una percentuale della media in base ai minuti attuali
		if ((medio != null) && (odierno != null))
		{
			if (CostiConsumi.consumoGiornaliero[max] != null)
			{
				if (CostiConsumi.consumoMedio[max] != null)
				{
					min = GestDate.GetActualDate().getMinutes();
					medio += CostiConsumi.consumoMedio[max] * (min / 60);
					odierno += CostiConsumi.consumoGiornaliero[max];
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
	Log.alert(80, CostiConsumi.MODULE, "VisIndicatoreConsumi: medio = " + medio + 
			" odierno = " + odierno + " perc = " + perc);
	$('#ConsumoIndicatoreImg').gauge("value", perc);
}

// visualizza elettrodomestico che in questo momento sta consumando di piu'
CostiConsumi.VisConsumoMaggiore = function() {
	
	if (CostiConsumi.maxConsumoElettr != null) {
		if (CostiConsumi.maxConsumoElettr.value == 0)
		{
			$("#DettaglioConsumoMaggiore").html("<span id='MsgConsumoMaggiore'></span>");
			$("#MsgConsumoMaggiore").text(Msg.home["maxDisp0"]);
			Log.alert(20, CostiConsumi.MODULE, "VisConsumoMaggiore : 0");
		}
		else
		{
			
			$("#DettaglioConsumoMaggiore").html("<span id='TestoConsumoMaggiore'></span><img id='ConsumoMaggioreImg' src=''>");
			Log.alert(80, CostiConsumi.MODULE, "VisConsumoMaggiore : "
				+ CostiConsumi.maxConsumoElettr.icona);
			// metto immagine del device che sta consumando di piu'
			$("#ConsumoMaggioreImg").attr("src",
					DefinePath.imgDispPath + CostiConsumi.maxConsumoElettr.icona);
			// il consumo e' in watt
			$("#TestoConsumoMaggiore").text(
					CostiConsumi.maxConsumoElettr.nome + " ("
					+ Math.round(CostiConsumi.maxConsumoElettr.value) + " W)");
			if (CostiConsumi.dimMaxDispImg == -1) {
				wDiv = $("#ConsumoMaggioreImg").width();
				hDiv = $("#ConsumoMaggioreImg").height();
				offsetTop = $("#ConsumoMaggioreImg").offset().top;
				offsetLeft = $("#ConsumoMaggioreImg").offset().left;

				// imposto dimensioni e offset img in px
				if (wDiv > hDiv)
					CostiConsumi.dimMaxDispImg = (hDiv * 0.9);
				else
					CostiConsumi.dimMaxDispImg = (wDiv * 0.9);
			}
			$("#ConsumoMaggioreImg").width(CostiConsumi.dimMaxDispImg);
			$("#ConsumoMaggioreImg").height(CostiConsumi.dimMaxDispImg);
		}
	} else {
		$("#DettaglioConsumoMaggiore").html("<span id='MsgConsumoMaggiore'></span>");
		$("#MsgConsumoMaggiore").text(Msg.home["noMaxDisp"]);
		Log.alert(20, CostiConsumi.MODULE, "VisConsumoMaggiore : null");
	}
}

CostiConsumi.VisGrafico = function() {
	var dati1 = new Array();

	Log.alert(80, CostiConsumi.MODULE, "VisGrafico");
	$.jqplot.config.enablePlugins = true;
	$("#DettaglioGraficoConsumoOdierno").html(null);
	barW = 0;
	max = Define.home["limConsumoOra"][Main.contatore]; 
	if (CostiConsumi.consumoGiornaliero != null) 
		dati = CostiConsumi.consumoGiornaliero;
	else
		// creo array di dati vuoti
		dati = new Array(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);

	// 24 ore piu' un po' di spazio
	barW = $("#DettaglioGraficoConsumoOdierno").width() / 28; 
	
	// controllo se sto nel limite massimo altrimenti lo sposto
	for (i = 0; i < dati.length; i++) {
		dati1[i] = new Array();
		dati1[i][1] = dati[i] / 1000;
		dati1[i][0] = i + 0.5;
		if (dati1[i][1] >= max)
		{
			max = dati1[i][1] * 1.05;
			Log.alert(20, CostiConsumi.MODULE, "VisGrafico: max = " + max + " dato = " + dati1[i][1]);
		}
	}
	Log.alert(50, CostiConsumi.MODULE, "VisGrafico : num dati = "
			+ dati.length + " max = " + max);
	
    numtick = 13;
    maxHour = 24;
    
    if (GestDate.DSTOttobre) {
    	
    	numtick++;
    	maxHour++;
    }
    
  
	
	
	plot1 = $.jqplot("DettaglioGraficoConsumoOdierno", [ dati1 ], {
		title : {
			text : "",// "Consumo Odierno (kWh)", // title for the plot,
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
				max : maxHour,
				numberTicks : numtick,
				tickOptions : {
					formatString : "%i",
					textColor : '#000000',
					fontSize : '0.8em'
				},
				label : ''
				
				
			},
			yaxis : {
				min : 0,
				max : max.toFixed(1),
				numberTicks : 5,
				label : '',
				tickOptions : {
					formatString : "%.1f",
					textColor : '#000000',
					fontSize : '0.8em'
				},
				autoscale : true
			}
		}
		});
	
if (GestDate.DSTMarzo){
	 
	 $(".jqplot-xaxis-tick:last-child").remove();
	
	 $('.jqplot-xaxis-tick').each(function(index) {
		    
		    if (index == 0) return true;
		    $(this).text( parseInt( $(this).text(),10) + 1  );
		});
   
	}
	else if (GestDate.DSTOttobre){
		
		$(".jqplot-xaxis-tick:last-child").remove();
		var label = $('.jqplot-xaxis-tick')[1];
		$(label).text(   parseInt( $(label).text(),10) + 1  );
		
		 $('.jqplot-xaxis-tick').each(function(index) {
			    
			    //if (index < 0) return true;
			    //$(this).text(   parseInt( $(this).text(),10) - 1  );
			});
		
	}
    
	
		$('#DettaglioGraficoConsumoOdierno').bind('jqplotDataHighlight', 
            function (ev, seriesIndex, pointIndex, data) {
                //$('#info1').html('series: '+seriesIndex+', point: '+pointIndex+', data: '+data);
			$(".highlightbar").remove(); // rimuovo quello precedente
			html = "<div class='highlightbar'>" + data[1].toFixed(3) + " kWh</div>";
			$('#DettaglioGraficoConsumoOdierno').append(html);
			$(".highlightbar").css("left", pointIndex*barW + "px");
			distTop = (max - data[1]) * 150;
			Log.alert(20, CostiConsumi.MODULE, 'click -> series: '+seriesIndex+', point: '+pointIndex+
					', data: '+data+' top: '+distTop);
			
			$(".highlightbar").css("top", distTop + "px");
			}
        );
		$('#DettaglioGraficoConsumoOdierno').bind('jqplotDataUnhighlight', 
            function (ev) {
              $(".highlightbar").remove();
            }
        );
}
 
CostiConsumi.GetImgPower = function() {
	var indImg;
	
	if (CostiConsumi.potenzaAttuale == null)
		indImg = 0;
	else
		indImg = Math.floor(CostiConsumi.potenzaAttuale / 4000 * 81);

	// temporaneo per ovviare a parte rossa troppo piccola
	if (indImg >= Define.home.gauge.length - 1)
		indImg = Define.home.gauge.length - 1;
	var imgPower = Define.home.gauge[indImg];
	Log.alert(20, CostiConsumi.MODULE, "GetImgPower: ind = " + indImg
			+ " power = " + CostiConsumi.potenzaAttuale + " img = " + imgPower);
	return CostiConsumi.pathImgPower + imgPower;
}

CostiConsumi.SetConsumoImg = function() {
	/**if (CostiConsumi.dimConsumoImg == -1) {
		hDiv = $("#ConsumoAttualeMeter").height();
		$("#ConsumoAttualeMeter").width(hDiv);
	}
	**/
// aggiorno il PowerMeter
	
	
	if (CostiConsumi.potenzaAttuale == null)
	{
		val = 0;
		$("#ValConsumoAttuale").html(Msg.home["datoNonDisponibile"]);
	}
	else
	{
		val = CostiConsumi.potenzaAttuale;
		$("#ValConsumoAttuale").html((CostiConsumi.potenzaAttuale / 1000.0).toFixed(3) + " kW");
	}
	//hDiv = $("#ConsumoAttualeMeter").height();
	//$("#ConsumoAttualeMeter").width(hDiv);
	val = val / 1000.0;
	// segnalo sovraccarico (zona gialla) e sovraccarico grave(zona rossa) dello speedometer
	if (val > Define.home["contatoreOk"][Main.contatore])
	{
		if (val > Define.home["contatoreWarn"][Main.contatore])
			$("#ValConsumoAttuale").css("color", "red");
		else
			$("#ValConsumoAttuale").css("color", "orange");
		if (CostiConsumi.timerBlink == null)
		{
			$("#ValConsumoAttuale").addClass("invisibleDiv")
			CostiConsumi.timerBlink = setInterval("CostiConsumi.BlinkVal()",
				CostiConsumi.TIMER_BLINK);
		}
	}
	else
	{
		clearInterval(CostiConsumi.timerBlink);
		CostiConsumi.timerBlink = null;
		$("#ValConsumoAttuale").css("color", "black");
		$("#ValConsumoAttuale").removeClass("invisibleDiv");
	}
	$('#ConsumoAttualeMeter').speedometer("value", val, "kW");

}
CostiConsumi.BlinkVal = function() {
	$("#ValConsumoAttuale").toggleClass("invisibleDiv");
	
}


CostiConsumi.DatiMaxElettr = function(elem) {
	CostiConsumi.maxConsumoElettr = elem;
	CostiConsumi.VisConsumoMaggiore();
	if (CostiConsumi.timerPotenza == null)
		CostiConsumi.timerPotenza = setInterval("CostiConsumi.GetDatiPotenza()",
				CostiConsumi.TIMER_UPDATE_POWER_METER);
}


CostiConsumi.DatiConsumoPrevistoCb = function(val) {
	if (val != null)
		CostiConsumi.consumoPrevisto = Math.round(val / 1000); // da w a kW
	else 
		CostiConsumi.consumoPrevisto = null;
	Log.alert(80, CostiConsumi.MODULE, "DatiConsumoPrevisto val = " + val);
	
	if (CostiConsumi.consumoPrevisto != null)
		txt = Math.round(CostiConsumi.consumoPrevisto) + " kWh";
	else
		txt = Msg.home["datoNonDisponibile"];
	$("#DettaglioConsumoPrevisto").html(Msg.home["consumoPrevisto"] + ":<br><br> <b>" + txt + "</b>");
	
	hideSpinner();

	
	/* Verifico la connessione al server e carico gli RSS feed */
	//InterfaceEnergyHome.GetSuggerimento(CostiConsumi.DatiSuggerimentoConsumi);
	if (InterfaceEnergyHome.mode == 0 || InterfaceEnergyHome.visError == InterfaceEnergyHome.ERR_CONN_SERVER){
		
		CostiConsumi.InitfeedSim()
	}
	else{
		//CostiConsumi.Initfeed(0);
		var script = document.createElement("script");
		  script.src = "https://www.google.com/jsapi?callback=CostiConsumi.loadFeed";
		  script.type = "text/javascript";
		  document.body.appendChild(script);
	}
}

/** Funzione lanciata al caricamento dello script google per gli RSS  **/

CostiConsumi.loadFeed = function () {
	
	google.load("feeds", "1",{"callback" : CostiConsumi.launchFeed});
	
	} 

CostiConsumi.launchFeed = function (){
	
	CostiConsumi.Initfeed(0);
	
}

/********************************************************************************/

CostiConsumi.DatiConsumoMedioCb = function(val) {
	CostiConsumi.consumoMedio = val;
	Log.alert(80, CostiConsumi.MODULE, "DatiConsumoMedio = " + val);

	CostiConsumi.VisIndicatoreConsumi();
	/**
	if (CostiConsumi.consumoOdierno != null)
		txt = (CostiConsumi.consumoOdierno).toFixed(2) + " kWh";
	else
		txt = Msg.home["datoNonDisponibile"];
	$("#DettaglioConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + txt + "</b>");
	**/
	InterfaceEnergyHome.GetConsumoPrevisto(CostiConsumi.DatiConsumoPrevistoCb);
	
}

CostiConsumi.DatiConsumoOdiernoCb = function(val) {
	CostiConsumi.consumoOdierno = val;
	Log.alert(80, CostiConsumi.MODULE, "DatiConsumoOdiernoCb = " + val);

	if (CostiConsumi.consumoOdierno != null)
		txt = (CostiConsumi.consumoOdierno / 1000).toFixed(2) + " kWh";
	else
		txt = Msg.home["datoNonDisponibile"];
	$("#DettaglioConsumoOdierno").html(Msg.home["consumoFinora"] + ":<br><br> <b>" + txt + "</b>");

	InterfaceEnergyHome.GetConsumoMedio(CostiConsumi.DatiConsumoMedioCb);
	//InterfaceEnergyHome.GetConsumoPrevisto(CostiConsumi.DatiConsumoPrevistoCb);
	
}

CostiConsumi.DatiConsumoGiornalieroCb = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	CostiConsumi.consumoGiornaliero = val;
	hideSpinner();
	Log.alert(80, CostiConsumi.MODULE, "DatiConsumoGiornaliero ");
	CostiConsumi.VisGrafico();
	
	showSpinner();
	//InterfaceEnergyHome.GetConsumoMedio(CostiConsumi.DatiConsumoMedioCb);
	InterfaceEnergyHome.GetConsumoOdierno(CostiConsumi.DatiConsumoOdiernoCb);

}

CostiConsumi.DatiSuggerimentoConsumi = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate

	if (val != null)
		CostiConsumi.suggerimento = val;
	else
		Log.alert(80, CostiConsumi.MODULE, "DatiSuggerimentoConsumi null");
	hideSpinner();
	Log.alert(80, CostiConsumi.MODULE, "DatiSuggerimentoConsumi = " + val);
	
	// se e' la prima volta creo il timer per la lettura dei dati
	// faccio setInterval anziche' setTimeout ogni volta perche'
	// se va male una chiamata riparto comunque
	// non ci sono problemi di accavallamento perche' l'intervallo e' molto alto
	if (CostiConsumi.timerConsumi == null)
		CostiConsumi.timerConsumi = setInterval("CostiConsumi.GetDatiConsumi()",
				CostiConsumi.TIMER_UPDATE_CONSUMI);
}

CostiConsumi.DatiPotenzaAttuale = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	Log.alert(80, CostiConsumi.MODULE, "DatiPotenzaAttuale val = " + val);
	
	CostiConsumi.potenzaAttuale = val;
	
	CostiConsumi.SetConsumoImg();
	// se e' la prima volta creo il timer per la lettura dei dati
	// faccio setInterval anziche' setTimeout ogni volta perche'
	// se va male una chiamata riparto comunque
	// non ci sono problemi di accavallamento perche' l'intervallo e' molto alto
	InterfaceEnergyHome.GetMaxElettr(CostiConsumi.DatiMaxElettr);

}

CostiConsumi.GetDatiPotenza = function() {
	Log.alert(80, CostiConsumi.MODULE, "GetDatiPotenza");
	// non tolgo togliere messaggio errore da piattaforma
	if (InterfaceEnergyHome.visError != InterfaceEnergyHome.ERR_CONN_SERVER)
		Main.ResetError();
	InterfaceEnergyHome.GetPotenzaAttuale(CostiConsumi.DatiPotenzaAttuale);
}

/*******************************************************************************
 * avvia le richieste dei dati, che vengono fatte in sequenza perche' asincrone,
 * visualizzo i dati una volta sola quando li ho tutti
 ******************************************************************************/
CostiConsumi.GetDatiConsumi = function() {
	Log.alert(80, CostiConsumi.MODULE, "GetDatiConsumi");
	Main.ResetError();
	InterfaceEnergyHome.GetConsumoGiornaliero(CostiConsumi.DatiConsumoGiornalieroCb);
}





/*******************************************************************************
 * gestisce il caricamento degli RSS feed nell'array CostiConsumi.notizie
 ******************************************************************************/

CostiConsumi.Initfeed = function (channel){
	var feed;
	
	/* Se i feed sono gi� stati caricati non viene inoltrata un altra richiesta */
	if(channel == 0 && CostiConsumi.notizie.length != 0){
		
		CostiConsumi.caricafeed();
	}
	
	else {
    /* Questa funzione viene richiamata un numero di volte pari al numero di canali che si vuole caricare
     *  e qui si differenzia il feed da caricare in base al canale */
	switch (channel){
	case 0 : {
		feed = new google.feeds.Feed("http://www.rsspect.com/rss/energyathome.xml ");
		break;
		
	}
	case 1:{
		feed = new google.feeds.Feed("http://gogreen.virgilio.it/rss/news.xml");
	}
	default: break;
	}
	
	
	feed.setNumEntries(10);
	
	/* Una volta settato il canale si caricano i feed e viene chiamata una funzione di callbak una volta caricati */
	feed.load(function(result) {
		
		if (!result.error) {
		      
			/* salvo i feed nella variabile CostiConsumi.notizie */
		      for (var i = 0; i < result.feed.entries.length; i++) {
		        var entry = result.feed.entries[i];
		        var item = {
		  
		                title: entry.title,
		                link: entry.link,
		                description: entry.contentSnippet,
		                
		        }
		        CostiConsumi.notizie.push(item);
		        }
		      }
		    /* Se ho caricato il primo canale allora chiamo la funzione per caricare il secondo */
		    if (channel == 0){
		    
		    CostiConsumi.Initfeed(1);
		    }
		    /* se ho caricato il secondo canale, nascondo o spinner e carico i feed nell'html */
		    else {
		    	hideSpinner();
		    	CostiConsumi.caricafeed();
		    	
		    }
		    
		    
		  
              });
	
	}
	
	
}

/* Funziona che visualizza gli RSS feed contenuti nella variabile notizie */
CostiConsumi.caricafeed = function(){
	
	$(".dettaglioNews,.titoloNews").removeAttr("threedots");
	$(".threedots_ellipsis").remove();
	
	altezza_news = Math.floor(($("#DettaglioSuggerimentiConsumi").height() - 1 - (Math.floor($("#DettaglioSuggerimentiConsumi").width()*0.01)*2))/2);
	
	$("#PrimaNews").css("height",altezza_news);
	$("#SecondaNews").css("height",altezza_news);
	
	
	$("#PrimaNews .titoloNews .ellipsis_text").html(CostiConsumi.notizie[CostiConsumi.notizieid]["title"]);
    $("#PrimaNews a").attr("href",CostiConsumi.notizie[CostiConsumi.notizieid]["link"]);
    $("#PrimaNews .dettaglioNews .ellipsis_text ").html(CostiConsumi.notizie[CostiConsumi.notizieid]["description"]);
    
    
    $("#SecondaNews .titoloNews .ellipsis_text").html(CostiConsumi.notizie[CostiConsumi.notizieid+1]["title"]);
    $("#SecondaNews a").attr("href",CostiConsumi.notizie[CostiConsumi.notizieid+1]["link"]);
    $("#SecondaNews .dettaglioNews .ellipsis_text").html(CostiConsumi.notizie[CostiConsumi.notizieid+1]["description"]);
    
    
   
    
    var diffContenitore_Notizie = $("#DettaglioSuggerimentiConsumi").outerHeight(true)-68-((Math.floor($("#DettaglioSuggerimentiConsumi").width()*0.01))*5);
    
    if (diffContenitore_Notizie < 0){
    	$("#SecondaNews").remove();
		$("#PrimaNews").css("position","absolute").css("top","25%").css("border","0px");
    }
   
    $(".titoloNews").ThreeDots({max_rows : 1});
    $(".dettaglioNews").ThreeDots();
	
}

/*******************************************************************************
 * gestisce la simulazione degli RSS feed nel div Suggerimenti
 ******************************************************************************/

CostiConsumi.InitfeedSim = function (){
	
	CostiConsumi.notizie = NotizieSimul;
    CostiConsumi.caricafeed();
    hideSpinner();
	
}



	
	    
	   
	    
	
