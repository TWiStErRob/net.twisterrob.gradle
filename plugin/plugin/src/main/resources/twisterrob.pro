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

# Android Gradle Plugin adds core-lambda-stubs.jar unconditionally (not using Java 8) to the classpath,
# even though android.jar has these classes.
# Reading library jar [P:\tools\sdk\android\build-tools\28.0.3\core-lambda-stubs.jar]
# Note: duplicate definition of library class [...]
# Note: there were 6 duplicate class definitions.
#       (http://proguard.sourceforge.net/manual/troubleshooting.html#duplicateclass)
-dontnote java.lang.invoke.CallSite
-dontnote java.lang.invoke.LambdaConversionException
-dontnote java.lang.invoke.MethodHandle
-dontnote java.lang.invoke.MethodHandles$Lookup
-dontnote java.lang.invoke.MethodHandles
-dontnote java.lang.invoke.MethodType

# Kotlin
# Note: kotlin.jvm.internal.Reflection: can't find dynamically referenced class kotlin.reflect.jvm.internal.ReflectionFactoryImpl
# Note: kotlin.jvm.internal.ReflectionFactory calls 'Class.getGenericInterfaces'
# Note: there were 1 classes trying to access generic signatures using reflection. (use '-keepattributes Signature').
#       (http://proguard.sourceforge.net/manual/troubleshooting.html#attributes)
# Kotlin uses reflection to print lambdas
# TODO reproduce and check what's the difference between debug and release
-dontnote kotlin.jvm.internal.ReflectionFactory
# Kotlin Reflection is not added to classpath
-dontnote kotlin.reflect.jvm.internal.ReflectionFactoryImpl
# Kotlin uses Class.forName to figure out JRE/JDK extensions (i.e. stdlib-jdk7 which is commonly usable in Android)
# Note: kotlin.internal.PlatformImplementationsKt: can't find dynamically referenced class kotlin.internal.JRE7PlatformImplementations
# Note: kotlin.internal.PlatformImplementationsKt: can't find dynamically referenced class kotlin.internal.JRE8PlatformImplementations
# Note: kotlin.internal.PlatformImplementationsKt: can't find dynamically referenced class kotlin.internal.jdk8.JDK8PlatformImplementations
# Note: there were 3 unresolved dynamic references to classes or interfaces.
#       (http://proguard.sourceforge.net/manual/troubleshooting.html#dynamicalclass)
# Ignore JRE7 and JRE8 classes as stdlib-jre7 and stdlib-jre8 are deprecated artifacts, these will never be on classpath
-dontnote kotlin.internal.JRE7PlatformImplementations
-dontnote kotlin.internal.JRE8PlatformImplementations
# Do not ignore JDK7 and JDK8 as these may be used, let the apps pick which one is used and ignore the other
#-dontnote kotlin.internal.jdk7.JDK7PlatformImplementations
#-dontnote kotlin.internal.jdk8.JDK8PlatformImplementations


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
