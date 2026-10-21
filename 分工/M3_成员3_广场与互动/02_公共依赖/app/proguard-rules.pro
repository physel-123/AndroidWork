# 节气民俗 App 混淆规则
#
# 本项目不依赖反射，Room 实体与 DAO 由注解处理器生成，均有对应的 keep 规则。
# 保留行号便于线上崩溃定位。

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room 生成的实现类
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**
