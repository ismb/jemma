var ifLampadaMac =  { 
        baseColor: null,
        isBusy:false,
        colorePercepito:null,
        coloreReale:null,
        bckhsl:null,
        max: null,
        lum:null,
        timeout_timer:null,
        clusters:{},
        fb:null, //farbtastic color picker
        UPDATE_FREQ: 30000,
        stato:1, //1=acceso;0=spento;-1=disconesso
        stato_colore:1 //1=acceso;0=spento;-1=disconesso
}

ifLampadaMac.init=function(_clusters){
        
        
        //fiz immagine lampadina
        
        //$("#ifLampadaMac #bg img").css("width","auto");
        $("#ifLampadaMac #bg").width($("#ifLampadaMac #bg").height());
        
        
        ifLampadaMac.baseColor=tinycolor("#9cc31c");
        ifLampadaMac.bckhsl=ifLampadaMac.baseColor.toHsl();
        ifLampadaMac.coloreReale=ifLampadaMac.baseColor;
        
        $('#bg').css( "background-color",ifLampadaMac.baseColor.toHexString() );
        
        
        
        ifLampadaMac.max=100;
        ifLampadaMac.lum=ifLampadaMac.max;
        ifLampadaMac.timeout_timer=new Date().getTime();
        ifLampadaMac.stato=-1;
        ifLampadaMac.clusters=_clusters;
        
        
        
        $( "#lum" ).slider({
            range: "min",
            value: ifLampadaMac.max,
            min: 0,
            max: ifLampadaMac.max,
            slide: function( event, ui ) {
                
                var nc=ifLampadaMac.getWhiteColor();

                if (ui.value>0 ) {
                    $("#onoff").addClass("ON");
                    $("#onoff").removeClass("OFF");
                    ifLampadaMac.stato=1;
                    $('#bg').css( "background-color", nc.toHexString() );
                }else if (ui.value==0 ) {
                    $("#onoff").addClass("OFF");
                    $("#onoff").removeClass("ON");
                    ifLampadaMac.stato=0;
                    $('#bg').css( "background-color","#202020");
                }
                ifLampadaMac.lum=ui.value;
                $('#lum_perc').html(ifLampadaMac.lum+"%");
            },
            start: function(event, ui ){
                ifLampadaMac.isBusy=true;
            },
            stop: function(event, ui ){
                ifLampadaMac.isBusy=false;
                
                var pid=$("#Interfaccia").data("pid");
                var value=Math.round(ifLampadaMac.lum/100*254);
                var time=10;
                InterfaceEnergyHome.objService.levelControlExecMoveToLevelWithOnOff(function(result, err){
                    Elettrodomestici.removeSpinner("#Interfaccia");
                    $("#ifLampadaMac").show();
                        
                        if (err!=null) {
                                ifLampadaMac.update(true);        
                        }else if (result!=null) {
                                if (result==true) {
                                        ifLampadaMac.timeout_timer=new Date().getTime();
                                }
                        }
                        
                },pid,value,time);
                Elettrodomestici.addSpinner("#Interfaccia","none");
                $("#ifLampadaMac").hide();
                
            }
        });
        
        
        $( "#onoff" ).click(function( event ) {
                event.preventDefault();
                var myId= "#"+$(this).attr("id");
                var i = $("#Interfaccia").data("current_index");
                if (ifLampadaMac.stato==1) {
   
                        var pid=$("#Interfaccia").data("pid");
                        if (pid==undefined)
                                return;
                        
                        InterfaceEnergyHome.objService.setDeviceState(function(result, err){
                            Elettrodomestici.removeSpinner("#Interfaccia");
                            $("#ifLampadaMac").show();
                                if (err!=null) {
                                        ifLampadaMac.update(true);
                                        
                                }else if (result!=null) {
                                        if (result==true) {
                                                
                                                ifLampadaMac.stato=0;
                       
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
                                        ifLampadaMac.timeout_timer=new Date().getTime();
                                }
                        }, pid,0);
                        Elettrodomestici.addSpinner("#Interfaccia","none");
                        $("#ifLampadaMac").hide();
                    
                    
                }else if(ifLampadaMac.stato==0){
                    
                    var pid=$("#Interfaccia").data("pid");
                    if (pid==undefined)
                            return;
                    
                    InterfaceEnergyHome.objService.setDeviceState(function(result, err){
                        Elettrodomestici.removeSpinner("#Interfaccia");
                        $("#ifLampadaMac").show();
                            if (err!=null) {
                                    ifLampadaMac.update(true);
                            }else if (result!=null) {
                                    if (result==true) {
                                        
                                        ifLampadaMac.stato=1;   
                                        
                                        var nc=ifLampadaMac.getWhiteColor();
                                            
                                        $('#bg').css( "background-color",nc.toHexString() );
                                        $('#lum').slider("value",ifLampadaMac.lum);
                                            
                                        $(myId).addClass("ON");
                                        $(myId).removeClass("OFF");
                                        $('#lum_perc').html(ifLampadaMac.lum+"%");

                                        ifLampadaMac.timeout_timer=new Date().getTime();
                                        
                                    	$("#device_" + i + " .StatoElettrodomestico .stato").text("ON");
                                    	
                                        $("#device_" + i).removeClass("ONOFF");
                                        $("#device_" + i).removeClass("OFF");
                                        $("#device_" + i).removeClass("ON");
                                        $("#device_" + i).addClass("ON");
                                    }
                            }
                                     
                    }, pid,1);
                    Elettrodomestici.addSpinner("#Interfaccia","none");
                    $("#ifLampadaMac").hide();
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
            
        $("#onoffluce").attr('unselectable','on')
            .css({'-moz-user-select':'-moz-none',
                  '-moz-user-select':'none',
                  '-o-user-select':'none',
                  '-khtml-user-select':'none', /* you could also put this in a class */
                  '-webkit-user-select':'none',/* and add the CSS class here instead */
                  '-ms-user-select':'none',
                  'user-select':'none'
        }).bind('selectstart', function(){ return false; });
        
        if( ifLampadaMac.clusters!=null){
                if(ifLampadaMac.clusters["org.energy_home.jemma.ah.cluster.zigbee.general.LevelControlServer"]!=true )
                        $( "#lum" ).slider("disable");
                
                
                
                if(ifLampadaMac.clusters["org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer"]==true){
                        
                        ifLampadaMac.fb=$.farbtastic('#picker');
                        
                        
                        ifLampadaMac.fb.setColor(ifLampadaMac.baseColor.toHexString());
                        
                        ifLampadaMac.fb.linkTo(function(color){
                            
                                var c=tinycolor(color);
                                var c2=c.toHsl();
                                var rc= ifLampadaMac.coloreReale.toHsl();
                        
                                
                                rc.h=c2.h;
                                
                                rc.s=1;
                                
                                ifLampadaMac.coloreReale= tinycolor(rc);
                                ifLampadaMac.colorePercepito=ifLampadaMac.toColorePercepito(ifLampadaMac.coloreReale);
                                
                        });
                        
                        ifLampadaMac.fb.onStart(function(){
                                ifLampadaMac.isBusy=true;
                        });
                        ifLampadaMac.fb.onStop(function(){
                                        ifLampadaMac.isBusy=false;
                                        var rc= ifLampadaMac.coloreReale.toHsl();
                                        var h=Math.round(rc.h/360*254);
                                        
                                        var s=254;//Math.round( rc.s*254);
                                        
                                        var pid=$("#Interfaccia").data("pid");
                                        if (pid==undefined)
                                                return;
                                        
                                        InterfaceEnergyHome.objService.colorControlMoveToColorHS(function(result, err){
                                        	Elettrodomestici.removeSpinner("#Interfaccia");
                                            $("#ifLampadaMac").show();
                                                
                                                if (err!=null) {
                                                        ifLampadaMac.update(true);     
                                                }else if (result!=null) {
                                                        if (result==true) {
                                                                ifLampadaMac.timeout_timer=new Date().getTime();
                                                                $("#Interfaccia #onoffluce").removeClass("ON");
                                                                $("#Interfaccia #onoffluce").removeClass("OFF");
                                                                $("#Interfaccia #onoffluce").removeClass("NP");
                                                                $("#Interfaccia #onoffluce").addClass("ON");
                                                        }
                                                }
                                             
                                        }, pid,h,s,10);
                                        Elettrodomestici.addSpinner("#Interfaccia","none");
                                        $("#ifLampadaMac").hide();
                        });
                        
                        
                        
                        $( "#onoffluce" ).click(function( event ) {
                                event.preventDefault();
                                var myId= "#"+$(this).attr("id");
                                
                                var tc=ifLampadaMac.coloreReale.toHsl();
                               
                                
                                if (ifLampadaMac.stato_colore==1) {
                   
                                        var pid=$("#Interfaccia").data("pid");
                                        if (pid==undefined)
                                                return;
                                        
                                        var h=Math.round(tc.h/360*254);
                                        var s=0;//Math.round( rc.s*254);
                                        InterfaceEnergyHome.objService.colorControlMoveToColorHS(function(result, err){
                                        	Elettrodomestici.removeSpinner("#Interfaccia");
                                            $("#ifLampadaMac").show();
                                                if (err!=null) {
                                                        ifLampadaMac.update(true);
                                                        
                                                }else if (result!=null) {
                                                        //if (result==true) {
                                                                                                
                                                                $(myId).addClass("OFF");
                                                                $(myId).removeClass("ON");
                                                                ifLampadaMac.stato_colore=0;
                                                        //}
                                                        ifLampadaMac.timeout_timer=new Date().getTime();
                                                        
                                                }
                                        }, pid,h,s,10);
                                        Elettrodomestici.addSpinner("#Interfaccia","none");
                                        $("#ifLampadaMac").hide();
                                    
                                    
                                }else if(ifLampadaMac.stato_colore==0){
                                    
                                    var pid=$("#Interfaccia").data("pid");
                                    if (pid==undefined)
                                            return;
                                    
                                        var h=Math.round(tc.h/360*254);
                                        var s=254;//Math.round( rc.s*254);
                                        InterfaceEnergyHome.objService.colorControlMoveToColorHS(function(result, err){
                                        	Elettrodomestici.removeSpinner("#Interfaccia");
                                            $("#ifLampadaMac").show();
                                                if (err!=null) {
                                                        ifLampadaMac.update(true);
                                                        
                                                }else if (result!=null) {
                                                        //if (result==true) {
                                                                                                
                                                                $(myId).addClass("ON");
                                                                $(myId).removeClass("OFF");
                                                                ifLampadaMac.stato_colore=1;
                                                                
                                
                                                        //}
                                                        ifLampadaMac.timeout_timer=new Date().getTime();
                                                }
                                        }, pid,h,s,10);
                                        Elettrodomestici.addSpinner("#Interfaccia","none");
                                        $("#ifLampadaMac").hide();
                                }
                               
                                
                        });
                        
                        
                        
                        
                }
        }
        ifLampadaMac.update(true);
};

ifLampadaMac.getWhiteColor=function(){
        var tc=ifLampadaMac.baseColor;//tinycolor(c);
        var hsl=tc.toHsl();
        hsl.l=ifLampadaMac.lum/100;
        if (hsl.l>=1) {
                hsl.l=0.99;
        }
        if (hsl.l==0) {
                hsl.l=0.01;
        }
        var nc=ifLampadaMac.toColorePercepito(tinycolor(hsl));
        return nc;
}

ifLampadaMac.toColorePercepito=function(color){
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

ifLampadaMac.update= function(now){
        
        
        
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
        if (!now && ifLampadaMac.timeout_timer!=null) {
                if (t-ifLampadaMac.timeout_timer < ifLampadaMac.UPDATE_FREQ) {
                        return;
                }
        }
        
                
        

        if (ifLampadaMac.isBusy) {
                return;
        }
        
        ifLampadaMac.timeout_timer=t;
        
        var class_stato="NP"
        var _stato="";
        
        if (Elettrodomestici.listaElettrodomestici[i].connessione==2) {
                
                if (Elettrodomestici.listaElettrodomestici[i].stato==1){
                        _stato="ON";
                        class_stato="ON";
                        ifLampadaMac.stato=1;

                                
                        var nc=ifLampadaMac.getWhiteColor();
                        $('#bg').css( "background-color",nc.toHexString() );
                        
                        $('#lum').slider("value",ifLampadaMac.lum);
                        $('#lum_perc').html(ifLampadaMac.lum+"%");
                        $("#picker").show();
                        
                }
                else if (Elettrodomestici.listaElettrodomestici[i].stato==0){
                        _stato="OFF";
                        class_stato="OFF";
                        ifLampadaMac.stato=0;
                        $("#picker").show();
                        
                }
                else{
                        ifLampadaMac.stato=-1;
                }
        }else{
                ifLampadaMac.stato=-1;
        }
    	
        $("#Interfaccia #OnOffControl .btnToggle").removeClass("ON");
        $("#Interfaccia #OnOffControl .btnToggle").removeClass("OFF");
        $("#Interfaccia #OnOffControl .btnToggle").removeClass("NP");
        $("#Interfaccia #OnOffControl .btnToggle").addClass(class_stato);
       
        if (class_stato=="NP") {
                $("#Interfaccia #OnOffControl .btnToggle").text(_stato);
                $("#Interfaccia #onoffluce").text(_stato);
                $('#Interfaccia #lum').slider("value",0);
                $('#Interfaccia #lum_perc').html(0+"%");
                $('#bg').css( "background-color","#333" );
                $( "#lum" ).slider("disable");
                
                $("#Interfaccia #onoffluce").removeClass("ON");
                $("#Interfaccia #onoffluce").removeClass("OFF");
                $("#Interfaccia #onoffluce").removeClass("NP");
                $("#Interfaccia #onoffluce").addClass("NP");
                $("#picker").hide();
                
                
        }

	if(ifLampadaMac.stato==0){
                $('#Interfaccia #lum').slider("value",0);
                $('#Interfaccia #lum_perc').html(0+"%");
                $('#bg').css( "background-color","#333" );
        }
        if(ifLampadaMac.stato==-1){
                $('#Interfaccia #lum').slider("value",0);
                $('#Interfaccia #lum_perc').html(0+"%");
                $('#bg').css( "background-color","#333" );
                $( "#lum" ).slider("disable");
        }
        
        
        
        var pid=$("#Interfaccia").data("pid");
        //Aggiorno i valori dello slider e del colore
        if (ifLampadaMac.clusters==null) {
                return;
        }
        if(ifLampadaMac.clusters["org.energy_home.jemma.ah.cluster.zigbee.general.LevelControlServer"]==true){
                
                if (ifLampadaMac.stato!=-1) {
                        $( "#lum" ).slider("enable");
                }
                
                InterfaceEnergyHome.objService.levelControlGetCurrentValue(function(result, err){
                    Elettrodomestici.removeSpinner("#Interfaccia");
                    $("#ifLampadaMac").show();
                        if (err!=null) {
                                code
                                
                        }else if (result!=null) {
                                if (result>1) {
                                        var ll=Math.round(result/254*100);
                                        $('#lum').slider("value",ll);
                                        ifLampadaMac.lum=ll;
                                        $('#lum_perc').html(ifLampadaMac.lum+"%");
                                        var nc=ifLampadaMac.getWhiteColor();  
                                        $('#bg').css( "background-color",nc.toHexString() );
                                }
                               
                        }
                }, pid);  
                Elettrodomestici.addSpinner("#Interfaccia","none");
                $("#ifLampadaMac").hide();   
        
        }
        
        
        if(ifLampadaMac.clusters["org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer"]==true){
                
                Elettrodomestici.addSpinner("#Interfaccia", "#0a0a0a");
                
                InterfaceEnergyHome.objService.colorControlGetColorHS(function(result, err){
                    Elettrodomestici.removeSpinner("#Interfaccia");
                    $("#ifLampadaMac").show();
                        
                        Elettrodomestici.removeSpinner("#Interfaccia");
                        
                        if (err!=null) {
                                ifLampadaMac.update(true);
                                
                        }else if (result!=null) {
                                //console.log(result["map"].hue+" "+result["map"].saturation+" "+result["map"].level+" "+result["map"].Errore);
                                if (result["map"]["Errore"]==null) {
                                        var h= result["map"].hue;  
                                        var s= result["map"].saturation;  
                                        var l= 254;//result["map"].level;
                                        
                                        var tc=ifLampadaMac.coloreReale;//tinycolor(c);
                                        var hsl=tc.toHsl();
                                        
                                        hsl.l=l/255;
                                        hsl.h=h/254*360;
                                        if (hsl.l<0) {
                                                hsl.l=0.5;
                                        }
                                        hsl.s=s/254;
                                        if (s==0) {
                                                hsl.s=1;         
                                        }
                                        var tc= tinycolor(hsl);
                                        ifLampadaMac.coloreReale=tc;
                                        ifLampadaMac.colorePercepito=ifLampadaMac.toColorePercepito(ifLampadaMac.coloreReale);
                                        
                                        ifLampadaMac.fb.setColor(tc.toHexString());
                                        
                                        $("#Interfaccia #onoffluce").removeClass("ON");
                                        $("#Interfaccia #onoffluce").removeClass("OFF");
                                        $("#Interfaccia #onoffluce").removeClass("NP");
                                        if (s<1) {
                                                ifLampadaMac.stato_colore=0;
                                                $("#Interfaccia #onoffluce").addClass("OFF");
                                        }else{
                                                ifLampadaMac.stato_colore=1;
                                                $("#Interfaccia #onoffluce").addClass("ON");
                                        }
                                        
                                }
                        }
                }, pid);  
                Elettrodomestici.addSpinner("#Interfaccia","none");
                $("#ifLampadaMac").hide();
        
        }
        
        
}
