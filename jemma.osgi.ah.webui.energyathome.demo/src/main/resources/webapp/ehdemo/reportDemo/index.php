<!DOCTYPE html>
<!-- paulirish.com/2008/conditional-stylesheets-vs-css-hacks-answer-neither/ -->
<!--[if lt IE 7]> <html class="no-js ie6 oldie" lang="en"> <![endif]-->
<!--[if IE 7]>    <html class="no-js ie7 oldie" lang="en"> <![endif]-->
<!--[if IE 8]>    <html class="no-js ie8 oldie" lang="en"> <![endif]-->
<!-- Consider adding an manifest.appcache: h5bp.com/d/Offline -->
<!--[if gt IE 8]><!--> <html class="no-js" lang="en"> <!--<![endif]-->
<head>
  	<meta charset="utf-8">

  	<!-- Use the .htaccess and remove these lines to avoid edge case issues. More info: h5bp.com/b/378 -->
  	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

  	<title>Report Page</title>
  	<meta name="description" content="">
  	<meta name="author" content="">

  	<!-- Mobile viewport optimized: j.mp/bplateviewport -->
  	<meta name="viewport" content="width=device-width,initial-scale=1">

  	<!-- Place favicon.ico and apple-touch-icon.png in the root directory: mathiasbynens.be/notes/touch-icons -->

  	<!-- CSS: implied media=all -->
  	<!-- CSS concatenated and minified via ant build script-->
  	<link rel="stylesheet" href="css/style.css">
  	<!-- end CSS-->

  	<!-- More ideas for your <head> here: h5bp.com/d/head-Tips -->

  	<!-- All JavaScript at the bottom, except for Modernizr / Respond.
       	 Modernizr enables HTML5 elements & feature detects; Respond is a polyfill for min/max-width CSS3 Media Queries
         For optimal performance, use a custom Modernizr build: www.modernizr.com/download/ -->
  	<script src="js/libs/modernizr-2.0.6.min.js"></script>
</head>

<body>
		<h1>Report page: realizzato in PHP e MySQL</h1>
		<?php
			try{
				// inclusione del file di classe
				include "php/funzioni_mysql.php";
				// istanza della classe
				$data = new MysqlClass();
				// chiamata alla funzione di connessione
				$data->connetti();
				// query per l'estrazione dei record
				$post_sql = $data->query('SELECT * FROM Forced_Day_Tarif');
				// controllo sul numero di record presenti in tabella
				if(mysql_num_rows($post_sql) > 0){
					// estrazione dei record tramite ciclo
				  	while($post_obj = $data->estrai($post_sql)){
					    $date = $post_obj->date;
					    $tarif_code = stripslashes($post_obj->tarif_code);
					    //$data_post = $post_obj->data_post;
					       
					    // visualizzazione dei dati
					    echo '<h2>Data: ' . $date.'</h2> - ';
					    echo 'Codice tariffa: <b>' . $tarif_code . '</b>';
						//echo '<br />\n';
					    //echo 'Pubblicato il <b>' . $data->format_data($data_post) . '</b>\n';
						//echo '<br />\n';
						// link al testo completo del post
					    //$leggi_tutto = '<br /><a href=\"php/post.php?id_post=$id_post\">Articolo completo</a>\n';
						// anteprima del testo
						//echo '<p>' . $data->preview($testo_post, 50, $leggi_tutto) . '</p>\n';
						// parte relativa al conteggio dei commenti
						//echo 'Commenti: ' . $data->conta_commenti('id_commento', 'commenti', 'id_post', $id_post, 'approvato','1');
						//echo ' :: <a href="php/commenti.php?id_post=$id_post">Inserisci un commento</a>\n';
					    echo '<hr>';
				  	} 
				}else{
				  	// notifica in assenza di record
				  	echo 'Per il momento non sono disponibili post.';
				}
				// chiusura della connessione a MySQL
				$data->disconnetti();
			} catch (Exception $e) {
		        echo ('An error has occured ('.$e.'): '.mysql_errno());        
		    }
		?>

	  	<!-- JavaScript at the bottom for fast page loading -->
	
	  	<!-- Grab Google CDN's jQuery, with a protocol relative URL; fall back to local if offline -->
	  	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js"></script>
	  	<script>window.jQuery || document.write('<script src="js/libs/jquery-1.6.2.min.js"><\/script>')</script>
	
	  	<!-- scripts concatenated and minified via ant build script-->
	  	<script defer src="js/plugins.js"></script>
	  	<script defer src="js/script.js"></script>
	  	<!-- end scripts-->
	
	  	<!-- Prompt IE 6 users to install Chrome Frame. Remove this if you want to support IE 6.
	         chromium.org/developers/how-tos/chrome-frame-getting-started -->
	  	<!--[if lt IE 7 ]>
	    <script src="//ajax.googleapis.com/ajax/libs/chrome-frame/1.0.3/CFInstall.min.js"></script>
	    <script>window.attachEvent('onload',function(){CFInstall.check({mode:'overlay'})})</script>
	  	<![endif]-->
  
	</body>
</html>
