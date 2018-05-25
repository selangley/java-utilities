package com.scottlangley.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

/**
 * Interface for accessing all file attributes available, even those requiring reflection to internal JDK classes. If the given attribute is not available for a particular operating system, the value of the attribute is returned as null.
 */
public interface DeepFileAttributesReader {

    /**
     * Create the most specific File Attributes Reader possible for the detected operating system with access to OS-specific file attributes.
     *
     * @param file
     * @return
     */
    public static DeepFileAttributesReader createOsSpecificReader(File file) {
        return createOsSpecificReader(file.toPath());
    }

    /**
     * Create the most specific File Attributes Reader possible for the detected operating system with access to OS-specific file attributes.
     *
     * @param path
     * @return
     */
    public static DeepFileAttributesReader createOsSpecificReader(Path path) {
        if (FileAttributesReader.IS_WINDOWS) {
            return new WindowsFileAttributesReader(path);
        } else {
            return new UnixFileAttributesReader(path);
        }
    }

    default String getPosixFilePermissionsAsString() {
        return null;
    }

    default String getPosixFilePermissionsInNumericForm() {
        return null;
    }

    default String getDev() {
        return null;
    }

    default String getInode() {
        return null;
    }

    default FileTime getLastModifiedTime() {
        return null;
    }

    default FileTime getLastAccessTime() {
        return null;
    }

    default FileTime getCreationTime() {
        return null;
    }

    default Long getLastModifiedTimeSec() {
        return null;
    }

    default Long getLastAccessTimeSec() {
        return null;
    }

    default Long getCreationTimeSec() {
        return null;
    }

    default Boolean isRegularFile() {
        return null;
    }

    default Boolean isDirectory() {
        return null;
    }

    // UNIX-specific
    default Integer getMode() {
        return null;
    }

    default Long getRdev() {
        return null;
    }

    default Integer getNlink() {
        return null;
    }

    default FileTime getCtime() {
        return null;
    }

    default Boolean isDevice() {
        return null;
    }

    // Windows-specific

    default Integer getAttributes() {
        return null;
    }

    default Integer getVolSerialNumber() {
        return null;
    }

    default Integer getFileIndexHigh() {
        return null;
    }

    default Integer getFileIndexLow() {
        return null;
    }

    default Boolean isReparsePoint() {
        return null;
    }

    default Boolean isDirectoryLink() {
        return null;
    }

}
