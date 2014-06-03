<?php
	try{
		$last = $_GET['last'];
		$hag_id = $_GET['hag_id'];
		$strForEldoId = '';
		
		for ($iCounter = 0; $iCounter <= $last; $iCounter++){
			if ($iCounter == ($last)){
				//$strForEldoId .= "'ah.app.".$_GET['eldo'.$iCounter]."'";
				$strForEldoId .= "'".$_GET['eldo'.$iCounter]."'";
			} else {
				//$strForEldoId .= "'ah.app.".$_GET['eldo'.$iCounter]."',";
				$strForEldoId .= "'".$_GET['eldo'.$iCounter]."',";
			}
		}
		
		// inclusione del file di classe
		include "php/funzioni_mysql.php";
		// istanza della classe
		$data = new MysqlClass();
		// chiamata alla funzione di connessione
		$data->connetti();
		// query per l'estrazione dei record
		$query = "  SELECT 
					    name, category_pid, location_pid, creation_time, appliance_pid
					FROM
					    Appliances
					WHERE
					    hag_id = '".$hag_id."' AND end_point_id = 1
					        AND appliance_pid IN ($strForEldoId);";
		
		$post_sql = $data->query($query);
		
		$response = array();
										
		// controllo sul numero di record presenti in tabella
		if(mysql_num_rows($post_sql) > 0){
			// estrazione dei record tramite ciclo
		  	while($post_obj = $data->estrai($post_sql)){
			    $name = $post_obj->name;
			    $category_pid = $post_obj->category_pid;
			    $location_pid = $post_obj->location_pid;
			    $creation_time = $post_obj->creation_time;
			    $appliance_pid = $post_obj->appliance_pid;
			       
			    // visualizzazione dei dati
				array_push($response, array('name' => $name,
			    				  			'category_pid' => $category_pid,
			    				  			'location_pid' => $location_pid,
			    				  			'creation_time' => $creation_time,
			    				  			'appliance_pid' => $appliance_pid));
		  	} 
		} else {
		  	// notifica in assenza di record
			array_push($response, array('name' => NULL,
		    				  			'category_pid' => NULL,
		    				  			'location_pid' => NULL,
		    				  			'creation_time' => NULL,
		    				  			'appliance_pid' => NULL));
		}
	    echo json_encode($response);
		die();
		// chiusura della connessione a MySQL
		$data->disconnetti();
	} catch (Exception $e) {
        echo ('An error has occured ('.$e.'): '.mysql_errno());        
    }
?>