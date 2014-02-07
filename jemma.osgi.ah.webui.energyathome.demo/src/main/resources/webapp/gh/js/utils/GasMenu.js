var GasMenu = {
	MODULE : "GasMenu",
	tabIndex: 0,
	maxTabIndex: 0,
	MainW : -1, // width div menu principale
	MainH : -1, // height div menu principale
	MainHtml : null,
	ContentW : -1, // width div sub menu
	ContentH : -1, // height div sub menu
	ContentHtml : null,
	sfondoElImg : GasDefine.menu["sfondoButton"],
	MainMenu : GasDefineMenu,
	SubMenuHtml: null

};

/**
 * Inizializza il menu principale e i sottomenu
 * I sottomenu vengono creati tutti all'inizio e poi ne viene visualizzato uno solo per volta
 */
GasMenu.Init = function(mainDiv, contentDiv) 
{
	var dim;
	var num = GasMenu.MainMenu.length;

	GasMenu.MainW  = $("#" + mainDiv).width();
	GasMenu.MainH  = $("#" + mainDiv).height();
	
	MainMenuH = Math.round(GasMenu.MainH / num);
	MainMenuW = Math.round(GasMenu.MainW * 0.9);
	if (MainMenuH > MainMenuW )
		dim = MainMenuW ;
	else
		dim = MainMenuH ;
	dimImage = Math.round(dim * 0.6);
	
	GasMenu.ContentW  = $("#" + contentDiv).width();
	GasMenu.ContentH  = $("#" + contentDiv).height();
	
	dist = Math.round(((GasMenu.MainH - (dim * num)) / (num+1)));
	Log.alert(80, "Menu", "Init: main = " + GasMenu.MainW + " " + GasMenu.MainH + " dim = " + dim + " dimImage = " + dimImage );
	hDiv = Math.round(dim * 0.9);
	extraTopOffset = Math.round(dim * 0.1);
	imgTopOffset = Math.round((hDiv - dimImage)/4);
	imgLeftOffset = Math.round((dim - dimImage)/2);
	hTitle = Math.round(hDiv * 0.1);
	topTitle = imgTopOffset + dimImage;
	
	topOffset = dist;
	leftOffset = Math.round((GasMenu.MainW - MainMenuW) / 2) - (dim * 0.015); // per spostare fuori bordo sx
	
	GasMenu.MainHtml = "";
	for (i = 0; i < num; i++)
	{
		GasMenu.MainHtml = GasMenu.MainHtml +
			"<img id='MainElSfondo' src='" + GasMenu.sfondoElImg + "' style='position:absolute;top:" + topOffset + 
			"px;left:" + leftOffset + "px' + width='" + dim + "px' + height='" + dim + "px'>" +
			"<div class='MainMenuEl' id='MainEl" + i +  "' tabIndex='0' onClick='GasMenu.OnClickMainMenu(" + i + 
			")' width='" + dim + "px' height='" + hDiv + "px' style='position:absolute;top:" + 
			(topOffset+extraTopOffset) + "px;left:" + leftOffset + "px' >" +
			"<img id='MainImg" + i + "' class='MainMenuImg' width='" + dimImage + "px' height='" + dimImage +
			"px' src='" + GasMenu.MainMenu[i].Image + "' style='position:absolute;top:" + imgTopOffset + "px;left:" + 
			imgLeftOffset + "px'><p id='NomeMain" + i + "' class='MainMenuTitle'>" + GasMenu.MainMenu[i].Nome + "</p></div>";

		topOffset = topOffset + dim + dist;
	}
	$("#" + mainDiv).html(GasMenu.MainHtml);
	
	// forzo dimensioni da css altrimenti il titolo ha dimensioni 0
	$(".MainMenuEl").css("width", dim + "px"); 
	$(".MainMenuEl").css("height", hDiv + "px");
	
	GasMenu.InitContentMenu(contentDiv);
	GasMenu.OnClickMainMenu(0);
};


GasMenu.OnClickMainMenu= function(val) {
	// richiamo funzione di Exit per l'elemento che lascio
	oldContent = $(".ContentMenuElSelected").attr("id");
	Log.alert(80, "Menu", "OnClickMainMenu oldContent = " + oldContent);

	if (oldContent != undefined)
	{
		j = 2; // numero main dopo 'el', numero content dopo 'Content'
		k = oldContent.indexOf('Content');
		iMain = parseInt(oldContent.substring(j, k));
		iContent = parseInt(oldContent.substring(k+7));
		exitFunc = GasMenu.MainMenu[iMain].SubMenu[iContent].FuncExit;
		if ((exitFunc != undefined) && (exitFunc != null))
		{
			GasTracing.Trace(GasMenu.MainMenu[iMain].SubMenu[iContent].Section, GasTracing.OUT, null, null);
			eval(exitFunc); // Esegue la funzione di Exit
		}
		// tolgo selezione a elemento content menu: cambio icona a immagine, cambio classe al div
		$("#img" + iMain + "Content" + iContent).attr("src", GasMenu.MainMenu[iMain].SubMenu[iContent].Image);
		$("#el" + iMain + "Content" + iContent).removeClass("ContentMenuElSelected");
	}
	// tolgo selezione a elemento main menu: cambio icona a immagine, cambio classe al div
	mainElId = $(".MainMenuElSelected").attr("id");
	if (mainElId != undefined)
	{
		ind = parseInt(mainElId.substr(6)); // MainEl e' lungo 6
		Log.alert(80, "Menu", "OnClickMainMenu : id = " + mainElId + " ind = " +  parseInt(mainElId.substr(6)));
		$("#MainImg" + ind).attr("src", GasMenu.MainMenu[ind].Image);
		$("#" + mainElId).removeClass("MainMenuElSelected");
		 
	}

	// seleziono nuovo elemento main e visualizzo nuova barra content menu
	$("#MainEl" + val).addClass("MainMenuElSelected");
	$("#MainImg" + val).attr("src", GasMenu.MainMenu[val].ImageSelected);

	$(".visibleDiv").addClass("invisibleDiv");
	$(".visibleDiv").removeClass("visibleDiv");
	$("#ContentMenu" + val).addClass("visibleDiv");
	$("#ContentMenu" + val).removeClass("invisibleDiv");
	// seleziono primo elemento del menu
	$("#el" + val + "Content0").addClass("ContentMenuElSelected");
	$("#img" + val + "Content0").attr("src", GasMenu.MainMenu[val].SubMenu[0].ImageSelected);
	var func = GasMenu.MainMenu[val].SubMenu[0].FuncEnter;
	if ((func != undefined) && (func != null))
	{
		Log.alert(80, "Menu", "OnClickMainMenu enter = " + func);
		GasTracing.Trace(GasMenu.MainMenu[val].SubMenu[0].Section, GasTracing.IN, null, null);
		eval(func); // Esegue la funzione di Enter
	}
	else
		Log.alert(80, "Menu", "OnClickMainMenu enter undefined");

};


/** 
 * Inizializza i sottomenu
 * Ogni sottomenu e' un div. Ogni elemento del sottomenu e' rettangolare con immagine a sinistra e
 * nome a destra. Sono allineati a sinistra e hanno dimensione fissa
 * Li crea tutti ma non ne visualizza nessuno
 */
GasMenu.InitContentMenu = function(contentDiv)
{
	var nMain = GasMenu.MainMenu.length;
	var num = 0;

	GasMenu.SubMenuHtml = "";
	wDiv = Math.round($("#ContentMenu").width() / 5.5);	// max 5 elementi
	hDiv = $("#ContentMenu").height();
	dimImg = Math.round(hDiv * 0.4);
	
	for (i = 0; i < nMain; i++)
	{
		leftOffset = 0;
		num = GasMenu.MainMenu[i].SubMenu.length;
		GasMenu.SubMenuHtml = GasMenu.SubMenuHtml + "<div class='ContentMenuRow' id='ContentMenu" + i + "' >";
				
		// calcolo distanza tra gli elementi
		for (j = 0; j < num; j++)
		{		
			GasMenu.SubMenuHtml = GasMenu.SubMenuHtml + "<div tabindex='0'style='position:absolute;left:" + leftOffset + 
			"px;top:1%' onClick='GasMenu.OnClickContentMenu(" + i + ", " + j + ")' class='ContentMenuEl' id='el" + i + "Content" + j + 
			"' height='" + hDiv + "px' width='" + wDiv + "px'><img  id='img" + i + "Content" + j + "' class='ContentMenuImg' src='" + 
			GasMenu.MainMenu[i].SubMenu[j].Image + "'><div id='NomeContent" + i + "_" + j + "' class='ContentMenuTitle'>" + 
			GasMenu.MainMenu[i].SubMenu[j].Nome + "</div></div>";
			leftOffset += wDiv + 2;
		}
		GasMenu.SubMenuHtml = GasMenu.SubMenuHtml + "</div>";
	}

	$("#" + contentDiv).html(GasMenu.SubMenuHtml);

	// tengo conto della lunghezza dei campi nome per la dimensione del div
	// le icone restano a distanza costante
	
	maxW = 0;
	for (i = 0; i < nMain; i++)
	{
		num = GasMenu.MainMenu[i].SubMenu.length;
		for (j = 0; j < num; j++)
		{
			w = $("#img" + i + "Content" + j).width();
			$("#img" + i + "Content" + j).height(w);
		}
	}
	   // rendo visibile il primo sottomenu
	$("#ContentMenu0").addClass("visibleDiv");
	for (i = 1; i < GasMenu.MainMenu.length; i++)
		$("#ContentMenu"+i).addClass("invisibleDiv");
		
};

GasMenu.OnClickContentMenu = function(valMain, valContent) {
	// richiamo funzione di Exit per l'elemento che lascio
	oldContent = $(".ContentMenuElSelected").attr("id");
	Log.alert(80, "Menu", "OnClickContentMenu oldContent = " + oldContent);
	if (oldContent != undefined)
	{
		j = 2; // numero main dopo 'el', numero content dopo 'Content'
		k = oldContent.indexOf('Content');
		iMain = parseInt(oldContent.substring(j, k));
		iContent = parseInt(oldContent.substring(k+7));
		exitFunc = GasMenu.MainMenu[iMain].SubMenu[iContent].FuncExit;
		if ((exitFunc != undefined) && (exitFunc != null))
		{
			GasTracing.Trace(GasMenu.MainMenu[iMain].SubMenu[iContent].Section, GasTracing.OUT, null, null);
			eval(exitFunc);
		}
		// tolgo selezione a elemento content menu: cambio icona a immagine, cambio classe al div
		$("#img" + iMain + "Content" + iContent).attr("src", GasMenu.MainMenu[iMain].SubMenu[iContent].Image);
		$("#el" + iMain + "Content" + iContent).removeClass("ContentMenuElSelected");
	}
	// seleziono nuovo elemento
	$("#el" + valMain + "Content" + valContent).addClass("ContentMenuElSelected");
	$("#img" + valMain + "Content" + valContent).attr("src", GasMenu.MainMenu[valMain].SubMenu[valContent].ImageSelected);

	func = GasMenu.MainMenu[valMain].SubMenu[valContent].FuncEnter;
	if ((func != undefined) && (func != null))
	{
		Log.alert(80, "Menu", "OnClickContentMenu eval = " + func);
		GasTracing.Trace(GasMenu.MainMenu[valMain].SubMenu[valContent].Section, GasTracing.IN, null, null);
		eval(func);
	}	
	else
		Log.alert(80, "Menu", "OnClickContentMenu func undefined");

};

