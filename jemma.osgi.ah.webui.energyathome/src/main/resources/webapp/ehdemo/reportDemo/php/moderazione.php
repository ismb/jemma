<?php
// inizializzazione della sessione
session_start();
// controllo sul valore di sessione
if (!isset($_SESSION['login']))
{
 // reindirizzamento alla home page in caso di login mancato
 header("Location: index.php");
}

// controllo sull'id del commento inviato per querystring
if (isset($_GET['id_commento']) && is_numeric($_GET['id_commento']))
{
$id_commento = $_GET['id_commento'];
?>
<form action="<?php echo $_SERVER['PHP_SELF']; ?>" method="POST">
  <h1>Attenzione!</h1>
  Si sta per approvare il commento selezionato.<br />
  Premere il pulsante per eseguire l'operazione richiesta.<br />
  <br>
  <input name="commento_id" type="hidden" value="<?php echo $id_commento; ?>">
  <input name="submit" type="submit" value="Modera">
</form>
<?php
}
// controllo sull'id del commento inviato per form
elseif(isset($_POST['commento_id']) && is_numeric($_POST['commento_id']))
{
 $commento_id = $_POST['commento_id'];
 // inclusione del file di classe
 include "funzioni_mysql.php";
 // istanza della classe
 $data = new MysqlClass();
 // chiamata alla funzione di connessione
 $data->connetti();
 $data->query("UPDATE commenti SET approvato='1' WHERE id_commento = $commento_id");
 //redirezione alla pagina di gestione dei commenti
 header("Location: lista_commenti.php");
 // chiusura della connessione a MySQL
 $data->disconnetti();
}
?>