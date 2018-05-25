/*
 * Copyright 2018 Scott Langley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.scottlangley.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;

/**
 * Utility class for reading the file attributes available via the Java NIO package plus the Windows-specific
 * attributes available from the WindowsFileAttributes internal JDK class.
 *
 * @author Scott Langley (https://github.com/selangley)
 * @see <a href="https://github.com/frohoff/jdk8u-jdk/blob/master/src/windows/classes/sun/nio/fs/WindowsFileAttributes.java">https://github.com/frohoff/jdk8u-jdk/blob/master/src/windows/classes/sun/nio/fs/WindowsFileAttributes.java</a>
 */
@SuppressWarnings("unchecked")
public class WindowsFileAttributesReader extends FileAttributesReader implements DeepFileAttributesReader {

    private Integer attributes;
    private Integer volSerialNumber;
    private Integer fileIndexHigh;
    private Integer fileIndexLow;
    private Boolean isReparsePoint;
    private Boolean isDirectoryLink;

    // Setup reflection for accessing the non-public methods the internal JDK class.
    private static String WINDOWS_FILE_ATTRIBUTES_CLASSNAME = "sun.nio.fs.WindowsFileAttributes";
    private static String WINDOWS_FILE_ATTRIBUTES_ATTRIBUTES_METHOD = "attributes";
    private static String WINDOWS_FILE_ATTRIBUTES_VOL_SERIAL_NUMBER_METHOD = "volSerialNumber";
    private static String WINDOWS_FILE_ATTRIBUTES_FILE_INDEX_LOW_METHOD = "fileIndexLow";
    private static String WINDOWS_FILE_ATTRIBUTES_FILE_INDEX_HIGH_METHOD = "fileIndexHigh";
    private static String WINDOWS_FILE_ATTRIBUTES_IS_REPARSE_POINT_METHOD = "isReparsePoint";
    private static String WINDOWS_FILE_ATTRIBUTES_IS_DIRECTORY_LINK_METHOD = "isDirectoryLink";

    @SuppressWarnings("rawtypes")
    private static Class WindowsFileAttributesClass;
    private static Method attributesMethod;
    private static Method volSerialNumberMethod;
    private static Method fileIndexHighMethod;
    private static Method fileIndexLowMethod;
    private static Method isReparsePointMethod;
    private static Method isDirectoryLinkMethod;
    private static boolean illegalReflectionAllowed = true;

    static {
        try {
            WindowsFileAttributesClass = Class.forName(WINDOWS_FILE_ATTRIBUTES_CLASSNAME);
            attributesMethod = WindowsFileAttributesClass.getDeclaredMethod(WINDOWS_FILE_ATTRIBUTES_ATTRIBUTES_METHOD);
            attributesMethod.setAccessible(true);
            volSerialNumberMethod = WindowsFileAttributesClass.getDeclaredMethod(WINDOWS_FILE_ATTRIBUTES_VOL_SERIAL_NUMBER_METHOD);
            volSerialNumberMethod.setAccessible(true);
            fileIndexHighMethod = WindowsFileAttributesClass.getDeclaredMethod(WINDOWS_FILE_ATTRIBUTES_FILE_INDEX_HIGH_METHOD);
            fileIndexHighMethod.setAccessible(true);
            fileIndexLowMethod = WindowsFileAttributesClass.getDeclaredMethod(WINDOWS_FILE_ATTRIBUTES_FILE_INDEX_LOW_METHOD);
            fileIndexLowMethod.setAccessible(true);
            isReparsePointMethod = WindowsFileAttributesClass.getDeclaredMethod(WINDOWS_FILE_ATTRIBUTES_IS_REPARSE_POINT_METHOD);
            isReparsePointMethod.setAccessible(true);
            isDirectoryLinkMethod = WindowsFileAttributesClass.getDeclaredMethod(WINDOWS_FILE_ATTRIBUTES_IS_DIRECTORY_LINK_METHOD);
            isDirectoryLinkMethod.setAccessible(true);

        } catch (Exception e) {
            illegalReflectionAllowed = false;
        }

    }


    /**
     * Instantiates a new windows file attributes reader.
     *
     * @param file the file
     */
    public WindowsFileAttributesReader(File file) {
        this(file.toPath());
    }

    /**
     * Instantiates a new windows file attributes reader.
     *
     * @param path the path
     */
    public WindowsFileAttributesReader(Path path) {
        super(path);
        processWindowsFileAttributes();
    }

    /**
     * Gets the attributes.
     *
     * @return the attributes
     */

    @Override
    public Integer getAttributes() {
        return attributes;
    }

    /**
     * Gets the Volume Serial Number.
     *
     * @return the Volume Serial Number
     */
    @Override
    public Integer getVolSerialNumber() {
        return volSerialNumber;
    }

    /**
     * Gets the file index high.
     *
     * @return the file index high
     */
    @Override
    public Integer getFileIndexHigh() {
        return fileIndexHigh;
    }

    /**
     * Gets the file index low.
     *
     * @return the file index low
     */
    @Override
    public Integer getFileIndexLow() {
        return fileIndexLow;
    }

    /**
     * Checks if is reparse point.
     *
     * @return the boolean
     */
    @Override
    public Boolean isReparsePoint() {
        return isReparsePoint;
    }

    /**
     * Checks if is directory link.
     *
     * @return the boolean
     */
    @Override
    public Boolean isDirectoryLink() {
        return isDirectoryLink;
    }

    private void processWindowsFileAttributes() {
        attributes = null;
        volSerialNumber = null;
        fileIndexHigh = null;
        fileIndexLow = null;
        isReparsePoint = null;
        isDirectoryLink = null;

        if (illegalReflectionAllowed) {
            try {
                attributes = (Integer) attributesMethod.invoke(dosFileAttributes);
                volSerialNumber = (Integer) volSerialNumberMethod.invoke(dosFileAttributes);
                fileIndexHigh = (Integer) fileIndexHighMethod.invoke(dosFileAttributes);
                fileIndexLow = (Integer) fileIndexLowMethod.invoke(dosFileAttributes);
                isReparsePoint = (Boolean) isReparsePointMethod.invoke(dosFileAttributes);
                isDirectoryLink = (Boolean) isDirectoryLinkMethod.invoke(dosFileAttributes);
            } catch (IllegalAccessException | InvocationTargetException e) {
                illegalReflectionAllowed = false;
            }
        }
    }

}
