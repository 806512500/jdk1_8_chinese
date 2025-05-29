
/*
 * 版权所有 (c) 2009, 2013, Oracle 和/或其附属公司。保留所有权利。
 * 版权所有 2009 Google Inc. 保留所有权利。
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

package java.util;

/**
 * 一种稳定的、自适应的、迭代的归并排序，当运行在部分排序的数组上时，需要的比较次数远少于 n lg(n)，
 * 而在随机数组上运行时，性能与传统的归并排序相当。像所有正确的归并排序一样，这种排序是稳定的，
 * 并且运行时间为 O(n log n)（最坏情况）。在最坏的情况下，这种排序需要临时存储 n/2 个对象引用的空间；
 * 在最好的情况下，它只需要少量的常量空间。
 *
 * 此实现是从 Tim Peters 为 Python 编写的列表排序中改编而来的，详细描述如下：
 *
 *   http://svn.python.org/projects/python/trunk/Objects/listsort.txt
 *
 * Tim 的 C 代码可以在以下位置找到：
 *
 *   http://svn.python.org/projects/python/trunk/Objects/listobject.c
 *
 * 底层技术在以下论文中有所描述（可能有更早的起源）：
 *
 *  "Optimistic Sorting and Information Theoretic Complexity"
 *  Peter McIlroy
 *  SODA (第四届年度 ACM-SIAM 离散算法研讨会),
 *  pp 467-474, Austin, Texas, 25-27 January 1993.
 *
 * 虽然此类的 API 仅由静态方法组成，但它（私有地）可实例化；TimSort 实例保存正在进行的排序的状态，
 * 假设输入数组足够大，值得使用完整的 TimSort。小数组使用二分插入排序原地排序。
 *
 * @author Josh Bloch
 */
class TimSort<T> {
    /**
     * 这是最小的将被合并的序列长度。较短的序列将通过调用 binarySort 延长。如果整个数组小于这个长度，
     * 将不会执行任何合并。
     *
     * 此常量应该是 2 的幂。在 Tim Peter 的 C 实现中，它是 64，但在此实现中，经验确定 32 更好。如果你将此常量设置为
     * 不是 2 的幂的数字，你将需要更改 {@link #minRunLength} 计算。
     *
     * 如果你减小此常量，你必须更改 TimSort 构造函数中的 stackLen 计算，否则你可能会遇到
     * ArrayOutOfBounds 异常。有关作为数组长度和最小合并序列长度的函数的最小堆栈长度的讨论，请参见 listsort.txt。
     */
    private static final int MIN_MERGE = 32;

    /**
     * 正在排序的数组。
     */
    private final T[] a;

    /**
     * 此排序的比较器。
     */
    private final Comparator<? super T> c;

    /**
     * 当我们进入跳跃模式时，我们将保持这种状态，直到两个运行都少于 MIN_GALLOP 次连续获胜。
     */
    private static final int  MIN_GALLOP = 7;

    /**
     * 这控制我们何时进入跳跃模式。它初始化为 MIN_GALLOP。mergeLo 和 mergeHi 方法对随机数据进行调整，
     * 对高度结构化的数据进行降低。
     */
    private int minGallop = MIN_GALLOP;

    /**
     * 用于合并的最大初始临时数组大小。该数组可以增长以适应需求。
     *
     * 与 Tim 的原始 C 版本不同，我们在排序较小的数组时不会分配这么多存储空间。此更改是为了性能考虑。
     */
    private static final int INITIAL_TMP_STORAGE_LENGTH = 256;

    /**
     * 用于合并的临时存储。可以在构造函数中选择性地提供一个工作空间数组，如果提供且足够大，将使用它。
     */
    private T[] tmp;
    private int tmpBase; // tmp 数组切片的基址
    private int tmpLen;  // tmp 数组切片的长度

    /**
     * 一个待合并的运行堆栈。运行 i 从地址 base[i] 开始，扩展 len[i] 个元素。只要索引在范围内，就总是满足：
     *
     *     runBase[i] + runLen[i] == runBase[i + 1]
     *
     * 因此我们可以减少此存储，但这是一个较小的量，保持所有信息显式简化了代码。
     */
    private int stackSize = 0;  // 堆栈上的待合并运行数
    private final int[] runBase;
    private final int[] runLen;

    /**
     * 创建一个 TimSort 实例以维护正在进行的排序的状态。
     *
     * @param a 要排序的数组
     * @param c 确定排序顺序的比较器
     * @param work 工作空间数组（切片）
     * @param workBase 工作数组中可用空间的起始位置
     * @param workLen 工作数组的可用大小
     */
    private TimSort(T[] a, Comparator<? super T> c, T[] work, int workBase, int workLen) {
        this.a = a;
        this.c = c;

        // 分配临时存储（如果必要，可以稍后增加）
        int len = a.length;
        int tlen = (len < 2 * INITIAL_TMP_STORAGE_LENGTH) ?
            len >>> 1 : INITIAL_TMP_STORAGE_LENGTH;
        if (work == null || workLen < tlen || workBase + tlen > work.length) {
            @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
            T[] newArray = (T[])java.lang.reflect.Array.newInstance
                (a.getClass().getComponentType(), tlen);
            tmp = newArray;
            tmpBase = 0;
            tmpLen = tlen;
        }
        else {
            tmp = work;
            tmpBase = workBase;
            tmpLen = workLen;
        }

        /*
         * 分配待合并运行堆栈（不能扩展）。堆栈长度要求在 listsort.txt 中描述。C 版本始终使用相同的堆栈长度（85），
         * 但测量表明，当在 Java 中对“中等大小”的数组（例如，100 个元素）进行排序时，这过于昂贵。
         * 因此，我们为较小的数组使用较小（但足够大）的堆栈长度。下面计算中的“魔法数字”必须在减小 MIN_MERGE 时更改。
         * 有关更多信息，请参见 MIN_MERGE 声明。最大值 49 允许数组长度达到 Integer.MAX_VALUE-4，
         * 如果数组按最坏情况堆栈大小增加场景填充。更多解释见：
         * http://envisage-project.eu/wp-content/uploads/2015/02/sorting.pdf
         */
        int stackLen = (len <    120  ?  5 :
                        len <   1542  ? 10 :
                        len < 119151  ? 24 : 49);
        runBase = new int[stackLen];
        runLen = new int[stackLen];
    }

                /*
     * 下一个方法（包私有且静态）构成了此类的整个API。
     */

    /**
     * 对给定范围进行排序，尽可能使用给定的工作区数组切片作为临时存储。此方法设计为在执行任何必要的数组边界检查并将参数扩展为所需形式后，从公共方法（在Arrays类中）调用。
     *
     * @param a 要排序的数组
     * @param lo 要排序的第一个元素的索引（包含）
     * @param hi 要排序的最后一个元素的索引（不包含）
     * @param c 使用的比较器
     * @param work 工作区数组（切片）
     * @param workBase 工作区数组中可用空间的起始位置
     * @param workLen 工作区数组的可用大小
     * @since 1.8
     */
    static <T> void sort(T[] a, int lo, int hi, Comparator<? super T> c,
                         T[] work, int workBase, int workLen) {
        assert c != null && a != null && lo >= 0 && lo <= hi && hi <= a.length;

        int nRemaining  = hi - lo;
        if (nRemaining < 2)
            return;  // 长度为0和1的数组总是已排序

        // 如果数组较小，执行没有合并的“迷你TimSort”
        if (nRemaining < MIN_MERGE) {
            int initRunLen = countRunAndMakeAscending(a, lo, hi, c);
            binarySort(a, lo, hi, lo + initRunLen, c);
            return;
        }

        /**
         * 从左到右遍历数组一次，找到自然运行序列，将短的自然运行序列扩展到minRun元素，并合并运行以维护堆栈不变性。
         */
        TimSort<T> ts = new TimSort<>(a, c, work, workBase, workLen);
        int minRun = minRunLength(nRemaining);
        do {
            // 确定下一个运行序列
            int runLen = countRunAndMakeAscending(a, lo, hi, c);

            // 如果运行序列较短，扩展到min(minRun, nRemaining)
            if (runLen < minRun) {
                int force = nRemaining <= minRun ? nRemaining : minRun;
                binarySort(a, lo, lo + force, lo + runLen, c);
                runLen = force;
            }

            // 将运行序列推入待处理运行序列堆栈，并可能合并
            ts.pushRun(lo, runLen);
            ts.mergeCollapse();

            // 前进以找到下一个运行序列
            lo += runLen;
            nRemaining -= runLen;
        } while (nRemaining != 0);

        // 合并所有剩余的运行序列以完成排序
        assert lo == hi;
        ts.mergeForceCollapse();
        assert ts.stackSize == 1;
    }

    /**
     * 使用二进制插入排序对指定数组的指定部分进行排序。这是对少量元素进行排序的最佳方法。它需要O(n log n)次比较，但O(n^2)次数据移动（最坏情况）。
     *
     * 如果指定范围的初始部分已经排序，此方法可以利用这一点：该方法假设从索引 {@code lo}（包含）到 {@code start}（不包含）的元素已经排序。
     *
     * @param a 要排序的数组中的范围
     * @param lo 要排序的范围中的第一个元素的索引
     * @param hi 要排序的范围中的最后一个元素之后的索引
     * @param start 范围中第一个不是已知排序的元素的索引（{@code lo <= start <= hi}）
     * @param c 用于排序的比较器
     */
    @SuppressWarnings("fallthrough")
    private static <T> void binarySort(T[] a, int lo, int hi, int start,
                                       Comparator<? super T> c) {
        assert lo <= start && start <= hi;
        if (start == lo)
            start++;
        for ( ; start < hi; start++) {
            T pivot = a[start];

            // 设置left（和right）为a[start]（枢轴）所属的索引
            int left = lo;
            int right = start;
            assert left <= right;
            /*
             * 不变量：
             *   pivot >= [lo, left) 中的所有元素。
             *   pivot <  [right, start) 中的所有元素。
             */
            while (left < right) {
                int mid = (left + right) >>> 1;
                if (c.compare(pivot, a[mid]) < 0)
                    right = mid;
                else
                    left = mid + 1;
            }
            assert left == right;

            /*
             * 不变量仍然成立：pivot >= [lo, left) 中的所有元素，且
             * pivot < [left, start) 中的所有元素，因此pivot属于left。注意
             * 如果有等于pivot的元素，left指向它们之后的第一个位置——这就是为什么这种排序是稳定的。
             * 滑动元素以腾出空间放置pivot。
             */
            int n = start - left;  // 需要移动的元素数量
            // switch 只是默认情况下对arraycopy的优化
            switch (n) {
                case 2:  a[left + 2] = a[left + 1];
                case 1:  a[left + 1] = a[left];
                         break;
                default: System.arraycopy(a, left, a, left + 1, n);
            }
            a[left] = pivot;
        }
    }

    /**
     * 返回从指定位置开始的运行序列的长度，并在运行序列是降序时将其反转（确保方法返回时运行序列总是升序）。
     *
     * 运行序列是最长的升序序列，满足：
     *
     *    a[lo] <= a[lo + 1] <= a[lo + 2] <= ...
     *
     * 或最长的降序序列，满足：
     *
     *    a[lo] >  a[lo + 1] >  a[lo + 2] >  ...
     *
     * 为了其在稳定归并排序中的预期用途，"降序"的严格定义是必要的，以便调用可以安全地反转降序序列而不违反稳定性。
     *
     * @param a 要计数并可能反转的运行序列所在的数组
     * @param lo 运行序列中第一个元素的索引
     * @param hi 可能包含在运行序列中的最后一个元素之后的索引。
              要求 {@code lo < hi}。
     * @param c 用于排序的比较器
     * @return 从指定位置开始的运行序列的长度
     */
    private static <T> int countRunAndMakeAscending(T[] a, int lo, int hi,
                                                    Comparator<? super T> c) {
        assert lo < hi;
        int runHi = lo + 1;
        if (runHi == hi)
            return 1;


                    // 查找运行的结束，并在降序时反转范围
        if (c.compare(a[runHi++], a[lo]) < 0) { // 降序
            while (runHi < hi && c.compare(a[runHi], a[runHi - 1]) < 0)
                runHi++;
            reverseRange(a, lo, runHi);
        } else {                              // 升序
            while (runHi < hi && c.compare(a[runHi], a[runHi - 1]) >= 0)
                runHi++;
        }

        return runHi - lo;
    }

    /**
     * 反转指定数组的指定范围。
     *
     * @param a 要反转范围的数组
     * @param lo 要反转范围的第一个元素的索引
     * @param hi 要反转范围的最后一个元素之后的索引
     */
    private static void reverseRange(Object[] a, int lo, int hi) {
        hi--;
        while (lo < hi) {
            Object t = a[lo];
            a[lo++] = a[hi];
            a[hi--] = t;
        }
    }

    /**
     * 返回指定长度数组的最小可接受运行长度。自然运行短于此长度将通过
     * {@link #binarySort} 进行扩展。
     *
     * 大致来说，计算如下：
     *
     *  如果 n < MIN_MERGE，返回 n（太小，不值得使用复杂的方法）。
     *  否则如果 n 是 2 的精确幂，返回 MIN_MERGE/2。
     *  否则返回一个整数 k，MIN_MERGE/2 <= k <= MIN_MERGE，使得 n/k
     *   接近但严格小于 2 的精确幂。
     *
     * 有关理由，请参见 listsort.txt。
     *
     * @param n 要排序的数组的长度
     * @return 要合并的最小运行长度
     */
    private static int minRunLength(int n) {
        assert n >= 0;
        int r = 0;      // 如果有任何 1 位被移出，则变为 1
        while (n >= MIN_MERGE) {
            r |= (n & 1);
            n >>= 1;
        }
        return n + r;
    }

    /**
     * 将指定的运行推送到待合并的运行堆栈上。
     *
     * @param runBase 运行中第一个元素的索引
     * @param runLen  运行中的元素数量
     */
    private void pushRun(int runBase, int runLen) {
        this.runBase[stackSize] = runBase;
        this.runLen[stackSize] = runLen;
        stackSize++;
    }

    /**
     * 检查等待合并的运行堆栈，并合并相邻的运行，直到堆栈不变量重新建立：
     *
     *     1. runLen[i - 3] > runLen[i - 2] + runLen[i - 1]
     *     2. runLen[i - 2] > runLen[i - 1]
     *
     * 每次将新运行推送到堆栈时调用此方法，因此在进入方法时保证 i < stackSize 的不变量。
     */
    private void mergeCollapse() {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n-1] <= runLen[n] + runLen[n+1]) {
                if (runLen[n - 1] < runLen[n + 1])
                    n--;
                mergeAt(n);
            } else if (runLen[n] <= runLen[n + 1]) {
                mergeAt(n);
            } else {
                break; // 不变量已建立
            }
        }
    }

    /**
     * 合并堆栈上的所有运行，直到只剩下一个。此方法调用一次，以完成排序。
     */
    private void mergeForceCollapse() {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] < runLen[n + 1])
                n--;
            mergeAt(n);
        }
    }

    /**
     * 合并堆栈索引 i 和 i+1 处的两个运行。运行 i 必须是倒数第二个或倒数第三个运行。
     * 换句话说，i 必须等于 stackSize-2 或 stackSize-3。
     *
     * @param i 要合并的两个运行中第一个运行的堆栈索引
     */
    private void mergeAt(int i) {
        assert stackSize >= 2;
        assert i >= 0;
        assert i == stackSize - 2 || i == stackSize - 3;

        int base1 = runBase[i];
        int len1 = runLen[i];
        int base2 = runBase[i + 1];
        int len2 = runLen[i + 1];
        assert len1 > 0 && len2 > 0;
        assert base1 + len1 == base2;

        /*
         * 记录合并运行的长度；如果 i 是当前倒数第三个运行，
         * 还将最后一个运行（不参与此次合并）滑动。无论如何，当前运行（i+1）将消失。
         */
        runLen[i] = len1 + len2;
        if (i == stackSize - 3) {
            runBase[i + 1] = runBase[i + 2];
            runLen[i + 1] = runLen[i + 2];
        }
        stackSize--;

        /*
         * 查找运行2中的第一个元素在运行1中的插入位置。运行1中之前的元素可以忽略
         * （因为它们已经就位）。
         */
        int k = gallopRight(a[base2], a, base1, len1, 0, c);
        assert k >= 0;
        base1 += k;
        len1 -= k;
        if (len1 == 0)
            return;

        /*
         * 查找运行1中的最后一个元素在运行2中的插入位置。运行2中后续的元素可以忽略
         * （因为它们已经就位）。
         */
        len2 = gallopLeft(a[base1 + len1 - 1], a, base2, len2, len2 - 1, c);
        assert len2 >= 0;
        if (len2 == 0)
            return;

        // 合并剩余的运行，使用具有 min(len1, len2) 个元素的临时数组
        if (len1 <= len2)
            mergeLo(base1, len1, base2, len2);
        else
            mergeHi(base1, len1, base2, len2);
    }

    /**
     * 查找将指定键插入指定排序范围的位置；如果范围包含一个等于键的元素，
     * 返回最左边相等元素的索引。
     *
     * @param key 要搜索插入点的键
     * @param a 要搜索的数组
     * @param base 范围中第一个元素的索引
     * @param len 范围的长度；必须大于 0
     * @param hint 开始搜索的索引，0 <= hint < n。hint 越接近结果，此方法运行得越快。
     * @param c 用于排序范围和搜索的比较器
     * @return int k,  0 <= k <= n，使得 a[b + k - 1] < key <= a[b + k]，
     *    假设 a[b - 1] 是负无穷大，a[b + n] 是正无穷大。
     *    换句话说，键属于索引 b + k；或者换句话说，
     *    a 的前 k 个元素应该在键之前，最后 n - k 个元素应该在键之后。
     */
    private static <T> int gallopLeft(T key, T[] a, int base, int len, int hint,
                                      Comparator<? super T> c) {
        assert len > 0 && hint >= 0 && hint < len;
        int lastOfs = 0;
        int ofs = 1;
        if (c.compare(key, a[base + hint]) > 0) {
            // 以 a[base+hint+lastOfs] < key <= a[base+hint+ofs] 的方式向右跳跃
            int maxOfs = len - hint;
            while (ofs < maxOfs && c.compare(key, a[base + hint + ofs]) > 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0)   // int 溢出
                    ofs = maxOfs;
            }
            if (ofs > maxOfs)
                ofs = maxOfs;


                        // 使偏移量相对于 base
            lastOfs += hint;
            ofs += hint;
        } else { // key <= a[base + hint]
            // 向左跳跃，直到 a[base+hint-ofs] < key <= a[base+hint-lastOfs]
            final int maxOfs = hint + 1;
            while (ofs < maxOfs && c.compare(key, a[base + hint - ofs]) <= 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0)   // int 溢出
                    ofs = maxOfs;
            }
            if (ofs > maxOfs)
                ofs = maxOfs;

            // 使偏移量相对于 base
            int tmp = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmp;
        }
        assert -1 <= lastOfs && lastOfs < ofs && ofs <= len;

        /*
         * 现在 a[base+lastOfs] < key <= a[base+ofs]，所以 key 应该位于 lastOfs 的右侧，
         * 但不超过 ofs。执行二分查找，保持不变性 a[base + lastOfs - 1] < key <= a[base + ofs]。
         */
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);

            if (c.compare(key, a[base + m]) > 0)
                lastOfs = m + 1;  // a[base + m] < key
            else
                ofs = m;          // key <= a[base + m]
        }
        assert lastOfs == ofs;    // 所以 a[base + ofs - 1] < key <= a[base + ofs]
        return ofs;
    }

    /**
     * 类似于 gallopLeft，但如果范围中包含一个等于 key 的元素，gallopRight 返回最右侧相等元素之后的索引。
     *
     * @param key 要搜索的键的插入点
     * @param a 要搜索的数组
     * @param base 范围中第一个元素的索引
     * @param len 范围的长度；必须 > 0
     * @param hint 开始搜索的索引，0 <= hint < n。
     *     hint 越接近结果，此方法运行得越快。
     * @param c 用于对范围进行排序和搜索的比较器
     * @return int k,  0 <= k <= n 使得 a[b + k - 1] <= key < a[b + k]
     */
    private static <T> int gallopRight(T key, T[] a, int base, int len,
                                       int hint, Comparator<? super T> c) {
        assert len > 0 && hint >= 0 && hint < len;

        int ofs = 1;
        int lastOfs = 0;
        if (c.compare(key, a[base + hint]) < 0) {
            // 向左跳跃，直到 a[b+hint - ofs] <= key < a[b+hint - lastOfs]
            int maxOfs = hint + 1;
            while (ofs < maxOfs && c.compare(key, a[base + hint - ofs]) < 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0)   // int 溢出
                    ofs = maxOfs;
            }
            if (ofs > maxOfs)
                ofs = maxOfs;

            // 使偏移量相对于 b
            int tmp = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmp;
        } else { // a[b + hint] <= key
            // 向右跳跃，直到 a[b+hint + lastOfs] <= key < a[b+hint + ofs]
            int maxOfs = len - hint;
            while (ofs < maxOfs && c.compare(key, a[base + hint + ofs]) >= 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0)   // int 溢出
                    ofs = maxOfs;
            }
            if (ofs > maxOfs)
                ofs = maxOfs;

            // 使偏移量相对于 b
            lastOfs += hint;
            ofs += hint;
        }
        assert -1 <= lastOfs && lastOfs < ofs && ofs <= len;

        /*
         * 现在 a[b + lastOfs] <= key < a[b + ofs]，所以 key 应该位于 lastOfs 的右侧，
         * 但不超过 ofs。执行二分查找，保持不变性 a[b + lastOfs - 1] <= key < a[b + ofs]。
         */
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);

            if (c.compare(key, a[base + m]) < 0)
                ofs = m;          // key < a[b + m]
            else
                lastOfs = m + 1;  // a[b + m] <= key
        }
        assert lastOfs == ofs;    // 所以 a[b + ofs - 1] <= key < a[b + ofs]
        return ofs;
    }

    /**
     * 就地稳定地合并两个相邻的运行。第一个运行的第一个元素必须大于第二个运行的第一个元素 (a[base1] > a[base2])，
     * 且第一个运行的最后一个元素 (a[base1 + len1-1]) 必须大于第二个运行的所有元素。
     *
     * 为了性能，当 len1 <= len2 时应调用此方法；如果 len1 >= len2，则应调用其孪生方法 mergeHi。
     * （如果 len1 == len2，可以调用任一方法。）
     *
     * @param base1 要合并的第一个运行的第一个元素的索引
     * @param len1  要合并的第一个运行的长度（必须 > 0）
     * @param base2 要合并的第二个运行的第一个元素的索引
     *        （必须是 base1 + len1）
     * @param len2  要合并的第二个运行的长度（必须 > 0）
     */
    private void mergeLo(int base1, int len1, int base2, int len2) {
        assert len1 > 0 && len2 > 0 && base1 + len1 == base2;

        // 将第一个运行复制到临时数组
        T[] a = this.a; // 为了性能
        T[] tmp = ensureCapacity(len1);
        int cursor1 = tmpBase; // 临时数组中的索引
        int cursor2 = base2;   // a 中的索引
        int dest = base1;      // a 中的索引
        System.arraycopy(a, base1, tmp, cursor1, len1);

        // 移动第二个运行的第一个元素并处理退化情况
        a[dest++] = a[cursor2++];
        if (--len2 == 0) {
            System.arraycopy(tmp, cursor1, a, dest, len1);
            return;
        }
        if (len1 == 1) {
            System.arraycopy(a, cursor2, a, dest, len2);
            a[dest + len2] = tmp[cursor1]; // 第一个运行的最后一个元素到合并的末尾
            return;
        }

        Comparator<? super T> c = this.c;  // 为了性能使用局部变量
        int minGallop = this.minGallop;    //  "    "       "     "      "
    outer:
        while (true) {
            int count1 = 0; // 第一个运行连续获胜的次数
            int count2 = 0; // 第二个运行连续获胜的次数


                        /*
             * 直到（如果有的话）其中一个序列开始持续获胜，执行直接操作。
             */
            do {
                assert len1 > 1 && len2 > 0;
                if (c.compare(a[cursor2], tmp[cursor1]) < 0) {
                    a[dest++] = a[cursor2++];
                    count2++;
                    count1 = 0;
                    if (--len2 == 0)
                        break outer;
                } else {
                    a[dest++] = tmp[cursor1++];
                    count1++;
                    count2 = 0;
                    if (--len1 == 1)
                        break outer;
                }
            } while ((count1 | count2) < minGallop);

            /*
             * 一个序列如此持续地获胜，以至于跳跃可能是一个巨大的胜利。因此尝试这样做，并继续跳跃，直到（如果有的话）没有一个序列看起来持续获胜。
             */
            do {
                assert len1 > 1 && len2 > 0;
                count1 = gallopRight(a[cursor2], tmp, cursor1, len1, 0, c);
                if (count1 != 0) {
                    System.arraycopy(tmp, cursor1, a, dest, count1);
                    dest += count1;
                    cursor1 += count1;
                    len1 -= count1;
                    if (len1 <= 1) // len1 == 1 || len1 == 0
                        break outer;
                }
                a[dest++] = a[cursor2++];
                if (--len2 == 0)
                    break outer;

                count2 = gallopLeft(tmp[cursor1], a, cursor2, len2, 0, c);
                if (count2 != 0) {
                    System.arraycopy(a, cursor2, a, dest, count2);
                    dest += count2;
                    cursor2 += count2;
                    len2 -= count2;
                    if (len2 == 0)
                        break outer;
                }
                a[dest++] = tmp[cursor1++];
                if (--len1 == 1)
                    break outer;
                minGallop--;
            } while (count1 >= MIN_GALLOP | count2 >= MIN_GALLOP);
            if (minGallop < 0)
                minGallop = 0;
            minGallop += 2;  // 退出跳跃模式的惩罚
        }  // "outer" 循环结束
        this.minGallop = minGallop < 1 ? 1 : minGallop;  // 写回字段

        if (len1 == 1) {
            assert len2 > 0;
            System.arraycopy(a, cursor2, a, dest, len2);
            a[dest + len2] = tmp[cursor1]; // 第一个序列的最后一个元素到合并的末尾
        } else if (len1 == 0) {
            throw new IllegalArgumentException(
                "比较方法违反其通用合同！");
        } else {
            assert len2 == 0;
            assert len1 > 1;
            System.arraycopy(tmp, cursor1, a, dest, len1);
        }
    }

    /**
     * 类似于 mergeLo，但此方法仅应在 len1 >= len2 时调用；如果 len1 <= len2 应调用 mergeLo。（如果 len1 == len2，可以调用任一方法。）
     *
     * @param base1 第一个要合并的序列的第一个元素的索引
     * @param len1  第一个要合并的序列的长度（必须 > 0）
     * @param base2 第二个要合并的序列的第一个元素的索引
     *        （必须是 aBase + aLen）
     * @param len2  第二个要合并的序列的长度（必须 > 0）
     */
    private void mergeHi(int base1, int len1, int base2, int len2) {
        assert len1 > 0 && len2 > 0 && base1 + len1 == base2;

        // 将第二个序列复制到临时数组中
        T[] a = this.a; // 为了性能
        T[] tmp = ensureCapacity(len2);
        int tmpBase = this.tmpBase;
        System.arraycopy(a, base2, tmp, tmpBase, len2);

        int cursor1 = base1 + len1 - 1;  // 指向 a 的索引
        int cursor2 = tmpBase + len2 - 1; // 指向临时数组的索引
        int dest = base2 + len2 - 1;     // 指向 a 的索引

        // 移动第一个序列的最后一个元素并处理退化情况
        a[dest--] = a[cursor1--];
        if (--len1 == 0) {
            System.arraycopy(tmp, tmpBase, a, dest - (len2 - 1), len2);
            return;
        }
        if (len2 == 1) {
            dest -= len1;
            cursor1 -= len1;
            System.arraycopy(a, cursor1 + 1, a, dest + 1, len1);
            a[dest] = tmp[cursor2];
            return;
        }

        Comparator<? super T> c = this.c;  // 为了性能使用局部变量
        int minGallop = this.minGallop;    //  "    "       "     "      "
    outer:
        while (true) {
            int count1 = 0; // 第一个序列连续获胜的次数
            int count2 = 0; // 第二个序列连续获胜的次数

            /*
             * 直到（如果有的话）其中一个序列开始持续获胜，执行直接操作。
             */
            do {
                assert len1 > 0 && len2 > 1;
                if (c.compare(tmp[cursor2], a[cursor1]) < 0) {
                    a[dest--] = a[cursor1--];
                    count1++;
                    count2 = 0;
                    if (--len1 == 0)
                        break outer;
                } else {
                    a[dest--] = tmp[cursor2--];
                    count2++;
                    count1 = 0;
                    if (--len2 == 1)
                        break outer;
                }
            } while ((count1 | count2) < minGallop);

            /*
             * 一个序列如此持续地获胜，以至于跳跃可能是一个巨大的胜利。因此尝试这样做，并继续跳跃，直到（如果有的话）没有一个序列看起来持续获胜。
             */
            do {
                assert len1 > 0 && len2 > 1;
                count1 = len1 - gallopRight(tmp[cursor2], a, base1, len1, len1 - 1, c);
                if (count1 != 0) {
                    dest -= count1;
                    cursor1 -= count1;
                    len1 -= count1;
                    System.arraycopy(a, cursor1 + 1, a, dest + 1, count1);
                    if (len1 == 0)
                        break outer;
                }
                a[dest--] = tmp[cursor2--];
                if (--len2 == 1)
                    break outer;


                            count2 = len2 - gallopLeft(a[cursor1], tmp, tmpBase, len2, len2 - 1, c);
                if (count2 != 0) {
                    dest -= count2;
                    cursor2 -= count2;
                    len2 -= count2;
                    System.arraycopy(tmp, cursor2 + 1, a, dest + 1, count2);
                    if (len2 <= 1)  // len2 == 1 || len2 == 0
                        break outer;
                }
                a[dest--] = a[cursor1--];
                if (--len1 == 0)
                    break outer;
                minGallop--;
            } while (count1 >= MIN_GALLOP | count2 >= MIN_GALLOP);
            if (minGallop < 0)
                minGallop = 0;
            minGallop += 2;  // Penalize for leaving gallop mode
        }  // End of "outer" loop
        this.minGallop = minGallop < 1 ? 1 : minGallop;  // Write back to field

        if (len2 == 1) {
            assert len1 > 0;
            dest -= len1;
            cursor1 -= len1;
            System.arraycopy(a, cursor1 + 1, a, dest + 1, len1);
            a[dest] = tmp[cursor2];  // Move first elt of run2 to front of merge
        } else if (len2 == 0) {
            throw new IllegalArgumentException(
                "Comparison method violates its general contract!");
        } else {
            assert len1 == 0;
            assert len2 > 0;
            System.arraycopy(tmp, tmpBase, a, dest - (len2 - 1), len2);
        }
    }

    /**
     * 确保外部数组 tmp 至少有指定的元素数量，必要时增加其大小。大小以指数方式增加，以确保摊销线性时间复杂度。
     *
     * @param minCapacity tmp 数组所需的最小容量
     * @return tmp，无论是否增长
     */
    private T[] ensureCapacity(int minCapacity) {
        if (tmpLen < minCapacity) {
            // 计算大于 minCapacity 的最小 2 的幂
            int newSize = minCapacity;
            newSize |= newSize >> 1;
            newSize |= newSize >> 2;
            newSize |= newSize >> 4;
            newSize |= newSize >> 8;
            newSize |= newSize >> 16;
            newSize++;

            if (newSize < 0) // 几乎不可能发生！
                newSize = minCapacity;
            else
                newSize = Math.min(newSize, a.length >>> 1);

            @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
            T[] newArray = (T[])java.lang.reflect.Array.newInstance
                (a.getClass().getComponentType(), newSize);
            tmp = newArray;
            tmpLen = newSize;
            tmpBase = 0;
        }
        return tmp;
    }
}
