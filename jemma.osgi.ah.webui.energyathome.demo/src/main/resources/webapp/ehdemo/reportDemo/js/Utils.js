var Utils = {
		
}
/**
 * Gestisce il resize di un'imamgine in modo che sia nel contenitore senza cambiare le proporzioni
 */
Utils.ResizeImg = function(imgTag, image, set, modo, align)
{
	tag = "#" + imgTag;
	img = new Image();
	if (image != null)
		img.src = image;
	else
		img.src = $(tag).attr("src");
	imgH = img.height;
	imgW = img.width;
	rapp = imgW / imgH;
	contH = $(tag).height();
	contW = $(tag).width();
	
	if (modo == 1)
	{
		// uso la massima altezza possibile
		h = contH;
		w = h * rapp;
		if (w > contW)
		{
			// se troppo largo diminuisco immagine mantenendo le proporzioni
			w = contW;
			h = w / rapp;
		}
	}
	else
	{
		// uso la massima larghezza possibile
		w = contW;
		h = w / rapp;
		if (h > contH)
		{
			// se troppo alto diminuisco immagine mantenendo le proporzioni
			h = contH;
			w = h * rapp;
		}
	}
	
	// calcolo offset dall'angolo in alto a sinistra in modo da allineare l'immagine 
	// nel contenitore in base alle richieste 
	// 1=TopLeft, 2=TopRight, 3=BottomLeft, 4=BottomRight, 5=center height, left, 6=center height, right 
	// 7=center width, top, 8=center width, bottom, 9=center all
	// bottom e right non sempre funzionano, fatto tutto rispetto a top e left
	switch (align)
	{
		case 1: // alto a sinistra
			t = 0;
			l = 0;
			break;
		case 2: // alto a destra
			t = 0;
			l = Math.round(contW-w);
			break;
		case 3: // basso a sinistra
			t = Math.round(contH-h);
			l = 0;
			break;
		case 4: // basso a destra
			t = Math.round(contH-h);
			l = Math.round(contW-w);
			break;
		case 5: // centrato rispetto altezza, a sinistra
			t = Math.round((contH-h)/2);
			l = 0;
			break;
		case 6: // centrato rispetto altezza, a destra
			t = Math.round((contH-h)/2);
			l = Math.round(contW-w);
			break;
		case 7: // centrato rispetto larghezza, in alto
			t = 0;
			l = Math.round((contW-w)/2);
			break;
		case 8: // centrato rispetto larghezza, in basso
			t = Math.round(contH-h);
			l = Math.round((contW-w)/2);
			break;
		case 9: // centrato rispetto larghezza e altezza
			t = Math.round((contH-h)/2);
			l = Math.round((contW-w)/2);
			break;
	}
	if (set)
	{
		// se richiesto imposta i valori per il tag
		$(tag).width(Math.round(w)+"px");
		$(tag).height(Math.round(h)+"px");
		$(tag).css("top", t + "px");
		$(tag).css("left", l + "px");
	}
	// ritorna i valori di width, height, top e left calcolati
	return new Array(Math.round(w), Math.round(h), t, l);
}

//formatta la data in base al parametro tipo
//1 = data + ora
//2 = solo data
//3 = solo ora
Utils.FormatDate = function(dataIn, tipo)
{
	var d, M, y, h, m;
	var tmp;
	
	d = dataIn.getDate();
	M = dataIn.getMonth();
	y = dataIn.getFullYear();
	h = dataIn.getHours();
	m = dataIn.getMinutes();
	if (m < 10)
		m = "0" + m;
	if (tipo == 1)
		tmp = d + " " + Msg.mesiCompleto[M] + " " + y + " " + h + ":" + m;
	else
		if (tipo == 2)
			tmp = d + " " + Msg.mesiCompleto[M] + " " + y;
		else
			if (tipo == 3)
				tmp = h + ":" + m;
	return tmp;
}

//funzione che esegue l'arrotondamento
Utils.RoundTo = function(value, decimalpositions){
    var i = value * Math.pow(10,decimalpositions);
    i = Math.round(i);
    return i / Math.pow(10,decimalpositions);
}

Utils.scriviCookie = function(nomeCookie,valoreCookie,durataCookie){
  var scadenza = new Date();
  var adesso = new Date();
  scadenza.setTime(adesso.getTime() + (parseInt(durataCookie) * 60000));
  document.cookie = nomeCookie + '=' + escape(valoreCookie) + '; expires=' + scadenza.toGMTString() + '; path = /';
}

Utils.leggiCookie = function(nomeCookie){
  if (document.cookie.length > 0){
    var inizio = document.cookie.indexOf(nomeCookie + "=");
    if (inizio != -1){
      inizio = inizio + nomeCookie.length + 1;
      var fine = document.cookie.indexOf(";",inizio);
      if (fine == -1) fine = document.cookie.length;
      return unescape(document.cookie.substring(inizio,fine));
    } else {
       return "";
    }
  }
  return "";
}

Utils.cencellaCookie = function(nomeCookie){
  scriviCookie(nomeCookie,'',-1);
}

Utils.verificaCookie = function(){
  document.cookie = 'verifica_cookie';
  var testcookie = (document.cookie.indexOf('verifica_cookie') != -1) ? true : false;
  return testcookie;
}

var LazyScript = {
	foglio : []
};

LazyScript.load = function(urlscr, callback) {
	try {
		if ($.inArray(urlscr, LazyScript.foglio) == -1) {
			LazyScript.foglio.push(urlscr);
			var script = document.createElement("script");
			script.src = urlscr;
			script.type = "text/javascript";
			$("head")[0].appendChild(script);

			if (callback) {
				script.onreadystatechange = function() {
					if (script.readyState == 'loaded' || script.readyState == 'complete') {
						callback();
					}
				}
				script.onload = function() {
					callback();
					return;
				}
			}
		} else {
			if (callback) {
				callback();
			}
		}
	} catch (e) {
		alert(e);
	}
}

function Querystring(qs) {
	this.params = {};

	if (qs == null)
		qs = location.search.substring(1, location.search.length);
	if (qs.length == 0)
		return;

	// Turn <plus> back to <space>
	// See: http://www.w3.org/TR/REC-html40/interact/forms.html#h-17.13.4.1

	qs = qs.replace(/\+/g, ' ');
	var args = qs.split('&'); // parse out name/value pairs separated via &
	// split out each name=value pair
	for ( var i = 0; i < args.length; i++) {
		var pair = args[i].split('=');
		var name = decodeURIComponent(pair[0]);

		var value = (pair.length == 2) ? decodeURIComponent(pair[1]) : name;

		this.params[name] = value;
	}
}

Querystring.prototype.get = function(key, default_) {
	var value = this.params[key];
	return (value != null) ? value : default_;
}

Querystring.prototype.contains = function(key) {
	var value = this.params[key];
	return (value != null);
}

function addCSSinDocument(url){
	var headID = document.getElementsByTagName("head")[0];         
	var cssNode = document.createElement('link');
	cssNode.type = 'text/css';
	cssNode.rel = 'stylesheet';
	cssNode.href = url;
	cssNode.media = 'screen';
	headID.appendChild(cssNode);
}

function addJavaScriptinDocument(url){
	var headID = document.getElementsByTagName("head")[0];         
	var newScript = document.createElement('script');
	newScript.type = 'text/javascript';
	newScript.src = url;
	headID.appendChild(newScript);
}

function roundTo(value, decimalpositions){
	
	var i = value * Math.pow(10, decimalpositions);
	i = Math.round(i);
	return i / Math.pow(10, decimalpositions);
}
