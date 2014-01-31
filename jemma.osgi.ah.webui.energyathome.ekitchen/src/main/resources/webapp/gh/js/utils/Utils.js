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
};

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
};
