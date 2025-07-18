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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbutils.BaseTestCase;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.TestBean;
import org.junit.jupiter.api.Test;

/**
 * BeanListHandlerTest
 */
class BeanListHandlerTest extends BaseTestCase {

    public static class SubTestBean extends TestBean implements SubTestBeanInterface {
    }

    public interface SubTestBeanInterface {
        String getDoNotSet();

        String getOne();

        TestBean.Ordinal getThree();

        String getTwo();
    }

    @Test
    void testEmptyResultSetHandle() throws SQLException {
        final ResultSetHandler<List<TestBean>> h = new BeanListHandler<>(TestBean.class);
        final List<TestBean> results = h.handle(getEmptyResultSet());

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testHandle() throws SQLException {
        final ResultSetHandler<List<TestBean>> h = new BeanListHandler<>(TestBean.class);
        final List<TestBean> results = h.handle(getResultSet());

        assertNotNull(results);
        assertEquals(ROW_COUNT, results.size());

        final Iterator<TestBean> iter = results.iterator();
        TestBean row;
        assertTrue(iter.hasNext());
        row = iter.next();
        assertEquals("1", row.getOne());
        assertEquals("2", row.getTwo());
        assertEquals(TestBean.Ordinal.THREE, row.getThree());
        assertEquals("not set", row.getDoNotSet());

        assertTrue(iter.hasNext());
        row = iter.next();

        assertEquals("4", row.getOne());
        assertEquals("5", row.getTwo());
        assertEquals(TestBean.Ordinal.SIX, row.getThree());
        assertEquals("not set", row.getDoNotSet());

        assertFalse(iter.hasNext());
    }

    @Test
    void testHandleToInterface() throws SQLException {
        final ResultSetHandler<List<SubTestBeanInterface>> h = new BeanListHandler<>(SubTestBean.class);
        final List<SubTestBeanInterface> results = h.handle(getResultSet());

        assertNotNull(results);
        assertEquals(ROW_COUNT, results.size());

        final Iterator<SubTestBeanInterface> iter = results.iterator();
        SubTestBeanInterface row;
        assertTrue(iter.hasNext());
        row = iter.next();
        assertSame(SubTestBean.class, row.getClass());

        assertEquals("1", row.getOne());
        assertEquals("2", row.getTwo());
        assertEquals(TestBean.Ordinal.THREE, row.getThree());
        assertEquals("not set", row.getDoNotSet());

        assertTrue(iter.hasNext());
        row = iter.next();
        assertSame(SubTestBean.class, row.getClass());

        assertEquals("4", row.getOne());
        assertEquals("5", row.getTwo());
        assertEquals(TestBean.Ordinal.SIX, row.getThree());
        assertEquals("not set", row.getDoNotSet());

        assertFalse(iter.hasNext());
    }

    @Test
    void testHandleToSuperClass() throws SQLException {
        final ResultSetHandler<List<TestBean>> h = new BeanListHandler<>(SubTestBean.class);
        final List<TestBean> results = h.handle(getResultSet());

        assertNotNull(results);
        assertEquals(ROW_COUNT, results.size());

        final Iterator<TestBean> iter = results.iterator();
        assertTrue(iter.hasNext());
        TestBean row = iter.next();
        assertSame(SubTestBean.class, row.getClass());

        assertEquals("1", row.getOne());
        assertEquals("2", row.getTwo());
        assertEquals(TestBean.Ordinal.THREE, row.getThree());
        assertEquals("not set", row.getDoNotSet());

        assertTrue(iter.hasNext());
        row = iter.next();
        assertSame(SubTestBean.class, row.getClass());

        assertEquals("4", row.getOne());
        assertEquals("5", row.getTwo());
        assertEquals(TestBean.Ordinal.SIX, row.getThree());
        assertEquals("not set", row.getDoNotSet());

        assertFalse(iter.hasNext());
    }
}
