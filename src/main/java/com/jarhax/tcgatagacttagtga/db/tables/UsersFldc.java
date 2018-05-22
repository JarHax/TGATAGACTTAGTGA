/*
 * This file is generated by jOOQ.
*/
package com.jarhax.tcgatagacttagtga.db.tables;


import com.jarhax.tcgatagacttagtga.db.Folding;
import com.jarhax.tcgatagacttagtga.db.Indexes;
import com.jarhax.tcgatagacttagtga.db.Keys;
import com.jarhax.tcgatagacttagtga.db.tables.records.UsersFldcRecord;

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
public class UsersFldc extends TableImpl<UsersFldcRecord> {

    private static final long serialVersionUID = -1368548577;

    /**
     * The reference instance of <code>folding.users_fldc</code>
     */
    public static final UsersFldc USERS_FLDC = new UsersFldc();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<UsersFldcRecord> getRecordType() {
        return UsersFldcRecord.class;
    }

    /**
     * The column <code>folding.users_fldc.DATE</code>.
     */
    public final TableField<UsersFldcRecord, Date> DATE = createField("DATE", org.jooq.impl.SQLDataType.DATE.nullable(false), this, "");

    /**
     * The column <code>folding.users_fldc.USER_ID</code>.
     */
    public final TableField<UsersFldcRecord, Integer> USER_ID = createField("USER_ID", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>folding.users_fldc.ADDRESS</code>.
     */
    public final TableField<UsersFldcRecord, String> ADDRESS = createField("ADDRESS", org.jooq.impl.SQLDataType.VARCHAR(34), this, "");

    /**
     * The column <code>folding.users_fldc.CREDIT_NEW</code>.
     */
    public final TableField<UsersFldcRecord, Double> CREDIT_NEW = createField("CREDIT_NEW", org.jooq.impl.SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>folding.users_fldc.TOKEN</code>.
     */
    public final TableField<UsersFldcRecord, String> TOKEN = createField("TOKEN", org.jooq.impl.SQLDataType.VARCHAR(20), this, "");

    /**
     * Create a <code>folding.users_fldc</code> table reference
     */
    public UsersFldc() {
        this(DSL.name("users_fldc"), null);
    }

    /**
     * Create an aliased <code>folding.users_fldc</code> table reference
     */
    public UsersFldc(String alias) {
        this(DSL.name(alias), USERS_FLDC);
    }

    /**
     * Create an aliased <code>folding.users_fldc</code> table reference
     */
    public UsersFldc(Name alias) {
        this(alias, USERS_FLDC);
    }

    private UsersFldc(Name alias, Table<UsersFldcRecord> aliased) {
        this(alias, aliased, null);
    }

    private UsersFldc(Name alias, Table<UsersFldcRecord> aliased, Field<?>[] parameters) {
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
        return Arrays.<Index>asList(Indexes.USERS_FLDC_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<UsersFldcRecord> getPrimaryKey() {
        return Keys.KEY_USERS_FLDC_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<UsersFldcRecord>> getKeys() {
        return Arrays.<UniqueKey<UsersFldcRecord>>asList(Keys.KEY_USERS_FLDC_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<UsersFldcRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<UsersFldcRecord, ?>>asList(Keys.USER_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsersFldc as(String alias) {
        return new UsersFldc(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsersFldc as(Name alias) {
        return new UsersFldc(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public UsersFldc rename(String name) {
        return new UsersFldc(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public UsersFldc rename(Name name) {
        return new UsersFldc(name, null);
    }
}
