<?php
include_once "./../includes/init_database.php";
$dbh = initDB();

if(isset($_GET['lang'])){
	if($_GET['lang']=="en"){
		$name_field="data_item_name_english";
	} else {
		$name_field="data_item_name";
	}
} else {
	$name_field="data_item_name";
}

$df = fopen("php://output", 'w');
$query="SELECT data_item_id, ".$name_field.", data_item_default_units_id, data_item_type, is_crop_specific, is_treatment_specific, is_retroactive FROM data_item ORDER BY ".$name_field;
$result = mysqli_query($dbh,$query);
while($row = mysqli_fetch_array($result,MYSQL_NUM)){
	fputcsv($df, $row);
}
fclose($df);

?>