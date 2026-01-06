package com.chinatelecom.scheduler.data.jdbc.catalog;


import com.chinatelecom.scheduler.data.jdbc.dialect.DialectEnum;

public interface JdbcCatalogFactory {

    DialectEnum jdbcDialect();

    /**
     * Creates a {@link JdbcCatalog} using the options.
     */
    JdbcCatalog createCatalog();
}
