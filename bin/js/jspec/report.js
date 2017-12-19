// レポート情報出力処理.
(function(global) {

// レポート出力用オブジェクト.
var ReportOutput = Java.type(
    "origin.test.ReportOutput");
var ro = null;

// レポート出力オブジェクト.
var o = {};

o.open = function(name) {
    o.close();
    ro = new ReportOutput(name);
}
o.close = function() {
    if(ro != null) {
        ro.close();
        ro = null;
    }
}
o.print = function(n) {
    ro.print(n);
    console.log(n);
}
o.println = function(n) {
    ro.println(n);
    console.log(n);
}

return o;
})(this);
