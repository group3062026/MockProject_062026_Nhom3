# Pagination Guide

## Giới thiệu

Phân trang (Pagination) là cơ chế chia dữ liệu thành từng trang nhỏ để tối ưu performance và trải nghiệm người dùng.

---

## 1. Cấu Trúc Pagination

### 1.1 Request Parameters

```
GET /api/v1/resources?page=0&pageSize=10&sortBy=id&sortDirection=DESC
```

| Parameter | Loại | Mặc định | Mô Tả |
|-----------|------|---------|-------|
| page | Integer | 0 | Số trang (bắt đầu từ 0) |
| pageSize | Integer | 10 | Số record trên 1 trang (1-100) |
| sortBy | String | id | Trường để sort |
| sortDirection | String | DESC | Hướng sort (ASC/DESC) |

### 1.2 Response Structure

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "items": [
      { "id": 1, "name": "Item 1" },
      { "id": 2, "name": "Item 2" }
    ],
    "pageNo": 0,
    "pageSize": 10,
    "totalElements": 250,
    "totalPages": 25,
    "isLast": false
  }
}
```

---

## 2. Các Lớp Hỗ Trợ

### 2.1 PaginationRequest

```java
@Data
@Builder
public class PaginationRequest {
    @Builder.Default
    private int page = 0;                    // Trang hiện tại
    
    @Builder.Default
    private int pageSize = 10;               // Items trên trang
    
    @Builder.Default
    private String sortBy = "id";            // Trường sort
    
    @Builder.Default
    private String sortDirection = "DESC";   // ASC hoặc DESC
    
    // Tự động validate input
    public void validate() { }
}
```

### 2.2 PageResponse

```java
@Data
@Builder
public class PageResponse<T> {
    private List<T> items;          // Danh sách items
    private int pageNo;             // Số trang hiện tại
    private int pageSize;           // Items trên trang
    private long totalElements;     // Tổng records
    private int totalPages;         // Tổng trang
    private boolean isLast;         // Có phải trang cuối?
}
```

### 2.3 PaginationUtils

```java
public class PaginationUtils {
    // Tạo PageRequest từ PaginationRequest
    public static PageRequest createPageRequest(PaginationRequest request) {
        // Validate & convert
    }
    
    // Validate tham số phân trang
    public static void validateIndex(int page, int pageSize) {
        // Check valid range
    }
}
```

---

## 3. Sử Dụng Pagination trong Controller

### 3.1 Controller Method

```java
@GetMapping
public ApiResponse<PageResponse<ItemDTO>> getItems(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDirection) {
    
    log.info("Getting items - page: {}, pageSize: {}", page, pageSize);
    
    // Tạo pagination request
    PaginationRequest paginationRequest = PaginationRequest.builder()
        .page(page)
        .pageSize(pageSize)
        .sortBy(sortBy)
        .sortDirection(sortDirection)
        .build();
    
    // Lấy dữ liệu từ service
    PageResponse<ItemDTO> result = itemService.getAllPaginated(paginationRequest);
    
    return ApiResponse.success(result);
}
```

### 3.2 Service Implementation

```java
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    
    private final ItemRepository itemRepository;
    
    @Override
    public PageResponse<ItemDTO> getAllPaginated(PaginationRequest paginationRequest) {
        // Validate input
        paginationRequest.validate();
        
        // Tạo PageRequest cho JPA
        PageRequest pageRequest = PaginationUtils.createPageRequest(paginationRequest);
        
        // Query database
        Page<Item> pageResult = itemRepository.findAll(pageRequest);
        
        // Map kết quả
        return PageResponse.<ItemDTO>builder()
            .items(pageResult.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList()))
            .pageNo(pageResult.getNumber())
            .pageSize(pageResult.getSize())
            .totalElements(pageResult.getTotalElements())
            .totalPages(pageResult.getTotalPages())
            .isLast(pageResult.isLast())
            .build();
    }
    
    private ItemDTO toDTO(Item item) {
        return ItemDTO.builder()
            .id(item.getId())
            .name(item.getName())
            .build();
    }
}
```

---

## 4. Client-Side Examples

### 4.1 Lấy trang đầu tiên

```bash
curl -X GET "http://localhost:8080/api/v1/items"
```

**Response:**
```json
{
  "data": {
    "items": [...],
    "pageNo": 0,
    "pageSize": 10,
    "totalElements": 250,
    "totalPages": 25,
    "isLast": false
  }
}
```

### 4.2 Lấy trang thứ 2 với 20 items

```bash
curl -X GET "http://localhost:8080/api/v1/items?page=1&pageSize=20"
```

### 4.3 Sort theo tên tăng dần

```bash
curl -X GET "http://localhost:8080/api/v1/items?sortBy=name&sortDirection=ASC"
```

### 4.4 JavaScript/Fetch Example

```javascript
async function getItems(page = 0, pageSize = 10) {
    const params = new URLSearchParams({
        page: page,
        pageSize: pageSize,
        sortBy: 'id',
        sortDirection: 'DESC'
    });
    
    const response = await fetch(`/api/v1/items?${params}`);
    const data = await response.json();
    
    console.log('Items:', data.data.items);
    console.log('Total Pages:', data.data.totalPages);
    console.log('Is Last:', data.data.isLast);
    
    return data.data;
}

// Sử dụng
const pageData = await getItems(0, 10);
```

### 4.5 Angular Example

```typescript
import { HttpClient, HttpParams } from '@angular/common/http';

export class ItemService {
    constructor(private http: HttpClient) {}
    
    getItems(page = 0, pageSize = 10, sortBy = 'id', sortDirection = 'DESC') {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('pageSize', pageSize.toString())
            .set('sortBy', sortBy)
            .set('sortDirection', sortDirection);
        
        return this.http.get<any>('/api/v1/items', { params });
    }
}
```

---

## 5. Validation Rules

### 5.1 Tự động Validation

```java
public void validate() {
    // Page >= 0
    if (this.page < 0) {
        this.page = 0;
    }
    
    // PageSize: 1-100
    if (this.pageSize <= 0 || this.pageSize > 100) {
        this.pageSize = 10;
    }
    
    // SortDirection: ASC hoặc DESC
    if (this.sortDirection == null || 
        (!this.sortDirection.equals("ASC") && 
         !this.sortDirection.equals("DESC"))) {
        this.sortDirection = "DESC";
    }
}
```

### 5.2 Ví dụ Validation

| Input | Output | Reason |
|-------|--------|--------|
| page=-1 | page=0 | Trang không thể âm |
| pageSize=200 | pageSize=10 | Vượt quá giới hạn |
| sortDirection=INVALID | DESC | Invalid value |

---

## 6. Performance Tips

### 6.1 Giới hạn Page Size

```java
// Không nên > 100 để tránh query lớn
if (this.pageSize > 100) {
    this.pageSize = 100;
}
```

### 6.2 Index Database

```sql
-- Thêm index trên trường sort
CREATE INDEX idx_items_id ON items(id DESC);
CREATE INDEX idx_items_created_at ON items(created_at DESC);
```

### 6.3 Lazy Loading

```java
@ManyToOne(fetch = FetchType.LAZY)
private User user;  // Không load ngay
```

---

## 7. Lỗi Thường Gặp

### 7.1 PageSize quá lớn
```
Problem: Client gửi pageSize=10000
Solution: Limit to 100 max
```

### 7.2 Page index out of range
```
Problem: page=100 nhưng totalPages=25
Solution: Client kiểm tra isLast flag
```

### 7.3 Sort field không tồn tại
```
Problem: sortBy='invalid_field'
Solution: Validate sortBy field hoặc default to 'id'
```

---

## 8. Advanced Pagination Patterns

### 8.1 Cursor-based Pagination (Cho real-time data)

```java
// Thay vì offset, sử dụng cursor
@GetMapping
public ApiResponse<PageResponse<ItemDTO>> getItems(
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "10") int limit) {
    
    // Query using cursor
    List<Item> items = itemRepository.findByIdGreaterThan(cursor, PageRequest.of(0, limit));
    
    return ApiResponse.success(items);
}
```

### 8.2 Filter + Pagination

```java
@GetMapping
public ApiResponse<PageResponse<ItemDTO>> search(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page) {
    
    PageRequest pageRequest = PageRequest.of(page, 10);
    Page<Item> result = itemRepository.findByNameContains(keyword, pageRequest);
    
    return ApiResponse.success(toPageResponse(result));
}
```

### 8.3 Aggregation + Pagination

```java
@GetMapping("/summary")
public ApiResponse<PageResponse<SummaryDTO>> getSummary(
        @RequestParam(defaultValue = "0") int page) {
    
    PageRequest pageRequest = PageRequest.of(page, 10);
    Page<SummaryDTO> result = itemRepository.findSummary(pageRequest);
    
    return ApiResponse.success(toPageResponse(result));
}
```

---

## 9. Testing Pagination

### 9.1 Unit Test

```java
@Test
public void testPagination() {
    PaginationRequest request = PaginationRequest.builder()
        .page(0)
        .pageSize(10)
        .build();
    
    PageResponse<ItemDTO> result = itemService.getAllPaginated(request);
    
    assertEquals(0, result.getPageNo());
    assertEquals(10, result.getPageSize());
    assertNotNull(result.getItems());
}
```

### 9.2 Integration Test

```java
@Test
public void testPaginationEndpoint() {
    ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
        "/api/v1/items?page=0&pageSize=10",
        ApiResponse.class
    );
    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(200, response.getBody().getCode());
}
```

---

## 10. Checklist Pagination

- [ ] Thêm `@RequestParam` cho page, pageSize, sortBy, sortDirection
- [ ] Tạo `PaginationRequest` từ parameters
- [ ] Validate input bằng `paginationRequest.validate()`
- [ ] Tạo `PageRequest` bằng `PaginationUtils.createPageRequest()`
- [ ] Query database với `PageRequest`
- [ ] Map kết quả vào `PageResponse<T>`
- [ ] Return `ApiResponse.success(pageResponse)`
- [ ] Test với các page khác nhau
- [ ] Test với invalid input

---

**Cập nhật gần nhất:** 17/07/2026

