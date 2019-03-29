### 源码导入
1. 下载源码 [disruptor Release 3.3.2](https://github.com/LMAX-Exchange/disruptor/releases/tag/3.3.2)
1. 解压并将其转换成 maven 项目，参考 [Gradle、Maven项目相互转换](https://notes.doublemine.me/2017-08-21-Gradle%E3%80%81Maven%E9%A1%B9%E7%9B%AE%E7%9B%B8%E4%BA%92%E8%BD%AC%E6%8D%A2.html)
	- 执行 `gradle install`
	- 依据错误信息，编辑 `build.gradle` 文件，移除 `dependencies` 中的本地依赖 `perfCompile 'org.hdrhistogram:HdrHistogram:1.2.1'`，再次执行 `gradle install`
	- 依据错误信息，编辑 `build.gradle` 文件，移除 `tasks.withType(Compile) {...}` 整段配置，再次执行 `gradle install`
	-  编译通过，得到 `build/poms/pom-default.xml`，将其移动到根目录并重命名 `mv build/poms/pom-default.xml ./pom.xml` 即可
	-  修改 `pom.xml` 中的 `<artifactId>disruptor</artifactId>` 为 `<artifactId>disruptor-source</artifactId>`，用于区别公共 maven 仓库中的 jar 包
	-  删除一些不必要的文件
1. 执行 `mvn install` 可以将下载的源码打包到本地 maven 仓库中，这里直接导入 IDEA，Module 名使用 `disruptor-source`，修改 `disruptor-example` 的 `pom.xml` 文件，使其使用导入的源码
1. 修改 `disruptor-source` 的 `pom.xml`，添加依赖
    ```xml
    <!-- https://mvnrepository.com/artifact/org.hdrhistogram/HdrHistogram -->
    <dependency>
        <groupId>org.hdrhistogram</groupId>
        <artifactId>HdrHistogram</artifactId>
        <version>2.1.11</version>
    </dependency>
    ```
1. 修改 `disruptor-source` 的 `pom.xml`，指定 Java 版本
    ```xml
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
    </properties>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
    ```