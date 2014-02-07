var Storico = {
	MODULE : "Storico",
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
	
	htmlContent : "<div class='ContentTitle'>" + Msg.home["titoloStorico"] + "</div><div id='Storico'>" +
			"<img id='StoricoBg' src='" + Define.home["sfondo_sx"] + "'>"  +
			"<div id='MsgStorico' style='display:none'>" + Msg.home["datiMancanti"] + "</div>" +
			"<div id='LabelStoricokWh'>kWh</div><div id='LabelStoricoEuro'>€</div>" +
			"<div id='Prec'><img id='PrecImg' src='" + Define.home["frecciaPrec"] + "'><div id='LabelPrec'></div></div>" +
			"<div id='Succ'><img id='SuccImg' src='" + Define.home["frecciaSucc"] + "'><div id='LabelSucc'></div></div>" +
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

Storico.ExitStorico = function() {
	hideSpinner();
	Main.ResetError();
	Storico.datiCosto = null;
	Storico.datiConsumo = null;
	Log.alert(20, Storico.MODULE, "ExitStorico");
	if (Storico.enableResize)
		$(window).unbind('resize', Storico.ResizePlot);
	
	InterfaceEnergyHome.Abort();
}


Storico.DateIsSame = function(valDate, val1Date)
{
	d1 = valDate.getDate();
	m1 = valDate.getMonth();
	y1 = valDate.getFullYear();
	//oggi = GestDate.GetActualDate();
	d2 = val1Date.getDate();
	m2 = val1Date.getMonth();
	y2 = val1Date.getFullYear();
	if ((d1 == d2) & (m1 == m2) && (y1 == y2))
		return true;
	else
		return false;
}

// controllo se la data specificata rientra tra la data di installazione e quella attuale
Storico.DateIsValid = function(valDate, periodo)
{
	// nel caso di giorno tengo conto dell'ora, altrimenti no
	if (periodo == Storico.GIORNO)
	{
		ora = new Date(GestDate.GetActualDate().getTime());
		tmp = new Date(valDate.getTime());
		// se giorno di installazione non segnalo errore
		//if (Storico.DateIsSame(tmp, Storico.installData))
		//	return true;
		tmp.setMinutes(0); // fine dell'ora attuale
		// controllo per data di installazione
		tmp.setHours(tmp.getHours()+1);
		if (tmp.getTime() < Storico.installData.getTime())
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
		if (tmpI.getTime() < Storico.installData.getTime())
			return false;
		tmpI.setDate(tmpI.getDate()+1);
		domani = new Date(GestDate.GetActualDate().getTime());
		domani.setDate(domani.getDate()+1);
		domani.setHours(0);
		domani.setMinutes(0);
		if (tmpI.getTime() < domani.getTime())
			return true;
	}
	return false;
}


// se tipo = 0 visualizza costi e consumi
// se tipo = 1 visualizza i consumi
Storico.VisStorico = function(tipo) {
	var tickers =  new Array();	
	var maxCosto, maxConsumo, rapp, barW, max;
	var dati1 = null;
	var dati2 = null;

	Storico.datiConsumoNulli = false;
	Storico.datiCostoNulli = false;
	$.jqplot.config.enablePlugins = true;
	$('#StoricoGraph').html(null);
	$("#LabelStoricokWh").hide();
	$("#LabelStoricoEuro").hide();
	//tipo = Storico.SOLO_CONSUMI;
	valTicker = new Date(Storico.dataInizio.getTime());
	valTicker.setHours(0);
	valTicker.setMinutes(0);
	valTicker.setSeconds(0);
	aTicks = 0;
	// se smart info metto tutti
	if (Storico.GetDispTipo(Storico.dispositivoScelto) == InterfaceEnergyHome.SMARTINFO_APP_TYPE)
		disp = Msg.home["tuttiStorico"];
	else
		disp = Storico.dispositivoScelto;
	
	
	if (Storico.periodoScelto == Storico.GIORNO)	
	{
		labelX = Msg.home["oraLabelStorico"];
		titolo = Msg.home["giornoGrafStorico"] + ":  " + Storico.dataInizio.getDate() + "  " + Msg.mesiAbbrev[Storico.dataInizio.getMonth()].toUpperCase() + 
				" " +	Storico.dataInizio.getFullYear() + "  -  " + Msg.home["dispGrafStorico"] + ": " + disp;
		maxCosto = Define.home["limCostoOra"][Main.contatore];
		maxConsumo = Define.home["limConsumoOra"][Main.contatore];
		nTickskWh = 6;
		nTicksE = 6;
		formatStr = "%.2f";
	}
	else
		if (Storico.periodoScelto == Storico.ANNO)
		{
			titolo = Msg.home["daGrafStorico"] + " " + Msg.mesiAbbrev[Storico.dataInizio.getMonth()].toUpperCase() + " " + Storico.dataInizio.getFullYear() + 
			" " + Msg.home["aGrafStorico"] + " " + Msg.mesiAbbrev[Storico.dataFine.getMonth()].toUpperCase() + " " + Storico.dataFine.getFullYear() + 
			"  -  " +  Msg.home["dispGrafStorico"] + ": " + disp;
			labelX = Msg.home["meseLabelStorico"];
			maxCosto = Define.home["limCostoMese"][Main.contatore];
			maxConsumo = Define.home["limConsumoMese"][Main.contatore];
			nTickskWh = 8;
			nTicksE = 8;
			formatStr = "%.1f";
		}
		else
		{
			//titolo = "Dal " + Main.FormatDate(Storico.dataInizio, 2) + " al " +  Main.FormatDate(Storico.dataFine, 2) + " - " + Storico.dispositivoScelto,
			titolo = Msg.home["daGrafStorico"] + " " + Storico.dataInizio.getDate() + " " + Msg.mesiAbbrev[Storico.dataInizio.getMonth()].toUpperCase() + 
			" " + Storico.dataInizio.getFullYear() + " " + Msg.home["aGrafStorico"] + " "  + Storico.dataFine.getDate() + " " + 
			Msg.mesiAbbrev[Storico.dataFine.getMonth()].toUpperCase() + " " + Storico.dataFine.getFullYear() + 
			"  -  " +  Msg.home["dispGrafStorico"] + ": " +  disp;
			if (Storico.periodoScelto == Storico.MESE)
				aTicks = -80;
			labelX = Msg.home["giornoLabelStorico"];
			maxCosto = Define.home["limCostoGiorno"][Main.contatore];
			maxConsumo = Define.home["limConsumoGiorno"][Main.contatore];
			nTickskWh = 6;
			nTicksE = 6;
			formatStr = "%.1f";
		}			
	// non prevedo solo costo per il momento
	if (Storico.datiConsumo == null)
	{
		//$("#Prec").hide();
		//$("#Succ").hide();
		$("#MsgStorico").hide();
		Log.alert(40, Storico.MODULE, "VisStorico : nessun dato");
		$("#StoricoGraph").html("<div id='StoricoVuotoTitolo'>" + titolo + "</div><div id='StoricoVuoto'>" + 
				  Msg.home["noGrafStorico"] + "</div>");
		return;
	}
	
	if (Storico.datiConsumo != null)
		dati1 = Storico.datiConsumo.slice(0); // copio array perche' lo modifico (per simulazione)
	if (Storico.datiCosto != null)
		dati2 = Storico.datiCosto.slice(0);
	else
		tipo = Storico.SOLO_CONSUMI; // se non ho dati dei costi visualizzo solo consumi
	
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
			if (Storico.DateIsValid(valTicker, Storico.periodoScelto))
				Storico.datiConsumoNulli = true;
		}
		else
		{
			dati1[i] = dati1[i] / 1000; // da w a kWh
			// controllo se sono nei limiti altrimenti sposto i limiti
			if (dati1[i] > maxConsumo)
				maxConsumo = dati1[i] * 1.05;
		}
		if ((tipo == Storico.COSTI_CONSUMI) && (dati2 != null))
		{
			if (dati2[i] == null)
			{
				dati2[i] = 0;
				dati1[i] = 0;
				if (Storico.DateIsValid(valTicker, Storico.periodoScelto))
					Storico.datiCostoNulli = true;
			}
			else
			{
				if (dati2[i] > maxCosto)
					maxCosto = dati2[i] * 1.05;
			}
		}
		//label ticks diverso a seconda di cosa sto visualizzando
		if (Storico.periodoScelto == Storico.GIORNO)
		{	
			tickers[i] = valTicker.getHours().toString();
			Log.alert(80, Storico.MODULE, "VisStorico : ora = " + tickers[i]);
			valTicker.setHours(valTicker.getHours()+1);	// giorno
		}	
		else
			if (Storico.periodoScelto == Storico.SETTIMANA)
			{	
				tickers[i] = valTicker.getDate() + "-" + (valTicker.getMonth()+1);
				valTicker.setDate(valTicker.getDate()+1);	// settimana
			}
			else
				if (Storico.periodoScelto == Storico.MESE)
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

	if (tipo == Storico.COSTI_CONSUMI)
	{
		barW = Math.round($('#StoricoGraph').width() * 0.4 /(dati1.length + 2));
		rapp = maxConsumo / maxCosto;
		Log.alert(20, Storico.MODULE, "VisStorico: maxConsumo = " + maxConsumo + " maxCosto = " + maxCosto +
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
	if (tipo == Storico.COSTI_CONSUMI)
	{
		Storico.plot1 = $.jqplot('StoricoGraph', [dati1, dati2], {
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
		Storico.plot1 = $.jqplot('StoricoGraph', [dati1], {
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
				html = "<div class='highlightbarStorico'>" + data[1].toFixed(3) + " kWh</div>";
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
			Log.alert(20, CostiConsumi.MODULE, 'click -> series: '+seriesIndex+', point: '+pointIndex+
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
	if ((Storico.periodoScelto == Storico.GIORNO) &&
			Storico.DateIsSame(Storico.dataInizio, Storico.installData))
		$("#MsgStorico").hide();
	else
		if ((Storico.datiCostoNulli) || (Storico.datiConsumoNulli))
			$("#MsgStorico").show();
		else
			$("#MsgStorico").hide();
	
}


Storico.DatiConsumoStorico = function(val) {
	
	Storico.datiConsumo = val;
	if (val == null)
		Log.alert(80, Storico.MODULE, "DatiConsumoStorico null");
	hideSpinner();
	Log.alert(80, Storico.MODULE, "DatiConsumoStorico = " + val);
	// Nella prima fase del trial gestisco solo i dati del consumo
	if (InterfaceEnergyHome.mode == InterfaceEnergyHome.MODE_FULL)
		Storico.VisStorico(1);
	else
		Storico.VisStorico(0);
}

Storico.DatiCostoStorico = function(val) {
	Storico.datiCosto = val;
	if (val == null)
		Log.alert(80, Storico.MODULE, "DatiCostoStorico null");

	Log.alert(80, Storico.MODULE, "DatiCostoStorico = " + val);
	InterfaceEnergyHome.GetStorico("Consumo", Storico.device, Storico.dataInizio, Storico.dataFine, Storico.periodoScelto, Storico.DatiConsumoStorico);
}

/*************************************************
 * Legge i dati del costo e del consumo 
 * relativi al periodo e dispositivo selezionato
 *************************************************/
Storico.GetStorico = function() {
	Main.ResetError();
	showSpinner();
	Log.alert(20, Storico.MODULE, "Dispositivo= " + Storico.dispositivoScelto + " Periodo = " + Storico.periodoScelto);
	Log.alert(20, Storico.MODULE, "============ GetStorico: inizio = " + Storico.dataInizio.toString() + 
			" fine = " + Storico.dataFine.toString());
	
	// imposta id del dispositivo selezionato
	Storico.device = Storico.GetDispId(Storico.dispositivoScelto);
	Log.alert(80, Storico.MODULE, "Periodo: pid = " + Storico.device + " inizio = " + Storico.dataInizio.toString() + " fine = " + Storico.dataFine.toString());
	
	// Nella prima fase del trial gestisco solo i dati del consumo
	if (InterfaceEnergyHome.mode == InterfaceEnergyHome.MODE_FULL)
		InterfaceEnergyHome.GetStorico("Consumo", Storico.device, Storico.dataInizio, Storico.dataFine, Storico.periodoScelto, Storico.DatiConsumoStorico);
	else
		InterfaceEnergyHome.GetStorico("Costo", Storico.device, Storico.dataInizio, Storico.dataFine, Storico.periodoScelto, Storico.DatiCostoStorico);
}

Storico.GetDispId = function(nomeElettr) {
	if (Storico.datiElettr != null)
		for (i = 0; i < Storico.datiElettr.length; i++)
			if (Storico.datiElettr[i].nome == nomeElettr)
				return Storico.datiElettr[i].pid;
	return null;
}

Storico.GetDispTipo = function(nomeElettr) {
	if (Storico.datiElettr != null)
		for (i = 0; i < Storico.datiElettr.length; i++)
			if (Storico.datiElettr[i].nome == nomeElettr)
				return Storico.datiElettr[i].tipo;
	return null;
}

// se cambio in breve tempo sia periodo che dispositivo (o che date con le frecce)
// potrei lanciare una chiamata prima che sia finita quella precedente
// bisogna vedere cosa succede
// cambio le date in base al periodo precedente e al tipo di periodo scelto
// non devo mettere ora 23 nel caso di richiesta di giorno perche' quando c'e' l'ora legale 
// altrimenti passo al giorno dopo
Storico.SceltaPeriodo = function() {
	Storico.periodoScelto = parseInt($("input[type=radio][name='Periodo']:checked").val());
	Log.alert(20, Storico.MODULE, "========== SceltaPeriodo: precedente = " + Storico.tipoUltimoPeriodo +
					" nuovo = " + Storico.periodoScelto);
	if (Storico.periodoScelto != Storico.tipoUltimoPeriodo)
	{
		$("#Prec").show();
		$("#Succ").show();
		switch (Storico.periodoScelto)
		{
		case Storico.GIORNO:
			// prendo sempre l'ultimo giorno del periodo attualmente visualizzato
			// a meno che il giorno non sia maggiore di ieri
			ieri = new Date(GestDate.GetActualDate().getTime());
			ieri.setHours(0);
			//oggi.setMinutes(0);
			//oggi.setSeconds(0);
			//oggi.setMilliseconds(0);
			if (Storico.dataFine.getTime() >= ieri.getTime())
			{
				// prendo ieri
				Storico.dataFine = new Date(ieri.getTime());
				Storico.dataFine.setDate(Storico.dataFine.getDate()-1);
			}
			Storico.dataFine.setHours(23);
			Storico.dataFine.setMinutes(59);
			Storico.dataInizio = new Date(Storico.dataFine.getTime());
			Storico.dataInizio.setHours(0);
			Storico.dataInizio.setMinutes(0);
			break;
			
		case Storico.SETTIMANA:
			// prendo settimana da lunedi' a domenica (N.B. 0 = domenica, 1 = lunedi')
			// prendo la settimana in cui cade l'ultimo giorno 
			// data fine <= ieri
			ieri = new Date(GestDate.GetActualDate().getTime());
			ieri.setDate(ieri.getDate()-1);
			ieri.setHours(0);
			ieri.setMinutes(0);
			ieri.setSeconds(0);
			ieri.setMilliseconds(0);
			if ((Storico.dataFine.getTime()) >= ieri.getTime())
			Storico.dataFine = new Date(ieri.getTime());
			Storico.dataFine.setHours(12);
			day = Storico.dataFine.getDay();
			// se domenica resta giorno finale della settimana
			if (day != 0)
			{
				// ultimo giorno = differenza tra domenica e il giorno in questione
				Storico.dataFine.setDate(Storico.dataFine.getDate()+(7-day));
			}
			Storico.dataInizio = new Date(Storico.dataFine.getTime());
			Storico.dataInizio.setDate(Storico.dataInizio.getDate()-6);
			Storico.dataInizio.setHours(0);
			Storico.dataFine.setHours(12);
			break;
			
		case Storico.MESE:
			if (Storico.tipoUltimoPeriodo == Storico.ANNO)
			{
				// prendo dicembre o ultimo mese
				Storico.dataInizio = new Date(Storico.dataFine.getTime());
				Storico.dataInizio.setMonth(11);
				Storico.dataInizio.setDate(1);
				Storico.dataInizio.setHours(0);
				Storico.dataInizio.setMinutes(0);
				
			}
			else
			{
				// prendo il mese in cui cade l'ultimo giorno selezionato
				Storico.dataInizio = new Date(Storico.dataFine.getTime());
				Storico.dataInizio.setDate(1); // primo giorno del mese
			}
			if (Storico.dataInizio.getTime() > GestDate.GetActualDate().getTime())
			{
				Storico.dataInizio = new Date(GestDate.GetActualDate().getTime());
				Storico.dataInizio.setDate(1);
			}
			// ultimo giorno del mese (giorno prima del primo giorno del mese successivo)
			Storico.dataFine = new Date(Storico.dataInizio.getTime());
			Storico.dataFine.setMonth(Storico.dataFine.getMonth()+1);
			Storico.dataFine.setDate(Storico.dataFine.getDate()-1);
			Storico.dataInizio.setHours(0);
			Storico.dataFine.setHours(12);
			break;
			
		case Storico.ANNO:
			// prendo sempre l'anno dell'ultimo giorno selezionato
			Storico.dataInizio = new Date(Storico.dataFine.getTime());
			Storico.dataInizio.setDate(1);
			Storico.dataInizio.setMonth(0);
			Storico.dataFine = new Date(Storico.dataInizio.getTime());
			Storico.dataFine.setDate(31);
			Storico.dataFine.setMonth(11);
			Storico.dataInizio.setHours(0);
			Storico.dataFine.setHours(12);
			break;
			
		}
		// controllo di non superare i limiti temporali
		if (Storico.dataInizio.getTime() < Storico.installData.getTime())
			$("#Prec").hide();
		
		// data fine <= ieri
		ieri = new Date(GestDate.GetActualDate().getTime());
		//ieri.setDate(ieri.getDate()-1);
		ieri.setHours(0);
		ieri.setMinutes(0);
		ieri.setSeconds(0);
		ieri.setMilliseconds(0);
		if ((Storico.dataFine.getTime()) >= ieri.getTime())
			$("#Succ").hide();
		
		Storico.tipoUltimoPeriodo = Storico.periodoScelto;
		Storico.GetStorico();
	}
	else
	{
		Log.alert(80, Storico.MODULE, "SceltaPeriodo: non ho cambiato tipo di periodo");
	}
}

// cambio solo dispositivo, lascio le date attuali
Storico.SceltaDispositivo = function() {
	Storico.dispositivoScelto = $("input[type=radio][name='Dispositivo']:checked").val();
	Storico.GetStorico();
}

// imposta periodo precedente in base al tipo di periodo scelto ed esegue richiesta
Storico.Precedente = function() {
	Log.alert(20, Storico.MODULE, "============ Precedente: periodoScelto = " + Storico.periodoScelto);
	switch (Storico.periodoScelto)
	{
	case Storico.GIORNO:
		Storico.dataInizio.setDate(Storico.dataInizio.getDate()-1);
		Storico.dataFine.setDate(Storico.dataFine.getDate()-1);
		break;
	
	case Storico.SETTIMANA:
		Storico.dataInizio.setDate(Storico.dataInizio.getDate()-7);
		Storico.dataFine.setDate(Storico.dataFine.getDate()-7);
		break;
		
	case Storico.MESE:
		Storico.dataInizio.setMonth(Storico.dataInizio.getMonth()-1);
		// ultimo giorno del mese precedente
		Storico.dataFine = new Date(Storico.dataFine.getFullYear(), Storico.dataFine.getMonth(),
				0, 12, 00, 00);
		break;
		
	case Storico.ANNO:
		Storico.dataInizio.setFullYear(Storico.dataInizio.getFullYear()-1);
		// ultimo giorno dell'anno precedente
		Storico.dataFine.setFullYear(Storico.dataFine.getFullYear()-1);
		Storico.dataFine.setMonth(11);
		Storico.dataFine.setDate(31);
		break;
	}
	$("#Succ").show();
	// se data inizio e' prima data possibile nascondo freccia precedente
	if (Storico.dataInizio.getTime() <= Storico.installData.getTime())
	{
		//Storico.dataInizio.setTime(Storico.primaData.getTime());
		$("#Prec").hide();
	}
	else
		$("#Prec").show();
	Storico.GetStorico();
}

//imposta periodo successivo in base al tipo di periodo scelto ed esegue richiesta
Storico.Successivo = function() {
	Log.alert(20, Storico.MODULE, "=========== Successivo: periodoScelto = " + Storico.periodoScelto);
	switch (Storico.periodoScelto)
	{
	case Storico.GIORNO:
		Storico.dataInizio.setDate(Storico.dataInizio.getDate()+1);
		Storico.dataFine.setDate(Storico.dataFine.getDate()+1);
		break;
	
	case Storico.SETTIMANA:
		Storico.dataInizio.setDate(Storico.dataInizio.getDate()+7);
		Storico.dataFine.setDate(Storico.dataFine.getDate()+7);
		break;
		
	case Storico.MESE:
		Storico.dataInizio.setMonth(Storico.dataInizio.getMonth()+1);
		Storico.dataFine = new Date(Storico.dataFine.getFullYear(), Storico.dataFine.getMonth()+2,
				0, 12, 59, 59);
		break;
		
	case Storico.ANNO:
		Storico.dataInizio.setFullYear(Storico.dataInizio.getFullYear()+1);
		//Storico.dataInizio.setMonth(0);
		//Storico.dataInizio.setDate(1);
		Storico.dataFine.setFullYear(Storico.dataFine.getFullYear()+1);
		//Storico.dataFine.setMonth(11);
		//Storico.dataFine.setDate(31);
		break;
	}
	$("#Prec").show();
	// se data  fine e' ieri nascondo freccia successivo
	ieri = new Date(GestDate.GetActualDate().getTime());
	//ieri.setDate(ieri.getDate()-1);
	ieri.setHours(0);
	ieri.setMinutes(0);
	ieri.setSeconds(0);
	ieri.setMilliseconds(0);
	if ((Storico.dataFine.getTime()) >= ieri.getTime())
		$("#Succ").hide();
	else
		$("#Succ").show();
	Storico.GetStorico();
}

// la prima volta default tutti e giorno, poi seleziona gli ultimi valori selezionati
Storico.VisScelta = function() {
	var listDisp = "", tmp = "", tutti;
	
	/** imposto la prima e l'ultima data possibili
	Storico.primaData = Storico.installData;
	Storico.ultimaData = GestDate.GetActualDate();
	Storico.ultimaData.setDate(Storico.ultimaData.getDate()-1);	// ieri
	Storico.ultimaData.setHours(0);
	**/
	// imposto smart info per tutti
	tutti = "<input class='ButtonScelta' name='Dispositivo' type='radio' checked='checked' value='" + 
	Msg.home["tuttiStorico"] + "'>"  + Msg.home["tuttiStorico"];
	if (Storico.datiElettr != null)
	{
		for (i = 0; i < Storico.datiElettr.length; i++)
		{
			// creo elenco dei dispositivi selezionabili
			if (Storico.datiElettr[i].tipo == InterfaceEnergyHome.SMARTINFO_APP_TYPE)
			{
				tutti = "<input class='ButtonScelta' name='Dispositivo' type='radio' checked='checked' value='" + 
					Storico.datiElettr[i].nome + "'>"  + Msg.home["tuttiStorico"];
				//Storico.dispositivoScelto = Storico.datiElettr[i].nome;
			}
			else
				tmp = tmp + "<br><input class='ButtonScelta' name='Dispositivo' type='radio' value='" + 
					Storico.datiElettr[i].nome + "'>" + Storico.datiElettr[i].nome;
		}
		listaDisp = tutti + tmp;
		$("#SceltaDispositivo").html(listaDisp);
	}
	else
	{
		$("#SceltaDispositivo").html("<input class='ButtonScelta' name='Dispositivo' type='radio' checked='checked' value='" + 
				Msg.home["tuttiStorico"] + "'>"  + Msg.home["tuttiStorico"]);
	}
	Storico.periodoScelto = Storico.GIORNO;
	Storico.tipoUltimoPeriodo = Storico.GIORNO;
	Storico.dispositivoScelto = Msg.home["tuttiStorico"];
	Log.alert(80, Storico.MODULE, "dispositivoScelto = " + Storico.dispositivoScelto + 
			" periodoScelto = " + Storico.periodoScelto);
	
	// metto data inizio e fine = ieri
	Storico.dataFine = new Date(GestDate.GetActualDate().getTime());
	Storico.dataFine.setDate(Storico.dataFine.getDate()-1);
	Storico.dataFine.setHours(23);
	Storico.dataFine.setMinutes(59);
	Storico.dataInizio = new Date(Storico.dataFine.getTime());
	Storico.dataInizio.setHours(0);
	Storico.dataInizio.setMinutes(0);
	
	// gestisco caso giorno installazione == oggi, non ho dati nello storico 
	if ((Storico.dataInizio.getTime() < Storico.installData.getTime()) &&
			(Storico.dataFine.getTime() < Storico.installData.getTime()))
	{
		$("#Prec").hide();
		$("#Succ").hide();
		Log.alert(40, Storico.MODULE, "VisStorico : nessun dato");
		$("#StoricoGraph").html("<div id='StoricoVuoto'>"  +  Msg.home["noGrafStorico"] + "</div>");
		return;
	}
	
	// imposta gestione scelta
	$("input[type=radio][name='Periodo']").change(Storico.SceltaPeriodo);
	$("input[type=radio][name='Dispositivo']").change(Storico.SceltaDispositivo);
	
	// gestisce frecce
	// per funzionare su iPad devo mettere un div sopra l'immagine della freccia se non non e' cliccabile
	$("#Prec").click(Storico.Precedente);
	$("#Succ").click(Storico.Successivo);
	if (Storico.dataInizio.getTime() < Storico.installData.getTime())
		$("#Prec").hide();
	//$("#Succ").hide();
	
	// visualizza grafico per valori di default
	Storico.GetStorico(); 
}


/***********************************************
 * Elenco elettrodomestici per storico
 * Potrebbe essere diverso da quello normale ?
 ***********************************************/ 
Storico.DatiElettr = function(val) {
	Log.alert(80, Storico.MODULE, "Storico.DatiElettr ");
	Storico.datiElettr = val;
	hideSpinner();
	Storico.VisScelta();	
}

Storico.DatiInizio = function(val) {
	Log.alert(80, Storico.MODULE, "Storico.DatiElettr ");
	Storico.installData = new Date(val);
	showSpinner();
	// leggo lista elettrodomestici
	InterfaceEnergyHome.GetElettrStorico(Storico.DatiElettr);
}

Storico.ResizePlot = function(event) {
	if (Storico.plot1 != null) {
		Storico.plot1.replot(); 
	}
}

/*******************************************************************************
 * Gestisce lo storico dei dati Crea la parte statica di visualizzazione
 ******************************************************************************/
Storico.GestStorico = function() {
	Log.alert(80, Storico.MODULE, "Storico.GestStorico");
	$("#Content").html(Storico.htmlContent);
	
	// FIXME: must not bind on window but on the specific div. But it doesn't seems to work!!!
	// FIXME: it doesn't work on IE7 so it has been commented out.
	if (Storico.enableResize)
		$(window).bind('resize', Storico.ResizePlot);

	// leggo la data di installazione e i dispositivi una sola volta
	if (Storico.installData == null)
		InterfaceEnergyHome.GetInitialTime(Storico.DatiInizio);
	else
		Storico.VisScelta();
}
