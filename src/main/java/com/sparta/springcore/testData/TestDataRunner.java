package com.sparta.springcore.testData;

import com.sparta.springcore.dto.ItemDto;
import com.sparta.springcore.model.Product;
import com.sparta.springcore.model.UserRoleEnum;
import com.sparta.springcore.model.Users;
import com.sparta.springcore.repository.ProductRepository;
import com.sparta.springcore.repository.UserRepository;
import com.sparta.springcore.service.ItemSearchService;
import com.sparta.springcore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.sparta.springcore.service.ProductService.MIN_MY_PRICE;

@Component
public class TestDataRunner implements ApplicationRunner {

    @Autowired
    UserService userService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ItemSearchService itemSearchService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
// 테스트 Users 생성
        Users testUser1 = new Users("정국", passwordEncoder.encode("123"), "jg@sparta.com", UserRoleEnum.USER);
        Users testUser2 = new Users("제이홉", passwordEncoder.encode("123"), "hope@sparta.com", UserRoleEnum.USER);
        Users testAdminUser1 = new Users("아미", passwordEncoder.encode("123"), "army@sparta.com", UserRoleEnum.ADMIN);
        testUser1 = userRepository.save(testUser1);
        testUser2 = userRepository.save(testUser2);
        testAdminUser1 = userRepository.save(testAdminUser1);

// 테스트 Users 의 관심상품 등록
// 검색어 당 관심상품 10개 등록
        createTestData(testUser1, "신발");
        createTestData(testUser1, "과자");
        createTestData(testUser1, "키보드");
        createTestData(testUser1, "휴지");
        createTestData(testUser1, "휴대폰");
        createTestData(testUser1, "앨범");
        createTestData(testUser1, "헤드폰");
        createTestData(testUser1, "이어폰");
        createTestData(testUser1, "노트북");
        createTestData(testUser1, "무선 이어폰");
        createTestData(testUser1, "모니터");
    }

    private void createTestData(Users user, String searchWord) throws IOException {
// 네이버 쇼핑 API 통해 상품 검색
        List<ItemDto> itemDtoList = itemSearchService.getItems(searchWord);

        List<Product> productList = new ArrayList<>();

        for (ItemDto itemDto : itemDtoList) {
            Product product = new Product();
// 관심상품 저장 사용자
            product.setUserId(user.getId());
// 관심상품 정보
            product.setTitle(itemDto.getTitle());
            product.setLink(itemDto.getLink());
            product.setImage(itemDto.getImage());
            product.setLprice(itemDto.getLprice());

// 희망 최저가 랜덤값 생성
// 최저 (100원) ~ 최대 (상품의 현재 최저가 + 10000원)
            int myPrice = getRandomNumber(MIN_MY_PRICE, itemDto.getLprice() + 10000);
            product.setMyprice(myPrice);

            productList.add(product);
        }

        productRepository.saveAll(productList);
    }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}