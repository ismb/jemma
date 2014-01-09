var MIN_DAY = 60 * 24;
// costi
var CostoOdierno = [{"list":[0.01, 0.01, 0.01, 0.01, 0.01, 0.02, 0.1, 0.24, 0.13, 0.03, 0.02, 0.03, 
                           0.1, 0.13, 0.17, 0.14, 0.15, 0.18, 0.21, 0.23, 0.21, 0.09, 0.08, 0.01]},
                    {"list":[0.01, 0.01, 0.01, 0.01, 0.01, 0.02, 0.2, 0.23, 0.13, 0.04, 0.02, 0.03, 
                           0.11, 0.13, 0.17, 0.14, 0.15, 0.18, 0.21, 0.23, 0.21, 0.09, 0.08, 0.01]},
                    {"list":[0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.1, 0.22, 0.13, 0.03, 0.01, 0.01, 
                           0.09, 0.13, 0.17, 0.13, 0.15, 0.18, 0.21, 0.23, 0.21, 0.09, 0.08, 0.01]}];
var indCostoOdierno = 0;
var CostoMedio = {"list":[0.01, 0.01, 0.01, 0.01, 0.01, 0.02, 0.1, 0.14, 0.13, 0.03, 0.02, 0.03, 
                          0.1, 0.13, 0.17, 0.14, 0.15, 0.18, 0.21, 0.23, 0.21, 0.09, 0.08, 0.01]};
var CostoPrevisto = 39.50;
var CostoGiornaliero = {"list":[0.01, 0.01, 0.01, 0.01, 0.01, 0.02, 0.1, 0.14, 0.13, 0.03, 0.02, 0.03, 
                          0.1, 0.13, 0.17, 0.14, 0.15, 0.18, 0.21, 0.23, 0.21, 0.09, 0.08, 0.01]};
//formato dati da verificare
var SuddivisioneCosti = {"map": {"Oven": {"list":[2.571]},
	"Washing M.": {"list":[17.571]},
	"Fridge": {"list":[39.571]},
	"SmartPlug": {"list":[124.571]},
	"Lamp": {"list":[null]},
	"TV": {"list":[8.571]},
	"PC Zone": {"list":[1.571]}}};

// consumo
var ConsumoOdierno = [{"list":[0, 0, 0, 0, 0, 0, 0, 0, 0.14, 0.37, 0.29, 0.27, 
                               0.26, 0.06, 0, 0.25, 0.19, 0, 0.01, 0.24, 0, 0, 0, 0]},
                      {"list":[0, 0, 0, 0, 0, 0, 0, 0, 0.14, 0.37, 0.29, 0.27, 
                               0.26, 0.06, 0, 0.25, 0.19, 0, 0.01, 0.24, 0, 0, 0, 0]},
                      {"list":[0, 0, 0, 0, 0, 0, 0, 0, 0.14, 0.37, 0.29, 0.27, 
                               0.26, 0.06, 0, 0.25, 0.19, 0, 0.01, 0.24, 0, 0, 0, 0]}];
var tsUltimaLettura = "19/04/12 13:00:11";
var indConsumoOdierno = 0;

var ConsumoMedio = {"list":[0, 0, 0, 0, 0, 0, 0, 0, 0.14, 0.37, 0.29, 0.27, 
                            0.26, 0.06, 0, 0.25, 0.19, 0, 0.01, 0.24, 0, 0, 0, 0]};


var ConsumoPrevisto = 4350.000;

var ConsumoGiornaliero = {"list":[0, 0, 0, 0, 0, 0, 0, 0, 0.14, 0.37, 0.29, 0.27, 
                                  0.26, 0.06, 0, 0.25, 0.19, 0, 0.01, 0.24, 0, 0, 0, 0]};

var PotenzaAttuale = {"value":0};
var valPotenzaAttuale = 4304.410;

// --- valori videata COSTI relativi a Ultima bolletta
var costoOdiernoData = "19 Aprile 2012";
var costoEuro = "23.65";
var costoOdiernoFascia1 = 0.03;
var costoOdiernoFascia2 = 0.01;
var costoOdiernoFascia3 = 0.07;
var inizioPeriodoRif = "12/05/2011";
var finePeriodoRif = "26/09/2011";
var letturaFatt = 16123;
var consumoUltimaBoll = 30;
var totFornituraGas = 43.08;
var totOneriDiversi = 0.36;
var totBolletta = totFornituraGas + totOneriDiversi;
var totDaPagare = totBolletta;
var pagataIl = "19/10/2011";

// --- valori di Costo al metro cubo per le fasce orarie
var costoFascia1 = 0.36071400;
var costoFascia2 = 0.38950900;
var costoFascia3 = 0.42185800;

// --- valori videata CONTROLLO DISPOSITIVI
var stForn = "REGOLARE";
var stCald = "OK";
var senCO2 = 380;
var senFugaBatt = 73;
var senCO = 30;
var temper = 19;


// storico
var StoricoElettr = [{"nome": "Washing M.", "id" : "id1", "perc": 16}, 
     {"nome":"Fridge", "id" : "id2", "perc": 36}, 
     {"nome":"PC Zone", "id" : "id3", "perc": 10}, 
     {"nome":"Oven", "id" : "id4", "perc": 11}];

var StoricoCostoI = {"list":[0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.05, 0.14, 0.11, 0.11, 
                             0.11, 0.02, 0.00, 0.10, 0.07, 0.00, 0.01, 0.09, 0.00, 0.00, 0.00, 0.00]};
var StoricoConsumoI = {"list":[0, 0, 0, 0, 0, 0, 0, 0, 0.14, 0.37, 0.29, 0.27, 
                               0.26, 0.06, 0, 0.25, 0.19, 0, 0.01, 0.24, 0, 0, 0, 0]}; 

var StoricoCostoS   = {"list":[0.51, 0.67, 0.84, 0.83, 0.74, 0.75, 0.83]};

var StoricoConsumoS = {"list":[1.310, 1.720, 2.160, 2.120, 1.910, 1.930, 2.140]}; // settimana dal 09/04/12 al 15/04/12 di 26758849

var StoricoCostoM = {"list":[1.22, 0.95, 0.95, 0.90, 0.84, 1.15, 1.22, 1.25,
                             1.11, 1.11, 1.19, 1.09, 1.63, 0.30, 0.96, 0.83,
                             0.76, 0.77, 0.84, 0.83, 0.60, 0.59, 0.55, 0.59,
                             0.54, 0.54, 0.41, 0.37, 0.35, 0.22, 0.20]};
var StoricoConsumoM = {"list":[3.120, 2.450, 2.440, 2.320, 2.150, 2.940, 3.120, 3.200, 
                               2.840, 2.860, 3.050, 2.790, 4.180, 0.760, 2.470, 2.130, 
                               1.990, 1.980, 2.160, 2.130, 1.530, 1.520, 1.410, 1.520,
                               1.390, 1.380, 1.040, 0.950, 0.890, 0.560, 0.510]}; 

var StoricoCostoA = {"list":[58.22, 44.85, 34.62, 2.40, 0.21, 0.00,
                             0.00, 0.00, 0.00, 10.90, 29.21, 48.80, 55.29]};
var StoricoConsumoA = {"list":[149.470, 115.150, 88.890, 6.150, 0.540, 0.000, 
                               0.000, 0.000, 0.000, 27.990, 74.980, 125.280, 141.960]}; 

var indLista = 0;

var SuggerimentiIt = ["Scegli la nuova vantaggiosa offerta interamente da fonti rinnovabili", 
                      "Scegli la nuova vantaggiosa offerta interamente da fonti rinnovabili", 
                      "Scegli la nuova vantaggiosa offerta interamente da fonti rinnovabili", 
                      "Scegli la nuova vantaggiosa offerta interamente da fonti rinnovabili", 
                      "Scegli la nuova vantaggiosa offerta interamente da fonti rinnovabili",
                      "Scegli la nuova vantaggiosa offerta interamente da fonti rinnovabili", 
                      "Scegli la nuova vantaggiosa offerta interamente da fonti rinnovabili", 
                      "Scegli la nuova vantaggiosa offerta interamente da fonti rinnovabili"];

//var SuggerimentiEn = ["Wash at low temperatures", 
//                   "Use more the washing machine in the evening", 
//                    "Exploits the maximum capacity <br>of the washing machine basket", 
//                	"Turn off the oven <br>before the end of cooking", 
//                	"Don't open the oven during the preheating",
//                	"Don't put hot food in the fridge", 
//                	"Adjust the thermostat of the <br>refrigerator from 4 degrees up", 
//                	"Turn off your air-conditioner <br>an hour before you leave the room"];
//
var ListaLocazioni = [{"pid":"1","name":"Kitchen","iconName":"cucina.png"},{"pid":"2","name":"Bathroom","iconName":"bagno.png"},
			{"pid":"3","name":"Living Room","iconName":"soggiorno.png"}, {"pid":"4","name":"Bed Room","iconName":"camera.png"},
			{"pid":"5","name":"Other","iconName":"altro.png"}];

var ListaElettr = {"list":[
	{"map":{"appliance.pid": "1", "ah.app.name":"Indesit", 
	"ah.app.type":"com.indesit.ah.app.whitegood", "device_state_avail": "true",
	"ah.category.pid":"2", "ah.location.pid":"2", "ah.icon": "lvb1.png", "availability": 2, 
	"device_state":1, "device_value":{"name":"power", "value": {"timestamp":0, "value":"150"}}}},
	{"map":{"appliance.pid": "2", "ah.app.name":"Electrolux", 
	"ah.app.type":"com.indesit.ah.app.whitegood", "device_state_avail": "true",
	"ah.category.pid":"2", "ah.location.pid":"3", "ah.icon": "lvb2.png", "availability": 0, 
	"device_state":1, "device_value":{"name":"power", "value": {"timestamp":0, "value":"0"}}}},
	{"map":{"appliance.pid": "0", "ah.app.name":"SmartInfo", 
	"ah.app.type":"it.telecomitalia.ah.zigbee.metering", 
	"ah.category.pid":"2", "ah.location.pid":"3", "ah.icon": "lampada.png", "availability": 0, 
	"device_state":0, "device_state_avail": "false",
	"device_value":{"name":"power", "value": {"timestamp":0, "value":"0"}}}},
	{"map":{"appliance.pid": "6", "ah.app.name":"Lampada", "ah.app.type":"ah.app.lamp", 
	"device_state_avail": "true", "ah.category.pid":"4", "ah.location.pid": "1", 
	"ah.icon": "pczone.png", "availability": 2, "device_state":1, 
	"device_value":{"name":"power", "value": {"timestamp":0, "value":"95"}}}}
  ]};

var ListaElettr1 = {"list":[{"map":{"appliance.pid": "0", "ah.app.name":"SmartInfo", 
	"ah.app.type":"it.telecomitalia.ah.zigbee.metering", "ah.category.pid":"2", 
	"ah.location.pid":"3", "ah.icon": "lampada.png", "availability": 0, "device_state":0,
	"device_state_avail": "false","device_value":{"name":"power", "value": {"timestamp":0, "value":"0"}}}},
	{"map":{"appliance.pid": "1", "ah.app.name":"Indesit", 
	"ah.app.type":"com.indesit.ah.app.whitegood",  "device_state_avail": "true",
	"device_state_avail": "true", "ah.category.pid":"2", "ah.location.pid":"2", 
	"ah.icon": "lvb1.png",	"availability": 2, "device_state":1, 
	"device_value":{"name":"power", "value": {"timestamp":0, "value":"92"}}}},
	{"map":{"appliance.pid": "2", "ah.app.name":"TV", "ah.app.type":"app.lamp", 
	"device_state_avail": "true", "ah.category.pid":"2", "ah.location.pid":"3", 
	"ah.icon": "tv.png", "availability": 2, "device_state":1,
	"device_value":{"name":"power", "value": {"timestamp":0, "value":"10"}}}},
	{"map":{"appliance.pid": "3", "ah.app.name":"Electrolux",
	"ah.app.type":"com.indesit.ah.app.whitegood", "device_state_avail": "true",
	"ah.category.pid":"2", "ah.location.pid":"3", "ah.icon": "lvb2.png", "availability": 2, 
	"device_state":0, "device_value":{"name":"power", "value": {"timestamp":0, "value":"0"}}}},
	{"map":{"appliance.pid": "6", "ah.app.name":"Lampada", "ah.app.type":"ah.app.lamp","device_state_avail": "true", 
	"ah.category.pid":"4", "ah.location.pid": "1", "ah.icon": "lampada.png", "availability": 2, 
	"device_state":1, "device_value":{"name":"power", "value": {"timestamp":0, "value":"115"}}}}
  ]};

var codiceCliente = "00880000049317";
var codicePDR = "01234567890123";
var tipoMeter = "G4";
var numMeter = "26758849";
var tipoContr = "uso domestico";
var dataAtt = "11/11/2009";
var consAnnuo = 12;
var numVerde = "800 478.538";
