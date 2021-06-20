package fr.fuluty.plugin.bluemap.plotsquared;

import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.implementations.NameFlag;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.marker.MarkerAPI;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import java.io.IOException;
import org.bukkit.plugin.java.JavaPlugin;

public class BlueMap2PlotSquared extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Register custom flag
        GlobalFlagContainer.getInstance().addFlag( NameFlag.NAME_FLAG_EMPTY );

        BlueMapAPI.onEnable( blueMapAPI -> {
            try {
                final MarkerAPI markerAPI = blueMapAPI.getMarkerAPI();
                markerAPI.load();

                markerAPI.removeMarkerSet( "plot_region" );
                final MarkerSet marker = markerAPI.createMarkerSet( "plot_region" );
                marker.setDefaultHidden( this.getConfig().getBoolean( "hideMarkerSet", false ) );
                marker.setToggleable( true );
                marker.setLabel( this.getConfig().getString( "markerSetName", "Liste des plots" ) );

                new RegionUpdater( this, blueMapAPI, markerAPI, marker )
                    .runTaskTimerAsynchronously( this, 600L, this.getConfig().getLong( "updateInterval", 6000L ) );
            } catch ( final IOException exception ) {
                exception.printStackTrace();
            }
        } );
    }
}
