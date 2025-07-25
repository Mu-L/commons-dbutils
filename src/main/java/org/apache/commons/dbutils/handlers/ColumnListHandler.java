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
package org.apache.commons.dbutils.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@code ResultSetHandler} implementation that converts one
 * {@code ResultSet} column into a {@code List} of
 * {@code Object}s. This class is thread safe.
 *
 * @param <T> The type of the column.
 * @see org.apache.commons.dbutils.ResultSetHandler
 * @since 1.1
 */
public class ColumnListHandler<T> extends AbstractListHandler<T> {

    /**
     * The column number to retrieve.
     */
    private final int columnIndex;

    /**
     * The column name to retrieve.  Either columnName or columnIndex
     * will be used but never both.
     */
    private final String columnName;

    /**
     * Creates a new instance of ColumnListHandler.  The first column of each
     * row will be returned from {@code handle()}.
     */
    public ColumnListHandler() {
        this(1, null);
    }

    /**
     * Creates a new instance of ColumnListHandler.
     *
     * @param columnIndex The index of the column to retrieve from the
     * {@code ResultSet}.
     */
    public ColumnListHandler(final int columnIndex) {
        this(columnIndex, null);
    }

    /** Private Helper
     * @param columnIndex The index of the column to retrieve from the
     * {@code ResultSet}.
     * @param columnName The name of the column to retrieve from the
     * {@code ResultSet}.
     */
    private ColumnListHandler(final int columnIndex, final String columnName) {
        this.columnIndex = columnIndex;
        this.columnName = columnName;
    }

    /**
     * Creates a new instance of ColumnListHandler.
     *
     * @param columnName The name of the column to retrieve from the
     * {@code ResultSet}.
     */
    public ColumnListHandler(final String columnName) {
        this(1, columnName);
    }

    /**
     * Returns one {@code ResultSet} column value as {@code Object}.
     * @param resultSet {@code ResultSet} to process.
     * @return {@code Object}, never {@code null}.
     * @throws SQLException if a database access error occurs
     * @throws ClassCastException if the class datatype does not match the column type
     * @see org.apache.commons.dbutils.handlers.AbstractListHandler#handle(ResultSet)
     */
    // We assume that the user has picked the correct type to match the column
    // so getObject will return the appropriate type and the cast will succeed.
    @SuppressWarnings("unchecked")
    @Override
    protected T handleRow(final ResultSet resultSet) throws SQLException {
        if (this.columnName == null) {
            return (T) resultSet.getObject(this.columnIndex);
        }
        return (T) resultSet.getObject(this.columnName);
   }

}
