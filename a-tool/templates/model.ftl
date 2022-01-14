package ${table.packageName};


import com.server.model.*;
import com.server.model.operator.*;
import com.server.model.annotation.*;
import com.server.model.index.*;
import lombok.*;
import lombok.experimental.*;

import java.util.*;

/**
 *
 * ${table.name!}
 *
 * @author ${author}
 */
@Getter
@Setter
@ToString
@ModelObject
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
public class ${table.entityName} extends AbstractModel<${table.entityName}> {

<#-- 生成字段 -->
<#list table.fields as field>
    <#if field.valueType>
    /**
     * ${field.description}
     */
    <#if field.primaryKey>
    @PrimaryKey
    </#if>
    <#if field.array>
    ${field.type} ${field.name} = new ArrayList<>();
    <#else>
    <#if field.defaultValue??>
    ${field.type} ${field.name} = ${field.defaultValue};
    <#else>
    ${field.type} ${field.name};
    </#if>
    </#if>
    </#if>

</#list>
    public static ModelTable<${table.entityName}> table() {
        return Models.getContainer().getTable(${table.entityName}.class);
    }

    public static List<${table.entityName}> getAll() {
        return table().getAll();
    }

    public static ${table.entityName} get(Integer id) {
        return table().get(id);
    }

    public static ${table.entityName} get(String field, Object... id) {
        return table().get(field, (Class<? extends IIndex>) null).query(id);
    }

    public static ${table.entityName} get(String field, Class<? extends IIndex> indexClass, Object... id) {
        return table().get(field, indexClass).query(id);
    }

    public static <O extends IOperator<${table.entityName}>> O get(Class<O> index) {
        return table().getOperator(index);
    }

    public static ${table.entityName} getOrDefault(int id, ${table.entityName} def) {
        return table().getOrDefault(id, def);
    }

    public static boolean contains(int id) {
        return table().contains(id);
    }
}