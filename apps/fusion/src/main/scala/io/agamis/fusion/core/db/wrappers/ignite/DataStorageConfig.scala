package io.agamis.fusion.core.db.wrappers.ignite

import org.apache.ignite.configuration.DataRegionConfiguration
import org.apache.ignite.configuration.DataStorageConfiguration

object DataStorageConfig {
    def getDefault: DataStorageConfiguration = {
        return new DataStorageConfiguration()
            .setDefaultDataRegionConfiguration(
              new DataRegionConfiguration()
                  .setName("Fusion")
                  .setInitialSize(100 * 1024 * 1024)
            )
    }
}
