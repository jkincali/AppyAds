# AppyAds
AppyAds is an Android library easily included into any Android application.  Once included, the application author/owner is able to control advertising within their app.  Advertisements can be dynamically changed at any time, introducing new advertising campaigns when desired, without any code changes.  Several options allow the application author/owner to release in-home developed advertisements, or subscribe to external sources.  Options are dynamically controlled through the AppyAds.com website after an account has been set up for the application.

#Requirements
Target build for the Android application should be API 11 or higher.

#Dependencies
The external java library, TroyOzEZStrOut.jar needs to be included in the application.  All requests for advertisement campaigns from the Android device to the AppyAds server must be in a certain format, and this library ensures that format is consistent.
TroyOzEZStrOut.jar can be obtained <a href="http://troyozis.com/downloads/libraries/TroyOzEZStrOut.jar">here</a>.  Once obtained, place this .jar library in the libs directory of the application and include the following line into the build.gradle file:
```ruby
  compile files('libs/TroyOzEZStrOut.jar')
```
#Install the AppyAds library
The easiest way to include the AppyAds library into a project is to include the following in the dependencies section of the build.gradle file:
```ruby
  compile 'com.appyads.services:appyads:1.0'
```
#Use the AppyAds library
The AppyAds library consists of a sub MVC framework, which controls the advertising campaigns visible to users of the Android application.  Where these ads are placed within the application depends on the desires of the app designer/author/owner. For example, if the author wishes to have ads in a specific Activity within the Android application all that is required is to place a code snippet similar to the following in the layout xml file for that activity:
```ruby
  <com.appyads.services.AppyAdManager
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        appyadmgr:accountID="myaccount"
        appyadmgr:campaignID="mycampaign" >

  </com.appyads.services.AppyAdManager>
```
Please note that the AppyAdManager view has custom attributes.  Because of this, at the top of the layout xml file, the appyadmgr name space must be defined as follows:
```ruby
  xmlns:appyadmgr="http://schemas.appyads.com/attributes"
```
Of course, there are further options available, and intial views can even be embedded within the AppyAdManager view.  Please see the appyads.com developer pages for more information.
#Setting up Account IDs and Campaign IDs
All advertising campaigns are initiated through an account at <a href="appyads.com">AppyAds.com</a>.  From there, you can sign up for an account and begin to create your own ads, or sign up for ads to be delivered to your app.

