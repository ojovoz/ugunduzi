<?php
include_once "./../includes/init_database.php";
include_once "./../includes/functions.php";
$dbh = initDB();

if(isset($_GET['farm']) && isset($_GET['user'])){
	$user_id=$_GET['user'];
	$farm_list=$_GET['farm'];
	$farm_app_id=explode(";",$farm_list);
	for($i=0;$i<sizeof($farm_app_id);$i++){
		$farm_ids=getFarmIDsFromFarmAppIdUser($dbh,$farm_app_id[$i],$user_id);
		for($j=0;$j<sizeof($farm_ids);$j++){
			$this_id=$farm_ids[$j];
			deleteFarmData($dbh,$this_id);
			deleteFarmPlots($dbh,$this_id);
			$query="DELETE FROM farm WHERE farm_id=$this_id";
			$result = mysqli_query($dbh,$query);
		}
	}
	echo("ok");
}

?>