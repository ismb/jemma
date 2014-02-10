var MIN_DAY = 60 * 24;
//utente
var IDUtente = 'DT106';
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
var ConsumoGiornaliero  = {"list":[85, 85, 88, 89, 93, 93, 209, 1132, 1210, 160, 720, 2325, 3360, 2492, 450, 400, 1421, 535, 1643, 2181, 3352, 332, 789, 78]};
var EnergiaProdottaGiornalieroSimul  = {"list":[null, null, null, null, null, null, 5, 72, 193, 420, 780, 1600, 2000, 2000, 2000, 1200, 634, 256, 65, 13, null, null, null, null]};
var EnergiaVendutaGiornalieroSimul  = {"list":[null, null, null, null, null, null, null, null, null, 260, 60, null, null, null, 1550, 800, null, null, null, null, null, null, null, null]};

var PotenzaAttuale = {"value":0};

// storico
var StoricoElettr = [{"nome": "Washing M.", "id" : "id1", "perc": 16}, 
				     {"nome": "Fridge", "id" : "id2", "perc": 36}, 
				     {"nome": "PC Zone", "id" : "id3", "perc": 10}, 
				     {"nome": "Oven", "id" : "id4", "perc": 11}];

var ReportServiceDemo = {weekly_min_power: "175", 
						 yearly_min_power_cost: "296.29", 
						 min_power_rank: "29", 
						 f23_rank: "23", 
						 f23_percentage: "123", 
						 yearly_cost_forecast: "568.302", 
						 yearly_consumption_forecast: "2955540", 
						 weekly_avg_min_power: "70.2353", 
						 weekly_avg_f23_percentage: "68.7174", 
						 settPrecDate: [["13","01","2014"],["19","01","2014"]]};
 
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
		"ah.app.name":"Lavastoviglie", 
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
		"ah.app.name":"Lavatrice", 
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

var ListaElettr1 = {"id": -1, 
		"list":[
		    {"map":{
		    	"appliance_pid": "ah.app.1234567891230", 
		    	 "ah.app.name":"SmartInfo", 
		    	 "name":"SmartInfo", //for Report Demo 
		    	 "ah.app.type":"it.telecomitalia.ah.zigbee.metering", 
		    	 "ah.category.pid":"12", 
		    	 "category_pid":"12", 
		    	 "ah.location.pid":"3", 
		    	 "ah.icon": "plug.png", 
		    	 "availability": 0, 
		    	 "device_state":0,
		    	 "device_state_avail": "false",
		    	 "device_value":{
		    		 "name":"power", 
		    		 "value": {"timestamp":0, "value":"0"}}}},
		    {"map":{
		    	"appliance_pid": "ah.app.1234567891237", 
		    	 "ah.app.name":"SmartInfo PV", 
		    	 "name":"SmartInfo PV", //for Report Demo 
		    	 "ah.app.type":"it.telecomitalia.ah.zigbee.metering", 
		    	 "ah.category.pid":"14", 
		    	 "category_pid":"14", 
		    	 "ah.location.pid":"3", 
		    	 "ah.icon": "plug.png", 
		    	 "availability": 0, 
		    	 "device_state":0,
		    	 "device_state_avail": "false",
		    	 "device_value":{
		    		 "name":"power", 
		    		 "value": {"timestamp":0, "value":"0"}}}},
			{"map":{
				"appliance_pid": "ah.app.1234567891236", 
				"ah.app.name":"Frigo",  
				"name":"Frigo", //for Report Demo
				"ah.app.type":"com.indesit.ah.app.whitegood",  
				"device_state_avail": "true",
				"device_state_avail": "true", 
				"ah.category.pid":"2", 
				"category_pid":"2", 
				"ah.location.pid":"2", 
				"ah.icon": "frigorifero.png",	
				"availability": 2, 
				"device_state":1, 
				"device_value":{
					"name":"power", 
					"value": {"timestamp":0, "value":"0"}}}},
			{"map":{
				"appliance_pid": "ah.app.1234567891231", 
				"ah.app.name":"TV", 
				"name":"TV",  //for Report Demo
				"ah.app.type":"app.lamp", 
				"device_state_avail": "true", 
				"ah.category.pid":"2", 
				"category_pid":"2", 
				"ah.location.pid":"3", 
				"ah.icon": "tv.png", 
				"availability": 2, 
				"device_state":1,
				"device_value":{
					"name":"power", 
					"value": {"timestamp":0, "value":"10"}}}},
			{"map":{
				"appliance_pid": "ah.app.1234567891232", 
				"ah.app.name":"Lavatrice",
				"name":"Lavatrice", //for Report Demo
				"ah.app.type":"com.indesit.ah.app.whitegood", 
				"ah.category.pid":"2", 
				"category_pid":"2", 
				"ah.location.pid":"3", 
				"ah.icon": "lvb2.png", 
				"availability": 2, 
				"device_state_avail": "true",
				"device_state":0, 
				"device_value":{
					"name":"power", 
					"value": {"timestamp":0, "value":"4"}}}},
			{"map":{
				"appliance_pid": "ah.app.1234567891233", 
				"ah.app.name":"Micro onde", 
				"name":"Micro onde", //for Report Demo 
				"ah.app.type":"ah.app.lamp",
				"device_state_avail": "true", 
				"ah.category.pid":"4", 
				"category_pid":"4", 
				"ah.location.pid": "1", 
				"ah.icon": "microonde.png", 
				"availability": 2, 
				"device_state":0, 
				"device_value":{
					"name":"power", 
					"value": {"timestamp":0, "value":"0"}}}},
			{"map":{
				"appliance_pid": "ah.app.1234567891234", 
				"ah.app.name":"Lampada", 
				"name":"Lampada", //for Report Demo 
				"ah.app.type":"ah.app.lamp",
				"device_state_avail": "true", 
				"ah.category.pid":"4", 
				"category_pid":"4", 
				"ah.location.pid": "1", 
				"ah.icon": "lampada.png", 
				"availability": 0, 
				"device_state": 0, 
				"device_value":{
					"name":"power", 
					"value": {"timestamp":0, "value":"0"}}}},
			{"map":{
				"appliance_pid": "ah.app.1234567891235", 
				"ah.app.name":"Forno",
				"name":"Forno", //for Report Demo
				"ah.app.type":"com.indesit.ah.app.whitegood", 
				"device_state_avail": "true",
				"ah.category.pid":"2", 
				"category_pid":"2", 
				"ah.location.pid":"3", 
				"ah.icon": "forno.png", 
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
		 IAC : "19%",
		 Eldo : "20 min"
		
	
}

var DataSim = new Date (2012,2,25,22,56);

var NotizieSimul = [
{	description : "Sale al 20,3% la percentuale di elettricit� convertita da ogni singola cella fotovoltaica. E ora la primatista Suntech punta al �",
	link : "http://gogreen.virgilio.it/news/green-design/fotovoltaico-pannello-record-efficienza_6276.html?pmk=rss",
	title : "Fotovoltaico: ecco il pannello con il record di efficienza"
},
{	description : "Un volumetto scaricabile online ricco di consigli utili per risparmiare dai 700 ai 1000 euro all'anno in bolletta con piccoli ...",
	link : "http://gogreen.virgilio.it/news/green-trends/eco-risparmio-arriva-manuale-ridurre-costi-acqua-luce-gas_6274.html?pmk=rss",
	title : "Eco risparmio: arriva il manuale per ridurre i costi di acqua, luce e gas"
},
{	description : "In piazza le associazioni delle rinnovabili. hanno chiesto al governo, come un appello pubblicato sui giornali, di rivedere il ...",
	link : "http://gogreen.virgilio.it/news/green-economy/rinnovabili-mobilitazione-durera_6273.html?pmk=rss",
	title : "Rinnovabili, la mobilitazione partita da Roma e sui giornali durer�"
},
{	description : "L'appuntamento � il 28 aprile alle 15 presso i Fori Imperiali. L'obiettivo finale � quello di ottenere pi� sicurezza per i ...",
	link : "http://gogreen.virgilio.it/eventi/salvaciclisti_6272.html?pmk=rss",
	title : "Salvaciclisti"
},
{	description : "A ridosso della decisione itaiana di prorogare o meno la sospensione dell'impiego di alcuni tipi di agrofarmaci, si pubblica la ...",
	link : "http://gogreen.virgilio.it/news/ambiente-energia/pesticidi-api-governo-decide-sospensioni_6271.html?pmk=rss",
	title : "Pesticidi e api: il governo decide sulla sospensione degli agrofarmaci"
},
{	description : "Estrarre lo shale gas, grande alternativa al petrolio in questa fase in cui il prezzo del barile � caro, genera piccoli sismi ...",
	link : "http://gogreen.virgilio.it/news/ambiente-energia/terremoti-locali-estrazione-scisto_6270.html?pmk=rss",
	title : "Terremoti: a generare quelli locali � pure l'estrazione dello scisto"
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
}];

var autoConsumptionReportService = [{
									  hag_id: "hag-0103",
									  autocons_rate: "42.24",
									  autocons_rank: "6",
									  weekly_mean_autoconsumption_rate: "41.67",
									  autocons_energy: "20.19"}];

var energyBalanceReportService = [{
									hag_id: "hag-0103",
									autocons_rate: "42.24",
									autocons_energy: "20.19",
									sold_energy: "27.61",
									buy_energy: "77.79",
									prod_energy: "47.8",
									used_energy: "97.98",
									observed_hours: "168"}];

var applianceReportServiceDemo = [{ mean_daily_on_time: null,
									category_pid: "11",
									appl_ep_resource_id: "3295",
									appliance_pid: "ah.app.1234567891232",
									weekly_standby_consumption: null,
									weekly_avg_min_power: null,
									yearly_standby_cost: null,
									f23_percentage: "72.99",
									f23_rank: "6",
									weekly_avg_f23_percentage: "58.3",
									yearly_cost_forecast: "67.86"},
								{
									mean_daily_on_time: null,
									category_pid: "8",
									appl_ep_resource_id: "3288",
									appliance_pid: "ah.app.1234567891233",
									weekly_standby_consumption: null,
									weekly_avg_min_power: null,
									yearly_standby_cost: null,
									f23_percentage: "66.38",
									f23_rank: "6",
									weekly_avg_f23_percentage: "70.7",
									yearly_cost_forecast: "62.91"},
								{
									mean_daily_on_time: null,
									category_pid: "4",
									appl_ep_resource_id: "3293",
									appliance_pid: "ah.app.1234567891234",
									weekly_standby_consumption: null,
									weekly_avg_min_power: null,
									yearly_standby_cost: null,
									f23_percentage: "60.64",
									f23_rank: "4",
									weekly_avg_f23_percentage: "72.7",
									yearly_cost_forecast: "116.54"},
								{
									mean_daily_on_time: null,
									category_pid: "6",
									appl_ep_resource_id: "3286",
									appliance_pid: "ah.app.1234567891235",
									weekly_standby_consumption: null,
									weekly_avg_min_power: null,
									yearly_standby_cost: null,
									f23_percentage: "75.64",
									f23_rank: "4",
									weekly_avg_f23_percentage: "74",
									yearly_cost_forecast: "23.64"},
								{
									mean_daily_on_time: null,
									category_pid: "5",
									appl_ep_resource_id: "3290",
									appliance_pid: "ah.app.1234567891236",
									weekly_standby_consumption: null,
									weekly_avg_min_power: null,
									yearly_standby_cost: null,
									f23_percentage: "93.71",
									f23_rank: "3",
									weekly_avg_f23_percentage: "65",
									yearly_cost_forecast: "37.82"},
								{
									mean_daily_on_time: null,
									category_pid: "27",
									appl_ep_resource_id: "3291",
									appliance_pid: "ah.app.1234567891237",
									weekly_standby_consumption: null,
									weekly_avg_min_power: null,
									yearly_standby_cost: null,
									f23_percentage: "61.78",
									f23_rank: "3",
									weekly_avg_f23_percentage: "64",
									yearly_cost_forecast: "14.32"},
								{
									mean_daily_on_time: null,
									category_pid: "6",
									appl_ep_resource_id: "3286",
									appliance_pid: "ah.app.1234567891238",
									weekly_standby_consumption: null,
									weekly_avg_min_power: null,
									yearly_standby_cost: null,
									f23_percentage: "75.64",
									f23_rank: "4",
									weekly_avg_f23_percentage: "74",
									yearly_cost_forecast: "23.64"}];
