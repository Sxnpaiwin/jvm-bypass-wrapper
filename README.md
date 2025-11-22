# JVM Wrapper

This is a simple Java wrapper that reads a startup command from `jvm.config` and executes it.

## How to Build

1. Ensure you have Java installed.
2. Run `./gradlew build` (Linux/Mac) or `gradlew build` (Windows).
3. The executable jar will be in `build/libs/jvm-wrapper-1.0.0.jar`.

## How to Use

1. Place `jvm-wrapper-1.0.0.jar` in your server directory.
2. Create a `jvm.config` file in the same directory.
3. Add your startup command to `jvm.config`.
   Example:
   ```
   java -Xms128M -Xmx7168M -Dterminal.jline=false -Dterminal.ansi=true -jar server.jar
   ```
4. Run the wrapper:
   ```
   java -jar jvm-wrapper-1.0.0.jar
   ```

The wrapper will read the config and launch the server, inheriting the terminal I/O.

## Using a Specific JDK

If you want to run your server with a specific Java version (different from the system default), simply specify the full path to the java executable in `jvm.config`.

**Example `jvm.config` (Windows):**
```
"C:\Program Files\Java\jdk-17\bin\java.exe" -Xms128M -Xmx7168M -jar server.jar
```

**Example `jvm.config` (Linux/GraalVM):**
```
/home/container/graalvm-jdk-21.0.9+7.1/bin/java -Xms128M -Xmx7168M -Dterminal.jline=false -Dterminal.ansi=true -jar server.jar
```
