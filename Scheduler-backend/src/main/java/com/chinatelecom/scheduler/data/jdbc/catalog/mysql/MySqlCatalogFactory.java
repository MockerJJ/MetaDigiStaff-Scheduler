package com.chinatelecom.scheduler.data.jdbc.catalog.mysql;

import com.google.auto.service.AutoService;
import com.chinatelecom.scheduler.data.jdbc.catalog.JdbcCatalog;
import com.chinatelecom.scheduler.data.jdbc.catalog.JdbcCatalogFactory;
import com.chinatelecom.scheduler.data.jdbc.dialect.DialectEnum;


@AutoService(JdbcCatalogFactory.class)
public class MySqlCatalogFactory implements JdbcCatalogFactory {
    @Override
    public DialectEnum jdbcDialect() {
        return DialectEnum.MYSQL;
    }

    @Override
    public JdbcCatalog createCatalog() {
        return new MySqlCatalog();
    }
}
