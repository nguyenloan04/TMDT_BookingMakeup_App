package com.example.tmdt_bookingmakeup_app.dto.response.search;

import com.example.tmdt_bookingmakeup_app.dto.response.search.MakeupService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private List<MakeupService> services;
    private int totalCount;
    private int page;
    private int pageSize;
    private int totalPages;
}
