package com.github.alexvictoor.proxy;

import java.io.File;

public class FileSystemRoute {

    public final String uriPrefix;
    public final File directory;

    private FileSystemRoute(String uriPrefix, File directory) {
        this.uriPrefix = uriPrefix;
        this.directory = directory;
    }

    public static FileSystemRoute create(String uriPrefix, String path) {
        File directory = new File(path);
        if (!directory.exists() || !directory.canRead() || !directory.isDirectory()) {
            throw new RuntimeException(path + " is not a path to a readable directory");
        }
        return new FileSystemRoute(uriPrefix, directory);
    }

    public static FileSystemRoute parse(String input) {
        String[] tokens = input.split("\\|");
        String prefix = tokens[0].trim();
        String path = tokens[1].trim();
        return create(prefix, path);
    }

    public File findFile(String uri) {
        if (uri ==null || !uri.startsWith(uriPrefix)) {
            return null;
        }
        String relativePath = uri.substring(uriPrefix.length() + 1).replace('/', File.separatorChar);
        File file = new File(directory, relativePath);
        return file;
    }
}
