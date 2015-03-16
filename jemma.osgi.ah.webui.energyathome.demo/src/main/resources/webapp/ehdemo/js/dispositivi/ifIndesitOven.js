var ifIndesitOven =  { 
        isBusy:false,
        timeout_timer:null,
        interval_blink:null,
	durationObj:{h:"00",m:"00"},
        clusters:{},
        UPDATE_FREQ: 10000,
	duration:0,
	temperature:0,
	appliance_status:0,
	consumption:0,
	power:0,
	programma:0,
	programmi:{
		0: "--",
		1: "Multilevel",
		2: "Grill",
		3: "Fan grilling",
		4: "Roast",
		5: "Fish",
		6: "Pizza",
		7: "Proving",
		8: "Pastry",
		9: "Pasteurisation",
		10: "Slow cook meat",
		11: "Slow cook fish",
		12: "Slow cook vegetables",
		13: "Defrosting",
		14: "Eco cooking",
		15: "Bread",
		16: "Beef",
		17: "Veal",
		18: "Lamb",
		19: "Pork",
		20: "Chicken",
		21: "Fish fillet",
		22: "Fish and Papillotte",
		23: "Tarts",
		24: "Shortcrust tarts",
		25: "Brioche",
		26: "Cake",
		27: "Desserts",
		28: "Paella",
		29: "Pilau rice",
		30: "Yoghurt",
		31: "Pizza with stone"
		},
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

ifIndesitOven.init=function(_clusters){

	ifIndesitOven.clusters=_clusters;

        if( ifIndesitOven.clusters!=null){               
                
                
                if(ifIndesitOven.clusters["org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer"]==true){
                        
                       
                }
        }
        ifIndesitOven.update(true);
    
};


ifIndesitOven.update= function(now){
        
        var t= new Date().getTime();
        var i= $("#Interfaccia").data("current_index");
        //console.log("index:"+i);
        var device= Elettrodomestici.listaElettrodomestici[i];

        var consumo=Elettrodomestici.listaElettrodomestici[i].consumo;
        if (consumo!="n.a.") {
                consumo=Math.round(Elettrodomestici.listaElettrodomestici[i].consumo)+"W";
        }
        $("#Interfaccia .StatoElettrodomestico .consumo").text(consumo);
        $("#Interfaccia .StatoElettrodomestico .posizione_value").text(Elettrodomestici.locazioni[device.location]);
        
                
        //Non aggiorno oltre l'interfaccia se passa troppo poco tempo dall'ultimo comando
        if (!now && ifIndesitOven.timeout_timer!=null) {
                if (t-ifIndesitOven.timeout_timer < ifIndesitOven.UPDATE_FREQ) {
                        return;
                }
        }

        if (ifIndesitOven.isBusy) {
                return;
        }
        
        ifIndesitOven.timeout_timer=t;
        
        var class_stato="NP"
        var _stato="";
        
        if (Elettrodomestici.listaElettrodomestici[i].connessione==2) {
                
                if (Elettrodomestici.listaElettrodomestici[i].stato==1){
                        _stato="ON";
                        class_stato="ON";
                        ifIndesitOven.stato=1;
                        
                        
                }
                else if (Elettrodomestici.listaElettrodomestici[i].stato==0){
                        _stato="OFF";
                        class_stato="OFF";
                        ifIndesitOven.stato=0;
                }
                else{
                        ifIndesitOven.stato=-1;
                }
        }else{
                ifIndesitOven.stato=-1;
        }
       
        if (class_stato=="NP") {
                
        }
/*
	if(ifIndesitOven.stato==0){
               
        }
        if(ifIndesitOven.stato==-1){
               
        }
        
  */      
        //in case of mode noservernodev: fake values
        if(InterfaceEnergyHome.mode==-2)
        {
        	var program=6;
        	ifIndesitOven.programma=program;
        	s_programmi=ifIndesitOven.programmi[program];
        	$(".val_program").text("Program: "+s_programmi);
        	
        	var temper=200;
        	ifIndesitOven.temperature=temper;
			msg=""+temper+"&deg;C";
			$(".val_temperature").html(msg);

			$(".val_label_duration").text("Timer:");
			ifIndesitOven.duration=400;
			_msg=ifIndesitOven.minutesToString(ifIndesitOven.duration);
			$(".val_duration").text(_msg);


			ifIndesitOven.appliance_status=2;		
			msg=ifIndesitOven.stati[ifIndesitOven.appliance_status];
			$(".val_status").text(msg);
			
        	return;
        }
        
        var pid=$("#Interfaccia").data("pid");
        //Aggiorno i valori dello slider e del colore
        if (ifIndesitOven.clusters==null) {
                return;
        }
        
        
        if(ifIndesitOven.clusters["org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer"]==true /*&& ifIndesitOven.stato!=-1*/){
        
      
		
		//CICLO
		InterfaceEnergyHome.objService.applianceControlGetCycleTarget0(function(result, err){
                        
                        var s_programmi="--";
                        if (err!=null) {
                                ifIndesitOven.update(true);
                                
                        }else if (result!=null) {
                                ifIndesitOven.programma=result;
				s_programmi=ifIndesitOven.programmi[result];
				
                        }
			
			$(".val_program").text("Program: "+s_programmi);
			
                }, pid);  
                
		//Temperatura
		InterfaceEnergyHome.objService.applianceControlGetTemperatureTarget0(function(result, err){
                        
                        var msg="--";
                        if (err!=null) {
                                ifIndesitOven.update(true);
                                
                        }else if (result!=null) {
                                ifIndesitOven.temperature=result;
				msg=""+result+"&deg;C";
				
				
                        }
			
			$(".val_temperature").html(msg);
			
                }, pid);
		
		//Status
		InterfaceEnergyHome.objService.applianceControlExecSignalState(function(result, err){
                        
                        var msg="--";
                        if (err!=null) {
                                ifIndesitOven.update(true);
                                
                        }else if (result!=null) {
                                ifIndesitOven.appliance_status=result["map"].ApplianceStatus;
				
				msg=ifIndesitOven.stati[ifIndesitOven.appliance_status];
				
				if (ifIndesitOven.appliance_status>0){
					msg=ifIndesitOven.stati[ifIndesitOven.appliance_status];
				}
				
				
                        }
			
			$(".val_status").text(msg);
			
			//Durata -running 
			if (ifIndesitOven.appliance_status==5) {
				InterfaceEnergyHome.objService.applianceControlGetRemainingTime(function(result2, err2){
					
					var _msg="--";
					if (err2!=null) {
						ifIndesitOven.update(true);
						
					}else if (result2!=null) {
						ifIndesitOven.duration=result2;
						_msg=ifIndesitOven.minutesToString(result2);
						$(".val_duration").text(_msg);
						$(".val_label_duration").text("Remaining time:");
					}
					
					
					
				}, pid);
				if (ifIndesitOven.interval_blink==null) {
						
					ifIndesitOven.interval_blink=setInterval(ifIndesitOven.blink,2000);
				}
			}
			//Durata -programming
			else if (ifIndesitOven.appliance_status==3) {
				InterfaceEnergyHome.objService.applianceControlGetFinishTime(function(result2, err2){
					
					var _msg="--";
					if (err2!=null) {
						ifIndesitOven.update(true);
						
					}else if (result2!=null) {
						ifIndesitOven.duration=result2;
						_msg=ifIndesitOven.minutesToString(result2);
						$(".val_duration").text(_msg);
						if (result2>0) 
							$(".val_label_duration").text("Finish time:");
						else
							$(".val_label_duration").text("Timer:");
					}
					
					
					
				}, pid);
				if (ifIndesitOven.interval_blink!=null) {
					clearInterval(ifIndesitOven.interval_blink);
					ifIndesitOven.interval_blink=null;
				}
			}
			else{
				$(".val_label_duration").text("Timer:");
				$(".val_duration").text("00:00");
				if (ifIndesitOven.interval_blink!=null) {
					clearInterval(ifIndesitOven.interval_blink);
					ifIndesitOven.interval_blink=null;
				}
				
			}
			
                }, pid); 
		
        
        }
        
        
}

ifIndesitOven.minutesToString=function(val){
	if (val==0) {
		ifIndesitOven.durationObj.h="00";
		ifIndesitOven.durationObj.m="00";
		return "00:00";
	}
	if (val<0) {
		ifIndesitOven.durationObj.h="00";
		ifIndesitOven.durationObj.m="00";
		return "00:00";
	}
	
	var h = (val>>8);
	var m = val-((val>>6)<<6);
	
	
	var sh=""+h;
	if (h<10)
		sh="0"+sh;
		
	var sm=""+m;
	if (m<10)
		sm="0"+sm;
	
	ifIndesitOven.durationObj.h=sh;
	ifIndesitOven.durationObj.m=sm;
	
	return sh+":"+sm;
	
}

ifIndesitOven.blink=function(elem){
	
	$(".val_duration").fadeTo(300,0.3,function(){
	    $(".val_duration").fadeTo(300,1);
	});    

}
