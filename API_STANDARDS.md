# API Standards Documentation

## Tổng Quan

Đây là tài liệu hướng dẫn chuẩn hóa các API của hệ thống Mock Project Group 3. Các tiêu chuẩn này bao gồm:
- Kiểm tra quyền dựa trên role (Role-Based Access Control)
- Phân trang dữ liệu (Pagination)
- Xử lý lỗi chuẩn hóa với Error Codes
- Cấu trúc response thống nhất

---

## 1. Cấu Trúc Response API

### 1.1 Response thành công
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    // Dữ liệu trả về
  }
}
```

### 1.2 Response lỗi
```json
{
  "code": 1005,
  "message": "You do not have permission to access this resource",
  "data": null
}
```

### 1.3 Response phân trang
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "items": [
      // Danh sách items
    ],
    "pageNo": 0,
    "pageSize": 10,
    "totalElements": 50,
    "totalPages": 5,
    "isLast": false
  }
}
```

---

## 2. Error Codes

Hệ thống sử dụng các mã lỗi được định nghĩa trong `ErrorCode.java`:

### 2.1 Mã lỗi chuẩn (1000 - 1999)
| Code | Message | HTTP Status |
|------|---------|-------------|
| 1001 | Invalid message key | 400 Bad Request |
| 1002 | Resource not found | 404 Not Found |
| 1003 | Resource already exists | 409 Conflict |
| 1004 | You need to log in | 401 Unauthorized |
| 1005 | No permission to access | 403 Forbidden |
| 1006 | Invalid request parameter | 400 Bad Request |
| 1007 | Validation error | 400 Bad Request |
| 1008 | Business rule violation | 400 Bad Request |

### 2.2 Mã lỗi User & Role (2000 - 2999)
| Code | Message | HTTP Status |
|------|---------|-------------|
| 2001 | User not found | 404 Not Found |
| 2002 | User already exists | 409 Conflict |
| 2003 | Role not found | 404 Not Found |
| 2004 | Permission not found | 404 Not Found |

### 2.3 Mã lỗi Clinical (5000 - 5999)
| Code | Message | HTTP Status |
|------|---------|-------------|
| 5001 | Clinical record not found | 404 Not Found |
| 5002 | Care plan not found | 404 Not Found |
| 5003 | Medication order not found | 404 Not Found |
| 5004 | Vital sign record not found | 404 Not Found |

---

## 3. Role-Based Access Control (RBAC)

### 3.1 Sử dụng @RequireRole Annotation

```java
@GetMapping("/tasks")
@RequireRole({"CNA", "NURSE", "MANAGER", "ADMIN"})
public ApiResponse<CareTaskResponseDTO> getCareTask() {
    // Logic xử lý
}
```

### 3.2 Cách hoạt động

- Annotation `@RequireRole` được xử lý bởi `RoleCheckAspect`
- Aspect sẽ kiểm tra header `X-User-Role` từ request
- Nếu role không nằm trong danh sách được cho phép, trả về lỗi `FORBIDDEN (1005)`
- Nếu không có role header, trả về lỗi `UNAUTHORIZED (1004)`

### 3.3 Gửi Request với Role

```bash
curl -X GET http://localhost:8080/api/v1/care-task/tasks \
  -H "X-User-Role: CNA"
```

**Lưu ý:** Trong thực tế, role nên được lấy từ JWT Token hoặc Spring Security Context, không phải từ header!

### 3.4 Danh sách các Roles

- **ADMIN**: Quản trị viên toàn hệ thống
- **MANAGER**: Quản lý (Giám sát)
- **NURSE**: Y tá/Điều dưỡng
- **CNA**: Certified Nursing Assistant (Hộ lý)

---

## 4. Phân Trang (Pagination)

### 4.1 Sử dụng PaginationRequest

```java
@GetMapping("/list")
public ApiResponse<PageResponse<ItemDTO>> getItems(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int pageSize,
    @RequestParam(defaultValue = "id") String sortBy,
    @RequestParam(defaultValue = "DESC") String sortDirection) {
    
    PaginationRequest paginationRequest = PaginationRequest.builder()
        .page(page)
        .pageSize(pageSize)
        .sortBy(sortBy)
        .sortDirection(sortDirection)
        .build();
    
    PageRequest pageRequest = PaginationUtils.createPageRequest(paginationRequest);
    
    // Sử dụng pageRequest trong repository query
}
```

### 4.2 Client Request

```bash
# Lấy trang đầu tiên, 10 items trên trang
GET /api/v1/items?page=0&pageSize=10

# Lấy trang thứ 2, 20 items, sort theo name tăng dần
GET /api/v1/items?page=1&pageSize=20&sortBy=name&sortDirection=ASC
```

### 4.3 Response Phân Trang

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "items": [...],
    "pageNo": 0,
    "pageSize": 10,
    "totalElements": 150,
    "totalPages": 15,
    "isLast": false
  }
}
```

---

## 5. Error Handling

### 5.1 Ném Lỗi Trong Service

```java
// Đúng: Sử dụng AppException với ErrorCode
throw new AppException(ErrorCode.CARE_PLAN_NOT_FOUND);

// Sai: Không sử dụng EntityNotFoundException
// throw new EntityNotFoundException("Not found");
```

### 5.2 Global Exception Handler

Mọi exception sẽ được xử lý bởi `GlobalExceptionHandler` và trả về format JSON chuẩn:

```java
@ExceptionHandler(value = AppException.class)
public ResponseEntity<ApiResponse<Object>> handleAppException(AppException exception) {
    ErrorCode errorCode = exception.getErrorCode();
    return ResponseEntity.status(errorCode.getStatusCode())
            .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
}
```

---

## 6. Hướng Dẫn Cập Nhật API Hiện Tại

### 6.1 Bước 1: Cập Nhật Controller

```java
@RestController
@RequestMapping("/api/v1/resource")
@RequiredArgsConstructor
@Slf4j
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    @RequireRole({"ADMIN", "MANAGER"})
    public ApiResponse<PageResponse<ResourceDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        log.info("Getting resources, page: {}, pageSize: {}", page, pageSize);
        
        PaginationRequest paginationRequest = PaginationRequest.builder()
            .page(page)
            .pageSize(pageSize)
            .build();
        
        PageResponse<ResourceDTO> result = resourceService.getAllPaginated(paginationRequest);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @RequireRole({"ADMIN", "MANAGER", "USER"})
    public ApiResponse<ResourceDTO> getById(@PathVariable Long id) {
        log.info("Getting resource with ID: {}", id);
        ResourceDTO result = resourceService.getById(id);
        return ApiResponse.success(result);
    }

    @PostMapping
    @RequireRole({"ADMIN"})
    public ApiResponse<ResourceDTO> create(@RequestBody CreateResourceRequest request) {
        log.info("Creating new resource");
        ResourceDTO result = resourceService.create(request);
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN"})
    public ApiResponse<ResourceDTO> update(
            @PathVariable Long id,
            @RequestBody UpdateResourceRequest request) {
        log.info("Updating resource with ID: {}", id);
        ResourceDTO result = resourceService.update(id, request);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN"})
    public ApiResponse<Void> delete(@PathVariable Long id) {
        log.info("Deleting resource with ID: {}", id);
        resourceService.delete(id);
        return ApiResponse.<Void>builder()
            .code(ErrorCode.SUCCESS.getCode())
            .message("Resource deleted successfully")
            .build();
    }
}
```

### 6.2 Bước 2: Cập Nhật Service

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;

    @Override
    public PageResponse<ResourceDTO> getAllPaginated(PaginationRequest paginationRequest) {
        PageRequest pageRequest = PaginationUtils.createPageRequest(paginationRequest);
        
        Page<Resource> page = resourceRepository.findAll(pageRequest);
        
        return PageResponse.<ResourceDTO>builder()
            .items(page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList()))
            .pageNo(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .isLast(page.isLast())
            .build();
    }

    @Override
    public ResourceDTO getById(Long id) {
        Resource resource = resourceRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return toDTO(resource);
    }

    @Override
    public ResourceDTO create(CreateResourceRequest request) {
        // Kiểm tra validate
        if (resourceRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS);
        }
        
        Resource resource = new Resource();
        resource.setName(request.getName());
        // Thêm các trường khác
        
        Resource saved = resourceRepository.save(resource);
        return toDTO(saved);
    }

    @Override
    public void delete(Long id) {
        Resource resource = resourceRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        resourceRepository.delete(resource);
    }

    private ResourceDTO toDTO(Resource resource) {
        return ResourceDTO.builder()
            .id(resource.getId())
            // Map các trường khác
            .build();
    }
}
```

---

## 7. Testing API

### 7.1 Test với Postman

**Request:**
```
GET /api/v1/care-task/tasks?page=0&pageSize=10
Headers:
  X-User-Role: CNA
  Content-Type: application/json
```

**Response:**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "items": [...],
    "pageNo": 0,
    "pageSize": 10,
    "totalElements": 50,
    "totalPages": 5,
    "isLast": false
  }
}
```

### 7.2 Test Error Handling

**Request (Không có role):**
```
GET /api/v1/care-task/tasks
```

**Response (401 Unauthorized):**
```json
{
  "code": 1004,
  "message": "You need to log in to access this resource",
  "data": null
}
```

**Request (Role không được phép):**
```
GET /api/v1/care-task/tasks
Headers:
  X-User-Role: INVALID_ROLE
```

**Response (403 Forbidden):**
```json
{
  "code": 1005,
  "message": "You do not have permission to access this resource",
  "data": null
}
```

---

## 8. Logging

Mỗi endpoint đều có logging để theo dõi:

```java
log.info("Getting care tasks for date: {}", localDate);
log.warn("Access denied for role: {}", userRole);
log.error("Error occurred: ", exception);
```

---

## 9. Lưu ý quan trọng

1. **Role Security**: Hiện tại role được lấy từ header `X-User-Role`. Trong production, cần integrate với JWT/OAuth2
2. **Pagination**: Giới hạn pageSize tối đa là 100 để tránh quá tải
3. **Error Handling**: Luôn sử dụng `AppException` với `ErrorCode` thích hợp
4. **Logging**: Sử dụng `@Slf4j` và log các action quan trọng
5. **Validation**: Validate input trong controller bằng `@Valid` và `@RequestBody`

---

## 10. Danh sách Controllers đã chuẩn hóa

- ✅ `CareTaskController` - Quản lý công việc chăm sóc
- ✅ `VitalSignController` - Ghi nhận chỉ số sống
- ✅ `ReassessmentController` - Tái đánh giá kế hoạch chăm sóc
- ⚪ `UserController` - Cần cập nhật

---

**Cập nhật gần nhất:** 17/07/2026

