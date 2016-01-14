package miles.scribble.data.model;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import miles.scribble.util.Logg;

/**
 * Created by mbpeele on 1/14/16.
 */
public class Migration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        Logg.log(oldVersion, newVersion);

        if (oldVersion == 0) {
            RealmObjectSchema objectSchema = schema.create("Profile");
            oldVersion++;
        }
    }
}
