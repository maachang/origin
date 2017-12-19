// util.js

(function(global) {
"use strict";

var _u = undefined;

// UTF8文字列を、通常バイナリ(配列)に変換.
var utf8ToBinary = function( n,off,len ) {
    var lst = [] ;
    var cnt = 0 ;
    var c ;
    len += off ;
    for( var i = off ; i < len ; i ++ ) {
        c = n.charCodeAt(i)|0;
        if (c < 128) {
            lst[cnt++] = c|0 ;
        }
        else if ((c > 127) && (c < 2048)) {
            lst[cnt++] = (c >> 6) | 192 ;
            lst[cnt++] = (c & 63) | 128 ;
        }
        else {
            lst[cnt++] = (c >> 12) | 224 ;
            lst[cnt++] = ((c >> 6) & 63) | 128 ;
            lst[cnt++] = (c & 63) | 128 ;
        }
    }
    return lst ;
}
global.utf8ToBinary = utf8ToBinary;

// バイナリ(配列)をUTF8文字列に変換.
var binaryToUTF8 = function( n,off,len ) {
    var c ;
    var ret = "" ;
    len += off ;
    for( var i = off ; i < len ; i ++ ) {
        c = n[i] & 255;
        if (c < 128) {
            ret += String.fromCharCode(c);
        }
        else if ((c > 191) && (c < 224)) {
            ret += String.fromCharCode(((c & 31) << 6) |
                ((n[i+1] & 255) & 63));
            i += 1;
        }
        else {
            ret += String.fromCharCode(((c & 15) << 12) |
                (((n[i+1] & 255) & 63) << 6) |
                ((n[i+2] & 255) & 63));
            i += 2;
        }
    }
    return ret ;
}
global.binaryToUTF8 = binaryToUTF8;

// Base64 - encode.
var _b = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
global.btoa = function(t) {
    var a, c, n;
    var r = '', l = 0, s = 0;
    t = utf8ToBinary(t,0,t.length);
    var tl = t.length;
    for (n = 0; n < tl; n++) {
        c = t[n]|0;
        if (s == 0) {
            r += _b.charAt((c >> 2) & 63);
            a = (c & 3) << 4;
        }
        else if (s == 1) {
            r += _b.charAt((a | (c >> 4) & 15));
            a = (c & 15) << 2;
        }
        else if (s == 2) {
            r += _b.charAt(a | ((c >> 6) & 3));
            l += 1;
            r += _b.charAt(c & 63);
        }
        l += 1;
        s += 1;
        if (s == 3) s = 0;
    }
    if (s > 0) {
        r += _b.charAt(a);
        l += 1;
        r += '=';
        l += 1;
    }
    if (s == 1) {
        r += '=';
    }
    return r;
}

// Base64 - decode.
global.atob = function(t) {
    var c, n;
    var r = [], s = 0, a = 0;
    var tl = t.length;
    for (n = 0; n < tl; n++) {
        if ((c = _b.indexOf(t.charAt(n))) >= 0) {
            if (s) r[r.length] = (a | (c >> (6 - s)) & 255);
            s = (s + 2) & 7;
            a = (c << s) & 255;
        }
    }
    return binaryToUTF8(r,0,r.length);
}

// nullチェック.
global.isNull = function( value ) {
    return ( value == _u || value == null ) ;
}

// 文字存在チェック.
global.useString = ( function() {
    var _USE_STR_REG = /\S/g ;
    return function( str ) {
        var s = str ;
        if( isNull( s ) ) {
            return false ;
        }
        if( typeof( s ) != "string" ) {
            if( !isNull( s["length"] ) ) {
                return s["length"] != 0 ;
            }
            s = "" + s ;
        }
        return s.match( _USE_STR_REG ) != _u ;
    }
})() ;

// 数値チェック.
global.isNumeric = ( function() {
    var _IS_NUMERIC_REG = /[^0-9.0-9]/g ;
    return function( num ){
        var n = num ;
        if( isNull( n ) ) {
            return false ;
        }
        if( typeof( n ) != "string" ) {
            return true ;
        }
        if( n.indexOf( "-" ) == 0 ) {
            n = n.substring( 1 ) ;
        }
        return !( n.length == 0 || n.match( _IS_NUMERIC_REG ) ) && !(targetCharCount(0,n,".")>1) ;
    }
})() ;

// 指定文字の数を取得.
var targetCharCount = function(off,src,value) {
    var ret = 0;
    var p;
    while((p = src.indexOf(value,off)) != -1) {
        ret ++;
        off = p + value.length;
    }
    return ret;
}

// 文字列を置き換える.
global.changeString = function( base,src,dest ) {
    if( typeof( base ) != "string" ) {
        return base ;
    }
    if( typeof( src ) != "string" ) {
        src = "" + src ;
    }
    if( typeof( dest ) != "string" ) {
        dest = "" + dest ;
    }
    var old = val = base ;
    while( true ) {
        val = val.replace( src,dest ) ;
        if( old == val ) {
            return val ;
        }
        old = val ;
    }
}

// 小数点変換処理.
// n : 変換対象の情報を設定します.
// nn : 桁数を設定します.
// mode : 四捨五入の場合は[true]を設定します.
//        設定しない場合は四捨五入で処理されます.
// strFlag : 文字列で返却する場合は[true]をセットします.
// 戻り値 : 対象の数値が返却されます.
global.parseDecimal = function( n,nn,mode,strFlag ) {
    strFlag = ( strFlag == true || strFlag == "true" ) ;
    if( isNumeric( n ) ) {
        if( mode == false || mode == "false" ) {
            mode = false ;
        }
        else {
            mode = true ;
        }
        n = parseFloat( n ) ;
        if( typeof( nn ) == "number" || isNumeric( nn ) ) {
            var keta = nn = ( nn|0 ) ;
            if( nn < 1 ) {
                return n|0 ;
            }
            var cc = 1 ;
            for( var i = 0 ; i < nn ; i ++ ) {
                cc *= 10.0 ;
            }
            n = n * cc ;
            // 四捨五入
            if( mode ) {
                nn = parseFloat( n|0 ) ;
                nn = n - nn ;
                if( nn >= 0.5 ) {
                    n = parseFloat( ( n+1 )|0 ) ;
                }
                else {
                    n = parseFloat( ( n )|0 ) ;
                }
            }
            // 切捨て.
            else {
                n = parseFloat( ( n )|0 ) ;
            }
            n = n / cc ;
            var x = ""+n ;
            var p = x.indexOf( "." ) ;
            if( p != -1 ) {
                var pp = x.length ;
                for( var i = x.length-1 ; i >= p ; i -- ) {
                    c = x.substring( i,i+1 ) ;
                    if( c == "0" ) {
                        pp = i ;
                    }
                    else if( c == "." ) {
                        pp = p ;
                        break ;
                    }
                    else {
                        break ;
                    }
                }
                if( pp != x.length ) {
                    x = x.substring( 0,pp ) ;
                }
                if( strFlag == true ) {
                    if( ( p = x.indexOf( "." ) ) == -1 ) {
                        x = x + "." ;
                        for( var i = 0 ; i < keta ; i ++ ) {
                            x += "0" ;
                        }
                        return x ;
                    }
                    else if( x.length - p < keta ) {
                        nn = x.length - p ;
                        for( var i = 0 ; i < nn ; i ++ ) {
                            x += "0" ;
                        }
                        return x ;
                    }
                }
                if( x.indexOf( "." ) == -1 ) {
                    return parseFloat( x ) ;
                }
                return parseFloat( x ) ;
            }
            else if( strFlag == true ) {
                if( ( p = x.indexOf( "." ) ) == -1 ) {
                    x = x + "." ;
                    for( var i = 0 ; i < keta ; i ++ ) {
                        x += "0" ;
                    }
                    return x ;
                }
                else if( x.length - p < keta ) {
                    nn = x.length - p ;
                    for( var i = 0 ; i < nn ; i ++ ) {
                        x += "0" ;
                    }
                    return x ;
                }
            }
        }
    }
    return n ;
}

})(this);
