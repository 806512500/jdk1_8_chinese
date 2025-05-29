
/*
 * 版权所有 (c) 2003, 2011, Oracle 和/或其附属公司。保留所有权利。
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

/* 我们使用访问所谓的 Windows "环境块" 的 API，
 * 这看起来像一个 jchars 数组，如下所示：
 *
 * FOO=BAR\u0000 ... GORP=QUUX\u0000\u0000
 *
 * 这个数据结构有许多我们必须应对的特殊性：
 * (参见: http://windowssdk.msdn.microsoft.com/en-us/library/ms682009.aspx)
 * - NUL jchar 分隔符，以及双 NUL jchar 终止符。
 *   似乎 Windows 实现要求即使环境为空也必须双 NUL 终止。我们应始终
 *   生成具有双 NUL 终止的环境，同时接受由单个 NUL 组成的空环境。
 * - 在 Windows9x 上，这实际上是一个 8 位字符数组，而不是 jchars，
 *   使用系统默认编码。
 * - 必须按 Unicode 值进行不区分大小写的排序，
 *   仿佛转换为大写。
 * - Windows 维护了一些以 `=' (!) 字符开头的特殊环境变量。
 *   这些用于 Windows 驱动器当前目录（例如 "=C:=C:\WINNT"）或
 *   上一个命令的退出代码（例如 "=ExitCode=0000001"）。
 *
 * 由于 Java 和非 9x Windows 使用相同的字符集，甚至相同的编码，
 * 我们不必处理不可靠的字节流转换。只需添加几个 NUL 终止符。
 *
 * System.getenv(String) 是不区分大小写的，而 System.getenv()
 * 返回一个区分大小写的映射，这与本机 Windows API 一致。
 *
 * 本类中的非私有方法即使在本包内也不供一般使用。相反，它们是
 * 系统无关方法的系统相关部分。除非你的方法名出现在下面，否则
 * 千万不要使用此类。
 *
 * @author Martin Buchholz
 * @since 1.5
 */

package java.lang;

import java.io.*;
import java.util.*;

final class ProcessEnvironment extends HashMap<String,String>
{

    private static final long serialVersionUID = -8017839552603542824L;

    private static String validateName(String name) {
        // 初始的 `=' 表示一个特殊的 Windows 变量名 -- 可接受
        if (name.indexOf('=', 1)   != -1 ||
            name.indexOf('\u0000') != -1)
            throw new IllegalArgumentException
                ("无效的环境变量名: \"" + name + "\"");
        return name;
    }

    private static String validateValue(String value) {
        if (value.indexOf('\u0000') != -1)
            throw new IllegalArgumentException
                ("无效的环境变量值: \"" + value + "\"");
        return value;
    }

    private static String nonNullString(Object o) {
        if (o == null)
            throw new NullPointerException();
        return (String) o;
    }

    public String put(String key, String value) {
        return super.put(validateName(key), validateValue(value));
    }

    public String get(Object key) {
        return super.get(nonNullString(key));
    }

    public boolean containsKey(Object key) {
        return super.containsKey(nonNullString(key));
    }

    public boolean containsValue(Object value) {
        return super.containsValue(nonNullString(value));
    }

    public String remove(Object key) {
        return super.remove(nonNullString(key));
    }

    private static class CheckedEntry
        implements Map.Entry<String,String>
    {
        private final Map.Entry<String,String> e;
        public CheckedEntry(Map.Entry<String,String> e) {this.e = e;}
        public String getKey()   { return e.getKey();}
        public String getValue() { return e.getValue();}
        public String setValue(String value) {
            return e.setValue(validateValue(value));
        }
        public String toString() { return getKey() + "=" + getValue();}
        public boolean equals(Object o) {return e.equals(o);}
        public int hashCode()    {return e.hashCode();}
    }

    private static class CheckedEntrySet
        extends AbstractSet<Map.Entry<String,String>>
    {
        private final Set<Map.Entry<String,String>> s;
        public CheckedEntrySet(Set<Map.Entry<String,String>> s) {this.s = s;}
        public int size()        {return s.size();}
        public boolean isEmpty() {return s.isEmpty();}
        public void clear()      {       s.clear();}
        public Iterator<Map.Entry<String,String>> iterator() {
            return new Iterator<Map.Entry<String,String>>() {
                Iterator<Map.Entry<String,String>> i = s.iterator();
                public boolean hasNext() { return i.hasNext();}
                public Map.Entry<String,String> next() {
                    return new CheckedEntry(i.next());
                }
                public void remove() { i.remove();}
            };
        }
        private static Map.Entry<String,String> checkedEntry(Object o) {
            @SuppressWarnings("unchecked")
            Map.Entry<String,String> e = (Map.Entry<String,String>) o;
            nonNullString(e.getKey());
            nonNullString(e.getValue());
            return e;
        }
        public boolean contains(Object o) {return s.contains(checkedEntry(o));}
        public boolean remove(Object o)   {return s.remove(checkedEntry(o));}
    }

    private static class CheckedValues extends AbstractCollection<String> {
        private final Collection<String> c;
        public CheckedValues(Collection<String> c) {this.c = c;}
        public int size()                  {return c.size();}
        public boolean isEmpty()           {return c.isEmpty();}
        public void clear()                {       c.clear();}
        public Iterator<String> iterator() {return c.iterator();}
        public boolean contains(Object o)  {return c.contains(nonNullString(o));}
        public boolean remove(Object o)    {return c.remove(nonNullString(o));}
    }

    private static class CheckedKeySet extends AbstractSet<String> {
        private final Set<String> s;
        public CheckedKeySet(Set<String> s) {this.s = s;}
        public int size()                  {return s.size();}
        public boolean isEmpty()           {return s.isEmpty();}
        public void clear()                {       s.clear();}
        public Iterator<String> iterator() {return s.iterator();}
        public boolean contains(Object o)  {return s.contains(nonNullString(o));}
        public boolean remove(Object o)    {return s.remove(nonNullString(o));}
    }


                    public Set<String> keySet() {
        return new CheckedKeySet(super.keySet());
    }

    public Collection<String> values() {
        return new CheckedValues(super.values());
    }

    public Set<Map.Entry<String,String>> entrySet() {
        return new CheckedEntrySet(super.entrySet());
    }


    private static final class NameComparator
        implements Comparator<String> {
        public int compare(String s1, String s2) {
            // 我们不能使用 String.compareToIgnoreCase，因为它会将字符串转换为小写，而 Windows
            // 会将字符串转换为大写！例如，"_" 应该排在 "Z" 之后，而不是之前。
            int n1 = s1.length();
            int n2 = s2.length();
            int min = Math.min(n1, n2);
            for (int i = 0; i < min; i++) {
                char c1 = s1.charAt(i);
                char c2 = s2.charAt(i);
                if (c1 != c2) {
                    c1 = Character.toUpperCase(c1);
                    c2 = Character.toUpperCase(c2);
                    if (c1 != c2)
                        // 没有溢出，因为有数值提升
                        return c1 - c2;
                }
            }
            return n1 - n2;
        }
    }

    private static final class EntryComparator
        implements Comparator<Map.Entry<String,String>> {
        public int compare(Map.Entry<String,String> e1,
                           Map.Entry<String,String> e2) {
            return nameComparator.compare(e1.getKey(), e2.getKey());
        }
    }

    // 允许 `=' 作为名称的第一个字符，例如 =C:=C:\DIR
    static final int MIN_NAME_LENGTH = 1;

    private static final NameComparator nameComparator;
    private static final EntryComparator entryComparator;
    private static final ProcessEnvironment theEnvironment;
    private static final Map<String,String> theUnmodifiableEnvironment;
    private static final Map<String,String> theCaseInsensitiveEnvironment;

    static {
        nameComparator  = new NameComparator();
        entryComparator = new EntryComparator();
        theEnvironment  = new ProcessEnvironment();
        theUnmodifiableEnvironment
            = Collections.unmodifiableMap(theEnvironment);

        String envblock = environmentBlock();
        int beg, end, eql;
        for (beg = 0;
             ((end = envblock.indexOf('\u0000', beg  )) != -1 &&
              // 初始的 `=' 表示一个特殊的 Windows 变量名 —— 允许
              (eql = envblock.indexOf('='     , beg+1)) != -1);
             beg = end + 1) {
            // 忽略损坏的环境字符串。
            if (eql < end)
                theEnvironment.put(envblock.substring(beg, eql),
                                   envblock.substring(eql+1,end));
        }

        theCaseInsensitiveEnvironment = new TreeMap<>(nameComparator);
        theCaseInsensitiveEnvironment.putAll(theEnvironment);
    }

    private ProcessEnvironment() {
        super();
    }

    private ProcessEnvironment(int capacity) {
        super(capacity);
    }

    // 仅用于 System.getenv(String)
    static String getenv(String name) {
        // 原始实现使用了本地调用 _wgetenv，
        // 但事实证明，_wgetenv 仅在使用 `wmain' 而不是 `main' 时
        // 与 GetEnvironmentStringsW（对于非 ASCII 字符）一致，即使在使用
        // CREATE_UNICODE_ENVIRONMENT 创建的进程中也是如此。因此，我们自己执行
        // 不区分大小写的比较。至少这保证了 System.getenv().get(String) 将与
        // System.getenv(String) 一致。
        return theCaseInsensitiveEnvironment.get(name);
    }

    // 仅用于 System.getenv()
    static Map<String,String> getenv() {
        return theUnmodifiableEnvironment;
    }

    // 仅用于 ProcessBuilder.environment()
    @SuppressWarnings("unchecked")
    static Map<String,String> environment() {
        return (Map<String,String>) theEnvironment.clone();
    }

    // 仅用于 ProcessBuilder.environment(String[] envp)
    static Map<String,String> emptyEnvironment(int capacity) {
        return new ProcessEnvironment(capacity);
    }

    private static native String environmentBlock();

    // 仅用于 ProcessImpl.start()
    String toEnvironmentBlock() {
        // 按名称进行 Unicode 不区分大小写的排序
        List<Map.Entry<String,String>> list = new ArrayList<>(entrySet());
        Collections.sort(list, entryComparator);

        StringBuilder sb = new StringBuilder(size()*30);
        int cmp = -1;

        // 某些版本的 MSVCRT.DLL 要求设置 SystemRoot。
        // 因此，我们确保它总是被设置，即使调用者没有提供。
        final String SYSTEMROOT = "SystemRoot";

        for (Map.Entry<String,String> e : list) {
            String key = e.getKey();
            String value = e.getValue();
            if (cmp < 0 && (cmp = nameComparator.compare(key, SYSTEMROOT)) > 0) {
                // 未设置，所以在这里添加
                addToEnvIfSet(sb, SYSTEMROOT);
            }
            addToEnv(sb, key, value);
        }
        if (cmp < 0) {
            // 列表结束时仍未找到
            addToEnvIfSet(sb, SYSTEMROOT);
        }
        if (sb.length() == 0) {
            // 环境为空且父进程中未设置 SystemRoot
            sb.append('\u0000');
        }
        // 块以双 NUL 结束
        sb.append('\u0000');
        return sb.toString();
    }

    // 如果父进程中存在该环境变量，则将其添加到子进程中
    private static void addToEnvIfSet(StringBuilder sb, String name) {
        String s = getenv(name);
        if (s != null)
            addToEnv(sb, name, s);
    }

    private static void addToEnv(StringBuilder sb, String name, String val) {
        sb.append(name).append('=').append(val).append('\u0000');
    }

    static String toEnvironmentBlock(Map<String,String> map) {
        return map == null ? null :
            ((ProcessEnvironment)map).toEnvironmentBlock();
    }
}
