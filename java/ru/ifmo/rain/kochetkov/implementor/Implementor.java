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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;


/**
 * @author Kochetkov Nikita M3234
 * Date: 18.03.2019
 */
public class Implementor implements JarImpler {
    private static final String TAB = "    ";
    private static final String SPACE = " ";
    private static final String AUTHOR = "Kochetkov Nikita";
    private static final String EOL = System.lineSeparator();

    private static class MethodWrap {
        private final Method method;
        private final static int MAGIC_CONST = 37;
        private final static int MOD = (int) (1e9 + 7);

        MethodWrap(Method other) {
            method = other;
        }

        Method getMethod() {
            return method;
        }

        @Override
        public int hashCode() {
            return (Arrays.hashCode(method.getParameterTypes())
                    + MAGIC_CONST * method.getReturnType().hashCode()
                    + 2 * MAGIC_CONST * method.getName().hashCode()) % MOD;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof MethodWrap) {
                MethodWrap other = (MethodWrap) obj;
                return Arrays.equals(method.getParameterTypes(), other.method.getParameterTypes())
                        && method.getReturnType().equals(other.method.getReturnType())
                        && method.getName().equals(other.method.getName());
            }
            return false;
        }
    }

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

    @Override
    public void implementJar(Class<?> clazz, Path jarFile) throws ImplerException {
        checkOnNull(clazz, jarFile);
        createDirectories(jarFile);
        Path tmpDir;
        try {
            tmpDir = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "temp");
            implement(clazz, tmpDir);
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            try {
                Objects.requireNonNull(compiler);
            } catch (NullPointerException e) {
                throw new ImplerException("Can't find javac for compiling", e);
            }
            String[] argsForCompiling = new String[]{"-cp",
                    tmpDir.toString() + File.pathSeparator + System.getProperty("java.class.path"),
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

    private String toUnicode(String text) {
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

    private String getTabs(final int number) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < number; i++) {
            stringBuilder.append(TAB);
        }
        return stringBuilder.toString();
    }

    private String getReturnedType(Class<?> clazz) {
        if (clazz.equals(boolean.class)) {
            return "false";
        } else if (clazz.equals(void.class)) {
            return "";
        } else if (clazz.isPrimitive()) {
            return " 0";
        }
        return "null";
    }

    private static void getAbstractMethods(Method[] methods, Set<MethodWrap> storage) {
        Arrays.stream(methods)
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .map(MethodWrap::new)
                .collect(Collectors.toCollection(() -> storage));
    }

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

    private String getMethod(Method method) {
        return new StringBuilder(TAB).append("public" + SPACE)
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

    private String getExceptions(Class<?>[] exceptionTypes) {
        if (exceptionTypes.length == 0) return "";
        return "throws " + Arrays.stream(exceptionTypes)
                .map(Class::getCanonicalName)
                .collect(Collectors.joining("," + SPACE));
    }

    private String getParams(Executable executable) {
        return Arrays.stream(executable.getParameters())
                .map(this::getParam)
                .collect(Collectors.joining("," + SPACE, "(", ")"));
    }

    private String getParam(Parameter param) {
        return param.getType().getCanonicalName() + SPACE + param.getName();
    }

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

    private String getConstructor(Constructor<?> constructor) {
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

    private String getParamsWithoutType(Constructor<?> constructor) {
        return Arrays.stream(constructor.getParameters())
                .map(Parameter::getName)
                .collect(Collectors.joining("," + SPACE, "(", ")"));
    }

    private String getPackage(Class<?> clazz) {
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

    private String getHeadClass(Class<?> clazz) {
        return new StringBuilder()
                .append("public class" + SPACE)
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

    private Path getPathToFile(Class<?> clazz, Path path, String extension) {
        return path.resolve(clazz.getPackageName().replace('.', File.separatorChar)).resolve(clazz.getSimpleName() + "Impl" + extension);
    }

    private void createDirectories(Path path) throws ImplerException {
        try {
            Files.createDirectories(Objects.requireNonNull(path.getParent()));
        } catch (IOException e) {
            throw new ImplerException("Can't create directories for output file", e);
        } catch (NullPointerException e) {
            throw new ImplerException("Path for creating are null", e);
        }
    }

    private void checkOnNull(Class<?> token, Path root) throws ImplerException {
        try {
            Objects.requireNonNull(token);
            Objects.requireNonNull(root);
        } catch (NullPointerException e) {
            throw new ImplerException("Arguments are null");
        }
    }

    private void checkOnDescent(Class<?> clazz) throws ImplerException {
        if (Modifier.isFinal(clazz.getModifiers()) || clazz == Enum.class || clazz.isArray() || clazz.isPrimitive() || clazz.isEnum()) {
            throw new ImplerException("It is final class for descent");
        }
    }

    private static boolean checkInputArguments(String[] args) throws ImplerException {
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
