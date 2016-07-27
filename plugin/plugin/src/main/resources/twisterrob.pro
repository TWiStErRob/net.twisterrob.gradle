### -- twister-plugin-gradle/twisterrob.pro -- ###

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
#   http://omgitsmgp.com/2013/09/09/a-conservative-guide-to-proguard-for-android/

-keepattributes SourceFile,LineNumberTable

# Classes that extend from the framework, may be used from Manifest, layout, or xml resources
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.preference.Preference

# Used by xml layout inflation
#-keep public class * extends android.view.View {
#    public <init>(android.content.Context);
#    public <init>(android.content.Context, android.util.AttributeSet);
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#}
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context, android.util.AttributeSet);
#}
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#}
# View.android:onClick in layouts
#-keepclassmembers class * extends android.content.Context {
#    public void *(android.view.View);
#    public void *(android.view.MenuItem);
#}

# Keep names of classes and their constructors for any Views that stay after shrinking
-keepclasseswithmembernames public class * extends android.view.View {
	public <init>(android.content.Context);
}
-keepclasseswithmembernames public class * extends android.view.View {
	public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembernames public class * extends android.view.View {
	public <init>(android.content.Context, android.util.AttributeSet, int);
}

# JavaScript Interface for WebView (from API 17 the annotation is required)
-keepclasseswithmembers class * {
	@android.webkit.JavascriptInterface public <methods>;
}

# https://code.google.com/p/android/issues/detail?id=194513
# Reading library jar [P:\tools\android-sdk-windows\platforms\android-23\optional\org.apache.http.legacy.jar]
# Note: duplicate definition of library class [...]
# Note: there were 7 duplicate class definitions.
#       (http://proguard.sourceforge.net/manual/troubleshooting.html#duplicateclass)
-dontnote android.net.http.SslError
-dontnote android.net.http.SslCertificate
-dontnote android.net.http.SslCertificate$DName
-dontnote org.apache.http.conn.scheme.HostNameResolver
-dontnote org.apache.http.conn.scheme.SocketFactory
-dontnote org.apache.http.conn.ConnectTimeoutException
-dontnote org.apache.http.params.HttpParams

# Reading library jar [P:\tools\sdk\android\platforms\android-24\optional\org.apache.http.legacy.jar]
# Note: duplicate definition of library class [...]
# Note: there were 4 duplicate class definitions.
#       (http://proguard.sourceforge.net/manual/troubleshooting.html#duplicateclass)
-dontnote android.net.http.HttpResponseCache
-dontnote org.apache.http.conn.scheme.LayeredSocketFactory
-dontnote org.apache.http.params.CoreConnectionPNames
-dontnote org.apache.http.params.HttpConnectionParams

#-libraryjars libs

# The official support library.
#-keep class android.support.v4.app.** { *; }
#-keep interface android.support.v4.app.** { *; }

# Library JARs
#-keep class de.greenrobot.dao.** { *; }
#-keep interface de.greenrobot.dao.** { *; }
# http://joda-time.sourceforge.net/
#-keep class org.joda.** { *; }
#-keep interface org.joda.** { *; }
# http://loopj.com/android-async-http/
#-keep class com.loopj.android.http.** { *; }
#-keep interface com.loopj.android.http.** { *; }

# Library Projects
# http://actionbarsherlock.com/
#-keep class com.actionbarsherlock.** { *; }
#-keep interface com.actionbarsherlock.** { *; }
# http://viewpagerindicator.com/
#-keep class com.viewpagerindicator.** { *; }
#-keep interface com.viewpagerindicator.** { *; }
