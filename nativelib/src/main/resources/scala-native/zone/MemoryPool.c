#include <stdlib.h>
#include <memory.h>
#include "MemoryPool.h"
#include "../gc/shared/GCScalaNative.h"
#include "../gc/shared/MemoryMap.h"

void memorypool_alloc_chunk(MemoryPool *pool);
void memorypool_alloc_page(MemoryPool *pool);

MemoryPool *memorypool_open() {
    MemoryPool *pool = malloc(sizeof(MemoryPool));
    pool->chunkPageCount = MEMORYPOOL_MIN_PAGE_COUNT;
    pool->chunk = NULL;
    pool->page = NULL;
    memorypool_alloc_chunk(pool);
    return pool;
}

void memorypool_alloc_chunk(MemoryPool *pool) {
    if (pool->chunkPageCount < MEMORYPOOL_MAX_PAGE_COUNT) {
        pool->chunkPageCount *= 2;
    }
    MemoryChunk *chunk = malloc(sizeof(MemoryChunk));
    chunk->size = MEMORYPOOL_PAGE_SIZE * pool->chunkPageCount;
    chunk->offset = 0;
    chunk->start = memoryMap(chunk->size);
    chunk->next = pool->chunk;
    pool->chunk = chunk;
}

void memorypool_alloc_page(MemoryPool *pool) {
    if (pool->chunk->offset >= pool->chunk->size) {
        memorypool_alloc_chunk(pool);
    }
    MemoryPage *page = malloc(sizeof(MemoryPage));
    page->start = pool->chunk->start + pool->chunk->offset;
    page->offset = 0;
    page->next = pool->page;
    pool->chunk->offset += MEMORYPOOL_PAGE_SIZE;
    pool->page = page;
}

/** Borrow a single unused page, to be reclaimed later. */
MemoryPage *memorypool_claim(void *_pool) {
    MemoryPool *pool = (MemoryPool *)_pool;
    if (pool->page == NULL) {
        memorypool_alloc_page(pool);
    }
    MemoryPage *result = pool->page;
    pool->page = result->next;
    result->next = NULL;
    result->offset = 0;
    // Notify the GC that the page is in use.
    scalanative_add_roots(result->start, result->start + MEMORYPOOL_PAGE_SIZE);
    return result;
}

/** Reclaimed a list of previously borrowed pages. */
void memorypool_reclaim(void *_pool, void *_head_page, void *_tail_page) {
    MemoryPool *pool = (MemoryPool *)_pool;
    MemoryPage *head = (MemoryPage *)_head_page;
    MemoryPage *tail = (MemoryPage *)_tail_page;
    // Notify the GC that the pages are no longer in use.
    MemoryPage *page = head;
    while (page != NULL) {
        scalanative_remove_roots(page->start,
                                 page->start + MEMORYPOOL_PAGE_SIZE);
        if (page == tail)
            break;
        page = page->next;
    }
    // Append the reclaimed pages to the pool.
    tail->next = pool->page;
    pool->page = head;
}

void memorypool_free(void *_pool) {
    MemoryPool *pool = (MemoryPool *)_pool;
    // Free chunks.
    MemoryChunk *chunk = pool->chunk, *pre_chunk = NULL;
    while (chunk != NULL) {
        pre_chunk = chunk;
        chunk = chunk->next;
        memoryUnmap(pre_chunk->start, pre_chunk->size);
        free(pre_chunk);
    }
    // Free pages.
    MemoryPage *page = pool->page, *pre_page = NULL;
    while (page != NULL) {
        pre_page = page;
        page = page->next;
        free(pre_page);
    }
    // Free the pool.
    free(pool);
}

// #include <stdio.h>

// void debug_print_chunk(MemoryChunk *chunk, size_t idx) {
//     printf("%02zu chunk (start: %p, size: %zx, offset: %zx)\n", idx,
//            chunk->start, chunk->size, chunk->offset);
// }

// void debug_print_page(MemoryPage *page, size_t idx) {
//     printf("%02zu page (start: %p, size: %x, offset: %zx)\n", idx,
//     page->start,
//            MEMORYPOOL_PAGE_SIZE, page->offset);
// }

// size_t debug_get_pages_length(MemoryPage *head_page, MemoryPage *tail_page) {
//     MemoryPage *page = head_page;
//     size_t length = 1;
//     while (page != NULL) {
//         if (page == tail_page)
//             break;
//         page = page->next;
//         length += 1;
//     }
//     return length;
// }

// void debug_print_pages(MemoryPage *head_page, MemoryPage *tail_page) {
//     printf("== pages start ==\n");
//     MemoryPage *page = head_page;
//     int idx = debug_get_pages_length(head_page, tail_page) - 1;
//     while (page != NULL) {
//         debug_print_page(page, idx);
//         if (page == tail_page)
//             break;
//         page = page->next;
//         idx -= 1;
//     }
//     printf("== pages end  ==\n");
// }

// size_t debug_get_chunks_length(MemoryChunk *head_chunk) {
//     MemoryChunk *chunk = head_chunk;
//     size_t length = 1;
//     while (chunk != NULL) {
//         chunk = chunk->next;
//         if (chunk != NULL) {
//             length += 1;
//         }
//     }
//     return length;
// }

// void debug_print_chunks(MemoryChunk *head_chunk) {
//     printf("== chunks start ==\n");
//     MemoryChunk *chunk = head_chunk;
//     size_t idx = debug_get_chunks_length(head_chunk) - 1;
//     while (chunk != NULL) {
//         debug_print_chunk(chunk, idx);
//         chunk = chunk->next;
//         idx -= 1;
//     }
//     printf("== chunks end  ==\n");
// }

// void debug_print_pool(void *_pool) {
//     MemoryPool *pool = (MemoryPool *)_pool;
//     printf("== pool start ==\n");
//     debug_print_chunks(pool->chunk);
//     printf("== pool end  ==\n");
// }
