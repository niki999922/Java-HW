package ru.ifmo.rain.kochetkov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Kochetkov Nikita M3234
 * Date: 18.03.2019
 */
public class Implementor implements Impler {
    private static final String TAB = "    ";
    private static final String SPACE = " ";
    private static final String EOL = System.lineSeparator();   // next line

    @Override
    public void implement(Class<?> clazz, Path root) throws ImplerException {
        checkOnDescent(clazz);
        checkOnNull(clazz, root);
        root = getPathToFile(clazz, root);
        createDirectories(root);
        try (BufferedWriter writer = Files.newBufferedWriter(root, StandardCharsets.UTF_8)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getPackage(clazz)).
                    append(getHeadClass(clazz)).
                    append((!clazz.isInterface()) ? getConstructorsClass(clazz) : "").
                    append(getMetods(clazz)).
                    append("}");
            writer.write(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class MethodWrap {
        private final Method metod;
        private final static int MAGIK_CONST = 37;
        private final static int MOD = (int) (1e9 + 7);

        MethodWrap(Method other) {
            metod = other;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof MethodWrap) {
                MethodWrap other = (MethodWrap) obj;
                return Arrays.equals(metod.getParameterTypes(), other.metod.getParameterTypes())
                        && metod.getReturnType().equals(other.metod.getReturnType())
                        && metod.getName().equals(other.metod.getName());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (Arrays.hashCode(metod.getParameterTypes())
                    + MAGIK_CONST * metod.getReturnType().hashCode()
                    + 2 * MAGIK_CONST * metod.getName().hashCode()) % MOD;
        }

        Method getMetod() {
            return metod;
        }
    }


    private static void getAbstractMethods(Method[] methods, Set<MethodWrap> storage) {
        Arrays.stream(methods)
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .map(MethodWrap::new)
                .collect(Collectors.toCollection(() -> storage));
    }

    private String getMetods(Class<?> clazz) {
        StringBuilder stringBuilder = new StringBuilder();
        HashSet<MethodWrap> hashSet = new HashSet<>();
        getAbstractMethods(clazz.getMethods(), hashSet);
        while (clazz != null) {
            getAbstractMethods(clazz.getDeclaredMethods(), hashSet);
            clazz = clazz.getSuperclass();
        }
        for (MethodWrap method : hashSet) {
            stringBuilder.append(getMetod(method.getMetod()));
        }
        return stringBuilder.toString();
    }

    private String getTabs(final int number) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < number; i++) {
            stringBuilder.append(TAB);
        }
        return stringBuilder.toString();
    }

    private String getMetod(Method method) {
        StringBuilder stringBuilder = new StringBuilder(TAB);
        stringBuilder.append("public" + SPACE).
                append(method.getReturnType().getCanonicalName()).
                append(SPACE).
                append(method.getName()).
                append(getParams(method)).
                append(SPACE).
                append(getExceptions(method.getExceptionTypes())).
                append(SPACE + "{").
                append(EOL).
                append(getTabs(2)).
                append("return").
                append(SPACE).
                append(getReturnedType(method.getReturnType())).
                append(";").
                append(EOL).
                append(getTabs(1)).
                append("}").
                append(EOL);
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


    private String getExcept(Class<?> method) {
        return method.getSimpleName();
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
        StringBuilder stringBuilder = new StringBuilder(TAB);
        stringBuilder.append(constructor.getDeclaringClass().getSimpleName() + "Impl").
                append(getParams(constructor)).
                append(SPACE).
                append(getExceptions(constructor.getExceptionTypes())).
                append(SPACE + "{").
                append(EOL).
                append(getTabs(2)).
                append("super" + getParamsWithoutType(constructor)).
                append(";").
                append(EOL).
                append(getTabs(1)).
                append("}").
                append(EOL);
        return stringBuilder.toString();

    }

    private String getParamsWithoutType(Constructor<?> constructor) {
        return Arrays.stream(constructor.getParameters())
                .map(Parameter::getName)
                .collect(Collectors.joining("," + SPACE, "(", ")"));
    }

    private String createDefaultConstructor(Class<?> clazz) {
        return new StringBuilder().append(clazz.getSimpleName()).
                append("() {}").
                append(EOL).toString();
    }

    private String getPackage(Class<?> clazz) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!clazz.getPackage().getName().equals("")) {
            stringBuilder.append("package" + SPACE).
                    append(clazz.getPackage().getName()).
                    append(";").
                    append(EOL);
        }
        stringBuilder.append(EOL);
        return stringBuilder.toString();
    }

    private String getHeadClass(Class<?> clazz) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("public class" + SPACE).
                append(clazz.getSimpleName() + "Impl").
                append(SPACE).
                append(clazz.isInterface() ? "implements" : "extends").
                append(SPACE).
                append(clazz.getSimpleName()).
                append(SPACE).
                append("{").
                append(EOL);
        return stringBuilder.toString();
    }


    private Path getPathToFile(Class<?> clazz, Path path) {
        return path.resolve(clazz.getPackageName().replace('.', File.separatorChar)).resolve(clazz.getSimpleName() + "Impl.java");
    }

    private void createDirectories(Path path) throws ImplerException {
        try {
            Files.createDirectories(Objects.requireNonNull(path.getParent()));
        } catch (IOException e) {
            throw new ImplerException("Can't create directories for output file", e);
        } catch (NullPointerException e) {
            throw new ImplerException("Path for creating are null.", e);
        }
    }


    private void checkOnNull(Class<?> token, Path root) throws ImplerException {
        try {
            Objects.requireNonNull(token);
            Objects.requireNonNull(root);
        } catch (NullPointerException e) {
            throw new ImplerException("Arguments are null.");
        }
    }

    private void checkOnDescent(Class<?> clazz) throws ImplerException {
        if (Modifier.isFinal(clazz.getModifiers()) || clazz == Enum.class || clazz.isArray() || clazz.isPrimitive() || clazz.isEnum()) {
            throw new ImplerException("It is final class for descent.");
        }
    }

    private static void checkInputArguments(String[] args) throws ImplerException {
        if (args == null) {
            throw new ImplerException("Arguments are not initialized.");
        }
        if (args.length == 2) {
            for (String arg : args) {
                if (arg == null) {
                    throw new ImplerException("Arguments are not all initialized for just implementing.");
                }
            }
            return;
        } else if (args.length == 3) {
            for (String arg : args) {
                if (arg == null) {
                    throw new ImplerException("Arguments are not all initialized for just jar implementing.");
                }
            }
            return;
        }
        throw new ImplerException("Arguments are not enough initialized.");
    }

    public static void main(String[] args) {
        try {
            checkInputArguments(args);
            Implementor implementor = new Implementor();
            implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
        } catch (ImplerException e) {
            System.err.println("Error occurred during implementation: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Class name not found: " + e.getMessage());
        }
    }
}
