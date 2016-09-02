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
    NSDictionary* options = command.arguments[0];
    NSString* imageUri;
    if(options == nil){
        [self callbackError:@"JSON_EXCEPTION"];
    }else if((imageUri = options[@"imageUri"]) == nil){
        [self callbackError:@"NO_IMAGE_URI"];
    }else{
        imageURL = [NSURL URLWithString:imageUri];
        UIImage* sourceImage = [UIImage imageWithData: [NSData dataWithContentsOfURL: imageURL]];
        RSKImageCropViewController *imageCropVC = [[RSKImageCropViewController alloc] initWithImage:sourceImage cropMode:RSKImageCropModeCircle];

        imageCropVC.delegate = self;
        [self.viewController showViewController:imageCropVC sender:nil];
    }
}

-(void)imageCropViewController:(RSKImageCropViewController *)controller didCropImage:(UIImage *)croppedImage usingCropRect:(CGRect)cropRect {
    NSLog(@"ProfileImageCrop: Finish Crop");
    [controller.presentingViewController dismissViewControllerAnimated:YES completion:^{
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
        if([data writeToFile:filePath options: NSAtomicWrite error:&error]){
            //stop loading animation
            if (ai != nil) {
                [ai stopAnimating];
            }

            //return result uri
            NSDictionary *resultDict = @{ @"resultUri" : [[NSURL fileURLWithPath:filePath] absoluteString] };
            CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary: resultDict];
            [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
        }else{
            //save error
            [self callbackError:@"UNABLE_TO_SAVE"];
            NSLog([NSString stringWithFormat: @"ProfileImageCrop: UNABLE_TO_SAVE %@", error.localizedDescription], [NSThread callStackSymbols]);
        }
    }];
}

- (void)imageCropViewControllerDidCancelCrop:(RSKImageCropViewController *)controller {
    NSLog(@"ProfileImageCrop: User pressed cancel button");
    [controller.presentingViewController dismissViewControllerAnimated:YES completion:^{
        [self callbackError:@"USER_CANCELLED"];
    }];
}

- (void) imageCropViewController:(RSKImageCropViewController *)controller willCropImage:(UIImage *)originalImage {
    ai = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle: UIActivityIndicatorViewStyleWhite];
    ai.center = controller.view.center;
    [controller.view addSubview:ai];
}

- (void) callbackError: (NSString *) errorMessage {
    NSDictionary* resultDict = @{ @"name": @"ProfileImageCrop", @"code": errorMessage };
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT messageAsDictionary: resultDict];
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

@end
