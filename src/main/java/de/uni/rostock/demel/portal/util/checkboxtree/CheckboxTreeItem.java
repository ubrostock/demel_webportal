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
import java.util.Set;

import de.uni.rostock.demel.SuppressFBWarnings;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class CheckboxTreeItem {
    private String id;
    private Boolean checked = Boolean.FALSE;
    private CheckboxTreeItem parent;
    private List<CheckboxTreeItem> items = null;

    public CheckboxTreeItem() {

    }

    public CheckboxTreeItem(String id, CheckboxTreeItem parent) {
        this.parent = parent;
        this.id = id;
    }

    public List<CheckboxTreeItem> getItems() {
        return items;
    }

    public CheckboxTreeItem getParent() {
        return parent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setParent(CheckboxTreeItem parent) {
        this.parent = parent;
    }

    public void setItems(List<CheckboxTreeItem> items) {
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
            for (CheckboxTreeItem child : getItems()) {
                child.collectCheckedItems(set);
            }
        }
    }
}
