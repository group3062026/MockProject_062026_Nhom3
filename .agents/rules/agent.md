---
trigger: always_on
---

# AGENTS.md — Quy tắc phát triển doanh nghiệp
## 1. Phạm vi
Áp dụng cho toàn bộ repository:
- Backend: Java 21, Spring Boot, REST API.
- Frontend: Next.js, React, TypeScript.
- Database, build tool và package manager: theo cấu hình hiện có của dự án.
Thứ tự ưu tiên khi có xung đột:
1. Yêu cầu trực tiếp của người dùng.
2. `AGENTS.md` gần thư mục đang sửa nhất.
3. Kiến trúc, ADR, contract và convention trong repository.
4. Tệp này.
Không tự ý đổi kiến trúc, framework, database, package manager, cơ chế xác thực hoặc nâng major dependency.
## 2. Nguyên tắc làm việc
- Hành xử như kỹ sư phần mềm cấp cao.
- Ưu tiên đúng nghiệp vụ, bảo mật, dễ bảo trì, dễ kiểm thử và tương thích ngược.
- Chỉ thay đổi phần cần thiết; không refactor hoặc format hàng loạt ngoài phạm vi.
- Tìm và tuân theo implementation tương tự đã có trong repository.
- Không suy đoán API, schema hoặc convention khi có thể kiểm tra trực tiếp.
- Mọi thay đổi nghiệp vụ phải có test tương ứng.
- Không tuyên bố build hoặc test thành công nếu chưa thực sự chạy.
## 3. Cách Codex phản hồi
- Trả lời bằng tiếng Việt, trừ khi được yêu cầu khác.
- Trước mọi đoạn code, lệnh terminal, cấu hình hoặc diff, viết một chú thích ngắn nêu rõ mục đích.
- Chú thích tối đa hai câu, rõ ràng và không dài dòng.
- Ghi rõ đường dẫn tệp khi trình bày thay đổi ở nhiều tệp.
- Chỉ hiển thị phần mã cần thiết, không dán toàn bộ tệp nếu không cần.
- Không dùng placeholder mơ hồ trong kết quả hoàn chỉnh.
- Cuối tác vụ phải nêu: tệp đã đổi, hành vi đã đổi, kiểm tra đã chạy và rủi ro còn lại.
Trong source code, chỉ viết comment để giải thích lý do, ràng buộc nghiệp vụ hoặc quyết định khó hiểu; không comment lại điều tên hàm và biến đã thể hiện.
## 4. Dependency và công cụ
- Bắt buộc dùng Java 21.
- Giữ nguyên phiên bản Spring Boot, Next.js, React và TypeScript đã được ghim.
- Không thêm dependency nếu thư viện chuẩn hoặc dependency hiện có đáp ứng được.
- Không sửa lockfile bằng tay.
- Không trộn Maven với Gradle hoặc nhiều package manager JavaScript.
- Ưu tiên wrapper và package manager của dự án: `mvnw`, `gradlew`, npm, pnpm, Yarn hoặc Bun theo lockfile.
- Dependency mới phải có lý do, license phù hợp và không có lỗ hổng nghiêm trọng đã biết.
## 5. Kiến trúc
- Tôn trọng cấu trúc hiện tại; không áp dụng pattern chỉ để tăng số lớp.
- Ưu tiên phân chia theo module hoặc feature, trách nhiệm rõ ràng và không phụ thuộc vòng.
- Domain không phụ thuộc trực tiếp web, persistence hoặc framework khi có thể tránh.
- Không truy cập repository hoặc bảng của module khác nếu kiến trúc không cho phép.
- Không đưa business logic vào controller, React component hoặc utility chung.
Cấu trúc khuyến nghị khi tạo mới:
- `backend/`: Java 21 và Spring Boot.
- `frontend/`: Next.js và TypeScript.
- `docs/`: ADR, API contract và tài liệu vận hành.
- `infra/`: container, CI/CD và hạ tầng.
## 6. Java 21
- Ưu tiên immutable object, kiểu dữ liệu mạnh và composition.
- Dùng `record` cho DTO hoặc value object bất biến khi phù hợp.
- Dùng `Optional` chủ yếu cho return type có thể vắng mặt.
- Không trả `null` cho collection; trả collection rỗng.
- Không dùng raw type, `System.out`, static mutable state hoặc field injection.
- Không bắt `Exception` hoặc `Throwable` chung chung nếu không có chiến lược xử lý rõ ràng.
- Không bỏ qua exception và không dùng exception cho luồng nghiệp vụ bình thường.
- Không tạo thread thủ công trong business code.
- Chỉ dùng virtual thread khi dependency, transaction, pool và observability tương thích.
- Tên class dùng `PascalCase`; method và biến dùng `camelCase`; constant dùng `UPPER_SNAKE_CASE`.
- Method ngắn, một trách nhiệm; tránh parameter list dài và inheritance sâu.
- Không dùng Lombok nếu dự án chưa dùng; tránh `@Data` cho entity.
## 7. Spring Boot backend
### API và controller
- Controller chỉ xử lý HTTP, validation, authorization boundary, gọi use case và mapping response.
- Không trả JPA entity trực tiếp; dùng request/response DTO.
- Dùng Bean Validation cho dữ liệu đầu vào.
- Chuẩn hóa lỗi bằng `ProblemDetail` hoặc format chung của dự án.
- Không để lộ stack trace, SQL, secret hoặc cấu trúc nội bộ trong response.
- Dùng đúng HTTP method, status code, pagination và giới hạn page size.
- Timestamp dùng ISO 8601, ưu tiên UTC.
- Tiền tệ dùng kiểu chính xác, không dùng `float` hoặc `double`.
- API public phải đồng bộ với OpenAPI contract nếu dự án sử dụng OpenAPI.
### Application và domain
- Một use case thể hiện một mục tiêu nghiệp vụ rõ ràng.
- Domain invariant phải được bảo vệ ở backend, không chỉ ở giao diện.
- Entity và value object phải tự duy trì trạng thái hợp lệ.
- Ưu tiên constructor injection; dependency bắt buộc nên là `final`.
- Không dùng `ApplicationContext` như service locator.
### Transaction và persistence
- Đặt transaction tại application/service boundary, không đặt tại controller.
- Transaction phải ngắn; không giữ transaction khi gọi dịch vụ bên ngoài.
- Dùng migration có version; không sửa migration đã chạy ở môi trường dùng chung.
- Tránh N+1 query, unbounded query và load toàn bộ bảng vào bộ nhớ.
- Không ghép SQL từ input người dùng; sort và filter phải được whitelist.
- Repository chỉ xử lý persistence, không chứa business workflow.
- Có index cho truy vấn quan trọng dựa trên nhu cầu thực tế.
### Tích hợp bên ngoài
- Mọi HTTP client phải có timeout rõ ràng.
- Retry chỉ dùng cho thao tác phù hợp và phải có backoff, giới hạn lần thử.
- Phân biệt lỗi timeout, xác thực, giới hạn tần suất, lỗi client và lỗi server.
- Không log token, credential hoặc payload nhạy cảm.
- Dùng idempotency cho thao tác có nguy cơ thực hiện lặp.
## 8. Bảo mật
- Mặc định từ chối truy cập; chỉ cấp quyền tối thiểu cần thiết.
- Kiểm tra authorization ở backend cho từng tài nguyên, không chỉ ẩn nút trên UI.
- Không hard-code secret; dùng biến môi trường hoặc secret manager.
- Không tự viết thuật toán mã hóa hoặc xác thực khi có thư viện chuẩn.
- Validate input, encode output và chống injection, XSS, CSRF, SSRF theo ngữ cảnh.
- Cookie nhạy cảm phải dùng `HttpOnly`, `Secure` và `SameSite` phù hợp.
- CORS chỉ cho phép origin, method và header cần thiết.
- Không ghi mật khẩu, token, PII hoặc dữ liệu nhạy cảm vào log.
- Dependency và container image phải được quét lỗ hổng trong CI khi có thể.
## 9. Logging và observability
- Dùng structured logging và correlation/trace ID.
- Log đúng mức; không dùng `ERROR` cho tình huống nghiệp vụ bình thường.
- Có metric và trace cho luồng quan trọng khi hạ tầng hỗ trợ.
- Health check không được làm lộ dữ liệu nhạy cảm.
- Audit log phải tách biệt với log kỹ thuật khi nghiệp vụ yêu cầu.
## 10. Kiểm thử backend
- Unit test cho domain và business logic.
- Integration test cho database, security, transaction và external adapter quan trọng.
- Test API ở boundary, gồm cả lỗi validation và authorization.
- Test phải độc lập, xác định và không phụ thuộc thứ tự chạy.
- Không mock entity hoặc value object đơn giản; mock tại external boundary.
- Bug fix phải có regression test khi khả thi.
- Test tên theo hành vi, không theo chi tiết triển khai.
## 11. Next.js và TypeScript
- Bật TypeScript strict; không dùng `any` nếu không có lý do rõ ràng.
- Tôn trọng App Router hoặc Pages Router hiện có; không trộn tùy tiện.
- Mặc định dùng Server Component; chỉ thêm `'use client'` khi cần state, effect hoặc browser API.
- Component nhỏ, một trách nhiệm; logic nghiệp vụ đặt trong feature/service/hook phù hợp.
- Không gọi API rải rác; dùng API client tập trung, typed và xử lý lỗi thống nhất.
- Không đưa secret vào biến môi trường public hoặc client bundle.
- Validate dữ liệu nhận từ API tại boundary khi dữ liệu không đáng tin cậy.
- Không lưu token nhạy cảm trong `localStorage` nếu có phương án cookie an toàn hơn.
- Không dùng `dangerouslySetInnerHTML` trừ khi nội dung đã được sanitize.
- State cục bộ dùng `useState` hoặc `useReducer`; không thêm global state library nếu chưa cần.
- Form phải hiển thị lỗi rõ ràng và backend vẫn phải validate lại.
- Tái sử dụng design system hiện có; không tạo style trùng lặp tùy tiện.
- Đảm bảo accessibility theo WCAG 2.2 AA: semantic HTML, label, keyboard, focus và contrast.
- Tối ưu ảnh, font, bundle và data fetching; chỉ memo hóa sau khi xác định có lợi.
## 12. Kiểm thử frontend
- Unit test cho logic thuần và utility quan trọng.
- Component test theo hành vi người dùng, không phụ thuộc implementation detail.
- Integration test cho form, data fetching, error state và authorization-sensitive UI.
- E2E test cho luồng nghiệp vụ quan trọng.
- Bao phủ loading, empty, success, validation error và server error.
- Không lạm dụng snapshot lớn.
## 13. Contract frontend–backend
- Không khai báo thủ công nhiều bản type cho cùng một API nếu có thể sinh từ contract.
- Giữ thống nhất tên field, enum, pagination, date-time và error format.
- Breaking change phải có versioning hoặc kế hoạch chuyển đổi.
- Không để frontend phụ thuộc vào message lỗi tự do để điều khiển logic.
## 14. Dữ liệu và hiệu năng
- Thu thập và lưu dữ liệu tối thiểu cần thiết.
- Không dùng dữ liệu production trong test nếu chưa được ẩn danh.
- Cache phải có chiến lược invalidation, TTL và giới hạn dung lượng.
- Tối ưu dựa trên profiling, metric hoặc truy vấn thực tế; không tối ưu theo cảm tính.
- Có timeout, rate limit, pagination và backpressure tại boundary phù hợp.
- Xử lý race condition bằng constraint, locking, optimistic concurrency hoặc idempotency theo nhu cầu.
## 15. Messaging và bất đồng bộ
- Message phải có schema/version rõ ràng và tương thích ngược khi cần.
- Consumer phải idempotent.
- Có chiến lược retry, dead-letter và quan sát lỗi.
- Không giả định exactly-once nếu hạ tầng không bảo đảm.
- Chỉ đánh dấu hoàn tất sau khi side effect cần thiết đã thành công.
## 16. Build, CI/CD và Git
Quality gate tối thiểu:
- Backend: format, compile, unit test và integration test liên quan.
- Frontend: format, lint, type-check, test và production build khi cần.
- Quét secret, dependency vulnerability và container image nếu pipeline hỗ trợ.
Quy tắc Git:
- Commit nhỏ, tập trung và có thông điệp rõ ràng.
- Không commit secret, file sinh tự động không cần thiết hoặc thay đổi ngoài phạm vi.
- Không force push, rebase, reset hoặc xóa branch khi chưa được yêu cầu.
- PR phải nêu mục tiêu, thay đổi chính, cách kiểm thử, migration, rủi ro và rollback khi cần.
## 17. Tài liệu
Cập nhật tài liệu khi thay đổi:
- API public hoặc contract.
- Cấu hình, biến môi trường hoặc cách triển khai.
- Database migration hoặc quy trình vận hành.
- Kiến trúc quan trọng; dùng ADR cho quyết định có ảnh hưởng dài hạn.
Không để tài liệu mô tả hành vi khác với code.
## 18. Các hành vi bị cấm
- Hard-code secret, token, mật khẩu hoặc dữ liệu production.
- Bỏ qua validation, authorization hoặc lỗi compiler/linter.
- Tắt test để pipeline xanh.
- Dùng `any`, `@SuppressWarnings` hoặc ignore rule mà không có lý do.
- Trả entity trực tiếp qua API.
- Ghép SQL từ input người dùng.
- Gọi dịch vụ ngoài mà không có timeout.
- Thay đổi API, schema hoặc dependency lớn ngoài phạm vi yêu cầu.
- Xóa dữ liệu, migration, branch hoặc tài nguyên mà chưa có chỉ dẫn rõ ràng.
## 19. Definition of Done
Chỉ coi là hoàn thành khi:
- Đúng yêu cầu và không phá vỡ hành vi ngoài phạm vi.
- Code tuân thủ convention của repository.
- Có test phù hợp và các kiểm tra liên quan đã chạy thành công.
- Không có secret, log nhạy cảm hoặc cảnh báo nghiêm trọng mới.
- API, migration, cấu hình và tài liệu đã được cập nhật khi cần.
- Codex báo cáo trung thực phần đã làm, phần chưa kiểm tra và rủi ro còn lại.
## 20. Nguyên tắc cuối cùng
Ưu tiên theo thứ tự:
1. Tính đúng đắn.
2. Bảo mật.
3. Khả năng bảo trì.
4. Khả năng kiểm thử.
5. Tính nhất quán.
6. Hiệu năng đã được đo lường.
7. Tốc độ triển khai.
Khi yêu cầu chưa rõ, chọn giải pháp ít rủi ro và ít phá vỡ nhất; ghi rõ giả định thay vì tự ý mở rộng phạm vi.