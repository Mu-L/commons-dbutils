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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

/**
 * <p>
 * {@code ResultSetHandler} implementation that returns a Map.
 * {@code ResultSet} rows are converted into objects (Vs) which are then stored
 * in a Map under the given keys (Ks).
 * </p>
 *
 * @param <K> the type of keys maintained by the returned map
 * @param <V> the type of mapped values
 * @see org.apache.commons.dbutils.ResultSetHandler
 * @since 1.3
 */
public abstract class AbstractKeyedHandler<K, V> implements ResultSetHandler<Map<K, V>> {

    /**
     * Constructs a new instance for subclasses.
     */
    public AbstractKeyedHandler() {
        // empty
    }

    /**
     * This factory method is called by {@code handle()} to retrieve the
     * key value from the current {@code ResultSet} row.
     * @param resultSet ResultSet to create a key from
     * @return K from the configured key column name/index
     * @throws SQLException if a database access error occurs
     */
    protected abstract K createKey(ResultSet resultSet) throws SQLException;

    /**
     * This factory method is called by {@code handle()} to create the Map
     * to store records in.  This implementation returns a {@code HashMap}
     * instance.
     *
     * @return Map to store records in
     */
    protected Map<K, V> createMap() {
        return new HashMap<>();
    }

    /**
     * This factory method is called by {@code handle()} to store the
     * current {@code ResultSet} row in some object.
     * @param resultSet ResultSet to create a row from
     * @return V object created from the current row
     * @throws SQLException if a database access error occurs
     */
    protected abstract V createRow(ResultSet resultSet) throws SQLException;

    /**
     * Convert each row's columns into a Map and store then
     * in a {@code Map} under {@code ResultSet.getObject(key)} key.
     * @param resultSet {@code ResultSet} to process.
     * @return A {@code Map}, never {@code null}.
     * @throws SQLException if a database access error occurs
     * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
     */
    @Override
    public Map<K, V> handle(final ResultSet resultSet) throws SQLException {
        final Map<K, V> result = createMap();
        while (resultSet.next()) {
            result.put(createKey(resultSet), createRow(resultSet));
        }
        return result;
    }

}
