var indListaElettr = 0;
var ListaElettr =  {"list":[]};

function InquiredDevicesVuoto() {
	this.list = [{"map":{"appliance.pid": "1", "ah.app.name":"Rex", "ah.app.type":"com.indesit.ah.app.whitegood", 
					"ah.category.pid":"2", "ah.location.pid":"2", "availability": 2, "device_state":2,
					 "device_value":{"name":"power", "value": {"timestamp":0, "value":"10"}}}}];
}
 
var InquiredDevicesList = {"list":[{"map":{"appliance.pid": "1", "ah.app.name":"Rex", "ah.app.type":"com.indesit.ah.app.whitegood", 
					"ah.category.pid":"2", "ah.location.pid":"2", "ah.icon": "lavatrice.png", "availability": 2,
					"device_value":{"name":"power", "value": {"timestamp":0, "value":"150"}}}},
				   {"map":{"appliance.pid": "2", "ah.app.name":"Frigorifero", "ah.app.type":"ah.app.frigorifero",  "availability": 2,
					"ah.category.pid":"2", "ah.location.pid": "2"}, "device_value":{"name":"power", "value": {"timestamp":0, "value":"100"}}}
				  ]};

var StatoConnessione = [0, 1, 2, 0, 0, 1, 0, 2, 0];

var ListaCategorie = [{"pid":"1","name":"SmartPlug", "iconName":"smartplug.png"},{"pid":"2","name":"Lavatrice", "iconName":"lavatrice.png"},
			{"pid":"3","name":"Forno", "iconName":"forno.png"},{"pid":"4","name":"Frigorifero", "iconName":"frigorifero.png"}];

var ListaLocazioni = [{"pid":"1","name":"Cucina","iconName":"cucina.png"},{"pid":"2","name":"Bagno","iconName":"bagno.png"},
			{"pid":"3","name":"Soggiorno","iconName":"soggiorno.png"},{"pid":"4","name":"Camera da letto","iconName":"camera.png"}];

