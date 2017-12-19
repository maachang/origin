////////////////////////////////////////////////////////////////////////////////
// RSpecのようなテスト環境をjavascriptで実装.
////////////////////////////////////////////////////////////////////////////////
(function(global) {

// 出力定義.
if(typeof(global["_output"]) != "function") {
    var _output = function(n) {
        console.log(n);
    }
} else {
    var _output = global["_output"];
}

// データ削除.
global["_$report"] = {};

// 全データを入れ子で管理.
var manager = {};

var subjectList = [];
var letList = [];

// リセット.
var reset = function() {
    manager = {};
    subjectList = [];
    letList = [];
}

// マネージャに条件を１つセット.
var addMan = function(name,value) {
    var n = manager[name];
    if(n == undefined || n == null) {
        n = [];
        manager[name] = n;
    }
    n[n.length] = value;
}

// マネージャの条件を更新.
var setMan = function(name,value) {
    var n = manager[name];
    if(n != undefined && n != null && n.length > 0) {
        n[n.length-1] = value;
    }
}

// マネージャから条件を１つクリア.
var removeMan = function(name) {
    var n = manager[name];
    if(n != undefined && n != null && n.length > 0) {
        n.splice(n.length-1,1);
    }
}

// マネージャから現在の条件を取得.
var getMan = function(name) {
    var n = manager[name];
    if(n != undefined && n != null && n.length > 0) {
        return n[n.length-1];
    }
    return null;
}

// 一番最後に定義されたsubjectを取得.
var callSubject = function() {
    var ret;
    var len = subjectList.length;
    for(var i = len-1; i >= 0; i--) {
        if((ret = subjectList[i]) != undefined && ret != null) {
            return ret;
        }
    }
    return null;
}

// 差し替え条件を取得.
var _ = function(name) {
    var map;
    var ret;
    var len = letList.length;
    for(var i = len-1; i >= 0; i--) {
        if((map = letList[i]) != undefined && map != null) {
            ret = map[name];
            if(ret != undefined && ret != null) {
                return ret();
            }
        }
    }
    return function(){return null;}();
}

// expactとsubjectの処理呼び出しをマージして実行.
var margeCall = function(call,subject) {
    var func,a,b;
    if(typeof(subject) != "function" && typeof(call) != "function") {
        return null;
    }
    if(typeof(subject) != "function") {
        a = ""+call;
        func = a.substring(a.indexOf("{")+1,a.length-1)
    } else if(typeof(call) != "function") {
        b = ""+subject;
        func = b.substring(b.indexOf("{")+1,b.length-1);
    } else {
        
        a = ""+call;
        b = ""+subject;
        func = b.substring(b.indexOf("{")+1,b.length-1) + "\n" +
            a.substring(a.indexOf("{")+1,a.length-1)
    }
    var ret = null;
    try {
        ret = eval(func);
    } catch(e) {
        ret = e;
    }
    return ret;
}

// エラーセット.
var addError = function() {
    var n = getMan("describeError");
    if(n != null) setMan("describeError",n+1);
    n = getMan("contextError");
    if(n != null) setMan("contextError",n+1);
    n = getMan("exampleError");
    if(n != null) setMan("exampleError",n+1);
    n = getMan("itError");
    if(n != null) setMan("itError",n+1);
}

// 処理件数セット.
var addCount = function() {
    var n = getMan("describeCount");
    if(n != null) setMan("describeCount",n+1);
    n = getMan("contextCount");
    if(n != null) setMan("contextCount",n+1);
    n = getMan("exampleCount");
    if(n != null) setMan("exampleCount",n+1);
    n = getMan("itCount");
    if(n != null) setMan("itCount",n+1);
}

// テストの対象が何かを記述する.
var describe = function(text,call) {
    addMan("describeText",text);
    addMan("describeCount",0);
    addMan("describeError",0);
    
    _output("describe: " + text + ": start");
    
    subjectList[subjectList.length] = null;
    letList[letList.length] = null;
    
    call();
    
    subjectList.splice(subjectList.length-1,1) ;
    letList.splice(letList.length-1,1) ;
    
    var describeCount = getMan("describeCount");
    var describeError = getMan("describeError");
    var ret = {text:text,all:describeCount,error:describeError};
    
    _output("describe: " + text + ": success:" + (describeCount-describeError));
    _output("describe: " + text + ": error  :" + describeError);
    
    removeMan("describeText");
    removeMan("describeCount");
    removeMan("describeError");
    
    // １つのテスト処理結果をグローバル展開.
    global["_$report"] = ret;
    return ret;
}

// 特定の条件が何かを記述する.
var context = function(text,call) {
    addMan("contextText",text);
    addMan("contextCount",0);
    addMan("contextError",0);
    
    _output("  context: " + text + ": start");
    
    subjectList[subjectList.length] = null;
    letList[letList.length] = null;
    
    call();
    
    subjectList.splice(subjectList.length-1,1) ;
    letList.splice(letList.length-1,1) ;
    
    var contextCount = getMan("contextCount");
    var contextError = getMan("contextError");
    
    _output("  context: " + text + ": success:" + (contextCount-contextError));
    _output("  context: " + text + ": error  :" + contextError);
    
    removeMan("contextText");
    removeMan("contextCount");
    removeMan("contextError");
}

// アウトプットが何かを記述する.
var example = function(text,call) {
    addMan("exampleText",text);
    addMan("exampleCount",0);
    addMan("exampleError",0);
    
    _output("    example: " + text + ": start");
    
    subjectList[subjectList.length] = null;
    letList[letList.length] = null;
    
    call();
    
    subjectList.splice(subjectList.length-1,1) ;
    letList.splice(letList.length-1,1) ;
    
    var exampleCount = getMan("exampleCount");
    var exampleError = getMan("exampleError");
    
    _output("    example: " + text + ": success:" + (exampleCount-exampleError));
    _output("    example: " + text + ": error  :" + exampleError);
    
    removeMan("exampleText");
    removeMan("exampleCount");
    removeMan("exampleError");
}

// アウトプットが何かを記述する.
var lt = function(text,call) {
    addMan("itText",text);
    addMan("itCount",0);
    addMan("itError",0);
    
    _output("    it: " + text + ": start");
    
    subjectList[subjectList.length] = null;
    letList[letList.length] = null;
    
    call();
    
    subjectList.splice(subjectList.length-1,1) ;
    letList.splice(letList.length-1,1) ;
    
    var itCount = getMan("itCount");
    var itError = getMan("itError");
    
    _output("    it: " + text + ": success:" + (itCount-itError));
    _output("    it: " + text + ": error  :" + itError);
    
    removeMan("itText");
    removeMan("itCount");
    removeMan("itError");
}

// expactの実行前に実行する処理をセット.
var subject = function(call) {
    subjectList[subjectList.length] = call;
}

// subject,expact内の処理に対して、差し替え条件をセット.
var let = function(name,call) {
    var n = letList[letList.length];
    if(n == undefined || n == null) {
        n = {};
        letList[letList.length] = n;
    }
    n[name] = call;
}

// テスト比較処理.
var expact = function(call,rsCall) {
    try {
        var ret = margeCall(call,callSubject());
        if(!rsCall(ret)) {
            if(global["Json"]) {
                ret = global["Json"].encode(ret);
            }
            _output("      expact:error");
            _output("       > false :" + ret);
            addError();
        }
    } catch(e) {
        if(global["Json"]) {
            ret = global["Json"].encode(ret);
        }
        _output("      expact:error:" + e);
        _output("       > false :" + ret);
        addError();
    } finally {
        addCount();
    }
}

// グローバルセット.
global.JSpec = {reset:reset};
global.describe = describe;
global.context = context;
global.example = example;
global.lt = lt;
global.subject = subject;
global.let = let;
global._ = _;
global.expact = expact;

})(this);


////////////////////////////////////////////////////////////////////////////////
// 処理結果の比較系.
////////////////////////////////////////////////////////////////////////////////
(function(global) {

// map長を取得.
var objectLength = function(n) {
    var cnt = 0;
    for(k in n) {
        cnt ++;
    }
    return cnt;
}

// Objectかチェック.
var isObject = function(n) {
    return (n instanceof Object) || (n instanceof java.util.Map);
}

// Arrayかチェック.
var isArray = function(n) {
    return (n instanceof Array) || (n instanceof java.util.List);
}

// Dateかチェック.
var isDate = function(n) {
    return (n instanceof Date) || (n instanceof java.util.Date);
}

// Exceptionかチェック.
var isException = function(n) {
    return (n instanceof Error) || (n instanceof java.lang.Throwable);
}

// Array内容チェック.
var toArray = function(src,dest) {
    var len = src.length;
    if(dest.length != len) {
        return false;
    }
    for(var i = 0; i < len; i ++) {
        if(!eq(src[i],dest[i])) {
            return false;
        }
    }
    return true;
}

// map内容チェック.
var toObject = function(src,dest) {
    var len = objectLength(src);
    if(objectLength(dest) != len) {
        return false;
    }
    var s,d
    for(var k in src) {
        s = src[k];
        d = dest[k];
        if(!eq(s,d)) {
            return false;
        }
    }
    return true;
}

// 全ての内容が一致.
var eq = function(src,dest) {
    if(src == null || src == undefined) {
        if(dest == null || dest == undefined) {
            return true;
        }
        return false;
    }
    else if(isDate(src)) {
        if(isDate(dest)) {
            return src.getTime() == dest.getTime();
        }
        return false;
    }
    else if(isException(src)) {
        if(isException(dest)) {
            return true;
        }
        return false;
    }
    else if(isArray(src)) {
        if(isArray(dest)) {
            return toArray(src,dest);
        }
        return false;
    }
    else if(isObject(src)) {
        if(isObject(dest)) {
            return toObject(src,dest);
        }
        return false;
    }
    else if(src == dest) {
        return true;
    }
    return false;
}


// Array内容部分チェック.
var partArray = function(dest,src) {
    var cnt = 0;
    var chk = {};
    var lenD = dest.length;
    var lenS = src.length;
    for(var i = 0; i < lenD; i++) {
        for(var j = 0; j < lenS; j++) {
            if(chk[j] == true) {
                continue;
            }
            else if(part(src[j],dest[i])) {
                chk[j] = true;
                cnt ++;
                break;
            }
        }
    }
    return cnt == lenD;
}

// map内容チェック.
var partObject = function(dest,src) {
    for(var k in dest) {
        if(!part(src[k],dest[k])) {
            return false;
        }
    }
    return true;
}

// 部分一致.
var part = function(src,dest) {
    if(dest == null || dest == undefined) {
        if(src == null || src == undefined) {
            return true;
        }
        return false;
    }
    else if(isDate(dest)) {
        if(isDate(src)) {
            return dest.getTime() == src.getTime();
        }
        return false;
    }
    else if(isException(dest)) {
        if(isException(src)) {
            return true;
        }
        return false;
    }
    else if(isArray(dest)) {
        if(isArray(src)) {
            return partArray(dest,src);
        }
        return false;
    }
    else if(isObject(dest)) {
        if(isObject(src)) {
            return partObject(dest,src);
        }
        return false;
    }
    else if(src == dest) {
        return true;
    }
    return false;
}

// グローバルセット.
global.eq = eq;                     // 全ての内容が一致.
global.part = part;                 // 部分的に一致しているか.
global.exception = isException;     // 例外属性.
global.date = isDate;               // 日付属性.
global.array = isArray;             // 配列属性.
global.object = isObject;           // オブジェクト属性.

})(this);
