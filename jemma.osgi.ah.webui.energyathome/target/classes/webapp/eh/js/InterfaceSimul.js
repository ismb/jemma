/**
 * Valori per simulazione server
 */
var InterfaceSimul = {  
    //GetListDevices :  '{"retStatus": 0, "retValue":[ { "name": "Standby TV", "device_state": 4, "device_state_avail": "false", "availability": 2, "pid": "HacApplication.StandbyManager", "device_status": 0, "type": "StandbyManagerImpl", "icon": "standby.png", "location": { "name": "Altro", "icon": "other.png", "pid": "9"}, "device_value": "" }, { "name": "Contatore", "device_state": 4, "device_state_avail": "false", "availability": 2, "pid": "HacApplication.HomeMeter", "device_status": 0, "type": "HomeMeterImpl", "icon": "elettrodomestici.png", "location": { "name": "Altro", "icon": "other.png", "pid": "9"}, "device_value": "0.0 W (0)" }, { "name": "Sovraccarichi", "device_state": 4, "device_state_avail": "false", "availability": 2, "pid": "HacApplication.OverloadControl", "device_status": 0, "type": "OverloadControlImpl", "icon": "overload.png", "location": { "name": "Altro", "icon": "other.png", "pid": "9"}, "device_value": "0 W" }, { "name": "TV CRT", "category": { "name": "TV", "icon": "television.png" }, "device_state": 2, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099B54", "device_status": 0, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "television.png", "location": { "name": "Cucina", "icon": "kitchen.png", "pid": "1"}, "device_value": "nd" }, { "name": "TV LCD", "category": { "name": "TV", "icon": "television.png" }, "device_state": 0, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099F5C", "device_status": 2, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "television.png", "location": { "name": "Soggiorno", "icon": "livingroom.png", "pid": "6"}, "device_value": "nd" }, { "name": "SmartPlug 2", "category": { "name": "Lampadina", "icon": "lampadina.png" }, "device_state": 0, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099F94", "device_status": 0, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "lampadina.png", "device_value": "0.000 kW" }, { "name": "Zona PC", "category": { "name": "PC", "icon": "pc.png" }, "device_state": 1, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F00000DB7AB", "device_status": 0, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "pc.png", "location": { "name": "Studio", "icon": "studio.png", "pid": "2"}, "device_value": "nd" }, { "name": "Lavastoviglie", "category": { "name": "Lavastoviglie", "icon": "lavastoviglie.png" }, "device_state": 0, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099F8F", "device_status": 2, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "lavastoviglie.png", "location": { "name": "Cucina", "icon": "kitchen.png", "pid": "1"}, "device_value": "nd" }, { "name": "SmartPlug 1", "category": { "name": "Lampadina", "icon": "lampadina.png" }, "device_state": 1, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099F6B", "device_status": 0, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "lampadina.png", "device_value": "nd" }, { "name": "Frigorifero", "category": { "name": "Frigorifero", "icon": "frigorifero.png" }, "device_state": 1, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099B82", "device_status": 0, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "frigorifero.png", "location": { "name": "Sala da pranzo", "icon": "kitchen.png", "pid": "1"}, "device_value": "nd" }, { "name": "Aqualtis", "device_state": 1, "device_state_avail": "true", "availability": 2, "pid": "zigbee.1111000000001111", "device_status": 0, "type": "ZBIndesitDriverImpl", "icon": "aqualtis.png", "location": { "name": "Bagno", "icon": "bathroom.png", "pid": "4"}, "device_value": "nd" }, { "name": "Forno", "device_state": 0, "device_state_avail": "true", "availability": 2, "pid": "zigbee.3333000000003333", "device_status": 0, "type": "ZBIndesitOvenImpl", "icon": "openspace.png", "location": { "name": "Cucina", "icon": "kitchen.png", "pid": "1"}, "device_value": "nd" } ]}',
    //GetListDevices :  '{"retStatus": 0, "retValue":[  { "name": "Contatore", "device_state": 4, "device_state_avail": "false", "availability": 2, "pid": "HacApplication.HomeMeter", "device_status": 0, "type": "HomeMeterImpl", "icon": "elettrodomestici.png", "location": { "name": "Altro", "icon": "other.png", "pid": "9"}, "device_value": "0.0 W (0)" }, { "name": "TV CRT", "category": { "name": "TV", "icon": "television.png" }, "device_state": 2, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099B54", "device_status": 0, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "television.png", "location": { "name": "Cucina", "icon": "kitchen.png", "pid": "1"}, "device_value": "nd" }, { "name": "TV LCD", "category": { "name": "TV", "icon": "television.png" }, "device_state": 0, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099F5C", "device_status": 2, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "television.png", "location": { "name": "Soggiorno", "icon": "livingroom.png", "pid": "6"}, "device_value": "nd" }, { "name": "Rex", "category": { "name": "Lavatrice", "icon": "Rex.png" }, "device_state": 0, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099F94", "device_status": 0, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "rex.png", "device_value": "0.000 kW" }, { "name": "Zona PC", "category": { "name": "PC", "icon": "pc.png" }, "device_state": 1, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F00000DB7AB", "device_status": 0, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "pc.png", "location": { "name": "Studio", "icon": "studio.png", "pid": "2"}, "device_value": "nd" }, { "name": "Lavastoviglie", "category": { "name": "Lavastoviglie", "icon": "lavastoviglie.png" }, "device_state": 0, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099F8F", "device_status": 2, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "lavastoviglie.png", "location": { "name": "Cucina", "icon": "kitchen.png", "pid": "1"}, "device_value": "nd" }, { "name": "SmartPlug 1", "category": { "name": "Lampadina", "icon": "lampadina.png" }, "device_state": 1, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099F6B", "device_status": 0, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "lampadina.png", "device_value": "nd" }, { "name": "Frigorifero", "category": { "name": "Frigorifero", "icon": "frigorifero.png" }, "device_state": 1, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099B82", "device_status": 0, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "frigorifero.png", "location": { "name": "Sala da pranzo", "icon": "kitchen.png", "pid": "1"}, "device_value": "nd" }, { "name": "Aqualtis", "device_state": 1, "device_state_avail": "true", "availability": 2, "pid": "zigbee.1111000000001111", "device_status": 0, "type": "ZBIndesitDriverImpl", "icon": "aqualtis.png", "location": { "name": "Bagno", "icon": "bathroom.png", "pid": "4"}, "device_value": "nd" }, { "name": "Forno", "device_state": 0, "device_state_avail": "true", "availability": 2, "pid": "zigbee.3333000000003333", "device_status": 0, "type": "ZBIndesitOvenImpl", "icon": "openspace.png", "location": { "name": "Cucina", "icon": "kitchen.png", "pid": "1"}, "device_value": "nd" } ]}',
    GetListDevices :  '{"retStatus": 0, "retValue":[  { "name": "Contatore", "device_state": 4, "device_state_avail": "false", "availability": 2, "pid": "HacApplication.HomeMeter", "device_status": 10, "type": "HomeMeterImpl", "icon": "elettrodomestici.png", "location": { "name": "Altro", "icon": "other.png", "pid": "9"}, "device_value": "0.0 W (0)" }, { "name": "TV LCD", "category": { "name": "TV", "icon": "tv.png" }, "device_state": 0, "device_state_avail": "true", "availability": 1, "pid": "zigbee.000D6F0000099F5C", "device_status": 2, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "tv.png", "location": { "name": "Soggiorno", "icon": "livingroom.png", "pid": "6"}, "device_value": "nd" }, { "name": "Rex", "category": { "name": "Lavatrice", "icon": "Rex.png" }, "device_state": 0, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099F94", "device_status": 8, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "lavatrice.png", "device_value": "0 w" }, { "name": "Zona PC", "category": { "name": "PC", "icon": "pc.png" }, "device_state": 1, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F00000DB7AB", "device_status": 0, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "pczone.png", "location": { "name": "Soggiorno", "icon": "studio.png", "pid": "2"}, "device_value": "5 w" }, { "name": "Lavastoviglie", "category": { "name": "Lavastoviglie", "icon": "lavastoviglie.png" }, "device_state": 0, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099F8F", "device_status": 9, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "lavastoviglie.png", "location": { "name": "Cucina", "icon": "kitchen.png", "pid": "1"}, "device_value": "0 w" }, { "name": "Frigorifero", "category": { "name": "Frigorifero", "icon": "frigorifero.png" }, "device_state": 1, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099B82", "device_status": 10, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "frigorifero.png", "location": { "name": "Sala da pranzo", "icon": "kitchen.png", "pid": "1"}, "device_value": "10 w" }, { "name": "Aqualtis", "device_state": 1, "device_state_avail": "true", "availability": 2, "pid": "zigbee.1111000000001111", "device_status": 20, "type": "ZBIndesitDriverImpl", "icon": "lavatrice.png", "location": { "name": "Bagno", "icon": "bathroom.png", "pid": "4"}, "device_value": "18 w" }, { "name": "Forno", "device_state": 0, "device_state_avail": "true", "availability": 2, "pid": "zigbee.3333000000003333", "device_status": 0, "type": "ZBIndesitOvenImpl", "icon": "forno.png", "location": { "name": "Cucina", "icon": "kitchen.png", "pid": "1"}, "device_value": "0 w" } ]}',
    //GetListDevices :  '{"retStatus": 0, "retValue":[  { "name": "Contatore", "device_state": 4, "device_state_avail": "false", "availability": 2, "pid": "HacApplication.HomeMeter", "device_status": 10, "type": "HomeMeterImpl", "icon": "elettrodomestici.png", "location": { "name": "Altro", "icon": "other.png", "pid": "9"}, "device_value": "0.0 W (0)" }, { "name": "TV LCD", "category": { "name": "TV", "icon": "tv.png" }, "device_state": 0, "device_state_avail": "true", "availability": 1, "pid": "zigbee.000D6F0000099F5C", "device_status": 2, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "tv.png", "location": { "name": "Soggiorno", "icon": "livingroom.png", "pid": "6"}, "device_value": "nd" }, { "name": "Zona PC", "category": { "name": "PC", "icon": "pc.png" }, "device_state": 1, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F00000DB7AB", "device_status": 0, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "pczone.png", "location": { "name": "Studio", "icon": "studio.png", "pid": "2"}, "device_value": "5 w" }, { "name": "Lavastoviglie", "category": { "name": "Lavastoviglie", "icon": "lavastoviglie.png" }, "device_state": 0, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099F8F", "device_status": 9, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "lavastoviglie.png", "location": { "name": "Cucina", "icon": "kitchen.png", "pid": "1"}, "device_value": "0 w" }, { "name": "Frigorifero", "category": { "name": "Frigorifero", "icon": "frigorifero.png" }, "device_state": 1, "device_state_avail": "true", "availability": 2, "pid": "zigbee.000D6F0000099B82", "device_status": 10, "type": "ZBPowerMeterSwitchDriverImpl", "icon": "frigorifero.png", "location": { "name": "Cucina", "icon": "kitchen.png", "pid": "1"}, "device_value": "10 w" }, { "name": "Aqualtis", "device_state": 1, "device_state_avail": "true", "availability": 2, "pid": "zigbee.1111000000001111", "device_status": 20, "type": "ZBIndesitDriverImpl", "icon": "lavatrice.png", "location": { "name": "Bagno", "icon": "bathroom.png", "pid": "4"}, "device_value": "18 w" }, { "name": "Forno", "device_state": 0, "device_state_avail": "true", "availability": 2, "pid": "zigbee.3333000000003333", "device_status": 0, "type": "ZBIndesitOvenImpl", "icon": "forno.png", "location": { "name": "Cucina", "icon": "kitchen.png", "pid": "1"}, "device_value": "0 w" } ]}',
    

    GetConsumoAttuale : '{"retStatus": 0, "retValue":{"type":"double", "value":500.0}}',
    //GetEnergyAll  : '{"retStatus": 0, "retValue":["Frigorifero", "20", "30",  "Zona TV", "10", "32", "Lavatrice", "50", "70", "Forno", "10", "10", "Altro", "40", "45"]}',
    //GetEnergyAll  : '{"retStatus": 0, "retValue":["Boiler", "22", "15", "Frigorifero", "12", "15",  "Zona TV", "5", "12", "Lavatrice", "9", "11",  "Altro", "16", "30"]}',
    EnergyAll  : ["Boiler", 43, 30, "Zona TV", 10, 23, "Frigorifero", 24, 30,  "Lavatrice", 18, 21,  "Altro", 30, 58],
    //ConsumoMensile  : '{"retStatus": 0, "retValue":[[100, 50], [150, 20], [50, 80], [80,80], [70, 30], [120, 100], [100, 80], [20, 60], [50, 50], [170, 30], [80, 60], [50, 50], [80,100]]}',
    //ConsumoMensile1  : '{"retStatus": 0, "retValue":[[60, 50], [50, 20], [50, 20], [80,50], [40, 30], [70, 100], [150, 80], [80, 60], [50, 90], [190, 30], [80, 60], [60, 50], [90,20]]}',
    SovraccarichiFixed : [[(new Date("Jan 2, 2010 12:30:00")).getTime(), 1],
        [(new Date("Jan 14, 2010 17:20:00")).getTime(), 1],[(new Date("Feb 3, 2010 20:10:00")).getTime(), 1],
        [(new Date("Feb 12, 2010 11:20:00")).getTime(), 1],[(new Date("Feb 13, 2010 19:50:00")).getTime(), 1],
        [(new Date("Mar 5, 2010 18:20:00")).getTime(), 1],[(new Date("Mar 19, 2010 17:50:00")).getTime(), 1],
        [(new Date("Apr 4, 2010 12:20:00")).getTime(), 1],[(new Date("Apr 13, 2010 19:10:00")).getTime(), 1],
        [(new Date("May 9, 2010 13:00:00")).getTime(), 1],[(new Date("May 17, 2010 19:40:00")).getTime(), 1],
        [(new Date("May 20, 2010 12:00:00")).getTime(), 1],[(new Date("Jun 5, 2010 18:20:00")).getTime(), 1],
        [(new Date("Jun 23, 2010 18:30:00")).getTime(), 1],[(new Date("Jun 27, 2010 19:40:00")).getTime(), 1],
        [(new Date("Jul 14, 2010 20:00:00")).getTime(), 1],[(new Date("Aug 27, 2010 18:40:00")).getTime(), 1],
        [(new Date("Sep 4, 2010 17:50:00")).getTime(), 0],[(new Date("Sep 23, 2010 18:40:00")).getTime(), 1],
        [(new Date("Sep 25, 2010 21:00:00")).getTime(), 0],[(new Date("Oct 12, 2010 18:40:00")).getTime(), 0],
        [(new Date("Oct 22, 2010 19:30:00")).getTime(), 1],[(new Date("Nov 12, 2010 20:40:00")).getTime(), 0],
        [(new Date("Nov 24, 2010 12:00:00")).getTime(), 0],[(new Date("Nov 27, 2010 18:40:00")).getTime(), 1],
        [(new Date("Nov 30, 2010 13:50:00")).getTime(), 0],[(new Date("Dec 7, 2010 19:00:00")).getTime(), 0],
        [(new Date("Dec 12, 2010 12:50:00")).getTime(), 0],[(new Date("Dec 17, 2010 17:40:00")).getTime(), 0],
        [(new Date("Dec 24, 2010 11:50:00")).getTime(), 0],[(new Date("Jan 6, 2011 19:20:00")).getTime(), 0],
        [(new Date("Jan 11, 2011 12:00:00")).getTime(), 1],[(new Date("Jan 17, 2011 18:40:00")).getTime(), 0],
        [(new Date("Jan 28, 2011 20:00:00")).getTime(), 0],[(new Date("Jan 30, 2011 11:40:00")).getTime(), 0],
        [(new Date("Feb 1, 2011 16:00:00")).getTime(), 0]],
    consumoArray : null, // salvo valori consumo attuale ogni volta che eseguo GetValueForAlert (richiamata in qualsiasi schermata
    consumoInterval : 5, // minuti di intervallo tra un valore e l'altro per il consumo giornaliero
    num : 0,
    sovraccarichi : null,
    storico : null, // array con date descrescenti (0 = mese attuale, 1= mese scorso, ecc.)
    ieri : null,
    storicoPercent : null, // array con percentuale consumo per ogni dispositivo
    odierno : null, 
    odiernoVal : [  /** ogni 5 minuti **/
        130, 130, 130, 130, 280, 280, 280, 130, 130, 130, 130, 130,  // 0:1
        130, 130, 130, 280, 280, 280, 130, 130, 130, 130, 130, 130,  // 1:2
        130, 130, 130, 280, 280, 280, 130, 130, 130, 130, 130, 130,  // 2:3
        130, 130, 130, 130, 280, 280, 280, 130, 130, 130, 130, 130,  // 3:4
        130, 130, 130, 130, 280, 280, 280, 130, 130, 130, 130, 130,  // 4:5
        130, 130, 130, 130, 280, 280, 130, 130, 130, 130, 130, 130,  // 5:6
        130, 130, 130, 130, 280, 280, 330, 430, 360, 830, 1130, 840, // 6:7
        945, 843, 1530, 1280, 2860, 2080, 1330, 1180, 860, 830, 1030, 444, // 7:8
        130, 130, 130, 130, 280, 288, 130, 130, 130, 130, 130, 130,  // 8:9
        130, 130, 130, 130, 130, 280, 330, 130, 130, 130, 130, 130,  // 9:10
        130, 130, 130, 130, 280, 270, 330, 130, 132, 128, 830, 840, // 10:11
        1130, 1340, 2130, 1230, 2809, 3280, 3390, 4390, 3060, 1830, 1130, 849, // 11:12
        730, 1430, 1306, 1250, 880, 1270, 930, 877, 660, 980, 1130, 640, // 12:13
        1130, 1380, 990, 930, 1280, 2430, 3303, 1430, 870, 830, 1110, 890, // 13:14
        230, 230, 360, 430, 280, 240, 330, 430, 760, 830, 150, 240, // 14:15
        130, 130, 130, 280, 310, 135, 130, 430, 360, 530, 1160, 848, // 15:16
        730, 1327, 1399, 1280, 1460, 1580, 3304, 1430, 3060, 830, 1230, 850, // 16:17
        745, 730, 930, 980, 2080, 1260, 1330, 1436, 1364, 1830, 1130, 640, // 17:18
        687, 890, 730, 680, 798, 760, 430, 860, 811, 923, 1030, 940, // 18:19
        830, 1230, 1390, 2480, 2870, 2678, 2330, 1430, 1360, 838, 1190, 765, // 19:20
        670, 680, 730, 880, 680, 620, 670, 530, 660, 560, 630, 740, // 20:21
        630, 670, 760, 580, 880, 765, 530, 490, 650, 834, 443, 340, // 21:22
        530, 507, 430, 480, 503, 450, 430, 430, 360, 678, 730, 805,   // 22:23
        330, 130, 130, 280, 280, 280, 130, 130, 130, 130, 130, 130 // 23:24
    ]
}


// creo array di valori (timestamp + value) partendo dall'inizio del giorno fino al momento attuale
// se ho memorizzato i valori di consumo istantaneo ne faccio la media (ogni 5 minuti) e li metto al 
// posto di quelli statici a partire dal momento in cui è iniziata la memorizzazione dei valori
InterfaceSimul.GetInstantConsume = function ()
{
    var tmp, i, j, tmpIni, tmpEnd, sum;
    var ret = '{"retStatus": 0, "retValue":[';
    var getReal = false;
    
    if (InterfaceSimul.odierno == null)
        InterfaceSimul.CreateOdierno();
 
    if ((InterfaceSimul.consumoArray != null) && (InterfaceSimul.consumoArray.length > 0))
    {
        tmp = InterfaceSimul.consumoArray[0][0];
        getReal = true;
    }
    else
        tmp = (GestDate.GetActualDate()).getTime();
    if (InterfaceSimul.consumoArray != null)
        Log.alert(90, "InterfaceSimul", "num consumoArray = " + InterfaceSimul.consumoArray.length);
    
    // memorizzo fino al momento attuale o al momento in cui ho iniziato a campionare i valori reali
    i = 0;
    while((i < InterfaceSimul.odierno.length) && (InterfaceSimul.odierno[i][0] < tmp))
    {
        if (i > 0)
            ret += ",";
        ret = ret +  '[' + InterfaceSimul.odierno[i][0] + ',' + InterfaceSimul.odierno[i][1] + ']';
        i++;
    }
    if (getReal)
    {
        // creo valori medi ogni 5 minuti da quando ho iniziato a campionare i valori (start applicazione)
        i = 0;
        //tmp = (GestDate.GetActualDate()).getTime();
        //sum = 0;
        
        while(i < InterfaceSimul.consumoArray.length) //&& (InterfaceSimul.consumoArray[i][0] < tmp))
        {
            j = 0;
            sum = 0;
            tmpIni = InterfaceSimul.consumoArray[i][0];
            tmpEnd = tmpIni + 60 * InterfaceSimul.consumoInterval * 1000;
            while ((i < InterfaceSimul.consumoArray.length) && (InterfaceSimul.consumoArray[i][0] <= tmpEnd))
            {
                sum += InterfaceSimul.consumoArray[i][1];
                Log.alert(90, "InterfaceSimul", "Prendo valori reali  = " + InterfaceSimul.consumoArray[i][1] + " i = " + i);
                j++;
                i++;
            }
            val = Math.floor(sum / j);
            ret = ret +  ',[' + tmpIni + ',' + val + ']';
            Log.alert(90, "InterfaceSimul", "Prendo valori reali val = " + val + " j = " + j);
            //i++;
        }
    }
    ret += ']}';
    Log.alert(90, "InterfaceSimul", "GetInstantConsume ret = " + ret);
    return ret;
}

// cerca nell'array dei sovraccarichi quelli relativi al periodo voluto
InterfaceSimul.GetSovraccarichi = function (ini, fine)
{
    var tmpI, tmpF, i, j;
    var ret = '{"retStatus": 0, "retValue":[';
    var rand_no;
    var val1, val2;

    //if (InterfaceSimul.sovraccarichi == null)
    //    InterfaceSimul.CreateSovraccarichi();
    Log.alert(90, "InterfaceSimul",  "ini = " + ini.toString() + " fine = " + fine.toString() + " len = " + InterfaceSimul.SovraccarichiFixed.length);
    tmpI = ini.getTime();
    tmpF = fine.getTime();
    j = 0;
    // parto dalla fine perchè sono messi in ordine descrescente
    for (i = InterfaceSimul.SovraccarichiFixed.length - 1; i>= 0; i--)
    {
        Log.alert(90, "InterfaceSimul",  "tmpI = " + tmpI + " tmpF = " + tmpF + " tmp = " + InterfaceSimul.SovraccarichiFixed[i][0]);
        val = InterfaceSimul.SovraccarichiFixed[i][0];
        if ((val>= tmpI) && (val <= tmpF))
        {
            if (j > 0)
                ret += ',';
            else
                j = 1;
           ret += '[' + InterfaceSimul.SovraccarichiFixed[i][0] + ',' + InterfaceSimul.SovraccarichiFixed[i][1] + ']';
           Log.alert(90, "InterfaceSimul", "i = " + i + " val = " + new Date(val).toString()); 
        }    
    }

    ret += ']}';
    Log.alert(90, "InterfaceSimul",  "GetSovraccarichi = " + ret);
    return ret;
}  

// crea un insieme di sovraccarichi da cui prendere poi i valori 
InterfaceSimul.CreateSovraccarichi = function ()
{
    var diff, tmpD, i, j, n, ini, fine;
    var rand_no;
    var val1, val2;
//  '{"retStatus": 0, "retValue":[[ 1257030047309, 10],[1257030049309, 10],[1257030050309, 20],[1257030051309, 21],[1257030064309, 27],[1257030047309, 50],[1257030047309, 51],[1257030047309, 54],[1257030047309, 61],[1257030047309, 61],[1257030047309, 61],[1257030047309, 75],[1257030047309, 76],[1257030047309, 80],[1257030047309, 81],[1257030047309, 101],[1257030047309, 110],[1257030047309, 150],[1257030047309, 250],[1257030047309, 256],[1257030047309, 280],[1257030047309, 278],[1257030047309, 250],[1257030047309, 251],[1257030047309, 531],[1257030047309, 501],[1257030047309, 481],[1257030047309, 431],[1257030047309, 441]]}',
    
    InterfaceSimul.sovraccarichi = new Array();
    fine = GestDate.GetActualDate();
    ini = new Date(fine.getTime());
    ini.setDate(ini.getDate() - 15);
    n = 0;
    for (j = 0; j < 25; j++)
    {
        diff = fine.getTime() - ini.getTime(); 
        rand_no =  Math.floor(Math.random() * 5);
    
        for (i = 0; i < rand_no; i++)
        {
            val1 = Math.floor(Math.random() * diff);
            tmpD = new Date(ini.getTime() + val1);
            rn = Math.random();
            if (rn > 0.5)
                val2 = 1;
            else
                val2 = 0;
            // tolgo ore troppo presto o troppo tardi
            if ((tmpD.getHours() >= 7) && (tmpD.getHours() < 22))
            {
                InterfaceSimul.sovraccarichi[n] = new Array(tmpD.getTime(), val2);
                n++;
            }
                            
        }
        ini.setDate(ini.getDate() - 15);
        fine.setDate(fine.getDate() - 15);
    }
    Log.alert(30, "InterfaceSimul", "Creati " + n + " sovraccarichi");
    
}  


// cerca nell'array dei sovraccarichi quelli relativi al perdiodo voluto
InterfaceSimul.GetStorico = function (objId, ini, fine, interval)
{
    var today, n, j, diff;
    var ret = '{"retStatus": 0, "retValue":[';
    var rand1, rand2;
    var val1, val2;

    if (InterfaceSimul.storico == null)
        InterfaceSimul.CreateStorico();

// prendo numero elementi diversi a seconda dell'intervallo
// prendo valore in proporzione a seconda se prendo per ora, per giorno o per mese 
// costo -> giorno = mese / 20; ora = mese / 40;
// consumo -> giorno = mese / 30; ora = mese / 60;


    if (interval == 0)
    {
	  i1 = 23; //ieri
	  rand1 = 1/(24*30);
        rand2 = 1/(24*30);
    }
    else
	  if (interval == 1)
	  {		
		i1 = 7; // settimana
		rand1 = 1/30;
            rand2 = 1/30;
	  }
	  else
		if (interval == 2)
		{
			i1 = 31;  // mese
	  		rand1 = 1/30; 
             	rand2 = 1/30;
	      }
		else
			if (interval == 3)
			{
				i1 = 4; // anno
		    		rand1 = 1;
            		rand2 = 1;
	  		}
    i2 = 0;

    Log.alert(80, "InterfaceSimul",  "ini = " + ini.toString() + " fine = " + fine.toString() + " objId = " + objId);
    
    // se homeauto prendo valore totale, altrimenti una percentuale, che metto in un array in modo
    // da usare sempre la stessa per lo stesso dispositivo
    if (objId != "homeauto")
    {   
        // se li voglio fissi creo array statico già inizializzato con i nomi dei dispositivi e i valori voluti
        // e automaticamente uso quelli
        if(InterfaceSimul.storicoPercent[objId] == undefined)
        {
        // se singolo dispositivo prendo una frazione tra 10% e 60%
            r1 = ((Math.random() * 50) + 10) / 100;
            r2 = ((Math.random() * 50) + 10) / 100;
            InterfaceSimul.storicoPercent[objId] = new Array(r1, r2);
        }
        else
        {
            r1 = InterfaceSimul.storicoPercent[objId][0];
            r2 = InterfaceSimul.storicoPercent[objId][1];
        }
    }
    else 
    {    
	  r1 = 1;
        r2 = 1;
    }
    j = 0;
    // mese attuale = 0
    for (i = i2; i <= i1; i++)
    {
        if (j > 0)
            ret += ',';
        else
            j = 1;
        if (i < 0)
            ret += '[0,0]'; // non ho dati
        else
	  {
		
      	if (interval == 0)
    		{
			ret += '[' + (InterfaceSimul.ieri[i][1] * r1) + ',' + (InterfaceSimul.ieri[i][0] * r2) + ']';
		}
		else
		
            //ret += '[' + Math.floor(InterfaceSimul.storico[i][0] * rand1 * r1) + ',' + Math.floor(InterfaceSimul.storico[i][1] * rand2 * r2) + ']';
		// inverto per non riscrivere tutto
			ret += '[' + (InterfaceSimul.storico[i][1] * rand1 * r1) + ',' + (InterfaceSimul.storico[i][0] * rand2 * r2) + ']';
    	}
    }
    ret += ']}';

    return ret;
}

// crea un insieme di dati per lo storico, per gli ultimi 24 mesi
// l'ultimo lo creo in base al numero di giorni 
// min 40 max 300 w, min 10 t1, max 80 t1
InterfaceSimul.CreateStorico = function ()
{
    var tmp, j,ini, fine;
    var ret = '{"retStatus": 0, "retValue":[';
    var rand1, rand2;
    var val1, val2;
//  '{"retStatus": 0, "retValue":[[ 1257030047309, 10],[1257030049309, 10],[1257030050309, 20],[1257030051309, 21],[1257030064309, 27],[1257030047309, 50],[1257030047309, 51],[1257030047309, 54],[1257030047309, 61],[1257030047309, 61],[1257030047309, 61],[1257030047309, 75],[1257030047309, 76],[1257030047309, 80],[1257030047309, 81],[1257030047309, 101],[1257030047309, 110],[1257030047309, 150],[1257030047309, 250],[1257030047309, 256],[1257030047309, 280],[1257030047309, 278],[1257030047309, 250],[1257030047309, 251],[1257030047309, 531],[1257030047309, 501],[1257030047309, 481],[1257030047309, 431],[1257030047309, 441]]}',
    
    InterfaceSimul.storicoPercent = new Array();
    InterfaceSimul.storico = new Array();
    InterfaceSimul.ieri = new Array();
    InterfaceSimul.ieri[0] = new Array(0.040, 0.13);
    InterfaceSimul.ieri[1] = new Array(0.040, 0.13);
    InterfaceSimul.ieri[2] = new Array(0.041, 0.14);
    InterfaceSimul.ieri[3] = new Array(0.040, 0.13);
    InterfaceSimul.ieri[4] = new Array(0.042, 0.15);
    InterfaceSimul.ieri[5] = new Array(0.040, 0.13);
    InterfaceSimul.ieri[6] = new Array(0.050, 0.18);
    InterfaceSimul.ieri[7] = new Array(0.45, 1.23);
    InterfaceSimul.ieri[8] = new Array(0.78, 2.03);
    InterfaceSimul.ieri[9] = new Array(0.44, 1.22);
    InterfaceSimul.ieri[10] = new Array(0.06, 0.18);
    InterfaceSimul.ieri[11] = new Array(0.05, 0.14);
    InterfaceSimul.ieri[12] = new Array(0.82, 2.24);
    InterfaceSimul.ieri[13] = new Array(0.67, 1.83);
    InterfaceSimul.ieri[14] = new Array(0.50, 1.05);
    InterfaceSimul.ieri[15] = new Array(0.05, 0.15);
    InterfaceSimul.ieri[16] = new Array(0.06, 0.18);
    InterfaceSimul.ieri[17] = new Array(0.91, 2.80);
    InterfaceSimul.ieri[18] = new Array(0.74, 2.10);
    InterfaceSimul.ieri[19] = new Array(0.98, 3.40);
    InterfaceSimul.ieri[20] = new Array(0.94, 3.04);
    InterfaceSimul.ieri[21] = new Array(0.49, 2.13);
    InterfaceSimul.ieri[22] = new Array(0.53, 2.43);
    InterfaceSimul.ieri[23] = new Array(0.23, 1.13);



    tmp = GestDate.GetActualDate().getDate();
    InterfaceSimul.storico[0] = new Array(30, 100);
    InterfaceSimul.storico[1] = new Array(34, 110);
    InterfaceSimul.storico[2] = new Array(43, 140);
    InterfaceSimul.storico[3] = new Array(45, 115);
    InterfaceSimul.storico[4] = new Array(31, 98);
    InterfaceSimul.storico[5] = new Array(48, 141);
    InterfaceSimul.storico[6] = new Array(50, 152);
    InterfaceSimul.storico[7] = new Array(51, 153);
    InterfaceSimul.storico[8] = new Array(49, 129);
    InterfaceSimul.storico[9] = new Array(40, 104);
    InterfaceSimul.storico[10] = new Array(42, 123);
    InterfaceSimul.storico[11] = new Array(35, 103);
    InterfaceSimul.storico[12] = new Array(35, 101);    
    InterfaceSimul.storico[13] = new Array(42, 120);    
    InterfaceSimul.storico[14] = new Array(45, 149);    
    InterfaceSimul.storico[15] = new Array(42, 143);    
    InterfaceSimul.storico[16] = new Array(40, 135);    
    InterfaceSimul.storico[17] = new Array(32, 130);    
    InterfaceSimul.storico[18] = new Array(38, 133);   // agosto 
    InterfaceSimul.storico[19] = new Array(34, 122);    
    InterfaceSimul.storico[20] = new Array(43, 152);    
    InterfaceSimul.storico[21] = new Array(37, 127);    
    InterfaceSimul.storico[22] = new Array(39, 127); 
    InterfaceSimul.storico[23] = new Array(48, 159); 
    InterfaceSimul.storico[24] = new Array(38, 120); 
    InterfaceSimul.storico[25] = new Array(32, 96); 
    InterfaceSimul.storico[26] = new Array(30, 100); 
    InterfaceSimul.storico[27] = new Array(38, 130); 
    InterfaceSimul.storico[28] = new Array(31, 110); 
    InterfaceSimul.storico[29] = new Array(30, 100); 
    InterfaceSimul.storico[30] = new Array(40, 118); 
    InterfaceSimul.storico[31] = new Array(42, 120); 

    InterfaceSimul.storico[32] = new Array(120 * tmp / 30, 160 * tmp / 30); 
    Log.alert(90, "InterfaceSimul", "CreateStorico fine");            
}  



// crea dati odierni in modo statico per tutto il giorno
// metto un dato ogni 5 minuti, che indica la media nell'intervallo di tempo (il primo è all'ora 0)
// creo i timestamp e aggiungo il valore preso sa un array statico predefinito
InterfaceSimul.CreateOdierno = function ()
{
    var tmpI, tmpF, i;
    var tsI, tsF;
    
    tmpI = GestDate.GetActualDate();
    tmpI.setHours(0);
    tmpI.setMinutes(0);
    tmpI.setSeconds(0);
    tmpI.setMilliseconds(0);
    tmpF = GestDate.GetActualDate();
    tmpF.setHours(23);
    tmpF.setMinutes(59);
    tmpF.setSeconds(59);
    tmpF.setMilliseconds(999);
    tsF = tmpF.getTime();
    tsI = tmpI.getTime();
    InterfaceSimul.odierno = new Array();
    i = 0;
    do
    {
        InterfaceSimul.odierno[i] = new Array(tsI, InterfaceSimul.odiernoVal[i]);
        //Log.alert(30, "InterfaceSimul", "val = " + InterfaceSimul.odiernoVal[i]);
        tmpI.setMinutes(tmpI.getMinutes() + InterfaceSimul.consumoInterval);
        tsI = tmpI.getTime();
        i++;
    }
    while(tsI < tsF);
    Log.alert(60, "InterfaceSimul", "num elem odierno = " + InterfaceSimul.odierno.length + " len val = " + InterfaceSimul.odiernoVal.length);
}

InterfaceSimul.SaveConsumoAttuale = function (res)
{
    var tmpT, tmpV;
    
    if (InterfaceSimul.consumoArray == null)
        InterfaceSimul.consumoArray = new Array();
    
    len = InterfaceSimul.consumoArray.length;
    tmpT = GestDate.GetActualDate().getTime();
    tmpV = res.retValue.value;
    Log.alert(80, "InterfaceSimul", "SaveConsumoAttuale = " + tmpV);
    InterfaceSimul.consumoArray[len] = new Array(tmpT, tmpV);
}

InterfaceSimul.GetEnergyAll = function ()
{
    var ret = '{"retStatus": 0, "retValue":[';
    var tmp;
    
    // prendo percentuale in base alla data nel mese (considero mese di 30 giorni)
    tmp = GestDate.GetActualDate();
    perc = (tmp.getDate() + 1) / 30;
    
    for (i = 0; i < InterfaceSimul.EnergyAll.length; i=i+3)
    {
        if (i > 0)
            ret += ",";
        ret +=  '"' + InterfaceSimul.EnergyAll[i] + '",' + Math.round(InterfaceSimul.EnergyAll[i+1] * perc) +
                    ',' + Math.round(InterfaceSimul.EnergyAll[i+2] * perc)
    }
    ret += ']}';
    Log.alert(80, "InterfaceSimul", "perc = " + perc + " GetEnergyAll = " + ret);
    return ret;
}