package org.hswebframework.web.datasource.switcher;

import org.hswebframework.web.ThreadLocalUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 默认的动态数据源切换器,基于ThreadLocal,queue
 *
 * @author zhouhao
 * @since 3.0
 */
public class DefaultDataSourceSwitcher implements DataSourceSwitcher {

    //默认数据源标识
    private static final String DEFAULT_DATASOURCE_ID = DataSourceSwitcher.class.getName() + "_default_";

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String getDefaultDataSourceIdKey(){
        return DEFAULT_DATASOURCE_ID;
    }

    protected Deque<String> getUsedHistoryQueue() {
        // 从ThreadLocal中获取一个使用记录
        return ThreadLocalUtils.get(DefaultDataSourceSwitcher.class.getName() + "_queue", LinkedList::new);
    }

    @Override
    public void useLast() {
        // 没有上一次了
        if (getUsedHistoryQueue().isEmpty()) {
            return;
        }
        //移除队尾,则当前的队尾则为上一次的数据源
        getUsedHistoryQueue().removeLast();
        if (logger.isDebugEnabled()) {
            String current = currentDataSourceId();
            if (null != current) {
                logger.debug("try use last datasource : {}", currentDataSourceId());
            } else {
                logger.debug("try use last default datasource");
            }
        }
    }

    @Override
    public void use(String dataSourceId) {
        //添加对队尾
        getUsedHistoryQueue().addLast(dataSourceId);
        if (logger.isDebugEnabled()) {
            logger.debug("try use datasource : {}", dataSourceId);
        }
    }

    @Override
    public void useDefault() {
        getUsedHistoryQueue().addLast(getDefaultDataSourceIdKey());
        if (logger.isDebugEnabled()) {
            logger.debug("try use default datasource");
        }
    }

    @Override
    public String currentDataSourceId() {
        if (getUsedHistoryQueue().isEmpty()) {
            return null;
        }

        String activeId = getUsedHistoryQueue().getLast();
        if (getDefaultDataSourceIdKey().equals(activeId)) {
            return null;
        }
        return activeId;
    }

    @Override
    public void reset() {
        getUsedHistoryQueue().clear();
        if (logger.isDebugEnabled()) {
            logger.debug("reset datasource history");
        }
    }
}
