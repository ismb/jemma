var Storico = {
	MODULE : "Storico",
	dataInizio : null,
	dataFine : null,
	dispositivi : null,
	indDispositivo : -1,
	periodoScelto: null,
	dispositivoScelto: null,
	idDispositivoScelto: null,
	intervallo: 0,
	datiElettr : null,
	datiCosto : null,
	datiConsumo : null,
	titoloGraph : null,
	mesi : ["Gen", "Feb", "Mar", "Apr", "Mag", "Giu", "Lug", "Ago", "Set", "Ott", "Nov", "Dic"],
	htmlContent : "<div id='Storico'><div id='StoricoTitle'>Storico costi e consumi</div><div id='StoricoGraph'></div><div id='StoricoScelta'></div></div>",
	
	htmlScelta : "<div id='TitoloSceltaPeriodo'>Scegli il periodo</div><div id='SceltaPeriodo'>" +
			 "<input class='ButtonScelta' name='Periodo' type='radio' checked='checked' value='Ieri'>Ieri<br>" + 
      		 "<input class='ButtonScelta' name='Periodo' type='radio' value='Ultima settimana'>Ultima settimana<br>" +     
      		 "<input class='ButtonScelta' name='Periodo' type='radio' value='Ultimo mese'>Ultimo mese<br>" +     
      		 "<input class='ButtonScelta' name='Periodo' type='radio' value='Ultimo anno'>Ultimo anno<br></div>"  +
			 "<div id='TitoloSceltaDispositivo'>Scegli il dispositivo</div><div id='SceltaDispositivo'></div>" +
			 "<input id='ButtonScegliStorico' name='ScegliStorico' type='button' value='Aggiorna'>"
};

Storico.ExitStorico = function() {
	datiCosto = null;
	datiConsumo = null;
	InterfaceEnergyHome.Abort();
}

Storico.VisStorico = function() {
	var tickers =  new Array();	
	var maxCosto, maxConsumo, rapp;
	var dati1;
	var dati2;

	$.jqplot.config.enablePlugins = true;
	$('#StoricoGraph').html(null);

	valTicker = new Date(Storico.dataInizio.getTime());
	valTicker.setHours(0);
	valTicker.setMinutes(0);
	valTicker.setSeconds(0);
	aTicks = 0;
	if (Storico.intervallo == 0)	
	{
		labelX = "Ora";
		titolo = "Giorno " + Storico.dataInizio.getDate() + "  " + Storico.mesi[Storico.dataInizio.getMonth()] + " " +  
				Storico.dataInizio.getFullYear() + " - " + Storico.dispositivoScelto;
		maxCosto = 1.5;
		maxConsumo = 4.5;
		nTickskWh = 10;
		nTicksE = 4;
		formatStr = "%.1f";
	}
	else
		if (Storico.intervallo == 3)
		{
			titolo = "Da " + Storico.mesi[Storico.dataInizio.getMonth()] + " " + Storico.dataInizio.getFullYear() + " a " + 
					Storico.mesi[Storico.dataFine.getMonth()] + " " + Storico.dataFine.getFullYear() + " - " + Storico.dispositivoScelto;
			labelX = "Mese";
			maxCosto = 100;
			maxConsumo = 300;
			nTickskWh = 7;
			nTicksE = 6;
			formatStr = "%i";
		}
		else
		{
			//titolo = "Dal " + Main.FormatDate(Storico.dataInizio, 2) + " al " +  Main.FormatDate(Storico.dataFine, 2) + " - " + Storico.dispositivoScelto,
			titolo = "Dal " + Storico.dataInizio.getDate() + " " + Storico.mesi[Storico.dataInizio.getMonth()] + " " + Storico.dataInizio.getFullYear() + " al " + 
					Storico.dataFine.getDate() + " " + Storico.mesi[Storico.dataFine.getMonth()] + " " + Storico.dataFine.getFullYear() + " - " + Storico.dispositivoScelto;
			if (Storico.intervallo == 2)
				aTicks = -80;
			labelX = "Giorno";
			maxCosto = 5;
			maxConsumo = 15;
			nTickskWh = 7;
			nTicksE = 6;
			formatStr = "%i";
		}			
		
	if ((Storico.datiConsumo != null) && (Storico.datiCosto != null))
	{
		
		dati1 = Storico.datiCosto;
		dati2 = Storico.datiConsumo.slice(0); // copio array perche' lo modifico
		barW = Math.round($('#StoricoGraph').width() * 0.4 /(dati2.length + 2));
		rapp = maxConsumo / maxCosto;
		
		// creo array con i dati e creo le label per i tickers
		for (i = 0; i < dati2.length; i++)
		{
			// moltiplico il costo per il rapporto max consumo/costo perche' i dati sono sempre messi rispetto asse consumo
			dati2[i] = (dati2[i] / 1000 / rapp).toFixed(2); // da w a kWh
			//label ticks diverso a seconda di cosa sto visualizzando
			if (Storico.intervallo == 0)
			{	
				tickers[i] = valTicker.getHours().toString();
				Log.alert(80, Storico.MODULE, "VisStorico : ora = " + tickers[i]);
				valTicker.setHours(valTicker.getHours()+1);	// ieri
			}	
			else
				if (Storico.intervallo == 1)
				{	
					tickers[i] = valTicker.getDate() + "-" + (valTicker.getMonth()+1);
					valTicker.setDate(valTicker.getDate()+1);	//  ultima settimana
				}
				else
					if (Storico.intervallo == 2)
					{	
						tickers[i] = valTicker.getDate() + "-" + (valTicker.getMonth()+1);
						valTicker.setDate(valTicker.getDate()+1);	//  ultimo mese
					}
					else
					{				
						tickers[i] = Storico.mesi[valTicker.getMonth()] + "-" + (valTicker.getFullYear()-2000);
						valTicker.setMonth(valTicker.getMonth()+1);	//  ultimo mese
					}
		}

		plot1 = $.jqplot('StoricoGraph', [dati1, dati2], {
		title: {
			text : titolo,
        		show: true
    		},
    		legend: {
            	show: true,
		      location: 'n',     // compass direction, nw, n, ne, e, se, s, sw, w.
        		xoffset: 0,        // pixel offset of the legend box from the x (or x2) axis.
		      yoffset: 0,        // pixel offset of the legend box from the y (or y2) axis.
            	placement: 'outsideGrid'
      	},
		seriesColors: ["#ff8000", "#2020ff"],
		seriesDefaults:{
			renderer:$.jqplot.BarRenderer,
     			rendererOptions: {barMargin: 3, barPadding:0, barWidth: barW}
		},
		series: [ 
			{label:'Costo', color:'#ff8000'},			
      	      {label:'Consumo', color:'#2020ff'},
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
				min: 0,
				max: maxCosto,
				numberTicks: nTicksE, 
				label: '€',
				labelOptions: {textColor:'#ff8000', fontSize:'1.6em'},
				tickOptions:{showGridline:false,formatString:formatStr}, 
				autoscale:true
			},
			y2axis: {
				show: true,
				min: 0,
				max: maxConsumo,
				numberTicks: nTickskWh,
				label: 'kWh',
				labelOptions: {textColor:'#2020ff'},
				tickOptions:{formatString:formatStr},
				autoscale: true
			}
		}
    		});
 	}
	else
	{
		Log.alert(40, Storico.MODULE, "VisStorico : nessun dato");
		$("#StoricoGraph").html("<div id='StoricoVuoto'> Nessun dato disponibile</div>");
	}
		
}


Storico.DatiConsumoStorico = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	
	if (val != null)
		Storico.datiConsumo = val;
	else
		Log.alert(80, Storico.MODULE, "DatiConsumoStorico null");

	Log.alert(80, Storico.MODULE, "DatiConsumoStorico = " + val);
	Storico.VisStorico();
}

Storico.DatiCostoStorico = function(val) {
	// se il dato e' null c'e' errore, non aggiorno il valore attuale
	// vado comunque avanti con le chiamate
	
	if (val != null)
		Storico.datiCosto = val;
	else
		Log.alert(80, Storico.MODULE, "DatiCostoStorico null");

	Log.alert(80, Storico.MODULE, "DatiCostoStorico = " + val);
	InterfaceEnergyHome.GetStorico("Consumo", Storico.device, Storico.dataInizio, Storico.dataFine, Storico.intervallo, Storico.DatiConsumoStorico);
}

/*************************************************
 * Legge i dati del costo e del consumo 
 * relativi al periodo e dispositivo selezionato
 *************************************************/
Storico.GetStorico = function() {
	Log.alert(80, Storico.MODULE, "Dispositivo= " + Storico.dispositivoScelto + " Periodo = " + Storico.periodoScelto);
	// data fine = ieri sera
	Storico.dataFine = GestDate.GetActualDate();
	Storico.dataFine.setDate(Storico.dataFine.getDate()-1);
	// imposta data inizio in base a scelta utente
	Storico.dataInizio = new Date(Storico.dataFine.getTime());
	if (Storico.periodoScelto == "Ieri")
	{
		Storico.intervallo = 0; // ore
	}
	else
		if (Storico.periodoScelto == "Ultima settimana")
		{
			Storico.intervallo = 1; // giorni
			Storico.dataInizio.setDate(Storico.dataInizio.getDate() - 7);
		}
		else
			if (Storico.periodoScelto == "Ultimo mese")
			{
				Storico.intervallo = 2; // giorni // cambiare
				Storico.dataInizio.setMonth(Storico.dataInizio.getMonth() - 1);
			}
			else
				if (Storico.periodoScelto == "Ultimo anno")
				{
					Storico.intervallo = 3; // mesi
					Storico.dataInizio.setFullYear(Storico.dataInizio.getFullYear()-1);
				}

	// imposta id del dispositivo selezionato
	if (Storico.dispositivoScelto == "Totale")
		Storico.device = "homeauto";
	else
		Storico.device = Storico.GetDispId(Storico.dispositivoScelto);
	Log.alert(80, Storico.MODULE, "Periodo: pid = " + Storico.device + " inizio = " + Storico.dataInizio.toString() + " fine = " + Storico.dataFine.toString());
	InterfaceEnergyHome.GetStorico("Costo", Storico.device, Storico.dataInizio, Storico.dataFine, Storico.intervallo, Storico.DatiCostoStorico);
}

Storico.GetDispId = function(nomeElettr) {
	for (i = 0; i < Storico.datiElettr.length; i++)
		if (Storico.datiElettr[i].nome == nomeElettr)
			return Storico.datiElettr[i].pid;
	return "homeauto";
}

Storico.SceltaPeriodo = function() {
	Storico.periodoScelto = $("input[type=radio][name='Periodo']:checked").val();
}

Storico.SceltaDispositivo = function() {
	Storico.dispositivoScelto = $("input[type=radio][name='Dispositivo']:checked").val();
}


Storico.VisScelta = function() {
	listaDisp = "<input class='ButtonScelta' name='Dispositivo' type='radio' checked='checked' value='Totale'>Totale";
	if (Storico.datiElettr != null)
	{
		for (i = 0; i < Storico.datiElettr.length; i++)
		{
			// creo elenco dei dispositivi selezionabili
			listaDisp += "<br><input class='ButtonScelta' name='Dispositivo' type='radio' value='" + Storico.datiElettr[i].nome + "'>" +
				 Storico.datiElettr[i].nome;
		}
	}
	Storico.dispositivoScelto = "Totale";
	Storico.periodoScelto == "Ieri";
	$("#SceltaDispositivo").html(listaDisp);
	Log.alert(80, Storico.MODULE, "listaDisp = " + listaDisp);
	// imposta gestione scelta
	$("input[type=radio][name='Periodo']").change(Storico.SceltaPeriodo);
	$("input[type=radio][name='Dispositivo']").change(Storico.SceltaDispositivo);
	$("#ButtonScegliStorico").click(Storico.GetStorico);

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
	Storico.VisScelta();	
}

/********************************************
 * Gestisce lo storico dei dati
 * Crea la parte statica di visualizzazione
 ********************************************/
Storico.GestStorico = function() {
	Log.alert(80, Storico.MODULE, "Storico.GestStorico");
	$("#Content").html(Storico.htmlContent);

	$("#StoricoScelta").html(Storico.htmlScelta);
	// leggo i dispositivi una sola volta
	if (Storico.datiElettr == null)
		InterfaceEnergyHome.GetElettrStorico(Storico.DatiElettr);
	else
		Storico.VisScelta();
}

