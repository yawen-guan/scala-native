// #include <stdio.h>
// #include <stdlib.h>
// #include <memory.h>
// #include "MemoryPool.h"
// #include "MemoryPoolZone.h"

// void test_pool_claim_n_pages_then_reclaim(MemoryPool *pool, size_t n) {
//     MemoryPage *head_page = memorypool_claim(pool);
//     MemoryPage *tail_page = head_page;
//     for (int i = 0; i < n - 1; i++) {
//         MemoryPage *new_page = memorypool_claim(pool);
//         new_page->next = head_page;
//         head_page = new_page;
//     }
//     debug_print_pages(head_page, tail_page);
//     memorypool_reclaim(pool, head_page, tail_page);
//     debug_print_pool(pool);
// }

// void test_memorypool() {
//     MemoryPool *pool = memorypool_open();
//     test_pool_claim_n_pages_then_reclaim(pool, 10);
//     test_pool_claim_n_pages_then_reclaim(pool, 15);
//     test_pool_claim_n_pages_then_reclaim(pool, 30);
//     memorypool_free(pool);
// }

// void test_zone_alloc_n_byte(MemoryPoolZone *zone, size_t n) {
//     size_t size = sizeof(char) * n;
//     char *ptr = NULL; // (char *)malloc(size);
//     memorypoolzone_alloc(zone, ptr, size);
//     printf("zone alloc: size = %zx\n", size);
//     // debug_print_pages(zone->head_page, zone->head_page);
// }

// void test_memorypoolzone_given_pool(void *pool, int *nums, size_t nums_size)
// {
//     MemoryPoolZone *zone = memorypoolzone_open(pool);
//     printf("zone is open? %d\n", memorypoolzone_isopen(zone));
//     for (int i = 0; i < nums_size; i++) {
//         test_zone_alloc_n_byte(zone, nums[i]);
//     }
//     debug_print_pages(zone->head_page, zone->tail_page);
//     // memorypoolzone_free(zone); // report "Zone is still open."
//     memorypoolzone_close(zone);
//     printf("zone is open? %d\n", memorypoolzone_isopen(zone));
//     // test_zone_alloc_n_byte(zone, 0x10); // report "Zone is already
//     closed." memorypoolzone_free(zone);
// }

// void test_memorypoolzone() {
//     MemoryPool *pool = memorypool_open();

//     int nums[] = {0x10, 0x1000, 0x20, 0x700, 0x900};
//     int numsMore[] = {0x10, 0x1000, 0x20, 0x700, 0x900, 0x500, 0x300};

//     test_memorypoolzone_given_pool(pool, nums, 5);
//     test_memorypoolzone_given_pool(pool, nums, 5);
//     test_memorypoolzone_given_pool(pool, numsMore, 7);
//     memorypool_free(pool);
// }

// int main() {
//     // test_memorypool();
//     test_memorypoolzone();
//     return 0;
// }