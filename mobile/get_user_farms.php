<?php
include_once "./../includes/init_database.php";
$dbh = initDB();

$user_id=$_GET['user'];

$df = fopen("php://output", 'w');
$query_farm="SELECT farm_name, farm_app_id, farm_version, farm_size_acres, farm_date_created, plot_id, internal_plot_id, plot_x, plot_y, plot_w, plot_h, plot_size FROM farm, plot WHERE farm.user_id = $user_id AND plot.farm_id = farm.farm_id ORDER BY farm_name, farm_version, internal_plot_id";
$result_farm = mysqli_query($dbh,$query_farm);
while($row_farm = mysqli_fetch_array($result_farm,MYSQL_NUM)){
	$plot_id=$row_farm[5];
	$query_crops="SELECT crop_id FROM crop_x_plot WHERE plot_id=$plot_id";
	$result_crops = mysqli_query($dbh,$query_crops);
	$string_crops = "-1";
	while($row1 = mysqli_fetch_array($result_crops,MYSQL_NUM)){
		if($string_crops=="-1"){
			$string_crops=$row1[0];
		} else {
			$string_crops.="|".$row1[0];
		}
	}
	$row_crops=array($string_crops);
	$query_pest_control="SELECT treatment_ingredient.treatment_ingredient_id FROM treatment_ingredient_x_plot, treatment_ingredient WHERE plot_id=$plot_id AND treatment_ingredient_x_plot.treatment_ingredient_id = treatment_ingredient.treatment_ingredient_id AND treatment_id=1";
	$result_pest_control = mysqli_query($dbh,$query_pest_control);
	$string_pest_control="-1";
	while($row2 = mysqli_fetch_array($result_pest_control,MYSQL_NUM)){
		if($string_pest_control=="-1"){
			$string_pest_control=$row2[0];
		} else {
			$string_pest_control.="|".$row2[0];
		}
	}
	$row_pest_control = array($string_pest_control);
	$query_soil_management="SELECT treatment_ingredient.treatment_ingredient_id FROM treatment_ingredient_x_plot, treatment_ingredient WHERE plot_id=$plot_id AND treatment_ingredient_x_plot.treatment_ingredient_id = treatment_ingredient.treatment_ingredient_id AND treatment_id=2";
	$result_soil_management = mysqli_query($dbh,$query_soil_management);
	$string_soil_management="-1";
	while($row3 = mysqli_fetch_array($result_soil_management,MYSQL_NUM)){
		if($string_soil_management=="-1"){
			$string_soil_management=$row3[0];
		} else {
			$string_soil_management.="|".$row3[0];
		}
	}
	$row_soil_management = array($string_soil_management);
	$row_separator=array("*");
	$complete=array_merge($row_farm,$row_crops,$row_pest_control,$row_soil_management,$row_separator);
	fputcsv($df, $complete);
}
fclose($df);

?>