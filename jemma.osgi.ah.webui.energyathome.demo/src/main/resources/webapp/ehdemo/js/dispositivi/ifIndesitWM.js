var ifIndesitWM = {
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
		1 : "Cotton Standard",
		2 : "Synthetics Resistant",
		3 : "Whites",
		4 : "Coloureds",
		5 : "Darks",
		6 : "Shirts",
		7 : "Duvets",
		8 : "Bed and Bath",
		9 : "Rinse",
		10 : "Spin",
		11 : "Anti Allergy",
		12 : "Ultradelicates",
		13 : "Wool Platinum Care",
		14 : "Mix 30",
		15 : "Cotton Standard",
		16 : "Cotton Standard",
		17 : "Cotton Standard",
		18 : "Cotton Standard",
		19 : "Cotton Standard",
		20 : "Cotton Standard",
		21 : "Synthetics Resistant",
		22 : "Synthetics Resistant",
		23 : "Coloureds",
		24 : "Coloureds",
		25 : "Shirts",
		26 : "Shirts",
		27 : "Bed and Bath",
		28 : "Bed and Bath",
		29 : "Spin",
		30 : "Ultradelicates",
		31 : "Cotton Standard",
		32 : "Cotton Standard",
		33 : "Cotton Standard",
		34 : "Cotton Standard",
		35 : "Cotton Standard",
		36 : "Cotton Standard",
		37 : "Cotton Standard",
		38 : "Cotton Standard",
		39 : "Cotton Standard"
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

ifIndesitWM.init = function(_clusters) {

	ifIndesitWM.clusters = _clusters;
	if (ifIndesitWM.clusters != null) {

		if (ifIndesitWM.clusters["org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer"] == true) {

		}
	}
	ifIndesitWM.update(true);
};

ifIndesitWM.update = function(now) {

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
	if (!now && ifIndesitWM.timeout_timer != null) {
		if (t - ifIndesitWM.timeout_timer < ifIndesitWM.UPDATE_FREQ) {
			return;
		}
	}
	if (ifIndesitWM.isBusy) {
		return;
	}
	ifIndesitWM.timeout_timer = t;

	var class_stato = "NP"
	var _stato = "";
	
	
	if(InterfaceEnergyHome.mode==-2)
	{
		ifIndesitWM.cycle = 1;
		s_ciclo = ifIndesitWM.cicli[ifIndesitWM.cycle];
		$(".val_cycle").text("CYCLE: " + s_ciclo);
		
		msg = "60&deg;C";
		$(".val_temperature").html(msg);
		
		_msg = ifIndesitWM.minutesToString(260);
		$(".val_duration").text(_msg);
		
		msg = ifIndesitWM.stati[3];
		$(".val_status").text(msg);
		
		return;
	}

	if (Elettrodomestici.listaElettrodomestici[i].connessione == 2) {
		if (Elettrodomestici.listaElettrodomestici[i].stato == 1) {
			_stato = "ON";
			class_stato = "ON";
			ifIndesitWM.stato = 1;

		} else if (Elettrodomestici.listaElettrodomestici[i].stato == 0) {
			_stato = "OFF";
			class_stato = "OFF";
			ifIndesitWM.stato = 0;
		} else {
			ifIndesitWM.stato = -1;
		}
	} else {
		ifIndesitWM.stato = -1;
	}

	if (class_stato == "NP") {

	}

	if (ifIndesitWM.stato == 0) {

	}
	if (ifIndesitWM.stato == -1) {

	}

	var pid = $("#Interfaccia").data("pid");
	// Aggiorno i valori dello slider e del colore
	if (ifIndesitWM.clusters == null) {
		return;
	}
	if (ifIndesitWM.clusters["org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer"] == true ) { // && ifIndesitWM.stato!=-1

		// Elettrodomestici.addSpinner("#Interfaccia", "#0a0a0a");

		// CICLO
		InterfaceEnergyHome.objService.applianceControlGetCycleTarget0(function(result, err) {
			var s_ciclo = "--";
			if (err != null) {
				ifIndesitWM.update(true);
			} else if (result != null) {
				ifIndesitWM.cycle = result;
				s_ciclo = ifIndesitWM.cicli[result];
			}
			$(".val_cycle").text("CYCLE: " + s_ciclo);
		}, pid);

		// Temperatura
		InterfaceEnergyHome.objService.applianceControlGetTemperatureTarget0(function(result, err) {
			var msg = "--";
			if (err != null) {
				ifIndesitWM.update(true);

			} else if (result != null) {
				if (result < 0) {
					ifIndesitWM.temperature = 0;
					msg = "ERRORE";
				} else {
					ifIndesitWM.temperature = result;
					msg = "" + result + "&deg;C";
				}
			}
			$(".val_temperature").html(msg);
		}, pid);

		// Status
		InterfaceEnergyHome.objService.applianceControlExecSignalState(function(result, err) {
			var msg = "--";
			if (err != null) {
				ifIndesitWM.update(true);
			} else if (result != null) {
				ifIndesitWM.appliance_status = result["map"].ApplianceStatus;
				msg = ifIndesitWM.stati[ifIndesitWM.appliance_status];
				if (ifIndesitWM.appliance_status > 0) {
					msg = ifIndesitWM.stati[ifIndesitWM.appliance_status];
				}
			} else {
				ifIndesitWM.appliance_status = 1;
				msg = ifIndesitWM.stati[ifIndesitWM.appliance_status];
				if (ifIndesitWM.appliance_status > 0) {
					msg = ifIndesitWM.stati[ifIndesitWM.appliance_status];
				}
			}

			$(".val_status").text(msg);

			// Durata -running
			if (ifIndesitWM.appliance_status == 5) {
				InterfaceEnergyHome.objService.applianceControlGetRemainingTime(function(result2, err2) {
					var _msg = "--";
					if (err2 != null) {
						ifIndesitWM.update(true);
					} else if (result2 != null) {
						ifIndesitWM.duration = result2;
						_msg = ifIndesitWM.minutesToString(result2);
					}

					$(".val_duration").text(_msg);
					$(".val_label_duration").text("Remaining time:");
				}, pid);

				if (ifIndesitWM.interval_blink == null) {
					ifIndesitWM.interval_blink = setInterval(
						ifIndesitWM.blink, 2000);
				}
			}
			// Durata -programming
			else /* if (ifIndesitWM.appliance_status==3) */{
				InterfaceEnergyHome.objService.applianceControlGetFinishTime(function(result2, err2) {
					var _msg = "--";
					if (err2 != null) {
						ifIndesitWM.update(true);
					} else if (result2 != null) {
						ifIndesitWM.duration = result2;
						_msg = ifIndesitWM.minutesToString(result2);
					}
					$(".val_label_duration").text("Duration:");
					$(".val_duration").text(_msg);
				}, pid);
				if (ifIndesitWM.interval_blink != null) {
					clearInterval(ifIndesitWM.interval_blink);
					ifIndesitWM.interval_blink = null;
				}
			}
		}, pid);
	}
}

ifIndesitWM.minutesToString = function(val) {
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

ifIndesitWM.blink = function(elem) {

	$(".val_duration").fadeTo(300, 0.3, function() {
		$(".val_duration").fadeTo(300, 1);
	});

}