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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uni.rostock.demel.SuppressFBWarnings;

/**
 * JavaBean representing parameters send by the Datable javascript widget
 * for server side processing 
 * @see https://datatables.net/manual/server-side#Sent-parameters
 * @author Robert Stephan
 *
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public class DatatableParams {
    private DatatableParamsSearch dtSearch = new DatatableParamsSearch();
    private List<DatatableParamsColumn> dtColumns = new ArrayList<>();
    private List<DatatableParamsOrder> dtOrders = new ArrayList<>();

    /**
     * Draw counter. This is used by DataTables to ensure that the Ajax returns from server-side processing requests 
     * are drawn in sequence by DataTables (Ajax requests are asynchronous and thus can return out of sequence).
     * This is used as part of the draw return parameter (see below).
     */
    private int dtDraw = -1;

    /**
     * Paging first record indicator. This is the start point in the current data set 
     * (0 index based - i.e. 0 is the first record).
     */
    private int dtStart = 0;

    /**
     * Number of records that the table can display in the current draw. It is expected that the number of records returned
     *  will be equal to this number, unless the server has fewer records to return. 
     *  Note that this can be -1 to indicate that all records should be returned 
     *  (although that negates any benefits of server-side processing!)
     */
    private int dtLength = 1;

    public DatatableParams(Map<String, String[]> requestParams) {
        for (Entry<String, String[]> entry : requestParams.entrySet()) {
            //columns[0][data] -> function
            //columns[0][name] -> id
            //columns[0][searchable] -> true
            //columns[0][orderable] -> true
            //columns[0][search][value] -> 
            //columns[0][search][regex] -> false

            if (entry.getKey().startsWith("columns")) {
                int idx = Integer.parseInt(entry.getKey().substring("columns[".length(), entry.getKey().indexOf("]")));
                while (dtColumns.size() <= idx) {
                    dtColumns.add(new DatatableParamsColumn());
                }
                if (entry.getKey().endsWith("[data]")) {
                    dtColumns.get(idx).setData(entry.getValue()[0]);
                }
                if (entry.getKey().endsWith("[name]")) {
                    dtColumns.get(idx).setName(entry.getValue()[0]);
                }
                if (entry.getKey().endsWith("[searchable]")) {
                    dtColumns.get(idx).setSearchable(Boolean.parseBoolean(entry.getValue()[0]));
                }
                if (entry.getKey().endsWith("[orderable]")) {
                    dtColumns.get(idx).setOrderable(Boolean.parseBoolean(entry.getValue()[0]));
                }
                if (entry.getKey().endsWith("[search][value]")) {
                    dtColumns.get(idx).getSearch().setValue(entry.getValue()[0]);
                }
                if (entry.getKey().endsWith("[search][regex]")) {
                    dtColumns.get(idx).getSearch().setRegex(Boolean.parseBoolean(entry.getValue()[0]));
                }
            }

            //order[0][column] -> 4
            //order[0][dir] -> asc
            if (entry.getKey().startsWith("order[") && entry.getKey().contains("]")) {
                int idx = Integer.parseInt(entry.getKey().substring("order[".length(), entry.getKey().indexOf("]")));
                while (dtOrders.size() <= idx) {
                    dtOrders.add(new DatatableParamsOrder());
                }
                if (entry.getKey().endsWith("[column]")) {
                    dtOrders.get(idx).setColumn(-1);
                    try {
                        dtOrders.get(idx).setColumn(Integer.parseInt(entry.getValue()[0]));
                    } catch (NumberFormatException e) {
                        //ignore, use default
                    }
                }

                if (entry.getKey().endsWith("[dir]")) {
                    dtOrders.get(idx).setDir(entry.getValue()[0]);
                }
            }
            //draw -> 2
            if (entry.getKey().equals("draw")) {
                setDtDraw(-1);
                try {
                    setDtDraw(Integer.parseInt(entry.getValue()[0]));
                } catch (NumberFormatException e) {
                    //ignore, use default
                }
            }
            //start -> 0
            if (entry.getKey().equals("start")) {
                setDtStart(-1);
                try {
                    setDtStart(Integer.parseInt(entry.getValue()[0]));
                } catch (NumberFormatException e) {
                    //ignore, use default
                }
            }
            //length -> 50
            if (entry.getKey().equals("length")) {
                setDtLength(-1);
                try {
                    setDtLength(Integer.parseInt(entry.getValue()[0]));
                } catch (NumberFormatException e) {
                    //ignore, use default
                }
            }
            //search[value] -> 
            if (entry.getKey().equals("search[value]")) {
                getDtSearch().setValue(entry.getValue()[0]);
            }

            //search[regex] -> false
            if (entry.getKey().equals("search[regex]")) {
                getDtSearch().setRegex(Boolean.parseBoolean(entry.getValue()[0]));
            }

            // _ -> 1564144342672
            // token from jquery to circumvent browser/proxy caching 
        }
    }

    public DatatableParamsSearch getDtSearch() {
        return dtSearch;
    }

    public int getDtDraw() {
        return dtDraw;
    }

    public void setDtDraw(int draw) {
        this.dtDraw = draw;
    }

    public int getDtStart() {
        return dtStart;
    }

    public void setDtStart(int start) {
        this.dtStart = start;
    }

    public int getDtLength() {
        return dtLength;
    }

    public void setDtLength(int length) {
        this.dtLength = length;
    }

    public List<DatatableParamsColumn> getDtColumns() {
        return dtColumns;
    }

    public List<DatatableParamsOrder> getDtOrders() {
        return dtOrders;
    }
}
