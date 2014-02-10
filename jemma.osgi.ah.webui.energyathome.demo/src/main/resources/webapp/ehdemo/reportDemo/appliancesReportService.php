<?php
	//echo "<pre>";
	try{
		$last = $_GET['last'];
		$numWeekBack = $_GET['numWeekBack'];
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
					
		/*  Descrizione dei campi nella query
		    mean_daily_on_time,  tempo medio di accensione (report a riga 1 da 3 colonne) 
		    weekly_standby_consumption,  consumo stand-by settimanale (report a riga 2 colonna 2) 
		    weekly_avg_min_power,  media utilizzo della community (report a riga 2 colonna 1) 
		    yearly_standby_cost,  previsione costo stand-by annuale (report a riga 2 colonna 3) 
		    f23_percentage,  % di consumo nella fascia economica f23 (report b  riga 1 da 3 colonne) 
		    f23_rank,  posizione in classifica per il consumo in fascia economica (report b riga 2 colonna 1) 
		    weekly_avg_f23_percentage,  media di consumo della community in f23 (report b riga 2 colonna 2) 
		    yearly_cost_forecast  previsione annuale di costo totale (report b riga 2 colonna 3) */
		 
		$query = "SELECT 
					    appl_ep_resource_id,
					    a.start_date,
					    mean_daily_on_time,
					    weekly_standby_consumption,
					    weekly_avg_min_power,
					    yearly_standby_cost,
					    f23_percentage,
					    f23_rank,
					    weekly_avg_f23_percentage,
					    yearly_cost_forecast,
					    c.category_pid,
					    c.appliance_pid
					FROM
					    WeeklyReport_Appliances a
					        JOIN
					    WeeklyReport_Appliances_Means b USING (contract_code , category_pid , start_date)
					        JOIN
					    (SELECT * FROM Appliances WHERE (hag_id , end_point_id) = ('".$hag_id."', 1)) c USING (appl_ep_resource_id)
					WHERE
					    (a.start_date) = subdate(current_date, weekday(current_date) + 7 * (".$numWeekBack." + 1))"; 
					    
		//print_r($query);echo "<br>";
		$post_sql = $data->query($query);
		
		$response = array();
										
		// controllo sul numero di record presenti in tabella
		if(mysql_num_rows($post_sql) > 0){
			// estrazione dei record tramite ciclo
		  	while($post_obj = $data->estrai($post_sql)){
			    $mean_daily_on_time = $post_obj->mean_daily_on_time;
			    $category_pid = $post_obj->category_pid;
			    $appl_ep_resource_id = $post_obj->appl_ep_resource_id;
			    $appliance_pid = $post_obj->appliance_pid;
			    $weekly_standby_consumption = $post_obj->weekly_standby_consumption;
			    $weekly_avg_min_power = $post_obj->weekly_avg_min_power;
			    $yearly_standby_cost = $post_obj->yearly_standby_cost;
			    $f23_percentage = $post_obj->f23_percentage;
			    $f23_rank = $post_obj->f23_rank;
			    $weekly_avg_f23_percentage = $post_obj->weekly_avg_f23_percentage;
			    $yearly_cost_forecast = $post_obj->yearly_cost_forecast;
			       
			    // visualizzazione dei dati
				array_push($response, array('mean_daily_on_time' => $mean_daily_on_time,
			    				  			'category_pid' => $category_pid,
			    				  			'appl_ep_resource_id' => $appl_ep_resource_id,
			    				  			'appliance_pid' => $appliance_pid,
			    				  			'weekly_standby_consumption' => $weekly_standby_consumption,
			    				  			'weekly_avg_min_power' => $weekly_avg_min_power,
			    				  			'yearly_standby_cost' => $yearly_standby_cost,
			    				  			'f23_percentage' => $f23_percentage,
			    				  			'f23_rank' => $f23_rank,
			    				  			'weekly_avg_f23_percentage' => $weekly_avg_f23_percentage,
			    				  			'yearly_cost_forecast' => $yearly_cost_forecast));
		  	} 
		} else {
		  	// notifica in assenza di record
			array_push($response, array('mean_daily_on_time' => NULL,
			    				  			'category_pid' => NULL,
			    				  			'appl_ep_resource_id' => NULL,
			    				  			'appliance_pid' => NULL,
			    				  			'weekly_standby_consumption' => NULL,
			    				  			'weekly_avg_min_power' => NULL,
			    				  			'yearly_standby_cost' => NULL,
			    				  			'f23_percentage' => NULL,
			    				  			'f23_rank' => NULL,
			    				  			'weekly_avg_f23_percentage' => NULL,
			    				  			'yearly_cost_forecast' => NULL));
		}
	    echo json_encode($response);
		die();
		// chiusura della connessione a MySQL
		$data->disconnetti();
	} catch (Exception $e) {
        echo ('An error has occured ('.$e.'): '.mysql_errno());        
    }
?>