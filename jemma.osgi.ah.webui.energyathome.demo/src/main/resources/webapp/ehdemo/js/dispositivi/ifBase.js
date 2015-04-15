var ifBase =  { 
        timeout_timer:null,
        clusters:{},
        UPDATE_FREQ: 30000,
        stato:1 //1=acceso
}

ifBase.init=function(clusters){  
	
        ifBase.timeout_timer=null;
        ifBase.stato=-1;

        $( "#onoff" ).click(function( event ) {
        	
        	event.preventDefault();

        	 var myId= "#"+$(this).attr("id");
        	
        	//fake behavior
    		if(InterfaceEnergyHome.mode==-2)
    		{
    			var pid=$("#Interfaccia").data("pid");
    			var i= $("#Interfaccia").data("current_index");
    			if(ifBase.stato==1)
    			{
    				
    				$(myId).addClass("OFF");
                    $(myId).removeClass("ON");
    				ifBase.stato=0;
    				Elettrodomestici.listaElettrodomestici[i].stato=0;
    				updateFakeDeviceValueByNameAndPID("OnOffState",pid,false);
    				updateFakeDeviceConsumptionByPid(pid,0);
    				Elettrodomestici.update();

    				
    			}else{
    				msg="ON"
    				$(myId).addClass("ON");
                    $(myId).removeClass("OFF");
    				ifBase.stato=1
    				Elettrodomestici.listaElettrodomestici[i].stato=1;
    				updateFakeDeviceValueByNameAndPID("OnOffState",pid,true);
    				updateFakeDeviceConsumptionByPid(pid,35);
    				Elettrodomestici.update();
    			}
    			
    			 ifBase.updateIcon(ifBase.stato);
    			
    			//$("#device_"+i+" .StatoElettrodomestico .row .stato").html(msg);
    			
    			return;
    		}
        	
                
               
                var i = $("#Interfaccia").data("current_index");
                if (ifBase.stato==1) {
                    
                    
                        var pid=$("#Interfaccia").data("pid");
                        if (pid==undefined)
                            return;
                        if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)){
                            InterfaceEnergyHome.objService.setDeviceState(function(result, err){
                              
                                    if (err!=null) {
                                            ifBase.update(true);
                                    }else if (result!=null) {
                                            if (result==true) {
                                                    
                                                ifBase.stato=0;
                                                $(myId).addClass("OFF");
                                                $(myId).removeClass("ON");
                                                ifBase.updateIcon(0);
                                                $("#device_" + i + " .StatoElettrodomestico .stato").text("OFF");
                                                $("#device_" + i).removeClass("ON");
                                                $("#device_" + i).removeClass("OFF");
                                                $("#device_" + i).removeClass("ONOFF");
                                                $("#device_" + i).addClass("ONOFF");
                                            }
                                    }
                                    ifBase.timeout_timer=new Date().getTime();
                            }, pid,0);
                        } else{
                                ifBase.stato=0;
                                $(myId).addClass("OFF");
                                $(myId).removeClass("ON");
                                ifBase.updateIcon(0);
                                
                        }
                    
                }else if (ifBase.stato==0){
                    
                        var pid=$("#Interfaccia").data("pid");
                        if (pid==undefined)
                                return;
                        if ((InterfaceEnergyHome.mode > 0) || (InterfaceEnergyHome.mode == -1)){
                                InterfaceEnergyHome.objService.setDeviceState(function(result, err){
                                        if (err!=null) {
                                                ifBase.update(true);
                                        }else if (result!=null) {
                                                if (result==true) {
                                                    ifBase.stato=1;
                                                    $(myId).addClass("ON");
                                                    $(myId).removeClass("OFF");
                                                    ifBase.updateIcon(1);
                                                    $("#device_" + i + " .StatoElettrodomestico .stato").text("ON");
                                                    $("#device_" + i).removeClass("ONOFF");
                                                    $("#device_" + i).removeClass("OFF");
                                                    $("#device_" + i).removeClass("ON");
                                                    $("#device_" + i).addClass("ON");
                                                }
                                        }
                                        ifBase.timeout_timer=new Date().getTime();
                                }, pid,1);
                        } else{
                                ifBase.stato=1;
                                $(myId).addClass("ON");
                                $(myId).removeClass("OFF");
                                ifBase.updateIcon(1);
                        }
                }
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
        
        ifBase.update();
};

ifBase.updateIcon=function(stato){
        
        if (stato!=1 && stato!=0) {
                stato=null;
        }
        var i= $("#Interfaccia").data("current_index");
        var icona_src= "Resources/Images/Devices/"+Elettrodomestici.getIcon(Elettrodomestici.listaElettrodomestici[i],stato);
        
        $("#Interfaccia .icona .icona-dispositivo").attr("src",icona_src);
        
        var class_stato="NP";
        
        if (stato==1) {
                class_stato="ON";
        }
        else if (stato==0){
                class_stato="OFF";
        }
        $("#Interfaccia .icona").removeClass("ON");
        $("#Interfaccia .icona").removeClass("OFF");
        $("#Interfaccia .icona").addClass(class_stato);
        
}

ifBase.update= function(now){
        
        var t= new Date().getTime();
        
        var i= $("#Interfaccia").data("current_index");

        var consumo=Elettrodomestici.listaElettrodomestici[i].consumo;
        if (consumo!="n.a.") {
                consumo=Math.round(Elettrodomestici.listaElettrodomestici[i].consumo)+"W";
        }
        $("#Interfaccia .StatoElettrodomestico .consumo").text(consumo);
        $("#Interfaccia .StatoElettrodomestico .posizione_value").text(Elettrodomestici.locazioni[Elettrodomestici.listaElettrodomestici[i].location]);
        
        
        
        //Non aggiorno oltre l'interfaccia se passa troppo poco tempo dall'ultimo comando
        if (!now && ifBase.timeout_timer!=null) {
                if (t-ifBase.timeout_timer < ifBase.UPDATE_FREQ) {
                        return;
                }
        }
        
        ifBase.timeout_timer=t;
        
        var class_stato="NP"
        var _stato="";
               
        if (Elettrodomestici.listaElettrodomestici[i].connessione==2) {
                
        		console.debug("STATOOO!");
        		console.debug(Elettrodomestici.listaElettrodomestici[i].stato==1)
        	
                if (Elettrodomestici.listaElettrodomestici[i].stato==1){
                        _stato="ON";
                        class_stato="ON";
                        ifBase.stato=1;
                        
                }
                else if (Elettrodomestici.listaElettrodomestici[i].stato==0){
                        _stato="OFF";
                        class_stato="OFF";
                        ifBase.stato=0;

                }
                else{
                        ifBase.stato=-1;
                }
        }
        $("#Interfaccia #OnOffControl .btnToggle").removeClass("ON");
        $("#Interfaccia #OnOffControl .btnToggle").removeClass("OFF");
        $("#Interfaccia #OnOffControl .btnToggle").removeClass("NP");
        $("#Interfaccia #OnOffControl .btnToggle").addClass(class_stato);
        if (class_stato=="NP") {
                $("#Interfaccia #OnOffControl .btnToggle").text(_stato);
        }
        
        ifBase.updateIcon(ifBase.stato);

        
}
