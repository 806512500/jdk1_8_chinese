/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.beans;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

/**
 * MethodDescriptor 描述了一个 Java Bean 支持的可以被其他组件外部访问的特定方法。
 */

public class MethodDescriptor extends FeatureDescriptor {

    private final MethodRef methodRef = new MethodRef();

    private String[] paramNames;

    private List<WeakReference<Class<?>>> params;

    private ParameterDescriptor parameterDescriptors[];

    /**
     * 从一个 Method 构造一个 MethodDescriptor。
     *
     * @param method    低级别的方法信息。
     */
    public MethodDescriptor(Method method) {
        this(method, null);
    }


    /**
     * 从一个 Method 构造一个 MethodDescriptor，并为方法的每个参数提供描述信息。
     *
     * @param method    低级别的方法信息。
     * @param parameterDescriptors  方法的每个参数的描述信息。
     */
    public MethodDescriptor(Method method,
                ParameterDescriptor parameterDescriptors[]) {
        setName(method.getName());
        setMethod(method);
        this.parameterDescriptors = (parameterDescriptors != null)
                ? parameterDescriptors.clone()
                : null;
    }

    /**
     * 获取此 MethodDescriptor 封装的方法。
     *
     * @return 方法的低级别描述。
     */
    public synchronized Method getMethod() {
        Method method = this.methodRef.get();
        if (method == null) {
            Class<?> cls = getClass0();
            String name = getName();
            if ((cls != null) && (name != null)) {
                Class<?>[] params = getParams();
                if (params == null) {
                    for (int i = 0; i < 3; i++) {
                        // 查找最多 2 个参数的方法。这里是在猜测。
                        // 除非加载参数类的类加载器消失，否则此块不应执行。
                        method = Introspector.findMethod(cls, name, i, null);
                        if (method != null) {
                            break;
                        }
                    }
                } else {
                    method = Introspector.findMethod(cls, name, params.length, params);
                }
                setMethod(method);
            }
        }
        return method;
    }

    private synchronized void setMethod(Method method) {
        if (method == null) {
            return;
        }
        if (getClass0() == null) {
            setClass0(method.getDeclaringClass());
        }
        setParams(getParameterTypes(getClass0(), method));
        this.methodRef.set(method);
    }

    private synchronized void setParams(Class<?>[] param) {
        if (param == null) {
            return;
        }
        paramNames = new String[param.length];
        params = new ArrayList<>(param.length);
        for (int i = 0; i < param.length; i++) {
            paramNames[i] = param[i].getName();
            params.add(new WeakReference<Class<?>>(param[i]));
        }
    }

    // pp getParamNames 用作优化，以避免调用 method.getParameterTypes。
    String[] getParamNames() {
        return paramNames;
    }

    private synchronized Class<?>[] getParams() {
        Class<?>[] clss = new Class<?>[params.size()];

        for (int i = 0; i < params.size(); i++) {
            Reference<? extends Class<?>> ref = (Reference<? extends Class<?>>)params.get(i);
            Class<?> cls = ref.get();
            if (cls == null) {
                return null;
            } else {
                clss[i] = cls;
            }
        }
        return clss;
    }

    /**
     * 获取此 MethodDescriptor 的方法的每个参数的 ParameterDescriptor。
     *
     * @return 参数的本地化无关名称。如果参数名称未知，可能返回 null 数组。
     */
    public ParameterDescriptor[] getParameterDescriptors() {
        return (this.parameterDescriptors != null)
                ? this.parameterDescriptors.clone()
                : null;
    }

    private static Method resolve(Method oldMethod, Method newMethod) {
        if (oldMethod == null) {
            return newMethod;
        }
        if (newMethod == null) {
            return oldMethod;
        }
        return !oldMethod.isSynthetic() && newMethod.isSynthetic() ? oldMethod : newMethod;
    }

    /*
     * 包私有构造函数
     * 合并两个方法描述符。当它们冲突时，优先使用第二个参数 (y) 而不是第一个参数 (x)。
     * @param x  第一个（优先级较低的）MethodDescriptor
     * @param y  第二个（优先级较高的）MethodDescriptor
     */

    MethodDescriptor(MethodDescriptor x, MethodDescriptor y) {
        super(x, y);

        this.methodRef.set(resolve(x.methodRef.get(), y.methodRef.get()));
        params = x.params;
        if (y.params != null) {
            params = y.params;
        }
        paramNames = x.paramNames;
        if (y.paramNames != null) {
            paramNames = y.paramNames;
        }

        parameterDescriptors = x.parameterDescriptors;
        if (y.parameterDescriptors != null) {
            parameterDescriptors = y.parameterDescriptors;
        }
    }

    /*
     * 包私有复制构造函数
     * 必须使新对象与旧对象的任何更改隔离。
     */
    MethodDescriptor(MethodDescriptor old) {
        super(old);

        this.methodRef.set(old.getMethod());
        params = old.params;
        paramNames = old.paramNames;

        if (old.parameterDescriptors != null) {
            int len = old.parameterDescriptors.length;
            parameterDescriptors = new ParameterDescriptor[len];
            for (int i = 0; i < len ; i++) {
                parameterDescriptors[i] = new ParameterDescriptor(old.parameterDescriptors[i]);
            }
        }
    }

    void appendTo(StringBuilder sb) {
        appendTo(sb, "method", this.methodRef.get());
        if (this.parameterDescriptors != null) {
            sb.append("; parameterDescriptors={");
            for (ParameterDescriptor pd : this.parameterDescriptors) {
                sb.append(pd).append(", ");
            }
            sb.setLength(sb.length() - 2);
            sb.append("}");
        }
    }
}
