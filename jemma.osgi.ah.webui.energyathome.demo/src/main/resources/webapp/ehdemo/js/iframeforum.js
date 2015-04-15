var iFrameForum = {
	MODULE : "Forum",
	forumURL: "http://ehforum.polito.it:8181/ehforum/forums/list.page",
	listEldo: {}
}

iFrameForum.Init = function () {
	if(InterfaceEnergyHome.mode==-2)
	{
		NonDisponibile.GestNDUser();
		return;
	}
	$("#iframeForum").show();
	//$("#iframeForum").css("display","show");
	
	iFrameForum.GetElettrodomestici();
}

iFrameForum.Exit = function () {
	
	if(InterfaceEnergyHome.mode==-2)
	{
		NonDisponibile.ExitND();
		return;
	}
	
	$("#iframeForum iframe").empty();
	$("#iframeForum").html(null);
	$("#iframeForum").hide();
	//$("#iframeForum").css("display","none");
	hideSpinner();
}

iFrameForum.VisIFrame = function () {
	hideSpinner();
	$("#iframeFrm").css("display","block");
}

iFrameForum.GetElettrodomestici = function() {
	if (Main.env == 0) console.log('CostiConsumi1.js', 'GetElettrodomestici', 'Entro!');

	if (InterfaceEnergyHome.mode > 0) {
		try {
			InterfaceEnergyHome.objService.getAppliancesConfigurationsDemo(iFrameForum.DatiElettrodomesticiCB);
		} catch (err) {
			//if (Main.env == 0) console.log('exception in iFrameForum.js - in iFrameForum.GetElettrodomestici method: ', err);
			InterfaceEnergyHome.GestErrorEH("GetMaxElettr", err);
		}
	} else {
		// per test
		var val;
		var indLista = 0;
		if (indLista == 0) {
			val = ListaElettr1;
			indLista = 1;
		} else {
			val = ListaElettr1;
			indLista = 0;
		}
		iFrameForum.DatiElettrodomesticiCB(val, null);
	}

	if (Main.env == 0) console.log('iFrameForum.js', 'GetElettrodomestici', 'Esco!');
}

iFrameForum.DatiElettrodomesticiCB = function(result, err) {
	if (Main.env == 0) console.log('iFrameForum.js', 'DatiElettrodomesticiCB', 'Entro!');

	//CostiConsumi.popSemaphoro('iFrameForum.GetElettrodomestici', 1);
	
	if (err != null){
		//if (Main.env == 0) console.log('exception in iFrameForum.js - in iFrameForum.DatiElettrodomesticiCB method: ', err);
		InterfaceEnergyHome.GestErrorEH("DatiElettrodomestici", err);
	}
	if ((err == null) && (result != null)) {
		$.each(result.list,function(indice, elettrodom) {
			if (elettrodom["map"][InterfaceEnergyHome.ATTR_APP_TYPE] == InterfaceEnergyHome.SMARTINFO_APP_TYPE) {
				Main.appIdSmartInfo = elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID];
			}
		});
	}
	
	if(InterfaceEnergyHome.mode==-2)
	{
		$("#iframeForum").html("<iframe id='iframeFrm' src='"+iFrameForum.forumURL+"' width='100%' height='100%' onload='iFrameForum.VisIFrame()' frameborder='0'>Contenuto alternativo per i browser che non leggono gli iframe.</iframe>");
		return;
	}
	
	var hmac = CryptoJS.algo.HMAC.create(CryptoJS.algo.SHA256, Main.secretPassPhrase);

    hmac.update(Main.appIdSmartInfo);
    hmac.update(Main.hagId);

    var hash = hmac.finalize();
    
    $("#iframeForum").html("<iframe id='iframeFrm' src='"+iFrameForum.forumURL+"?user=" + hash + "' width='100%' height='100%' onload='iFrameForum.VisIFrame()' frameborder='0'>Contenuto alternativo per i browser che non leggono gli iframe.</iframe>");
	showSpinner();
	
	if (Main.env == 0) console.log('iFrameForum.js', 'DatiElettrodomesticiCB', 'Esco!');
}