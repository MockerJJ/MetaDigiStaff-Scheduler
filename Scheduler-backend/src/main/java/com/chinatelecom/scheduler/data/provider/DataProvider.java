package com.chinatelecom.scheduler.data.provider;


import com.chinatelecom.scheduler.data.QueryResult;


public interface DataProvider<T extends DataQueryRequest> {

    QueryResult queryData(T request) throws Exception;

    boolean queryForTest(T request);
}
