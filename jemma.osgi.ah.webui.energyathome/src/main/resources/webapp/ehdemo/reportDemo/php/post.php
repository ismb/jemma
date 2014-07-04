<?php
// controllo sulla variabile inviata per querystring
if((!isset($_GET['id_post'])) || (!is_numeric($_GET['id_post'])))
{
// reindirizzamento del browser nel caso in cui la variabile non venga validata
header("Location: index.php");
}else{
$id_post = $_GET['id_post'];
}
?>
<html>
<head>
<title>MioBlog</title>
</head>
<body>
<?php
// inclusione del file di classe
include "funzioni_mysql.php";
// istanza della classe
$data = new MysqlClass();
// chiamata alla funzione di connessione
$data->connetti();
// query per l'estrazione dei record
$post_sql = $data->query("SELECT * FROM post WHERE id_post = $id_post");
// controllo sulla presenza in tabella del record corrispondente dell'id richiesto
if(mysql_num_rows($post_sql) > 0){
  // estrazione dei record
    $post_obj = $data->estrai($post_sql);
    $id_post = $post_obj->id_post;
    $titolo_post = stripslashes($post_obj->titolo_post);
	$testo_post = stripslashes($post_obj->testo_post);
    $autore_post = stripslashes($post_obj->autore_post);
    $data_post = $post_obj->data_post;
       
    // visualizzazione dei dati
    echo "<h1>".$titolo_post."</h1>\n";
    echo  "Autore <b>". $autore_post . "</b>\n";
	echo  "<br />\n";
    echo  "Pubblicato il <b>" . $data->format_data($data_post) . "</b>\n";
	echo  "<br />\n";
	echo "<p>".$testo_post."</p>\n"; 
	echo " :: <a href=\"commenti.php?id_post=$id_post\">Inserisci un commento</a>\n";
	echo  "<br />\n";
	// estrazione dei commenti
	$post_commenti = $data->query("SELECT autore_commento,testo_commento,data_commento FROM commenti WHERE id_post = $id_post AND approvato='1' ORDER BY data_commento DESC");
    if(mysql_num_rows($post_commenti) > 0){
	 echo "<ul>\n";
     while($commenti_obj = $data->estrai($post_commenti))
	 {
	 $autore_commento = stripslashes($commenti_obj->autore_commento);
	 $testo_commento = stripslashes($commenti_obj->testo_commento);
	 $data_commento = stripslashes($commenti_obj->data_commento);
	 echo "<li>\n";
	 echo "Autore: " . $autore_commento . " Scritto il ". $data->format_data($data_commento) . "\n";
     echo  "<br />\n";
	 echo "Commento: " . $testo_commento;
 	 echo "</li>\n";
	 }
	 echo "</ul>\n";
	}else{
	echo "Nessun commento per questo post";
	}
}else{
  // notifica in assenza di record
  echo "Non esiste alcun post per questo id.";
}
// chiusura della connessione a MySQL
$data->disconnetti();
?>
</body>
</html>