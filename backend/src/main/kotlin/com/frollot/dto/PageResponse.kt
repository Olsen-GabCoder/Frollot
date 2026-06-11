package com.frollot.dto

import org.springframework.data.domain.Page

/**
 * DTO générique pour les réponses paginées.
 * 
 * Compatible avec les clients existants tout en ajoutant les métadonnées de pagination.
 */
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isFirst: Boolean,
    val isLast: Boolean,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    companion object {
        /**
         * Crée un PageResponse à partir d'une Page Spring Data.
         */
        fun <T> fromPage(page: Page<T>): PageResponse<T> {
            return PageResponse(
                content = page.content,
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                isFirst = page.isFirst,
                isLast = page.isLast,
                hasNext = page.hasNext(),
                hasPrevious = page.hasPrevious()
            )
        }

        /**
         * Crée un PageResponse à partir d'une liste (pour rétro-compatibilité).
         */
        fun <T> fromList(list: List<T>, page: Int = 0, size: Int = 20): PageResponse<T> {
            val totalElements = list.size.toLong()
            val totalPages = if (size > 0) ((totalElements + size - 1) / size).toInt() else 1
            return PageResponse(
                content = list,
                page = page,
                size = size,
                totalElements = totalElements,
                totalPages = totalPages,
                isFirst = page == 0,
                isLast = page >= totalPages - 1,
                hasNext = page < totalPages - 1,
                hasPrevious = page > 0
            )
        }
    }
}

