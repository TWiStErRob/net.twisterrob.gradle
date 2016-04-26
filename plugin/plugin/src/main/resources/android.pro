### -- twister-plugin-gradle/android.pro -- ###
### MODIFICATION: based on <SDK>/tools/proguard/proguard-android-optimize.txt ###

# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

# Optimizations: If you don't want to optimize, use the
# proguard-android.txt configuration file instead of this one, which
# turns off the optimization flags.  Adding optimization introduces
# certain risks, since for example not all optimizations performed by
# ProGuard works on all versions of Dalvik.  The following flags turn
# off various optimizations known to have issues, but the list may not
# be complete or up to date. (The "arithmetic" optimization can be
# used if you are only targeting Android 2.0 or later.)  Make sure you
# test thoroughly if you go this route.
# MODIFICATION: removed !code/simplification/arithmetic
-optimizations !code/simplification/cast,!field/*,!class/merging/*
# MODIFICATION: disable passes, it's overridden in twisterrob-buildType.pro
#-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# The remainder of this file is identical to the non-optimized version
# of the Proguard configuration file (except that the other file has
# flags to turn off optimization).

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

-keepattributes *Annotation*

# MODIFICATION: -dontnote in case no dependency on play services, allowshrinking in case it's not used
-keep,allowshrinking public class com.google.vending.licensing.ILicensingService
-dontnote com.google.vending.licensing.ILicensingService

# MODIFICATION: -dontnote in case no dependency on play services, allowshrinking in case it's not used
-keep,allowshrinking public class com.android.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService

# MODIFICATION: add IInAppBillingService similar to the above
-keep,allowshrinking public class com.android.vending.billing.IInAppBillingService
-dontnote com.android.vending.billing.IInAppBillingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
# This will result in a note, but need to keep those of ObjectAnimator.ofObject to work:
# Note: the configuration keeps the entry point '... { void setP(C); }', but not the descriptor class 'C'
# MODIFICATION: change *** to % to include only primitive types
# animating complex types is not a good idea, and likely doesn't happen often, when it does a specific keep is better
-keepclassmembers public class * extends android.view.View {
   void set*(%);
   % get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep CREATOR fields, but not force keeping the class, as fixed in https://code.google.com/p/android/issues/detail?id=175464
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**

# MODIFICATION: add -dontnote for support libs
# Note: the configuration keeps the entry point '...', but not the descriptor class '...'
-dontnote android.support.**

# Understand the @Keep support annotation.
-keep class android.support.annotation.Keep

-keep @android.support.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <init>(...);
}
