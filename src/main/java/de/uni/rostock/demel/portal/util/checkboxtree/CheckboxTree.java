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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CheckboxTree extends CheckboxTreeItem {

    public CheckboxTree(String id, String json) {
        super(id, null);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<CheckboxTreeItem> liste = objectMapper.readValue(json, new TypeReference<List<CheckboxTreeItem>>() {
            });
            this.setItems(liste);
            initParents(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> collectCheckedItems() {
        HashSet<String> result = new HashSet<>();
        collectCheckedItems(result);
        return result;
    }

    public void initSelect(List<String> selectedItems, String lastCheckedItemId) {
        calcSelectedItems(this, selectedItems);
        calcLastUncheckedItem(this, lastCheckedItemId);
        checkChildren(this);
        checkParents(this);
    }

    private static void initParents(CheckboxTreeItem item) {
        if (item.getItems() != null) {
            for (CheckboxTreeItem child : item.getItems()) {
                child.setParent(item);
                initParents(child);
            }
        }
    }

    private void calcSelectedItems(CheckboxTreeItem item, List<String> selectedItems) {
        if (selectedItems.contains(item.getId())) {
            item.setChecked(true);
        }
        if (item.getItems() != null) {
            for (CheckboxTreeItem child : item.getItems()) {
                calcSelectedItems(child, selectedItems);
            }
        }
    }

    private static void checkChildren(CheckboxTreeItem item) {
        if (item.getItems() != null) {
            for (CheckboxTreeItem child : item.getItems()) {
                if (Boolean.TRUE.equals(item.getChecked())) {
                    child.setChecked(true);
                }
                checkChildren(child);
            }
        }
    }

    private static void checkParents(CheckboxTreeItem item) {
        if (item.getItems() != null) {
            int count = 0;
            for (CheckboxTreeItem child : item.getItems()) {
                checkParents(child);
                if (Boolean.TRUE.equals(child.getChecked())) {
                    count = count + 10;
                }
                if (null == child.getChecked()) {
                    count = count + 5;
                }
            }
            if (count == item.getItems().size() * 10) {
                item.setChecked(true);
            } else if (count > 0) {
                item.setChecked(null);
            }
        }
    }

    private static void calcLastUncheckedItem(CheckboxTree tree, String itemId) {
        CheckboxTreeItem item = findItem(tree, itemId);
        if (item != null && Boolean.FALSE.equals(item.getChecked())) {
            uncheckChildren(item);
            uncheckParents(item);
        }

    }

    private static void uncheckParents(CheckboxTreeItem item) {
        if (item.getParent() != null) {
            item.getParent().setChecked(false);
            uncheckParents(item.getParent());
        }
    }

    private static void uncheckChildren(CheckboxTreeItem item) {
        if (item.getItems() != null) {
            for (CheckboxTreeItem child : item.getItems()) {
                child.setChecked(false);
                uncheckChildren(child);
            }
        }
    }

    private static CheckboxTreeItem findItem(CheckboxTreeItem item, String itemId) {
        if (item.getId().equals(itemId)) {
            return item;
        }
        if (item.getItems() != null) {
            for (CheckboxTreeItem child : item.getItems()) {
                CheckboxTreeItem result = findItem(child, itemId);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

}
