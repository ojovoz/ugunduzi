<?php
include_once "./../includes/init_database.php";
$dbh = initDB();

$user_id=$_GET['user'];

$df = fopen("php://output", 'w');
$query="SELECT plot_id FROM plot, farm WHERE farm.user_id=$user_id AND plot.farm_id = farm.farm_id";
$result = mysqli_query($dbh,$query);
$in_plots="";
while($row = mysqli_fetch_array($result,MYSQL_NUM)){
	if($in_plots==""){
		$in_plots="IN(".$row[0];
	} else {
		$in_plots.=",".$row[0];
	}
}
if($in_plots!=""){
	$in_plots.=")";
	$query="SELECT farm_app_id, farm_version, internal_plot_id, log_date, log_data_item_id, log_quantity, log_value, log_units_id, log_crop_id, log_treatment_id, log_comments FROM log, plot, farm WHERE plot.plot_id = log.plot_id AND log.plot_id ".$in_plots." AND farm.farm_id = plot.farm_id AND log_picture='' ORDER BY log_date";
	$result = mysqli_query($dbh,$query);
	while($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$separator=array("*");
		$complete=array_merge($row,$separator);
		fputcsv($df, $complete);
	}
}
fclose($df);
?>