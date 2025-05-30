
/*
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.util.prefs;

import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;

/**
 * 为 java.util.prefs 提供 XML 支持。用于导入和导出偏好节点和子树的方法。
 *
 * @author  Josh Bloch 和 Mark Reinhold
 * @see     Preferences
 * @since   1.4
 */
class XmlSupport {
    // 导出偏好设置时所需的 DTD URI
    private static final String PREFS_DTD_URI =
        "http://java.sun.com/dtd/preferences.dtd";

    // 与 URI 对应的实际 DTD
    private static final String PREFS_DTD =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +

        "<!-- DTD for preferences -->"               +

        "<!ELEMENT preferences (root) >"             +
        "<!ATTLIST preferences"                      +
        " EXTERNAL_XML_VERSION CDATA \"0.0\"  >"     +

        "<!ELEMENT root (map, node*) >"              +
        "<!ATTLIST root"                             +
        "          type (system|user) #REQUIRED >"   +

        "<!ELEMENT node (map, node*) >"              +
        "<!ATTLIST node"                             +
        "          name CDATA #REQUIRED >"           +

        "<!ELEMENT map (entry*) >"                   +
        "<!ATTLIST map"                              +
        "  MAP_XML_VERSION CDATA \"0.0\"  >"         +
        "<!ELEMENT entry EMPTY >"                    +
        "<!ATTLIST entry"                            +
        "          key CDATA #REQUIRED"              +
        "          value CDATA #REQUIRED >"          ;
    /**
     * 导出偏好设置文件的格式版本号。
     */
    private static final String EXTERNAL_XML_VERSION = "1.0";

    /*
     * 内部映射文件的版本号。
     */
    private static final String MAP_XML_VERSION = "1.0";

    /**
     * 将指定的偏好节点及其所有子节点（如果 subTree 为 true）导出到指定的输出流。偏好设置以符合 Preferences 规范定义的 XML 文档形式导出。
     *
     * @throws IOException 如果写入指定的输出流时发生 <tt>IOException</tt>。
     * @throws BackingStoreException 如果无法从后端存储读取偏好数据。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link Preferences#removeNode()} 方法移除。
     */
    static void export(OutputStream os, final Preferences p, boolean subTree)
        throws IOException, BackingStoreException {
        if (((AbstractPreferences)p).isRemoved())
            throw new IllegalStateException("Node has been removed");
        Document doc = createPrefsDoc("preferences");
        Element preferences =  doc.getDocumentElement() ;
        preferences.setAttribute("EXTERNAL_XML_VERSION", EXTERNAL_XML_VERSION);
        Element xmlRoot =  (Element)
        preferences.appendChild(doc.createElement("root"));
        xmlRoot.setAttribute("type", (p.isUserNode() ? "user" : "system"));

        // 从 p 到根节点的自底向上节点列表，不包括根节点
        List<Preferences> ancestors = new ArrayList<>();

        for (Preferences kid = p, dad = kid.parent(); dad != null;
                                   kid = dad, dad = kid.parent()) {
            ancestors.add(kid);
        }
        Element e = xmlRoot;
        for (int i=ancestors.size()-1; i >= 0; i--) {
            e.appendChild(doc.createElement("map"));
            e = (Element) e.appendChild(doc.createElement("node"));
            e.setAttribute("name", ancestors.get(i).name());
        }
        putPreferencesInXml(e, doc, p, subTree);

        writeDoc(doc, os);
    }

    /**
     * 将指定的 Preferences 节点中的偏好设置放入指定的 XML 元素中，该元素假定为指定 XML 文档中的一个节点，且该文档符合 PREFS_DTD。如果 subTree 为 true，则创建指定 XML 节点的所有子节点，这些子节点对应于指定 Preferences 节点的所有子节点，并递归处理。
     *
     * @throws BackingStoreException 如果无法从指定的偏好节点读取偏好设置或子节点。
     */
    private static void putPreferencesInXml(Element elt, Document doc,
               Preferences prefs, boolean subTree) throws BackingStoreException
    {
        Preferences[] kidsCopy = null;
        String[] kidNames = null;

        // 锁定节点以导出其内容并获取子节点的副本，然后释放锁，
        // 如果 subTree 为 true，则对子节点进行递归调用
        synchronized (((AbstractPreferences)prefs).lock) {
            // 检查此节点是否已被并发移除。如果是，则从 XML 文档中移除并返回。
            if (((AbstractPreferences)prefs).isRemoved()) {
                elt.getParentNode().removeChild(elt);
                return;
            }
            // 将映射放入 XML 元素中
            String[] keys = prefs.keys();
            Element map = (Element) elt.appendChild(doc.createElement("map"));
            for (int i=0; i<keys.length; i++) {
                Element entry = (Element)
                    map.appendChild(doc.createElement("entry"));
                entry.setAttribute("key", keys[i]);
                // 下一句抛出空指针异常而不是断言失败
                entry.setAttribute("value", prefs.get(keys[i], null));
            }
            // 适当递归
            if (subTree) {
                /* 在持有锁时获取子节点的副本 */
                kidNames = prefs.childrenNames();
                kidsCopy = new Preferences[kidNames.length];
                for (int i = 0; i <  kidNames.length; i++)
                    kidsCopy[i] = prefs.node(kidNames[i]);
            }
            // 释放锁
        }

        if (subTree) {
            for (int i=0; i < kidNames.length; i++) {
                Element xmlKid = (Element)
                    elt.appendChild(doc.createElement("node"));
                xmlKid.setAttribute("name", kidNames[i]);
                putPreferencesInXml(xmlKid, doc, kidsCopy[i], subTree);
            }
        }
    }

    /**
     * 从指定的输入流导入偏好设置，该输入流假定包含符合 Preferences 规范描述的 XML 文档。
     *
     * @throws IOException 如果从指定的输出流读取时发生 <tt>IOException</tt>。
     * @throws InvalidPreferencesFormatException 输入流中的数据不构成具有强制文档类型的有效 XML 文档。
     */
    static void importPreferences(InputStream is)
        throws IOException, InvalidPreferencesFormatException
    {
        try {
            Document doc = loadPrefsDoc(is);
            String xmlVersion =
                doc.getDocumentElement().getAttribute("EXTERNAL_XML_VERSION");
            if (xmlVersion.compareTo(EXTERNAL_XML_VERSION) > 0)
                throw new InvalidPreferencesFormatException(
                "导出的偏好设置文件格式版本 " + xmlVersion +
                " 不受支持。此 Java 安装可以读取 " +
                " 版本 " + EXTERNAL_XML_VERSION + " 或更早版本。您可能需要" +
                " 安装更新版本的 JDK。");

            Element xmlRoot = (Element) doc.getDocumentElement().
                                               getChildNodes().item(0);
            Preferences prefsRoot =
                (xmlRoot.getAttribute("type").equals("user") ?
                            Preferences.userRoot() : Preferences.systemRoot());
            ImportSubtree(prefsRoot, xmlRoot);
        } catch(SAXException e) {
            throw new InvalidPreferencesFormatException(e);
        }
    }

    /**
     * 创建新的偏好设置 XML 文档。
     */
    private static Document createPrefsDoc( String qname ) {
        try {
            DOMImplementation di = DocumentBuilderFactory.newInstance().
                newDocumentBuilder().getDOMImplementation();
            DocumentType dt = di.createDocumentType(qname, null, PREFS_DTD_URI);
            return di.createDocument(null, qname, dt);
        } catch(ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 从指定的输入流加载 XML 文档，该输入流必须具有必需的 DTD URI。
     */
    private static Document loadPrefsDoc(InputStream in)
        throws SAXException, IOException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setValidating(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringComments(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setEntityResolver(new Resolver());
            db.setErrorHandler(new EH());
            return db.parse(new InputSource(in));
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 将 XML 文档写入指定的输出流。
     */
    private static final void writeDoc(Document doc, OutputStream out)
        throws IOException
    {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            try {
                tf.setAttribute("indent-number", new Integer(2));
            } catch (IllegalArgumentException iae) {
                // 忽略 IAE。即使转换器提供者不支持 "indent-number"，也不应导致写入失败。
            }
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doc.getDoctype().getSystemId());
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            // 如果 "result" 是包含 OutputStream 对象的 StreamResult，则 Transformer 会重置 "indent" 信息，
            // 但在 OutputStream 对象上创建 Writer 对象则可以正常工作。
            t.transform(new DOMSource(doc),
                        new StreamResult(new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))));
        } catch(TransformerException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 递归遍历指定的偏好节点，并将描述的偏好设置存储到系统或当前用户的偏好树中，具体取决于情况。
     */
    private static void ImportSubtree(Preferences prefsNode, Element xmlNode) {
        NodeList xmlKids = xmlNode.getChildNodes();
        int numXmlKids = xmlKids.getLength();
        /*
         * 首先锁定节点，导入其内容并获取子节点。然后解锁节点并转到子节点
         * 由于某些子节点可能已被并发删除，因此需要检查这一点。
         */
        Preferences[] prefsKids;
        /* 锁定节点 */
        synchronized (((AbstractPreferences)prefsNode).lock) {
            // 如果已被移除，则静默返回
            if (((AbstractPreferences)prefsNode).isRemoved())
                return;

            // 导入此节点的任何偏好设置
            Element firstXmlKid = (Element) xmlKids.item(0);
            ImportPrefs(prefsNode, firstXmlKid);
            prefsKids = new Preferences[numXmlKids - 1];

            // 获取相关子节点
            for (int i=1; i < numXmlKids; i++) {
                Element xmlKid = (Element) xmlKids.item(i);
                prefsKids[i-1] = prefsNode.node(xmlKid.getAttribute("name"));
            }
        } // 解锁节点
        // 导入子节点
        for (int i=1; i < numXmlKids; i++)
            ImportSubtree(prefsKids[i-1], (Element)xmlKids.item(i));
    }

    /**
     * 将指定 XML 元素（偏好文档中的映射）描述的偏好设置导入到指定的偏好节点中。
     */
    private static void ImportPrefs(Preferences prefsNode, Element map) {
        NodeList entries = map.getChildNodes();
        for (int i=0, numEntries = entries.getLength(); i < numEntries; i++) {
            Element entry = (Element) entries.item(i);
            prefsNode.put(entry.getAttribute("key"),
                          entry.getAttribute("value"));
        }
    }

    /**
     * 将指定的 Map<String,String> 导出到指定的输出流上的映射文档，符合 prefs DTD。这是 FileSystemPrefs 的内部（未记录的）格式。
     *
     * @throws IOException 如果写入指定的输出流时发生 <tt>IOException</tt>。
     */
    static void exportMap(OutputStream os, Map<String, String> map) throws IOException {
        Document doc = createPrefsDoc("map");
        Element xmlMap = doc.getDocumentElement( ) ;
        xmlMap.setAttribute("MAP_XML_VERSION", MAP_XML_VERSION);

        for (Iterator<Map.Entry<String, String>> i = map.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, String> e = i.next();
            Element xe = (Element)
                xmlMap.appendChild(doc.createElement("entry"));
            xe.setAttribute("key",   e.getKey());
            xe.setAttribute("value", e.getValue());
        }

        writeDoc(doc, os);
    }

    /**
     * 从指定的输入流导入 Map，该输入流假定包含符合 prefs DTD 的映射文档。这是 FileSystemPrefs 的内部（未记录的）格式。XML 文档中指定的键值对将放入指定的 Map 中。（如果此 Map 为空，则当此方法返回时，它将包含 XML 文档中的所有键值对。）
     *
     * @throws IOException 如果从指定的输出流读取时发生 <tt>IOException</tt>。
     * @throws InvalidPreferencesFormatException 输入流中的数据不构成具有强制文档类型的有效 XML 文档。
     */
    static void importMap(InputStream is, Map<String, String> m)
        throws IOException, InvalidPreferencesFormatException
    {
        try {
            Document doc = loadPrefsDoc(is);
            Element xmlMap = doc.getDocumentElement();
            // 检查版本
            String mapVersion = xmlMap.getAttribute("MAP_XML_VERSION");
            if (mapVersion.compareTo(MAP_XML_VERSION) > 0)
                throw new InvalidPreferencesFormatException(
                "偏好映射文件格式版本 " + mapVersion +
                " 不受支持。此 Java 安装可以读取 " +
                " 版本 " + MAP_XML_VERSION + " 或更早版本。您可能需要" +
                " 安装更新版本的 JDK。");


                        NodeList entries = xmlMap.getChildNodes();
            for (int i=0, numEntries=entries.getLength(); i<numEntries; i++) {
                Element entry = (Element) entries.item(i);
                m.put(entry.getAttribute("key"), entry.getAttribute("value"));
            }
        } catch(SAXException e) {
            throw new InvalidPreferencesFormatException(e);
        }
    }

    private static class Resolver implements EntityResolver {
        public InputSource resolveEntity(String pid, String sid)
            throws SAXException
        {
            if (sid.equals(PREFS_DTD_URI)) {
                InputSource is;
                is = new InputSource(new StringReader(PREFS_DTD));
                is.setSystemId(PREFS_DTD_URI);
                return is;
            }
            throw new SAXException("无效的系统标识符: " + sid);
        }
    }

    private static class EH implements ErrorHandler {
        public void error(SAXParseException x) throws SAXException {
            throw x;
        }
        public void fatalError(SAXParseException x) throws SAXException {
            throw x;
        }
        public void warning(SAXParseException x) throws SAXException {
            throw x;
        }
    }
}
