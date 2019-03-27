package ru.ifmo.rain.kochetkov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;


/**
 * Implementor class for {@link JarImpler} interface
 * <p>
 * Date: 13.03.2019
 *
 * @author Kochetkov Nikita M3234
 * @since 11
 */
@SuppressWarnings("Duplicates")
public final class Implementor implements JarImpler {
    /**
     * Tab for generated classes.
     */
    private static final String TAB = "    ";

    /**
     * Space for generated classes.
     */
    private static final String SPACE = " ";

    /**
     * Author name of class.
     */
    private static final String AUTHOR = "Kochetkov Nikita";

    /**
     * Line separator for generated classes.
     */
    private static final String EOL = System.lineSeparator();

    /**
     * Special static class for correct collisions of Methods {@link Method}
     */
    private static class MethodWrap {
        /**
         * Wpar instance of {@link Method}
         */
        private final Method method;

        /**
         * Magic const for correct collisions
         */
        private final static int MAGIC_CONST = 37;

        /**
         * Mod for prevent overflow
         */
        private final static int MOD = Integer.MAX_VALUE;

        /**
         * Construct a wrap by dint of {@link Method}
         *
         * @param method instance of {@link Method}
         */
        MethodWrap(final Method method) {
            this.method = method;
        }

        /**
         * Getter for {@link #method}
         *
         * @return wrapped instance of {@link Method}
         */
        Method getMethod() {
            return method;
        }

        /**
         * Count hashcode for {@link #method} using hash of parameters,
         * returned type and name of method
         *
         * @return hashcode for this Wrap of {@link Method}
         */
        @Override
        public int hashCode() {
            return (Arrays.hashCode(method.getParameterTypes())
                    + MAGIC_CONST * method.getReturnType().hashCode()
                    + 2 * MAGIC_CONST * method.getName().hashCode()) % MOD;
        }

        /**
         * Compare two objects. If methods have equal name, return type and parameters,
         * they will equals
         *
         * @param obj objects with whom will be compared {@link #method}
         * @return true if object is equal with  this {@link #method}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MethodWrap) {
                MethodWrap other = (MethodWrap) obj;
                return Arrays.equals(method.getParameterTypes(), other.method.getParameterTypes())
                        && method.getReturnType().equals(other.method.getReturnType())
                        && method.getName().equals(other.method.getName());
            }
            return false;
        }
    }

    /**
     * Produces code implementing class or interface specified by provided class.
     * <p>
     * For example, the implementation of the interface {@link java.util.List}
     * should go to $root/java/util/ListImpl.java
     *
     * @param clazz type class to create implementation for
     * @param root  root of directory
     * @throws ImplerException when implementation cannot be generated
     */
    @Override
    public void implement(Class<?> clazz, Path root) throws ImplerException {
        checkOnDescent(clazz);
        checkOnNull(clazz, root);
        root = getPathToFile(clazz, root, ".java");
        createDirectories(root);
        try (BufferedWriter writer = Files.newBufferedWriter(root, StandardCharsets.UTF_8)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getPackage(clazz)).
                    append(getHeadClass(clazz)).
                    append((!clazz.isInterface()) ? getConstructorsClass(clazz) : "").
                    append(getMethods(clazz)).
                    append("}");
            writer.write(toUnicode(stringBuilder.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Produces .jar file implementing class or interface specified by provided token.
     * <p>
     * During implementation creates temporary folder to store files: .java and .class files.
     * <p>
     * If program fails you will get doesn't deleted directory with files.
     * <p>
     * If program complete normal, all temporary files will be deleted along with the directory.
     *
     * @param clazz   type token to create implementation for
     * @param jarFile target .jar file
     * @throws ImplerException when implementation cannot be generated
     */
    @Override
    public void implementJar(Class<?> clazz, Path jarFile) throws ImplerException {
        checkOnNull(clazz, jarFile);
        createDirectories(jarFile);
        Path tmpDir;
        try {
            tmpDir = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "temp");
            implement(clazz, tmpDir);
            StringBuilder stringBuilder = new StringBuilder();
            String[] classPathStrings = System.getProperty("java.class.path").split(File.pathSeparator);
            for (String string : classPathStrings) {
                Path path = Paths.get(string);
                File[] files = path.toFile().listFiles();
                for (File file : files) {
                    stringBuilder.append(Paths.get(file.toURI()).toAbsolutePath().toString()).append(File.pathSeparator);
                }
            }
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            try {
                Objects.requireNonNull(compiler);
            } catch (NullPointerException e) {
                throw new ImplerException("Can't find javac for compiling", e);
            }
            String[] argsForCompiling = new String[]{"-cp",
                    tmpDir.toString() + File.pathSeparator + stringBuilder.toString(),
                    "-encoding",
                    "UTF8",
                    getPathToFile(clazz, tmpDir, ".java").toString()};
            if (compiler.run(null, null, null, argsForCompiling) != 0) {
                throw new ImplerException("Can't compile to class file");
            }

            Manifest manifest = createManifest();
            try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
                JarEntry jarAdd = new JarEntry(clazz.getName().replace('.', '/') + "Impl.class");
                jarAdd.setTime(getPathToFile(clazz, tmpDir, ".class").toFile().lastModified());
                writer.putNextEntry(jarAdd);
                Files.copy(getPathToFile(clazz, tmpDir, ".class"), writer);
            } catch (IOException e) {
                throw new ImplerException("Can't write to JAR file", e);
            }
            Files.delete(getPathToFile(clazz, tmpDir, ".java"));
            tmpDir.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new ImplerException("Can't create temp directory", e);
        }

    }

    /**
     * Create and initializing {@link Manifest} for jar archive
     *
     * @return Manifest for archiving in jar file with short info about jar
     */
    private Manifest createManifest() {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.IMPLEMENTATION_VERSION, "1.0");
        attributes.put(new Attributes.Name("Bundle-ManifestVersion"), "1.0");
        attributes.put(Attributes.Name.IMPLEMENTATION_TITLE, "Implementor");
        attributes.put(Attributes.Name.CONTENT_TYPE, "application/java-archive");
        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, AUTHOR);
        attributes.put(new Attributes.Name("Created-By"), AUTHOR);
        return manifest;
    }

    /**
     * Convert text to unicode
     *
     * @param text {@link String} to converting
     * @return converted string
     */
    private String toUnicode(final String text) {
        StringBuilder b = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 128) {
                b.append(String.format("\\u%04X", (int) c));
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    /**
     * Returns tabs, whose amount if specified by
     * Returns <em>n</em> tabs whose number equals if specified by
     *
     * @param number number of tabs
     * @return {@link String} of tabs
     */
    private String getTabs(final int number) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < number; i++) {
            stringBuilder.append(TAB);
        }
        return stringBuilder.toString();
    }

    /**
     * Extract for {@code clazz} special default type.
     *
     * <ul>
     * <li> {@link Boolean}: false</li>
     * <li> {@link Void}: ""</li>
     * <li> Primitive: 0</li>
     * <li> Other: {@code null}</li>
     * </ul>
     *
     * @param clazz {@link Class} of method
     * @return {@link String} of special type
     */
    private String getReturnedType(final Class<?> clazz) {
        if (clazz.equals(boolean.class)) {
            return "false";
        } else if (clazz.equals(void.class)) {
            return "";
        } else if (clazz.isPrimitive()) {
            return " 0";
        }
        return "null";
    }

    /**
     * Collect abstracts methods in {@code storage}.
     *
     * @param methods massive methods of class
     * @param storage {@link Set} where will put all accepted methods
     */
    private static void getAbstractMethods(Method[] methods, Set<MethodWrap> storage) {
        Arrays.stream(methods)
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .map(MethodWrap::new)
                .collect(Collectors.toCollection(() -> storage));
    }

    /**
     * Writes implementation of abstract methods of given {@link Class}.
     *
     * @param clazz base class or implemented interface
     * @return {@link String} of realized methods of {@code clazz}
     */
    private String getMethods(Class<?> clazz) {
        StringBuilder stringBuilder = new StringBuilder();
        HashSet<MethodWrap> hashSet = new HashSet<>();
        getAbstractMethods(clazz.getMethods(), hashSet);
        while (clazz != null) {
            getAbstractMethods(clazz.getDeclaredMethods(), hashSet);
            clazz = clazz.getSuperclass();
        }
        for (MethodWrap method : hashSet) {
            stringBuilder.append(getMethod(method.getMethod()));
        }
        return stringBuilder.toString();
    }

    /**
     * Build realize of method with {@link StringBuilder}.
     *
     * @param method {@link Method} of implemented class or interface
     * @return {@link String} of realized method
     */
    private String getMethod(final Method method) {
        return new StringBuilder(TAB).append("public")
                .append(SPACE)
                .append(method.getReturnType().getCanonicalName())
                .append(SPACE)
                .append(method.getName())
                .append(getParams(method))
                .append(SPACE)
                .append(getExceptions(method.getExceptionTypes()))
                .append(SPACE + "{")
                .append(EOL)
                .append(getTabs(2))
                .append("return")
                .append(SPACE)
                .append(getReturnedType(method.getReturnType()))
                .append(";")
                .append(EOL)
                .append(getTabs(1))
                .append("}")
                .append(EOL)
                .toString();
    }

    /**
     * Return list of exceptions as {@link String} created like {@code ex1, ex2, ...}.
     * <p>
     * If amount exceptions {@code 0} return "".
     *
     * @param exceptionTypes massive of {@link Class}. It's {@link Exception}.
     * @return {@link String} list of exceptions
     */
    private String getExceptions(Class<?>[] exceptionTypes) {
        if (exceptionTypes.length == 0) return "";
        return "throws " + Arrays.stream(exceptionTypes)
                .map(Class::getCanonicalName)
                .collect(Collectors.joining("," + SPACE));
    }

    /**
     * Returns list of parameters of {@link Constructor} with {@code (...)} and their types.
     *
     * @param executable {@link Executable}
     * @return {@link String} representing list of parameters
     */
    private String getParams(Executable executable) {
        return Arrays.stream(executable.getParameters())
                .map(this::getParam)
                .collect(Collectors.joining("," + SPACE, "(", ")"));
    }

    /**
     * Construct from {@link Parameter} {@code -> type name}.
     * <blockquote><pre>
     *     public void getCost(Java.lang.String arg1, Integer arg2);
     *                         ^^^^^^^^^^^^^^^^^^^^^
     *                         |||||||||||||||||||||
     * </pre></blockquote>
     *
     * @param param {@link Parameter}
     * @return {@link String} of {@code param.type + " " + param.name}
     */
    private String getParam(final Parameter param) {
        return param.getType().getCanonicalName() + SPACE + param.getName();
    }

    /**
     * Return concatenate via {@link StringBuilder} constructors of {@code clazz}.
     * They are realizing with call constructor of super class.
     *
     * @param clazz base class
     * @return {@link String} of class realizing constructors
     * @throws ImplerException if all constructors are private
     */
    private String getConstructorsClass(Class<?> clazz) throws ImplerException {
        StringBuilder stringBuilder = new StringBuilder();
        Constructor<?>[] constructors = Arrays.stream(clazz.getDeclaredConstructors()).
                filter(constructor -> !Modifier.isPrivate(constructor.getModifiers())).
                toArray(Constructor<?>[]::new);
        if (constructors.length == 0) {
            throw new ImplerException("All constructors are private");
        } else {
            for (Constructor<?> constructor : constructors) {
                stringBuilder.append(getConstructor(constructor));
            }
        }
        return stringBuilder.toString();
    }


    /**
     * Build with {@link StringBuilder} ealising of class constructor.
     *
     * @param constructor {@link Constructor}
     * @return {@link String} representing realising of class constructor
     */
    private String getConstructor(final Constructor<?> constructor) {
        return new StringBuilder(TAB).append(constructor.getDeclaringClass().getSimpleName() + "Impl")
                .append(getParams(constructor))
                .append(SPACE)
                .append(getExceptions(constructor.getExceptionTypes()))
                .append(SPACE + "{")
                .append(EOL)
                .append(getTabs(2))
                .append("super" + getParamsWithoutType(constructor))
                .append(";")
                .append(EOL)
                .append(getTabs(1))
                .append("}")
                .append(EOL)
                .toString();
    }

    /**
     * Returns list of parameters of {@link Constructor} with {@code ()} but without their types for super.
     *
     * @param constructor {@link Constructor}
     * @return {@link String} representing list of parameters
     */
    private String getParamsWithoutType(Constructor<?> constructor) {
        return Arrays.stream(constructor.getParameters())
                .map(Parameter::getName)
                .collect(Collectors.joining("," + SPACE, "(", ")"));
    }

    /**
     * Construct {@link String} package of given file.
     *
     * @param clazz class or interface for getting package
     * @return {@link String} representing package
     */
    private String getPackage(final Class<?> clazz) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!clazz.getPackage().getName().equals("")) {
            stringBuilder.append("package")
                    .append(SPACE)
                    .append(clazz.getPackage().getName())
                    .append(";")
                    .append(EOL);
        }
        stringBuilder.append(EOL);
        return stringBuilder.toString();
    }

    /**
     * Build with {@link StringBuilder} beginning declaration of the class or implemented interface.
     *
     * @param clazz base class or implemented interface
     * @return {@link String} representing beginning of class declaration
     */
    private String getHeadClass(final Class<?> clazz) {
        return new StringBuilder()
                .append("public class")
                .append(SPACE)
                .append(clazz.getSimpleName() + "Impl")
                .append(SPACE)
                .append(clazz.isInterface() ? "implements" : "extends")
                .append(SPACE)
                .append(clazz.getSimpleName())
                .append(SPACE)
                .append("{")
                .append(EOL)
                .toString();
    }

    /**
     * Return path to file, containing implementation of given class or interface.
     *
     * @param clazz     class or interface to get name
     * @param path      {@link Path} to parent directory of class
     * @param extension end file extension
     * @return {@link Path} to new file for implementing
     */
    private Path getPathToFile(final Class<?> clazz, final Path path, final String extension) {
        return path.resolve(clazz.getPackageName().replace('.', File.separatorChar)).resolve(clazz.getSimpleName() + "Impl" + extension);
    }

    /**
     * Create nested directories.
     *
     * @param path {@link Path} where create directories
     * @throws ImplerException if error occurred during creating of directories
     */
    private void createDirectories(final Path path) throws ImplerException {
        try {
            Files.createDirectories(Objects.requireNonNull(path.getParent()));
        } catch (IOException e) {
            throw new ImplerException("Can't create directories for output file", e);
        } catch (NullPointerException e) {
            throw new ImplerException("Path for creating are null", e);
        }
    }

    /**
     * Check {@code token and root} on nullable through method of {@link Objects#requireNonNull(Object)}.
     *
     * @param token base class or implemented interface
     * @param root  root of directory
     * @throws ImplerException if {@code token and root} are null
     */
    private void checkOnNull(final Class<?> token, final Path root) throws ImplerException {
        try {
            Objects.requireNonNull(token);
            Objects.requireNonNull(root);
        } catch (NullPointerException e) {
            throw new ImplerException("Arguments are null");
        }
    }

    /**
     * Check {@code clazz} on final modificator.
     *
     * @param clazz base class or implemented interface
     * @throws ImplerException throw if class is final
     */
    private void checkOnDescent(final Class<?> clazz) throws ImplerException {
        if (Modifier.isFinal(clazz.getModifiers()) || clazz == Enum.class || clazz.isArray() || clazz.isPrimitive() || clazz.isEnum()) {
            throw new ImplerException("It is final class for descent");
        }
    }

    /**
     * Return true if all arguments are amount {@code 2} and they are not null.
     * <p>
     * Return false if all arguments are amount {@code 3} and they are not null.
     * <p>
     * In other situation throw {@link ImplerException}.
     *
     * @param args {@link String} massive
     * @return true if two arguments or false if they are tree
     * @throws ImplerException if some arguments are not corrected
     */
    private static boolean checkInputArguments(final String[] args) throws ImplerException {
        if (args == null) {
            throw new ImplerException("Arguments are not initialized");
        }
        if (args.length == 2) {
            for (String arg : args) {
                if (arg == null) {
                    throw new ImplerException("Arguments are not all initialized for just implementing");
                }
            }
            return true;
        } else if (args.length == 3) {
            for (String arg : args) {
                if (arg == null) {
                    throw new ImplerException("Arguments are not all initialized for jar implementing");
                }
            }
            return false;
        }
        throw new ImplerException("Arguments are not enough initialized");
    }

    /**
     * This function choose which way of implementation to execute.
     * <p>
     * Work {@link Implementor} in two possible ways:
     * <ul>
     * <li> 2 arguments: className rootPath - runs {@link #implement(Class, Path)} with first and second arguments</li>
     * <li> 3 arguments: -jar className jarPath - runs {@link #implementJar(Class, Path)} with second and third arguments</li>
     * </ul>
     * If arguments are incorrect or you have {@link ImplerException} during working, you will get correct end with information in error stream.
     * <p>
     * Here are some more examples of how strings can be used:
     * <blockquote><pre>
     *     java ru.ifmo.rain.kochetkov.implementor.Implementor outputDirectory/dev/
     *     java -jar ru.ifmo.rain.kochetkov.implementor.Implementor outputDirectory/dev/impler.jar
     * </pre></blockquote>
     *
     * @param args massive of {@link String}
     */
    public static void main(String[] args) {
        Implementor implementor = new Implementor();
        try {
            if (checkInputArguments(args)) {
                implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
            } else {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            }
        } catch (ImplerException e) {
            System.err.println("Error occurred during implementation: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Class name not found: " + e.getMessage());
        }
    }
}