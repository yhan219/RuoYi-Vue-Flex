package org.dromara.generator.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import com.mybatisflex.core.datasource.DataSourceKey;
import com.mybatisflex.core.keygen.impl.SnowFlakeIDKeyGenerator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.file.FileUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.helper.DataBaseHelper;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.generator.constant.GenConstants;
import org.dromara.generator.domain.GenTable;
import org.dromara.generator.domain.GenTableColumn;
import org.dromara.generator.mapper.GenTableColumnMapper;
import org.dromara.generator.mapper.GenTableMapper;
import org.dromara.generator.util.GenUtils;
import org.dromara.generator.util.VelocityInitializer;
import org.dromara.generator.util.VelocityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.dromara.generator.domain.table.GenTableTableDef.GEN_TABLE;

/**
 * 业务 服务层实现
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GenTableServiceImpl implements IGenTableService {

    private final GenTableMapper baseMapper;
    private final GenTableColumnMapper genTableColumnMapper;

    /**
     * 查询业务字段列表
     *
     * @param tableId 业务字段编号
     * @return 业务字段集合
     */
    @Override
    public List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId) {
        return genTableColumnMapper.selectListByQuery(QueryWrapper.create()
            .where(GenTableColumn::getTableId).eq(tableId)
            .orderBy(GenTableColumn::getSort, true));
    }

    /**
     * 查询业务信息
     *
     * @param id 业务ID
     * @return 业务信息
     */
    @Override
    public GenTable selectGenTableById(Long id) {
        GenTable genTable = baseMapper.selectOneWithRelationsById(id);
        setTableFromOptions(genTable);
        return genTable;
    }

    @Override
    public TableDataInfo<GenTable> selectPageGenTableList(GenTable genTable, PageQuery pageQuery) {
        QueryWrapper queryWrapper = this.buildGenTableQueryWrapper(genTable);
        Page<GenTable> page = baseMapper.paginate(pageQuery, queryWrapper);
        return TableDataInfo.build(page);
    }

    private QueryWrapper buildGenTableQueryWrapper(GenTable genTable) {
        Map<String, Object> params = genTable.getParams();
        return QueryWrapper.create().from(GEN_TABLE)
            .where(GEN_TABLE.DATA_NAME.eq(genTable.getDataName()))
            .and(QueryMethods.lower(GEN_TABLE.TABLE_NAME).like(StringUtils.lowerCase(genTable.getTableName())))
            .and(QueryMethods.lower(GEN_TABLE.TABLE_COMMENT).like(StringUtils.lowerCase(genTable.getTableComment())))
            .and(GEN_TABLE.CREATE_TIME.between(params.get("beginTime"), params.get("endTime"), params.get("beginTime") != null && params.get("endTime") != null));
    }

    @Override
    public TableDataInfo<GenTable> selectPageDbTableList(GenTable genTable, PageQuery pageQuery) {
        try {
            DataSourceKey.use(genTable.getDataName());
            List<String> value = baseMapper.selectTableNameList(genTable.getDataName());
            genTable.getParams().put("genTableNames", value);
            Page<GenTable> page = selectPageDbTableList(pageQuery.build(), genTable);
            return TableDataInfo.build(page);
        } finally {
            DataSourceKey.clear();
        }
    }


    private Page<GenTable> selectPageDbTableList(Page<GenTable> page, GenTable genTable) {
        List<String> genTableNames = (List<String>) genTable.getParams().get("genTableNames");
        String tableName = StringUtils.lowerCase(genTable.getTableName());
        String tableComment = StringUtils.lowerCase(genTable.getTableComment());

        if (DataBaseHelper.isMySql()) {
            QueryWrapper queryWrapper = QueryWrapper.create()
                .select("table_name", "table_comment", "create_time", "update_time")
                .from("information_schema.tables")
                .where("table_schema = (select database())")
                .and("table_name NOT LIKE 'pj_%' AND table_name NOT LIKE 'gen_%'")
                .and(QueryMethods.column("table_name").notIn(genTableNames, If::isNotEmpty))
                .and(QueryMethods.column("lower(table_name)").like(tableName))
                .and(QueryMethods.column("lower(table_comment)").like(tableComment))
                .orderBy("create_time", false);
            return baseMapper.paginate(page, queryWrapper);
        }
        if (DataBaseHelper.isOracle()) {
            QueryWrapper queryWrapper = QueryWrapper.create()
                .select(new QueryColumn("lower(dt.table_name)").as("table_name"),
                    new QueryColumn("dtc.comments").as("table_comment"),
                    new QueryColumn("uo.created").as("create_time"),
                    new QueryColumn("uo.last_ddl_time").as("update_time")
                )
                .from(new QueryTable("user_tables").as("dt"), new QueryTable("user_tab_comments").as("dtc"), new QueryTable("user_objects").as("uo"))
                .where("dt.table_name = dtc.table_name and dt.table_name = uo.object_name and uo.object_type = 'TABLE'")
                .and("dt.table_name NOT LIKE 'pj_%' AND dt.table_name NOT LIKE 'GEN_%'")
                .and(QueryMethods.column("lower(dt.table_name)").notIn(genTableNames, If::isNotEmpty))
                .and(QueryMethods.column("lower(dt.table_name)").like(tableName))
                .and(QueryMethods.column("lower(dtc.comments)").like(tableComment))
                .orderBy("create_time", false);
            return baseMapper.paginate(page, queryWrapper);
        }
        if (DataBaseHelper.isPostgerSql()) {

            QueryWrapper queryWrapper = QueryWrapper.create()
                .with("list_table").asRaw("""
                    SELECT c.relname AS table_name,
                                            obj_description(c.oid) AS table_comment,
                                            CURRENT_TIMESTAMP AS create_time,
                                            CURRENT_TIMESTAMP AS update_time
                                    FROM pg_class c
                                        LEFT JOIN pg_namespace n ON n.oid = c.relnamespace
                                    WHERE (c.relkind = ANY (ARRAY ['r'::"char", 'p'::"char"]))
                                        AND c.relname != 'spatial_%'::text
                                        AND n.nspname = 'public'::name
                                        AND n.nspname <![CDATA[ <> ]]> ''::name
                    """)
                .select(new QueryColumn("c.relname").as("table_name"),
                    new QueryColumn("obj_description(c.oid)").as("table_comment"),
                    new QueryColumn("CURRENT_TIMESTAMP").as("create_time"),
                    new QueryColumn("CURRENT_TIMESTAMP").as("update_time")
                )
                .from("list_table")
                .where("table_name NOT LIKE 'pj_%' AND table_name NOT LIKE 'gen_%'")
                .and(QueryMethods.column("table_name").notIn(genTableNames, If::isNotEmpty))
                .and(QueryMethods.lower("table_name").like(tableName))
                .and(QueryMethods.lower("table_comment").like(tableComment))
                .orderBy("create_time", false);
            return baseMapper.paginate(page, queryWrapper);
        }
        if (DataBaseHelper.isSqlServer()) {
           QueryWrapper queryWrapper = QueryWrapper.create()
                .select(new QueryColumn("cast(D.NAME as nvarchar)").as("table_name"),
                    new QueryColumn("cast(F.VALUE as nvarchar)").as("table_comment"),
                    new QueryColumn("crdate").as("create_time"),
                    new QueryColumn("refdate").as("update_time")
                )
                .from(new QueryTable("SYSOBJECTS").as("D"))
                .innerJoin("SYS.EXTENDED_PROPERTIES F")
                .on("D.ID = F.MAJOR_ID")
                .where("F.MINOR_ID = 0 AND D.XTYPE = 'U' AND D.NAME != 'DTPROPERTIES' AND D.NAME NOT LIKE 'pj_%' AND D.NAME NOT LIKE 'gen_%'")
                .and(QueryMethods.column("D.NAME").notIn(genTableNames, If::isNotEmpty))
                .and(QueryMethods.lower("D.NAME").like(tableName))
                .and(QueryMethods.lower("CAST(F.VALUE AS nvarchar)").like(tableComment))
                .orderBy("crdate", false);
            return baseMapper.paginate(page, queryWrapper);
        }
        throw new ServiceException("不支持的数据库类型");
    }

    /**
     * 查询据库列表
     *
     * @param tableNames 表名称组
     * @param dataName   数据源名称
     * @return 数据库表集合
     */
    @Override
    public List<GenTable> selectDbTableListByNames(String[] tableNames, String dataName) {
        try {
            DataSourceKey.use(dataName);
            return baseMapper.selectDbTableListByNames(tableNames);
        } finally {
            DataSourceKey.clear();
        }
    }

    /**
     * 查询所有表信息
     *
     * @return 表信息集合
     */
    @Override
    public List<GenTable> selectGenTableAll() {
        return baseMapper.selectGenTableAll();
    }

    /**
     * 修改业务
     *
     * @param genTable 业务信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateGenTable(GenTable genTable) {
        String options = JsonUtils.toJsonString(genTable.getParams());
        genTable.setOptions(options);
        int row = baseMapper.update(genTable);
        if (row > 0) {
            for (GenTableColumn cenTableColumn : genTable.getColumns()) {
                genTableColumnMapper.update(cenTableColumn);
            }
        }
    }

    /**
     * 删除业务对象
     *
     * @param tableIds 需要删除的数据ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteGenTableByIds(Long[] tableIds) {
        List<Long> ids = Arrays.asList(tableIds);
        baseMapper.deleteBatchByIds(ids);
        genTableColumnMapper.deleteByQuery(QueryWrapper.create().from(GenTableColumn.class).where(GenTableColumn::getTableId).in(ids));
    }

    /**
     * 导入表结构
     *
     * @param tableList 导入表列表
     * @param dataName  数据源名称
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importGenTable(List<GenTable> tableList, String dataName) {
        Long operId = LoginHelper.getUserId();
        try {
            for (GenTable table : tableList) {
                String tableName = table.getTableName();
                GenUtils.initTable(table, operId);
                table.setDataName(dataName);
                int row = baseMapper.insert(table,true);
                if (row > 0) {
                    // 保存列信息
                    try {
                        DataSourceKey.use(dataName);
                        List<GenTableColumn> genTableColumns = genTableColumnMapper.selectDbTableColumnsByName(tableName);
                        List<GenTableColumn> saveColumns = new ArrayList<>();
                        for (GenTableColumn column : genTableColumns) {
                            GenUtils.initColumnField(column, table);
                            saveColumns.add(column);
                        }
                        if (CollUtil.isNotEmpty(saveColumns)) {
                            genTableColumnMapper.insertBatch(saveColumns);
                        }
                    } finally {
                        DataSourceKey.clear();
                    }

                }
            }
        } catch (Exception e) {
            throw new ServiceException("导入失败：" + e.getMessage());
        }
    }

    /**
     * 预览代码
     *
     * @param tableId 表编号
     * @return 预览数据列表
     */
    @Override
    public Map<String, String> previewCode(Long tableId) {
        Map<String, String> dataMap = new LinkedHashMap<>();
        // 查询表信息
        GenTable table = baseMapper.selectOneWithRelationsById(tableId);
        List<Long> menuIds = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            menuIds.add(new SnowFlakeIDKeyGenerator().nextId());
        }
        table.setMenuIds(menuIds);
        // 设置主键列信息
        setPkColumn(table);
        VelocityInitializer.initVelocity();

        VelocityContext context = VelocityUtils.prepareContext(table);

        // 获取模板列表
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF8);
            tpl.merge(context, sw);
            dataMap.put(template, sw.toString());
        }
        return dataMap;
    }

    /**
     * 生成代码（下载方式）
     *
     * @param tableId 表名称
     * @return 数据
     */
    @Override
    public byte[] downloadCode(Long tableId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        generatorCode(tableId, zip);
        IoUtil.close(zip);
        return outputStream.toByteArray();
    }

    /**
     * 生成代码（自定义路径）
     *
     * @param tableId 表名称
     */
    @Override
    public void generatorCode(Long tableId) {
        // 查询表信息
        GenTable table = baseMapper.selectOneWithRelationsById(tableId);
        // 设置主键列信息
        setPkColumn(table);

        VelocityInitializer.initVelocity();

        VelocityContext context = VelocityUtils.prepareContext(table);

        // 获取模板列表
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
        for (String template : templates) {
            if (!StringUtils.containsAny(template, "sql.vm", "api.ts.vm", "types.ts.vm", "index.vue.vm", "index-tree.vue.vm")) {
                // 渲染模板
                StringWriter sw = new StringWriter();
                Template tpl = Velocity.getTemplate(template, Constants.UTF8);
                tpl.merge(context, sw);
                try {
                    String path = getGenPath(table, template);
                    FileUtils.writeUtf8String(sw.toString(), path);
                } catch (Exception e) {
                    throw new ServiceException("渲染模板失败，表名：" + table.getTableName());
                }
            }
        }
    }

    /**
     * 同步数据库
     *
     * @param tableId 表名称
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void synchDb(Long tableId) {
        GenTable table = baseMapper.selectOneWithRelationsById(tableId);
        List<GenTableColumn> tableColumns = table.getColumns();
        Map<String, GenTableColumn> tableColumnMap = StreamUtils.toIdentityMap(tableColumns, GenTableColumn::getColumnName);
        List<GenTableColumn> dbTableColumns = null;
        try {
            DataSourceKey.use(table.getDataName());
            dbTableColumns = genTableColumnMapper.selectDbTableColumnsByName(table.getTableName());
        } finally {
            DataSourceKey.clear();
        }
        if (CollUtil.isEmpty(dbTableColumns)) {
            throw new ServiceException("同步数据失败，原表结构不存在");
        }
        List<String> dbTableColumnNames = StreamUtils.toList(dbTableColumns, GenTableColumn::getColumnName);

        List<GenTableColumn> saveColumns = new ArrayList<>();
        dbTableColumns.forEach(column -> {
            GenUtils.initColumnField(column, table);
            if (tableColumnMap.containsKey(column.getColumnName())) {
                GenTableColumn prevColumn = tableColumnMap.get(column.getColumnName());
                column.setColumnId(prevColumn.getColumnId());
                if (column.isList()) {
                    // 如果是列表，继续保留查询方式/字典类型选项
                    column.setDictType(prevColumn.getDictType());
                    column.setQueryType(prevColumn.getQueryType());
                }
                if (StringUtils.isNotEmpty(prevColumn.getIsRequired()) && !column.isPk()
                    && (column.isInsert() || column.isEdit())
                    && ((column.isUsableColumn()) || (!column.isSuperColumn()))) {
                    // 如果是(新增/修改&非主键/非忽略及父属性)，继续保留必填/显示类型选项
                    column.setIsRequired(prevColumn.getIsRequired());
                    column.setHtmlType(prevColumn.getHtmlType());
                }
            }
            saveColumns.add(column);
        });
        if (CollUtil.isNotEmpty(saveColumns)) {
            genTableColumnMapper.insertBatch(saveColumns);
        }
        List<GenTableColumn> delColumns = StreamUtils.filter(tableColumns, column -> !dbTableColumnNames.contains(column.getColumnName()));
        if (CollUtil.isNotEmpty(delColumns)) {
            List<Long> ids = StreamUtils.toList(delColumns, GenTableColumn::getColumnId);
            if (CollUtil.isNotEmpty(ids)) {
                genTableColumnMapper.deleteBatchByIds(ids);
            }
        }
    }

    /**
     * 批量生成代码（下载方式）
     *
     * @param tableIds 表ID数组
     * @return 数据
     */
    @Override
    public byte[] downloadCode(String[] tableIds) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (String tableId : tableIds) {
            generatorCode(Long.parseLong(tableId), zip);
        }
        IoUtil.close(zip);
        return outputStream.toByteArray();
    }

    /**
     * 查询表信息并生成代码
     */
    private void generatorCode(Long tableId, ZipOutputStream zip) {
        // 查询表信息
        GenTable table = baseMapper.selectOneWithRelationsById(tableId);
        List<Long> menuIds = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            menuIds.add(new SnowFlakeIDKeyGenerator().nextId());
        }
        table.setMenuIds(menuIds);
        // 设置主键列信息
        setPkColumn(table);

        VelocityInitializer.initVelocity();

        VelocityContext context = VelocityUtils.prepareContext(table);

        // 获取模板列表
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF8);
            tpl.merge(context, sw);
            try {
                // 添加到zip
                zip.putNextEntry(new ZipEntry(VelocityUtils.getFileName(template, table)));
                IoUtil.write(zip, StandardCharsets.UTF_8, false, sw.toString());
                IoUtil.close(sw);
                zip.flush();
                zip.closeEntry();
            } catch (IOException e) {
                log.error("渲染模板失败，表名：" + table.getTableName(), e);
            }
        }
    }

    /**
     * 修改保存参数校验
     *
     * @param genTable 业务信息
     */
    @Override
    public void validateEdit(GenTable genTable) {
        if (GenConstants.TPL_TREE.equals(genTable.getTplCategory())) {
            String options = JsonUtils.toJsonString(genTable.getParams());
            Dict paramsObj = JsonUtils.parseMap(options);
            if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_CODE))) {
                throw new ServiceException("树编码字段不能为空");
            } else if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_PARENT_CODE))) {
                throw new ServiceException("树父编码字段不能为空");
            } else if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_NAME))) {
                throw new ServiceException("树名称字段不能为空");
            }
        }
    }

    /**
     * 设置主键列信息
     *
     * @param table 业务表信息
     */
    public void setPkColumn(GenTable table) {
        for (GenTableColumn column : table.getColumns()) {
            if (column.isPk()) {
                table.setPkColumn(column);
                break;
            }
        }
        if (ObjectUtil.isNull(table.getPkColumn())) {
            table.setPkColumn(table.getColumns().get(0));
        }

    }

    /**
     * 设置代码生成其他选项值
     *
     * @param genTable 设置后的生成对象
     */
    public void setTableFromOptions(GenTable genTable) {
        Dict paramsObj = JsonUtils.parseMap(genTable.getOptions());
        if (ObjectUtil.isNotNull(paramsObj)) {
            String treeCode = paramsObj.getStr(GenConstants.TREE_CODE);
            String treeParentCode = paramsObj.getStr(GenConstants.TREE_PARENT_CODE);
            String treeName = paramsObj.getStr(GenConstants.TREE_NAME);
            String parentMenuId = paramsObj.getStr(GenConstants.PARENT_MENU_ID);
            String parentMenuName = paramsObj.getStr(GenConstants.PARENT_MENU_NAME);

            genTable.setTreeCode(treeCode);
            genTable.setTreeParentCode(treeParentCode);
            genTable.setTreeName(treeName);
            genTable.setParentMenuId(parentMenuId);
            genTable.setParentMenuName(parentMenuName);
        }
    }

    /**
     * 获取代码生成地址
     *
     * @param table    业务表信息
     * @param template 模板文件路径
     * @return 生成地址
     */
    public static String getGenPath(GenTable table, String template) {
        String genPath = table.getGenPath();
        if (StringUtils.equals(genPath, "/")) {
            return System.getProperty("user.dir") + File.separator + "src" + File.separator + VelocityUtils.getFileName(template, table);
        }
        return genPath + File.separator + VelocityUtils.getFileName(template, table);
    }
}

