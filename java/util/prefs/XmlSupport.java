
/*
 * 版权所有 (c) 2002, 2012, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;

/**
 * java.util.prefs 的 XML 支持。用于导入和导出首选项节点和子树的方法。
 *
 * @author  Josh Bloch 和 Mark Reinhold
 * @see     Preferences
 * @since   1.4
 */
class XmlSupport {
    // 导出首选项所需的 DTD URI
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
     * 导出首选项文件格式的版本号。
     */
    private static final String EXTERNAL_XML_VERSION = "1.0";

    /*
     * 内部映射文件的版本号。
     */
    private static final String MAP_XML_VERSION = "1.0";

    /**
     * 将指定的首选项节点及其所有子节点（如果 subTree 为 true）导出到指定的输出流。首选项以符合 Preferences 规范定义的 XML 文档形式导出。
     *
     * @throws IOException 如果写入指定的输出流时发生 <tt>IOException</tt>。
     * @throws BackingStoreException 如果无法从后端存储读取首选项数据。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link Preferences#removeNode()} 方法移除。
     */
    static void export(OutputStream os, final Preferences p, boolean subTree)
        throws IOException, BackingStoreException {
        if (((AbstractPreferences)p).isRemoved())
            throw new IllegalStateException("节点已被移除");
        Document doc = createPrefsDoc("preferences");
        Element preferences =  doc.getDocumentElement() ;
        preferences.setAttribute("EXTERNAL_XML_VERSION", EXTERNAL_XML_VERSION);
        Element xmlRoot =  (Element)
        preferences.appendChild(doc.createElement("root"));
        xmlRoot.setAttribute("type", (p.isUserNode() ? "user" : "system"));

        // 获取从 p 到根节点的自底向上节点列表，不包括根节点
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
     * 将指定的 Preferences 节点中的首选项放入指定的 XML 元素中，假设该 XML 元素代表指定 XML 文档中的一个节点，并且该文档符合 PREFS_DTD。如果 subTree 为 true，则创建与指定 Preferences 节点的所有子节点相对应的指定 XML 节点的子节点并递归。
     *
     * @throws BackingStoreException 如果无法从指定的首选项节点读取首选项或子节点。
     */
    private static void putPreferencesInXml(Element elt, Document doc,
               Preferences prefs, boolean subTree) throws BackingStoreException
    {
        Preferences[] kidsCopy = null;
        String[] kidNames = null;

        // 锁定节点以导出其内容并获取子节点的副本，然后释放锁，
        // 如果 subTree = true，则对子节点进行递归调用
        synchronized (((AbstractPreferences)prefs).lock) {
            // 检查此节点是否被并发移除。如果是，
            // 从 XML 文档中移除此节点并返回。
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
                // 下一条语句抛出空指针异常而不是断言失败
                entry.setAttribute("value", prefs.get(keys[i], null));
            }
            // 适当情况下递归
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
     * 从指定的输入流导入偏好设置，假设该输入流包含一个符合 Preferences 规范描述的 XML 文档。
     *
     * @throws IOException 如果从指定的输出流读取导致 <tt>IOException</tt>。
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
                " 不受支持。此 Java 安装可以读取" +
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
     * 创建一个新的偏好设置 XML 文档。
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
                // 忽略 IAE。即使变换器提供者不支持 "indent-number"，也不应导致写入失败。
            }
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doc.getDoctype().getSystemId());
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            // 如果 "result" 是一个包含 OutputStream 对象的 StreamResult，Transformer 会重置 "indent" 信息。
            // 但是，在该 OutputStream 对象上创建一个 Writer 对象可以解决这个问题。
            t.transform(new DOMSource(doc),
                        new StreamResult(new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))));
        } catch(TransformerException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 递归遍历指定的偏好设置节点，并将描述的偏好设置存储到适当的系统或当前用户偏好设置树中。
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
            // 如果已删除，静默返回
            if (((AbstractPreferences)prefsNode).isRemoved())
                return;

            // 导入此节点的任何偏好设置
            Element firstXmlKid = (Element) xmlKids.item(0);
            ImportPrefs(prefsNode, firstXmlKid);
            prefsKids = new Preferences[numXmlKids - 1];

            // 获取涉及的子节点
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
     * 将由指定的 XML 元素（偏好设置文档中的映射）描述的偏好设置导入到指定的偏好设置节点中。
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
     * 将指定的 Map<String,String> 导出到指定的 OutputStream 上的映射文档，
     * 根据 prefs DTD。这是用于 FileSystemPrefs 的内部（未记录）格式。
     *
     * @throws IOException 如果写入指定的输出流导致 <tt>IOException</tt>。
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
     * 从指定的输入流导入映射，假设该输入流包含符合 prefs DTD 的映射文档。
     * 这是用于 FileSystemPrefs 的内部（未记录）格式。XML 文档中指定的键值对将被放入指定的 Map 中。
     * （如果此 Map 为空，则当此方法返回时，它将包含 XML 文档中的所有键值对。）
     *
     * @throws IOException 如果从指定的输出流读取导致 <tt>IOException</tt>。
     * @throws InvalidPreferencesFormatException 输入流中的数据不构成具有强制文档类型的有效的 XML 文档。
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
                "Preferences map file format version " + mapVersion +
                " is not supported. This java installation can read" +
                " versions " + MAP_XML_VERSION + " or older. You may need" +
                " to install a newer version of JDK.");

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
            throw new SAXException("Invalid system identifier: " + sid);
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
