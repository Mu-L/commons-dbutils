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

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;

/**
 * {@code ResultSetHandler} implementation that converts the first
 * {@code ResultSet} row into a JavaBean. This class is thread safe.
 *
 * @param <T> the target bean type
 * @see org.apache.commons.dbutils.ResultSetHandler
 */
public class BeanHandler<T> implements ResultSetHandler<T> {

    /**
     * The Class of beans produced by this handler.
     */
    private final Class<? extends T> type;

    /**
     * The RowProcessor implementation to use when converting rows
     * into beans.
     */
    private final RowProcessor convert;

    /**
     * Creates a new instance of BeanHandler.
     *
     * @param type The Class that objects returned from {@code handle()}
     * are created from.
     */
    public BeanHandler(final Class<? extends T> type) {
        this(type, ArrayHandler.ROW_PROCESSOR);
    }

    /**
     * Creates a new instance of BeanHandler.
     *
     * @param type The Class that objects returned from {@code handle()}
     * are created from.
     * @param convert The {@code RowProcessor} implementation
     * to use when converting rows into beans.
     */
    public BeanHandler(final Class<? extends T> type, final RowProcessor convert) {
        this.type = type;
        this.convert = convert;
    }

    /**
     * Convert the first row of the {@code ResultSet} into a bean with the
     * {@code Class} given in the constructor.
     * @param resultSet {@code ResultSet} to process.
     * @return An initialized JavaBean or {@code null} if there were no
     * rows in the {@code ResultSet}.
     *
     * @throws SQLException if a database access error occurs
     * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
     */
    @Override
    public T handle(final ResultSet resultSet) throws SQLException {
        return resultSet.next() ? this.convert.toBean(resultSet, this.type) : null;
    }

}
