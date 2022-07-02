package util;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.ClassPath ;
import com.google.common.reflect.ClassPath.ClassInfo;
import java.io.IOException;
import java.util.Objects;
import java.util.HashSet;

public class IsolatedClassLoader extends ClassLoader {
    private static ImmutableMap<String, ClassInfo> classPaths;

    public static ImmutableMap<String, ClassInfo> getClassPaths() throws IOException {
        if (IsolatedClassLoader.classPaths == null){
            var classes = ClassPath.from(
                            ClassLoader.getSystemClassLoader())
                    .getTopLevelClasses();

            var b = new ImmutableMap.Builder<String, ClassInfo>();

            for(var c : classes){
                b.put(c.getName(), c);
            }

            IsolatedClassLoader.classPaths = b.build();
        }

        return IsolatedClassLoader.classPaths;
    }

    public static IsolatedClassLoader getClassLoader() throws IOException{
        var cp = IsolatedClassLoader.getClassPaths();
        var classLoader = new IsolatedClassLoader();
        return classLoader;
    }

    private final HashSet<String> packages = new HashSet<>();

    public IsolatedClassLoader() {
        super(ClassLoader.getSystemClassLoader());
        packages.add("tla2sany");
        packages.add("pcal");
        packages.add("util");
        packages.add("tla2tex");
        packages.add("tlc2");
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> loadedClass = this.findLoadedClass(name);
        
        boolean load_isolated = false;

        for (String p : this.packages) {
            if(name.contains(p)){
                load_isolated = true;
            }
        }

        if(!load_isolated) {
            Class<?> lc = super.loadClass(name, resolve);
            return lc;
        }

        if(!Objects.isNull(loadedClass)){
            return loadedClass;
        }

        if (loadedClass == null && classPaths.containsKey(name)){
            var classInfo = classPaths.get(name);
            var byteSource = classInfo.asByteSource();
            byte[] bytes;
            try {
                bytes = byteSource.read();
            } catch (IOException e) {
                throw new ClassNotFoundException();
            }

            var c = defineClass(name, bytes, 0, bytes.length);

            if(resolve){
                resolveClass(c);
            }

            return c;
        }

        return super.loadClass(name, resolve);
    }
}