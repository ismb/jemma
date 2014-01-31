var Main = {
	MODULE : "Main",
	SCREEN_LARGE : 1,	 // screen normale PC (oltre 1000x1000)
	SCREEN_MEDIUM : 2, // screen con dim. max sotto 700
	SCREEN_SMALL : 3,  // screen con dim. max sotto 500 
	userAgent : null,
	deviceSize : 0,
	screenRatio : 1,
	screenW : -1,
	screenH : -1,
	contentW : -1,
	contentH : -1,
	headerW : -1,
	headerH : -1,
	dataAttuale : null,
	mesi : ["Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"]
}

Main.onLoad = function() 
{
	var imgW, imgH;

	Log.setLevel(50);
	GestDate.InitActualDate();
	Main.dataAttuale = GestDate.GetActualDate();
	$(document).ready(function() {
		Main.userAgent = navigator.userAgent;

		Main.contentW  = $("#Content").width();
		Main.contentH  = $("#Content").height();
	
		// inizializzo connessione ad AG
		if (InterfaceEnergyHome.Init() == null)
			Log.alert(20, Main.MODULE, "onLoad: errore in connessione servizio");
		Log.alert(80, Main.MODULE, "deviceSize = " + Main.deviceSize + " w = " + Main.screenW + " wC = " + Main.contentW  + " font-size = " + $("*").css("font-size"));

		// imposto dimensioni img nel titolo in modo da mantenere le proporzioni dell'immagine
		Main.headerW = $("#Header").width();
		Main.headerH = $("#Header").height();		
		Log.alert(80, Main.MODULE, "Header w = " + Main.headerW + " h = " + Main.headerH);

		$("#BackgroundImg").height(Main.headerH);
		$("#BackgroundImg").width(Main.headerW);
		imgW = $("#BannerImg").width();
		imgH = $("#BannerImg").height();
		$("#BannerImg").height(Main.headerH );
		$("#BannerImg").width(Main.headerH  * imgW/imgH);
		imgW = $("#PartnerImg").width();
		imgH = $("#PartnerImg").height();
		$("#PartnerImg").height(Main.headerH );
		$("#PartnerImg").width(Main.headerH  * imgW/imgH);
		imgW = $("#PartnerImg").width();
		imgH = $("#PartnerImg").height();
		$("#LogoImg").height(Main.headerH );
		$("#LogoImg").width(Main.headerH  * imgW/imgH);

		Log.alert(80, Main.MODULE, "LogoImg w = " + $("#LogoImg").width() + " h = " + $("#LogoImg").height());
		Menu.Init('MainMenu', 'ContentMenu');

		$("#OraData").html(Main.FormatDate(Main.dataAttuale, 1));
		// ogni minuto aggiungo data manualmente o facendo chiamata ad AG
		timerTimeout = setInterval(function(){$("#OraData").html(Main.FormatDate(Main.dataAttuale), 1);}, 60000);
		
	});
}

Main.onUnload = function()
{
	if (GestDate.timerDate != null)
		clearTimeout(GestDate.timerDate);
}

Main.FormatDate = function(dataIn, tipo)
{
	var d, M, y, h, m;
	var tmp;
	
	d = dataIn.getDate();
	M = dataIn.getMonth();
	y = dataIn.getFullYear();
	h = dataIn.getHours();
	m = dataIn.getMinutes();
	if (m < 10)
		m = "0" + m;
	if (tipo == 1)
		tmp = d + " " + Main.mesi[M] + " " + y + " " + h + ":" + m;
	else
		if (tipo == 2)
			tmp = d + " " + Main.mesi[M] + " " + y;
	Log.alert(80, Main.MODULE, "FormatDate : " + tmp); 
	return tmp;
}



