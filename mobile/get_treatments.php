<?php
include_once "./../includes/init_database.php";
$dbh = initDB();

if(isset($_GET['lang'])){
	if($_GET['lang']=="en"){
		$name_field="treatment_name_english";
	} else {
		$name_field="treatment_name";
	}
} else {
	$name_field="treatment_name";
}

$df = fopen("php://output", 'w');
$query="SELECT treatment_id, ".$name_field.", treatment_category FROM treatment ORDER BY ".$name_field;
$result = mysqli_query($dbh,$query);
while($row = mysqli_fetch_array($result,MYSQL_NUM)){
	fputcsv($df, $row);
}
fclose($df);

?>