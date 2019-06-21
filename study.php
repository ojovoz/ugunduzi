<?php
header("Cache-Control: no-cache, must-revalidate");
session_start();
include_once "includes/init_database.php";
include_once "includes/functions.php";
include_once "includes/vars.php";
$dbh = initDB();

if(isset($_SESSION['admin'])){
	if($_SESSION['admin']){

	

?>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="css/w3.css">
<link rel="stylesheet" href="css/green_theme.css">
<script src="includes/audio.min.js"></script>
<title>Ugunduzi - Study</title>
<script>

var farm_versions = [];
var current_version = 0;
var this_farm_id = -1;
var this_data = [];
var data_index = 0;
var total_data = 0;
var plot_contents = "";
var max_items_per_page = <?php echo($max_items_per_page); ?>;
var data_header = "";

audiojs.events.ready(function() {
	var as = audiojs.createAll();
});

function getUserFarms(){
	var u = document.getElementById("u");
	var id = u.options[u.selectedIndex].value; 
	
	if(id!=""){
		
		var farm_date = document.getElementById("farm_date");
		farm_date.innerHTML = "";
		
		var plot_grid = document.getElementById("farm_grid");
		plot_grid.style.display="none";
		
		var data_container = document.getElementById("data_container");
		data_container.innerHTML="";
		
		data_index=0;
	
		var xmlhttp = new XMLHttpRequest();

		xmlhttp.open("GET", "includes/get_user_farms.php?id=" + id);
		xmlhttp.send();
	
		xmlhttp.onreadystatechange = function() {
			if (this.readyState === 4 && this.status === 200) {
				
				var farms = this.responseText;
				var farms_array = farms.split(";");
				if(farms_array.length==0 || farms==""){
					var farm_placeholder = document.getElementById("farm_name");
					farm_placeholder.innerHTML = "User has no farms";
					
					var farm_chooser = document.getElementById("farm_chooser");
					farm_chooser.style.display="none";
					
				} else if(farms_array.length==1){
					var this_farm = farms_array[0];
					var this_farm_parts = this_farm.split(",");
					var farm_id = this_farm_parts[0];
					var farm_name = this_farm_parts[1];
					var farm_size = this_farm_parts[2];
				
					var farm_placeholder = document.getElementById("farm_name");
					farm_placeholder.innerHTML = farm_name;
					
					var farm_chooser = document.getElementById("farm_chooser");
					farm_chooser.style.display="none";
					
					getFarmPlots(farm_id);
				} else {
					var farm_placeholder = document.getElementById("farm_name");
					farm_placeholder.innerHTML = "";
					
					var farm_chooser = document.getElementById("farm_chooser");
					farm_chooser.style.display="block";
					
					var farm_select = document.getElementById("farm");
					
					while(farm_select.hasChildNodes()){
						farm_select.removeChild(farm_select.firstChild);
					}
					
					var option = document.createElement("option");
					option.setAttribute("value","");
					option.setAttribute("selected",true);
					option.setAttribute("disabled",true);
					option.appendChild(document.createTextNode("Select farm"));
					farm_select.appendChild(option);
					
					for(var i=0;i<farms_array.length;i++){
						var this_farm_parts = farms_array[i].split(",");
						var farm_id = this_farm_parts[0];
						var farm_name = this_farm_parts[1];
						var farm_size = this_farm_parts[2];
						
						var option = document.createElement("option");
						option.setAttribute("value",farm_id);
						option.appendChild(document.createTextNode(farm_name));
						farm_select.appendChild(option);
					}
				}
			}
		};
	}
}

function getFarmPlots(id){
	
	if(id==-1){
		var farm = document.getElementById("farm");
		var id = farm.options[farm.selectedIndex].value;
	}
	
	if(id!=""){
	
		var xmlhttp = new XMLHttpRequest();

		xmlhttp.open("GET", "includes/get_farm_plots.php?id=" + id);
		xmlhttp.send();
		xmlhttp.onreadystatechange = function() {
			if (this.readyState === 4 && this.status === 200) {
				plots = this.responseText;
				if(plots==""){
					var plot_grid = document.getElementById("farm_grid");
					plot_grid.style.display="none";
				} else {
					farm_versions = plots.split("*");
					current_version = farm_versions.length-1;
					
					drawCurrentFarm();
					
				}
			}
		};
		
	}
	
}

function drawCurrentFarm(){
	
	var this_farm = farm_versions[current_version];
	
	data_index=0;
	
	var plot_grid = document.getElementById("farm_grid");
	plot_grid.style.display="grid";
	
	while(plot_grid.hasChildNodes()){
		plot_grid.removeChild(plot_grid.firstChild);
	}
	
	var plots = this_farm.split("|");
	
	for(var i=0;i<plots.length;i++){
		
		var this_plot = plots[i].split(";");
		var this_plot_id = this_plot[0];
		var this_plot_x = this_plot[1];
		var this_plot_y = this_plot[2];
		var this_plot_w = this_plot[3];
		var this_plot_h = this_plot[4];
		var this_plot_size = this_plot[5];
		var this_plot_crops = this_plot[6];
		var this_plot_pest_control = this_plot[7];
		var this_plot_soil_management = this_plot[8];
		var this_farm_date = this_plot[9];
		var this_farm_size = this_plot[10];
		this_farm_id = this_plot[11];
		
		var div = document.createElement("div");
		div.className="grid-item";
		
		var crops = this_plot_crops.split(",");
		var crop_names="";
		for(var j=0;j<crops.length;j++){
			crop_names = (crop_names == "")? crops[j] : crop_names+"<br>"+crops[j];
		}
		crop_names = (crop_names=="")? "Empty" : crop_names;
		crop_names = '<a href="#" onclick="getPlotData('+this_plot_id+','+data_index+',\''+this_plot_crops+'\',\''+this_plot_pest_control+'\',\''+this_plot_soil_management+'\','+this_plot_size+')">'+crop_names+'</a>';
		div.innerHTML = crop_names;
		
		var bgcolor;
		if(this_plot_pest_control=="" && this_plot_soil_management==""){
			bgcolor="#BBBBBB";
		} else if(this_plot_pest_control!="" && this_plot_soil_management==""){
			bgcolor="#2196F3";
		} else if(this_plot_pest_control=="" && this_plot_soil_management!=""){
			bgcolor="#FFC107";
		} else if(this_plot_pest_control!="" && this_plot_soil_management!=""){
			bgcolor="#4CAF50";
		}
		
		var cssString="grid-area: "+(parseInt(this_plot_y)+1).toString()+" / "+(parseInt(this_plot_x)+1).toString()+" / span "+this_plot_h+" / span "+this_plot_w+"; background-color: "+bgcolor;
		
		div.style.cssText=cssString;
		
		plot_grid.appendChild(div);
	}
	
	var navigation = document.getElementById("navigation");
	
	if(farm_versions.length>1){
		navigation.style.display="block";
		
		var prev = document.getElementById("prev");
		var next = document.getElementById("next");
		
		prev.style.display = (current_version>0)? "inline" : "none";
		next.style.display = (current_version<(farm_versions.length-1))? "inline" : "none";
		
	} else {
		navigation.style.display="none";
	}
	
	var farm_date = document.getElementById("farm_date");
	farm_date.style.display = "inline";
	farm_date.innerHTML = '<strong>Farm created on:</strong> '+this_farm_date+', <strong>Size:</strong> '+this_farm_size+' acres. <a href="#" onclick="getFarmData('+this_farm_id+','+data_index+');">See all farm data</a>';
	
	var data_container = document.getElementById("data_container");
	data_container.innerHTML="Choose a plot or entire farm to see data";
	
}

function goToPrevFarm(){
	current_version--;
	drawCurrentFarm();
}

function goToNextFarm(){
	current_version++;
	drawCurrentFarm();
}

function getFarmData(id,index){
	if(id>0){
		
		var xmlhttp = new XMLHttpRequest();

		xmlhttp.open("GET", "includes/get_farm_data.php?id=" + id + "&from=" + index);
		xmlhttp.send();
		xmlhttp.onreadystatechange = function() {
			if (this.readyState === 4 && this.status === 200) {
				var response_data = this.responseText;
				var data_container = document.getElementById("data_container");
				if(response_data==""){
					data_container.innerHTML="No data";
				} else {
					this_data = response_data.split("*");
					total_data = parseInt(this_data[0]);
					data_header="<strong>Entire farm</strong><br><hr>";
					displayData();
					
				}
			}
		};
	}
}

function getPlotData(id,index,crops,pest_control,soil_management,size){
	
	if(id>0){
		
		if(crops==""){
			plot_contents = "<strong>Plot:</strong> Empty ("+size.toString()+" acres). ";
		} else {
			var plot_crops="";
			var crop_list=crops.split(",");
			for(var i=0;i<crop_list.length;i++){
				plot_crops = (plot_crops=="")? crop_list[i] : plot_crops+", "+crop_list[i];
			}
			plot_contents = "<strong>Plot:</strong> "+plot_crops+" ("+size.toString()+" acres). ";
		}
		
		if(pest_control==""){
			plot_contents += "<strong>Pest control:</strong> None. ";
		} else {
			var plot_pc="";
			var pc_list=pest_control.split(",");
			for(var i=0;i<pc_list.length;i++){
				plot_pc = (plot_pc=="")? pc_list[i] : plot_pc+", "+pc_list[i];
			}
			plot_contents += "<strong>Pest control:</strong> "+plot_pc+". ";
		}
		
		if(soil_management==""){
			plot_contents += "<strong>Soil management:</strong> None.";
		} else {
			var plot_sm="";
			var sm_list=soil_management.split(",");
			for(var i=0;i<sm_list.length;i++){
				plot_sm = (plot_sm=="")? sm_list[i] : plot_sm+", "+sm_list[i];
			}
			plot_contents += "<strong>Soil management:</strong> "+plot_sm+".";
		}
		
		var xmlhttp = new XMLHttpRequest();

		xmlhttp.open("GET", "includes/get_plot_data.php?id=" + id + "&from=" + index);
		xmlhttp.send();
		xmlhttp.onreadystatechange = function() {
			if (this.readyState === 4 && this.status === 200) {
				var response_data = this.responseText;
				if(response_data==""){
					var data_container = document.getElementById("data_container");
					data_container.innerHTML="No data";
				} else {
					this_data = response_data.split("*");
					total_data = parseInt(this_data[0]);
					data_header=plot_contents+"<br><hr>";
					displayData();
					
				}
			}
		};
	}
}

function displayData(){
	
	msg=data_header;
	
	var data_container = document.getElementById("data_container");
	
	data_html="";
	
	var start=data_index+1;
	var end=((start+max_items_per_page)>this_data.length)? this_data.length : start+max_items_per_page;
	
	for(var i=start;i<end;i++){
		this_data_parts=this_data[i].split(";");
		if(this_data_parts.length==3){
			this_data_html="Date: "+this_data_parts[0]+"<br><br>"+this_data_parts[1]+"<br>";
			this_data_html=(this_data_parts[2]=="")? this_data_html : this_data_html+"Comments: "+decodeURIComponent(this_data_parts[2].replace(/\+/g, ' '))+"<br>";
			data_html=(data_html=="")? this_data_html : data_html+"<hr>"+this_data_html;
		} else if(this_data_parts.length==4){
			this_data_html="Date: "+this_data_parts[0]+"<br>"+this_data_parts[1]+"<br><br>"+this_data_parts[2]+"<br>";
			this_data_html=(this_data_parts[3]=="")? this_data_html : this_data_html+"Comments: "+decodeURIComponent(this_data_parts[3].replace(/\+/g, ' '))+"<br>";
			data_html=(data_html=="")? this_data_html : data_html+"<hr>"+this_data_html;
		} else if(this_data_parts.length==5){
			if(this_data_parts[0]=="-"){
				this_data_html="Date: "+this_data_parts[1]+"<br><br>";
				this_data_html+='<img style="width:100%;" src="./content'+this_data_parts[2]+'"><br>';
				this_data_html+='<audio src="./content'+this_data_parts[3]+'" preload="none"></audio><br>';
				this_data_html=(this_data_parts[4]=="")? this_data_html : this_data_html+"Comments: "+decodeURIComponent(this_data_parts[4].replace(/\+/g, ' '))+"<br>";
			} else {
				this_data_html="Date: "+this_data_parts[0]+"<br>"+this_data_parts[1]+"<br><br>";
				this_data_html+='<img style="width:100%;" src="./content'+this_data_parts[2]+'"><br>';
				this_data_html+='<audio src="./content'+this_data_parts[3]+'" preload="none"></audio><br>';
				this_data_html=(this_data_parts[4]=="")? this_data_html : this_data_html+"Comments: "+decodeURIComponent(this_data_parts[4].replace(/\+/g, ' '))+"<br>";
			}
			data_html=(data_html=="")? this_data_html : data_html+"<hr>"+this_data_html;
		}
	}
	
	data_html=data_html+"<br><br>";

	var navigation="";
	if(data_index>0){
		navigation='<a href="#" onclick="goToPrevPage();" style="text-decoration:none;" class="w3-theme-d2 w3-hover-theme w3-button"><<</a>&nbsp;';
	}
	if((data_index+max_items_per_page+1)<this_data.length){
		navigation=navigation+'<a href="#" onclick="goToNextPage();" style="text-decoration:none;" class="w3-theme-d2 w3-hover-theme w3-button">>></a>';
	}
	navigation=navigation+"<br><br>";
	
	data_container.innerHTML=msg+data_html+navigation;
	var as = audiojs.createAll();
	data_container.scrollTop=0;
}

function goToNextPage(){
	data_index=data_index+max_items_per_page;
	displayData();
}

function goToPrevPage(){
	data_index=data_index-max_items_per_page;
	displayData();
}

</script>
<style>
.main {
	padding: 16px;
	margin-top: 0px;
}

.grid-container {
  display: grid;
  grid-template-columns: 150px 150px 150px 150px;
  grid-template-rows: 108px 108px 108px 108px;
  background-color: #ffffff;
  padding: 5px;
  grid-gap: 2px;
}

.grid-item {
  border: 0px;
  padding: 1px;
  font-size: 14px;
  text-align: center;
}

.audiojs .scrubber {
    background: none repeat scroll 0 0 #5A5A5A;
    border-bottom: 0 none;
    border-left: 0 none;
    border-top: 1px solid #3F3F3F;
    float: left;
    height: 14px;
    margin: 10px;
    overflow: hidden;
    position: relative;
    width: 50%; /* smaller width */
}

.audiojs .time {
    border-left: 1px solid #000000;
    color: #DDDDDD;
    float: left;
    height: 36px;
    line-height: 36px;
    margin: 0; /* no margin */
    padding: 0 6px 0 9px; /* 2px smaller left padding */
    text-shadow: 1px 1px 0 rgba(0, 0, 0, 0.5);
}

.audiojs {
    font-family: monospace;
    font-size: 10px; /* reduced font size */
	width:100%; 
	/* max-width:600px; */
	background: #15420b; 
}
</style>
</head>
<body class="w3-theme-l4">
<div style="width:48%; max-width:800px; display:block; margin-left:10px; margin-right:auto;">
<div class="main"><p>
<div class="w3-container w3-card-4 w3-white w3-padding-medium w3-text-black">
<strong>Ugunduzi - Study</strong><br><br>
 <select class="w3-select w3-text-black w3-border" id="u" onchange="getUserFarms();">
    <option value="" disabled selected>Choose a user</option>
	<?php
	$query="SELECT user_name, user_id FROM user WHERE user_name<>'' ORDER BY user_name";
	$result = mysqli_query($dbh,$query);
	while($row=mysqli_fetch_array($result,MYSQL_NUM)){
		echo('<option value="'.$row[1].'">'.$row[0].'</option>');
	}
	?>
  </select>
  <br><br>
  <div style="display:none;" id="farm_chooser">
	<select class="w3-select w3-text-black w3-border" id="farm" onchange="getFarmPlots(-1);">
	</select>
  </div>
  <span id="farm_name"></span><br><br>
  <div class="grid-container" id="farm_grid" style="display:none; width:630px; margin-left:auto; margin-right:auto;">
  </div><br>
  <div id="navigation" style="display:none;">
  <span id="prev" style="display:none;"><a href="#" onclick="goToPrevFarm();" style="text-decoration:none;" class="w3-theme-d2 w3-hover-theme w3-button"><<</a></span>
  &nbsp;
  <span id="next" style="display:none;"><a href="#" onclick="goToNextFarm();" style="text-decoration:none;" class="w3-theme-d2 w3-hover-theme w3-button">>></a></span>
  </div>
  <span id="farm_date" style="display:none; align:right;"></span>
</div> 
</p></div>
</div>
<div class="w3-container w3-card-4 w3-white w3-padding-medium w3-text-black" style="width:48%; max-width:800px; display:inline; position:fixed; left:50%; top:32px; margin-right:10px; height:701px; overflow:auto;" id="data_container"></div>
</div>
</body>
</html>

<?php
	}
}
?>