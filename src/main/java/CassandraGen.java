import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaoxi on 16-8-24.
 */

public class CassandraGen {
    public static void main(String[] args) throws Exception{
        generate();
    }

    private static List<Map<String,Object>> metadata=new ArrayList<Map<String,Object>>();

    private static final Boolean WETHER_GEN_MYDAO=true;
    private static final String TPL_MYDAO="MyDao.ftl";
    private static final String GEN_MYPATH="src/main/java/org/xiaoxi/cassandra/dao/";

    private static final String TPL_PATH="src/main/resources/freemarker/";
    private static final String TPL_TABLE="Table.ftl";
    private static final String TPL_DTO="Dto.ftl";
    private static final String TPL_DAO="AbstractDao.ftl";
    private static final String GEN_PATH="gensrc/main/java/org/xiaoxi/cassandra/";
    private static final String GEN_TABLE_PATH="table/";
    private static final String GEN_DTO_PATH="dto/";
    private static final String GEN_DAO_PATH="dao/";


    private static final String PK="PartitionKey";
    private static final String CK="ClusteringColumn";
    private static final String CL="Column";
    private static final String IDX="SecondaryIndex";

    private static final String TYPE_BOOLEAN="Boolean";
    private static final String TYPE_STRING="String";
    private static final String TYPE_INTEGER="Integer";
    private static final String TYPE_LONG="Long";
    private static final String TYPE_FLOAT="Float";
    private static final String TYPE_DOUBLE="Double";
    private static final String TYPE_BIGINTERGER="BigInteger";//java.math.BigInteger
    private static final String TYPE_DECIMAL="BigDecimal";//java.math.BigDecimal
    private static final String TYPE_UUID="UUID";
    private static final String TYPE_DATE="DATE";
    private static final String TYPE_INET="InetAddress";//java.net.InetAddress
    private static final String TYPE_BYTEBUFFER="ByteBuffer";//java.nio.ByteBuffer
    private static final String TYPE_MAP="Map";
    private static final String TYPE_SET="Set";
    private static final String TYPE_LIST="List";

    private static final String _TYPE_BOOLEAN="Boolean";
    private static final String _TYPE_STRING="UTF8"; //Reversed(UTF8)
    private static final String _TYPE_INTEGER="Int32";
    private static final String _TYPE_LONG="Long";
    private static final String _TYPE_FLOAT="Float";
    private static final String _TYPE_DOUBLE="Double";
    private static final String _TYPE_BIGINTERGER="BigInteger";//not found
    private static final String _TYPE_DECIMAL="BigDecimal";//not found
    private static final String _TYPE_UUID="UUID";
    private static final String _TYPE_TIMEUUID="TimeUUID";
    private static final String _TYPE_DATE="Timestamp";
    private static final String _TYPE_INET="InetAddress";
    private static final String _TYPE_BYTEBUFFER="Bytes";
    private static final String _TYPE_MAP="Map";//Map(UUID,Bytes)
    private static final String _TYPE_SET="Set";// Set(Int32)
    private static final String _TYPE_LIST="List";//List(Frozen(Map(UTF8,UTF8)))


    private static void generate() throws Exception{
        getMetaData();
        metadata.stream().forEach(list->System.out.println(list));
        generateTable();
        generateDto();
        generateDao();
        if(WETHER_GEN_MYDAO) generateMyDao();
    }

    private static void generateTable() throws Exception{
        Configuration cfg = new Configuration();

        Template template = cfg.getTemplate(TPL_PATH+TPL_TABLE);
        for(Map<String,Object> table:metadata){
            File newsDir = new File(GEN_PATH+GEN_TABLE_PATH);
            if (!newsDir.exists()) newsDir.mkdirs();
            Writer out = new OutputStreamWriter(new FileOutputStream(GEN_PATH+GEN_TABLE_PATH
                    +table.get("tableClass")+"Table.java"), "UTF-8");
            template.process(table, out);
        }
    }

    private static void generateDto() throws Exception{
        Configuration cfg = new Configuration();

        Template template = cfg.getTemplate(TPL_PATH+TPL_DTO);
        for(Map<String,Object> table:metadata){
            File newsDir = new File(GEN_PATH+GEN_DTO_PATH);
            if (!newsDir.exists()) newsDir.mkdirs();
            Writer out = new OutputStreamWriter(new FileOutputStream(GEN_PATH+GEN_DTO_PATH
                    +table.get("tableClass")+"Dto.java"), "UTF-8");
            template.process(table, out);
        }
    }

    private static void generateDao() throws Exception{
        Configuration cfg = new Configuration();

        Template template = cfg.getTemplate(TPL_PATH+TPL_DAO);
        for(Map<String,Object> table:metadata){
            File newsDir = new File(GEN_PATH+GEN_DAO_PATH);
            if (!newsDir.exists()) newsDir.mkdirs();
            Writer out = new OutputStreamWriter(new FileOutputStream(GEN_PATH+GEN_DAO_PATH
                    +"Abstract"+table.get("tableClass")+"Dao.java"), "UTF-8");
            template.process(table, out);
        }
    }

    private static void generateMyDao() throws Exception{
        Configuration cfg = new Configuration();

        Template template = cfg.getTemplate(TPL_PATH+TPL_MYDAO);
        for(Map<String,Object> table:metadata){
            File newsDir = new File(GEN_MYPATH);
            if (!newsDir.exists()) newsDir.mkdirs();
            Writer out = new OutputStreamWriter(new FileOutputStream(GEN_MYPATH
                    +table.get("tableClass")+"Dao.java"), "UTF-8");
            template.process(table, out);
        }
    }


    private static void getMetaData(){
        Session session = Cluster.builder()
                .withClusterName("Hue Cluster")
                .addContactPoint("localhost")
                .build().connect();

        ResultSet rs=session.execute("select columnfamily_name, column_name, component_index, index_type, type,validator " +
                "from system.schema_columns where keyspace_name='elephas'");

        for(Row r: rs){
            Map<String,Object> map=new HashMap<String,Object>();
            map.put("columns",new ArrayList<Map<String,Object>>());
            boolean isfound=false;
            for(Map<String,Object> table: metadata){
                if(table.get("tableName").equals(r.getString("columnfamily_name"))){
                    map=table;
                    isfound=true;
                }
            }

            if(!isfound) {
                metadata.add(map);
                map.put("tableName", r.getString("columnfamily_name"));
                map.put("tableClass", toClassName(r.getString("columnfamily_name")));
                map.put("tableField", toFieldName(r.getString("columnfamily_name")));
            }

            List<Map<String,Object>> columns=(List<Map<String,Object>>)map.get("columns");
            Map<String,Object> column=new HashMap<String,Object>();
            column.put("columnName",r.getString("column_name"));
            column.put("columnConstant", r.getString("column_name").toUpperCase());
            column.put("columnClass", toClassName(r.getString("column_name")));
            column.put("columnField", toFieldName(r.getString("column_name")));
            column.put("keyIndex", r.getInt("component_index"));
            column.put("keyType", toKeyType(r.getString("type"),r.getString("index_type")));
            column.put("type", toType(r.getString("validator")));
            column.put("typeFrozen", toTypeFrozen(r.getString("validator")));
            columns.add(column);
        }

        for(Map<String,Object> table: metadata){
            List<Map<String,Object>> columns = (List<Map<String,Object>>)table.get("columns");
            columns.sort((o1,o2)->{
                Integer t1=getKeyTypeOrder((String)o1.get("keyType"));
                Integer i1=(Integer)o1.get("keyIndex");
                String c1=(String)o1.get("columnField");

                Integer t2=getKeyTypeOrder((String)o2.get("keyType"));
                Integer i2=(Integer)o2.get("keyIndex");
                String c2=(String)o2.get("columnField");

                if(t1<t2) return -1;
                else if(t1>t2) return 1;
                else if(i1<i2) return -1;
                else if(i1>i2) return 1;
                else return c1.compareTo(c2);
            });
            int pkCount=0;
            int ckCount=0;
            int idxCount=0;

            for(Map<String,Object> column:columns){
                if(column.get("keyType").equals(PK)) pkCount++;
                else if(column.get("keyType").equals(CK)) ckCount++;
                else if(column.get("keyType").equals(IDX)) idxCount++;
            }

            table.put("pkCount",pkCount);
            table.put("ckCount",ckCount);
            table.put("idxCount",idxCount);
        }
        session.getCluster().close();
    }

    private static int getKeyTypeOrder(String keyType){
        int order=2;
        switch(keyType) {
            case PK:
                order = 0;
                break;
            case CK:
                order = 1;
                break;
            case IDX:
                order =2;
            default:
                order = 3;
                break;
        }
        return order;
    }

    private static String toFieldName(String name){
        StringBuilder result0 = new StringBuilder();
        if (name != null && name.length() > 0) {
            result0.append(name.substring(0, 1).toLowerCase());
            for (int i = 1; i < name.length(); i++) {
                String s = name.substring(i, i + 1);
                if (s.equals(s.toUpperCase()) && !Character.isDigit(s.charAt(0))) {
                    result0.append("_");
                }
                result0.append(s.toLowerCase());
            }
        }

        name=result0.toString();

        StringBuilder result = new StringBuilder();
        if (name == null || name.isEmpty()) {
            return "";
        } else if (!name.contains("_")) {
            return name.substring(0, 1).toLowerCase() + name.substring(1);
        }
        String camels[] = name.split("_");
        for (String camel :  camels) {
            if (camel.isEmpty()) {
                continue;
            }
            if (result.length() == 0) {
                result.append(camel.toLowerCase());
            } else {
                result.append(camel.substring(0, 1).toUpperCase());
                result.append(camel.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }



    private static String toClassName(String name){
        if(name==null) return null;
        String s= toFieldName(name);

        if(Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    private static String toKeyType(String str, String indexType){
        if(indexType!=null) return IDX;
        if(str.equals("partition_key")) return PK;
        if(str.equals("clustering_key")) return CK;
        return CL;
    }

    private static String toType(String str){
        String result=str;
        result=result.replace("org.apache.cassandra.db.marshal.","");
        result=result.replace("Type","");
        int count=countSubStr(result,"Frozen(")+countSubStr(result,"Reversed(");
        result=result.substring(0,result.length()-count);
        result=result.replace("Frozen(","");
        result=result.replace("Reversed(","");
        result=result.replace("(","<");
        result=result.replace(")",">");
        result=result.replace("UTF8","String");
        result=result.replace("Int32","Integer");
        result=result.replace("Bytes","ByteBuffer");
        result=result.replace("Timestamp","Date");
        result=result.replace("TimeUUID","UUID");
        return result;
    }

    private static String toTypeFrozen(String str){
        String result=str;
        if(str.indexOf("FrozenType(")==-1) return null;
        result=result.replace("org.apache.cassandra.db.marshal.","");
        result=result.replace("Type","");
        int count=countSubStr(result,"Reversed(");
        result=result.substring(0,result.length()-count);
        result=result.replace("Reversed(","");
        result=result.replace("(","<");
        result=result.replace(")",">");
        result=result.replace("UTF8","String");
        result=result.replace("Int32","Integer");
        result=result.replace("Bytes","ByteBuffer");
        result=result.replace("Timestamp","Date");
        result=result.replace("TimeUUID","UUID");
        return result.toLowerCase();
    }

    private static int countSubStr (String str, String str1){
        int count = 0;
        int start = 0;
        while (str.indexOf(str1, start) >= 0 && start < str.length()) {
            count++;
            start = str.indexOf(str1, start) + str1.length();
        }
        return count;
    }























}
