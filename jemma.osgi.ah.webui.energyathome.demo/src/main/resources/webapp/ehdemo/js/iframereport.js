var iFrameReport = {
	MODULE : "Report",
	//TODO: check merge, following commented lines were different in 3.3.0
	//reportURL: "https://trial.energy-home.it/ehreport/report.html",
	//reportURLDEV: "http://163.162.42.30/ehreport/report_dev.html",
	//reportURLDEMO: "./reportDemo/report.html",
	//FIXME: address statically specified here!
	reportURL: "http://163.162.42.30/ehreport/report.html",
	listEldo: {}
}

iFrameReport.Init = function () {
	$("#iframeReport").show();
	iFrameReport.GetElettrodomestici();
}

iFrameReport.gestIFrameURI = function(){

	var arrVars = [];
	var vars = '';
	var iCounter;
	var index = 1;
	//TODO: check merge, following line not in 3.3.0
	//var urlForReport = '';
	
	for (el in iFrameReport.listEldo){
		var appID = iFrameReport.listEldo[el].app_id;
		var iconID = iFrameReport.listEldo[el].icon;
		if (iFrameReport.listEldo[el].cat_id == '12'){
			arrVars[0] = '&a0='+appID.slice(7)+'&i0='+iconID;
		} else {
			arrVars[index] = '&a'+index+'='+appID.slice(7)+'&i'+index+'='+iconID;
			index++;
		}
	}
	if (arrVars[0] == undefined){
		arrVars[0] = '&a0='+0+'&i0='+0;
	}
	for (iCounter = 0; iCounter < arrVars.length; iCounter++){
		vars += arrVars[iCounter];
	}
	iCounter--;
	vars += '&last='+iCounter;
	//TODO: check merge, following commented block was not in 3.3.0
/*	if (developer) {
		urlForReport = iFrameReport.reportURLDEV;
	} else {
		if (InterfaceEnergyHome.mode > 0){
			urlForReport = iFrameReport.reportURL;
		} else {
			urlForReport = iFrameReport.reportURLDEMO;
		}
	}*/
	//TODO: check merge, following line was different in 3.3.0
	//$("#iframeReport").html("<iframe id='iframeRep' src='"+urlForReport+"?hagID="+Main.userId+"&app=a&icon=i" + vars + "' width='100%' height='100%' onload='iFrameReport.VisIFrame()' frameborder='0'>Contenuto alternativo per i browser che non leggono gli iframe.</iframe>");
	//line from 3.3.0 below

    $("#iframeReport").html("<iframe id='iframeRep' src='"+iFrameReport.reportURL+"?hagID="+Main.userId+"&app=a&icon=i" + vars + "' width='100%' height='100%' onload='iFrameReport.VisIFrame()' frameborder='0'>Contenuto alternativo per i browser che non leggono gli iframe.</iframe>");
	showSpinner();
}

iFrameReport.Exit = function () {
	
	$("#iframeReport iframe").empty();
	$("#iframeReport").html(null);
	$("#iframeReport").hide();
	$("#iframeReport").css("display","none");
	hideSpinner();
}

iFrameReport.VisIFrame = function () {
	hideSpinner();
	$("#iframeRep").css("display","block");
}

iFrameReport.GetElettrodomestici = function() {
	if (Main.env == 0) console.log('iFrameReport.js', 'GetElettrodomestici', 'Entro!');

	if (InterfaceEnergyHome.mode > 0) {
		try {
			InterfaceEnergyHome.objService.getAppliancesConfigurationsDemo(iFrameReport.DatiElettrodomesticiCB);
		} catch (err) {
			//if (Main.env == 0) console.log('exception in iFrameReport.js - in iFrameReport.GetElettrodomestici method: ', err);
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
		iFrameReport.DatiElettrodomesticiCB(val, null);
	}

	if (Main.env == 0) console.log('iFrameReport.js', 'GetElettrodomestici', 'Esco!');
}

iFrameReport.DatiElettrodomesticiCB = function(result, err) {
	if (Main.env == 0) console.log('iFrameReport.js', 'DatiElettrodomesticiCB', 'Entro!');

	//CostiConsumi.popSemaphoro('iFrameReport.GetElettrodomestici', 1);
	
	if (err != null){
		//if (Main.env == 0) console.log('exception in iFrameReport.js - in iFrameReport.DatiElettrodomesticiCB method: ', err);
		InterfaceEnergyHome.GestErrorEH("DatiElettrodomestici", err);
	}
	if ((err == null) && (result != null)) {
		$.each(result.list,function(indice, elettrodom) {
			iFrameReport.listEldo[indice] = {'app_id': elettrodom["map"][InterfaceEnergyHome.ATTR_APP_PID], 
											 'cat_id': elettrodom["map"][InterfaceEnergyHome.ATTR_APP_CATEGORY], 
											 'icon': elettrodom["map"][InterfaceEnergyHome.ATTR_APP_ICON]};
		});
	}
	
	iFrameReport.gestIFrameURI();
	
	if (Main.env == 0) console.log('iFrameReport.js', 'DatiElettrodomesticiCB', 'Esco!');
}