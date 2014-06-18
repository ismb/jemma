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
		    prod_energy,  energia generata
		    used_energy,  energia totale consumata
		    autocons_energy,  energia auto-consumata
		    buy_energy,  energia prelevata dalla rete
		    sold_energy  energia immessa in rete */
		    
		    $query = " SELECT 
						    hag_id,
						    autocons_rate,
						    @acons_energy:=round((prod_energy - sold_energy) / 1000, 2) as autocons_energy,
						    @sld_energy:=round(sold_energy / 1000, 2) as sold_energy,
						    @by_energy:=round(buy_energy / 1000, 2) as buy_energy,
						    @acons_energy + @sld_energy as prod_energy,
						    @acons_energy + @by_energy as used_energy
						FROM
						    Weekly_Prosumers_Data
						WHERE
						    (hag_id , start_date) = ('".$hag_id."' , (select subdate(current_date, weekday(current_date) + 7 * (".$numWeekBack." + 1)) )) AND 
						    ((greatest(buy_hours, sold_hours, prod_hours) - least(buy_hours, sold_hours, prod_hours)) <= 4) AND 
						    (least(buy_hours, sold_hours, prod_hours) >= (24 * 6.5));";
							
			$query = "SELECT 
						    hag_id,
						    autocons_rate,
						    @acons_energy:=round((prod_energy - sold_energy) / 1000, 2) as autocons_energy,
						    @sld_energy:=round(sold_energy / 1000, 2) as sold_energy,
						    @by_energy:=round(buy_energy / 1000, 2) as buy_energy,
						    @acons_energy + @sld_energy as prod_energy,
						    @acons_energy + @by_energy as used_energy,
						    least(buy_hours, sold_hours, prod_hours) as observed_hours
						FROM
						    Weekly_Prosumers_Data
						WHERE
						    (hag_id , start_date) = ('".$hag_id."' , (select subdate(current_date, weekday(current_date) + (".$numWeekBack." + 1) * 7) )) AND 
						    ((greatest(buy_hours, sold_hours, prod_hours) - least(buy_hours, sold_hours, prod_hours)) <= 4) and 
						    (least(buy_hours, sold_hours, prod_hours) >= (24 * 4));";
					    
		//print_r($query);echo "<br>";
		$post_sql = $data->query($query);
		
		$response = array();
										
		// controllo sul numero di record presenti in tabella
		if(mysql_num_rows($post_sql) > 0){
			// estrazione dei record tramite ciclo
		  	while($post_obj = $data->estrai($post_sql)){
			    $hag_id = $post_obj->hag_id;
			    $autocons_rate = $post_obj->autocons_rate;
			    $autocons_energy = $post_obj->autocons_energy;
			    $sold_energy = $post_obj->sold_energy;
			    $buy_energy = $post_obj->buy_energy;
			    $prod_energy = $post_obj->prod_energy;
			    $used_energy = $post_obj->used_energy;
			    $observed_hours = $post_obj->observed_hours;
			       
			    // visualizzazione dei dati
				array_push($response, array('hag_id' => $hag_id,
			    				  			'autocons_rate' => $autocons_rate,
			    				  			'autocons_energy' => $autocons_energy,
			    				  			'sold_energy' => $sold_energy,
			    				  			'buy_energy' => $buy_energy,
			    				  			'prod_energy' => $prod_energy,
			    				  			'used_energy' => $used_energy,
			    				  			'observed_hours' => $observed_hours));
		  	} 
		} else {
		  	// notifica in assenza di record
			array_push($response, array('hag_id' => NULL,
		    				  			'autocons_rate' => NULL,
		    				  			'autocons_energy' => NULL,
		    				  			'sold_energy' => NULL,
		    				  			'buy_energy' => NULL,
		    				  			'prod_energy' => NULL,
		    				  			'used_energy' => NULL,
			    				  		'observed_hours' => NULL));
		}
	    echo json_encode($response);
		die();
		// chiusura della connessione a MySQL
		$data->disconnetti();
	} catch (Exception $e) {
        echo ('An error has occured ('.$e.'): '.mysql_errno());        
    }
?>