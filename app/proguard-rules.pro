# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/ywen/Documents/Android/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-optimizationpasses 5  # 通过指定数量的优化能执行

-dontusemixedcaseclassnames    #混淆时不会产生形形色色的类名
-dontskipnonpubliclibraryclasses  #指定不去忽略非公共的库类

#<!--more-->
# -dontpreverify    不预校验
-ignorewarnings
-verbose    #输出生成信息

#optimizations  {optimization_filter}     优化选项
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*


-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keepclasseswithmembernames class * {
    native <methods>;
}

#webview js
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

#common
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# modify 修改合并
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

#--------------------------
# 保护类型   -keepattributes 说明
# Exceptions, Signature, Deprecated, SourceFile, SourceDir, LineNumberTable, LocalVariableTable,
# LocalVariableTypeTable, Synthetic, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations,
# RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations, and AnnotationDefault
# --------------------
-keepattributes **

-dontskipnonpubliclibraryclassmembers
-dontshrink    #不压缩指定的文件

-dontoptimize    #不优化指定的文件

#  -dontobfuscate    #不混淆指定的文件

# ----- 混淆包路径 -------
-repackageclasses ''
-flattenpackagehierarchy ''
-target 1.6

-dontwarn com.classpackage.AA    #打包时忽略以下类的警告

#-libraryjars libs/android-logging-log4j-1.0.3.jar
#-libraryjars libs/android-support-v4.jar
#-libraryjars libs/android-async-http-1.4.6.jar
#-libraryjars libs/log4j-1.2.17.jar
#-libraryjars libs/protobuf-java-2.6.1.jar
#-libraryjars libs/netty-buffer-4.0.24.Final.jar
#-libraryjars libs/netty-codec-4.0.24.Final.jar
#-libraryjars libs/netty-common-4.0.24.Final.jar
#-libraryjars libs/netty-transport-4.0.24.Final.jar
#-libraryjars libs/eventbus-2.4.0.jar
#-libraryjars libs/gson-2.2.4.jar
#-libraryjars libs/umeng-analytics-v5.4.2.jar
#-libraryjars libs/universal-image-loader-1.9.3.jar

#保护三方的jar包不被混淆  
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class com.pando.core.bean.* { *; }
-keepclassmembernames class com.pando.core.bean.* { *; }
-keep class com.google.protobuf.** { *; }
-keep public class * extends com.google.protobuf.** { *; }
-dontwarn org.apache.log4j.**
-dontwarn de.mindpipe.android.logging.log4j.**
-keep class org.apache.log4j.** { *;}
-keep class de.mindpipe.android.logging.log4j.** { *; }

#关闭javax等错误警告  
-dontwarn javax.**
-dontwarn java.awt.**
-dontwarn com.sun.jdmk.comm.**

#umeng统计
-keepclassmembers class * {
    public <init>(org.json.JSONObject);
}
-keep public class com.pando.outlet.R$*{
    public static final int *;
}


# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

-dontwarn io.netty.**

# Get rid of warnings about unreachable but unused classes referred to by Netty
-dontwarn org.jboss.netty.**

# Needed by commons logging
-keep class org.apache.commons.logging.* {*;}

#Some Factory that seemed to be pruned
-keep class java.util.concurrent.atomic.AtomicReferenceFieldUpdater {*;}
-keep class java.util.concurrent.atomic.AtomicReferenceFieldUpdaterImpl{*;}

-keepclassmembers class * implements java.io.Serializable {
	static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#pando code proguard
-keep class com.pandocloud.freeiot.ui.bean.** { *; }