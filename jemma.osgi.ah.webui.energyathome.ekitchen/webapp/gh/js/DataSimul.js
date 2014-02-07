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
var CostoPrevisto = 39.5;
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
var ConsumoOdierno = [{"list":[82, 88, 83, 89, 983, 93, 90, 512, 210, 160, 173, 125, 
                               360, 492, 450, 401, 421, 565, 643, 681, 652, 332, 310, 78]},
                      {"list":[85, 85, 88, 89, 93, 93, 90, 432, 210, 160, 123, 125, 
                                      360, 492, 450, 401, 421, 635, 643, 681, 652, 332, 310, 78]},
                      {"list":[80, 85, 82, 89, 93, 93, 90, 412, 210, 160, 103, 125, 
                                      360, 492, 450, 401, 421, 515, 643, 681, 652, 332, 310, 78]}];
var indConsumoOdierno = 0;
var ConsumoMedio = {"list":[85, 85, 88, 89, 93, 93, 90, 632, 210, 160, 123, 125, 
                            360, 492, 450, 401, 421, 535, 643, 681, 652, 332, 310, 78]};
var ConsumoPrevisto = 219300;
var ConsumoGiornaliero = {"list":[85, 85, 88, 89, 93, 93, 109, 632, 210, 160, 123, 125, 
                                  360, 492, 450, 401, 421, 535, 643, 681, 652, 332, 310, 78]};

var PotenzaAttuale = {"value":0};

// storico
var StoricoElettr = [{"nome": "Washing M.", "id" : "id1", "perc": 16}, 
     {"nome":"Fridge", "id" : "id2", "perc": 36}, 
     {"nome":"PC Zone", "id" : "id3", "perc": 10}, 
     {"nome":"Oven", "id" : "id4", "perc": 11}];

var StoricoCostoI = {"list":[0.01, 0.01, 0.01, 0.01, 0.01, 0.02, 0.02, 0.14, 0.07, 0.04, 0.02, 0.03, 
                             0.11, 0.13, 0.17, 0.14, 0.15, 0.18, 0.21, 0.25, 0.21, 0.09, 0.08, 0.01]};
var StoricoConsumoI = {"list":[85, 85, 88, 89, 93, 93, 90, 432, 210, 160, null, null, 
                                       360, 1192, 450, 401, 421, 1635, 643, 681, 652, 332, 310, 78]}; 

var StoricoCostoS   = {"list":[2.0, 2.4, 2.7, 1.6, 2.4, 2.1, 2.3]};
var StoricoConsumoS = {"list":[4476, 4558, 8100, 5400, 7200, 6400, 6900]};

var StoricoCostoM = {"list":[2.0, 2.4, 2.7, 1.6, 2.4, 2.1, 2.3, 1.4, 
                      2.2, 2.4, 2.8, 1.6, 2.4, 2.1, 2.3, 1.2, 
                      2.5, 2.4, 2.5, 1.6, 2.4, 2.1, 2.0, 1.4, 
                      2.0, 2.4, 2.2, 1.4, 2.4, 2.1, 2.3]};
var StoricoConsumoM = {"list":[null, null, 8100, 5400, 7200, 6400, 6900, 4900, 
                      6500, 7200, 8300, 5400, 7200, 6400, 6900, 4400, 
                      6900, 7200, 8000, 5400, 7200, 6400, 6200, 4900,
                      6200, 7200, 7200, 5100, 7200, 6400, 6900]}; 

var StoricoCostoA = {"list":[30.2, 	32.9, 	41.2, 	34.0, 	33.4, 	36.0, 	
                             29.0, 	28.4, 	42.0,  38.9, 35.0, 43.0, 38.0]};
var StoricoConsumoA = {"list":[167000, 181000, 230300, 190540, 165000, 186900, 
                               159000, 141000, 245000, 209000, 170000, 261000, 205000]}; 

var indLista = 0;

var SuggerimentiIt = ["Lava a basse temperature", 
                    "Usa di piu' la lavatrice in fascia serale", 
                    "Sfrutta la capienza massima del cestello", 
                    "Spegni il forno prima del termine della cottura", 
                    "Non aprire il forno nel preriscaldamento",
                    "Non introdurre cibi caldi in frigo", 
                    "Regola il termostato del frigo dai 4 gradi in su", 
                    "Spegni il condizionatore <br>un'ora prima di uscire dal locale"];
var SuggerimentiEn = ["Wash at low temperatures", 
                   "Use more the washing machine in the evening", 
                    "Exploits the maximum capacity <br>of the washing machine basket", 
                	"Turn off the oven <br>before the end of cooking", 
                	"Don't open the oven during the preheating",
                	"Don't put hot food in the fridge", 
                	"Adjust the thermostat of the <br>refrigerator from 4 degrees up", 
                	"Turn off your air-conditioner <br>an hour before you leave the room"];

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


