### 爱零工任务相关的SDK

#### 集成方式
algtasklibrary通过集成module的方式接入工程。以下主要说明通过Android Studio导入的方法：

1，打开你的 AS 项目 → File → New → Import Module → 选择或输入 EaseUI 库路径 → Next → Next → Finish。

2，在工程层级下的 settings.gradle文件中注册module，即在后面添加上 ':algtasklibrary',然后sync now

3,在App层级的build.gradle 文件中，dependencies {}中添加

    dependencies {
        *******
        implementation 'com.jakewharton:butterknife:8.5.1'
        annotationProcessor 'com.jakewharton:butterknife-compiler:8.5.1'
        implementation project(':algtasklibrary')
    }

3，如果遇到 "Plugin with id 'com.jakewharton.butterknife' not found" 报错，在工程层级 build.gradle文件中，
    butterknife比较特殊，除了放在library的gradle里面，还需要在app的gradle里面也要加进去，才可以两边都可以使用
    dependencies {
        classpath 'com.android.tools.build:gradle:3.1.0'
        classpath 'com.jakewharton:butterknife-gradle-plugin:8.4.0' ### 添加这一条
    }
    在app层级的build.gradle文件中添加
    dependencies {
        ****
        implementation 'com.jakewharton:butterknife:8.5.1'
        annotationProcessor 'com.jakewharton:butterknife-compiler:8.5.1'
    }
    然后sync now

4，如果遇到 "com.android.builder.dexing.DexArchiveBuilderException"
    App层级的build.gradle文件中，android{}下添加
    compileOptions {

            sourceCompatibility JavaVersion.VERSION_1_8

            targetCompatibility JavaVersion.VERSION_1_8

        }
        sync now

#### 使用方式

1，在工程的Application中对sdk进行初始化

    SliceApp.getInstance().init(this);

2，在需要打开爱零工任务流程的地方

    SliceApp.getInstance().openAlg();

    进入到爱零工的任务列表，然后进行抢单-做单-提交等一系列操作。

3,如果遇到sdk初始化报错的话，比如 "java.lang.UnsatisfiedLinkError: No implementation found for int com.baidu.mapsdkplatform.comjni.tools.JNITools.initClass"
    需要工程支持ndk，在工程层级的 gradle.properties 文件中添加
    android.useDeprecatedNdk=true
    然后在app层级的build.gradle文件中，defaultConfog{}中添加
    ndk {
        abiFilters "x86"
        abiFilters "armeabi"
    }，
    然后sync now


