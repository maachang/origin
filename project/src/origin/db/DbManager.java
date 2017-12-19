package origin.db;

import origin.db.core.DbFactory;
import origin.util.sequence.Time16SequenceId;

/**
 * DB管理.
 */
public class DbManager {
    private DbFactory dbFactory;
    private Time16SequenceId sequence;

    private static final DbManager SNGL = new DbManager();

    public static final DbManager getInstance() {
        return SNGL;
    }

    public DbFactory getDbFactory() {
        return dbFactory;
    }

    public void setDbFactory(DbFactory dbFactory) {
        this.dbFactory = dbFactory;
    }

    public Time16SequenceId getSequence() {
        return sequence;
    }

    public void setSequence(Time16SequenceId sequence) {
        this.sequence = sequence;
    }
}
