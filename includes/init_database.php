<?
//initialize dbConnection
function initDB() {
	$host="localhost";
	$db="ugunduzi";
	$db_user="mysautiyaw";
	$db_pass="j8bA0y11";
	$dbh=mysqli_connect($host, $db_user, $db_pass, $db);
	return $dbh;
}

//
?>