var Elettrodomestici = {
	MODULE : "Elettrodomestici ",
	TIMER_UPDATE_ELETTR : 20000,
	listaElettrodomestici : [],
	lista1ricevuta : {},
	SmartInfo : null,
	SmartInfoPV : null,
	locazioni : [],
	categorie: [],
	timerElettr : null,
	indexElettrodomestico : 0,
	numDispositivi : 0,
	numDispoSchermo : 0,
	numMaxDispoSchermo : 6,
	timerDispo : null,
	requestCB: null,
	nextStep: null,
	startTime: null,
	midTime: null,
	endTime: null,
	consumoTotale: 0,
	altroConsumo: 0,
	potenzaAttuale : {},
	htmlContent: $(document.createElement('div')).attr('id', 'Elettrodomestici'),
	interfaccia:null,
	numPagine:0,
	pagina:0,
	lock:false,
	perPagina:6

};

//Funzione che crea un dizionario pid->nome locazione
Elettrodomestici.GetLocations=function(callBack){
	if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)){
		try {
			InterfaceEnergyHome.objService.getLocations(function(result, err) {
					if (err != null) {
						retVal = null;
						// in realta' puo' essere richiamato anche da configuratore
						InterfaceEnergyHome.GestErrorEH("GetLocations", err);
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
					callBack();
				});
			
		} catch (err) {
			console.log("ERRORE:");
			console.log(err);
			InterfaceEnergyHome.GestErrorEH("GetLocations", err);
		}
	} else {
		for (i = 0; i < ListaLocazioni.length; i++) {
			pid = ListaLocazioni[i][InterfaceEnergyHome.ATTR_LOCATION_PID];
			name = ListaLocazioni[i][InterfaceEnergyHome.ATTR_LOCATION_NAME];
			Elettrodomestici.locazioni[pid] = name;
		}
		callBack();
	}
}

//Funzione che crea un dizionario pid->nome categoria
Elettrodomestici.GetCategories=function(callBack){
	if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)) {
		try {
			InterfaceEnergyHome.objService.getCategoriesWithPid(function(result, err) {
					
					if (err != null) {
						retVal = null;
						// in realta' puo' essere richiamato anche da configuratore
						InterfaceEnergyHome.GestErrorEH("GetCategories", err);
						console.log("Err: "+err);
					} else {
						// ritorna dizionario pid-nome
						if (result != null) {
							for (i = 0; i < result.list.length; i++) {
								pid = result.list[i]["map"]["pid"];
								name = result.list[i]["map"][InterfaceEnergyHome.ATTR_CATEGORY_NAME];
								icona = result.list[i]["map"]["icon"];
								
								Elettrodomestici.categorie[i]={"name":name ,"icon": icona, "pid": pid};
							}
						}
						
					}
					callBack();
				});
			
		} catch (err) {
			console.log("ERRORE:");
			console.log(err);
			InterfaceEnergyHome.GestErrorEH("GetCategories", err);
		}
	} else {
		callBack();
	}
}

Elettrodomestici.getCategoryIndex=function(name){
	
	for (var cat in Elettrodomestici.categorie) {
		if (Elettrodomestici.categorie[cat].name==name) {
			return cat;
		}
	}
	return -1;
}

//Funzione alternativa per la lista di elettrodomestici
Elettrodomestici.GetDevicesInfos=function(callBack){
	if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)) {
		try {
			Elettrodomestici.requestCB = InterfaceEnergyHome.objService.getAppliancesConfigurationsDemo(function(result, err, req) {   //getInfosDemo(function(result, err, req) {
				if (Elettrodomestici.requestCB == result.id){
					Elettrodomestici.requestCB = null;
					hideSpinnerElettro();
					if (err != null){
						InterfaceEnergyHome.GestErrorEH("DatiElettrodomestici", err);
						console.log("err:"+err);
					}
					if ((err == null) && (result != null)) {
						Elettrodomestici.lista1ricevuta = {};
						$.each(result.list,
								function(indice, elettrodom) {
									if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.SMARTINFO_APP_TYPE) {
										//Analizzo lo SmartInfo
										if (elettrodom["map"]["category"]["name"] == "Meter"){
											Elettrodomestici.SmartInfo = elettrodom["map"];
										}else{
											//it may be the PV meter... skip it
											Elettrodomestici.SmartInfoPV = elettrodom["map"];
										}
									} else if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WHITEGOOD_APP_TYPE){
										
										Elettrodomestici.lista1ricevuta[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
									} else if ((elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.TEMPERATURE_SENSOR_APP_TYPE) || (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.TEMPERATURE_SENSOR_APP_TYPE_2)){
										Elettrodomestici.lista1ricevuta[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
									} else if ((elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.THERMOSTAT_SENSOR_APP_TYPE) || (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.THERMOSTAT_SENSOR_APP_TYPE_2)){
										Elettrodomestici.lista1ricevuta[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
									} else if ((elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.LOCKDOOR_APP_TYPE) || (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.LOCKDOOR_APP_TYPE_2)){
										Elettrodomestici.lista1ricevuta[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
									} else if ((elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WINDOWCOVERING_APP_TYPE) || (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WINDOWCOVERING_APP_TYPE_2)){
										Elettrodomestici.lista1ricevuta[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
									} else {
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
									Elettrodom["id"] = elemento[InterfaceEnergyHome.ATTR_APP_PID];
									Elettrodom["nome"] = elemento[InterfaceEnergyHome.ATTR_APP_NAME];
									//Elettrodom["categoryObj"] = elemento[InterfaceEnergyHome.ATTR_APP_CATEGORY];
									//var catIndex = Elettrodomestici.getCategoryIndex(Elettrodom["categoryObj"].name);
									Elettrodom["categoria"] = elemento[InterfaceEnergyHome.ATTR_APP_CATEGORY];
									var values = elemento["device_value"];
									Elettrodom["measure"] = {principal: {value: " ", unity: " ", label: " ", name: " ", type: ""}};
									Elettrodom["stato"] = 0;
									
									$.each(values.list, function(idx, el) {
										if (el.name == "IstantaneousDemands"){
											Elettrodom["consumo"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value.toFixed(0), unity: "W", label: "Consumption: ", name: "watt", type: el.name};
											Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
											Elettrodomestici.consumoTotale += Elettrodom["consumo"];
										} else if (el.name == "CurrentLevel"){
											Elettrodom["level"] = el.value.value;
											var val = Math.round((el.value.value/254)*100);
											Elettrodom["measure"][el.name] = {value: val, unity: "% ", label: "Level", name: "", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "OnOffState"){
											Elettrodom["stato"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value, unity: " ", label: "State: ", name: "", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "LocalHumidity"){
											Elettrodom["humidity"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value, unity: "% RH", label: "Umidity: ", name: "relative humidity", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "ZoneStatus"){
											Elettrodom["zonestatus"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value, unity: " ", label: "State: ", name: "", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "Illuminance"){
											Elettrodom["illuminance"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value, unity: " ", label: "State: ", name: "", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "Occupancy"){
											Elettrodom["occupancy"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value, unity: " ", label: "State: ", name: "", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "Temperature"){
											Elettrodom["temperature"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value.toFixed(1), unity: "C", label: "Temperature: ", name: "celsius", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "LocalTemperature"){
											Elettrodom["temperature"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value.toFixed(1), unity: "C", label: "Temperature: ", name: "celsius", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "LockState"){
											Elettrodom["lockState"] = el.value.value;
											var val = null;
											if (Elettrodom["lockState"] == 2){
												val = "OPEN";
											} else if (Elettrodom["lockState"] == 1){
												val = "CLOSE";
											} else {
												val = "NP";
											}
											Elettrodom["measure"][el.name] = {value: val, unity: " ", label: "Door: ", name: "", type: el.name};
											Elettrodom["measure"][el.name] = {value: " ", unity: " ", label: " ", name: "", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "CurrentPositionLiftPercentage"){
											Elettrodom["WindowState"] = el.value.value;
											var val = null;
											if (Elettrodom["WindowState"] > 0){
												val = "OPEN";
											} else if (Elettrodom["WindowState"] == 0){
												val = "CLOSE";
											} else {
												val = "NP";
											}
											Elettrodom["measure"][el.name] = {value: val, unity: " ", label: "Window: ", name: "", type: el.name};
											Elettrodom["measure"][el.name] = {value: " ", unity: " ", label: " ", name: ""};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										}
										
									});
									
									Elettrodom["location"] = elemento[InterfaceEnergyHome.ATTR_APP_LOCATION];
									Elettrodom["connessione"] = elemento[InterfaceEnergyHome.ATTR_APP_AVAIL];
									
									if (elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WHITEGOOD_APP_TYPE){
										Elettrodom["type"] = 'whitegood';
										if(Elettrodom["connessione"] == 2){
											Elettrodom["stato"] = 1;
										} 
									}
									else if ((elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.THERMOSTAT_SENSOR_APP_TYPE) || (elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.THERMOSTAT_SENSOR_APP_TYPE_2)) {
										Elettrodom["type"] = "thermostat";
										if(Elettrodom["connessione"] == 2){
											Elettrodom["stato"] = 1;
										}
									} else if ((elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.TEMPERATURE_SENSOR_APP_TYPE) || (elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.TEMPERATURE_SENSOR_APP_TYPE_2)) {
										Elettrodom["type"] = "thermostat";
										if(Elettrodom["connessione"] == 2){
											Elettrodom["stato"] = 1;
										}
									} else if (elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.TEMPERATURE_URMET_SENSOR_APP_TYPE) {
										Elettrodom["type"] = "thermostat";
										if(Elettrodom["connessione"] == 2){
											Elettrodom["stato"] = 1;
										}
									} else if ((elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.LOCKDOOR_APP_TYPE) || (elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.LOCKDOOR_APP_TYPE_2)) {  //LockDoor
										Elettrodom["type"] = elemento[InterfaceEnergyHome.ATTR_APP_TYPE];
										if(Elettrodom["connessione"] == 2){
											val = Elettrodom["lockState"];
											if (val == 2){
												Elettrodom["icon"] = "lockdoor_acceso.png";
												Elettrodom["stato"] = 2; //Forzo
											} else if (val == 2){
												Elettrodom["icon"] = "lockdoor_spento.png";
												Elettrodom["stato"] = 1; //Forzo
											} else {
												Elettrodom["icon"] = "lockdoor_acceso.png";
												Elettrodom["stato"] = 0; //Forzo
											}
										} else {
											Elettrodom["icon"] = "lockdoor_disconnesso.png";
											Elettrodom["stato"] = 0; //Forzo
										}
										elemento["icon"] = "lockdoor.png"; //Forzo x ora
									} else if ((elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WINDOWCOVERING_APP_TYPE) || (elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WINDOWCOVERING_APP_TYPE_2)) {  //WindowCovering
										Elettrodom["type"] = elemento[InterfaceEnergyHome.ATTR_APP_TYPE];
										if(Elettrodom["connessione"] == 2){
											val = Elettrodom["WindowState"];
											if ((val > 0) && (val < 254)){
												Elettrodom["icon"] = "windowc_acceso.png";
												Elettrodom["stato"] = 8; //Forzo
											} else if (val == 255){
												Elettrodom["icon"] = "windowc_aperta.png";
												Elettrodom["stato"] = 6; //Forzo
											} else if (val == 0){
												Elettrodom["icon"] = "windowc_aperta.png";
												Elettrodom["stato"] = 7; //Forzo
											} else {
												Elettrodom["icon"] = "windowc_disconnesso.png";
												Elettrodom["stato"] = 7; //Forzo
											}
										} else {
											Elettrodom["icon"] = "windowc_disconnesso.png";
											Elettrodom["stato"] = 0; //Forzo
										}
										elemento["icon"] = "windowc.png"; //Forzo x ora
									} else {
										Elettrodom["type"] = 'smartplug';
										if(Elettrodom["connessione"] == 2){
											//Elettrodom["stato"] = 1;
										}
									}
									var str = elemento[InterfaceEnergyHome.ATTR_APP_ICON];
				
									Elettrodom["icona"] = str.replace(".png", "");
									Elettrodomestici.listaElettrodomestici.push(Elettrodom);
								});
				
					if (Elettrodomestici.SmartInfo != null) {
						var Elettrodom = {};
						Elettrodom["icona"] = "plug";
						Elettrodom["nome"] = "Altri consumi";
						Elettrodom["categoria"] = 12;
						Elettrodomestici.altroConsumo = Elettrodomestici.potenzaAttuale.value - Elettrodomestici.consumoTotale;
						Elettrodomestici.altroConsumo = Elettrodomestici.altroConsumo > 0 ? Elettrodomestici.altroConsumo : 0;
						Elettrodom["consumo"] = Elettrodomestici.altroConsumo;
						Elettrodom["location"] = 10;
						Elettrodom["stato"] = 1;
						Elettrodom["connessione"] = 2;
						//Elettrodomestici.listaElettrodomestici.push(Elettrodom);
					}
					Elettrodomestici.numDispositivi = Elettrodomestici.listaElettrodomestici.length;
					Elettrodomestici.numPagine = Math.ceil(Elettrodomestici.listaElettrodomestici.length/Elettrodomestici.perPagina);
					callBack();
				}
			});
		} catch (err) {
			console.log("ERRORE:");
			console.log(err);
			InterfaceEnergyHome.GestErrorEH("Dispositivi", err);
		}
	}else if(InterfaceEnergyHome.mode==-2)
	{
		
		try {
				Elettrodomestici.lista1ricevuta = {};
						$.each(AppliancesConfigurationFake.result.list,
								function(indice, elettrodom) {
									if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.SMARTINFO_APP_TYPE) {
										//Analizzo lo SmartInfo
										if (elettrodom["map"]["category"]["name"] == "Meter"){
											Elettrodomestici.SmartInfo = elettrodom["map"];
										}else{
											//it may be the PV meter... skip it
											Elettrodomestici.SmartInfoPV = elettrodom["map"];
										}
									} else if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WHITEGOOD_APP_TYPE){
										
										Elettrodomestici.lista1ricevuta[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
									} else if ((elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.TEMPERATURE_SENSOR_APP_TYPE) || (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.TEMPERATURE_SENSOR_APP_TYPE_2)){
										Elettrodomestici.lista1ricevuta[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
									} else if ((elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.THERMOSTAT_SENSOR_APP_TYPE) || (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.THERMOSTAT_SENSOR_APP_TYPE_2)){
										Elettrodomestici.lista1ricevuta[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
									} else if ((elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.LOCKDOOR_APP_TYPE) || (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.LOCKDOOR_APP_TYPE_2)){
										Elettrodomestici.lista1ricevuta[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
									} else if ((elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WINDOWCOVERING_APP_TYPE) || (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WINDOWCOVERING_APP_TYPE_2)){
										Elettrodomestici.lista1ricevuta[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
									} else {
										Elettrodomestici.lista1ricevuta[elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID]] = elettrodom["map"];
									}
								});
				
					Elettrodomestici.listaElettrodomestici = [];
					Elettrodomestici.consumoTotale = 0;
					Elettrodomestici.altroConsumo = 0;
					
					$.each(Elettrodomestici.lista1ricevuta,
								function(index, elemento) {
									var Elettrodom = {};
									Elettrodom["id"] = elemento[InterfaceEnergyHome.ATTR_APP_PID];
									Elettrodom["nome"] = elemento[InterfaceEnergyHome.ATTR_APP_NAME];
									//Elettrodom["categoryObj"] = elemento[InterfaceEnergyHome.ATTR_APP_CATEGORY];
									//var catIndex = Elettrodomestici.getCategoryIndex(Elettrodom["categoryObj"].name);
									Elettrodom["categoria"] = elemento[InterfaceEnergyHome.ATTR_APP_CATEGORY];
									var values = elemento["device_value"];
									Elettrodom["measure"] = {principal: {value: " ", unity: " ", label: " ", name: " ", type: ""}};
									Elettrodom["stato"] = 0;
									
									$.each(values.list, function(idx, el) {
										if (el.name == "IstantaneousDemands"){
											Elettrodom["consumo"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value.toFixed(0), unity: "W", label: "Consumption: ", name: "watt", type: el.name};
											Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
											Elettrodomestici.consumoTotale += Elettrodom["consumo"];
										} else if (el.name == "CurrentLevel"){
											Elettrodom["level"] = el.value.value;
											var val = Math.round((el.value.value/254)*100);
											Elettrodom["measure"][el.name] = {value: val, unity: "% ", label: "Level", name: "", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "OnOffState"){
											Elettrodom["stato"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value, unity: " ", label: "State: ", name: "", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "LocalHumidity"){
											Elettrodom["humidity"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value, unity: "% RH", label: "Umidity: ", name: "relative humidity", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "ZoneStatus"){
											Elettrodom["zonestatus"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value, unity: " ", label: "State: ", name: "", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "Illuminance"){
											Elettrodom["illuminance"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value, unity: " ", label: "State: ", name: "", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "Occupancy"){
											Elettrodom["occupancy"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value, unity: " ", label: "State: ", name: "", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "Temperature"){
											Elettrodom["temperature"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value.toFixed(1), unity: "C", label: "Temperature: ", name: "celsius", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "LocalTemperature"){
											Elettrodom["temperature"] = el.value.value;
											Elettrodom["measure"][el.name] = {value: el.value.value.toFixed(1), unity: "C", label: "Temperature: ", name: "celsius", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "LockState"){
											Elettrodom["lockState"] = el.value.value;
											var val = null;
											if (Elettrodom["lockState"] == 2){
												val = "OPEN";
											} else if (Elettrodom["lockState"] == 1){
												val = "CLOSE";
											} else {
												val = "NP";
											}
											Elettrodom["measure"][el.name] = {value: val, unity: " ", label: "Door: ", name: "", type: el.name};
											Elettrodom["measure"][el.name] = {value: " ", unity: " ", label: " ", name: "", type: el.name};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										} else if (el.name == "CurrentPositionLiftPercentage"){
											Elettrodom["WindowState"] = el.value.value;
											var val = null;
											if (Elettrodom["WindowState"] > 0){
												val = "OPEN";
											} else if (Elettrodom["WindowState"] == 0){
												val = "CLOSE";
											} else {
												val = "NP";
											}
											Elettrodom["measure"][el.name] = {value: val, unity: " ", label: "Window: ", name: "", type: el.name};
											Elettrodom["measure"][el.name] = {value: " ", unity: " ", label: " ", name: ""};
											if (Elettrodom["measure"]["principal"] == null)
												Elettrodom["measure"]["principal"] = Elettrodom["measure"][el.name];
										}
										
									});
									
									Elettrodom["location"] = elemento[InterfaceEnergyHome.ATTR_APP_LOCATION];
									Elettrodom["connessione"] = elemento[InterfaceEnergyHome.ATTR_APP_AVAIL];
									
									if (elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WHITEGOOD_APP_TYPE){
										Elettrodom["type"] = 'whitegood';
										if(Elettrodom["connessione"] == 2){
											Elettrodom["stato"] = 1;
										} 
									}
									else if ((elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.THERMOSTAT_SENSOR_APP_TYPE) || (elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.THERMOSTAT_SENSOR_APP_TYPE_2)) {
										Elettrodom["type"] = "thermostat";
										if(Elettrodom["connessione"] == 2){
											Elettrodom["stato"] = 1;
										}
									} else if ((elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.TEMPERATURE_SENSOR_APP_TYPE) || (elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.TEMPERATURE_SENSOR_APP_TYPE_2)) {
										Elettrodom["type"] = "thermostat";
										if(Elettrodom["connessione"] == 2){
											Elettrodom["stato"] = 1;
										}
									} else if (elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.TEMPERATURE_URMET_SENSOR_APP_TYPE) {
										Elettrodom["type"] = "thermostat";
										if(Elettrodom["connessione"] == 2){
											Elettrodom["stato"] = 1;
										}
									} else if ((elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.LOCKDOOR_APP_TYPE) || (elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.LOCKDOOR_APP_TYPE_2)) {  //LockDoor
										Elettrodom["type"] = elemento[InterfaceEnergyHome.ATTR_APP_TYPE];
										if(Elettrodom["connessione"] == 2){
											val = Elettrodom["lockState"];
											if (val == 2){
												Elettrodom["icon"] = "lockdoor_acceso.png";
												Elettrodom["stato"] = 2; //Forzo
											} else if (val == 2){
												Elettrodom["icon"] = "lockdoor_spento.png";
												Elettrodom["stato"] = 1; //Forzo
											} else {
												Elettrodom["icon"] = "lockdoor_acceso.png";
												Elettrodom["stato"] = 0; //Forzo
											}
										} else {
											Elettrodom["icon"] = "lockdoor_disconnesso.png";
											Elettrodom["stato"] = 0; //Forzo
										}
										elemento["icon"] = "lockdoor.png"; //Forzo x ora
									} else if ((elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WINDOWCOVERING_APP_TYPE) || (elemento[InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.WINDOWCOVERING_APP_TYPE_2)) {  //WindowCovering
										Elettrodom["type"] = elemento[InterfaceEnergyHome.ATTR_APP_TYPE];
										if(Elettrodom["connessione"] == 2){
											val = Elettrodom["WindowState"];
											if ((val > 0) && (val < 254)){
												Elettrodom["icon"] = "windowc_acceso.png";
												Elettrodom["stato"] = 8; //Forzo
											} else if (val == 255){
												Elettrodom["icon"] = "windowc_aperta.png";
												Elettrodom["stato"] = 6; //Forzo
											} else if (val == 0){
												Elettrodom["icon"] = "windowc_aperta.png";
												Elettrodom["stato"] = 7; //Forzo
											} else {
												Elettrodom["icon"] = "windowc_disconnesso.png";
												Elettrodom["stato"] = 7; //Forzo
											}
										} else {
											Elettrodom["icon"] = "windowc_disconnesso.png";
											Elettrodom["stato"] = 0; //Forzo
										}
										elemento["icon"] = "windowc.png"; //Forzo x ora
									} else {
										Elettrodom["type"] = 'smartplug';
										if(Elettrodom["connessione"] == 2){
											//Elettrodom["stato"] = 1;
										}
									}
									var str = elemento[InterfaceEnergyHome.ATTR_APP_ICON];
				
									Elettrodom["icona"] = str.replace(".png", "");
									Elettrodomestici.listaElettrodomestici.push(Elettrodom);
								});
				
					if (Elettrodomestici.SmartInfo != null) {
						var Elettrodom = {};
						Elettrodom["icona"] = "plug";
						Elettrodom["nome"] = "Altri consumi";
						Elettrodom["categoria"] = 12;
						//initialize consumption with values from configAdmin
						$.each(fakeValues.noServerCustomDevice.list, function(indice, elettrodom) {
							if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.SMARTINFO_APP_TYPE) {
								if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_CATEGORY] == "12") {
									Elettrodomestici.SmartInfo.device_value.list[0].value.value=elettrodom["map"]["potenza"]
								}
							}
						});
						
						
						Elettrodomestici.potenzaAttuale={
								value: Elettrodomestici.SmartInfo.device_value.list[0].value.value
						}
						Elettrodomestici.altroConsumo = Elettrodomestici.potenzaAttuale.value - Elettrodomestici.consumoTotale;
						Elettrodomestici.altroConsumo = Elettrodomestici.altroConsumo > 0 ? Elettrodomestici.altroConsumo : 0;
						Elettrodom["consumo"] = Elettrodomestici.altroConsumo;
						Elettrodom["location"] = 10;
						Elettrodom["stato"] = 1;
						Elettrodom["connessione"] = 2;
						
						
						//Elettrodomestici.listaElettrodomestici.push(Elettrodom);
					}
					Elettrodomestici.numDispositivi = Elettrodomestici.listaElettrodomestici.length;
					Elettrodomestici.numPagine = Math.ceil(Elettrodomestici.listaElettrodomestici.length/Elettrodomestici.perPagina);
					callBack();
		} catch (err) {
			console.log("ERRORE:");
			console.log(err);
			InterfaceEnergyHome.GestErrorEH("Dispositivi", err);
		}
		
	}else {

		//Lista finta di elettrodomestici per simulazione
		Elettrodomestici.listaElettrodomestici = [];
		Elettrodomestici.consumoTotale = 0;
		for (var i=0;i<5;i++) {
			var icone = ["lampadina.png","lvb1.png","frigorifero.png","forno.png","pczone.png","plug.png"];
			var nomi = ["lampadina","lavatrice","frigorifero","forno","computer","smart plug"];
			var stati = [0,1,1,1,4,1];
			var Elettrodom = {};
			Elettrodom["id"] = "ah.pid.1000"+i;
			Elettrodom["nome"] = nomi[i];
			Elettrodom["categoria"] = 3;
			var pot = "1" + i;
			Elettrodom["consumo"] = parseFloat(pot);
			Elettrodom["location"] = 3;
			Elettrodom["stato"] = stati[i];
			Elettrodom["connessione"] = 2;
			Elettrodom["type"] = 'smartplug';
			Elettrodomestici.consumoTotale += Elettrodom["consumo"];
			var str = icone[i];
			Elettrodom["icona"] = str.replace(".png", "");
			Elettrodomestici.listaElettrodomestici.push(Elettrodom);
		}
		Elettrodomestici.numPagine = Math.ceil(Elettrodomestici.listaElettrodomestici.length/Elettrodomestici.perPagina);
		callBack();
	}
}

Elettrodomestici.ReadCurrentProduction=function(callBack){
	if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)) {
		try {
			InterfaceEnergyHome.objService.getAttribute( function(result2, err2){
				if (err2 != null){
					if (Main.env == 0)
						console.log('exception in FotoVoltaico.js - in Elettrodomestici.GetElettrodomestici method: ', err2);
					InterfaceEnergyHome.GestErrorEH("GetElettrodomestici", err2);
				} else if (result2 != null){
					if (!isNaN(result2.value)) {
						
						//Elettrodomestici.potenzaAttuale.value -= result2.value;
						if (Elettrodomestici.potenzaAttuale.value < 0) {
							Elettrodomestici.potenzaAttuale.value = 0;
						}
						if (Elettrodomestici.potenzaAttuale.value < Elettrodomestici.consumoTotale){
							Elettrodomestici.potenzaAttuale.value = Elettrodomestici.consumoTotale;
						}
					}
				} 		
				callBack();
			}, InterfaceEnergyHome.PRODUZIONE_TOTALE);
		} catch (err) {
			console.log("ERRORE:");
			console.log(err);
			InterfaceEnergyHome.GestErrorEH("GetDatiPotenzaElettr", err);
		}
	}else {
		callBack();
	}
};

Elettrodomestici.ReadCurrentPower=function(callBack){
	if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)) {
		try {
			InterfaceEnergyHome.objService.getAttribute( function(result, err){
					if (err != null){
						if (Main.env == 0)
							console.log('exception in FotoVoltaico.js - in Elettrodomestici.GetElettrodomestici method: ', err);
						InterfaceEnergyHome.GestErrorEH("GetElettrodomestici", err);
					} else if (result != null){
						Elettrodomestici.potenzaAttuale.value = result.value;
					} else {
						Elettrodomestici.potenzaAttuale.value = null;
					}
					callBack();
					
				}, InterfaceEnergyHome.POTENZA_TOTALE);
		} catch (err) {
			console.log("ERRORE:");
			console.log(err);
			InterfaceEnergyHome.GestErrorEH("GetDatiPotenzaElettr", err);
		}
	} else {
		Elettrodomestici.potenzaAttuale.value = 0;
		callBack();
	}
}

Elettrodomestici.GestElettrodomestici = function(){
	Elettrodomestici.init();
}

Elettrodomestici.init=function(){
	Elettrodomestici.indexElettrodomestico = 0;
	Elettrodomestici.interfaccia = null;
	
	var divElettro = $("#Elettrodomestici");

	if (divElettro.length == 0) {
		$("#ContentMain").append(Elettrodomestici.htmlContent);
	} else {
		$("#Elettrodomestici").show();
	}
	
	Elettrodomestici.addSpinner("#Elettrodomestici","none");
	
	$.get('js/dispositivi/templateElettrodomestici.html', function(data) {
		$('#Elettrodomestici').html(data);
		
		//add translations
		$("#TitoloRiepilogo").html(Msg.home.consumptionsSummary+":");
		$("#ConsumoDispositivi").html(Msg.home.devicesConsumption+":");
		$("#AltriConsumi").html(Msg.home.otherConsumption+":");
		$("#ConsumiTotali").html(Msg.home.totalConsumption+":");
				
		$("#RigaPulsanti").hide();
		$("#RigaPulsanti #indietro").click(function(){
		console.log("->"+Elettrodomestici.pagina+","+Elettrodomestici.numPagine);
		if (Elettrodomestici.pagina>0) {
			Elettrodomestici.pagina--;
			Elettrodomestici.refreshDevices();
		}
	
		});
		$("#RigaPulsanti #avanti").click(function(){
			console.log("->"+Elettrodomestici.pagina+","+Elettrodomestici.numPagine);
			if (Elettrodomestici.pagina<Elettrodomestici.numPagine-1) {
				Elettrodomestici.pagina++;
				Elettrodomestici.refreshDevices();
			}
		});
		
		Elettrodomestici.addSpinner("#RigaElettrodomestici","none");
		Elettrodomestici.GetCategories(function(){
			Elettrodomestici.GetLocations(function(){
				Elettrodomestici.lock=false;
				Elettrodomestici.update();
			});
		});
	});
}

Elettrodomestici.update=function(){
	if(InterfaceEnergyHome.mode== -2)
	{
		Elettrodomestici.GetDevicesInfos(function(){
			Elettrodomestici.removeSpinner("#RigaElettrodomestici");
			Elettrodomestici.refreshDevices();
			Elettrodomestici.refreshConsumi();
			if (Elettrodomestici.interfaccia!=null) {
				Elettrodomestici.interfaccia.update(false);
			}
		});
		return;
	}
	if(Elettrodomestici.timerDispo==null){
		Elettrodomestici.timerDispo=setInterval(Elettrodomestici.update,Elettrodomestici.TIMER_UPDATE_ELETTR);
	}
	if (Elettrodomestici.lock) {
		return;
	}
	Elettrodomestici.lock=true;
	Elettrodomestici.ReadCurrentPower(function(){
		Elettrodomestici.ReadCurrentProduction(function(){
			Elettrodomestici.GetDevicesInfos(function(){
				Elettrodomestici.removeSpinner("#RigaElettrodomestici");
				Elettrodomestici.refreshDevices();
				Elettrodomestici.refreshConsumi();
				if (Elettrodomestici.interfaccia!=null) {
					Elettrodomestici.interfaccia.update(false);
				}
				Elettrodomestici.lock=false;
			});
		});
	});
}

Elettrodomestici.stopUpdate=function(){
	if (Elettrodomestici.timerDispo != null) {
		clearInterval(Elettrodomestici.timerDispo);
		Elettrodomestici.timerDispo = null;
	}
}

Elettrodomestici.refreshDevices=function(){
	$("#RigaElettrodomestici").html(" ");
	
	
	var start= Elettrodomestici.pagina*Elettrodomestici.perPagina;
	var end= ( start+Elettrodomestici.perPagina);
	if (end>Elettrodomestici.listaElettrodomestici.length) {
		end=Elettrodomestici.listaElettrodomestici.length;
	}
	
	if (Elettrodomestici.numPagine<2) {
		$("#RigaPulsanti").hide();
	}else{
		$("#RigaPulsanti").show();
	}
	if (Elettrodomestici.pagina>0) {
		$("#RigaPulsanti #indietro").show();
	}else{
		$("#RigaPulsanti #indietro").hide();
	}
	if (Elettrodomestici.pagina>= Elettrodomestici.numPagine-1) {
		$("#RigaPulsanti #avanti").hide();
	}else{
		$("#RigaPulsanti #avanti").show();
	}
	
	$.get('js/dispositivi/templateDispositivo.html', function(data) {
		
		for (i=start; i<end; i++) {
			var el=$(document.createElement('div')).attr('id', "device_"+i).attr('class', "Elettrodomestico").append(data);
			$("#RigaElettrodomestici").append(el);
			var pid=Elettrodomestici.listaElettrodomestici[i].id;
			//Salva l'appliance id all'interno dell'elemento
			$("#device_"+i).data("pid", pid);
			$("#device_"+i).data("categoria_id", Elettrodomestici.listaElettrodomestici[i].categoria);
			$("#device_"+i).data("current_index", i);
			
			//Imposto i valori dei campi
			$("#device_"+i+ " .NomeElettrodomestico .titolo").text(Elettrodomestici.listaElettrodomestici[i].nome);
			$("#device_"+i+ " .StatoElettrodomestico .stato").text(Elettrodomestici.listaElettrodomestici[i].nome);
			var stato="--";
			var class_stato="NONPRESENTE"
			if (Elettrodomestici.listaElettrodomestici[i].connessione==2) {
				if (Elettrodomestici.listaElettrodomestici[i].stato == 1){
					if (Elettrodomestici.listaElettrodomestici[i].categoria == 40){ //doorlock
						stato = "OPEN";
						class_stato = "ON";
					} else if (Elettrodomestici.listaElettrodomestici[i].categoria == 44){ //windowcovering
						stato = "OPEN";
						class_stato = "ON";
					} else if ((Elettrodomestici.listaElettrodomestici[i].categoria == 35) || (Elettrodomestici.listaElettrodomestici[i].categoria == 34)){  //
						stato = "ON";
						class_stato = "ON";
					} else if (Elettrodomestici.listaElettrodomestici[i].type == 'whitegood'){
						if (Elettrodomestici.listaElettrodomestici[i].consumo_value <= 0){
							//Il device whitegood consuma 0W, quindi spento
							stato = "OFF";
							class_stato = "ONOFF";
						} else if ((Elettrodomestici.listaElettrodomestici[i].consumo_value> 0) && (Elettrodomestici.listaElettrodomestici[i].consumo_value < 1)){
							//Il device consuma, ma meno di 1km, quindi spento ma stato connesso, per adesso spento
							stato = "OFF";
							class_stato = "ONOFF";
						} else if ((Elettrodomestici.listaElettrodomestici[i].consumo_value >= 1) && (Elettrodomestici.listaElettrodomestici[i].consumo_value <= 5)){
							//Il device consuma pi� di un 1W, ma meno di 5km, quindi standby
							//al momento lo mettiamo ON.
							stato = "ON";
							class_stato = "ON";
						} else {
							//Il device whitegood consuma tanto, quindi sta operando
							stato = "ON";
							class_stato = "ON";
						}
					} else {
						stato = "ON";
						class_stato = "ON";
					}
				} else if (Elettrodomestici.listaElettrodomestici[i].stato == 0){
					if (Elettrodomestici.listaElettrodomestici[i].categoria == 40){
						stato = "CLOSE";
						class_stato = "ON";
					} else if (Elettrodomestici.listaElettrodomestici[i].categoria == 44){
						stato = "CLOSE";
						class_stato = "ON";
					} else if ((Elettrodomestici.listaElettrodomestici[i].categoria == 35) || (Elettrodomestici.listaElettrodomestici[i].categoria == 34)){
						stato = "OFF";
						class_stato = "ONOFF";
					} else if (Elettrodomestici.listaElettrodomestici[i].type == 'whitegood'){
						if (Elettrodomestici.listaElettrodomestici[i].consumo_value <= 0){
							//Il device whitegood consuma 0W, quindi spento
							stato = "OFF";
							class_stato = "ONOFF";
						} else if ((Elettrodomestici.listaElettrodomestici[i].consumo_value> 0) && (Elettrodomestici.listaElettrodomestici[i].consumo_value < 1)){
							//Il device consuma, ma meno di 1km, quindi spento ma stato connesso, per adesso spento
							stato = "OFF";
							class_stato = "ONOFF";
						} else if ((Elettrodomestici.listaElettrodomestici[i].consumo_value >= 1) && (Elettrodomestici.listaElettrodomestici[i].consumo_value <= 5)){
							//Il device consuma pi� di un 1W, ma meno di 5km, quindi standby
							//al momento lo mettiamo ON.
							stato = "OFF";
							class_stato = "ON";
						} else {
							//Il device whitegood consuma tanto, quindi sta operando
							stato = "OFF";
							class_stato = "ON";
						}
					} else {
						stato = "OFF";
						class_stato = "ONOFF";
					}
				} else if (Elettrodomestici.listaElettrodomestici[i].stato > 1){
					if (Elettrodomestici.listaElettrodomestici[i].categoria == 40){
						if (Elettrodomestici.listaElettrodomestici[i].stato == 2){
							stato = "OPEN";
							class_stato = "ON";
						} else if (Elettrodomestici.listaElettrodomestici[i].stato == 1){
							stato = "CLOSE";
							class_stato = "ON";
						} else {
							stato = "OPEN";
							class_stato = "ON";
						}
					} else if (Elettrodomestici.listaElettrodomestici[i].categoria == 44){
						if (Elettrodomestici.listaElettrodomestici[i].stato == 7){
							stato = "CLOSE";
							class_stato = "ON";
						} else {
							stato = "OPEN";
							class_stato = "ON";
						}
					} else if ((Elettrodomestici.listaElettrodomestici[i].categoria == 35) || (Elettrodomestici.listaElettrodomestici[i].categoria == 34)){
						if (Elettrodomestici.listaElettrodomestici[i].stato == 1){
							stato = "ON";
							class_stato = "ON";
						} else {
							stato = "OFF";
							class_stato = "ONOFF";
						}
					} else if (Elettrodomestici.listaElettrodomestici[i].type == 'whitegood'){
						if (Elettrodomestici.listaElettrodomestici[i].consumo_value <= 0){
							//Il device whitegood consuma 0W, quindi spento
							stato = "OFF";
							class_stato = "ONOFF";
						} else if ((Elettrodomestici.listaElettrodomestici[i].consumo_value> 0) && (Elettrodomestici.listaElettrodomestici[i].consumo_value < 1)){
							//Il device consuma, ma meno di 1km, quindi spento ma stato connesso, per adesso spento
							stato = "OFF";
							class_stato = "ONOFF";
						} else if ((Elettrodomestici.listaElettrodomestici[i].consumo_value >= 1) && (Elettrodomestici.listaElettrodomestici[i].consumo_value <= 5)){
							//Il device consuma pi� di un 1W, ma meno di 5km, quindi standby
							//al momento lo mettiamo ON.
							stato = "ON";
							class_stato = "ON";
						} else {
							//Il device whitegood consuma tanto, quindi sta operando
							stato = "ON";
							class_stato = "ON";
						}
					} else {
						stato = "OFF";
						class_stato = "ONOFF";
					}
				}

			} else {
				stato = "--";
				class_stato = "OFF";
			}
			$("#device_" + i).addClass(class_stato);
			$("#device_" + i + " .StatoElettrodomestico .stato").text(stato);
			
			$("#device_"+i+ " .StatoElettrodomestico .lblFirstValue").text(Elettrodomestici.listaElettrodomestici[i]["measure"]["principal"].label);
			$("#device_"+i+ " .StatoElettrodomestico .lblMeasure").text(Elettrodomestici.listaElettrodomestici[i]["measure"]["principal"].value + " " + Elettrodomestici.listaElettrodomestici[i]["measure"]["principal"].unity);
			$("#device_"+i+ " .StatoElettrodomestico .posizione_value").text(Elettrodomestici.locazioni[Elettrodomestici.listaElettrodomestici[i].location]);
			var icona_src= "Resources/Images/Devices/"+Elettrodomestici.getIcon(Elettrodomestici.listaElettrodomestici[i]);
			$("#device_"+i+ " .IconaElettrodomestico .icona-dispositivo").attr("src",icona_src);
			
			$("#device_"+i).click(function(){
				$('#Interfaccia .content').html(" ");
				$('#Interfaccia .header .titolo').text("");
				
				Elettrodomestici.addSpinner("#Interfaccia","none");
				
				var pid = $(this).data("pid");
				var id = $(this).attr("id");
				var cat_id = $(this).data("categoria_id");
				var index = $(this).data("current_index");
				//console.log("--_index:"+index);
				var nome = $("#"+id+" .NomeElettrodomestico .titolo").text();
				var interfaccia_src = "js/dispositivi/ifBase.html";
				Elettrodomestici.interfaccia = ifBase; //variabile che punta alla classe interfaccia da eseguire
				//Lampade
				if(cat_id == 35){
					interfaccia_src = "js/dispositivi/ifLampada.html";
					Elettrodomestici.interfaccia = ifLampada;
				}
				//luce Mac
				if(cat_id == 34){
					interfaccia_src = "js/dispositivi/ifLampadaMac.html";
					Elettrodomestici.interfaccia = ifLampadaMac;
				}
				//termostato
				//if (Elettrodomestici.listaElettrodomestici[index].type=="thermostat") {
				if(cat_id == 36){
					interfaccia_src = "js/dispositivi/ifThermostat.html";
					Elettrodomestici.interfaccia = ifThermostat;
				}
				//Indesit lavatrice
				if(cat_id == 37){
					interfaccia_src = "js/dispositivi/ifIndesitWM.html";
					Elettrodomestici.interfaccia = ifIndesitWM;
				}
				//Indesit forno
				if(cat_id == 38){
					interfaccia_src = "js/dispositivi/ifIndesitOven.html";
					Elettrodomestici.interfaccia = ifIndesitOven;
				}
				//Indesit frigorifero
				if(cat_id == 39){
					interfaccia_src = "js/dispositivi/ifIndesitFridge.html";
					Elettrodomestici.interfaccia = ifIndesitFridge;
				}
				//LockDoor
				if(cat_id == 40){
					interfaccia_src = "js/dispositivi/ifLockDoor.html";
					Elettrodomestici.interfaccia = ifLockDoor;
				}
				//WindowCovering
				if(cat_id == 44){
					interfaccia_src = "js/dispositivi/ifWindowCovering.html";
					Elettrodomestici.interfaccia = ifWindowCovering;
				}
				
				if(Elettrodomestici.listaElettrodomestici[index].connessione != 2 || Elettrodomestici.listaElettrodomestici[index].stato == 4 ){
					Elettrodomestici.loadInterfaccia(nome,pid,cat_id,index, interfaccia_src,null);
				}else{
				//Leggo tutti i cluster disponibili sul dispositivo
					Elettrodomestici.getDeviceClusters(pid,function(clusters, err){
							if (err != null) {
								console.log("Elettrodomestici.getDeviceClusters: error"+ err);
							}
							console.debug(clusters);
							Elettrodomestici.loadInterfaccia(nome,pid,cat_id,index,interfaccia_src,clusters);
					});
				}
			});
		}
	});
}

Elettrodomestici.loadInterfaccia=function(nome,pid, cat_id, index,interfaccia_src,clusters){
	$('#Interfaccia .content').html(" ");
	$('#Interfaccia .header .titolo').text("");
	$.get( interfaccia_src, function(data){
		$("#Interfaccia").hide();
		$("#Interfaccia").data("pid",undefined);
		$("#Interfaccia").data("current_index",-1);
		$('#Interfaccia .header .titolo').text(nome);
		$('#Interfaccia .content').append(data);
		
		Elettrodomestici.removeSpinner("#Interfaccia");
		
		$("#Interfaccia").data("pid",pid);
		$("#Interfaccia").data("current_index",index);
		var icona_src= "Resources/Images/Devices/"+Elettrodomestici.getIcon(Elettrodomestici.listaElettrodomestici[index]);
		$("#Interfaccia .icona .icona-dispositivo").attr("src",icona_src);
		$("#Interfaccia").fadeIn(200);
		if (Elettrodomestici.interfaccia!=null) {
			console.debug(clusters);
			Elettrodomestici.interfaccia.init(clusters, index);
		}
	});
}

Elettrodomestici.getDeviceClusters=function(pid,callBack){
	if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)){
		InterfaceEnergyHome.objService.getDeviceClusters(function(clusters, err){
			if (err != null) {
				console.log("err:" + err);
				callBack(null, err);
			}else if (clusters != null) {
				var clusterobj = {};
				for (var keys in clusters["map"]) {
					n= clusters["map"][keys];
					clusterobj[n] = true;
				}	
			}
			callBack(clusterobj, err);
		},pid);
	}else if(InterfaceEnergyHome.mode == -2){
		var appliance= getFakeDeviceValueByPID(pid);
		console.debug(appliance.map.clusters);
		if(appliance!=null)
		{
			if(appliance.map.clusters!=null)
			{
				
				callBack(appliance.map.clusters,null);
				return;
			}
		}
		callBack({}, null);
	}else{
		callBack({}, null);
	}
}

Elettrodomestici.refreshConsumi=function(){
	$("#Elettrodomestici #RiepilogoConsumi .dispositivi_consumi_value").text(Math.round(Elettrodomestici.consumoTotale) + "W");
	$("#Elettrodomestici #RiepilogoConsumi .altri_consumi_value").text(Math.round(Elettrodomestici.altroConsumo) + "W");
	$("#Elettrodomestici #RiepilogoConsumi .totale_consumi_value").text(Math.round(Elettrodomestici.potenzaAttuale.value) + "W");
	
}

Elettrodomestici.getIcon=function(elettrodomestico, forza_stato){
	connessioneElettr = elettrodomestico["connessione"];
	consumoElettr = elettrodomestico["consumo"];
	categoriaElettr = elettrodomestico["categoria"];
	nomeElettr = elettrodomestico["nome"];
	statoElettr = elettrodomestico["stato"];
	if (forza_stato!=null) {
		statoElettr = forza_stato;
	}
	typeElettr = elettrodomestico["type"];
	locationElettr = elettrodomestico["location"];
	estensioneIcona = ".png";
	if (connessioneElettr != 2 || statoElettr == 4 ) {
		estensioneIcona = "_disconnesso.png";
	} else if (statoElettr) {
		if (nomeElettr == 'Altri consumi'){
			if (consumoElettr > 0) {
				//Se c'e' consumo visualizzo l'icona verde
				estensioneIcona = "_acceso.png";
			} else {
				//Altrimenti lo visualizzo spento
				estensioneIcona = "_spento.png";

			}
		} else if (categoriaElettr == 40) {
			//DoorLock
			var lockStateElettr;
			if (forza_stato != null) {
				lockStateElettr = forza_stato;
				if (lockStateElettr == 2) {
					estensioneIcona = "_acceso.png";
				} else {
					estensioneIcona = "_spento.png";
				}
			} else {
				lockStateElettr = elettrodomestico["lockState"];
				if (lockStateElettr == 2) {
					estensioneIcona = "_acceso.png";
				} else {
					estensioneIcona = "_spento.png";
				}
			}
		} else if (categoriaElettr == 44) {
			//WindowCovering
			var wCoveringStateElettr;
			if (forza_stato != null) {
				wCoveringStateElettr = forza_stato;
				if (wCoveringStateElettr == 6) {
					estensioneIcona = "_aperta.png";
				} else if (wCoveringStateElettr == 7) {
					estensioneIcona = "_chiusa.png";
				} else {
					estensioneIcona = "_acceso.png";
				}
			} else {
				wCoveringStateElettr = elettrodomestico["WindowState"];
				if ((wCoveringStateElettr > 0) && (wCoveringStateElettr < 65535)){
					estensioneIcona = "_acceso.png";
				} else if (wCoveringStateElettr == 65535){
					estensioneIcona = "_aperta.png";
				} else if (wCoveringStateElettr == 0){
					estensioneIcona = "_aperta.png";
				} else {
					estensioneIcona = "_disconnesso.png";
				}
			}
		} else if ((typeElettr == 'whitegood') || (categoriaElettr == 34) || (categoriaElettr == 35)){
			//Se il dispositivo e' una lavatrice ed e' connesso
			if (connessioneElettr == 2){
				estensioneIcona = "_acceso.png";
			} else {
				//Se il dispositivo e' una lavatrice ed NON e' connesso
				estensioneIcona = "_spento.png";
			}
		} else if (consumoElettr > 0) {
			//Se c'� consumo visualizzo l'icona verde
			estensioneIcona = "_acceso.png";

		} else {
			if (connessioneElettr == 2){
				estensioneIcona = "_acceso.png";
			} else {
				//Se il dispositivo e' una lavatrice ed NON e' connesso
				estensioneIcona = "_spento.png";
			}

		}
	} else {
		//whitegood
		if ((typeElettr == 'whitegood') || (categoriaElettr == 34) || (categoriaElettr == 35)){
			/*//Se il dispositivo e' una lavatrice ed e' in standby
			if (consumoElettr <= 0){
				//Il device whitegood consuma 0W, quindi spento
				estensioneIcona = "_spento.png";
			} else if ((consumoElettr> 0) && (consumoElettr < 1)){
				//Il device consuma, ma meno di 1km, quindi spento ma stato connesso, per adesso spento
				estensioneIcona = "_spento.png";
			} else if ((consumoElettr >= 1) && (consumoElettr <= 5)){
				//Il device consuma pu' di un 1W, ma meno di 5km, quindi standby
				//al momento lo mettiamo ON.
				estensioneIcona = "_acceso.png";
			} else {
				//Il device whitegood consuma tanto, quindi sta operando
				estensioneIcona = "_acceso.png";
			}*/
			//Se il dispositivo e' una lavatrice ed e' connesso
			if (connessioneElettr == 2){
				estensioneIcona = "_acceso.png";
			} else {
				//Se il dispositivo e' una lavatrice ed NON e' connesso
				estensioneIcona = "_spento.png";
			}
		} else if (categoriaElettr == 40) {
			//DoorLock
			var lockStateElettr;
			if (forza_stato != null) {
				lockStateElettr = forza_stato;
				if (lockStateElettr == 1) {
					estensioneIcona = "_acceso.png";
				} else {
					estensioneIcona = "_spento.png";
				}
			} else {
				lockStateElettr = elettrodomestico["lockState"];
				if (lockStateElettr == 2) {
					estensioneIcona = "_acceso.png";
				} else {
					estensioneIcona = "_spento.png";
				}
			}
		} else if (categoriaElettr == 44) {
			//WindowCovering
			var wCoveringStateElettr;
			if (forza_stato != null) {
				wCoveringStateElettr = forza_stato;
				if (wCoveringStateElettr == 6) {
					estensioneIcona = "_aperta.png";
				} else if (wCoveringStateElettr == 7) {
					estensioneIcona = "_chiusa.png";
				} else {
					estensioneIcona = "_acceso.png";
				}
			} else {
				wCoveringStateElettr = elettrodomestico["WindowState"];
				if ((wCoveringStateElettr > 0) && (wCoveringStateElettr < 65535)){
					estensioneIcona = "_acceso.png";
				} else if (wCoveringStateElettr == 65535){
					estensioneIcona = "_aperta.png";
				} else if (wCoveringStateElettr == 0){
					estensioneIcona = "_aperta.png";
				} else {
					estensioneIcona = "_disconnesso.png";
				}
			}
		} else {
			estensioneIcona = "_spento.png";
		}
	}
	//return Elettrodomestici.categorie[elettrodomestico.categoria].icon;
	return elettrodomestico.icona + estensioneIcona;	
	
}

Elettrodomestici.addSpinner=function(parentId, background){
	$(parentId+" .el-spinner").remove();
	
	$(parentId).append("<div class=\"el-spinner\"></div>");
	$(parentId+" .el-spinner").css("background-color",background);
	
	$(parentId+" .el-spinner").width($(parentId).width());
        $(parentId+" .el-spinner").height($(parentId).height());
        $(parentId+" .el-spinner").fadeTo(0,.95);
}

Elettrodomestici.removeSpinner=function(parentId){
	if (parentId==null) {
		$(".el-spinner").remove();
	}else{
		$(parentId+" .el-spinner").remove();
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
