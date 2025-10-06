package com.magsav.util;

import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.util.function.Function;
import java.util.function.LongConsumer;

public final class TableFx {
  private TableFx() {}

  public static <T> void openOnDoubleClick(TableView<T> table,
                                           Function<T, Long> idExtractor,
                                           LongConsumer openFn) {
    if (table == null || idExtractor == null || openFn == null) return;
    table.setRowFactory(tv -> {
      TableRow<T> row = new TableRow<>();
      row.setOnMouseClicked(e -> {
        if (e.getClickCount() == 2 && !row.isEmpty()) {
          Long id = idExtractor.apply(row.getItem());
          if (id != null) openFn.accept(id);
        }
      });
      return row;
    });
  }
}