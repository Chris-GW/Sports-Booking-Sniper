package de.chrisgw.sportbooking.gui.component;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.BasicTextImage;
import com.googlecode.lanterna.gui2.AbstractComponent;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.TextGUIGraphics;

import java.util.EnumSet;


public class SportBuchungsBotLogo extends AbstractComponent<SportBuchungsBotLogo> {

    private static final String[] LOGO_STR = new String[] { //
            "███████╗██████╗ ██████╗  ██████╗ ████████╗██████╗ ██╗   ██╗ ██████╗██╗  ██╗██╗   ██╗███╗   ██╗ ██████╗ ███████╗      ██████╗  ██████╗ ████████╗            ",
            "██╔════╝██╔══██╗██╔══██╗██╔═══██╗╚══██╔══╝██╔══██╗██║   ██║██╔════╝██║  ██║██║   ██║████╗  ██║██╔════╝ ██╔════╝      ██╔══██╗██╔═══██╗╚══██╔══╝            ",
            "███████╗██████╔╝██████╔╝██║   ██║   ██║   ██████╔╝██║   ██║██║     ███████║██║   ██║██╔██╗ ██║██║  ███╗███████╗█████╗██████╔╝██║   ██║   ██║               ",
            "╚════██║██╔═══╝ ██╔══██╗██║   ██║   ██║   ██╔══██╗██║   ██║██║     ██╔══██║██║   ██║██║╚██╗██║██║   ██║╚════██║╚════╝██╔══██╗██║   ██║   ██║               ",
            "███████║██║     ██║  ██║╚██████╔╝   ██║   ██████╔╝╚██████╔╝╚██████╗██║  ██║╚██████╔╝██║ ╚████║╚██████╔╝███████║      ██████╔╝╚██████╔╝   ██║               ",
            "╚══════╝╚═╝     ╚═╝  ╚═╝ ╚═════╝    ╚═╝   ╚═════╝  ╚═════╝  ╚═════╝╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝ ╚══════╝      ╚═════╝  ╚═════╝    ╚═╝               ",
            "                                                                                                                                                           ",
            "██████╗ ██╗    ██╗████████╗██╗  ██╗    ██╗  ██╗ ██████╗  ██████╗██╗  ██╗███████╗ ██████╗██╗  ██╗██╗   ██╗██╗     ███████╗██████╗  ██████╗ ██████╗ ████████╗",
            "██╔══██╗██║    ██║╚══██╔══╝██║  ██║    ██║  ██║██╔═══██╗██╔════╝██║  ██║██╔════╝██╔════╝██║  ██║██║   ██║██║     ██╔════╝██╔══██╗██╔═══██╗██╔══██╗╚══██╔══╝",
            "██████╔╝██║ █╗ ██║   ██║   ███████║    ███████║██║   ██║██║     ███████║███████╗██║     ███████║██║   ██║██║     ███████╗██████╔╝██║   ██║██████╔╝   ██║   ",
            "██╔══██╗██║███╗██║   ██║   ██╔══██║    ██╔══██║██║   ██║██║     ██╔══██║╚════██║██║     ██╔══██║██║   ██║██║     ╚════██║██╔═══╝ ██║   ██║██╔══██╗   ██║   ",
            "██║  ██║╚███╔███╔╝   ██║   ██║  ██║    ██║  ██║╚██████╔╝╚██████╗██║  ██║███████║╚██████╗██║  ██║╚██████╔╝███████╗███████║██║     ╚██████╔╝██║  ██║   ██║   ",
            "╚═╝  ╚═╝ ╚══╝╚══╝    ╚═╝   ╚═╝  ╚═╝    ╚═╝  ╚═╝ ╚═════╝  ╚═════╝╚═╝  ╚═╝╚══════╝ ╚═════╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚══════╝╚═╝      ╚═════╝ ╚═╝  ╚═╝   ╚═╝   " };

    private final BasicTextImage logoTextImage;


    public SportBuchungsBotLogo() {
        this(TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
    }

    public SportBuchungsBotLogo(TextColor foregroundColor, TextColor backgroundColor) {
        super();
        this.logoTextImage = createLogoTextImage(foregroundColor, backgroundColor);
    }

    private BasicTextImage createLogoTextImage(TextColor foregroundColor, TextColor backgroundColor) {
        BasicTextImage logoTextImage = new BasicTextImage(LOGO_STR[0].length(), LOGO_STR.length);
        for (int row = 0; row < LOGO_STR.length; row++) {
            for (int column = 0; column < LOGO_STR[0].length(); column++) {
                char c = LOGO_STR[row].charAt(column);
                TextCharacter textCharacter = new TextCharacter(c, foregroundColor, backgroundColor,
                        EnumSet.noneOf(SGR.class));
                logoTextImage.setCharacterAt(column, row, textCharacter);
            }
        }
        return logoTextImage;
    }


    @Override
    protected ComponentRenderer<SportBuchungsBotLogo> createDefaultRenderer() {
        return new ComponentRenderer<SportBuchungsBotLogo>() {

            @Override
            public TerminalSize getPreferredSize(SportBuchungsBotLogo component) {
                return component.logoTextImage.getSize();
            }

            @Override
            public void drawComponent(TextGUIGraphics graphics, SportBuchungsBotLogo component) {
                graphics.drawImage(TerminalPosition.TOP_LEFT_CORNER, component.logoTextImage);
            }

        };
    }


}
