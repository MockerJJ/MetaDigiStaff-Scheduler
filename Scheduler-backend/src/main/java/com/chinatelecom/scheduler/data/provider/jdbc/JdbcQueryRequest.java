package com.chinatelecom.scheduler.data.provider.jdbc;

import com.chinatelecom.scheduler.data.jdbc.JdbcConnectionConfig;
import com.chinatelecom.scheduler.data.provider.DataQueryRequest;
import lombok.Data;

@Data
public class JdbcQueryRequest implements DataQueryRequest {

    private JdbcConnectionConfig jdbcConnectionConfig;
    private String sql;
    private int limit;

    private int pageIndex;
    private int pageSize;
}
