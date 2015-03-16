var MIN_DAY = 60 * 24;
//utente
var IDUtente = '0006';

//Perc IAC
var PERCIAC2 = [20, 20, 20, 20, 20, 5];
var PERCIAC = [10, 10, 10, 10, 10, 5];
// costi
var CostoOdierno 	 = [{"list":[0.01, 0.01, 0.01, 0.01, 0.01, 0.02, 0.1, 0.24, 0.13, 0.03, 0.02, 0.03, 0.1, 0.13, 0.17, 0.14, 0.15, 0.18, 0.21, 0.23, 0.21, 0.09, 0.08, 0.01]},
                 	    {"list":[0.01, 0.01, 0.01, 0.01, 0.01, 0.02, 0.2, 0.23, 0.13, 0.04, 0.02, 0.03, 0.11, 0.13, 0.17, 0.14, 0.15, 0.18, 0.21, 0.23, 0.21, 0.09, 0.08, 0.01]},
                 	    {"list":[0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.1, 0.22, 0.13, 0.03, 0.01, 0.01, 0.09, 0.13, 0.17, 0.13, 0.15, 0.18, 0.21, 0.23, 0.21, 0.09, 0.08, 0.01]}];
var indCostoOdierno  = 0;
var CostoMedio 		 =  {"list":[0.01, 0.01, 0.01, 0.01, 0.01, 0.02, 0.1, 0.14, 0.13, 0.03, 0.02, 0.03, 0.1, 0.13, 0.17, 0.14, 0.15, 0.18, 0.21, 0.23, 0.21, 0.09, 0.08, 0.01]};
var CostoPrevisto 	 = 39.5;
var CostoGiornaliero =  {"list":[0.01, 0.01, 0.01, 0.01, 0.01, 0.02, 0.1, 0.14, 0.13, 0.03, 0.02, 0.03, 0.1, 0.13, 0.17, 0.14, 0.15, 0.18, 0.21, 0.23, 0.21, 0.09, 0.08, 0.01]};
//formato dati da verificare
var SuddivisioneCosti = {"map": 
							{"1": {"list":[2.571]},
						     "0": {"list":[17.571]},
						     "2": {"list":[39.571]},
						     "6": {"list":[124.571]},
						     "3": {"list":[0]},
						     "7": {"list":[139.571]},
						     "8": {"list":[224.571]}
						}};
var SuddivisioneConsumi = {"map": 
							{"1": {"list":[440]},
							 "0": {"list":[153256]},
							 "2": {"list":[8056]},
							 "6": {"list":[24057]},
							 "3": {"list":[0]},
							 "7": {"list":[30957]},
							 "8": {"list":[40057]}
						}};
/*var SuddivisioneCosti = {"map": {"1": {"list":[0]},
						   "0": {"list":[0]},
						   "2": {"list":[0]},
						   "6": {"list":[0]},
						   "3": {"list":[0]},
						   "7": {"list":[0]},
						   "8": {"list":[0]}
						}};*/

// consumo
var ConsumoOdierno 		= [{"list":[82, 88, 83, 89, 983, 93, 90, 512, 210, 160, 173, 125, 360, 492, 450, 401, 421, 565, 643, 681, 652, 332, 310, 78]},
                   		   {"list":[85, 85, 88, 89, 93, 93, 90, 432, 210, 160, 123, 125, 360, 492, 450, 401, 421, 635, 643, 681, 652, 332, 310, 78]},
                   		   {"list":[80, 85, 82, 89, 93, 93, 90, 412, 210, 160, 103, 125, 360, 492, 450, 401, 421, 515, 643, 681, 652, 332, 310, 78]}];
var indConsumoOdierno 	= 0;
var ConsumoMedio 		= {"list":[85, 85, 88, 89, 93, 93, 90, 632, 210, 160, 123, 125, 360, 492, 450, 401, 421, 535, 643, 681, 652, 332, 310, 78]};
var ConsumoPrevisto 	= 219300;
//TODO: check merge, different values in 3.3.0, original values commented below
//var ConsumoGiornaliero  = {"list":[85, 85, 88, 89, 93, 93, 209, 1132, 1210, 160, 720, 2325, 3360, 2492, 450, 400, 1421, 535, 1643, 1781, 1852, 332, 789, 78]};
//var EnergiaProdottaGiornalieroSimul  = {"list":[null, null, null, null, null, null, 5, 72, 193, 420, 780, 1600, 1800, 1850, 2000, 1200, 634, 256, 65, 13, null, null, null, null]};
var ConsumoGiornaliero  = {"list":[85, 85, 88, 89, 93, 93, 209, 1132, 1210, 160, 720, 2325, 3360, 2492, 450, 400, 1421, 535, 1643, 2181, 3352, 332, 789, 78]};
var EnergiaProdottaGiornalieroSimul  = {"list":[null, null, null, null, null, null, 5, 72, 193, 420, 780, 1600, 2000, 2000, 2000, 1200, 634, 256, 65, 13, null, null, null, null]};
var EnergiaVendutaGiornalieroSimul  = {"list":[null, null, null, null, null, null, null, null, null, 260, 60, null, null, null, 1550, 800, null, null, null, null, null, null, null, null]};
//TODO: check merge, the variable below was not present in 3.3.0
var PrevisioneEnergiaProdotta = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.123, 0.245, 0.600, 1.224, 1.490, 1.586, 1.401, 1.172, 0.819, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0];
var ConsumoMedioSettimanale = {"list":[85, 85, 88, 89, 93, 93, 90]};
var ForecastGiornaliero = [0,0,0,0,0,0,0.1,0.2,0.5,0.8,1.3,1.6,2,1.8,1.7,1.3,1,0.5,0.3,0,0,0,0];

var PotenzaAttuale = {"value":0};

// storico
var StoricoElettr = [{"nome": "Washing M.", "id" : "id1", "perc": 16}, 
				     {"nome": "Fridge", "id" : "id2", "perc": 36}, 
				     {"nome": "PC Zone", "id" : "id3", "perc": 10}, 
				     {"nome": "Oven", "id" : "id4", "perc": 11}];

/*
var StoricoCostoI = {"list":[0.01, 0.01, 0.01, 0.01, 0.01, 0.02, 0.02, 0.14, 0.07, 0.04, 0.02, 0.03, 0.11, 0.13, 0.17, 0.14, 0.15, 0.18, 0.21, 0.25, 0.21, 0.09, 0.08, 0.01]};
var StoricoConsumoI = {"list":[85, 85, 88, 89, 93, 93, 90, 432, 210, 160, null, null, 360, 192, 450, 401, 421, 635, 643, 681, 652, 332, 310,200]}; 
var StoricoCostoS   = {"list":[2.0, 2.4, 2.7, 1.6, 2.4, 2.1, 2.3]};
var StoricoConsumoS = {"list":[4476, 4558, 8100, 5400, 7200, 6400, 6900]};

var StoricoCostoM = {"list":[2.0, 2.4, 2.7, 1.6, 2.4, 2.1, 2.3, 1.4, 2.2, 2.4, 2.8, 1.6, 2.4, 2.1, 2.3, 1.2, 2.5, 2.4, 2.5, 1.6, 2.4, 2.1, 2.0, 1.4, 2.0, 2.4, 2.2, 1.4, 2.4, 2.1, 2.3]};
var StoricoConsumoM = {"list":[null, null, 8100, 5400, 7200, 6400, 6900, 4900, 6500, 7200, 8300, 5400, 7200, 6400, 6900, 4400, 6900, 7200, 8000, 5400, 7200, 6400, 6200, 4900, 6200, 7200, 7200, 5100, 7200, 6400, 6900]};

var StoricoCostoA = {"list":[30.2, 	32.9, 	41.2, 	34.0, 	33.4, 	36.0, 	29.0, 	28.4, 	42.0,  38.9, 35.0, 43.0, 38.0]};
var StoricoConsumoA = {"list":[167000, 181000, 230300, 190540, 165000, 186900, 159000, 141000, 245000, 209000, 170000, 261000, 205000]}; 
*/
//TODO: check merge, variable below were not in 3.3.0
//var StoricoCostoO =   {"list":[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]};
//var StoricoConsumoO = {"list":[98, 99, 98, 80, 76, 77, 80, 88, 70, 78, 77, 70, 70, 90, 80, 70, 70, 77, 70, 70, 77, 88, 86, 92, 100, 90, 70, 80, null, null, 77, 76, 78, 98, 70, 77, 78, 80, 88, 70, 78, 70, 70, 90, 80, 70, 70, 70, 70, 70, 70, 70, 80, 90, 88, 86, 92, 100, 90, 70, 98, 99, 98, 70, 75, 77, 80, 88, 70, 78, 77, 70, 70, 90, 80, 70, 70, 70, 77, 70, 70, 70, 80, 85, 88, 86, 92, 100, 90, 70]}; 
//var StoricoProduzioneO = {"list":[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]}; 
var StoricoCostoI =   {"list":[0.01, 0.01, 0.01, 0.01, 0.07, 0.04, 0.02, 0.03, 0.11, 0.13, 0.17, 0.12, 0.34, null, null, null, null, null, null, null, null, null, null, null]};
var StoricoConsumoI = {"list":[88, 85, 88, 89, 250, 110, 98, 120, 360, 450, 650, 420, 1100, null, null, null, null, null, null, null, null, null, null, null]}; 
var StoricoProduzioneI = {"list":[0, 0, 0, 0, 0, 0, 0, 0, 0, 45, 650, 1200, 2400, null, null, null, null, null, null, null, null, null, null, null]}; 
var StoricoCostoS   = {"list":[2.0, 2.4, 2.1, 0.5, null, null, null]};
var StoricoConsumoS = {"list":[4476, 4558, 8100, 1100, null, null, null]};
var StoricoProduzioneS = {"list":[2234, 2500, 3400, 4000, null, null, null]};
var StoricoCostoM =   {"list":[2.0, 2.4, 2.1, 2, 3, 2.4, 2.3, 1.4, 2.2, 2.4, 2.8, 1.6, 2.1, 2.2, 2, 2.1, 2.5, 2.6, 1.6, 1.6, null, null, null, null, null, null, null, null, null, null, null]};
var StoricoConsumoM = {"list":[6400, 6400, 8100, 6400, 7200, 6400, 6900, 4900, 6500, 7200, 6700, 6500, 5600, 6400, 6900, 4400, 6900, 7200, 8000, 5600, null, null, null, null, null, null, null, null, null, null, null]}; 
var StoricoProduzioneM = {"list":[3200, 3300, 3200, 4500, 3200, 4400, 1900, 2900, 3500, 4200, 4700, 4500, 4600, 400, 5900, 4400, 2900, 4200, 1000, 600, null, null, null, null, null, null, null, null, null, null, null]}; 
var StoricoCostoA =   {"list":[30.2, 32.5, 33, 34.2, 33.4, 26.0, null, null, null, null, null, null]};
var StoricoConsumoA = {"list":[167000, 141000, 186900, 205000, 165000, 110000, null, null, null, null, null, null]}; 
var StoricoProduzioneA = {"list":[127000, 101000, 146900, 185000, 105000, 10000, null, null, null, null, null, null]}; 

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

var ListaLocazioni = [{"pid":"1","name":"Kitchen","iconName":"cucina.png"},
                      {"pid":"2","name":"Bathroom","iconName":"bagno.png"},
                      {"pid":"3","name":"Living Room","iconName":"soggiorno.png"}, 
                      {"pid":"4","name":"Bed Room","iconName":"camera.png"},
                      {"pid":"5","name":"Other","iconName":"altro.png"}];

var ListaElettr = {"list":[
	{"map":{
		"appliance.pid": "1", 
		"ah.app.name":"Indesit", 
	    "ah.app.type":"com.indesit.ah.app.whitegood",
	    "device_state_avail": "true",
	    "ah.category.pid":"2",
	    "ah.location.pid":"2", 
	    "ah.icon": "lvb1.png", 
	    "availability": 2, 
	    "device_state":1, 
	    "device_value":{
	    	"name":"power", 
	    	"value": {"timestamp":0, "value":"0"}}}},
	{"map":{
		"appliance.pid": "2", 
		"ah.app.name":"Electrolux", 
	    "ah.app.type":"com.indesit.ah.app.whitegood", 
	    "device_state_avail": "true",
	    "ah.category.pid":"2", 
	    "ah.location.pid":"3", 
	    "ah.icon": "lvb2.png",
	    "availability": 0, 
	    "device_state":1, 
	    "device_value":{
	    	"name":"power",
	    	"value": {"timestamp":0, "value":"0"}}}},
	{"map":{
		"appliance.pid": "0", 
		"ah.app.name":"SmartInfo", 
	    "ah.app.type":"it.telecomitalia.ah.zigbee.metering", 
	    "ah.category.pid":"12", 
	    "ah.location.pid":"3", 
	    "ah.icon": "plug.png", 
	    "availability": 0, 
	    "device_state":0, 
	    "device_state_avail": "false",
	    "device_value":{
	    	"name":"power", 
	    	"value": {"timestamp":0, "value":"0"}}}},
	 {"map":{
		"appliance.pid": "6", 
		"ah.app.name":"Lampada", 
		"ah.app.type":"ah.app.lamp", 
	    "device_state_avail": "true",
	    "ah.category.pid":"4", 
	    "ah.location.pid": "1", 
	    "ah.icon": "lampada.png",
	    "availability": 2, 
	    "device_state":1, 
	    "device_value":{
	    	"name":"power", 
	    	"value": {"timestamp":0, "value":"95"}}}}
  ]};

//TODO: check merge general comment: should "appliance.pid" fields below be renamed?
var ListaElettr1 = {"id": -1, 
//TODO: check merge, commented lines below were not in 3.3.0
/*		"list":[
		    {"map":{
		    	"appliance.pid": "ah.app.1234567891230", 
		    	 "ah.app.name":"SmartInfo", 
		    	 "name":"SmartInfo", //for Report Demo 
		    	 "ah.app.type":"it.telecomitalia.ah.zigbee.metering", 
		    	 "ah.category.pid":"12", 
		    	 "categoryEldo":"12", 
		    	 "ah.location.pid":"3", 
		    	 "ah.icon": "plug.png", 
		    	 "availability": 0, 
		    	 "device_state":0,
		    	 "device_state_avail": "false",
		    	 "device_value":{
		    		 "name":"power", 
		    		 "value": {"timestamp":0, "value":"0"}}}},
		    {"map":{
		    	"appliance.pid": "ah.app.1234567891234", 
		    	 "ah.app.name":"SmartInfo PV", 
		    	 "name":"SmartInfo PV", //for Report Demo 
		    	 "ah.app.type":"it.telecomitalia.ah.zigbee.metering", 
		    	 "ah.category.pid":"14", 
		    	 "categoryEldo":"14", 
		    	 "ah.location.pid":"3", 
		    	 "ah.icon": "plug.png", 
		    	 "availability": 0, 
		    	 "device_state":0,
		    	 "device_state_avail": "false",
		    	 "device_value":{
		    		 "name":"power", 
		    		 "value": {"timestamp":0, "value":"0"}}}},
			{"map":{
				"appliance.pid": "ah.app.1234567891231", 
				"ah.app.name":"Frigo",  
				"name":"Frigo", //for Report Demo
				"ah.app.type":"com.indesit.ah.app.whitegood",  
				"device_state_avail": "true",
				"device_state_avail": "true", 
				"ah.category.pid":"2", 
				"categoryEldo":"2", 
				"ah.location.pid":"2", 
				"ah.icon": "frigorifero.png",	
				"availability": 2, 
				"device_state":1, 
				"device_value":{
					"name":"power", 
					"value": {"timestamp":0, "value":"0"}}}},
			{"map":{
				"appliance.pid": "ah.app.1234567891232", 
				"ah.app.name":"TV", 
				"name":"TV",  //for Report Demo
				"ah.app.type":"app.lamp", 
				"device_state_avail": "true", 
				"ah.category.pid":"2", 
				"categoryEldo":"2", 
				"ah.location.pid":"3", 
				"ah.icon": "tv.png", 
				"availability": 2, 
				"device_state":1,
				"device_value":{
					"name":"power", 
					"value": {"timestamp":0, "value":"10"}}}},
			{"map":{
				"appliance.pid": "ah.app.1234567891233", 
				"ah.app.name":"Lavatrice",
				"name":"Lavatrice", //for Report Demo
				"ah.app.type":"com.indesit.ah.app.whitegood", 
				"ah.category.pid":"2", 
				"categoryEldo":"2", 
				"ah.location.pid":"3", 
				"ah.icon": "lvb2.png", 
				"availability": 2, 
				"device_state_avail": "true",
				"device_state":0, 
				"device_value":{
					"name":"power", 
					"value": {"timestamp":0, "value":"4"}}}},
			{"map":{
				"appliance.pid": "ah.app.1234567891236", 
				"ah.app.name":"Micro onde", 
				"name":"Micro onde", //for Report Demo 
				"ah.app.type":"ah.app.lamp",
				"device_state_avail": "true", 
				"ah.category.pid":"4", 
				"categoryEldo":"4", 
				"ah.location.pid": "1", 
				"ah.icon": "microonde.png", 
				"availability": 2, 
				"device_state":0, 
				"device_value":{
					"name":"power", 
					"value": {"timestamp":0, "value":"0"}}}},
			{"map":{
				"appliance.pid": "ah.app.1234567891237", 
				"ah.app.name":"Lampada", 
				"name":"Lampada", //for Report Demo 
				"ah.app.type":"ah.app.lamp",
				"device_state_avail": "true", 
				"ah.category.pid":"4", 
				"categoryEldo":"4", 
				"ah.location.pid": "1", 
				"ah.icon": "lampada.png", 
				"availability": 0, 
				"device_state": 0, 
				"device_value":{
					"name":"power", 
					"value": {"timestamp":0, "value":"0"}}}},
			{"map":{
				"appliance.pid": "ah.app.1234567891238", 
				"ah.app.name":"Forno",
				"name":"Forno", //for Report Demo
				"ah.app.type":"com.indesit.ah.app.whitegood", 
				"device_state_avail": "true",
				"ah.category.pid":"2", 
				"categoryEldo":"2", 
				"ah.location.pid":"3", 
				"ah.icon": "forno.png", 
				"availability": 2, 
				"device_state":0, 
				"device_value":{
					"name":"power", 
					"value": {"timestamp":0, "value":"50"}}}}
		
		  ]};

var ListaElettr2 = {"id": -1, 
*/
					"list":[
					    {"map":{
					    	"appliance.pid": "ah.app.36276195726973103", 
					    	 "ah.app.name":"SmartInfo", 
					    	 "ah.app.type":"it.telecomitalia.ah.zigbee.metering", 
					    	 "ah.category.pid":"12", 
					    	 "ah.location.pid":"2", 
					    	 "ah.icon": "lampada.png", 
					    	 "availability": 0, 
					    	 "device_state":0,
					    	 "device_state_avail": "false",
					    	 "device_value":{
					    		 "name":"power", 
					    		 "value": {"timestamp":0, "value":"0"}}}},
						{"map":{
							"appliance.pid": "ah.app.3781220529323282", 
							"ah.app.name":"TV", 
							"ah.app.type":"app.lamp", 
							"device_state_avail": "true", 
							"ah.category.pid":"4", 
							"ah.location.pid":"6", 
							"ah.icon": "tv.png", 
							"availability": 2, 
							"device_state":1,
							"device_value":{
								"name":"power", 
								"value": {"timestamp":0, "value":"10"}}}},
						{"map":{
							"appliance.pid": "ah.app.3781220529323365", 
							"ah.app.name":"Zona PC",
							"ah.app.type":"it.telecomitalia.ah.zigbee.smartplug", 
							"ah.category.pid":"5", 
							"ah.location.pid":"2", 
							"ah.icon": "pczone.png", 
							"availability": 2, 
							"device_state_avail": "true",
							"device_state":0, 
							"device_value":{
								"name":"power", 
								"value": {"timestamp":0, "value":"4"}}}},
						{"map":{
							"appliance.pid": "ah.app.3521399293210525877", 
							"ah.app.name":"Phon", 
							"ah.app.type":"it.telecomitalia.ah.zigbee.smartplug",
							"device_state_avail": "true", 
							"ah.category.pid":"25", 
							"ah.location.pid": "4", 
							"ah.icon": "plug.png", 
							"availability": 0, 
							"device_state": 0, 
							"device_value":{
								"name":"power", 
								"value": {"timestamp":0, "value":"0"}}}},
						{"map":{
							"appliance.pid": "ah.app.3521399293213672068", 
							"ah.app.name":"Lavatrice",
							"ah.app.type":"com.indesit.ah.app.whitegood", 
							"device_state_avail": "true",
							"ah.category.pid":"11", 
							"ah.location.pid":"0", 
							"ah.icon": "lvb2.png", 
							"availability": 2, 
							"device_state":0, 
							"device_value":{
								"name":"power", 
								"value": {"timestamp":0, "value":"50"}}}}
					
					  ]};

var ReportSim ={
		/* Dato rilevato | Previsione annua | Media community */
		DatiSim : [
		           /* Utilizzo elettrodomestico */      
		           ["<p style='color:green;'>Ad oggi 40 min</p>",
		            "<p style='color:green;'>50 min</p>",
		            "<p style='color:green;'>100 &euro;</p> "],       
		           /*Consumi Fissi*/
		           ["<img src='./Resources/Images/trofeo_primo_.png' alt='Primo in classifica' title='Bravo sei 1&#176; in classifica continua cos&#237;!' />",
		            "<p style='color:green;'>80 mW</p>",
		            "<p style='color:green;'>30 &euro;</p>"],
		           /*Fascia verde*/ 
		           ["<img src='./Resources/Images/MedagliaArgento_.png' alt='Secondo in classifica' title='Sei 2&#176; in classifica !' />",
		            "<p style='color:green;'>60%</p>",
		            "<p style='color:green;'>1000 mila &euro; </p>"],
		           /*CO2*/
		           /* ["<img src='./Resources/Images/albero_piange.png'><p style='color:green;'>Produrrai 2,2 t di CO2",
		            *  "<p style='color:green;'>Ad oggi 15 Kg</p>",
		            *  "<img  src='./Resources/Images/albero.png'><img src='./Resources/Images/albero.png'><img  src='./Resources/Images/albero.png'><img src='./Resources/Images/albero.png'><p style='color:green;'>Piantando 4 nuovi alberelli!</p> "]*/
		           ["<img src='./Resources/Images/cucchiaio_legno_.png' alt='Ultimo in classifica' title='Sei ultimo in classifica' />",
		            "<p style='color:green;'>Ad oggi 15 Kg</p>",
		            "<p style='color:green;'>200 Kg</p> "]
		          ],
		 consumiFissi : "100 mW",
		 consumoAnno : "70% ",
		 standBy : "30 &euro;",
		 C02 : "20 kg",
		 Eldo : "20 min"
		
	
}
//TODO: check merge, simulation date was different in 3.3.0, below old value commented
//var DataSim = new Date (2012,3,25,12,56);
var DataSim = new Date (2012,2,25,22,56);

var NotizieSimul = [
{	description : "Sale al 20,3% la percentuale di elettricit&agrave; convertita da ogni singola cella fotovoltaica. E ora la primatista Suntech punta al",
	link : "http://gogreen.virgilio.it/news/green-design/fotovoltaico-pannello-record-efficienza_6276.html?pmk=rss",
	title : "Fotovoltaico: ecci il pannello con il record di efficienza"
},
{	description : "Un volumetto scaricabile online ricco di consigli utili per risparmiare dai 700 ai 1000 euro all'anno in bolletta con piccoli ...",
	link : "http://gogreen.virgilio.it/news/green-trends/eco-risparmio-arriva-manuale-ridurre-costi-acqua-luce-gas_6274.html?pmk=rss",
	title : "Eco risparmio: arriva il manuale per ridurre i costi di acqua, luce e gas"
},
{	description : "In piazza le associazioni delle rinnovabili. hanno chiesto al governo, come un appello pubblicato sui giornali, di rivedere il ...",
	link : "http://gogreen.virgilio.it/news/green-economy/rinnovabili-mobilitazione-durera_6273.html?pmk=rss",
	title : "Rinnovabili, la mobilitazione partita da Roma e sui giornali durer&agrave;"
},
{	description : "L'appuntamento &egrave; il 28 aprile alle 15 presso i Fori Imperiali. L'obiettivo finale &egrave; quello di ottenere pi&ugrave; sicurezza per i ...",
	link : "http://gogreen.virgilio.it/eventi/salvaciclisti_6272.html?pmk=rss",
	title : "Salvaciclisti"
},
{	description : "A ridosso della decisione itaiana di prorogare o meno la sospensione dell'impiego di alcuni tipi di agrofarmaci, si pubblica la ...",
	link : "http://gogreen.virgilio.it/news/ambiente-energia/pesticidi-api-governo-decide-sospensioni_6271.html?pmk=rss",
	title : "Pesticidi e api: il governo decide sulla sospensione degli agrofarmaci"
},
{	description : "Estrarre lo shale gas, grande alternativa al petrolio in questa fase in cui il prezzo del barile &egrave; caro, genera piccoli sismi ...",
	link : "http://gogreen.virgilio.it/news/ambiente-energia/terremoti-locali-estrazione-scisto_6270.html?pmk=rss",
	title : "Terremoti: a generare quelli locali &egrave; pure l'estrazione dello scisto"
},
{	description : "Confermato il taglio degli incentivi del 32-36% e il registro obbligatorio per gli impianti di potenza superiore ai 12 ...",
	link : "http://gogreen.virgilio.it/news/ambiente-energia/quinto-conto-energia-testo-decreto.html?pmk=rss",
	title : "Quinto conto energia, il testo del decreto"
},
{	description : "Lanciata dalla Philips Usa, fa luce per 60 watt consumando da 10 e tende a durare due decadi. Il prodotto rivoluzionario ...",
	link : "http://gogreen.virgilio.it/news/green-design/lampadina-eco-rivoluzionaria-dura-20-anni-costa-46-euro_6267.html?pmk=rss",
	title : "Lampadina eco: dura 20 anni e consuma poco, ma per ora costa 46 euro"
},
{
	description : "A fronte di una sensibile contrazione del mercato dell'automotive - soprattutto nel comparto delle auto di lusso - aumentano ...",
	link : "http://gogreen.virgilio.it/news/ambiente-energia/ferrari-maserati-garage-25mln-italiani-bici.html?pmk=rss",
	title : "Ferrari e Maserati in garage e 25mln di italiani passano alla bici"
},
{
	description : "Il ministro dell'ambiente ha presentato il piano nazionale antiemissioni di Co2. Carbon tax, 55%, smart grid e smart cities tra ...",
	link : "http://gogreen.virgilio.it/news/green-economy/bonus-55-esteso-2020-piano-clini-presentato-cipe_6263.html?pmk=rss",
	title : "Bonus 55% esteso al 2020. Ecco il piano di Clini presentato al Cipe"
},
];


//this property represents a fake response returned Green@Home, to be used in mode=noservernodev
var AppliancesConfigurationFake={
	    "fixups": [
	               [
	                   [
	                       "list",
	                       4,
	                       "map",
	                       "category"
	                   ],
	                   [
	                       "list",
	                       0,
	                       "map",
	                       "category"
	                   ]
	               ],
	               [
	                   [
	                       "list",
	                       5,
	                       "map",
	                       "category"
	                   ],
	                   [
	                       "list",
	                       0,
	                       "map",
	                       "category"
	                   ]
	               ],
	               [
	                   [
	                       "list",
	                       7,
	                       "map",
	                       "category"
	                   ],
	                   [
	                       "list",
	                       0,
	                       "map",
	                       "category"
	                   ]
	               ],
	               [
	                   [
	                       "list",
	                       8,
	                       "map",
	                       "category"
	                   ],
	                   [
	                       "list",
	                       0,
	                       "map",
	                       "category"
	                   ]
	               ]
	           ],
	           "id": 16,
	           "result": {
	               "javaClass": "java.util.ArrayList",
	               "list": [
	                     {
	                       "map": {
	                           "device_value": {
	                               "javaClass": "java.util.LinkedList",
	                               "list": [
	                                   {
	                                       "name": "IstantaneousDemands",
	                                       "value": {
	                                           "timestamp": 1426004605365,
	                                           "value": 5,
	                                           "javaClass": "org.energy_home.jemma.ah.hac.lib.AttributeValue"
	                                       },
	                                       "javaClass": "org.energy_home.jemma.ah.internal.greenathome.AttributeValueExtended"
	                                   }
	                               ]
	                           },
	                           "category": {
	                               "name": "Indesit Oven",
	                               "iconName": "oven.png",
	                               "javaClass": "org.energy_home.jemma.ah.hac.lib.ext.Category",
	                               "pid": "38"
	                           },
	                           "ah.category.pid": "38",
	                           "appliance.pid": "ah.app.22731331059870400-1",
	                           "ah.location.pid": "7",
	                           "ah.app.type": "org.energy_home.jemma.ah.zigbee.whitegood",
	                           "ah.icon": "forno.png",
	                           "ah.app.name": "Forno",
	                           "ah.app.eps.types": [
	                               "ah.ep.common",
	                               "ah.ep.zigbee.WhiteGoods"
	                           ],
	                           "availability": 2
	                       },
	                       "javaClass": "java.util.Hashtable"
	                   },
	                   {
	                       "map": {
	                           "device_value": {
	                               "javaClass": "java.util.LinkedList",
	                               "list": [
	                                   {
	                                       "name": "IstantaneousDemands",
	                                       "value": {
	                                           "timestamp": 1426004605366,
	                                           "value": 4.800000190734863,
	                                           "javaClass": "org.energy_home.jemma.ah.hac.lib.AttributeValue"
	                                       },
	                                       "javaClass": "org.energy_home.jemma.ah.internal.greenathome.AttributeValueExtended"
	                                   }
	                               ]
	                           },
	                           "category": {
	                               "name": "Indesit Fridge",
	                               "iconName": "frigorifero.png",
	                               "javaClass": "org.energy_home.jemma.ah.hac.lib.ext.Category",
	                               "pid": "39"
	                           },
	                           "ah.category.pid": "39",
	                           "appliance.pid": "ah.app.3521399293213678865-1",
	                           "ah.location.pid": "7",
	                           "ah.app.type": "org.energy_home.jemma.ah.zigbee.whitegood",
	                           "ah.icon": "frigorifero.png",
	                           "ah.app.name": "Frigorifero",
	                           "ah.app.eps.types": [
	                               "ah.ep.common",
	                               "ah.ep.zigbee.WhiteGoods"
	                           ],
	                           "availability": 2
	                       },
	                       "javaClass": "java.util.Hashtable"
	                   },
	                   {
	                       "map": {
	                           "device_value": {
	                               "javaClass": "java.util.LinkedList",
	                               "list": [
	                                   {
	                                       "name": "IstantaneousDemands",
	                                       "value": {
	                                           "timestamp": 1426004605366,
	                                           "value": 4.800000190734863,
	                                           "javaClass": "org.energy_home.jemma.ah.hac.lib.AttributeValue"
	                                       },
	                                       "javaClass": "org.energy_home.jemma.ah.internal.greenathome.AttributeValueExtended"
	                                   }
	                               ]
	                           },
	                           "category": {
	                               "name": "Indesit Washing Machine",
	                               "iconName": "lavatrice.png",
	                               "javaClass": "org.energy_home.jemma.ah.hac.lib.ext.Category",
	                               "pid": "37"
	                           },
	                           "ah.category.pid": "37",
	                           "appliance.pid": "ah.app.3521399293213672255-1",
	                           "ah.location.pid": "7",
	                           "ah.app.type": "org.energy_home.jemma.ah.zigbee.whitegood",
	                           "ah.icon": "lavatrice.png",
	                           "ah.app.name": "Lavatrice",
	                           "ah.app.eps.types": [
	                               "ah.ep.common",
	                               "ah.ep.zigbee.WhiteGoods"
	                           ],
	                           "availability": 2
	                       },
	                       "javaClass": "java.util.Hashtable"
	                   },
	                   {
	                       "map": {
	                           "device_value": {
	                               "javaClass": "java.util.LinkedList",
	                               "list": [
	                                   {
	                                       "name": "IstantaneousDemands",
	                                       "value": {
	                                           "timestamp": 1426004605466,
	                                           "value": 35,
	                                           "javaClass": "org.energy_home.jemma.ah.hac.lib.AttributeValue"
	                                       },
	                                       "javaClass": "org.energy_home.jemma.ah.internal.greenathome.AttributeValueExtended"
	                                   },
	                                   {
	                                       "name": "OnOffState",
	                                       "value": {
	                                           "timestamp": 1426004605466,
	                                           "value": true,
	                                           "javaClass": "org.energy_home.jemma.ah.hac.lib.AttributeValue"
	                                       },
	                                       "javaClass": "org.energy_home.jemma.ah.internal.greenathome.AttributeValueExtended"
	                                   }
	                               ]
	                           },
	                           "ah.category.pid": "4",
	                           "appliance.pid": "ah.app.3521399293210526124-8",
	                           "ah.location.pid": "7",
	                           "ah.app.type": "org.energy_home.jemma.ah.zigbee.smartplug",
	                           "ah.icon": "tv.png",
	                           "ah.app.name": "Televisore",
	                           "ah.app.eps.types": [
	                               "ah.ep.common",
	                               "ah.ep.zigbee.SmartPlug"
	                           ],
	                           "availability": 2
	                       },
	                       "javaClass": "java.util.Hashtable"
	                   },
	                   {
	                       "map": {
	                           "device_value": {
	                               "javaClass": "java.util.LinkedList",
	                               "list": [
	                                   {
	                                       "name": "IstantaneousDemands",
	                                       "value": {
	                                           "timestamp": 1426004605417,
	                                           "value": 153,
	                                           "javaClass": "org.energy_home.jemma.ah.hac.lib.AttributeValue"
	                                       },
	                                       "javaClass": "org.energy_home.jemma.ah.internal.greenathome.AttributeValueExtended"
	                                   }
	                               ]
	                           },
	                           "category": {
	                               "name": "Meter",
	                               "iconName": "meter.png",
	                               "javaClass": "org.energy_home.jemma.ah.hac.lib.ext.Category",
	                               "pid": "12"
	                           },
	                           "ah.category.pid": "12",
	                           "appliance.pid": "ah.app.3521399293210526113-8",
	                           "ah.location.pid": "7",
	                           "ah.app.type": "org.energy_home.jemma.ah.zigbee.metering",
	                           "ah.icon": "plug.png",
	                           "ah.app.name": "Smart Info",
	                           "ah.app.eps.types": [
	                               "ah.ep.common",
	                               "ah.ep.zigbee.MeteringDevice"
	                           ],
	                           "availability": 2
	                       },
	                       "javaClass": "java.util.Hashtable"
	                   },
	                   {
	                       "map": {
	                           "device_value": {
	                               "javaClass": "java.util.LinkedList",
	                               "list": [
	                                        {
	 	                                       "name": "OnOffState",
	 	                                       "value": {
	 	                                           "timestamp": 1426004605466,
	 	                                           "value": true,
	 	                                           "javaClass": "org.energy_home.jemma.ah.hac.lib.AttributeValue"
	 	                                       },
	 	                                       "javaClass": "org.energy_home.jemma.ah.internal.greenathome.AttributeValueExtended"
	 	                                   },
	 	                                  {
	 	                                       "name": "CurrentLevel",
	 	                                       "value": {
	 	                                           "timestamp": 1426004605466,
	 	                                           "value": 200,
	 	                                           "javaClass": "org.energy_home.jemma.ah.hac.lib.AttributeValue"
	 	                                       },
	 	                                       "javaClass": "org.energy_home.jemma.ah.internal.greenathome.AttributeValueExtended"
	 	                                   }
	 	                                  
	                               ]
	                           },
	                           "category": {
	                               "name": "Philps lamp",
	                               "iconName": "lampada.png",
	                               "javaClass": "org.energy_home.jemma.ah.hac.lib.ext.Category",
	                               "pid": "35"
	                           },
	                           "ah.category.pid": "35",
	                           "appliance.pid": "ah.app.3521399290000526176-8",
	                           "ah.location.pid": "7",
	                           "ah.app.type": "org.energy_home.jemma.ah.zigbee.ColorLight",
	                           "ah.icon": "lampada.png",
	                           "ah.app.name": "Philips HUE",
	                           "ah.app.eps.types": [
	                               "ah.ep.common",
	                               "ah.ep.zigbee.ColorLight",
	                               "ah.ep.zigbee.OnOff"
	                           ],
	                           clusters: {
	                        	   "org.energy_home.jemma.ah.cluster.zigbee.general.LevelControlServer":true,
	                        	   "org.energy_home.jemma.ah.cluster.zigbee.general.dimmablelight":true,
	                        	   "org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer":true
	                           },
	                           "availability": 2
	                       },
	                       "javaClass": "java.util.Hashtable"
	                   },
	                   {
	                       "map": {
	                           "device_value": {
	                               "javaClass": "java.util.LinkedList",
	                               "list": [
	                                        {
	 	                                       "name": "CurrentPositionLiftPercentage",
	 	                                       "value": {
	 	                                           "timestamp": 1426004605466,
	 	                                           "value": 200,
	 	                                           "javaClass": "org.energy_home.jemma.ah.hac.lib.AttributeValue"
	 	                                       },
	 	                                       "javaClass": "org.energy_home.jemma.ah.internal.greenathome.AttributeValueExtended"
	 	                                   }
	                               ]
	                           },
	                           "category": {
	                               "name": "Serranda",
	                               "iconName": "windowc.png",
	                               "javaClass": "org.energy_home.jemma.ah.hac.lib.ext.Category",
	                               "pid": "44"
	                           },
	                           "ah.category.pid": "44",
	                           "appliance.pid": "ah.app.3521399290000526178-8",
	                           "ah.location.pid": "7",
	                           "ah.app.type": "org.energy_home.jemma.ah.zigbee.WindowCovering",
	                           "ah.icon": "windowc.png",
	                           "ah.app.name": "Serranda",
	                           "ah.app.eps.types": [
	                               "ah.ep.common",
	                               "ah.ep.zigbee.WindowCovering",
	                           ],
	                           "availability": 2
	                       },
	                       "javaClass": "java.util.Hashtable"
	                   }
	               ]
	           } 
	       };

var updateFakeDeviceValueByNameAndPID = function(valueName,pid,newValue)
{
	var appliances=AppliancesConfigurationFake.result.list;
	for(var i=0;i<appliances.length;i++)
	{
		if(appliances[i].map["appliance.pid"]==pid)
		{
			var values=appliances[i].map.device_value.list;
			for(var j=0;j<values.length;j++)
			{
				if(values[j].name == valueName)
				{
					values[j].value.value=newValue;
					values[j].timestamp=new Date().getTime();
					console.debug(getFakeDeviceValueByPID(pid));
				}
			}
		}
	}
}

var updateFakeDeviceConsumptionByPid = function(pid,consumption)
{
	var oldConsumption=0;
	
	var appliances=AppliancesConfigurationFake.result.list;
	for(var i=0;i<appliances.length;i++)
	{
		if(appliances[i].map["appliance.pid"]==pid)
		{
			var values=appliances[i].map.device_value.list;
			for(var j=0;j<values.length;j++)
			{
				if(values[j].name == "IstantaneousDemands")
				{
					oldConsumption=values[j].value.value;
					values[j].value.value=consumption;
					values[j].timestamp=new Date().getTime();
					console.debug(getFakeDeviceValueByPID(pid));
				}
			}
		}
	}
	
	//now update Smart Info device and fake values from configAdmin (only works at runtime)
	for(var i=0;i<appliances.length;i++)
	{
		if(appliances[i].map["ah.category.pid"]=="12")
		{
			var values=appliances[i].map.device_value.list;
			for(var j=0;j<values.length;j++)
			{
				if(values[j].name == "IstantaneousDemands")
				{
					values[j].value.value+=(consumption-oldConsumption);
					values[j].timestamp=new Date().getTime();
					console.debug(getFakeDeviceValueByPID(pid));
				}
			}
		}
	}
	
	
	$.each(fakeValues.noServerCustomDevice.list, function(indice, elettrodom) {
		if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.SMARTINFO_APP_TYPE) {
			if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_CATEGORY] == "12") {
				elettrodom["map"]["potenza"]=parseInt(elettrodom["map"]["potenza"])+(consumption-oldConsumption);
			}
		}
	});
}

var getFakeDeviceValueByPID = function(pid)
{
	var appliances=AppliancesConfigurationFake.result.list;
	for(var i=0;i<appliances.length;i++)
	{
		if(appliances[i].map["appliance.pid"]==pid)
		{
			return appliances[i];
		}
	}
}