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
package org.apache.commons.dbutils.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.BaseTestCase;
import org.apache.commons.dbutils.MockResultSet;
import org.apache.commons.dbutils.ProxyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * StringTrimmedResultSetTest
 */
class StringTrimmedResultSetTest extends BaseTestCase {

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setResultSet(StringTrimmedResultSet.wrap(getResultSet()));
    }

    @Test
    void testGetObject() throws SQLException {
        getResultSet().next();
        assertEquals("notInBean", getResultSet().getObject(4));
    }

    @Test
    void testGetString() throws SQLException {
        getResultSet().next();
        assertEquals("notInBean", getResultSet().getString(4));
    }

    /**
     * Make sure 2 wrappers work together.
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    void testMultipleWrappers() throws Exception {
        // Create a ResultSet with data
        final Object[][] rows = { { null } };
        ResultSet rs = MockResultSet.create(META_DATA, rows, false);

        // Wrap the ResultSet with a null checked version
        final SqlNullCheckedResultSet ncrs = new SqlNullCheckedResultSet(rs);
        ncrs.setNullString("   trim this   ");
        rs = ProxyFactory.instance().createResultSet(ncrs);

        // Wrap the wrapper with a string trimmed version
        rs = StringTrimmedResultSet.wrap(rs);

        rs.next();
        assertEquals("trim this", rs.getString(1));
    }

}
