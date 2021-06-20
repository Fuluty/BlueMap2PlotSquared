package fr.fuluty.plugin.bluemap.plotsquared;

import com.flowpowered.math.vector.Vector2d;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.implementations.DescriptionFlag;
import com.plotsquared.core.plot.flag.implementations.NameFlag;
import com.plotsquared.core.util.MainUtil;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.marker.MarkerAPI;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import de.bluecolored.bluemap.api.marker.Shape;
import de.bluecolored.bluemap.api.marker.ShapeMarker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class RegionUpdater extends BukkitRunnable {
    private final BlueMap2PlotSquared plugin;
    private final BlueMapAPI blueMapAPI;
    private final MarkerAPI markerAPI;
    private final MarkerSet marker;

    public RegionUpdater(@NotNull final BlueMap2PlotSquared plugin, @NotNull final BlueMapAPI blueMapAPI, @NotNull final MarkerAPI markerAPI, @NotNull final MarkerSet marker) {
        this.plugin = plugin;
        this.blueMapAPI = blueMapAPI;
        this.markerAPI = markerAPI;
        this.marker = marker;
    }

    @Override
    public void run() {
        this.marker.getMarkers().forEach( this.marker::removeMarker );

        for ( final BlueMapWorld blueWorld : this.blueMapAPI.getWorlds() ) {
            for ( final BlueMapMap map : blueWorld.getMaps() ) {
                final World world = Bukkit.getWorld( map.getWorld().getUuid() );
                if ( world == null || !PlotSquared.get().hasPlotArea( world.getName() ) ) continue;

                for ( final Plot plot : new ArrayList<>( PlotSquared.get().getPlots( world.getName() ) ) ) {
                    if ( !plot.isBasePlot() ) continue;
                    this.createMarker( world, map, plot );
                }
            }
        }
    }

    private void createMarker(@NotNull final World world, @NotNull final BlueMapMap map, @NotNull final Plot plot) {
        try {
            final ShapeMarker shape = this.marker.createShapeMarker(
                world.getName() + "_" + plot.getId().toCommaSeparatedString(), map,
                new Shape( Utils.plotAllCorners( plot ).toArray( new Vector2d[0] ) ),
                this.plugin.getConfig().getInt( "renderHeight", 63 )
            );

            this.optionForShape( shape, world, plot );
            shape.setLabel(
                this.plugin.getConfig().getString( "markerLabel", "%name% (%id%)" )
                    .replaceAll( "%name%", !plot.getFlag( NameFlag.class ).isEmpty()
                        ? plot.getFlag( NameFlag.class ).trim() + " - " + MainUtil.getName( plot.getOwner() )
                        : this.plugin.getConfig().getString( "htmlNoPlotName", "Plot de %owner%" )
                            .replaceAll( "%owner%", MainUtil.getName( plot.getOwner() ) )
                    )
                    .replaceAll( "%id%", plot.getId().toCommaSeparatedString() )
            );
            shape.setMinDistance( this.plugin.getConfig().getInt( "minDistance", 100 ) );
            shape.setMaxDistance( this.plugin.getConfig().getInt( "maxDistance", 1000 ) );
            shape.setDetail( this.htmlForPlot( plot ) );

            this.markerAPI.save();
        } catch ( final IOException exception ) {
            exception.printStackTrace();
        }
    }

    private void optionForShape(@NotNull final ShapeMarker shape, @NotNull final World world, @NotNull final Plot plot) {
        if ( this.plugin.getConfig().contains( world.getName() ) ) {
            if ( this.plugin.getConfig().contains( world.getName() + "." + plot.getId().toCommaSeparatedString() ) ) {
                shape.setFillColor( Utils.stringToColor( this.plugin.getConfig().getString( world.getName() + "." + plot.getId().toCommaSeparatedString() + ".fillColor", "0087ff96" ) ) );
                shape.setLineColor( Utils.stringToColor( this.plugin.getConfig().getString( world.getName() + "." + plot.getId().toCommaSeparatedString() + ".lineColor", "0060ff" ) ) );
                shape.setLineWidth( this.plugin.getConfig().getInt( world.getName() + "." + plot.getId().toCommaSeparatedString() + ".lineWidth", 2 ) );
                return;
            }

            shape.setFillColor( Utils.stringToColor( this.plugin.getConfig().getString( world.getName() + ".fillColor", "0087ff96" ) ) );
            shape.setLineColor( Utils.stringToColor( this.plugin.getConfig().getString( world.getName() + ".lineColor", "0060ff" ) ) );
            shape.setLineWidth( this.plugin.getConfig().getInt( world.getName() + ".lineWidth", 2 ) );
            return;
        }

        shape.setFillColor( Utils.stringToColor( this.plugin.getConfig().getString( "defaultFillColor", "0087ff96" ) ) );
        shape.setLineColor( Utils.stringToColor( this.plugin.getConfig().getString( "defaultLineColor", "0060ff" ) ) );
        shape.setLineWidth( this.plugin.getConfig().getInt( "defaultLineWidth", 2 ) );
    }

    private String htmlForPlot(@NotNull final Plot plot) {
        String html = this.plugin.getConfig().getString( "html", "%name%" );

        if ( html.contains( "%name%" ) ) {
            html = html.replaceAll(
                "%name%",
                !plot.getFlag( NameFlag.class ).isEmpty()
                    ? plot.getFlag( NameFlag.class ).trim()
                    : this.plugin.getConfig().getString( "htmlNoPlotName", "Plot de %owner%" )
                        .replaceAll( "%owner%", MainUtil.getName( plot.getOwner() ) )
            );
        }
        if ( html.contains( "%id%" ) ) html = html.replaceAll( "%id%", plot.getId().toCommaSeparatedString() );
        if ( html.contains( "%owner%" ) ) html = html.replaceAll( "%owner%", MainUtil.getName( plot.getOwner() ) );
        if ( html.contains( "%members%" ) ) {
            html = html.replaceAll(
                "%members%",
                ( plot.getMembers().size() > 0 )
                    ? plot.getMembers().stream().map( uuid -> "<div>" + MainUtil.getName( uuid ) + "</div>" ).collect( Collectors.joining() )
                    : this.plugin.getConfig().getString( "htmlNobody", "aucun" )
            );
        }
        if ( html.contains( "%trusted%" ) ) {
            html = html.replaceAll(
                "%trusted%",
                ( plot.getTrusted().size() > 0 )
                    ? plot.getTrusted().stream().map( uuid -> "<div>" + MainUtil.getName( uuid ) + "</div>" ).collect( Collectors.joining() )
                    : this.plugin.getConfig().getString( "htmlNobody", "aucun" )
            );
        }
        if ( html.contains( "%all_members%" ) ) {
            final List<UUID> allMembers = new ArrayList<>();
            allMembers.addAll( plot.getMembers() );
            allMembers.addAll( plot.getTrusted() );

            html = html.replaceAll(
                "%all_members%",
                ( allMembers.size() > 0 )
                    ? allMembers.stream().map( uuid -> "<div>" + MainUtil.getName( uuid ) + "</div>" ).collect( Collectors.joining() )
                    : this.plugin.getConfig().getString( "htmlNobody", "aucun" )
            );
        }
        if ( html.contains( "%description%" ) ) {
            html = html.replaceAll(
                "%description%",
                !plot.getFlag( DescriptionFlag.class ).isEmpty()
                    ? plot.getFlag( DescriptionFlag.class ).trim()
                    : this.plugin.getConfig().getString( "htmlNoDescription", "Pas de description pour ce plot" )
            );
        }
        if ( html.contains( "%like%" ) ) {
            html = html.replaceAll(
                "%like%",
                Integer.toString( (int) plot.getLikes().values().stream().filter( bool -> bool ).count() )
            );
        }
        if ( html.contains( "%dislike%" ) ) {
            html = html.replaceAll(
                "%dislike%",
                Integer.toString( (int) plot.getLikes().values().stream().filter( bool -> !bool ).count() )
            );
        }
        return html;
    }
}
