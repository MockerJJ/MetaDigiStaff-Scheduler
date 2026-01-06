package com.chinatelecom.scheduler.data.jdbc.dialect.mysql;

import com.google.auto.service.AutoService;
import com.chinatelecom.scheduler.data.jdbc.dialect.DialectEnum;
import com.chinatelecom.scheduler.data.jdbc.dialect.JdbcDialect;
import com.chinatelecom.scheduler.data.jdbc.dialect.JdbcDialectFactory;

@AutoService(JdbcDialectFactory.class)
public class MySqlDialectFactory implements JdbcDialectFactory {
    @Override
    public boolean acceptsURL(String url) {
        return url.startsWith(DialectEnum.MYSQL.getUrlPrefix());
    }

    @Override
    public JdbcDialect create() {
        return new MysqlDialect();
    }
}
