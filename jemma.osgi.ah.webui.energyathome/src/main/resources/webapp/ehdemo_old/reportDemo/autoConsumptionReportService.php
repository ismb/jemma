<?php
	//echo "<pre>";
	try{
		$numWeekBack = $_GET['numWeekBack'];
		$hag_id = $_GET['hag_id'];
		$strForEldoId = '';
		
		// inclusione del file di classe
		include "php/funzioni_mysql.php";
		// istanza della classe
		$data = new MysqlClass();
		// chiamata alla funzione di connessione
		$data->connetti();
		// query per l'estrazione dei record
					
		/*  Descrizione dei campi nella query
		    autocons_rate,  indice di auto-consumo
		    autocons_rank,  posizione in classifica
		    weekly_mean_autoconsumption_rate,  media utilizzo della community
		    autocons_energy  energia auto-consumata */
		    
		    $query = "SELECT 
					    hag_id,
					    autocons_rate,
					    autocons_rank,
					    weekly_mean_autoconsumption_rate,
					    round((prod_energy-sold_energy)/1000,2) as autocons_energy
					FROM
					    Weekly_Prosumers_Data
					        JOIN
					    Weekly_Prosumers_Data_Means USING (start_date)
					WHERE
					    (hag_id,start_date) = ('".$hag_id."', (select subdate(current_date,weekday(current_date) + 7 * (".$numWeekBack." + 1))) );";
					    
		//print_r($query);echo "<br>";
		$post_sql = $data->query($query);
		
		$response = array();
										
		// controllo sul numero di record presenti in tabella
		if(mysql_num_rows($post_sql) > 0){
			// estrazione dei record tramite ciclo
		  	while($post_obj = $data->estrai($post_sql)){
			    $hag_id = $post_obj->hag_id;
			    $autocons_rate = $post_obj->autocons_rate;
			    $autocons_rank = $post_obj->autocons_rank;
			    $weekly_mean_autoconsumption_rate = $post_obj->weekly_mean_autoconsumption_rate;
			    $autocons_energy = $post_obj->autocons_energy;
			       
			    // visualizzazione dei dati
				array_push($response, array('hag_id' => $hag_id,
			    				  			'autocons_rate' => $autocons_rate,
			    				  			'autocons_rank' => $autocons_rank,
			    				  			'weekly_mean_autoconsumption_rate' => $weekly_mean_autoconsumption_rate,
			    				  			'autocons_energy' => $autocons_energy));
		  	} 
		} else {
		  	// notifica in assenza di record
			array_push($response, array('hag_id' => NULL,
		    				  			'autocons_rate' => NULL,
		    				  			'autocons_rank' => NULL,
		    				  			'weekly_mean_autoconsumption_rate' => NULL,
		    				  			'autocons_energy' => NULL));
		}
	    echo json_encode($response);
		die();
		// chiusura della connessione a MySQL
		$data->disconnetti();
	} catch (Exception $e) {
        echo ('An error has occured ('.$e.'): '.mysql_errno());        
    }
?>