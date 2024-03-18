package com.example.crawler.domain.fastcampus;

import com.example.crawler.domain.CategoryMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FastCampusCategoryMap implements CategoryMap {
    CATEGORY001("프로그래밍", "프론트엔드 개발", "프로그래밍", "웹"),
    CATEGORY002("프로그래밍", "백엔드 개발", "프로그래밍", "웹"),
    CATEGORY003("프로그래밍", "모바일 앱 개발", "프로그래밍", "모바일"),
    CATEGORY004("프로그래밍", "DevOps/Infra", "프로그래밍", "데브옵스/인프라"),
    CATEGORY005("프로그래밍", "블록체인 개발", "프로그래밍", "블록체인"),
    CATEGORY006("프로그래밍", "게임 개발", "게임", "게임개발"),
    CATEGORY007("프로그래밍", "컴퓨터 공학/SW 엔지니어링", "프로그래밍", "컴퓨터공학"),
    CATEGORY008("데이터사이언스", "데이터분석", "데이터사이언스", "데이터분석"),
    CATEGORY009("데이터사이언스", "머신러닝", "데이터사이언스", "인공지능"),
    CATEGORY010("데이터사이언스", "데이터엔지니어링", "데이터사이언스", "데이터엔지니어링"),
    CATEGORY011("인공지능", "딥러닝", "데이터사이언스", "인공지능"),
    CATEGORY012("인공지능", "컴퓨터비전", "데이터사이언스", "인공지능"),
    CATEGORY013("인공지능", "자연어처리", "데이터사이언스", "인공지능"),
    CATEGORY014("디자인", "UX/UI", "디자인", "UX/UI"),
    CATEGORY015("디자인", "2D/그래픽/브랜딩", "디자인", "2D/그래픽/브랜딩"),
    CATEGORY016("디자인", "3D/건축", "디자인", "3D/건축"),
    CATEGORY017("영상/3D", "영상/사진", "영상/3D/애니메이션", "영상/사진"),
    CATEGORY018("영상/3D", "모션그래픽", "영상/3D/애니메이션", "모션그래픽"),
    CATEGORY019("영상/3D", "블렌더", "영상/3D/애니메이션", "블렌더"),
    CATEGORY020("영상/3D", "3D·CG", "영상/3D/애니메이션", "3D/CG"),
    CATEGORY021("일러스트", "캐릭터일러스트", "드로잉", "캐릭터일러스트"),
    CATEGORY022("일러스트", "웹툰/웹소설", "드로잉", "웹툰/웹소설"),
    CATEGORY023("일러스트", "취미드로잉", "드로잉", "취미드로잉"),
    CATEGORY024("금융/투자", "부동산", "금융/투자", "부동산"),
    CATEGORY025("금융/투자", "금융 투자 실무", "금융/투자", "금융투자실무"),
    CATEGORY026("금융/투자", "재무/회계/세무", "금융/투자", "재무/회계/세무"),
    CATEGORY027("금융/투자", "재테크/주식", "금융/투자", "재태크/주식"),
    CATEGORY028("마케팅", "디지털마케팅", "커리어", "마케팅"),
    CATEGORY029("마케팅", "콘텐츠/마케팅 전략", "커리어", "마케팅"),
    CATEGORY030("업무 생산성", "엑셀/VBA/업무자동화", "커리어", "업무생산성"),
    CATEGORY031("업무 생산성", "PPT/보고서", "커리어", "업무생산성"),
    CATEGORY032("업무 생산성", "글쓰기/카피라이팅", "커리어", "업무생산성"),
    CATEGORY033("업무 생산성", "ChatGPT 활용", "커리어", "업무생산성"),
    CATEGORY034("비즈니스/기획", "PM / PO / 기획", "커리어", "비즈니스/기획"),
    CATEGORY035("비즈니스/기획", "경영/부업/창업", "커리어", "비즈니스/기획"),
    CATEGORY036("비즈니스/기획", "커뮤니케이션/리더십", "커리어", "비즈니스/기획");

    private final String originalMainCategory;
    private final String originalSubCategory;
    private final String convertedMainCategory;
    private final String convertedSubCategory;
}
