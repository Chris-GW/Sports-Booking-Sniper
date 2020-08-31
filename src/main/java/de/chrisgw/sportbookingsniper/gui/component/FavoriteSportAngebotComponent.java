package de.chrisgw.sportbookingsniper.gui.component;

import com.googlecode.lanterna.gui2.CheckBoxList;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportbookingsniper.gui.state.ApplicationStateDao;


public class FavoriteSportAngebotComponent extends BasicPanelComponent {


    public FavoriteSportAngebotComponent(ApplicationStateDao applicationStateDao, Window window) {
        super(applicationStateDao, window, "SportAngebot Favoriten", KeyType.F4);

        addComponent(new Label("Meine Favoriten"));
        addComponent(new CheckBoxList<>().addItem("1 Favorite")
                .addItem("2 Favorite")
                .addItem("3 Favorite")
                .addItem("4 Favorite"));
    }

}
