/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.dbutils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.jxpath.util.TypeUtils;

/**
 * MockResultSet dynamically implements the ResultSet interface.
 */
public class MockResultSet implements InvocationHandler {

    private static final Set<String> METHOD_NAMES = Set.of("isLast", "hashCode", "toString", "equals");

    /**
     * Create a {@code MockResultSet} proxy object. This is equivalent to:
     *
     * <pre>
     * ProxyFactory.instance().createResultSet(new MockResultSet(metaData, rows));
     * </pre>
     *
     * @param metaData Result set metadata.
     * @param rows     A null value indicates an empty {@code ResultSet}.
     * @param unsupportedToDefault Whether to throw an exception or return null when an operation is unsupported.
     */
    public static ResultSet create(final ResultSetMetaData metaData, final Object[][] rows, final boolean unsupportedToDefault) {
        return ProxyFactory.instance().createResultSet(new MockResultSet(metaData, rows, unsupportedToDefault));
    }

    private Object[] currentRow;

    private Iterator<Object[]> iterator;

    private final ResultSetMetaData metaData;

    private Boolean wasNull = Boolean.FALSE;

    private final boolean unsupportedToDefault;

    /**
     * MockResultSet constructor.
     *
     * @param metaData Result set metadata.
     * @param rows     A null value indicates an empty {@code ResultSet}.
     * @param unsupportedToDefault Whether to throw an exception or return null when an operation is unsupported.
     */
    public MockResultSet(final ResultSetMetaData metaData, final Object[][] rows, final boolean unsupportedToDefault) {
        this.metaData = metaData;
        this.unsupportedToDefault = unsupportedToDefault;
        if (rows == null) {
            this.iterator = Collections.<Object[]>emptyList().iterator();
        } else {
            this.iterator = Arrays.asList(rows).iterator();
        }
    }

    /**
     * The get* methods can have an int column index or a String column name as the parameter. This method handles both cases and returns the column index that
     * the client is trying to get at.
     *
     * @param args
     * @return A column index.
     * @throws SQLException if a database access error occurs
     */
    private int columnIndex(final Object[] args) throws SQLException {
        if (args[0] instanceof Integer) {
            return ((Integer) args[0]).intValue();
        }
        if (args[0] instanceof String) {
            return columnNameToIndex((String) args[0]);
        }
        throw new SQLException(args[0] + " must be Integer or String");
    }

    /**
     * Returns the column index for the given column name.
     *
     * @return A 1 based index
     * @throws SQLException if the column name is invalid
     */
    private int columnNameToIndex(final String columnName) throws SQLException {
        if (currentRow == null) {
            throw new SQLException("No current row.");
        }
        for (int i = 0; i < this.currentRow.length; i++) {
            final int c = i + 1;
            if (this.metaData.getColumnName(c).equalsIgnoreCase(columnName)) {
                return c;
            }
        }
        throw new SQLException(columnName + " is not a valid column name.");
    }

    /**
     * Gets the boolean value at the given column index.
     *
     * @param columnIndex A 1 based index.
     * @throws SQLException if a database access error occurs
     */
    protected Object getBoolean(final int columnIndex) throws SQLException {
        final Object obj = getValueAt(columnIndex);
        setWasNull(obj);
        try {
            return obj == null ? Boolean.FALSE : Boolean.valueOf(obj.toString());
        } catch (final NumberFormatException e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * Gets the byte value at the given column index.
     *
     * @param columnIndex A 1 based index.
     * @throws SQLException if a database access error occurs
     */
    protected Object getByte(final int columnIndex) throws SQLException {
        final Object obj = getValueAt(columnIndex);
        setWasNull(obj);
        try {
            return obj == null ? Byte.valueOf((byte) 0) : Byte.valueOf(obj.toString());
        } catch (final NumberFormatException e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * Gets the double value at the given column index.
     *
     * @param columnIndex A 1 based index.
     * @throws SQLException if a database access error occurs
     */
    protected Object getDouble(final int columnIndex) throws SQLException {
        final Object obj = getValueAt(columnIndex);
        setWasNull(obj);
        try {
            return obj == null ? Double.valueOf(0) : Double.valueOf(obj.toString());
        } catch (final NumberFormatException e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * Gets the float value at the given column index.
     *
     * @param columnIndex A 1 based index.
     * @throws SQLException if a database access error occurs
     */
    protected Object getFloat(final int columnIndex) throws SQLException {
        final Object obj = getValueAt(columnIndex);
        setWasNull(obj);
        try {
            return obj == null ? Float.valueOf(0) : Float.valueOf(obj.toString());
        } catch (final NumberFormatException e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * Gets the int value at the given column index.
     *
     * @param columnIndex A 1 based index.
     * @throws SQLException if a database access error occurs
     */
    protected Object getInt(final int columnIndex) throws SQLException {
        final Object obj = getValueAt(columnIndex);
        setWasNull(obj);
        try {
            return obj == null ? Integer.valueOf(0) : Integer.valueOf(obj.toString());
        } catch (final NumberFormatException e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * Gets the long value at the given column index.
     *
     * @param columnIndex A 1 based index.
     * @throws SQLException if a database access error occurs
     */
    protected Object getLong(final int columnIndex) throws SQLException {
        final Object obj = getValueAt(columnIndex);
        setWasNull(obj);
        try {
            return obj == null ? Long.valueOf(0) : Long.valueOf(obj.toString());
        } catch (final NumberFormatException e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * @throws SQLException
     */
    protected ResultSetMetaData getMetaData() throws SQLException {
        return this.metaData;
    }

    /**
     * Gets the object at the given column index.
     *
     * @param columnIndex A 1 based index.
     * @throws SQLException if a database access error occurs
     */
    protected Object getObject(final int columnIndex) throws SQLException {
        final Object obj = getValueAt(columnIndex);
        setWasNull(obj);
        return obj;
    }

    /**
     * Gets the short value at the given column index.
     *
     * @param columnIndex A 1 based index.
     * @throws SQLException if a database access error occurs
     */
    protected Object getShort(final int columnIndex) throws SQLException {
        final Object obj = getValueAt(columnIndex);
        setWasNull(obj);
        try {
            return obj == null ? Short.valueOf((short) 0) : Short.valueOf(obj.toString());
        } catch (final NumberFormatException e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * Gets the String at the given column index.
     *
     * @param columnIndex A 1 based index.
     * @throws SQLException if a database access error occurs
     */
    protected String getString(final int columnIndex) throws SQLException {
        final Object obj = getObject(columnIndex);
        setWasNull(obj);
        return Objects.toString(obj, null);
    }

    private Object getValueAt(final int columnIndex) {
        return currentRow != null ? currentRow[columnIndex - 1] : null;
    }

    private Object handleColumnMethod(final Method method, final Object[] args) throws SQLException {
        final String methodName = method.getName();
        switch (methodName) {
        case "getBoolean":
            return getBoolean(columnIndex(args));
        case "getByte":
            return getByte(columnIndex(args));
        case "getDouble":
            return getDouble(columnIndex(args));
        case "getFloat":
            return getFloat(columnIndex(args));
        case "getInt":
            return getInt(columnIndex(args));
        case "getLong":
            return getLong(columnIndex(args));
        case "getObject":
            return getObject(columnIndex(args));
        case "getShort":
            return getShort(columnIndex(args));
        case "getString":
            return getString(columnIndex(args));
        case "wasNull":
            return wasNull();
        default:
            if (unsupportedToDefault) {
                return unsupportedToDefault(method);
            }
            throw new UnsupportedOperationException("Unsupported column method: " + methodName);
        }
    }

    private Object handleNonColumnMethod(final Method method, final Object proxy, final Object[] args) throws SQLException {
        final String methodName = method.getName();
        switch (methodName) {
            case "isLast":
                return isLast();
            case "hashCode":
                return Integer.valueOf(System.identityHashCode(proxy));
            case "toString":
                return "MockResultSet " + System.identityHashCode(proxy);
            case "equals":
                return Boolean.valueOf(proxy == args[0]);
            default:
                if (unsupportedToDefault) {
                    return unsupportedToDefault(method);
                }
                throw new UnsupportedOperationException("Unsupported non-column method: " + methodName);
        }
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String methodName = method.getName();
        switch (methodName) {
        case "getMetaData":
            return getMetaData();
        case "next":
            return next();
        case "previous":
            break;
        case "close":
            break;
        default:
            if (isColumnMethod(methodName)) {
                return handleColumnMethod(method, args);
            }
            if (METHOD_NAMES.contains(methodName)) {
                return handleNonColumnMethod(method, proxy, args);
            }
            break;
        }
        if (unsupportedToDefault) {
            return unsupportedToDefault(method);
        }
        throw new UnsupportedOperationException("Unsupported method: " + methodName);
    }

    private Object unsupportedToDefault(final Method method) {
        final Class<?> returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            return TypeUtils.convert(null, returnType);
        }
        return null;
    }

    private boolean isColumnMethod(final String methodName) {
        return methodName.startsWith("get") || methodName.equals("wasNull");
    }

    /**
     * @throws SQLException
     */
    protected Boolean isLast() throws SQLException {
        return hasNext() ? Boolean.FALSE : Boolean.TRUE;
    }

    private boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * @throws SQLException
     */
    protected Boolean next() throws SQLException {
        if (!hasNext()) {
            return Boolean.FALSE;
        }
        this.currentRow = iterator.next();
        return Boolean.TRUE;
    }

    /**
     * Assigns this.wasNull a Boolean value based on the object passed in.
     *
     * @param isNull
     */
    private void setWasNull(final Object isNull) {
        this.wasNull = isNull == null ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * @throws SQLException
     */
    protected Boolean wasNull() throws SQLException {
        return this.wasNull;
    }
}
