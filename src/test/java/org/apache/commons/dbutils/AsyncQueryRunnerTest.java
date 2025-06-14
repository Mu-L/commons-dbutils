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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.lang3.stream.Streams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("boxing") // test code
@ExtendWith(MockitoExtension.class)
class AsyncQueryRunnerTest {
    private AsyncQueryRunner runner;
    private ArrayHandler handler;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private DataSource dataSource;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private Connection conn;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private PreparedStatement prepStmt;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private Statement stmt;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private ParameterMetaData meta;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private ResultSet results;

    // helper method for calling batch when an exception is expected
    private void callBatchWithException(final String sql, final Object[][] params) throws Exception {
        Future<int[]> future = null;
        boolean caught = false;

        try {
            future = runner.batch(sql, params);

            future.get();

            verify(prepStmt, times(2)).addBatch();
            verify(prepStmt, times(1)).executeBatch();
            verify(prepStmt, times(1)).close(); // make sure the statement is closed
            verify(conn, times(1)).close(); // make sure the connection is closed
        } catch (final Exception e) {
            caught = true;
        }

        if (!caught) {
            fail("Exception never thrown, but expected");
        }
    }

    //
    // Batch test cases
    //
    private void callGoodBatch(final Connection conn, final Object[][] params) throws Exception {
        when(meta.getParameterCount()).thenReturn(2);
        final Future<int[]> future = runner.batch(conn, "select * from blah where ? = ?", params);

        future.get();

        verify(prepStmt, times(2)).addBatch();
        verify(prepStmt, times(1)).executeBatch();
        verify(prepStmt, times(1)).close();    // make sure we closed the statement
        verify(conn, times(0)).close();    // make sure we closed the connection
    }

    private void callGoodBatch(final Object[][] params) throws Exception {
        when(meta.getParameterCount()).thenReturn(2);
        final Future<int[]> future = runner.batch("select * from blah where ? = ?", params);

        future.get();

        verify(prepStmt, times(2)).addBatch();
        verify(prepStmt, times(1)).executeBatch();
        verify(prepStmt, times(1)).close();    // make sure we closed the statement
        verify(conn, times(1)).close();    // make sure we closed the connection
    }

    private void callGoodQuery() throws Exception {
        when(meta.getParameterCount()).thenReturn(2);
        String sql = "select * from blah where ? = ?";
        runner.query(sql, handler, "unit", "test").get();

        verify(prepStmt, times(1)).executeQuery();
        verify(results, times(1)).close();
        verify(prepStmt, times(1)).close();    // make sure we closed the statement
        verify(conn, times(1)).close();    // make sure we closed the connection

        // call the other variation of query
        sql = "select * from blah";
        runner.query(sql, handler).get();

        verify(stmt, times(1)).executeQuery(sql);
        verify(results, times(2)).close();
        verify(stmt, times(1)).close();    // make sure we closed the statement
        verify(conn, times(2)).close();    // make sure we closed the connection
    }

    //
    // Query test cases
    //
    private void callGoodQuery(final Connection conn) throws Exception {
        when(meta.getParameterCount()).thenReturn(2);
        String sql = "select * from blah where ? = ?";
        runner.query(conn, sql, handler, "unit", "test").get();

        verify(prepStmt, times(1)).executeQuery();
        verify(results, times(1)).close();
        verify(prepStmt, times(1)).close();    // make sure we closed the statement
        verify(conn, times(0)).close();    // make sure we closed the connection

        // call the other variation of query
        sql = "select * from blah";
        runner.query(conn, sql, handler).get();

        verify(stmt, times(1)).executeQuery(sql);
        verify(results, times(2)).close();
        verify(stmt, times(1)).close();    // make sure we closed the statement
        verify(conn, times(0)).close();    // make sure we closed the connection
    }

    private void callGoodUpdate() throws Exception {
        when(meta.getParameterCount()).thenReturn(2);
        String sql = "update blah set ? = ?";
        runner.update(sql, "unit", "test").get();

        verify(prepStmt, times(1)).executeUpdate();
        verify(prepStmt, times(1)).close();    // make sure we closed the statement
        verify(conn, times(1)).close();    // make sure we closed the connection

        // call the other variation
        when(meta.getParameterCount()).thenReturn(0);
        sql = "update blah set unit = test";
        runner.update(sql).get();

        verify(stmt, times(1)).executeUpdate(sql);
        verify(stmt, times(1)).close();    // make sure we closed the statement
        verify(conn, times(2)).close();    // make sure we closed the connection

        // call the other variation
        when(meta.getParameterCount()).thenReturn(1);
        runner.update("update blah set unit = ?", "test").get();

        verify(prepStmt, times(2)).executeUpdate();
        verify(prepStmt, times(2)).close();    // make sure we closed the statement
        verify(conn, times(3)).close();    // make sure we closed the connection
    }

    //
    // Update test cases
    //
    private void callGoodUpdate(final Connection conn) throws Exception {
        when(meta.getParameterCount()).thenReturn(2);
        String sql = "update blah set ? = ?";
        runner.update(conn, sql, "unit", "test").get();

        verify(prepStmt, times(1)).executeUpdate();
        verify(prepStmt, times(1)).close();    // make sure we closed the statement
        verify(conn, times(0)).close();    // make sure we closed the connection

        // call the other variation
        when(meta.getParameterCount()).thenReturn(0);
        sql = "update blah set unit = test";
        runner.update(conn, sql).get();

        verify(stmt, times(1)).executeUpdate(sql);
        verify(stmt, times(1)).close();    // make sure we closed the statement
        verify(conn, times(0)).close();    // make sure we closed the connection

        // call the other variation
        when(meta.getParameterCount()).thenReturn(1);
        sql = "update blah set unit = ?";
        runner.update(conn, sql, "test").get();

        verify(prepStmt, times(2)).executeUpdate();
        verify(prepStmt, times(2)).close();    // make sure we closed the statement
        verify(conn, times(0)).close();    // make sure we closed the connection
    }

    // helper method for calling batch when an exception is expected
    private void callQueryWithException(final Object... params) throws Exception {
        boolean caught = false;

        try {
            when(meta.getParameterCount()).thenReturn(2);
            runner.query("select * from blah where ? = ?", handler, params).get();

            verify(prepStmt, times(1)).executeQuery();
            verify(results, times(1)).close();
            verify(prepStmt, times(1)).close(); // make sure we closed the statement
            verify(conn, times(1)).close(); // make sure we closed the connection
        } catch (final Exception e) {
            caught = true;
        }

        if (!caught) {
            fail("Exception never thrown, but expected");
        }
    }

    // helper method for calling batch when an exception is expected
    private void callUpdateWithException(final Object... params) throws Exception {
        boolean caught = false;

        try {
            when(meta.getParameterCount()).thenReturn(2);
            runner.update("select * from blah where ? = ?", params).get();

            verify(prepStmt, times(1)).executeUpdate();
            verify(prepStmt, times(1)).close(); // make sure we closed the statement
            verify(conn, times(1)).close(); // make sure we closed the connection
        } catch (final Exception e) {
            caught = true;
        }

        if (!caught) {
            fail("Exception never thrown, but expected");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(conn);

        when(conn.prepareStatement(any(String.class))).thenReturn(prepStmt);
        when(prepStmt.getParameterMetaData()).thenReturn(meta);
        when(prepStmt.executeQuery()).thenReturn(results);

        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(any(String.class))).thenReturn(results);

        when(results.next()).thenReturn(false);

         handler = new ArrayHandler();
         runner = new AsyncQueryRunner(Executors.newFixedThreadPool(1), new QueryRunner(dataSource));
    }

    @Test
    void testAddBatchException() throws Exception {
        final String[][] params = { { "unit", "unit" }, { "test", "test" } };

        callBatchWithException("select * from blah where ? = ?", params);
    }

    //
    // Random tests
    //
    @Test
    void testBadPrepareConnection() throws Exception {
        assertThrows(ExecutionException.class, () -> {
            runner = new AsyncQueryRunner(Executors.newFixedThreadPool(1));
            runner.update("update blah set unit = test").get();
        });
    }

    @Test
    void testExecuteBatchException() throws Exception {
        final String[][] params = { { "unit", "unit" }, { "test", "test" } };

        callBatchWithException("select * from blah where ? = ?", params);
    }

    @Test
    void testExecuteQueryException() throws Exception {
        callQueryWithException(handler, "unit", "test");
    }

    @Test
    void testExecuteUpdateException() throws Exception {
        doThrow(new SQLException()).when(prepStmt).executeUpdate();

        callUpdateWithException("unit", "test");
    }

    @Test
    void testGoodBatch() throws Exception {
        final String[][] params = { { "unit", "unit" }, { "test", "test" } };

        callGoodBatch(params);
    }

    @Test
    void testGoodBatchDefaultConstructor() throws Exception {
        runner = new AsyncQueryRunner(Executors.newFixedThreadPool(1));
        final String[][] params = { { "unit", "unit" }, { "test", "test" } };

        callGoodBatch(conn, params);
    }

    @SuppressWarnings("deprecation") // deliberate test of deprecated code
    @Test
    void testGoodBatchPmdTrue() throws Exception {
        runner = new AsyncQueryRunner(dataSource, true, Executors.newFixedThreadPool(1));
        final String[][] params = { { "unit", "unit" }, { "test", "test" } };

        callGoodBatch(params);
    }

    @Test
    void testGoodQuery() throws Exception {
        callGoodQuery();
    }

    @Test
    void testGoodQueryDefaultConstructor() throws Exception {
        runner = new AsyncQueryRunner(Executors.newFixedThreadPool(1));
        callGoodQuery(conn);
    }

    @SuppressWarnings("deprecation") // deliberate test of deprecated code
    @Test
    void testGoodQueryPmdTrue() throws Exception {
        runner = new AsyncQueryRunner(true, Executors.newFixedThreadPool(1));
        callGoodQuery(conn);
    }

    @Test
    void testGoodUpdate() throws Exception {
        callGoodUpdate();
    }

    @Test
    void testGoodUpdateDefaultConstructor() throws Exception {
        runner = new AsyncQueryRunner(Executors.newFixedThreadPool(1));
        callGoodUpdate(conn);
    }

    @SuppressWarnings("deprecation") // deliberate test of deprecated code
    @Test
    void testGoodUpdatePmdTrue() throws Exception {
        runner = new AsyncQueryRunner(true, Executors.newFixedThreadPool(1));
        callGoodUpdate(conn);
    }

    @Test
    void testInsertUsesGivenQueryRunner() throws Exception {
        final QueryRunner mockQueryRunner = mock(QueryRunner.class, org.mockito.Mockito.withSettings().verboseLogging()); // debug for Continuum
        runner = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), mockQueryRunner);
        final List<Future<Object[]>> futures = new ArrayList<>();
        futures.add(runner.insert("1", handler));
        futures.add(runner.insert("2", handler, "param1"));
        futures.add(runner.insert(conn, "3", handler));
        futures.add(runner.insert(conn, "4", handler, "param1"));
        Streams.failableStream(futures).forEach(f -> f.get(10, TimeUnit.SECONDS));
        verify(mockQueryRunner).insert("1", handler);
        verify(mockQueryRunner).insert("2", handler, "param1");
        verify(mockQueryRunner).insert(conn, "3", handler);
        verify(mockQueryRunner).insert(conn, "4", handler, "param1");
    }

    @Test
    void testNoParamsQuery() throws Exception {
        callGoodQuery();
    }

    @Test
    void testNoParamsUpdate() throws Exception {
        callGoodUpdate();
    }

    @Test
    void testNullConnectionBatch() throws Exception {
        assertThrows(ExecutionException.class, () -> {
            final String[][] params = {{"unit", "unit"}, {"test", "test"}};

            when(dataSource.getConnection()).thenReturn(null);

            runner.batch("select * from blah where ? = ?", params).get();
        });
    }

    @Test
    void testNullConnectionQuery() throws Exception {
        assertThrows(ExecutionException.class, () -> {
            when(dataSource.getConnection()).thenReturn(null);

            runner.query("select * from blah where ? = ?", handler, "unit", "test").get();
        });
    }

    @Test
    void testNullConnectionUpdate() throws Exception {
        assertThrows(ExecutionException.class, () -> {
            when(dataSource.getConnection()).thenReturn(null);

            runner.update("select * from blah where ? = ?", "unit", "test").get();
        });
    }

    @Test
    void testNullHandlerQuery() throws Exception {
        assertThrows(ExecutionException.class, () ->
            runner.query("select * from blah where ? = ?", null).get());
    }

    @Test
    void testNullParamsArgBatch() throws Exception {
        assertThrows(ExecutionException.class, () ->
            runner.batch("select * from blah where ? = ?", null).get());
    }

    @Test
    void testNullParamsBatch() throws Exception {
        final String[][] params = { { null, "unit" }, { "test", null } };

        callGoodBatch(params);
    }

    @Test
    void testNullSqlBatch() throws Exception {
        assertThrows(ExecutionException.class, () -> {
            final String[][] params = {{"unit", "unit"}, {"test", "test"}};

            runner.batch(null, params).get();
        });
    }

    @Test
    void testNullSqlQuery() throws Exception {
        assertThrows(ExecutionException.class, () ->
            runner.query(null, handler).get());
    }

    @Test
    void testNullSqlUpdate() throws Exception {
        assertThrows(ExecutionException.class, () ->
            runner.update(null).get());
    }

    @Test
    void testTooFewParamsBatch() throws Exception {
        final String[][] params = { { "unit" }, { "test" } };

        callBatchWithException("select * from blah where ? = ?", params);
    }

    @Test
    void testTooFewParamsQuery() throws Exception {
        callQueryWithException("unit");
    }

    @Test
    void testTooFewParamsUpdate() throws Exception {
        callUpdateWithException("unit");
    }

    @Test
    void testTooManyParamsBatch() throws Exception {
        final String[][] params = { { "unit", "unit", "unit" }, { "test", "test", "test" } };

        callBatchWithException("select * from blah where ? = ?", params);
    }

    @Test
    void testTooManyParamsQuery() throws Exception {
        callQueryWithException("unit", "test", "fail");
    }

    @Test
    void testTooManyParamsUpdate() throws Exception {
        callUpdateWithException("unit", "test", "fail");
    }
}
