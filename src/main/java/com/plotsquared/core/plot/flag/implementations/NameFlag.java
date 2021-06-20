package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.Caption;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.plot.flag.PlotFlag;
import org.jetbrains.annotations.NotNull;

public class NameFlag extends PlotFlag<String, NameFlag> {
    public static final NameFlag NAME_FLAG_EMPTY = new NameFlag( "" );

    public NameFlag(@NotNull final String value) {
        super( value, Captions.FLAG_CATEGORY_STRING, new Caption() {
            @Override
            public String getTranslated() {
                return "set the plot name";
            }

            @Override
            public boolean usePrefix() {
                return false;
            }
        } );
    }

    @Override public NameFlag parse(@NotNull final String input) {
        return this.flagOf( input );
    }

    @Override public NameFlag merge(@NotNull final String newValue) {
        return this.flagOf( this.getValue() + " " + newValue );
    }

    @Override public String toString() {
        return this.getValue();
    }

    @Override public String getExample() {
        return "&6This is my plot!";
    }

    @Override protected NameFlag flagOf(@NotNull final String value) {
        return new NameFlag( value );
    }
}
