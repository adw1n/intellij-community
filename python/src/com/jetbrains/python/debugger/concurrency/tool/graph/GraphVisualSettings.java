/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.debugger.concurrency.tool.graph;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GraphVisualSettings {
  private int myScale = 1;
  private int myHorizontalValue = 0;
  private int myHorizontalExtent;
  private int myHorizontalMax;
  private int myVerticalValue = 0;
  private int myVerticalExtent;
  private int myVerticalMax;
  private int myNamesPanelWidth = NAMES_PANEL_INITIAL_WIDTH;
  public static int NAMES_PANEL_INITIAL_WIDTH = 200;
  public static int DIVIDER_WIDTH = 5;

  private List<SettingsListener> myListeners = new ArrayList<SettingsListener>();

  public int getMillisPerCell() {
    return myScale * 1;
  }


  public int getNamesPanelWidth() {
    return myNamesPanelWidth;
  }

  public void setNamesPanelWidth(int namesPanelWidth) {
    myNamesPanelWidth = namesPanelWidth;
  }

  public int getDividerWidth() {
    return DIVIDER_WIDTH;
  }

  public int getVerticalValue() {
    return myVerticalValue;
  }

  public int getVerticalExtent() {
    return myVerticalExtent;
  }

  public int getVerticalMax() {
    return myVerticalMax;
  }

  public int getHorizontalValue() {
    return myHorizontalValue;
  }

  public int getHorizontalExtent() {
    return myHorizontalExtent;
  }

  public int getHorizontalMax() {
    return myHorizontalMax;
  }

  public int getScale() {
    return myScale;
  }

  public int getHeightForPanes(int linesNumber) {
    return Math.max((GraphSettings.CELL_HEIGHT + GraphSettings.INTERVAL) * linesNumber +
                    2 * GraphSettings.INTERVAL,
                    getVerticalValue() + getVerticalExtent());
  }


  public void updateHorizontalScrollbar(int scrollbarValue, int scrollbarExtent, int scrollMax) {
    myHorizontalValue = scrollbarValue;
    myHorizontalExtent = scrollbarExtent;
    myHorizontalMax = scrollMax;
    notifyListeners();
  }

  public void updateVerticalScrollbar(int scrollbarValue, int scrollbarExtent, int scrollMax) {
    myVerticalValue = scrollbarValue;
    myVerticalExtent = scrollbarExtent;
    myVerticalMax = scrollMax;
    notifyListeners();
  }

  public interface SettingsListener {
    void settingsChanged();
  }

  public void registerListener(@NotNull SettingsListener logListener) {
    myListeners.add(logListener);
  }

  public void notifyListeners() {
    for (SettingsListener logListener : myListeners) {
      logListener.settingsChanged();
    }
  }
}
