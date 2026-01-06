package com.chinatelecom.scheduler.data.jdbc.catalog.h2;

import com.google.auto.service.AutoService;
import com.chinatelecom.scheduler.data.jdbc.catalog.JdbcCatalog;
import com.chinatelecom.scheduler.data.jdbc.catalog.JdbcCatalogFactory;
import com.chinatelecom.scheduler.data.jdbc.dialect.DialectEnum;


@AutoService(JdbcCatalogFactory.class)
public class H2CatalogFactory implements JdbcCatalogFactory {
    @Override
    public DialectEnum jdbcDialect() {
        return DialectEnum.H2;
    }

    @Override
    public JdbcCatalog createCatalog() {
        return new H2SqlCatalog();
    }
}
