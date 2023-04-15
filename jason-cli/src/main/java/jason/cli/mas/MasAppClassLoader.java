package jason.cli.mas;

import jason.cli.JasonCLI;

import java.io.*;
import java.net.URL;
import java.util.List;

class MasAppClassLoader extends ClassLoader {

    private List<String> paths;

    public MasAppClassLoader(ClassLoader parent, List<String> paths) {

        super(parent);
        this.paths = paths;
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        //System.out.println("Loading Class '" + name + "' ");

        // CLILocalMAS must be loaded by this loader, so that classes latter loaded from it use this loader
        if (name.equals(CLILocalMAS.class.getName())) {
            Class<?> c = getJasonCLIClass();
            // force this class to be assigned with this loader
            resolveClass(c);
            return c;
        }

        Class<?> c = null;
        try {
            c = super.loadClass(name);
            if (c != null)
                return c;
        } catch (Exception e) {
            // ok, super does not solve
        }

        if (c == null) { // c still null
            //System.out.println("looking for  " + name);
            c = getAppClass(name);
            if (c != null) {
                resolveClass(c);
                return c;
            }
        }

        return null;
    }

    private Class getJasonCLIClass() throws ClassNotFoundException {
        try {
            //var file = new SourcePath().fixPath("$jason/jason/cli/JasonCLI.class");
            var file = //"jar:file:/Users/jomi/pro/jason-cli/build/libs/jason-cli-1.0-SNAPSHOT.jar!/jason/cli/mas/CLILocalMAS.class";
                    "jar:"+
                            JasonCLI.class.getProtectionDomain().getCodeSource().getLocation()+"!/"+
                            CLILocalMAS.class.getName().replace('.',File.separatorChar) + ".class";
            var b = loadClassData(new URL(file).openStream());
            if (b != null) {
                return defineClass(CLILocalMAS.class.getName(), b, 0, b.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Class getAppClass(String name) throws ClassNotFoundException {
        // TODO: consider proper application classpath
        // .:bin/classes:build/classes/java/main:project classpath:*lib

        for (String path: paths) {
            try {
                if (!path.isEmpty() && !path.endsWith("/"))
                    path += "/";
                String file = path + name.replace('.', File.separatorChar) + ".class";
                //System.out.println("try to load class "+name+" from "+file);
                // This loads the byte code data from the file
                var b = loadClassData(new FileInputStream(file));
                if (b != null) {
                    return defineClass(name, b, 0, b.length);
                }
            } catch (IOException e) {
                //e.printStackTrace();
                // ignore, tries next path
            }
        }
        return null;
    }

    private byte[] loadClassData(InputStream stream) throws IOException {
        if (stream == null)
            return null;
        int size = stream.available();
        byte buff[] = new byte[size];
        DataInputStream in = new DataInputStream(stream);
        in.readFully(buff);
        in.close();
        return buff;
    }

    @Override
    public String toString() {
        return "Jason Application Class Loader ---" + super.toString();
    }
}
