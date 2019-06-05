package sample;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class SearchableListView {

    public static void set(TextField searchField, ListView<CbrValute> listView) {
        // 1. Wrap the ObservableList in a FilteredList (initially display all data).
        FilteredList<CbrValute> filteredValuteList = new FilteredList<>(listView.getItems(), p -> true);

        // 2. Set the filter Predicate whenever the filter changes.
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredValuteList.setPredicate(valute -> {
                // If filter text is empty, display all valutes.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare name of every valute with filter text.
                String lowerCaseFilter = newValue.toLowerCase();

                if ((valute.getCharCode() + valute.getName())
                        .toLowerCase()
                        .contains(lowerCaseFilter)) {
                    return true; // Filter matches.
                }
                return false; // Does not match.
            });
        });

        // 3. Wrap the FilteredList in a SortedList.
        SortedList<CbrValute> sortedList = new SortedList<>(filteredValuteList);

        // 4. Add sorted (and filtered) data to the table.
        listView.setItems(sortedList);
    }
}
