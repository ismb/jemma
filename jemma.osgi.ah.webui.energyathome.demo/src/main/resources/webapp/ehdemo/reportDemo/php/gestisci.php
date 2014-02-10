<?php
// inizializzazione della sessione
session_start();
// controllo sul valore di sessione
if (!isset($_SESSION['login']))
{
 // reindirizzamento alla home page in caso di login mancato
 header("Location: index.php");
}

// valorizzazione delle variabili con i parametri dal form
if(isset($_POST['submit'])&&($_POST['submit']=="Scrivi")){

  if(isset($_POST['autore'])){
    $autore = addslashes(filter_var($_POST['autore'], FILTER_SANITIZE_STRING));
  }
  if(isset($_POST['titolo'])){
    $titolo = addslashes(filter_var($_POST['titolo'], FILTER_SANITIZE_STRING));
  }
  if(isset($_POST['testo'])){
    $testo = addslashes(filter_var($_POST['testo'], FILTER_SANITIZE_STRING));
  }

  	// inclusione del file della classe
	include "funzioni_mysql.php";
	// istanza della classe
	$data = new MysqlClass();
	// chiamata alla funzione di connessione
	$data->connetti();
    $t = "post"; # nome della tabella
    $v = array ($titolo,$testo,$autore,date("Y-m-d")); # valori da inserire
    $r =  "titolo_post,testo_post,autore_post,data_post"; # campi da popolare

   // chiamata alla funzione per l’inserimento dei dati
    $data->inserisci($t,$v,$r);
    echo "Articolo inserito con successo.";
	// disconnessione
    $data->disconnetti();
   }else{
  // form per l'inserimento
  ?>
<h1>Inserimento post:</h1>
<form action="<?php echo $_SERVER['PHP_SELF']; ?>" method="post">
Titolo:<br>
<input name="titolo" type="text"><br />
Testo:<br>
<textarea name="testo" cols="30" rows="10"></textarea><br />
Autore:<br>
<input name="autore" type="text"><br />
<input name="submit" type="submit" value="Scrivi">
</form>
  <?
}
?>