/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.invoke;

import java.util.ArrayList;
import java.util.Arrays;
import static java.lang.invoke.LambdaForm.*;
import static java.lang.invoke.LambdaForm.BasicType.*;

/** 用于正在转换的 LF 的工作存储。
 *  类似于 StringBuffer，编辑可以分多个步骤进行。
 */
final class LambdaFormBuffer {
    private int arity, length;
    private Name[] names;
    private Name[] originalNames;  // 事务前名称的快照
    private byte flags;
    private int firstChange;
    private Name resultName;
    private String debugName;
    private ArrayList<Name> dups;

    private static final int F_TRANS = 0x10, F_OWNED = 0x03;

    LambdaFormBuffer(LambdaForm lf) {
        this.arity = lf.arity;
        setNames(lf.names);
        int result = lf.result;
        if (result == LAST_RESULT)  result = length - 1;
        if (result >= 0 && lf.names[result].type != V_TYPE)
            resultName = lf.names[result];
        debugName = lf.debugName;
        assert(lf.nameRefsAreLegal());
    }

    private LambdaForm lambdaForm() {
        assert(!inTrans());  // 需要调用 endEdit 来整理事务
        return new LambdaForm(debugName, arity, nameArray(), resultIndex());
    }

    Name name(int i) {
        assert(i < length);
        return names[i];
    }

    Name[] nameArray() {
        return Arrays.copyOf(names, length);
    }

    int resultIndex() {
        if (resultName == null)  return VOID_RESULT;
        int index = indexOf(resultName, names);
        assert(index >= 0);
        return index;
    }

    void setNames(Name[] names2) {
        names = originalNames = names2;  // 记录所有初始位置
        length = names2.length;
        flags = 0;
    }

    private boolean verifyArity() {
        for (int i = 0; i < arity && i < firstChange; i++) {
            assert(names[i].isParam()) : "#" + i + "=" + names[i];
        }
        for (int i = arity; i < length; i++) {
            assert(!names[i].isParam()) : "#" + i + "=" + names[i];
        }
        for (int i = length; i < names.length; i++) {
            assert(names[i] == null) : "#" + i + "=" + names[i];
        }
        // 检查 resultName
        if (resultName != null) {
            int resultIndex = indexOf(resultName, names);
            assert(resultIndex >= 0) : "未找到: " + resultName.exprString() + Arrays.asList(names);
            assert(names[resultIndex] == resultName);
        }
        return true;
    }

    private boolean verifyFirstChange() {
        assert(inTrans());
        for (int i = 0; i < length; i++) {
            if (names[i] != originalNames[i]) {
                assert(firstChange == i) : Arrays.asList(firstChange, i, originalNames[i].exprString(), Arrays.asList(names));
                return true;
            }
        }
        assert(firstChange == length) : Arrays.asList(firstChange, Arrays.asList(names));
        return true;
    }

    private static int indexOf(NamedFunction fn, NamedFunction[] fns) {
        for (int i = 0; i < fns.length; i++) {
            if (fns[i] == fn)  return i;
        }
        return -1;
    }

    private static int indexOf(Name n, Name[] ns) {
        for (int i = 0; i < ns.length; i++) {
            if (ns[i] == n)  return i;
        }
        return -1;
    }

    boolean inTrans() {
        return (flags & F_TRANS) != 0;
    }

    int ownedCount() {
        return flags & F_OWNED;
    }

    void growNames(int insertPos, int growLength) {
        int oldLength = length;
        int newLength = oldLength + growLength;
        int oc = ownedCount();
        if (oc == 0 || newLength > names.length) {
            names = Arrays.copyOf(names, (names.length + growLength) * 5 / 4);
            if (oc == 0) {
                flags++;
                oc++;
                assert(ownedCount() == oc);
            }
        }
        if (originalNames != null && originalNames.length < names.length) {
            originalNames = Arrays.copyOf(originalNames, names.length);
            if (oc == 1) {
                flags++;
                oc++;
                assert(ownedCount() == oc);
            }
        }
        if (growLength == 0)  return;
        int insertEnd = insertPos + growLength;
        int tailLength = oldLength - insertPos;
        System.arraycopy(names, insertPos, names, insertEnd, tailLength);
        Arrays.fill(names, insertPos, insertEnd, null);
        if (originalNames != null) {
            System.arraycopy(originalNames, insertPos, originalNames, insertEnd, tailLength);
            Arrays.fill(originalNames, insertPos, insertEnd, null);
        }
        length = newLength;
        if (firstChange >= insertPos) {
            firstChange += growLength;
        }
    }

    int lastIndexOf(Name n) {
        int result = -1;
        for (int i = 0; i < length; i++) {
            if (names[i] == n)  result = i;
        }
        return result;
    }

    /** 我们刚刚用 pos2 位置的名称覆盖了 pos1 位置的名称。
     *  这意味着有两个名称副本，我们稍后需要修复。
     */
    private void noteDuplicate(int pos1, int pos2) {
        Name n = names[pos1];
        assert(n == names[pos2]);
        assert(originalNames[pos1] != null);  // pos1 位置被替换
        assert(originalNames[pos2] == null || originalNames[pos2] == n);
        if (dups == null) {
            dups = new ArrayList<>();
        }
        dups.add(n);
    }

    /** 将重复的名称替换为 null，并移除所有 null。 */
    private void clearDuplicatesAndNulls() {
        if (dups != null) {
            // 移除重复项。
            assert(ownedCount() >= 1);
            for (Name dup : dups) {
                for (int i = firstChange; i < length; i++) {
                    if (names[i] == dup && originalNames[i] != dup) {
                        names[i] = null;
                        assert(Arrays.asList(names).contains(dup));
                        break;  // 只删除一个重复项
                    }
                }
            }
            dups.clear();
        }
        // 现在我们已经完成了 originalNames，移除“已删除”的名称。
        int oldLength = length;
        for (int i = firstChange; i < length; i++) {
            if (names[i] == null) {
                System.arraycopy(names, i + 1, names, i, (--length - i));
                --i;  // 从这个位置重新开始循环
            }
        }
        if (length < oldLength) {
            Arrays.fill(names, length, oldLength, null);
        }
        assert(!Arrays.asList(names).subList(0, length).contains(null));
    }

    /** 创建名称的私有可写副本。
     *  保留原始副本以供参考。
     */
    void startEdit() {
        assert(verifyArity());
        int oc = ownedCount();
        assert(!inTrans());  // 不允许嵌套事务
        flags |= F_TRANS;
        Name[] oldNames = names;
        Name[] ownBuffer = (oc == 2 ? originalNames : null);
        assert(ownBuffer != oldNames);
        if (ownBuffer != null && ownBuffer.length >= length) {
            names = copyNamesInto(ownBuffer);
        } else {
            // 创建一个新缓冲区来保存名称
            final int SLOP = 2;
            names = Arrays.copyOf(oldNames, Math.max(length + SLOP, oldNames.length));
            if (oc < 2)  ++flags;
            assert(ownedCount() == oc + 1);
        }
        originalNames = oldNames;
        assert(originalNames != names);
        firstChange = length;
        assert(inTrans());
    }

    private void changeName(int i, Name name) {
        assert(inTrans());
        assert(i < length);
        Name oldName = names[i];
        assert(oldName == originalNames[i]);  // 不能多次更改
        assert(verifyFirstChange());
        if (ownedCount() == 0)
            growNames(0, 0);
        names[i] = name;
        if (firstChange > i) {
            firstChange = i;
        }
        if (resultName != null && resultName == oldName) {
            resultName = name;
        }
    }

    /** 更改结果名称。null 表示 void 结果。 */
    void setResult(Name name) {
        assert(name == null || lastIndexOf(name) >= 0);
        resultName = name;
    }

    /** 完成事务。 */
    LambdaForm endEdit() {
        assert(verifyFirstChange());
        // 假设名称已从 originalNames[i] 更改为 names[i]，
        // 更新参数以确保引用完整性。
        for (int i = Math.max(firstChange, arity); i < length; i++) {
            Name name = names[i];
            if (name == null)  continue;  // 已删除重复项的空间
            Name newName = name.replaceNames(originalNames, names, firstChange, i);
            if (newName != name) {
                names[i] = newName;
                if (resultName == name) {
                    resultName = newName;
                }
            }
        }
        assert(inTrans());
        flags &= ~F_TRANS;
        clearDuplicatesAndNulls();
        originalNames = null;
        // 如果任何参数已更改，则按需重新排序。
        // 这是一个“羊和山羊”稳定排序，将所有非参数推到所有参数的右侧。
        if (firstChange < arity) {
            Name[] exprs = new Name[arity - firstChange];
            int argp = firstChange, exprp = 0;
            for (int i = firstChange; i < arity; i++) {
                Name name = names[i];
                if (name.isParam()) {
                    names[argp++] = name;
                } else {
                    exprs[exprp++] = name;
                }
            }
            assert(exprp == (arity - argp));
            // 将 exprs 复制到最后一个剩余参数之后
            System.arraycopy(exprs, 0, names, argp, exprp);
            // 调整 arity
            arity -= exprp;
        }
        assert(verifyArity());
        return lambdaForm();
    }

    private Name[] copyNamesInto(Name[] buffer) {
        System.arraycopy(names, 0, buffer, 0, length);
        Arrays.fill(buffer, length, buffer.length, null);
        return buffer;
    }

    /** 将任何函数在 oldFns 中的 Name 替换为新函数在 newFns 中对应位置的副本。
     *  仅当参数完全相等时才执行此操作。
     */
    LambdaFormBuffer replaceFunctions(NamedFunction[] oldFns, NamedFunction[] newFns,
                                      Object... forArguments) {
        assert(inTrans());
        if (oldFns.length == 0)  return this;
        for (int i = arity; i < length; i++) {
            Name n = names[i];
            int nfi = indexOf(n.function, oldFns);
            if (nfi >= 0 && Arrays.equals(n.arguments, forArguments)) {
                changeName(i, new Name(newFns[nfi], n.arguments));
            }
        }
        return this;
    }

    private void replaceName(int pos, Name binding) {
        assert(inTrans());
        assert(verifyArity());
        assert(pos < arity);
        Name param = names[pos];
        assert(param.isParam());
        assert(param.type == binding.type);
        changeName(pos, binding);
    }

    /** 用新参数替换参数。 */
    LambdaFormBuffer renameParameter(int pos, Name newParam) {
        assert(newParam.isParam());
        replaceName(pos, newParam);
        return this;
    }

    /** 用新表达式替换参数。 */
    LambdaFormBuffer replaceParameterByNewExpression(int pos, Name binding) {
        assert(!binding.isParam());
        assert(lastIndexOf(binding) < 0);  // 否则使用 replaceParameterByCopy
        replaceName(pos, binding);
        return this;
    }

    /** 用形式中的另一个参数或表达式替换参数。 */
    LambdaFormBuffer replaceParameterByCopy(int pos, int valuePos) {
        assert(pos != valuePos);
        replaceName(pos, names[valuePos]);
        noteDuplicate(pos, valuePos);  // 暂时在 names 数组中出现两次
        return this;
    }

    private void insertName(int pos, Name expr, boolean isParameter) {
        assert(inTrans());
        assert(verifyArity());
        assert(isParameter ? pos <= arity : pos >= arity);
        growNames(pos, 1);
        if (isParameter)  arity += 1;
        changeName(pos, expr);
    }

    /** 插入新表达式。 */
    LambdaFormBuffer insertExpression(int pos, Name expr) {
        assert(!expr.isParam());
        insertName(pos, expr, false);
        return this;
    }

    /** 插入新参数。 */
    LambdaFormBuffer insertParameter(int pos, Name param) {
        assert(param.isParam());
        insertName(pos, param, true);
        return this;
    }
}
