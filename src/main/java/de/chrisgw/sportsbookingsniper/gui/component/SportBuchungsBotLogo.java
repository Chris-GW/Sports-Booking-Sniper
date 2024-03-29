package de.chrisgw.sportsbookingsniper.gui.component;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.BasicTextImage;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.AbstractComponent;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.GUIBackdrop;
import com.googlecode.lanterna.gui2.TextGUIGraphics;


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
        this(ANSI.YELLOW, ANSI.DEFAULT);
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
                TextCharacter textCharacter = TextCharacter.fromCharacter(c, foregroundColor, backgroundColor)[0];
                logoTextImage.setCharacterAt(column, row, textCharacter);
            }
        }
        return logoTextImage;
    }


    public BasicTextImage getLogoTextImage() {
        return logoTextImage;
    }


    @Override
    protected ComponentRenderer<SportBuchungsBotLogo> createDefaultRenderer() {
        return new ComponentRenderer<>() {

            @Override
            public TerminalSize getPreferredSize(SportBuchungsBotLogo component) {
                return component.logoTextImage.getSize();
            }

            @Override
            public void drawComponent(TextGUIGraphics graphics, SportBuchungsBotLogo component) {
                ThemeDefinition guiBackdropDefinition = getTheme().getDefinition(GUIBackdrop.class);
                graphics.applyThemeStyle(guiBackdropDefinition.getNormal());
                graphics.drawImage(TerminalPosition.TOP_LEFT_CORNER, component.logoTextImage);
            }

        };
    }


}
