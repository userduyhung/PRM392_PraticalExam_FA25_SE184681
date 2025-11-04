package com.example.nguyenduyhung_se184681.data;

import com.example.nguyenduyhung_se184681.model.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock data với nội dung có nghĩa bằng tiếng Việt
 * Chủ đề: Công nghệ, Lập trình, Android
 */
public class MockPostData {

    public static List<Post> getMockPosts() {
        List<Post> posts = new ArrayList<>();

        // Android Development Posts
        posts.add(createPost(1, 1, "Giới thiệu về Android Studio",
                "Android Studio là IDE chính thức để phát triển ứng dụng Android. Nó cung cấp nhiều công cụ mạnh mẽ như code editor thông minh, debugger, layout editor trực quan và nhiều tính năng hỗ trợ khác giúp lập trình viên phát triển ứng dụng hiệu quả hơn."));

        posts.add(createPost(2, 1, "RecyclerView trong Android",
                "RecyclerView là widget mạnh mẽ để hiển thị danh sách dữ liệu. Nó tái sử dụng các view items đã tạo, giúp tiết kiệm bộ nhớ và tăng hiệu suất. RecyclerView kết hợp với LayoutManager và Adapter để tạo ra các danh sách linh hoạt và hiệu quả."));

        posts.add(createPost(3, 1, "Room Database - Lưu trữ dữ liệu local",
                "Room là thư viện ORM (Object Relational Mapping) giúp làm việc với SQLite dễ dàng hơn. Nó cung cấp compile-time verification của SQL queries, giảm thiểu lỗi runtime. Room gồm 3 thành phần chính: Database, DAO và Entity."));

        posts.add(createPost(4, 1, "LiveData và Observer Pattern",
                "LiveData là một lifecycle-aware observable data holder. Nó tự động cập nhật UI khi dữ liệu thay đổi và chỉ notify observers đang active. Điều này giúp tránh memory leaks và crashes do activity đã destroyed."));

        posts.add(createPost(5, 1, "MVVM Architecture Pattern",
                "MVVM (Model-View-ViewModel) là pattern kiến trúc phổ biến trong Android. Model quản lý dữ liệu, View hiển thị UI, ViewModel làm cầu nối giữa Model và View. Pattern này giúp code dễ test và maintain hơn."));

        // Java Programming Posts
        posts.add(createPost(6, 2, "Java Collections Framework",
                "Collections Framework cung cấp các cấu trúc dữ liệu như List, Set, Map. ArrayList cho phép truy cập nhanh theo index, LinkedList tốt cho việc thêm/xóa phần tử. HashMap lưu trữ key-value pairs với thời gian truy cập O(1)."));

        posts.add(createPost(7, 2, "Exception Handling trong Java",
                "Exception handling giúp xử lý lỗi gracefully. Try-catch block bắt exceptions, finally block thực thi code dù có exception hay không. Checked exceptions phải được handle, unchecked exceptions là runtime errors."));

        posts.add(createPost(8, 2, "Java Stream API",
                "Stream API giới thiệu functional programming vào Java. Nó cho phép xử lý collections một cách declarative với các operations như filter, map, reduce. Streams có thể được parallel để tăng hiệu suất xử lý dữ liệu lớn."));

        posts.add(createPost(9, 2, "Generics trong Java",
                "Generics cho phép viết code tổng quát hơn, type-safe hơn. Thay vì dùng Object, ta dùng type parameters như <T>. Điều này giúp compiler kiểm tra type errors lúc compile time thay vì runtime."));

        posts.add(createPost(10, 2, "Lambda Expressions",
                "Lambda expressions là cách ngắn gọn để implement functional interfaces. Syntax: (parameters) -> expression. Lambdas làm code ngắn gọn hơn, đặc biệt khi làm việc với Collections và Streams."));

        // Mobile App Development
        posts.add(createPost(11, 3, "Material Design Guidelines",
                "Material Design là ngôn ngữ thiết kế của Google. Nó nhấn mạnh vào animations, transitions, padding và depth effects. Material components cung cấp UI elements chuẩn như buttons, cards, dialogs giúp tạo consistent UX."));

        posts.add(createPost(12, 3, "Responsive UI Design",
                "Responsive design đảm bảo app hoạt động tốt trên mọi kích thước màn hình. Sử dụng ConstraintLayout để tạo flexible layouts, define different resources cho các screen sizes khác nhau bằng qualifiers."));

        posts.add(createPost(13, 3, "App Performance Optimization",
                "Tối ưu hiệu suất app bằng cách giảm overdraw, tối ưu layouts, sử dụng ViewHolder pattern. Profiler tools giúp phát hiện memory leaks, CPU bottlenecks. LazyLoading images và pagination cho large lists."));

        posts.add(createPost(14, 3, "Testing trong Android",
                "Unit tests kiểm tra logic code, UI tests kiểm tra giao diện. JUnit cho unit testing, Espresso cho UI testing. TDD (Test-Driven Development) giúp viết code chất lượng cao và ít bugs hơn."));

        posts.add(createPost(15, 3, "Dependency Injection với Dagger",
                "Dependency Injection giúp code loosely coupled và dễ test. Dagger tự động generate code để inject dependencies. Components định nghĩa graph, Modules cung cấp dependencies."));

        // API & Networking
        posts.add(createPost(16, 4, "RESTful API Design",
                "REST API sử dụng HTTP methods: GET để lấy data, POST để tạo mới, PUT để update, DELETE để xóa. URL endpoints nên rõ ràng, responses dùng JSON format. Status codes như 200, 404, 500 chỉ trạng thái request."));

        posts.add(createPost(17, 4, "Retrofit Library cho Android",
                "Retrofit là HTTP client mạnh mẽ. Nó convert HTTP API thành Java interfaces. Hỗ trợ async requests, automatic JSON parsing với Gson, error handling. Interceptors cho logging và authentication."));

        posts.add(createPost(18, 4, "JSON Parsing với Gson",
                "Gson convert JSON string thành Java objects và ngược lại. Dùng annotations như @SerializedName để map JSON fields. Gson.fromJson() deserialize JSON, Gson.toJson() serialize objects."));

        posts.add(createPost(19, 4, "Offline-First Architecture",
                "Offline-first apps cache data locally, sync khi có network. Single source of truth từ database, network chỉ là updater. Users có thể sử dụng app mọi lúc, data tự sync khi online."));

        posts.add(createPost(20, 4, "Authentication & Security",
                "JWT tokens cho authentication, refresh tokens cho security. HTTPS cho encrypted communication. Store sensitive data trong Encrypted Shared Preferences. Validate inputs để tránh SQL injection."));

        // Git & Version Control
        posts.add(createPost(21, 5, "Git Basics - Version Control",
                "Git là distributed version control system. git init tạo repo mới, git add stages changes, git commit lưu snapshot. Branches cho phép phát triển features độc lập mà không ảnh hưởng main code."));

        posts.add(createPost(22, 5, "Git Branching Strategy",
                "Git Flow: master branch cho production, develop cho development, feature branches cho new features. Pull requests review code trước khi merge. Tags đánh dấu releases."));

        posts.add(createPost(23, 5, "Merge vs Rebase",
                "Merge giữ nguyên commit history, tạo merge commit. Rebase rewrite history, tạo linear history. Rebase tốt cho feature branches, merge tốt cho public branches. Never rebase public branches."));

        posts.add(createPost(24, 5, "Git Conflicts Resolution",
                "Conflicts xảy ra khi 2 branches modify cùng dòng code. Git đánh dấu conflicts trong file. Resolve bằng cách chọn version nào giữ lại hoặc combine cả 2. Sau đó git add và commit."));

        posts.add(createPost(25, 5, "GitHub Collaboration",
                "Fork repository để contribute, clone về local. Create branch cho changes, push lên origin. Open pull request để merge, review code và discuss. Collaborators approve rồi merge."));

        // Continue with more posts...
        posts.add(createPost(26, 6, "Kotlin vs Java",
                "Kotlin là ngôn ngữ hiện đại hơn Java. Null safety ngăn NullPointerException, extension functions thêm tính năng cho classes. Coroutines cho async programming dễ hơn threads. Kotlin interoperable với Java."));

        posts.add(createPost(27, 6, "Kotlin Coroutines",
                "Coroutines là lightweight threads. suspend functions có thể pause và resume. launch tạo fire-and-forget coroutine, async return result. withContext switch context. Coroutines tránh callback hell."));

        posts.add(createPost(28, 6, "Data Classes trong Kotlin",
                "Data classes tự động generate equals(), hashCode(), toString(), copy(). Chỉ cần keyword data trước class. Primary constructor parameters trở thành properties. Destructuring declarations extract properties dễ dàng."));

        posts.add(createPost(29, 6, "Sealed Classes",
                "Sealed classes hạn chế inheritance hierarchy. Subclasses phải trong cùng file. Useful cho representing restricted states. when expression với sealed classes exhaustive, không cần else branch."));

        posts.add(createPost(30, 6, "Kotlin Flow",
                "Flow là cold asynchronous stream. emit() phát values, collect() nhận values. Operators như map, filter transform data. StateFlow và SharedFlow cho hot streams. Flow thay thế LiveData trong nhiều cases."));

        // UI/UX Posts
        posts.add(createPost(31, 7, "User Interface Best Practices",
                "UI phải intuitive và consistent. Dùng standard patterns users quen thuộc. Touch targets tối thiểu 48dp. Feedback ngay lập tức cho user actions. Loading indicators khi processing."));

        posts.add(createPost(32, 7, "Navigation Patterns",
                "Bottom navigation cho 3-5 top-level destinations. Navigation drawer cho nhiều destinations. Tabs cho related content. Back stack maintain navigation history. Deep links mở specific screens."));

        posts.add(createPost(33, 7, "Animations và Transitions",
                "Animations làm UI sống động. Property animations animate attributes như alpha, translation. Transition framework animate layout changes. Shared element transitions giữa screens. Duration 200-300ms cho smooth feel."));

        posts.add(createPost(34, 7, "Accessibility trong Apps",
                "Content descriptions cho images và icons. Labels cho input fields. Minimum text size 12sp. Color contrast ratio tối thiểu 4.5:1. TalkBack screen reader support. Keyboard navigation support."));

        posts.add(createPost(35, 7, "Dark Mode Implementation",
                "Dark theme giảm eye strain, tiết kiệm pin OLED. DayNight themes tự động switch. Define colors trong colors.xml, values-night cho dark variants. Test cả 2 themes thoroughly."));

        // Database Posts
        posts.add(createPost(36, 8, "SQLite Database Basics",
                "SQLite là embedded relational database. CREATE TABLE định nghĩa schema, INSERT thêm data, SELECT query data. Indexes tăng query speed. Transactions đảm bảo data consistency."));

        posts.add(createPost(37, 8, "Database Migration",
                "Migration update database schema khi app updates. Room migration strategy: fallbackToDestructiveMigration xóa data, addMigrations giữ data. Test migrations thoroughly để tránh data loss."));

        posts.add(createPost(38, 8, "Query Optimization",
                "Indexes trên frequently queried columns. Limit results với LIMIT clause. Eager loading cho related data. Avoid N+1 queries. EXPLAIN QUERY PLAN analyze query performance."));

        posts.add(createPost(39, 8, "Database Relationships",
                "One-to-One: một user có một profile. One-to-Many: một user có nhiều posts. Many-to-Many: posts có nhiều tags, tags có nhiều posts, cần junction table. Foreign keys maintain referential integrity."));

        posts.add(createPost(40, 8, "Content Providers",
                "Content Providers share data giữa apps. URI identify data, CRUD operations qua resolver. Cursor return query results. Loaders load data asynchronously. Permissions control access."));

        // Add more posts to reach 100
        for (int i = 41; i <= 100; i++) {
            int userId = ((i - 1) / 20) + 1;
            posts.add(createPost(i, userId,
                    "Bài viết số " + i + " - " + getCategoryName(userId),
                    "Đây là nội dung chi tiết của bài viết số " + i + ". Nội dung này mang tính chất minh họa và được tạo tự động. Trong thực tế, mỗi bài viết sẽ có nội dung riêng biệt, hữu ích và có giá trị với người đọc. Bài viết này thuộc chủ đề " + getCategoryName(userId) + " và cung cấp thông tin bổ ích cho người học lập trình Android."));
        }

        return posts;
    }

    private static Post createPost(int id, int userId, String title, String body) {
        Post post = new Post();
        post.setId(id);
        post.setUserId(userId);
        post.setTitle(title);
        post.setBody(body);
        post.setFavorite(false);
        post.setCategory("Category " + ((userId % 5) + 1));
        post.setImageUrl("https://picsum.photos/400/300?random=" + id);
        return post;
    }

    private static String getCategoryName(int userId) {
        switch ((userId % 5) + 1) {
            case 1: return "Android Development";
            case 2: return "Java Programming";
            case 3: return "Mobile Design";
            case 4: return "API & Network";
            case 5: return "Git & Tools";
            default: return "Technology";
        }
    }
}

