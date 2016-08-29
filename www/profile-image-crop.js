var exec = require('cordova/exec');

var ProfileImageCrop = function(){};

ProfileImageCrop.prototype.crop = function(options) {
    if(!options){
        options = {};
    }
    return new window.Promise(function(resolve, reject) {
        exec(resolve, reject, "ProfileImageCrop", "crop", [options]);
    });
};

window.ProfileImageCrop = new ProfileImageCrop();
