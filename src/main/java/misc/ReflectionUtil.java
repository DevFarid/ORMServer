package misc;

import org.slf4j.LoggerFactory;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reflection Util class.
 * Created by SixEyes on 06/02/2024.
 */
public class ReflectionUtil {
    private static final Logger logger = Logger.getLogger(ReflectionUtil.class.getName());
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ReflectionUtil.class);

    /**
     * Get java class name from a given .java file.
     * @param javaFile the java file interested.
     * @return the java class name found within the java file.
     */
    public static String getClassName(File javaFile) {
        String fileName = javaFile.getName();
        if(javaFile.isDirectory())
            return null;
        return fileName.substring(0, fileName.length() - 5);
    }

    /**
     * Finds java files within a given directory.
     * @param directory the directory to search.
     * @return java associated files.
     */
    public static List<File> findJavaFiles(File directory) {
        List<File> javaFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
        return javaFiles;
    }

    /**
     * Compiles given java files.
     * @param javaFiles the files to compile.
     * @throws IOException Any errors occurring whilst compilation.
     */
    public static void compileJavaFiles(List<File> javaFiles) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<String> fileNames = new ArrayList<>();
        for (File javaFile : javaFiles) {
            fileNames.add(javaFile.getPath());
        }
        compiler.run(null,null,null, fileNames.toArray(new String[0]));
    }

    /**
     * Checks if the java files were compiled.
     * @param javaFiles the files to check.
     * @return true if the files were compiled, false otherwise.
     */
    public static boolean wasCompiled(List<File> javaFiles) {
        for(File f : javaFiles) {
            File classFile = new File(f.getPath().replace(".java", ".class"));
            if (!classFile.exists()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the package name from a given file.
     * @param file the file to get the package name from.
     * @return the package name found within the file.
     */
    public static String getPackage(File file) {
        String packageName = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("package")) {
                    packageName = line.substring(8, line.indexOf(';')).trim();
                    break;
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e.getCause());
        }
        return packageName;
    }

    /**
     * Load classes from java files.
     * Assumes: That java class names and the name of a java file are the same.
     * @param javaFiles the files to load classes from.
     * @return list of loaded classes. Not guaranteed to be compile-runtime-error free.
     */
    public static List<Class<?>> loadClass(List<File> javaFiles) throws MalformedURLException {
        List<Class<?>> loadedClasses = new ArrayList<>();
        for (File f : javaFiles) {
            String className = getClassName(f);
            String packagedName = String.format("%s.%s", getPackage(f), className);
            try {
                URL[] urls = { f.getParentFile().toURI().toURL() };
                try (URLClassLoader classLoader = new URLClassLoader(urls)) {
                    Class<?> clazz = classLoader.loadClass(packagedName);
                    loadedClasses.add(clazz);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e.getCause());
            }
        }
        return loadedClasses;
    }

    /**
     * Gets the declared member fields within a class.
     * @param clazz the class at interest.
     * @return Field array containing the declared fields.
     */
    public static Field[] getMemberFieldsFromClazz(Class<?> clazz) {
        return clazz.getDeclaredFields();
    }

}
