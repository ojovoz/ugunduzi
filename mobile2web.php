<?php
header("Cache-Control: no-cache, must-revalidate");
session_start();
include_once "includes/init_database.php";
include_once "includes/functions.php";
$dbh = initDB();

if(isset($_GET['user']) && isset($_GET['pass'])){
	$user_alias=$_GET['user'];
	$user_password=$_GET['pass'];
	$id=getUserIDFromAliasPass($dbh,$user_alias,$user_password);
	if($id!=-1){
		$_SESSION['user_alias']=$user_alias;
		$_SESSION['user_id']=$id;
		$_SESSION['mode']=0; //mode: 0=everybody's images, 1=my data
		$_SESSION['admin']=userIsAdmin($dbh,$id);
		
		$_SESSION['user_filter']=array();
		$_SESSION['farm_filter']=array();
		$_SESSION['crop_filter']=array();
		$_SESSION['ingredient_filter']=array();
		
		header("Location: feed.php");
	} else {
		header("Location: index.php");
	}
} else {
	header("Location: index.php");
}
?>