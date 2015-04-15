var ifLockDoor =  { 
        timeout_timer:null,
        clusters:{},
        UPDATE_FREQ: 30000,
        stato:1, //1=porta chiusa, 2=porta aperta, 0=porta chiusa non a chiave
        DOORLOCK_OPEN_STATE: 2,
        DOORLOCK_CLOSE_STATE: 1,
        DOORLOCK_CLOSEUNLOCK_STATE: 0,
        counterPositionDevice: null
}

ifLockDoor.init=function(clusters, i){  
	
    ifLockDoor.timeout_timer=null;
    ifLockDoor.stato=-1;
    
    var stato;
    var class_stato;
    
    ifLockDoor.counterPositionDevice = i;

    $( "#onoff" ).click(function( event ) {
        event.preventDefault();
        var myId= "#"+$(this).attr("id");
        if (ifLockDoor.stato == ifLockDoor.DOORLOCK_CLOSE_STATE) {
            var pid = $("#Interfaccia").data("pid");
            if (pid == undefined)
                return;
            if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)){
                InterfaceEnergyHome.objService.setDeviceState(function(result, err){
                  
                    if (err!=null) {
                        ifLockDoor.update(ifLockDoor.DOORLOCK_CLOSE_STATE);
                    }else if (result != null) {
                        if (result == true) {
                            ifLockDoor.stato = ifLockDoor.DOORLOCK_OPEN_STATE;
                            stato = "OPEN";
        					class_stato = "OPEN";
                            $(myId).addClass("OPEN");
                            $(myId).removeClass("CLOSE");
                            ifLockDoor.update(ifLockDoor.DOORLOCK_OPEN_STATE); //Icon
                        }
                    }
                    ifLockDoor.timeout_timer = new Date().getTime();
                }, pid, ifLockDoor.DOORLOCK_OPEN_STATE);
            } else{
                ifLockDoor.stato = ifLockDoor.DOORLOCK_OPEN_STATE;
                stato = "OPEN";
				class_stato = "OPEN";
                $(myId).addClass("OPEN");
                $(myId).removeClass("CLOSE");
                ifLockDoor.update(ifLockDoor.DOORLOCK_OPEN_STATE); //Icon
            }
            
        }else if (ifLockDoor.stato == ifLockDoor.DOORLOCK_OPEN_STATE){
            
            var pid = $("#Interfaccia").data("pid");
            if (pid == undefined)
                    return;
            if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)){
                InterfaceEnergyHome.objService.setDeviceState(function(result, err){
                    if (err!=null) {
                        ifLockDoor.update(ifLockDoor.DOORLOCK_OPEN_STATE);
                    }else if (result != null) {
                        if (result == true) {
                            ifLockDoor.stato = ifLockDoor.DOORLOCK_CLOSE_STATE;
                            stato = "OPEN";
        					class_stato = "CLOSE";
                            $(myId).addClass("CLOSE");
                            $(myId).removeClass("OPEN");
                            ifLockDoor.update(ifLockDoor.DOORLOCK_CLOSE_STATE); //Icon
                        }
                    }
                    ifLockDoor.timeout_timer=new Date().getTime();
                }, pid, ifLockDoor.DOORLOCK_CLOSE_STATE);
            } else{
                ifLockDoor.stato = ifLockDoor.DOORLOCK_CLOSE_STATE;
                stato = "CLOSE";
				class_stato = "CLOSE";
                $(myId).addClass("CLOSE");
                $(myId).removeClass("OPEN");
                ifLockDoor.update(ifLockDoor.DOORLOCK_CLOSE_STATE); //Icon
            }
        } else {
        	var pid = $("#Interfaccia").data("pid");
            if (pid == undefined)
                return;
            if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)){
                InterfaceEnergyHome.objService.setDeviceState(function(result, err){
                  
                    if (err!=null) {
                        ifLockDoor.update(ifLockDoor.DOORLOCK_CLOSE_STATE);
                    }else if (result != null) {
                        if (result == true) {
                            ifLockDoor.stato = ifLockDoor.DOORLOCK_OPEN_STATE;
                            stato = "OPEN";
        					class_stato = "OPEN";
                            $(myId).addClass("OPEN");
                            $(myId).removeClass("CLOSE");
                            ifLockDoor.update(ifLockDoor.DOORLOCK_OPEN_STATE); //Icon
                        }
                    }
                    ifLockDoor.timeout_timer = new Date().getTime();
                }, pid, ifLockDoor.DOORLOCK_OPEN_STATE);
            } else{
                ifLockDoor.stato = ifLockDoor.DOORLOCK_OPEN_STATE;
                stato = "OPEN";
				class_stato = "OPEN";
                $(myId).addClass("OPEN");
                $(myId).removeClass("CLOSE");
                ifLockDoor.update(ifLockDoor.DOORLOCK_OPEN_STATE); //Icon
            }
        }
        
        $("#device_" + ifLockDoor.counterPositionDevice).addClass(class_stato);
		$("#device_" + ifLockDoor.counterPositionDevice + " .StatoElettrodomestico .stato").text(stato);
    });
    
    
    $("#onoff").attr('unselectable','on')
        .css({'-moz-user-select':'-moz-none',
              '-moz-user-select':'none',
              '-o-user-select':'none',
              '-khtml-user-select':'none', /* you could also put this in a class */
              '-webkit-user-select':'none',/* and add the CSS class here instead */
              '-ms-user-select':'none',
              'user-select':'none'
    }).bind('selectstart', function(){ return false; });
    
    ifLockDoor.update();
};

ifLockDoor.updateIcon=function(stato){
    if (stato < 0 && stato > 2) {
            stato=null;
    }
    var i= $("#Interfaccia").data("current_index");
    var icona_src= "Resources/Images/Devices/"+Elettrodomestici.getIcon(Elettrodomestici.listaElettrodomestici[i], stato);
    
    $("#Interfaccia .icona .icona-dispositivo").attr("src",icona_src);
    $("#device_" + ifLockDoor.counterPositionDevice + " .IconaElettrodomestico .icona-dispositivo").attr("src", icona_src);
    
    var class_stato="NP";
    
    if (stato == 1) {
            class_stato="CLOSE";
    } else if (stato == 2){
            class_stato="OPEN";
    } else {
            class_stato="CLOSE";
    }
    
    $("#Interfaccia .icona").removeClass("OPEN");
    $("#Interfaccia .icona").removeClass("CLOSE");
    $("#Interfaccia .icona").addClass(class_stato);
}

ifLockDoor.update= function(now){
    var t= new Date().getTime();
    var i= $("#Interfaccia").data("current_index");
    var device_value = Elettrodomestici.listaElettrodomestici[i].lockState;
    var txtValue = null;
    
    if (now != null){
    	if ((now !== true) && (now !== false))
    		device_value = now;
    }
    if (device_value == 2) {
    	txtValue = "OPEN";
    } else if (device_value == 1) {
    	txtValue = "CLOSE";
    } else {
    	txtValue = "CLOSE";
    }
    $("#Interfaccia .StatoElettrodomestico .consumo").text(txtValue);
    $("#Interfaccia .StatoElettrodomestico .posizione_value").text(Elettrodomestici.locazioni[Elettrodomestici.listaElettrodomestici[i].location]);
    
	$("#device_" + ifLockDoor.counterPositionDevice + " .StatoElettrodomestico .stato").text(txtValue);
    
	//Non aggiorno oltre l'interfaccia se passa troppo poco tempo dall'ultimo comando
    if (!now && ifLockDoor.timeout_timer!=null) {
        if (t-ifLockDoor.timeout_timer < ifLockDoor.UPDATE_FREQ) {
            return;
        }
    }
    
    ifLockDoor.timeout_timer=t;
    
    var class_stato="NP"
    var _stato="";
    
    if (Elettrodomestici.listaElettrodomestici[i].connessione==2) {
        //if (Elettrodomestici.listaElettrodomestici[i].device_value == 2){
    	if (device_value == 2){
            _stato="OPEN";
            class_stato="OPEN";
            ifLockDoor.stato=2;
        //} else if (Elettrodomestici.listaElettrodomestici[i].device_value == 1){
        } else if (device_value == 1){
            _stato="CLOSE";
            class_stato="CLOSE";
            ifLockDoor.stato=1;
        } else{
            _stato="OPEN";
            class_stato="OPEN";
            ifLockDoor.stato=0;
        }
    } else {
        _stato="NP";
        class_stato="NP";
    	ifLockDoor.stato=-1;
    }
    $("#Interfaccia #OnOffControl .btnToggle").removeClass("NP");
    $("#Interfaccia #OnOffControl .btnToggle").removeClass("OPEN");
    $("#Interfaccia #OnOffControl .btnToggle").removeClass("CLOSE");
    $("#Interfaccia #OnOffControl .btnToggle").addClass(class_stato);
    if (class_stato=="NP") {
        $("#Interfaccia #OnOffControl .btnToggle").text(_stato);
    }
    
    ifLockDoor.updateIcon(ifLockDoor.stato);
}
