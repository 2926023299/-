package com.tanhua.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YearReturnDto {
    private List<YearDateDto> thisYear;
    private List<YearDateDto> lastYear;
}
