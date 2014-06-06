<?php
	date_default_timezone_set('Europe/Rome');
	//echo "<pre>";
	try{
		$hag_id = $_GET['hag_id'];
		$numWeekBack = $_GET['numWeekBack'];

		// inclusione del file di classe
		include "php/funzioni_mysql.php";
		// istanza della classe
		$data = new MysqlClass();
		// chiamata alla funzione di connessione
		$data->connetti();
		// query per l'estrazione dei record
		$query = "  SELECT 
						a.start_date,
					    weekly_min_power,
					    yearly_min_power_cost,
					    min_power_rank,
					    f23_percentage, 
						f23_rank,
					    yearly_cost_forecast,
					    yearly_consumption_forecast, 
					    weekly_avg_min_power,
						weekly_avg_f23_percentage
					FROM
					    WeeklyReport_TotalHouse a
					        JOIN
					    Profiles USING (hag_id)
					        JOIN
					    WeeklyReport_TotalHouse_Means USING (contract_code, start_date)
					WHERE
					    (a.hag_id , a.start_date) = ('".$hag_id."' , (SELECT SUBDATE(CURRENT_DATE(), WEEKDAY(CURRENT_DATE()) + 7 * (1+".$numWeekBack.")) AS start_date))";
		//print_r($query);echo "<br>";
		$post_sql = $data->query($query);
		//print_r($post_sql);			echo "<br>";					
		// controllo sul numero di record presenti in tabella
		if(mysql_num_rows($post_sql) > 0){
			// estrazione dei record tramite ciclo
		  	while($post_obj = $data->estrai($post_sql)){
		  		//print_r($post_obj);echo "<br>";
			    $start_date = $post_obj->start_date;
			    $weekly_min_power = $post_obj->weekly_min_power;
			    $yearly_min_power_cost = $post_obj->yearly_min_power_cost;
			    $min_power_rank = $post_obj->min_power_rank;
			    $f23_percentage = $post_obj->f23_percentage;
			    $f23_rank = $post_obj->f23_rank;
			    $yearly_cost_forecast = $post_obj->yearly_cost_forecast;
			    $yearly_consumption_forecast = $post_obj->yearly_consumption_forecast;
			    $weekly_avg_min_power = $post_obj->weekly_avg_min_power;
			    $weekly_avg_f23_percentage = $post_obj->weekly_avg_f23_percentage;
			    
			    $settPrecDate = gestSettimana($start_date);
			    
			    $response = array('weekly_min_power' => $weekly_min_power,
			    				  'yearly_min_power_cost' => $yearly_min_power_cost,
			    				  'min_power_rank' => $min_power_rank,
			    				  'f23_rank' => $f23_rank,
			    				  'f23_percentage' => $f23_percentage,
			    				  'yearly_cost_forecast' => $yearly_cost_forecast,
			    				  'yearly_consumption_forecast' => $yearly_consumption_forecast,
			    				  'weekly_avg_min_power' => $weekly_avg_min_power,
			    				  'weekly_avg_f23_percentage' => $weekly_avg_f23_percentage,
								  'settPrecDate' => $settPrecDate);
			       
			    // visualizzazione dei dati
		  	} 
		}else{
			    
			$settPrecDate = gestSettimana(NULL);
		  	// notifica in assenza di record
		    $response = array('weekly_min_power' => NULL,
		    				  'yearly_min_power_cost' => NULL,
		    				  'min_power_rank' => NULL,
		    				  'f23_rank' => NULL,
		    				  'f23_percentage' => NULL,
		    				  'yearly_cost_forecast' => NULL,
		    				  'yearly_consumption_forecast' => NULL,
		    				  'weekly_avg_min_power' => NULL,
		    				  'weekly_avg_f23_percentage' => NULL,
							  'settPrecDate' => $settPrecDate);
		}
	    echo json_encode($response);
		die();
		// chiusura della connessione a MySQL
		$data->disconnetti();
	} catch (Exception $e) {
        echo ('An error has occured ('.$e.'): '.mysql_errno());        
    }
	
	function gestSettimana($dbDate){
		
		if ($dbDate == NULL){
			$dbDate = date ("d-m-Y",mktime( 0, 0, 0, date("m"), date("d"), date("Y")));
			$oggi = getdate();

			$lunPrecWeekDate = date("d", mktime( 0, 0, 0, date("m"), date("d") - ($oggi['wday'] + 6), date("Y")));
			$domPrecWeekDate = date("d", mktime( 0, 0, 0, date("m"), date("d") - ($oggi['wday']), date("Y")));
			
			$monthLunPrecWeekDate = date("m", mktime( 0, 0, 0, date("m"), date("d") - ( $oggi['wday'] + 6), date("Y")));
			$monthDomPrecWeekDate = date("m", mktime( 0, 0, 0, date("m"), date("d") - ( $oggi['wday']), date("Y")));
			
			$yearLunPrecWeekDate = date("Y", mktime( 0, 0, 0, date("m"), date("d") - ( $oggi['wday'] + 6), date("Y")));
			$yearDomPrecWeekDate = date("Y", mktime( 0, 0, 0, date("m"), date("d") - ( $oggi['wday']), date("Y")));
	
			$returnDate[0] = array($lunPrecWeekDate, $monthLunPrecWeekDate, $yearLunPrecWeekDate);
			$returnDate[1] = array($domPrecWeekDate, $monthDomPrecWeekDate, $yearDomPrecWeekDate);
		} else {
			
			$myDBDate = explode(' ', $dbDate);
			$myDate = explode('-', $myDBDate[0]);
			
			$returnDate = array();
	
			$lunPrecWeekDate = date("d", mktime( 0, 0, 0, $myDate[1], $myDate[2], $myDate[0]));
			$domPrecWeekDate = date("d", mktime( 0, 0, 0, $myDate[1], $myDate[2] + 6, $myDate[0]));
			
			$monthLunPrecWeekDate = date("m", mktime( 0, 0, 0, $myDate[1], $myDate[2], $myDate[0]));
			$monthDomPrecWeekDate = date("m", mktime( 0, 0, 0, $myDate[1], $myDate[2] + 6, $myDate[0]));
			
			$yearLunPrecWeekDate = date("Y", mktime( 0, 0, 0, $myDate[1], $myDate[2], $myDate[0]));
			$yearDomPrecWeekDate = date("Y", mktime( 0, 0, 0, $myDate[1], $myDate[2] + 6, $myDate[0]));

			$returnDate[0] = array($lunPrecWeekDate, $monthLunPrecWeekDate, $yearLunPrecWeekDate);
			$returnDate[1] = array($domPrecWeekDate, $monthDomPrecWeekDate, $yearDomPrecWeekDate);
		}
		return $returnDate;
	}
?>