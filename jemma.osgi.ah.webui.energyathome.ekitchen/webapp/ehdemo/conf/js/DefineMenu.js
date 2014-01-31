var DefineMenu = [ {
	"Nome" : Msg.menu["config"],
	"Image" : Define.menu["config"],
	"ImageSelected" : Define.menu["configSel"],
	"SubMenu" : [ {
		"Nome" : Msg.menu["confUtente"],
		"Image" : Define.menu["confUtente"],
		"ImageSelected" : Define.menu["confUtenteSel"],
		"Section" : Tracing.CONFIG_USER,
		"FuncEnter" : "Configurazione.GestUtente()",
		"FuncExit" : "Configurazione.ExitUtente()"
	}, {
		"Nome" : Msg.menu["confDisp"],
		"Image" : Define.menu["confDisp"],
		"ImageSelected" : Define.menu["confDispSel"],
		"Section" : Tracing.CONFIG_APPLIANCES,
		"FuncEnter" : "Configurazione.GestElettrodomestici()",
		"FuncExit" : "Configurazione.ExitElettrodomestici()"
	} ]
} ];
