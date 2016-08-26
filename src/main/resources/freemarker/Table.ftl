package org.xiaoxi.cassandra.table;

/**
 * @generated on ${.now}
 */
public interface ${tableClass}Table {

    public final String _NAME="${tableName}";
    <#list columns as column>

    public final String ${column.columnConstant}="${column.columnName}";
    </#list>
}
