<?php
include_once "./../includes/init_database.php";
$dbh = initDB();

$df = fopen("php://output", 'w');
$query="SELECT data_item_id, data_item_name, data_item_default_units_id, data_item_type, is_crop_specific, is_treatment_specific, is_retroactive FROM data_item ORDER BY data_item_name";
$result = mysqli_query($dbh,$query);
while($row = mysqli_fetch_array($result,MYSQL_NUM)){
	fputcsv($df, $row);
}
fclose($df);

?>