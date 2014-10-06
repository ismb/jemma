var ifWindowCovering =  { 
        timeout_timer: null,
        clusters: {},
        UPDATE_FREQ: 30000,
        min: null,
        max: 100,
        liftPerc: 50,
        isBusy: false,
        stato:6, //6 = finestra aperta, 7 = finestra chiusa, 8 = finestra semi aperta
        WINDOWCOVERING_CURRENTPOSITIONLIFTPERCENTAGE: 0,
        WINDOWCOVERING_INFOINSTALLEDOPENLIMIT: 1,
        WINDOWCOVERING_INFOINSTALLEDCLOSEDLIMIT: 2,
        WINDOWCOVERING_INFOINSTALLEDOPENLIMITTILT: 3,
        WINDOWCOVERING_INFOINSTALLEDCLOSEDLIMITTILT: 4,
        WINDOWCOVERING_STOPPED: 5,
        WINDOWCOVERING_UPOPEN: 6,
        WINDOWCOVERING_DOWNCLOSE: 7,
        WINDOWCOVERING_OPENPECENTAGE: 8,
        counterPositionDevice: null,
}

ifWindowCovering.init=function(clusters, i){  
	
    ifWindowCovering.timeout_timer=null;
    ifWindowCovering.stato =-1;

    var pid = $("#Interfaccia").data("pid");
    
    ifWindowCovering.counterPositionDevice = i;

    if (InterfaceEnergyHome.mode > 0){
	    InterfaceEnergyHome.objService.getDeviceState(function(result, err){
	    	if (err != null){
	    		ifWindowCovering.max = 100;
	    	} else {
	        	ifWindowCovering.max = result;
	    	}
	    	ifWindowCovering.gestValue();
	    }, pid, ifWindowCovering.WINDOWCOVERING_INFOINSTALLEDCLOSEDLIMIT);
    } else {
    	ifWindowCovering.max = 100;
    	ifWindowCovering.gestValue();
    }
}

ifWindowCovering.gestLiftPerc=function(){  

    var pid = $("#Interfaccia").data("pid");

    if (InterfaceEnergyHome.mode > 0){
	    InterfaceEnergyHome.objService.getDeviceState(function(result, err){
	    	if (err != null){
	    		ifWindowCovering.liftPerc = 100;
	    	} else {
	        	ifWindowCovering.liftPerc = result;
	    	}
	    	ifWindowCovering.gestSlider();
	    }, pid, ifWindowCovering.WINDOWCOVERING_CURRENTPOSITIONLIFTPERCENTAGE);
    } else {
    	ifWindowCovering.liftPerc = 100;
    	ifWindowCovering.gestSlider();
    }
}

ifWindowCovering.gestSlider=function(){  

    var pid = $("#Interfaccia").data("pid");
    
    var stato;
    var class_stato;
    
    $( "#lum" ).slider({
        range: "min",
        value: ifWindowCovering.liftPerc,
        min: 0,
        max: ifWindowCovering.max,
        slide: function( event, ui ) {
            
            if (ui.value == ifWindowCovering.max) {
                $("#onoff").addClass("ON");
                $("#onoff").removeClass("OFF");
                ifWindowCovering.stato = 1;
            }else if (ui.value == 0 ) {
                $("#onoff").addClass("OFF");
                $("#onoff").removeClass("ON");
                ifWindowCovering.stato = 0;
            }
            ifWindowCovering.lum=ui.value;
            $('#lum_perc').html(ifWindowCovering.lum+"%");
        },
        start: function(event, ui ){
        	ifWindowCovering.isBusy=true;
        },
        stop: function(event, ui ){
        	ifWindowCovering.isBusy=false;
            
            var pid = $("#Interfaccia").data("pid");
            var value = Math.round(ifWindowCovering.lum);
            ifWindowCovering.liftPerc = value;
            InterfaceEnergyHome.objService.setDeviceState(function(result, err){
            	ifWindowCovering.stato = 8;
                if (err == null) {
                	ifWindowCovering.update(true, true);        
                }
                    
            }, pid, WINDOWCOVERING_OPENPECENTAGE, value);
        }
    });
    
    $("#btnCloseW").click(function( event ) {
        event.preventDefault();
        if (InterfaceEnergyHome.mode > 0){
            InterfaceEnergyHome.objService.setDeviceState(function(result, err){
            	alert("I'm closing");
            	ifWindowCovering.stato = 8;
                ifWindowCovering.liftPerc = 100;
            	ifWindowCovering.update(true, false);
            }, pid, WINDOWCOVERING_DOWNCLOSE, null);
        } else {
        	alert("I'm closing");
        	ifWindowCovering.stato = 8;
            ifWindowCovering.liftPerc = 100;
        	ifWindowCovering.update(true, false);
        }
    });
    
    $("#btnOpenW").click(function( event ) {
        event.preventDefault();
        if (InterfaceEnergyHome.mode > 0){
            InterfaceEnergyHome.objService.setDeviceState(function(result, err){
            	alert("I'm opening");
            	ifWindowCovering.stato = 6;
                ifWindowCovering.liftPerc = 0;
            	ifWindowCovering.update(true, false);
            }, pid, WINDOWCOVERING_UPOPEN, null);
        } else {
        	alert("I'm opening");
        	ifWindowCovering.stato = 6;
            ifWindowCovering.liftPerc = 0;
        	ifWindowCovering.update(true, false);
        }
    });
    
    $("#btnStopW").click(function( event ) {
        event.preventDefault();
        if (InterfaceEnergyHome.mode > 0){
            InterfaceEnergyHome.objService.setDeviceState(function(result, err){
            	alert("I'm stopped");
            	ifWindowCovering.stato = 7;
                ifWindowCovering.liftPerc = null;
            	ifWindowCovering.update(true, false);
            }, pid, WINDOWCOVERING_STOPPED, null);
        } else {
        	alert("I'm stopped");
        	ifWindowCovering.stato = 7;
            ifWindowCovering.liftPerc = null;
        	ifWindowCovering.update(true, false);
        }
    });
    
    ifWindowCovering.update(true, false);
};

ifWindowCovering.updateIcon=function(){

    var i= $("#Interfaccia").data("current_index");
    var icona_src= "Resources/Images/Devices2/"+Elettrodomestici.getIcon(Elettrodomestici.listaElettrodomestici[i], ifWindowCovering.stato);
    
    $("#Interfaccia .icona .icona-dispositivo").attr("src",icona_src);
    $("#device_" + ifWindowCovering.counterPositionDevice + " .IconaElettrodomestico .icona-dispositivo").attr("src",icona_src);
    
    var class_stato="NP";
    
    if (ifWindowCovering.stato == 7) {
            class_stato="CLOSE";
    } else if (ifWindowCovering.stato == 6){
            class_stato="OPEN";
    } else {
            class_stato="OPEN";
    }
    
    $("#Interfaccia .icona").removeClass("OPEN");
    $("#Interfaccia .icona").removeClass("CLOSE");
    $("#Interfaccia .icona").addClass(class_stato);
}

ifWindowCovering.update = function(now, moving){
    var t = new Date().getTime();
    var i = $("#Interfaccia").data("current_index");

    //ifWindowCovering.liftPerc = liftPerc = Elettrodomestici.listaElettrodomestici[i].device_value;
    if (ifWindowCovering.liftPerc = null){
    	//E' stato fatto stop, bisogna chiedere il nuovo valore.
    	if (InterfaceEnergyHome.mode > 0){
    	    InterfaceEnergyHome.objService.getDeviceState(function(result, err){
    	    	if (err != null){
    	    		ifWindowCovering.liftPerc = 100;
    	    	} else {
    	        	ifWindowCovering.liftPerc = result;
    	    	}
    	    	ifWindowCovering.gestSlider();
    	    }, pid, ifWindowCovering.WINDOWCOVERING_CURRENTPOSITIONLIFTPERCENTAGE);
        } else {
        	ifWindowCovering.liftPerc = 100;
        	ifWindowCovering.gestUpdate(now, moving);
        }
    } else {
    	ifWindowCovering.gestUpdate(now, moving);
    }
}

ifWindowCovering.gestUpdate = function(now, moving){
	var liftPerc = "";
	
    if (ifWindowCovering.liftPerc == ifWindowCovering.max) {
    	liftPerc = "total open";
    } else if (ifWindowCovering.liftPerc == 0) {
    	liftPerc = " total close";
    } else {
    	liftPerc = "ajar";
    }
    $("#Interfaccia .StatoElettrodomestico .consumo").text(liftPerc);
    $("#Interfaccia .StatoElettrodomestico .posizione_value").text(Elettrodomestici.locazioni[Elettrodomestici.listaElettrodomestici[i].location]);
    
    //Non aggiorno oltre l'interfaccia se passa troppo poco tempo dall'ultimo comando
    if (!now && ifWindowCovering.timeout_timer!=null) {
        if (t-ifWindowCovering.timeout_timer < ifWindowCovering.UPDATE_FREQ) {
            return;
        }
    }
    
    var class_stato=""
    var _stato="";
    
    if (Elettrodomestici.listaElettrodomestici[i].connessione==2) {
        if (Elettrodomestici.listaElettrodomestici[i].device_value == ifWindowCovering.max){
            _stato="OPEN";
            class_stato="OPEN";
        } else if (Elettrodomestici.listaElettrodomestici[i].device_value == 0){
            _stato="CLOSE";
            class_stato="CLOSE";
        } else{
            _stato="OPEN";
            class_stato="OPEN";
        }
    } else {
        _stato = "NP";
        class_stato = "NP";
    	ifWindowCovering.stato = -1;
    }
    $("#Interfaccia #OnOffControl .btnToggle").removeClass("NP");
    $("#Interfaccia #OnOffControl .btnToggle").removeClass("OPEN");
    $("#Interfaccia #OnOffControl .btnToggle").removeClass("CLOSE");
    $("#Interfaccia #OnOffControl .btnToggle").addClass(class_stato);
    if (class_stato=="NP") {
        $("#Interfaccia #OnOffControl .btnToggle").text(_stato);
    }
    
    ifWindowCovering.updateIcon(ifWindowCovering.stato);
}
