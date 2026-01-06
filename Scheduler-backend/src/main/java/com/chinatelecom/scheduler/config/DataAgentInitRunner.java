package com.chinatelecom.scheduler.config;

import com.chinatelecom.scheduler.config.data.DataAgentConfig;
import com.chinatelecom.scheduler.config.data.DataAgentConstants;
import com.chinatelecom.scheduler.config.data.EsConfig;
import com.chinatelecom.scheduler.config.data.QdrantConfig;
import com.chinatelecom.scheduler.service.ChatModelInfoService;
import com.chinatelecom.scheduler.service.ColumnValueSyncService;
import com.chinatelecom.scheduler.service.QdrantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataAgentInitRunner implements CommandLineRunner {

    @Autowired
    private DataAgentConfig dataAgentConfig;
    @Autowired
    private QdrantService qdrantService;
    @Autowired
    private ChatModelInfoService chatModelInfoService;
    @Autowired
    private ColumnValueSyncService columnValueSyncService;


    @Override
    public void run(String... args) throws Exception {
        log.info("dataAgent config:{}", dataAgentConfig);
        QdrantConfig qdrantConfig = dataAgentConfig.getQdrantConfig();
        if (qdrantConfig.getEnable()) {
            qdrantService.createCosineCollection(DataAgentConstants.SCHEMA_COLLECTION_NAME, 1024);
            log.info("qdrant collection init success");
        }
        EsConfig esConfig = dataAgentConfig.getEsConfig();
        if (esConfig.getEnable()) {
            columnValueSyncService.initColumnValueIndex();
            log.info("column value es index init success");
        }
        chatModelInfoService.initModelInfo(dataAgentConfig);
    }
}
