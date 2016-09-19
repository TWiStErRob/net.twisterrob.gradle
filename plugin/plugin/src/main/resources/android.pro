### -- twister-plugin-gradle/android.pro -- ###
### MODIFICATION: based on <SDK>/tools/proguard/proguard-android-optimize.txt ###
### Last diff to pull changes: 2016-09-17 ###

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
# MODIFICATION: split and explain -optimizations
#Original: !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/* 
# MODIFICATION: turn on arithmetic optimizations; it was fixed since 2.0 (API 5) http://b.android.com/28192
#-optimizations !code/simplification/arithmetic
# TODO no idea why
-optimizations !code/simplification/cast
# TODO no idea why
-optimizations !class/merging/*
# MODIFICATION: removed !field/* to enable field/removal/writeonly for logging removal (the others seem useful too)
#-optimizations !field/*
# MODIFICATION: Field value propagation has to be disabled,
# because otherwise it misses some null checks and throws RuntimeExceptions or fails to dex properly.
# DEX example (support 24.1.1, Gradle plugin 2.1.3, ProGuard 5.2.1):
# Uncaught translation error: com.android.dx.cf.code.SimException: local variable type mismatch: attempt to set or
# access a value of type int using a local variable of type android.support.design.widget.CoordinatorLayout$LayoutParams.
# This is symptomatic of .class transformation tools that ignore local variable information.
# NullPointerException example (support 24.1.1, Gradle plugin 2.1.3, ProGuard 5.2.1):
# App shows an appcompat-v7 dialog which uses AppCompatButton on Android 5.
# To get it running obfuscation had to be enabled, because otherwise the above DEX error prevented a successful build.
-optimizations !field/propagation/value
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
