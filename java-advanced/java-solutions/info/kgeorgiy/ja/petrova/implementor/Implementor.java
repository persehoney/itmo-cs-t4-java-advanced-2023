package info.kgeorgiy.ja.petrova.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * <p> Homework for <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 *
 * @author Petrova Ksenia
 */
public class Implementor implements Impler, JarImpler {
    /**
     * Line separator.
     */
    private final String NEWLINE = System.lineSeparator();

    /**
     * Default constructor.
     */
    public Implementor() {
    }

    /**
     * Implements given interface.
     *
     * <p> Checks whether some arguments missing and implements interface otherwise.
     * Chooses correct mode of implementation: if {@code path} is jar file name, method also creates jar file
     * of implementing class.
     *
     * @param args {@code token} - an interface to implement and
     *              {@code path} - depending on mode: if simple - root directory of implementing class,
     *                          if jar - jar file name to create
     * @throws ImplerException if implementation error occurred
     */
    public static void main(String[] args) throws ImplerException {
        if (args.length == 2 && args[0] != null && args[1] != null) {
            try {
                Class<?> token = Class.forName(args[0]);
                Path path = Path.of(args[1]);
                if (!path.toString().contains("jar")) {
                    Impler impler = new Implementor();
                    impler.implement(token, path);
                } else {
                    JarImpler jarImpler = new Implementor();
                    jarImpler.implementJar(token, path);
                }
            } catch (ImplerException e) {
                throw new ImplerException(e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new ImplerException("Given token isn't a class");
            }
        } else {
            throw new ImplerException("Illegal arguments");
        }
    }

    /**
     * Implements given interface.
     *
     * <p> Generates java file of class implementing given interface and puts it in root directory.
     * Generated class has name of interface + {@code "Impl.java"}. Implementation of interface's methods includes
     * return of default value only.
     *
     * @param token an interface to implement
     * @param root  root directory of implementing class
     * @throws ImplerException if implementation error occurred
     * @see #implementJar(Class, Path)
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        checkAccess(token);

        Path path = getPath(token, root);
        createDirectories(path);

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(generateHeader(token));
            writer.write(" {");
            writer.write(generateMethods(token));
            writer.write("}");
        } catch (IOException e) {
            throw new ImplerException("IOException occurred");
        }
    }

    /**
     * Checks if given class is available to implement.
     *
     * <p> Doesn't throw any exceptions if given class is an interface and isn't private.
     *
     * @param token the class to check access to
     * @throws ImplerException if given class is not an interface or has private modifier
     */
    private void checkAccess(Class<?> token) throws ImplerException {
        if (!token.isInterface())
            throw new ImplerException("Not an interface");
        if (Modifier.isPrivate(token.getModifiers()))
            throw new ImplerException("Interface isn't accessible");
    }

    /**
     * Generates header of given class.
     *
     * <p> Header includes package name, modifier (public by default), class name and implemented interface.
     *
     * @param token the class to generate header for
     * @return header
     * @see #generateMethods(Class)
     */
    private String generateHeader(Class<?> token) {
        return "package " +
                token.getPackageName() +
                ";" +
                NEWLINE +
                NEWLINE +
                "public class " +
                token.getSimpleName() + "Impl" +
                " implements " + getCorrectInterfaceName(token.getName());
    }

    /**
     * Generates correct name of implemented interface.
     *
     * <p> Replaces dollar signs with dots in case interface is local.
     *
     * @param name name of the interface
     * @return correct name of the interface
     */
    private String getCorrectInterfaceName(String name) {
        return name.replace('$', '.');
    }

    /**
     * Generates methods of given class.
     *
     * <p> Generates headers and bodies of implemented methods. Note: generates non-static methods only.
     *
     * @param token the class to generate methods for
     * @return methods of given class
     * @see #generateHeader(Class)
     * @see #generateMethod(Method)
     */
    public String generateMethods(Class<?> token) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Method method : token.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                stringBuilder.append(NEWLINE)
                        .append(generateMethod(method));
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Generates header and body of given method.
     *
     * <p> Header includes @Override tag, modifier (public by default), return type, method's name and parameters.
     * Body includes return of default value only. Default values are different for each return type
     * (more: {@link #getDefaultValue(Method)}).
     *
     * @param method method to generate implementation for
     * @return method's implementation
     * @see #generateMethods(Class)
     * @see #generateMethodHeader(Method)
     * @see #getDefaultValue(Method)
     */
    private String generateMethod(Method method) {
        return generateMethodHeader(method) +
                "        return" +
                getDefaultValue(method) +
                ";" +
                NEWLINE +
                "    }" +
                NEWLINE;
    }

    /**
     * Generates header of given method.
     *
     * <p> Header includes @Override tag, modifier (public by default), return type, method's name and parameters.
     *
     * @param method method to generate header for
     * @return method's header
     * @see #generateMethod(Method)
     * @see #generateMethods(Class)
     * @see #generateHeader(Class)
     */
    private String generateMethodHeader(Method method) {
        return "    @Override" +
                NEWLINE +
                "    public " +
                getReturnType(method) +
                " " +
                method.getName() +
                getParameters(method) +
                " {" +
                NEWLINE;
    }

    /**
     * Returns return type of given method.
     *
     * @param method method to get return type of
     * @return method's return type
     * @see Method#getReturnType()
     */
    private String getReturnType(Method method) {
        return method.getReturnType().getTypeName();
    }

    /**
     * Generates parameters of given method.
     *
     * <p> Each parameter has its type and argument name. Argument name is {@code "arg" + i}, where i - number of the parameter,
     * starting with 0
     *
     * @param method method to generate parameters for
     * @return method's parameters
     * @see #generateMethod(Method)
     */
    private String getParameters(Method method) {
        AtomicInteger i = new AtomicInteger();
        return Arrays.stream(method.getParameterTypes())
                .map(p -> getCorrectInterfaceName((p.getTypeName())) + " arg" + i.getAndIncrement())
                .collect(Collectors.joining(", ", "(", ")"));
    }

    /**
     * Returns default value of method's return type.
     *
     * @param method method to get default value of
     * @return default value of method's return type
     * @see #generateMethod(Method)
     * @see #generateMethodHeader(Method)
     */
    private String getDefaultValue(Method method) {
        Class<?> type = method.getReturnType();
        if (type == boolean.class) {
            return " false";
        } else if (type == void.class) {
            return " ";
        } else if (type.isPrimitive()) {
            return " 0";
        } else {
            return " null";
        }
    }

    /**
     * Returns correct class name of given class.
     *
     * <p> Constructs full class name and replaces dots with slashes.
     *
     * @param token class to get correct class name of
     * @return correct class name of given class
     */
    private String getClassName(Class<?> token) {
        return (token.getPackageName() + "/" + token.getSimpleName()).replace('.', '/');
    }

    /**
     * Generates path to the java file of class, implementing given interface.
     *
     * @param token implemented interface
     * @param root  root directory of class, implementing token
     * @return path to the class, implementing given interface
     * @see #getClassName(Class)
     * @see #getClassPath(Class)
     */
    private Path getPath(Class<?> token, Path root) {
        return Path.of(root + "/" + getClassName(token) + "Impl.java");
    }

    /**
     * Generates path to the compiled file of class, implementing given interface.
     *
     * @param token implemented interface
     * @return path to the compiled file of class, implementing given interface
     * @see #getClassName(Class)
     * @see #getPath(Class, Path)
     */
    private String getClassPath(Class<?> token) {
        return getClassName(token) + "Impl.class";
    }

    /**
     * Creates all nonexistent parent directories from given path.
     *
     * <p> Checks whether file with given path exists and creates missing directories otherwise.
     *
     * @param path path to create parent directories from
     * @throws ImplerException if an I/O error occurs
     * @see Files#createDirectories(Path, FileAttribute[])
     * @see #getClassPath(Class)
     * @see #getPath(Class, Path)
     */
    private void createDirectories(Path path) throws ImplerException {
        if (!path.toFile().exists()) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException("Cannot create directories");
            }
        }
    }

    /**
     * Implements given interface and creates jar file of implementing class.
     *
     * <p> Generates java file of class implementing given interface.
     * Generated class has name of interface + {@code "Impl.java"}. Implementation of interface's methods includes
     * return of default value only.
     *
     * <p> Compiles implementing class, creates new jar file and puts compiled file into jar.
     *
     * @param token   an interface to implement
     * @param jarFile jar file name to create
     * @throws ImplerException if implementation error occurred
     * @see #implement(Class, Path)
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path root = jarFile.getParent();
        Path path = getPath(token, root);

        implement(token, root);
        compileFiles(path, token);
        makeJar(getClassPath(token), jarFile, root);
    }

    /**
     * Creates compiled file, of the file located by given path.
     *
     * @param path  path to file to compile
     * @param token class to create classpath from
     * @throws ImplerException if compilation isn't successful
     */
    public void compileFiles(final Path path, Class<?> token) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final String classpath = getClasspath(token).toString();
        String[] args = new String[]{path.toString(), "-cp", classpath, "-encoding", StandardCharsets.UTF_8.name()};
        final int exitCode = compiler.run(null, null, null, args);
        if (exitCode == 1) {
            throw new ImplerException("Cannot compile class");
        }
    }

    /**
     * Returns classpath of given class.
     *
     * @param token class to return classpath of
     * @return classpath of given class
     * @throws ImplerException if given URL is not formatted strictly according to
     *                         RFC2396 and cannot be converted to a URI
     */
    private Path getClasspath(Class<?> token) throws ImplerException {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new ImplerException("Cannot create classpath");
        }
    }

    /**
     * Creates jar file of given class.
     *
     * <p> Creates new manifest, jar file and copies given class file to jar.
     *
     * @param className name of the class to make a jar of
     * @param jarFile   name of jar to create
     * @param root      root directory of jar to create
     * @throws ImplerException if an I/O error occurs
     */
    private void makeJar(String className, Path jarFile, Path root) throws ImplerException {
        Manifest manifest = new Manifest();
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            jarOutputStream.putNextEntry(new ZipEntry(className));
            Files.copy(root.resolve(className), jarOutputStream);
        } catch (IOException e) {
            throw new ImplerException("Cannot create Jar file");
        }
    }
}