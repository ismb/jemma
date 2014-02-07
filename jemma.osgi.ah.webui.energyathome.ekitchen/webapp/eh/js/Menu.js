var Menu = {
	MODULE : "Menu",
	tabIndex: 0,
	maxTabIndex: 0,
	MainW : -1, // width div menu principale
	MainH : -1, // height div menu principale
	MainHtml : null,
	ContentW : -1, // width div sub menu
	ContentH : -1, // height div sub menu
	ContentHtml : null,
	MainMenu : [ {
		"Nome" : "Home",
		"Image" : "Resources/Images/menu/principale_bianca.png",
		"ImageSelected" : "Resources/Images/menu/principale_verdech.png",
		"SubMenu": [ {
			"Nome" : "Costi",
			"Image" : "Resources/Images/menu/costo_bianco.png",
			"ImageSelected" : "Resources/Images/menu/costo_verdech.png",
			"FuncEnter" : "CostiConsumi.GestCosti()",
			"FuncExit": "CostiConsumi.ExitCosti()" }, {
			"Nome" : "Consumi",
			"Image" : "Resources/Images/menu/consumi_bianco.png",
			"ImageSelected" : "Resources/Images/menu/consumi_verdech.png",
			"FuncEnter" : "CostiConsumi.GestConsumi()",
			"FuncExit": "CostiConsumi.ExitConsumi()" }, {
			"Nome" : "Elettrodomestici",
			"Image" : "Resources/Images/menu/elettrodomestici_bianca.png",
			"ImageSelected" : "Resources/Images/menu/elettrodomestici_verdech.png",
			"FuncEnter" : "Elettrodomestici.GestElettrodomestici()",
			"FuncExit": "Elettrodomestici.ExitElettrodomestici()" },{
			"Nome" : "Storico",
			"Image" : "Resources/Images/menu/storico_bianca.png",
			"ImageSelected" : "Resources/Images/menu/storico_verdech.png",
			"FuncEnter" : "Storico.GestStorico()",
			"FuncExit": "Storico.ExitStorico()" 
			}  ]},
		{ 
		"Nome" : "Configurazioni",
		"Image" : "Resources/Images/menu/configurazioni_bianca.png",
		"ImageSelected" : "Resources/Images/menu/configurazioni_verdech.png",
		"SubMenu": [ {
			"Nome" : "Elettrodomestici",
			"Image" : "Resources/Images/menu/elettrodomestici_bianca.png",
			"ImageSelected" : "Resources/Images/menu/elettrodomestici_verdech.png",
			"FuncEnter" : "NonDisponibile.GestND()",
			"FuncExit": "NonDisponibile.ExitND()"}, {
			"Nome" : "Impostazioni",
			"Image" : "Resources/Images/menu/impostazioni_bianca.png",
			"ImageSelected" : "Resources/Images/menu/impostazioni_verdech.png",
			"FuncEnter" : "NonDisponibile.GestND()",
			"FuncExit": "NonDisponibile.ExitND()"}]},
		{ 
		"Nome" : "Community",
		"Image" : "Resources/Images/menu/community_bianca.png",
		"ImageSelected" : "Resources/Images/menu/community_verdech.png",
		"SubMenu": [ /**{
			"Nome" : "Registrazione",
			"Image" : "Resources/Images/menu/Registrazione.jpg",
			"ImageSelected" : "Resources/Images/menu/Registrazione.jpg",
			"FuncEnter" : "NonDisponibile.GestND()",
			"FuncExit": "NonDisponibile.ExitND()"}, {
			"Nome" : "Confronti",
			"Image" : "Resources/Images/menu/Confronti.jpg",
			"ImageSelected" : "Resources/Images/menu/Confronti.jpg",
			"FuncEnter" : "NonDisponibile.GestND()",
			"FuncExit": "NonDisponibile.ExitND()"} **/ ]},
		{ 
		"Nome" : "Tutto sul trial",
		"Image" : "Resources/Images/menu/tuttotrial_bianca.png",
		"ImageSelected" : "Resources/Images/menu/tuttotrial_verdech.png",
		"SubMenu": [ {
			"Nome" : "Tariffa",
			"Image" : "Resources/Images/menu/tariffa_bianca.png",
			"ImageSelected" : "Resources/Images/menu/tariffa_verdech.png",
			"FuncEnter" : "Trial.GestTariffa()",
			"FuncExit" : "Trial.ExitTariffa()"}, {
			"Nome" : "Informazioni",
			"Image" : "Resources/Images/menu/infotrial_bianca.png",
			"ImageSelected" : "Resources/Images/menu/infotrial_verdech.png",
			"FuncEnter" : "Trial.GestInformazioni()",
			"FuncExit": "Trial.ExitInformazioni()"}, {
			"Nome" : "Contatti",
			"Image" : "Resources/Images/menu/contatti_bianca.png",
			"ImageSelected" : "Resources/Images/menu/contatti_verdech.png",
			"FuncEnter" : "Trial.GestContatti()",
			"FuncExit": "Trial.ExitContatti()"}, {
			"Nome" : "Questionari",
			"Image" : "Resources/Images/menu/questionari_bianca.png",
			"ImageSelected" : "Resources/Images/menu/questionari_verdech.png",
			"FuncEnter" : "Trial.GestQuestionari()",
			"FuncExit": "Trial.ExitQuestionari()"}

			]
	}],
	SubMenuHtml: null


}

/**
 * Inizializza il menu principale e i sottomenu
 * I sottomenu vengono creati tutti all'inizio e poi ne viene visualizzato uno solo per volta
 */
Menu.Init = function(mainDiv, contentDiv) 
{
	var dim;
	var num = Menu.MainMenu.length;

	Menu.MainW  = $("#" + mainDiv).width();
	Menu.MainH  = $("#" + mainDiv).height();
	
	MainMenuH = Math.round(Menu.MainH /(num + 1));
	MainMenuW = Math.round(Menu.MainW * 0.8);
	if (MainMenuH > MainMenuW )
		dim = MainMenuW ;
	else
		dim = MainMenuH ;
	dimImage = Math.round(dim * 0.6);
	dimImage = Math.round(dim * 0.6);
	
	Menu.ContentW  = $("#" + contentDiv).width();
	Menu.ContentH  = $("#" + contentDiv).height();
	
	dist = Math.floor((Menu.MainH - (dim * num)) / (num+1));
	Log.alert(80, "Menu", "Init: main = " + Menu.MainW + " " + Menu.MainH + " dim = " + dim + " dimImage = " + dimImage );
	topOffset = dist;
	leftOffset = Math.floor((Menu.MainW - MainMenuW) / 2);
	//Menu.MainHtml = "<img id='MainMenuBG' src='Resources/Images/menu/sfondo_main.png' width='" + Menu.MainW + "px' height='" + Menu.MainH + "px'>"; 
	Menu.MainHtml = "";
	for (i = 0; i < num; i++)
	{
		Menu.MainHtml = Menu.MainHtml + "<div class='MainMenuEl' id='MainEl" + i +  "' tabIndex='0' onClick='Menu.OnClickMainMenu(" + i + 
			")' style='position:absolute;top:" + topOffset + "px;left:" + leftOffset + "px' ><img id='MainImg" + i + "' class='MainMenuImg' src='" + 
			//Menu.MainMenu[i].Image + "' width='" + dimImage + "px' height='" + dimImage + "px' style='margin-top:" + 
			Menu.MainMenu[i].Image + "' style='margin-top:" + 
			(Menu.MainH * 0.01) + "px'><p id='NomeMain" + i + "' class='MainMenuTitle'>" + Menu.MainMenu[i].Nome + "</p></div>";
		topOffset = topOffset + dim + dist;
	}
	$("#" + mainDiv).html(Menu.MainHtml);
	// tengo conto della lunghezza dei campi nome per la dimensione del div
	// le icone restano a distanza costante
	maxW = 0;
	for (i = 0; i < Menu.MainMenu.length; i++)
	{	
		w = $("#NomeMain" + i).width();
		if (w > maxW)
			maxW = w;
		nome = $("#NomeMain" + i).text();
		Log.alert(80, "Menu", "NomeMain w = " + w + " nome = " + nome);
		
	}
	if (maxW < dim)
		maxW = dim;
	$(".MainMenuEl").css("width", (maxW+4) + "px"); 
	$(".MainMenuEl").css("height", dim + "px");
	// per screen large aumento font
	
	if (Main.deviceSize == Main.SCREEN_LARGE)
		$(".MainMenuEl").css("font-size", "1em");
	Log.alert(80, "Menu", "Init: mainHtml = " + Menu.MainHtml);
	Menu.InitContentMenu(contentDiv);
	Menu.OnClickMainMenu(0);
}


Menu.OnClickMainMenu= function(val) {
	// richiamo funzione di Exit per l'elemento che lascio
	oldContent = $(".ContentMenuElSelected").attr("id");
	Log.alert(80, "Menu", "OnClickMainMenu oldContent = " + oldContent);

	if (oldContent != undefined)
	{
		j = 2; // numero main dopo 'el', numero content dopo 'Content'
		k = oldContent.indexOf('Content');
		iMain = parseInt(oldContent.substring(j, k));
		iContent = parseInt(oldContent.substring(k+7));
		exitFunc = Menu.MainMenu[iMain].SubMenu[iContent].FuncExit;
		if ((exitFunc != undefined) && (exitFunc != null))
			eval(exitFunc);
		// tolgo selezione a elemento content menu: cambio icona a immagine, cambio classe al div
		$("#img" + iMain + "Content" + iContent).attr("src", Menu.MainMenu[iMain].SubMenu[iContent].Image);
		$("#el" + iMain + "Content" + iContent).removeClass("ContentMenuElSelected");
	}
	// tolgo selezione a elemento main menu: cambio icona a immagine, cambio classe al div
	mainElId = $(".MainMenuElSelected").attr("id");
	if (mainElId != undefined)
	{
		ind = parseInt(mainElId.substr(6)); // MainEl e' lungo 6
		Log.alert(80, "Menu", "OnClickMainMenu : id = " + mainElId + " ind = " +  parseInt(mainElId.substr(6)));
		$("#MainImg" + ind).attr("src", Menu.MainMenu[ind].Image);
		$("#" + mainElId).removeClass("MainMenuElSelected");
		 
	}

	// seleziono nuovo elemento main e visualizzo nuova barra content menu
	$("#MainEl" + val).addClass("MainMenuElSelected");
	$("#MainImg" + val).attr("src", Menu.MainMenu[val].ImageSelected);

	$(".visibleDiv").addClass("invisibleDiv");
	$(".visibleDiv").removeClass("visibleDiv");
	$("#ContentMenu" + val).addClass("visibleDiv");
	$("#ContentMenu" + val).removeClass("invisibleDiv");
	// seleziono primo elemento del menu
	$("#el" + val + "Content0").addClass("ContentMenuElSelected");
	$("#img" + val + "Content0").attr("src", Menu.MainMenu[val].SubMenu[0].ImageSelected);
	func = Menu.MainMenu[val].SubMenu[0].FuncEnter;
	if ((func != undefined) && (func != null))
	{
		Log.alert(80, "Menu", "OnClickMainMenu enter = " + func);
		eval(func);
	}
	else
		Log.alert(80, "Menu", "OnClickMainMenu enter undefined");

}


/** 
 * Inizializza i sottomenu
 * Ogni sottomenu e' un div. Ogni elemento e' quadrato, con un'immagine (quadrata) e un titolo, e viene fissata la distanza tra un elemento 
 * e l'altro in base al numero di elementi. 
 * Li crea tutti ma non ne visualizza nessuno
 */
Menu.InitContentMenu = function(contentDiv)
{
	var nMax = 0, w = 0;
	var dim, spacing;

	// determino la dimensione degli elementi in base all'altezza della riga del menu
	// e al numero mssimo di elementi del menu
	for (i = 0; i < Menu.MainMenu.length; i++)
	{
		if (Menu.MainMenu[i].SubMenu.length > nMax)
			nMax  = Menu.MainMenu[i].SubMenu.length;
	}
	ContentMenuElemW = Math.round((Menu.ContentW - Menu.MainW)  / nMax); // non considero la parte sotto il main menu
	ContentMenuElemH = Math.round(Menu.ContentH * 0.9);

	if (ContentMenuElemH > ContentMenuElemW) 
		dim = ContentMenuElemW;
	else
		dim = ContentMenuElemH;
	
	ContentImageH = Math.round(dim * 0.6);
	ContentImageW = Math.round(dim * 0.6);
	
	Menu.SubMenuHtml = "";
	
	for (i = 0; i < Menu.MainMenu.length; i++)
	{
		num = Menu.MainMenu[i].SubMenu.length;
		//Menu.SubMenuHtml = Menu.SubMenuHtml + "<div class='ContentMenuRow' id='ContentMenu" + i + 
		//	"' ><img id='ContentMenuBG' src='Resources/Images/menu/sfondo_content.png' width='" + Menu.ContentW + "px' height='" + Menu.ContentH + "px'>";
		Menu.SubMenuHtml = Menu.SubMenuHtml + "<div class='ContentMenuRow' id='ContentMenu" + i + "' >";
				
		// calcolo distanza tra gli elementi
		dist = Math.floor(((Menu.ContentW - Menu.MainW) - (dim * num)) / (num+1));
		leftOffset = dist + Menu.MainW; // parto dopo il main menu 
		for (j = 0; j < num; j++)
		{		
			Menu.SubMenuHtml = Menu.SubMenuHtml + "<div tabindex='0'style='position:absolute;left:" + leftOffset + 
				"px;top:1%' onClick='Menu.OnClickContentMenu(" + i + ", " + j + ")' class='ContentMenuEl' id='el" + i + "Content" + j + 
				"' height='" + dim + "px' width='" + dim + "px'><img id='img" + i + "Content" + j + "' class='ContentMenuImg' src='" + 
				//Menu.MainMenu[i].SubMenu[j].Image + "' width='" + ContentImageW + "px' height='" + ContentImageH + "px' style='margin-top:" + 
				Menu.MainMenu[i].SubMenu[j].Image + "' style='margin-top:" + 
				(Menu.MainH * 0.01) + "px'><p id='NomeContent" + i + "_" + j + "' class='ContentMenuTitle'>" + 
				Menu.MainMenu[i].SubMenu[j].Nome + "</p></div>";
			leftOffset = leftOffset + dim + dist;
		}
		Menu.SubMenuHtml = Menu.SubMenuHtml + "</div>";
	}

	$("#" + contentDiv).html(Menu.SubMenuHtml);

	// tengo conto della lunghezza dei campi nome per la dimensione del div
	// le icone restano a distanza costante
	maxW = 0;
	for (i = 0; i < Menu.MainMenu.length; i++)
	{
		num = Menu.MainMenu[i].SubMenu.length;
		for (j = 0; j < num; j++)
		{
			w = $("#NomeContent" + i + "_" + j).width();
			if (w > maxW)
				maxW = w;
			//nome = $("#NomeContent" + i + "_" + j).text();
			//Log.alert(40, "Menu", "NomeContent w = " + w + " nome = " + nome);
		}
	}
	if (maxW < dim)
		maxW = dim;
	$(".ContentMenuEl").css("width", (maxW+4) + "px"); 
	$(".ContentMenuEl").css("height", dim + "px");
      if (Main.deviceSize == Main.SCREEN_LARGE)
		$(".ContentMenuEl").css("font-size", "1em");
	$("#ContentMenu0").addClass("visibleDiv");
	for (i = 1; i < Menu.MainMenu.length; i++)
		$("#ContentMenu"+i).addClass("invisibleDiv");
	
	//Menu.OnClickContentMenu (0, 0);
	ContentImageH = $(".ContentMenuImg").css("height");
	ContentImageW = $(".ContentMenuImg").css("width");
	Log.alert(80, "Menu", "Dim content menu = " + dim + " dimImage = " + ContentImageH);
	Log.alert(80, "Menu", "Init: ContentHtml = " + Menu.SubMenuHtml );

}

Menu.OnClickContentMenu = function(valMain, valContent) {
	// richiamo funzione di Exit per l'elemento che lascio
	oldContent = $(".ContentMenuElSelected").attr("id");
	Log.alert(80, "Menu", "OnClickContentMenu oldContent = " + oldContent);
	if (oldContent != undefined)
	{
		j = 2; // numero main dopo 'el', numero content dopo 'Content'
		k = oldContent.indexOf('Content');
		iMain = parseInt(oldContent.substring(j, k));
		iContent = parseInt(oldContent.substring(k+7));
		exitFunc = Menu.MainMenu[iMain].SubMenu[iContent].FuncExit;
		if ((exitFunc != undefined) && (exitFunc != null))
			eval(exitFunc );
		// tolgo selezione a elemento content menu: cambio icona a immagine, cambio classe al div
		$("#img" + iMain + "Content" + iContent).attr("src", Menu.MainMenu[iMain].SubMenu[iContent].Image);
		$("#el" + iMain + "Content" + iContent).removeClass("ContentMenuElSelected");
	}
	// seleziono nuovo elemento
	$("#el" + valMain + "Content" + valContent).addClass("ContentMenuElSelected");
	$("#img" + valMain + "Content" + valContent).attr("src", Menu.MainMenu[valMain].SubMenu[valContent].ImageSelected);

	func = Menu.MainMenu[valMain].SubMenu[valContent].FuncEnter;
	if ((func != undefined) && (func != null))
	{
		Log.alert(80, "Menu", "OnClickContentMenu eval = " + func);
		eval(func);
	}	
	else
		Log.alert(80, "Menu", "OnClickContentMenu func undefined");

}

