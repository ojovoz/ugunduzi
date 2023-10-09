<?php
header("Cache-Control: no-cache, must-revalidate");
session_start();
include_once "init_database.php";
include_once "functions.php";
$dbh = initDB();

if(isset($_GET['id'])){
	$plots=getFarmPlots($dbh,$_GET['id']);
	echo($plots);
} 
?>