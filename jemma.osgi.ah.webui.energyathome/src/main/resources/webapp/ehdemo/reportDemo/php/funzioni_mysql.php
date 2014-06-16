<?php
class MysqlClass{
    // variabili per la connessione al database
	private $nomehost = "163.162.42.28";     
    private $nomeuser = "energy_home";          
    private $password = "energy_home";
    private $nomedb   = "Energy_At_Home";


    // controllo sulle connessioni attive
	private $attiva = false;

    // funzione per la connessione a MySQL
    public function connetti(){
        if(!$this->attiva){
        	if($connessione = mysql_connect($this->nomehost,$this->nomeuser,$this->password) or die (mysql_error())){
		 		$selezione = mysql_select_db($this->nomedb,$connessione) or die (mysql_error());
		 	}
		 	
		 	//try { 
			//	$conn = mysql_connect($this->nomehost,$this->nomeuser,$this->password) or throw new Exception('Error Connecting'); 
			//	mysql_select_db($this->nomedb, $conn) or throw new Expection(mysql_error()); 
			//}  catch( Exception $e ) { 
			//	echo 'MySQL Error!'; 
			//	die ($e);
			//}
        } else {
         	return true;
        }
    }
	
	//funzione per l'esecuzione delle query 
	public function query($sql){
		if(isset($this->attiva)){
			$sql = mysql_query($sql) or die (mysql_error());
			return $sql; 
	  	} else {
	  		return false; 
	  	}
	}
	 
	//funzione per l'inserimento dei dati in tabella
	public function inserisci($t,$v,$r = null){
		if(isset($this->attiva)){
			$istruzione = 'INSERT INTO '.$t;
	        if($r != null){
	        	$istruzione .= ' ('.$r.')';
	        }
	
	        for($i = 0; $i < count($v); $i++){
	            if(is_string($v[$i]))
	            	$v[$i] = '"'.$v[$i].'"';
	        }
	        $v = implode(',',$v);
	        $istruzione .= ' VALUES ('.$v.')';
	
	        $query = mysql_query($istruzione) or die (mysql_error());
	
	    } else {
			return false;
	    }
	}
	//funzione per l'estrazione dei record 
	public function estrai($risultato){
	  	if(isset($this->attiva)){
	  		$r = mysql_fetch_object($risultato);
	  		return $r;
	  	} else {
	  		return false; 
		}
	}
	 
	// funzione per la formattazione della data
	public function format_data($d){
	  	$vet = explode("-", $d); 
	  	$df = $vet[2]."-".$vet[1]."-".$vet[0]; 
	  	return $df; 
	}
	 
	// funzione per l'anteprima degli articoli
	public function preview($post, $offset, $collegamento) {
	 	return (count($anteprima = explode(" ", $post)) > $offset) ? implode(" ", array_slice($anteprima, 0, $offset)) . $collegamento : $post;
	}
	
	// funzione per il conteggio dei commenti
	public function conta_commenti($id_c, $tbl, $campo, $id_post,$enum, $valore_enum){
		if(isset($this->attiva)){
	  		$query_n_com = mysql_query("SELECT COUNT($id_c) AS n_commenti from $tbl WHERE $campo = $id_post AND $enum = '$valore_enum'") or die (mysql_error());
	  		$obj_n_com = mysql_fetch_object($query_n_com) or die (mysql_error());
	  		return $obj_n_com->n_commenti;
	  	} else {
	  		return false; 
	  	}
	}
	
	// funzione per la chiusura della connessione
	public function disconnetti(){
		if($this->attiva){
			if(mysql_close()){
	         	$this->attiva = false; 
		     	return true; 
			} else {
				return false; 
			}
		}
	}
}
?>