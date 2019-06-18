<?php
header("Cache-Control: no-cache, must-revalidate");
session_start();
include_once "init_database.php";
include_once "functions.php";
$dbh = initDB();

if(isset($_GET['id']) && isset($_GET['from'])){
	$farms=getPlotDataEntries($dbh,$_GET['id'],$_GET['from']);
	echo($farms);
} 
?>