# Role-Based Access Control (RBAC) Guide

## Giới thiệu

Role-Based Access Control (RBAC) là một cơ chế bảo mật để kiểm soát quyền truy cập vào các endpoint API dựa trên role của user.

---

## 1. Cách Hoạt Động

### 1.1 Architecture

```
Request with Header (X-User-Role)
    ↓
@RequireRole Annotation trên method
    ↓
RoleCheckAspect (AOP)
    ↓
Kiểm tra role có trong danh sách được phép?
    ↓
✓ Có → Cho phép thực thi method
✗ Không → Ném AppException(FORBIDDEN)
```

### 1.2 Thành phần chính

#### RequireRole.java
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    String[] value() default {};
}
```

#### RoleCheckAspect.java
- Sử dụng Spring AOP để intercept method có @RequireRole
- Kiểm tra role từ HTTP header `X-User-Role`
- Ném exception nếu role không được phép

---

## 2. Cách Sử Dụng

### 2.1 Thêm @RequireRole vào method

```java
@GetMapping("/tasks")
@RequireRole({"CNA", "NURSE", "MANAGER", "ADMIN"})
public ApiResponse<CareTaskResponseDTO> getCareTask() {
    // Logic xử lý
}
```

### 2.2 Gửi Request

```bash
# Cách 1: Sử dụng curl
curl -X GET http://localhost:8080/api/v1/care-task/tasks \
  -H "X-User-Role: CNA"

# Cách 2: Sử dụng Postman
# Method: GET
# URL: http://localhost:8080/api/v1/care-task/tasks
# Headers:
#   X-User-Role: CNA
```

### 2.3 Response

**Khi role được phép:**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    // Dữ liệu
  }
}
```

**Khi role không được phép:**
```json
{
  "code": 1005,
  "message": "You do not have permission to access this resource",
  "data": null
}
```

**Khi không có role header:**
```json
{
  "code": 1004,
  "message": "You need to log in to access this resource",
  "data": null
}
```

---

## 3. Danh sách Roles

| Role | Mô Tả | Quyền hạn |
|------|-------|----------|
| ADMIN | Quản trị viên | Toàn quyền |
| MANAGER | Quản lý/Giám sát | Duyệt, tái đánh giá |
| NURSE | Y tá/Điều dưỡng | Tạo, chỉnh sửa |
| CNA | Certified Nursing Assistant | Xem, ghi nhận |

---

## 4. Quy Tắc Gán Role

### 4.1 Endpoint công khai (cho phép tất cả)
```java
@GetMapping("/public")
// Không có @RequireRole annotation
public ApiResponse<?> publicEndpoint() { }
```

### 4.2 Endpoint yêu cầu 1 role cụ thể
```java
@DeleteMapping("/{id}")
@RequireRole({"ADMIN"})
public ApiResponse<?> deleteResource(@PathVariable Long id) { }
```

### 4.3 Endpoint cho phép nhiều roles
```java
@GetMapping
@RequireRole({"ADMIN", "MANAGER", "NURSE"})
public ApiResponse<?> getResources() { }
```

---

## 5. Bảng Quyền Theo Role

| Endpoint | ADMIN | MANAGER | NURSE | CNA |
|----------|-------|---------|-------|-----|
| GET /tasks | ✓ | ✓ | ✓ | ✓ |
| GET /tasks/{id} | ✓ | ✓ | ✓ | ✓ |
| PATCH /tasks/{id} | ✓ | ✓ | ✓ | ✓ |
| POST /reassessment | ✓ | ✓ | ✓ | ✗ |
| DELETE /resource/{id} | ✓ | ✗ | ✗ | ✗ |

---

## 6. Lỗi Thường Gặp

### 6.1 Lỗi 1004 - UNAUTHORIZED
```
Nguyên nhân: Không gửi header X-User-Role
Giải pháp: Thêm header X-User-Role vào request
```

### 6.2 Lỗi 1005 - FORBIDDEN
```
Nguyên nhân: Role không nằm trong danh sách được phép
Giải pháp: Kiểm tra role, yêu cầu admin cấp quyền hoặc sử dụng role khác
```

### 6.3 Method không có @RequireRole
```
Hành vi: Endpoint sẽ được truy cập bình thường mà không cần kiểm tra role
Lưu ý: Chỉ nên dùng cho endpoint public
```

---

## 7. Migration từ EntityNotFoundException

### 7.1 Trước (Cũ)
```java
throw new EntityNotFoundException("Not found");
// → Generic error message, không theo error code format
```

### 7.2 Sau (Mới)
```java
throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
// → Chuẩn hóa error code, dễ xử lý client-side
```

---

## 8. Security Best Practices

### ⚠️ Lưu ý bảo mật

1. **Hiện tại**: Role từ HTTP header (KHÔNG AN TOÀN cho production)
   ```java
   String userRole = request.getHeader("X-User-Role");
   ```

2. **Production**: Nên sử dụng JWT Token
   ```java
   // Lấy role từ JWT Token
   String userRole = extractRoleFromJWT(request);
   ```

3. **Hoặc**: Sử dụng Spring Security
   ```java
   String userRole = SecurityContextHolder.getContext()
       .getAuthentication().getAuthorities();
   ```

---

## 9. Ví dụ Thực Tế

### 9.1 CareTaskController

```java
@RestController
@RequestMapping("api/v1/care-task")
@RequiredArgsConstructor
public class CareTaskController {
    
    private final CareTaskService careTaskService;
    
    // Cả CNA, Nurse, Manager, Admin đều có thể xem
    @GetMapping("/tasks")
    @RequireRole({"CNA", "NURSE", "MANAGER", "ADMIN"})
    public ApiResponse<CareTaskResponseDTO> getCareTask() {
        return ApiResponse.success(careTaskService.getCareTasks(LocalDate.now()));
    }
    
    // Cả CNA, Nurse, Manager, Admin đều có thể cập nhật
    @PatchMapping("/statustasks/{taskId}")
    @RequireRole({"CNA", "NURSE", "MANAGER", "ADMIN"})
    public ApiResponse<Void> updateTaskStatus(@PathVariable Long taskId) {
        // Update logic
        return ApiResponse.success(null);
    }
}
```

### 9.2 ReassessmentController

```java
@RestController
@RequestMapping("/api/v1/reassessment")
@RequiredArgsConstructor
public class ReassessmentController {
    
    // Chỉ Nurse, Manager, Admin có thể gửi tái đánh giá
    @PostMapping("/{planId}")
    @RequireRole({"NURSE", "MANAGER", "ADMIN"})
    public ApiResponse<Void> submitReassessment(@PathVariable Long planId) {
        // Submit logic
        return ApiResponse.success(null);
    }
    
    // Nurse, Manager, Admin có thể xem form
    @GetMapping("/{planId}")
    @RequireRole({"NURSE", "MANAGER", "ADMIN"})
    public ApiResponse<CarePlanReassessmentResponseDTO> getReassessmentForm(@PathVariable Long planId) {
        // Get form logic
        return ApiResponse.success(null);
    }
}
```

---

## 10. Testing RBAC

### 10.1 Test case 1: Role được phép
```bash
curl -X GET http://localhost:8080/api/v1/care-task/tasks \
  -H "X-User-Role: CNA"
  
# Expected: 200 OK
```

### 10.2 Test case 2: Role không được phép
```bash
curl -X POST http://localhost:8080/api/v1/reassessment/1 \
  -H "X-User-Role: CNA"
  
# Expected: 403 Forbidden
```

### 10.3 Test case 3: Không có role
```bash
curl -X GET http://localhost:8080/api/v1/care-task/tasks

# Expected: 401 Unauthorized
```

---

## 11. Logging & Monitoring

RoleCheckAspect ghi log tất cả quyết định kiểm tra role:

```
DEBUG: Access granted for role: CNA to method: getCareTask
WARN: Access denied for role: CNA to access submitReassessment. 
      Required roles: [NURSE, MANAGER, ADMIN]
```

---

**Cập nhật gần nhất:** 17/07/2026

