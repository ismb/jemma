var Elettrodomestici = {
	MODULE : "Elettrodomestici ",
	TIMER_UPDATE_ELETTR : 5000,
	listaElettrodomestici : [],
	lista1ricevuta : {},
	SmartInfo : null,
	locazioni : [],
	timerElettr : null,
	indexElettrodomestico : 0,
	numDispositivi : 0,
	numDispoSchermo : 0,
	numMaxDispoSchermo : 6,
	timerDispo : null,
	requestCB: null,
	startTime: null,
	midTime: null,
	endTime: null,
	consumoTotale: 0,
	altroConsumo: 0,
	potenzaAttuale : {},
	htmlContent: $(document.createElement('div')).attr('id', 'Elettrodomestici').attr('class', 'Content')
												 .append($(document.createElement('div')).attr('id', 'ElettrodomesticiTitolo').text(Msg.home["titoloDispositivi"]))
												 .append($(document.createElement('div')).attr('id', 'Pulsanti')
														 								 .append($(document.createElement('button')).attr('id', 'backDevices'))
														 								 .append($(document.createElement('button')).attr('id', 'nextDevices')))
												 .append($(document.createElement('div')).attr('id', 'RigaElettrodomestici'))
												 .append($(document.createElement('div')).attr('class', 'TitoloConsumo').text(Msg.home["titoloConsumoDisp"]))
												 .append($(document.createElement('div')).attr('id', 'ConsumoRow').attr('class', 'RigaDati'))
												 .append($(document.createElement('div')).attr('class', 'TitoloConsumo').attr('id', 'TotaleDisp'))
												 .append($(document.createElement('div')).attr('class', 'TitoloConsumo').attr('id', 'AltriConsumi'))
												 .append($(document.createElement('div')).attr('class', 'TitoloConsumo').attr('id', 'TotaleConsumi'))
												 //.append($(document.createElement('div')).attr('id', 'LocRow').attr('class', 'RigaDati'))
												 .append($(document.createElement('div')).attr('id', 'ElettrodomesticiVuoto').text(Msg.home["nessunDisp"]))
};

Elettrodomestici.GestElettrodomestici = function() { 

	Elettrodomestici.indexElettrodomestico = 0;
	var divElettro = $("#Elettrodomestici");

	if (divElettro.length == 0) {
		$("#Container").append(Elettrodomestici.htmlContent);
	} else {
		$("#Elettrodomestici").show();
	}

	$("#backDevices").hide();
	$("#nextDevices").hide();

	if (Elettrodomestici.locazioni.length == 0) {
		//Eseguo il codice solo all'inizio
		if (InterfaceEnergyHome.mode > 0) {
			try {
				InterfaceEnergyHome.objService.getLocations(Elettrodomestici.DatiLocazioni);
			} catch (err) {
				InterfaceEnergyHome.GestErrorEH("GestElettrodomestici", err);
			}
		} else {
			Elettrodomestici.DatiLocazioni(ListaLocazioni, null);
		}
	} else {
		//Elettrodomestici.GetElettrodomestici();
		Elettrodomestici.GetDatiPotenzaElettr();
	}
}

Elettrodomestici.DatiLocazioni = function(result, err) {

	if (err != null) {
		retVal = null;
		// in realta' puo' essere richiamato anche da configuratore
		InterfaceEnergyHome.GestErrorEH("DatiLocazioni", err);
	} else {
		// ritorna dizionario pid-nome
		if (result != null) {
			for (i = 0; i < result.length; i++) {
				pid = result[i][InterfaceEnergyHome.ATTR_LOCATION_PID];
				name = result[i][InterfaceEnergyHome.ATTR_LOCATION_NAME];
				Elettrodomestici.locazioni[pid] = name;
			}
		}
	}
	// Quando finisco di eseguire la callback di successo per 
	// InterfaceEnergyHome.objService.getLocations proseguo
	Elettrodomestici.GetDatiPotenzaElettr();
}

/*
 * Metodo che si occupa di eseguire la chiamata AJAX per prelevare la potenza attuale di consumo
 */
Elettrodomestici.GetDatiPotenzaElettr = function() {
	if (Main.env == 0) console.log('Elettrodomestici.js', 'GetDatiPotenzaElettr', 'Entro!');

	// non tolgo togliere messaggio errore da piattaforma
	if (InterfaceEnergyHome.visError != InterfaceEnergyHome.ERR_CONN_SERVER){
		Main.ResetError();
	}
	if (InterfaceEnergyHome.mode > 0) {
		try {
			//InterfaceEnergyHome.objService.getAttribute(Elettrodomestici.GetElettrodomestici, InterfaceEnergyHome.POTENZA_TOTALE);
			InterfaceEnergyHome.objService.getAppliancesConfigurationsDemo(Elettrodomestici.GetElettrodomestici);
		} catch (err) {
			if (Main.env == 0) console.log('exception in Elettrodomestici.js - in Elettrodomestici.GetDatiPotenzaElettr method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetDatiPotenzaElettr", err);
		}
	} else {
		// per test
		if (Elettrodomestici.potenzaAttuale.value == null){
			Elettrodomestici.potenzaAttuale.value = 0;
		}
		Elettrodomestici.potenzaAttuale.value += 200;
		if (Elettrodomestici.potenzaAttuale.value > (Define.home["tipoContatore"][Main.contatore] + 2000)){
			Elettrodomestici.potenzaAttuale.value = 0;
		}
		
		Elettrodomestici.GetElettrodomestici(Elettrodomestici.potenzaAttuale, null);
	}
	if (Main.env == 0) console.log('Elettrodomestici.js', 'GetDatiPotenzaElettr', 'Esco!');
}

Elettrodomestici.GetElettrodomestici = function(result, err) {

	if (err != null){
		if (Main.env == 0) console.log('exception in FotoVoltaico.js - in Elettrodomestici.GetElettrodomestici method: ', err);
		InterfaceEnergyHome.GestErrorEH("GetElettrodomestici", err);
	} else if (result != null){
		$.each(result.list,function(indice, elettrodom) {
			if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.SMARTINFO_APP_TYPE) {
				if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_CATEGORY] == "12") {
					device_value = elettrodom["map"].device_value;
					if (device_value != undefined) {
						Elettrodomestici.potenzaAttuale.value = device_value.value.value;
					}
				}
				if (Main.env == 0)
					console.log('COSTICONSUMI3', 'SmartInfo - '+CostiConsumi.SmartInfo);
			}
		});
	} else {
		Elettrodomestici.potenzaAttuale.value = null;
	}
	
	if (Elettrodomestici.startTime != null){

		Elettrodomestici.startTime = null;
		Elettrodomestici.requestCB = null;
		hideSpinnerElettro();
	} else  if (Elettrodomestici.requestCB == null){
		showSpinnerElettro();
		if (InterfaceEnergyHome.mode > 0) {
			try {
				Elettrodomestici.requestCB = InterfaceEnergyHome.objService.getAppliancesConfigurationsDemo(Elettrodomestici.DatiElettrodomestici);
			} catch (err) {
				InterfaceEnergyHome.GestErrorEH("Dispositivi", err);
			}
		} else {
			// per test
			if (indLista == 0) {
				val = ListaElettr1;
				indLista = 1;
			} else {
				val = ListaElettr1;
				indLista = 0;
			}

			Elettrodomestici.requestCB = -1;
			Elettrodomestici.DatiElettrodomestici(val, null);
		}
	}
}

Elettrodomestici.DatiElettrodomestici = function(result, err, req) {
	if (Elettrodomestici.requestCB == result.id){
		Elettrodomestici.requestCB = null;
		hideSpinnerElettro();
		if (err != null)
			InterfaceEnergyHome.GestErrorEH("DatiElettrodomestici", err);
		if ((err == null) && (result != null)) {
			$.each(result.list,
					function(indice, elettrodom) {
						if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.SMARTINFO_APP_TYPE) {
							//Analizzo lo SmartInfo
							if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE] == undefined) {
								elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE] = {};
								elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value = {value : 0};
							} else {
								var val = parseFloat(elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value);
								elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value = val;
							}
							Elettrodomestici.SmartInfo = elettrodom["map"];
						} else if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WHITEGOOD_APP_TYPE){
							//Analizzo la lavatrice whitegood (per adesso stesso codice di uno smart plug)
							if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE] == undefined){
								elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value = 0;
							} else {
								var val = parseFloat(elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value);
								elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value = val;
							}
							
							Elettrodomestici.lista1ricevuta[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
						} else {
							//Analizzo gli altri elettrodomestici quindi elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.SMARTPLUG_APP_TYPE
							if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE] == undefined){
								elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value = 0;
							} else {
								var val = parseFloat(elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value);
								elettrodom["map"][InterfaceEnergyHome.ATTR_APP_VALUE].value.value = val;
							}
							Elettrodomestici.lista1ricevuta[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
						}
					});
		}
	
		Elettrodomestici.listaElettrodomestici = [];
		Elettrodomestici.consumoTotale = 0;
		Elettrodomestici.altroConsumo = 0;
	
		$.each(Elettrodomestici.lista1ricevuta,
					function(index, elemento) {
	
						var Elettrodom = {};
						Elettrodom["nome"] = elemento[InterfaceEnergyHome.ATTR_APP_NAME];
						if (Elettrodom["consumo"] = elemento[InterfaceEnergyHome.ATTR_APP_VALUE]["value"]["value"] == "NaN") {
							Elettrodom["consumo"] = Msg.NA;
						} else {
							Elettrodom["consumo"] = elemento[InterfaceEnergyHome.ATTR_APP_VALUE]["value"]["value"];
						}
						Elettrodom["location"] = elemento[InterfaceEnergyHome.ATTR_APP_LOCATION];

						Elettrodom["stato"] = elemento[InterfaceEnergyHome.ATTR_APP_STATE];
						Elettrodom["connessione"] = elemento[InterfaceEnergyHome.ATTR_APP_AVAIL];
						
						if (elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WHITEGOOD_APP_TYPE){
							Elettrodom["type"] = 'whitegood';
						} else {
							Elettrodom["type"] = 'smartplug';
						}
	
						Elettrodomestici.consumoTotale += Elettrodom["consumo"];
	
						var str = elemento[InterfaceEnergyHome.ATTR_APP_ICON];
	
						Elettrodom["icona"] = str.replace(".png", "");
						Elettrodomestici.listaElettrodomestici.push(Elettrodom);
					});
	
		if (Elettrodomestici.SmartInfo != null) {
			var Elettrodom = {};
			Elettrodom["icona"] = "plug";
			Elettrodom["nome"] = "Altri consumi";
			
			Elettrodomestici.altroConsumo = Elettrodomestici.potenzaAttuale.value - Elettrodomestici.consumoTotale;
			Elettrodomestici.altroConsumo = Elettrodomestici.altroConsumo > 0 ? Elettrodomestici.altroConsumo : 0;
			Elettrodom["consumo"] = Elettrodomestici.altroConsumo;
			Elettrodom["location"] = 10;
			Elettrodom["stato"] = 1;
			Elettrodom["connessione"] = 2;
			Elettrodomestici.listaElettrodomestici.push(Elettrodom);
		}
	
		Elettrodomestici.numDispositivi = Elettrodomestici.listaElettrodomestici.length;
		
		$("#TotaleDisp").html(Msg.home['titleTotDisp'] + ': ' + Math.floor(Elettrodomestici.consumoTotale) + " W");
		$("#AltriConsumi").html(Msg.home['titleAltriDisp'] + ': ' + Math.floor(Elettrodomestici.altroConsumo) + " W");
		$("#TotaleConsumi").html(Msg.home['titleTotCons'] + ': ' + Math.floor(Elettrodomestici.potenzaAttuale.value) + " W");
	
		if (Elettrodomestici.timerDispo == null) {
			Elettrodomestici.InizializzaPagina();
			Elettrodomestici.timerDispo = setInterval("Elettrodomestici.GetDatiPotenzaElettr()", Elettrodomestici.TIMER_UPDATE_ELETTR);
		} else {
			Elettrodomestici.VisualizzaElettro(Elettrodomestici.indexElettrodomestico);
		}
	}
}

Elettrodomestici.InizializzaPagina = function() {

	$("#ElettrodomesticiVuoto").css("display", "none");
	$("#RigaElettrodomestici,.RigaDati").children().remove();
	
	//Non prendo in considerazione la voce "Altri Dispositivi" nella creazione della riga degli Elettrodomestici
	if (Elettrodomestici.SmartInfo != null) {
		Elettrodomestici.numDispositivi--;
	}

	/* Si organizza la pagina in base al numero di dispoditivi presenti */
	switch (Elettrodomestici.numDispositivi) {
		case 0: {
			var altezza = $("#ElettrodomesticiVuoto").height();
			$("#ElettrodomesticiVuoto").css("line-height", altezza + "px").css("display", "block");
			$("#backDevices,#nextDevices").hide();
			break;
		}
		case 1: {
			$("#RigaElettrodomestici,.RigaDati").css("margin-left", "37.5%");
			$("#backDevices,#nextDevices").hide();
			break;
		}
		case 2: {
			$("#RigaElettrodomestici,.RigaDati").css("margin-left", "25%");
			$("#backDevices,#nextDevices").hide();
			break;
		}
		case 3: {
			$("#RigaElettrodomestici,.RigaDati").css("margin-left", "12.5%");
			$("#backDevices,#nextDevices").hide();
			break;
		}
		case 4: {
			$("#RigaElettrodomestici,.RigaDati").css("margin-left", "0px");
			$("#backDevices,#nextDevices").hide();
			break;
		}
		default: {
			$("#RigaElettrodomestici,.RigaDati").css("margin-left", "0px");
			$("#backDevices").button({
										text : false,
										icons : {primary : "ui-icon-seek-prev"}})
							 .click(function() {
								 		$("#nextDevices").show();
								 		Elettrodomestici.indexElettrodomestico -= Elettrodomestici.numMaxDispoSchermo;
								 		if (Elettrodomestici.indexElettrodomestico <= 0) {
								 			$("#backDevices").hide();
								 			Elettrodomestici.indexElettrodomestico = 0;
								 		}
								 		Elettrodomestici.VisualizzaElettro(Elettrodomestici.indexElettrodomestico);});
			$("#nextDevices").button({
										text : false,
										icons : {primary : "ui-icon-seek-next"}})
							 .click(function() {
								 		$("#ConsumoRow .DatiElettrodomestico").hide();
								 		$("#LocRow .DatiElettrodomestico").hide();
								 		$("#RigaElettrodomestici .DatiElettrodomestico").hide();
								 		$("#backDevices").show();
								 		Elettrodomestici.indexElettrodomestico += Elettrodomestici.numMaxDispoSchermo;
								 		if (Elettrodomestici.indexElettrodomestico >= (Elettrodomestici.numDispositivi - Elettrodomestici.numMaxDispoSchermo)) {
								 			$("#nextDevices").hide();
								 		}
								 		Elettrodomestici.VisualizzaElettro(Elettrodomestici.indexElettrodomestico);});
			$("#backDevices,#nextDevices").show();
			if ($.browser.msie)
				$("#nextDevices,#backDevices").corner("3px");
			if (Elettrodomestici.indexElettrodomestico == 0)
				$("#backDevices").hide();
			break;
		}
	} // end switch

	$("#RigaElettrodomestici,.RigaDati").css("margin-left", "auto");
	$("#RigaElettrodomestici,.RigaDati").css("margin-right", "auto");

	var altezzaDivDati = $("#ConsumoRow ").height();
	Elettrodomestici.numDispoSchermo = (Elettrodomestici.numDispositivi > Elettrodomestici.numMaxDispoSchermo) ? Elettrodomestici.numMaxDispoSchermo : Elettrodomestici.numDispositivi;
	var dimWidthDatiElettrodomestico = (Elettrodomestici.numDispoSchermo < 6) ? '19.8px': '16.5px';
	$(".DatiElettrodomestico").width(dimWidthDatiElettrodomestico);
	for (i = 0; i < Elettrodomestici.numDispoSchermo; i++) {

		var stato = $(document.createElement('div')).attr('class', 'StatoElettrodomestico');
		var img = $(document.createElement('img'));
		var label = $(document.createElement('div')).attr('class', 'NomeElettrodomestico');
		var cella = $(document.createElement('div')).attr('class', 'DatiElettrodomestico').append(img).append(label).append(stato);
		
		$("#RigaElettrodomestici").append(cella);
		
		$("#ConsumoRow").append($(document.createElement('div')).attr('class', 'DatiElettrodomestico')).css("line-height", altezzaDivDati + "px");
		//$("#LocRow").append($(document.createElement('div')).attr('class', 'DatiElettrodomestico')).css("line-height", altezzaDivDati + "px");

	}
	//var consumiTotale = Elettrodomestici.altroConsumo + Elettrodomestici.consumoTotale;
	$("#TotaleDisp").html(Msg.home['titleTotDisp'] + ': ' + Math.floor(Elettrodomestici.consumoTotale) + " W");
	$("#AltriConsumi").html(Msg.home['titleAltriDisp'] + ': ' + Math.floor(Elettrodomestici.altroConsumo) + " W");
	$("#TotaleConsumi").html(Msg.home['titleTotCons'] + ': ' + Math.floor(Elettrodomestici.potenzaAttuale.value) + " W");

	Elettrodomestici.VisualizzaElettro(Elettrodomestici.indexElettrodomestico);
}

/* Funzione che si occupa di riempire la pagina con i dati dei dispositivi */
Elettrodomestici.VisualizzaElettro = function(offset) {
	var numElettroPagina = 0;
	
	var connessioneElettr, consumoElettr, nomeElettr, statoElettr, typeElettr;
	var locationElettr, nomeElettrodomestico, statoElettrodomestico, consumoDatiElettrodomestico;
	var idxElettr, estensioneIcona;

	var tmpOffset = Elettrodomestici.numDispositivi - Elettrodomestici.numMaxDispoSchermo;
	if ((offset > tmpOffset) && (Elettrodomestici.numDispositivi > Elettrodomestici.numMaxDispoSchermo)){
		numElettroPagina = Elettrodomestici.numDispositivi % Elettrodomestici.numDispoSchermo;
	} else {
		numElettroPagina = Elettrodomestici.numDispoSchermo;
	}
	
	if (tmpOffset <= 0){
		//Ci sono meno dispositivi di quanti la pagina pu˜ contenerne
		$("#nextDevices").hide();
	}

	for (i = 0; i < numElettroPagina; i++) {

		idxElettr = i + offset;
		if (Elettrodomestici.listaElettrodomestici[idxElettr]["nome"] != 'Altri consumi'){
	
			nomeElettrodomestico = $("#RigaElettrodomestici .DatiElettrodomestico .NomeElettrodomestico")[i];
			statoElettrodomestico = $("#RigaElettrodomestici .DatiElettrodomestico .StatoElettrodomestico")[i];
			consumoDatiElettrodomestico = $("#ConsumoRow .DatiElettrodomestico")[i];
			
	
			$($("#RigaElettrodomestici .DatiElettrodomestico")[i]).show();
			$(consumoDatiElettrodomestico).show();
			$($("#LocRow .DatiElettrodomestico")[i]).show();
	
			connessioneElettr = Elettrodomestici.listaElettrodomestici[idxElettr]["connessione"];
			consumoElettr = Elettrodomestici.listaElettrodomestici[idxElettr]["consumo"];
			nomeElettr = Elettrodomestici.listaElettrodomestici[idxElettr]["nome"];
			statoElettr = Elettrodomestici.listaElettrodomestici[idxElettr]["stato"];
			typeElettr = Elettrodomestici.listaElettrodomestici[idxElettr]["type"];
			locationElettr = Elettrodomestici.listaElettrodomestici[idxElettr]["location"];
	
			/* Visaulizzazione nome elettrodomestico */
			$(nomeElettrodomestico).text(nomeElettr);
	
			/* Gestione label stato, contenitore consumo ed estensione icona */
			if (connessioneElettr != 2) {
				//DISCONNESSO ICONA GRIGIO
				estensioneIcona = "_disconnesso.png";
				$(consumoDatiElettrodomestico).text(" ");
				$(statoElettrodomestico).css('color', '');
				$(statoElettrodomestico).css("color", "light grey").css("style-weight", "bold").text(" ");
	
			} else if (statoElettr) {
				if (nomeElettr == 'Altri consumi'){
					if (consumoElettr > 0) {
						//Se c'� consumo visualizzo l'icona verde
						estensioneIcona = "_acceso.png";
						$(statoElettrodomestico).css('color', '').text('');
						$(consumoDatiElettrodomestico).text(consumoElettr.toFixed(1) + " W");
					} else {
						//Altrimenti lo visualizzo spento
						estensioneIcona = "_spento.png";
						$(statoElettrodomestico).css('color', '').text('');
						$(consumoDatiElettrodomestico).text(" ");
					}
				} else if (consumoElettr > 0) {
					//Se c'� consumo visualizzo l'icona verde
					estensioneIcona = "_acceso.png";
					$(statoElettrodomestico).css('color', '');
					$(statoElettrodomestico).css("color", "#9CC31C").css("style-weight", "bold").text(Msg.home.statoDisp[1]);
					$(consumoDatiElettrodomestico).text(consumoElettr.toFixed(1) + " W");
				} else {
					//Altrimenti lo visualizzo spento
					estensioneIcona = "_spento.png";
					$(statoElettrodomestico).css('color', '');
					$(statoElettrodomestico).css("color", "light grey").css("style-weight", "bold").text(Msg.home.statoDisp[0]);
					$(consumoDatiElettrodomestico).text(" ");
				}
			} else {
				if (typeElettr == 'whitegood'){
					//Se il dispositivo � una lavatrice ed � in standby
					if (consumoElettr <= 0){
						//Il device whitegood consuma 0W, quindi spento
						estensioneIcona = "_spento.png";
						$(statoElettrodomestico).css('color', '');
						$(statoElettrodomestico).css("color", "light grey").css("style-weight", "bold").text(Msg.home.statoDisp[0]);
						$(consumoDatiElettrodomestico).text(" ");
					} else if ((consumoElettr> 0) && (consumoElettr < 1)){
						//Il device consuma, ma meno di 1km, quindi spento ma stato connesso, per adesso spento
						estensioneIcona = "_spento.png";
						$(statoElettrodomestico).css('color', '');
						$(statoElettrodomestico).css("color", "light grey").css("style-weight", "bold").text(Msg.home.statoDisp[0]);
						$(consumoDatiElettrodomestico).text(" ");
						//$(consumoDatiElettrodomestico).text(consumoElettr.toFixed(1) + " W");
					} else if ((consumoElettr >= 1) && (consumoElettr <= 5)){
						//Il device consuma pi� di un 1W, ma meno di 5km, quindi standby
						//al momento lo mettiamo ON.
						estensioneIcona = "_acceso.png";
						$(statoElettrodomestico).css('color', '');
						$(statoElettrodomestico).css("color", "#9CC31C").css("style-weight", "bold").text(Msg.home.statoDisp[1]);
						//$(statoElettrodomestico).css("color", "orange").css("style-weight", "bold").text(Msg.home.statoDisp[3]);
						$(consumoDatiElettrodomestico).text(consumoElettr.toFixed(1) + " W");
					} else {
						//Il device whitegood consuma tanto, quindi sta operando
						estensioneIcona = "_acceso.png";
						$(statoElettrodomestico).css('color', '');
						$(statoElettrodomestico).css("color", "#9CC31C").css("style-weight", "bold").text(Msg.home.statoDisp[1]);
						$(consumoDatiElettrodomestico).text(consumoElettr.toFixed(1) + " W");
					}
				} else {
					estensioneIcona = "_spento.png";
					$(statoElettrodomestico).css('color', '');
					$(statoElettrodomestico).css("color", "light grey").css("style-weight", "bold").text(Msg.home.statoDisp[0]);
					$(consumoDatiElettrodomestico).text(" ");
				}
			}
	
			/* visualizzazione dell'immagine */
			var imgDiv = $("#RigaElettrodomestici .DatiElettrodomestico img")[i];
			var urlImg = "Resources/Images/Devices/" + Elettrodomestici.listaElettrodomestici[idxElettr]["icona"] + estensioneIcona;
			if ((nomeElettr == 'Altri consumi') || (typeElettr == 'whitegood')){
				$(imgDiv).attr("src", urlImg).attr('title', '');
			} else {
				if (connessioneElettr == 2){
					if (statoElettr == 1){
						$(imgDiv).attr("src", urlImg).attr('title', 'SmartPlug ON');
					} else {
						$(imgDiv).attr("src", urlImg).attr('title', 'SmartPlug OFF');
					}
				} else {
					$(imgDiv).attr("src", urlImg).attr('title', 'SmartPlug Disconnesso');
				}
			}
	
			/* Visualizzazione location */
			//var loc = Elettrodomestici.locazioni[locationElettr];
			//loc = Lang.Convert(loc, Msg.locazioni);
	
			//$($("#LocRow .DatiElettrodomestico")[i]).text(loc);
		}

	}

}

Elettrodomestici.ExitElettrodomestici = function() {

	Main.ResetError();
	if (Elettrodomestici.timerDispo != null) {
		clearInterval(Elettrodomestici.timerDispo);
		Elettrodomestici.timerDispo = null;
	}
	InterfaceEnergyHome.Abort();
	Elettrodomestici.listaElettrodomestici = [];
	$("#Elettrodomestici").remove();
}
