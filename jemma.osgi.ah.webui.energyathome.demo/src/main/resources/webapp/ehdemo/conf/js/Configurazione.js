var MIN_YEAR = 60 * 24 * 365;

var Configurazione = {
	MODULE : "Configurazione",
	modAttuale : 0,
	UTENTE : 1,
	ELETTRODOMESTICI : 2,
	timerStato : null,
	TIMOUT_STATO : 4000,
	CONN_OK : 0,
	CONN_SERVER_ERR : 1,
	CONN_AG_ERR : 2,
	DEFAULT_ICON : Define.config["defaultIcona"],
	statoImages : [ Define.config["statoVerde"], Define.config["statoGiallo"], Define.config["statoRosso"] ],
	TIMEOUT_INQUIRY : 180, // (sercondi) parametro timeout inquiryDevices
	INQUIRY_GUARD : 20, // (secondi) parametro timeout dopo il quale si smette
	// di fare polling sui nuovi dispositivi deve essere
	// maggiore di TIMEOUT_INQUIRY
	INTERVALLO_CONTROLLO : 10000, // (ms) intervallo con il quale controllo se
	// sono stati rilevati dei dispositivi
	TIMER_UPDATE_ELETTR : 4000, // intervallo con cui si rileggono i dispositivi
	MAX_LAST_UPDATE_DELAY : 1200, // massimo scarto del last update time
	// rispetto all'ora attuale in sec (20 min)
	incrControllo : 0, // quante volte ho controllato, dopo MAX_CONTROLLO
	// segnalo errore
	timerInquiry : null,
	timerVis : null,
	infoDisp : [],
	numDisp : 0,
	indSel : 0,
	nuovoDispositivo : null, // nuovo dispositivo trovato
	idUtente : null,
	limit : null,
	limitProduction : null,
	autenticazione : null,
	lastUpdate : null,
	popUp : null,
	categorie : null,
	optionsCategorie : null,
	categorieConf : null,
	locazioni : null,
	optionslocazioni : null,
	icone : null,
	hDivIcona : null,
	wDivIcona : null,
	isFirstTime : true,
	/*
	 * categorieGroup: {"ah.ep.zigbee.SmartPlug": [1, 2, 3, 4, 5, 6, 7, 8, 9,
	 * 10, 11, 13, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
	 * 31, 32, 33], "ah.ep.zigbee.Generic": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
	 * 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
	 * 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44],
	 * "ah.ep.zigbee.MeteringDevice": [12, 14, 15], "ah.ep.zigbee.WhiteGoods":
	 * [37, 38, 39], "ah.ep.zigbee.ColorLight": [43, 34, 35],
	 * "ah.ep.zigbee.OnOffLight": [43, 34, 35], "ah.ep.zigbee.DimmableLight":
	 * [42], "ah.ep.zigbee.LightSensor": [43, 34, 35], "ah.ep.zigbee.DoorLock":
	 * [40], "ah.ep.zigbee.WindowCovering": [44],
	 * "ah.ep.zigbee.WindowCoveringController": [45], "ah.ep.zigbee.Thermostat":
	 * [36, 41], "ah.ep.zigbee.TemperatureSensor": [36, 41]},
	 */
	categorieGroup : {
		"ah.ep.zigbee.SmartPlug" : [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 16,
				17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
				33 ],
		"ah.ep.zigbee.Generic" : [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
				14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
				30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44,46 ],
		"ah.ep.zigbee.MeteringDevice" : [ 12, 14, 15 ],
		"ah.ep.zigbee.WhiteGoods" : [ 37, 38, 39, 46 ],
		"ah.ep.zigbee.ColorLight" : [ 43, 34, 35 ],
		"ah.ep.zigbee.OnOffLight" : [ 43, 34, 35 ],
		"ah.ep.zigbee.DimmableLight" : [ 42 ],
		"ah.ep.zigbee.LightSensor" : [ 43, 34, 35 ],
		"ah.ep.zigbee.DoorLock" : [ 40 ],
		"ah.ep.zigbee.WindowCovering" : [ 44 ],
		"ah.ep.zigbee.WindowCoveringController" : [ 45 ],
		"ah.ep.zigbee.Thermostat" : [ 36, 41 ],
		"Generic Thermostat" : [ 36, 41 ], // TO FIX!!!!,
		"URMET-Temperature & Humidity" : [ 36, 41 ], // TO FIX!!!!,
		"ah.ep.zigbee.TemperatureSensor" : [ 36, 41 ],
		"org.energy_home.jemma.ah.zigbee.bitronhome.remotecontrol": [ ]

	},
	calcNumIcone : [ [ 0, 0 ], [ 1, 1 ], [ 1, 2 ], [ 2, 2 ], [ 2, 2 ],
			[ 2, 3 ], [ 2, 3 ], [ 3, 3 ], [ 3, 3 ], [ 3, 3 ], [ 4, 3 ],
			[ 4, 3 ], [ 4, 3 ], [ 5, 3 ], [ 5, 3 ], [ 5, 3 ], [ 6, 3 ],
			[ 6, 3 ], [ 6, 3 ] ], // num righe, colonne

	htmlUtente : "<div id='ConfPopUp' style='display:none'></div>"
			+ "<div id='StatoConnessione'>" + "		<div id='TitoloStato'>"
			+ Msg.config["titoloStato"]
			+ "</div>"
			+ "		<img id='ImgStato' src=''>"
			+ "		<div id='Autenticazione'></div>"
			+ "		<div id='LastUpdate'></div>"
			+ "</div>"
			+ "<div id='TitoloConfUtente'>"
			+ Msg.config["titoloUtente"]
			+ "</div>"
			+ "<div id='DatiUtente' class='Form'>"
			+ "		<div>"
			+ "			<label>"
			+ Msg.config["txtUtente"]
			+ ": </label>"
			+ "			<input type='text' id='NomeUtente'>"
			+ "			<div style='clear:both'></div>"
			+ "		</div>"
			+ "		<div>"
			+ "			<label>"
			+ Msg.config["maxPotenza"]
			+ ": </label>"
			+ "			<select id='MaxPower'>"
			+ "				<option value='3000'>3 kW (fondo scala 4kW)</option>"
			+ "				<option value='4500'>4.5 kW (fondo scala 6kW)</option>"
			+ "				<option value='6000'>6 kW (fondo scala 8kW)</option>"
			+ "			</select>"
			+ "			<div style='clear:both'></div>"
			+ "		</div>"
			+ "		<div>"
			+ "			<label>"
			+ Msg.config["maxProduzione"]
			+ ": </label>"
			+ "			<select id='MaxProduction'>"
			+ "			</select>"
			+ "			<div style='clear:both'></div>"
			+ "		</div>"
			+ "</div>"
			+ "<input type='button' class='ButtonConf' id='ButtonSalvaUtente' name='SalvaUtente' value='"
			+ Msg.config["Salva"]
			+ "'>"
			+ "<input type='button' class='ButtonConf' id='ButtonAnnullaUtente' name='AnnullaUtente' value='"
			+ Msg.config["Annulla"] + "'>",

	htmlConfElettr : "<div id='StatoConnessione'>" + "		<div id='TitoloStato'>"
			+ Msg.config["titoloStato"] + "</div>"
			+ "		<img id='ImgStato' src=''>"
			+ "		<div id='Autenticazione'></div>"
			+ "		<div id='LastUpdate'></div>" + "</div>"
			+ "<div id='TitoloConfElettr'>" + Msg.config["titoloDisp"]
			+ "</div>" + "<div id='ConfPopUp' style='display:none'></div>"
			+ "<div id='ConfElettrDiv'></div>",

	htmlConfIns : "<input type='button' class='ButtonConf' id='ButtonAggiungiElettr' name='AggiungiElettr' value='"
			+ Msg.config["buttonAggiungi"]
			+ "'>"
			+ "<div id='ElencoElettr'></div>",

	htmlConfDati : "<div id='ScegliIconaDiv'></div>"
			+ "<div id='DatiPlug' class='Form'>" + "		<div>" + "			<label>"
			+ Msg.config["txtNome"]
			+ "</label>"
			+ "			<input type='text' id='NomeElettr'>"
			+ "			<div style='clear:both'></div>"
			+ "		</div>"
			+ "		<div>"
			+ "			<label>"
			+ Msg.config["txtCategoria"]
			+ "</label>"
			+ "			<select id='CategoriaElettr'></select>"
			+ "			<div style='clear:both'></div>"
			+ "		</div>"
			+ "		<div>"
			+ "			<label>"
			+ Msg.config["txtLocazione"]
			+ "</label>"
			+ "			<select id='LocazioneElettr'></select>"
			+ " 		<div style='clear:both'></div>"
			+ "		</div>"
			+ "		<div>"
			+ "			<label>"
			+ Msg.config["txtIcona"]
			+ "</label>"
			+ "			<img id='IconaElettr' src=''><div style='clear:both'></div>"
			// + " <input type='button' class='ButtonConfSmall'
			// style='vertical-align:top;' id='ButtonScegliIcona' value='" +
			// Msg.config["Modifica"] + "'>"
			+ "		</div>"
			+ "		<div>"
			+ "			<label>"
			+ Msg.config["txtTipo"]
			+ "</label>"
			+ "			<span id='TextTipoElettr'></span>"
			+ "			<div style='clear:both'></div>"
			+ "		</div>"
			+ "		<div>"
			+ "			<label>"
			+ Msg.config["txtId"]
			+ "</label>"
			+ "			<span id='TextIdElettr'></span>"
			+ "			<div style='clear:both'></div>"
			+ "		</div>"
			+ "		<input type='button' class='ButtonConf' id='ButtonSalvaDati' name='SalvaDati' value='"
			+ Msg.config["Salva"]
			+ "'>"
			+ "		<input type='button' class='ButtonConf' id='ButtonAnnullaDati' name='AnnullaDati' value='"
			+ Msg.config["Annulla"] + "'>" + "</div>"
};

Configurazione.HandleError = function(e) {
	if (e.code == JSONRpcClient.Exception.CODE_REMOTE_EXCEPTION) {
		// this is a remote exception
		if (e.msg == "hap service not bound") {
			$("#ImgStato").attr("src", Configurazione.statoImages[2]);
			return;
		}
	} else if (e.code == JSONRpcClient.Exception.CODE_ERR_NOMETHOD) {
		// capita quando l'interfaccia remota e' scomparsa
		msgCode = "errorAG";
		showSpinner();
		InterfaceEnergyHome.reconnect();
	} else if (e.code == 0) {
		// remote down
		showSpinner();
		InterfaceEnergyHome.reconnect();
	} else {
		msgCode = "errorAG";
	}
}

Configurazione.HandleServiceEvent = function(status) {
	if (status == InterfaceEnergyHome.CONNECTED) {
		hideSpinner();
		Configurazione.StartPolling();
	} else if (status == InterfaceEnergyHome.DISCONNECTED) {
		showSpinner();
		Configurazione.cancelTimeouts();
	}
}

Configurazione.cancelTimeouts = function() {
	if (Configurazione.timerStato != null) {
		clearInterval(Configurazione.timerStato);
		Configurazione.timerStato = null;
	}
	if (Configurazione.timerVis != null) {
		clearTimeout(Configurazione.timerVis);
		Configurazione.timerVis = null;
	}
	if (Configurazione.timerInquiry != null) {
		clearTimeout(Configurazione.timerInquiry);
		Configurazione.timerInquiry = null;
	}
}

Configurazione.ExitUtente = function() {
	// console.log(90, Configurazione.MODULE, "ExitUtente ");
}

Configurazione.AnnullaUtente = function() {
	// console.log(80, Configurazione.MODULE, "AnnullaUtente ");
}

Configurazione.getPowerLimitCb = function(attributeValue, e) {
	hideSpinner();

	if (e != null) {
		Configurazione.HandleError(e);
		return;
	}

	if (attributeValue != null) {
		var limit = attributeValue.value;
		$("#MaxPower").val(limit);
	}

	InterfaceEnergyHome.getHapConnectionId(Configurazione.IdUtenteCb);
}

Configurazione.IdUtenteCb = function(userId, e) {
	hideSpinner();

	if (e != null) {
		Configurazione.HandleError(err);
		return;
	}

	if (userId == null) {
		// deve essere inserito un nome utente prima di accedere alla
		// configurazione degli elettrodomestici
		$("#el0Content1").addClass("PopUpInvisible");
		// console.log(80, ConfMain.MODULE, "Occorre inserire un Id Utente");
	} else {
		// e' gia' stato impostato un id se ho appena rinfrescato la pagina vado
		// in Dispositivi
		if (Configurazione.isFirstTime) {
			Configurazione.isFirstTime = false;
			Configurazione.idUtente = userId;
			Menu.OnClickContentMenu(0, 1);
		}
	}
}

Configurazione.setHapConnectionIdCb = function(result, e) {
	hideSpinner();
	if (e == null) {
		// console.log(80, Configurazione.MODULE, "Utente inserito
		// correttamente");
		// abilito configurazione simulando il click del mouse sul bottone
		// 'Dispositivi'
		Menu.OnClickContentMenu(0, 1);

	} else {
		// console.log(80, Configurazione.MODULE, "Id utente errato");
		$.dialog(Msg.config["errorIdUtente"], {
			'buttons' : [ Msg.config["Chiudi"] ]
		});
	}
}

Configurazione.setAttributeCbPV = function(result, e) {
	if (e != null) {
		// KO!
		InterfaceEnergyHome.setAttribute(Configurazione.setAttributeCb,
				"PeakProducedPower", 0);
	} else {
		// ok registro i cookie
		InterfaceEnergyHome.setAttribute(Configurazione.setAttributeCb,
				"PeakProducedPower", Configurazione.limitProduction);
	}
}

Configurazione.setAttributeCb = function(result, e) {
	if (e != null) {
		// KO!
	}

	InterfaceEnergyHome.setHapConnectionId(Configurazione.setHapConnectionIdCb,
			Configurazione.idUtente);
}

Configurazione.SalvaUtente = function() {
	Configurazione.idUtente = $("#NomeUtente").val();
	Configurazione.limit = $("#MaxPower").val();
	Configurazione.limitProduction = $("#MaxProduction").val();

	// elimina spazi
	Configurazione.idUtente = Configurazione.idUtente.replace(/ /gi, "");
	$("#NomeUtente").val(Configurazione.idUtente);
	// console.log(80, Configurazione.MODULE, "SalvaUtente id = " +
	// Configurazione.idUtente);
	showSpinner();

	InterfaceEnergyHome.setAttribute(Configurazione.setAttributeCbPV,
			"InstantaneousPowerLimit", Configurazione.limit);
}

Configurazione.GestUtente = function() {
	// console.log(90, Configurazione.MODULE, "GestUtente ");
	// se sono gia' in configurazione utente non faccio niente
	if (Configurazione.modAttuale == Configurazione.UTENTE)
		return;

	Configurazione.modAttuale = Configurazione.UTENTE;
	$("#Content").html(Configurazione.htmlUtente); // Configurazione.htmlConfElettr

	InterfaceEnergyHome.getAttribute(Configurazione.GestConfigurazione,
			"PeakProducedPower");
}

Configurazione.GestConfigurazione = function(result, e) {

	if (e != null) {
		// KO!
	}

	var maxPowerProductionCookie = result.value;

	var opt0 = "<option value='0'>0 kW (o assenza di impianto fotovoltaico)</option>";
	var opt1 = "<option value='1000'>1 kW (fondo scala 1.5 kW)</option>";
	var opt2 = "<option value='2000'>2 kW (fondo scala 2.5 kW)</option>";
	var opt3 = "<option value='3000'>3 kW (fondo scala 3.5 kW)</option>";
	var opt4 = "<option value='4000'>4 kW (fondo scala 5 kW)</option>";
	var opt5 = "<option value='5000'>5 kW (fondo scala 5.5 kW)</option>";
	var opt6 = "<option value='6000'>6 kW (fondo scala 6.5 kW)</option>";
	var opt11 = "<option value='11000'>11 kW (fondo scala 12.5 kW)</option>";
	if ((maxPowerProductionCookie == null) || (maxPowerProductionCookie == 0)) {
		opt0 = "<option value='0' selected='selected'>0 kW (o assenza di impianto fotovoltaico)</option>";
	}
	if (maxPowerProductionCookie == 1000) {
		opt1 = "<option value='1000' selected='selected'>1 kW (fondo scala 1.5 kW)</option>";
	}
	if (maxPowerProductionCookie == 2000) {
		opt2 = "<option value='2000' selected='selected'>2 kW (fondo scala 2.5 kW)</option>";
	}
	if (maxPowerProductionCookie == 3000) {
		opt3 = "<option value='3000' selected='selected'>3 kW (fondo scala 3.5 kW)</option>";
	}
	if (maxPowerProductionCookie == 4000) {
		opt4 = "<option value='4000' selected='selected'>4 kW (fondo scala 5 kW)</option>";
	}
	if (maxPowerProductionCookie == 5000) {
		opt5 = "<option value='5000' selected='selected'>5 kW (fondo scala 5.5 kW)</option>";
	}
	if (maxPowerProductionCookie == 6000) {
		opt6 = "<option value='6000' selected='selected'>6 kW (fondo scala 6.5 kW)</option>";
	}
	if (maxPowerProductionCookie == 11000) {
		opt11 = "<option value='11000' selected='selected'>11 kW (fondo scala 12.5 kW)</option>";
	}

	$('#MaxProduction').append(opt0).append(opt1).append(opt2).append(opt3)
			.append(opt4).append(opt5).append(opt6).append(opt11);

	Configurazione.refreshHapStatus();

	$("#ButtonSalvaUtente").click(Configurazione.SalvaUtente);
	$("#ButtonAnnullaUtente").click(Configurazione.AnnullaUtente);

	if (Configurazione.idUtente != null)
		$("#NomeUtente").val(Configurazione.idUtente);

	if (Configurazione.limit != null)
		$("#MaxPower").val(Configurazione.limit);

	showSpinner();

	InterfaceEnergyHome.getAttribute(Configurazione.getPowerLimitCb, "InstantaneousPowerLimit");
}

Configurazione.ExitElettrodomestici = function() {
	InterfaceEnergyHome.removeListener("service.connection", Configurazione.HandleServiceEvent);
	Configurazione.cancelTimeouts();
	// console.log(90, Configurazione.MODULE, "ExitElettrodomestici");
}

Configurazione.DatiCategorie = function(lista) {
	if (lista != null) {
		Configurazione.categorie = lista;
		Configurazione.optionsCategorie = "";
		Configurazione.categorieConf = new Array();
		for (val in lista) {
			// console.log(90, Configurazione.MODULE, "DatiLocazioni : id = " +
			// val + " nome = " + Configurazione.categorie[val]);
			// DT Qui effettuo la traduzione
			Configurazione.categorie[val] = Msg.dispositivi[val];
			Configurazione.optionsCategorie += "<option value='" + val
					+ "' class='OptionConf'>" + Configurazione.categorie[val]
					+ "</option>";
			Configurazione.categorieConf[val] = "<option value='" + val
					+ "' class='OptionConf'>" + Configurazione.categorie[val]
					+ "</option>";
		}
	}
	// presenta pagina di configurazione, diversa per configurazione o modifica
	// (visivamente uguale ma cambia da dove predne i dati e cosa fa dopo)
	if (Configurazione.nuovoDispositivo != null) {
		Configurazione.ConfiguraElettr(Configurazione.nuovoDispositivo);
	} else {
		Configurazione.ConfiguraElettr(Configurazione.infoDisp[Configurazione.indSel]);
	}
}

Configurazione.GetLocazioniCb = function(lista) {
	// console.log(80, Configurazione.MODULE, "DatiLocazioni");
	if (lista != null) {
		Configurazione.locazioni = lista;
		// Configurazione.locazioni = Lang.Convert(lista, Msg.locazioni);
		Configurazione.optionsLocazioni = "";
		for (val in lista) {
			// console.log(90, Configurazione.MODULE, "DatiLocazioni : id = " +
			// val + " nome = " + Configurazione.locazioni[val]);
			// DT Qui effettuo la traduzione
			Configurazione.locazioni[val] = Msg.stanze[val];
			Configurazione.optionsLocazioni += "<option value='" + val
					+ "' class='OptionConf'>" + Configurazione.locazioni[val]
					+ "</option>";
		}
		/**
		 * for (i = 0; i < Configurazione.locazioni.length; i++)
		 * Configurazione.optionsLocazioni += "<option value='" +
		 * Configurazione.locazioni[i][InterfaceEnergyHome.ATTR_LOCATION_PID] + "'
		 * class='OptionConf'>" +
		 * Configurazione.locazioni[i][InterfaceEnergyHome.ATTR_LOCATION_NAME] + "</option>";
		 */
	}
	Configurazione.GetListaAppliances(Configurazione.DatiElettrodomesticiCb);
}

Configurazione.GetIcone = function() {
	Configurazione.icone = new Array();
	Configurazione.icone[0] = "forno.png";
	Configurazione.icone[1] = "frigorifero.png";
	Configurazione.icone[2] = "microonde.png";
	Configurazione.icone[3] = "lampada.png";
	Configurazione.icone[4] = "pczone.png";
	Configurazione.icone[5] = "plug.png";
	Configurazione.icone[6] = "lvb1.png";
	Configurazione.icone[7] = "lvb2.png";
	Configurazione.icone[8] = "tv.png";
	Configurazione.icone[9] = "stufa.png";
	Configurazione.icone[10] = "lockdoor.png";
	Configurazione.icone[11] = "windowc.png";
	Configurazione.icone[12] = "temp.png";
	/*
	 * Configurazione.icone[10] = "production_meter.png";
	 * Configurazione.icone[11] = "secondary_meter.png";
	 * Configurazione.icone[12] = "printer.png"; 
	 * Configurazione.icone[13] = "modem-router.png"; 
	 * Configurazione.icone[14] = "decoder-recorder-player.png"; 
	 * Configurazione.icone[15] = "home_theatre-stereo.png"; 
	 * Configurazione.icone[16] = "play_station.png";
	 * Configurazione.icone[17] = "media_center.png"; 
	 * Configurazione.icone[18] = "freezer.png"; 
	 * Configurazione.icone[19] = "washer_dryer.png";
	 * Configurazione.icone[20] = "vacuum_cleaner.png"; 
	 * Configurazione.icone[21] = "hair_dryer.png"; 
	 * Configurazione.icone[22] = "bread_machine.png";
	 * Configurazione.icone[23] = "coffee_machine.png"; 
	 * Configurazione.icone[24] = "toaster.png"; 
	 * Configurazione.icone[25] = "food_robot.png";
	 * Configurazione.icone[26] = "water_purifier.png"; 
	 * Configurazione.icone[27] = "hob.png"; 
	 * Configurazione.icone[28] = "electric_heater.png";
	 * Configurazione.icone[29] = "swimming_pool_pump.png";
	 */
}

Configurazione.ScegliIcona = function() {
	var nRow, nCol, num, top, distTop, left, distLeft, dim, dimH, dimW;

	// console.log(80, Configurazione.MODULE, "Configurazione.ScegliIcona");

	$("#ScegliIconaDiv").html("");

	// TODO: da notare che non e' assolutamnte necessario che hDivIcona,
	// wDivIcona
	// siano delle variabili di Configurazione. Possono essere locali.

	// TODO: non ho ben capito a cosa serve la variable calcNumIcone!
	// Apparentemente serve a stabilire la struttura della matrice di icone
	// in base al loro numero (attenzione agli arrotondamenti). Es:
	// if (n <= 9)
	// nCol = sqr(n);
	// else
	// nCol = 3;
	//	
	// nRow = n / nCol
	// magari fare swap di righe e colonne a seconda del rapporto di forma.

	if (true) {
		num = Configurazione.icone.length;
		// metto icone nel div in base al numero di icone disponibili (max 3 per
		// riga) max totale 15
		nRow = Configurazione.calcNumIcone[num][0];
		nCol = Configurazione.calcNumIcone[num][1];
		// console.log(80, Configurazione.MODULE, "num icone = " + num + " nRow
		// = " + nRow + " nCol = " + nCol);

		dimW = Math.round($(window).width() * 0.5 / (nRow + 2));
		dimH = Math.round($(window).height() * 0.5 / (nCol + 2));

		if (dimH > dimW) {
			dim = dimW;
		} else {
			dim = dimH;
		}

		Configurazione.hDivIcona = dim * (nRow + 2);
		Configurazione.wDivIcona = dim * (nCol + 2);
		top = distTop = Math.round((Configurazione.hDivIcona - (dim * nRow))
				/ (nRow + 1));
		left = distLeft = Math.round((Configurazione.wDivIcona - (dim * nCol))
				/ (nCol + 1));

		for (i = 0; i < num;) {
			var img = jQuery('<img>', {
				'class' : 'ImgIcona',
				'id' : 'Icona_' + i,
				'src' : DefinePath.imgDispPath + Configurazione.icone[i]
			}).css({
				'position' : 'absolute',
				'top' : top,
				'left' : left,
				'width' : dim,
				'height' : dim
			});

			img.appendTo("#ScegliIconaDiv");

			i++;
			if ((i % nCol) == 0) {
				top = top + distTop + dim;
				left = distLeft;
			} else {
				left = left + distLeft + dim;
			}
		}
	}

	$("#ScegliIconaDiv").height(Configurazione.hDivIcona);
	$("#ScegliIconaDiv").width(Configurazione.wDivIcona);
	$("#ScegliIconaDiv").show();

	$("img[id*=Icona_]").click(function() {
		$("#IconaElettr").attr("src", $(this).attr("src"));
		$("#ScegliIconaDiv").hide();
	});
}

Configurazione.InseritoDispositivoCb = function(val) {
	if (val == 0) {
		// console.log(80, Configurazione.MODULE, "Dispositivo installato
		// correttamente");
		// console.log(80, Configurazione.MODULE, "Errore in StartInquiry");
		$.dialog(Msg.config["insOk"], {});
	} else {
		// console.log(80, Configurazione.MODULE, "Errore in installazione
		// dispositivo");
		$.dialog(Msg.config["errorInsDisp"], {
			'title' : 'Title',
			'buttons' : [ Msg.config["Chiudi"] ]
		});
	}

	Configurazione.VisElettrodomestici();
}

Configurazione.InserisciDispositivo = function() {
	// potrebbero essere necessari dei controlli
	Configurazione.nuovoDispositivo.map[InterfaceEnergyHome.ATTR_APP_NAME] = $("#NomeElettr").val();
	catPid = $("#CategoriaElettr option:selected").val();
	Configurazione.nuovoDispositivo.map[InterfaceEnergyHome.ATTR_APP_CATEGORY] = catPid;
	locPid = $("#LocazioneElettr option:selected").val();
	// console.log(80, Configurazione.MODULE, "InserisciDispositivo: loc = " +
	// locPid + " cat = " + catPid);
	Configurazione.nuovoDispositivo.map[InterfaceEnergyHome.ATTR_APP_LOCATION] = locPid;
	var tmp = $("#IconaElettr").attr("src");
	var j = tmp.lastIndexOf("/");
	Configurazione.nuovoDispositivo.map[InterfaceEnergyHome.ATTR_APP_ICON] = tmp.substr(j + 1);

	InterfaceEnergyHome.InstallAppliance(Configurazione.InseritoDispositivoCb, Configurazione.nuovoDispositivo);
	Configurazione.scheduleGetListaAppliances();
}

Configurazione.ModificaDispositivoCb = function() {
	// console.log(80, Configurazione.MODULE, "Dispositivo Modificato");
	Configurazione.VisElettrodomestici();
	Configurazione.scheduleGetListaAppliances();
}

Configurazione.ModificaDispositivo = function() {
	// console.log(80, Configurazione.MODULE, "ModificaDispositivo");
	Configurazione.infoDisp[Configurazione.indSel].map[InterfaceEnergyHome.ATTR_APP_NAME] = $("#NomeElettr").val();
	catPid = $("#CategoriaElettr option:selected").val();
	Configurazione.infoDisp[Configurazione.indSel].map[InterfaceEnergyHome.ATTR_APP_CATEGORY] = catPid;
	locPid = $("#LocazioneElettr option:selected").val();
	// console.log(80, Configurazione.MODULE, "InserisciDispositivo: loc = " +
	// locPid + " cat = " + catPid);
	Configurazione.infoDisp[Configurazione.indSel].map[InterfaceEnergyHome.ATTR_APP_LOCATION] = locPid;
	var tmp = $("#IconaElettr").attr("src");
	var j = tmp.lastIndexOf("/");
	Configurazione.infoDisp[Configurazione.indSel].map[InterfaceEnergyHome.ATTR_APP_ICON] = tmp.substr(j + 1);

	var newConfig = {
		'javaClass' : Configurazione.infoDisp[Configurazione.indSel].javaClass,
		'map' : {}
	};
	// newConfig['javaClass'] =
	// Configurazione.infoDisp[Configurazione.indSel].javaClass;
	// newConfig['map'] = new Array();
	newConfig.map[InterfaceEnergyHome.ATTR_APP_NAME] = Configurazione.infoDisp[Configurazione.indSel].map[InterfaceEnergyHome.ATTR_APP_NAME];
	newConfig.map[InterfaceEnergyHome.ATTR_APP_CATEGORY] = Configurazione.infoDisp[Configurazione.indSel].map[InterfaceEnergyHome.ATTR_APP_CATEGORY];
	newConfig.map[InterfaceEnergyHome.ATTR_APP_LOCATION] = Configurazione.infoDisp[Configurazione.indSel].map[InterfaceEnergyHome.ATTR_APP_LOCATION];
	newConfig.map[InterfaceEnergyHome.ATTR_APP_ICON] = Configurazione.infoDisp[Configurazione.indSel].map[InterfaceEnergyHome.ATTR_APP_ICON];
	newConfig.map["appliance.pid"] = Configurazione.infoDisp[Configurazione.indSel].map["appliance.pid"];
	newConfig.map["ah.app.type"] = Configurazione.infoDisp[Configurazione.indSel].map["ah.app.type"];

	InterfaceEnergyHome.ModificaDispositivo(Configurazione.ModificaDispositivoCb, newConfig);
}

Configurazione.EliminatoDispositivo = function(e) {
	// console.log(80, Configurazione.MODULE, "EliminatoDispositivo");
	InterfaceEnergyHome.GetListaAppliances(Configurazione.DatiElettrodomesticiCb);
}

Configurazione.EliminaElettr = function(ind) {
	$.dialog(Msg.config["confermaCanc"],{
						'buttons' : [ Msg.config["OK"], Msg.config["Annulla"] ],
						'exit' : function(value) {
							if (value == Msg.config["OK"]) {
								InterfaceEnergyHome.EliminaDispositivo(
												Configurazione.EliminatoDispositivo,
												Configurazione.infoDisp[ind].map[InterfaceEnergyHome.ATTR_APP_PID]);
							} else if (value == Msg.config["Annulla"]) {
								InterfaceEnergyHome.GetListaAppliances(Configurazione.DatiElettrodomesticiCb);
							}
						}
					});
}

// selezioni le categorie da visualizzare sulla base dell'array passato come
// parametro
/*
 * esempio di types: ah.app.eps.types: Array[2] 0: "ah.ep.common" 1:
 * "ah.ep.zigbee.MeteringDevice"
 * 
 * 
 */
Configurazione.selectCategorie = function(types) {
	var returnList = '';
	var typeAlreadyUsed = new Array();
	var catArray, indexCC = null;

/*	Old loop to filter categories according to attached driver.
	disabled to ease new devices integration/test
	for (t in types) {
		var tp = types[t];
		for (confT in Configurazione.categorieGroup) {
			if ((tp == confT) && ($.inArray(confT, typeAlreadyUsed) == -1)) {
				catArray = Configurazione.categorieGroup[confT];
				for (var iC = 0; iC <= catArray.length; iC++) {
					indexCC = catArray[iC];
					returnList += Configurazione.categorieConf[indexCC];
				}
				typeAlreadyUsed.push(confT);
			}
		}

	}*/
	if (returnList == '') {
		catArray = Configurazione.categorieGroup['ah.ep.zigbee.Generic'];
		for (var iC = 0; iC <= catArray.length; iC++) {
			indexCC = catArray[iC];
			returnList += Configurazione.categorieConf[indexCC];
		}
		
	}
	return returnList;
}

// richiamato su modifica di un dispositivo esistente o su inserimento di nuovo
// dispositivo
Configurazione.ConfiguraElettr = function(elem) {
	// console.log(80, Configurazione.MODULE, "Configuro");
	$("#ConfElettrDiv").html(Configurazione.htmlConfDati);

	// Assegna valori ai campi
	$("#NomeElettr").val(elem.map[InterfaceEnergyHome.ATTR_APP_NAME]);

	// Configurazione.categorieGroup;
	var catToView = Configurazione.selectCategorie(elem.map[InterfaceEnergyHome.ATTR_APP_EPS_TYPE]);
	// $("#CategoriaElettr").html(Configurazione.optionsCategorie);
	$("#CategoriaElettr").html(catToView);
	catPid = elem.map[InterfaceEnergyHome.ATTR_APP_CATEGORY];
	// console.log(80, Configurazione.MODULE, "ConfiguraElettr catPid = " +
	// catPid);
	if (catPid != undefined) {
		// tmp = $("#CategoriaElettr option[value=" + catPid + "]").text();
		$("#CategoriaElettr option[value=" + catPid + "]").attr("selected",
				"selected");
	}

	$("#LocazioneElettr").html(Configurazione.optionsLocazioni);
	locPid = elem.map[InterfaceEnergyHome.ATTR_APP_LOCATION];
	// console.log(80, Configurazione.MODULE, "InstallaElettr locPid = " +
	// locPid);
	if (locPid != undefined) {
		// tmp = $("#LocazioneElettr option[value=" + locPid + "]").text();
		$("#LocazioneElettr option[value=" + locPid + "]").attr("selected",
				"selected");
	}
	tmpIcon = elem.map[InterfaceEnergyHome.ATTR_APP_ICON];
	if ((tmpIcon == undefined) || (tmpIcon == null))
		tmpIcon = Configurazione.DEFAULT_ICON;
	else
		tmpIcon = DefinePath.imgDispPath + tmpIcon;
	$("#IconaElettr").attr("src", tmpIcon);

	$("#IconaElettr").width($("#IconaElettr").height());
	$("#IconaElettrSel").height($("#IconaElettr").width());
	$("#TextTipoElettr").text(elem.map[InterfaceEnergyHome.ATTR_APP_TYPE]);
	$("#TextIdElettr").text(elem.map[InterfaceEnergyHome.ATTR_APP_PID]);

	// gestione bottoni
	$("#ButtonScegliIcona").click(function() {
		Configurazione.ScegliIcona();
	});

	$("#IconaElettr").click(function() {
		Configurazione.ScegliIcona();
	});

	$("#IconaElettr").bind('resize', function(elem) {
		$("#IconaElettr").height($("#IconaElettr").width());
	});

	if (Configurazione.nuovoDispositivo !== null)
		$("#ButtonSalvaDati").click(Configurazione.InserisciDispositivo);
	else
		$("#ButtonSalvaDati").click(Configurazione.ModificaDispositivo);

	$("#ButtonAnnullaDati").click(function() {
		Configurazione.VisElettrodomestici();
		Configurazione.scheduleGetListaAppliances();
	});
}

Configurazione.TrovatoPlug = function() {
	// console.log(80, Configurazione.MODULE, "Trovato Plug");

	$.dialog(Msg.config["trovatoPlug"],{
						'buttons' : [ Msg.config["buttonConfigura"], Msg.config["Annulla"] ],
						'exit' : function(value) {
							if (value == Msg.config["buttonConfigura"]) {
								InterfaceEnergyHome.GetCategorie(
												Configurazione.DatiCategorie,
												Configurazione.nuovoDispositivo.map[InterfaceEnergyHome.ATTR_APP_PID]);
							}
						}
					});
}

// controlla se sono stati rilevati dei dispositivi
Configurazione.DatiInquiredDevicesCb = function(lista) {
	if ((lista == null) || (lista.length == 0)) {
		// non sono stati trovati nuovi smartplug, riprovo dopo un po'
		// se il tempo e' maggiore di quello messo nella start smetto di provare
		// non devo fare stop perche' la startInquiry finisce per conto suo
		Configurazione.incrControllo += Configurazione.INTERVALLO_CONTROLLO;
		if (Configurazione.incrControllo > (Configurazione.TIMEOUT_INQUIRY + Configurazione.INQUIRY_GUARD) * 1000) {
			// console.log(80, Configurazione.MODULE, "Nessun device trovato");
			Configurazione.timerInquiry = null;

			$.dialog(Msg.config["noPlug"],{
								'buttons' : [ Msg.config["OK"] ],
								'exit' : function(value) {
									InterfaceEnergyHome.GetListaAppliances(Configurazione.DatiElettrodomesticiCb);
								}
							});
		} else {
			Configurazione.timerInquiry = setTimeout(
					Configurazione.ControllaInquiry,
					Configurazione.INTERVALLO_CONTROLLO);
		}
	} else {
		// fermo la ricerca
		if (Configurazione.timerInquiry != null) {
			clearInterval(Configurazione.timerInquiry);
			Configurazione.timerInquiry = null;
		}

		Configurazione.nuovoDispositivo = lista[0]; // configuro sempre il primo
		InterfaceEnergyHome.stopInquiry(Configurazione.TrovatoPlug);
	}
}

Configurazione.ControllaInquiry = function() {
	// console.log(80, Configurazione.MODULE, "Reimposto timeout");
	InterfaceEnergyHome.GetInquiredDevices(Configurazione.DatiInquiredDevicesCb);
}

Configurazione.StopInquiryCb = function(res, e) {
	InterfaceEnergyHome.GetListaAppliances(Configurazione.DatiElettrodomesticiCb);
}

Configurazione.StartInquiryCb = function(res, e) {
	hideSpinner();
	if (e == null) {
		// la prima volta aspetto poco (al fine di rendere piu' veloce la
		// simulazione)
		Configurazione.timerInquiry = setTimeout(
				Configurazione.ControllaInquiry,
				Configurazione.INTERVALLO_CONTROLLO / 4);
		$.dialog(Msg.config["insPlug"], {
			'buttons' : [ Msg.config["Annulla"] ],
			'exit' : function(value) {
				if (Configurazione.timerInquiry != null) {
					clearTimeout(Configurazione.timerInquiry);
					Configurazione.timerInquiry = null;
				}
				InterfaceEnergyHome.stopInquiry(Configurazione.StopInquiryCb);
			}
		});
	} else {
		// console.log(80, Configurazione.MODULE, "Errore in StartInquiry");
		$.dialog(Msg.config["errorInitRic"], {
			'buttons' : [ Msg.config["Chiudi"] ]
		});
	}
}

Configurazione.AggiungiElettrodomestico = function() {
	// console.log(80, Configurazione.MODULE, "AggiungiElettrodomestico");
	if (Configurazione.timerVis != null) {
		clearTimeout(Configurazione.timerVis);
		Configurazione.timerVis = null;
	}

	Configurazione.timerVis = null;
	// chiamata ad AG che attiva la ricerca di nuovi plug
	// a intervalli va a vedere se e' stato rilevato qualche dispositivo
	Configurazione.incrControllo = 0;

	showSpinner();
	InterfaceEnergyHome.startInquiry(Configurazione.StartInquiryCb,
			Configurazione.TIMEOUT_INQUIRY);
}

Configurazione.VisElettrodomestici = function() {
	var htmlElettr;
	// console.log(20, Configurazione.MODULE, "VisElettrodomestici");
	$("#ConfElettrDiv").html(Configurazione.htmlConfIns);
	$("#ButtonAggiungiElettr").click(Configurazione.AggiungiElettrodomestico);

	htmlElettr = "";
	for (i = 0; i < Configurazione.numDisp; i++) {
		val = Msg.config["nd"];

		// imgDisp = Configurazione.infoDisp[i].icona;
		imgDisp = Configurazione.infoDisp[i].map[InterfaceEnergyHome.ATTR_APP_ICON];
		if (typeof (imgDisp) === 'undefined') {
			imgDisp = Configurazione.icone[5];
		}

		availability = Configurazione.infoDisp[i].map[InterfaceEnergyHome.ATTR_APP_AVAIL];
		device_value = Configurazione.infoDisp[i].map[InterfaceEnergyHome.ATTR_APP_VALUE];
		category_value = Configurazione.infoDisp[i].map[InterfaceEnergyHome.ATTR_APP_CATEGORY];

		if ((availability == undefined) || (availability != 2)) {
			htmlStato = "<div class='StatoDisconnesso'>"
					+ Msg.config["disconnesso"] + "</div>";
			val = "nd";
		} else {
			htmlStato = "<div class='StatoConnesso'>" + Msg.config["connesso"]
					+ "</div>";
			val = "";

			// if
			// (Configurazione.infoDisp[i].map[InterfaceEnergyHome.ATTR_APP_STATE]
			// == 1) {
			// htmlStato = "<div class='StatoOn'>" + Msg.config["statoAcceso"] +
			// "</div>";
			// } else if
			// (Configurazione.infoDisp[i].map[InterfaceEnergyHome.ATTR_APP_STATE]
			// == 0) {
			// htmlStato = "<div class='StatoOff'>" + Msg.config["statoSpento"]
			// + "</div>";
			// } else {
			// htmlStato = "<div class='StatoOff'>" + Msg.config["statoAcceso"]
			// + "</div>";
			// }

			var hDimIcon = 'height: 75px;';
			var wDimIcon = '';
			var consumo = null;
			var stato = null;
			var humidity = null;
			var zonestatus = null;
			var illuminance = null;
			var occupancy = null;
			var temperature = null;
			var lockState = null;
			var WindowState = null;
			var Measure = {};

			$.each(device_value.list, function(idx, el) {
				if (el.name == "IstantaneousDemands") {
					consumo = el.value.value;
					Measure[el.name] = {
						value : consumo.toFixed(1),
						unity : "W",
						label : "Cons.",
						name : "watt",
						type : el.name
					};
				} else if (el.name == "OnOffState") {
					stato = el.value.value;

					Measure[el.name] = {
						value : stato,
						unity : " ",
						label : "State",
						name : "",
						type : el.name
					};
				} else if (el.name == "LocalHumidity") {
					humidity = el.value.value;
					Measure[el.name] = {
						value : humidity,
						unity : "% RH",
						label : "Umidity",
						name : "Humid.",
						type : el.name
					};
				} else if (el.name == "ZoneStatus") {
					zonestatus = el.value.value;
					Measure[el.name] = {
						value : zonestatus,
						unity : " ",
						label : "State",
						name : "",
						type : el.name
					};
				} else if (el.name == "Illuminance") {
					illuminance = el.value.value;
					Measure[el.name] = {
						value : illuminance,
						unity : " ",
						label : "State",
						name : "",
						type : el.name
					};
				} else if (el.name == "Occupancy") {
					occupancy = el.value.value;
					Measure[el.name] = {
						value : occupancy,
						unity : " ",
						label : "State",
						name : "",
						type : el.name
					};
				} else if (el.name == "Temperature") {
					temperature = el.value.value;
					Measure[el.name] = {
						value : temperature.toFixed(1),
						unity : "C",
						label : "Temp.",
						name : "celsius",
						type : el.name
					};
				} else if (el.name == "LocalTemperature") {
					temperature = el.value.value;
					Measure[el.name] = {
						value : temperature.toFixed(1),
						unity : "C",
						label : "Temp.",
						name : "celsius",
						type : el.name
					};
				} else if (el.name == "LockState") {
					lockState = el.value.value;
					Measure[el.name] = {
						value : lockState,
						unity : " ",
						label : "State",
						name : "",
						type : el.name
					};
				} else if (el.name == "CurrentPositionLiftPercentage") {
					WindowState = el.value.value;
					Measure[el.name] = {
						value : WindowState,
						unity : " ",
						label : "State",
						name : "",
						type : el.name
					};
				}
			});

			for (lbl in Measure) {
				if (Measure[lbl] != "undefined") {
					if (val != "")
						val += "</br>" + Measure[lbl].label + ": "
								+ Measure[lbl].value + " " + Measure[lbl].unity;
					else
						val += Measure[lbl].label + ": " + Measure[lbl].value
								+ " " + Measure[lbl].unity;
				}
			}

			/*
			 * if (device_value != undefined) { if (typeof
			 * (device_value.value.value) == "string") { val =
			 * device_value.value.value; } else if (category_value == "40") {
			 * val = device_value.value.value; if (val == 2){ val = 'open';
			 * imgDisp = "lockdoor_acceso.png"; } else { val = 'close'; imgDisp =
			 * "lockdoor_spento.png"; } } else if (category_value == "44") { val =
			 * device_value.value.value; if (val > 0){ val = 'open'; imgDisp =
			 * "windowc_aperta.png"; } else { val = 'close'; imgDisp =
			 * "windowc_spento.png"; } wDimIcon = 'width: 43px;'; } else { val =
			 * parseFloat(device_value.value.value); val = val.toFixed(1) + "
			 * W"; } } else { val = Msg.config["nd"]; }
			 */
		}
		htmlElettr += "<div id='Elettr_"
				+ i
				+ "' class='ElettrVis'>"
				+ "		<img class='ElettrIcona' id='ElettrIcona_"
				+ i
				+ "' src='"
				+ DefinePath.imgDispPath
				+ imgDisp
				+ "' style='"
				+ hDimIcon
				+ " "
				+ wDimIcon
				+ "'>"
				+ "		<div id='StatoElettr_"
				+ i
				+ "'></div>"
				+ "		<div id='NomeElettr_"
				+ i
				+ "' class='NomeElettr' >"
				+ Configurazione.infoDisp[i].map[InterfaceEnergyHome.ATTR_APP_NAME]
				+ "		</div>"
				+ "		<div class='ValueElettr' id='ValueElettr_"
				+ i
				+ "'>"
				+ val
				+ "</div>"
				+ htmlStato
				// + "<div class='IdElettr' id='IdElettr_" + i + "'>"
				// +
				// Configurazione.infoDisp[i].map[InterfaceEnergyHome.ATTR_APP_PID]
				// + "</div>"
				+ "		<input type='button' class='ButtonConfSmall ButtonConfModifica' id='ButtonModificaElettr_"
				+ i
				+ "' value='"
				+ Msg.config["Modifica"]
				+ "'>"
				+ "		<input type='button' class='ButtonConfSmall ButtonConfElimina'  id='ButtonEliminaElettr_"
				+ i + "' value='" + Msg.config["Elimina"] + "'>" + "</div>";

	}

	// //console.log(80, Configurazione.MODULE, "VisElettrodomestici html = " +
	// htmlElettr);
	$("#ElencoElettr").html(htmlElettr);

	// devo scrivere prima l'html per poterne legegre le proprieta'
	// calcolo distanza tra i dispositivi in base al numero di dispositivi
	wT = $("#ElencoElettr").width();
	wD = $(".ElettrVis").width();
	dist = Math.round((wT - (wD * (Configurazione.numDisp + 1)))
			/ (Configurazione.numDisp + 1));
	dist = 30;
	left = dist + wD / 2;
	// wIcona = $(".ElettrIcona").width();
	// console.log(80, Configurazione.MODULE, "VisElettrodomestici wT = " + wT +
	// " wD = " + wD + " dist = " + dist);

	for (i = 0; i < Configurazione.numDisp; i++) {
		$('ElettrIcona_' + i).width($('ElettrIcona_' + i).height());
		// coords = Utils.ResizeImg("ElettrIcona_" + i, null, true, 1, 9);
		$("#Elettr_" + i).css("left", left + "px");

		$("#ButtonModificaElettr_" + i)
				.click(
						function() {
							if (Configurazione.timerVis != null) {
								clearTimeout(Configurazione.timerVis);
								Configurazione.timerVis = null;
							}
							nomeDisp = $(this).attr("id");
							indDisp = nomeDisp
									.substr(nomeDisp.indexOf("_") + 1);
							Configurazione.nuovoDispositivo = null;

							// legge le categorie per il dispositivo selezionato
							Configurazione.indSel = parseInt(indDisp);
							InterfaceEnergyHome
									.GetCategorie(
											Configurazione.DatiCategorie,
											Configurazione.infoDisp[Configurazione.indSel].map[InterfaceEnergyHome.ATTR_APP_PID]);
						});

		$("#ButtonEliminaElettr_" + i).click(function() {
			if (Configurazione.timerVis != null) {
				clearTimeout(Configurazione.timerVis);
				Configurazione.timerVis = null;
			}
			nomeDisp = $(this).attr("id");
			indDisp = nomeDisp.substr(nomeDisp.indexOf("_") + 1);
			Configurazione.EliminaElettr(parseInt(indDisp));
		});

		left = left + wD + dist;
	}
}

/*******************************************************************************
 * Legge le informazioni sui devices e le visualizza
 ******************************************************************************/
Configurazione.GetListaAppliances = function() {
	InterfaceEnergyHome
			.GetListaAppliances(Configurazione.DatiElettrodomesticiCb);
}

Configurazione.DatiElettrodomesticiCb = function(lista) {
	if (lista != null) {
		Configurazione.infoDisp = lista;
		// console.log(20, Configurazione.MODULE, "-------Trovati " +
		// lista.length + " dispositivi");
		Configurazione.numDisp = lista.length;
	} else {
		// console.log(80, Configurazione.MODULE, "Nessun device trovato");
		Configurazione.infoDisp = [];
		Configurazione.numDisp = 0;
	}

	Configurazione.VisElettrodomestici();
	Configurazione.scheduleGetListaAppliances();
}

Configurazione.scheduleGetListaAppliances = function() {

	if (Configurazione.timerVis != null) {
		clearTimeout(Configurazione.timerVis);
		Configurazione.timerVis = null;
	}

	Configurazione.timerVis = setTimeout("Configurazione.GetListaAppliances()",
			Configurazione.TIMER_UPDATE_ELETTR);
}

Configurazione.refreshHapStatus = function() {
	// controllo che il last update time sia inferiore all'ora attuale di non
	// piu' di un tempo stabilito
	now = GestDate.GetActualDate().getTime();
	if (Configurazione.lastUpdateTime != null) {
		diff = Math.abs((now - Configurazione.lastUpdateTime)) / 1000;
		// console.log(80, Configurazione.MODULE, "LastUpdate = " +
		// Configurazione.lastUpdateTime.toString() + " diff = " + diff);
	} else {
		diff = Configurazione.MAX_LAST_UPDATE_DELAY;
	}

	if (diff < Configurazione.MAX_LAST_UPDATE_DELAY)
		statoUpdate = true;
	else
		statoUpdate = false;
	// visualizzo stato
	indStato = 1;
	if (Configurazione.autenticazione == true) {
		$("#Autenticazione").html(Msg.config["autenticazioneOk"]);
		if (statoUpdate == true)
			indStato = 0;
	} else {
		$("#Autenticazione").html(Msg.config["autenticazioneFallita"]);
	}

	if ((Configurazione.lastUpdateTime != null)
			&& (Configurazione.lastUpdateTime > 0)) {
		lastDate = new Date(Configurazione.lastUpdateTime);
		m = lastDate.getMinutes();
		m = (m < 10) ? "0" + m : m;
		s = lastDate.getSeconds();
		s = (s < 10) ? "0" + s : s;
		visTime = lastDate.getDate() + "-" + (lastDate.getMonth() + 1) + "-"
				+ (lastDate.getFullYear()) + " " + lastDate.getHours() + ":"
				+ m + ":" + s;
	} else {
		visTime = "";
	}

	$("#LastUpdate").html(Msg.config["ultimoAggiornamento"] + "<br>" + visTime);
	$("#ImgStato").css("height", $("#ImgStato").css("width"));
	$("#ImgStato").attr("src", Configurazione.statoImages[indStato]);
}

Configurazione.getHapLastUploadTimeCb = function(timestamp, err) {
	if (err != null) {
		Configurazione.HandleError(err);
		return;
	}
	if (timestamp != null)
		Configurazione.lastUpdateTime = timestamp;

	Configurazione.refreshHapStatus();
	if (Configurazione.timerStato == null)
		Configurazione.timerStato = setInterval(
				"InterfaceEnergyHome.GetStatoConnessione(Configurazione.StatoConnessioneCb)",
				Configurazione.TIMOUT_STATO);
}

// leggo stato della connessione
Configurazione.StatoConnessioneCb = function(status) {
	// console.log(80, Configurazione.MODULE, "Stato Connessione = " + status);
	if (status)
		Configurazione.autenticazione = status;

	// leggo timestamp ultimo aggiornamento del server
	InterfaceEnergyHome
			.getHapLastUploadTime(Configurazione.getHapLastUploadTimeCb);
}

Configurazione.StartPolling = function() {

	// legge le locazioni e successivamente l'elenco dispositivi
	InterfaceEnergyHome.GetLocazioni(Configurazione.GetLocazioniCb);
	// legge lo stato della connessione con la piattaforma
	InterfaceEnergyHome.GetStatoConnessione(Configurazione.StatoConnessioneCb)
}

Configurazione.GestElettrodomestici = function() {
	// se sono gia' in configurazione elettrodomestici non faccio niente
	if (Configurazione.modAttuale == Configurazione.ELETTRODOMESTICI)
		return;

	InterfaceEnergyHome.addListener("service.connection",
			Configurazione.HandleServiceEvent);

	Configurazione.modAttuale = Configurazione.ELETTRODOMESTICI;

	// console.log(80, Configurazione.MODULE,
	// "Configurazione.GestElettrodomestici");
	$("#Content").html(Configurazione.htmlConfElettr);

	if (Configurazione.icone == null)
		Configurazione.GetIcone();

	Configurazione.VisElettrodomestici();
	Configurazione.refreshHapStatus();

	Configurazione.StartPolling();
}
