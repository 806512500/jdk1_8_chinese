
/*
 * Copyright (c) 2000, 2018, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.util.prefs;

import java.util.Map;
import java.util.TreeMap;
import java.util.StringTokenizer;
import java.io.ByteArrayOutputStream;
import sun.util.logging.PlatformLogger;

/**
 * 基于 Windows 注册表的 <tt>Preferences</tt> 实现。
 * <tt>Preferences</tt> 的 <tt>systemRoot</tt> 和 <tt>userRoot</tt> 分别存储在
 * <tt>HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Prefs</tt> 和
 * <tt>HKEY_CURRENT_USER\Software\JavaSoft\Prefs</tt> 中。
 *
 * @author  Konstantin Kladko
 * @see Preferences
 * @see PreferencesFactory
 * @since 1.4
 */

class WindowsPreferences extends AbstractPreferences{

    /**
     * 用于记录错误信息的日志器
     */
    private static PlatformLogger logger;

    /**
     * Windows 注册表路径，指向 <tt>Preferences</tt> 的根节点。
     */
    private static final byte[] WINDOWS_ROOT_PATH =
        stringToByteArray("Software\\JavaSoft\\Prefs");

    /**
     * Windows 注册表中 <tt>HKEY_CURRENT_USER</tt> 和 <tt>HKEY_LOCAL_MACHINE</tt> 的句柄。
     */
    private static final int HKEY_CURRENT_USER = 0x80000001;
    private static final int HKEY_LOCAL_MACHINE = 0x80000002;

    /**
     * <tt>Preferences</tt> 用户根节点的挂载点。
     */
    private static final int USER_ROOT_NATIVE_HANDLE = HKEY_CURRENT_USER;

    /**
     * <tt>Preferences</tt> 系统根节点的挂载点。
     */
    private static final int SYSTEM_ROOT_NATIVE_HANDLE = HKEY_LOCAL_MACHINE;

    /**
     * Windows 本地函数的最大字节编码路径长度，不包括结尾的 <tt>null</tt> 字符。
     */
    private static final int MAX_WINDOWS_PATH_LENGTH = 256;

    /**
     * 用户根节点。
     */
    private static volatile Preferences userRoot;

    static Preferences getUserRoot() {
        Preferences root = userRoot;
        if (root == null) {
            synchronized (WindowsPreferences.class) {
                root = userRoot;
                if (root == null) {
                    root = new WindowsPreferences(USER_ROOT_NATIVE_HANDLE, WINDOWS_ROOT_PATH);
                    userRoot = root;
                }
            }
        }
        return root;
    }

    /**
     * 系统根节点。
     */
    private static volatile Preferences systemRoot;

    static Preferences getSystemRoot() {
        Preferences root = systemRoot;
        if (root == null) {
            synchronized (WindowsPreferences.class) {
                root = systemRoot;
                if (root == null) {
                    root = new WindowsPreferences(SYSTEM_ROOT_NATIVE_HANDLE, WINDOWS_ROOT_PATH);
                    systemRoot = root;
                }
            }
        }
        return root;
    }

    /* Windows 错误代码。 */
    private static final int ERROR_SUCCESS = 0;
    private static final int ERROR_FILE_NOT_FOUND = 2;
    private static final int ERROR_ACCESS_DENIED = 5;

    /* 用于解释本地函数返回值的常量 */
    private static final int NATIVE_HANDLE = 0;
    private static final int ERROR_CODE = 1;
    private static final int SUBKEYS_NUMBER = 0;
    private static final int VALUES_NUMBER = 2;
    private static final int MAX_KEY_LENGTH = 3;
    private static final int MAX_VALUE_NAME_LENGTH = 4;
    private static final int DISPOSITION = 2;
    private static final int REG_CREATED_NEW_KEY = 1;
    private static final int REG_OPENED_EXISTING_KEY = 2;
    private static final int NULL_NATIVE_HANDLE = 0;

    /* Windows 安全掩码 */
    private static final int DELETE = 0x10000;
    private static final int KEY_QUERY_VALUE = 1;
    private static final int KEY_SET_VALUE = 2;
    private static final int KEY_CREATE_SUB_KEY = 4;
    private static final int KEY_ENUMERATE_SUB_KEYS = 8;
    private static final int KEY_READ = 0x20019;
    private static final int KEY_WRITE = 0x20006;
    private static final int KEY_ALL_ACCESS = 0xf003f;

    /**
     * 初始的注册表访问尝试间隔时间，以毫秒为单位。每次失败的尝试（第一次除外）后，时间会翻倍。
     */
    private static int INIT_SLEEP_TIME = 50;

    /**
     * 注册表访问的最大尝试次数。
     */
    private static int MAX_ATTEMPTS = 5;

    /**
     * 后端存储的可用性标志。
     */
    private boolean isBackingStoreAvailable = true;

    /**
     * Windows 注册表 API RegOpenKey() 的 Java 封装。
     */
    private static native int[] WindowsRegOpenKey(int hKey, byte[] subKey,
                                                  int securityMask);
    /**
     * 在放弃之前尝试 RegOpenKey() MAX_ATTEMPTS 次。
     */
    private static int[] WindowsRegOpenKey1(int hKey, byte[] subKey,
                                            int securityMask) {
        int[] result = WindowsRegOpenKey(hKey, subKey, securityMask);
        if (result[ERROR_CODE] == ERROR_SUCCESS) {
            return result;
        } else if (result[ERROR_CODE] == ERROR_FILE_NOT_FOUND) {
            logger().warning("Trying to recreate Windows registry node " +
            byteArrayToString(subKey) + " at root 0x" +
            Integer.toHexString(hKey) + ".");
            // 尝试重新创建
            int handle = WindowsRegCreateKeyEx(hKey, subKey)[NATIVE_HANDLE];
            WindowsRegCloseKey(handle);
            return WindowsRegOpenKey(hKey, subKey, securityMask);
        } else if (result[ERROR_CODE] != ERROR_ACCESS_DENIED) {
            long sleepTime = INIT_SLEEP_TIME;
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                    return result;
                }
                sleepTime *= 2;
                result = WindowsRegOpenKey(hKey, subKey, securityMask);
                if (result[ERROR_CODE] == ERROR_SUCCESS) {
                    return result;
                }
            }
        }
        return result;
    }

                 /**
     * Java包装器，用于Windows注册表API RegCloseKey()
     */
    private static native int WindowsRegCloseKey(int hKey);

    /**
     * Java包装器，用于Windows注册表API RegCreateKeyEx()
     */
    private static native int[] WindowsRegCreateKeyEx(int hKey, byte[] subKey);

    /**
     * 在放弃之前重试RegCreateKeyEx() MAX_ATTEMPTS次。
     */
    private static int[] WindowsRegCreateKeyEx1(int hKey, byte[] subKey) {
        int[] result = WindowsRegCreateKeyEx(hKey, subKey);
        if (result[ERROR_CODE] == ERROR_SUCCESS) {
            return result;
        } else {
            long sleepTime = INIT_SLEEP_TIME;
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                    return result;
                }
                sleepTime *= 2;
                result = WindowsRegCreateKeyEx(hKey, subKey);
                if (result[ERROR_CODE] == ERROR_SUCCESS) {
                    return result;
                }
            }
        }
        return result;
    }
    /**
     * Java包装器，用于Windows注册表API RegDeleteKey()
     */
    private static native int WindowsRegDeleteKey(int hKey, byte[] subKey);

    /**
     * Java包装器，用于Windows注册表API RegFlushKey()
     */
    private static native int WindowsRegFlushKey(int hKey);

    /**
     * 在放弃之前重试RegFlushKey() MAX_ATTEMPTS次。
     */
    private static int WindowsRegFlushKey1(int hKey) {
        int result = WindowsRegFlushKey(hKey);
        if (result == ERROR_SUCCESS) {
            return result;
        } else {
            long sleepTime = INIT_SLEEP_TIME;
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                    return result;
                }
                sleepTime *= 2;
                result = WindowsRegFlushKey(hKey);
                if (result == ERROR_SUCCESS) {
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * Java包装器，用于Windows注册表API RegQueryValueEx()
     */
    private static native byte[] WindowsRegQueryValueEx(int hKey,
                                                        byte[] valueName);
    /**
     * Java包装器，用于Windows注册表API RegSetValueEx()
     */
    private static native int WindowsRegSetValueEx(int hKey, byte[] valueName,
                                                   byte[] value);
    /**
     * 在放弃之前重试RegSetValueEx() MAX_ATTEMPTS次。
     */
    private static int WindowsRegSetValueEx1(int hKey, byte[] valueName,
                                             byte[] value) {
        int result = WindowsRegSetValueEx(hKey, valueName, value);
        if (result == ERROR_SUCCESS) {
            return result;
        } else {
            long sleepTime = INIT_SLEEP_TIME;
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                    return result;
                }
                sleepTime *= 2;
                result = WindowsRegSetValueEx(hKey, valueName, value);
                if (result == ERROR_SUCCESS) {
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * Java包装器，用于Windows注册表API RegDeleteValue()
     */
    private static native int WindowsRegDeleteValue(int hKey, byte[] valueName);

    /**
     * Java包装器，用于Windows注册表API RegQueryInfoKey()
     */
    private static native int[] WindowsRegQueryInfoKey(int hKey);

    /**
     * 在放弃之前重试RegQueryInfoKey() MAX_ATTEMPTS次。
     */
    private static int[] WindowsRegQueryInfoKey1(int hKey) {
        int[] result = WindowsRegQueryInfoKey(hKey);
        if (result[ERROR_CODE] == ERROR_SUCCESS) {
            return result;
        } else {
            long sleepTime = INIT_SLEEP_TIME;
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                    return result;
                }
                sleepTime *= 2;
                result = WindowsRegQueryInfoKey(hKey);
                if (result[ERROR_CODE] == ERROR_SUCCESS) {
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * Java包装器，用于Windows注册表API RegEnumKeyEx()
     */
    private static native byte[] WindowsRegEnumKeyEx(int hKey, int subKeyIndex,
                                                     int maxKeyLength);

    /**
     * 在放弃之前重试RegEnumKeyEx() MAX_ATTEMPTS次。
     */
    private static byte[] WindowsRegEnumKeyEx1(int hKey, int subKeyIndex,
                                               int maxKeyLength) {
        byte[] result = WindowsRegEnumKeyEx(hKey, subKeyIndex, maxKeyLength);
        if (result != null) {
            return result;
        } else {
            long sleepTime = INIT_SLEEP_TIME;
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                    return result;
                }
                sleepTime *= 2;
                result = WindowsRegEnumKeyEx(hKey, subKeyIndex, maxKeyLength);
                if (result != null) {
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * Java包装器，用于Windows注册表API RegEnumValue()
     */
    private static native byte[] WindowsRegEnumValue(int hKey, int valueIndex,
                                                     int maxValueNameLength);
    /**
     * 在放弃之前重试RegEnumValueEx() MAX_ATTEMPTS次。
     */
    private static byte[] WindowsRegEnumValue1(int hKey, int valueIndex,
                                               int maxValueNameLength) {
        byte[] result = WindowsRegEnumValue(hKey, valueIndex,
                                            maxValueNameLength);
        if (result != null) {
            return result;
        } else {
            long sleepTime = INIT_SLEEP_TIME;
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException e) {
                    return result;
                }
                sleepTime *= 2;
                result = WindowsRegEnumValue(hKey, valueIndex,
                                             maxValueNameLength);
                if (result != null) {
                    return result;
                }
            }
        }
        return result;
    }


                /**
     * 构造一个 <tt>WindowsPreferences</tt> 节点，创建底层的 Windows 注册表节点及其所有 Windows 父节点（如果它们尚未创建）。
     * 如果 Windows 注册表不可用，则记录警告消息。
     */
    private WindowsPreferences(WindowsPreferences parent, String name) {
        super(parent, name);
        int parentNativeHandle = parent.openKey(KEY_CREATE_SUB_KEY, KEY_READ);
        if (parentNativeHandle == NULL_NATIVE_HANDLE) {
            // 如果在这里，openKey 失败并记录
            isBackingStoreAvailable = false;
            return;
        }
        int[] result =
               WindowsRegCreateKeyEx1(parentNativeHandle, toWindowsName(name));
        if (result[ERROR_CODE] != ERROR_SUCCESS) {
            logger().warning("无法创建 Windows 注册表节点 " +
                    byteArrayToString(windowsAbsolutePath()) +
                    " 在根 0x" + Integer.toHexString(rootNativeHandle()) +
                    ". Windows RegCreateKeyEx(...) 返回错误代码 " +
                    result[ERROR_CODE] + ".");
            isBackingStoreAvailable = false;
            return;
        }
        newNode = (result[DISPOSITION] == REG_CREATED_NEW_KEY);
        closeKey(parentNativeHandle);
        closeKey(result[NATIVE_HANDLE]);
    }

    /**
     * 构造一个根节点，创建底层的 Windows 注册表节点及其所有父节点（如果它们尚未创建）。
     * 如果 Windows 注册表不可用，则记录警告消息。
     * @param rootNativeHandle 一个指向 Windows 顶级键的本地句柄。
     * @param rootDirectory 作为字节编码字符串的根目录路径。
     */
    private  WindowsPreferences(int rootNativeHandle, byte[] rootDirectory) {
        super(null, "");
        int[] result =
                WindowsRegCreateKeyEx1(rootNativeHandle, rootDirectory);
        if (result[ERROR_CODE] != ERROR_SUCCESS) {
            logger().warning("无法打开/创建偏好设置根节点 " +
                    byteArrayToString(windowsAbsolutePath()) +
                    " 在根 0x" + Integer.toHexString(rootNativeHandle()) +
                    ". Windows RegCreateKeyEx(...) 返回错误代码 " +
                    result[ERROR_CODE] + ".");
            isBackingStoreAvailable = false;
            return;
        }
        // 检查是否为新节点
        newNode = (result[DISPOSITION] == REG_CREATED_NEW_KEY);
        closeKey(result[NATIVE_HANDLE]);
    }

    /**
     * 返回当前节点的 Windows 绝对路径作为字节数组。
     * Java 的 "/" 分隔符转换为 Windows 的 "\"。
     * @see Preferences#absolutePath()
     */
    private byte[] windowsAbsolutePath() {
        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
        bstream.write(WINDOWS_ROOT_PATH, 0, WINDOWS_ROOT_PATH.length-1);
        StringTokenizer tokenizer = new StringTokenizer(absolutePath(), "/");
        while (tokenizer.hasMoreTokens()) {
            bstream.write((byte)'\\');
            String nextName = tokenizer.nextToken();
            byte[] windowsNextName = toWindowsName(nextName);
            bstream.write(windowsNextName, 0, windowsNextName.length-1);
        }
        bstream.write(0);
        return bstream.toByteArray();
    }

    /**
     * 使用给定的安全掩码打开当前节点的底层 Windows 注册表键。
     * @param securityMask Windows 安全掩码。
     * @return Windows 注册表键的句柄。
     * @see #openKey(byte[], int)
     * @see #openKey(int, byte[], int)
     * @see #closeKey(int)
     */
    private int openKey(int securityMask) {
        return openKey(securityMask, securityMask);
    }

    /**
     * 使用给定的安全掩码打开当前节点的底层 Windows 注册表键。
     * @param mask1 首选的 Windows 安全掩码。
     * @param mask2 备用的 Windows 安全掩码。
     * @return Windows 注册表键的句柄。
     * @see #openKey(byte[], int)
     * @see #openKey(int, byte[], int)
     * @see #closeKey(int)
     */
    private int openKey(int mask1, int mask2) {
        return openKey(windowsAbsolutePath(), mask1,  mask2);
    }

     /**
     * 使用给定的安全掩码打开给定绝对路径的 Windows 注册表键。
     * @param windowsAbsolutePath 作为字节编码字符串的键的 Windows 绝对路径。
     * @param mask1 首选的 Windows 安全掩码。
     * @param mask2 备用的 Windows 安全掩码。
     * @return Windows 注册表键的句柄。
     * @see #openKey(int)
     * @see #openKey(int, byte[],int)
     * @see #closeKey(int)
     */
    private int openKey(byte[] windowsAbsolutePath, int mask1, int mask2) {
        /*  检查键的路径是否足够短，可以一次打开
            否则使用路径分割过程 */
        if (windowsAbsolutePath.length <= MAX_WINDOWS_PATH_LENGTH + 1) {
            int[] result = WindowsRegOpenKey1(rootNativeHandle(),
                                              windowsAbsolutePath, mask1);
            if (result[ERROR_CODE] == ERROR_ACCESS_DENIED && mask2 != mask1)
                result = WindowsRegOpenKey1(rootNativeHandle(),
                                            windowsAbsolutePath, mask2);

            if (result[ERROR_CODE] != ERROR_SUCCESS) {
                logger().warning("无法打开 Windows 注册表节点 " +
                        byteArrayToString(windowsAbsolutePath()) +
                        " 在根 0x" +
                        Integer.toHexString(rootNativeHandle()) +
                        ". Windows RegOpenKey(...) 返回错误代码 " +
                        result[ERROR_CODE] + ".");
                result[NATIVE_HANDLE] = NULL_NATIVE_HANDLE;
                if (result[ERROR_CODE] == ERROR_ACCESS_DENIED) {
                    throw new SecurityException(
                            "无法打开 Windows 注册表节点 " +
                            byteArrayToString(windowsAbsolutePath()) +
                            " 在根 0x" +
                            Integer.toHexString(rootNativeHandle()) +
                            ": 访问被拒绝");
                }
            }
            return result[NATIVE_HANDLE];
        } else {
            return openKey(rootNativeHandle(), windowsAbsolutePath, mask1, mask2);
        }
    }

                 /**
     * 打开给定相对路径的 Windows 注册表项
     * 与给定的 Windows 注册表项相对。
     * @param windowsAbsolutePath Windows 注册表项的相对路径，作为字节编码的字符串。
     * @param nativeHandle 基础 Windows 注册表项的句柄。
     * @param mask1 优先的 Windows 安全掩码。
     * @param mask2 备用的 Windows 安全掩码。
     * @return Windows 注册表项的句柄。
     * @see #openKey(int)
     * @see #openKey(byte[],int)
     * @see #closeKey(int)
     */
    private int openKey(int nativeHandle, byte[] windowsRelativePath,
                        int mask1, int mask2) {
    /* 如果路径足够短则立即打开。否则分割路径 */
        if (windowsRelativePath.length <= MAX_WINDOWS_PATH_LENGTH + 1 ) {
            int[] result = WindowsRegOpenKey1(nativeHandle,
                                              windowsRelativePath, mask1);
            if (result[ERROR_CODE] == ERROR_ACCESS_DENIED && mask2 != mask1)
                result = WindowsRegOpenKey1(nativeHandle,
                                            windowsRelativePath, mask2);

            if (result[ERROR_CODE] != ERROR_SUCCESS) {
                logger().warning("无法打开 Windows 注册表节点 " +
                        byteArrayToString(windowsAbsolutePath()) +
                        " 在根 0x" + Integer.toHexString(nativeHandle) +
                        ". Windows RegOpenKey(...) 返回错误代码 " +
                        result[ERROR_CODE] + ".");
                result[NATIVE_HANDLE] = NULL_NATIVE_HANDLE;
            }
            return result[NATIVE_HANDLE];
        } else {
            int separatorPosition = -1;
            // 贪婪算法 - 打开尽可能长的路径
            for (int i = MAX_WINDOWS_PATH_LENGTH; i > 0; i--) {
                if (windowsRelativePath[i] == ((byte)'\\')) {
                    separatorPosition = i;
                    break;
                }
            }
            // 分割路径并递归处理
            byte[] nextRelativeRoot = new byte[separatorPosition+1];
            System.arraycopy(windowsRelativePath, 0, nextRelativeRoot,0,
                                                      separatorPosition);
            nextRelativeRoot[separatorPosition] = 0;
            byte[] nextRelativePath = new byte[windowsRelativePath.length -
                                      separatorPosition - 1];
            System.arraycopy(windowsRelativePath, separatorPosition+1,
                             nextRelativePath, 0, nextRelativePath.length);
            int nextNativeHandle = openKey(nativeHandle, nextRelativeRoot,
                                           mask1, mask2);
            if (nextNativeHandle == NULL_NATIVE_HANDLE) {
                return NULL_NATIVE_HANDLE;
            }
            int result = openKey(nextNativeHandle, nextRelativePath,
                                 mask1,mask2);
            closeKey(nextNativeHandle);
            return result;
        }
    }

     /**
     * 关闭 Windows 注册表项。
     * 如果 Windows 注册表不可用，则记录警告。
     * @param key 的 Windows 注册表句柄。
     * @see #openKey(int)
     * @see #openKey(byte[],int)
     * @see #openKey(int, byte[],int)
    */
    private void closeKey(int nativeHandle) {
        int result = WindowsRegCloseKey(nativeHandle);
        if (result != ERROR_SUCCESS) {
            logger().warning("无法关闭 Windows 注册表节点 " +
                    byteArrayToString(windowsAbsolutePath()) +
                    " 在根 0x" +
                    Integer.toHexString(rootNativeHandle()) +
                    ". Windows RegCloseKey(...) 返回错误代码 " +
                    result + ".");
        }
    }

     /**
     * 实现 <tt>AbstractPreferences</tt> <tt>putSpi()</tt> 方法。
     * 将名称-值对放入底层的 Windows 注册表节点中。
     * 如果 Windows 注册表不可用，则记录警告。
     * @see #getSpi(String)
     */
    protected void putSpi(String javaName, String value) {
        int nativeHandle = openKey(KEY_SET_VALUE);
        if (nativeHandle == NULL_NATIVE_HANDLE) {
            isBackingStoreAvailable = false;
            return;
        }
        int result = WindowsRegSetValueEx1(nativeHandle,
                toWindowsName(javaName), toWindowsValueString(value));
        if (result != ERROR_SUCCESS) {
            logger().warning("无法为键赋值 " +
                    byteArrayToString(toWindowsName(javaName)) +
                    " 在 Windows 注册表节点 " +
                    byteArrayToString(windowsAbsolutePath()) +
                    " 在根 0x" +
                    Integer.toHexString(rootNativeHandle()) +
                    ". Windows RegSetValueEx(...) 返回错误代码 " +
                    result + ".");
            isBackingStoreAvailable = false;
        }
        closeKey(nativeHandle);
    }

    /**
     * 实现 <tt>AbstractPreferences</tt> <tt>getSpi()</tt> 方法。
     * 从底层的 Windows 注册表节点获取字符串值。
     * 如果 Windows 注册表不可用，则记录警告。
     * @see #putSpi(String, String)
     */
    protected String getSpi(String javaName) {
    int nativeHandle = openKey(KEY_QUERY_VALUE);
    if (nativeHandle == NULL_NATIVE_HANDLE) {
        return null;
    }
    Object resultObject =  WindowsRegQueryValueEx(nativeHandle,
                                                  toWindowsName(javaName));
    if (resultObject == null) {
        closeKey(nativeHandle);
        return null;
    }
    closeKey(nativeHandle);
    return toJavaValueString((byte[]) resultObject);
    }

    /**
     * 实现 <tt>AbstractPreferences</tt> <tt>removeSpi()</tt> 方法。
     * 从底层的 Windows 注册表节点删除字符串名称-值对，如果此值仍然存在。
     * 如果 Windows 注册表不可用或键已被删除，则记录警告。
     */
    protected void removeSpi(String key) {
        int nativeHandle = openKey(KEY_SET_VALUE);
        if (nativeHandle == NULL_NATIVE_HANDLE) {
        return;
        }
        int result =
            WindowsRegDeleteValue(nativeHandle, toWindowsName(key));
        if (result != ERROR_SUCCESS && result != ERROR_FILE_NOT_FOUND) {
            logger().warning("无法删除 Windows 注册表值 " +
                    byteArrayToString(windowsAbsolutePath()) + "\\" +
                    toWindowsName(key) + " 在根 0x" +
                    Integer.toHexString(rootNativeHandle()) +
                    ". Windows RegDeleteValue(...) 返回错误代码 " +
                    result + ".");
            isBackingStoreAvailable = false;
        }
        closeKey(nativeHandle);
    }


                /**
     * 实现 <tt>AbstractPreferences</tt> <tt>keysSpi()</tt> 方法。
     * 从底层的 Windows 注册表节点获取值名称。
     * 如果 Windows 注册表不可用，则抛出 BackingStoreException 并记录警告。
     */
    protected String[] keysSpi() throws BackingStoreException{
        // 确定值的数量
        int nativeHandle = openKey(KEY_QUERY_VALUE);
        if (nativeHandle == NULL_NATIVE_HANDLE) {
            throw new BackingStoreException(
                    "无法打开 Windows 注册表节点 " +
                    byteArrayToString(windowsAbsolutePath()) +
                    " 在根 0x" +
                    Integer.toHexString(rootNativeHandle()) + ".");
        }
        int[] result =  WindowsRegQueryInfoKey1(nativeHandle);
        if (result[ERROR_CODE] != ERROR_SUCCESS) {
            String info = "无法查询 Windows 注册表节点 " +
                    byteArrayToString(windowsAbsolutePath()) +
                    " 在根 0x" +
                    Integer.toHexString(rootNativeHandle()) +
                    ". Windows RegQueryInfoKeyEx(...) 返回错误代码 " +
                    result[ERROR_CODE] + ".";
            logger().warning(info);
            throw new BackingStoreException(info);
        }
        int maxValueNameLength = result[MAX_VALUE_NAME_LENGTH];
        int valuesNumber = result[VALUES_NUMBER];
        if (valuesNumber == 0) {
            closeKey(nativeHandle);
            return new String[0];
        }
        // 获取值
        String[] valueNames = new String[valuesNumber];
        for (int i = 0; i < valuesNumber; i++) {
            byte[] windowsName = WindowsRegEnumValue1(nativeHandle, i,
                                                      maxValueNameLength+1);
            if (windowsName == null) {
                String info =
                    "无法枚举 Windows 节点 " +
                    byteArrayToString(windowsAbsolutePath()) + " 在根 0x" +
                    Integer.toHexString(rootNativeHandle()) + " 的值 #" + i + "。";
                logger().warning(info);
                throw new BackingStoreException(info);
            }
            valueNames[i] = toJavaName(windowsName);
        }
        closeKey(nativeHandle);
        return valueNames;
    }

    /**
     * 实现 <tt>AbstractPreferences</tt> <tt>childrenNamesSpi()</tt> 方法。
     * 调用 Windows 注册表以检索此节点的子节点。
     * 如果 Windows 注册表不可用，则抛出 BackingStoreException 并记录警告消息。
     */
    protected String[] childrenNamesSpi() throws BackingStoreException {
        // 打开键
        int nativeHandle = openKey(KEY_ENUMERATE_SUB_KEYS | KEY_QUERY_VALUE);
        if (nativeHandle == NULL_NATIVE_HANDLE) {
            throw new BackingStoreException(
                    "无法打开 Windows 注册表节点 " +
                    byteArrayToString(windowsAbsolutePath()) +
                    " 在根 0x" +
                    Integer.toHexString(rootNativeHandle()) + ".");
        }
        // 获取子节点数量
        int[] result =  WindowsRegQueryInfoKey1(nativeHandle);
        if (result[ERROR_CODE] != ERROR_SUCCESS) {
            String info = "无法查询 Windows 注册表节点 " +
                    byteArrayToString(windowsAbsolutePath()) +
                    " 在根 0x" + Integer.toHexString(rootNativeHandle()) +
                    ". Windows RegQueryInfoKeyEx(...) 返回错误代码 " +
                    result[ERROR_CODE] + ".";
            logger().warning(info);
            throw new BackingStoreException(info);
        }
        int maxKeyLength = result[MAX_KEY_LENGTH];
        int subKeysNumber = result[SUBKEYS_NUMBER];
        if (subKeysNumber == 0) {
            closeKey(nativeHandle);
            return new String[0];
        }
        String[] subkeys = new String[subKeysNumber];
        String[] children = new String[subKeysNumber];
        // 获取子节点
        for (int i = 0; i < subKeysNumber; i++) {
            byte[] windowsName = WindowsRegEnumKeyEx1(nativeHandle, i,
                                                      maxKeyLength+1);
            if (windowsName == null) {
                String info =
                    "无法枚举 Windows 节点 " +
                    byteArrayToString(windowsAbsolutePath()) + " 在根 0x" +
                    Integer.toHexString(rootNativeHandle()) + " 的键 #" + i + "。";
                logger().warning(info);
                throw new BackingStoreException(info);
            }
            String javaName = toJavaName(windowsName);
            children[i] = javaName;
        }
        closeKey(nativeHandle);
        return children;
    }

    /**
     * 实现 <tt>Preferences</tt> <tt>flush()</tt> 方法。
     * 将 Windows 注册表的更改刷新到磁盘。
     * 如果 Windows 注册表不可用，则抛出 BackingStoreException 并记录警告消息。
     */
    public void flush() throws BackingStoreException{

        if (isRemoved()) {
            parent.flush();
            return;
        }
        if (!isBackingStoreAvailable) {
            throw new BackingStoreException(
                    "flush(): 后端存储不可用。");
        }
        int nativeHandle = openKey(KEY_READ);
        if (nativeHandle == NULL_NATIVE_HANDLE) {
            throw new BackingStoreException(
                    "无法打开 Windows 注册表节点 " +
                    byteArrayToString(windowsAbsolutePath()) +
                    " 在根 0x" +
                    Integer.toHexString(rootNativeHandle()) + ".");
        }
        int result = WindowsRegFlushKey1(nativeHandle);
        if (result != ERROR_SUCCESS) {
            String info = "无法刷新 Windows 注册表节点 " +
                    byteArrayToString(windowsAbsolutePath()) +
                    " 在根 0x" +
                    Integer.toHexString(rootNativeHandle()) +
                    ". Windows RegFlushKey(...) 返回错误代码 " +
                    result + ".";
            logger().warning(info);
            throw new BackingStoreException(info);
        }
        closeKey(nativeHandle);
    }

    /**
     * 实现 <tt>Preferences</tt> <tt>sync()</tt> 方法。
     * 将 Windows 注册表更改刷新到磁盘。等同于 flush()。
     * @see flush()
     */
    public void sync() throws BackingStoreException{
        if (isRemoved())
            throw new IllegalStateException("节点已被删除");
        flush();
    }

    /**
     * 实现 <tt>AbstractPreferences</tt> <tt>childSpi()</tt> 方法。
     * 构造一个具有给定名称的子节点，并创建其底层的 Windows 注册表节点，
     * 如果该节点不存在的话。
     * 如果 Windows 注册表不可用，则记录警告信息。
     */
    protected AbstractPreferences childSpi(String name) {
        return new WindowsPreferences(this, name);
    }

    /**
     * 实现 <tt>AbstractPreferences</tt> <tt>removeNodeSpi()</tt> 方法。
     * 删除底层的 Windows 注册表节点。
     * 如果 Windows 注册表不可用，则抛出 BackingStoreException 并记录警告。
     */
    public void removeNodeSpi() throws BackingStoreException {
        int parentNativeHandle =
                ((WindowsPreferences)parent()).openKey(DELETE);
        if (parentNativeHandle == NULL_NATIVE_HANDLE) {
            throw new BackingStoreException(
                    "无法打开 " +
                    byteArrayToString(windowsAbsolutePath()) +
                    " 在根 0x" +
                    Integer.toHexString(rootNativeHandle()) + " 的父 Windows 注册表节点。");
        }
        int result =
                WindowsRegDeleteKey(parentNativeHandle, toWindowsName(name()));
        if (result != ERROR_SUCCESS) {
            String info = "无法删除 Windows 注册表节点 " +
                    byteArrayToString(windowsAbsolutePath()) +
                    " 在根 0x" + Integer.toHexString(rootNativeHandle()) +
                    "。 Windows RegDeleteKeyEx(...) 返回错误代码 " +
                    result + ".";
            logger().warning(info);
            throw new BackingStoreException(info);
        }
        closeKey(parentNativeHandle);
    }

    /**
     * 将值或节点的名称从其字节数组表示转换为 Java 字符串。使用两种编码，简单和 altBase64。有关编码约定的详细描述，请参见
     * {@link #toWindowsName(String) toWindowsName()}。
     * @param windowsNameArray 以 null 结尾的字节数组。
     */
    private static String toJavaName(byte[] windowsNameArray) {
        String windowsName = byteArrayToString(windowsNameArray);
        // 检查是否为 Alt64
        if ((windowsName.length() > 1) &&
                (windowsName.substring(0, 2).equals("/!"))) {
            return toJavaAlt64Name(windowsName);
        }
        StringBuilder javaName = new StringBuilder();
        char ch;
        // 从简单编码解码
        for (int i = 0; i < windowsName.length(); i++) {
            if ((ch = windowsName.charAt(i)) == '/') {
                char next = ' ';
                if ((windowsName.length() > i + 1) &&
                        ((next = windowsName.charAt(i+1)) >= 'A') &&
                        (next <= 'Z')) {
                    ch = next;
                    i++;
                } else if ((windowsName.length() > i + 1) &&
                           (next == '/')) {
                    ch = '\\';
                    i++;
                }
            } else if (ch == '\\') {
                ch = '/';
            }
            javaName.append(ch);
        }
        return javaName.toString();
    }

    /**
     * 将值或节点的名称从其 Windows 表示转换为 Java 字符串，使用 altBase64 编码。有关编码约定的详细描述，请参见
     * {@link #toWindowsName(String) toWindowsName()}。
     */

    private static String toJavaAlt64Name(String windowsName) {
        byte[] byteBuffer =
                Base64.altBase64ToByteArray(windowsName.substring(2));
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < byteBuffer.length; i++) {
            int firstbyte = (byteBuffer[i++] & 0xff);
            int secondbyte =  (byteBuffer[i] & 0xff);
            result.append((char)((firstbyte << 8) + secondbyte));
        }
        return result.toString();
    }

    /**
     * 将值或节点的名称转换为其 Windows 表示形式，作为字节编码的字符串。
     * 使用两种编码，简单和 altBase64。
     * <p>
     * 如果 Java 字符串不包含任何小于 0x0020 或大于 0x007f 的字符，则使用 <i>简单</i> 编码。
     * 简单编码为大写字母添加 "/" 字符，即 "A" 编码为 "/A"。字符 '\' 编码为 '//'，'/' 编码为 '\'。
     * 构建的字符串通过截断最高字节并添加终止 <tt>null</tt> 字符转换为字节数组。
     * <p>
     * 如果 Java 字符串包含至少一个小于 0x0020 或大于 0x007f 的字符，则使用 <i>altBase64</i> 编码。
     * 该编码通过将 Windows 字符串的前两个字节设置为 '/!' 来标记。然后使用 Base64 类中的
     * byteArrayToAltBase64() 方法对 Java 名称进行编码。
     */
    private static byte[] toWindowsName(String javaName) {
        StringBuilder windowsName = new StringBuilder();
        for (int i = 0; i < javaName.length(); i++) {
            char ch = javaName.charAt(i);
            if ((ch < 0x0020) || (ch > 0x007f)) {
                // 如果遇到非平凡字符，则使用 altBase64
                return toWindowsAlt64Name(javaName);
            }
            if (ch == '\\') {
                windowsName.append("//");
            } else if (ch == '/') {
                windowsName.append('\\');
            } else if ((ch >= 'A') && (ch <='Z')) {
                windowsName.append('/').append(ch);
            } else {
                windowsName.append(ch);
            }
        }
        return stringToByteArray(windowsName.toString());
    }

    /**
     * 将值或节点的名称转换为其 Windows 表示形式，作为字节编码的字符串，使用 altBase64 编码。有关编码约定的详细描述，请参见
     * {@link #toWindowsName(String) toWindowsName()}。
     */
    private static byte[] toWindowsAlt64Name(String javaName) {
        byte[] javaNameArray = new byte[2*javaName.length()];
        // 转换为字节对
        int counter = 0;
        for (int i = 0; i < javaName.length();i++) {
            int ch = javaName.charAt(i);
            javaNameArray[counter++] = (byte)(ch >>> 8);
            javaNameArray[counter++] = (byte)ch;
        }


                    return stringToByteArray("/!" +
                Base64.byteArrayToAltBase64(javaNameArray));
    }

    /**
     * 将值字符串从其Windows表示形式转换为Java字符串。请参阅
     * {@link #toWindowsValueString(String) toWindowsValueString()} 以获取编码算法的描述。
     */
     private static String toJavaValueString(byte[] windowsNameArray) {
        // 使用修改后的native2ascii算法
        String windowsName = byteArrayToString(windowsNameArray);
        StringBuilder javaName = new StringBuilder();
        char ch;
        for (int i = 0; i < windowsName.length(); i++){
            if ((ch = windowsName.charAt(i)) == '/') {
                char next = ' ';

                if (windowsName.length() > i + 1 &&
                        (next = windowsName.charAt(i + 1)) == 'u') {
                    if (windowsName.length() < i + 6) {
                        break;
                    } else {
                        ch = (char)Integer.parseInt(
                                windowsName.substring(i + 2, i + 6), 16);
                        i += 5;
                    }
                } else
                if ((windowsName.length() > i + 1) &&
                        ((windowsName.charAt(i+1)) >= 'A') &&
                        (next <= 'Z')) {
                    ch = next;
                    i++;
                } else if ((windowsName.length() > i + 1) &&
                        (next == '/')) {
                    ch = '\\';
                    i++;
                }
            } else if (ch == '\\') {
                ch = '/';
            }
            javaName.append(ch);
        }
        return javaName.toString();
    }

    /**
     * 将值字符串转换为其Windows表示形式。
     * 编码算法在大写字母前添加"/"字符，即
     * "A" 编码为 "/A"。字符 '\' 编码为 '//'，
     * '/' 编码为 '\'。
     * 然后使用类似于JDK的native2ascii转换器的编码方案
     * 将Java字符串转换为ASCII字符的字节数组。
     */
    private static byte[] toWindowsValueString(String javaName) {
        StringBuilder windowsName = new StringBuilder();
        for (int i = 0; i < javaName.length(); i++) {
            char ch = javaName.charAt(i);
            if ((ch < 0x0020) || (ch > 0x007f)){
                // 写入 \udddd
                windowsName.append("/u");
                String hex = Integer.toHexString(javaName.charAt(i));
                StringBuilder hex4 = new StringBuilder(hex);
                hex4.reverse();
                int len = 4 - hex4.length();
                for (int j = 0; j < len; j++){
                    hex4.append('0');
                }
                for (int j = 0; j < 4; j++){
                    windowsName.append(hex4.charAt(3 - j));
                }
            } else if (ch == '\\') {
                windowsName.append("//");
            } else if (ch == '/') {
                windowsName.append('\\');
            } else if ((ch >= 'A') && (ch <='Z')) {
                windowsName.append('/').append(ch);
            } else {
                windowsName.append(ch);
            }
        }
        return stringToByteArray(windowsName.toString());
    }

    /**
     * 返回此节点的顶级Windows节点的本机句柄。
     */
    private int rootNativeHandle() {
        return (isUserNode()
                ? USER_ROOT_NATIVE_HANDLE
                : SYSTEM_ROOT_NATIVE_HANDLE);
    }

    /**
     * 返回此Java字符串作为以空字符终止的字节数组
     */
    private static byte[] stringToByteArray(String str) {
        byte[] result = new byte[str.length()+1];
        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }
        result[str.length()] = 0;
        return result;
    }

    /**
     * 将以空字符终止的字节数组转换为Java字符串
     */
    private static String byteArrayToString(byte[] array) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < array.length - 1; i++) {
            result.append((char)array[i]);
        }
        return result.toString();
    }

   /**
    * AbstractPreferences.flushSpi() 的空实现，从未使用。
    */
    protected void flushSpi() throws BackingStoreException {
        // assert false;
    }

   /**
    * AbstractPreferences.syncSpi() 的空实现，从未使用。
    */
    protected void syncSpi() throws BackingStoreException {
        // assert false;
    }

    private static synchronized PlatformLogger logger() {
        if (logger == null) {
            logger = PlatformLogger.getLogger("java.util.prefs");
        }
        return logger;
    }
}
