package org.xiaoxi.cassandra.dao;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.google.common.util.concurrent.ListenableFuture;
import org.xiaoxi.cassandra.dao.Abstract${tableClass}Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
* Created by xiaoxi on ${.now}.
*/
@Repository
public class ${tableClass}Dao extends Abstract${tableClass}Dao {

    @Autowired
    public ${tableClass}Dao(Session session, ListenableFuture<Session> sessionAsync, MappingManager manager){
        super(session,sessionAsync,manager);
        //mapper.setDefaultGetOptions(Mapper.Option.tracing(true), Mapper.Option.consistencyLevel(ConsistencyLevel.QUORUM));
        //mapper.setDefaultSaveOptions(Mapper.Option.ttl(60), Mapper.Option.saveNullFields(false), Mapper.Option.consistencyLevel(ConsistencyLevel.TWO));
        //mapper.setDefaultDeleteOptions(Mapper.Option.consistencyLevel(ConsistencyLevel.TWO));
    }
}
