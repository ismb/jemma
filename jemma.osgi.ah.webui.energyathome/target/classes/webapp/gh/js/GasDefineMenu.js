/*
 * Structure of the main menu and all sub menus
 */

var GasDefineMenu = [ {
		"Nome" : Msg.menu["home"],
		"Image" : GasDefine.menu["home"],
		"ImageSelected" : GasDefine.menu["homeSel"],
		"SubMenu": [ {
			"Nome" : Msg.menu["consumi"],
			"Image" : GasDefine.menu["consumi"],
			"ImageSelected" : GasDefine.menu["consumiSel"],
			"Section": GasTracing.CONSUMPTIONS,
			"FuncEnter" : "GasCostiConsumi.GestConsumi()",
			"FuncExit": "GasCostiConsumi.ExitConsumi()" }, {
				
			"Nome" : Msg.menu["costi"],
			"Image" : GasDefine.menu["costi"],
			"ImageSelected" : GasDefine.menu["costiSel"],
			"Section": GasTracing.COSTS,
			"FuncEnter" : "GasCostiConsumi.GestCosti()",
			"FuncExit": "GasCostiConsumi.ExitCosti()" }, {
				
			"Nome" : Msg.menu["dispositivi"],
			"Image" : GasDefine.menu["dispositivi"],
			"ImageSelected" : GasDefine.menu["dispositiviSel"],
			"Section": GasTracing.APPLIANCES,
			"FuncEnter" : "Dispositivi.GestDispositivi()",
			"FuncExit": "Dispositivi.ExitDispositivi()" },{
				
			"Nome" : Msg.menu["storico"],
			"Image" : GasDefine.menu["storico"],
			"ImageSelected" : GasDefine.menu["storicoSel"],
			"Section": GasTracing.HISTORY,
			"FuncEnter" : "GasStorico.GestStorico()",
			"FuncExit": "GasStorico.ExitStorico()" 
			}  ]},
		{ 
		"Nome" : Msg.menu["infogas"],
		"Image" : GasDefine.menu["infogas"],
		"ImageSelected" : GasDefine.menu["infogasSel"],
		"SubMenu": [ {
				
			"Nome" : Msg.menu["informazioni"],
			"Image" : GasDefine.menu["informazioni"],
			"ImageSelected" : GasDefine.menu["informazioniSel"],
			"Section": GasTracing.INFO,
			"FuncEnter" : "GasInfo.GestInformazioni()",
			"FuncExit": "GasInfo.ExitInformazioni()"}, { 

			"Nome" : Msg.menu["tariffa"],
			"Image" : GasDefine.menu["tariffa"],
			"ImageSelected" : GasDefine.menu["tariffaSel"],
			"Section": GasTracing.TARIFF,
			"FuncEnter" : "GasInfo.GestTariffa()",
			"FuncExit" : "GasInfo.ExitTariffa()"}, 

			]
	}];