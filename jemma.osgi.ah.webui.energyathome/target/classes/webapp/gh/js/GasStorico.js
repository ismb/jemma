var GasStorico = {
	MODULE : "GasStorico",
	installData : null,
	primaData : null,
	ultimaData : null,
	dataInizio : null,
	dataFine : null,
	dispositivi : null,
	tipoUltimoPeriodo : null,
	periodoScelto: null,
	dispositivoScelto: null,
	datiCostoNulli: false,
	datiConsumoNulli: false,
	enableResize: true,
	plot1: null,
	datiElettr : null,
	datiCosto : null,
	datiConsumo : null,
	titoloGraph : null,
	COSTI_CONSUMI : 0,
	SOLO_CONSUMI : 1,
	GIORNO : 0,
	SETTIMANA : 1, 
	MESE : 2, 
	ANNO : 3,
	
	htmlContent : "<div class='ContentTitle'>" + Msg.home["titoloStorico"] + "</div><div id='GasStorico'>" +
			"<img id='StoricoBg' src='" + GasDefine.home["sfondo_sx"] + "'>"  +
			"<div id='MsgStorico' style='display:none'>" + Msg.home["datiMancanti"] + "</div>" +
			"<div id='LabelStoricokWh'>smc</div><div id='LabelStoricoEuro'>€</div>" +
			"<div id='Prec'><img id='PrecImg' src='" + GasDefine.home["frecciaPrec"] + "'><div id='LabelPrec'></div></div>" +
			"<div id='Succ'><img id='SuccImg' src='" + GasDefine.home["frecciaSucc"] + "'><div id='LabelSucc'></div></div>" +
			"<div id='StoricoGraphContainer'><div id='StoricoGraph'></div></div><div id='SepStorico'></div><div id='StoricoScelta'>" +
			"<div id='TitoloSceltaPeriodo'>" + Msg.home["periodoStorico"] + "</div>" +
			"<div id='SceltaPeriodo'>" +
			"	<input class='ButtonScelta' name='Periodo' type='radio' checked='checked' value='0'>" + Msg.home["giornoStorico"] + "<br>" + 
     		"	<input class='ButtonScelta' name='Periodo' type='radio' value='1'>" + Msg.home["settStorico"] + "<br>" +     
     		"	<input class='ButtonScelta' name='Periodo' type='radio' value='2'>" + Msg.home["meseStorico"] + "<br>" +     
     		"	<input class='ButtonScelta' name='Periodo' type='radio' value='3'>" + Msg.home["annoStorico"] + "<br>" +
     		"</div>"  +
			"<div id='TitoloSceltaDispositivo'>" + Msg.home["dispStorico"] + "</div><div id='SceltaDispositivo'></div>" +
			"</div></div>"

};

GasStorico.ExitStorico = function() {
	hideSpinner();
	GasMain.ResetError();
	GasStorico.datiCosto = null;
	GasStorico.datiConsumo = null;
	Log.alert(20, GasStorico.MODULE, "ExitStorico");
	if (GasStorico.enableResize)
		$(window).unbind('resize', GasStorico.ResizePlot);
	
	GasInterfaceEnergyHome.Abort();
};


GasStorico.DateIsSame = function(valDate, val1Date)
{
	d1 = valDate.getDate();
	m1 = valDate.getMonth();
	y1 = valDate.getFullYear();
	//oggi = GasGestDate.GetActualDate();
	d2 = val1Date.getDate();
	m2 = val1Date.getMonth();
	y2 = val1Date.getFullYear();
	if ((d1 == d2) & (m1 == m2) && (y1 == y2))
		return true;
	else
		return false;
};

// controllo se la data specificata rientra tra la data di installazione e quella attuale
GasStorico.DateIsValid = function(valDate, periodo)
{
	// nel caso di giorno tengo conto dell'ora, altrimenti no
	if (periodo == GasStorico.GIORNO)
	{
		ora = new Date(GasGestDate.GetActualDate().getTime());
		tmp = new Date(valDate.getTime());
		// se giorno di installazione non segnalo errore
		//if (GasStorico.DateIsSame(tmp, GasStorico.installData))
		//	return true;
		tmp.setMinutes(0); // fine dell'ora attuale
		// controllo per data di installazione
		tmp.setHours(tmp.getHours()+1);
		if (tmp.getTime() < GasStorico.installData.getTime())
			return false;
		// controllo per ora attuale
		// aumento ancora di 1 perche' potrie avere dati incompleti per le ultime 2 ore
		tmp.setHours(tmp.getHours()+1);
		if (tmp.getTime() > ora)
			return false;
		
		return true;
	}
	else
	{
		// nel caso di giorno il giorno di installazione e quello attuale possono avere 
		// dati nulli e non devo segnalare errore
		tmpI = new Date(valDate.getTime());
		tmpI.setHours(0);
		if (tmpI.getTime() < GasStorico.installData.getTime())
			return false;
		tmpI.setDate(tmpI.getDate()+1);
		domani = new Date(GasGestDate.GetActualDate().getTime());
		domani.setDate(domani.getDate()+1);
		domani.setHours(0);
		domani.setMinutes(0);
		if (tmpI.getTime() < domani.getTime())
			return true;
	}
	return false;
};


// se tipo = 0 visualizza costi e consumi
// se tipo = 1 visualizza i consumi
GasStorico.VisStorico = function(tipo) {
	var tickers =  new Array();	
	var maxCosto, maxConsumo, rapp, barW, max;
	var dati1 = null;
	var dati2 = null;

	GasStorico.datiConsumoNulli = false;
	GasStorico.datiCostoNulli = false;
	$.jqplot.config.enablePlugins = true;
	$('#StoricoGraph').html(null);
	$("#LabelStoricokWh").hide();
	$("#LabelStoricoEuro").hide();
	//tipo = GasStorico.SOLO_CONSUMI;
	valTicker = new Date(GasStorico.dataInizio.getTime());
	valTicker.setHours(0);
	valTicker.setMinutes(0);
	valTicker.setSeconds(0);
	aTicks = 0;
	// se smart info metto tutti
	if (GasStorico.GetDispTipo(GasStorico.dispositivoScelto) == GasInterfaceEnergyHome.SMARTINFO_APP_TYPE)
		disp = Msg.home["tuttiStorico"];
	else
		disp = GasStorico.dispositivoScelto;
	
	
	if (GasStorico.periodoScelto == GasStorico.GIORNO)	
	{
		labelX = Msg.home["oraLabelStorico"];
		titolo = Msg.home["giornoGrafStorico"] + ":  " + GasStorico.dataInizio.getDate() + "  " + Msg.mesiAbbrev[GasStorico.dataInizio.getMonth()].toUpperCase() + 
		" " +	GasStorico.dataInizio.getFullYear();

		maxCosto = GasDefine.home["limCostoOra"][GasMain.contatore];
		maxConsumo = GasDefine.home["limConsumoOra"][GasMain.contatore];
		nTickskWh = 6;
		nTicksE = 6;
		formatStr = "%.2f";
	}
	else
		if (GasStorico.periodoScelto == GasStorico.ANNO)
		{
			titolo = Msg.home["daGrafStorico"] + " " + Msg.mesiAbbrev[GasStorico.dataInizio.getMonth()].toUpperCase() + " " + GasStorico.dataInizio.getFullYear() + 
			" " + Msg.home["aGrafStorico"] + " " + Msg.mesiAbbrev[GasStorico.dataFine.getMonth()].toUpperCase() + " " + GasStorico.dataFine.getFullYear(); 

			labelX = Msg.home["meseLabelStorico"];
			maxCosto = GasDefine.home["limCostoMese"][GasMain.contatore];
			maxConsumo = GasDefine.home["limConsumoMese"][GasMain.contatore];
			nTickskWh = 8;
			nTicksE = 8;
			formatStr = "%.1f";
		}
		else
		{
			titolo = Msg.home["daGrafStorico"] + " " + GasStorico.dataInizio.getDate() + " " + Msg.mesiAbbrev[GasStorico.dataInizio.getMonth()].toUpperCase() + 
			" " + GasStorico.dataInizio.getFullYear() + " " + Msg.home["aGrafStorico"] + " "  + GasStorico.dataFine.getDate() + " " + 
			Msg.mesiAbbrev[GasStorico.dataFine.getMonth()].toUpperCase() + " " + GasStorico.dataFine.getFullYear(); 

			if (GasStorico.periodoScelto == GasStorico.MESE)
				aTicks = -80;
			labelX = Msg.home["giornoLabelStorico"];
			maxCosto = GasDefine.home["limCostoGiorno"][GasMain.contatore];
			maxConsumo = GasDefine.home["limConsumoGiorno"][GasMain.contatore];
			nTickskWh = 6;
			nTicksE = 6;
			formatStr = "%.1f";
		}			
	// non prevedo solo costo per il momento
	if (GasStorico.datiConsumo == null)
	{
		//$("#Prec").hide();
		//$("#Succ").hide();
		$("#MsgStorico").hide();
		Log.alert(40, GasStorico.MODULE, "VisStorico : nessun dato");
		$("#StoricoGraph").html("<div id='StoricoVuotoTitolo'>" + titolo + "</div><div id='StoricoVuoto'>" + 
				  Msg.home["noGrafStorico"] + "</div>");
		return;
	}
	
	if (GasStorico.datiConsumo != null)
		dati1 = GasStorico.datiConsumo.slice(0); // copio array perche' lo modifico (per simulazione)
	if (GasStorico.datiCosto != null)
		dati2 = GasStorico.datiCosto.slice(0);
	else
		tipo = GasStorico.SOLO_CONSUMI; // se non ho dati dei costi visualizzo solo consumi
	
	// creo array con i dati e creo le label per i tickers
	// controllo se i dati sono nei limiti altrimenti li sposto (massimo valore + 5%)
	// se dato nullo lo segnalo (lo ignoro se prima della data di installazione o da oggi in poi)
	for (i = 0; i < dati1.length; i++)
	{
		// se uno dei 2 e' null metto a null anche l'altro
		if (dati1[i] == null)
		{
			dati1[i] = 0;
			if (dati2 != null)
				dati2[i] = 0;
			if (GasStorico.DateIsValid(valTicker, GasStorico.periodoScelto))
				GasStorico.datiConsumoNulli = true;
		}
		else
		{
//			dati1[i] = dati1[i] / 1000; // da w a kWh
			dati1[i] = dati1[i]; // da w a kWh
			// controllo se sono nei limiti altrimenti sposto i limiti
			if (dati1[i] > maxConsumo)
				maxConsumo = dati1[i] * 1.05;
		}
		if ((tipo == GasStorico.COSTI_CONSUMI) && (dati2 != null))
		{
			if (dati2[i] == null)
			{
				dati2[i] = 0;
				dati1[i] = 0;
				if (GasStorico.DateIsValid(valTicker, GasStorico.periodoScelto))
					GasStorico.datiCostoNulli = true;
			}
			else
			{
				if (dati2[i] > maxCosto)
					maxCosto = dati2[i] * 1.05;
			}
		}
		//label ticks diverso a seconda di cosa sto visualizzando
		if (GasStorico.periodoScelto == GasStorico.GIORNO)
		{	
			tickers[i] = valTicker.getHours().toString();
			Log.alert(80, GasStorico.MODULE, "VisStorico : ora = " + tickers[i]);
			valTicker.setHours(valTicker.getHours()+1);	// giorno
		}	
		else
			if (GasStorico.periodoScelto == GasStorico.SETTIMANA)
			{	
				tickers[i] = valTicker.getDate() + "-" + (valTicker.getMonth()+1);
				valTicker.setDate(valTicker.getDate()+1);	// settimana
			}
			else
				if (GasStorico.periodoScelto == GasStorico.MESE)
				{	
					tickers[i] = valTicker.getDate() + "-" + (valTicker.getMonth()+1);
					valTicker.setDate(valTicker.getDate()+1);	// mese
				}
				else
				{				
					tickers[i] = Msg.mesiAbbrev[valTicker.getMonth()] + "-" + (valTicker.getFullYear()-2000);
					valTicker.setMonth(valTicker.getMonth()+1);	// anno
				}
	}

	if (tipo == GasStorico.COSTI_CONSUMI)
	{
		barW = Math.round($('#StoricoGraph').width() * 0.4 /(dati1.length + 2));
		rapp = maxConsumo / maxCosto;
		Log.alert(20, GasStorico.MODULE, "VisStorico: maxConsumo = " + maxConsumo + " maxCosto = " + maxCosto +
							" rapp = " + rapp);
		$("#LabelStoricokWh").show();
		$("#LabelStoricoEuro").show();
		
		// ricalcolo valore costo
		for (i = 0; i < dati2.length; i++)
			dati2[i] = dati2[i] * rapp; 
	}
	else
	{
		// per adesso per il trial gestisco solo consumo
		$("#LabelStoricoEuro").hide();
		$("#LabelStoricokWh").show();
		barW = Math.round($('#StoricoGraph').width() * 0.8 /(dati1.length + 2));
		rapp = 1;
	}
	
	maxConsumo = maxConsumo.toFixed(1);
	if (tipo == GasStorico.COSTI_CONSUMI)
	{
		GasStorico.plot1 = $.jqplot('StoricoGraph', [dati1, dati2], {
		title: {
			text : titolo,
			textAlign: 'left',
			show: true
    		},
    		legend: {
    			show: true,
    			location: 'n',     // compass direction, nw, n, ne, e, se, s, sw, w.
        		xoffset: 0,        // pixel offset of the legend box from the x (or x2) axis.
        		yoffset: 0,        // pixel offset of the legend box from the y (or y2) axis.
            	placement: 'outsideGrid'
      	},
	    highlighter: {
	        show: true,
	        sizeAdjust: 7.5,
	        showTooltip: true
	    },	      	
	    seriesColors: ["#0B0B96", "#FFD800"],
	    seriesDefaults:{
			renderer:$.jqplot.BarRenderer,
     			rendererOptions: {barMargin: 3, barPadding:0, barWidth: barW}
		},
		series: [ 
		    {label: Msg.home["consumoGrafStorico"], color:'#0B0B96'},
			{label: Msg.home["costoGrafStorico"], color:'#FFD800'},		
			{xaxis:'xaxis'},
			{yaxis:'yaxis'},
			{yaxis:'y2axis'}
        	],
    		axes: {
			xaxis:{
				renderer: $.jqplot.CategoryAxisRenderer,
               	ticks: tickers,
				rendererOptions:{tickRenderer:$.jqplot.CanvasAxisTickRenderer},
				tickOptions:{showMark:true, angle:aTicks, textColor:'#000000', fontWeight:'normal', markSize:8},
				label: labelX
				}, 
			yaxis: {
				show: true,
				min: 0,
				max: maxConsumo,
				numberTicks: nTickskWh,
				label: '',
				labelOptions: {textColor:'#0B0B96'},
				tickOptions:{showGridline:false,formatString:formatStr}, 
				autoscale: true
			},
			y2axis: {
				show: true,
				min: 0,
				max: maxCosto,
				numberTicks: nTicksE, 
				label: '',
				labelOptions: {textColor:'#FFD800', fontSize:'1.6em'},
				tickOptions:{formatString:formatStr},
				autoscale:true
			}
    		}
    		});

 	}
	else
		{
		// solo consumi
		GasStorico.plot1 = $.jqplot('StoricoGraph', [dati1], {
			title: {
				text : titolo,
				textAlign: 'left',
				show: true
	    		},
	    		legend: {
	    			show: true,
	    			location: 'n',     // compass direction, nw, n, ne, e, se, s, sw, w.
	        		xoffset: 0,        // pixel offset of the legend box from the x (or x2) axis.
	        		yoffset: 0,        // pixel offset of the legend box from the y (or y2) axis.
	            	placement: 'outsideGrid'
	      	},
			seriesColors: ["#FFD800", "#0B0B96"],
			seriesDefaults:{
				renderer:$.jqplot.BarRenderer,
	     			rendererOptions: {barMargin: 3, barPadding:0, barWidth: barW}
			},
		    highlighter: {
		        show: true,
		        sizeAdjust: 7.5,
		        showTooltip: true
		    },	  	
			series: [ 
				{label: Msg.home["consumoGrafStorico"], color:'#0B0B96'},
				{xaxis:'xaxis'},
				{yaxis:'yaxis'}
				],
	    		axes: {
				xaxis:{
					renderer: $.jqplot.CategoryAxisRenderer,
	               	ticks: tickers,
					rendererOptions:{tickRenderer:$.jqplot.CanvasAxisTickRenderer},
					tickOptions:{showMark:true, angle:aTicks, textColor:'#000000', fontWeight:'normal', markSize:8},
					label: labelX
					}, 
				yaxis: {
					show: true,
					min: 0,
					max: maxConsumo,
					numberTicks: nTickskWh,
					label: '',
					labelOptions: {textColor:'#0B0B96'},
					tickOptions:{formatString:formatStr},
					autoscale: true
				}
			}
	    		});
		}
	// gestione highlight
		$('#StoricoGraph').bind('jqplotDataHighlight', 
            function (ev, seriesIndex, pointIndex, data) {
        	$(".highlightbarStorico").remove(); // rimuovo quello precedente
			if (seriesIndex == 0)
				html = "<div class='highlightbarStorico'>" + data[1].toFixed(3) + " smc</div>";
			else
				html =  "<div class='highlightbarStorico'>" + (data[1]/rapp).toFixed(2) + " €</div>";
			$('#StoricoGraph').append(html);
			// calcolo distanza da sinistra
			w = $("#StoricoGraph").width();
			if (seriesIndex == 1)
				// se due colonne per la seconds ho un offset
				offsetBar = barW * 2;
			else
				offsetBar = 0;
			k = w * 0.9 / dati1.length;
			//lf = (l * 0.25) + k * pointIndex + offsetBar;
			lf = k * pointIndex + offsetBar;
			$(".highlightbarStorico").css("left", lf + "px");
			// calcola distanza dal top
			h = $("#StoricoGraph").height();
			k = h * 0.7 / maxConsumo;
			distTop = (h * 0.1) + k * (maxConsumo - data[1]);
			Log.alert(20, GasCostiConsumi.MODULE, 'click -> series: '+seriesIndex+', point: '+pointIndex+
					', data: '+data+' top: '+distTop);
			
			$(".highlightbarStorico").css("top", distTop + "px");
			}
        );
		$('#StoricoGraph').bind('jqplotDataUnhighlight', 
            function (ev) {
              $(".highlightbarStorico").remove();
            }
        );
	
	// Se mancano dei dati viene visualizzato un messaggio
	// non lo visualizzo se sto visualizzando il giorno ed e' quello di installazione
	if ((GasStorico.periodoScelto == GasStorico.GIORNO) &&
			GasStorico.DateIsSame(GasStorico.dataInizio, GasStorico.installData))
		$("#MsgStorico").hide();
	else
		if ((GasStorico.datiCostoNulli) || (GasStorico.datiConsumoNulli))
			$("#MsgStorico").show();
		else
			$("#MsgStorico").hide();
	
};


GasStorico.DatiConsumoStorico = function(val) {
	
	GasStorico.datiConsumo = val;
	if (val == null)
		Log.alert(80, GasStorico.MODULE, "DatiConsumoStorico null");
	hideSpinner();
	Log.alert(80, GasStorico.MODULE, "DatiConsumoStorico = " + val);
	// Nella prima fase del trial gestisco solo i dati del consumo
	if (GasInterfaceEnergyHome.mode == GasInterfaceEnergyHome.MODE_FULL)
		GasStorico.VisStorico(1);
	else
		GasStorico.VisStorico(0);
};

GasStorico.DatiCostoStorico = function(val) {
	GasStorico.datiCosto = val;
	if (val == null)
		Log.alert(80, GasStorico.MODULE, "DatiCostoStorico null");

	Log.alert(80, GasStorico.MODULE, "DatiCostoStorico = " + val);
	GasInterfaceEnergyHome.GetStorico("Consumo", GasStorico.device, GasStorico.dataInizio, GasStorico.dataFine, GasStorico.periodoScelto, GasStorico.DatiConsumoStorico);
};

/*************************************************
 * Legge i dati del costo e del consumo 
 * relativi al periodo e dispositivo selezionato
 *************************************************/
GasStorico.GetStorico = function() {
	GasMain.ResetError();
	showSpinner();
	Log.alert(20, GasStorico.MODULE, "Dispositivo= " + GasStorico.dispositivoScelto + " Periodo = " + GasStorico.periodoScelto);
	Log.alert(20, GasStorico.MODULE, "============ GetStorico: inizio = " + GasStorico.dataInizio.toString() + 
			" fine = " + GasStorico.dataFine.toString());
	
	// imposta id del dispositivo selezionato
	GasStorico.device = GasStorico.GetDispId(GasStorico.dispositivoScelto);
	Log.alert(80, GasStorico.MODULE, "Periodo: pid = " + GasStorico.device + " inizio = " + GasStorico.dataInizio.toString() + " fine = " + GasStorico.dataFine.toString());
	
	// Nella prima fase del trial gestisco solo i dati del consumo
	if (GasInterfaceEnergyHome.mode == GasInterfaceEnergyHome.MODE_FULL)
		GasInterfaceEnergyHome.GetStorico("Consumo", GasStorico.device, GasStorico.dataInizio, GasStorico.dataFine, GasStorico.periodoScelto, GasStorico.DatiConsumoStorico);
	else
		GasInterfaceEnergyHome.GetStorico("Costo", GasStorico.device, GasStorico.dataInizio, GasStorico.dataFine, GasStorico.periodoScelto, GasStorico.DatiCostoStorico);
};

GasStorico.GetDispId = function(nomeElettr) {
	if (GasStorico.datiElettr != null)
		for (i = 0; i < GasStorico.datiElettr.length; i++)
			if (GasStorico.datiElettr[i].nome == nomeElettr)
				return GasStorico.datiElettr[i].pid;
	return null;
};

GasStorico.GetDispTipo = function(nomeElettr) {
	if (GasStorico.datiElettr != null)
		for (i = 0; i < GasStorico.datiElettr.length; i++)
			if (GasStorico.datiElettr[i].nome == nomeElettr)
				return GasStorico.datiElettr[i].tipo;
	return null;
};


// se cambio in breve tempo sia periodo che dispositivo (o che date con le frecce)
// potrei lanciare una chiamata prima che sia finita quella precedente
// bisogna vedere cosa succede
// cambio le date in base al periodo precedente e al tipo di periodo scelto
// non devo mettere ora 23 nel caso di richiesta di giorno perche' quando c'e' l'ora legale 
// altrimenti passo al giorno dopo
GasStorico.SceltaPeriodo = function() {
	GasStorico.periodoScelto = parseInt($("input[type=radio][name='Periodo']:checked").val());
	Log.alert(20, GasStorico.MODULE, "========== SceltaPeriodo: precedente = " + GasStorico.tipoUltimoPeriodo +
					" nuovo = " + GasStorico.periodoScelto);
	if (GasStorico.periodoScelto != GasStorico.tipoUltimoPeriodo)
	{
		$("#Prec").show();
		$("#Succ").show();
		switch (GasStorico.periodoScelto)
		{
		case GasStorico.GIORNO:
			// prendo sempre l'ultimo giorno del periodo attualmente visualizzato
			// a meno che il giorno non sia maggiore di ieri
			ieri = new Date(GasGestDate.GetActualDate().getTime());
			ieri.setHours(0);
			//oggi.setMinutes(0);
			//oggi.setSeconds(0);
			//oggi.setMilliseconds(0);
			if (GasStorico.dataFine.getTime() >= ieri.getTime())
			{
				// prendo ieri
				GasStorico.dataFine = new Date(ieri.getTime());
				GasStorico.dataFine.setDate(GasStorico.dataFine.getDate()-1);
			}
			GasStorico.dataFine.setHours(23);
			GasStorico.dataFine.setMinutes(59);
			GasStorico.dataInizio = new Date(GasStorico.dataFine.getTime());
			GasStorico.dataInizio.setHours(0);
			GasStorico.dataInizio.setMinutes(0);
			break;
			
		case GasStorico.SETTIMANA:
			// prendo settimana da lunedi' a domenica (N.B. 0 = domenica, 1 = lunedi')
			// prendo la settimana in cui cade l'ultimo giorno 
			// data fine <= ieri
			ieri = new Date(GasGestDate.GetActualDate().getTime());
			ieri.setDate(ieri.getDate()-1);
			ieri.setHours(0);
			ieri.setMinutes(0);
			ieri.setSeconds(0);
			ieri.setMilliseconds(0);
			if ((GasStorico.dataFine.getTime()) >= ieri.getTime())
			GasStorico.dataFine = new Date(ieri.getTime());
			GasStorico.dataFine.setHours(12);
			day = GasStorico.dataFine.getDay();
			// se domenica resta giorno finale della settimana
			if (day != 0)
			{
				// ultimo giorno = differenza tra domenica e il giorno in questione
				GasStorico.dataFine.setDate(GasStorico.dataFine.getDate()+(7-day));
			}
			GasStorico.dataInizio = new Date(GasStorico.dataFine.getTime());
			GasStorico.dataInizio.setDate(GasStorico.dataInizio.getDate()-6);
			GasStorico.dataInizio.setHours(0);
			GasStorico.dataFine.setHours(12);
			break;
			
		case GasStorico.MESE:
			if (GasStorico.tipoUltimoPeriodo == GasStorico.ANNO)
			{
				// prendo dicembre o ultimo mese
				GasStorico.dataInizio = new Date(GasStorico.dataFine.getTime());
				GasStorico.dataInizio.setMonth(11);
				GasStorico.dataInizio.setDate(1);
				GasStorico.dataInizio.setHours(0);
				GasStorico.dataInizio.setMinutes(0);
				
			}
			else
			{
				// prendo il mese in cui cade l'ultimo giorno selezionato
				GasStorico.dataInizio = new Date(GasStorico.dataFine.getTime());
				GasStorico.dataInizio.setDate(1); // primo giorno del mese
			}
			if (GasStorico.dataInizio.getTime() > GasGestDate.GetActualDate().getTime())
			{
				GasStorico.dataInizio = new Date(GasGestDate.GetActualDate().getTime());
				GasStorico.dataInizio.setDate(1);
			}
			// ultimo giorno del mese (giorno prima del primo giorno del mese successivo)
			GasStorico.dataFine = new Date(GasStorico.dataInizio.getTime());
			GasStorico.dataFine.setMonth(GasStorico.dataFine.getMonth()+1);
			GasStorico.dataFine.setDate(GasStorico.dataFine.getDate()-1);
			GasStorico.dataInizio.setHours(0);
			GasStorico.dataFine.setHours(12);
			break;
			
		case GasStorico.ANNO:
			// prendo sempre l'anno dell'ultimo giorno selezionato
			GasStorico.dataInizio = new Date(GasStorico.dataFine.getTime());
			GasStorico.dataInizio.setDate(1);
			GasStorico.dataInizio.setMonth(0);
			GasStorico.dataFine = new Date(GasStorico.dataInizio.getTime());
			GasStorico.dataFine.setDate(31);
			GasStorico.dataFine.setMonth(11);
			GasStorico.dataInizio.setHours(0);
			GasStorico.dataFine.setHours(12);
			break;
			
		}
		// controllo di non superare i limiti temporali
		if (GasStorico.dataInizio.getTime() < GasStorico.installData.getTime())
			$("#Prec").hide();
		
		// data fine <= ieri
		ieri = new Date(GasGestDate.GetActualDate().getTime());
		//ieri.setDate(ieri.getDate()-1);
		ieri.setHours(0);
		ieri.setMinutes(0);
		ieri.setSeconds(0);
		ieri.setMilliseconds(0);
		if ((GasStorico.dataFine.getTime()) >= ieri.getTime())
			$("#Succ").hide();
		
		GasStorico.tipoUltimoPeriodo = GasStorico.periodoScelto;
		GasStorico.GetStorico();
	}
	else
	{
		Log.alert(80, GasStorico.MODULE, "SceltaPeriodo: non ho cambiato tipo di periodo");
	}
};

// cambio solo dispositivo, lascio le date attuali
GasStorico.SceltaDispositivo = function() {
	GasStorico.dispositivoScelto = $("input[type=radio][name='Dispositivo']:checked").val();
	GasStorico.GetStorico();
};

// imposta periodo precedente in base al tipo di periodo scelto ed esegue richiesta
GasStorico.Precedente = function() {
	Log.alert(20, GasStorico.MODULE, "============ Precedente: periodoScelto = " + GasStorico.periodoScelto);
	switch (GasStorico.periodoScelto)
	{
	case GasStorico.GIORNO:
		GasStorico.dataInizio.setDate(GasStorico.dataInizio.getDate()-1);
		GasStorico.dataFine.setDate(GasStorico.dataFine.getDate()-1);
		break;
	
	case GasStorico.SETTIMANA:
		GasStorico.dataInizio.setDate(GasStorico.dataInizio.getDate()-7);
		GasStorico.dataFine.setDate(GasStorico.dataFine.getDate()-7);
		break;
		
	case GasStorico.MESE:
		GasStorico.dataInizio.setMonth(GasStorico.dataInizio.getMonth()-1);
		// ultimo giorno del mese precedente
		GasStorico.dataFine = new Date(GasStorico.dataFine.getFullYear(), GasStorico.dataFine.getMonth(),
				0, 12, 00, 00);
		break;
		
	case GasStorico.ANNO:
		GasStorico.dataInizio.setFullYear(GasStorico.dataInizio.getFullYear()-1);
		// ultimo giorno dell'anno precedente
		GasStorico.dataFine.setFullYear(GasStorico.dataFine.getFullYear()-1);
		GasStorico.dataFine.setMonth(11);
		GasStorico.dataFine.setDate(31);
		break;
	}
	$("#Succ").show();
	// se data inizio e' prima data possibile nascondo freccia precedente
	if (GasStorico.dataInizio.getTime() <= GasStorico.installData.getTime())
	{
		//GasStorico.dataInizio.setTime(GasStorico.primaData.getTime());
		$("#Prec").hide();
	}
	else
		$("#Prec").show();
	GasStorico.GetStorico();
};

//imposta periodo successivo in base al tipo di periodo scelto ed esegue richiesta
GasStorico.Successivo = function() {
	Log.alert(20, GasStorico.MODULE, "=========== Successivo: periodoScelto = " + GasStorico.periodoScelto);
	switch (GasStorico.periodoScelto)
	{
	case GasStorico.GIORNO:
		GasStorico.dataInizio.setDate(GasStorico.dataInizio.getDate()+1);
		GasStorico.dataFine.setDate(GasStorico.dataFine.getDate()+1);
		break;
	
	case GasStorico.SETTIMANA:
		GasStorico.dataInizio.setDate(GasStorico.dataInizio.getDate()+7);
		GasStorico.dataFine.setDate(GasStorico.dataFine.getDate()+7);
		break;
		
	case GasStorico.MESE:
		GasStorico.dataInizio.setMonth(GasStorico.dataInizio.getMonth()+1);
		GasStorico.dataFine = new Date(GasStorico.dataFine.getFullYear(), GasStorico.dataFine.getMonth()+2,
				0, 12, 59, 59);
		break;
		
	case GasStorico.ANNO:
		GasStorico.dataInizio.setFullYear(GasStorico.dataInizio.getFullYear()+1);
		//GasStorico.dataInizio.setMonth(0);
		//GasStorico.dataInizio.setDate(1);
		GasStorico.dataFine.setFullYear(GasStorico.dataFine.getFullYear()+1);
		//GasStorico.dataFine.setMonth(11);
		//GasStorico.dataFine.setDate(31);
		break;
	}
	$("#Prec").show();
	// se data  fine e' ieri nascondo freccia successivo
	ieri = new Date(GasGestDate.GetActualDate().getTime());
	//ieri.setDate(ieri.getDate()-1);
	ieri.setHours(0);
	ieri.setMinutes(0);
	ieri.setSeconds(0);
	ieri.setMilliseconds(0);
	if ((GasStorico.dataFine.getTime()) >= ieri.getTime())
		$("#Succ").hide();
	else
		$("#Succ").show();
	GasStorico.GetStorico();
};

// la prima volta default tutti e giorno, poi seleziona gli ultimi valori selezionati
GasStorico.VisScelta = function() {
	var listDisp = "", tmp = "", tutti;
	
	/** imposto la prima e l'ultima data possibili
	GasStorico.primaData = GasStorico.installData;
	GasStorico.ultimaData = GasGestDate.GetActualDate();
	GasStorico.ultimaData.setDate(GasStorico.ultimaData.getDate()-1);	// ieri
	GasStorico.ultimaData.setHours(0);
	**/
	// imposto smart info per tutti
	tutti = "<input class='ButtonScelta' name='Dispositivo' type='radio' checked='checked' value='" + 
	Msg.home["tuttiStorico"] + "'>"  + Msg.home["tuttiStorico"];
	if (GasStorico.datiElettr != null)
	{
		for (i = 0; i < GasStorico.datiElettr.length; i++)
		{
			// creo elenco dei dispositivi selezionabili
			if (GasStorico.datiElettr[i].tipo == GasInterfaceEnergyHome.SMARTINFO_APP_TYPE)
			{
				tutti = "<input class='ButtonScelta' name='Dispositivo' type='radio' checked='checked' value='" + 
					GasStorico.datiElettr[i].nome + "'>"  + Msg.home["tuttiStorico"];
				//GasStorico.dispositivoScelto = GasStorico.datiElettr[i].nome;
			}
			else
				tmp = tmp + "<br><input class='ButtonScelta' name='Dispositivo' type='radio' value='" + 
					GasStorico.datiElettr[i].nome + "'>" + GasStorico.datiElettr[i].nome;
		}
		listaDisp = tutti + tmp;
//		$("#SceltaDispositivo").html(listaDisp); TODO ripristinare, visualizza i radio button relativi al dispositivo
	}
	else
	{
		$("#SceltaDispositivo").html("<input class='ButtonScelta' name='Dispositivo' type='radio' checked='checked' value='" + 
				Msg.home["tuttiStorico"] + "'>"  + Msg.home["tuttiStorico"]);
	}
	GasStorico.periodoScelto = GasStorico.GIORNO;
	GasStorico.tipoUltimoPeriodo = GasStorico.GIORNO;
	GasStorico.dispositivoScelto = Msg.home["tuttiStorico"];
	Log.alert(80, GasStorico.MODULE, "dispositivoScelto = " + GasStorico.dispositivoScelto + 
			" periodoScelto = " + GasStorico.periodoScelto);
	
	// metto data inizio e fine = ieri
	GasStorico.dataFine = new Date(GasGestDate.GetActualDate().getTime());
	GasStorico.dataFine.setDate(GasStorico.dataFine.getDate()-1);
	GasStorico.dataFine.setHours(23);
	GasStorico.dataFine.setMinutes(59);
	GasStorico.dataInizio = new Date(GasStorico.dataFine.getTime());
	GasStorico.dataInizio.setHours(0);
	GasStorico.dataInizio.setMinutes(0);
	
	// gestisco caso giorno installazione == oggi, non ho dati nello GasStorico 
	if ((GasStorico.dataInizio.getTime() < GasStorico.installData.getTime()) &&
			(GasStorico.dataFine.getTime() < GasStorico.installData.getTime()))
	{
		$("#Prec").hide();
		$("#Succ").hide();
		Log.alert(40, GasStorico.MODULE, "VisStorico : nessun dato");
		$("#StoricoGraph").html("<div id='StoricoVuoto'>"  +  Msg.home["noGrafStorico"] + "</div>");
		return;
	}
	
	// imposta gestione scelta
	$("input[type=radio][name='Periodo']").change(GasStorico.SceltaPeriodo);
	$("input[type=radio][name='Dispositivo']").change(GasStorico.SceltaDispositivo);
	
	// gestisce frecce
	// per funzionare su iPad devo mettere un div sopra l'immagine della freccia se non non e' cliccabile
	$("#Prec").click(GasStorico.Precedente);
	$("#Succ").click(GasStorico.Successivo);
	if (GasStorico.dataInizio.getTime() < GasStorico.installData.getTime())
		$("#Prec").hide();
	//$("#Succ").hide();
	
	// visualizza grafico per valori di default
	GasStorico.GetStorico(); 
};


/***********************************************
 * Elenco elettrodomestici per GasStorico
 * Potrebbe essere diverso da quello normale ?
 ***********************************************/ 
GasStorico.DatiElettr = function(val) {
	Log.alert(80, GasStorico.MODULE, "GasStorico.DatiElettr ");
	GasStorico.datiElettr = val;
	hideSpinner();
	GasStorico.VisScelta();	
};

GasStorico.DatiInizio = function(val) {
	Log.alert(80, GasStorico.MODULE, "GasStorico.DatiElettr ");
	GasStorico.installData = new Date(val);
	showSpinner();
	// leggo lista elettrodomestici
	GasInterfaceEnergyHome.GetElettrStorico(GasStorico.DatiElettr);
};

GasStorico.ResizePlot = function(event) {
	if (GasStorico.plot1 != null) {
		GasStorico.plot1.replot(); 
	}
};

/*******************************************************************************
 * Gestisce lo GasStorico dei dati Crea la parte statica di visualizzazione
 ******************************************************************************/
GasStorico.GestStorico = function() {
	Log.alert(80, GasStorico.MODULE, "GasStorico.GestStorico");
	$("#Content").html(GasStorico.htmlContent);
	
	// FIXME: must not bind on window but on the specific div. But it doesn't seems to work!!!
	// FIXME: it doesn't work on IE7 so it has been commented out.
	if (GasStorico.enableResize)
		$(window).bind('resize', GasStorico.ResizePlot);

	// leggo la data di installazione e i dispositivi una sola volta
	if (GasStorico.installData == null)
		GasInterfaceEnergyHome.GetInitialTime(GasStorico.DatiInizio);
	else
		GasStorico.VisScelta();
};
