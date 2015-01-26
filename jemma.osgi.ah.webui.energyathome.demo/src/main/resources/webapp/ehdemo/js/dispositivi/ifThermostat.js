var ifThermostat =  { 
        timeout_timer:null,
        clusters:{},
        UPDATE_FREQ: 30000,
        stato:1 //1=acceso
}

ifThermostat.init=function(clusters){  

        ifThermostat.timeout_timer=null;
        ifThermostat.stato=-1;
        
        
        ifThermostat.update(true);
    
};

ifThermostat.updateIcon=function(stato){
        
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

ifThermostat.update= function(now){
        
        var t= new Date().getTime();
        
        var i= $("#Interfaccia").data("current_index");

        //var consumo=Elettrodomestici.listaElettrodomestici[i].consumo;
        //if (consumo!="n.a.") {
        //        consumo=Math.round(Elettrodomestici.listaElettrodomestici[i].consumo)+"W";
        //}
        //$("#Interfaccia .StatoElettrodomestico .consumo").text(consumo);
        $("#Interfaccia .StatoElettrodomestico .posizione_value").text(Elettrodomestici.locazioni[Elettrodomestici.listaElettrodomestici[i].location]);
        
        
        
        //Non aggiorno oltre l'interfaccia se passa troppo poco tempo dall'ultimo comando
        if (!now && ifThermostat.timeout_timer!=null) {
                if (t-ifThermostat.timeout_timer < ifThermostat.UPDATE_FREQ) {
                        return;
                }
        }
        
        ifThermostat.timeout_timer=t;
        
        var class_stato = "NP"
        var t_value = "n.a.";
        var h_value = "n.a.";
        ifThermostat.stato = -1;
        
        if (Elettrodomestici.listaElettrodomestici[i].connessione==2) {
                
                ifThermostat.stato=1;
                if (Elettrodomestici.listaElettrodomestici[i].temperature != undefined)
                	t_value=Elettrodomestici.listaElettrodomestici[i].temperature+" C";
                if (Elettrodomestici.listaElettrodomestici[i].humidity != undefined)
                	h_value=Elettrodomestici.listaElettrodomestici[i].humidity+" %"
        }
        
        $("#Interfaccia #Temperatura #temperatura_value").text(t_value);
        $("#Interfaccia #Humidity #humidity_value").text(h_value);
        
        ifThermostat.updateIcon(ifThermostat.stato);

        
}
