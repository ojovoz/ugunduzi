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

function createNewFarm($dbh,$farm_name,$farm_size,$farm_date_created,$user_id,$parent_id){
	$query="INSERT INTO farm (user_id, farm_name, farm_size_acres, farm_date_created, parent_farm_id) VALUES($user_id, '$farm_name', $farm_size, '$farm_date_created', $parent_id)";
	$result = mysqli_query($dbh,$query);
	$id = mysqli_insert_id($dbh);
	return $id;
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
	$query="DELETE FROM plot WHERE farm_id=$farm_id";
	$result = mysqli_query($dbh,$query);
}

function deleteFarmData($dbh,$farm_id){
	$query="DELETE FROM log WHERE plot_id IN (SELECT plot_id FROM plot WHERE farm_id=$farm_id)";
	$result = mysqli_query($dbh,$query);
}

function createNewPlot($dbh,$farm_id,$plot_id,$plot_x,$plot_y,$plot_w,$plot_h,$plot_c1,$plot_c2,$plot_t1,$plot_t2){
	$query="INSERT INTO plot(internal_plot_id, farm_id, plot_x, plot_y, plot_w, plot_h, plot_crop1, plot_crop2, plot_treatment1, plot_treatment2) VALUES ($plot_id, $farm_id, $plot_x,$plot_y,$plot_w,$plot_h,$plot_c1,$plot_c2,$plot_t1,$plot_t2)";
	$result = mysqli_query($dbh,$query);
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
							if(sizeof($log_item_parts)==9){
								
								$farm_name=$log_item_parts[0];
								$user_id=$log_item_parts[1];
								$farm_id=getFarmIDFromNameUser($dbh,$farm_name,$user_id);
								$plot_id=getPlotIDFromFarm($dbh,$farm_id,$log_item_parts[2]);
								$date=$log_item_parts[3];
								$data_item_id=$log_item_parts[4];
								$value=number_format($log_item_parts[5],2,'.','');
								$units_id=$log_item_parts[6];
								$crop_id=$log_item_parts[7];
								$treatment_id=$log_item_parts[8];
								
								if($i==0){
									$query="DELETE FROM log WHERE plot_id IN (SELECT plot_id FROM plot WHERE farm_id=$farm_id) AND log_data_item_id>0";
									$result = mysqli_query($dbh,$query);
								}
							
								$query="INSERT INTO log (plot_id, log_date, log_data_item_id, log_value, log_units_id, log_crop_id, log_treatment_id) VALUES ($plot_id, '$date', $data_item_id, $value, $units_id, $crop_id, $treatment_id)";
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
						
						$farm_name=$log_item_parts[0];
						$user_id=$log_item_parts[1];
						$farm_id=getFarmIDFromNameUser($dbh,$farm_name,$user_id);
						$plot_id=getPlotIDFromFarm($dbh,$farm_id,$log_item_parts[2]);
						$date=$log_item_parts[3];
						
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



