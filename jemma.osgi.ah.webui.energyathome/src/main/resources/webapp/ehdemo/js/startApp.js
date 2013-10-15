if (Main.env == 0) console.log('carico startApp!');
// legge tipo contatore 
if (Main.enablePV === true){
	InterfaceEnergyHome.GetPowerLimitFotoVoltaico(Main.PowerLimitCbFotoVoltaico);
	//InterfaceEnergyHome.GetPowerLimitRete(Main.PowerLimitCbRete);
} else if (Main.enablePV === false){ 
	InterfaceEnergyHome.GetPowerLimit(Main.PowerLimitCb);
} else {
	if (Main.env == 0) console.log('IMPOSSIBILE PROSEGUIRE!');
}