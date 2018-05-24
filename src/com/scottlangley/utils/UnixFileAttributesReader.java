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
import java.nio.file.attribute.FileTime;

/**
 * Utility class for reading the file attributes available via the Java NIO package plus the UNIX-specific
 * attributes available from the UnixFileAttributes internal JDK class.
 *
 * @see <a href="https://github.com/openjdk-mirror/jdk7u-jdk/blob/master/src/solaris/classes/sun/nio/fs/UnixFileAttributes.java">https://github.com/openjdk-mirror/jdk7u-jdk/blob/master/src/solaris/classes/sun/nio/fs/UnixFileAttributes.java</a>
 *
 * @author Scott Langley (https://github.com/selangley)
 */
@SuppressWarnings("unchecked")
public class UnixFileAttributesReader extends FileAttributesReader {

	// UNIX File Attributes
	// Defined inside sun.nio.fs.UnixFileAttributes

	private Integer mode;
	private Long rdev;
	private Integer nlink;
	private FileTime ctime;
	private Boolean isDevice;

	// Setup reflection for accessing the non-public methods the internal JDK class.
	private static String UNIX_FILE_ATTRIBUTES_CLASSNAME = "sun.nio.fs.UnixFileAttributes";
	private static String UNIX_FILE_ATTRIBUTES_MODE_METHOD = "mode";
	private static String UNIX_FILE_ATTRIBUTES_RDEV_METHOD = "rdev";
	private static String UNIX_FILE_ATTRIBUTES_NLINK_METHOD = "nlink";
	private static String UNIX_FILE_ATTRIBUTES_CTIME_METHOD = "ctime";
	private static String UNIX_FILE_ATTRIBUTES_IS_DEVICE_METHOD = "isDevice";

	@SuppressWarnings("rawtypes")
	private static Class UnixFileAttributesClass;
	private static Method modeMethod;
	private static Method rdevMethod;
	private static Method nlinkMethod;
	private static Method ctimeMethod;
	private static Method isDeviceMethod;
	private static boolean illegalReflectionAllowed = true;

	static {
		try {
			UnixFileAttributesClass = Class.forName(UNIX_FILE_ATTRIBUTES_CLASSNAME);
			modeMethod = UnixFileAttributesClass.getDeclaredMethod(UNIX_FILE_ATTRIBUTES_MODE_METHOD);
			modeMethod.setAccessible(true);
			rdevMethod = UnixFileAttributesClass.getDeclaredMethod(UNIX_FILE_ATTRIBUTES_RDEV_METHOD);
			rdevMethod.setAccessible(true);
			nlinkMethod = UnixFileAttributesClass.getDeclaredMethod(UNIX_FILE_ATTRIBUTES_NLINK_METHOD);
			nlinkMethod.setAccessible(true);
			ctimeMethod = UnixFileAttributesClass.getDeclaredMethod(UNIX_FILE_ATTRIBUTES_CTIME_METHOD);
			ctimeMethod.setAccessible(true);
			isDeviceMethod = UnixFileAttributesClass.getDeclaredMethod(UNIX_FILE_ATTRIBUTES_IS_DEVICE_METHOD);
			isDeviceMethod.setAccessible(true);

		} catch (Exception e) {
			illegalReflectionAllowed = false;
		}

	}

	/**
	 * Instantiates a new unix file attributes reader.
	 *
	 * @param file
	 *            the file
	 */
	public UnixFileAttributesReader(File file) {
		this(file.toPath());
	}

	/**
	 * Instantiates a new unix file attributes reader.
	 *
	 * @param path
	 *            the path
	 */
	public UnixFileAttributesReader(Path path) {
		super(path);
		processUnixFileAttributes();
	}

	/**
	 * Gets the mode.
	 *
	 * @return the mode
	 */
	public Integer getMode() {
		return mode;
	}

	/**
	 * Gets the device type.
	 *
	 * @return the rdev
	 */
	public Long getRdev() {
		return rdev;
	}

	/**
	 * Gets the number of hard links to the file.
	 *
	 * @return the nlink
	 */
	public Integer getNlink() {
		return nlink;
	}

	/**
	 * Gets the ctime.
	 *
	 * @return the ctime
	 */
	public FileTime getCtime() {
		return ctime;
	}

	/**
	 * Checks if file is a device file.
	 *
	 * @return the boolean
	 */
	public Boolean isDevice() {
		return isDevice;
	}

	private void processUnixFileAttributes() {
		mode = null;
		rdev = null;
		nlink = null;
		ctime = null;
		isDevice = null;

		if (illegalReflectionAllowed) {
			try {
				mode = ((Integer) modeMethod.invoke(posixFileAttributes)).intValue();
				rdev = ((Long) rdevMethod.invoke(posixFileAttributes)).longValue();
				nlink = ((Integer) nlinkMethod.invoke(posixFileAttributes)).intValue();
				ctime = ((FileTime) ctimeMethod.invoke(posixFileAttributes));
				isDevice = ((Boolean) isDeviceMethod.invoke(posixFileAttributes));
			} catch (IllegalAccessException | InvocationTargetException e) {
				illegalReflectionAllowed = false;
			}
		}
	}
}
