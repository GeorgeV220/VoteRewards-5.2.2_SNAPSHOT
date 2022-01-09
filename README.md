# VoteRewards

Vote Rewards is a simple plugin to reward your players when they vote for your server. Vote Rewards contains many
features like daily vote rewards and voteparty. I promise that the Vote Rewards will not cause problems to your server
and if you have a bug you can report it to the Discussion section, send me a private message or open an issue here

I uploaded the source code so that everyone can make their own modifications

You can use the source code to do whatever you want but do not upload sell it or upload it without my permission (except
github)

# Adding VoteRewards as a dependency to your build system

### Maven

You can have your project depend on VoteRewards as a dependency through the following code snippets:

```xml

<project>
    <repositories>
        <repository>
            <id>georgev22</id>
            <url>https://maven.georgev22.com/repository/georgev22/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>com.georgev22</groupId>
            <artifactId>voterewards</artifactId>
            <version>5.2.1</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

### Gradle

You can include VoteRewards into your gradle project using the following lines:

```groovy
repositories {
    maven {
        url 'https://maven.georgev22.com/repository/georgev22/'
    }
}

dependencies {
    compileOnly "com.georgev22:voterewards:5.2.1"
}
```

# Building VoteRewards

VoteRewards can be built by running the following: `mvn package`. The resultant jar is built and written
to `target/voterewards-{version}.jar`.

The build directories can be cleaned instead using the `mvn clean` command.

If you want to clean (install) and build the plugin use `mvn clean package` (or `mvn clean install package`) command.

# Contributing

VoteRewards is an open source `GNU General Public License v3.0` licensed project. I accept contributions through pull
requests, and will make sure to credit you for your awesome contribution.
