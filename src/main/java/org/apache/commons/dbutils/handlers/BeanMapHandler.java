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
 * <p>
 * {@code ResultSetHandler} implementation that returns a Map of Beans.
 * {@code ResultSet} rows are converted into Beans which are then stored in
 * a Map under the given key.
 * </p>
 * <p>
 * If you had a Person table with a primary key column called ID, you could
 * retrieve rows from the table like this:
 *
 * <pre>
 * ResultSetHandler&lt;Map&lt;Long, Person&gt;&gt; h = new BeanMapHandler&lt;Long, Person&gt;(Person.class, &quot;id&quot;);
 * Map&lt;Long, Person&gt; found = queryRunner.query(&quot;select id, name, age from person&quot;, h);
 * Person jane = found.get(1L); // jane's id is 1
 * String janesName = jane.getName();
 * Integer janesAge = jane.getAge();
 * </pre>
 *
 * Note that the "id" passed to BeanMapHandler can be in any case. The data type
 * returned for id is dependent upon how your JDBC driver converts SQL column
 * types from the Person table into Java types. The "name" and "age" columns are
 * converted according to their property descriptors by DbUtils.
 * &lt;/p&gt;
 * <p>
 * This class is thread safe.
 * &lt;/p&gt;
 *
 * @param <K>
 *            the type of keys maintained by the returned map
 * @param <V>
 *            the type of the bean
 * @see org.apache.commons.dbutils.ResultSetHandler
 * @since 1.5
 */
public class BeanMapHandler<K, V> extends AbstractKeyedHandler<K, V> {

    /**
     * The Class of beans produced by this handler.
     */
    private final Class<V> type;

    /**
     * The RowProcessor implementation to use when converting rows into Objects.
     */
    private final RowProcessor convert;

    /**
     * The column index to retrieve key values from. Defaults to 1.
     */
    private final int columnIndex;

    /**
     * The column name to retrieve key values from. Either columnName or
     * columnIndex will be used but never both.
     */
    private final String columnName;

    /**
     * Creates a new instance of BeanMapHandler. The value of the first column
     * of each row will be a key in the Map.
     *
     * @param type
     *            The Class that objects returned from {@code createRow()}
     *            are created from.
     */
    public BeanMapHandler(final Class<V> type) {
        this(type, ArrayHandler.ROW_PROCESSOR, 1, null);
    }

    /**
     * Creates a new instance of BeanMapHandler.
     *
     * @param type
     *            The Class that objects returned from {@code createRow()}
     *            are created from.
     * @param columnIndex
     *            The values to use as keys in the Map are retrieved from the
     *            column at this index.
     */
    public BeanMapHandler(final Class<V> type, final int columnIndex) {
        this(type, ArrayHandler.ROW_PROCESSOR, columnIndex, null);
    }

    /**
     * Creates a new instance of BeanMapHandler. The value of the first column
     * of each row will be a key in the Map.
     *
     * @param type
     *            The Class that objects returned from {@code createRow()}
     *            are created from.
     * @param convert
     *            The {@code RowProcessor} implementation to use when
     *            converting rows into Beans
     */
    public BeanMapHandler(final Class<V> type, final RowProcessor convert) {
        this(type, convert, 1, null);
    }

    /**
     * Private Helper
     *
     * @param convert
     *            The {@code RowProcessor} implementation to use when
     *            converting rows into Beans
     * @param columnIndex
     *            The values to use as keys in the Map are retrieved from the
     *            column at this index.
     * @param columnName
     *            The values to use as keys in the Map are retrieved from the
     *            column with this name.
     */
    private BeanMapHandler(final Class<V> type, final RowProcessor convert,
            final int columnIndex, final String columnName) {
        this.type = type;
        this.convert = convert;
        this.columnIndex = columnIndex;
        this.columnName = columnName;
    }

    /**
     * Creates a new instance of BeanMapHandler.
     *
     * @param type
     *            The Class that objects returned from {@code createRow()}
     *            are created from.
     * @param columnName
     *            The values to use as keys in the Map are retrieved from the
     *            column with this name.
     */
    public BeanMapHandler(final Class<V> type, final String columnName) {
        this(type, ArrayHandler.ROW_PROCESSOR, 1, columnName);
    }

    /**
     * This factory method is called by {@code handle()} to retrieve the
     * key value from the current {@code ResultSet} row.
     * @param resultSet ResultSet to create a key from
     * @return K from the configured key column name/index
     * @throws SQLException if a database access error occurs
     * @throws ClassCastException if the class datatype does not match the column type
     * @see org.apache.commons.dbutils.handlers.AbstractKeyedHandler#createKey(ResultSet)
     */
    // We assume that the user has picked the correct type to match the column
    // so getObject will return the appropriate type and the cast will succeed.
    @SuppressWarnings("unchecked")
    @Override
    protected K createKey(final ResultSet resultSet) throws SQLException {
        return columnName == null ?
               (K) resultSet.getObject(columnIndex) :
               (K) resultSet.getObject(columnName);
    }

    @Override
    protected V createRow(final ResultSet resultSet) throws SQLException {
        return this.convert.toBean(resultSet, type);
    }

}
