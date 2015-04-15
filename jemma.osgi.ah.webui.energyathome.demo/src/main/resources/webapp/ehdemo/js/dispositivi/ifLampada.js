var ifLampada =  { 
        baseColor: null,
        isBusy:false,
        colorePercepito:null,
        coloreReale:null,
        bckhsl:null,
        max: null,
        lum:null,
        timeout_timer:null,
        counterPositionDevice:null,
        clusters:{},
        fb:null, //farbtastic color picker
        UPDATE_FREQ: 30000,
        stato:1 //1=acceso;0=spento;-1=disconesso
}

ifLampada.init=function(_clusters, i){  
        
        
        //fiz immagine lampadina
		console.debug(_clusters);
	
        //$("#ifLampada #bg img").css("width","auto");
        $("#ifLampada #bg").width($("#ifLampada #bg").height());
        
        
        ifLampada.baseColor = tinycolor("#9cc31c");
        ifLampada.bckhsl = ifLampada.baseColor.toHsl();
        ifLampada.coloreReale = ifLampada.baseColor;
        
        $('#bg').css("background-color", ifLampada.baseColor.toHexString() );
        
        ifLampada.max = 100;
        ifLampada.lum = ifLampada.max;
        ifLampada.timeout_timer = new Date().getTime();
        ifLampada.stato = -1;
        ifLampada.clusters = _clusters;
        
        ifLampada.counterPositionDevice = i;
        
        $( "#lum" ).slider({
            range: "min",
            value: ifLampada.max,
            min: 0,
            max: ifLampada.max,
            slide: function( event, ui ) {
                
                if (ifLampada.coloreReale==null) {
                        return;
                }
                var tc=ifLampada.coloreReale; //tinycolor(c);
                var hsl=tc.toHsl();
                hsl.l=ui.value/100;
                if (hsl.l>=1) {
                        hsl.l=0.99;
                }
                if (hsl.l==0) {
                        hsl.l=0.01;
                }
                //var nc=tinycolor(hsl);
                
                //hsl.h=ifLampada.bckhsl.h;
                //hsl.s=ifLampada.bckhsl.s;
                ifLampada.coloreReale = tinycolor(hsl);
                ifLampada.colorePercepito = ifLampada.toColorePercepito(ifLampada.coloreReale);

                if (ui.value>0 ) {
                    $("#onoff").addClass("ON");
                    $("#onoff").removeClass("OFF");
                    ifLampada.stato=1;
                    $('#bg').css( "background-color", ifLampada.colorePercepito.toHexString() );
                }else if (ui.value==0 ) {
                    $("#onoff").addClass("OFF");
                    $("#onoff").removeClass("ON");
                    ifLampada.stato=0;
                    $('#bg').css( "background-color","#202020");
                }
                ifLampada.lum=ui.value;
                $('#lum_perc').html(ifLampada.lum+"%");
            },
            start: function(event, ui ){
                ifLampada.isBusy=true;
            },
            stop: function(event, ui ){
            	
            	ifLampada.isBusy=false;
            	if(InterfaceEnergyHome.mode==-2)
            	{
            		$("#device_"+ifLampada.counterPositionDevice+ " .StatoElettrodomestico .lblMeasure").text(ifLampada.lum + " %");
            		return;
            	}
                
                
                var pid=$("#Interfaccia").data("pid");
                var value=Math.round(ifLampada.lum/100*254);
                var time=10;
                InterfaceEnergyHome.objService.levelControlExecMoveToLevelWithOnOff(function(result, err){
                        
                        if (err!=null) {
                                ifLampada.update(true);        
                        }else if (result!=null) {
                                if (result==true) {
                                        ifLampada.timeout_timer=new Date().getTime();
                                        $("#device_"+ifLampada.counterPositionDevice+ " .StatoElettrodomestico .lblMeasure").text(ifLampada.lum + " %");
                                }
                        }
                        
                },pid,value,time);
                
            }
        });
        
        $( "#sync" ).click(function( event ){
                if (ifLampada.stato==1) {
                        ifLampada.sync();
                }
        });
        
        $( "#onoff" ).click(function( event ) {
                event.preventDefault();
                var myId= "#"+$(this).attr("id");
                var i = $("#Interfaccia").data("current_index");
                
                if(InterfaceEnergyHome.mode==-2)
                {
                	if(ifLampada.stato==1)
                	{
                		 $('#bg').css( "background-color","#333" );
                         
                         $(myId).addClass("OFF");
                         $(myId).removeClass("ON");
                         
                         $('#lum').slider("value",0);
                         $('#lum_perc').html(0+"%");
                         
                     	$("#device_" + i + " .StatoElettrodomestico .stato").text("OFF");
                     	
                         $("#device_" + i).removeClass("ONOFF");
                         $("#device_" + i).removeClass("OFF");
                         $("#device_" + i).removeClass("ON");
                         $("#device_" + i).addClass("ONOFF");
                         ifLampada.stato=0;
                	}else{
                		$('#bg').css( "background-color",ifLampada.colorePercepito.toHexString() );
                        $('#lum').slider("value",ifLampada.lum);
                        
                        $(myId).addClass("ON");
                        $(myId).removeClass("OFF");
                        $('#lum_perc').html(ifLampada.lum+"%");
                       
                    	$("#device_" + i + " .StatoElettrodomestico .stato").text("ON");
                    	
                        $("#device_" + i).removeClass("ONOFF");
                        $("#device_" + i).removeClass("OFF");
                        $("#device_" + i).removeClass("ON");
                        $("#device_" + i).addClass("ON");
                        ifLampada.stato=1;
                	}
                	return;
                }
                
                if (ifLampada.stato==1) {
   
                        var pid=$("#Interfaccia").data("pid");
                        if (pid==undefined)
                                return;
                        
                        
                        InterfaceEnergyHome.objService.setDeviceState(function(result, err){
                                if (err!=null) {
                                        ifLampada.update(true);
                                        
                                }else if (result!=null) {
                                        if (result==true) {
                                                
                                                ifLampada.stato=0;
                       
                                                $('#bg').css( "background-color","#333" );
                                                
                                                $(myId).addClass("OFF");
                                                $(myId).removeClass("ON");
                                                
                                                $('#lum').slider("value",0);
                                                $('#lum_perc').html(0+"%");
                                                
                                            	$("#device_" + i + " .StatoElettrodomestico .stato").text("OFF");
                                            	
                                                $("#device_" + i).removeClass("ONOFF");
                                                $("#device_" + i).removeClass("OFF");
                                                $("#device_" + i).removeClass("ON");
                                                $("#device_" + i).addClass("ONOFF");
                                                
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
                                        ifLampada.timeout_timer=new Date().getTime();
                                }
                        }, pid,0);
                    
                    
                }else if(ifLampada.stato==0){
                    
                    var pid=$("#Interfaccia").data("pid");
                    if (pid==undefined)
                            return;
                    
                    InterfaceEnergyHome.objService.setDeviceState(function(result, err){
                            if (err!=null) {
                                    ifLampada.update(true);
                            }else if (result!=null) {
                                    if (result==true) {
                                            
                                            ifLampada.stato=1;
                                            ifLampada.colorePercepito=ifLampada.toColorePercepito(ifLampada.coloreReale);
                                            
                                            $('#bg').css( "background-color",ifLampada.colorePercepito.toHexString() );
                                            $('#lum').slider("value",ifLampada.lum);
                                            
                                            $(myId).addClass("ON");
                                            $(myId).removeClass("OFF");
                                            $('#lum_perc').html(ifLampada.lum+"%");

                                            ifLampada.timeout_timer=new Date().getTime();
                                            
                                        	$("#device_" + i + " .StatoElettrodomestico .stato").text("ON");
                                        	
                                            $("#device_" + i).removeClass("ONOFF");
                                            $("#device_" + i).removeClass("OFF");
                                            $("#device_" + i).removeClass("ON");
                                            $("#device_" + i).addClass("ON");
                                    }
                            }
                                     
                    }, pid,1);
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
        
        if( ifLampada.clusters!=null){
        	console.debug(ifLampada.clusters);
                if(ifLampada.clusters["org.energy_home.jemma.ah.cluster.zigbee.general.LevelControlServer"]!=true )
                        $( "#lum" ).slider("disable");
                
                
                
                if(ifLampada.clusters["org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer"]==true){
                        
                        ifLampada.fb=$.farbtastic('#picker');
                        
                        
                        ifLampada.fb.setColor(ifLampada.baseColor.toHexString());
                        
                        ifLampada.fb.linkTo(function(color){
                            
                                var c=tinycolor(color);
                                var c2=c.toHsl();
                                var rc= ifLampada.coloreReale.toHsl();
                        
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
                                
                                ifLampada.coloreReale= tinycolor(rc);
                                ifLampada.colorePercepito=ifLampada.toColorePercepito(ifLampada.coloreReale);
                                if (ifLampada.stato==1) {
                                    $('#bg').css( "background-color",ifLampada.colorePercepito.toHexString() );
                                }   
                        });
                        
                        ifLampada.fb.onStart(function(){
                                ifLampada.isBusy=true;
                        });
                        ifLampada.fb.onStop(function(){
                        				if(InterfaceEnergyHome.mode==-2)
                        				{
                        					return;
                        				}
                                        ifLampada.isBusy=false;
                                        var rc= ifLampada.coloreReale.toHsl();
                                        var h=Math.round(rc.h/360*254);
                                        var s=Math.round( rc.s*254);
                                        
                                        var pid=$("#Interfaccia").data("pid");
                                        if (pid==undefined)
                                                return;
                                        
                                        InterfaceEnergyHome.objService.colorControlMoveToColorHS(function(result, err){
                                                
                                                if (err!=null) {
                                                        ifLampada.update(true);     
                                                }else if (result!=null) {
                                                        if (result==true) {
                                                                ifLampada.timeout_timer=new Date().getTime();
                                                        }
                                                }
                                             
                                        }, pid,h,s,10);
                        });
                        
                }
        }
        ifLampada.update(true);
        ifLampada.sync();
};

ifLampada.toColorePercepito=function(color){
        var c= color.toHsl();
        c.l+=0.2;
        
        if (c.s>=0.5) {
                c.l*=0.6;
        }else{
             c.l*=0.85;
             if (c.l>1) {
                c.l=1;
             }
        }

        
        return tinycolor(c);
};

ifLampada.update= function(now){
        
        
        var t= new Date().getTime();
        var i= $("#Interfaccia").data("current_index");
        //console.log("index:"+i);
        var device= Elettrodomestici.listaElettrodomestici[i];
        var consumo=Elettrodomestici.listaElettrodomestici[i].consumo;
        if (consumo!="n.a.") {
                //consumo=Math.round(Elettrodomestici.listaElettrodomestici[i].consumo)+"W";
        }
        
        if (ifLampada.lum != device.measure.CurrentLevel.value){
        	ifLampada.lum = device.measure.CurrentLevel.value;
        }
        
        $("#device_"+ifLampada.counterPositionDevice+ " .StatoElettrodomestico .lblMeasure").text(ifLampada.lum + " %");
        
        $("#Interfaccia .StatoElettrodomestico .consumo").text(consumo);
        $("#Interfaccia .StatoElettrodomestico .posizione_value").text(Elettrodomestici.locazioni[device.location]);
        
                
        //Non aggiorno oltre l'interfaccia se passa troppo poco tempo dall'ultimo comando
        if (!now && ifLampada.timeout_timer!=null) {
                if (t-ifLampada.timeout_timer < ifLampada.UPDATE_FREQ) {
                        return;
                }
        }
        
                
        

        if (ifLampada.isBusy) {
                return;
        }
        
        ifLampada.timeout_timer=t;
        
        var class_stato="NP"
        var _stato="";
        
        if (Elettrodomestici.listaElettrodomestici[i].connessione==2) {
                
                if (Elettrodomestici.listaElettrodomestici[i].stato==1){
                        _stato="ON";
                        class_stato="ON";
                        ifLampada.stato=1;
                        if(ifLampada.clusters["org.energy_home.jemma.ah.cluster.zigbee.general.dimmablelight"]==true)
                                $( "#lum" ).slider("enable");
                                
                        ifLampada.colorePercepito=ifLampada.toColorePercepito(ifLampada.coloreReale);
                        $('#bg').css( "background-color",ifLampada.colorePercepito.toHexString() );
                        
                        $('#lum').slider("value",ifLampada.lum);
                        $('#lum_perc').html(ifLampada.lum+"%");
                        
                }
                else if (Elettrodomestici.listaElettrodomestici[i].stato==0){
                        _stato="OFF";
                        class_stato="OFF";
                        ifLampada.stato=0;
                }
                else{
                        ifLampada.stato=-1;
                }
        }else{
                ifLampada.stato=-1;
        }
    	
        $("#Interfaccia #OnOffControl .btnToggle").removeClass("ON");
        $("#Interfaccia #OnOffControl .btnToggle").removeClass("OFF");
        $("#Interfaccia #OnOffControl .btnToggle").removeClass("NP");
        $("#Interfaccia #OnOffControl .btnToggle").addClass(class_stato);
        $("#Interfaccia #OnOffControl .btnSync").addClass(class_stato);
        if (class_stato=="NP") {
                $("#Interfaccia #OnOffControl .btnToggle").text(_stato);
                $('#Interfaccia #lum').slider("value",0);
                $('#Interfaccia #lum_perc').html(0+"%");
                $('#bg').css( "background-color","#333" );
                $( "#lum" ).slider("disable");
        }

	if(ifLampada.stato==0){
                $('#Interfaccia #lum').slider("value",0);
                $('#Interfaccia #lum_perc').html(0+"%");
                $('#bg').css( "background-color","#333" );
        }
        if(ifLampada.stato==-1){
                $('#Interfaccia #lum').slider("value",0);
                $('#Interfaccia #lum_perc').html(0+"%");
                $('#bg').css( "background-color","#333" );
                $( "#lum" ).slider("disable");
        }
        
        
}

ifLampada.sync=function(){
        var pid=$("#Interfaccia").data("pid");
        
        if (ifLampada.clusters==null) {
                return;
        }
        
        if(InterfaceEnergyHome.mode==-2)
        {
        	return;
        }
        
        if(ifLampada.clusters["org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer"]==true && ifLampada.stato!=-1){
                
                Elettrodomestici.addSpinner("#Interfaccia", "#0a0a0a");
                
                InterfaceEnergyHome.objService.colorControlGetColorHSL(function(result, err){
                        
                        Elettrodomestici.removeSpinner("#Interfaccia");
                        
                        if (err!=null) {
                                ifLampada.update(true);
                                
                        }else if (result!=null) {
                                //console.log(result["map"].hue+" "+result["map"].saturation+" "+result["map"].level+" "+result["map"].Errore);
                                if (result["map"]["Errore"]==null) {
                                        var h= result["map"].hue;  
                                        var s= result["map"].saturation;  
                                        var l= result["map"].level;
                                        
                                        //ifLampada.lum=l;
                                        
                                        var tc=ifLampada.coloreReale;//tinycolor(c);
                                        var hsl=tc.toHsl();
                                        var ll=Math.round(l/254*100);
                                        if (ll<0) {
                                                ll=50;
                                        }
                                        hsl.l=l/255;
                                        if (hsl.l<0) {
                                                hsl.l=0.5;
                                        }
                                        hsl.h=h/254*360;
                                        hsl.s=s/254;
                                        var tc= tinycolor(hsl);
                                        ifLampada.coloreReale=tc;
                                        ifLampada.colorePercepito=ifLampada.toColorePercepito(ifLampada.coloreReale);
                                        
                                        if (ifLampada.stato==1) {
                                                $('#bg').css( "background-color",ifLampada.colorePercepito.toHexString() );
                                                if (l>1) {
                                                    
                                                    $('#lum').slider("value",ll);
                                                    ifLampada.lum=ll;
                                                    $('#lum_perc').html(ifLampada.lum+"%");
                                                }
                                        }
                                        
                                        ifLampada.fb.setColor(tc.toHexString());
                                        
                                        
                                        
                                }
                        }
                }, pid);  
        
        }
        
        
}
