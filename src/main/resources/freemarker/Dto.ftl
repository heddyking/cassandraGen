package org.xiaoxi.cassandra.dto;

import org.xiaoxi.cassandra.table.${tableClass}Table;
import com.datastax.driver.mapping.annotations.*;
import lombok.*;
import java.util.*;
import java.math.*;
import java.net.*;
import java.nio.*;

/**
 * @generated on ${.now}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = ${tableClass}Table._NAME)
public class ${tableClass}Dto {
    <#list columns as column>

    <#compress>
    <#if column.keyType="PartitionKey" >@PartitionKey(${column.keyIndex})</#if>
    <#if column.keyType="ClusteringColumn" >@ClusteringColumn(${column.keyIndex})</#if>
    <#if column.keyType="SecondaryIndex" >//@SecondaryIndex</#if>
    <#if column.typeFrozen?? >@Frozen("${column.typeFrozen}")</#if>
    @Column(name = ${tableClass}Table.${column.columnConstant})
    private ${column.type} ${column.columnField};
    </#compress>

    </#list>
}
