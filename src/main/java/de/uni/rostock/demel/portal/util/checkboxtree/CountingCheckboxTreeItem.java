/*
 * This file is part of the DEMel web application.
 * 
 * Copyright 2023
 * DEMel project team, Rostock University, 18051 Rostock, Germany
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
package de.uni.rostock.demel.portal.util.checkboxtree;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni.rostock.demel.SuppressFBWarnings;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class CountingCheckboxTreeItem {
    private String id;
    private Boolean checked = Boolean.FALSE;
    private CountingCheckboxTreeItem parent;
    private List<CountingCheckboxTreeItem> items = null;
    private long countActive = 0;
    private long countAll = 0;

    public CountingCheckboxTreeItem() {

    }

    public CountingCheckboxTreeItem(String id, CountingCheckboxTreeItem parent) {
        this.parent = parent;
        this.id = id;
    }

    public List<CountingCheckboxTreeItem> getItems() {
        return items;
    }

    public CountingCheckboxTreeItem getParent() {
        return parent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setParent(CountingCheckboxTreeItem parent) {
        this.parent = parent;
    }

    public void setItems(List<CountingCheckboxTreeItem> items) {
        this.items = items;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    protected void collectCheckedItems(Set<String> set) {
        if (Boolean.TRUE.equals(checked)) {
            set.add(id);
        }
        if (getItems() != null) {
            for (CountingCheckboxTreeItem child : getItems()) {
                child.collectCheckedItems(set);
            }
        }
    }

    public void initCounts(Map<String, Long> mapCountActive, Map<String, Long> mapCountAll) {
        if (mapCountActive.containsKey(id)) {
            setCountActive(mapCountActive.get(id));
        }
        if (mapCountAll.containsKey(id)) {
            setCountAll(mapCountAll.get(id));
        }
        if (getItems() != null) {
            for (CountingCheckboxTreeItem child : getItems()) {
                child.initCounts(mapCountActive, mapCountAll);
            }
        }
    }

    public long getCountActive() {
        return countActive;
    }

    public void setCountActive(long countChecked) {
        this.countActive = countChecked;
    }

    public long getCountAll() {
        return countAll;
    }

    public void setCountAll(long countAll) {
        this.countAll = countAll;
    }
}
