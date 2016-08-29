//
//  ProfileImageCrop.m
//
//  Created by lihau on 28/8/16.
//  Copyright © 2016 Tan Li Hau. All rights reserved.
//

#import "ProfileImageCrop.h"
#import <Cordova/CDV.h>
#import <Cordova/CDVpluginResult.h>
#import "RSKImageCropViewController.h"
@interface ProfileImageCrop()<RSKImageCropViewControllerDelegate>
@end

@implementation ProfileImageCrop

UIActivityIndicatorView* ai = nil;
NSURL* imageURL;
@synthesize callbackId;

- (void) crop:(CDVInvokedUrlCommand *)command{
    self.callbackId = command.callbackId;
    NSDictionary* options = command.arguments[0] ?: [NSDictionary dictionary];
    NSString* imageUri = options[@"imageUri"];
    if(imageUri == nil){
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
        [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
        return;
    }
    imageURL = [NSURL URLWithString:imageUri];
    UIImage* sourceImage = [UIImage imageWithData: [NSData dataWithContentsOfURL: imageURL]];
    RSKImageCropViewController *imageCropVC = [[RSKImageCropViewController alloc] initWithImage:sourceImage cropMode:RSKImageCropModeCircle];

    imageCropVC.delegate = self;
    [self.viewController showViewController:imageCropVC sender:nil];
}

-(void)imageCropViewController:(RSKImageCropViewController *)controller didCropImage:(UIImage *)croppedImage usingCropRect:(CGRect)cropRect {
    NSLog(@"ProfileImageCrop: Finish Crop");
    [controller.presentingViewController dismissViewControllerAnimated:YES completion:^{
        if (ai != nil) {
            [ai stopAnimating];
        }
        //get the temp directory path
        NSString* docsPath = [NSTemporaryDirectory() stringByStandardizingPath];
        NSError* error = nil;
        NSFileManager* fileMgr = [[NSFileManager alloc] init];

        //generate unique file name
        NSString* filePath;
        NSString* fileName = (imageURL == nil) ? @"profile_pic" : imageURL.lastPathComponent.stringByDeletingPathExtension;
        NSData* data = UIImagePNGRepresentation(croppedImage);
        int i = 1;
        do {
            filePath = [NSString stringWithFormat:@"%@/%@_%03d_cropped.%@", docsPath, fileName, i++, @"png"];
        } while ([fileMgr fileExistsAtPath:filePath]);

        //save file
        if(![data writeToFile:filePath options: NSAtomicWrite error:&error]){
            CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_IO_EXCEPTION messageAsString:[error localizedDescription]];
            [self.commandDelegate sendPluginResult:result callbackId: self.callbackId];
            return;
        }
        NSMutableDictionary *resultDict = [NSMutableDictionary dictionary];
        [resultDict setValue: [[NSURL fileURLWithPath:filePath] absoluteString] forKey:@"resultUri"];
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary: resultDict];
        [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
    }];
}

- (void)imageCropViewControllerDidCancelCrop:(RSKImageCropViewController *)controller {
    NSLog(@"ProfileImageCrop: User pressed cancel button");
    [controller.presentingViewController dismissViewControllerAnimated:YES completion:^{
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
        [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
    }];
}

- (void) imageCropViewController:(RSKImageCropViewController *)controller willCropImage:(UIImage *)originalImage {
    ai = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle: UIActivityIndicatorViewStyleWhite];
    ai.center = controller.view.center;
    [controller.view addSubview:ai];
}

@end
