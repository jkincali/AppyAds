# AppyAds
AppyAds is an Android library easily included into any Android application.  Once included, the application author/owner is able to control advertising within their app.  Advertisements can be dynamically changed at any time, introducing new advertising campaigns when desired, without any code changes.  Several options allow the application author/owner to release in-home developed advertisements, or subscribe to external sources.  Options are dynamically controlled through the AppyAds.com website after an account has been set up for the application.

#Library Build Notes
```ruby
minSdkVersion 16 // Should be 16 or higher.
targetSdkVersion 28
```

#Permissions
The application's manifest should allow the following permissions:
```ruby
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

#Include/Compile the AppyAds library
The easiest way to include the AppyAds library into a project is to include the following in the dependencies section of the build.gradle file:
```ruby
  implementation 'com.appyads.services:appyads:1.1.4'
```
So the final edit of the application's dependencies section of the build.gradle file might look something like:
```ruby
  dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.android.support:appcompat-v7:28.0.0'
    implementation 'com.appyads.services:appyads:1.1.4'
}
```
#Implement AppyAds service functionality
The AppyAds library consists of a sub MVC framework, which controls the advertising campaigns visible to users of the Android application.  Where these ads are placed within the application depends on the desires of the app designer/author/owner. For example, if the author wishes to have ads in a specific Activity within the Android application all that is required is to place a code snippet similar to the following in the layout xml file for that activity:
```ruby
  <com.appyads.services.AppyAdManager
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        appyadmgr:accountID="myaccount" >

  </com.appyads.services.AppyAdManager>
```
Please note that the AppyAdManager view has custom attributes.  Because of this, at the top of the layout xml file, the appyadmgr name space must be defined as follows:
```ruby
  xmlns:appyadmgr="http://schemas.appyads.com/attributes"
```
Of course, there are further options available, and intial views can even be embedded within the AppyAdManager view.  Please see the <a href="http://appyads.com/support/docs/android/">appyads.com developer pages</a> (especially the AppyAdManager section) for more information.
#Setting up Account IDs and Campaign IDs
All advertising campaigns are initiated through an account at <a href="appyads.com">AppyAds.com</a>.  From there, you can sign up for an account and begin to create your own ads, or sign up for ads to be delivered to your app.

