var Registrazione = {
	MODULE : "Registrazione"
}

Registrazione.Init = function () {
	$("#Registra").show();
	/*$("#Content").append("<iframe id='iframeReg' src='PROXY/changepwd_form.php' height='90%' onload = 'Registrazione.VisIFrame()' frameborder='0'>Contenuto alternativo per i browser che non leggono gli iframe.</iframe>");*/
    $("#Registra").html("<iframe id='iframeReg' src='/esp/registerUser' height='90%' onload = 'Registrazione.VisIFrame()' frameborder='0'>Contenuto alternativo per i browser che non leggono gli iframe.</iframe>");
	showSpinner();
}

Registrazione.Exit = function () {
	
	$("#Registra iframe").empty();
	$("#Registra").html(null);
	$("#Registra").hide();
	hideSpinner();
}

Registrazione.VisIFrame = function () {
	hideSpinner();
	$("#iframeReg").css("display","block");
}