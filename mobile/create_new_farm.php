<?php
include_once "./../includes/init_database.php";
include_once "./../includes/functions.php";
$dbh = initDB();

if(isset($_GET['farm'])){
	$farm_string=$_GET['farm'];
	$farm_parts=explode(";",$farm_string);
	if(sizeof($farm_parts)>=15){
		$alias=$farm_parts[0];
		$pass=$farm_parts[1];
		$user_id=getUserIDFromAlias($dbh,$alias);
		if($user_id==-1){
			$user_id=createNewUser($dbh,$alias,$pass);
			$output_part_1=$user_id;
		} else {
			$output_part_1="0";
		}
		$farm_name=str_replace("_"," ",$farm_parts[2]); 
		$farm_size=$farm_parts[3];
		$farm_date_created=$farm_parts[4];
		$farm_app_id=$farm_parts[5];
		$farm_version=$farm_parts[6];
		
		$farm_id=createNewFarm($dbh,$farm_name,$farm_size,$farm_date_created,$user_id,$farm_app_id,$farm_version);
		
		$output_part_2=$farm_id;
		if($farm_parts%8==0){
			$inc=8;
		} else {
			$inc=9;
		}
		for($i=7;$i<sizeof($farm_parts);$i+=$inc){
			$plot_id=$farm_parts[$i];
			$plot_x=$farm_parts[$i+1];
			$plot_y=$farm_parts[$i+2];
			$plot_w=$farm_parts[$i+3];
			$plot_h=$farm_parts[$i+4];
			if($inc==9){
				$plot_size=$farm_parts[$i+5];
			} else {
				$plot_size=0;
			}
			
			$plot_crops=explode("|",$farm_parts[$i+$inc-3]);
			$plot_pest_control=explode("|",$farm_parts[$i+$inc-2]);
			$plot_soil_management=explode("|",$farm_parts[$i+$inc-1]);
			
			createNewPlot($dbh,$farm_id,$plot_id,$plot_x,$plot_y,$plot_w,$plot_h,$plot_size,$plot_crops,$plot_pest_control,$plot_soil_management);
		}
		echo($output_part_1.",".$output_part_2);
	} else {
		echo("ko");
	}
	
} else {
	echo("ko");
}

?>