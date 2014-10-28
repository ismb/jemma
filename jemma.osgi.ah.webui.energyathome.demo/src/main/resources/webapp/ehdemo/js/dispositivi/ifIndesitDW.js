var ifIndesitDW = {
	isBusy : false,
	timeout_timer : null,
	interval_blink : null,
	clusters : {},
	UPDATE_FREQ : 10000,
	duration : 0,
	temperature : 0,
	spin : 0,
	appliance_status : 0,
	consumption : 0,
	power : 0,
	cycle : 0,
	cicli : {
		0 : "--",
		1 : "Auto Super Wash",
		2 : "Normale",
		3 : "Daily a 60'",
		4 : "Ammollo",
		5 : "Eco 50°",
		6 : "Speed 25'",
		7 : "Cristalli",
		8 : "Baby Cycle",
		9 : "Auto Duo Wash",
		10 : "Ultra Intensivo",
		11 : "Ciclo Smart",
		12 : "Self Care"
	},
	stati : {
		0 : "--",
		1 : "Off",
		2 : "Standby",
		3 : "Programming",
		4 : "Delay",
		5 : "Running",
		6 : "Pause",
		7 : "End",
		8 : "Fault"
	},
	stato : 1
// 1=acceso;0=spento;-1=disconesso
}

ifIndesitDW.init = function(_clusters) {

	ifIndesitDW.clusters = _clusters;
	if (ifIndesitDW.clusters != null) {

		if (ifIndesitDW.clusters["org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer"] == true) {

		}
	}
	ifIndesitDW.update(true);
};

ifIndesitDW.update = function(now) {

	var t = new Date().getTime();
	var i = $("#Interfaccia").data("current_index");
	var device = Elettrodomestici.listaElettrodomestici[i];
	var consumo = Elettrodomestici.listaElettrodomestici[i].consumo;
	if (consumo != "n.a.") {
		consumo = Math.round(Elettrodomestici.listaElettrodomestici[i].consumo) + "W";
	}
	$("#Interfaccia .StatoElettrodomestico .consumo").text(consumo);
	$("#Interfaccia .StatoElettrodomestico .posizione_value").text(Elettrodomestici.locazioni[device.location]);

	// Non aggiorno oltre l'interfaccia se passa troppo poco tempo dall'ultimo
	// comando
	if (!now && ifIndesitDW.timeout_timer != null) {
		if (t - ifIndesitDW.timeout_timer < ifIndesitDW.UPDATE_FREQ) {
			return;
		}
	}
	if (ifIndesitDW.isBusy) {
		return;
	}
	ifIndesitDW.timeout_timer = t;

	var class_stato = "NP"
	var _stato = "";

	if (Elettrodomestici.listaElettrodomestici[i].connessione == 2) {
		if (Elettrodomestici.listaElettrodomestici[i].stato == 1) {
			_stato = "ON";
			class_stato = "ON";
			ifIndesitDW.stato = 1;

		} else if (Elettrodomestici.listaElettrodomestici[i].stato == 0) {
			_stato = "OFF";
			class_stato = "OFF";
			ifIndesitDW.stato = 0;
		} else {
			ifIndesitDW.stato = -1;
		}
	} else {
		ifIndesitDW.stato = -1;
	}

	if (class_stato == "NP") {

	}

	if (ifIndesitDW.stato == 0) {

	}
	if (ifIndesitDW.stato == -1) {

	}

	var pid = $("#Interfaccia").data("pid");
	// Aggiorno i valori dello slider e del colore
	if (ifIndesitDW.clusters == null) {
		return;
	}
	if (ifIndesitDW.clusters["org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer"] == true ) { // && ifIndesitDW.stato!=-1

		// CICLO
		InterfaceEnergyHome.objService.applianceControlGetCycleTarget0(function(result, err) {
			var s_ciclo = "--";
			if (err != null) {
				ifIndesitDW.update(true);
			} else if (result != null) {
				ifIndesitDW.cycle = result;
				s_ciclo = ifIndesitDW.cicli[result];
			}
			$(".val_cycle").text("CYCLE: " + s_ciclo);
		}, pid);

		// Temperatura
		InterfaceEnergyHome.objService.applianceControlGetTemperatureTarget0(function(result, err) {
			var msg = "--";
			if (err != null) {
				ifIndesitDW.update(true);

			} else if (result != null) {
				if (result < 0) {
					ifIndesitDW.temperature = 0;
					msg = "ERRORE";
				} else {
					ifIndesitDW.temperature = result;
					msg = "" + result + "°C";
				}
			}
			$(".val_temperature").html(msg);
		}, pid);

		// Status
		InterfaceEnergyHome.objService.applianceControlExecSignalState(function(result, err) {
			var msg = "--";
			if (err != null) {
				ifIndesitDW.update(true);
			} else if (result != null) {
				ifIndesitDW.appliance_status = result["map"].ApplianceStatus;
				msg = ifIndesitDW.stati[ifIndesitDW.appliance_status];
				if (ifIndesitDW.appliance_status > 0) {
					msg = ifIndesitDW.stati[ifIndesitDW.appliance_status];
				}
			}

			$(".val_status").text(msg);

			// Durata -running
			if (ifIndesitDW.appliance_status == 5) {
				InterfaceEnergyHome.objService.applianceControlGetRemainingTime(function(result2, err2) {
					var _msg = "--";
					if (err2 != null) {
						ifIndesitDW.update(true);
					} else if (result2 != null) {
						ifIndesitDW.duration = result2;
						_msg = ifIndesitDW.minutesToString(result2);
					}

					$(".val_duration").text(_msg);
					$(".val_label_duration").text("Remaining time:");
				}, pid);

				if (ifIndesitDW.interval_blink == null) {
					ifIndesitDW.interval_blink = setInterval(ifIndesitDW.blink, 2000);
				}
			}
			// Durata -programming
			else /* if (ifIndesitDW.appliance_status==3) */{
				InterfaceEnergyHome.objService.applianceControlGetFinishTime(function(result2, err2) {
					var _msg = "--";
					if (err2 != null) {
						ifIndesitDW.update(true);
					} else if (result2 != null) {
						ifIndesitDW.duration = result2;
						_msg = ifIndesitDW.minutesToString(result2);
					}
					$(".val_label_duration").text("Duration:");
					$(".val_duration").text(_msg);
				}, pid);
				if (ifIndesitDW.interval_blink != null) {
					clearInterval(ifIndesitDW.interval_blink);
					ifIndesitDW.interval_blink = null;
				}
			}
		}, pid);
	}
}

ifIndesitDW.minutesToString = function(val) {
	if (val == 0) {
		return "00:00";
	}
	if (val < 0) {
		return "00:00";
	}

	var h = (val >> 8);
	var m = val - ((val >> 6) << 6);

	var sh = "" + h;
	if (h < 10)
		sh = "0" + sh;

	var sm = "" + m;
	if (m < 10)
		sm = "0" + sm;

	return sh + ":" + sm;
}

ifIndesitDW.blink = function(elem) {

	$(".val_duration").fadeTo(300, 0.3, function() {
		$(".val_duration").fadeTo(300, 1);
	});

}