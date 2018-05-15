<?php
include_once "includes/vars.php";
include_once "includes/functions.php";
include_once "includes/init_database.php";
$dbh = initDB();

checkRecords($dbh,$ugunduzi_email,$ugunduzi_pass,$data_subject,$multimedia_subject,$mail_server,$servpath,$root_folder,$ffmpeg_path,$sample_rate);

?>