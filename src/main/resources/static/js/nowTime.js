function time(){
    var date = new Date();
    var s = date.getFullYear().toString();  //  年
    var s1 = (date.getMonth()+1).toString(); // 月（0-11）
    var s2 = date.getDate().toString();  // 日（0-31）
    var s3 = date.getHours().toString(); // 时（0-23）
    var s4 = date.getMinutes().toString(); // 分（0-59）
    var s5 = date.getSeconds().toString(); // 秒（0-59）
    s6=s+"-"+s1+"-"+s2+" "+s3+":"+s4+":"+s5;
    return s6;
}
