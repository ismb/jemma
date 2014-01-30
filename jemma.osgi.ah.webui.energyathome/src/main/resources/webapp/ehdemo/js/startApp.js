if (Main.env == 0) console.log('carico startApp!');
// legge tipo contatore 
//per test
if (InterfaceEnergyHome) {
	if (InterfaceEnergyHome.mode == 0) {
		if (qs.get("report", "") != "") {
			if (DefineMenu){
				DefineMenu[0]["SubMenu"][4]["FuncEnter"] = "LazyScript.load('js/Report.js?201305315125',function(){Report.Init();})";
				DefineMenu[0]["SubMenu"][4]["FuncExit"] = "Report.Exit()";
			}
		}
	}
}
if (Main.enablePV === true){
	InterfaceEnergyHome.GetPowerLimitFotoVoltaico(Main.PowerLimitCbFotoVoltaico);
	//InterfaceEnergyHome.GetPowerLimitRete(Main.PowerLimitCbRete);
} else if (Main.enablePV === false){ 
	InterfaceEnergyHome.GetPowerLimit(Main.PowerLimitCb);
} else {
	if (Main.env == 0) console.log('IMPOSSIBILE PROSEGUIRE!');
}