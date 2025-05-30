/*
 * Copyright (c) 1995, 1997, Oracle and/or its affiliates. All rights reserved.
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

package java.applet;

/**
 * <code>AudioClip</code> 接口是一个简单的播放声音剪辑的抽象。多个 <code>AudioClip</code> 项可以同时播放，
 * 播放的声音会被混合在一起以产生复合声音。
 *
 * @author      Arthur van Hoff
 * @since       JDK1.0
 */
public interface AudioClip {
    /**
     * 开始播放此音频剪辑。每次调用此方法时，剪辑都会从头开始播放。
     */
    void play();

    /**
     * 开始循环播放此音频剪辑。
     */
    void loop();

    /**
     * 停止播放此音频剪辑。
     */
    void stop();
}
