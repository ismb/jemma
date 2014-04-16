<?php
// inizializzazione della sessione
session_start();
// controllo sul valore di sessione
if (!isset($_SESSION['login']))
{
 // reindirizzamento alla home page in caso di login mancato
 header("Location: index.php");
}
// inclusione del file di classe
include "funzioni_mysql.php";
// istanza della classe
$data = new MysqlClass();
// chiamata alla funzione di connessione
$data->connetti();
// query per l'estrazione dei record
$commento_sql = $data->query("SELECT id_commento,testo_commento,autore_commento,data_commento FROM commenti WHERE approvato='0' ORDER BY data_commento DESC");

echo "<h1>Elenco dei commenti da approvare</h1>\n"; 
// controllo sul numero di records presenti in tabella
if(mysql_num_rows($commento_sql) > 0){
  echo "<ul>\n";
  // estrazione dei record tramite ciclo
  while($commento_obj = $data->estrai($commento_sql)){
    $id_commento = $commento_obj->id_commento;
	$testo_commento = stripslashes($commento_obj->testo_commento);
    $autore_commento = stripslashes($commento_obj->autore_commento);
    $data_commento = $commento_obj->data_commento;
       
    // visualizzazione dei dati
	 echo "<li>\n";
	 echo "Autore: " . $autore_commento . " Scritto il ". $data->format_data($data_commento) . "\n";
     echo  "<br />\n";
	 echo "Commento: " . $testo_commento;
     echo  "<br />\n";
	 echo " :: <a href=\"moderazione.php?id_commento=$id_commento\">approva</a>\n";
 	 echo "</li>\n";
  } 
  echo "</ul>\n";
}else{
  // notifica in assenza di record che soddisfino le caratteristiche richieste
  echo "Per il momento non sono disponibili commenti da approvare.";
}
// chiusura della connessione a MySQL
$data->disconnetti();
?>