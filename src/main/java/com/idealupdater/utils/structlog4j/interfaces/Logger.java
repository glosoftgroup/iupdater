/*
 * MIT License
 *
 * Copyright (c) 2018 Kelvin Wahome
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.idealupdater.utils.structlog4j.interfaces;

/**
 * Core standard structured logger interface
 *
 * @author kelvin.wahome
 */
public interface Logger {
    // slf4j APIs
    void error(String message, Object...params);
    void warn(String message, Object...params);
    void info(String message, Object...params);
    void debug(String message, Object...params);
    void trace(String message, Object...params);

    // logging level checks
    boolean isErrorEnabled();
    boolean isWarnEnabled();
    boolean isInfoEnabled();
    boolean isDebugEnabled();
    boolean isTraceEnabled();

    // context bind handling
    void newBind(LoggableObject loggerContext);
    void newBind(Object...params);
    void bind(LoggableObject loggerContext);
    void bind(Object...params);
    void unbind(LoggableObject loggerContext);
    void unbind(Object...params);
}
