<?php
$json = file_get_contents('php://input');
$string = json_decode($json);
$num = $string->num;
$uuid = $string->uuid;
$data;
if(!file_exists('./pnum.txt')){
    file_put_contents('./pnum.txt', "0");
    error_log(file_get_contents('./pnum.txt'));
}
$num = file_get_contents('./pnum.txt');
$num = $num + 1;
error_log($num);
file_put_contents('./pnum.txt', $num);


try{
$url = parse_url(getenv('DATABASE_URL'));
$dsn = sprintf('pgsql:host=%s;dbname=%s', $url['host'], substr($url['path'], 1));
$pdo = new PDO($dsn, $url['user'], $url['pass']);

$sql = 'select * from list_db where uuid = :uuid';
$db_data = $pdo->prepare($sql);
$db_data->execute(array(':uuid'=>$uuid));
$result = $db_data->fetch();
if($num <= 5){
$data = $result['url1'];
}else if(5 < $num && $num <= 10){
$data = $result['url2'];
}else if(10 < $num){
$data = $result['url3'];
}

echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);

}catch (PDOException $e){
    print('Error:'.$e->getMessage());
    error_log('error');
    die();
}

$pdo = null;

?>
