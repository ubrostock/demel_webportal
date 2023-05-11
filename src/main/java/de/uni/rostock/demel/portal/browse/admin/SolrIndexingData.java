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
package de.uni.rostock.demel.portal.browse.admin;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SolrIndexingData {
    private int totalCount = 0;
    private long currentCount = 0;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean running = false;

    public int getTotalCount() {
        return totalCount;
    }

    public long getCurrentCount() {
        return currentCount;
    }

    public void increment() {
        currentCount++;
        if (currentCount == totalCount) {
            running = false;
            end = LocalDateTime.now();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public String getStartOutput() {
        if (start == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String result = start.format(formatter);
        return result;
    }

    public String getEndOutput() {
        if (end == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String result = end.format(formatter);
        return result;
    }

    public String getDurationOutput() {
        if (start == null) {
            return "";
        }
        long s = Duration.between(start, LocalDateTime.now()).getSeconds();
        if (!running) {
            s = Duration.between(start, end).getSeconds();
        }
        return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    }

    public void stop() {
        end = LocalDateTime.now();
        running = false;
    }

    public void start(int totalCount) {
        this.totalCount = totalCount;
        currentCount = 0;
        start = LocalDateTime.now();
        running = true;
    }
}
