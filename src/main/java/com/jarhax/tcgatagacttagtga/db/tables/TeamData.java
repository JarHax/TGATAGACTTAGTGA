/*
 * This file is generated by jOOQ.
*/
package com.jarhax.tcgatagacttagtga.db.tables;


import com.jarhax.tcgatagacttagtga.db.Folding;
import com.jarhax.tcgatagacttagtga.db.Indexes;
import com.jarhax.tcgatagacttagtga.db.Keys;
import com.jarhax.tcgatagacttagtga.db.tables.records.TeamDataRecord;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.7"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TeamData extends TableImpl<TeamDataRecord> {

    private static final long serialVersionUID = -129925943;

    /**
     * The reference instance of <code>folding.team_data</code>
     */
    public static final TeamData TEAM_DATA = new TeamData();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TeamDataRecord> getRecordType() {
        return TeamDataRecord.class;
    }

    /**
     * The column <code>folding.team_data.DATE</code>.
     */
    public final TableField<TeamDataRecord, Date> DATE = createField("DATE", org.jooq.impl.SQLDataType.DATE.nullable(false), this, "");

    /**
     * The column <code>folding.team_data.TEAM_ID</code>.
     */
    public final TableField<TeamDataRecord, Integer> TEAM_ID = createField("TEAM_ID", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>folding.team_data.POINTS_TOTAL</code>.
     */
    public final TableField<TeamDataRecord, Long> POINTS_TOTAL = createField("POINTS_TOTAL", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>folding.team_data.WORK_UNITS_TOTAL</code>.
     */
    public final TableField<TeamDataRecord, Long> WORK_UNITS_TOTAL = createField("WORK_UNITS_TOTAL", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>folding.team_data.FLDC_FOLDERS_TOTAL</code>.
     */
    public final TableField<TeamDataRecord, Integer> FLDC_FOLDERS_TOTAL = createField("FLDC_FOLDERS_TOTAL", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * Create a <code>folding.team_data</code> table reference
     */
    public TeamData() {
        this(DSL.name("team_data"), null);
    }

    /**
     * Create an aliased <code>folding.team_data</code> table reference
     */
    public TeamData(String alias) {
        this(DSL.name(alias), TEAM_DATA);
    }

    /**
     * Create an aliased <code>folding.team_data</code> table reference
     */
    public TeamData(Name alias) {
        this(alias, TEAM_DATA);
    }

    private TeamData(Name alias, Table<TeamDataRecord> aliased) {
        this(alias, aliased, null);
    }

    private TeamData(Name alias, Table<TeamDataRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Folding.FOLDING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.TEAM_DATA_FKIDX_87, Indexes.TEAM_DATA_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<TeamDataRecord> getPrimaryKey() {
        return Keys.KEY_TEAM_DATA_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<TeamDataRecord>> getKeys() {
        return Arrays.<UniqueKey<TeamDataRecord>>asList(Keys.KEY_TEAM_DATA_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<TeamDataRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<TeamDataRecord, ?>>asList(Keys.FK_87);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TeamData as(String alias) {
        return new TeamData(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TeamData as(Name alias) {
        return new TeamData(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public TeamData rename(String name) {
        return new TeamData(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public TeamData rename(Name name) {
        return new TeamData(name, null);
    }
}
