var ifWindowCovering =  { 
        timeout_timer:null,
        clusters:{},
        UPDATE_FREQ: 30000,
        stato:1 //1=acceso
}

ifWindowCovering.init=function(clusters){  

	ifWindowCovering.timeout_timer=null;
	ifWindowCovering.stato=-1;
        
	$( "#onoff" ).click(function( event ) {
        event.preventDefault();
        var myId= "#"+$(this).attr("id");
        if (ifWindowCovering.stato==1) {

                var pid=$("#Interfaccia").data("pid");
                if (pid==undefined)
                        return;
                
                InterfaceEnergyHome.objService.setDeviceState(function(result, err){
                        if (err!=null) {
                                ifWindowCovering.update(true);
                                
                        }else if (result!=null) {
                                if (result==true) {
                                        
                                        ifWindowCovering.stato=0;
               
                                        $('#bg').css( "background-color","#333" );
                                        
                                        $(myId).addClass("OFF");
                                        $(myId).removeClass("ON");
                                        
                                        $('#lum').slider("value",0);
                                        $('#lum_perc').html(0+"%");
                                        
                                        //InterfaceEnergyHome.objService.getInfos(function(result, err){
                                        //        for ( var k in result.list) {
                                        //                console.log("->>"+k+": "+result.list[k]);
                                        //                for ( var o in result.list[k].map) {
                                        //                        console.log("---->> map:"+o+": "+result.list[k].map[o]);
                                        //                }
                                        //        }
                                        //        
                                        //        console.log($.param(err));     
                                        //});
                                }
                                ifWindowCovering.timeout_timer=new Date().getTime();
                        }
                }, pid,0);
            
            
        }else if(ifWindowCovering.stato==0){
            
            var pid=$("#Interfaccia").data("pid");
            if (pid==undefined)
                    return;
            
            InterfaceEnergyHome.objService.setDeviceState(function(result, err){
                    if (err!=null) {
                            ifWindowCovering.update(true);
                    }else if (result!=null) {
                            if (result==true) {
                                    
                                    ifWindowCovering.stato=1;
                                    ifWindowCovering.colorePercepito=ifWindowCovering.toColorePercepito(ifWindowCovering.coloreReale);
                                    
                                    $('#bg').css( "background-color",ifWindowCovering.colorePercepito.toHexString() );
                                    $('#lum').slider("value",ifWindowCovering.lum);
                                    
                                    $(myId).addClass("ON");
                                    $(myId).removeClass("OFF");
                                    $('#lum_perc').html(ifWindowCovering.lum+"%");

                                    ifWindowCovering.timeout_timer=new Date().getTime();
                            }
                    }
                             
            }, pid,1);
        }
    	
    	$("#onoff").attr('unselectable','on')
    		.css({'-moz-user-select':'-moz-none',
    		      '-moz-user-select':'none',
    		      '-o-user-select':'none',
    		      '-khtml-user-select':'none', /* you could also put this in a class */
    		      '-webkit-user-select':'none',/* and add the CSS class here instead */
    		      '-ms-user-select':'none',
    		      'user-select':'none'
    	}).bind('selectstart', function(){ return false; });
        
        if( ifWindowCovering.clusters!=null){
                /*
                if(ifWindowCovering.clusters["org.energy_home.jemma.ah.cluster.zigbee.general.LevelControlServer"]!=true )
                        $( "#lum" ).slider("disable");
                 */
                
                if(ifWindowCovering.clusters["org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer"]==true){
                        
                        ifWindowCovering.fb=$.farbtastic('#picker');
                        ifWindowCovering.fb.setColor(ifWindowCovering.baseColor.toHexString());
                        ifWindowCovering.fb.linkTo(function(color){
                            
                                var c=tinycolor(color);
                                var c2=c.toHsl();
                                var rc= ifWindowCovering.coloreReale.toHsl();
                        
                                if (c2.h!=0) {
                                    rc.h=c2.h;
                                }else{
                                    c2.h=rc.h;
                                }
                                c2.l=rc.l;
                                if (c2.s!=0) {
                                    rc.s=c2.s;
                                    
                                }else{
                                    c2.s=rc.s;
                                }
                                
                                ifWindowCovering.coloreReale= tinycolor(rc);
                                ifWindowCovering.colorePercepito=ifWindowCovering.toColorePercepito(ifWindowCovering.coloreReale);
                                if (ifWindowCovering.stato==1) {
                                    $('#bg').css( "background-color",ifWindowCovering.colorePercepito.toHexString() );
                                }   
                        });
                        
                        ifWindowCovering.fb.onStart(function(){
                                ifWindowCovering.isBusy=true;
                        });
                        ifWindowCovering.fb.onStop(function(){
                                        ifWindowCovering.isBusy=false;
                                        var rc= ifWindowCovering.coloreReale.toHsl();
                                        var h=Math.round(rc.h/360*254);
                                        var s=Math.round( rc.s*254);
                                        
                                        var pid=$("#Interfaccia").data("pid");
                                        if (pid==undefined)
                                                return;
                                        
                                        InterfaceEnergyHome.objService.colorControlMoveToColorHS(function(result, err){
                                                
                                                if (err!=null) {
                                                        ifWindowCovering.update(true);     
                                                }else if (result!=null) {
                                                        if (result==true) {
                                                                ifWindowCovering.timeout_timer=new Date().getTime();
                                                        }
                                                }
                                             
                                        }, pid,h,s,10);
                        });
                        
                }
        }
        ifWindowCovering.update(true);
	});
};

ifWindowCovering.updateIcon=function(stato){
        
        if (stato!=1 && stato!=0) {
                stato=null;
        }
        var i= $("#Interfaccia").data("current_index");
        var icona_src= "Resources/Images/Devices2/"+Elettrodomestici.getIcon(Elettrodomestici.listaElettrodomestici[i],stato);
        
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

ifWindowCovering.update= function(now){
	
        var t= new Date().getTime();
        
        var i= $("#Interfaccia").data("current_index");

        /*var consumo=Elettrodomestici.listaElettrodomestici[i].consumo;
        if (consumo!="n.a.") {
                consumo=Math.round(Elettrodomestici.listaElettrodomestici[i].consumo)+"W";
        }
        $("#Interfaccia .StatoElettrodomestico .consumo").text(consumo);*/
        $("#Interfaccia .StatoElettrodomestico .posizione_value").text(Elettrodomestici.locazioni[Elettrodomestici.listaElettrodomestici[i].location]);
        
        
        
        //Non aggiorno oltre l'interfaccia se passa troppo poco tempo dall'ultimo comando
        if (!now && ifWindowCovering.timeout_timer!=null) {
                if (t - ifWindowCovering.timeout_timer < ifWindowCovering.UPDATE_FREQ) {
                        return;
                }
        }
        
        ifWindowCovering.timeout_timer=t;
        
        var class_stato="NP"
        var statoDoor="";
        
        if (Elettrodomestici.listaElettrodomestici[i].connessione == 2) {
        	ifWindowCovering.stato=1;
            statoDoor = Elettrodomestici.listaElettrodomestici[i].lockState;
            if (statoDoor == 1){
            	statoDoor="porta aperta";
            } else {
            	statoDoor="porta chiusa";
            }
        } else {
    		statoDoor="n.a.";
            ifWindowCovering.stato=-1;
        }
        
        $("#statodoor_value").text(statoDoor);
        
        ifWindowCovering.updateIcon(ifWindowCovering.stato);
}