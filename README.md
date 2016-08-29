# profile-image-crop

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
})
```
