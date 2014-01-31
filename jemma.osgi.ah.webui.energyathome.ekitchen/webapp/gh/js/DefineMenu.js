var DefineMenu = [ {
		"Nome" : Msg.menu["home"],
		"Image" : Define.menu["home"],
		"ImageSelected" : Define.menu["homeSel"],
		"SubMenu": [ {
			"Nome" : Msg.menu["consumi"],
			"Image" : Define.menu["consumi"],
			"ImageSelected" : Define.menu["consumiSel"],
			"Section": Tracing.CONSUMPTIONS,
			"FuncEnter" : "CostiConsumi.GestConsumi()",
			"FuncExit": "CostiConsumi.ExitConsumi()" }, {
			"Nome" : Msg.menu["costi"],
			"Image" : Define.menu["costi"],
			"ImageSelected" : Define.menu["costiSel"],
			"Section": Tracing.COSTS,
			"FuncEnter" : "CostiConsumi.GestCosti()",
			"FuncExit": "CostiConsumi.ExitCosti()" }, {
			"Nome" : Msg.menu["dispositivi"],
			"Image" : Define.menu["dispositivi"],
			"ImageSelected" : Define.menu["dispositiviSel"],
			"Section": Tracing.APPLIANCES,
			"FuncEnter" : "Elettrodomestici.GestElettrodomestici()",
			"FuncExit": "Elettrodomestici.ExitElettrodomestici()" },{
			"Nome" : Msg.menu["storico"],
			"Image" : Define.menu["storico"],
			"ImageSelected" : Define.menu["storicoSel"],
			"Section": Tracing.HISTORY,
			"FuncEnter" : "Storico.GestStorico()",
			"FuncExit": "Storico.ExitStorico()" 
			}  ]},
		/**
		{ 
		"Nome" : Msg.menu["config"],
		"Image" : Define.menu["config"],
		"ImageSelected" : Define.menu["configSel"],
		"SubMenu": [ {
			"Nome" : Msg.menu["confSched"],
			"Image" : Define.menu["confSched"],
			"ImageSelected" : Define.menu["confSchedSel"],
			"FuncEnter" : "NonDisponibile.GestND()",
			"FuncExit": "NonDisponibile.ExitND()"}, {
			"Nome" : Msg.menu["confImpost"],
			"Image" : Define.menu["confImpost"],
			"ImageSelected" : Define.menu["confImpostSel"],
			"FuncEnter" : "NonDisponibile.GestND()",
			"FuncExit": "NonDisponibile.ExitND()"}]},
		**/	
		{ 
		"Nome" : Msg.menu["community"],
		"Image" : Define.menu["community"],
		"ImageSelected" : Define.menu["communitySel"],
		"SubMenu": [{
			"Nome" : Msg.menu["forum"],
		 	"Image" : Define.menu["forum"],
			"ImageSelected" : Define.menu["forumSel"],
			"Section": Tracing.FORUM,
			"FuncEnter" : "Community.GestForum()",
			"FuncExit": "Community.ExitForum()"} ]},
		{ 
		"Nome" : Msg.menu["infotrial"],
		"Image" : Define.menu["infotrial"],
		"ImageSelected" : Define.menu["infotrialSel"],
		"SubMenu": [ {
			"Nome" : Msg.menu["tariffa"],
			"Image" : Define.menu["tariffa"],
			"ImageSelected" : Define.menu["tariffaSel"],
			"Section": Tracing.TARIFF,
			"FuncEnter" : "Trial.GestTariffa()",
			"FuncExit" : "Trial.ExitTariffa()"}, {
			"Nome" : Msg.menu["informazioni"],
			"Image" : Define.menu["informazioni"],
			"ImageSelected" : Define.menu["informazioniSel"],
			"Section": Tracing.INFO,
			"FuncEnter" : "Trial.GestInformazioni()",
			"FuncExit": "Trial.ExitInformazioni()"}, {
			"Nome" : Msg.menu["contatti"],
			"Image" : Define.menu["contatti"],
			"ImageSelected" : Define.menu["contattiSel"],
			"Section": Tracing.CONTACT,
			"FuncEnter" : "Trial.GestContatti()",
			"FuncExit": "Trial.ExitContatti()"}, {
			"Nome" : Msg.menu["questionari"],
			"Image" : Define.menu["questionari"],
			"ImageSelected" : Define.menu["questionariSel"],
			"Section": Tracing.FORMS,
			"FuncEnter" : "NonDisponibile.GestND()",
			"FuncExit": "NonDisponibile.GestND()"}

			]
	}];