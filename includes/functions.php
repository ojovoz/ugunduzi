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

function getUserPassFromId($dbh,$id){
	$ret="-1";
	$query="SELECT user_password FROM user WHERE user_id=$id";
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
}

function createNewPlot($dbh,$farm_id,$plot_id,$plot_x,$plot_y,$plot_w,$plot_h,$plot_crops,$plot_pest_control,$plot_soil_management){
	$query="INSERT INTO plot(internal_plot_id, farm_id, plot_x, plot_y, plot_w, plot_h) VALUES ($plot_id, $farm_id, $plot_x,$plot_y,$plot_w,$plot_h)";
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
						
						for($i=0;$i<sizeof($log_items);$i++){
							$log_item=str_replace('=','',$log_items[$i]);
							$log_item_parts=explode(";",$log_item);
							if(sizeof($log_item_parts)==13){
								
								$farm_app_id=$log_item_parts[0];
								$farm_version=$log_item_parts[1];
								$user_id=$log_item_parts[2];
								$farm_id=getFarmIDFromFarmAppIdVersionUser($dbh,$farm_app_id,$farm_version,$user_id);
								$plot_id=getPlotIDFromFarm($dbh,$farm_id,$log_item_parts[3]);
								$date=$log_item_parts[4];
								$data_item_id=$log_item_parts[5];
								$value=number_format($log_item_parts[6],2,'.','');
								$quantity=number_format($log_item_parts[7],2,'.','');
								$units_id=$log_item_parts[8];
								$crop_id=$log_item_parts[9];
								$treatment_id=$log_item_parts[10];
								$cost=$log_item_parts[11];
								$comments=$log_item_parts[12];
								
								if($i==0){
									$query="DELETE FROM log WHERE plot_id IN (SELECT plot_id FROM plot WHERE farm_id=$farm_id) AND log_data_item_id>0";
									$result = mysqli_query($dbh,$query);
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
						$date=$log_item_parts[4];
						$farm_id=getFarmIDFromFarmAppIdVersionUser($dbh,$farm_app_id,$farm_version,$user_id);
						$plot_id=getPlotIDFromFarm($dbh,$farm_id,$plot_app_id);
						
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
	//echo($ffmpeg_path."ffmpeg -i ".$servpath.$root_folder."/content".$file1." -ar ".$sample_rate." ".$servpath.$root_folder."/content".$file2);
}
?>



