package fr.fuluty.plugin.bluemap.plotsquared;

import com.flowpowered.math.vector.Vector2d;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.MathMan;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class Utils {
    public static List<Vector2d> plotAllCorners(@NotNull final Plot plot) {
        final Area area = new Area();
        for ( final CuboidRegion region : plot.getRegions() ) {
            area.add( new Area( new Rectangle2D.Double(
                region.getMinimumPoint().getX() - 0.6, region.getMinimumPoint().getZ() - 0.6,
                region.getMaximumPoint().getX() - region.getMinimumPoint().getX() + 2.2,
                region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ() + 2.2
            ) ) );
        }

        final double[] positions = new double[6];
        final List<Vector2d> points = new LinkedList<>();
        for ( PathIterator pi = area.getPathIterator( null ); !pi.isDone(); pi.next() ) {
            if ( pi.currentSegment( positions ) != 4 ) {
                points.add( new Vector2d(
                    (int) MathMan.inverseRound( positions[0] ),
                    (int) MathMan.inverseRound( positions[1] )
                ) );
            }
        }
        return points;
    }

    public static Color stringToColor(@NotNull final String hex) {
        switch ( hex.length() ) {
            case 6:
                return new Color(
                    Integer.valueOf( hex.substring( 0, 2 ), 16 ),
                    Integer.valueOf( hex.substring( 2, 4 ), 16 ),
                    Integer.valueOf( hex.substring( 4, 6 ), 16 ) );
            case 8:
                return new Color(
                    Integer.valueOf( hex.substring( 0, 2 ), 16 ),
                    Integer.valueOf( hex.substring( 2, 4 ), 16 ),
                    Integer.valueOf( hex.substring( 4, 6 ), 16 ),
                    Integer.valueOf( hex.substring( 6, 8 ), 16 ) );
        }
        return null;
    }
}
