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

import org.apache.commons.dbutils.RowProcessor;

/**
 * {@code ResultSetHandler} implementation that converts the
 * {@code ResultSet} into a {@code List} of {@code Object[]}s.
 * This class is thread safe.
 *
 * @see org.apache.commons.dbutils.ResultSetHandler
 */
public class ArrayListHandler extends AbstractListHandler<Object[]> {

    /**
     * The RowProcessor implementation to use when converting rows
     * into Object[]s.
     */
    private final RowProcessor convert;

    /**
     * Creates a new instance of ArrayListHandler using a
     * {@code BasicRowProcessor} for conversions.
     */
    public ArrayListHandler() {
        this(ArrayHandler.ROW_PROCESSOR);
    }

    /**
     * Creates a new instance of ArrayListHandler.
     *
     * @param convert The {@code RowProcessor} implementation
     * to use when converting rows into Object[]s.
     */
    public ArrayListHandler(final RowProcessor convert) {
        this.convert = convert;
    }

    /**
     * Convert row's columns into an {@code Object[]}.
     * @param resultSet {@code ResultSet} to process.
     * @return {@code Object[]}, never {@code null}.
     * @throws SQLException if a database access error occurs
     * @see org.apache.commons.dbutils.handlers.AbstractListHandler#handle(ResultSet)
     */
    @Override
    protected Object[] handleRow(final ResultSet resultSet) throws SQLException {
        return this.convert.toArray(resultSet);
    }

}
