# mysql-connector-java 5.1.x 内部引用了 Android 上不存在的 JDK 类（JNDI、JMX 等），
# 开启混淆时需要忽略这些警告，并保留驱动类不被移除
-dontwarn com.mysql.**
-dontwarn javax.naming.**
-dontwarn javax.management.**
-dontwarn java.beans.**
-keep class com.mysql.** { *; }
-keep class com.example.shopapp.entity.** { *; }
-keep class com.example.shopapp.vo.** { *; }
-keep class com.example.shopapp.req.** { *; }
