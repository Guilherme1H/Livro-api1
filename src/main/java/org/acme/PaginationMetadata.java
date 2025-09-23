package org.acme;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Metadados da paginação para resultados de busca")
public class PaginationMetadata {
    public long totalElements;
    public long totalPages;
    public int currentPage;
    public int pageSize;

    public PaginationMetadata(long totalElements, long totalPages, int currentPage, int pageSize) {
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public PaginationMetadata() {
    }
}