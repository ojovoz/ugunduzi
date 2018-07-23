<?php
include_once "./../includes/init_database.php";
$dbh = initDB();

$df = fopen("php://output", 'w');
$query="SELECT treatment_ingredient_id, treatment_id, treatment_ingredient_name FROM treatment_ingredient ORDER BY treatment_ingredient_name";
$result = mysqli_query($dbh,$query);
while($row = mysqli_fetch_array($result,MYSQL_NUM)){
	fputcsv($df, $row);
}
fclose($df);

?>