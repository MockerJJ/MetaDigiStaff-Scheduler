package com.chinatelecom.scheduler.data.provider;


import com.chinatelecom.scheduler.data.SimpleTable;
import com.chinatelecom.scheduler.data.TableColumn;

import java.sql.SQLException;
import java.util.List;

public interface DataMetaProvider<T extends DataQueryRequest> {

    List<SimpleTable> queryTables(T request, String schema) throws Exception;

    List<TableColumn> queryColumns(T request, String tableName, String schema) throws Exception;

    List<TableColumn> getTableColumnsOfSql(T request) throws SQLException;
}