<?php
include_once "./../includes/init_database.php";
$dbh = initDB();

if(isset($_GET['lang'])){
	if($_GET['lang']=="en"){
		$name_field="treatment_ingredient_name_english";
	} else {
		$name_field="treatment_ingredient_name";
	}
} else {
	$name_field="treatment_ingredient_name";
}

$df = fopen("php://output", 'w');
$query="SELECT treatment_ingredient_id, treatment_id, ".$name_field." FROM treatment_ingredient ORDER BY ".$name_field;
$result = mysqli_query($dbh,$query);
while($row = mysqli_fetch_array($result,MYSQL_NUM)){
	fputcsv($df, $row);
}
fclose($df);

?>