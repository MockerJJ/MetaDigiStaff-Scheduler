
package com.chinatelecom.scheduler.data.jdbc.catalog;


import com.chinatelecom.scheduler.data.SimpleTable;
import com.chinatelecom.scheduler.data.TableColumn;
import com.chinatelecom.scheduler.data.exception.CatalogException;

import java.sql.Connection;
import java.util.List;

public interface JdbcCatalog {

    List<SimpleTable> listTables(Connection connection, String schema) throws CatalogException;

    List<TableColumn> getTableColumns(Connection connection, String tablePath, String schema) throws CatalogException;
}
