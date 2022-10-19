package com.sparta.springcore.controller;


import com.sparta.springcore.dto.ProductMypriceRequestDto;
import com.sparta.springcore.dto.ProductRequestDto;
import com.sparta.springcore.model.ApiUseTime;
import com.sparta.springcore.model.Product;
import com.sparta.springcore.model.UserRoleEnum;
import com.sparta.springcore.model.Users;
import com.sparta.springcore.repository.ApiUseTimeRepository;
import com.sparta.springcore.security.UserDetailsImpl;
import com.sparta.springcore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController // JSON으로 데이터를 주고받음을 선언합니다.
public class ProductController {

    private final ProductService productService;
    private final ApiUseTimeRepository apiUseTimeRepository;

    @Autowired
    public ProductController(ProductService productService,ApiUseTimeRepository apiUseTimeRepository) {
        this.productService = productService;
        this.apiUseTimeRepository = apiUseTimeRepository;
    }

    // 신규 상품 등록
    @PostMapping("/api/products")
    public Product createProduct(@RequestBody ProductRequestDto requestDto,
                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
// 측정 시작 시간
        long startTime = System.currentTimeMillis();

        try {
// 로그인 되어 있는 회원 테이블의 ID
            Long userId = userDetails.getUser().getId();

            Product product = productService.createProduct(requestDto, userId);

// 응답 보내기
            return product;
        } finally {
// 측정 종료 시간
            long endTime = System.currentTimeMillis();
// 수행시간 = 종료 시간 - 시작 시간
            long runTime = endTime - startTime;

// 로그인 회원 정보
            Users loginUser = userDetails.getUser();

// API 사용시간 및 DB 에 기록
            ApiUseTime apiUseTime = apiUseTimeRepository.findByUser(loginUser)
                    .orElse(null);
            if (apiUseTime == null) {
// 로그인 회원의 기록이 없으면
                apiUseTime = new ApiUseTime(loginUser, runTime);
            } else {
// 로그인 회원의 기록이 이미 있으면
                apiUseTime.addUseTime(runTime);
            }

            System.out.println("[API Use Time] Username: " + loginUser.getUsername() + ", Total Time: " + apiUseTime.getTotalTime() + " ms");
            apiUseTimeRepository.save(apiUseTime);
        }
    }

    // 설정 가격 변경
    @PutMapping("/api/products/{id}")
    public Long updateProduct(@PathVariable Long id, @RequestBody ProductMypriceRequestDto requestDto) {
        Product product = productService.updateProduct(id, requestDto);

// 응답 보내기 (업데이트된 상품 id)
        return product.getId();
    }

    // 로그인한 회원이 등록한 관심 상품 조회
    @GetMapping("/api/products")
    public Page<Product> getProducts(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("isAsc") boolean isAsc,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
// 로그인 되어 있는 회원 테이블의 ID
        Long userId = userDetails.getUser().getId();
        page = page - 1;

        return productService.getProducts(userId, page, size, sortBy, isAsc);
    }

    // (관리자용) 전체 상품 조회
    @Secured(UserRoleEnum.Authority.ADMIN)
    @GetMapping("/api/admin/products")
    public Page<Product> getAllProducts(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("isAsc") boolean isAsc
    ) {
        page = page - 1;
        return productService.getAllProducts(page, size, sortBy, isAsc);
    }

    // 상품에 폴더 추가
    @PostMapping("/api/products/{productId}/folder")
    public Long addFolder(
            @PathVariable Long productId,
            @RequestParam Long folderId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Users user = userDetails.getUser();
        Product product = productService.addFolder(productId, folderId, user);
        return product.getId();
    }
}