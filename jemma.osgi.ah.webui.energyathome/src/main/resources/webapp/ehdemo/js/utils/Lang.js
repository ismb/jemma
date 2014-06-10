var Lang = {
    MODULE : "ConvertDate",
    convTable : null
}

Lang.SetTable = function(table)
{
	Lang.setTable(table);
}

Lang.Convert = function(nome)
{
	Lang.Convert(nome, Lang.convTable);
}

// converte il nome passato come parametro secondo la tabella passata come parametro
// se non viene trovata nessuna corrispondenza viene ritornato il valore passato
Lang.Convert = function(nome, table){
	if ((table != undefined) && (table != null)){
		// nella tabella metto tutto lower case come indice
		var tmp = table[nome.toLowerCase()];
		if (tmp == undefined){
			// per il caso in cui non ho lower case come indice
			tmp = table[nome];
			if (tmp == undefined){
				return nome;
			} else {
				return tmp;
			}
		} else {
			return tmp;
		}
	}
	return nome;
}