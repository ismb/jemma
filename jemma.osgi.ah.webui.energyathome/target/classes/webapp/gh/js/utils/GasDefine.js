/*
 * Logical names for all the images 
 */

var GasDefine = {
	lang: "it",
	// images path
	EHLogo: "E@HLogo.png",
//	partner: "sponsors.png",
	clock: "orologio.png",
	headerSep: "header_sep.png",
	
	// path images for menu
	menu: {	"sfondoButton": GasDefinePath.imgMenuPath + "sfondo_main_el.png",
	       	"home": GasDefinePath.imgMenuPath + "home_bianco.png",
	       	"homeSel": GasDefinePath.imgMenuPath + "home_verde.png",
	       	"infogas": GasDefinePath.imgMenuPath + "info_bianco.png",
	       	"infogasSel": GasDefinePath.imgMenuPath + "info_verde.png",
	       	"costi": GasDefinePath.imgMenuPath + "costi_bianco.png",
	       	"costiSel": GasDefinePath.imgMenuPath + "costi_verde.png",
	       	"consumi": GasDefinePath.imgMenuPath + "consumi_bianco.png",
	       	"consumiSel": GasDefinePath.imgMenuPath + "consumi_verde.png",
	       	"dispositivi": GasDefinePath.imgMenuPath + "dispositivi_bianco.png",
	       	"dispositiviSel": GasDefinePath.imgMenuPath + "dispositivi_verde.png",
	       	"storico": GasDefinePath.imgMenuPath + "storico_bianco.png",
	       	"storicoSel": GasDefinePath.imgMenuPath + "storico_verde.png",
	       	"tariffa": GasDefinePath.imgMenuPath + "tariffa_bianco.png",
	       	"tariffaSel": GasDefinePath.imgMenuPath + "tariffa_verde.png",	       
	       	"contatti": GasDefinePath.imgMenuPath + "contatti_bianco.png",
	       	"contattiSel": GasDefinePath.imgMenuPath + "contatti_verde.png",
	       	"informazioni": GasDefinePath.imgMenuPath + "informazioni_bianco.png",
	       	"informazioniSel": GasDefinePath.imgMenuPath + "informazioni_verde.png"
	},	       
	home: {	"termSfondo": GasDefinePath.imgPath + "termometro_base.png",
			"termSopra": GasDefinePath.imgPath + "termometro_sopra.png",
			"iconaSugg": GasDefinePath.imgPath + "icona_chat.png",
			"sfondo_sx": GasDefinePath.imgPath + "sfondo_sinistra.png",
			"sfondo_dx": GasDefinePath.imgPath + "sfondo_destra.png",
			"sfondoSugg":  GasDefinePath.imgPath + "sfondogrigio.png",
			"costoVerde": GasDefinePath.imgPath + "costo_verde.png",
			"costoGiallo": GasDefinePath.imgPath + "costo_giallo.png",
			"costoRosso": GasDefinePath.imgPath + "costo_rosso.png",
			"costoGrigio": GasDefinePath.imgPath + "costo_grigio.png",
			"immagineVuota": GasDefinePath.imgPath + "imgEmpty.png",
			"tariffaFestiva": GasDefinePath.imgPath + "tariffa_festiva.png",
			"tariffaFeriale": GasDefinePath.imgPath + "tariffa_feriale.png",
			"contatore": GasDefinePath.imgPath + "contatore.png",
			"caldaia": GasDefinePath.imgPath + "caldaia.png",
			"sensoreGen": GasDefinePath.imgPath + "sensore_gen.png",
			"termostato": GasDefinePath.imgPath + "termostato.png",
			"defaultDispImg": GasDefinePath.imgDispPath + "default_acceso.png",
			"buttonAggiorna": GasDefinePath.imgPath + "btn_aggiorna.png",
			"frecciaPrec": GasDefinePath.imgPath + "frecciasx.png",
			"frecciaSucc": GasDefinePath.imgPath + "frecciadx.png",
			"contatoreDefault" : "G4",
			"tipoContatore" : ["G4", "G6", "G10"],
			"limContatore" : [4.0, 6.0, 8.0],
			"contatoreOk" : [3.0, 4.5, 6.0],	// valori normali
			"contatoreWarn" : [4.0, 6.0, 8.0],	// sovraccarico
			"limCostoOra": [0.3, 0.5, 0.7], 
			"limCostoGiorno": [5.0, 7.5, 10.0],
			"limCostoMese": [70.0, 110.0, 150.0],
			"limConsumoOra": [1.0, 1.5, 2.0],
			"limConsumoGiorno": [20.0, 18.0, 25.0],
			"limConsumoMese": [300.0, 550.0, 700.00]
	},
	trial : {
		"sfondoTrial": GasDefinePath.imgPath + "sfondo_sinistra.png",
		"tariffaFeriale": GasDefinePath.imgPath + "tariffa_feriale.png",
		"tariffaFestiva": GasDefinePath.imgPath + "tariffa_festiva.png",
		"tariffaSabato": GasDefinePath.imgPath + "tariffa_sabato.png",
		"tariffaQVerde": GasDefinePath.imgPath + "qverde.png",
		"tariffaQGiallo": GasDefinePath.imgPath + "qgiallo.png",
		"tariffaQRosso": GasDefinePath.imgPath + "qrosso.png"
	},
	// giorni festivi (giorno,mese) con mese che parte da 0
	// si tralascia pasquetta che e' variabile
	festivi : [[1,0], [6,0], [25,3], [1,4], [2,5], [15,7], [1,10], [8,11], [25,11], [26,11]]
	


};
