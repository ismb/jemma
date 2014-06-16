var iFrameOverload = {
	MODULE : "Overload",
	linkDemo: './reportDemo/overload.html',
	linkProduction: 'https://trial.energy-home.it/ehreport/overload.html'
}

iFrameOverload.Init = function () {
	$("#iframeOverload").show();
	
	var powerThreshold = Define.home["contatoreOk"][Main.contatore];
	var enablePV = (Main.enablePV) ? 'yes' : 'no';
	
	var linkToUse = '';
	
	if (InterfaceEnergyHome.mode > 0) {
		linkToUse = iFrameOverload.linkProduction;
	} else {
		linkToUse = iFrameOverload.linkDemo;
	} 

    $("#iframeOverload").html("<iframe id='iframeOver' src='"+linkToUse+"?power="+powerThreshold+"&enablePV="+enablePV+"' width='100%' height='100%' onload='iFrameOverload.VisIFrame()' frameborder='0'>Contenuto alternativo per i browser che non leggono gli iframe.</iframe>");
	showSpinner();
}

iFrameOverload.Exit = function () {
	
	$("#iframeOverload iframe").empty();
	$("#iframeOverload").html(null);
	$("#iframeOverload").hide();
	hideSpinner();
}

iFrameOverload.VisIFrame = function () {
	hideSpinner();
	$("#iframeOver").css("display","block");
}