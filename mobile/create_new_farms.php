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
		if($farm_parts%8==0){
			$inc=8;
		} else {
			$inc=9;
		}
		for($j=5;$j<sizeof($farm_parts);$j+=$inc){
			$plot_id=$farm_parts[$j];
			$plot_x=$farm_parts[$j+1];
			$plot_y=$farm_parts[$j+2];
			$plot_w=$farm_parts[$j+3];
			$plot_h=$farm_parts[$j+4];
			if($inc==9){
				$plot_size=$farm_parts[$i+5];
			} else {
				$plot_size=0;
			}
			
			$plot_crops=explode("|",$farm_parts[$j+$inc-3]);
			$plot_pest_control=explode("|",$farm_parts[$j+$inc-2]);
			$plot_soil_management=explode("|",$farm_parts[$j+$inc-1]);
			
			createNewPlot($dbh,$farm_id,$plot_id,$plot_x,$plot_y,$plot_w,$plot_h,$plot_size,$plot_crops,$plot_pest_control,$plot_soil_management);
		}
	}
	echo("ok");
} else {
	echo("ko");
}
?>