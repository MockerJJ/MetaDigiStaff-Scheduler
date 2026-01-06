package com.chinatelecom.scheduler.data.jdbc.connection;

import com.chinatelecom.scheduler.data.jdbc.catalog.JdbcCatalog;
import com.chinatelecom.scheduler.data.jdbc.dialect.JdbcDialect;
import lombok.Data;

import javax.sql.DataSource;

@Data
public class DatasourceWrapper {

    private DataSource dataSource;

    private JdbcDialect jdbcDialect;

    private JdbcCatalog catalog;

    private Long freshTime;
}
