// seabass32.js

(function() {
"use strict";

// undefined定義.
var _u = undefined ;
var __u = "undefined" ;

// CustomBase64.
var CBase64 = (function() {
    var o = {};
    var NOT_DEC = -1;
    var EQ = '=';
    var ENC_CD = "0123456789+abcdefghijklmnopqrstuvwxyz/ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    var DEC_CD = (function() {
        var src = ENC_CD;
        var ret = {};
        var len = src.length;
        for(var i = 0; i < len; i ++) {
            ret[src[i]] = i;
        }
        return ret;
    })();
    o.encode = function(bin) {
        var i, j, k;
        var allLen = allLen = bin.length ;
        var etc = (allLen % 3)|0;
        var len = (allLen / 3)|0;
        var ary = new Array((len * 4) + ((etc != 0) ? 4 : 0));
        for (i = 0, j = 0, k = 0; i < len; i++, j += 3, k += 4) {
            ary[k] = ENC_CD[((bin[j] & 0x000000fc) >> 2)];
            ary[k + 1] = ENC_CD[(((bin[j] & 0x00000003) << 4) | ((bin[j+1] & 0x000000f0) >> 4))];
            ary[k + 2] = ENC_CD[(((bin[j+1] & 0x0000000f) << 2) | ((bin[j+2] & 0x000000c0) >> 6))];
            ary[k + 3] = ENC_CD[(bin[j+2] & 0x0000003f)];
        }
        switch (etc) {
        case 1:
            j = len * 3;
            k = len * 4;
            ary[k] = ENC_CD[((bin[j] & 0x000000fc) >> 2)];
            ary[k + 1] = ENC_CD[((bin[j] & 0x00000003) << 4)];
            ary[k + 2] = EQ;
            ary[k + 3] = EQ;
            break;
        case 2:
            j = len * 3;
            k = len * 4;
            ary[k] = ENC_CD[((bin[j] & 0x000000fc) >> 2)];
            ary[k + 1] = ENC_CD[(((bin[j] & 0x00000003) << 4) | ((bin[j+1] & 0x000000f0) >> 4))];
            ary[k + 2] = ENC_CD[(((bin[j+1] & 0x0000000f) << 2))];
            ary[k + 3] = EQ;
            break;
        }
        return ary.join('');
    }
    o.decode = function(base64) {
        var i, j, k;
        var allLen = base64.length ;
        var etc = 0 ;
        for (i = allLen - 1; i >= 0; i--) {
            if (base64.charAt(i) == EQ) {
                etc++;
            } else {
                break;
            }
        }
        var len = (allLen / 4)|0;
        var ret = new Array((len * 3) - etc);
        len -= 1;
        for (i = 0, j = 0, k = 0; i < len; i++, j += 4, k += 3) {
            ret[k] = (((DEC_CD[base64[j]] & 0x0000003f) << 2) | ((DEC_CD[base64[j+1]] & 0x00000030) >> 4));
            ret[k + 1] = (((DEC_CD[base64[j+1]] & 0x0000000f) << 4) | ((DEC_CD[base64[j+2]] & 0x0000003c) >> 2));
            ret[k + 2] = (((DEC_CD[base64[j+2]] & 0x00000003) << 6) | (DEC_CD[base64[j+3]] & 0x0000003f));
        }
        switch (etc) {
        case 0:
            j = len * 4;
            k = len * 3;
            ret[k] = (((DEC_CD[base64[j]] & 0x0000003f) << 2) | ((DEC_CD[base64[j+1]] & 0x00000030) >> 4));
            ret[k + 1] = (((DEC_CD[base64[j+1]] & 0x0000000f) << 4) | ((DEC_CD[base64[j+2]] & 0x0000003c) >> 2));
            ret[k + 2] = (((DEC_CD[base64[j+2]] & 0x00000003) << 6) | (DEC_CD[base64[j+3]] & 0x0000003f));
            break;
        case 1:
            j = len * 4;
            k = len * 3;
            ret[k] = (((DEC_CD[base64[j]] & 0x0000003f) << 2) | ((DEC_CD[base64[j+1]] & 0x00000030) >> 4));
            ret[k + 1] = (((DEC_CD[base64[j+1]] & 0x0000000f) << 4) | ((DEC_CD[base64[j+2]] & 0x0000003c) >> 2));
            break;
        case 2:
            j = len * 4;
            k = len * 3;
            ret[k] = (((DEC_CD[base64[j]] & 0x0000003f) << 2) | ((DEC_CD[base64[j+1]] & 0x00000030) >> 4));
            break;
        }
        return ret;
    }
    return o;
})();

// seabass32.
return (function(){
    var o = {};
    var SB32HEAD = "";
    var rand = new Xor128(new Date().getTime());
    o.setHeader = function(value) {
        SB32HEAD = "" + value;
    }
    o.getHeader = function() {
        return SB32HEAD;
    }
    o.createKey = function(baseKey,domain) {
        if(domain == _u || domain == null) {
            domain = document.domain;
            if(!useString(domain)) {
                domain = "0.0.0.0";
            }
        }
        var domainBin = code16(domain,1) ;
        var baseKeyBin = code16(baseKey,1) ;
        var ret = domainBin.concat(baseKeyBin) ;
        for( var i = 0 ; i < 16 ; i ++ ) {
            ret[ i ] = _convert( ret,i,baseKeyBin[ i ] ) ;
        }
        for( var i = 15,j = 0 ; i >= 0 ; i --,j ++ ) {
            ret[ i+16 ] = _convert( ret,i+16,domainBin[ j ] ) ;
        }
        return ret ;
    }
    o.encode = function( value,pKey ) {
        return o.encodeB(strToArray( ""+value ),pKey) ;
    }
    o.encodeB = function( bin,pKey ) {
        // 第一引数がバイナリ.
        var pubKey = _randKey() ;
        var key32 = _convertKey(pKey,pubKey) ;
        var key256 = _key256(key32) ;
        key32 = null ;
        var stepNo = _getStepNo( pKey,bin ) & 0x0000007f ;
        var nowStep = _convert256To( key256,pubKey,stepNo ) ;
        _ed( true,bin,key256,nowStep ) ;
        var eb = new Array(34+bin.length) ;
        eb[ 0 ] = rand.nextInt() & 0x000000ff;
        eb[ 1 ] = (~(stepNo^eb[ 0 ])) ;
        arraycopy( pubKey,0,eb,2,32 ) ;
        arraycopy( bin,0,eb,34,bin.length ) ;
        return SB32HEAD + CBase64.encode(eb);
    }
    o.decode = function( value,pKey ) {
        return aryToString(o.decodeB(value,pKey)) ;
    }
    o.decodeB = function( value,pKey ) {
        // 戻り値がバイナリ.
        if(value.indexOf(SB32HEAD) != 0) {
            throw "decode:Unknown data format" ;
        }
        var bin = CBase64.decode(value.substring(SB32HEAD.length));
        if( bin.length <= 34 ) {
            throw "decode:Invalid binary length" ;
        }
        var stepNo = ((~(bin[ 1 ]^bin[0]))&0x0000007f) ;
        var pubKey = new Array( 32 ) ;
        arraycopy( bin,2,pubKey,0,32 ) ;
        var bodyLen = bin.length - 34 ;
        var body = new Array( bodyLen ) ;
        arraycopy(bin,34,body,0,bodyLen) ;
        bin = null ;
        var key32 = _convertKey(pKey,pubKey) ;
        var key256 = _key256( key32 ) ;
        key32 = null ;
        var nowStep = _convert256To( key256,pubKey,stepNo ) ;
        _ed( false,body,key256,nowStep ) ;
        if( ( _getStepNo( pKey,body ) & 0x0000007f ) != stepNo ) {
            throw "decode:Decryption process failed" ;
        }
        return body;
    }
    var _convert = function(key,no,pause) {
        switch ((no & 0x00000001)) {
            case 0:
                return ((~(pause ^ key[no])) & 0x000000ff) ;
            case 1:
                return ((pause ^ key[no]) & 0x000000ff) ;
        }
        return 0 ;
    }
    var _randKey = function() {
        var bin = new Array(32) ;
        for( var i = 0 ; i < 32 ; i ++ ) {
            bin[ i ] = ( rand.next() & 0x000000ff ) ;
        }
        return bin ;
    }
    var code16 = function(s,mode) {
        var ret = [177, 75, 163, 143, 73, 49, 207, 40, 87, 41, 169, 91, 184, 67, 254, 89];
        var n;
        var len = s.length;
        mode = mode|0;
        for(var i = 0; i < len; i ++) {
            n = mode==1 ? s.charCodeAt(i)|0 : s[i];
            if((i&0x00000001) == 0) {
                for(var j = 1; j < 16; j+= 2) {
                    ret[j] = ret[j] ^ (n-(i+j));
                }
                for(var j = 0; j < 16; j+= 2) {
                    ret[j] = ret[j] * (n-(i+j));
                }
            }
            else {
                for(var j = 0; j < 16; j+= 2) {
                    ret[j] = ret[j] ^ (n-(i+j));
                }
                for(var j = 1; j < 16; j+= 2) {
                    ret[j] = ret[j] * (n-(i+j));
                }
            }
        }
        for(var i = 0; i < 16; i++) {
            ret[i] = ret[i] & 0x000000ff;
        }
        return ret;
    }
    var _convertKey = function( pKey,key ) {
        var low = code16(pKey,0);
        var hight = code16(key,0);
        var ret = new Array(32);
        for (var i = 0,j = 0,k = 15; i < 16; i++, j += 2, k--) {
            ret[j] = _convert(low, i, key[j]);
            ret[j + 1] = _convert(hight, i, low[k]);
        }
        return ret;
    }
    var _key256 = function( key32 ) {
        var ret = new Array( 256 ) ;
        var b = new Array( 4 ) ;
        var o ;
        var n = 0 ;
        var s,e ;
        for( var i = 0,j = 0 ; i < 31 ; i += 2,j += 16 ) {
            s = ( key32[i] & 0x000000ff ) ;
            e = ( key32[i+1] & 0x000000ff ) ;
            if( ( n & 0x00000001 ) != 0 ) {
                n += s ^ (~ e ) ;
            }
            else {
                n -= (~s) ^ e ;
            }
            b[0] = (n & 0x000000ff) ;
            b[1] = (((n & 0x0000ff00)>>8)&0x000000ff) ;
            b[2] = (((n & 0x00ff0000)>>16)&0x000000ff) ;
            b[3] = (((n & 0xff000000)>>24)&0x000000ff) ;
            o = code16(b,0) ;
            arraycopy( o,0,ret,j,16 ) ;
        }
        return ret ;
    }
    var _getStepNo = function(pubKey,binary) {
        var i, j;
        var bin;
        var ret = 0;
        var len = binary.length ;
        var addCd = (pubKey[(binary[len>>1] & 0x0000001f)] & 0x00000003) + 1;
        for (i = 0, j = 0; i < len; i += addCd, j += addCd) {
            bin = ((~binary[i]) & 0x000000ff);
            ret = ((bin & 0x00000001) + ((bin & 0x00000002) >> 1)
                    + ((bin & 0x00000004) >> 2) + ((bin & 0x00000008) >> 3)
                    + ((bin & 0x00000010) >> 4) + ((bin & 0x00000020) >> 5)
                    + ((bin & 0x00000040) >> 6) + ((bin & 0x00000080) >> 7))
                    + (j & 0x000000ff) + ret;
        }
        if ((ret & 0x00000001) == 0) {
            for (i = 0; i <32; i++) {
                bin = (((pubKey[i] & 0x00000001) == 0) ? ((~pubKey[i]) & 0x000000ff)
                        : (pubKey[i] & 0x000000ff));
                ret += ((bin & 0x00000001) + ((bin & 0x00000002) >> 1)
                        + ((bin & 0x00000004) >> 2) + ((bin & 0x00000008) >> 3)
                        + ((bin & 0x00000010) >> 4) + ((bin & 0x00000020) >> 5)
                        + ((bin & 0x00000040) >> 6) + ((bin & 0x00000080) >> 7));
            }
        } else {
            for (i = 0; i < 32; i++) {
                bin = (((pubKey[i] & 0x00000001) == 0) ? ((~pubKey[i]) & 0x000000ff)
                        : (pubKey[i] & 0x000000ff));
                ret -= ((bin & 0x00000001) + ((bin & 0x00000002) >> 1)
                        + ((bin & 0x00000004) >> 2) + ((bin & 0x00000008) >> 3)
                        + ((bin & 0x00000010) >> 4) + ((bin & 0x00000020) >> 5)
                        + ((bin & 0x00000040) >> 6) + ((bin & 0x00000080) >> 7));
            }
        }
        return ((~ret) & 0x000000ff);
    }
    var _flip = function(pause, step) {
        switch (step & 0x00000007) {
        case 1:
            return ((((pause & 0x00000003) << 6) & 0x000000c0) | (((pause & 0x000000fc) >> 2) & 0x0000003f)) & 0x000000ff ;
        case 2:
            return ((((pause & 0x0000003f) << 2) & 0x000000fc) | (((pause & 0x000000c0) >> 6) & 0x00000003)) & 0x000000ff ;
        case 3:
            return ((((pause & 0x00000001) << 7) & 0x00000080) | (((pause & 0x000000fe) >> 1) & 0x0000007f)) & 0x000000ff ;
        case 4:
            return ((((pause & 0x0000000f) << 4) & 0x000000f0) | (((pause & 0x000000f0) >> 4) & 0x0000000f)) & 0x000000ff ;
        case 5:
            return ((((pause & 0x0000007f) << 1) & 0x000000fe) | (((pause & 0x00000080) >> 7) & 0x00000001)) & 0x000000ff ;
        case 6:
            return ((((pause & 0x00000007) << 5) & 0x000000e0) | (((pause & 0x000000f8) >> 3) & 0x0000001f)) & 0x000000ff ;
        case 7:
            return ((((pause & 0x0000001f) << 3) & 0x000000f8) | (((pause & 0x000000e0) >> 5) & 0x00000007)) & 0x000000ff ;
        }
        return pause & 0x000000ff ;
    }
    var _nflip = function(pause, step) {
        switch (step & 0x00000007) {
        case 1:
            return ((((pause & 0x0000003f) << 2) & 0x000000fc) | (((pause & 0x000000c0) >> 6) & 0x00000003)) & 0x000000ff ;
        case 2:
            return ((((pause & 0x00000003) << 6) & 0x000000c0) | (((pause & 0x000000fc) >> 2) & 0x0000003f)) & 0x000000ff ;
        case 3:
            return ((((pause & 0x0000007f) << 1) & 0x000000fe) | (((pause & 0x00000080) >> 7) & 0x00000001)) & 0x000000ff ;
        case 4:
            return ((((pause & 0x0000000f) << 4) & 0x000000f0) | (((pause & 0x000000f0) >> 4) & 0x0000000f)) & 0x000000ff ;
        case 5:
            return ((((pause & 0x00000001) << 7) & 0x00000080) | (((pause & 0x000000fe) >> 1) & 0x0000007f)) & 0x000000ff ;
        case 6:
            return ((((pause & 0x0000001f) << 3) & 0x000000f8) | (((pause & 0x000000e0) >> 5) & 0x00000007)) & 0x000000ff ;
        case 7:
            return ((((pause & 0x00000007) << 5) & 0x000000e0) | (((pause & 0x000000f8) >> 3) & 0x0000001f)) & 0x000000ff ;
        }
        return pause & 0x000000ff ;
    }
    var _convert256To = function( key256, pKey, step) {
        var ns = step ;
        for (var i = 0, j = 0; i < 256; i++, j = ((j + 1) & 0x0000001f)) {
            ns = (ns ^ (~(key256[i]))) ;
            if( (ns & 0x00000001 ) == 0 ) {
                ns = ~ns ;
            }
            key256[i] = _convert(pKey, j, key256[i]);
            key256[i] = _flip(key256[i], ns);
        }
        return ns;
    }
    var _ed = function(mode,binary,key256,step) {
        var len = binary.length ;
        var ns = step ;
        if( mode ) {
            for (var i = 0, j = 0; i < len; i++, j = ((j + 1) & 0x000000ff)) {
                ns = (ns ^ (~( key256[j]))) ;
                if( (ns & 0x00000001 ) != 0 ) {
                    ns = ~ns ;
                }
                binary[i] = _convert(key256, j, binary[i]);
                binary[i] = _flip( binary[ i ],ns ) ;
            }
        }
        else {
            for (var i = 0, j = 0; i < len; i++, j = ((j + 1) & 0x000000ff)) {
                ns = (ns ^ (~( key256[j]))) ;
                if( (ns & 0x00000001 ) != 0 ) {
                    ns = ~ns ;
                }
                binary[i] = _nflip( binary[ i ],ns ) ;
                binary[i] = _convert(key256, j, binary[i]);
            }
        }
    }
    var strToArray = function( s ) {
        var len = s.length ;
        var ret = new Array( len ) ;
        for( var i = 0 ; i < len ; i ++ ) {
            ret[ i ] = s.charCodeAt( i )|0 ;
        }
        return ret ;
    }
    var aryToString = function( s ) {
        var len = s.length ;
        var ret = new Array( len ) ;
        for( var i = 0 ; i < len ; i ++ ) {
            ret[ i ] = String.fromCharCode( s[ i ] ) ;
        }
        return ret.join('') ;
    },
    var arraycopy = function( s,sp,d,dp,len ) {
        len = len|0;
        sp = sp|0;
        dp = dp|0;
        for( var i = 0 ; i < len ; i ++ ) {
            d[(dp+i)] = s[(sp+i)] ;
        }
    }
    return o;
})();

})();
