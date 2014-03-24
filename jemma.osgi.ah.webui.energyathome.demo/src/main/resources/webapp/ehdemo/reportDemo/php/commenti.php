<?php
// inclusione del file di classe
include "funzioni_mysql.php";
// istanza della classe
$data = new MysqlClass();
// chiamata alla funzione di connessione
$data->connetti();

// valorizzazione delle variabili con i parametri dal form
if(isset($_POST['submit'])){
  if(!isset($_POST['autore']) || !isset($_POST['commento']) || !isset($_POST['post_id']) || !is_numeric($_POST['post_id']))
  {
   echo "Tutti i campi sono obbligatori";
  }else{   
    $autore = htmlentities(addslashes($_POST['autore']));
    $post_id = $_POST['post_id'];
    $commento = htmlentities(addslashes($_POST['commento']));

    $t = "commenti"; # nome della tabella
    $v = array ($post_id,$autore,$commento,date("Y-m-d")); # valori da inserire
    $r =  "id_post,autore_commento,testo_commento,data_commento"; # campi da popolare

   // chiamata alla funzione per l’inserimento dei dati
    $data->inserisci($t,$v,$r);
    header("Location: post.php?id_post=$post_id");
 }
}else{
  // controllo sull'id del post inviato per querystring
  if( isset($_GET['id_post']) && is_numeric($_GET['id_post']) ){
    $id_post = $_GET['id_post'];
    $sql_commenti = $data->query("SELECT id_post FROM post WHERE id_post='$id_post'");
    if(mysql_num_rows($sql_commenti) > 0){
      // viene visualizzato il form solo nel caso in cui l'unico dato inviato sia l'id del post
      ?>
<form action="<?php echo $_SERVER['PHP_SELF']; ?>" method="post">
Autore:<br />
<input name="autore" type="text"><br />
Commento:<br />
<textarea name="commento" cols="30" rows="10"></textarea><br />
<input name="post_id" type="hidden" value="<?php echo $id_post; ?>">
<input name="submit" type="submit" value="Invia">
</form>
      <?php
      // notifiche in caso di querystring vuota o non valida
    }else{
      echo "Non è possibile accedere alla pagina da questo percorso.";
    }
  }else{
    echo "Commenti non consentiti, articolo inesistente.";
  }
}
// disconnessione
$data->disconnetti();
?>
</body>
</html>