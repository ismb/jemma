var iFrameOverload = {
	MODULE : "Overload"
}

alert('iFrameOverload');

iFrameOverload.Init = function () {
	$("#iframeOverload").show();
	
	var powerThreshold = Define.home["contatoreOk"][Main.contatore];
	var enablePV = (Main.enablePV) ? 'yes' : 'no';

    $("#iframeOverload").html("<iframe id='iframeOver' src='https://trial.energy-home.it/ehreport/overload.html?power="+powerThreshold+"&enablePV="+enablePV+"' width='100%' height='100%' onload='iFrameOverload.VisIFrame()' frameborder='0'>Contenuto alternativo per i browser che non leggono gli iframe.</iframe>");
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