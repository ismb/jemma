var jsonrpc = null;
var OSGi = {
	http_max_spare : 2,
	servlet : "/energyathome/JSON-RPC"
};

OSGi.getServletURI = function()
{
	return OSGi.servlet;
}

OSGi.find = function (clazz) {
	OSGi.refresh();
	
	var services = null;
	services = new Array();
    try {  	
    	services = jsonrpc.OSGi.find(clazz);
    }
    catch (e) {
    	if (window.console)
    		console.log(e.message);
    }

	return services.list;
};

OSGi.initialize = function () {
	if (jsonrpc == null) {
		jsonrpc = new JSONRpcClient(OSGi.servlet);	
		jsonrpc.http_max_spare = OSGi.http_max_spare;
		JSONRpcClient.toplevel_ex_handler = function (e) {
			if (window.console)
				console.log('eccezione in JSONRpcClient');
		}
	}
}

OSGi.refresh = function () {
	if (jsonrpc != null) {
		delete jsonrpc;
		jsonrpc = null;
	}
	
	OSGi.initialize();
}

OSGi.bind = function (sRef) {
	OSGi.initialize();
	jsonrpc.OSGi.bind(sRef);
	
	var pippo = jsonrpc;
	var pluto = sRef.map['service.id'];
	var ref = jsonrpc[pluto];
	if (ref == undefined) {
		return null;
	}
	return ref;
};

