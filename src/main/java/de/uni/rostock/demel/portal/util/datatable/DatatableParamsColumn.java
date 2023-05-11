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
package de.uni.rostock.demel.portal.util.datatable;

import de.uni.rostock.demel.SuppressFBWarnings;

/**
 * columns[0][data] -> function
 * columns[0][name] -> id
 * columns[0][searchable] -> true
 * columns[0][orderable] -> true
 * columns[0][search][value] -> 
 * columns[0][search][regex] -> false
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public class DatatableParamsColumn {
    private String data;
    private String name;
    private boolean searchable = false;
    private boolean orderable = false;
    private DatatableParamsSearch search = new DatatableParamsSearch("", false);

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isOrderable() {
        return orderable;
    }

    public void setOrderable(boolean orderable) {
        this.orderable = orderable;
    }

    public DatatableParamsSearch getSearch() {
        return search;
    }

    public void setSearch(DatatableParamsSearch search) {
        this.search = search;
    }

}
