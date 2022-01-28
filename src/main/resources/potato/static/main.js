function validatePort(rule, value, callback) {
    if (value === '') {
        if (rule.required) {
            callback(new Error('请填写端口号'));
        }
    } else {
        if((/(^[1-9]\d*$)/.test(value))){
            callback();
        } else {
            callback(new Error('端口号格式错误'));
        }
    }
}
function guid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
        return v.toString(16);
    });
}
function validateArray(rule, value, callback) {
    if (rule.required) {
        if (!Array.isArray(value) || value.length == 0) {
            callback(new Error(rule.message));
        } else {
            callback();
        }
    } else {
        callback();
    }
}
function validateNumber(rule, value, callback) {
    if (typeof value === 'undefined' || value === null || value === '') {
        if (rule.required) {
            callback(new Error(rule.message));
        } else {
            callback();
        }
    } else {
        if((/(^[1-9]\d*$)/.test(value))){
            callback();
        } else {
            callback(new Error(rule.message));
        }
    }
}
function validateZNumber(rule, value, callback) {
    if (typeof value === 'undefined' || value === null || value === '') {
        if (rule.required) {
            callback(new Error(rule.message));
        } else {
            callback();
        }
    } else {
        if((/^(0|[1-9][0-9]*)$/.test(value))){
            callback();
        } else {
            callback(new Error(rule.message));
        }
    }
}
function validateAllNumber(rule, value, callback) {
    if (typeof value === 'undefined' || value === null || value === '') {
        if (rule.required) {
            callback(new Error(rule.message));
        } else {
            callback();
        }
    } else {
        if((/^(\-|\+)?(\d{1,8})?([\.]\d*)?$/.test(value))){
            callback();
        } else {
            callback(new Error(rule.message));
        }
    }
}
function validateHtmlUri(rule, value, callback) {
    if (typeof value === 'undefined' || value === null || value === '') {
        if (rule.required) {
            callback(new Error(rule.message));
        } else {
            callback();
        }
    } else {
        if((/^(\/([0-9]|[a-z]|[A-Z])+)+\.html$/.test(value))){
            callback();
        } else {
            callback(new Error(rule.message));
        }
    }
}
function validateApi(rule, value, callback) {
    if (typeof value === 'undefined' || value === null || value === '') {
        if (rule.required) {
            callback(new Error(rule.message));
        } else {
            callback();
        }
    } else {
        if((/^(\/([0-9]|[a-z]|[A-Z])+)+$/.test(value))){
            callback();
        } else {
            callback(new Error(rule.message));
        }
    }
}
function validateSystemHtmlUri(rule, value, callback) {
    if (typeof value === 'undefined' || value === null || value === '') {
        if (rule.required) {
            callback(new Error(rule.message));
        } else {
            callback();
        }
    } else {
        if (value === '/db.html'
            || value === '/error.html'
            || value === '/index.html'
            || value === '/operate.html'
            || value === '/search.html'
            || value === '/storage.html'
            || value === '/table.html'
            || value === '/api.html') {
            callback(new Error(rule.message));
        } else {
            callback();
        }
    }
}
function validateSystemApi(rule, value, callback) {
    if (typeof value === 'undefined' || value === null || value === '') {
        if (rule.required) {
            callback(new Error(rule.message));
        } else {
            callback();
        }
    } else {
        if (value === '/db/tables'
            || value === '/db/descTable'
            || value === '/metaApi/info'
            || value === '/metaApi/update'
            || value === '/meta/add'
            || value === '/meta/update'
            || value === '/meta/list'
            || value === '/meta/delete'
            || value === '/meta/boot'
            || value === '/meta/unBoot'
            || value === '/meta/getBootCode'
            || value === '/meta/generate'
            || value === '/metaDb/info'
            || value === '/metaDb/update'
            || value === '/metaOperate/info'
            || value === '/metaOperate/update'
            || value === '/metaSearch/info'
            || value === '/metaSearch/update'
            || value === '/metaStorage/info'
            || value === '/metaStorage/update'
            || value === '/metaTable/info'
            || value === '/metaTable/update'
            || value === '/setting/size'
            || value === '/setting/judge'
            || value === '/setting/searchElementType'
            || value === '/setting/operateElementType'
            || value === '/setting/java'
            || value === '/setting/storageElementType'
            || value === '/setting/tableColumnFormatterData') {
            callback(new Error(rule.message));
        } else {
            callback();
        }
    }
}

Date.prototype.format = function(fmt)
{
    let o = {
        "M+" : this.getMonth()+1, //月份
        "d+" : this.getDate(), //日
        "h+" : this.getHours()%12 == 0 ? 12 : this.getHours()%12, //小时
        "H+" : this.getHours(), //小时
        "m+" : this.getMinutes(), //分
        "s+" : this.getSeconds(), //秒
        "q+" : Math.floor((this.getMonth()+3)/3), //季度
        "S" : this.getMilliseconds() //毫秒
    };
    if(/(y+)/.test(fmt))
        fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
    for(let k in o)
        if(new RegExp("("+ k +")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
    return fmt;
}