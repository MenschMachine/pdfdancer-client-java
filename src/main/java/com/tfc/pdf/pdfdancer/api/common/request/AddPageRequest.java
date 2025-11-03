package com.tfc.pdf.pdfdancer.api.common.request;
import com.tfc.pdf.pdfdancer.api.common.model.Orientation;
import com.tfc.pdf.pdfdancer.api.common.model.PageSize;
public record AddPageRequest(Integer pageIndex, Orientation orientation, PageSize pageSize) {
}
