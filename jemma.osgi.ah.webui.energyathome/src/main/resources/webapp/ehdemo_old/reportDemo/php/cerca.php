<html>
<head>
<title>MioBlog</title>
</head>
<body>
<h1>Motore di ricerca</h1>
<?php
// inclusione del file di classe
include "funzioni_mysql.php";
// istanza della classe
$data = new MysqlClass();
// chiamata alla funzione di connessione
$data->connetti();
?>
<form action="<?php echo $_SERVER['PHP_SELF']; ?>" method="POST">
<input type="text" name="key" value="" />
<input type="submit" value="cerca" class="submit" />
</form>
<?php
if(isset($_POST['key'])&&($_POST['key']!="")&&(preg_match("/^[a-z0-9]+$/i", $_POST['key'])))
 {
$key = $_POST['key'];

$sql_cerca = $data->query("SELECT * FROM post WHERE (titolo_post LIKE '%" . $key . "%') OR (testo_post LIKE '%" . $key . "%') ORDER BY id_post");
$trovati = mysql_num_rows($sql_cerca);
if($trovati > 0)
{
 echo "<p>Trovate $trovati voci per il termine <b>".stripslashes($key)."</b></p>\n";
 while($cerca_obj = $data->estrai($sql_cerca))
  {
    $id_post = $cerca_obj->id_post;
    $titolo_post = stripslashes($cerca_obj->titolo_post);
	$testo_post = stripslashes($cerca_obj->testo_post);
    $autore_post = stripslashes($cerca_obj->autore_post);
    $data_post = $cerca_obj->data_post;
       
    // visualizzazione dei dati
    echo "<h2>".$titolo_post."</h2>\n";
    echo  "Autore <b>". $autore_post . "</b>\n";
	echo  "<br />\n";
    echo  "Pubblicato il <b>" . $data->format_data($data_post) . "</b>\n";
	echo  "<br />\n";
	// link al testo completo del post
    $leggi_tutto = "<br /><a href=\"post.php?id_post=$id_post\">Articolo completo</a>\n";
	// anteprima del testo
	echo "<p>".$data->preview($testo_post, 50, $leggi_tutto)."</p>\n";
  }
  }else{
  // notifica in caso di mancanza di risultati
  echo "Al momento non sono stati pubblicati post che contengano questo termine.";
 }
}
// disconnessione
$data->disconnetti();
?>
</body>
</html>