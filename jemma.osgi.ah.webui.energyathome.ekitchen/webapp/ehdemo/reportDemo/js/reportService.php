<?php
	try{
		$hag_id = $_GET['hag_id'];

		// inclusione del file di classe
		include "php/funzioni_mysql.php";
		// istanza della classe
		$data = new MysqlClass();
		// chiamata alla funzione di connessione
		$data->connetti();
		// query per l'estrazione dei record
		$query = "  SELECT 
					    weekly_min_power,
					    yearly_min_power_cost,
					    min_power_rank,
					    f23_percentage, 
						f23_rank,
					    yearly_cost_forecast,
					    weekly_avg_min_power,
						weekly_avg_f23_percentage
					FROM
					    WeeklyReport_TotalHouse a
					        JOIN
					    Gui_Report_Groups USING (hag_id)
					        JOIN
					    WeeklyReport_TotalHouse_Means USING (contract_code, start_date)
					WHERE
					    (a.hag_id , a.start_date) = ('".$hag_id."' , (SELECT SUBDATE(CURRENT_DATE(), WEEKDAY(CURRENT_DATE()) + 7) AS start_date))";
		
		$post_sql = $data->query($query);
										
		// controllo sul numero di record presenti in tabella
		if(mysql_num_rows($post_sql) > 0){
			// estrazione dei record tramite ciclo
		  	while($post_obj = $data->estrai($post_sql)){
			    $weekly_min_power = $post_obj->weekly_min_power;
			    $yearly_min_power_cost = $post_obj->yearly_min_power_cost;
			    $min_power_rank = $post_obj->min_power_rank;
			    $f23_percentage = $post_obj->f23_percentage;
			    $f23_rank = $post_obj->f23_rank;
			    $yearly_cost_forecast = $post_obj->yearly_cost_forecast;
			    $weekly_avg_min_power = $post_obj->weekly_avg_min_power;
			    $weekly_avg_f23_percentage = $post_obj->weekly_avg_f23_percentage;
			    
			    $response = array('weekly_min_power' => $weekly_min_power,
			    				  'yearly_min_power_cost' => $yearly_min_power_cost,
			    				  'min_power_rank' => $min_power_rank,
			    				  'f23_rank' => $f23_rank,
			    				  'f23_percentage' => $f23_percentage,
			    				  'yearly_cost_forecast' => $yearly_cost_forecast,
			    				  'weekly_avg_min_power' => $weekly_avg_min_power,
			    				  'weekly_avg_f23_percentage' => $weekly_avg_f23_percentage);
			       
			    // visualizzazione dei dati
		  	} 
		    echo json_encode($response);
			die();
		}else{
		  	// notifica in assenza di record
		  	echo 'ERRORE!';
		}
		// chiusura della connessione a MySQL
		$data->disconnetti();
	} catch (Exception $e) {
        echo ('An error has occured ('.$e.'): '.mysql_errno());        
    }
?>