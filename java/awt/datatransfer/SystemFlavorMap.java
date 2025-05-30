
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.awt.datatransfer;

import java.awt.Toolkit;

import java.lang.ref.SoftReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import sun.awt.AppContext;
import sun.awt.datatransfer.DataTransferer;

/**
 * SystemFlavorMap 是一个可配置的映射，用于将“本地”（字符串）与“风味”（DataFlavors）进行映射。
 * “本地”对应于特定平台的数据格式，而“风味”对应于平台无关的 MIME 类型。此映射用于数据传输子系统，
 * 以在 Java 和本地应用程序之间，以及在单独的虚拟机中的 Java 应用程序之间传输数据。
 * <p>
 *
 * @since 1.2
 */
public final class SystemFlavorMap implements FlavorMap, FlavorTable {

    /**
     * 用于标记转换为本地平台类型的 Java 类型的常量前缀。
     */
    private static String JavaMIME = "JAVA_DATAFLAVOR:";

    private static final Object FLAVOR_MAP_KEY = new Object();

    /**
     * 从 java.util.Properties 复制的。
     */
    private static final String keyValueSeparators = "=: \t\r\n\f";
    private static final String strictKeyValueSeparators = "=:";
    private static final String whiteSpaceChars = " \t\r\n\f";

    /**
     * 有效、解码的文本风味表示类列表，按从最佳到最差的顺序排列。
     */
    private static final String[] UNICODE_TEXT_CLASSES = {
        "java.io.Reader", "java.lang.String", "java.nio.CharBuffer", "\"[C\""
    };

    /**
     * 有效、编码的文本风味表示类列表，按从最佳到最差的顺序排列。
     */
    private static final String[] ENCODED_TEXT_CLASSES = {
        "java.io.InputStream", "java.nio.ByteBuffer", "\"[B\""
    };

    /**
     * 表示 text/plain MIME 类型的字符串。
     */
    private static final String TEXT_PLAIN_BASE_TYPE = "text/plain";

    /**
     * 表示 text/html MIME 类型的字符串。
     */
    private static final String HTML_TEXT_BASE_TYPE = "text/html";

    /**
     * 将本地字符串映射到 DataFlavors 列表（或文本 DataFlavors 的基本类型字符串）。
     * 不要直接使用该字段，而是使用 getNativeToFlavor()。
     */
    private final Map<String, LinkedHashSet<DataFlavor>> nativeToFlavor = new HashMap<>();

    /**
     * 访问 nativeToFlavor 映射。由于我们使用延迟初始化，因此必须使用此访问器而不是直接访问可能尚未初始化的字段。
     * 如果需要，此方法将初始化该字段。
     *
     * @return nativeToFlavor
     */
    private Map<String, LinkedHashSet<DataFlavor>> getNativeToFlavor() {
        if (!isMapInitialized) {
            initSystemFlavorMap();
        }
        return nativeToFlavor;
    }

    /**
     * 将 DataFlavors（或文本 DataFlavors 的基本类型字符串）映射到本地字符串列表。
     * 不要直接使用该字段，而是使用 getFlavorToNative()。
     */
    private final Map<DataFlavor, LinkedHashSet<String>> flavorToNative = new HashMap<>();

    /**
     * 访问 flavorToNative 映射。由于我们使用延迟初始化，因此必须使用此访问器而不是直接访问可能尚未初始化的字段。
     * 如果需要，此方法将初始化该字段。
     *
     * @return flavorToNative
     */
    private synchronized Map<DataFlavor, LinkedHashSet<String>> getFlavorToNative() {
        if (!isMapInitialized) {
            initSystemFlavorMap();
        }
        return flavorToNative;
    }

    /**
     * 将文本 DataFlavor 主 MIME 类型映射到本地。仅用于存储在 flavormap.properties 中注册的标准映射。
     * 不要直接使用该字段，而是使用 getTextTypeToNative()。
     */
    private Map<String, LinkedHashSet<String>> textTypeToNative = new HashMap<>();

    /**
     * 显示对象是否已初始化。
     */
    private boolean isMapInitialized = false;

    /**
     * 访问 textTypeToNative 映射。由于我们使用延迟初始化，因此必须使用此访问器而不是直接访问可能尚未初始化的字段。
     * 如果需要，此方法将初始化该字段。
     *
     * @return textTypeToNative
     */
    private synchronized Map<String, LinkedHashSet<String>> getTextTypeToNative() {
        if (!isMapInitialized) {
            initSystemFlavorMap();
            // 从这一点开始，映射不应再被修改
            textTypeToNative = Collections.unmodifiableMap(textTypeToNative);
        }
        return textTypeToNative;
    }

    /**
     * 缓存 getNativesForFlavor() 的结果。将 DataFlavors 映射到引用本地字符串 LinkedHashSet 的 SoftReferences。
     */
    private final SoftCache<DataFlavor, String> nativesForFlavorCache = new SoftCache<>();

    /**
     * 缓存 getFlavorsForNative() 的结果。将本地字符串映射到引用 DataFlavors LinkedHashSet 的 SoftReferences。
     */
    private final SoftCache<String, DataFlavor> flavorsForNativeCache = new SoftCache<>();

    /**
     * 动态映射生成仅用于文本映射，不应应用于已使用 setFlavorsForNative() 或 setNativesForFlavor() 明确指定映射的 DataFlavors 和本地字符串。
     * 这保留了所有这样的键。
     */
    private Set<Object> disabledMappingGenerationKeys = new HashSet<>();

    /**
     * 返回当前线程 ClassLoader 的默认 FlavorMap。
     */
    public static FlavorMap getDefaultFlavorMap() {
        AppContext context = AppContext.getAppContext();
        FlavorMap fm = (FlavorMap) context.get(FLAVOR_MAP_KEY);
        if (fm == null) {
            fm = new SystemFlavorMap();
            context.put(FLAVOR_MAP_KEY, fm);
        }
        return fm;
    }

    private SystemFlavorMap() {
    }

    /**
     * 通过读取 flavormap.properties 和 AWT.DnD.flavorMapFileURL 初始化 SystemFlavorMap。
     * 为了线程安全，必须在锁上调用此方法。
     */
    private void initSystemFlavorMap() {
        if (isMapInitialized) {
            return;
        }

        isMapInitialized = true;
        BufferedReader flavormapDotProperties =
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<BufferedReader>() {
                    public BufferedReader run() {
                        String fileName =
                            System.getProperty("java.home") +
                            File.separator +
                            "lib" +
                            File.separator +
                            "flavormap.properties";
                        try {
                            return new BufferedReader
                                (new InputStreamReader
                                    (new File(fileName).toURI().toURL().openStream(), "ISO-8859-1"));
                        } catch (MalformedURLException e) {
                            System.err.println("MalformedURLException:" + e + " while loading default flavormap.properties file:" + fileName);
                        } catch (IOException e) {
                            System.err.println("IOException:" + e + " while loading default flavormap.properties file:" + fileName);
                        }
                        return null;
                    }
                });

        String url =
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<String>() {
                    public String run() {
                        return Toolkit.getProperty("AWT.DnD.flavorMapFileURL", null);
                    }
                });

        if (flavormapDotProperties != null) {
            try {
                parseAndStoreReader(flavormapDotProperties);
            } catch (IOException e) {
                System.err.println("IOException:" + e + " while parsing default flavormap.properties file");
            }
        }

        BufferedReader flavormapURL = null;
        if (url != null) {
            try {
                flavormapURL = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "ISO-8859-1"));
            } catch (MalformedURLException e) {
                System.err.println("MalformedURLException:" + e + " while reading AWT.DnD.flavorMapFileURL:" + url);
            } catch (IOException e) {
                System.err.println("IOException:" + e + " while reading AWT.DnD.flavorMapFileURL:" + url);
            } catch (SecurityException e) {
                // 忽略
            }
        }

        if (flavormapURL != null) {
            try {
                parseAndStoreReader(flavormapURL);
            } catch (IOException e) {
                System.err.println("IOException:" + e + " while parsing AWT.DnD.flavorMapFileURL");
            }
        }
    }
    /**
     * 从 java.util.Properties 复制的代码。自行解析数据是处理重复键和值的唯一方法。
     */
    private void parseAndStoreReader(BufferedReader in) throws IOException {
        while (true) {
            // 获取下一行
            String line = in.readLine();
            if (line == null) {
                return;
            }

            if (line.length() > 0) {
                // 如果不是注释行，则继续以斜杠结尾的行
                char firstChar = line.charAt(0);
                if (firstChar != '#' && firstChar != '!') {
                    while (continueLine(line)) {
                        String nextLine = in.readLine();
                        if (nextLine == null) {
                            nextLine = "";
                        }
                        String loppedLine =
                            line.substring(0, line.length() - 1);
                        // 跳过新行开头的空白
                        int startIndex = 0;
                        for(; startIndex < nextLine.length(); startIndex++) {
                            if (whiteSpaceChars.
                                    indexOf(nextLine.charAt(startIndex)) == -1)
                            {
                                break;
                            }
                        }
                        nextLine = nextLine.substring(startIndex,
                                                      nextLine.length());
                        line = loppedLine+nextLine;
                    }

                    // 查找键的开始位置
                    int len = line.length();
                    int keyStart = 0;
                    for(; keyStart < len; keyStart++) {
                        if(whiteSpaceChars.
                               indexOf(line.charAt(keyStart)) == -1) {
                            break;
                        }
                    }

                    // 忽略空白行
                    if (keyStart == len) {
                        continue;
                    }

                    // 查找键和值之间的分隔符
                    int separatorIndex = keyStart;
                    for(; separatorIndex < len; separatorIndex++) {
                        char currentChar = line.charAt(separatorIndex);
                        if (currentChar == '\\') {
                            separatorIndex++;
                        } else if (keyValueSeparators.
                                       indexOf(currentChar) != -1) {
                            break;
                        }
                    }

                    // 跳过键后的空白（如果有）
                    int valueIndex = separatorIndex;
                    for (; valueIndex < len; valueIndex++) {
                        if (whiteSpaceChars.
                                indexOf(line.charAt(valueIndex)) == -1) {
                            break;
                        }
                    }

                    // 跳过一个非空白的键值分隔符（如果有）
                    if (valueIndex < len) {
                        if (strictKeyValueSeparators.
                                indexOf(line.charAt(valueIndex)) != -1) {
                            valueIndex++;
                        }
                    }

                    // 跳过其他分隔符后的空白（如果有）
                    while (valueIndex < len) {
                        if (whiteSpaceChars.
                                indexOf(line.charAt(valueIndex)) == -1) {
                            break;
                        }
                        valueIndex++;
                    }

                    String key = line.substring(keyStart, separatorIndex);
                    String value = (separatorIndex < len)
                        ? line.substring(valueIndex, len)
                        : "";

                    // 转换并存储键和值
                    key = loadConvert(key);
                    value = loadConvert(value);

                    try {
                        MimeType mime = new MimeType(value);
                        if ("text".equals(mime.getPrimaryType())) {
                            String charset = mime.getParameter("charset");
                            if (DataTransferer.doesSubtypeSupportCharset
                                    (mime.getSubType(), charset))
                            {
                                // 我们需要存储字符集和换行参数（如果有），以便 DataTransferer 在转换为本地格式时使用这些信息。
                                DataTransferer transferer =
                                    DataTransferer.getInstance();
                                if (transferer != null) {
                                    transferer.registerTextFlavorProperties
                                        (key, charset,
                                         mime.getParameter("eoln"),
                                         mime.getParameter("terminators"));
                                }
                            }


    /**
     * 从 java.util.Properties 复制。
     */
    private boolean continueLine (String line) {
        int slashCount = 0;
        int index = line.length() - 1;
        while((index >= 0) && (line.charAt(index--) == '\\')) {
            slashCount++;
        }
        return (slashCount % 2 == 1);
    }

    /**
     * 从 java.util.Properties 复制。
     */
    private String loadConvert(String theString) {
        char aChar;
        int len = theString.length();
        StringBuilder outBuffer = new StringBuilder(len);

        for (int x = 0; x < len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // 读取 xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                          case '0': case '1': case '2': case '3': case '4':
                          case '5': case '6': case '7': case '8': case '9': {
                             value = (value << 4) + aChar - '0';
                             break;
                          }
                          case 'a': case 'b': case 'c':
                          case 'd': case 'e': case 'f': {
                             value = (value << 4) + 10 + aChar - 'a';
                             break;
                          }
                          case 'A': case 'B': case 'C':
                          case 'D': case 'E': case 'F': {
                             value = (value << 4) + 10 + aChar - 'A';
                             break;
                          }
                          default: {
                              throw new IllegalArgumentException(
                                           "Malformed \\uxxxx encoding.");
                          }
                        }
                    }
                    outBuffer.append((char)value);
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') {
                        aChar = '\f';
                    }
                    outBuffer.append(aChar);
                }
            } else {
                outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

    /**
     * 在指定的哈希键下将列出的对象存储在 map 中。与标准 map 不同，列出的对象不会替换已存在于适当 Map 位置的对象，而是附加到该位置存储的 List 中。
     */
    private <H, L> void store(H hashed, L listed, Map<H, LinkedHashSet<L>> map) {
        LinkedHashSet<L> list = map.get(hashed);
        if (list == null) {
            list = new LinkedHashSet<>(1);
            map.put(hashed, list);
        }
        if (!list.contains(listed)) {
            list.add(listed);
        }
    }

    /**
     * 语义上等同于 'nativeToFlavor.get(nat)'。此方法处理 'nat' 未在 'nativeToFlavor' 中找到的情况。在这种情况下，如果指定的 native 被编码为 Java MIME 类型，则会合成、存储并返回一个新的 DataFlavor。
     */
    private LinkedHashSet<DataFlavor> nativeToFlavorLookup(String nat) {
        LinkedHashSet<DataFlavor> flavors = getNativeToFlavor().get(nat);

        if (nat != null && !disabledMappingGenerationKeys.contains(nat)) {
            DataTransferer transferer = DataTransferer.getInstance();
            if (transferer != null) {
                LinkedHashSet<DataFlavor> platformFlavors =
                    transferer.getPlatformMappingsForNative(nat);
                if (!platformFlavors.isEmpty()) {
                    if (flavors != null) {
                        // 在列表开头添加平台特定的映射，确保通过
                        // addFlavorForUnencodedNative() 添加的 flavors 位于列表末尾。
                        platformFlavors.addAll(flavors);
                    }
                    flavors = platformFlavors;
                }
            }
        }

        if (flavors == null && isJavaMIMEType(nat)) {
            String decoded = decodeJavaMIMEType(nat);
            DataFlavor flavor = null;

            try {
                flavor = new DataFlavor(decoded);
            } catch (Exception e) {
                System.err.println("Exception \"" + e.getClass().getName() +
                                   ": " + e.getMessage()  +
                                   "\"while constructing DataFlavor for: " +
                                   decoded);
            }

            if (flavor != null) {
                flavors = new LinkedHashSet<>(1);
                getNativeToFlavor().put(nat, flavors);
                flavors.add(flavor);
                flavorsForNativeCache.remove(nat);

                LinkedHashSet<String> natives = getFlavorToNative().get(flavor);
                if (natives == null) {
                    natives = new LinkedHashSet<>(1);
                    getFlavorToNative().put(flavor, natives);
                }
                natives.add(nat);
                nativesForFlavorCache.remove(flavor);
            }
        }

        return (flavors != null) ? flavors : new LinkedHashSet<>(0);
    }

    /**
     * 语义上等同于 'flavorToNative.get(flav)'。此方法处理 'flav' 未在 'flavorToNative' 中找到的情况，具体取决于 'synthesize' 参数的值。如果 'synthesize' 是 SYNTHESIZE_IF_NOT_FOUND，则通过编码 DataFlavor 的 MIME 类型来合成、存储并返回一个 native。否则返回一个空 List，且 'flavorToNative' 保持不变。
     */
    private LinkedHashSet<String> flavorToNativeLookup(final DataFlavor flav,
                                                       final boolean synthesize) {

        LinkedHashSet<String> natives = getFlavorToNative().get(flav);

        if (flav != null && !disabledMappingGenerationKeys.contains(flav)) {
            DataTransferer transferer = DataTransferer.getInstance();
            if (transferer != null) {
                LinkedHashSet<String> platformNatives =
                    transferer.getPlatformMappingsForFlavor(flav);
                if (!platformNatives.isEmpty()) {
                    if (natives != null) {
                        // 在列表开头添加平台特定的映射，确保通过
                        // addUnencodedNativeForFlavor() 添加的 natives 位于列表末尾。
                        platformNatives.addAll(natives);
                    }
                    natives = platformNatives;
                }
            }
        }

        if (natives == null) {
            if (synthesize) {
                String encoded = encodeDataFlavor(flav);
                natives = new LinkedHashSet<>(1);
                getFlavorToNative().put(flav, natives);
                natives.add(encoded);

                LinkedHashSet<DataFlavor> flavors = getNativeToFlavor().get(encoded);
                if (flavors == null) {
                    flavors = new LinkedHashSet<>(1);
                    getNativeToFlavor().put(encoded, flavors);
                }
                flavors.add(flav);

                nativesForFlavorCache.remove(flav);
                flavorsForNativeCache.remove(encoded);
            } else {
                natives = new LinkedHashSet<>(0);
            }
        }

        return new LinkedHashSet<>(natives);
    }

    /**
     * 返回一个 <code>List</code>，其中包含数据传输子系统可以将指定的 <code>DataFlavor</code> 转换为的 <code>String</code> native。该 <code>List</code> 将按从最佳 native 到最差 native 排序。也就是说，第一个 native 将最能反映指定 flavor 的数据到底层的本地平台。
     * <p>
     * 如果指定的 <code>DataFlavor</code> 之前未知于数据传输子系统，并且数据传输子系统无法将此 <code>DataFlavor</code> 转换为任何现有的 native，则调用此方法将在两个方向上建立指定的 <code>DataFlavor</code> 和其 MIME 类型的编码版本之间的映射。
     *
     * @param flav 应返回其对应 native 的 <code>DataFlavor</code>。如果指定 <code>null</code>，则返回数据传输子系统当前已知的所有 native，顺序不确定。
     * @return 一个 <code>java.util.List</code>，其中包含 <code>java.lang.String</code> 对象，这些对象是平台特定的数据格式的平台特定表示。
     *
     * @see #encodeDataFlavor
     * @since 1.4
     */
    @Override
    public synchronized List<String> getNativesForFlavor(DataFlavor flav) {
        LinkedHashSet<String> retval = nativesForFlavorCache.check(flav);
        if (retval != null) {
            return new ArrayList<>(retval);
        }

        if (flav == null) {
            retval = new LinkedHashSet<>(getNativeToFlavor().keySet());
        } else if (disabledMappingGenerationKeys.contains(flav)) {
            // 在这种情况下，我们不应该为这个 flavor 合成一个 native，因为其映射是显式指定的。
            retval = flavorToNativeLookup(flav, false);
        } else if (DataTransferer.isFlavorCharsetTextType(flav)) {
            retval = new LinkedHashSet<>(0);

            // 对于 text/* flavors，flavormap.properties 中指定的 flavor-to-native 映射按 flavor 的基础类型存储。
            if ("text".equals(flav.getPrimaryType())) {
                LinkedHashSet<String> textTypeNatives =
                        getTextTypeToNative().get(flav.mimeType.getBaseType());
                if (textTypeNatives != null) {
                    retval.addAll(textTypeNatives);
                }
            }

            // 还包括 text/plain natives，但不要重复 Strings
            LinkedHashSet<String> textTypeNatives =
                    getTextTypeToNative().get(TEXT_PLAIN_BASE_TYPE);
            if (textTypeNatives != null) {
                retval.addAll(textTypeNatives);
            }

            if (retval.isEmpty()) {
                retval = flavorToNativeLookup(flav, true);
            } else {
                // 在这个分支中，可以保证为 flav 的 MIME 类型显式列出的 natives 是通过
                // addUnencodedNativeForFlavor() 添加的，因此它们的优先级较低。
                retval.addAll(flavorToNativeLookup(flav, false));
            }
        } else if (DataTransferer.isFlavorNoncharsetTextType(flav)) {
            retval = getTextTypeToNative().get(flav.mimeType.getBaseType());

            if (retval == null || retval.isEmpty()) {
                retval = flavorToNativeLookup(flav, true);
            } else {
                // 在这个分支中，可以保证为 flav 的 MIME 类型显式列出的 natives 是通过
                // addUnencodedNativeForFlavor() 添加的，因此它们的优先级较低。
                retval.addAll(flavorToNativeLookup(flav, false));
            }
        } else {
            retval = flavorToNativeLookup(flav, true);
        }

        nativesForFlavorCache.put(flav, retval);
        // 创建一个副本，因为客户端代码可以修改返回的列表。
        return new ArrayList<>(retval);
    }

    /**
     * 返回一个 <code>List</code>，其中包含数据传输子系统可以将指定的 <code>String</code> native 转换为的 <code>DataFlavor</code>。该 <code>List</code> 将按从最佳 <code>DataFlavor</code> 到最差 <code>DataFlavor</code> 排序。也就是说，第一个 <code>DataFlavor</code> 将最能反映指定 native 的数据到 Java 应用程序。
     * <p>
     * 如果指定的 native 之前未知于数据传输子系统，并且该 native 已被正确编码，则调用此方法将在两个方向上建立指定的 native 和一个 <code>DataFlavor</code> 之间的映射，该 <code>DataFlavor</code> 的 MIME 类型是 native 的解码版本。
     * <p>
     * 如果指定的 native 未被正确编码，并且此 native 的映射未通过 <code>setFlavorsForNative</code> 被修改，则 <code>List</code> 的内容是平台依赖的，但不能返回 <code>null</code>。
     *
     * @param nat 应返回其对应 <code>DataFlavor</code> 的 native。如果指定 <code>null</code>，则返回数据传输子系统当前已知的所有 <code>DataFlavor</code>，顺序不确定。
     * @return 一个 <code>java.util.List</code>，其中包含 <code>DataFlavor</code> 对象，这些对象可以将指定的平台特定 native 转换为平台特定的数据。
     *
     * @see #encodeJavaMIMEType
     * @since 1.4
     */
    @Override
    public synchronized List<DataFlavor> getFlavorsForNative(String nat) {
        LinkedHashSet<DataFlavor> returnValue = flavorsForNativeCache.check(nat);
        if (returnValue != null) {
            return new ArrayList<>(returnValue);
        } else {
            returnValue = new LinkedHashSet<>();
        }


                    if (nat == null) {
            for (String n : getNativesForFlavor(null)) {
                returnValue.addAll(getFlavorsForNative(n));
            }
        } else {
            final LinkedHashSet<DataFlavor> flavors = nativeToFlavorLookup(nat);
            if (disabledMappingGenerationKeys.contains(nat)) {
                return new ArrayList<>(flavors);
            }

            final LinkedHashSet<DataFlavor> flavorsWithSynthesized =
                    nativeToFlavorLookup(nat);

            for (DataFlavor df : flavorsWithSynthesized) {
                returnValue.add(df);
                if ("text".equals(df.getPrimaryType())) {
                    String baseType = df.mimeType.getBaseType();
                    returnValue.addAll(convertMimeTypeToDataFlavors(baseType));
                }
            }
        }
        flavorsForNativeCache.put(nat, returnValue);
        return new ArrayList<>(returnValue);
    }

    private static Set<DataFlavor> convertMimeTypeToDataFlavors(
        final String baseType) {

        final Set<DataFlavor> returnValue = new LinkedHashSet<>();

        String subType = null;

        try {
            final MimeType mimeType = new MimeType(baseType);
            subType = mimeType.getSubType();
        } catch (MimeTypeParseException mtpe) {
            // 不可能发生，因为我们已经在加载时检查了所有映射。
        }

        if (DataTransferer.doesSubtypeSupportCharset(subType, null)) {
            if (TEXT_PLAIN_BASE_TYPE.equals(baseType))
            {
                returnValue.add(DataFlavor.stringFlavor);
            }

            for (String unicodeClassName : UNICODE_TEXT_CLASSES) {
                final String mimeType = baseType + ";charset=Unicode;class=" +
                                            unicodeClassName;

                final LinkedHashSet<String> mimeTypes =
                    handleHtmlMimeTypes(baseType, mimeType);
                for (String mt : mimeTypes) {
                    DataFlavor toAdd = null;
                    try {
                        toAdd = new DataFlavor(mt);
                    } catch (ClassNotFoundException cannotHappen) {
                    }
                    returnValue.add(toAdd);
                }
            }

            for (String charset : DataTransferer.standardEncodings()) {

                for (String encodedTextClass : ENCODED_TEXT_CLASSES) {
                    final String mimeType =
                            baseType + ";charset=" + charset +
                            ";class=" + encodedTextClass;

                    final LinkedHashSet<String> mimeTypes =
                        handleHtmlMimeTypes(baseType, mimeType);

                    for (String mt : mimeTypes) {

                        DataFlavor df = null;

                        try {
                            df = new DataFlavor(mt);
                            // 检查是否等于 plainTextFlavor，以确保使用 plainTextFlavor 的确切字符集，
                            // 而不是规范字符集或其他等效字符集。
                            if (df.equals(DataFlavor.plainTextFlavor)) {
                                df = DataFlavor.plainTextFlavor;
                            }
                        } catch (ClassNotFoundException cannotHappen) {
                        }

                        returnValue.add(df);
                    }
                }
            }

            if (TEXT_PLAIN_BASE_TYPE.equals(baseType))
            {
                returnValue.add(DataFlavor.plainTextFlavor);
            }
        } else {
            // 不支持字符集的文本原生类型应被视为不透明的 8 位数据，无论其各种表示形式如何。
            for (String encodedTextClassName : ENCODED_TEXT_CLASSES) {
                DataFlavor toAdd = null;
                try {
                    toAdd = new DataFlavor(baseType +
                         ";class=" + encodedTextClassName);
                } catch (ClassNotFoundException cannotHappen) {
                }
                returnValue.add(toAdd);
            }
        }
        return returnValue;
    }

    private static final String [] htmlDocumntTypes =
            new String [] {"all", "selection", "fragment"};

    private static LinkedHashSet<String> handleHtmlMimeTypes(String baseType,
                                                             String mimeType) {

        LinkedHashSet<String> returnValues = new LinkedHashSet<>();

        if (HTML_TEXT_BASE_TYPE.equals(baseType)) {
            for (String documentType : htmlDocumntTypes) {
                returnValues.add(mimeType + ";document=" + documentType);
            }
        } else {
            returnValues.add(mimeType);
        }

        return returnValues;
    }

    /**
     * 返回一个指定的 <code>DataFlavor</code> 到其最优先的 <code>String</code> 原生类型的映射。
     * 每个原生值将与 <code>getNativesForFlavor</code> 为指定的 <code>DataFlavor</code> 返回的列表中的第一个原生值相同。
     * <p>
     * 如果指定的 <code>DataFlavor</code> 之前未知于数据传输子系统，则调用此方法将建立一个双向映射，将指定的 <code>DataFlavor</code>
     * 映射到其 MIME 类型的编码版本作为其原生类型。
     *
     * @param flavors 一个 <code>DataFlavor</code> 数组，将成为返回的 <code>Map</code> 的键集。
     *        如果指定 <code>null</code>，则返回一个映射，包含数据传输子系统已知的所有 <code>DataFlavor</code>
     *        到其最优先的 <code>String</code> 原生类型。
     * @return 一个 <code>java.util.Map</code>，键为 <code>DataFlavor</code>，值为 <code>String</code> 原生类型。
     *
     * @see #getNativesForFlavor
     * @see #encodeDataFlavor
     */
    @Override
    public synchronized Map<DataFlavor,String> getNativesForFlavors(DataFlavor[] flavors)
    {
        // 使用 getNativesForFlavor 为文本 <code>DataFlavor</code> 和 stringFlavor 生成额外的原生类型

        if (flavors == null) {
            List<DataFlavor> flavor_list = getFlavorsForNative(null);
            flavors = new DataFlavor[flavor_list.size()];
            flavor_list.toArray(flavors);
        }

        Map<DataFlavor, String> retval = new HashMap<>(flavors.length, 1.0f);
        for (DataFlavor flavor : flavors) {
            List<String> natives = getNativesForFlavor(flavor);
            String nat = (natives.isEmpty()) ? null : natives.get(0);
            retval.put(flavor, nat);
        }

        return retval;
    }

    /**
     * 返回一个指定的 <code>String</code> 原生类型到其最优先的 <code>DataFlavor</code> 的映射。
     * 每个 <code>DataFlavor</code> 值将与 <code>getFlavorsForNative</code> 为指定的原生类型返回的列表中的第一个 <code>DataFlavor</code> 相同。
     * <p>
     * 如果指定的原生类型之前未知于数据传输子系统，并且该原生类型已被正确编码，则调用此方法将建立一个双向映射，将指定的原生类型
     * 映射到一个 <code>DataFlavor</code>，其 MIME 类型是原生类型的解码版本。
     *
     * @param natives 一个 <code>String</code> 数组，将成为返回的 <code>Map</code> 的键集。
     *        如果指定 <code>null</code>，则返回一个映射，包含所有支持的 <code>String</code> 原生类型
     *        到其最优先的 <code>DataFlavor</code>。
     * @return 一个 <code>java.util.Map</code>，键为 <code>String</code> 原生类型，值为 <code>DataFlavor</code>。
     *
     * @see #getFlavorsForNative
     * @see #encodeJavaMIMEType
     */
    @Override
    public synchronized Map<String,DataFlavor> getFlavorsForNatives(String[] natives)
    {
        // 使用 getFlavorsForNative 为文本原生类型生成额外的 <code>DataFlavor</code>
        if (natives == null) {
            List<String> nativesList = getNativesForFlavor(null);
            natives = new String[nativesList.size()];
            nativesList.toArray(natives);
        }

        Map<String, DataFlavor> retval = new HashMap<>(natives.length, 1.0f);
        for (String aNative : natives) {
            List<DataFlavor> flavors = getFlavorsForNative(aNative);
            DataFlavor flav = (flavors.isEmpty())? null : flavors.get(0);
            retval.put(aNative, flav);
        }
        return retval;
    }

    /**
     * 添加一个从指定的 <code>DataFlavor</code>（和所有等于指定的 <code>DataFlavor</code> 的 <code>DataFlavor</code>）
     * 到指定的 <code>String</code> 原生类型的映射。
     * 与 <code>getNativesForFlavor</code> 不同，映射将仅在一个方向上建立，并且原生类型不会被编码。要建立双向映射，
     * 请调用 <code>addFlavorForUnencodedNative</code>。新的映射将具有比任何现有映射更低的优先级。
     * 如果指定的或等于指定的 <code>DataFlavor</code> 的 <code>DataFlavor</code> 到指定的 <code>String</code> 原生类型的映射已经存在，
     * 则此方法无效。
     *
     * @param flav 用于映射的 <code>DataFlavor</code> 键
     * @param nat 用于映射的 <code>String</code> 原生类型值
     * @throws NullPointerException 如果 flav 或 nat 为 <code>null</code>
     *
     * @see #addFlavorForUnencodedNative
     * @since 1.4
     */
    public synchronized void addUnencodedNativeForFlavor(DataFlavor flav,
                                                         String nat) {
        Objects.requireNonNull(nat, "不允许为 null 的原生类型");
        Objects.requireNonNull(flav, "不允许为 null 的数据类型");

        LinkedHashSet<String> natives = getFlavorToNative().get(flav);
        if (natives == null) {
            natives = new LinkedHashSet<>(1);
            getFlavorToNative().put(flav, natives);
        }
        natives.add(nat);
        nativesForFlavorCache.remove(flav);
    }

    /**
     * 丢弃指定的 <code>DataFlavor</code> 和所有等于指定的 <code>DataFlavor</code> 的 <code>DataFlavor</code> 的当前映射，
     * 并创建新的映射到指定的 <code>String</code> 原生类型。
     * 与 <code>getNativesForFlavor</code> 不同，映射将仅在一个方向上建立，并且原生类型不会被编码。要建立双向映射，
     * 请调用 <code>setFlavorsForNative</code>。数组中的第一个原生类型将表示最高优先级的映射。后续的原生类型将表示优先级递减的映射。
     * <p>
     * 如果数组包含多个引用相等的 <code>String</code> 原生类型的元素，此方法将为这些元素中的第一个建立新的映射，并忽略其余的元素。
     * <p>
     * 建议客户端代码不要重置数据传输子系统建立的映射。此方法仅应用于应用程序级别的映射。
     *
     * @param flav 用于映射的 <code>DataFlavor</code> 键
     * @param natives 用于映射的 <code>String</code> 原生类型值
     * @throws NullPointerException 如果 flav 或 natives 为 <code>null</code>，或者 natives 包含 <code>null</code> 元素
     *
     * @see #setFlavorsForNative
     * @since 1.4
     */
    public synchronized void setNativesForFlavor(DataFlavor flav,
                                                 String[] natives) {
        Objects.requireNonNull(natives, "不允许为 null 的原生类型");
        Objects.requireNonNull(flav, "不允许为 null 的数据类型");

        getFlavorToNative().remove(flav);
        for (String aNative : natives) {
            addUnencodedNativeForFlavor(flav, aNative);
        }
        disabledMappingGenerationKeys.add(flav);
        nativesForFlavorCache.remove(flav);
    }

    /**
     * 添加一个从单个 <code>String</code> 原生类型到单个 <code>DataFlavor</code> 的映射。
     * 与 <code>getFlavorsForNative</code> 不同，映射将仅在一个方向上建立，并且原生类型不会被编码。要建立双向映射，
     * 请调用 <code>addUnencodedNativeForFlavor</code>。新的映射将具有比任何现有映射更低的优先级。
     * 如果指定的 <code>String</code> 原生类型到指定的或等于指定的 <code>DataFlavor</code> 的 <code>DataFlavor</code> 的映射已经存在，
     * 则此方法无效。
     *
     * @param nat 用于映射的 <code>String</code> 原生类型键
     * @param flav 用于映射的 <code>DataFlavor</code> 值
     * @throws NullPointerException 如果 nat 或 flav 为 <code>null</code>
     *
     * @see #addUnencodedNativeForFlavor
     * @since 1.4
     */
    public synchronized void addFlavorForUnencodedNative(String nat,
                                                         DataFlavor flav) {
        Objects.requireNonNull(nat, "不允许为 null 的原生类型");
        Objects.requireNonNull(flav, "不允许为 null 的数据类型");

        LinkedHashSet<DataFlavor> flavors = getNativeToFlavor().get(nat);
        if (flavors == null) {
            flavors = new LinkedHashSet<>(1);
            getNativeToFlavor().put(nat, flavors);
        }
        flavors.add(flav);
        flavorsForNativeCache.remove(nat);
    }

    /**
     * 丢弃指定的 <code>String</code> 原生类型的当前映射，并创建新的映射到指定的 <code>DataFlavor</code>。
     * 与 <code>getFlavorsForNative</code> 不同，映射将仅在一个方向上建立，并且原生类型不需要被编码。要建立双向映射，
     * 请调用 <code>setNativesForFlavor</code>。数组中的第一个 <code>DataFlavor</code> 将表示最高优先级的映射。
     * 后续的 <code>DataFlavor</code> 将表示优先级递减的映射。
     * <p>
     * 如果数组包含多个引用相等的 <code>DataFlavor</code> 的元素，此方法将为这些元素中的第一个建立新的映射，并忽略其余的元素。
     * <p>
     * 建议客户端代码不要重置数据传输子系统建立的映射。此方法仅应用于应用程序级别的映射。
     *
     * @param nat 用于映射的 <code>String</code> 原生类型键
     * @param flavors 用于映射的 <code>DataFlavor</code> 值
     * @throws NullPointerException 如果 nat 或 flavors 为 <code>null</code>，或者 flavors 包含 <code>null</code> 元素
     *
     * @see #setNativesForFlavor
     * @since 1.4
     */
    public synchronized void setFlavorsForNative(String nat,
                                                 DataFlavor[] flavors) {
        Objects.requireNonNull(nat, "不允许为 null 的原生类型");
        Objects.requireNonNull(flavors, "不允许为 null 的数据类型");


                    getNativeToFlavor().remove(nat);
        for (DataFlavor flavor : flavors) {
            addFlavorForUnencodedNative(nat, flavor);
        }
        disabledMappingGenerationKeys.add(nat);
        flavorsForNativeCache.remove(nat);
    }

    /**
     * 编码 MIME 类型以用作 <code>String</code> 本地。编码表示的格式取决于实现。
     * 唯一的限制是：
     * <ul>
     * <li>编码表示为 <code>null</code> 当且仅当 MIME 类型 <code>String</code> 为 <code>null</code>。</li>
     * <li>两个非 <code>null</code> MIME 类型 <code>String</code> 的编码表示相等当且仅当这两个 <code>String</code> 根据 <code>String.equals(Object)</code> 相等。</li>
     * </ul>
     * <p>
     * 该方法的参考实现返回指定的 MIME 类型 <code>String</code> 前缀为 <code>JAVA_DATAFLAVOR:</code>。
     *
     * @param mimeType 要编码的 MIME 类型
     * @return 编码的 <code>String</code>，如果 mimeType 为 <code>null</code> 则返回 <code>null</code>
     */
    public static String encodeJavaMIMEType(String mimeType) {
        return (mimeType != null)
            ? JavaMIME + mimeType
            : null;
    }

    /**
     * 编码 <code>DataFlavor</code> 以用作 <code>String</code> 本地。编码 <code>DataFlavor</code> 的格式取决于实现。
     * 唯一的限制是：
     * <ul>
     * <li>编码表示为 <code>null</code> 当且仅当指定的 <code>DataFlavor</code> 为 <code>null</code> 或其 MIME 类型 <code>String</code> 为 <code>null</code>。</li>
     * <li>两个非 <code>null</code> <code>DataFlavor</code> 的编码表示相等当且仅当这两个 <code>DataFlavor</code> 的 MIME 类型 <code>String</code> 根据 <code>String.equals(Object)</code> 相等。</li>
     * </ul>
     * <p>
     * 该方法的参考实现返回指定 <code>DataFlavor</code> 的 MIME 类型 <code>String</code> 前缀为 <code>JAVA_DATAFLAVOR:</code>。
     *
     * @param flav 要编码的 <code>DataFlavor</code>
     * @return 编码的 <code>String</code>，如果 flav 为 <code>null</code> 或其 MIME 类型为 <code>null</code> 则返回 <code>null</code>
     */
    public static String encodeDataFlavor(DataFlavor flav) {
        return (flav != null)
            ? SystemFlavorMap.encodeJavaMIMEType(flav.getMimeType())
            : null;
    }

    /**
     * 返回指定的 <code>String</code> 是否为编码的 Java MIME 类型。
     *
     * @param str 要测试的 <code>String</code>
     * @return 如果 <code>String</code> 是编码的，则返回 <code>true</code>；否则返回 <code>false</code>
     */
    public static boolean isJavaMIMEType(String str) {
        return (str != null && str.startsWith(JavaMIME, 0));
    }

    /**
     * 解码 <code>String</code> 本地以用作 Java MIME 类型。
     *
     * @param nat 要解码的 <code>String</code>
     * @return 解码的 Java MIME 类型，如果 nat 不是编码的 <code>String</code> 本地则返回 <code>null</code>
     */
    public static String decodeJavaMIMEType(String nat) {
        return (isJavaMIMEType(nat))
            ? nat.substring(JavaMIME.length(), nat.length()).trim()
            : null;
    }

    /**
     * 解码 <code>String</code> 本地以用作 <code>DataFlavor</code>。
     *
     * @param nat 要解码的 <code>String</code>
     * @return 解码的 <code>DataFlavor</code>，如果 nat 不是编码的 <code>String</code> 本地则返回 <code>null</code>
     */
    public static DataFlavor decodeDataFlavor(String nat)
        throws ClassNotFoundException
    {
        String retval_str = SystemFlavorMap.decodeJavaMIMEType(nat);
        return (retval_str != null)
            ? new DataFlavor(retval_str)
            : null;
    }

    private static final class SoftCache<K, V> {
        Map<K, SoftReference<LinkedHashSet<V>>> cache;

        public void put(K key, LinkedHashSet<V> value) {
            if (cache == null) {
                cache = new HashMap<>(1);
            }
            cache.put(key, new SoftReference<>(value));
        }

        public void remove(K key) {
            if (cache == null) return;
            cache.remove(null);
            cache.remove(key);
        }

        public LinkedHashSet<V> check(K key) {
            if (cache == null) return null;
            SoftReference<LinkedHashSet<V>> ref = cache.get(key);
            if (ref != null) {
                return ref.get();
            }
            return null;
        }
    }
}
