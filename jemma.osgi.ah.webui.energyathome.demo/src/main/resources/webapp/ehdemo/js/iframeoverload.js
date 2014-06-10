var iFrameOverload = {
	MODULE : "Overload"
	//TODO: check merge, following 2 lines were not in 3.3.0
//	,linkDemo: './reportDemo/overload.html',
//	linkProduction: 'https://trial.energy-home.it/ehreport/overload.html'
}

iFrameOverload.Init = function () {
	$("#iframeOverload").show();
	//TODO: check merge, following commented block was not in 3.3.0
/*	var powerThreshold = Define.home["contatoreOk"][Main.contatore];
	var enablePV = (Main.enablePV) ? 'yes' : 'no';
	
	var linkToUse = '';
	
	if (InterfaceEnergyHome.mode > 0) {
		linkToUse = iFrameOverload.linkProduction;
	} else {
		linkToUse = iFrameOverload.linkDemo;
	} */
	//TODO: check merge, following line was different in in 3.3.0, the static
	//FIXME: address statically declared here!!!
    //$("#iframeOverload").html("<iframe id='iframeOver' src='"+linkToUse+"?power="+powerThreshold+"&enablePV="+enablePV+"' width='100%' height='100%' onload='iFrameOverload.VisIFrame()' frameborder='0'>Contenuto alternativo per i browser che non leggono gli iframe.</iframe>");
    $("#iframeOverload").html("<iframe id='iframeOver' src='http://10.38.0.1/ehreport/index.html' width='100%' height='100%' onload='iframeOverload.VisIFrame()' frameborder='0'>Contenuto alternativo per i browser che non leggono gli iframe.</iframe>");
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