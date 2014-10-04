# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
#   http://omgitsmgp.com/2013/09/09/a-conservative-guide-to-proguard-for-android/

# Classes that extend from the framework, may be used from Manifest, layout, or xml resources
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference

# Used by xml layout inflation
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
# View.android:onClick in layouts
-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.MenuItem);
}

# In-app purchases
-keep public class com.android.vending.billing.IInAppBillingService
# License Verification Library
-keep public class com.android.vending.licensing.ILicensingService
-keep public class com.google.vending.licensing.ILicensingService

# JavaScript Interface for WebView (from API 17 the annotation is required)
-keepclasseswithmembers class * {
    @android.webkit.JavascriptInterface public <methods>;
}


#-libraryjars libs

# The official support library.
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }

# Library JARs
-keep class de.greenrobot.dao.** { *; }
-keep interface de.greenrobot.dao.** { *; }
# http://joda-time.sourceforge.net/
-keep class org.joda.** { *; }
-keep interface org.joda.** { *; }
# http://loopj.com/android-async-http/
-keep class com.loopj.android.http.** { *; }
-keep interface com.loopj.android.http.** { *; }

# Library Projects
# http://actionbarsherlock.com/
-keep class com.actionbarsherlock.** { *; }
-keep interface com.actionbarsherlock.** { *; }
# http://viewpagerindicator.com/
-keep class com.viewpagerindicator.** { *; }
-keep interface com.viewpagerindicator.** { *; }
