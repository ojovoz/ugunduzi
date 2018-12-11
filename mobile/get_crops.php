<?php
include_once "./../includes/init_database.php";
$dbh = initDB();

if(isset($_GET['lang'])){
	if($_GET['lang']=="en"){
		$name_field="crop_name_english";
	} else {
		$name_field="crop_name";
	}
} else {
	$name_field="crop_name";
}

$df = fopen("php://output", 'w');
$query="SELECT crop_id, ".$name_field.", crop_variety FROM crop ORDER BY ".$name_field.", crop_variety";
$result = mysqli_query($dbh,$query);
while($row = mysqli_fetch_array($result,MYSQL_NUM)){
	fputcsv($df, $row);
}
fclose($df);

?>