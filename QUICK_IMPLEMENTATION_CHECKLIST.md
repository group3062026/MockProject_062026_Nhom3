# Quick Implementation Checklist

## Danh sách kiểm tra nhanh cho việc chuẩn hóa API

---

## 1. Chuẩn bị Môi Trường

- [x] Thêm `spring-boot-starter-aop` dependency vào `pom.xml`
- [x] Tạo file `RequireRole.java` annotation
- [x] Tạo file `RoleCheckAspect.java` 
- [x] Tạo file `PaginationRequest.java`
- [x] Tạo file `PaginationUtils.java`

---

## 2. Chuẩn hóa Service Layer

### Cho mỗi Service/ServiceImpl:

- [ ] Thay thế `EntityNotFoundException` bằng `AppException` với `ErrorCode` thích hợp
- [ ] Import `com.nguyenquyen.mockproject_062026_group3.exception.AppException`
- [ ] Import `com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode`
- [ ] Xóa import `jakarta.persistence.EntityNotFoundException`
- [ ] Thêm `@Slf4j` annotation nếu chưa có
- [ ] Thêm logging cho các operation quan trọng

**Ví dụ thay thế:**
```java
// Trước
throw new EntityNotFoundException("Resource not found");

// Sau
throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
```

---

## 3. Chuẩn hóa Controller Layer

### Cho mỗi Controller:

#### 3.1 Import cần thiết
- [ ] Import `RequireRole` annotation
- [ ] Import `Slf4j` annotation
- [ ] Import `RequiredArgsConstructor` từ lombok

#### 3.2 Class Level
```java
@RestController
@RequestMapping("/api/v1/resource")
@RequiredArgsConstructor
@Slf4j
public class ResourceController {
    
    private final ResourceService resourceService;
```

- [ ] Thêm `@RequiredArgsConstructor` thay vì `@Autowired`
- [ ] Thêm `@Slf4j` annotation
- [ ] Sử dụng `private final` thay vì `@Autowired`

#### 3.3 Mỗi GET/Retrieve Method
```java
@GetMapping
@RequireRole({"ADMIN", "MANAGER"})
public ApiResponse<PageResponse<ResourceDTO>> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int pageSize) {
    
    log.info("Getting all resources, page: {}, pageSize: {}", page, pageSize);
    
    PaginationRequest paginationRequest = PaginationRequest.builder()
        .page(page)
        .pageSize(pageSize)
        .build();
    
    PageResponse<ResourceDTO> result = resourceService.getAllPaginated(paginationRequest);
    return ApiResponse.success(result);
}
```

- [ ] Thêm `@RequireRole({role1, role2})` annotation
- [ ] Thêm logging bằng `log.info(...)`
- [ ] Sử dụng `ApiResponse.success(data)` helper
- [ ] Thêm pagination support nếu cần

#### 3.4 Mỗi POST/CREATE Method
```java
@PostMapping
@RequireRole({"ADMIN"})
public ApiResponse<ResourceDTO> create(
        @RequestBody CreateResourceRequest request) {
    
    log.info("Creating new resource");
    ResourceDTO result = resourceService.create(request);
    return ApiResponse.success(result);
}
```

- [ ] Thêm `@RequireRole` annotation
- [ ] Thêm `@RequestBody` annotation
- [ ] Thêm logging
- [ ] Return `ApiResponse.success(result)`

#### 3.5 Mỗi PUT/UPDATE Method
```java
@PutMapping("/{id}")
@RequireRole({"ADMIN"})
public ApiResponse<ResourceDTO> update(
        @PathVariable Long id,
        @RequestBody UpdateResourceRequest request) {
    
    log.info("Updating resource with ID: {}", id);
    ResourceDTO result = resourceService.update(id, request);
    return ApiResponse.success(result);
}
```

- [ ] Thêm `@RequireRole` annotation
- [ ] Thêm `@PathVariable` cho ID
- [ ] Thêm logging
- [ ] Return `ApiResponse.success(result)`

#### 3.6 Mỗi DELETE Method
```java
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
```

- [ ] Thêm `@RequireRole` annotation
- [ ] Thêm logging
- [ ] Return `ApiResponse` với success code
- [ ] Ghi message thành công

---

## 4. Danh sách Error Codes Phổ biến

Khi xảy ra lỗi, sử dụng error code phù hợp:

| Tình Huống | Error Code |
|-----------|-----------|
| Resource không tìm thấy | `ErrorCode.RESOURCE_NOT_FOUND` |
| Resource đã tồn tại | `ErrorCode.RESOURCE_ALREADY_EXISTS` |
| User không tìm thấy | `ErrorCode.USER_NOT_FOUND` |
| Role không có quyền | `ErrorCode.FORBIDDEN` (1005) |
| Chưa đăng nhập | `ErrorCode.UNAUTHORIZED` (1004) |
| Validation error | `ErrorCode.VALIDATION_ERROR` |
| Care plan không tìm thấy | `ErrorCode.CARE_PLAN_NOT_FOUND` |
| Vital sign không tìm thấy | `ErrorCode.VITAL_SIGN_NOT_FOUND` |

---

## 5. Testing Checklist

### Cho mỗi Endpoint:

- [ ] Test GET với role được phép → 200 OK
- [ ] Test GET với role không được phép → 403 FORBIDDEN
- [ ] Test GET mà không có role header → 401 UNAUTHORIZED
- [ ] Test GET với page=0, pageSize=10 → Trả về đúng phân trang
- [ ] Test GET với page vượt quá tổng trang → Xử lý gracefully
- [ ] Test POST/PUT/DELETE với valid data → 200 OK
- [ ] Test POST/PUT/DELETE với invalid ID → 404 NOT_FOUND
- [ ] Test POST/PUT/DELETE với duplicate data → 409 CONFLICT

### Postman/Curl Commands:

```bash
# Test GET với role
curl -X GET http://localhost:8080/api/v1/resources \
  -H "X-User-Role: ADMIN"

# Test GET với phân trang
curl -X GET "http://localhost:8080/api/v1/resources?page=0&pageSize=10"

# Test GET mà không có role
curl -X GET http://localhost:8080/api/v1/resources

# Test POST
curl -X POST http://localhost:8080/api/v1/resources \
  -H "X-User-Role: ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"name": "New Resource"}'

# Test DELETE
curl -X DELETE http://localhost:8080/api/v1/resources/1 \
  -H "X-User-Role: ADMIN"
```

---

## 6. Roles Mapping

Gán role phù hợp dựa trên quyền:

```java
// Chỉ ADMIN
@RequireRole({"ADMIN"})

// ADMIN + MANAGER
@RequireRole({"ADMIN", "MANAGER"})

// ADMIN + MANAGER + NURSE
@RequireRole({"ADMIN", "MANAGER", "NURSE"})

// Tất cả roles
@RequireRole({"ADMIN", "MANAGER", "NURSE", "CNA"})

// Không có @RequireRole (Public)
// Không cần ghi gì
```

---

## 7. Logging Standards

Thêm logging cho:

- [ ] Info: Mỗi method entry point
- [ ] Info: Kết quả thành công
- [ ] Warn: Khi bị từ chối quyền
- [ ] Error: Khi xảy ra exception

```java
log.info("Getting resource with ID: {}", id);
log.warn("Access denied for role: {}", userRole);
log.error("Error processing resource: ", exception);
```

---

## 8. Danh Sách Controllers Cần Chuẩn hóa

### Đã chuẩn hóa ✅
- [x] CareTaskController
- [x] VitalSignController
- [x] ReassessmentController

### Cần chuẩn hóa ⚪
- [ ] UserController
- [ ] (Thêm các controller khác ở đây)

---

## 9. Documentation

- [x] API_STANDARDS.md - Tài liệu tổng quát
- [x] ROLE_BASED_ACCESS.md - Hướng dẫn RBAC
- [x] PAGINATION_GUIDE.md - Hướng dẫn Pagination
- [x] QUICK_IMPLEMENTATION_CHECKLIST.md - File này

---

## 10. Build & Deploy

- [ ] Chạy `mvn clean install` để build lại
- [ ] Không có compile errors
- [ ] Không có warnings quan trọng
- [ ] Test local trước khi push

```bash
cd E:\Test\mock-project-backend\MockProject_062026_Nhom3
mvn clean install
```

---

## 11. Common Mistakes to Avoid

❌ **Sai:**
```java
// Ném EntityNotFoundException
throw new EntityNotFoundException("Not found");

// Không ghi log
public void getResource(Long id) { }

// Không check role
@GetMapping
public ApiResponse<?> getResource() { }

// Hardcode error message
throw new AppException("Error message");
```

✅ **Đúng:**
```java
// Ném AppException với ErrorCode
throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);

// Ghi log
log.info("Getting resource with ID: {}", id);

// Check role
@GetMapping
@RequireRole({"ADMIN", "MANAGER"})
public ApiResponse<?> getResource() { }

// Sử dụng ErrorCode
throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
```

---

## 12. Important Files

| File | Vị Trí | Mục Đích |
|------|--------|---------|
| RequireRole.java | common/ | Annotation cho role checking |
| RoleCheckAspect.java | common/ | Aspect xử lý role check |
| PaginationRequest.java | common/ | Request model cho pagination |
| PaginationUtils.java | common/ | Utility cho pagination |
| ApiResponse.java | common/ | Response wrapper |
| PageResponse.java | common/ | Response pagination |
| ErrorCode.java | exception/ | Enum định nghĩa error codes |
| AppException.java | exception/ | Custom exception |
| GlobalExceptionHandler.java | exception/ | Handler xử lý exception |

---

## 13. Next Steps

1. **Immediate (Ngay):**
   - [ ] Build project và test
   - [ ] Verify 3 controllers đã chuẩn hóa
   - [ ] Test role checking với Postman

2. **Short-term (Tuần này):**
   - [ ] Chuẩn hóa UserController
   - [ ] Chuẩn hóa các controllers còn lại
   - [ ] Write unit tests

3. **Medium-term (Tháng này):**
   - [ ] Integrate JWT authentication
   - [ ] Migrate from header-based role to JWT-based
   - [ ] Add comprehensive API documentation

4. **Long-term (Future):**
   - [ ] Implement OAuth2/OIDC
   - [ ] Add API rate limiting
   - [ ] Add audit logging
   - [ ] Add caching layer

---

**Cập nhật gần nhất:** 17/07/2026
**Phiên bản:** 1.0

