# profile-image-crop

<!-- badge -->
[![npm version](https://img.shields.io/npm/v/profile-image-crop.svg)](https://www.npmjs.com/package/profile-image-crop)
[![npm license](https://img.shields.io/npm/l/profile-image-crop.svg)](https://www.npmjs.com/package/profile-image-crop)

<!-- endbadge -->

# Install

```bash
$ cordova plugin add profile-image-crop
```

# Demo

### Android

![android](https://github.com/tanhauhau/cordova-plugin-imagecrop/blob/master/demo/android.png?raw=true)

### iOS

![ios](https://github.com/tanhauhau/cordova-plugin-imagecrop/blob/master/demo/ios.png?raw=true)

# Usage

```javascript
window.ProfileImageCrop.crop({
   imageUri: imageUri,
})
.then(function(result){
   console.log(result);
   /*
    { resultUri: '......' }
   */
})
.catch(function(e){
   console.log(e);
   /*
    { name: 'ProfileImageCrop', code: '...' }
   */
})
```

## Error Code

**UNABLE\_TO\_SAVE** - Failed to save the cropped image to a temp file

**NO\_SUCH\_FILE** - Image Uri given does not exist

**USER\_CANCELLED** - User cancelled

**NO\_IMAGE\_URI** - Empty Image Uri given

**JSON\_EXCEPTION** - Failed to marshall or unmarshall json objects

**UNKNOWN\_ERROR** - Unknown errors


# Library Used

# Android

[cookie-cutter](https://github.com/adamstyrc/cookie-cutter) by adamstyrc

# iOS

[RSKImagecropper](https://github.com/ruslanskorb/RSKImageCropper) by ruslanskorb

# License

The MIT License (MIT)

Copyright (c) 2016 Tan Li Hau

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
