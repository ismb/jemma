var iFrameOverload = {
	MODULE : "Overload"
}

alert('iFrameOverload');

iFrameOverload.Init = function () {
	$("#iframeOverload").show();

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