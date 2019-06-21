<?php

function getUserIDFromAlias($dbh,$alias){
	$ret=-1;
	$query="SELECT user_id FROM user WHERE user_alias='$alias'";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=$row[0];
	}
	return $ret;
}

function getUserAliasFromID($dbh,$id){
	$ret="";
	$query="SELECT user_alias FROM user WHERE user_id=$id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=$row[0];
	}
	return $ret;
}

function getUserNameFromID($dbh,$id){
	$ret="";
	$query="SELECT user_name FROM user WHERE user_id=$id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=$row[0];
	}
	return $ret;
}

function getUserIDFromAliasPass($dbh,$alias,$pass){
	$ret=-1;
	$query="SELECT user_id FROM user WHERE user_alias='$alias' AND user_password='$pass'";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=$row[0];
	}
	return $ret;
}

function getUserPassFromId($dbh,$id){
	$ret="-1";
	$query="SELECT user_password FROM user WHERE user_id=$id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=$row[0];
	}
	return $ret;
}

function userIsAdmin($dbh,$id){
	$ret=false;
	$query="SELECT is_admin FROM user WHERE user_id=$id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=($row[0]==1);
	}
	return $ret;
}

function getUserFarms($dbh,$id){
	$ret="";
	$query="SELECT farm_id, farm_app_id, farm_name, farm_size_acres, farm_date_created, farm_version FROM farm WHERE user_id=$id ORDER BY farm_app_id, farm_version";
	$result = mysqli_query($dbh,$query);
	$i=0;
	$prev_app_id="";
	while($row = mysqli_fetch_array($result,MYSQL_NUM)){
		if($i==0){
			$farm_id=$row[0];
			$farm_app_id=$row[1];
			$farm_name=$row[2];
			$farm_size=$row[3];
			$farm_date=$row[4];
			$prev_app_id=$farm_app_id;
		} else {
			if($row[1]==$prev_app_id){
				$farm_id=$farm_id.":".$row[0];
				$farm_name=$row[2];
				$farm_size=$row[3];
				$farm_date=$row[4];
			} else {
				$ret=($ret=="" ? $farm_id.",".$farm_name.",".$farm_size.",".$farm_date : $ret.";".$farm_id.",".$farm_name.",".$farm_size.",".$farm_date);
				$farm_id=$row[0];
				$farm_app_id=$row[1];
				$farm_name=$row[2];
				$farm_size=$row[3];
				$farm_date=$row[4];
				$prev_app_id=$farm_app_id;
			}
		}
		$i++;
	}
	if($i>0){
		$ret=($ret=="" ? $farm_id.",".$farm_name.",".$farm_size.",".$farm_date : $ret.";".$farm_id.",".$farm_name.",".$farm_size.",".$farm_date);
	}
	return $ret;
}

function getFarmPlots($dbh,$id){
	$ret="";
	
	$farm_ids=explode(":",$id);
	
	for($i=count($farm_ids)-1;$i>=0;$i--){
		
		$this_farm=$farm_ids[$i];
		
		$farm_date=getFarmDate($dbh,$this_farm);
		$farm_size=getFarmSize($dbh,$this_farm);
		
		$query_plot="SELECT plot_id, plot_x, plot_y, plot_w, plot_h, plot_size FROM plot WHERE farm_id=$this_farm ORDER BY plot_y, plot_x";
		$result_plot = mysqli_query($dbh,$query_plot);
		while($row_plot = mysqli_fetch_array($result_plot,MYSQL_NUM)){
			$plot_id=$row_plot[0];
			$plot_x=$row_plot[1];
			$plot_y=$row_plot[2];
			$plot_w=$row_plot[3];
			$plot_h=$row_plot[4];
			$plot_size=$row_plot[5];
		
			$query_crop="SELECT crop_name FROM crop, crop_x_plot WHERE crop_x_plot.plot_id = $plot_id AND crop.crop_id = crop_x_plot.crop_id";
			$result_crop = mysqli_query($dbh,$query_crop);
			$crop_names="";
			while($row_crop = mysqli_fetch_array($result_crop,MYSQL_NUM)){
				$crop_names=($crop_names=="" ? $row_crop[0] : $crop_names.",".$row_crop[0]);
			}
		
			$query_pest_control="SELECT treatment_ingredient_name FROM treatment_ingredient, treatment_ingredient_x_plot WHERE treatment_ingredient_x_plot.plot_id = $plot_id AND treatment_ingredient.treatment_ingredient_id = treatment_ingredient_x_plot.treatment_ingredient_id AND treatment_id = 1";
			$result_pest_control = mysqli_query($dbh,$query_pest_control);
			$pest_control_names="";
			while($row_pest_control = mysqli_fetch_array($result_pest_control,MYSQL_NUM)){
				$pest_control_names=($pest_control_names=="" ? $row_pest_control[0] : $pest_control_names.",".$row_pest_control[0]);
			}
		
			$query_soil_management="SELECT treatment_ingredient_name FROM treatment_ingredient, treatment_ingredient_x_plot WHERE treatment_ingredient_x_plot.plot_id = $plot_id AND treatment_ingredient.treatment_ingredient_id = treatment_ingredient_x_plot.treatment_ingredient_id AND treatment_id = 2";
			$result_soil_management = mysqli_query($dbh,$query_soil_management);
			$soil_management_names="";
			while($row_soil_management = mysqli_fetch_array($result_soil_management,MYSQL_NUM)){
				$soil_management_names=($soil_management_names=="" ? $row_soil_management[0] : $soil_management_names.",".$row_soil_management[0]);
			}
		
			$this_plot=$plot_id.";".$plot_x.";".$plot_y.";".$plot_w.";".$plot_h.";".$plot_size.";".$crop_names.";".$pest_control_names.";".$soil_management_names;
			$ret=($ret=="" ? $this_plot : (substr($ret,-1)=="*" ? $ret.$this_plot : $ret."|".$this_plot));
		}
		$ret.=";".$farm_date.";".$farm_size.";".$this_farm;
		$ret=($i==0 ? $ret : $ret."*");
	}
	
	return $ret;
}

function getFarmDate($dbh,$id){
	$ret="";
	$query="SELECT farm_date_created FROM farm WHERE farm_id=$id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=$row[0];
	}
	return $ret;
}

function getFarmSize($dbh,$id){
	$ret="";
	$query="SELECT farm_size_acres FROM farm WHERE farm_id=$id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=$row[0];
	}
	return $ret;
}

function createNewUser($dbh,$alias,$pass){
	$query="INSERT INTO user (user_alias, user_password) VALUES('$alias', '$pass')";
	$result = mysqli_query($dbh,$query);
	$id = mysqli_insert_id($dbh);
	return $id;
}

function getFarmIDFromNameUser($dbh,$farm_name,$user_id){
	$ret=-1;
	$query="SELECT farm_id FROM farm WHERE farm_name='$farm_name' AND user_id=$user_id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=$row[0];
	}
	return $ret;
}

function getFarmNameFromID($dbh,$farm_id){
	$ret="";
	$query="SELECT farm_name FROM farm WHERE farm_id=$farm_id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=$row[0];
	}
	return $ret;
}

function getFarmIDsFromFarmAppIdUser($dbh,$farm_app_id,$user_id){
	$ret=array();
	$query="SELECT farm_id FROM farm WHERE farm_app_id=$farm_app_id AND user_id=$user_id";
	$result = mysqli_query($dbh,$query);
	$n=0;
	while($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret[$n]=$row[0];
		$n++;
	}
	return $ret;
}

function getFarmIDFromFarmAppIdVersionUser($dbh,$farm_app_id,$farm_version,$user_id){
	$ret=-1;
	$query="SELECT farm_id FROM farm WHERE farm_app_id=$farm_app_id AND farm_version=$farm_version AND user_id=$user_id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=$row[0];
	}
	return $ret;
}

function createNewFarm($dbh,$farm_name,$farm_size,$farm_date_created,$user_id,$farm_app_id,$farm_version){
	$query="INSERT INTO farm (user_id, farm_name, farm_size_acres, farm_date_created, farm_app_id, farm_version) VALUES ($user_id, '$farm_name', $farm_size, '$farm_date_created', $farm_app_id, $farm_version)";
	$result = mysqli_query($dbh,$query);
	$id = mysqli_insert_id($dbh);
	return $id;
}

function updateFarm($dbh,$farm_id,$farm_name,$farm_size,$farm_date_created,$farm_version){
	$query="UPDATE farm SET farm_name='$farm_name', farm_size_acres=$farm_size, farm_date_created='$farm_date_created' WHERE farm_id=$farm_id";
	$result = mysqli_query($dbh,$query);
}

function farmHasData($dbh,$farm_id){
	$ret=false;
	$query="SELECT COUNT(log_id) FROM log,plot WHERE plot.farm_id=$farm_id AND log.plot_id=plot.plot_id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		if($row[0]>0){
			$ret=true;
		}
	}
	return $ret;
}

function getFarmDataEntries($dbh,$id,$from){
	
	$in=strval($id*-1);
	
	$query="SELECT plot_id FROM plot WHERE farm_id=$id";
	$result = mysqli_query($dbh,$query);
	while($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$in.=",".$row[0];
	}
	
	$in="(".$in.")";
	
	$query="SELECT COUNT(log_id) FROM log WHERE log.plot_id IN ".$in;
	$result = mysqli_query($dbh,$query);
	$ret=($row = mysqli_fetch_array($result,MYSQL_NUM))? $row[0] : "0";
	
	$query="SELECT log_date, log_data_item_id, log_quantity, log_value, log_units_id, log_crop_id, log_treatment_id, log_comments, log_picture, log_sound, plot_id FROM log WHERE log.plot_id IN ".$in." ORDER BY log_date DESC";
	$result = mysqli_query($dbh,$query);
	while($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$this_entry=$row[0];
		if($row[10]!=($id*-1)){
			$this_entry.=";".getPlotContents($dbh,$row[10]);
		} else {
			$this_entry.=";Entire farm";
		}
		if($row[8]!="" && $row[9]!=""){
			$this_entry.=";".$row[8].";".$row[9].";".$row[7];
		} else {
			$data_item_string=getDataItemString($dbh,$row[1],$row[5],$row[6],$row[2],$row[3],$row[4]);
			$this_entry.=";".$data_item_string.";".$row[7];
		}
		$ret=$ret."*".$this_entry;
	}
	return $ret;
}

function getPlotDataEntries($dbh,$id,$from){
	$query="SELECT COUNT(log_id) FROM log WHERE log.plot_id=$id";
	$result = mysqli_query($dbh,$query);
	$ret=($row = mysqli_fetch_array($result,MYSQL_NUM))? $row[0] : "0";
	
	$query="SELECT log_date, log_data_item_id, log_quantity, log_value, log_units_id, log_crop_id, log_treatment_id, log_comments, log_picture, log_sound FROM log WHERE log.plot_id=$id ORDER BY log_date DESC";
	$result = mysqli_query($dbh,$query);
	while($row = mysqli_fetch_array($result,MYSQL_NUM)){
		if($row[8]!="" && $row[9]!=""){
			$this_entry="-;".$row[0].";".$row[8].";".$row[9].";".$row[7];
		} else {
			$data_item_string=getDataItemString($dbh,$row[1],$row[5],$row[6],$row[2],$row[3],$row[4]);
			$this_entry=$row[0].";".$data_item_string.";".$row[7];
		}
		$ret=$ret."*".$this_entry;
	}
	return $ret;
}

function deleteFarmPlots($dbh,$farm_id){
	$query="DELETE FROM crop_x_plot WHERE plot_id IN (SELECT plot_id FROM plot WHERE farm_id=$farm_id)";
	$result = mysqli_query($dbh,$query);
	
	$query="DELETE FROM treatment_ingredient_x_plot WHERE plot_id IN (SELECT plot_id FROM plot WHERE farm_id=$farm_id)";
	$result = mysqli_query($dbh,$query);
	
	$query="DELETE FROM plot WHERE farm_id=$farm_id";
	$result = mysqli_query($dbh,$query);
}

function deleteFarmData($dbh,$farm_id){
	$query="DELETE FROM log WHERE plot_id IN (SELECT plot_id FROM plot WHERE farm_id=$farm_id)";
	$result = mysqli_query($dbh,$query);
	$farm_id=$farm_id*-1;
	$query="DELETE FROM log WHERE plot_id = $farm_id";
	$result = mysqli_query($dbh,$query);
}

function createNewPlot($dbh,$farm_id,$plot_id,$plot_x,$plot_y,$plot_w,$plot_h,$plot_size,$plot_crops,$plot_pest_control,$plot_soil_management){
	$query="INSERT INTO plot(internal_plot_id, farm_id, plot_x, plot_y, plot_w, plot_h, plot_size) VALUES ($plot_id, $farm_id, $plot_x,$plot_y,$plot_w,$plot_h,$plot_size)";
	$result = mysqli_query($dbh,$query);
	$plot_id = mysqli_insert_id($dbh);
	
	
	for($i=0;$i<sizeof($plot_crops);$i++){
		$crop_id = $plot_crops[$i];
		if($crop_id!="-1"){
			$query="INSERT INTO crop_x_plot(plot_id, crop_id) VALUES ($plot_id, $crop_id)";
			$result = mysqli_query($dbh,$query);
		}
	}
	
	for($i=0;$i<sizeof($plot_pest_control);$i++){
		$pest_control_id = $plot_pest_control[$i];
		if($pest_control_id!="-1"){
			$query="INSERT INTO treatment_ingredient_x_plot(plot_id, treatment_ingredient_id) VALUES ($plot_id, $pest_control_id)";
			$result = mysqli_query($dbh,$query);
		}
	}
	
	for($i=0;$i<sizeof($plot_soil_management);$i++){
		$soil_management_id = $plot_soil_management[$i];
		if($soil_management_id!="-1"){
			$query="INSERT INTO treatment_ingredient_x_plot(plot_id, treatment_ingredient_id) VALUES ($plot_id, $soil_management_id)";
			$result = mysqli_query($dbh,$query);
		}
	}
}

function getPlotIDFromFarm($dbh,$farm_id,$internal_plot_id){
	$ret=-1;
	$query="SELECT plot_id FROM plot WHERE farm_id=$farm_id AND internal_plot_id=$internal_plot_id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=$row[0];
	}
	return $ret;
}

function getPlotContents($dbh,$plot_id){
	
	$query="SELECT crop_name FROM crop, crop_x_plot WHERE crop_x_plot.plot_id=$plot_id AND crop.crop_id = crop_x_plot.crop_id";
	$result = mysqli_query($dbh,$query);
	$crops="Empty";
	while($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$crops=($crops=="Empty" ? $row[0] : $crops.", ".$row[0]);
	}
	
	$query="SELECT treatment_ingredient_name FROM treatment_ingredient, treatment_ingredient_x_plot WHERE treatment_ingredient_x_plot.plot_id=$plot_id AND treatment_ingredient.treatment_ingredient_id = treatment_ingredient_x_plot.treatment_ingredient_id AND treatment_id=1";
	$result = mysqli_query($dbh,$query);
	$pest_control="None";
	while($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$pest_control=($pest_control=="None" ? $row[0] : $pest_control.", ".$row[0]);
	}
	
	$query="SELECT treatment_ingredient_name FROM treatment_ingredient, treatment_ingredient_x_plot WHERE treatment_ingredient_x_plot.plot_id=$plot_id AND treatment_ingredient.treatment_ingredient_id = treatment_ingredient_x_plot.treatment_ingredient_id AND treatment_id=2";
	$result = mysqli_query($dbh,$query);
	$soil_management="None";
	while($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$soil_management=($soil_management=="None" ? $row[0] : $soil_management.", ".$row[0]);
	}
	
	$ret="Plot: ".$crops."<br>Pest control: ".$pest_control."<br>Soil management: ".$soil_management;
	return $ret;
}

function getPlotData($dbh,$plot_id,$none_word,$plot_word,$pest_control_word,$soil_management_word,$page){
	$none_word=ucfirst($none_word);
	
	$query="SELECT crop_name, crop.crop_id FROM crop, crop_x_plot WHERE crop_x_plot.plot_id=$plot_id AND crop.crop_id = crop_x_plot.crop_id";
	$result = mysqli_query($dbh,$query);
	$crops=$none_word;
	while($row = mysqli_fetch_array($result,MYSQL_NUM)){
		if($crops==$none_word){
			$crops='<a href="'.$page.'?crop='.$row[1].'">'.$row[0].'</a>';
		} else {
			$crops.=', <a href="'.$page.'?crop='.$row[1].'">'.$row[0].'</a>';
		}
	}
	
	$query="SELECT treatment_ingredient_name, treatment_ingredient.treatment_ingredient_id FROM treatment_ingredient, treatment_ingredient_x_plot WHERE treatment_ingredient_x_plot.plot_id=$plot_id AND treatment_ingredient.treatment_ingredient_id = treatment_ingredient_x_plot.treatment_ingredient_id AND treatment_id=1";
	$result = mysqli_query($dbh,$query);
	$pest_control=$none_word;
	while($row = mysqli_fetch_array($result,MYSQL_NUM)){
		if($pest_control==$none_word){
			$pest_control='<a href="'.$page.'?ingredient='.$row[1].'">'.$row[0].'</a>';
		} else {
			$pest_control.=', <a href="'.$page.'?ingredient='.$row[1].'">'.$row[0].'</a>';
		}
	}
	
	$query="SELECT treatment_ingredient_name, treatment_ingredient.treatment_ingredient_id FROM treatment_ingredient, treatment_ingredient_x_plot WHERE treatment_ingredient_x_plot.plot_id=$plot_id AND treatment_ingredient.treatment_ingredient_id = treatment_ingredient_x_plot.treatment_ingredient_id AND treatment_id=2";
	$result = mysqli_query($dbh,$query);
	$soil_management=$none_word;
	while($row = mysqli_fetch_array($result,MYSQL_NUM)){
		if($soil_management==$none_word){
			$soil_management='<a href="'.$page.'?ingredient='.$row[1].'">'.$row[0].'</a>';
		} else {
			$soil_management.=', <a href="'.$page.'?ingredient='.$row[1].'">'.$row[0].'</a>';
		}
	}
	
	$crops=ucfirst($plot_word).": ".$crops;
	$pest_control=ucfirst($pest_control_word).": ".$pest_control;
	$soil_management=ucfirst($soil_management_word).": ".$soil_management;
	return $crops."<br>".$pest_control."<br>".$soil_management;
}

function getCropNameFromID($dbh,$crop_id){
	$ret="";
	$query="SELECT crop_name FROM crop WHERE crop_id=$crop_id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=$row[0];
	}
	return $ret;
}

function getTreatmentIngredientNameFromID($dbh,$treatment_ingredient_id){
	$ret="";
	$query="SELECT treatment_ingredient_name FROM treatment_ingredient WHERE treatment_ingredient_id=$treatment_ingredient_id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=$row[0];
	}
	return $ret;
}

function getUserNames($dbh,$names_list,$page){
	$ret="";
	for($i=0;$i<sizeof($names_list);$i++){
		$id=$names_list[$i];
		$name=ucfirst(getUserAliasFromID($dbh,$id));
		$ret = ($ret=="" ? '<a class="w3-red" href="'.$page.'?user='.($id*-1).'">'.$name.'</a>' : $ret.= ' <a class="w3-red" href="'.$page.'?user='.($id*-1).'">'.$name.'</a>');
	}
	return $ret;
}

function getFarmNames($dbh,$names_list,$page){
	$ret="";
	for($i=0;$i<sizeof($names_list);$i++){
		$id=$names_list[$i];
		$name=ucfirst(getFarmNameFromID($dbh,$id));
		$ret = ($ret=="" ? '<a class="w3-red" href="'.$page.'?farm='.($id*-1).'">'.$name.'</a>' : $ret.= ' <a class="w3-red" href="'.$page.'?farm='.($id*-1).'">'.$name.'</a>');
	}
	return $ret;
}

function getCropNames($dbh,$names_list,$page){
	$ret="";
	for($i=0;$i<sizeof($names_list);$i++){
		$id=$names_list[$i];
		$name=ucfirst(getCropNameFromID($dbh,$id));
		$ret = ($ret=="" ? '<a class="w3-red" href="'.$page.'?crop='.($id*-1).'">'.$name.'</a>' : $ret.= ' <a class="w3-red" href="'.$page.'?crop='.($id*-1).'">'.$name.'</a>');
	}
	return $ret;
}

function getIngredientNames($dbh,$names_list,$page){
	$ret="";
	for($i=0;$i<sizeof($names_list);$i++){
		$id=$names_list[$i];
		$name=ucfirst(getTreatmentIngredientNameFromID($dbh,$id));
		$ret = ($ret=="" ? '<a class="w3-red" href="'.$page.'?ingredient='.($id*-1).'">'.$name.'</a>' : $ret.= ' <a class="w3-red" href="'.$page.'?ingredient='.($id*-1).'">'.$name.'</a>');
	}
	return $ret;
}

function getPlotsWithCropIngredient($dbh,$crops,$ingredients){
	$array_crops=array();
	$array_ingredients=array();
	$ret=array();
	if(!empty($crops)){
		$query="SELECT plot_id FROM crop_x_plot WHERE crop_id IN(".implode(",",$crops).") GROUP BY plot_id HAVING COUNT(DISTINCT crop_id)=".count($crops);
		$result = mysqli_query($dbh,$query);
		while($row = mysqli_fetch_array($result,MYSQL_NUM)){
			if(!in_array($row[0],$array_crops)){
				array_push($array_crops,$row[0]);
			}
		}
		$ret=$array_crops;
	}
	if(!empty($ingredients)){
		$query="SELECT plot_id FROM treatment_ingredient_x_plot WHERE treatment_ingredient_id IN(".implode(",",$ingredients).") GROUP BY plot_id HAVING COUNT(DISTINCT treatment_ingredient_id)=".count($ingredients);
		$result = mysqli_query($dbh,$query);
		while($row = mysqli_fetch_array($result,MYSQL_NUM)){
			if(!in_array($row[0],$array_ingredients)){
				array_push($array_ingredients,$row[0]);
			}
		}
		$ret=$array_ingredients;
	}
	if(!empty($array_crops) && !empty($array_ingredients)){
		$ret=array();
		foreach($array_crops as $crop){
			if(in_array($crop,$array_ingredients)){
				array_push($ret,$crop);
			}
		}
	} 
	if(empty($ret)){
		return "-1";
	} else {
		return implode(",",$ret);
	}
}

function getAllFarmIDS($dbh,$names_list,$user_id){
	$found_farms=array();
	for($i=0;$i<sizeof($names_list);$i++){
		$id=$names_list[$i];
		$query="SELECT farm_id FROM farm WHERE user_id=$user_id AND farm_app_id=(SELECT farm_app_id FROM farm WHERE farm_id=$id)";
		$result = mysqli_query($dbh,$query);
		while($row = mysqli_fetch_array($result,MYSQL_NUM)){
			if(!in_array($row[0],$found_farms)){
				array_push($found_farms,$row[0]);
			}
		}
	}
	return implode(",",$found_farms);
}

function getDefaultMoneyUnit($dbh){
	$ret="";
	$query="SELECT units_name FROM units WHERE units_type=2";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=$row[0];
	}
	return $ret;
}

function getUnitsNameFromID($dbh,$units_id){
	$ret="";
	$query="SELECT units_name FROM units WHERE units_id=$units_id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret=$row[0];
	}
	return $ret;
}

function getLogDataItemText($dbh,$data_item_id,$quantity,$value,$units_id,$crop_id,$treatment_ingredient_id,$comments){
	$ret="";
	$query="SELECT data_item_name, data_item_type, is_crop_specific, is_treatment_specific FROM data_item WHERE data_item_id=$data_item_id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$data_item_name=($row[2]==1 ? str_replace("Mbegu",getCropNameFromID($dbh,$crop_id),$row[0]) : ($row[3]==1 ? str_replace("Matibabu",getTreatmentIngredientNameFromID($dbh,$treatment_ingredient_id),$row[0]) : $row[0]));
		$item_data=($row[1]==0 || $row[1]==4 ? $value." ".getDefaultMoneyUnit($dbh) : $quantity." ".getUnitsNameFromID($dbh,$units_id)."<br>".$value." ".getDefaultMoneyUnit($dbh));
		$ret=$data_item_name.": ".$item_data;
		$ret.=($comments=="" ? "" : "<br><br>".urldecode($comments));
	}
	return $ret;
}

function getDataItemString($dbh,$data_item_id,$crop_id,$treatment_ingredient_id,$quantity,$value,$units_id){
	$ret="";
	$query="SELECT data_item_name, data_item_type, is_crop_specific, is_treatment_specific FROM data_item WHERE data_item_id=$data_item_id";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$data_item_name=($row[2]==1 ? str_replace("Mbegu",getCropNameFromID($dbh,$crop_id),$row[0]) : ($row[3]==1 ? str_replace("Matibabu",getTreatmentIngredientNameFromID($dbh,$treatment_ingredient_id),$row[0]) : $row[0]));
		$item_data=($row[1]==0 || $row[1]==4 ? ". Cost: ".$value." ".getDefaultMoneyUnit($dbh) : ", ".$quantity." ".getUnitsNameFromID($dbh,$units_id).". Cost: ".$value." ".getDefaultMoneyUnit($dbh));
		$ret=$data_item_name.$item_data;
	}
	return $ret;
}

function getTotalItems($dbh,$query){
	$result = mysqli_query($dbh,$query);
	return mysqli_num_rows($result);
}

function checkRecords($dbh,$ugunduzi_email,$ugunduzi_pass,$data_subject,$multimedia_subject,$mail_server,$servpath,$root_folder,$ffmpeg_path,$sample_rate){
	if ($inbox = imap_open ($mail_server, $ugunduzi_email, $ugunduzi_pass)) {
		$total = imap_num_msg($inbox);
		for($x=1; $x<=$total; $x++) {
			$headers = imap_header($inbox, $x);
			$structure = imap_fetchstructure($inbox, $x);
			$sections = parse($structure);
			if (isset($headers->subject)) {
				$subject = decodeSubject($headers->subject);
			} else {
				$subject = "";
			}
			
			if ($subject==$data_subject && is_array($sections) && sizeof($sections)>0) {
				for($y=0; $y<sizeof($sections); $y++) {	
					$type = $sections[$y]["type"];
					$encoding = $sections[$y]["encoding"];
					$pid = $sections[$y]["pid"];
					$attachment = imap_fetchbody($inbox,$x,$pid);
					if ($type=="text/plain" || $type=="text/html") {
						if ($encoding == "base64") {
							$text = trim(utf8_decode(imap_base64($attachment)));
						} else {
							$text = trim(utf8_decode(decodeISO88591($attachment)));
						}
						
						//remove new lines!!
						$text=str_replace(array("\n\r", "\n", "\r"),'',$text);
						
						$log_items=explode("*",$text);
						
						$cleared_farms=array();
						
						for($i=0;$i<sizeof($log_items);$i++){
							$log_item=str_replace('=','',$log_items[$i]);
							$log_item_parts=explode(";",$log_item);
							
							if(sizeof($log_item_parts)==13 || sizeof($log_item_parts)==12){
								
								$farm_app_id=$log_item_parts[0];
								$farm_version=$log_item_parts[1];
								$user_id=$log_item_parts[2];
								$farm_id=getFarmIDFromFarmAppIdVersionUser($dbh,$farm_app_id,$farm_version,$user_id);
								$plot_id=getPlotIDFromFarm($dbh,$farm_id,$log_item_parts[3]);
								if($plot_id==-1){
									$plot_id=$farm_id*-1;
								}
								$date=$log_item_parts[4];
								$data_item_id=$log_item_parts[5];
								$value=number_format($log_item_parts[6],2,'.','');
								$quantity=number_format($log_item_parts[7],2,'.','');
								$units_id=$log_item_parts[8];
								$crop_id=$log_item_parts[9];
								$treatment_id=$log_item_parts[10];
								$cost=$log_item_parts[11];
								$comments=(sizeof($log_item_parts)==13 ? trim($log_item_parts[12]) : "");
								
								if(!in_array($farm_id,$cleared_farms)){
									$query="DELETE FROM log WHERE plot_id IN (SELECT plot_id FROM plot WHERE farm_id=$farm_id) AND log_data_item_id>0";
									$result = mysqli_query($dbh,$query);
									$entire_farm=$farm_id*-1;
									$query="DELETE FROM log WHERE plot_id=$entire_farm AND log_data_item_id>0";
									$result = mysqli_query($dbh,$query);
									array_push($cleared_farms,$farm_id);
								}
							
								$query="INSERT INTO log (plot_id, log_date, log_data_item_id, log_value, log_quantity, log_units_id, log_crop_id, log_treatment_id, log_comments) VALUES ($plot_id, '$date', $data_item_id, $value, $quantity, $units_id, $crop_id, $treatment_id, '$comments')";
								
								$result = mysqli_query($dbh,$query);
								
							}
						}	
					}
				}
				$sections=NULL;
			} else if ($subject==$multimedia_subject && is_array($sections) && sizeof($sections)>0) {
				for($y=0; $y<sizeof($sections); $y++) {
					$type = $sections[$y]["type"];
					$encoding = $sections[$y]["encoding"];
					$pid = $sections[$y]["pid"];
					$attachment = imap_fetchbody($inbox,$x,$pid);
					if (strpos($sections[$y]["name"],".jpg") > 0 && $type=="image/jpeg") {
						$folder = "/img/img";
						$extension = ".jpg";
					} else if (strpos($sections[$y]["name"],".amr") > 0 && $type=="application/octet-stream") {
						$folder = "/snd/snd";
						$extension = ".amr";
					} else if ($type=="text/plain" || $type=="text/html") {
						if ($encoding == "base64") {
							$text = trim(utf8_decode(imap_base64($attachment)));
						} else {
							$text = trim(utf8_decode(decodeISO88591($attachment)));
						}
						$log_item_parts=explode(";",$text);
						
						$farm_app_id=$log_item_parts[0];
						$farm_version=$log_item_parts[1];
						$user_id=$log_item_parts[2];
						$plot_app_id=$log_item_parts[3];
						
						$farm_id=getFarmIDFromFarmAppIdVersionUser($dbh,$farm_app_id,$farm_version,$user_id);
						
						if($plot_app_id==-1){
							$plot_id=$farm_id*-1;
						} else {
							$plot_id=getPlotIDFromFarm($dbh,$farm_id,$plot_app_id);
						}
						$date=$log_item_parts[4];
						
						$extension="";
					}
					if($extension==".jpg" || $extension==".amr"){
						$index=getMaxFileIndex($dbh);
						$file = $folder.$index.$extension;
						$handle = fopen("content/".$file, "wb");
						$ok = fwrite($handle, imap_base64($attachment));
						fclose($handle);
						if($extension==".amr"){
							$file_dest=$folder.$index.".mp3";
							convertAMRToMP3($file,$file_dest,$servpath,$ffmpeg_path,$root_folder,$sample_rate);
							$file_sound=$file_dest;
						} else {
							$file_picture=$file;
						}
					}
				}
				$query="INSERT INTO log (plot_id, log_date, log_picture, log_sound) VALUES ($plot_id, '$date', '$file_picture', '$file_sound')";
				$result = mysqli_query($dbh,$query);
			}
			imap_delete($inbox,$x);
		}
		imap_close($inbox, CL_EXPUNGE);
	}
}

function parse($structure) {
	$type = array("text", "multipart", "message", "application", "audio", "image", "video", "other");
	$encoding = array("7bit", "8bit", "binary", "base64", "quoted-printable", "other");
	$ret = array();
	$parts = $structure->parts;
	for($x=0; $x<sizeof($parts); $x++) {
		$ret[$x]["pid"] = ($x+1);	
		$this_part = $parts[$x];
		if ($this_part->type == "") { $this_part->type = 0; }
		$ret[$x]["type"] = $type[$this_part->type] . "/" . strtolower($this_part->subtype);	
		if ($this_part->encoding == "") { $this_part->encoding = 0; }
		$ret[$x]["encoding"] = $encoding[$this_part->encoding];	
		$ret[$x]["size"] = strtolower($this_part->bytes);	
		if ($this_part->ifdisposition) {
			$ret[$x]["disposition"] = strtolower($this_part->disposition);	
			if (strtolower($this_part->disposition) == "attachment" || strtolower($this_part->disposition) == "inline") {
				$params = $this_part->dparameters;
				if (is_null($params)) {
					$params = $this_part->parameters;
				}
				if (!is_null($params)) {
					foreach ($params as $p) {
						if($p->attribute == "FILENAME" || $p->attribute == "NAME") {
							$ret[$x]["name"] = $p->value;	
							break;			
						}
					}
				}
			}
		} 
	}
	return $ret;
}

function decodeSubject($s) {
	$ret=$s;
	$elements=imap_mime_header_decode($s);
	if (sizeof($elements)>0) {
		if($elements[0]->charset=="utf-8") {
			$ret=utf8_decode($elements[0]->text);
		} else if ($elements[0]->charset="ISO-8859-1") {
			$ret=decodeISO88591($elements[0]->text);
		}
	}
	return $ret;
}

function decodeISO88591($string) {               
	$string=str_replace("=?iso-8859-1?q?","",$string);
  	$string=str_replace("=?iso-8859-1?Q?","",$string);
  	$string=str_replace("?=","",$string);

  	$charHex=array("0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F");
       
	for($z=0;$z<sizeof($charHex);$z++) {
		for($i=0;$i<sizeof($charHex);$i++) {
      		$string=str_replace(("=".($charHex[$z].$charHex[$i])),chr(hexdec($charHex[$z].$charHex[$i])),$string);
    	}
  	}
  	return($string);
}

function getMaxFileIndex($dbh) {
	$ret=0;
	$query = "SELECT MAX(log_id) FROM log";
	$result = mysqli_query($dbh,$query);
	if($row = mysqli_fetch_array($result,MYSQL_NUM)){
		$ret = $row[0]+1;			
	} 
	return $ret;
}

function ConvertAMRToMP3($file1, $file2, $servpath, $ffmpeg_path, $root_folder, $sample_rate) {
	exec($ffmpeg_path."ffmpeg -i ".$servpath."/".$root_folder."/content/".$file1." -ar ".$sample_rate." ".$servpath.$root_folder."/content/".$file2,$output);
}

?>