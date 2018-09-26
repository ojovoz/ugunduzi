<?php
include_once "./../includes/init_database.php";
include_once "./../includes/functions.php";
$dbh = initDB();

if(isset($_GET['farms']) && isset($_GET['user'])){
	$user_id=$_GET['user'];
	$farm_string=$_GET['farms'];
	$farms=explode("*",$farm_string);
	for($i=0;$i<sizeof($farms);$i++){
		$farm_parts=explode(";",$farms[$i]);
		$farm_name=str_replace("_"," ",$farm_parts[0]); 
		$farm_size=$farm_parts[1];
		$farm_date_created=$farm_parts[2];
		$farm_app_id=$farm_parts[3];
		$farm_version=$farm_parts[4];
		$farm_id=createNewFarm($dbh,$farm_name,$farm_size,$farm_date_created,$user_id,$farm_app_id,$farm_version);
		for($j=5;$j<sizeof($farm_parts);$j+=8){
			$plot_id=$farm_parts[$j];
			$plot_x=$farm_parts[$j+1];
			$plot_y=$farm_parts[$j+2];
			$plot_w=$farm_parts[$j+3];
			$plot_h=$farm_parts[$j+4];
			
			$plot_crops=explode("|",$farm_parts[$j+5]);
			$plot_pest_control=explode("|",$farm_parts[$j+6]);
			$plot_soil_management=explode("|",$farm_parts[$j+7]);
			
			createNewPlot($dbh,$farm_id,$plot_id,$plot_x,$plot_y,$plot_w,$plot_h,$plot_crops,$plot_pest_control,$plot_soil_management);
		}
	}
	echo("ok");
} else {
	echo("ko");
}
?>