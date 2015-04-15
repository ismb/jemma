var ifIndesitFridge =  { 
        isBusy:false,
        timeout_timer:null,
        clusters:{},
        UPDATE_FREQ: 10000,
	duration:0,
	temperatureFridge:0,
	temperatureFreezer:0,
	appliance_status:0,
	consumption:0,
	power:0,
	programma:0,
	stati:{
		0: "--",
		1: "Off",
		2: "Standby",
		3: "Programming",
		4: "Delay",
		5: "Running",
		6: "Pause",
		7: "End",
		8: "Fault"
		},
        stato:1 //1=acceso;0=spento;-1=disconesso
}

ifIndesitFridge.init=function(_clusters){
	
	ifIndesitFridge.clusters=_clusters;
	
        if( ifIndesitFridge.clusters!=null){               
                
                
                if(ifIndesitFridge.clusters["org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer"]==true){
                        
                       
                }
        }
        ifIndesitFridge.update(true);
    
};


ifIndesitFridge.update= function(now){
        
        
        
        var t= new Date().getTime();
        var i= $("#Interfaccia").data("current_index");
        console.log("index:"+i);
        var device= Elettrodomestici.listaElettrodomestici[i];
        var consumo=Elettrodomestici.listaElettrodomestici[i].consumo;
        if (consumo!="n.a.") {
                consumo=Math.round(Elettrodomestici.listaElettrodomestici[i].consumo)+"W";
        }
        $("#Interfaccia .StatoElettrodomestico .consumo").text(consumo);
        $("#Interfaccia .StatoElettrodomestico .posizione_value").text(Elettrodomestici.locazioni[device.location]);
        
                
        //Non aggiorno oltre l'interfaccia se passa troppo poco tempo dall'ultimo comando
        if (!now && ifIndesitFridge.timeout_timer!=null) {
                if (t-ifIndesitFridge.timeout_timer < ifIndesitFridge.UPDATE_FREQ) {
                        return;
                }
        }
        
                
        if(InterfaceEnergyHome.mode==-2)
        {
        	msg="4&deg;C";	
        	$(".val_temperature_fridge").html(msg);
        	
        	msg="-18&deg;C";	
        	$(".val_temperature_freezer").html(msg);
        	
        	msg="On";	
        	$(".val_status").html(msg);
        	
        	return;
        }

        if (ifIndesitFridge.isBusy) {
                return;
        }
        
        ifIndesitFridge.timeout_timer=t;
        
        var class_stato="NP"
        var _stato="";
        
        if (Elettrodomestici.listaElettrodomestici[i].connessione==2) {
                
                if (Elettrodomestici.listaElettrodomestici[i].stato==1){
                        _stato="ON";
                        class_stato="ON";
                        ifIndesitFridge.stato=1;
                        
                        
                }
                else if (Elettrodomestici.listaElettrodomestici[i].stato==0){
                        _stato="OFF";
                        class_stato="OFF";
                        ifIndesitFridge.stato=0;
                }
                else{
                        ifIndesitFridge.stato=-1;
                }
        }else{
                ifIndesitFridge.stato=-1;
        }
       
        if (class_stato=="NP") {
                
        }

	if(ifIndesitFridge.stato==0){
               
        }
        if(ifIndesitFridge.stato==-1){
               
        }
        
        
        
        var pid=$("#Interfaccia").data("pid");
	console.log(pid);
	
        //Aggiorno i valori dello slider e del colore
        if (ifIndesitFridge.clusters==null) {
                return;
        }
        
        
        if(ifIndesitFridge.clusters["org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer"]==true ){
                
                //Elettrodomestici.addSpinner("#Interfaccia", "#0a0a0a");
		console.log("valori:");
		
		//CICLO
		InterfaceEnergyHome.objService.applianceControlGetTemperatureTarget0(function(result, err){
                        console.log("status e:"+err);
                        console.log("status r:"+result);
                        var msg="--";
                        if (err!=null) {
                                ifIndesitFridge.update(true);
                                
                        }else if (result!=null) {
				if (result!=-1) {
					ifIndesitFridge.temperatureFridge=result;
					msg=""+result+"&deg;C";	
				}
				
                        }
			
			$(".val_temperature_fridge").html(msg);
			
                }, pid);  
                
		//Temperatura
		InterfaceEnergyHome.objService.applianceControlGetTemperatureTarget1(function(result, err){
                        
                        var msg="--";
                        if (err!=null) {
                                ifIndesitFridge.update(true);
                                
                        }else if (result!=null) {
				if (result!=-1) {
					var t= result-65510 -26;
					ifIndesitFridge.temperatureFreezer=t;
					msg=""+t+"&deg;C";
				}
				
                        }
			
			$(".val_temperature_freezer").html(msg);
			
                }, pid);
		
		//Status
		InterfaceEnergyHome.objService.applianceControlExecSignalState(function(result, err){
                        
                        var msg="--";
                        if (err!=null) {
                                ifIndesitFridge.update(true);
                                
                        }else if (result!=null) {
                                ifIndesitFridge.appliance_status=result["map"].ApplianceStatus;
				
				if (ifIndesitFridge.appliance_status>0){
					msg=ifIndesitFridge.stati[ifIndesitFridge.appliance_status];
				}
				
				
                        }
			
			$(".val_status").text(msg);
			
                }, pid); 
		
        
        }
        
        
}
