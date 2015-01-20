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

    if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)){
	    InterfaceEnergyHome.objService.getDeviceState(function(result, err){
	    	if (err != null){
	    		ifWindowCovering.max = 100;
	    	} else {
	        	ifWindowCovering.max = result;
	    	}
	    	ifWindowCovering.max = 255; //65535;
	    	ifWindowCovering.gestLiftPerc();
	    }, pid, ifWindowCovering.WINDOWCOVERING_INFOINSTALLEDCLOSEDLIMIT);
    } else {
    	ifWindowCovering.max = 100;
    	ifWindowCovering.gestLiftPerc();
    }
}

ifWindowCovering.gestLiftPerc=function(){  

    var pid = $("#Interfaccia").data("pid");

    if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)){
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
        value: ifWindowCovering.max,
        min: 0,
        max: ifWindowCovering.max,
        slide: function( event, ui ) {
            
            ifWindowCovering.lum=Math.floor((ui.value*100)/ifWindowCovering.max);
            $('#lum_perc').html(ifWindowCovering.lum+"%");
        },
        start: function(event, ui ){
        	ifWindowCovering.isBusy=true;
        },
        stop: function(event, ui ){
        	ifWindowCovering.isBusy=false;
            
            /*var pid = $("#Interfaccia").data("pid");
            var value = Math.round(ifWindowCovering.lum);
            ifWindowCovering.liftPerc = value;
            InterfaceEnergyHome.objService.setDeviceState(function(result, err){
            	ifWindowCovering.stato = 8;
                if (err == null) {
                	ifWindowCovering.update();        
                }
                    
            }, pid, ifWindowCovering.WINDOWCOVERING_OPENPECENTAGE, value);*/
        }
    });
    
    $("#btnCloseW").click(function( event ) {
        event.preventDefault();
        if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)){
            InterfaceEnergyHome.objService.setDeviceState(function(result, err){
            	ifWindowCovering.stato = 7;
                ifWindowCovering.liftPerc = 100;
            	ifWindowCovering.update();
            }, pid, ifWindowCovering.WINDOWCOVERING_DOWNCLOSE, 0);
        } else {
        	ifWindowCovering.stato = 7;
            ifWindowCovering.liftPerc = 100;
        	ifWindowCovering.update();
        }
    });
    
    $("#btnOpenW").click(function( event ) {
        event.preventDefault();
        if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)){
            InterfaceEnergyHome.objService.setDeviceState(function(result, err){
            	ifWindowCovering.stato = 6;
                ifWindowCovering.liftPerc = 0;
            	ifWindowCovering.update();
            }, pid, ifWindowCovering.WINDOWCOVERING_UPOPEN, 0);
        } else {
        	ifWindowCovering.stato = 6;
            ifWindowCovering.liftPerc = 0;
        	ifWindowCovering.update();
        }
    });
    
    $("#btnStopW").click(function( event ) {
        event.preventDefault();
        if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)){
            InterfaceEnergyHome.objService.setDeviceState(function(result, err){
            	ifWindowCovering.stato = 5;
                ifWindowCovering.liftPerc = null;
            	ifWindowCovering.update();
            }, pid, ifWindowCovering.WINDOWCOVERING_STOPPED, 0);
        } else {
        	ifWindowCovering.stato = 5;
            ifWindowCovering.liftPerc = null;
        	ifWindowCovering.update();
        }
    });
    
    ifWindowCovering.update();
};

ifWindowCovering.updateIcon=function(){

    var i= $("#Interfaccia").data("current_index");
    var icona_src= "Resources/Images/Devices/"+Elettrodomestici.getIcon(Elettrodomestici.listaElettrodomestici[i], ifWindowCovering.stato);
    
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

ifWindowCovering.update = function(){
    var t = new Date().getTime();
    var i = $("#Interfaccia").data("current_index");

    //ifWindowCovering.liftPerc = liftPerc = Elettrodomestici.listaElettrodomestici[i].device_value;
    if (ifWindowCovering.liftPerc == null){
    	//E' stato fatto stop, bisogna chiedere il nuovo valore.
    	if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)){
    	    InterfaceEnergyHome.objService.getDeviceState(function(result, err){
    	    	if (err != null){
    	    		ifWindowCovering.liftPerc = ifWindowCovering.max;
    	    	} else {
    	    		if (result > ifWindowCovering.max){
    	    			ifWindowCovering.liftPerc = ifWindowCovering.max;
    	    		} else {
    	    			ifWindowCovering.liftPerc = result;
    	    		}
    	    	}
    	    	ifWindowCovering.gestUpdate();
    	    }, pid, ifWindowCovering.WINDOWCOVERING_CURRENTPOSITIONLIFTPERCENTAGE);
        } else {
        	ifWindowCovering.liftPerc = ifWindowCovering.max;
        	ifWindowCovering.gestUpdate();
        }
    } else {
    	ifWindowCovering.gestUpdate();
    }
}

ifWindowCovering.gestUpdate = function(){
	var liftPerc = "";
    var i = $("#Interfaccia").data("current_index");
	
    if (ifWindowCovering.liftPerc > 0) {
    	liftPerc = "OPEN";
    } else {
    	liftPerc = "CLOSE";
    }
    $("#Interfaccia .StatoElettrodomestico .consumo").text(liftPerc);
    $("#Interfaccia .StatoElettrodomestico .posizione_value").text(Elettrodomestici.locazioni[Elettrodomestici.listaElettrodomestici[i].location]);

	$("#device_" + i + " .StatoElettrodomestico .stato").text(liftPerc);
    
    //Non aggiorno oltre l'interfaccia se passa troppo poco tempo dall'ultimo comando
    if (!now && ifWindowCovering.timeout_timer!=null) {
        if (t-ifWindowCovering.timeout_timer < ifWindowCovering.UPDATE_FREQ) {
            return;
        }
    }
    
    var class_stato=""
    var _stato="";
    
    if (Elettrodomestici.listaElettrodomestici[i].connessione==2) {
    	if (ifWindowCovering.liftPerc > 0){
            _stato = "OPEN";
            class_stato="OPEN";
        } else{
        	_stato = "CLOSE";
            class_stato = "CLOSE";
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
