//
//  ProfileImageCrop.h
//
//  Created by lihau on 28/8/16.
//  Copyright © 2016 Tan Li Hau. All rights reserved.
//

#import <Cordova/CDVPlugin.h>

@interface ProfileImageCrop : CDVPlugin

@property (copy) NSString* callbackId;

- (void) crop:(CDVInvokedUrlCommand *)command;

@end
