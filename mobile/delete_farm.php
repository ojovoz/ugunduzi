<?php
include_once "./../includes/init_database.php";
include_once "./../includes/functions.php";
$dbh = initDB();

if(isset($_GET['farm']) && isset($_GET['user'])){
	$user_id=$_GET['user'];
	$farm_list=$_GET['farm'];
	$farm_app_id=explode(";",$farm_list);
	for($i=0;$i<sizeof($farm_app_id);$i++){
		$farm_id=getFarmIDFromFarmAppId($dbh,$farm_app_id[$i]);
		deleteFarmData($dbh,$farm_id);
		deleteFarmPlots($dbh,$farm_id);
		$query="DELETE FROM farm WHERE farm_id=$farm_id";
		$result = mysqli_query($dbh,$query);
	}
	echo("ok");
}

?>