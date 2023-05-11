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

/**
 * order[0][column] -> 4
 * order[0][dir] -> asc
 *
 */
public class DatatableParamsOrder {
    private int column = -1;

    /**
     * values are "asc" or "desc"
     */
    private String dir = "";

    public DatatableParamsOrder() {
    }

    public DatatableParamsOrder(int column, String dir) {
        super();
        this.column = column;
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public boolean isAscending() {
        return "asc".equals(dir);
    }

    public boolean isDescending() {
        return "desc".equals(dir);
    }

}
