<?php
header("Cache-Control: no-cache, must-revalidate");
session_start();
include_once "includes/init_database.php";
include_once "includes/functions.php";
include_once "includes/vars.php";
$dbh = initDB();

if(isset($_SESSION['user_id']) && isset($_SESSION['user_alias']) && isset($_SESSION['mode']) && isset($_GET['id'])){
	$id=$_GET['id'];
	$query="DELETE FROM log WHERE log_id=$id";
	$result = mysqli_query($dbh,$query);
} 
header("Location: feed.php");
?>