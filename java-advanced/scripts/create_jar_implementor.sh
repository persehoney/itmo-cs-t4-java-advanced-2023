javac -cp ../../java-advanced-2023/artifacts/info.kgeorgiy.java.advanced.implementor.jar ../java-solutions/info/kgeorgiy/ja/petrova/implementor/Implementor.java
jar -cfm Implementor.jar MANIFEST.MF -C ../java-solutions info/kgeorgiy/ja/petrova/implementor/Implementor.class
java -jar Implementor.jar arg1 arg2