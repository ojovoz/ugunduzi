<?php
include_once "./../includes/init_database.php";
include_once "./../includes/functions.php";
$dbh = initDB();

if(isset($_GET['farm']) && isset($_GET['user'])){
	$user_id=$_GET['user'];
	$farm_list=$_GET['farm'];
	$farm_name=explode(";",$farm_list);
	for($i=0;$i<sizeof($farm_name);$i++){
		$farm_id=getFarmIDFromNameUser($dbh,str_replace("_"," ",$farm_name[$i]),$user_id);
		deleteFarmData($dbh,$farm_id);
		deleteFarmPlots($dbh,$farm_id);
		$query="DELETE FROM farm WHERE farm_id=$farm_id";
		$result = mysqli_query($dbh,$query);
	}
	echo("ok");
}

?>